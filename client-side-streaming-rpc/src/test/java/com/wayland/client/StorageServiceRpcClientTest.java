package com.wayland.client;

import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.log.Log;
import com.wayland._grpc.client_side_streaming_rpc.ClientSideStreamingRpcService;
import com.wayland.server.StorageServiceRpcServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-10-05 上午11:44
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
    void upload() throws IOException {
        String fileName = "CSV_" + RandomUtil.randomString(4) + ".csv";
        String encoding = CharsetUtil.UTF_8;
        ClientSideStreamingRpcService.CommonResponse response = client.upload(fileName, mockCsvFileData(16, 350000), encoding);
        Assertions.assertEquals(0, response.getCode());
    }

    private InputStream mockCsvFileData(int columnCount, int lineCount) {

        // output stream in memory
        try (
                FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
                CsvWriter writer = CsvUtil.getWriter(IoUtil.getWriter(outputStream, CharsetUtil.CHARSET_UTF_8));
        ) {

            // csv header
            String[] headers = ArrayUtil.newArray(String.class, columnCount);
            for (int i = 0; i < columnCount; i++) {
                headers[i] = "header_" + (i + 1);
            }
            writer.write(headers);

            // csv line data
            String[] lineValues = ArrayUtil.newArray(String.class, columnCount);
            for (int i = 0; i < lineCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    lineValues[j] = "col_" + (j + 1) + "_line" + (i + 1);
                }
                writer.write(lineValues);
            }

            return IoUtil.toStream(outputStream.toByteArray());
        }

    }

}