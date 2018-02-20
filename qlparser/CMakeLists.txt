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
find_package(FLEX REQUIRED)
find_package(BISON REQUIRED)

# -I gnererates an interactive scanner which doesn't try to look ahead past a newline
# -i generates a scanner which doesn't care about upper and lower case; doesn't work
FLEX_TARGET(QlScanner lex.ll "${CMAKE_CURRENT_BINARY_DIR}/lex.cc"
        COMPILE_FLAGS "-i -I")

# -d generates token definitions in .h file
BISON_TARGET(QlParser oql.yy "${CMAKE_CURRENT_BINARY_DIR}/oql.cc"
        COMPILE_FLAGS "-d -y")

ADD_FLEX_BISON_DEPENDENCY(QlScanner QlParser)

add_library(qlparser
        ${BISON_QlParser_OUTPUTS}
        ${FLEX_QlScanner_OUTPUTS}
        lex.ll oql.yy # These files are added to the list to allow QtCreator to see them as part of the project.
        alloca.cc
        parseinfo.hh parseinfo.cc parseinfo.icc
        qtatomicdata.hh qtatomicdata.cc qtatomicdata.icc
        qtbinaryfunc.hh qtbinaryfunc.cc qtbinaryfunc.icc
        qtbinaryinduce.hh qtbinaryinduce.cc qtbinaryinduce.icc
        qtbinaryinduce2.hh qtbinaryinduce2.cc qtbinaryinduce2.icc
        qtbinaryoperation.hh qtbinaryoperation.cc qtbinaryoperation.icc
        qtcaseequality.hh qtcaseequality.cc
        qtcaseop.hh qtcaseop.cc qtcaseop.icc
        qtcelltypeattributes.hh qtcelltypeattributes.cc
        qtclippingutil.hh qtclippingutil.cc
        qtclippingfunc.hh qtclippingfunc.cc qtclippingfunc.icc
        qtcollection.hh qtcollection.cc
        qtcommand.hh qtcommand.cc qtcommand.icc
        qtcomplexdata.hh qtcomplexdata.cc qtcomplexdata.icc
        qtconcat.hh qtconcat.cc qtconcat.icc
        qtcondense.hh qtcondense.cc qtcondense.icc
        qtcondenseop.hh qtcondenseop.cc qtcondenseop.icc
        qtconst.hh qtconst.cc qtconst.icc
        qtconversion.hh qtconversion.cc qtconversion.icc
        qtcreatecelltype.hh qtcreatecelltype.cc
        qtcreatemarraytype.hh qtcreatemarraytype.cc
        qtcreatesettype.hh qtcreatesettype.cc
        qtmulticlipping.hh qtmulticlipping.cc qtmulticlipping.icc
        qtdata.cc qtdata.hh qtdata.icc
        qtdelete.cc qtdelete.hh qtdelete.icc
        qtdomainoperation.cc qtdomainoperation.hh qtdomainoperation.icc
        qtdroptype.cc qtdroptype.hh
        qtemptystream.cc qtemptystream.hh qtemptystream.icc
        qtexecute.hh
        qtfindsection.hh qtfindsection.cc
        qtinfo.cc qtinfo.hh qtinfo.icc
        qtinsert.cc qtinsert.hh qtinsert.icc
        qtintervaldata.cc qtintervaldata.hh qtintervaldata.icc
        qtintervalop.cc qtintervalop.hh qtintervalop.icc
        qtiterator.cc qtiterator.hh qtiterator.icc
        qtjoiniterator.cc qtjoiniterator.hh qtjoiniterator.icc
        qtmarrayop.cc qtmarrayop.hh qtmarrayop.icc
        qtmarrayop2.cc qtmarrayop2.hh qtmarrayop2.icc
        qtmdd.cc qtmdd.hh qtmdd.icc
        qtmddaccess.cc qtmddaccess.hh qtmddaccess.icc
        qtmddcfgop.cc qtmddcfgop.hh
        qtmddconfig.cc qtmddconfig.hh
        qtmintervaldata.cc qtmintervaldata.hh qtmintervaldata.icc
        qtmintervalop.cc qtmintervalop.hh qtmintervalop.icc
        qtnaryoperation.cc qtnaryoperation.hh qtnaryoperation.icc
        qtnode.cc qtnode.hh qtnode.icc
        qtoid.cc qtoid.hh qtoid.icc
        qtoncstream.cc qtoncstream.hh qtoncstream.icc
        qtoperation.cc qtoperation.hh qtoperation.icc
        qtoperationiterator.cc qtoperationiterator.hh qtoperationiterator.icc
        qtpointdata.cc qtpointdata.hh qtpointdata.icc
        qtpointop.cc qtpointop.hh qtpointop.icc
        qtproject.cc qtproject.hh
        qtrangeconstructor.cc qtrangeconstructor.hh qtrangeconstructor.icc
        qtscalardata.cc qtscalardata.hh qtscalardata.icc
        qtselectioniterator.cc qtselectioniterator.hh qtselectioniterator.icc
        qtsimplecaseop.cc qtsimplecaseop.hh qtstringdata.cc
        qtstringdata.hh qtstringdata.icc
        qtunaryfunc.cc qtunaryfunc.hh qtunaryfunc.icc
        qtunaryinduce.cc qtunaryinduce.hh qtunaryinduce.icc
        qtunaryoperation.cc qtunaryoperation.hh qtunaryoperation.icc
        qtupdate.cc qtupdate.hh qtupdate.icc
        qtvariable.cc qtvariable.hh qtvariable.icc
        querytree.cc querytree.hh querytree.icc
        symtab.hh symtab.cc
        qtmshapedata.hh qtmshapedata.cc qtmshapedata.icc
        qtmshapeop.hh qtmshapeop.cc qtmshapeop.icc
        qtpolygonclipping.hh qtpolygonclipping.cc
        qtpolygonutil.hh qtpolygonutil.cc)


if (USE_GDAL)
    find_package(GDAL REQUIRED)

    set(OPTIONAL_LIBRARIES ${OPTIONAL_LIBRARIES} ${GDAL_LIBRARY})
    set(OPTIONAL_INCLUDE_DIRECTORIES ${OPTIONAL_INCLUDE_DIRECTORIES} ${GDAL_INCLUDE_DIR})
endif ()

target_include_directories(qlparser SYSTEM PUBLIC ${OPTIONAL_INCLUDE_DIRECTORIES})
target_include_directories(qlparser PUBLIC ${CMAKE_CURRENT_BINARY_DIR})
target_include_directories(qlparser PUBLIC "${CMAKE_SOURCE_DIR}/debug")

add_subdirectory(test)

target_link_libraries(qlparser ${OPTIONAL_LIBRARIES})

# The qlparser depends on clientcomm and servercomm because somebody included the generated RPC files.
add_dependencies(qlparser clientcomm servercomm catalogmgr)

install(TARGETS qlparser
        DESTINATION ${LIB_DIR})
