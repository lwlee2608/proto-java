package io.github.lwlee2608.proto.example.helloworld;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class GreeterTest {

    @Test
    void testHelloWorld() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        int port = 8080;
        Server server = ServerBuilder
                .forPort(port)
                .addService(new HelloworldProto.GreeterService.GreeterServerImpl(new Greeter() {
                    @Override
                    public CompletableFuture<HelloReply> sayHello(HelloRequest request) {
                        return CompletableFuture.completedFuture(new HelloReply().setName(request.getMessage() + " World"));
                    }
                }))
                .build();
        server.start();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();

        Greeter client = new HelloworldProto.GreeterService.GreeterClientImpl(channel, CallOptions.DEFAULT);
        HelloReply reply = client.sayHello(new HelloRequest().setMessage("Hello")).get(2, TimeUnit.SECONDS);

        Assertions.assertEquals("Hello World", reply.getName());
        server.shutdown();
    }

    @Test
    void testHelloWorldWithPayload() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        int port = 8080;
        Server server = ServerBuilder
                .forPort(port)
                .addService(new HelloworldProto.GreeterService.GreeterServerImpl(new Greeter() {
                    @Override
                    public CompletableFuture<HelloReply> sayHello(HelloRequest request) {
                        return CompletableFuture.completedFuture(new HelloReply()
                                .setName(request.getMessage() + " World")
                                .setPayload(new AllTypePayload()
                                        .setLongField(2L)
                                        .setBooleanField(true)
                                        .setDoubleField(3.1)
                                        .setFloatField(4.2f)
                                        .setResultCode(ResultCode.SUCCESS)
                                        .setArrayIntField(List.of(101, 102))
                                        .setArrayStringField(List.of("Aa1", "Bb2"))
                                        .setArrayPayloadField(List.of(new SimplePayload()
                                                .setStringField("foo")
                                                .setIntegerField(200)))
                                )
                        );
                    }
                }))
                .build();
        server.start();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();

        Greeter client = new HelloworldProto.GreeterService.GreeterClientImpl(channel, CallOptions.DEFAULT);
        HelloReply reply = client.sayHello(new HelloRequest().setMessage("Hello").setId(1)).get(2, TimeUnit.SECONDS);

        Assertions.assertEquals("Hello World", reply.getName());
        Assertions.assertNotNull(reply.getPayload());
        // Nullable Field
        Assertions.assertNull(reply.getPayload().getStringField());
        Assertions.assertNull(reply.getPayload().getIntegerField());
        // Assert Other Data Type
        Assertions.assertEquals(2L, reply.getPayload().getLongField());
        Assertions.assertEquals(true, reply.getPayload().getBooleanField());
        Assertions.assertEquals(3.1, reply.getPayload().getDoubleField());
        Assertions.assertEquals(4.2f, reply.getPayload().getFloatField());
        Assertions.assertEquals(ResultCode.SUCCESS, reply.getPayload().getResultCode());
        Assertions.assertArrayEquals(new Integer[]{101, 102}, reply.getPayload().getArrayIntField().toArray(new Integer[0]));
        Assertions.assertArrayEquals(new String[]{"Aa1", "Bb2"}, reply.getPayload().getArrayStringField().toArray(new String[0]));
        Assertions.assertEquals("foo", reply.getPayload().getArrayPayloadField().get(0).getStringField());
        Assertions.assertEquals(200, reply.getPayload().getArrayPayloadField().get(0).getIntegerField());
        server.shutdown();
    }
}