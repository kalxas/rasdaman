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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
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

# ------------------------------------------------------------------------------
#                                 VARIABLES
# ------------------------------------------------------------------------------

VERSION_NUMBER = 1.0

if GENERATE_DOCS
  GEN_DOCS = javadoc
  DOC          = doc
  DOCPETASCOPE = $(DOC)/doc-petascope
  APIFOLDER    = $(DOCPETASCOPE)/classes
endif

# Location of trees.

PROJECT_VERSION=@VERSION@
PETASCOPE_TARGET_DIR=target
RASJ_JAR=\$${project.basedir}/lib/rasj.jar

SRC       = src/main
SRC_MAIN  = petascope_main/$(SRC)/java
SRC_RES   = petascope_main/$(SRC)/resources
TEST      = src/test
BUILD     = build
DIST      = $(BUILD)/dist
WEB       = $(BUILD)/web
CLASSES   = $(WEB)/WEB-INF/classes
NAME      = rasdaman
WAR_NAME  = $(NAME).war
WAR_FILE  = petascope_main/$(PETASCOPE_TARGET_DIR)/petascope_main/$(WAR_NAME)
MIGRATION_JAR_FILE  = petascope_migration/$(PETASCOPE_TARGET_DIR)/petascope_migration/rasdaman-geo-migration.jar

if GDAL_JAVA_DIR_CENTOS
   GDAL_JAVA_DIR = "/usr/lib/java/gdal"
endif
if GDAL_JAVA_DIR_UBUNTU
   GDAL_JAVA_DIR = "/usr/lib/jni"
endif


