package io.github.lwlee2608.proto.annotation.example.helloworld;

import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;

@ProtoMessage(protoName = "helloworld", protoPackage = "example.helloworld")
public class HelloRequest {
    @ProtoField(tag = 1)
    private String message;
    @ProtoField(tag = 2)
    private Integer id;
}
