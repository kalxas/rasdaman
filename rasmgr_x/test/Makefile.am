check_PROGRAMS = main_test
main_test_SOURCES = main_test.cc \
					clienttest.cc \
					useradminrightstest.cc \
					userdatabaserightstest.cc \
					usertest.cc \
					usermanagertest.cc \
					clientmanagertest.cc \
					databasetest.cc \
					databasehosttest.cc \
					databasemanagertest.cc \
					servergrouptest.cc \
					servermanagertest.cc

main_test_LDADD = ../src/librasmgr.a ../../rasnet/src/librasnet.a ../../common/src/libcommon.a @ZMQ_LIBS@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@ -lboost_thread -lpthread -lboost_system
