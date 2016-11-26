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

RASJ_TARGET_DIR=target
PROJECT_VERSION=@VERSION@

if RMANRASNET
NETWORK_PROTOCOL=rasnet
else
NETWORK_PROTOCOL=rnp
endif

MAVEN_BASE_FOLDER = rasjbase
MAVEN_COMPLETE_STRUCT = $(MAVEN_BASE_FOLDER)/src/main/java

generate_pom:
	cp pom.xml.in pom.xml
	sed -i "s|@PROJECT_VERSION@|${PROJECT_VERSION}|g" pom.xml
	sed -i "s|@RASJ_TARGET_DIR@|${RASJ_TARGET_DIR}|g" pom.xml

all: generate_pom javadoc
	mvn package -P${NETWORK_PROTOCOL}
	cp ${RASJ_TARGET_DIR}/rasj-jar-with-dependencies.jar ../applications/petascope/lib/rasj.jar

if GENERATE_DOCS
APIDOCS   = ${RASJ_TARGET_DIR}/site/apidocs
APIFOLDER=doc/doc-rasj

.PHONY: javadoc
javadoc:
	mvn javadoc:javadoc -P${NETWORK_PROTOCOL} -q || exit 0
else
javadoc:
endif

examples: src/main/java/examples/*.java all
	mkdir -p bin
	javac -d bin src/main/java/examples/*.java -cp dist/rasj.jar

tests: src/test/*.java all
	mkdir -p bin
	javac -d bin src/tests/*.java -cp dist/rasj.jar:../applications/petascope/lib/junit-4.5.jar
	java -cp bin:dist/rasj.jar tests.UpdateTest
	java -cp bin:dist/rasj.jar tests.InsertBinaryDataTest
	java -cp bin:dist/rasj.jar:../applications/petascope/lib/junit-4.5.jar org.junit.runner.JUnitCore tests.SimultaneousConnectionsTest tests.RasTypeTest

clean:
	mvn clean
	rm -rf ${RASJ_TARGET_DIR}
	rm -f pom.xml

if RMANRASNET
    SUBDIRS=../rasnet ../common
endif

install-data-hook:
	mkdir -p $(DESTDIR)@libdir@
	cp ${RASJ_TARGET_DIR}/rasj-jar-with-dependencies.jar $(DESTDIR)@libdir@/rasj.jar
if GENERATE_DOCS
	mkdir -p $(DESTDIR)$(pkgdatadir)/$(APIFOLDER)
	cp -r $(APIDOCS)/* $(DESTDIR)$(pkgdatadir)/$(APIFOLDER)
endif
