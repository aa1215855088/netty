package com.xzl.netty.tcp.FixedLengthFrameDecoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;

/**
 * @author xzl
 * @date 2021-04-17 23:12
 **/
public class EchoClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new FixedLengthFrameDecoder(5));
                ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast(new ChannelHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        for (int i = 0; i < 1000; i++) {
                            ByteBuf byteBuf = Unpooled.copiedBuffer("hello".getBytes(StandardCharsets.UTF_8));
                            ctx.writeAndFlush(byteBuf);
                        }

                    }
                });
            }

        });

        ChannelFuture sync = b.connect("47.100.37.176", 8080).sync();
        ChannelFuture sync1 = sync.channel().closeFuture().sync();
        group.shutdownGracefully();
    }
}
