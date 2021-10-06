package com.wayland.client;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.log.Log;
import cn.hutool.setting.dialect.PropsUtil;
import com.wayland._grpc.server_side_streaming_rpc.ServerSideStreamingRpcService;
import com.wayland._grpc.server_side_streaming_rpc.StorageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-10-06 下午4:54
 */
public class StorageServiceRpcClient implements AutoCloseable {

    private StorageServiceGrpc.StorageServiceBlockingStub storageServiceStub;

    private final Log log = Log.get();

    public static StorageServiceRpcClient defaultClient() {
        Integer port = PropsUtil.get("application.properties").getInt("server-side-storage-upload-service.port");
        String host = PropsUtil.get("application.properties").getStr("server-side-storage-upload-service.host");
        StorageServiceRpcClient client = new StorageServiceRpcClient();
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext() // problem occur without this option but not when ssl(https)
                .build();
        client.storageServiceStub = StorageServiceGrpc.newBlockingStub(channel);
        return client;
    }

    public File downloadToDisk() throws IOException {
        // steaming read with iterator
        Iterator<ServerSideStreamingRpcService.DownloadResponse> iterator = storageServiceStub.download(ServerSideStreamingRpcService.EmptyRequest.newBuilder().build());
        String fileName = null;
        OutputStream outputStream = null;
        int current = 0;
        File outputFile = null;
        while (iterator.hasNext()) {
            ServerSideStreamingRpcService.DownloadResponse response = iterator.next();
            switch (response.getDataCase()) {
                case METADATA:
                    log.info("Client download - handle file metadata");
                    ServerSideStreamingRpcService.DownloadResponse.Metadata metadata = response.getMetadata();
                    fileName = metadata.getName();
                    outputFile = FileUtil.file("file/" + fileName);
                    outputStream = FileUtil.getOutputStream(outputFile);
                    break;
                case CHUNK:
                    log.info("Client download - handle data chunk {}", current++);
                    if (Objects.isNull(outputStream)) {
                        throw ExceptionUtil.wrapRuntime("OutputStream did't init yet. Hint: Client did't receive file metadata request");
                    }
                    outputStream.write(response.getChunk().toByteArray());
                    break;
                case ERROR:
                    throw ExceptionUtil.wrapRuntime("Code " + response.getError().getCode() + "\n" + response.getError().getHint());
                default:
                    throw ExceptionUtil.wrapRuntime("Unsupported response type.");
            }
        }
        IoUtil.close(outputStream);
        return CharSequenceUtil.isNotBlank(fileName) ? outputFile : null;
    }

    public ByteArrayOutputStream downloadToMemory() throws IOException {
        // WARNING: put data in memory, and then return it as a stream
        // steaming read with iterator
        Iterator<ServerSideStreamingRpcService.DownloadResponse> iterator = storageServiceStub.download(ServerSideStreamingRpcService.EmptyRequest.newBuilder().build());
        String fileName = null;
        ByteArrayOutputStream outputStream = null;
        int current = 0;
        while (iterator.hasNext()) {
            ServerSideStreamingRpcService.DownloadResponse response = iterator.next();
            switch (response.getDataCase()) {
                case METADATA:
                    log.info("Client download - handle file metadata");
                    ServerSideStreamingRpcService.DownloadResponse.Metadata metadata = response.getMetadata();
                    fileName = metadata.getName();
                    outputStream = new ByteArrayOutputStream();
                    break;
                case CHUNK:
                    log.info("Client download - handle data chunk {}", current++);
                    if (Objects.isNull(outputStream)) {
                        throw ExceptionUtil.wrapRuntime("OutputStream did't init yet. Hint: Client did't receive file metadata request");
                    }
                    outputStream.write(response.getChunk().toByteArray());
                    break;
                case ERROR:
                    throw ExceptionUtil.wrapRuntime("Code " + response.getError().getCode() + "\n" + response.getError().getHint());
                default:
                    throw ExceptionUtil.wrapRuntime("Unsupported response type.");
            }
        }

        return CharSequenceUtil.isNotBlank(fileName) ? outputStream : null;
    }


    @Override
    public void close() {
        ManagedChannel managedChannel = (ManagedChannel) this.storageServiceStub.getChannel();
        try {
            boolean closed;
            if (!managedChannel.isShutdown()) {
                closed = managedChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
                if (!closed) {
                    throw new InterruptedException("Channel shutdown failed");
                }
            } else {
                log.info(">>> Channel has benn already shutdown! <<<");
            }
            log.info(">>> Channel shutdown successfully! <<<");
        } catch (Exception e) {
            log.error("Channel shutdown failed");
            log.error(e.getMessage(), e);
        }
    }
}
