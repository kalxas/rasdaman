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
library("RRasdaman")

context("Connection management functions")

temp.coll = "tmpRtest"

clear.connections <- function(clear.tables=FALSE) {
    conns <- dbListConnections(Rasdaman())
    lapply(conns, dbDisconnect)
    if (clear.tables) {
        conn <- dbConnect(Rasdaman(), user="rasadmin", password="rasadmin",
                      mode = CONN_READ_WRITE)
        dbRemoveCollection(conn,temp.coll)
        dbDisconnect(conn)
    }
}

test_that("Connections number is correct", {
    clear.connections()
    drv <- Rasdaman()
    expect_equal(0, length(dbListConnections(drv)))
    conn <- dbConnect(Rasdaman())
    expect_equal(1, length(dbListConnections(drv)))
    dbDisconnect(conn)
    expect_equal(0, length(dbListConnections(drv)))
})

test_that("Readonly connection does not allow creating collections", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    query <- paste("create collection", temp.coll, "RGBSet")
    expect_error(dbGetQuery(conn, query))

    dbDisconnect(conn)
})

test_that("Create and remove collections with read-write connection", {
    clear.connections(TRUE)
    conn <- dbConnect(Rasdaman(), user = "rasadmin", password = "rasadmin",
        mode = CONN_READ_WRITE)

    query <- paste("create collection", temp.coll, "RGBSet")
    dbGetQuery(conn, query)
    expect_true(dbExistsCollection(conn, temp.coll))
    dbRemoveCollection(conn, temp.coll)
    dbExistsCollection(conn, temp.coll)
    expect_false(dbExistsCollection(conn, temp.coll))

    dbDisconnect(conn)
})

test_that("Commit and abort transactions work correctly", {
    clear.connections(TRUE)
    conn <- dbConnect(Rasdaman(), user = "rasadmin", password = "rasadmin",
        mode = CONN_READ_WRITE)

    # Create collection
    query <- paste("create collection", temp.coll, "RGBSet")
    dbGetQuery(conn, query)
    dbCommit(conn)
    expect_true(dbExistsCollection(conn, temp.coll))
    # Remove collection
    dbRemoveCollection(conn, temp.coll)
    dbExistsCollection(conn, temp.coll)
    expect_false(dbExistsCollection(conn, temp.coll))
    # Rollback: collection still exists
    dbRollback(conn, temp.coll)
    expect_true(dbExistsCollection(conn, temp.coll))
    # Commit removed collection
    dbRemoveCollection(conn, temp.coll)
    dbCommit(conn)
    expect_false(dbExistsCollection(conn, temp.coll))

    dbDisconnect(conn)
})
