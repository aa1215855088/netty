package com.xzl.netty.file;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @author xzl
 * @date 2021-04-24 14:40
 **/
public class FileServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new StringEncoder(CharsetUtil.UTF_8)
                                , new LineBasedFrameDecoder(1024)
                                , new StringDecoder(CharsetUtil.UTF_8)
                                ,new FileServerHandler() );
                    }
                });
        ChannelFuture sync = b.bind(8080).sync();
        sync.channel().closeFuture().sync();

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

    }

    private static class FileServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
            File file = new File(msg);
            if(file.exists()){
                if(!file.isFile()){
                    ctx.writeAndFlush("not a file:" + file + "\n");
                    return;
                }
                ctx.write(file + " " + file.length() + "\n");
                RandomAccessFile randomAccessFile = new RandomAccessFile(msg, "r");
                DefaultFileRegion region = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
                ctx.write(region);
                ctx.writeAndFlush("\n");
                randomAccessFile.close();
            }else {
                ctx.writeAndFlush("File not found:" + file + "\n");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
