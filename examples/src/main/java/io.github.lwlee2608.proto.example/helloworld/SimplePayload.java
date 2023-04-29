package io.github.lwlee2608.proto.example.helloworld;

import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ProtoMessage(protoName = "helloworld", protoPackage = "example.helloworld")
public class SimplePayload {
    @ProtoField(tag = 1) private String stringField;
    @ProtoField(tag = 2) private Integer integerField;
}
