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
# Copyright 2003-2016 Peter Baumann /Readline
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
###################################################################

if (RMANRASNET)
    find_package(Readline)

    set(REQUIRED_LIBRARIES "")

    if (READLINE_FOUND)
        add_definitions(-DREADLINELIB=1)
        set(REQUIRED_LIBRARIES ${READLINE_LIBRARIES})
    endif (READLINE_FOUND)

    # Create library from rascontrol sources so that we can test the functionality
    add_library(rascontrol_lib commandexecutor.cc
            commandexecutor.hh
            controlrasmgrcomm.cc
            controlrasmgrcomm.hh
            controlrasmgrrasnet.cc
            controlrasmgrrasnet.hh
            editline.cc
            editline.hh
            invalidrasctrlcommandexception.cc
            invalidrasctrlcommandexception.hh
            rascontrol.cc
            rascontrol.hh
            rascontrolconstants.hh
            rascontrolconfig.cc
            rascontrolconfig.hh
            usercredentials.cc
            usercredentials.hh)
    
    target_link_libraries(rascontrol_lib commline rasnet
            common_crypto common_logging common_exceptions common_grpc
            ${REQUIRED_LIBRARIES} ${CMAKE_THREAD_LIBS_INIT})

    # Create rascontrol executable
    add_executable(rascontrol rascontrol_main.cc)
    target_link_libraries(rascontrol rascontrol_lib)

    cotire(rascontrol rascontrol_lib)

    # Install rascontrol
    install(TARGETS rascontrol
            DESTINATION ${BIN_DIR})
endif (RMANRASNET)
