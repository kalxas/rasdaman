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
# PURPOSE: implementation of RasdamanResult class
#
##################################################################

##
## Class implementation: ResdamanResult
##

setGeneric("dbFetch", function(res, n = -1, ...) standardGeneric("dbFetch"))
setMethod("dbFetch", "RasdamanResult",
    def = function(res, n = -1, ...) { tryCatch({
        jarrays <- as.list(res@jObj$fetch(as.integer(n)))
        tohandle <- function(jarray) {
            if (jarray %instanceof% "rasj.RasGMArray") {
                typeid <- jarray$getBaseTypeSchema()$getTypeID()
                return(new("RasdamanArrayHandle", jObj=jarray, typeid=typeid))
            } else {
                return(new("RasdamanHandle", jObj=jarray))
            }
        }
        lapply(jarrays, tohandle)
    }, Exception = .handler) }
)

setGeneric("dbClearResult", function(res, ...) standardGeneric("dbClearResult"))
setMethod("dbClearResult", "RasdamanResult",
    def = function(res, ...) { tryCatch({
        res@jObj$clearResult()
        TRUE
    }, Exception = .handler) }
)

setGeneric("dbGetStatement", function(res, ...) standardGeneric("dbGetStatement"))
setMethod("dbGetStatement", "RasdamanResult",
    def = function(res, ...) { tryCatch({
        res@jObj$getStatement()
    }, Exception = .handler) }
)

setGeneric("dbHasCompleted", function(res, ...) standardGeneric("dbHasCompleted"))
setMethod("dbHasCompleted", "RasdamanResult",
    def = function(res, ...) { tryCatch({
        res@jObj$hasCompleted()
    }, Exception = .handler) }
)

setGeneric("dbGetRowCount", function(res, ...) standardGeneric("dbGetRowCount"))
setMethod("dbGetRowCount", "RasdamanResult",
    def = function(res, ...) { tryCatch({
        res@jObj$getRowCount()
    }, Exception = .handler) }
)
