# -*-Makefile-*-
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
#   rasdaman R package
#
##################################################################

CD = cd
CP = cp
MKDIR = mkdir -p
RM = rm -rf
JAVAC = javac
JAR_TOOL = jar
SPACE = $(eval) $(eval)

BUILD=build
PACKAGE_NAME=RRasdaman
VERSION=0.9
PACKAGE=$(PACKAGE_NAME)_$(VERSION).tar.gz

.PHONY: build help check install all clean

all: build


######## R package ########

R_SRCS=$(wildcard R/*.R)
RD_SRCS=$(wildcard man/*.Rd)
TEST_SRCS=$(wildcard tests/*.R) $(wildcard tests/testthat/*.R)
MISC_SRCS=DESCRIPTION NAMESPACE java/README

build: $(BUILD)/$(PACKAGE)

$(BUILD)/$(PACKAGE): $(RRASDAMAN_JAR) $(R_SRCS) $(RD_SRCS) $(TEST_SRCS) $(MISC_SRCS)
	$(MKDIR) $(BUILD)
	$(CD) $(BUILD) && $(RBIN) CMD build ..

check: build
	$(CD) $(BUILD) && $(RBIN) CMD check $(PACKAGE)

install: build
	$(CD) $(BUILD) && $(RBIN) CMD INSTALL $(PACKAGE)


######## Java compilation ########

RJAVADIR=inst/java/
JAVA_SRC=src/main/java
JAVA_OUT=$(BUILD)/out
RRASDAMAN_JAR=$(RJAVADIR)/RRasdaman.jar
source_dirs_list=. rrasdaman

DEP_JARS =../../java/dist/rasj.jar
LIBS=$(RJAVADIR)/rasj.jar

SRCDIRS=$(addprefix $(JAVA_SRC)/, $(source_dirs_list))
BARE_CLASSES=$(patsubst $(JAVA_SRC)/%.java,%.class,$(wildcard $(addsuffix /*.java, $(SRCDIRS))))
CLASSES=$(addprefix $(JAVA_OUT)/, $(BARE_CLASSES))
JLIBS=$(subst $(SPACE),:,$(LIBS))
VPATH = $(SRCDIRS)

$(JAVA_OUT)/%.class: $(JAVA_SRC)/%.java
	@echo compiling $@ $^
	$(MKDIR) $(dir $@)
	$(JAVAC) -d $(JAVA_OUT) -cp $(JLIBS) -sourcepath $(JAVA_SRC) $^

$(RRASDAMAN_JAR): $(LIBS) $(CLASSES)
	$(JAR_TOOL) cf $(RRASDAMAN_JAR) $(addprefix -C $(JAVA_OUT)$(SPACE), $(BARE_CLASSES))

$(LIBS):
	$(MKDIR) $(RJAVADIR)
	test -f ../../java/dist/rasj.jar || make -C ../../java
	$(CP) $(DEP_JARS)  $(RJAVADIR)

######## Misc ########

help:
	@echo "  Available rules:"
	@echo "    package  build R package"
	@echo "    install  install R package"
	@echo "    check    run automated tests"
	@echo "    clean    remove generated files"
	@echo "    help     display this help message"
	@echo

clean:
	$(RM) $(BUILD) $(RJAVADIR)
