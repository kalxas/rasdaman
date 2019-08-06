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
        png.cc
        bmp.cc
        tmpfile.cc
        transpose.cc
        colormap.cc
        json.cc)

set(OPTIONAL_LIBRARIES "")
set(OPTIONAL_INCLUDE_DIRECTORIES "")

if (USE_NETCDF)
    target_link_libraries(conversion ${NetCDF_LIBRARIES})
    target_include_directories(conversion SYSTEM PUBLIC ${NetCDF_INCLUDE_DIRS})
endif ()
if (USE_HDF4)
    target_link_libraries(conversion ${HDF4_LIBRARIES})
    target_include_directories(conversion SYSTEM PUBLIC ${HDF4_INCLUDE_DIR})
endif ()
if (USE_GRIB)
    target_link_libraries(conversion ${GRIB_LIBRARIES})
    target_include_directories(conversion SYSTEM PUBLIC ${GRIB_INCLUDE_DIR})  
endif ()
if (USE_GDAL)
    target_link_libraries(conversion ${GDAL_LIBRARY})
    target_include_directories(conversion SYSTEM PUBLIC ${GDAL_INCLUDE_DIR})
endif ()

if (USE_TIFF)
    target_link_libraries(conversion ${TIFF_LIBRARIES})
    target_include_directories(conversion SYSTEM PUBLIC ${TIFF_INCLUDE_DIRS})
endif ()
if (USE_PNG)
    target_link_libraries(conversion ${PNG_LIBRARIES})
    target_include_directories(conversion SYSTEM PUBLIC ${PNG_INCLUDE_DIRS})
endif ()
if (USE_JPEG)
    target_link_libraries(conversion ${JPEG_LIBRARIES})
    target_include_directories(conversion SYSTEM PUBLIC ${JPEG_INCLUDE_DIRS})
endif ()

target_link_libraries(conversion ${JSONCPP_LIBRARY})

install(TARGETS conversion
        DESTINATION ${LIB_DIR})

install(FILES ${CONVERSION_HEADER_FILES}
        DESTINATION "${INCLUDE_DIR}/conversion")
