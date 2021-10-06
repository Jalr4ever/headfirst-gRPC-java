package com.wayland.observer;

import com.wayland._grpc.client_side_streaming_rpc.ClientSideStreamingRpcService;
import io.grpc.stub.StreamObserver;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-10-05 上午11:27
 */
public interface IUploadResponseObserver<T> extends StreamObserver<T> {
    ClientSideStreamingRpcService.CommonResponse response();
}