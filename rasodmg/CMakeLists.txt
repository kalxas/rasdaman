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

add_library(rasodmg alignedtiling.hh alignedtiling.cc alignedtiling.icc
        collection.hh collection.cc collection.icc
        database.hh database.cc database.icc
        dirdecompose.hh dirdecompose.cc
        dirtiling.hh dirtiling.cc
        gmarray.hh gmarray.cc gmarray.icc
        interesttiling.hh interesttiling.cc
        iterator.hh iterator.cc iterator.icc
        marray.hh marray.cc marray.icc
        object.hh object.cc object.icc
        oqlquery.hh oqlquery.cc oqlquery.icc
        partinsert.hh partinsert.cc
        polycutout.hh polycutout.cc
        polygon.hh polygon.cc
        ref.hh ref.cc ref.icc
        set.hh set.cc
        stattiling.hh stattiling.cc
        storagelayout.hh storagelayout.cc storagelayout.icc
        tiling.hh tiling.cc
        transaction.hh transaction.cc transaction.icc)

target_link_libraries(rasodmg clientcomm)

# cotire(rasodmg)

install(TARGETS rasodmg
        DESTINATION ${LIB_DIR})

install(FILES
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
    transaction.hh
    alignedtiling.icc
    collection.icc
    database.icc
    gmarray.icc
    iterator.icc
    marray.icc
    object.icc
    oqlquery.icc
    ref.icc
    storagelayout.icc
    transaction.icc
    DESTINATION "${INCLUDE_DIR}/rasodmg")
