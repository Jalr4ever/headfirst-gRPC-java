package com.wayland.server;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import com.wayland._grpc.bidi_streaming_rpc.BidiStreamingRpcService;
import com.wayland._grpc.bidi_streaming_rpc.DesensitizeServiceGrpc;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-12-11 上午11:00
 */
public class DesensitizeService extends DesensitizeServiceGrpc.DesensitizeServiceImplBase {

    private static final Log log = Log.get();

    @Override
    public StreamObserver<BidiStreamingRpcService.DesensitizeRequest> desensitize(StreamObserver<BidiStreamingRpcService.DesensitizeResponse> responseObserver) {
        return new StreamObserver<BidiStreamingRpcService.DesensitizeRequest>() {
            @Override
            public void onNext(BidiStreamingRpcService.DesensitizeRequest value) {
                String rawData = value.getData();
                responseObserver.onNext(BidiStreamingRpcService.DesensitizeResponse.newBuilder()
                        .setData(desensitize(rawData, value.getDesensitizeType()))
                        .build());
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage(), t);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }

            @Nullable
            private String desensitize(String raw, BidiStreamingRpcService.DesensitizeRequest.Type type) {
                if (StrUtil.isBlank(raw)) {
                    return null;
                }

                if (type.equals(BidiStreamingRpcService.DesensitizeRequest.Type.COVER)) {
                    log.info("[Char cover] ==> raw：{}", raw);
                    int len = raw.length();
                    String result = "";
                    for (int i = 0; i < len; i++) {
                        // Array element index is odd num
                        if ((i & 1) == 1) {
                            result += "**";
                        } else {
                            result += raw.charAt(i);
                        }
                    }
                    log.info("[Char cover] ==> result：{}", result);
                    return result;
                }

                return null;
            }
        };
    }
}
