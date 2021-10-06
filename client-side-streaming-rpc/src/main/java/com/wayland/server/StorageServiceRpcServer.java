package com.wayland.server;

import cn.hutool.log.Log;
import cn.hutool.setting.dialect.PropsUtil;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

import java.io.IOException;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-09-23 下午3:28
 */
public class StorageServiceRpcServer {
    private Server server;

    private final Log log = Log.get();

    /**
     * start server
     *
     * @throws IOException e
     */
    public void start() throws IOException {
        this.server = NettyServerBuilder
                .forPort(getServerPort())
                .permitKeepAliveWithoutCalls(true)
                .addService(new StorageService()).build().start();
        log.info("Server started at port: {}", getServerPort());
        Runtime.getRuntime().addShutdownHook(new Thread(StorageServiceRpcServer.this::stop));
    }

    /**
     * stop server
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * keep server thread running，util thread been terminated
     */
    public void keep() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private static Integer getServerPort() {
        return PropsUtil.get("application.properties").getInt("server.port");
    }
}
