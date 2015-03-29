bin_PROGRAMS = rasmgr

PROTO_INPUT_DIR=protomessages
PROTO_OUTPUT_DIR=messages

PROTOBUF_FILES=$(PROTO_INPUT_DIR)/rasmgrmess.proto

PROTO_SRC=$(PROTO_OUTPUT_DIR)/rasmgrmess.pb.cc

SRC = $(PROTO_SRC) \
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



rasmgr_SOURCES = main.cc ${SRC}

rasmgr_LDFLAGS =
rasmgr_LDADD = ../../rasnet/src/librasnet.a ../../common/src/libcommon.a ../../commline/libcommline.a -L/usr/local/lib -lprotobuf -lzmq -lboost_thread -lboost_regex -lpthread -lboost_system

noinst_LIBRARIES = librasmgr.a

librasmgr_a_SOURCES = ${SRC}

librasmgr_a_LIBADD = ../../rasnet/src/librasnet.a ../../common/src/libcommon.a

$(PROTO_SRC): $(PROTOBUF_FILES)
	echo "Generating buffers"
	$(MKDIR_P) $(PROTO_OUTPUT_DIR)
	$(PROTOC) --proto_path=$(PROTO_INPUT_DIR) --cpp_out=$(PROTO_OUTPUT_DIR) $?
