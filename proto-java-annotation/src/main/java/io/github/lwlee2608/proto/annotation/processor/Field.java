package io.github.lwlee2608.proto.annotation.processor;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Field {
    private String name;
    private String javaType;
    private String protoType;
    private Integer tag;
}
