    add_library(common_stl INTERFACE)

    target_sources(common_stl INTERFACE
        "${CMAKE_CURRENT_SOURCE_DIR}/complex.hh"
        "${CMAKE_CURRENT_SOURCE_DIR}/make_unique.hh"
        )
