package io.github.lwlee2608.proto.example.helloworld;

import io.github.lwlee2608.proto.annotation.ProtoMethod;
import io.github.lwlee2608.proto.annotation.ProtoService;

import java.util.concurrent.CompletableFuture;

@ProtoService(protoName = "helloworld", protoPackage = "example.helloworld")
public interface Greeter {
    @ProtoMethod CompletableFuture<HelloReply> sayHello(HelloRequest request);
}
