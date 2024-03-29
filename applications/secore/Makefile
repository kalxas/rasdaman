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

# User-specified variables, e.g: make BUILD_DIR=../../build/java/target/

ifeq ($(BUILD_DIR),)
$(error BUILD_DIR was not provided)
endif

target = $(BUILD_DIR)/target/def.${PACKAGING}
targetwithdeps = $(BUILD_DIR)/target/def-jar-with-dependencies.jar
MVN = mvn

all: $(target)

$(target): pom.xml $(shell find -name '*.java')

	# in case def.war file is needed
	if [ "${PACKAGING}" = "war" ]; then \
		$(MVN) -q -B package -DbuildDir=$(BUILD_DIR) -Dversion=$(PROJECT_VERSION) -Dpackaging=${PACKAGING} -P ${PACKAGING}; \
	fi

	$(MVN) -q -B package -DbuildDir=$(BUILD_DIR) -Dversion=$(PROJECT_VERSION) -Dpackaging=jar

	# petascope needs def.jar in any cases
	$(MVN) -q install:install-file \
			-Dfile=$(targetwithdeps) \
			-DgroupId=org.rasdaman \
			-DartifactId=secore \
			-Dversion=$(PROJECT_VERSION) \
			-Dpackaging=jar
