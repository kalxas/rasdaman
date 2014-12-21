check_PROGRAMS = main_test
main_test_SOURCES = main_test.cc service/client/channeltest.cc service/server/servicemanagertest.cc service/server/servicerequesthandlertest.cc \
                    sockets/client/clientpinghandlertest.cc sockets/client/clientponghandlertest.cc sockets/client/clientsockettest.cc \
                    sockets/clientservertest.cc sockets/common/peerstatustest.cc sockets/server/clientpooltest.cc sockets/server/connectrequesthandlertest.cc \
                    sockets/server/serverpinghandlertest.cc sockets/server/serversockettest.cc util/proto/protozmqtest.cc \
                    service/serviceperftest.cc

main_test_LDADD = ../src/librasnet.a ../../common/src/libcommon.a -L/usr/local/lib -lprotobuf -lzmq -lboost_thread -lboost_regex -lboost_system -lboost_timer -lpthread
