package com.wayland.server;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;
import com.wayland._grpc.client_side_streaming_rpc.ClientSideStreamingRpcService;
import com.wayland._grpc.client_side_streaming_rpc.StorageServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-09-23 下午4:21
 */
public class StorageService extends StorageServiceGrpc.StorageServiceImplBase {

    private static final Log log = Log.get();

    @Override
    public StreamObserver<ClientSideStreamingRpcService.UploadRequest> upload(StreamObserver<ClientSideStreamingRpcService.CommonResponse> responseObserver) {
        return new StreamObserver<ClientSideStreamingRpcService.UploadRequest>() {

            private long current;
            private OutputStream outputStream;
            private String fileName;

            @Override
            public void onNext(ClientSideStreamingRpcService.UploadRequest value) {
                switch (value.getDataCase()) {
                    case REQUEST:
                        outputStream = FileUtil.getOutputStream(FileUtil.file("file/" + value.getRequest().getName()));
                        fileName = value.getRequest().getName();
                        log.info("Server handle file {}, metadata", fileName);
                        break;
                    case CHUNK:
                        current++;
                        byte[] chunkData = value.getChunk().toByteArray();
                        log.info("Server handle file {}, chunk-{}, chunk-size(byte) {}", fileName, current, chunkData.length);
                        try {
                            outputStream.write(value.getChunk().toByteArray());
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                        break;
                    default:
                        log.error("Server handle error onNext()");
                        break;
                }
            }

            @Override
            public void onError(Throwable t) {
                log.warn("server onError thread id {}", Thread.currentThread().getId());
                log.error(t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                if (Objects.nonNull(outputStream)) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                current++;
                responseObserver.onNext(ClientSideStreamingRpcService.CommonResponse.newBuilder()
                        .setCode(0)
                        .setMessage("Upload success, see data in target/test-classes/file/output.csv")
                        .build());
                responseObserver.onCompleted();
            }
        };
    }
}
