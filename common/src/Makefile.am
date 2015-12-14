noinst_LIBRARIES = libcommon.a

PROTOBUF_FILES= grpc/protomessages/error.proto \
				grpc/protomessages/healthservice.proto

PROTO_SRC= grpc/messages/error.pb.cc \
		   grpc/messages/healthservice.pb.cc \
		   grpc/messages/healthservice.grpc.pb.cc

libcommon_a_SOURCES = $(PROTO_SRC) \
				      crypto/crypto.cc \
					  exceptions/connectionfailedexception.cc \
					  exceptions/exception.cc \
					  exceptions/invalidargumentexception.cc \
					  exceptions/invalidstateexception.cc \
					  exceptions/logicexception.cc \
					  exceptions/missingresourceexception.cc \
					  exceptions/resourcebusyexception.cc \
					  exceptions/runtimeexception.cc \
					  logging/loggingutils.cc \
					  grpc/grpcutils.cc \
					  grpc/healthserviceimpl.cc \
					  mock/gmock-gtest-all.cc \
					  uuid/uuid.cc \
					  time/timer.cc

$(PROTO_SRC): $(PROTOBUF_FILES)
	echo "Generating buffers"
	$(MKDIR_P) grpc/messages
	$(PROTOC) --proto_path=grpc/protomessages --cpp_out=grpc/messages $?
	$(PROTOC) --proto_path=grpc/protomessages --grpc_out=grpc/messages --plugin=protoc-gen-grpc=${GRPCCPPPLUGIN} $?

if ENABLE_JAVA
	$(PROTOC) --proto_path=grpc/protomessages --java_out=../../java/src/ $?
	$(PROTOC) --plugin=protoc-gen-grpc-java=${GRPCJAVAPLUGIN} --grpc-java_out=../../java/src --proto_path=grpc/protomessages $?
endif

clean-local:
	rm -f grpc/messages/{*.cc,*.h}
