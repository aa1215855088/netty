package com.xzl.netty.timeServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }

        private static class TimeServerHandler extends ChannelHandlerAdapter {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                //读取客户端信息
                ByteBuf buf = (ByteBuf) msg;
                byte[] bytes = new byte[buf.readableBytes()];
                ByteBuf byteBuf = buf.readBytes(bytes);
                String body = new String(bytes, StandardCharsets.UTF_8);
                System.out.println("receive client message:" + body);
                //向客户端写消息
                ByteBuf writeBuf = Unpooled.copiedBuffer(LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(writeBuf);
            }
        }
    }
}
