AUTOMAKE_OPTIONS = foreign

if RMANRASNET

PROTOBUF_FILES_CLIENT= protomessages/client_rassrvr_service.proto \
					protomessages/common_service.proto \
					protomessages/rasmgr_client_service.proto

PROTOBUF_FILES_SERVER= protomessages/rasmgr_rasctrl_service.proto \
				protomessages/rasmgr_rassrvr_service.proto \
				protomessages/rassrvr_rasmgr_service.proto

PROTO_SRC_JAVA_CLIENT = ../java/src/org/rasdaman/rasnet/service/RasMgrClientServiceGrpc.java \
                        ../java/src/org/rasdaman/rasnet/service/RasmgrClientService.java \
                        ../java/src/org/rasdaman/rasnet/service/CommonService.java \
                        ../java/src/org/rasdaman/rasnet/service/ClientRasServerService.java \
                        ../java/src/org/rasdaman/rasnet/service/ClientRassrvrServiceGrpc.java

PROTO_SRC_CLIENT= messages/client_rassrvr_service.pb.cc \
           messages/client_rassrvr_service.grpc.pb.cc \
           messages/common_service.pb.cc \
           messages/common_service.grpc.pb.cc \
           messages/rasmgr_client_service.pb.cc \
           messages/rasmgr_client_service.grpc.pb.cc

PROTO_SRC_SERVER=messages/rasmgr_rasctrl_service.pb.cc \
           messages/rasmgr_rasctrl_service.grpc.pb.cc \
           messages/rasmgr_rassrvr_service.pb.cc \
           messages/rasmgr_rassrvr_service.grpc.pb.cc \
           messages/rassrvr_rasmgr_service.pb.cc \
           messages/rassrvr_rasmgr_service.grpc.pb.cc

noinst_LIBRARIES = librasnet.a
librasnet_a_SOURCES = $(PROTO_SRC_CLIENT) $(PROTO_SRC_SERVER)
librasnet_a_CXXFLAGS = @AM_CXXFLAGS@ @DISABLE_PROTOBUF_WARNINGS_FLAGS@

$(PROTO_SRC_CLIENT): $(PROTOBUF_FILES_CLIENT)
	echo "Generating client Protocol Buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=protomessages --grpc_out=messages --plugin=protoc-gen-grpc=${GRPCCPPPLUGIN} $?
	$(PROTOC) --proto_path=protomessages --cpp_out=messages $?

if ENABLE_JAVA
all-local: $(PROTO_SRC_JAVA_CLIENT)

$(PROTO_SRC_JAVA_CLIENT): $(PROTOBUF_FILES_CLIENT)
	$(PROTOC) --proto_path=protomessages --java_out=../java/src/ $?
	$(PROTOC) --plugin=protoc-gen-grpc-java=${GRPCJAVAPLUGIN} \
		 --grpc-java_out=../java/src --proto_path=protomessages $?
endif

$(PROTO_SRC_SERVER): $(PROTOBUF_FILES_SERVER)
	echo "Generating server Protocol Buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=protomessages --grpc_out=messages --plugin=protoc-gen-grpc=${GRPCCPPPLUGIN} $?
	$(PROTOC) --proto_path=protomessages --cpp_out=messages $?

clean-local:
	rm -f messages/*.h
	rm -f messages/*.cc
if ENABLE_JAVA
	rm -f $(PROTO_SRC_JAVA_CLIENT)
endif

endif
