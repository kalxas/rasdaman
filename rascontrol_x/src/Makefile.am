bin_PROGRAMS = rascontrol
rascontrol_CPPFLAGS = -D READLINELIB=1
rascontrol_SOURCES = rascontrol_main.cc \
					  commandexecutor.cc \
					  controlrasmgrcomm.cc \
					  controlrasmgrrasnet.cc \
					  editline.cc \
					  rascontrol.cc \
					  rascontrolconfig.cc \
					  usercredentials.cc

rascontrol_LDADD = ../../commline/libcommline.a ../../rasnet/src/librasnet.a ../../common/src/libcommon.a ../../rasmgr_x/src/librasmgr.a -L/usr/local/lib -lprotobuf  -lboost_thread -lzmq -lboost_system

noinst_LIBRARIES = librascontrol.a

librascontrol_a_SOURCES = commandexecutor.cc \
							controlrasmgrcomm.cc \
							controlrasmgrrasnet.cc \
							rascontrol.cc \
							 editline.cc \
							rascontrolconfig.cc \
							usercredentials.cc

librascontrol_a_LIBADD = ../../commline/libcommline.a ../../rasnet/src/librasnet.a ../../common/src/libcommon.a
