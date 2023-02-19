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
    collection.hh collection.cc
    database.hh
    gmarray.hh
    iterator.hh iterator.cc
    marray.hh marray.cc
    object.hh
    oqlquery.hh
    ref.hh ref.cc
    set.hh set.cc
    genreftype.hh
    transaction.hh
    template_inst.hh
    tiling.hh
    alignedtiling.hh
    dirdecompose.hh
    dirtiling.hh
    interesttiling.hh
    stattiling.hh
    storagelayout.hh)
add_library(rasodmg
    database.cc
    gmarray.cc
    object.cc
    oqlquery.cc
    transaction.cc
    alignedtiling.cc
    dirdecompose.cc
    dirtiling.cc
    interesttiling.cc
    stattiling.cc
    storagelayout.cc
    tiling.cc
    ${SRC_HEADERS})
target_link_libraries(rasodmg
    PUBLIC
    raslib
    PRIVATE
    clientcomm
    common_logging)

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(rasodmg REUSE_FROM common_logging)
endif()

install(TARGETS rasodmg DESTINATION ${LIB_DIR})
install(FILES ${SRC_HEADERS} ${TILING_SRC_HEADERS} DESTINATION "${INCLUDE_DIR}/rasodmg")
