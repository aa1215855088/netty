package com.xzl.netty.bytebuf;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author xzl
 * @date 2021-04-28 22:24
 **/
public class ByteBufTest {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(88);
        buffer.put("java".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        byte[] s = new byte[buffer.remaining()];
        buffer.get(s);
        System.out.println(new String(s));

        ByteBuf buf = Unpooled.buffer();
        PooledByteBufAllocator byteBuf = new PooledByteBufAllocator();
        buf.writeBytes("hello java".getBytes(StandardCharsets.UTF_8));
        byte[] b = new byte[buf.readableBytes()];
        buf.readBytes(b);
        System.out.println(new String(b));
    }
}
