# proto-java
Additional features for Protobuf Java

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
```protobuf
syntax = "proto3";

import "google/protobuf/wrappers.proto";
option java_package = "io.github.lwlee2608.proto.annotation.example.helloworld";

package example.helloworld;

message HelloRequest {
  string message = 1;
  int32 id = 2;
}

message HelloReply {
  string name = 1;
}

service Greeter {
  rpc sayHello (HelloRequest) returns (HelloRequest);
}
```