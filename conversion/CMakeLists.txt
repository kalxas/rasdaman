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
# Copyright 2003 - 2019 Peter Baumann /
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
        formatparams.hh
        csv.hh
        json.hh
        gdal.hh
        grib.hh
        mimetypes.hh
        netcdf.hh
        dem.hh
        hdf.hh
        memfs.hh
        tiff.hh
        jpeg.hh
        png.hh
        bmp.hh
        tmpfile.hh
        transpose.hh
        colormap.hh
        )

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
        jpeg.cc
        bmp.cc
        tmpfile.cc
        transpose.cc
        colormap.cc
        json.cc
        png.cc)

if (USE_NETCDF)
    target_link_libraries(conversion PRIVATE ${NetCDF_LIBRARIES})
    target_include_directories(conversion SYSTEM PRIVATE ${NetCDF_INCLUDE_DIRS})
endif ()
if (USE_HDF4)
    target_link_libraries(conversion PRIVATE ${HDF4_LIBRARIES})
    target_include_directories(conversion SYSTEM PRIVATE ${HDF4_INCLUDE_DIR})
endif ()
if (USE_GRIB)
    target_link_libraries(conversion PRIVATE ${GRIB_LIBRARIES})
    target_include_directories(conversion SYSTEM PRIVATE ${GRIB_INCLUDE_DIR})  
endif ()
if (USE_GDAL)
    target_link_libraries(conversion PUBLIC ${GDAL_LIBRARY} ${ZLIB_LIBRARIES})
    target_include_directories(conversion SYSTEM PUBLIC ${GDAL_INCLUDE_DIR})
endif ()
if (USE_TIFF)
    target_link_libraries(conversion PRIVATE ${TIFF_LIBRARIES})
    target_include_directories(conversion SYSTEM PRIVATE ${TIFF_INCLUDE_DIRS})
endif ()
if (USE_PNG)
    target_link_libraries(conversion PRIVATE ${PNG_LIBRARIES})
    target_include_directories(conversion SYSTEM PRIVATE ${PNG_INCLUDE_DIRS})
endif ()
if (USE_JPEG)
    target_link_libraries(conversion PRIVATE ${JPEG_LIBRARIES})
    target_include_directories(conversion SYSTEM PRIVATE ${JPEG_INCLUDE_DIRS})
endif ()

target_link_libraries(conversion
    PUBLIC
    raslib
    mymalloc_cln
    ${JSONCPP_LIBRARY}
    )

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(conversion REUSE_FROM common_logging)
endif()
