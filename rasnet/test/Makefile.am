PROTOBUF_FILES= protomessages/testing.proto

PROTO_SRC= messages/testing.pb.cc

check_PROGRAMS = main_test
main_test_SOURCES = $(PROTO_SRC) \
                    client/channeltest.cc \
                    client/clientpinghandlertest.cc \
                    client/clientponghandlertest.cc \
                    client/serviceresponsehandlertest.cc \
                    common/peerstatustest.cc \
                    common/zmqutiltest.cc \
                    integration/servicetest.cc \
                    main_test.cc \
                    server/clientpooltest.cc \
                    server/connectrequesthandlertest.cc \
                    server/serverpinghandlertest.cc \
                    server/serverponghandlertest.cc \
                    server/servicemanagertest.cc \
                    server/servicerequesthandlertest.cc \
                    testutilities.cc

main_test_CFLAGS= @ZMQ_CFLAGS@ @PROTOBUF_CFLAGS@ @BOOST_CPPFLAGS@
main_test_LDADD = ../src/librasnet.a ../../common/src/libcommon.a @ZMQ_LIBS@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@ -lboost_thread -lboost_regex -lboost_system -lboost_timer


$(PROTO_SRC): $(PROTOBUF_FILES)
	echo "Generating buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=protomessages --cpp_out=messages $?

clean: clean-am
	rm -f messages/*.h
	rm -f messages/*.cc
