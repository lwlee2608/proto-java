package io.github.lwlee2608.proto.example.helloworld;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

class GreeterTest {

    @Timeout(2)
    @Test
    void testHelloWorld() throws IOException, InterruptedException {
        CountDownLatch serverLatch = new CountDownLatch(1);
        CountDownLatch clientLatch = new CountDownLatch(1);

        Server server = ServerBuilder
                .forPort(8080)
                .addService(new HelloworldProto.GreeterService.GreeterServerImpl(new Greeter() {
                    @Override
                    public CompletableFuture<HelloReply> sayHello(HelloRequest request) {
                        serverLatch.countDown();
                        return CompletableFuture.completedFuture(new HelloReply().setName(request.getMessage() + " World"));
                    }
                }))
                .build();
        server.start();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        Greeter client = new HelloworldProto.GreeterService.GreeterClientImpl(channel, CallOptions.DEFAULT);
        client.sayHello(new HelloRequest().setMessage("Hello").setId(1))
                .thenAccept(reply -> clientLatch.countDown());

        serverLatch.await();
        clientLatch.await();
    }
}