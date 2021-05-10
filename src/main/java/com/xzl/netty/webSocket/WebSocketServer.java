package com.xzl.netty.webSocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.Charset;
import java.time.LocalDateTime;

/**
 * @author xzl
 * @date 2021-04-19 21:53
 **/
public class WebSocketServer {


    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(65536));
                ch.pipeline().addLast(new ChunkedWriteHandler());
                ch.pipeline().addLast(new WebSocketServerHandel());
            }
        });
        ChannelFuture sync = serverBootstrap.bind(8080).sync();

        sync.channel().closeFuture().sync();

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private static class WebSocketServerHandel extends SimpleChannelInboundHandler<Object> {

        private WebSocketServerHandshaker handshaker;

        public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            channels.add(channel);
        }

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
            //传统HTTP接入
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            }
            //WebSocket接入
            if (msg instanceof WebSocketFrame) {
                handleWEbSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        private void handleWEbSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
            //判断是否是关闭链路的指令
            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
            }
            //判断是否是Ping消息
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            }
            // 本文只支持文本消息
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException("不是文本消息");
            }
            //返回应答消息
            String request = ((TextWebSocketFrame) frame).text();
            channels.writeAndFlush(new TextWebSocketFrame(request + ",欢迎使用Netty WebSocket服务，现在时刻：" + LocalDateTime.now().toString()));
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
            if (!request.getDecoderResult().isSuccess()
                    || (!"websocket".equals(request.headers().get("Upgrade")))) {
                sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                return;
            }
            //构建握手响应返回，本机测试
            WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket", null, false);
            handshaker = factory.newHandshaker(request);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), request);
            }
        }

        private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
            if (response.getStatus().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(), Charset.defaultCharset());
                response.content().writeBytes(buf);
                buf.release();
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            }
            ChannelFuture f = ctx.channel().write(response);
            f.addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.channel();
        }
    }
}
