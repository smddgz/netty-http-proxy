package org.example.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import org.example.proxy.HttpClient;

public class ServerProxyHandler extends ChannelInboundHandlerAdapter {
    Channel target;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FullHttpRequest){
            ctx.pipeline().remove(HttpRequestDecoder.class);
            ctx.pipeline().remove(HttpObjectAggregator.class);
            try {
                target=new HttpClient().start(ctx.channel(), ((FullHttpRequest) msg));
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }else{
            target.writeAndFlush(msg);
        }
    }
}
