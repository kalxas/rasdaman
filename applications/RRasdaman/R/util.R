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
# PURPOSE: utility functions
#
##################################################################

##
## Utility
##

globals <- NULL

# This function is to be called on the package load
.rasInitGlobals <- function() {
    globals <<- J("rasj.global.RasGlobalDefs")
}

#########################
## Exceptions handling ##
#########################

.handler <- function(exc) {
    stop(exc$jobj$getMessage(), call. = FALSE)
}

#####################################
## Conversion from Java to R types ##
#####################################

setGeneric("simplify", function(handle, ...)
    standardGeneric("simplify"))
setMethod("simplify", "RasdamanHandle",
    def = function(handle) {
        .rasJavaToR(handle@jObj)
    }
)

.rasJavaToR <- function(jObj) {
    if (jObj %instanceof% "rasj.RasGMArray") {
        result <- .rasArrayToR(jObj)
    } else if (jObj %instanceof% "rasj.RasStructure") {
        result <- .rasStructureToR(jObj)
    } else if (jObj %instanceof% "rasj.RasMInterval") {
        result <- .rasMIntervalToR(jObj)
    } else if (jObj %instanceof% "rasj.RasSInterval") {
        result <- .rasSIntervalToR(jObj)
    } else if (jObj %instanceof% "rasj.RasPoint") {
        result <- .rasPointToR(jObj)
    } else {
        result <- .jsimplify(jObj)
    }
    if (class(result)[[1]] == "jobjRef") {
        stop("Can't convert the argument to an R object")
    }
    result
}

.rasArrayToR <- function(jRasArray) {
    domain <- .rasMIntervalToR(jRasArray$spatialDomain())
    dims <- c()
    origin <- c()
    for (i in 1:length(domain)) {
        dims[i] <- domain[[i]]$high - domain[[i]]$low + 1
        origin[i] <- domain[[i]]$low
    }

    if (jRasArray %instanceof% "rasj.RasMArrayLong") {
        flatArray <- jRasArray$getLongArray()
    } else if (jRasArray %instanceof% "rasj.RasMArrayInteger") {
        flatArray <- jRasArray$getIntArray()
    } else if (jRasArray %instanceof% "rasj.RasMArrayByte") {
        if (jRasArray$getBaseTypeSchema()$getTypeID() == globals$RAS_BOOLEAN) {
            flatArray <- as.logical(jRasArray$getArray())
        } else {
            flatArray <- as.integer(jRasArray$getArray())
            if (jRasArray$getBaseTypeSchema()$getTypeID() == globals$RAS_BYTE) {
                # both octet(byte) and char types are sent as arrays of bytes.
                # as.integer(<raw string>) interprents them as arrays of
                # unsigned integers, so signed octet values should be corrected
                flatArray <- sapply(flatArray, function(x) .toSigned(x, 1))
            }
        }
    } else if (jRasArray %instanceof% "rasj.RasMArrayDouble") {
        flatArray <- jRasArray$getDoubleArray()
    } else if (jRasArray %instanceof% "rasj.RasMArrayFloat") {
        flatArray <- jRasArray$getFloatArray()
    } else if (jRasArray %instanceof% "rasj.RasMArrayShort") {
        flatArray <- jRasArray$getShortArray()
    } else {
        flatArrays <- .rasStructureArrayToR(jRasArray)
        flatArrays <- lapply(flatArrays, function(x) array(x, dims))
        return(RasdamanArray(array=flatArrays, origin=as.integer(origin)))
    }
    data = array(flatArray, dims)
    RasdamanArray(array=list(data), origin=as.integer(origin))
}

.rasStructureArrayToR <- function(jRasArray) {
    type <- jRasArray$getBaseTypeSchema()
    if (!type$isStructType()) {
        stop(".rasStructureArrayToR works only with arrays of structures")
    }
    data <- .jevalArray(J("rrasdaman.RasUtil")$parseArray(jRasArray))
    data <- lapply(data, .jevalArray)
    attrs <- type$getAttributes()
    names(data) <- attrs
    return(data)
}

.rasStructureToR <- function(jobj) {
    type <- jobj$getType()
    attr.names <- type$getAttributes()
    values <- as.list(jobj$getElements())
    values <- lapply(values, .rasJavaToR)
    names(values) <- attr.names
    values
}

.rasMIntervalToR <- function(jObj) {
    dimcnt <- jObj$dimension()
    result <- lapply(1:dimcnt, function(i) {
        .rasSIntervalToR(jObj$item(as.integer(i-1)))
        })
    rev(result)
}

.rasSIntervalToR <- function(jObj) {
    list(low=jObj$low(), high=jObj$high())
}

.rasPointToR <- function(jobj) {
    dims <- jobj$dimension()
    result <- sapply(1:dims, function(i) jobj$item(as.integer(i - 1)))
    rev(result)
}

#####################################
## Conversion from R to Java types ##
#####################################

# Creates an object of type rasj.RasGMArray
.rasArrayToJava <- function(rasarray, schema, typename=NULL) {
    if (!is(rasarray, "RasdamanArray"))
        stop("only RasdamanArray conversion is supported")
    validObject(rasarray)
    jType <- J("rasj.RasType")$getAnyType(schema)
    if (jType$getTypeID() != globals$RAS_MARRAY)
        stop("only marray schemas are supported")
    jBaseType <- jType$getBaseType()
    jdomain <- .rasDomainToJava(dim(rasarray), rasarray@origin)

    if (jBaseType$isStructType()) {
        jArray <- .rasStructureArrayToJava(rasarray, jBaseType)
        jArray$setSpatialDomain(jdomain)
        jArray$setTypeStructure(schema)
    } else {
        jdata <- .rasToRawJavaArray(as.list(rasarray@array[[1]]), jBaseType)
        arrayTypeName <- .rasGetArrayTypeName(jBaseType)
        jArray <- .jnew(arrayTypeName, jdomain)
        jArray$setArray(jdata)
    }

    if (!is.null(typename))
        jArray$setObjectTypeName(typename)
    jArray
}

