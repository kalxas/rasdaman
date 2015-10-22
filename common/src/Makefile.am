noinst_LIBRARIES = libcommon.a

PROTOBUF_FILES= grpc/protomessages/error.proto

PROTO_SRC= grpc/messages/error.pb.cc

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
					  grpc/grpcutils.cc \
					  mock/gmock-gtest-all.cc \
					  uuid/uuid.cc \
					  time/timer.cc

$(PROTO_SRC): $(PROTOBUF_FILES)
	echo "Generating buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=grpc/protomessages --cpp_out=grpc/messages $?

if ENABLE_JAVA
	$(PROTOC) --proto_path=grpc/protomessages --java_out=../../java/src/ $?
endif
