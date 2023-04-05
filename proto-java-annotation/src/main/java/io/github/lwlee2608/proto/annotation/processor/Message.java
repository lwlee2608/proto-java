package io.github.lwlee2608.proto.annotation.processor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Message {
    private String fullClassName;
    private String className;
    private String packageName;
    private List<Field> fields = new ArrayList<>();

    public Message addField(Field field) {
        fields.add(field);
        return this;
    }
}