APIDOCS   = $(BUILD)/apidocs
STATIC_WEB_FOLDER = static
WCS_CLIENT_SRC = ../wcs-client/app/*
WCS_CLIENT_DEST = $(WEB)/$(STATIC_WEB_FOLDER)/wcs-client/

LOG4J_SPRING_SETTINGS_FILE = petascope_core/src/main/resources/log4j.properties.in
LOG4J_SPRING_SETTINGS_FILE_EDIT = petascope_core/src/main/resources/log4j.properties

SETTINGS_FILE = petascope_main/src/main/resources/petascope.properties.in
SETTINGS_FILE_EDIT = petascope_main/src/main/resources/petascope.properties

OLD_CONF=$(DESTDIR)$(sysconfdir)/petascope.properties
NEW_CONF=$(SETTINGS_FILE_EDIT)
OLD_LOG4JCONF=$(DESTDIR)$(sysconfdir)/log4j.properties

# Unix tools
FIND      = find
MKDIR     = mkdir -p
CP        = cp -r
CD        = cd
RM        = rm -rf
SHELL     = bash

# Java tools
JAVA      = java
JAVAC     = javac

JJARS_CP  = lib/gt9.jar:lib/ormlite-core-4.48.jar:lib/ormlite-jdbc-4.48.jar:lib/annotations-9.0.4.jar:lib/commons-lang3-3.1.jar:lib/antlr-4.1-complete.jar:lib/antlrworks-1.3.1.jar:lib/commons-fileupload-1.2.jar:lib/commons-io-1.3.2.jar:lib/commons-math-1.1.jar:lib/mockito-all-1.10.19.jar:lib/gml-v_3_1_1-schema-1.0-SNAPSHOT.jar:lib/jaxb-api.jar:lib/jaxb-impl.jar:lib/jaxb-xjc.jar:lib/joda-time-2.3.jar:lib/junit-4.5.jar:lib/log4j-1.2.15.jar:lib/ows-v_1_0_0-schema-1.0-SNAPSHOT.jar:lib/postgresql-9.3-1101.jdbc4.jar:../../java/dist/rasj.jar:lib/servlet-2_5-api.jar:lib/slf4j-api-1.7.7.jar:lib/slf4j-log4j12-1.7.7.jar:lib/sqlite-jdbc-3.7.15-M1.jar:lib/uuid-3.2.jar:lib/wcs-v_1_1_0-schema-1.0-SNAPSHOT.jar:lib/xercesImpl-2.7.1.jar:lib/xom-1.2.6.jar:lib/hsqldb.jar:lib/jts-1.11.jar:lib/jackson-annotations-2.8.1.jar:lib/jackson-core-2.8.1.jar:lib/jackson-databind-2.8.1.jar:lib/jackson-dataformat-xml-2.8.1.jar:lib/slf4j-log4j12-1.7.7.jar:lib/stax2-api-3.1.4.jar
JJARS_PATHS = lib/gt9.jar lib/ormlite-core-4.48.jar lib/ormlite-jdbc-4.48.jar  lib/annotations-9.0.4.jar lib/commons-lang3-3.1.jar lib/antlr-4.1-complete.jar lib/antlrworks-1.3.1.jar lib/commons-fileupload-1.2.jar lib/commons-io-1.3.2.jar lib/commons-math-1.1.jar lib/mockito-all-1.10.19.jar lib/gml-v_3_1_1-schema-1.0-SNAPSHOT.jar lib/jaxb-api.jar lib/jaxb-impl.jar lib/jaxb-xjc.jar lib/joda-time-2.3.jar lib/junit-4.5.jar lib/log4j-1.2.15.jar lib/ows-v_1_0_0-schema-1.0-SNAPSHOT.jar lib/postgresql-9.3-1101.jdbc4.jar ../../java/dist/rasj.jar lib/servlet-2_5-api.jar lib/slf4j-api-1.7.7.jar lib/slf4j-log4j12-1.7.7.jar lib/sqlite-jdbc-3.7.15-M1.jar lib/uuid-3.2.jar lib/wcs-v_1_1_0-schema-1.0-SNAPSHOT.jar lib/xercesImpl-2.7.1.jar lib/xom-1.2.6.jar lib/hsqldb.jar lib/jts-1.11.jar lib/jackson-annotations-2.8.1.jar lib/jackson-core-2.8.1.jar lib/jackson-databind-2.8.1.jar lib/jackson-dataformat-xml-2.8.1.jar lib/slf4j-log4j12-1.7.7.jar lib/stax2-api-3.1.4.jar

JFLAGS    = -sourcepath $(SRC_MAIN) \
               -d $(CLASSES)        \
               -cp $(JJARS_CP)      \
               -source 1.5

JVMFLAGS  = -ea                     \
            -esa                    \
            -Xfuture
JVM       = $(JAVA) $(JVMFLAGS)

JAR       = jar
JARFLAGS  = cf

JAVADOC   = javadoc
JDFLAGS   = -sourcepath $(SRC_MAIN) \
            -d $(APIDOCS)           \
            -source 1.5 -classpath $(JJARS_CP)

# ------------------------------------------------------------------------------
#                                 TARGETS
# ------------------------------------------------------------------------------

# Create war (Petascope embedded or external can use same war package)
# Check if gdal-java package is installed in Centos or Ubuntu first or returns error.
.PHONY: war
war: 
	echo "GDAL JAVA DIR: " $(GDAL_JAVA_DIR)
	if test -z $(GDAL_JAVA_DIR); then \
		echo "gdal-java package is not installed, please install it."; \
		exit 1 ;\
	fi
	cp $(SRC_RES)/application.properties.in $(SRC_RES)/application.properties
	$(SED) -i "s|@GDAL_JAVA_DIR@|$(GDAL_JAVA_DIR)|g" $(SRC_RES)/application.properties
	$(SED) -i "s|@CONF_DIR@|$(sysconfdir)|g" $(SRC_RES)/application.properties
	cp $(LOG4J_SPRING_SETTINGS_FILE) $(LOG4J_SPRING_SETTINGS_FILE_EDIT)
	$(SED) -i "s|@LOG_DIR@|$(logdir)|g" $(LOG4J_SPRING_SETTINGS_FILE_EDIT)		
	cp migrate_petascopedb.sh.in migrate_petascopedb.sh	
	$(SED) -i "s|@LOG_DIR@|$(logdir)|g" migrate_petascopedb.sh	
	$(SED) -i "s|@CONF_DIR@|$(sysconfdir)|g" migrate_petascopedb.sh	
	$(SED) -i "s|@PKGDATA_DIR@|$(pkgdatadir)|g" migrate_petascopedb.sh				
	cp pom.xml.in pom.xml
	$(SED) -i "s|@PROJECT_VERSION@|${PROJECT_VERSION}|g" pom.xml
	$(SED) -i "s|@PETASCOPE_TARGET_DIR@|${PETASCOPE_TARGET_DIR}|g" pom.xml
	cp petascope_core/pom.xml.in petascope_core/pom.xml
	$(SED) -i "s|@PROJECT_VERSION@|${PROJECT_VERSION}|g" petascope_core/pom.xml
	$(SED) -i "s|@PETASCOPE_TARGET_DIR@|${PETASCOPE_TARGET_DIR}|g" petascope_core/pom.xml
	cp petascope_main/pom.xml.in petascope_main/pom.xml
	$(SED) -i "s|@PROJECT_VERSION@|${PROJECT_VERSION}|g" petascope_main/pom.xml
	$(SED) -i "s|@PETASCOPE_TARGET_DIR@|${PETASCOPE_TARGET_DIR}|g" petascope_main/pom.xml
	cp petascope_migration/pom.xml.in petascope_migration/pom.xml
	$(SED) -i "s|@PROJECT_VERSION@|${PROJECT_VERSION}|g" petascope_migration/pom.xml
	$(SED) -i "s|@PETASCOPE_TARGET_DIR@|${PETASCOPE_TARGET_DIR}|g" petascope_migration/pom.xml
	mvn package

# Print the targets
.PHONY: help
help:
	@echo
	@echo "  Available rules:"
	@echo "    war      create WAR archive in $(DIST)"
	@echo "    install  copy generated WAR archive to wardir set at configure"
	@echo "    javadoc  to generate Javadoc in $(APIDOCS)"
	@echo "    setupdb  setup petascope database"
	@echo "    dropdb   destroy petascope database"
	@echo "    test     run automated tests"
	@echo "    clean    remove all build files"
	@echo "    wps      build 52n WPS server with petascope support and copy to wardir"
	@echo

# Generate the Java doc from sources
.PHONY: javadoc
javadoc: $(all_javas)
	$(JAVADOC) $(JDFLAGS) @$<

# B: Build petascope.properties and install war file to target directory.
# Open $(SETTINGS_FILE): petascope.properties file in ~petascope/src/main/resources to $RMANHOME/etc/
# Set java_server=external if --with-java-server=external or java_server=embedded if --with-java-server=embedded or not set.
# Finally, copy $(SETTINGS_FILE) file petascope.properties in ~petascope/src/main/resources to $RMANHOME/etc/
.PHONY: install
install: 
	echo -n "Creating temporary petascope.properties file for merging with old configuration file...";
	$(CP) $(SETTINGS_FILE) $(SETTINGS_FILE_EDIT);
	echo "Done.";
	$(MKDIR) $(DESTDIR)$(wardir)
	echo "JAVA SERVER = "$(JAVA_SERVER)
	echo "Install rasdaman.war to $(DESTDIR)$(wardir)"; \
	$(CP) $(WAR_FILE) $(DESTDIR)$(wardir); \
	echo "Install rasdaman-geo-migration.jar to $(DESTDIR)$(pkgdatadir)/petascope/"; \
	$(CP) $(MIGRATION_JAR_FILE) $(DESTDIR)$(pkgdatadir)/petascope/; \
	echo "Install migrate_petascopedb.sh to $(DESTDIR)$(bindir)"; \
	$(CP) migrate_petascopedb.sh $(DESTDIR)$(bindir); \
	echo "Install update_properties.sh to $(DESTDIR)$(bindir)"; \
	$(CP) update_properties.sh $(DESTDIR)$(bindir); \
	if [ $(JAVA_SERVER) == external ]; then \
		echo 'Update java_server=external Petascope in petascope properties file'; \
		sed -i 's|@GENERATED_embedded_petascope@|false|g' $(SETTINGS_FILE_EDIT); \
		sed -i 's|java_server=embedded|java_server=external|g' $(SETTINGS_FILE_EDIT); \
		sed -i 's|start_embedded_petascope=true|start_embedded_petascope=false|g' $(SETTINGS_FILE_EDIT); \
	else \
		echo 'Update java_server=embedded Petascope in petascope properties file'; \
		sed -i 's|@GENERATED_embedded_petascope@|true|g' $(SETTINGS_FILE_EDIT); \
		sed -i 's|java_server=external|java_server=embedded|g' $(SETTINGS_FILE_EDIT); \
		sed -i 's|start_embedded_petascope=false|start_embedded_petascope=true|g' $(SETTINGS_FILE_EDIT); \
	fi;
# If user's petascope.properties file has no option 'java_server' or 'start_embedded_petascope', it will be added automatically.
# If user's petascope.properties has option 'java_server' then it will update to 'embedded/external' based on user's configuration.  (./configure --with-java-server=embedded/external)
# [ -z "`grep -m 1 -h "java_server" "$$NEWCONF"`" ]; need to use -z "grep " to avoid  "too many arguments error".
# ---- From line 212, in any case, run scrip update_properties with input is old and new files (petascope.properties and log4j.properties)
if GENERATE_DOCS
	$(MKDIR) -p $(DESTDIR)$(pkgdatadir)/$(APIFOLDER)
	$(CP) $(APIDOCS)/* $(DESTDIR)$(pkgdatadir)/$(APIFOLDER)
endif
	sed -i "s|@GENERATED_rasdaman_bin_path@|$(bindir)/|g" $(SETTINGS_FILE_EDIT); \
	sed -i "s|@LOGDIR@|$(logdir)|g" $(SETTINGS_FILE_EDIT); \
	sed -i "s|@GENERATED_java_server@|$(JAVA_SERVER)|g" $(SETTINGS_FILE_EDIT); \
	echo -n "$@ is updating petascope.properties file: $(OLD_CONF) from $(NEW_CONF)"; \
	./update_properties.sh $(OLD_CONF) $(NEW_CONF); \
	echo "Done.";
	echo -e; \
	if test -f "$(OLD_LOG4JCONF)"; then \
		echo "log4j.properties file is merged in: $(OLD_CONF), move this file to .unused."; \
		mv $(OLD_LOG4JCONF) $(OLD_LOG4JCONF).unused 2>/dev/null; \
	fi; \
	echo "Done.";

# run any JUnit tests
.PHONY: test
test:
	${JAVA} -cp $(JJARS_CP):$(BUILD)/test/classes:$(BUILD)/web/WEB-INF/classes org.junit.runner.JUnitCore petascope.AllTests


# clean up
.PHONY: clean
clean:
	mvn clean -Pwar
	mvn clean -Pjar
	$(RM) $(BUILD) dist
	$(RM) $(PETASCOPE_TARGET_DIR)
	$(RM) pom.xml

# build jar file
.PHONY: jar
jar: compileit
	$(CD) $(CLASSES); $(JAR) $(JARFLAGS) ../../../$(NAME).jar .

# inject petascope libs into the 52n wps 
.PHONY: wps
wps: jar
	$(MKDIR) $(BUILD)/wps
	$(CP) $(wpswar) $(BUILD)/wps/wps.war
	cd $(BUILD)/wps; $(JAR) xf wps.war; $(RM) wps.war
	$(CP) $(BUILD)/petascope.jar lib/*.jar $(BUILD)/wps/WEB-INF/lib
	$(CD) $(BUILD)/wps; $(JAR) $(JARFLAGS) ../wps.war .
	$(CP) $(BUILD)/wps.war $(DESTDIR)$(wardir)
	$(SED) -e 's|@CONF_DIR@|$(sysconfdir)|g' $(SRC_RES)/n52/ProcessCoverages.java.in > $(SRC_RES)/n52/ProcessCoverages.java

.PHONY: all
all: war

EXTRA_DIST = \
	build.xml \
	INSTALL \
	lib \
	LICENCE \
	Makefile.am \
	Makefile.in \
	Makefile \
	nbproject \
	README \
	README.WCS2 \
	src \
	templates \
	xml

DISTFILES = $(EXTRA_DIST)

# Put petascope into archive for RPM generation
.PHONY: distdir
distdir:
	$(MKDIR) $(distdir)
	cp -R $(DISTFILES) $(distdir)
