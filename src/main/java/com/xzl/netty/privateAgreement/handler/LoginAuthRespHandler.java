package com.xzl.netty.privateAgreement.handler;

import com.xzl.netty.privateAgreement.MessageType;
import com.xzl.netty.privateAgreement.protocol.Header;
import com.xzl.netty.privateAgreement.protocol.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xzl
 * @date 2021-04-25 23:19
 **/
public class LoginAuthRespHandler  extends ChannelHandlerAdapter {
    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();

    /**
     * 白名单
     */
    private String[] whitelist = new String[]{"127.0.0.1"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        //如果是握手请求，处理，其他请求透穿
        if(message.getHeader()!=null && message.getHeader().getType()== MessageType.LOGIN_RESP.getCode()){
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp = null;
            //检查是否已经登录过
            if(nodeCheck.containsKey(nodeIndex)){
                loginResp = buildResponse((byte) -1);
            }else {
                //判断是否在白名单 如果存在加入nodeCheck
                InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                String hostAddress = socketAddress.getAddress().getHostAddress();
                boolean isOK = Arrays.asList(whitelist).contains(hostAddress);
                loginResp = isOK ? buildResponse((byte) 0) : buildResponse((byte) -1);
                if(isOK){
                    nodeCheck.put(nodeIndex,true);
                }
                System.out.println("The login response is :"+loginResp+" body ["+loginResp.getBody()+"]");
                ctx.writeAndFlush(loginResp);
            }
        }else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }

    private NettyMessage buildResponse(byte body) {
        NettyMessage nettyMessage = new NettyMessage();
        nettyMessage.setBody(body);
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP.getCode());
        nettyMessage.setHeader(header);
        return nettyMessage;
    }
}
