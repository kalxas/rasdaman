check_PROGRAMS = main_test
main_test_SOURCES = main_test.cc \
					useradminrightstest.cc \
					userdatabaserightstest.cc \
					usertest.cc \
					usermanagertest.cc \
					clienttest.cc \
					clientmanagertest.cc \
					databasetest.cc \
					databasehosttest.cc \
					databasehostmanagertest.cc \
					databasemanagertest.cc \
					rasservertest.cc \
					servergrouptest.cc \
					servermanagertest.cc \
					clientmanagementservicetest.cc

main_test_LDADD = ../src/librasmgr.a ../../rasnet/librasnet.a ../../common/src/libcommon.a @GRPC_LIBS@ @GPR_LIBS@ @GRPCPP_LIBS@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@ -lboost_thread -lpthread -lboost_system
