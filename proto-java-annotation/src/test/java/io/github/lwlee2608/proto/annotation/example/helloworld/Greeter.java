package io.github.lwlee2608.proto.annotation.example.helloworld;

import io.github.lwlee2608.proto.annotation.ProtoMethod;
import io.github.lwlee2608.proto.annotation.ProtoService;
import io.grpc.stub.StreamObserver;

@ProtoService(protoName = "helloworld", protoPackage = "example.helloworld")
public interface Greeter {
    @ProtoMethod
    void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver);
}
