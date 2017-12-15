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

# The name of the *.proto source files.
set(PROTO_SRC protomessages/rasmgrmess.proto)

set(PROTO_BIN_DIR "${CMAKE_CURRENT_BINARY_DIR}/messages")

# This file will contain the generated source files.
set(GENERATED_PROTO_SOURCES "")

foreach (PROTO_FILE ${PROTO_SRC})
    CompileProtobufFile(${PROTO_FILE} ${PROTO_BIN_DIR} GENERATED_PROTO_SOURCES)
endforeach ()

# Include the binary directory to use the generated proto files.
include_directories(${CMAKE_CURRENT_BINARY_DIR})

set_source_files_properties(${PROTO_SRC} PROPERTIES HEADER_FILE_ONLY TRUE)

SET(RASMGR_SOURCES ${GENERATED_PROTO_SOURCES}
        ${PROTO_SRC} # added only for convenience.
        exceptions/databasealreadyexistsexception.cc
        exceptions/databasealreadyexistsexception.hh
        exceptions/dbbusyexception.cc
        exceptions/dbbusyexception.hh
        exceptions/dbhostalreadyexistsexception.cc
        exceptions/dbhostalreadyexistsexception.hh
        exceptions/dbhostbusyexception.cc
        exceptions/dbhostbusyexception.hh
        exceptions/duplicatedbsessionexception.cc
        exceptions/duplicatedbsessionexception.hh
        exceptions/inexistentclientexception.cc
        exceptions/inexistentclientexception.hh
        exceptions/inexistentdatabaseexception.cc
        exceptions/inexistentdatabaseexception.hh
        exceptions/inexistentdbhostexception.cc
        exceptions/inexistentdbhostexception.hh
        exceptions/inexistentinpeerexception.cc
        exceptions/inexistentinpeerexception.hh
        exceptions/inexistentoutpeerexception.cc
        exceptions/inexistentoutpeerexception.hh
        exceptions/inexistentservergroupexception.cc
        exceptions/inexistentservergroupexception.hh
        exceptions/inexistentuserexception.cc
        exceptions/inexistentuserexception.hh
        exceptions/inpeeralreadyexistsexception.cc
        exceptions/inpeeralreadyexistsexception.hh
        exceptions/invalidclientcredentialsexception.cc
        exceptions/invalidclientcredentialsexception.hh
        exceptions/noavailableserverexception.cc
        exceptions/noavailableserverexception.hh
        exceptions/outpeeralreadyexistsexception.cc
        exceptions/outpeeralreadyexistsexception.hh
        exceptions/servergroupbusyexception.cc
        exceptions/servergroupbusyexception.hh
        exceptions/servergroupduplicateexception.cc
        exceptions/servergroupduplicateexception.hh
        exceptions/useralreadyexistsexception.cc
        exceptions/useralreadyexistsexception.hh
        exceptions/userdbrightsexception.cc
        exceptions/userdbrightsexception.hh
        authentication.hh
        client.cc
        client.hh
        clientcredentials.cc
        clientcredentials.hh
        clientmanagementservice.cc
        clientmanagementservice.hh
        clientmanager.cc
        clientmanager.hh
        clientmanagerconfig.cc
        clientmanagerconfig.hh
        clientserverrequest.cc
        clientserverrequest.hh
        clientserversession.hh
        configuration.cc
        configuration.hh
        configurationmanager.cc
        configurationmanager.hh
        constants.hh
        controlcommandexecutor.cc
        controlcommandexecutor.hh
        controlservice.cc
        controlservice.hh
        database.cc
        database.hh
        databasehost.cc
        databasehost.hh
        databasehostmanager.cc
        databasehostmanager.hh
        databasemanager.cc
        databasemanager.hh
        inpeer.cc
        inpeer.hh
        outpeer.cc
        outpeer.hh
        peermanager.cc
        peermanager.hh
        randomgenerator.hh
        randomgenerator.cc
        rascontrol.cc
        rascontrol.hh
        rascontrolgrammar.hh
        rasctrlgrammarconstants.cc
        rasctrlgrammarconstants.hh
        rasmanager.cc
        rasmanager.hh
        rasmgrconfig.cc
        rasmgrconfig.hh
        rasmgrservice.cc
        rasmgrservice.hh
        remoteclientsession.cc
        remoteclientsession.hh
        server.cc
        server.hh
        serverconfig.cc
        serverconfig.hh
        serverfactory.cc
        serverfactory.hh
        serverfactoryrasnet.cc
        serverfactoryrasnet.hh
        servergroup.cc
        servergroup.hh
        servergroupfactory.cc
        servergroupfactory.hh
        servergroupfactoryimpl.cc
        servergroupfactoryimpl.hh
        servergroupimpl.cc
        servergroupimpl.hh
        servermanagementservice.cc
        servermanagementservice.hh
        servermanager.cc
        servermanager.hh
        servermanagerconfig.cc
        servermanagerconfig.hh
        serverrasnet.cc
        serverrasnet.hh
        user.cc
        user.hh
        useradminrights.cc
        useradminrights.hh
        userauthconverter.cc
        userauthconverter.hh
        userdatabaserights.cc
        userdatabaserights.hh
        usermanager.cc
        usermanager.hh)

# Create a library from the source files so that we do not have to recompile.
add_library(rasmgr_lib ${RASMGR_SOURCES})

find_package(Threads REQUIRED)

target_link_libraries(rasmgr_lib rasnet commline common_crypto
        common_exceptions common_grpc common_logging common_time common_uuid
        ${Boost_THREAD_LIBRARY} ${CMAKE_THREAD_LIBS_INIT})

# Create the rasmgr executable
add_executable(rasmgr main.cc)
target_link_libraries(rasmgr rasmgr_lib)

# Install rasmgr 
install(TARGETS rasmgr
        DESTINATION ${BIN_DIR})
