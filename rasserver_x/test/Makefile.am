check_PROGRAMS = main_test
main_test_SOURCES = main_test.cc \
                    clientquerystreamedresulttest.cc \
                    ../src/clientquerystreamedresult.cc

main_test_LDADD = @GMOCK_LIB@ 

check-local:
	./main_test
