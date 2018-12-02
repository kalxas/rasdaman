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

set(CONVERSION_HEADER_FILES  
        convertor.hh
        convfactory.hh
        convtypes.hh
        convutil.hh
        csv.hh
        json.hh
        formatparams.hh
        gdal.hh
        grib.hh
        mimetypes.hh
        netcdf.hh
        dem.hh
        hdf.hh
        memfs.hh
        tiff.hh
        tmpfile.hh
        transpose.hh)

set_source_files_properties(csv.cc PROPERTIES COMPILE_FLAGS " -Wno-sign-conversion ")

add_library(conversion ${CONVERSION_HEADER_FILES}        
        convertor.cc
        convfactory.cc
        convutil.cc
        csv.cc
        dem.cc
        formatparams.cc        
        gdal.cc
        grib.cc
        hdf.cc
        memfs.cc
        mimetypes.cc
        netcdf.cc
        tiff.cc
        tmpfile.cc
        transpose.cc 
        json.cc)

set(OPTIONAL_LIBRARIES "")
set(OPTIONAL_INCLUDE_DIRECTORIES "")

if (USE_NETCDF)
    find_package(NetCDF REQUIRED)
    set(OPTIONAL_LIBRARIES ${OPTIONAL_LIBRARIES} ${NetCDF_LIBRARIES})
    set(OPTIONAL_INCLUDE_DIRECTORIES ${OPTIONAL_INCLUDE_DIRECTORIES} ${NetCDF_INCLUDE_DIRS})
endif ()

if (USE_GDAL)
    target_link_libraries(conversion ${GDAL_LIBRARY})
    target_include_directories(conversion SYSTEM PUBLIC ${GDAL_INCLUDE_DIR})
endif ()

if (USE_HDF4)
    find_package(HDF4 REQUIRED)
    set(OPTIONAL_LIBRARIES ${OPTIONAL_LIBRARIES} ${HDF4_LIBRARIES})
    set(OPTIONAL_INCLUDE_DIRECTORIES ${OPTIONAL_INCLUDE_DIRECTORIES} ${HDF4_INCLUDE_DIR})
endif ()

if (USE_GRIB)
    find_package(GRIB REQUIRED)
    set(OPTIONAL_LIBRARIES ${OPTIONAL_LIBRARIES} ${GRIB_LIBRARIES})
    set(OPTIONAL_INCLUDE_DIRECTORIES ${OPTIONAL_INCLUDE_DIRECTORIES} ${GRIB_INCLUDE_DIR})    
endif ()

find_package(TIFF REQUIRED)

target_include_directories(conversion SYSTEM PUBLIC ${OPTIONAL_INCLUDE_DIRECTORIES})
target_link_libraries(conversion ${TIFF_LIBRARIES} ${OPTIONAL_LIBRARIES} ${JSONCPP_LIBRARY})

install(TARGETS conversion
        DESTINATION ${LIB_DIR})

install(FILES ${CONVERSION_HEADER_FILES}
        DESTINATION "${INCLUDE_DIR}/conversion")
