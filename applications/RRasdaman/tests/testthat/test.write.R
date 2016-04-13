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
temp.coll = "tmpRtest"
library(RRasdaman)

clear.connections <- function(clear.tables=FALSE) {
    drv <- Rasdaman()
    conns <- dbListConnections(drv)
    lapply(conns, dbDisconnect)
    if (clear.tables) {
        conn <- dbConnect(Rasdaman(), user="rasadmin", password="rasadmin",
                          mode = CONN_READ_WRITE)
        dbRemoveCollection(conn, temp.coll)
        dbDisconnect(conn)
    }
}

create.collection <- function(conn, type) {
    query <- paste("create collection", temp.coll, type)
    dbGetQuery(conn, query)
}

remove.collection <- function(conn) {
    dbRemoveCollection(conn, temp.coll)
}

.setUp <- function() {
    set.seed(42, "Mersenne-Twister")
}

gen.array <- function(dimensions, mn, mx) {
    data <- sample(mn:mx, prod(dimensions), replace=T)
    array(data, dimensions)
}

test_that("Insert, update and read an array of integers", {
    clear.connections(TRUE)
    conn <- dbConnect(Rasdaman(), user = "rasadmin", password = "rasadmin",
        mode = CONN_READ_WRITE)

    # create an array
    create.collection(conn, "LongSet")
    data <- gen.array(c(3, 4), 0, 100)
    origin <- as.integer(c(1, 2))
    arr <- RasdamanArray(array = list(data), origin = origin)
    dbInsertCollection(conn, temp.coll, arr, "LongImage")
    dbCommit(conn)
    # read the array
    handles <- dbReadCollection(conn, temp.coll)
    expect_equal(1, length(handles))
    expect_equal(arr, simplify(handles[[1]]))
    # update
    subdata <- gen.array(c(2, 1), 0, 100)
    arr2 <- RasdamanArray(array = list(subdata), origin = as.integer(c(1,
        3)))
    arr@array[[1]][1:2, 2:2] <- subdata
    dbUpdateCollection(conn, temp.coll, arr2, "LongImage")
    dbCommit(conn)
    # check updated array
    handles <- dbReadCollection(conn, temp.coll)
    expect_equal(1, length(handles))
    expect_equal(arr, simplify(handles[[1]]))
    # clean up
    remove.collection(conn)

    dbDisconnect(conn)
})

test_that("Insert, update and read an RGB array", {
    clear.connections(TRUE)
    conn <- dbConnect(Rasdaman(), user = "rasadmin", password = "rasadmin",
        mode = CONN_READ_WRITE)

    # create an rgb array
    create.collection(conn, "RGBSet")
    green <- gen.array(c(3, 4), 0, 100)
    red <- gen.array(c(3, 4), 0, 100)
    blue <- gen.array(c(3, 4), 0, 100)
    origin <- as.integer(c(1, 2))
    data <- list(red = red, green = green, blue = blue)
    arr <- RasdamanArray(array = data, origin = origin)
    dbInsertCollection(conn, temp.coll, arr, "RGBImage")
    dbCommit(conn)

    # read inserted array
    handles <- dbReadCollection(conn, temp.coll)
    expect_equal(1, length(handles))
    arr_from_db <- simplify(handles[[1]])
    # the read array misses attributes names, so do a little trick for testing
    names(arr_from_db@array) <- names(arr@array)
    expect_equal(arr, arr_from_db)

    # update array
    green <- gen.array(c(2, 1), 0, 100)
    red <- gen.array(c(2, 1), 0, 100)
    blue <- gen.array(c(2, 1), 0, 100)
    subdata = list(green = green, red = red, blue = blue)
    arr2 <- RasdamanArray(array = subdata, origin = as.integer(c(1,
        3)))
    arr@array$green[1:2, 2:2] <- green
    arr@array$red[1:2, 2:2] <- red
    arr@array$blue[1:2, 2:2] <- blue
    dbUpdateCollection(conn, temp.coll, arr2, "RGBImage")
    dbCommit(conn)

    # read updated array
    handles <- dbReadCollection(conn, temp.coll)
    expect_equal(1, length(handles))
    arr_from_db <- simplify(handles[[1]])
    # the read array misses attributes names, so do a little trick for testing
    names(arr_from_db@array) <- names(arr@array)
    expect_equal(arr, arr_from_db)

    # clean up
    remove.collection(conn)
    dbDisconnect(conn)
})

test_that("Insertion of different array types", {
    test.insertion <- function(value, valuetype) {
        clear.connections(TRUE)
        conn <- dbConnect(Rasdaman(), user = "rasadmin", password = "rasadmin",
                          mode = CONN_READ_WRITE)
        if (temp.coll %in% dbListCollections(conn)) {
            remove.collection(conn)
            dbCommit(conn)
        }
        create.collection(conn, type=paste(valuetype, "Set1", sep=''))

        arr.write <- RasdamanArray(list(as.array(value)), as.integer(0))
        dbInsertCollection(conn, name=temp.coll, value=arr.write,
                           typename=paste(valuetype, "String", sep=''))
        arr.read <- simplify(dbReadCollection(conn, temp.coll)[[1]])
        expect_equal(arr.read, arr.write)

        dbRollback(conn)
        dbDisconnect(conn)
    }

    test.insertion(c(-128, 127), "Octet")
    test.insertion(c(0, 255), "Grey")
    # does not work because of rJava bug #40
    # test.insertion(c(-32768,32767), "Short")
    test.insertion(c(0,65535), "UShort")
    test.insertion(c(-2147483647,2147483647), "Long")
    test.insertion(c(0,4294967295), "ULong")
    test.insertion(c(3.14159265,2.71828182846), "Double")
    test.insertion(c(3.1415,2.7182), "Float")
})
