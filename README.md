# proto-java

### Introduction
This project offers an alternative way of working with gRPC in Java. Rather than defining the [Protocol Buffer files](https://protobuf.dev/overview/)
specification in a .proto file, we can now define it in Java using annotations.

### Modules
* proto-java-annotation
* proto-java-default-gen
* proto-java-plugin


## proto-java-annotation
This module automatically converts Java POJOs and interfaces to .proto files during time.

### Examples
Simply add custom annotation `@ProtoMessage` and `@ProtoField` annotation to the POJO.

```java
@ProtoMessage(protoName = "helloworld", protoPackage = "example.helloworld")
public class HelloReply {
    @ProtoField(tag = 1) private String name;
}
```

```java
@ProtoMessage(protoName = "helloworld", protoPackage = "example.helloworld")
public class HelloRequest {
    @ProtoField(tag = 1) private String message;
    @ProtoField(tag = 2) private Integer id;
}
```

And add custom annotation `@ProtoService` and `@ProtoMethod` annotation to an interface.
```java
@ProtoService(protoName = "helloworld", protoPackage = "example.helloworld")
public interface Greeter {
    @ProtoMethod CompletableFuture<HelloReply> sayHello(HelloRequest request);
}
```

A .proto file will be generated in  `target/classes` or `target/test-classes`


## proto-java-default-gen
If `protoc` binary is already available in the `$PATH`, this module automatically generate gRPC client and server implementation 
of interfaces annotated with `@ProtoService`.

### How to apply
Simply add the module as dependency
```xml
    <dependency>
        <groupId>io.github.lwlee2608</groupId>
        <artifactId>proto-java-default-gen</artifactId>
        <version>VERSION</version>
    </dependency>
```

### Example Server
```java
    Server server = ServerBuilder
        .forPort(8080)
        .addService(new HelloworldProto.GreeterService.GreeterServerImpl(new Greeter() {
            @Override
            public CompletableFuture<HelloReply> sayHello(HelloRequest request) {
                return CompletableFuture.completedFuture(new HelloReply().setName(request.getMessage() + " World"));
            }
        }))
        .build();
```

### Example Client
```java
    Greeter client = new HelloworldProto.GreeterService.GreeterClientImpl(channel, CallOptions.DEFAULT);
    client.sayHelloFuture(new HelloRequest().setMessage("Hello").setId(1));
```

View the full example [here](https://github.com/lwlee2608/proto-java/blob/main/examples/src/test/java/io/github/lwlee2608/proto/example/helloworld/GreeterTest.java)


### Supported Method
| Asynchronous Type | Project  | Supported          |
|-------------------|:---------|--------------------|
| StreamObserver    | io.grpc  | :heavy_check_mark: |
| CompletableFuture | JDK      | :heavy_check_mark: |

## proto-java-plugin 
If protoc binary is not available in `$PATH`, use this maven-plugin to automatically download it

### How to apply
Make sure execution phase is set to `process-sources`
```xml
    <plugin>
        <groupId>io.github.lwlee2608</groupId>
        <artifactId>proto-java-plugin</artifactId>
        <version>VERSION</version>
        <executions>
            <execution>
                <id>download-protoc-binary</id>
                <phase>process-sources</phase>
                <goals>
                    <goal>download</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
```