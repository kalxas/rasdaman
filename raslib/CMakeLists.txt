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

add_library(raslib attribute.hh attribute.cc
        basetype.hh basetype.cc
        collectiontype.hh collectiontype.cc
        commonutil.hh commonutil.cc
        complex.hh complex.cc
        complextype.hh complextype.cc
        dlist.hh dlist.cc
        endian.hh endian.cc
        error.hh error.cc
        flatbasetype.hh flatbasetype.cc
        itertype.hh itertype.cc
        marraytype.hh marraytype.cc
        mddtypes.hh mddtypes.cc
        memblockvec.hh memblockvec.cc
        metaobject.hh metaobject.cc
        minterval.hh minterval.cc minterval.icc
        mintervaltype.hh mintervaltype.cc
        miter.hh miter.icc
        mitera.hh mitera.cc
        miterd.hh miterd.cc miterd.icc
        miterf.hh miterf.icc
        nullvalues.cc nullvalues.hh
        odmgtypes.hh
        oid.hh oid.cc oid.icc
        oidtype.hh oidtype.cc
        parseparams.hh parseparams.cc
        point.hh point.cc point.icc
        pointtype.hh pointtype.cc
        primitive.hh primitive.cc
        primitivetype.hh primitivetype.cc
        property.hh property.cc
        rm.hh rm.cc
        rmdebug.hh rmdebug.cc rmdebug.icc
        rminit.hh rminit.cc
        scalar.hh scalar.cc
        shhopt.hh shhopt.cc
        sinterval.hh sinterval.cc sinterval.icc
        sintervaltype.hh sintervaltype.cc
        storageman.hh storageman.cc
        structure.hh structure.cc
        structuretype.hh structuretype.cc
        template_inst.hh
        type.hh type.cc
        pointdouble.hh pointdouble.cc pointdouble.icc)

target_include_directories(raslib PUBLIC "${CMAKE_SOURCE_DIR}/debug")
target_link_libraries(raslib 
        stdc++
        m)

install(TARGETS raslib
        DESTINATION ${LIB_DIR})

install(FILES
    attribute.hh
    basetype.hh
    collectiontype.hh
    commonutil.hh
    complex.hh
    complextype.hh
    dlist.hh
    dlist.cc
    endian.hh
    error.hh
    flatbasetype.hh
    itertype.hh
    itertype.cc
    marraytype.hh
    mddtypes.hh
    memblockvec.hh
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
    template_inst.hh
    type.hh
    minterval.icc
    miterd.icc
    miterf.icc
    miter.icc
    oid.icc
    pointdouble.icc
    point.icc
    rmdebug.icc
    sinterval.icc
    DESTINATION "${INCLUDE_DIR}/raslib")
