package com.wayland.client;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.log.Log;
import com.wayland._grpc.bidi_streaming_rpc.BidiStreamingRpcService;
import com.wayland.server.DesensitizeServiceRpcServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jalr4ever[routerhex@qq.com]
 * @since 2021-12-11 下午12:13
 */
class DesensitizeServiceRpcClientTest {

    private static final Log log = Log.get();

    private static DesensitizeServiceRpcClient client;

    @BeforeAll
    static void initClient() {
        client = DesensitizeServiceRpcClient.defaultClient();
    }

    @BeforeAll
    static void initServer() throws IOException {
        DesensitizeServiceRpcServer server = new DesensitizeServiceRpcServer();
        server.start();
        ThreadUtil.execAsync(() -> {
            try {
                server.keep();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @AfterAll
    static void postProcessing() {
        client.close();
    }

    @Test
    void desensitize() {
        List<String> raw = rawsMock();
        List<String> result = client.desensitize(raw, BidiStreamingRpcService.DesensitizeRequest.Type.COVER);
        Assertions.assertEquals(raw.size(), result.size());
    }

    private List<String> rawsMock() {
        // mock random email list
        List<String> emails = new ArrayList<>(500);
        for (int i = 0; i < 100; i++) {
            emails.add("wl_" + RandomUtil.randomString(6) + "@jalr4ever.com");
        }
        return emails;
    }

}
