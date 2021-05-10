package com.xzl.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @author xzl
 * @date 2021-04-20 22:06
 **/
public class ChineseProverClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChineseProverbClientHandler());

        Channel channel = b.bind(0).sync().channel();
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("hello xzl", CharsetUtil.UTF_8), new InetSocketAddress("255.255.255.255", 8080))).sync();
        if(!channel.closeFuture().await(15000)){
            System.out.println("查询超时");
        }
    }

    private static class ChineseProverbClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            String body = msg.content().toString();
            System.out.println(body);
        }
    }
}
