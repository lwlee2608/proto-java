package io.github.lwlee2608.proto.annotation.processor;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Method {
    private String methodName;
    private Message inputType;
    private Message outputType;
}
