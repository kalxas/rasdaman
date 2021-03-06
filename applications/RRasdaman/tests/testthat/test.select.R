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

clear.connections <- function() {
    conns <- dbListConnections(Rasdaman())
    lapply(conns, dbDisconnect)
}

test_that("Reading arrays", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    # read an array of integers: beware reversed order
    data = array(c(1, 2, 3, 4, 5, 6), c(2, 3))
    origin = as.integer(c(0, 1))
    a <- RasdamanArray(array = list(data), origin = origin)
    b <- simplify(dbGetQuery(conn, "select <[1:3,0:1] 1,2;3,4;5,6>")[[1]])
    expect_equal(b, a)
    # read an array of structures
    red <- array(c(1, 4))
    green <- array(c(2, 5))
    blue <- array(c(3, 6))
    origin <- as.integer(-1)
    data = list(red = red, green = green, blue = blue)
    a <- RasdamanArray(array = data, origin = origin)
    b <- simplify(dbGetQuery(conn, "select (RGBPixel) <[-1:0] {1,2,3}, {4,5,6}>")[[1]])
    expect_equal(b, a)

    dbDisconnect(conn)
})

test_that("Reading complex data types values", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    # SInterval
    sinterval <- list(low = 0, high = 12)
    expect_equal(sinterval, simplify(dbGetQuery(conn, "select 0:12")[[1]]))
    # MInterval: beware reverse order
    sinterval2 <- list(low = -1, high = 1)
    domain <- list(sinterval2, sinterval)
    expect_equal(domain, simplify(dbGetQuery(conn, "select [0:12, -1:1]")[[1]]))
    # Point: beware reverse order
    point <- c(3, 2, 1)
    expect_equal(point, simplify(dbGetQuery(conn, "select [1,2,3]")[[1]]))
    # RGB
    pixel <- list(red = 1, green = 2, blue = 3)
    expect_equal(pixel, simplify(dbGetQuery(conn, "select (RGBPixel) {1,2,3}")[[1]]))

    dbDisconnect(conn)
})

test_that("Reading primitive values", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    expect_equal(255, simplify(dbGetQuery(conn, "select 255c")[[1]]))
    expect_equal(-128, simplify(dbGetQuery(conn, "select -128o")[[1]]))
    expect_equal(-32768, simplify(dbGetQuery(conn, "select -32768s")[[1]]))
    expect_equal(65535, simplify(dbGetQuery(conn, "select 65535us")[[1]]))
    # rJava bug #39: .jsimplify does not work with -2147483648
    # expect_equal(-2147483648, simplify(dbGetQuery(conn, "select -2147483648l")[[1]]))
    expect_equal(-2147483647, simplify(dbGetQuery(conn, "select -2147483647l")[[1]]))
    expect_equal(4294967295, simplify(dbGetQuery(conn, "select 4294967295ul")[[1]]))

    expect_equal(3.1415, simplify(dbGetQuery(conn, "select 3.1415f")[[1]]))
    expect_equal(2.71828182846, simplify(dbGetQuery(conn, "select 2.71828182846d")[[1]]))

    dbDisconnect(conn)
})

test_that("Reading arrays of primitive values", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    expect_equal(
        list(array(c(TRUE,FALSE))),
        simplify(dbGetQuery(conn, "select <[0:1] true, false>")[[1]])@array)
    expect_equal(
        list(array(c(-128,127))),
        simplify(dbGetQuery(conn, "select <[0:1] -128o, 127o>")[[1]])@array)
    expect_equal(
        list(array(c(0,255))),
        simplify(dbGetQuery(conn, "select <[0:1] 0c, 255c>")[[1]])@array)
    expect_equal(
        list(array(c(-32768,32767))),
        simplify(dbGetQuery(conn, "select <[0:1] -32768s, 32767s>")[[1]])@array)
    expect_equal(
        list(array(c(0,65535))),
        simplify(dbGetQuery(conn, "select <[0:1] 0us, 65535us>")[[1]])@array)
    # rJava bug #39: wrong conversion of -2147483648
    expect_equal(
        list(array(c(-2147483647,2147483647))),
        simplify(dbGetQuery(conn, "select <[0:1] -2147483647l, 2147483647l>")[[1]])@array)
    expect_equal(
        list(array(c(0,4294967295))),
        simplify(dbGetQuery(conn, "select <[0:1] 0ul, 4294967295ul>")[[1]])@array)
    expect_equal(
        list(array(c(3.14159265,2.71828182846))),
        simplify(dbGetQuery(conn, "select <[0:1] 3.14159265d, 2.71828182846d>")[[1]])@array)
    expect_equal(
        list(array(c(3.1415,2.7182))),
        simplify(dbGetQuery(conn, "select <[0:1] 3.1415f, 2.7182f>")[[1]])@array)

    dbDisconnect(conn)
})

