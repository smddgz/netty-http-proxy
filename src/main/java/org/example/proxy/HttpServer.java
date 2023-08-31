package org.example.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import org.example.handler.ServerProxyHandler;

public class HttpServer {
    int port;

    int maxContentLength = 8192;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new HttpRequestDecoder())
                                .addLast(new HttpObjectAggregator(maxContentLength))
                                .addLast(new ServerProxyHandler());
                    }
                });

        try {
            serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

    }
}
