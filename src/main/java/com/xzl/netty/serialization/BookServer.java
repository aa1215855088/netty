package com.xzl.netty.serialization;

import com.sun.xml.internal.bind.api.ClassResolver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.rtsp.RtspObjectDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author xzl
 * @date 2021-04-18 14:21
 **/
public class BookServer {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ObjectDecoder(1024 * 1024, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                        ch.pipeline().addLast(new ObjectEncoder());
                        ch.pipeline().addLast(new ChannelHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                Book book = (Book) msg;
                                System.out.println(book);
                            }
                        });
                    }
                });

        ChannelFuture sync = server.bind(8081).sync();
        sync.channel().closeFuture().sync();

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
