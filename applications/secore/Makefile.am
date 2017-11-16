
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
#   petascope
#
# COMMENTS:
#
##################################################################

SECORE_TARGET_DIR=`pwd`/target
WAR_DIR=$(DESTDIR)$(wardir)

OLD_CONF=$(DESTDIR)$(sysconfdir)/secore.properties
NEW_CONF=src/main/resources/secore.properties

APPLICATION_PROPERTIES=src/main/resources/application.properties
# use only when Spring Boot starts.
LOG4J_PROPERTIES=src/main/resources/log4j.properties

APIFOLDER = doc/doc-secore/classes


all:
	cp src/main/resources/secore.properties.in $(NEW_CONF)
	sed -i "s|@SHARE_DATA_DIR@|$(pkgdatadir)|g" $(NEW_CONF)
	sed -i "s|@LOG_DIR@|$(logdir)|g" $(NEW_CONF)
	cp src/main/resources/application.properties.in $(APPLICATION_PROPERTIES)
	sed -i "s|@CONF_DIR@|$(sysconfdir)|g" $(APPLICATION_PROPERTIES)
	cp src/main/resources/log4j.properties.in $(LOG4J_PROPERTIES)
	sed -i "s|@LOG_DIR@|$(sysconfdir)|g" $(LOG4J_PROPERTIES)
	cp pom.xml.in pom.xml
	sed -i "s|@SECORE_TARGET_DIR@|${SECORE_TARGET_DIR}|g" pom.xml
	mvn package

install-exec-hook:	
	cp ${SECORE_TARGET_DIR}/def.war ${WAR_DIR}/
	cp ${SECORE_TARGET_DIR}/def.war $(DESTDIR)$(pkgdatadir)/war/
	echo "$@ is updating secore.properties: $(OLD_CONF) from $(NEW_CONF)";
	./update_properties.sh "$(OLD_CONF)" "$(NEW_CONF)";
	mkdir -p $(DESTDIR)$(pkgdatadir)/secore || true
	cp db_updates/* $(DESTDIR)$(pkgdatadir)/secore
if GENERATE_DOCS
	echo "Creating java doc..."
	mvn javadoc:javadoc 2>&1 > /dev/null || exit 0
	mkdir -p $(DESTDIR)$(pkgdatadir)/$(APIFOLDER)
	echo "Copying java doc from target/site to $(DESTDIR)$(pkgdatadir)/$(APIFOLDER)"
	cp -r "target/site" "$(DESTDIR)$(pkgdatadir)/$(APIFOLDER)"
endif

clean-local:
	mvn clean
	rm -rf ${SECORE_TARGET_DIR}
	rm -f secore/pom.xml
