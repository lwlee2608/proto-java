package io.github.lwlee2608.proto.annotation.example;

import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;

@ProtoMessage(protoName = "pojo", protoPackage = "testing.pojo")
public class AnotherExample {
    @ProtoField(tag = 1)
    private String var1;
    @ProtoField(tag = 2)
    private Long var2;
}
