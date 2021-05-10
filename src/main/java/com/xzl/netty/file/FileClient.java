package com.xzl.netty.file;

import com.xzl.netty.serialization.Book;
import com.xzl.netty.tcp.LineBasedFrameDecoder.TimeClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;

/**
 * @author xzl
 * @date 2021-04-24 15:49
 **/
public class FileClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                        ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                        ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                        ch.pipeline().addLast(new ChannelHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ctx.writeAndFlush("/Users/xuzilou/xzl/test/test.txt"+"\n");
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                });
        ChannelFuture f = b.connect("127.0.0.1", 8080).sync();

        f.channel().closeFuture().sync();

        group.shutdownGracefully();

    }
}
