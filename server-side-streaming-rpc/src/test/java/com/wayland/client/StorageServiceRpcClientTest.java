package com.wayland.client;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import com.wayland.server.StorageServiceRpcServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-10-06 下午5:50
 */
class StorageServiceRpcClientTest {

    private static final Log log = Log.get();

    private static StorageServiceRpcClient client;

    @BeforeAll
    static void initClient() {
        client = StorageServiceRpcClient.defaultClient();
    }

    @BeforeAll
    static void initServer() throws IOException {
        StorageServiceRpcServer server = new StorageServiceRpcServer();
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
    void downloadToDisk() throws IOException {
        File outputFile = client.downloadToDisk();
        log.info("Output file length: {}", FileUtil.size(outputFile));
        log.info("Output file path: {}", FileUtil.getAbsolutePath(outputFile));
        Assertions.assertTrue(Objects.nonNull(outputFile));
        Assertions.assertTrue(FileUtil.size(outputFile) > 0);
    }

    @Test
    void downloadToMemory() throws IOException {
        ByteArrayOutputStream outputStream = client.downloadToMemory();
        log.info("Output file length: {}", outputStream.toByteArray().length);
        Assertions.assertTrue(outputStream.toByteArray().length > 0);
    }


}