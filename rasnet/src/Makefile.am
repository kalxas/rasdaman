PROTOBUF_FILES= protomessages/client_rassrvr_service.proto \
                protomessages/common_service.proto \
                protomessages/communication.proto \
                protomessages/internal.proto \
                protomessages/rasmgr_client_service.proto \
                protomessages/rasmgr_rasctrl_service.proto \
                protomessages/rasmgr_rassrvr_service.proto \
                protomessages/rassrvr_rasmgr_service.proto \
                protomessages/service.proto \
                protomessages/test_mess.proto

PROTO_SRC= messages/client_rassrvr_service.pb.cc \
           messages/common_service.pb.cc \
           messages/communication.pb.cc \
           messages/internal.pb.cc \
           messages/rasmgr_client_service.pb.cc \
           messages/rasmgr_rasctrl_service.pb.cc \
           messages/rasmgr_rassrvr_service.pb.cc \
           messages/rassrvr_rasmgr_service.pb.cc \
           messages/service.pb.cc \
           messages/test_mess.pb.cc

noinst_LIBRARIES = librasnet.a
librasnet_a_SOURCES = $(PROTO_SRC) \
                      client/clientponghandler.cc \
                      client/serviceresponsehandler.cc \
                      client/channelconfig.cc \
                      client/channel.cc \
                      client/clientpinghandler.cc \
                      client/clientcontroller.cc \
                      exception/networkingexception.cc \
                      exception/notimplementedexception.cc \
                      exception/duplicateserviceexception.cc \
                      exception/unsupportedmessageexception.cc \
                      server/servicemanager.cc \
                      server/clientpool.cc \
                      server/servicerequesthandler.cc \
                      server/serverponghandler.cc \
                      server/connectrequesthandler.cc \
                      server/serverpinghandler.cc \
                      server/servercontroller.cc \
                      server/servicemanagerconfig.cc \
                      common/util.cc \
                      common/peerstatus.cc \
                      common/zmqutil.cc

librasnet_a_LIBADD = ../../common/src/libcommon.a


$(PROTO_SRC): $(PROTOBUF_FILES)
	echo "Generating buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=protomessages --cpp_out=messages $?
if ENABLE_JAVA
	$(PROTOC) --proto_path=protomessages --java_out=../../java/src/ $?
endif

clean: clean-am
	rm -f messages/*.h
	rm -f messages/*.cc