test_that("Test equivalence of different ways to read a cell", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    # Test equivalence of three different ways to get a cell:
    #   1. specify cell in a query
    #   2. get an array; get a cell via getCell(RasdamanArrayHandle, ...)
    #   3. get an array; convert to R; get a cell
    point <- simplify(dbGetQuery(conn, "select [2,0]")[[1]])
    data_handle <- dbGetQuery(conn, "select <[1:2,-1:1] 1,2,3;4,5,6>")[[1]]
    data <- simplify(data_handle)

    query_cell <- simplify(dbGetQuery(conn, "select <[1:2,-1:1] 1,2,3;4,5,6>[2,0]")[[1]])
    handle_get_cell <- getCell(data_handle, point)
    # get correct R point: R indexes start from [1,1], rasdaman indexes start from origin point
    data_point <- point - data@origin + 1
    data_cell <- data@array[[1]][data_point[1], data_point[2]]

    expect_equal(query_cell, handle_get_cell)
    expect_equal(query_cell, data_cell)

    dbDisconnect(conn)
})

test_that("Reading a complex type cell", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    handle <- dbGetQuery(conn, "select (RGBPixel) <[-1:0] {1,2,3}, {4,5,6}>")[[1]]
    point = c(-1)
    expected_cell = list(red=1, green=2, blue=3)
    cell <- getCell(handle, c(-1))
    expect_equal(cell, expected_cell)

    dbDisconnect(conn)
})

test_that("Reading structures with different attribute types", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    expect_equal(
        list(`0`=-128, `1`=127),
        simplify(dbGetQuery(conn, "select {-128o, 127o}")[[1]]))
    expect_equal(
        list(`0`=0, `1`=255),
        simplify(dbGetQuery(conn, "select {0c, 255c}")[[1]]))
    expect_equal(
        list(`0`=-32768, `1`=32767),
        simplify(dbGetQuery(conn, "select {-32768s, 32767s}")[[1]]))
    expect_equal(
        list(`0`=0, `1`=65535),
        simplify(dbGetQuery(conn, "select {0us, 65535us}")[[1]]))
    # rJava bug #39: wrong conversion of -2147483648
    expect_equal(
        list(`0`=-2147483647, `1`=2147483647),
        simplify(dbGetQuery(conn, "select {-2147483647l, 2147483647l}")[[1]]))
    expect_equal(
        list(`0`=0, `1`=4294967295),
        simplify(dbGetQuery(conn, "select {0ul, 4294967295ul}")[[1]]))
    expect_equal(
        list(`0`=3.14159265, `1`=2.71828182846),
        simplify(dbGetQuery(conn, "select {3.14159265d, 2.71828182846d}")[[1]]))
    expect_equal(
        list(`0`=3.1415, `1`=2.7182),
        simplify(dbGetQuery(conn, "select {3.1415f, 2.7182f}")[[1]]))

    dbDisconnect(conn)
})

test_that("Arrays of structures with different attribute types", {
    clear.connections()
    conn <- dbConnect(Rasdaman())

    expect_equal(
        RasdamanArray(list(`0`=as.array(-128), `1`=as.array(127)), as.integer(0)),
        simplify(dbGetQuery(conn, "select <[0:0] {-128o, 127o}>")[[1]]))
    expect_equal(
        RasdamanArray(list(`0`=as.array(0), `1`=as.array(255)), as.integer(0)),
        simplify(dbGetQuery(conn, "select <[0:0] {0c, 255c}>")[[1]]))
    expect_equal(
        RasdamanArray(list(`0`=as.array(-32768), `1`=as.array(32767)), as.integer(0)),
        simplify(dbGetQuery(conn, "select <[0:0] {-32768s, 32767s}>")[[1]]))
    expect_equal(
        RasdamanArray(list(`0`=as.array(0), `1`=as.array(65535)), as.integer(0)),
        simplify(dbGetQuery(conn, "select <[0:0] {0us, 65535us}>")[[1]]))
    # rJava bug #39: wrong conversion of -2147483648
    expect_equal(
        RasdamanArray(list(`0`=as.array(-2147483647), `1`=as.array(2147483647)), as.integer(0)),
        simplify(dbGetQuery(conn, "select <[0:0] {-2147483647l, 2147483647l}>")[[1]]))
    expect_equal(
        RasdamanArray(list(`0`=as.array(0), `1`=as.array(4294967295)), as.integer(0)),
        simplify(dbGetQuery(conn, "select <[0:0] {0ul, 4294967295ul}>")[[1]]))
    expect_equal(
        RasdamanArray(list(`0`=as.array(3.14159265), `1`=as.array(2.71828182846)), as.integer(0)),
        simplify(dbGetQuery(conn, "select <[0:0] {3.14159265d, 2.71828182846d}>")[[1]]))
    expect_equal(
        RasdamanArray(list(`0`=as.array(3.1415), `1`=as.array(2.7182)), as.integer(0)),
        simplify(dbGetQuery(conn, "select <[0:0] {3.1415f, 2.7182f}>")[[1]]))

    dbDisconnect(conn)
})
