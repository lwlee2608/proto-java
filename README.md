# proto-java
Additional features for Protobuf Java

## proto-java-annotation
Convert Java POJO to [Protocol Buffer files](https://protobuf.dev/overview/)

### Examples:
Simply add `@ProtoMessage` and `@ProtoField` annotation to a POJO.
```java
@ProtoMessage(protoName = "pojo", protoPackage = "testing.pojo")
public class Example {
    @ProtoField(tag = 1)
    private String foo;
    @ProtoField(tag = 2)
    private String bar;
}
```

A .proto file will be generated.
```protobuf
syntax = "proto3";

option java_package = "io.github.lwlee2608.proto.plugin.pojo";
package testing.pojo;

message Example {
  string foo = 1;
  string bar = 2;
}
```