package io.github.lwlee2608.proto.annotation.example;

import io.github.lwlee2608.proto.annotation.ProtoEnumConstant;
import io.github.lwlee2608.proto.annotation.ProtoEnumerated;

@ProtoEnumerated(protoName = "example", protoPackage = "testing.example")
public enum ResultCode {
    @ProtoEnumConstant(0)  SUCCESS (0),
    @ProtoEnumConstant(1)  ERROR   (1);

    private final int value;

    private ResultCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
