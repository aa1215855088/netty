package com.xzl.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class TimeClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < 10000; i++) {
            new Thread(new TimeClientTask("127.0.0.1", 8080)).start();
        }

    }

    static class TimeClientTask implements Runnable {

        private String host;

        private int port;

        private Selector selector;

        private SocketChannel socketChannel;

        private volatile boolean stop;

        public TimeClientTask(String host, int port) {
            this.host = host;
            this.port = port;
            try {
                selector = Selector.open();
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }

        @Override
        public void run() {
            try {
                doConnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (!stop) {
                try {
                    int select = selector.select(1000);
                    Set<SelectionKey> keys = selector.keys();
                    for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext(); ) {
                        SelectionKey key = iterator.next();
                        try {
                            handleInput(key);
                        } catch (Exception e) {
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
                SocketChannel sc = (SocketChannel) key.channel();
                if (key.isConnectable()) {
                    if(sc.finishConnect()){
                        sc.register(selector, SelectionKey.OP_READ);
                        doWrite(socketChannel);
                    }
                }
                if (key.isReadable()) {
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int read = sc.read(readBuffer);
                    if (read > 0) {
                        readBuffer.flip();
                        byte[] bytes = new byte[readBuffer.remaining()];
                        String body = new String(bytes, StandardCharsets.UTF_8);
                        System.out.println(body);
                    }
                }
            }
        }

        private void doConnect() throws IOException {
            if (socketChannel.connect(new InetSocketAddress(host, port))) {
                socketChannel.register(selector, SelectionKey.OP_READ);
                doWrite(socketChannel);
            } else {
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        }

        private void doWrite(SocketChannel socketChannel) throws IOException {
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
            writeBuffer.put("connect success".getBytes(StandardCharsets.UTF_8));
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
        }
    }

}