# Get typename of the appropriate rasj.RasGMArray subclass
.rasGetArrayTypeName <- function(type) {
    typeid <- type$getTypeID()
    if (typeid %in% c(globals$RAS_LONG, globals$RAS_INT, globals$RAS_USHORT))
        return("rasj.RasMArrayInteger")
    if (typeid == globals$RAS_ULONG)
        return("rasj.RasMArrayLong")
    if (typeid == globals$RAS_FLOAT)
        return("rasj.RasMArrayFloat")
    if (typeid == globals$RAS_DOUBLE )
        return("rasj.RasMArrayDouble")
    if (typeid == globals$RAS_SHORT)
        return("rasj.RasMArrayShort")
    if (typeid %in% c(globals$RAS_CHAR, globals$RAS_BYTE, globals$RAS_BOOLEAN))
        return("rasj.RasMArrayByte")
    return("rasj.RasGMArray")
}

# Convert the data into a raw java array (of primitives or bytes)
.rasToRawJavaArray <- function(data, type) {
    typeid <- type$getTypeID()
    if (type$isBaseType()) {
        if (typeid == globals$RAS_BYTE) {
            typesize <- type$getSize()
            data <- lapply(data, function(x) .toUnsigned(x, typesize))
        }
        if (typeid == globals$RAS_ULONG)
            result <- .jarray(.jlong(data))
        else if (typeid %in% c(globals$RAS_LONG, globals$RAS_INT, globals$RAS_USHORT))
            result <- .jarray(as.integer(data))
        else if (typeid == globals$RAS_SHORT)
            result <- .jarray(.jshort(data)) # DOES NOT WORK: rJava bug#40, no workaround found yet
        else if (typeid == globals$RAS_BOOLEAN)
            result <- .jarray(.jbyte(data))
        else if (typeid %in% c(globals$RAS_CHAR, globals$RAS_BYTE))
            result <- .jarray(as.raw(data))
        else if (typeid == globals$RAS_DOUBLE)
            result <- .jarray(as.numeric(data))
        else if (typeid == globals$RAS_FLOAT)
            result <- .jarray(.jfloat(data))
        else
            stop(paste("Unknown base type", type$toString()))
    } else {
        stop(paste("Unknown type", type$toString()))
    }
    result
}

.rasStructureArrayToJava <- function(data, type) {
    data <- lapply(data@array, array)
    df <- as.data.frame(data)
    l <- list()
    types <- .jevalArray(type$getBaseTypes())
    attrs <- type$getAttributes()
    perm <- match(attrs, names(data))
    for (i in 1:ncol(df)) {
        j <- perm[i]
        typeid <- types[[i]]$getTypeID()
        if (typeid %in% c(globals$RAS_CHAR, globals$RAS_BOOLEAN))
            l[[i]] <- .jarray(as.raw(df[,j]))
        else if (typeid %in% c(globals$RAS_ULONG, globals$RAS_LONG, globals$RAS_INT))
            l[[i]] <- .jarray(.jlong(df[,j]))
        else if (typeid %in% c(globals$RAS_SHORT, globals$RAS_USHORT, globals$RAS_BYTE))
            l[[i]] <- .jarray(as.integer(df[,j]))
        else if (typeid == globals$RAS_FLOAT)
            l[[i]] <- .jarray(.jfloat(df[,j]))
        else if (typeid == globals$RAS_DOUBLE)
            l[[i]] <- .jarray(as.numeric(df[,j]))
    }
    res <- J("rrasdaman.RasUtil")$createArray(.jarray(l), as.integer(nrow(df)), type)
    return(res)
}

.rasDomainToJava <- function(dimensions, origin) {
    dimensions <- rev(dimensions)
    origin <- rev(origin)
    if (length(dimensions) != length(origin))
        stop("Size of dimensions and origin must be the same")
    jdom <- .jnew("rasj/RasMInterval", as.integer(length(dimensions)))
    for (i in 1:length(dimensions)) {
        jsin <- jdom$item(as.integer(i-1))
        jsin$setLow(.jlong(origin[i]))
        jsin$setHigh(.jlong(origin[i] + dimensions[i] - 1))
    }
    jdom
}

.rasPointToJava <- function(point) {
    point <- rev(point)
    p <- .jnew("rasj.RasPoint", as.integer(length(point)))
    for (i in 1:length(point))
        p$setItem(as.integer(i-1), .jlong(point[i]))
    p
}

#####################################################
## Conversion between signed and unsigned integers ##
#####################################################

.toUnsigned <- function(value, intSize) {
    if (value < 0) {
        value <- value + 2 ** (intSize * 8)
    }
    value
}

.toSigned <- function(value, intSize) {
    bitCount <- 8 * intSize
    if (value >= 2 ** (bitCount - 1))
        value <- value - 2 ** bitCount
    value
}

# Retrieve object from the buffer of the given type
.rasElementFromBytes <- function(buffer, type) {
    jobject = J("rrasdaman.RasUtil")$parseElement(buffer, type)

    if (type$isStructType()) {
        attrs <- type$getAttributes()
        result <- .jevalArray(jobject)
        result <- lapply(result, .jsimplify)
        names(result) <- attrs

    } else {
        result <- .jsimplify(jobject)
    }
    result
}
