# This file is paBucharestrt of rasdaman community.
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
        attribute.hh
        banditerator.hh
        basetype.hh
        collectiontype.hh
        complex.hh
        complextype.hh
        endian.hh
        error.hh
        flatbasetype.hh
        marraytype.hh
        mddtypes.hh
        metaobject.hh
        minterval.hh
        mintervaltype.hh
        mitera.hh
        miterd.hh
        miterf.hh
        miter.hh
        nullvalues.hh
        odmgtypes.hh
        oid.hh
        oidtype.hh
        parseparams.hh
        pointdouble.hh
        point.hh
        pointtype.hh
        primitive.hh
        primitivetype.hh
        property.hh
        rmdebug.hh
        rm.hh
        rminit.hh
        scalar.hh
        shhopt.hh
        sinterval.hh
        sintervaltype.hh
        storageman.hh
        structure.hh
        structuretype.hh
        type.hh
        miterd.icc
        miterf.icc
        miter.icc
        )
    
add_library(raslib 
        attribute.cc
        banditerator.cc
        basetype.cc
        collectiontype.cc
        complex.cc
        complextype.cc
        endian.cc
        error.cc
        flatbasetype.cc
        marraytype.cc
        mddtypes.cc
        metaobject.cc
        minterval.cc
        mintervaltype.cc
        miter.cc
        mitera.cc
        miterd.cc
        miterf.cc
        nullvalues.cc
        oid.cc
        oidtype.cc
        parseparams.cc
        point.cc
        pointdouble.cc
        pointtype.cc
        primitive.cc
        primitivetype.cc
        property.cc
        rm.cc
        rmdebug.cc
        rminit.cc
        scalar.cc
        shhopt.cc
        sinterval.cc
        sintervaltype.cc
        storageman.cc
        structure.cc
        structuretype.cc
        turboqueryresult.cc
        type.cc
        ${SRC_HEADERS}
        )

target_link_libraries(raslib
        PRIVATE
        mymalloc_cln
        stdc++
        m
        )

install(TARGETS raslib
        DESTINATION ${LIB_DIR})

install(FILES ${SRC_HEADERS}
        DESTINATION "${INCLUDE_DIR}/raslib")
