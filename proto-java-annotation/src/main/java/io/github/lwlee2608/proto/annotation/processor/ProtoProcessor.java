package io.github.lwlee2608.proto.annotation.processor;

import com.google.auto.service.AutoService;
import io.github.lwlee2608.proto.annotation.ProtoField;
import io.github.lwlee2608.proto.annotation.ProtoMessage;
import io.github.lwlee2608.proto.annotation.ProtoService;
import io.github.lwlee2608.proto.gen.ClassFinder;
import io.github.lwlee2608.proto.gen.ProtoGen;
import lombok.SneakyThrows;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SupportedAnnotationTypes({
        "io.github.lwlee2608.proto.annotation.ProtoMessage",
        "io.github.lwlee2608.proto.annotation.ProtoField",
        "io.github.lwlee2608.proto.annotation.ProtoService",
        "io.github.lwlee2608.proto.annotation.ProtoMethod"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class ProtoProcessor extends AbstractProcessor {

    private static final Map<String, ProtoFile> protoFiles = new HashMap<>();
    private static final Map<String, Message> messages = new HashMap<>();
    private static final Map<String, Service> services = new HashMap<>();
    private static Boolean written = false;

    @SneakyThrows
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
                    String outerClassName = getOuterClassName(protoName);

                    Message message = messages.computeIfAbsent(fullClassName, key -> new Message().setFullClassName(fullClassName));
                    message.setClassName(className);
                    message.setPackageName(packageName);

                    ProtoFile protoFile = protoFiles.computeIfAbsent(protoName, key -> new ProtoFile().setFileName(protoName + ".proto"));
                    protoFile.setOuterClassName(outerClassName);
                    protoFile.setPackageName(packageName);
                    protoFile.setProtoPackage(protoPackage);
                    protoFile.addMessage(message);

//                    System.out.println("Message is " + message);

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

//                    System.out.println("Field is " + fieldName);

                } else if (element.getKind() == ElementKind.INTERFACE) {
                    TypeElement typeElement = (TypeElement) element;
                    String protoPackage = typeElement.getAnnotation(ProtoService.class).protoPackage();
                    String protoName = typeElement.getAnnotation(ProtoService.class).protoName();
                    String fullServiceName = element.asType().toString();
                    String serviceName = getSimpleClass(fullServiceName);
                    String outerClassName = getOuterClassName(protoName);

                    Service service = services.computeIfAbsent(fullServiceName, key -> new Service().setFullServiceName(fullServiceName));
                    service.setServiceName(serviceName);

                    ProtoFile protoFile = protoFiles.computeIfAbsent(protoName, key -> new ProtoFile().setFileName(protoName + ".proto"));
                    protoFile.setOuterClassName(outerClassName);
                    protoFile.addService(service);


                } else if (element.getKind() == ElementKind.METHOD) {
                    // String name = element.asType().toString();
                    ExecutableElement methodElement = (ExecutableElement) element;
                    String methodName = methodElement.getSimpleName().toString();

                    Method method = new Method();
                    method.setMethodName(methodName);

                    // Determine Asynchronous Type
                    String returnType = methodElement.getReturnType().toString();

                    String cf = extractTemplate(returnType, "java.util.concurrent.CompletableFuture<(.*?)>");
                    if (cf != null) {
                        if (methodElement.getParameters().size() != 1) {
                            throw new RuntimeException("Invalid parameters. Only one argument is allowed for CompletableFuture");
                        }

                        String inputName = methodElement.getParameters().get(0).asType().toString();
                        String outputName = cf;
                        Message inputType = messages.computeIfAbsent(inputName, key -> new Message().setFullClassName(inputName));
                        Message outputType = messages.computeIfAbsent(outputName, key -> new Message().setFullClassName(outputName));
                        method.setInputType(inputType);
                        method.setOutputType(outputType);
                        method.setAsyncType(AsyncType.COMPLETABLE_FUTURE);

                    } else if ("void".equals(returnType) ) {
                        if (methodElement.getParameters().size() != 2) {
                            throw new RuntimeException("Invalid parameters. Only two arguments are allowed for StreamObserver");
                        }

                        String inputName = methodElement.getParameters().get(0).asType().toString();
                        String arg1 = methodElement.getParameters().get(1).asType().toString();

                        // Extract Output Type
                        String outputName = extractTemplate(arg1, "io.grpc.stub.StreamObserver<(.*?)>");
                        if (outputName == null) {
                            throw new RuntimeException("Output argument format not supported");
                        }
                        Message inputType = messages.computeIfAbsent(inputName, key -> new Message().setFullClassName(inputName));
                        Message outputType = messages.computeIfAbsent(outputName, key -> new Message().setFullClassName(outputName));
                        method.setInputType(inputType);
                        method.setOutputType(outputType);
                        method.setAsyncType(AsyncType.STREAM_OBSERVER);

                    } else {
                        throw new RuntimeException("Return type of a ProtoMethod must be void or CompletableFuture. Type '" + returnType + "' not supported");
                    }

                    TypeElement serviceElement = (TypeElement) element.getEnclosingElement();
                    String fullServiceName = serviceElement.getQualifiedName().toString();
                    Service service = services.computeIfAbsent(fullServiceName, key -> new Service().setFullServiceName(fullServiceName));
                    service.addMethod(method);
                }
            }
        }

        if (!written) {
            written = true;
            for (Map.Entry<String, ProtoFile> entry : protoFiles.entrySet()) {
                ProtoFile protoFile = entry.getValue();
                File generatedProfoFile = writeProtoFile(protoFile);
                protoFile.setGeneratedFile(generatedProfoFile);
            }

            Class<ProtoGen> clazz = ClassFinder.findClass(ProtoGen.class, "io.github.lwlee2608.proto.gen");
            if (!clazz.isInterface()) {
                ProtoGen gen = clazz.getDeclaredConstructor().newInstance();
                gen.generate(processingEnv.getFiler(), new ArrayList<>(protoFiles.values()));
            }
        }

        return true;
    }

    private File writeProtoFile(ProtoFile protoFile) throws IOException {
        String fileName = protoFile.getFileName();
        String packageName = protoFile.getPackageName();
        String protoPackage = protoFile.getProtoPackage();

        FileObject resourceFile = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", fileName);

        try (PrintWriter out = new PrintWriter(resourceFile.openWriter())) {
            out.println("syntax = \"proto3\";");
            //out.println("");
            //out.println("import \"google/protobuf/wrappers.proto\";");
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
            for (Service service : protoFile.getServices()) {
                out.println("service " + service.getServiceName() + " {");
                for (Method method: service.getMethods()) {
                    out.println(String.format("    rpc %s (%s) returns (%s);",
                            method.getMethodName(),
                            method.getInputType().getClassName(),
                            method.getOutputType().getClassName()));
                }
                out.println("}");
                out.println("");
            }
        }

        return Paths.get(resourceFile.toUri()).toFile();
    }

    private String getPackage(String fullyQualifiedClassName) {
        String[] split = fullyQualifiedClassName.split("\\.");
        return String.join(".", Arrays.copyOfRange(split, 0, split.length - 1));
    }

    private String getSimpleClass(String fullyQualifiedClassName) {
        String[] split = fullyQualifiedClassName.split("\\.");
        return split[split.length - 1];
    }

    private String getOuterClassName(String protoName) {
        return protoName.substring(0, 1).toUpperCase() + protoName.substring(1);
    }

    private String extractTemplate(String inputString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
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
