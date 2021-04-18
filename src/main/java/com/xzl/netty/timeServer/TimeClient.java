package com.xzl.netty.timeServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.StandardCharsets;

/**
 * @author xzl
 * @date 2021-04-17 20:45
 **/
public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        //配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TimeClientHandler());
                    }
                });
        ChannelFuture f = b.connect("127.0.0.1", 8080).sync();

        f.channel().closeFuture().sync();

        group.shutdownGracefully();


    }

    private static class TimeClientHandler extends ChannelHandlerAdapter {

        private final ByteBuf firstMessage;

        public TimeClientHandler() {
            byte[] bytes = "hello world".getBytes(StandardCharsets.UTF_8);
            firstMessage = Unpooled.buffer(bytes.length);
            firstMessage.writeBytes(bytes);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            for (int i = 0; i < 100000; i++) {
                ctx.writeAndFlush(firstMessage);
            }

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;

            byte[] bytes = new byte[byteBuf.readableBytes()];

            byteBuf.readBytes(bytes);

            String body = new String(bytes, StandardCharsets.UTF_8);

            System.out.println("receive server message:" + body);


        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
