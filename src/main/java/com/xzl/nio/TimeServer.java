package com.xzl.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

public class TimeServer {

    public static void main(String[] args) throws IOException {
        new Thread(new TimeServerTask(8080)).start();
    }

    static class TimeServerTask implements Runnable {

        private Selector selector;

        private ServerSocketChannel serverSocketChannel;

        private volatile boolean stop;

        public TimeServerTask(int port) {
            try {
                //1 创建多路复用器
                selector = Selector.open();
                //2 创建ServerSocketChannel 并且绑定端口号和设置为非阻塞模式
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.socket().bind(new InetSocketAddress(port));
                //3 将ACCEPT操作注册到多路复用器上
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                System.out.println("The time server is start in port :" + port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            stop = true;
        }

        @Override
        public void run() {
            //循环遍历selector
            while (!stop) {
                try {
                    //设置休眠时间为1s,无论是否有读写等时间发生selector每个1s唤醒一次,selector也提供了一个无参的select方法,当有处于就绪状态的
                    //channel时,selector将返回就绪状态的channel的selectionKey集合,通过对就绪状态的Channel进行迭代,可以进行网络的异步读写
                    assert selector != null;
                    selector.select(1000);
                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext(); ) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        try {
                            handleInput(key);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (key != null) {
                                key.cancel();
                                if (key.channel() != null) {
                                    key.channel().close();
                                }
                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleInput(SelectionKey key) throws IOException {
            if (key.isValid()) {
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel accept = channel.accept();
                    accept.configureBlocking(false);
                    accept.register(selector, SelectionKey.OP_READ);
                }
                if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int read = socketChannel.read(readBuffer);
                    if (read > 0) {
                        readBuffer.flip();
                        byte[] bytes = new byte[readBuffer.remaining()];
                        readBuffer.get(bytes);
                        String body = new String(bytes);
                        System.out.println("TimeServer accept body:" + body);
                        doWrite(socketChannel);
                    } else if (read < 0) {
                        key.cancel();
                        socketChannel.close();
                    }
                }
            }
        }

        private void doWrite(SocketChannel socketChannel) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put("hello world".getBytes(StandardCharsets.UTF_8));
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
        }
    }
}
