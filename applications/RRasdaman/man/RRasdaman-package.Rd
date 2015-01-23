%% This file is part of rasdaman community.
%%
%% Rasdaman community is free software: you can redistribute it and/or modify
%% it under the terms of the GNU Lesser General Public License as published by
%% the Free Software Foundation, either version 3 of the License, or
%% (at your option) any later version.
%%
%% Rasdaman community is distributed in the hope that it will be useful,
%% but WITHOUT ANY WARRANTY; without even the implied warranty of
%% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
%% GNU Lesser General Public License for more details.
%%
%% You should have received a copy of the GNU Lesser General Public License
%% along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
%%
%% Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
%% rasdaman GmbH.
%%
%% For more information please see <http://www.rasdaman.org>
%% or contact Peter Baumann via <baumann@rasdaman.com>.
%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\name{RRasdaman-package}
\alias{RRasdaman-package}
\alias{RRasdaman}
\docType{package}
\title{
R interface to the Rasdaman database
}
\description{
Rasdaman driver for R, uses the database interface definition
from the package DBI. Allows interaction with Rasdaman databases
from R.
}
\section{Overview}{
    Typical session using the Rasdaman interface is:
    \enumerate{
    \item Create driver and connection:
    \preformatted{
    drv  <- Rasdaman()
    conn <- dbConnect(drv, user="rasuser", password="secret")
    }
    \item List collections and check collections for existence:
    \preformatted{
    dbListCollections(conn)
    dbExistsCollection(conn, "mr")
    }
    \item Get meta-information on connection and driver:
    \preformatted{
    dbListConnections(drv)
    summary(drv)
    summary(conn)
    }

    \item Retrieving data from the database and processing it:
    \preformatted{
    handles <- dbGetQuery("select x.blue from rgb as x")
    if (length(handles) > 0) {
        array <- simplify(handles[[1]])
        image(array@array)
    }

    handles <- dbGetQuery("select x from rgb as x")
    if (length(handles) > 0) {
        array <- simplify(handles[[1]])
        col <- rgb(array@array$red, array@array$green, array@array$blue, max=255)
        dim(col) <- dim(array)
        library(grid)
        grid.raster(col, interpolate=F)
    }
    }

    \item Closing the connection and releasing resources:
    \preformatted{
    dbCommit(conn)
    dbDisconnect(conn)
    dbUnloadDriver(drv)
    }
    }
}
\section{Notes}{
    Both R and Rasdaman store arrays in the column-major order, so an array within
    domain \code{[1:2,1:3]} has 2 columns and 3 rows. However, R and Rasdaman
    use different notation for referencing elements of arrays: \code{arr[1,2]}
    is the cell in the first row and the second column in R, but Rasdaman
    interprets it as the cell in the second row and the first column. All
    methods of RRasdaman package accept R-style points and dimensions (first
    rows, then columns).

    RRasdaman package uses Rasdaman java client library to communicate with
    servers. When working with large arrays, it is useful to set higher java
    heap size before loading the RRasdaman package, as by default it is set
    to 512 MB:
    \preformatted{
    options(java.parameters = "-Xmx1g")
    library(RRasdaman)
    }
}
\section{Terminology}{
    Traditionally in relational database systems users operate on tables of rows
    and columns, where each row represents an object and each column represents
    an attribute of these objects. Each table has a fixed schema, describing
    the set of columns, their names and types.

    However, in Rasdaman users deal with collections of arrays, not tables
    of rows, and each collection has a fixed schema for its arrays.

    \code{RRasdaman} is an implementation of database interface described in
    the package \code{DBI}. \code{DBI} was designed for relational databases and some
    of its methods operate on tables, rows and columns. To reduce mess in
    terminology, RRasdaman introduces analogous methods operating on collections
    and arrays. For example, instead of using \code{dbListTables} to list all
    collections in a system \code{dbListCollections} should be used.
}
\author{
 Veranika Liaukevich <v.liaukevich@jacobs-university.de>
}
\keyword{ package }
\references{
See \href{http://www.rasdaman.org/browser/manuals_and_examples/manuals/doc-guides/ql-guide.pdf}{Rasdaman Query Language Quide}
}
\seealso{
    DBI classes:

    \code{\link[DBI]{DBIObject-class}}
    \code{\link[DBI]{DBIDriver-class}}
    \code{\link[DBI]{DBIConnection-class}}
    \code{\link[DBI]{DBIResult-class}}

    Rasdaman classes:

    \code{\link{RasdamanObject}}
    \code{\link{RasdamanDriver}}
    \code{\link{RasdamanConnection}}
    \code{\link{RasdamanResult}}
    \code{\link{RasdamanArray}}
    \code{\link{RasdamanHandle}}

    Database management:

    \code{\link[DBI]{dbDriver}}

    Database connections:

    \code{\link[DBI]{dbConnect}}
    \code{\link[DBI]{dbDisconnect}}
    \code{\link[DBI]{dbCommit}}
    \code{\link[DBI]{dbRollback}}

    Meta-data:

    \code{\link[DBI]{dbListConnections}}
    \code{\link[RRasdaman]{dbListCollections}}
    \code{\link[RRasdaman]{dbExistsCollection}}
    \code{\link[DBI]{dbGetInfo}}
}
