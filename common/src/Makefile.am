noinst_LIBRARIES = libcommon.a

PROTOBUF_FILES= grpc/protomessages/error.proto \
				grpc/protomessages/healthservice.proto

PROTO_SRC= grpc/messages/error.pb.cc \
		   grpc/messages/healthservice.pb.cc \
		   grpc/messages/healthservice.grpc.pb.cc

PROTO_JAVA_SRC = ../../java/src/org/rasdaman/rasnet/service/Healthservice.java \
                ../../java/src/org/rasdaman/rasnet/service/HealthServiceGrpc.java \
                ../../java/src/org/rasdaman/rasnet/service/Error.java

libcommon_a_SOURCES = crypto/crypto.cc \
					  exceptions/connectionfailedexception.cc \
					  exceptions/exception.cc \
					  exceptions/invalidargumentexception.cc \
					  exceptions/invalidstateexception.cc \
					  exceptions/logicexception.cc \
					  exceptions/missingresourceexception.cc \
					  exceptions/resourcebusyexception.cc \
					  exceptions/runtimeexception.cc \
					  uuid/uuid.cc \
					  time/timer.cc \
                      network/networkresolverfactory.cc \
                      network/abstractnetworkresolver.cc \
                      network/ipv4resolver.cc \
                      network/ipv6resolver.cc

libcommon_a_CXXFLAGS = @AM_CXXFLAGS@ -Wno-shadow

if RMANRASNET

libcommon_a_SOURCES += $(PROTO_SRC) \
                       logging/loggingutils.cc \
                       grpc/grpcutils.cc \
                       grpc/healthserviceimpl.cc

libcommon_a_CXXFLAGS += -Wno-pedantic -Wno-sign-conversion -Wno-undef

$(PROTO_SRC): $(PROTOBUF_FILES)
	echo "Generating buffers"
	$(MKDIR_P) grpc/messages
	$(PROTOC) --proto_path=grpc/protomessages --cpp_out=grpc/messages $?
	$(PROTOC) --proto_path=grpc/protomessages --grpc_out=grpc/messages --plugin=protoc-gen-grpc=${GRPCCPPPLUGIN} $?

if ENABLE_JAVA
all-local: $(PROTO_JAVA_SRC)

$(PROTO_JAVA_SRC): $(PROTOBUF_FILES)
	$(PROTOC) --proto_path=grpc/protomessages --java_out=../../java/src/ $?
	$(PROTOC) --plugin=protoc-gen-grpc-java=${GRPCJAVAPLUGIN} --grpc-java_out=../../java/src --proto_path=grpc/protomessages $?

endif

clean-local:
	rm -f grpc/messages/{*.cc,*.h}
if ENABLE_JAVA
	rm -f $(PROTO_JAVA_SRC)
endif

endif
