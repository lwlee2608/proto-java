package io.github.lwlee2608.proto.annotation.processor;

import com.google.auto.service.AutoService;
import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes({"io.github.lwlee2608.proto.annotation.ProtoMessage", "io.github.lwlee2608.proto.annotation.ProtoField"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class ProtoProcessor extends AbstractProcessor {

    private static final Map<String, ProtoFile> protoFiles = new HashMap<>();
    private static final Map<String, Message> messages = new HashMap<>();
    private static Boolean written = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        for (TypeElement annotation : annotations) {
            for (Element element: roundEnvironment.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() == ElementKind.CLASS) {
                    //String name = element.asType().toString();
                    TypeElement typeElement = (TypeElement) element;
                    String fullClassName = typeElement.getQualifiedName().toString();
                    String className = typeElement.getSimpleName().toString();
                    String packageName = getPackage(fullClassName);
                    String protoPackage = typeElement.getAnnotation(ProtoMessage.class).protoPackage();
                    String protoName = typeElement.getAnnotation(ProtoMessage.class).protoName();

                    Message message = messages.computeIfAbsent(fullClassName, key -> new Message().setFullClassName(className));
                    message.setClassName(className);
                    message.setPackageName(packageName);

                    ProtoFile protoFile = protoFiles.computeIfAbsent(protoName, key -> new ProtoFile().setFileName(protoName + ".proto"));
                    protoFile.setPackageName(packageName);
                    protoFile.setProtoPackage(protoPackage);
                    protoFile.addMessage(message);

                } else if (element.getKind() == ElementKind.FIELD) {
                    TypeElement classElement = (TypeElement) element.getEnclosingElement();
                    String fullClassName = classElement.getQualifiedName().toString();
                    String fieldName = element.getSimpleName().toString();
                    String javaType = element.asType().toString();
                    String protoType = toProtoType(javaType);
                    Integer tag = element.getAnnotation(ProtoField.class).tag();

                    Message message = messages.computeIfAbsent(fullClassName, key -> new Message().setFullClassName(fullClassName));
                    message.addField(new Field()
                            .setName(fieldName)
                            .setJavaType(javaType)
                            .setProtoType(protoType)
                            .setTag(tag));
                }
            }
        }

        if (!written) {
            written = true;
            for (Map.Entry<String, ProtoFile> entry : protoFiles.entrySet()) {
                try {
                    writeProtoFile(entry.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    private void writeProtoFile(ProtoFile protoFile) throws IOException {
        String fileName = protoFile.getFileName();
        String packageName = protoFile.getPackageName();
        String protoPackage = protoFile.getProtoPackage();

        FileObject resourceFile = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", fileName);

        try (PrintWriter out = new PrintWriter(resourceFile.openWriter())) {
            out.println("syntax = \"proto3\";");
            out.println("");
            out.println("import \"google/protobuf/wrappers.proto\";");
            out.println("");
            out.println("option java_package = \"" + packageName + "\";");
            out.println("");
            out.println("package " + protoPackage + ";");
            out.println("");
            for (Message message : protoFile.getMessages()) {
                String className = message.getClassName();
                out.println("message " + className + " {");
                for (Field field : message.getFields()) {
                    out.println(String.format("    %s %s = %d;", field.getProtoType(), field.getName(), field.getTag()));
                }
                out.println("}");
                out.println("");
            }
        }
    }

    private String getPackage(String fullyQualifiedClassName) {
        String[] split = fullyQualifiedClassName.split("\\.");
        return String.join(".", Arrays.copyOfRange(split, 0, split.length - 1));
    }

    private String getSimpleClass(String fullyQualifiedClassName) {
        String[] split = fullyQualifiedClassName.split("\\.");
        return split[split.length - 1];
    }

    private String toProtoType(String javaType) {
        switch (javaType) {
            case "java.lang.String": return "string";
            case "java.lang.Integer": return "int32";
            case "java.lang.Long": return "int64";
            default: throw new RuntimeException("Java type " + javaType + " not supported");
        }
    }
}
