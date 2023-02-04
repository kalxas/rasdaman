add_library(common_tree_model
    dbinfo.cc dbinfo.hh
    )

target_link_libraries(common_tree_model
        common_exceptions)
