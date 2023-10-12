package io.github.lwlee2608.proto.annotation.example;

import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;

import java.util.List;
import java.util.Map;

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
    @ProtoField(tag = 11) private List<Integer> arrayIntField;
    @ProtoField(tag = 12) private List<String> arrayStringField;
    @ProtoField(tag = 13) private List<NestedPojo> arrayPayloadField;
    @ProtoField(tag = 20) private Map<String, String> metadata;
    @ProtoField(tag = 21) private Map<String, Integer> integerMapField;
}
