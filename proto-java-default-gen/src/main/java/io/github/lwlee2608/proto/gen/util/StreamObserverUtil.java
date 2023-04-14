package io.github.lwlee2608.proto.gen.util;

import io.grpc.stub.StreamObserver;

import java.util.function.Function;

public class StreamObserverUtil {
    public static <T, R> StreamObserver<T> transform(StreamObserver<R> observer, Function<T, R> transformer) {
        return new StreamObserver<>() {
            @Override
            public void onNext(T t) {
                observer.onNext(transformer.apply(t));
            }
            @Override
            public void onError(Throwable error) {
                observer.onError(error);
            }
            @Override
            public void onCompleted() {
                observer.onCompleted();
            }
        };
    }
}
