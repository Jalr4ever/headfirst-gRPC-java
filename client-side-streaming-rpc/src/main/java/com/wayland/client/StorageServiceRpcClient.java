package com.wayland.client;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.log.Log;
import cn.hutool.setting.dialect.PropsUtil;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.wayland._grpc.client_side_streaming_rpc.ClientSideStreamingRpcService;
import com.wayland._grpc.client_side_streaming_rpc.StorageServiceGrpc;
import com.wayland.observer.IUploadResponseObserver;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-10-04 下午11:46
 */
public class StorageServiceRpcClient implements AutoCloseable {

    private StorageServiceGrpc.StorageServiceStub storageServiceStub;

    private final Log log = Log.get();

    public static StorageServiceRpcClient defaultClient() {
        Integer port = PropsUtil.get("application.properties").getInt("storage-upload-service.port");
        String host = PropsUtil.get("application.properties").getStr("storage-upload-service.host");
        StorageServiceRpcClient client = new StorageServiceRpcClient();
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext() // problem occur without this option but not when ssl(https)
                .build();
        client.storageServiceStub = StorageServiceGrpc.newStub(channel);
        return client;
    }

    public ClientSideStreamingRpcService.CommonResponse upload(String fileName,
                                                               Long contentLength,
                                                               String encoding,
                                                               ClientSideStreamingRpcService.UploadRequest.ContentType contentType,
                                                               InputStream is) throws IOException {

        CountDownLatch cdl = ThreadUtil.newCountDownLatch(1);

        // response
        IUploadResponseObserver<ClientSideStreamingRpcService.CommonResponse> responseObserver = new IUploadResponseObserver<ClientSideStreamingRpcService.CommonResponse>() {
            private ClientSideStreamingRpcService.CommonResponse response;

            @Override
            public ClientSideStreamingRpcService.CommonResponse response() {
                return response;
            }

            @Override
            public void onNext(ClientSideStreamingRpcService.CommonResponse value) {
                response = value;
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                cdl.countDown();
            }
        };

        // request
        StreamObserver<ClientSideStreamingRpcService.UploadRequest> requestObserver = storageServiceStub.upload(responseObserver);
        // request meta info
        requestObserver.onNext(uploadInfoRequest(fileName, contentLength, encoding, contentType));
        // request chunk data info
        byte[] buffer = new byte[1024 * 1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            requestObserver.onNext(uploadChunkRequest(buffer, length));
        }
        requestObserver.onCompleted();
        try {
            boolean timeout = !cdl.await(5, TimeUnit.SECONDS);
            if (timeout) {
                throw ExceptionUtil.wrapRuntime("Client wait for server response timeout");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // result
        if (Objects.isNull(responseObserver.response())) {
            throw ExceptionUtil.wrapRuntime("Result is null");
        }
        String data = JSON.toJSONString(responseObserver.response().getData());
        String msg = JSON.toJSONString(responseObserver.response().getMessage());
        String code = JSON.toJSONString(responseObserver.response().getCode());
        log.info("Result is code {}, msg {}, data {}", code, msg, data);
        return responseObserver.response();
    }

    public ClientSideStreamingRpcService.CommonResponse upload(String fileName, InputStream is) throws IOException {
        return upload(fileName, null, CharsetUtil.UTF_8, null, is);
    }

    public ClientSideStreamingRpcService.CommonResponse upload(String fileName, InputStream is, String encoding) throws IOException {
        return upload(fileName, null, encoding, null, is);
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

    private ClientSideStreamingRpcService.UploadRequest uploadInfoRequest(String fileName,
                                                                          @Nullable Long contentLength,
                                                                          String encoding,
                                                                          ClientSideStreamingRpcService.UploadRequest.ContentType contentType) {
        ClientSideStreamingRpcService.UploadRequest.Request.Builder builder = ClientSideStreamingRpcService.UploadRequest.Request.newBuilder();
        if (Objects.nonNull(contentLength)) {
            builder.setContentLength(contentLength);
        }
        if (Objects.nonNull(encoding)) {
            builder.setContentEncoding(encoding);
        }
        if (Objects.nonNull(contentType)) {
            builder.setContentType(contentType);
        }
        builder.setName(fileName);
        return ClientSideStreamingRpcService.UploadRequest.newBuilder()
                .setRequest(builder.build())
                .build();
    }

    public ClientSideStreamingRpcService.UploadRequest uploadChunkRequest(byte[] bytes, int endOffset) {
        return ClientSideStreamingRpcService.UploadRequest.newBuilder()
                .setChunk(ByteString.copyFrom(bytes, 0, endOffset))
                .build();
    }


}
