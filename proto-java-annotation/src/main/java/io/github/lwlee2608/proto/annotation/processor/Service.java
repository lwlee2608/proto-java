package io.github.lwlee2608.proto.annotation.processor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Service {
    private String fullServiceName;
    private String serviceName;
    private List<Method> methods = new ArrayList<>();

    public Service addMethod(Method method) {
        methods.add(method);
        return this;
    }
}
