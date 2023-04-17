package io.github.lwlee2608.proto.example.helloworld;

import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ProtoMessage(protoName = "helloworld", protoPackage = "example.helloworld")
public class AllTypePayload {
    @ProtoField(tag = 1) private String stringField;
    @ProtoField(tag = 2) private Integer integerField;
    @ProtoField(tag = 3) private Long longField;
    @ProtoField(tag = 4) private Float floatField;
    @ProtoField(tag = 5) private Double doubleField;
    @ProtoField(tag = 6) private Boolean booleanField;
    //@ProtoField(tag = 7) private Short shortField;
    //@ProtoField(tag = 8) private Byte[] bytesField;
    @ProtoField(tag = 10) private ResultCode resultCode;
}
