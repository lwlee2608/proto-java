package io.github.lwlee2608.proto.example.helloworld;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
        HelloReply reply = client.sayHello(new HelloRequest().setMessage("Hello").setId(1)).get(2, TimeUnit.SECONDS);

        Assertions.assertEquals("Hello World", reply.getName());
    }
}