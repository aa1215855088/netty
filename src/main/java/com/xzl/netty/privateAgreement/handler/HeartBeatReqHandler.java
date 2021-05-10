package com.xzl.netty.privateAgreement.handler;

import com.xzl.netty.privateAgreement.MessageType;
import com.xzl.netty.privateAgreement.protocol.Header;
import com.xzl.netty.privateAgreement.protocol.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xzl
 * @date 2021-04-27 19:24
 **/
public class HeartBeatReqHandler extends ChannelHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        //握手成功，主动发送心跳消息
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_RESP.getCode()) {
            heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 0, 5000, TimeUnit.MILLISECONDS);
        } else if (message.getHeader() != null && message.getHeader().getType() == MessageType.HEARTBEAT_RESP.getCode()) {
            System.out.println("Client receive server heart beat message:--->" + message);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (heartBeat != null) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }

    private static class HeartBeatTask implements Runnable {
        final ChannelHandlerContext channelHandlerContext;

        public HeartBeatTask(ChannelHandlerContext ctx) {
            this.channelHandlerContext = ctx;
        }

        @Override
        public void run() {
            NettyMessage heatBeat = buildHeatBeat();
            System.out.println("Client send heart beat message to server: --->" + heatBeat);
            channelHandlerContext.writeAndFlush(heatBeat);
        }

        private NettyMessage buildHeatBeat() {
            NettyMessage message = new NettyMessage();
            Header header = new Header();
            header.setType(MessageType.HEARTBEAT_REQ.getCode());
            message.setHeader(header);
            return message;
        }
    }
}
