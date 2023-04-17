package io.github.lwlee2608.proto.annotation.example;

import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;

@ProtoMessage(protoName = "example", protoPackage = "testing.example")
public class NestedPojo {
    @ProtoField(tag = 1) private String foo;
    @ProtoField(tag = 2) private Integer bar;
}
