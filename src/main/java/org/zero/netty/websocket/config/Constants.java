package org.zero.netty.websocket.config;

/**
 * 项目中的全局常量定义
 *
 * @author 01
 */
public interface Constants {
    String WEB_SOCKET_URL = "ws://localhost:8080/websocket";
    String WEBSOCKET_STR = "websocket";
    String UPGRADE_STR = "Upgrade";
    int OK_CODE = 200;

    String HTTP_CODEC = "http-codec";
    String AGGREGATOR = "aggregator";
    String HTTP_CHUNKED = "http-chunked";
    String HANDLER = "handler";
    int MAX_CONTENT_LENGTH = 65536;
    int PORT = 8080;
}
