package io.github.lwlee2608.proto.gen;

import io.github.lwlee2608.proto.annotation.processor.AsyncType;
import io.github.lwlee2608.proto.annotation.processor.Method;
import io.github.lwlee2608.proto.annotation.processor.ProtoFile;
import io.github.lwlee2608.proto.annotation.processor.Service;
import lombok.SneakyThrows;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.List;

public class ProtoVerxGenImpl extends ProtoGenImpl implements ProtoGen {
    @Override
    public void generate(Filer filer, List<ProtoFile> protoFiles) {
        super.generate(filer, protoFiles);
        generateVerxServerImpl(filer, protoFiles);
        generateVerxClientImpl(filer, protoFiles);
    }

    @SneakyThrows
    public void generateVerxServerImpl(Filer filer, List<ProtoFile> protoFiles) {
        for (ProtoFile protoFile : protoFiles) {
            String protoClassName = protoFile.getOuterClassName() + "Proto";
            String className = protoFile.getOuterClassName() + "VertxGrpcServer";
            String fullClassName = protoFile.getPackageName() + "." + className;

            JavaFileObject builderFile = filer.createSourceFile(fullClassName);
            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                out.println("package " + protoFile.getPackageName() + ";");
                out.println("");
                out.println("import io.reactivex.Single;");
                out.println("import io.vertx.core.Handler;");
                out.println("import io.vertx.reactivex.core.Vertx;");
                out.println("import io.vertx.reactivex.core.http.HttpServerRequest;");
                out.println("import io.vertx.reactivex.grpc.server.GrpcServer;");
                out.println("");
                out.println("public class " + className + " {");
                out.println("");
                out.println("    private final GrpcServer server;");
                out.println("");
                out.println("    public " + className + "(Vertx vertx) {");
                out.println("        this.server = GrpcServer.server(vertx);");
                out.println("    }");
                out.println("");
                out.println("    public Handler<HttpServerRequest> getGrpcServer() {");
                out.println("        return server;");
                out.println("    }");
                out.println("");

                for (Service service : protoFile.getServices()) {
                    out.println("    public interface " + service.getServiceName() + "Api {");
                    for (Method method : service.getMethods()) {
                        String inputType = method.getInputType().getClassName();
                        String outputType = method.getOutputType().getClassName();
                        if (method.getAsyncType() == AsyncType.STREAM_OBSERVER) {
                            out.println("        Single<Void> " + method.getMethodName() + "(" + inputType + " request);");
                        } else if (method.getAsyncType() == AsyncType.COMPLETABLE_FUTURE) {
                            out.println("        Single<" + outputType + "> " + method.getMethodName() + "(" + inputType + " request);");
                        }
                        out.println("");
                    }
                    out.println("    }");
                    out.println("");

                    out.println("    public " + className + " callHandlers(" + service.getServiceName() + "Api implementation) {");
                    for (Method method : service.getMethods()) {
                        String inputType = method.getInputType().getClassName();
                        String outputType = method.getOutputType().getClassName();
                        out.println("        server.callHandler(" + protoClassName + "." + service.getServiceName() + "Service." + method.getMethodName() + "Method, request -> {");
                        out.println("            request.handler(req -> implementation." + method.getMethodName() + "(" + protoClassName + "." + inputType + "Message.fromProto(req))");

                        if (method.getAsyncType() == AsyncType.STREAM_OBSERVER) {
                            out.println("                    .doOnSuccess(resp -> request.response().end())");
                        } else if (method.getAsyncType() == AsyncType.COMPLETABLE_FUTURE) {
                            out.println("                    .doOnSuccess(resp -> request.response().end(" + protoClassName + "." + outputType + "Message.toProto(resp)))");
                        }
                        out.println("                    .subscribe()");
                        out.println("            );");
                        out.println("        });");
                        out.println("");
                    }
                    out.println("        return this;");
                    out.println("    }");

                }

                // end class
                out.println("}");
                out.println("");
            }
        }
    }

    @SneakyThrows
    public void generateVerxClientImpl(Filer filer, List<ProtoFile> protoFiles) {
        for (ProtoFile protoFile : protoFiles) {
            String protoClassName = protoFile.getOuterClassName() + "Proto";
            String className = protoFile.getOuterClassName() + "VertxGrpcClient";
            String fullClassName = protoFile.getPackageName() + "." + className;

            JavaFileObject builderFile = filer.createSourceFile(fullClassName);
            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                out.println("package " + protoFile.getPackageName() + ";");
                out.println("");
                out.println("import io.reactivex.Single;");
                out.println("import io.vertx.reactivex.core.Vertx;");
                out.println("import io.vertx.reactivex.core.net.SocketAddress;");
                out.println("import io.vertx.reactivex.grpc.client.GrpcClient;");
                out.println("");
                out.println("public class " + className + " {");
                out.println("");
                out.println("    private final GrpcClient client;");
                out.println("    private final SocketAddress socketAddress;");
                out.println("");
                out.println("    public " + className + "(Vertx vertx, SocketAddress socketAddress) {");
                out.println("        this.client = GrpcClient.client(vertx);");
                out.println("        this.socketAddress = socketAddress;");
                out.println("    }");
                out.println("");

                for (Service service : protoFile.getServices()) {

                    for(Method method: service.getMethods()) {
                        String inputType = method.getInputType().getClassName();
                        String outputType = method.getOutputType().getClassName();
                        if (method.getAsyncType() == AsyncType.STREAM_OBSERVER) {
                            out.println("    public Single<Void> " + method.getMethodName() + "(" + inputType +" request) {");
                        } else if (method.getAsyncType() == AsyncType.COMPLETABLE_FUTURE) {
                            out.println("    public Single<" + outputType + "> " + method.getMethodName() + "(" + inputType + " request) {");
                        }
                        out.println("        return Single.create(emitter -> client.request(socketAddress, " + protoClassName + "." + service.getServiceName() + "Service." + method.getMethodName() + "Method)");
                        out.println("                .compose(req -> {");
                        out.println("                    req.end(" + protoClassName + "." + inputType + "Message.toProto(request));");
                        out.println("                    return req.response().compose(resp -> resp.last());");
                        out.println("                }).onFailure(error -> emitter.onError(error))");
                        if (method.getAsyncType() == AsyncType.STREAM_OBSERVER) {
                            out.println("                .onSuccess(resp -> emitter.onSuccess(null))");
                        } else if (method.getAsyncType() == AsyncType.COMPLETABLE_FUTURE) {
                            out.println("                .onSuccess(resp -> emitter.onSuccess(" + protoClassName + "." + outputType + "Message.fromProto(resp)))");
                        }
                        out.println("        );");
                        out.println("    }");
                        out.println("");
                    }

                }

                // end class
                out.println("}");
                out.println("");
            }
        }
    }
}
