package org.zero.netty.websocket.config;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @program: Netty-WebSocket
 * @description: 工程的全局配置类
 * @author: 01
 * @create: 2018-11-03 17:28
 **/
public class NettyConfig {

    /**
     * 存储每一个客户端接入进来时的channel对象
     */
    public final static ChannelGroup GROUP =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}
