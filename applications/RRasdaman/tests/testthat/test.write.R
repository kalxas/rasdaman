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
    query <- paste("create collection", temp.coll, "LongSet")
    dbGetQuery(conn, query)
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
    dbRemoveCollection(conn, temp.coll)

    dbDisconnect(conn)
})

test_that("Insert, update and read an RGB array", {
    clear.connections(TRUE)
    conn <- dbConnect(Rasdaman(), user = "rasadmin", password = "rasadmin",
        mode = CONN_READ_WRITE)

    # create an rgb array
    query <- paste("create collection", temp.coll, "RGBSet")
    dbGetQuery(conn, query)
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
    dbRemoveCollection(conn, temp.coll)
    dbDisconnect(conn)
})
