package com.xzl.netty.privateAgreement;

import com.xzl.netty.privateAgreement.handler.HeartBeatReqHandler;
import com.xzl.netty.privateAgreement.handler.LoginAuthReqHandler;
import com.xzl.netty.privateAgreement.handler.NettyMessageDecoder;
import com.xzl.netty.privateAgreement.handler.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author xzl
 * @date 2021-04-27 21:19
 **/
public class NettyClient {
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    EventLoopGroup group = new NioEventLoopGroup();

    public void connect(int port,String host) throws Exception{
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyMessageDecoder(1024*1024,4,4));
                            ch.pipeline().addLast("MessageEncoder", new NettyMessageEncoder());
                            ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
                            ch.pipeline().addLast("LoginAuthHandler", new LoginAuthReqHandler());
                            ch.pipeline().addLast("HeartBeatHandler", new HeartBeatReqHandler());
                        }
                    });
            ChannelFuture sync = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            sync.channel().closeFuture().sync();
        }finally {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        try {
                            connect(8888,"127.0.0.1");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void main(String[] args) {
        try {
            new NettyClient().connect(8888,"127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
