package com.xzl.netty.privateAgreement.handler;

import com.xzl.netty.privateAgreement.MessageType;
import com.xzl.netty.privateAgreement.protocol.Header;
import com.xzl.netty.privateAgreement.protocol.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

/**
 * @author xzl
 * @date 2021-04-25 23:06
 **/
public class LoginAuthReqHandler extends ChannelHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage nettyMessage = (NettyMessage) msg;
        if (nettyMessage.getHeader() != null && nettyMessage.getHeader().getType() == MessageType.LOGIN_RESP.getCode()) {
            Object body = nettyMessage.getBody();
            //握手失败
            if (!Objects.equals(body, 0)) {
                ctx.close();
            } else {
                System.out.println("Login is ok :" + nettyMessage);
                ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

    /**
     * 构建登录请求
     *
     * @return NettyMessage
     */
    private NettyMessage buildLoginReq() {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP.getCode());
        message.setHeader(header);
        return message;
    }
}
