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

set(SRC_HEADERS
        alignedtiling.hh
        collection.hh
        collection.cc
        database.hh
        dirdecompose.hh
        dirtiling.hh
        gmarray.hh
        interesttiling.hh
        iterator.hh
        iterator.cc
        marray.hh
        marray.cc
        object.hh
        oqlquery.hh
        partinsert.hh
        polycutout.hh
        polygon.hh
        ref.hh
        ref.cc
        set.hh
        set.cc
        stattiling.hh
        storagelayout.hh
        tiling.hh
        genreftype.hh
        transaction.hh
        template_inst.hh
        )

add_library(rasodmg
        alignedtiling.cc
        database.cc
        dirdecompose.cc
        dirtiling.cc
        gmarray.cc
        interesttiling.cc
        object.cc
        oqlquery.cc
        partinsert.cc
        polycutout.cc
        polygon.cc
        stattiling.cc
        storagelayout.cc
        tiling.cc
        transaction.cc
        ${SRC_HEADERS}
        )

target_link_libraries(rasodmg
    PUBLIC
    raslib
    
    PRIVATE
    clientcomm
    common_logging
    )

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(rasodmg REUSE_FROM common_logging)
endif()

install(TARGETS rasodmg
        DESTINATION ${LIB_DIR})

install(FILES ${SRC_HEADERS}
        DESTINATION "${INCLUDE_DIR}/rasodmg")
