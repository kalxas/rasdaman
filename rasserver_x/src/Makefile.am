noinst_LIBRARIES = librasnetserver.a

librasnetserver_a_SOURCES = rasserverserviceimpl.cc rasnetserver.cc clientmanager.cc clientquerystreamedresult.cc

librasnetserver_a_LIBADD = ../../rasnet/librasnet.a ../../common/src/libcommon.a
librasnetserver_a_CXXFLAGS = @AM_CXXFLAGS@ @DISABLE_PROTOBUF_WARNINGS_FLAGS@

SUBDIRS=../../servercomm
