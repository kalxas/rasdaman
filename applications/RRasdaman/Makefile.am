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

BUILD=build
RJAVADIR=inst/java/
PACKAGE=RRasdaman
VERSION=0.9
DEP_JARS =../../java/dist/rasj.jar

CD = cd
CP = cp
MKDIR = mkdir -p
RM = rm -rf

.PHONY: build help check install all clean

all: build

build:
	$(MKDIR) $(BUILD)
	$(MKDIR) $(RJAVADIR)
	test -f ../../java/dist/rasj.jar || make -C ../../java
	$(CP) $(DEP_JARS)  $(RJAVADIR)
	$(CD) $(BUILD) && $(RBIN) CMD build ..

help:
	@echo "  Available rules:"
	@echo "    package  build R package"
	@echo "    install  install R package"
	@echo "    check    run automated tests"
	@echo "    clean    remove generated files"
	@echo "    help     display this help message"
	@echo

check: build
	$(CD) $(BUILD) && $(RBIN) CMD check $(PACKAGE)_$(VERSION).tar.gz

install: build
	$(CD) $(BUILD) && $(RBIN) CMD INSTALL $(PACKAGE)_$(VERSION).tar.gz

clean:
	$(RM) -rf $(BUILD) $(RJAVADIR)
