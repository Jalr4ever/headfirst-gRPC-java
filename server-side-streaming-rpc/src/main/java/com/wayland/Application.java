package com.wayland;

import com.wayland.server.StorageServiceRpcServer;

import java.io.IOException;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-10-06 下午5:51
 */
public class Application {
    public static void main(String[] args) throws IOException, InterruptedException {
        StorageServiceRpcServer server = new StorageServiceRpcServer();
        server.start();
        server.keep();
    }
}
