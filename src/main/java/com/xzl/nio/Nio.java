package com.xzl.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

public class Nio {
    public static void main(String[] args) throws IOException {
        //1 打开ServerSocketChannel,用于监听客户端的连接，它是所有客户端连接的父管道
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        //2 绑定端口号并且设置为非阻塞模式
        socketChannel.socket().bind(new InetSocketAddress(8080));
        socketChannel.configureBlocking(false);
        //3 创建Reactor线程,创建多路复用启动线程
        Selector selector = Selector.open();
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
        //4 将ServerSocketChannel注册到Reactor线程的多路复用器Selector上,监听ACCEPT事件
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //5 准备就绪的key
        int num = selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        for (SelectionKey key : keys) {

        }
        //6 多路复用器监听有新的客户端接入,处理新的接入请求,完成TCP三次握手,建立物理链路
        SocketChannel accept = socketChannel.accept();
        //7 设置客户端为非阻塞模式
        accept.configureBlocking(false);
        accept.socket().setReuseAddress(true);
        //8 将新接入的客户端连接注册到Reactor线程的多路复用上,监听操作用来读取客户端发送的网络消息
        SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
        //9 异步读取客户端请求信息到缓存区
        ByteBuffer[] byteBuffers = new ByteBuffer[1024];
        long read = accept.read(byteBuffers);
        //10 对ByteBuffer进行解码,如果有半包消息指针reset,继续读取后续报文,将解码成功的消息封装成Task,投递到业务线程池中,进行业务逻辑出来

        //11 异步写回给客户端
//        accept.write();
    }
}
