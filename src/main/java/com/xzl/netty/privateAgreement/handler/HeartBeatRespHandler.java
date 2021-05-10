package com.xzl.netty.privateAgreement.handler;

import com.xzl.netty.privateAgreement.MessageType;
import com.xzl.netty.privateAgreement.protocol.Header;
import com.xzl.netty.privateAgreement.protocol.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author xzl
 * @date 2021-04-27 21:08
 **/
public class HeartBeatRespHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.HEARTBEAT_REQ.getCode()) {
            System.out.println("Receive client heart beat message :--->" + message);
            NettyMessage heartBeat = buildHeatBeat();
            ctx.writeAndFlush(heartBeat);
            System.out.println("Send heart beat response message to cline :--->"+heartBeat);
        }else {
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildHeatBeat() {
        NettyMessage nettyMessage = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.HEARTBEAT_RESP.getCode());
        nettyMessage.setHeader(header);
        return nettyMessage;
    }
}
