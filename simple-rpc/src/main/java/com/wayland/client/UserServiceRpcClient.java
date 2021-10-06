package com.wayland.client;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.log.Log;
import cn.hutool.setting.dialect.PropsUtil;
import com.wayland._grpc.simple_rpc.SimpleRpcService;
import com.wayland._grpc.simple_rpc.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-09-14 下午9:35
 */
public class UserServiceRpcClient implements AutoCloseable {

    private UserServiceGrpc.UserServiceBlockingStub blockingStub;

    private final Log log = Log.get();

    public static UserServiceRpcClient defaultClient() {
        Integer port = PropsUtil.get("application.properties").getInt("user-service.port");
        String host = PropsUtil.get("application.properties").getStr("user-service.host");
        UserServiceRpcClient client = new UserServiceRpcClient();
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext() // problem occur without this option but not when ssl(https)
                .build();
        client.blockingStub = UserServiceGrpc.newBlockingStub(channel);
        return client;
    }

    public SimpleRpcService.CommonResponse login(String userName, String pwd) {

        SimpleRpcService.CommonResponse response = blockingStub.login(SimpleRpcService.LoginRequest.newBuilder()
                .setUsername(userName)
                .setPassword(pwd)
                .build()
        );
        log.info("Response result: code {}, message {}, data {}",
                response.getCode(),
                response.getMessage(),
                CharSequenceUtil.isBlank(response.getData()) ? "null" : response.getData()
        );
        return response;
    }

    public SimpleRpcService.CommonResponse logout() {
        SimpleRpcService.CommonResponse response = blockingStub.logout(SimpleRpcService.EmptyRequest.newBuilder().build());
        log.info("Response result: code {}, message {}, data {}",
                response.getCode(),
                response.getMessage(),
                response.getData()
        );
        return response;
    }

    @Override
    public void close() {
        ManagedChannel managedChannel = (ManagedChannel) this.blockingStub.getChannel();
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
