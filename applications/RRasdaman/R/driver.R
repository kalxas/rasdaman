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
# PURPOSE: implementation of RasdamanDriver class
#
##################################################################

##
## Class implementation: RasdamanDriver
##

"Rasdaman" <- function() { tryCatch({
    jdriver <- J("rrasdaman.RasDriver")$getInstance()
    driver <- new("RasdamanDriver", jObj = jdriver)
    driver
}, Exception = .handler) }

setAs("RasdamanObject", "RasdamanDriver",
    def = function(from) new("RasdamanDriver", jObj = from@jObj)
)

setGeneric("dbListConnections", function(drv, ...) standardGeneric("dbListConnections"))
setMethod("dbListConnections", "RasdamanDriver",
    def = function(drv, ...) { tryCatch({
        jlist <- drv@jObj$listConnections()
        jarray <- .jevalArray(jlist$toArray())
        lapply(jarray, function(x) new("RasdamanConnection", jObj=x))
    }, Exception = .handler) }
)

setGeneric("dbConnect", function(drv, ...) standardGeneric("dbConnect"))
setMethod("dbConnect", "RasdamanDriver",
    def = function(drv, host = "localhost", port = 7001, dbName = "RASBASE",
                 user = "rasguest", password = "rasguest", mode = CONN_READ_ONLY) {
    tryCatch({
        if (!is.character(host)) stop("host must be character")
        if (!is.numeric(port)) stop("port must be numeric")
        if (!is.character(dbName)) stop("dbName must be character")
        if (!is.character(user)) stop("user must be character")
        if (!is.character(password)) stop("password must be character")
        if (mode != CONN_READ_ONLY && mode != CONN_READ_WRITE)
            stop("wrong mode argument")

        port <- .jnew("java/lang/Integer", as.character(port))
        if (mode == CONN_READ_ONLY)
            mode <- J("rrasdaman.RasMode")$READ_ONLY
        else
            mode <- J("rrasdaman.RasMode")$READ_WRITE

        jConn <- drv@jObj$createConnection(host, port, dbName, user, password, mode)
        result <- new("RasdamanConnection", jObj = jConn)
        result
    }, Exception = .handler) },
    valueClass = "RasdamanConnection"
)
