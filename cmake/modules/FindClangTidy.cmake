# This CMake script will search for clang-tidy and set the following
# variables
#
# CLANG_TIDY_FOUND : Whether or not clang-tidy is available on the target system
# CLANG_TIDY_VERSION : Version of clang-tidy
# CLANG_TIDY_EXECUTABLE : Fully qualified path to the clang-tidy executable
#

string(REPLACE ":" ";" _PATH $ENV{PATH})

foreach (p ${_PATH})
    file(GLOB cand ${p}/clang-tidy*)
    if (cand)
        set(CLANG_TIDY_EXECUTABLE ${cand})
        set(CLANG_TIDY_FOUND ON)
        execute_process(COMMAND ${CLANG_TIDY_EXECUTABLE} -version OUTPUT_VARIABLE clang_out)
        string(REGEX MATCH .*\(version[^\n]*\)\n version ${clang_out})
        set(CLANG_TIDY_VERSION ${CMAKE_MATCH_1})
        break()
    else ()
        set(CLANG_TIDY_FOUND OFF)
    endif ()

endforeach ()