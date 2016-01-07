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

if RMANRASNET

all: init-third-party $(PROTOBUF_BIN) $(GRPC_BIN) $(GRPC_JAVA_BIN)

.NOTPARALLEL:

# We recursively initialize the submodules everytime make is called.
# The command is idempotent.
init-third-party:
	git submodule update --init --recursive

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
