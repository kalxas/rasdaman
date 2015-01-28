bin_PROGRAMS = rasmgr
SRC = messages/rasmgrmess.pb.cc \
      client.cc \
	  clientcredentials.cc \
	  clientmanagementservice.cc \
	  clientmanagerconfig.cc \
	  clientmanager.cc \
	  rasmgrconfig.cc \
	  rasserver.cc \
	  servermanagementservice.cc \
	  servermanager.cc \
	  useradminrights.cc \
	  userdatabaserights.cc \
	  user.cc \
	  usermanager.cc \
	  databasehost.cc \
	  database.cc \
	  databasehostmanager.cc\
	  databasemanager.cc\
	  servergroupconfig.cc\
	  servergroup.cc\
	  iservercreator.cc\
	  rasservercreator.cc\
	  controlservice.cc\
	  rasctrlgrammarconstants.cc\
	  configuration.cc\
	  rasmanager.cc\
	  rascontrol.cc\
	  controlcommandexecutor.cc

rasmgr_SOURCES = main.cc \
				${SRC}

rasmgr_LDFLAGS =
rasmgr_LDADD = ../../rasnet/src/librasnet.a ../../common/src/libcommon.a ../../commline/libcommline.a -L/usr/local/lib -lprotobuf -lzmq -lboost_thread -lboost_regex -lpthread -lboost_system

noinst_LIBRARIES = librasmgr.a

librasmgr_a_SOURCES = ${SRC}

librasmgr_a_LIBADD = ../../rasnet/src/librasnet.a ../../common/src/libcommon.a

messages/rasmgrmess.pb.cc: generate_protobuffers
.PHONY: generate_protobuffers
generate_protobuffers:
	echo "Generating buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=protomessages --cpp_out=messages protomessages/*
