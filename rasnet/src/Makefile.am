PROTO_SRC=messages/base.pb.cc messages/client_rassrvr_service.pb.cc messages/common_service.pb.cc messages/communication.pb.cc \
          messages/rasmgr_client_service.pb.cc messages/rasmgr_rassrvr_service.pb.cc messages/rassrvr_rasmgr_service.pb.cc \
          messages/rasmgr_rasctrl_service.pb.cc messages/service.pb.cc messages/test_mess.pb.cc

noinst_LIBRARIES = librasnet.a
librasnet_a_SOURCES = $(PROTO_SRC) service/client/channel.cc service/client/channelconfig.cc \
                      service/client/clientcontroller.cc service/server/servercontroller.cc service/server/servicemanager.cc \
                      service/server/servicemanagerconfig.cc service/server/servicerequesthandler.cc sockets/client/clientcommunicationhandler.cc \
                      sockets/client/clientpinghandler.cc sockets/client/clientponghandler.cc sockets/client/clientsocket.cc \
                      sockets/client/clientsocketconfig.cc sockets/common/peerstatus.cc sockets/server/clientpool.cc sockets/server/connectrequesthandler.cc \
                      sockets/server/servercommunicationhandler.cc sockets/server/serverpinghandler.cc sockets/server/serverponghandler.cc \
                      sockets/server/serversocket.cc sockets/server/serversocketconfig.cc util/proto/protozmq.cc util/string/stringutils.cc util/proto/zmqutil.cc
librasnet_a_LIBADD = ../../common/src/libcommon.a


$(PROTO_SRC): generate_protobuffers
generate_protobuffers:
	echo "Generating buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/base.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/client_rassrvr_service.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/common_service.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/communication.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/rasmgr_client_service.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/rasmgr_rasctrl_service.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/rasmgr_rassrvr_service.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/rassrvr_rasmgr_service.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/service.proto
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/test_mess.proto
if ENABLE_JAVA
	$(PROTOC) --proto_path=protomessages --java_out=../../java/src/ protomessages/base.proto
	$(PROTOC) --proto_path=protomessages --java_out=../../java/src/ protomessages/client_rassrvr_service.proto
	$(PROTOC) --proto_path=protomessages --java_out=../../java/src/ protomessages/common_service.proto
	$(PROTOC) --proto_path=protomessages --java_out=../../java/src/ protomessages/communication.proto
	$(PROTOC) --proto_path=protomessages --java_out=../../java/src/ protomessages/rasmgr_client_service.proto
	$(PROTOC) --proto_path=protomessages --java_out=../../java/src/ protomessages/service.proto
endif
