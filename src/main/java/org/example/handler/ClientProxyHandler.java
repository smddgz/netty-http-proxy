package org.example.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class ClientProxyHandler extends ChannelInboundHandlerAdapter {
    Channel source;
    FullHttpRequest request;

    public ClientProxyHandler(Channel source, FullHttpRequest request) {
        this.source = source;
        this.request = request;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (Objects.equals(request.method().name(), "CONNECT")) {
            source.writeAndFlush(responseBuf());
        } else {
            ctx.writeAndFlush(requestBuf());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        source.writeAndFlush(msg);
    }

    public ByteBuf responseBuf() {
        String response = String.format("%s 200 Connection Established\r\n\r\n", request.protocolVersion().text());
        return Unpooled.wrappedBuffer(response.getBytes());
    }

    public ByteBuf requestBuf() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %s %s\r\n", request.method().name(), request.uri(), request.protocolVersion().text()));
        for (Map.Entry<String, String> header : request.headers()) {
            sb.append(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
        }
        sb.append("\r\n");

        String requestStr = sb.toString();
        int i = request.content().readableBytes();
        byte[] content = new byte[i];
        request.content().readBytes(content);

        ByteBuf buffer = Unpooled.buffer(i + requestStr.length());
        buffer.writeBytes(requestStr.getBytes());
        buffer.writeBytes(content);
        return buffer;
    }
}
