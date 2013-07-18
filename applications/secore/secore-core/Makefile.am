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

# ------------------------------------------------------------------------------
#                                 VARIABLES
# ------------------------------------------------------------------------------

VERSION_NUMBER = 1.0


if GENERATE_DOCS
  GEN_DOCS = javadoc
endif

# Location of trees.
SRC       = src
SRC_MAIN  = $(SRC)/java
SRC_TEST  = $(SRC)/test
BUILD     = build
DIST      = $(BUILD)/dist
CLASSES   = $(BUILD)/classes
NAME      = secore-core
JAR_NAME  = $(NAME).jar
JAR_FILE  = $(DIST)/$(JAR_NAME)
APIDOCS   = $(BUILD)/apidocs

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
JJARS_CP  = lib/basex-api.jar:lib/BaseX.jar:lib/log4j-1.2.16.jar:lib/nanoxml-2.2.3.jar:lib/ows-v_1_0_0-schema-1.0-SNAPSHOT.jar:lib/slf4j-api-1.6.2.jar:lib/slf4j-log4j12-1.6.2.jar:lib/xmlunit-1.3.jar:lib/xqj-api-1.0.jar 
JJARS_PATHS = lib/basex-api.jar lib/BaseX.jar lib/log4j-1.2.16.jar lib/nanoxml-2.2.3.jar lib/ows-v_1_0_0-schema-1.0-SNAPSHOT.jar lib/slf4j-api-1.6.2.jar lib/slf4j-log4j12-1.6.2.jar lib/xmlunit-1.3.jar lib/xqj-api-1.0.jar
JFLAGS    = -sourcepath $(SRC_MAIN) \
               -d $(CLASSES)        \
               -cp $(JJARS_CP)      \
               -source 1.5

JAR       = jar
JARFLAGS  = cf

JAVADOC   = javadoc
JDFLAGS   = -sourcepath $(SRC_MAIN)  \
            -d $(APIDOCS)      \
            -source 1.5 -classpath $(JJARS_CP)

# ------------------------------------------------------------------------------
#                                 TARGETS
# ------------------------------------------------------------------------------


# Create jar
.PHONY: jar
jar: compileit
	$(MKDIR) $(DIST)
	$(CD) $(CLASSES); $(JAR) $(JARFLAGS) ../../$(JAR_FILE) .

# Print the targets
.PHONY: help
help:
	@echo
	@echo "  Available rules:"
	@echo "    jar      create JAR archive in $(DIST)"
	@echo "    javadoc  to generate Javadoc in $(APIDOCS)"
	@echo "    clean    remove all build files"
	@echo

# all_javas - Temp file for holding source file list
all_javas = $(CLASSES)/all.javas

# Gather source file list, excluding n52 which are exclusively compiled with maven
.INTERMEDIATE: $(all_javas)
$(all_javas):
	$(MKDIR) $(CLASSES)
	$(FIND) $(SRC_MAIN) -name '*.java' > $@

# compileit - Compile the source
.PHONY: compileit
compileit: $(all_javas) $(GEN_DOCS)
	$(MKDIR) $(BUILD)
	$(JAVAC) $(JFLAGS) @$<

# Generate the Java doc from sources
.PHONY: javadoc
javadoc: $(all_javas)
	$(JAVADOC) $(JDFLAGS) @$<

# clean up
.PHONY: clean
clean:
	$(RM) $(BUILD) dist

.PHONY: all
all: jar
