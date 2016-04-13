check_PROGRAMS = main_test


main_test_SOURCES = time/timertest.cc main_test.cc
main_test_LDADD = ../src/libcommon.a @BOOST_LDFLAGS@ -lboost_thread -lboost_regex -lboost_system -lpthread

main_test_CXXFLAGS = @AM_CXXFLAGS@ -Wno-undef -Wno-ctor-dtor-privacy -Wno-variadic-macros

if RMANRASNET
    main_test_SOURCES += grpc/grpcutilstest.cc \
                         grpc/healthserviceimpltest.cc \
                         grpc/healthserviceservertest.cc
    main_test_LDADD +=  @GRPC_LIBS@ @PROTOBUF_LIBS@ @GMOCK_LIB@
endif

check-local:
	./main_test