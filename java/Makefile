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
# Copyright 2003-2016 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
###################################################################

# User-specified variables, e.g: make PROJECT_VERSION=9.8.0
# PROJECT_VERSION=9.8.0
# RASJ_TARGET_DIR=../../build/java/target/

ifeq ($(RASJ_TARGET_DIR),)
$(error RASJ_TARGET_DIR was not provided)
endif
ifeq ($(PROJECT_VERSION),)
$(error PROJECT_VERSION was not provided)
endif

target = $(RASJ_TARGET_DIR)/rasj-$(PROJECT_VERSION).jar
targetwithdeps = $(RASJ_TARGET_DIR)/rasj-jar-with-dependencies.jar
targetapidocs = $(RASJ_TARGET_DIR)/site/apidocs
errtxts = src/main/java/rasj/global/RasErrorTexts.java

MVN = mvn

all: $(errtxts) $(target)
apidocs: $(targetapidocs)

$(target):
	$(MVN) -q -B package -Prasnet -Dpackaging=jar \
																-Dversion=$(PROJECT_VERSION) \
																-DbuildDir=$(RASJ_TARGET_DIR)
	$(MVN) -q install:install-file -Dfile=$(targetwithdeps) \
																-DgroupId=org.rasdaman \
																-DartifactId=rasj \
																-Dversion=$(PROJECT_VERSION) \
																-Dpackaging=jar

$(errtxts): ../bin/errtxts $(errtxts).in
	bash ./generate_errtxts_messages.sh $(errtxts).in


$(targetapidocs):
	$(MVN) -q -B javadoc:javadoc -Prasnet
