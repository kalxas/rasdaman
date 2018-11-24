# additional target to perform cppcheck run, requires cppcheck # get all project files
# HACK this workaround is required to avoid qml files checking ^_^

function(AddClangTidyTarget THIRD_PARTY_DIR CPP_FILE_EXTENSIONS)

	file(GLOB_RECURSE ALL_SOURCE_FILES ${CPP_FILE_EXTENSIONS})

	foreach (SOURCE_FILE ${ALL_SOURCE_FILES})
		string(FIND ${SOURCE_FILE} ${THIRD_PARTY_DIR} FOUND_IN_THIRD_PARTY_DIR)
		if (NOT ${FOUND_IN_THIRD_PARTY_DIR} EQUAL -1)
			list(REMOVE_ITEM ALL_SOURCE_FILES ${SOURCE_FILE})
		else()
			string(FIND ${SOURCE_FILE} "/cmake-" FOUND_IN_CMAKE_DIR)
			if (NOT ${FOUND_IN_CMAKE_DIR} EQUAL -1)
				list(REMOVE_ITEM ALL_SOURCE_FILES ${SOURCE_FILE})
			endif ()
		endif ()
	endforeach ()

    find_package(ClangTidy)
        
    if(CLANG_TIDY_FOUND)
        message("-- clang-tidy executable: ${CLANG_TIDY_EXECUTABLE}")
        message("-- clang-tidy version: ${CLANG_TIDY_VERSION}")
            
        #TODO:Check parameters
        add_custom_target(clang-tidy COMMAND ${CLANG_TIDY_EXECUTABLE}
				-p ${CMAKE_BINARY_DIR} -format-style=file ${ALL_SOURCE_FILES})
    else()
        message(STATUS "clang-tidy executable not found, the clang-tidy target was not added.")
    endif()


endfunction(AddClangTidyTarget)
