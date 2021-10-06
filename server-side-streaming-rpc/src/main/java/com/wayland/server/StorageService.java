package com.wayland.server;

import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.log.Log;
import com.google.protobuf.ByteString;
import com.wayland._grpc.server_side_streaming_rpc.ServerSideStreamingRpcService;
import com.wayland._grpc.server_side_streaming_rpc.StorageServiceGrpc;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Wayland Zhan[routerhex@qq.com]
 * @since 2021-10-06 下午3:55
 */
public class StorageService extends StorageServiceGrpc.StorageServiceImplBase {

    private final Log log = Log.get();

    @Override
    public void download(ServerSideStreamingRpcService.EmptyRequest request, StreamObserver<ServerSideStreamingRpcService.DownloadResponse> responseObserver) {
        // response - metadata
        String fileName = "Csv_" + RandomUtil.randomString(4) + ".csv";
        String encoding = CharsetUtil.UTF_8;
        responseObserver.onNext(downloadMetaDataResponse(fileName, null, encoding, null));

        // response - chunk,  mock server endpoint csv data
        InputStream is = mockCsvFileData(18, 200000);
        byte[] buffer = new byte[1024 * 1024];
        int length;
        try {
            while ((length = is.read(buffer)) != -1) {
                responseObserver.onNext(downloadChunkResponse(buffer, length));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseObserver.onNext(downloadErrorResponse(e.getMessage(), -1));
        }
        IoUtil.close(is);
        responseObserver.onCompleted();
    }

    private ServerSideStreamingRpcService.DownloadResponse downloadMetaDataResponse(String fileName,
                                                                                    @Nullable Long contentLength,
                                                                                    String encoding,
                                                                                    ServerSideStreamingRpcService.DownloadResponse.ContentType contentType) {
        ServerSideStreamingRpcService.DownloadResponse.Metadata.Builder builder = ServerSideStreamingRpcService.DownloadResponse.Metadata.newBuilder();
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
        return ServerSideStreamingRpcService.DownloadResponse.newBuilder()
                .setMetadata(builder.build())
                .build();
    }

    private ServerSideStreamingRpcService.DownloadResponse downloadChunkResponse(byte[] bytes, int endOffset) {
        return ServerSideStreamingRpcService.DownloadResponse.newBuilder()
                .setChunk(ByteString.copyFrom(bytes, 0, endOffset))
                .build();
    }

    private ServerSideStreamingRpcService.DownloadResponse downloadErrorResponse(String hint, int code) {
        return ServerSideStreamingRpcService.DownloadResponse.newBuilder()
                .setError(
                        ServerSideStreamingRpcService.DownloadResponse.Error.newBuilder()
                                .setCode(code)
                                .setHint(hint)
                                .build()
                )
                .build();
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
