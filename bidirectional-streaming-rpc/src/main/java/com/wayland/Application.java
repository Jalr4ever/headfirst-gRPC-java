package com.wayland;

import com.wayland.server.DesensitizeServiceRpcServer;

import java.io.IOException;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-12-11 上午10:57
 */
public class Application {
    public static void main(String[] args) throws IOException, InterruptedException {
        DesensitizeServiceRpcServer server = new DesensitizeServiceRpcServer();
        server.start();
        server.keep();
    }
}
