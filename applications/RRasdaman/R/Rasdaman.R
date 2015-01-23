# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# PURPOSE: definitions of classes and variables
#
##################################################################

CONN_READ_ONLY  <- 1
CONN_READ_WRITE <- 2

RAS_MARRAY <- 1

##
## Classes definitions
##

setClass("RasdamanObject",
    contains = c("DBIObject", "VIRTUAL"),
    slots = list(jObj = "jobjRef")
)

setClass("RasdamanHandle", contains = c("RasdamanObject"))

setClass("RasdamanDriver", contains = c("DBIDriver", "RasdamanObject"))

setClass("RasdamanConnection", contains = c("DBIConnection", "RasdamanObject"))

setClass("RasdamanResult", contains = c("DBIResult", "RasdamanObject"))

setClass("RasdamanArray", slots = list(array = "list", origin = "integer"))

RasdamanArray <- function(array, origin) {
    new("RasdamanArray", array=array, origin=origin)
}

setClass("RasdamanArrayHandle",
         contains = c("RasdamanHandle"),
         slots = list(typeid = "integer"))

##
## Class implementation: DBIObject
##

setMethod("dbGetInfo", "RasdamanObject",
    def = function(dbObj, ...) {
        rasobj <- dbObj@jObj
        message <- rasobj$toString()
        print(.jsimplify(message))
    }
)