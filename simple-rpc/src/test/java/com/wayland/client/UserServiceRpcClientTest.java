package com.wayland.client;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import com.wayland.server.UserServiceRpcServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-09-15 下午3:03
 */
class UserServiceRpcClientTest {

    private static final Log log = Log.get();

    private static UserServiceRpcClient client;

    @BeforeAll
    static void initClient() {
        client = UserServiceRpcClient.defaultClient();
    }

    @BeforeAll
    static void initServer() throws IOException {
        UserServiceRpcServer server = new UserServiceRpcServer();
        server.start();
        ThreadUtil.execAsync(() -> {
            try {
                server.keep();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Test
    void login() {
        String userName = "foo";
        String pwd = "foo";
        client.login(userName, pwd).getData();
    }

    @Test
    void logout() {
        client.logout();
    }

    @AfterAll
    static void postProcessing() {
        client.close();
    }

}