package com.xzl.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

/**
 * @author xzl
 * @date 2021-04-20 21:53
 **/
public class ChineseProverbServer {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChineseProverbServerHandler());
        b.bind(8080).sync().channel().closeFuture().await();

        group.shutdownGracefully();
    }

    private static class ChineseProverbServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {


        @Override
        protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            String s = msg.content().toString(CharsetUtil.UTF_8);
            System.out.println(s);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            cause.printStackTrace();

        }
    }
}
