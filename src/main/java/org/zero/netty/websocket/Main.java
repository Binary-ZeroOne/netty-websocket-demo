package org.zero.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.zero.netty.websocket.config.Constants;
import org.zero.netty.websocket.core.MyWebsocketChannelHandler;

/**
 * @program: Netty-WebSocket
 * @description: 程序的入口，负责启动应用
 * @author: 01
 * @create: 2018-11-03 22:06
 **/
@Slf4j
public class Main {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new MyWebsocketChannelHandler());
            log.info("服务端开启等待客户端连接...");

            Channel channel = bootstrap.bind(Constants.PORT).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("服务端启动失败", e);
        } finally {
            // 退出程序
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            log.info("服务端已关闭");
        }
    }
}
