package io.github.lwlee2608.proto.annotation;

@ProtoMessage(protoName = "pojo", protoPackage = "testing.pojo")
public class Example {
    @ProtoField(tag = 1)
    private String foo;
    @ProtoField(tag = 2)
    private String bar;
}
