package com.xzl.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author xzl
 * @date 2021-04-18 15:15
 **/
public class HttpFileServer {

    private static final String DEFAULT_URL = "/Users/xuzilou/xzl";

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.ERROR))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                        ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                        ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                        ch.pipeline().addLast("fileServerHandler", new FileServerHandler());
                    }
                });

        ChannelFuture sync = server.bind("192.168.0.104", 8081).sync();
        sync.channel().closeFuture().sync();

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

    }

    private static class FileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


        @Override
        protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            //检查
            if (!request.getDecoderResult().isSuccess()) {
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                return;
            }
            if (request.getMethod() != HttpMethod.GET) {
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                return;
            }
            String uri = request.getUri();
            String path = sanitizeUri(uri);
            if (path == null) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }

            File file = new File(path);

            if (file.isHidden() || !file.exists()) {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }

            if (file.isDirectory()) {
                if (uri.endsWith("/")) {
                    sendListing(ctx, file);
                } else {
                    sendRedirect(ctx, uri + "/");
                }
                return;
            }

            if (!file.isFile()) {
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                return;
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

            long length = randomAccessFile.length();

            HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            setContentLength(response, length);
            setContentTypeHeader(response, file);
            if (isKeepAlive(request)) {
                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            ctx.write(response);
            ChannelFuture sendFileFuture = ctx.writeAndFlush(new ChunkedFile(randomAccessFile),ctx.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                    if (total < 0) {
                        System.err.println("Transfer progress:" + progress);
                    } else {
                        System.err.println("Transfer progress:" + total);
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                    System.out.println("Transfer progress");
                }
            });
            ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if(!isKeepAlive(request)){
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            }

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            if (ctx.channel().isActive()) {
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }

        private static boolean isKeepAlive(FullHttpRequest request) {
            String isKeepAlive = request.headers().get("Connection");
            return isKeepAlive.equals("keep-alive");
        }

        private static void setContentTypeHeader(HttpResponse response, File file) {
            MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mimetypesFileTypeMap.getContentType(file.getName()));


        }

        private static void setContentLength(HttpResponse response, long length) {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, length);
        }

        private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT);
            response.headers().set("Location", newUri);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private static void sendListing(ChannelHandlerContext ctx, File file) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set("content-type", "text/html;charset=UTF-8");
            StringBuffer buf = new StringBuffer();
            String path = file.getPath();
            buf.append("<!DOCTYPE html>\r\n");
            buf.append("<html><head><title>");
            buf.append(path);
            buf.append(" 目录：");
            buf.append("</title></head><body>\r\n");
            buf.append("<h3>");
            buf.append(path).append("目录");
            buf.append("</h3>\r\n");
            buf.append("<ul>");
            buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
            for (File f : file.listFiles()) {
                if (f.isHidden() || !f.canRead()) {
                    continue;
                }
                String name = f.getName();
                buf.append("<li>链接:<a href=\"");
                buf.append(name);
                buf.append("\">");
                buf.append(name);
                buf.append("</a></li>\r\n");
            }
            buf.append("</ul></body></html>\r\n");
            ByteBuf byteBuf = Unpooled.copiedBuffer(buf, Charset.defaultCharset());
            response.content().writeBytes(byteBuf);
            byteBuf.release();
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private static String sanitizeUri(String uri) {
            try {
                uri = URLDecoder.decode(uri, "UTF-8");

            } catch (UnsupportedEncodingException e) {

                try {
                    uri = URLDecoder.decode(uri, "ISO-8859-1");
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                    throw new Error();
                }
            }
            if (!uri.startsWith(DEFAULT_URL)) {
                return null;
            }
            if (!uri.startsWith("/")) {
                return null;
            }
            uri = uri.replace("/", File.separator);
            if (uri.contains(File.separator + ".")
                    || uri.contains("." + File.separator)
                    || uri.startsWith(".")
                    || uri.endsWith(".")) {
                return null;
            }
            return uri;
        }

        private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status
                    , Unpooled.copiedBuffer("Failure:" + status.toString(), StandardCharsets.UTF_8));
            response.headers().set("content-type", "text/plain; charset=UTF-8");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }


}
