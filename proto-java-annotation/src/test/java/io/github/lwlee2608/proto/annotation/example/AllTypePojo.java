package io.github.lwlee2608.proto.annotation.example;

import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;

@ProtoMessage(protoName = "example", protoPackage = "testing.example")
public class AllTypePojo {
    @ProtoField(tag = 1) private String stringField;
    @ProtoField(tag = 2) private Integer integerField;
    @ProtoField(tag = 3) private Long longField;
    @ProtoField(tag = 4) private Float floatField;
    @ProtoField(tag = 5) private Double doubleField;
    @ProtoField(tag = 6) private Boolean booleanField;
    @ProtoField(tag = 7) private Short shortField;
    @ProtoField(tag = 8) private Byte[] bytesField;
    @ProtoField(tag = 9) private NestedPojo nestedField;
    @ProtoField(tag = 10) private ResultCode resultCode;
}
