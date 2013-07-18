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

# ------------------------------------------------------------------------------
#                                 VARIABLES
# ------------------------------------------------------------------------------

VERSION_NUMBER = 1.0


if GENERATE_DOCS
  GEN_DOCS = javadoc
endif

# Location of trees.
SRC	      = src
SRC_MAIN  = $(SRC)/java
SRC_WEB   = web
BUILD     = build
DIST      = $(BUILD)/dist
WEB       = $(BUILD)/web
CLASSES   = $(WEB)/WEB-INF/classes
NAME      = def
WAR_NAME  = $(NAME).war
WAR       = $(DIST)/$(WAR_NAME)
APIDOCS   = $(BUILD)/apidocs
SECORE_CORE = ../secore-core

SUBDIRS	  = ../secore-core

# Unix tools
FIND      = find
MKDIR     = mkdir -p
CP        = cp -r
CD        = cd
RM        = rm -rf
SHELL     = bash
TAR       = tar

# Java tools
JAVA      = java
JAVAC     = javac
JJARS_CP  = $(SECORE_CORE)/lib/basex-api.jar:$(SECORE_CORE)/lib/BaseX.jar:$(SECORE_CORE)/lib/log4j-1.2.16.jar:$(SECORE_CORE)/lib/nanoxml-2.2.3.jar:$(SECORE_CORE)/lib/ows-v_1_0_0-schema-1.0-SNAPSHOT.jar:$(SECORE_CORE)/lib/slf4j-api-1.6.2.jar:$(SECORE_CORE)/lib/slf4j-log4j12-1.6.2.jar:$(SECORE_CORE)/lib/xmlunit-1.3.jar:$(SECORE_CORE)/lib/xqj-api-1.0.jar:$(SECORE_CORE)/build/dist/secore-core.jar:lib/servlet-api.jar:lib/xercesImpl-2.7.1.jar

JJARS_PATHS = $(SECORE_CORE)/lib/basex-api.jar $(SECORE_CORE)/lib/BaseX.jar $(SECORE_CORE)/lib/log4j-1.2.16.jar $(SECORE_CORE)/lib/nanoxml-2.2.3.jar $(SECORE_CORE)/lib/ows-v_1_0_0-schema-1.0-SNAPSHOT.jar $(SECORE_CORE)/lib/slf4j-api-1.6.2.jar $(SECORE_CORE)/lib/slf4j-log4j12-1.6.2.jar $(SECORE_CORE)/lib/xmlunit-1.3.jar $(SECORE_CORE)/lib/xqj-api-1.0.jar $(SECORE_CORE)/build/dist/secore-core.jar lib/servlet-api.jar lib/xercesImpl-2.7.1.jar
JFLAGS    = -sourcepath $(SRC_MAIN) \
            -d $(CLASSES)           \
            -cp $(JJARS_CP)	        \
            -source 1.5

JAR       = jar
JARFLAGS  = cf

JAVADOC   = javadoc
JDFLAGS   = -sourcepath $(SRC_MAIN)  \
            -d $(APIDOCS)	     \
            -source 1.5 -classpath $(JJARS_CP)

# ------------------------------------------------------------------------------
#                                 TARGETS
# ------------------------------------------------------------------------------


# Create war
.PHONY: war
war: compileit
	$(MKDIR) $(DIST)
	$(CD) $(WEB); $(JAR) $(JARFLAGS) ../../$(WAR) .

# Print the targets
.PHONY: help
help:
	@echo
	@echo "  Available rules:"
	@echo "    war      create WAR archive in $(DIST)"
	@echo "    install  copy generated WAR archive to wardir set at configure"
	@echo "    javadoc  to generate Javadoc in $(APIDOCS)"
	@echo "    test     run automated tests"
	@echo "    clean    remove all build files"
	@echo "    wps      build 52n WPS server with petascope support and copy to wardir"
	@echo

# all_javas - Temp file for holding source file list
all_javas = $(CLASSES)/all.javas

# Gather source file list, excluding n52 which are exclusively compiled with maven
.INTERMEDIATE: $(all_javas)
$(all_javas):
	$(MKDIR) $(CLASSES)
	$(FIND) $(SRC_MAIN) $(TEST) -name '*.java' > $@

# compileit - Compile the source
.PHONY: compileit
compileit: $(all_javas) $(GEN_DOCS)
	$(MKDIR) $(BUILD)
	$(CP) $(SRC_WEB)/* $(WEB)
	$(CP) $(SECORE_CORE)/etc $(WEB)
	$(TAR) xzf $(WEB)/etc/gml.tar.gz -C $(WEB)/etc/; $(RM) $(WEB)/etc/gml.tar.gz $(WEB)/etc/testdata
	$(MKDIR) $(WEB)/WEB-INF/lib
	$(CP) $(JJARS_PATHS) $(WEB)/WEB-INF/lib
	$(CP) $(SRC_MAIN)/* $(CLASSES) # copy resources (xml, xsd, properties)
	$(FIND) $(BUILD) -name '*.java' | xargs $(RM)
	$(FIND) $(BUILD) -name '.svn' | xargs $(RM) # remove .svn directories
	$(JAVAC) $(JFLAGS) @$<

# Generate the Java doc from sources
.PHONY: javadoc
javadoc: $(all_javas)
	$(JAVADOC) $(JDFLAGS) @$<

# copy WAR to TOMCAT
.PHONY: install
install: war
	$(MKDIR) $(DESTDIR)$(wardir)
	$(CP) $(WAR) $(DESTDIR)$(wardir)
if GENERATE_DOCS
	$(MKDIR) $(DESTDIR)$(pkgdatadir)/doc
	$(CP) $(APIDOCS) $(DESTDIR)$(pkgdatadir)/doc/secore-web
endif

# clean up
.PHONY: clean
clean:
	$(RM) $(BUILD) dist

# build jar file
.PHONY: jar
jar: compileit
	$(CD) $(CLASSES); $(JAR) $(JARFLAGS) ../../../dist/$(NAME).jar .

all: war
