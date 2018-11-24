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

    set(EXCEPTIONS_HEADER_FILES
        connectionfailedexception.hh
        exception.hh
        invalidargumentexception.hh
        invalidstateexception.hh
        logicexception.hh
        missingresourceexception.hh
        rasexceptions.hh
        resourcebusyexception.hh
        runtimeexception.hh
        invalidargumentexception.hh
        notimplementedexception.hh
        invalidbranchexception.hh
        outofrangeexception.hh
        typeexception.hh
        )

    add_library(common_exceptions
        connectionfailedexception.cc
        exception.cc
        invalidargumentexception.cc
        invalidstateexception.cc
        logicexception.cc
        missingresourceexception.cc
        resourcebusyexception.cc
        runtimeexception.cc
        outofrangeexception.cc
        rasexceptions.hh
        invalidtransactionexception.cc
        notimplementedexception.cc 
        invalidbranchexception.cc
        ${EXCEPTIONS_HEADER_FILES}
        )

    target_link_libraries(common_exceptions
        # needed for linking with ldd
        stdc++
        m
        )

    install(TARGETS common_exceptions
        DESTINATION ${LIB_DIR})

    install(FILES ${EXCEPTIONS_HEADER_FILES}
        DESTINATION "${INCLUDE_DIR}/common/exceptions")
