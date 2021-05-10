package com.xzl.netty.privateAgreement.handler;

import com.alibaba.fastjson.JSON;
import com.xzl.netty.privateAgreement.protocol.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author xzl
 * @date 2021-04-25 22:23
 **/
public class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage> {


    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, List<Object> out) throws Exception {
        if (msg == null || msg.getHeader() == null) {
            throw new Exception("The encode message is null");
        }
        ByteBuf sendB = Unpooled.buffer();
        sendB.writeInt(msg.getHeader().getCrcCode());
        sendB.writeInt(msg.getHeader().getLength());
        sendB.writeLong(msg.getHeader().getSessionID());
        sendB.writeByte(msg.getHeader().getType());
        sendB.writeByte(msg.getHeader().getPriority());
        Map<String, Object> attachment = msg.getHeader().getAttachment();
        sendB.writeInt(attachment.size());
        for (String key : attachment.keySet()) {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            sendB.writeInt(keyBytes.length);
            sendB.writeBytes(keyBytes);
            byte[] bytes = JSON.toJSONBytes(attachment.get(key));
            sendB.writeInt(bytes.length);
            sendB.writeBytes(bytes);
        }
        if (msg.getBody() != null) {
            byte[] bytes = JSON.toJSONBytes(msg.getBody());
            sendB.writeInt(bytes.length);
            sendB.writeBytes(bytes);
        } else {
            sendB.writeInt(0);
        }
        sendB.setInt(4, sendB.readableBytes()-8);
        out.add(sendB);
    }
}
