package com.wayland.observer;

import io.grpc.stub.StreamObserver;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-12-11 上午10:59
 */
public interface IDesensitizeResponseObserver<T, E> extends StreamObserver<T> {
    E response();
}
