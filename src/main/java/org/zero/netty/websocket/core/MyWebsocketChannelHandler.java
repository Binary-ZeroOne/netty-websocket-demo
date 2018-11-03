package org.zero.netty.websocket.core;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.zero.netty.websocket.config.Constants;

/**
 * @program: Netty-WebSocket
 * @description: 初始化连接时的各个组件
 * @author: 01
 * @create: 2018-11-03 21:53
 **/
public class MyWebsocketChannelHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(Constants.HTTP_CODEC, new HttpServerCodec());
        ch.pipeline().addLast(Constants.AGGREGATOR, new HttpObjectAggregator(Constants.MAX_CONTENT_LENGTH));
        ch.pipeline().addLast(Constants.HTTP_CHUNKED, new ChunkedWriteHandler());
        ch.pipeline().addLast(Constants.HANDLER, new MyWebsocketHandler());
    }
}
