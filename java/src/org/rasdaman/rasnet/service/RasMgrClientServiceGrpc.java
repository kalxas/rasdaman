package org.rasdaman.rasnet.service;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;

@javax.annotation.Generated("by gRPC proto compiler")
public class RasMgrClientServiceGrpc {

  private RasMgrClientServiceGrpc() {}

  public static final String SERVICE_NAME = "rasnet.service.RasMgrClientService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq,
      org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl> METHOD_CONNECT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.RasMgrClientService", "Connect"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq,
      org.rasdaman.rasnet.service.CommonService.Void> METHOD_DISCONNECT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.RasMgrClientService", "Disconnect"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.CommonService.Void.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq,
      org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl> METHOD_OPEN_DB =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.RasMgrClientService", "OpenDb"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq,
      org.rasdaman.rasnet.service.CommonService.Void> METHOD_CLOSE_DB =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.RasMgrClientService", "CloseDb"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.CommonService.Void.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq,
      org.rasdaman.rasnet.service.CommonService.Void> METHOD_KEEP_ALIVE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.RasMgrClientService", "KeepAlive"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.CommonService.Void.getDefaultInstance()));

  public static RasMgrClientServiceStub newStub(io.grpc.Channel channel) {
    return new RasMgrClientServiceStub(channel);
  }

  public static RasMgrClientServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new RasMgrClientServiceBlockingStub(channel);
  }

  public static RasMgrClientServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new RasMgrClientServiceFutureStub(channel);
  }

  public static interface RasMgrClientService {

    public void connect(org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl> responseObserver);

    public void disconnect(org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver);

    public void openDb(org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl> responseObserver);

    public void closeDb(org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver);

    public void keepAlive(org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver);
  }

  public static interface RasMgrClientServiceBlockingClient {

    public org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl connect(org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq request);

    public org.rasdaman.rasnet.service.CommonService.Void disconnect(org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq request);

    public org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl openDb(org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq request);

    public org.rasdaman.rasnet.service.CommonService.Void closeDb(org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq request);

    public org.rasdaman.rasnet.service.CommonService.Void keepAlive(org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq request);
  }

  public static interface RasMgrClientServiceFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl> connect(
        org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> disconnect(
        org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl> openDb(
        org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> closeDb(
        org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> keepAlive(
        org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq request);
  }

  public static class RasMgrClientServiceStub extends io.grpc.stub.AbstractStub<RasMgrClientServiceStub>
      implements RasMgrClientService {
    private RasMgrClientServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RasMgrClientServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RasMgrClientServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RasMgrClientServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void connect(org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CONNECT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void disconnect(org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DISCONNECT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void openDb(org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_OPEN_DB, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void closeDb(org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CLOSE_DB, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void keepAlive(org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_KEEP_ALIVE, getCallOptions()), request, responseObserver);
    }
  }

  public static class RasMgrClientServiceBlockingStub extends io.grpc.stub.AbstractStub<RasMgrClientServiceBlockingStub>
      implements RasMgrClientServiceBlockingClient {
    private RasMgrClientServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RasMgrClientServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RasMgrClientServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RasMgrClientServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl connect(org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_CONNECT, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.CommonService.Void disconnect(org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_DISCONNECT, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl openDb(org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_OPEN_DB, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.CommonService.Void closeDb(org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_CLOSE_DB, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.CommonService.Void keepAlive(org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_KEEP_ALIVE, getCallOptions()), request);
    }
  }

  public static class RasMgrClientServiceFutureStub extends io.grpc.stub.AbstractStub<RasMgrClientServiceFutureStub>
      implements RasMgrClientServiceFutureClient {
    private RasMgrClientServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RasMgrClientServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RasMgrClientServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RasMgrClientServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl> connect(
        org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CONNECT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> disconnect(
        org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DISCONNECT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl> openDb(
        org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_OPEN_DB, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> closeDb(
        org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CLOSE_DB, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> keepAlive(
        org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_KEEP_ALIVE, getCallOptions()), request);
    }
  }

  public static io.grpc.ServerServiceDefinition bindService(
      final RasMgrClientService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
      .addMethod(
        METHOD_CONNECT,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq,
              org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.RasmgrClientService.ConnectReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.RasmgrClientService.ConnectRepl> responseObserver) {
              serviceImpl.connect(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_DISCONNECT,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq,
              org.rasdaman.rasnet.service.CommonService.Void>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.RasmgrClientService.DisconnectReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
              serviceImpl.disconnect(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_OPEN_DB,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq,
              org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.RasmgrClientService.OpenDbReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.RasmgrClientService.OpenDbRepl> responseObserver) {
              serviceImpl.openDb(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_CLOSE_DB,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq,
              org.rasdaman.rasnet.service.CommonService.Void>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.RasmgrClientService.CloseDbReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
              serviceImpl.closeDb(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_KEEP_ALIVE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq,
              org.rasdaman.rasnet.service.CommonService.Void>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.RasmgrClientService.KeepAliveReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
              serviceImpl.keepAlive(request, responseObserver);
            }
          })).build();
  }
}
