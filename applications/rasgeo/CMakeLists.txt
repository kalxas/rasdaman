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

if (ENABLE_JAVA AND USE_GDAL)
    add_executable(rasimport rasimport.hh rasimport.cc
            RasdamanConnector.hh RasdamanConnector.cc
            RasdamanHelper2.hh RasdamanHelper2.cc
            rasgeo_template_inst.hh)

    find_package(GDAL REQUIRED)

    target_link_libraries(rasimport rasodmg clientcomm
            raslib conversion commline network ${GDAL_LIBRARY} ${BASEDB_LIBRARIES})
    target_include_directories(rasimport PUBLIC ${BASEDB_INCLUDE_DIR} ${GDAL_INCLUDE_DIR})

    add_executable(raserase raserase.hh raserase.cc
            RasdamanConnector.hh RasdamanConnector.cc
            RasdamanHelper2.hh RasdamanHelper2.cc
            rasgeo_template_inst.hh)

    target_link_libraries(raserase rasodmg clientcomm
            raslib conversion commline network ${GDAL_LIBRARY} ${BASEDB_LIBRARIES})
    target_include_directories(raserase PUBLIC ${BASEDB_INCLUDE_DIR} ${GDAL_INCLUDE_DIR})

    if (RMANRASNET)
        target_link_libraries(rasimport rasnet)
        target_link_libraries(raserase rasnet)
    endif (RMANRASNET)

    install(TARGETS raserase rasimport
            DESTINATION ${BIN_DIR})

    install_if_not_exists(rasconnect "$ENV{HOME}/.rasdaman")

endif (ENABLE_JAVA AND USE_GDAL)
