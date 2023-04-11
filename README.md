# proto-java
Additional features for Protobuf Java

### modules:
* proto-java-annotation
* proto-java-default-gen

## proto-java-annotation
This module automatically converts Java POJOs and interfaces to [Protocol Buffer files](https://protobuf.dev/overview/) during 
compile time, without requiring any plugins or explicit invocation. 

### Examples:
Simply add custom annotation `@ProtoMessage` and `@ProtoField` annotation to the POJO.

```java
@ProtoMessage(protoName = "helloworld", protoPackage = "example.helloworld")
public class HelloReply {
    @ProtoField(tag = 1)
    private String name;
}
```

```java
@ProtoMessage(protoName = "helloworld", protoPackage = "example.helloworld")
public class HelloRequest {
    @ProtoField(tag = 1)
    private String message;
    @ProtoField(tag = 2)
    private Integer id;
}
```

And add custom annotation `@ProtoService` and `@ProtoMethod` annotation to an interface.
```java
@ProtoService(protoName = "helloworld", protoPackage = "example.helloworld")
public interface Greeter {
    @ProtoMethod
    void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver);
}
```

A .proto file will be generated in  `target/classes` or `target/test-classes`

## proto-java-default-gen (WIP)
If `protoc` binary is already available in the `PATH`.
This module will automatically generate a default implementation of an interface annotated with `@ProtoService` 
without any plugins or explicit invocation.

