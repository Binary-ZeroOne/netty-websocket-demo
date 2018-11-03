package org.zero.netty.websocket.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.zero.netty.websocket.config.Constants;
import org.zero.netty.websocket.config.NettyConfig;

import java.util.Date;

/**
 * @program: Netty-WebSocket
 * @description: 接收处理并响应客户端WebSocket请求的核心业务处理类
 * @author: 01
 * @create: 2018-11-03 17:34
 **/
@Slf4j
@ChannelHandler.Sharable
public class MyWebsocketHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketServerHandshaker handshaker;

    /**
     * 服务端处理客户端WebSocket请求的核心方法
     *
     * @param ctx ctx
     * @param msg msg
     * @throws Exception Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 处理客户端向服务端发起http握手请求的业务
        if (msg instanceof FullHttpRequest) {
            handHttpRequest(ctx, (FullHttpRequest) msg);
        }
        // 处理websocket连接
        else if (msg instanceof WebSocketFrame) {
            handWebsocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 处理客户端与服务端之间的websocket业务
     *
     * @param ctx   ctx
     * @param frame frame
     */
    private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否是关闭websocket的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
            log.debug("接收到关闭websocket的指令");
        }

        // 判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            log.debug("接收到ping消息");
            return;
        }

        // 判断是否是二进制消息，如果是二进制消息，则抛出异常
        if (!(frame instanceof TextWebSocketFrame)) {
            log.error("目前不支持二进制消息");
            throw new UnsupportedOperationException("【" + this.getClass().getName() + "】不支持的消息");
        }

        // 获取客户端向服务端发送的消息
        String requestStr = ((TextWebSocketFrame) frame).text();
        log.debug("服务端收到客户端的消息: {}", requestStr);

        // 返回应答消息
        String responseStr = new Date().toString()
                + ctx.channel().id() +
                " ===>>> " + requestStr;
        TextWebSocketFrame tws = new TextWebSocketFrame(responseStr);

        // 群发，服务端向每个连接上来的客户端群发消息
        NettyConfig.GROUP.writeAndFlush(tws);
        log.debug("群发消息完成. 群发的消息为: {}", responseStr);
    }

    /**
     * 处理客户端向服务端发起http握手请求的业务
     *
     * @param ctx     ctx
     * @param request request
     */
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        String upgrade = request.headers().get(Constants.UPGRADE_STR);
        // 非websocket的http握手请求处理
        if (!request.decoderResult().isSuccess() || !Constants.WEBSOCKET_STR.equals(upgrade)) {
            sendHttpResponse(ctx, request,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            log.warn("非websocket的http握手请求");
            return;
        }

        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(Constants.WEB_SOCKET_URL, null, false);
        handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            // 响应不支持的请求
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            log.warn("不支持的请求");
        } else {
            handshaker.handshake(ctx.channel(), request);
            log.debug("正常处理");
        }
    }

    /**
     * 服务端主动向客户端发送消息
     *
     * @param ctx      ctx
     * @param request  request
     * @param response response
     */
    private void sendHttpResponse(ChannelHandlerContext ctx,
                                  FullHttpRequest request,
                                  DefaultFullHttpResponse response) {
        // 不成功的响应
        if (response.status().code() != Constants.OK_CODE) {
            ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
            log.warn("不成功的响应");
        }

        // 服务端向客户端发送数据
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        if (!HttpUtil.isKeepAlive(request) ||
                response.status().code() != Constants.OK_CODE) {
            // 如果是非Keep-Alive，或不成功都关闭连接
            channelFuture.addListener(ChannelFutureListener.CLOSE);
            log.info("websocket连接关闭");
        }
    }

    /**
     * 客户端与服务端创建连接的时候调用
     *
     * @param ctx ctx
     * @throws Exception Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 将channel添加到channel group中
        NettyConfig.GROUP.add(ctx.channel());
        log.info("客户端与服务端连接开启...");
    }

    /**
     * 客户端与服务端断开连接的时候调用
     *
     * @param ctx ctx
     * @throws Exception Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 从channel group中移除这个channel
        NettyConfig.GROUP.remove(ctx.channel());
        log.info("客户端与服务端关闭连接...");
    }

    /**
     * 服务端接收客户端发送过来的数据结束之后调用
     *
     * @param ctx ctx
     * @throws Exception Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 清空数据
        ctx.flush();

        log.info("flush数据 {}", ctx.name());
    }

    /**
     * 工程出现异常的时候调用
     *
     * @param ctx   ctx
     * @param cause cause
     * @throws Exception Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 打印异常堆栈
        cause.printStackTrace();
        // 主动关闭连接
        ctx.close();
        log.error("WebSocket连接异常");
    }
}
