package com.xzl.netty.serialization;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @author xzl
 * @date 2021-04-17 20:45
 **/
public class BookClient {

    public static void main(String[] args) throws InterruptedException {
        //配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ObjectDecoder(1024 * 1024, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                        ch.pipeline().addLast(new ObjectEncoder());
                        ch.pipeline().addLast(new ChannelHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                Book book = new Book();
                                book.setName("西游记");
                                book.setAuthor("吴承恩");
                                book.setPrice(19.9);
                                book.setCreateTime(LocalDateTime.now());
                                ctx.write(book);
                                ctx.flush();
                            }
                        });
                    }
                });
        ChannelFuture f = b.connect("127.0.0.1", 8081).sync();

        f.channel().closeFuture().sync();

        group.shutdownGracefully();


    }

}
