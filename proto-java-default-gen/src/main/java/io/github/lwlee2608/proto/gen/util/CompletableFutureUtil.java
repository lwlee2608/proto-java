package io.github.lwlee2608.proto.gen.util;

import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CompletableFutureUtil {
    public static <T, R> StreamObserver<T> fromStreamObserver(CompletableFuture<R> future, Function<T, R> transformer) {
        return new StreamObserver<>() {
            @Override
            public void onNext(T t) {
                future.complete(transformer.apply(t));
            }
            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
            @Override
            public void onCompleted() {
            }
        };
    }

    public static <T, R> void toStreamObserver(CompletableFuture<T> future, StreamObserver<R> streamObserver, Function<T, R> transformer) {
        future.thenAccept(reply -> {
            streamObserver.onNext(transformer.apply(reply));
            streamObserver.onCompleted();
        }).exceptionally(error -> {
            streamObserver.onError(error);
            return null;
        });
    }
}
