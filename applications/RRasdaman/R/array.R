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
# PURPOSE: implementation of RasdamanArrayHandle and RasdamanArray classes
#
##################################################################

## RasdamanArrayHandle

setGeneric("getCell", function(handle, point, ...)
    standardGeneric("getCell"))
setMethod("getCell", "RasdamanArrayHandle",
    def = function(handle, point) { tryCatch({
        p <- .rasPointToJava(as.integer(point))
        .getCell(handle, p)
    }, Exception = .handler) }
)

setGeneric(".getCell", function(handle, jpoint, ...)
    standardGeneric(".getCell"))
setMethod(".getCell", "RasdamanArrayHandle",
    def = function(handle, jpoint) { tryCatch({
        if (handle@typeid %in% c(globals$RAS_SHORT, globals$RAS_LONG, globals$RAS_INT))
            result <- handle@jObj$getInt(jpoint)
        else if (handle@typeid == globals$RAS_ULONG)
            result <- handle@jObj$getLong(jpoint)
        else if (handle@typeid == globals$RAS_SHORT)
            result <- handle@jObj$getShort(jpoint)
        else if (handle@typeid %in% c(globals$RAS_CHAR, globals$RAS_BYTE, globals$RAS_BOOLEAN))
            result <- handle@jObj$getByte(jpoint)
        else if (handle@typeid == globals$RAS_DOUBLE)
            result <- handle@jObj$getDouble(jpoint)
        else if (handle@typeid == globals$RAS_FLOAT)
            result <- handle@jObj$getFloat(jpoint)
        else {
            bytes = .jarray(handle@jObj$getCell(jpoint))
            buffer <- J("java.nio.ByteBuffer")$wrap(bytes)
            result <- .rasElementFromBytes(buffer, handle@jObj$getBaseTypeSchema())
        }

        if (handle@typeid == c(globals$RAS_CHAR))
            result <- .toUnsigned(result, 1)
        result
    }, Exception = .handler) }
)

## RasdamanArray

setValidity("RasdamanArray",
            function(object) {
                checkDim <- function(arr) {
                    if (length(dim(arr)) != length(object@origin))
                        return("Origin must have the same size as array dimensionality")
                    else
                        return(TRUE)
                }
                for (i in 1:length(object@array))
                    checkDim(object@array[[i]])
                return(TRUE)
            }
)

setMethod("dim", "RasdamanArray",
    def =  function(x) dim(x@array[[1]])
)
