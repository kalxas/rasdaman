set(Boost_USE_MULTITHREADED ON)

set(test_sources
	util/proto/protozmqtest.cc
	sockets/common/peerstatustest.cc
	sockets/client/clientpinghandlertest.cc
	sockets/client/clientponghandlertest.cc
	sockets/client/clientsockettest.cc
	sockets/server/clientpooltest.cc
	sockets/server/serverpinghandlertest.cc
	sockets/server/connectrequesthandlertest.cc
	sockets/server/serversockettest.cc
	sockets/clientservertest.cc
	service/client/channeltest.cc
	service/server/servicemanagertest.cc
	service/server/servicerequesthandlertest.cc
	service/serviceperftest.cc
	main_test.cc
	)

find_package(Boost COMPONENTS system thread timer REQUIRED)
find_package (Threads)

# Add executable called "helloDemo" that is built from the source files
# "demo.cxx" and "demo_b.cxx". The extensions are automatically found.
add_executable (rasnetTests
				${SRC_FILES}
				${test_sources})

# Link the executable to the Hello library. Since the Hello library has
# public include directories we will use those link directories when building
# helloDemo
target_link_libraries (rasnetTests LINK_PUBLIC rasnet  protobuf zmq
${Boost_SYSTEM_LIBRARY}
${Boost_THREAD_LIBRARY}
${Boost_TIMER_LIBRARY}
${CMAKE_THREAD_LIBS_INIT}
/home/rasdaman/rnp/common/build/src/libcommon.a
)