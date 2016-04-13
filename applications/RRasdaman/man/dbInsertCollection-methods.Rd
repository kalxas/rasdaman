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
\name{dbInsertCollection-methods}
\alias{dbInsertCollection}
\alias{dbInsertCollection-methods}
\alias{dbInsertCollection,RasdamanConnection-method}
\alias{dbUpdateCollection}
\alias{dbUpdateCollection-methods}
\alias{dbUpdateCollection,RasdamanConnection-method}

\title{Inserting data into a Rasdaman database}

\description{
Inserting arrays into a Rasdaman database and updating them.
}

\usage{
\S4method{dbInsertCollection}{RasdamanConnection}(conn, name, value, typename, \dots)
\S4method{dbUpdateCollection}{RasdamanConnection}(conn, name, value, typename, region=NULL, where=NULL, \dots)
}
\arguments{
    \item{conn}{a \code{RasdamanConnection} object}
    \item{name}{the name of a collection to which insert the data}

    \item{value}{an \code{RasdamanArray} object. All cells of the array should
    be convertable to the type, specified by \code{typename} argument.}

    \item{typename}{the typename of the inserting/updating array, e.g.
    \code{RGBImage}, \code{LongString}. One can get the list of typenames
    available on their Rasdaman installaction by running \code{rasdl -p}. For
    more details see Rasdaman Query Language Quide.}

    \item{region}{the region to update specified as string representation of
    multidimensional interval (e.g. \code{"[0:7,0:7]"}). If this argument is not
    specified, this field will be filled based on the dimensionality and origin
    point of the \code{value} argument. Anyway, the origin of \code{value}
    should be the same as the specified \code{region}.}

    \item{where}{condition by which updating arrays should be selected.
    For example: \code{"oid(rgb) = 123"}.}

    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\value{
    \code{dbInsertCollection} returns OID of the freshly inserted array.
}
\references{
    See \href{http://www.rasdaman.org/browser/manuals_and_examples/manuals/doc-guides/ql-guide.pdf}{Rasdaman Query Language Quide}
    for more details about typenames (Section 4. Type Definition) and specifying
    multidimensional intervals (Section 8.2. Multidimensional Intervals).
}
\seealso{
    \code{\link[RRasdaman]{dbGetQuery}} \code{\link[RRasdaman]{dbSendQuery}}
    \code{\link{dbReadCollection}} \code{\link{dbRemoveCollection}}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman(), user="rasadmin", password="rasadmin", mode=CONN_READ_WRITE)

#Reading data
handles <- dbGetQuery(conn, "select x[100:200, 100:200] from rgb as x")
array <- simplify(handles[[1]])

# Inserting data
oid <- dbInsertCollection(conn, "rgb", array, "RGBImage")

# Generating new data
redch <- array(0, c(50,50))
greench <- array(255, c(50,50))
bluech <- array(0, c(50,50))
data <- list(red=redch, green=greench, blue=bluech)
array <- RasdamanArray(data, as.integer(c(100,100)))

# Updating
condition = paste("oid(rgb) =", oid)
dbUpdateCollection(conn, "rgb", array, "RGBImage", region="[100:149,100:149]",
    where=condition)

# Closing a connection
dbCommit(conn)
dbDisconnect(conn)
}}