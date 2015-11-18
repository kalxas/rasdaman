check_PROGRAMS = main_test

main_test_SOURCES = time/timertest.cc \
					grpc/grpcutilstest.cc \
					grpc/healthserviceimpltest.cc \
					grpc/healthserviceservertest.cc \
					main_test.cc

main_test_LDADD = ../src/libcommon.a @GRPCPP_LIBS@ @GPR_LIBS@  @GRPC_LIBS@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@ -lboost_thread -lboost_regex -lboost_system -lpthread
