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
public class ClientRassrvrServiceGrpc {

  private ClientRassrvrServiceGrpc() {}

  public static final String SERVICE_NAME = "rasnet.service.ClientRassrvrService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq,
      org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl> METHOD_OPEN_SERVER_DATABASE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "OpenServerDatabase"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq,
      org.rasdaman.rasnet.service.CommonService.Void> METHOD_CLOSE_SERVER_DATABASE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "CloseServerDatabase"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.CommonService.Void.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq,
      org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl> METHOD_CREATE_DATABASE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "CreateDatabase"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq,
      org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl> METHOD_DESTROY_DATABASE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "DestroyDatabase"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq,
      org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl> METHOD_BEGIN_TRANSACTION =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "BeginTransaction"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq,
      org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl> METHOD_COMMIT_TRANSACTION =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "CommitTransaction"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq,
      org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl> METHOD_ABORT_TRANSACTION =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "AbortTransaction"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq,
      org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl> METHOD_IS_TRANSACTION_OPEN =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "IsTransactionOpen"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq,
      org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl> METHOD_START_INSERT_MDD =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "StartInsertMDD"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq,
      org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl> METHOD_START_INSERT_TRANS_MDD =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "StartInsertTransMDD"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq,
      org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl> METHOD_INSERT_TILE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "InsertTile"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq,
      org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl> METHOD_END_INSERT_MDD =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "EndInsertMDD"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq,
      org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl> METHOD_INSERT_COLLECTION =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "InsertCollection"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq,
      org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl> METHOD_DELETE_COLLECTION_BY_NAME =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "DeleteCollectionByName"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq,
      org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl> METHOD_DELETE_COLLECTION_BY_OID =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "DeleteCollectionByOid"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq,
      org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl> METHOD_REMOVE_OBJECT_FROM_COLLECTION =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "RemoveObjectFromCollection"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq,
      org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl> METHOD_GET_COLLECTION_BY_NAME_OR_OID =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "GetCollectionByNameOrOid"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq,
      org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl> METHOD_GET_COLL_OIDS_BY_NAME_OR_OID =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "GetCollOidsByNameOrOid"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq,
      org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl> METHOD_GET_NEXT_MDD =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "GetNextMDD"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq,
      org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl> METHOD_GET_NEXT_TILE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "GetNextTile"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq,
      org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl> METHOD_END_TRANSFER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "EndTransfer"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq,
      org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl> METHOD_INIT_UPDATE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "InitUpdate"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq,
      org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl> METHOD_EXECUTE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "ExecuteQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq,
      org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl> METHOD_EXECUTE_HTTP_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "ExecuteHttpQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq,
      org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl> METHOD_GET_NEXT_ELEMENT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "GetNextElement"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq,
      org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl> METHOD_EXECUTE_UPDATE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "ExecuteUpdateQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq,
      org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl> METHOD_EXECUTE_INSERT_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "ExecuteInsertQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq,
      org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl> METHOD_GET_NEW_OID =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "GetNewOid"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq,
      org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl> METHOD_GET_OBJECT_TYPE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "GetObjectType"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq,
      org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl> METHOD_GET_TYPE_STRUCTURE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "GetTypeStructure"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq,
      org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl> METHOD_SET_FORMAT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "SetFormat"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest,
      org.rasdaman.rasnet.service.CommonService.Void> METHOD_KEEP_ALIVE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "rasnet.service.ClientRassrvrService", "KeepAlive"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.rasdaman.rasnet.service.CommonService.Void.getDefaultInstance()));

  public static ClientRassrvrServiceStub newStub(io.grpc.Channel channel) {
    return new ClientRassrvrServiceStub(channel);
  }

  public static ClientRassrvrServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ClientRassrvrServiceBlockingStub(channel);
  }

  public static ClientRassrvrServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ClientRassrvrServiceFutureStub(channel);
  }

  public static interface ClientRassrvrService {

    public void openServerDatabase(org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl> responseObserver);

    public void closeServerDatabase(org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver);

    public void createDatabase(org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl> responseObserver);

    public void destroyDatabase(org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl> responseObserver);

    public void beginTransaction(org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl> responseObserver);

    public void commitTransaction(org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl> responseObserver);

    public void abortTransaction(org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl> responseObserver);

    public void isTransactionOpen(org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl> responseObserver);

    public void startInsertMDD(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl> responseObserver);

    public void startInsertTransMDD(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl> responseObserver);

    public void insertTile(org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl> responseObserver);

    public void endInsertMDD(org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl> responseObserver);

    public void insertCollection(org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl> responseObserver);

    public void deleteCollectionByName(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl> responseObserver);

    public void deleteCollectionByOid(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl> responseObserver);

    public void removeObjectFromCollection(org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl> responseObserver);

    public void getCollectionByNameOrOid(org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl> responseObserver);

    public void getCollOidsByNameOrOid(org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl> responseObserver);

    public void getNextMDD(org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl> responseObserver);

    public void getNextTile(org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl> responseObserver);

    public void endTransfer(org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl> responseObserver);

    public void initUpdate(org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl> responseObserver);

    public void executeQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl> responseObserver);

    public void executeHttpQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl> responseObserver);

    public void getNextElement(org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl> responseObserver);

    public void executeUpdateQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl> responseObserver);

    public void executeInsertQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl> responseObserver);

    public void getNewOid(org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl> responseObserver);

    public void getObjectType(org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl> responseObserver);

    public void getTypeStructure(org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl> responseObserver);

    public void setFormat(org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl> responseObserver);

    public void keepAlive(org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver);
  }

  public static interface ClientRassrvrServiceBlockingClient {

    public org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl openServerDatabase(org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq request);

    public org.rasdaman.rasnet.service.CommonService.Void closeServerDatabase(org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl createDatabase(org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl destroyDatabase(org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl beginTransaction(org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl commitTransaction(org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl abortTransaction(org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl isTransactionOpen(org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl startInsertMDD(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl startInsertTransMDD(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl insertTile(org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl endInsertMDD(org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl insertCollection(org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl deleteCollectionByName(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl deleteCollectionByOid(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl removeObjectFromCollection(org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl getCollectionByNameOrOid(org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl getCollOidsByNameOrOid(org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl getNextMDD(org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl getNextTile(org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl endTransfer(org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl initUpdate(org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl executeQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl executeHttpQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl getNextElement(org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl executeUpdateQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl executeInsertQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl getNewOid(org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl getObjectType(org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl getTypeStructure(org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq request);

    public org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl setFormat(org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq request);

    public org.rasdaman.rasnet.service.CommonService.Void keepAlive(org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest request);
  }

  public static interface ClientRassrvrServiceFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl> openServerDatabase(
        org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> closeServerDatabase(
        org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl> createDatabase(
        org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl> destroyDatabase(
        org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl> beginTransaction(
        org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl> commitTransaction(
        org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl> abortTransaction(
        org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl> isTransactionOpen(
        org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl> startInsertMDD(
        org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl> startInsertTransMDD(
        org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl> insertTile(
        org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl> endInsertMDD(
        org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl> insertCollection(
        org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl> deleteCollectionByName(
        org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl> deleteCollectionByOid(
        org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl> removeObjectFromCollection(
        org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl> getCollectionByNameOrOid(
        org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl> getCollOidsByNameOrOid(
        org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl> getNextMDD(
        org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl> getNextTile(
        org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl> endTransfer(
        org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl> initUpdate(
        org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl> executeQuery(
        org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl> executeHttpQuery(
        org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl> getNextElement(
        org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl> executeUpdateQuery(
        org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl> executeInsertQuery(
        org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl> getNewOid(
        org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl> getObjectType(
        org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl> getTypeStructure(
        org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl> setFormat(
        org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq request);

    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> keepAlive(
        org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest request);
  }

  public static class ClientRassrvrServiceStub extends io.grpc.stub.AbstractStub<ClientRassrvrServiceStub>
      implements ClientRassrvrService {
    private ClientRassrvrServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClientRassrvrServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClientRassrvrServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClientRassrvrServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void openServerDatabase(org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_OPEN_SERVER_DATABASE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void closeServerDatabase(org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CLOSE_SERVER_DATABASE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void createDatabase(org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_DATABASE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void destroyDatabase(org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DESTROY_DATABASE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void beginTransaction(org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_BEGIN_TRANSACTION, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void commitTransaction(org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COMMIT_TRANSACTION, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void abortTransaction(org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ABORT_TRANSACTION, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void isTransactionOpen(org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_IS_TRANSACTION_OPEN, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void startInsertMDD(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_START_INSERT_MDD, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void startInsertTransMDD(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_START_INSERT_TRANS_MDD, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void insertTile(org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INSERT_TILE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void endInsertMDD(org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_END_INSERT_MDD, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void insertCollection(org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INSERT_COLLECTION, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void deleteCollectionByName(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DELETE_COLLECTION_BY_NAME, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void deleteCollectionByOid(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DELETE_COLLECTION_BY_OID, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void removeObjectFromCollection(org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REMOVE_OBJECT_FROM_COLLECTION, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getCollectionByNameOrOid(org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_COLLECTION_BY_NAME_OR_OID, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getCollOidsByNameOrOid(org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_COLL_OIDS_BY_NAME_OR_OID, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getNextMDD(org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_MDD, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getNextTile(org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_TILE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void endTransfer(org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_END_TRANSFER, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void initUpdate(org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INIT_UPDATE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void executeQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void executeHttpQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_HTTP_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getNextElement(org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_ELEMENT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void executeUpdateQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_UPDATE_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void executeInsertQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_INSERT_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getNewOid(org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_NEW_OID, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getObjectType(org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_OBJECT_TYPE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getTypeStructure(org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_TYPE_STRUCTURE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void setFormat(org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_FORMAT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void keepAlive(org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest request,
        io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_KEEP_ALIVE, getCallOptions()), request, responseObserver);
    }
  }

  public static class ClientRassrvrServiceBlockingStub extends io.grpc.stub.AbstractStub<ClientRassrvrServiceBlockingStub>
      implements ClientRassrvrServiceBlockingClient {
    private ClientRassrvrServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClientRassrvrServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClientRassrvrServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClientRassrvrServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl openServerDatabase(org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_OPEN_SERVER_DATABASE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.CommonService.Void closeServerDatabase(org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_CLOSE_SERVER_DATABASE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl createDatabase(org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_CREATE_DATABASE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl destroyDatabase(org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_DESTROY_DATABASE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl beginTransaction(org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_BEGIN_TRANSACTION, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl commitTransaction(org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_COMMIT_TRANSACTION, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl abortTransaction(org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_ABORT_TRANSACTION, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl isTransactionOpen(org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_IS_TRANSACTION_OPEN, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl startInsertMDD(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_START_INSERT_MDD, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl startInsertTransMDD(org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_START_INSERT_TRANS_MDD, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl insertTile(org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_INSERT_TILE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl endInsertMDD(org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_END_INSERT_MDD, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl insertCollection(org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_INSERT_COLLECTION, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl deleteCollectionByName(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_DELETE_COLLECTION_BY_NAME, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl deleteCollectionByOid(org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_DELETE_COLLECTION_BY_OID, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl removeObjectFromCollection(org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_REMOVE_OBJECT_FROM_COLLECTION, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl getCollectionByNameOrOid(org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_GET_COLLECTION_BY_NAME_OR_OID, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl getCollOidsByNameOrOid(org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_GET_COLL_OIDS_BY_NAME_OR_OID, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl getNextMDD(org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_MDD, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl getNextTile(org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_TILE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl endTransfer(org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_END_TRANSFER, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl initUpdate(org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_INIT_UPDATE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl executeQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl executeHttpQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_HTTP_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl getNextElement(org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_ELEMENT, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl executeUpdateQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_UPDATE_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl executeInsertQuery(org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_INSERT_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl getNewOid(org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_GET_NEW_OID, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl getObjectType(org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_GET_OBJECT_TYPE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl getTypeStructure(org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_GET_TYPE_STRUCTURE, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl setFormat(org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_SET_FORMAT, getCallOptions()), request);
    }

    @java.lang.Override
    public org.rasdaman.rasnet.service.CommonService.Void keepAlive(org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest request) {
      return blockingUnaryCall(
          getChannel().newCall(METHOD_KEEP_ALIVE, getCallOptions()), request);
    }
  }

  public static class ClientRassrvrServiceFutureStub extends io.grpc.stub.AbstractStub<ClientRassrvrServiceFutureStub>
      implements ClientRassrvrServiceFutureClient {
    private ClientRassrvrServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ClientRassrvrServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClientRassrvrServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClientRassrvrServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl> openServerDatabase(
        org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_OPEN_SERVER_DATABASE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> closeServerDatabase(
        org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CLOSE_SERVER_DATABASE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl> createDatabase(
        org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_DATABASE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl> destroyDatabase(
        org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DESTROY_DATABASE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl> beginTransaction(
        org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_BEGIN_TRANSACTION, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl> commitTransaction(
        org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COMMIT_TRANSACTION, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl> abortTransaction(
        org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ABORT_TRANSACTION, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl> isTransactionOpen(
        org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_IS_TRANSACTION_OPEN, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl> startInsertMDD(
        org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_START_INSERT_MDD, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl> startInsertTransMDD(
        org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_START_INSERT_TRANS_MDD, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl> insertTile(
        org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INSERT_TILE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl> endInsertMDD(
        org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_END_INSERT_MDD, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl> insertCollection(
        org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INSERT_COLLECTION, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl> deleteCollectionByName(
        org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DELETE_COLLECTION_BY_NAME, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl> deleteCollectionByOid(
        org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DELETE_COLLECTION_BY_OID, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl> removeObjectFromCollection(
        org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REMOVE_OBJECT_FROM_COLLECTION, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl> getCollectionByNameOrOid(
        org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_COLLECTION_BY_NAME_OR_OID, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl> getCollOidsByNameOrOid(
        org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_COLL_OIDS_BY_NAME_OR_OID, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl> getNextMDD(
        org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_MDD, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl> getNextTile(
        org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_TILE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl> endTransfer(
        org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_END_TRANSFER, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl> initUpdate(
        org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INIT_UPDATE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl> executeQuery(
        org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl> executeHttpQuery(
        org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_HTTP_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl> getNextElement(
        org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_ELEMENT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl> executeUpdateQuery(
        org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_UPDATE_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl> executeInsertQuery(
        org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXECUTE_INSERT_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl> getNewOid(
        org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_NEW_OID, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl> getObjectType(
        org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_OBJECT_TYPE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl> getTypeStructure(
        org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_TYPE_STRUCTURE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl> setFormat(
        org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_FORMAT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.rasdaman.rasnet.service.CommonService.Void> keepAlive(
        org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_KEEP_ALIVE, getCallOptions()), request);
    }
  }

  public static io.grpc.ServerServiceDefinition bindService(
      final ClientRassrvrService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
      .addMethod(
        METHOD_OPEN_SERVER_DATABASE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq,
              org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.OpenServerDatabaseRepl> responseObserver) {
              serviceImpl.openServerDatabase(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_CLOSE_SERVER_DATABASE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq,
              org.rasdaman.rasnet.service.CommonService.Void>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.CloseServerDatabaseReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
              serviceImpl.closeServerDatabase(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_CREATE_DATABASE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq,
              org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.CreateDatabaseRepl> responseObserver) {
              serviceImpl.createDatabase(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_DESTROY_DATABASE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq,
              org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DestroyDatabaseRepl> responseObserver) {
              serviceImpl.destroyDatabase(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_BEGIN_TRANSACTION,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq,
              org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.BeginTransactionRepl> responseObserver) {
              serviceImpl.beginTransaction(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_COMMIT_TRANSACTION,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq,
              org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.CommitTransactionRepl> responseObserver) {
              serviceImpl.commitTransaction(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_ABORT_TRANSACTION,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq,
              org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.AbortTransactionRepl> responseObserver) {
              serviceImpl.abortTransaction(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_IS_TRANSACTION_OPEN,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq,
              org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.IsTransactionOpenRepl> responseObserver) {
              serviceImpl.isTransactionOpen(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_START_INSERT_MDD,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq,
              org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertMDDRepl> responseObserver) {
              serviceImpl.startInsertMDD(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_START_INSERT_TRANS_MDD,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq,
              org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.StartInsertTransMDDRepl> responseObserver) {
              serviceImpl.startInsertTransMDD(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_INSERT_TILE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq,
              org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.InsertTileReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InsertTileRepl> responseObserver) {
              serviceImpl.insertTile(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_END_INSERT_MDD,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq,
              org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.EndInsertMDDRepl> responseObserver) {
              serviceImpl.endInsertMDD(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_INSERT_COLLECTION,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq,
              org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InsertCollectionRepl> responseObserver) {
              serviceImpl.insertCollection(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_DELETE_COLLECTION_BY_NAME,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq,
              org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByNameRepl> responseObserver) {
              serviceImpl.deleteCollectionByName(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_DELETE_COLLECTION_BY_OID,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq,
              org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.DeleteCollectionByOidRepl> responseObserver) {
              serviceImpl.deleteCollectionByOid(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_REMOVE_OBJECT_FROM_COLLECTION,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq,
              org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.RemoveObjectFromCollectionRepl> responseObserver) {
              serviceImpl.removeObjectFromCollection(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_GET_COLLECTION_BY_NAME_OR_OID,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq,
              org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetCollectionByNameOrOidRepl> responseObserver) {
              serviceImpl.getCollectionByNameOrOid(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_GET_COLL_OIDS_BY_NAME_OR_OID,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq,
              org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetCollOidsByNameOrOidRepl> responseObserver) {
              serviceImpl.getCollOidsByNameOrOid(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_GET_NEXT_MDD,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq,
              org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextMDDRepl> responseObserver) {
              serviceImpl.getNextMDD(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_GET_NEXT_TILE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq,
              org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextTileRepl> responseObserver) {
              serviceImpl.getNextTile(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_END_TRANSFER,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq,
              org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.EndTransferReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.EndTransferRepl> responseObserver) {
              serviceImpl.endTransfer(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_INIT_UPDATE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq,
              org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.InitUpdateRepl> responseObserver) {
              serviceImpl.initUpdate(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_EXECUTE_QUERY,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq,
              org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteQueryRepl> responseObserver) {
              serviceImpl.executeQuery(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_EXECUTE_HTTP_QUERY,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq,
              org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteHttpQueryRepl> responseObserver) {
              serviceImpl.executeHttpQuery(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_GET_NEXT_ELEMENT,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq,
              org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNextElementRepl> responseObserver) {
              serviceImpl.getNextElement(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_EXECUTE_UPDATE_QUERY,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq,
              org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteUpdateQueryRepl> responseObserver) {
              serviceImpl.executeUpdateQuery(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_EXECUTE_INSERT_QUERY,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq,
              org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.ExecuteInsertQueryRepl> responseObserver) {
              serviceImpl.executeInsertQuery(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_GET_NEW_OID,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq,
              org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetNewOidRepl> responseObserver) {
              serviceImpl.getNewOid(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_GET_OBJECT_TYPE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq,
              org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetObjectTypeRepl> responseObserver) {
              serviceImpl.getObjectType(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_GET_TYPE_STRUCTURE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq,
              org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.GetTypeStructureRepl> responseObserver) {
              serviceImpl.getTypeStructure(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_SET_FORMAT,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq,
              org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.SetFormatReq request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.ClientRasServerService.SetFormatRepl> responseObserver) {
              serviceImpl.setFormat(request, responseObserver);
            }
          }))
      .addMethod(
        METHOD_KEEP_ALIVE,
        asyncUnaryCall(
          new io.grpc.stub.ServerCalls.UnaryMethod<
              org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest,
              org.rasdaman.rasnet.service.CommonService.Void>() {
            @java.lang.Override
            public void invoke(
                org.rasdaman.rasnet.service.ClientRasServerService.KeepAliveRequest request,
                io.grpc.stub.StreamObserver<org.rasdaman.rasnet.service.CommonService.Void> responseObserver) {
              serviceImpl.keepAlive(request, responseObserver);
            }
          })).build();
  }
}
