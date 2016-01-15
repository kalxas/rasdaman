check_PROGRAMS = main_test

main_test_SOURCES = main_test.cc \
					usercredentialstest.cc

main_test_LDADD = ../src/librascontrol.a  ../../commline/libcommline.a ../../rasnet/librasnet.a ../../common/src/libcommon.a @GMOCK_LIB@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@

check-local:
	./main_test