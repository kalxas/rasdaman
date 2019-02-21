add_library(common_geo
    geobbox.cc geobbox.hh
    resamplingalg.cc resamplingalg.hh
    )

target_link_libraries(common_geo PUBLIC common_exceptions)

if (USE_GDAL)
    target_include_directories(common_geo SYSTEM PUBLIC ${GDAL_INCLUDE_DIR})
    target_link_libraries(common_geo PUBLIC ${GDAL_LIBRARY})
endif ()