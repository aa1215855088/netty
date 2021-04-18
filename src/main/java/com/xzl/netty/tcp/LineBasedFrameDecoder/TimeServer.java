package com.xzl.netty.tcp.LineBasedFrameDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeServer {

    public static void main(String[] args) throws InterruptedException {
        //配置服务端的NIO线程租
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildHandler());
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            //优雅退出
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }


    private static class ChildHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(100));
            socketChannel.pipeline().addLast(new StringDecoder());
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }

        private static class TimeServerHandler extends ChannelHandlerAdapter {
            AtomicInteger count = new AtomicInteger(1);

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                System.out.println("receive client message:" + (String) msg + "" + count.getAndIncrement());
                //向客户端写消息
                ByteBuf writeBuf = Unpooled.copiedBuffer((LocalDateTime.now().toString()+"\n").getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(writeBuf);
            }
        }
    }
}
