package io.github.lwlee2608.proto.annotation.processor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ProtoFile {
    private String fileName;

    private String outerClassName;
//    private String className;
    private String packageName;
    private String protoPackage;
    private List<Enumerated> enums = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
    private List<Service> services = new ArrayList<>();
    private File generatedFile;

    public ProtoFile addEnum(Enumerated enumerated) {
        enums.add(enumerated);
        return this;
    }

    public ProtoFile addMessage(Message message) {
        messages.add(message);
        return this;
    }

    public ProtoFile addService(Service service) {
        services.add(service);
        return this;
    }
}
