PROTO_INPUT_DIR=protomessages
PROTO_OUTPUT_DIR=messages

PROTOBUF_FILES=$(PROTO_INPUT_DIR)/base.proto\
				$(PROTO_INPUT_DIR)/client_rassrvr_service.proto\
				$(PROTO_INPUT_DIR)/common_service.proto\
				$(PROTO_INPUT_DIR)/communication.proto\
				$(PROTO_INPUT_DIR)/rasmgr_client_service.proto\
				$(PROTO_INPUT_DIR)/rasmgr_rasctrl_service.proto\
				$(PROTO_INPUT_DIR)/rasmgr_rassrvr_service.proto\
				$(PROTO_INPUT_DIR)/rassrvr_rasmgr_service.proto\
				$(PROTO_INPUT_DIR)/service.proto\
				$(PROTO_INPUT_DIR)/test_mess.proto

PROTO_SRC=$(PROTO_OUTPUT_DIR)/base.pb.cc $(PROTO_OUTPUT_DIR)/client_rassrvr_service.pb.cc $(PROTO_OUTPUT_DIR)/common_service.pb.cc $(PROTO_OUTPUT_DIR)/communication.pb.cc \
		  $(PROTO_OUTPUT_DIR)/rasmgr_client_service.pb.cc $(PROTO_OUTPUT_DIR)/rasmgr_rassrvr_service.pb.cc $(PROTO_OUTPUT_DIR)/rassrvr_rasmgr_service.pb.cc \
          $(PROTO_OUTPUT_DIR)/rasmgr_rasctrl_service.pb.cc $(PROTO_OUTPUT_DIR)/service.pb.cc $(PROTO_OUTPUT_DIR)/test_mess.pb.cc

noinst_LIBRARIES = librasnet.a
librasnet_a_SOURCES = $(PROTO_SRC) service/client/channel.cc service/client/channelconfig.cc \
                      service/client/clientcontroller.cc service/server/servercontroller.cc service/server/servicemanager.cc \
                      service/server/servicemanagerconfig.cc service/server/servicerequesthandler.cc sockets/client/clientcommunicationhandler.cc \
                      sockets/client/clientpinghandler.cc sockets/client/clientponghandler.cc sockets/client/clientsocket.cc \
                      sockets/client/clientsocketconfig.cc sockets/common/peerstatus.cc sockets/server/clientpool.cc sockets/server/connectrequesthandler.cc \
                      sockets/server/servercommunicationhandler.cc sockets/server/serverpinghandler.cc sockets/server/serverponghandler.cc \
                      sockets/server/serversocket.cc sockets/server/serversocketconfig.cc util/proto/protozmq.cc util/string/stringutils.cc util/proto/zmqutil.cc
librasnet_a_LIBADD = ../../common/src/libcommon.a


$(PROTO_SRC): $(PROTOBUF_FILES)
	echo "Generating buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=$(PROTO_INPUT_DIR) --cpp_out=$(PROTO_OUTPUT_DIR) $?
if ENABLE_JAVA
	$(PROTOC) --proto_path=$(PROTO_INPUT_DIR) --java_out=../../java/src/ $?
endif
