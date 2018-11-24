# - try to find cppcheck tool
#
# Cache Variables:
# CPPCHECK_EXECUTABLE
#
# Non-cache variables you might use in your CMakeLists.txt:
# CPPCHECK_FOUND
# TODO:Refactor
file(TO_CMAKE_PATH "C:\\Program Files\\Cppcheck" WINDOWS_PATH)
file(TO_CMAKE_PATH "${CPPCHECK_ROOT_DIR}" CPPCHECK_ROOT_DIR)
set(CPPCHECK_ROOT_DIR
        "${CPPCHECK_ROOT_DIR}"
        CACHE
        PATH
        "Path to search for cppcheck")

mark_as_advanced(CPPCHECK_ROOT_DIR)

# cppcheck app bundles on Mac OS X are GUI, we want command line only
set(_oldappbundlesetting ${CMAKE_FIND_APPBUNDLE})
set(CMAKE_FIND_APPBUNDLE NEVER)

# If we have a custom path, look there first.
if(CPPCHECK_ROOT_DIR)
    find_program(CPPCHECK_EXECUTABLE
            NAMES
            cppcheck
            cli
            PATHS
            "${CPPCHECK_ROOT_DIR}"
            "${WINDOWS_PATH}"
            PATH_SUFFIXES
            cli
            NO_DEFAULT_PATH)
endif()

find_program(CPPCHECK_EXECUTABLE NAMES cppcheck
    PATHS "${WINDOWS_PATH}")

# Restore original setting for appbundle finding
set(CMAKE_FIND_APPBUNDLE ${_oldappbundlesetting})

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(CPPCheck
        DEFAULT_MSG
        CPPCHECK_EXECUTABLE
        )

if(CPPCHECK_FOUND OR CPPCHECK_MARK_AS_ADVANCED)
    mark_as_advanced(CPPCHECK_ROOT_DIR)
endif()

mark_as_advanced(CPPCHECK_EXECUTABLE)