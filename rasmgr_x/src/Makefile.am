bin_PROGRAMS = rasmgr
SRC = messages/rasmgrmess.pb.cc \
      client.cc \
	  clientcredentials.cc \
	  clientmanagementservice.cc \
	  clientmanager.cc \
	  clientmanagerconfig.cc \
	  configuration.cc \
	  configurationmanager.cc \
	  controlcommandexecutor.cc \
	  controlservice.cc \
	  database.cc \
	  databasemanager.cc \
	  databasehost.cc \
	  databasehostmanager.cc \
	  rascontrol.cc \
	  rasctrlgrammarconstants.cc \
	  rasmanager.cc \
	  rasmgrconfig.cc \
	  server.cc \
	  serverfactory.cc \
	  serverfactoryrasnet.cc \
	  servergroup.cc \
	  servergroupimpl.cc \
	  servergroupconfig.cc \
	  servergroupfactory.cc \
	  servergroupfactoryimpl.cc \
	  servermanagementservice.cc \
	  servermanager.cc \
	  servermanagerconfig.cc \
	  serverrasnet.cc \
	  user.cc \
	  useradminrights.cc \
	  userdatabaserights.cc \
	  usermanager.cc

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
