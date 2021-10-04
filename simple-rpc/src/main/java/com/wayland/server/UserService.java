
package com.wayland.server;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Objects;
import com.wayland._grpc.simple_rpc.SimpleRpcService;
import com.wayland._grpc.simple_rpc.UserServiceGrpc;
import io.grpc.stub.StreamObserver;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-09-14 下午9:29
 */
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private static class User {
        private Integer id;

        public User(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    @Override
    public void login(SimpleRpcService.LoginRequest request, StreamObserver<SimpleRpcService.CommonResponse> responseObserver) {
        String userName = request.getUsername();
        String password = request.getPassword();
        SimpleRpcService.CommonResponse.Builder responseBuilder = SimpleRpcService.CommonResponse.newBuilder();
        if (Objects.equal(userName, password)) {
            responseBuilder.setCode(0).setMessage("SUCCESS").setData(JSON.toJSONString(new User(1)));
        } else {
            responseBuilder.setCode(-1).setMessage("FAILED");
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void logout(SimpleRpcService.EmptyRequest request, StreamObserver<SimpleRpcService.CommonResponse> responseObserver) {
        SimpleRpcService.CommonResponse.Builder responseBuilder = SimpleRpcService.CommonResponse.newBuilder();
        responseBuilder.setCode(1).setMessage("SUCCESS").setData("Empty User");
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
