package com.xzl.netty.privateAgreement.handler;

import com.alibaba.fastjson.JSON;
import com.xzl.netty.privateAgreement.protocol.Header;
import com.xzl.netty.privateAgreement.protocol.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xzl
 * @date 2021-04-25 22:35
 **/
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {
    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if(decode==null){
            return null;
        }
        ByteBuf buf = (ByteBuf) decode;
        NettyMessage nettyMessage = new NettyMessage();
        Header header = new Header();
        header.setCrcCode(buf.readInt());
        header.setLength(buf.readInt());
        header.setSessionID(buf.readLong());
        header.setType(buf.readByte());
        header.setPriority(buf.readByte());

        int size = buf.readInt();
        if (size > 0) {
            Map<String, Object> attch = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                int keySize = buf.readInt();
                byte[] keyArray = new byte[keySize];
                buf.readBytes(keyArray);
                String key = new String(keyArray, Charset.defaultCharset());
                int valueSize = buf.readInt();
                byte[] valueArray = new byte[valueSize];
                buf.readBytes(valueArray);
                attch.put(key, JSON.parseObject(valueArray, Object.class));
            }
            header.setAttachment(attch);
        }
        if (buf.readableBytes() > 1) {
            int bodySize = buf.readInt();
            byte[] bodyArray = new byte[bodySize];
            buf.readBytes(bodyArray);
            nettyMessage.setBody(JSON.parseObject(bodyArray, Object.class));
        }
        nettyMessage.setHeader(header);
        return nettyMessage;
    }
}
