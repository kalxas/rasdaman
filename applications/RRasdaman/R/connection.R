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
# PURPOSE: implementation of RasdamanConnection class
#
##################################################################

##
## Class implementation: RasdamanConnection
##

setGeneric("dbDisconnect", function(conn, ...) standardGeneric("dbDisconnect"))
setMethod("dbDisconnect", "RasdamanConnection",
    def = function(conn, ...) { tryCatch({
        conn@jObj$disconnect()
        TRUE
    }, Exception = .handler) }
)

##### Transaction management #####

setGeneric("dbCommit", function(conn, ...) standardGeneric("dbCommit"))
setMethod("dbCommit", "RasdamanConnection",
    def = function(conn, ...) { tryCatch({
        conn@jObj$commit()
        TRUE
    }, Exception = .handler) }
)

setGeneric("dbRollback", function(conn, ...) standardGeneric("dbRollback"))
setMethod("dbRollback", "RasdamanConnection",
    def = function(conn, ...) { tryCatch({
        conn@jObj$rollback()
        TRUE
    }, Exception = .handler) }
)

##### Listing collections #####

setGeneric("dbExistsCollection", function(conn, name, ...)
    standardGeneric("dbExistsCollection"))
setMethod("dbExistsCollection", "RasdamanConnection",
    def = function(conn, name, ...) { tryCatch({
        conn@jObj$existsCollection(name)
    }, Exception = .handler) }
)

setGeneric("dbListCollections", function(conn, ...)
    standardGeneric("dbListCollections"))
setMethod("dbListCollections", "RasdamanConnection",
    def = function(conn,...) { tryCatch({
        jarray <- .jevalArray(conn@jObj$listCollections()$toArray())
        sapply(jarray, .jsimplify)
    }, Exception = .handler) }
)

##### Sending queries #####

setGeneric("dbSendQuery", function(conn, statement, ...)
    standardGeneric("dbSendQuery"))
setMethod("dbSendQuery", "RasdamanConnection",
    def = function(conn, statement, ...) { tryCatch({
        jresult <- conn@jObj$executeQuery(statement)
        result <- new("RasdamanResult", jObj = jresult)
        result
    }, Exception = .handler) }
)

setGeneric("dbGetQuery", function(conn, statement, ...)
    standardGeneric("dbGetQuery"))
setMethod("dbGetQuery", "RasdamanConnection",
    def = function(conn, statement, ...) { tryCatch({
        result <- dbSendQuery(conn, statement)
        arrays <- dbFetch(result)
        dbClearResult(result)
        arrays
    }, Exception = .handler) }
)

setGeneric("dbListResults", function(conn, ...) standardGeneric("dbListResults"))
setMethod("dbListResults", "RasdamanConnection",
    def = function(conn, ...) { tryCatch({
        results <- as.list(conn@jObj$listResults())
        lapply(results, function(x) new("RasdamanResult", jObj=x))
    }, Exception = .handler) }
)

setGeneric("dbReadCollection", function(conn, name, ...)
    standardGeneric("dbReadCollection"))
setMethod("dbReadCollection", "RasdamanConnection",
    def = function(conn, name, ...) {
        dbGetQuery(conn, paste("select x from", name, "as x"))
    }
)

setGeneric("dbInsertCollection", function(conn, name, value, ...)
    standardGeneric("dbInsertCollection"))
setMethod("dbInsertCollection", "RasdamanConnection",
    # TODO: remove schema parameter, get schema from database
    def = function(conn, name, value, typename, ...) { tryCatch({
        schema <- conn@jObj$getTypeStructure(typename)
        jArray <- .rasArrayToJava(value, schema, typename)
        jArgsList <- .jnew("java/util/ArrayList", as.integer(1))
        jArgsList$add(jArray)
        query <- paste("insert into", name, "values $1")
        jResult <- conn@jObj$executeQuery(query, jArgsList)
        res <- new("RasdamanResult", jObj = jResult)
        oid <- simplify(dbFetch(res,1)[[1]])
        oid
    }, Exception = .handler) }
)

setGeneric("dbUpdateCollection", function(conn, name, value, ...)
    standardGeneric("dbUpdateCollection"))
setMethod("dbUpdateCollection", "RasdamanConnection",
    def = function(conn, name, value, typename, region=NULL, where=NULL, ...) {
    tryCatch({
        schema <- conn@jObj$getTypeStructure(typename)
        jArray <- .rasArrayToJava(value, schema, typename)
        if (is.null(where)) whereclause <- ""
        else                whereclause <- paste("where", where)
        if (is.null(region))
            region <- .rasDomainToJava(dim(value), value@origin)

        jArgsList <- .jnew("java/util/ArrayList")
        query <- "update $1 set $1 $2 assign $3 $4"
        jArgsList <- .jnew("java/util/ArrayList", as.integer(4))
        jArgsList$add(name)
        jArgsList$add(region)
        jArgsList$add(jArray)
        jArgsList$add(whereclause)

        jResult <- conn@jObj$executeQuery(query, jArgsList)
        new("RasdamanResult", jObj = jResult)
    }, Exception = .handler) }
)


setGeneric("dbRemoveCollection", function(conn, name, ...)
    standardGeneric("dbRemoveCollection"))
setMethod("dbRemoveCollection", "RasdamanConnection",
    def = function(conn, name, ...) { tryCatch({
        conn@jObj$removeCollection(name)
    }, Exception = .handler) }
)
