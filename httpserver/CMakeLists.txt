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

add_library(httpserver childs.cc
        config.cc
        defs.h
        http.h
        http.cc
        http-date.cc
        http-defs.h
        http-doit.cc
        http-error.cc
        http-fields.cc
        http-methods.cc
        http-readmsg.cc
        http-support.cc
        http-writemsg.cc
        httpserver.h
        init.cc
        logging.cc
        main.cc
        protos.h
        server.h
        signals.cc
        support.cc
        types.h
        )

target_link_libraries(httpserver mymalloc_cln)

# This has a dependency on clientcomm because it uses generated header files
add_dependencies(httpserver servercomm)

install(TARGETS httpserver
        DESTINATION ${LIB_DIR})