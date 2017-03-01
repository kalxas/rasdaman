if RMANRASNET
noinst_LIBRARIES = librasnetprotocol.a
librasnetprotocol_a_SOURCES = rasnetclientcomm.cc \
								rasnetservercomm.cc
librasnetprotocol_a_LIBADD = ../common/src/libcommon.a ../rasnet/librasnet.a
librasnetprotocol_a_CXXFLAGS = @AM_CXXFLAGS@ @DISABLE_PROTOBUF_WARNINGS_FLAGS@
endif
