check_PROGRAMS = main_test

main_test_SOURCES = main_test.cc \
					usercredentialstest.cc \
					rascontrolconfigtest.cc

main_test_LDADD = ../src/librascontrolx.a  ../../commline/libcommline.a ../../rasnet/src/librasnet.a ../../common/src/libcommon.a -L/usr/local/lib -lprotobuf -lpthread
