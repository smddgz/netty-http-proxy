package org.example.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import org.example.handler.ClientProxyHandler;

import java.util.Objects;

public class HttpClient {
    String host;
    int port;

    public Channel start(Channel source, FullHttpRequest request) throws InterruptedException {
        setHostAndPort(request);
        NioEventLoopGroup work = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(work)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ClientProxyHandler(source, request));
                    }
                });
        return bootstrap.connect(host, port).sync().channel();
    }

    public void setHostAndPort(FullHttpRequest request) {
        if (Objects.equals(request.method().name(), "CONNECT")) {
            String uri = request.uri();
            int i = uri.indexOf(":");
            if (i > 0) {
                host = uri.substring(0, i);
                port = Integer.parseInt(uri.substring(i + 1));
            } else {
                host = uri;
                port = 443;
            }
        } else {
            String hostAndPort = request.headers().get("Host");
            int i = hostAndPort.indexOf(":");
            if (i > 0) {
                host = hostAndPort.substring(0, i);
                port = Integer.parseInt(hostAndPort.substring(i + 1));
            } else {
                host = hostAndPort;
                port = 80;
            }
        }
    }
}
