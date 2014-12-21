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
SRCS += src/org/rasdaman/rasnet/channel/*.java src/org/rasdaman/rasnet/communication/*.java src/org/rasdaman/rasnet/exception/*.java \
       src/org/rasdaman/rasnet/message/*.java src/org/rasdaman/rasnet/service/*.java src/org/rasdaman/rasnet/util/*.java
CLASSPATH = -cp lib/protobuf-java-2.4.1.jar:lib/zmq.jar
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
	cd bin && jar xf ../lib/protobuf-java-2.4.1.jar && jar xf ../lib/zmq.jar && rm -rf META-INF
endif
	jar -cf dist/rasj.jar -C bin .

examples: src/examples/*.java all
	mkdir -p bin
	javac -d bin src/examples/*.java -cp dist/rasj.jar

tests: src/tests/*.java all
	mkdir -p bin
	javac -d bin src/tests/*.java -cp dist/rasj.jar:../applications/petascope/lib/junit-4.5.jar
	java -cp bin:dist/rasj.jar tests.UpdateTest
	java -cp bin:dist/rasj.jar:../applications/petascope/lib/junit-4.5.jar org.junit.runner.JUnitCore tests.SimultaneousConnectionsTest


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
