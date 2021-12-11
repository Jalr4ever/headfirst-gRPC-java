package com.wayland.client;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.log.Log;
import cn.hutool.setting.dialect.PropsUtil;
import com.wayland._grpc.bidi_streaming_rpc.BidiStreamingRpcService;
import com.wayland._grpc.bidi_streaming_rpc.DesensitizeServiceGrpc;
import com.wayland.observer.IDesensitizeResponseObserver;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-12-11 上午10:58
 */
public class DesensitizeServiceRpcClient implements AutoCloseable {

    private DesensitizeServiceGrpc.DesensitizeServiceStub desensitizeServiceStub;

    private final Log log = Log.get();

    public static DesensitizeServiceRpcClient defaultClient() {
        Integer port = PropsUtil.get("application.properties").getInt("desensitize-service.port");
        String host = PropsUtil.get("application.properties").getStr("desensitize-service.host");
        DesensitizeServiceRpcClient client = new DesensitizeServiceRpcClient();
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        client.desensitizeServiceStub = DesensitizeServiceGrpc.newStub(channel);
        return client;
    }

    public List<String> desensitize(List<String> raws,
                                    BidiStreamingRpcService.DesensitizeRequest.Type type) {

        // NOTE: bidi stream allow us get streaming response witch multi response, it's better response as a stream for up layer
        // like StreamSupport、PipeStream pattern...
        CountDownLatch cdl = new CountDownLatch(1);
        IDesensitizeResponseObserver<BidiStreamingRpcService.DesensitizeResponse, List<String>> responseObserver = new IDesensitizeResponseObserver<BidiStreamingRpcService.DesensitizeResponse, List<String>>() {

            private final List<String> results = new ArrayList<>();

            @Override
            public List<String> response() {
                return results;
            }

            @Override
            public void onNext(BidiStreamingRpcService.DesensitizeResponse value) {
                log.warn("[Client receive desensitized result] - {}", value.getData());
                results.add(value.getData());
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage(), t);
                cdl.countDown();
            }

            @Override
            public void onCompleted() {
                cdl.countDown();
            }
        };

        // request
        StreamObserver<BidiStreamingRpcService.DesensitizeRequest> requestObserver = desensitizeServiceStub.desensitize(responseObserver);

        // example here, raws would also as a stream role
        raws.forEach(raw ->
                requestObserver.onNext(
                        BidiStreamingRpcService.DesensitizeRequest.newBuilder()
                                .setData(raw)
                                .setDesensitizeType(BidiStreamingRpcService.DesensitizeRequest.Type.COVER)
                                .build()
                )
        );
        requestObserver.onCompleted();

        try {
            boolean timeout = !cdl.await(5, TimeUnit.SECONDS);
            if (timeout) {
                throw ExceptionUtil.wrapRuntime("Client wait for server response timeout");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        log.warn("Result list size {}", responseObserver.response().size());
        return responseObserver.response();
    }

    @Override
    public void close() {
        ManagedChannel managedChannel = (ManagedChannel) this.desensitizeServiceStub.getChannel();
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


