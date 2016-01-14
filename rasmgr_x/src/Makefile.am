bin_PROGRAMS = rasmgr

PROTOBUF_FILES=protomessages/rasmgrmess.proto

PROTO_SRC=messages/rasmgrmess.pb.cc

SRC = $(PROTO_SRC) \
	  exceptions/databasealreadyexistsexception.cc \
	  exceptions/dbbusyexception.cc \
	  exceptions/dbhostalreadyexistsexception.cc \
	  exceptions/dbhostbusyexception.cc \
	  exceptions/inexistentclientexception.cc \
	  exceptions/duplicatedbsessionexception.cc \
	  exceptions/inexistentdatabaseexception.cc \
	  exceptions/inexistentdbhostexception.cc \
	  exceptions/inexistentservergroupexception.cc \
	  exceptions/inexistentuserexception.cc \
	  exceptions/invalidclientcredentialsexception.cc \
	  exceptions/servergroupbusyexception.cc \
	  exceptions/servergroupduplicateexception.cc \
	  exceptions/useralreadyexistsexception.cc \
	  exceptions/userdbrightsexception.cc \
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
	  databasehost.cc \
	  databasehostmanager.cc \
	  databasemanager.cc \
	  rascontrol.cc \
	  rasctrlgrammarconstants.cc \
	  rasmanager.cc \
	  rasmgrconfig.cc \
	  server.cc \
	  serverconfig.cc \
	  serverfactory.cc \
	  serverfactoryrasnet.cc \
	  servergroup.cc \
	  servergroupfactory.cc \
	  servergroupfactoryimpl.cc \
	  servergroupimpl.cc \
	  servermanagementservice.cc \
	  servermanager.cc \
	  servermanagerconfig.cc \
	  serverrasnet.cc \
	  user.cc \
	  useradminrights.cc \
	  userdatabaserights.cc \
	  usermanager.cc

rasmgr_SOURCES = main.cc ${SRC}

rasmgr_LDADD = ../../rasnet/librasnet.a ../../common/src/libcommon.a ../../commline/libcommline.a @GRPC_LIBS@ @PROTOBUF_LIBS@  @BOOST_LDFLAGS@ -lboost_thread -lboost_regex -lpthread -lboost_system
rasmgr_CXXFLAGS = @AM_CXXFLAGS@ @DISABLE_PROTOBUF_WARNINGS_FLAGS@ -Wno-error=shadow


noinst_LIBRARIES = librasmgr.a

librasmgr_a_SOURCES = ${SRC}

librasmgr_a_LIBADD = ../../rasnet/librasnet.a ../../common/src/libcommon.a
librasmgr_a_CXXFLAGS = @AM_CXXFLAGS@ @DISABLE_PROTOBUF_WARNINGS_FLAGS@ -Wno-error=shadow

$(PROTO_SRC): $(PROTOBUF_FILES)
	echo "Generating buffers"
	$(MKDIR_P) messages
	$(PROTOC) --proto_path=protomessages --cpp_out=messages $?

clean: clean-am
	rm -f messages/*.h
	rm -f messages/*.cc
