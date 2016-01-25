#
# MAKEFILE FOR:
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
#
# COMMENTS:
# This Makefile is used to configure, build and install third party
# dependencies to a temporary local directory.
##################################################################

AUTOMAKE_OPTIONS = foreign

all-local: init-third-party compile-gmock compile-rasnet-deps

.NOTPARALLEL:

# We recursively initialize the submodules everytime make is called.
# The command is idempotent.
init-third-party:
	cd .. && git submodule update --init --recursive

# gmock should be compiled irrespectively
compile-gmock: $(GMOCK_LIB)

if RMANRASNET
compile-rasnet-deps: $(PROTOBUF_BIN) $(GRPC_BIN) $(GRPC_JAVA_BIN)
else
compile-rasnet-deps: ;
endif

# According to the instructions on the GoogleMock GitHub page
# we build a library using the following method
$(GMOCK_LIB):
	$(MKDIR_P) $(GMOCK_BIN) && \
	$(CXX) -isystem ${GTEST_DIR}/include -I${GTEST_DIR} \
	-isystem ${GMOCK_DIR}/include -I${GMOCK_DIR} \
	-pthread -c ${GTEST_DIR}/src/gtest-all.cc -o ${GMOCK_BIN}/gtest-all.o && \
	$(CXX) -isystem ${GTEST_DIR}/include -I${GTEST_DIR} \
	-isystem ${GMOCK_DIR}/include -I${GMOCK_DIR} \
	-pthread -c ${GMOCK_DIR}/src/gmock-all.cc -o ${GMOCK_BIN}/gmock-all.o && \
	$(AR) -rv ${GMOCK_LIB} ${GMOCK_BIN}/gtest-all.o ${GMOCK_BIN}/gmock-all.o

# Compile and install Google Protobuf to a temporary folder.
$(PROTOBUF_BIN):
	cd grpc/third_party/protobuf && \
	./autogen.sh && \
	./configure --prefix=${PROTOBUF_BIN} --disable-shared && \
	make && \
	$(MKDIR_P) $(PROTOBUF_BIN) && \
	make install

# Compile and install GRPC to a temporary folder.
$(GRPC_BIN):
	cd grpc && \
	make && \
	$(MKDIR_P) $(GRPC_BIN) && \
	make install prefix=${GRPC_BIN}

$(GRPC_JAVA_BIN):
if ENABLE_JAVA
	export CPLUS_INCLUDE_PATH=${GRPC_BIN}/include:${PROTOBUF_BIN}/include && \
	export LIBRARY_PATH=${GRPC_BIN}/lib:${PROTOBUF_BIN}/lib && \
	cd grpc-java/compiler && \
	../gradlew java_pluginExecutable && \
	cd ../../ && \
	$(MKDIR_P) $(GRPC_JAVA_BIN) && \
	cp grpc-java/compiler/build/binaries/java_pluginExecutable/protoc-gen-grpc-java ${GRPC_JAVA_BIN}
endif

clean-force: clean-am clean-bin clean-grpc clean-protobuf

clean-bin:
	rm -rf bin

clean-grpc:
	cd grpc && \
	make clean

clean-protobuf:
	cd grpc/third_party/protobuf && \
	make clean

if RMANRASNET

install-data-hook:
	$(MKDIR_P) $(DESTDIR)$(includedir) && \
	cp -R ${GTEST_DIR}/include/gtest $(DESTDIR)$(includedir) && \
	cp -R ${GMOCK_DIR}/include/gmock $(DESTDIR)$(includedir) && \
	cp easyloggingpp/easylogging++.h $(DESTDIR)$(includedir) && \
	cp -R ${GRPC_BIN}/include/grpc $(DESTDIR)$(includedir) && \
	cp -R ${GRPC_BIN}/include/grpc++ $(DESTDIR)$(includedir) && \
	cp -R ${PROTOBUF_BIN}/include/google $(DESTDIR)$(includedir)

install-exec-hook:
	$(MKDIR_P) $(DESTDIR)$(libdir) && \
	cp ${GMOCK_LIB} $(DESTDIR)$(libdir) && \
	cp ${GRPC_BIN}/lib/*.a $(DESTDIR)$(libdir) && \
	cp ${PROTOBUF_BIN}/lib/*.a $(DESTDIR)$(libdir)

else

install-data-hook:
	$(MKDIR_P) $(DESTDIR)$(includedir) && \
	cp -R ${GTEST_DIR}/include/gtest $(DESTDIR)$(includedir) && \
	cp -R ${GMOCK_DIR}/include/gmock $(DESTDIR)$(includedir) && \
	cp easyloggingpp/easylogging++.h $(DESTDIR)$(includedir)

install-exec-hook:
	$(MKDIR_P) $(DESTDIR)$(libdir) && \
	cp ${GMOCK_LIB} $(DESTDIR)$(libdir)

endif
