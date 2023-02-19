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
#
###################################################################

set(LOGGING_HEADERS loggingutils.hh logging.hh)

add_library(common_logging STATIC
        loggingutils.cc
        stacktrace.cc stacktrace.hh backward.hpp
        signalhandler.cc signalhandler.hh
        ${LOGGING_HEADERS})

target_link_libraries(common_logging PUBLIC ${ELPP_LIBRARY})
if (HAVE_LIBDW)
    target_link_libraries(common_logging PRIVATE ${LIBDW_LIBRARY})
endif()

if (${CMAKE_VERSION} GREATER_EQUAL "3.16.1")
target_precompile_headers(common_logging PRIVATE common.hh)
endif()

install(TARGETS common_logging
        DESTINATION "${LIB_DIR}")

install(FILES ${LOGGING_HEADERS}
        DESTINATION "${INCLUDE_DIR}")
