    add_library(common_pragmas INTERFACE)

    target_sources(common_pragmas INTERFACE "${CMAKE_CURRENT_SOURCE_DIR}/pragmas.hh"
            "${CMAKE_CURRENT_SOURCE_DIR}/gcc/gccpragmas.hh"
            "${CMAKE_CURRENT_SOURCE_DIR}/clang/clangpragmas.hh")
