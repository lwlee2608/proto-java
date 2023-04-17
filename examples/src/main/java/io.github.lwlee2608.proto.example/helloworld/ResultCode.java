package io.github.lwlee2608.proto.example.helloworld;

import io.github.lwlee2608.proto.annotation.ProtoEnumConstant;
import io.github.lwlee2608.proto.annotation.ProtoEnumerated;

@ProtoEnumerated(protoName = "helloworld", protoPackage = "example.helloworld")
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

    public static ResultCode valueOf(int value) {
        for (ResultCode resultCode : values()) {
            if (resultCode.value == value) {
                return resultCode;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
