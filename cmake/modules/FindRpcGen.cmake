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

# Find the rpcgen executable
#   TODO:Cleanup code
#   RPCGEN_EXECUTABLE - The rpcgen executable

IF (NOT RPCGEN_EXECUTABLE)
    MESSAGE(STATUS "Looking for rpcgen")
    FIND_PROGRAM(RPCGEN_EXECUTABLE rpcgen)
    IF (RPCGEN_EXECUTABLE)
        EXECUTE_PROCESS(COMMAND "${RPCGEN_EXECUTABLE}" --version OUTPUT_VARIABLE _version)
        STRING(REGEX MATCH "[0-9.]+" RPCGEN_VERSION ${_version})
        SET(RPCGEN_FOUND TRUE)
    ENDIF (RPCGEN_EXECUTABLE)
ELSE (NOT RPCGEN_EXECUTABLE)
    EXECUTE_PROCESS(COMMAND "${RPCGEN_EXECUTABLE}" --version OUTPUT_VARIABLE _version)
    STRING(REGEX MATCH "[0-9.]+" RPCGEN_VERSION ${_version})
    SET(RPCGEN_FOUND TRUE)
ENDIF (NOT RPCGEN_EXECUTABLE)

IF (RPCGEN_FOUND)
    MESSAGE(STATUS "Found rpcgen: ${RPCGEN_EXECUTABLE} (${RPCGEN_VERSION})")

    IF (NOT RPCGEN_FLAGS)
        SET(RPCGEN_FLAGS "")
    ENDIF (NOT RPCGEN_FLAGS)

    MACRO(RPCGEN_CREATE_XDR SRCFILE)
        GET_FILENAME_COMPONENT(SRCPATH "${SRCFILE}" PATH)
        GET_FILENAME_COMPONENT(SRCBASE "${SRCFILE}" NAME_WE)
        SET(OUTFILE1 "${CMAKE_CURRENT_BINARY_DIR}/${SRCPATH}/${SRCBASE}.h")
        SET(OUTFILE2 "${CMAKE_CURRENT_BINARY_DIR}/${SRCPATH}/${SRCBASE}.c")
        SET(OUTFILE3 "${CMAKE_CURRENT_BINARY_DIR}/${SRCPATH}/${SRCBASE}_xdr.c")
        FILE(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/${SRCPATH}")
        FILE(COPY ${SRCFILE} DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/${SRCPATH}")
        SET(INFILE "${CMAKE_CURRENT_BINARY_DIR}/${SRCFILE}")
        SET(_flags ${ARGV1})
        IF (NOT _flags)
            SET(_flags ${RPCGEN_FLAGS})
        ENDIF (NOT _flags)
        ADD_CUSTOM_COMMAND(OUTPUT ${OUTFILE1}
                COMMAND "${RPCGEN_EXECUTABLE}"
                ARGS -h ${_flags} -o "${OUTFILE1}" ${INFILE}
                DEPENDS "${SRCFILE}"
                WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}"
                COMMENT "Generating ${SRCBASE}.h from ${SRCFILE}"
                )
        ADD_CUSTOM_COMMAND(OUTPUT ${OUTFILE2}
                COMMAND "${RPCGEN_EXECUTABLE}"
                ARGS -c ${_flags} -o "${OUTFILE2}" ${INFILE}
                DEPENDS "${SRCFILE}"
                WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}"
                COMMENT "Generating ${SRCBASE}.c from ${SRCFILE}"
                )
    ENDMACRO(RPCGEN_CREATE_XDR)

ELSE (RPCGEN_FOUND)
    MESSAGE(FATAL_ERROR "Could not find rpcgen")
ENDIF (RPCGEN_FOUND)

MARK_AS_ADVANCED(RPCGEN_EXECUTABLE)