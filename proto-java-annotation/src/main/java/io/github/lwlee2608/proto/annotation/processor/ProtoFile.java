package io.github.lwlee2608.proto.annotation.processor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ProtoFile {
    private String fileName;

//    private String fullClassName;
//    private String className;
    private String packageName;
    private String protoPackage;
    private List<Message> messages = new ArrayList<>();

    public ProtoFile addMessage(Message message) {
        messages.add(message);
        return this;
    }
}
