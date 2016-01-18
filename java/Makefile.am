# -*-Makefile-*-
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
# MAKEFILE FOR:  
#   utility programs
# 
# COMMENTS: 
# - Various insertion-tools for special client data
# 
##################################################################

EXTRA_DIST = src/org/odmg/*.java src/rasj/*.java src/rasj/global/*.java src/rasj/clientcommhttp/*.java src/rasj/odmg/*.java src/rasj/rnp/*.java

MAVEN_BASE_FOLDER = rasjbase
MAVEN_COMPLETE_STRUCT = $(MAVEN_BASE_FOLDER)/src/main/java

SRCS = src/org/odmg/*.java src/rasj/*.java src/rasj/global/*.java src/rasj/clientcommhttp/*.java src/rasj/odmg/*.java src/rasj/rnp/*.java

if RMANRASNET
SRCS += src/org/rasdaman/rasnet/communication/*.java src/org/rasdaman/rasnet/service/*.java src/org/rasdaman/rasnet/util/*.java
CLASSPATH = -cp lib/commons-codec-1.3.jar:lib/commons-logging-1.1.1.jar:lib/google-auth-library-credentials-0.3.0.jar:lib/google-auth-library-oauth2-http-0.3.0.jar:lib/google-http-client-1.19.0.jar:lib/google-http-client-jackson2-1.19.0.jar:lib/grpc-all-0.9.0.jar:lib/grpc-auth-0.9.0.jar:lib/grpc-core-0.9.0.jar:lib/grpc-netty-0.9.0.jar:lib/grpc-okhttp-0.9.0.jar:lib/grpc-protobuf-0.9.0.jar:lib/grpc-protobuf-nano-0.9.0.jar:lib/grpc-stub-0.9.0.jar:lib/guava-18.0.jar:lib/hamcrest-core-1.3.jar:lib/hpack-0.10.1.jar:lib/httpclient-4.0.1.jar:lib/httpcore-4.0.1.jar:lib/jackson-core-2.1.3.jar:lib/jsr305-3.0.0.jar:lib/junit-4.11.jar:lib/junit-4.12.jar:lib/mockito-all-1.10.19.jar:lib/mockito-core-1.10.19.jar:lib/netty-buffer-4.1.0.Beta6.jar:lib/netty-codec-4.1.0.Beta6.jar:lib/netty-codec-http2-4.1.0.Beta6.jar:lib/netty-codec-http-4.1.0.Beta6.jar:lib/netty-common-4.1.0.Beta6.jar:lib/netty-handler-4.1.0.Beta6.jar:lib/netty-resolver-4.1.0.Beta6.jar:lib/netty-transport-4.1.0.Beta6.jar:lib/objenesis-2.1.jar:lib/okhttp-2.4.0.jar:lib/okio-1.4.0.jar:lib/protobuf-java-3.0.0-beta-1.jar:lib/protobuf-javanano-3.0.0-alpha-4.jar

endif

select_rasimplementation:
if RMANRASNET
	cp src/rasj/RasImplementation.java.rasnet src/rasj/RasImplementation.java
else
	cp src/rasj/RasImplementation.java.rnp src/rasj/RasImplementation.java
endif

all : select_rasimplementation src/org/odmg/*.java src/rasj/*.java src/rasj/global/*.java src/rasj/clientcommhttp/*.java src/rasj/odmg/*.java src/rasj/rnp/*.java
	mkdir -p bin dist
	javac $(CLASSPATH) -d bin $(SRCS)
if RMANRASNET
	cd bin && \
    jar xf ../lib/commons-codec-1.3.jar && \
    jar xf ../lib/commons-logging-1.1.1.jar && \
    jar xf ../lib/google-auth-library-credentials-0.3.0.jar && \
    jar xf ../lib/google-auth-library-oauth2-http-0.3.0.jar && \
    jar xf ../lib/google-http-client-1.19.0.jar && \
    jar xf ../lib/google-http-client-jackson2-1.19.0.jar && \
    jar xf ../lib/grpc-all-0.9.0.jar && \
    jar xf ../lib/grpc-auth-0.9.0.jar && \
    jar xf ../lib/grpc-core-0.9.0.jar && \
    jar xf ../lib/grpc-netty-0.9.0.jar && \
    jar xf ../lib/grpc-okhttp-0.9.0.jar && \
    jar xf ../lib/grpc-protobuf-0.9.0.jar && \
    jar xf ../lib/grpc-protobuf-nano-0.9.0.jar && \
    jar xf ../lib/grpc-stub-0.9.0.jar && \
    jar xf ../lib/guava-18.0.jar && \
    jar xf ../lib/hamcrest-core-1.3.jar && \
    jar xf ../lib/hpack-0.10.1.jar && \
    jar xf ../lib/httpclient-4.0.1.jar && \
    jar xf ../lib/httpcore-4.0.1.jar && \
    jar xf ../lib/jackson-core-2.1.3.jar && \
    jar xf ../lib/jsr305-3.0.0.jar && \
    jar xf ../lib/junit-4.11.jar && \
    jar xf ../lib/junit-4.12.jar && \
    jar xf ../lib/mockito-all-1.10.19.jar && \
    jar xf ../lib/mockito-core-1.10.19.jar && \
    jar xf ../lib/netty-buffer-4.1.0.Beta6.jar && \
    jar xf ../lib/netty-codec-4.1.0.Beta6.jar && \
    jar xf ../lib/netty-codec-http2-4.1.0.Beta6.jar && \
    jar xf ../lib/netty-codec-http-4.1.0.Beta6.jar && \
    jar xf ../lib/netty-common-4.1.0.Beta6.jar && \
    jar xf ../lib/netty-handler-4.1.0.Beta6.jar && \
    jar xf ../lib/netty-resolver-4.1.0.Beta6.jar && \
    jar xf ../lib/netty-transport-4.1.0.Beta6.jar && \
    jar xf ../lib/objenesis-2.1.jar && \
    jar xf ../lib/okhttp-2.4.0.jar && \
    jar xf ../lib/okio-1.4.0.jar && \
    jar xf ../lib/protobuf-java-3.0.0-beta-1.jar && \
    jar xf ../lib/protobuf-javanano-3.0.0-alpha-4.jar && \
    rm -rf META-INF
endif
	jar -cf dist/rasj.jar -C bin .
	cp dist/rasj.jar ../applications/petascope/lib/rasj.jar

examples: src/examples/*.java all
	mkdir -p bin
	javac -d bin src/examples/*.java -cp dist/rasj.jar

tests: src/tests/*.java all
	mkdir -p bin
	javac -d bin src/tests/*.java -cp dist/rasj.jar:../applications/petascope/lib/junit-4.5.jar
	java -cp bin:dist/rasj.jar tests.UpdateTest
	java -cp bin:dist/rasj.jar:../applications/petascope/lib/junit-4.5.jar org.junit.runner.JUnitCore tests.SimultaneousConnectionsTest tests.RasTypeTest


mavenSetup:
	mkdir -p $(MAVEN_BASE_FOLDER)
	mkdir -p $(MAVEN_COMPLETE_STRUCT)
	cp pom.xml $(MAVEN_BASE_FOLDER)
	cp -r src/rasj $(MAVEN_COMPLETE_STRUCT)
	cp -r src/org $(MAVEN_COMPLETE_STRUCT)

mavenJar : mavenSetup
	cd $(MAVEN_BASE_FOLDER) && mvn package

mavenDeploy : mavenSetup
	cd $(MAVEN_BASE_FOLDER) && mvn deploy

mavenClean :
	cd $(MAVEN_BASE_FOLDER) && mvn clean
	rm -rf $(MAVEN_BASE_FOLDER)

clean:
	rm -rf bin dist
	rm -rf $(MAVEN_BASE_FOLDER)
wps:
  
jar: all

if RMANRASNET
    SUBDIRS=../rasnet ../common
endif

install-data-hook:
	mkdir -p @libdir@
	cp dist/rasj.jar @libdir@
