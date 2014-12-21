check_PROGRAMS = main_test
main_test_SOURCES = main_test.cc databasetest.cc databasehosttest.cc \
					databasehostmanagertest.cc databasemanagertest.cc \
					usermanagertest.cc clienttest.cc clientmanagertest.cc \
					rasservertest.cc
main_test_LDADD = ../src/librasmgr.a ../../rasnet/src/librasnet.a ../../common/src/libcommon.a -L/usr/local/lib -lprotobuf -lzmq -lboost_thread -lpthread -lboost_system
