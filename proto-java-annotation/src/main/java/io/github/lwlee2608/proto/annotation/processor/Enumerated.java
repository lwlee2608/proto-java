package io.github.lwlee2608.proto.annotation.processor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Enumerated {
    private String fullClassName;
    private String className;
    private String packageName;
    private List<EnumConstant> constants = new ArrayList<>();

    public Enumerated addConstant(EnumConstant constant) {
        constants.add(constant);
        return this;
    }
}
