package io.github.lwlee2608.proto.annotation.processor;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EnumConstant {
    private String constant;
    private int value;
}
