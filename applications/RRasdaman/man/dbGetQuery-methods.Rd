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
\name{dbGetQuery-methods}
\alias{dbGetQuery}
\alias{dbGetQuery-methods}
\alias{dbGetQuery,RasdamanConnection-method}
\alias{dbSendQuery}
\alias{dbSendQuery-methods}
\alias{dbSendQuery,RasdamanConnection-method}
\docType{methods}
\title{Executing a statement}

\description{Executing a statement on a given connection to Rasdaman database.}

\usage{
dbSendQuery(conn, statement, \dots)
dbGetQuery(conn, statement, \dots)
}
\arguments{
    \item{conn}{a connection object of class RasdamanConnection}
    \item{statement}{a query statement to execute}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\value{
    \code{dbSendQuery} returns a \code{RasdamanResult} object.

    \code{dbGetQuery} returns a list of \code{RasdamanHandle} objects, through
    which the result can be accessed.
}
\seealso{
    \code{\link{RasdamanResult}} \code{\link{RasdamanHandle}}
    \code{\link{RasdamanArrayHandle}} \code{\link{RasdamanConnection}}

    \code{\link[RRasdaman]{simplify}} \code{\link{dbReadCollection}}
    \code{\link{dbInsertCollection}} \code{\link{dbUpdateCollection}}
    \code{\link{dbFetch}}
}
\examples{
\dontrun{
conn <- dbConnect(Rasdaman())

rasres <- dbSendQuery(conn, "select rgb from rgb")
handle <- dbFetch(rasres, 1)[[1]]
array <- simplify(handle)

# The same result can be get by dbGetQuery
handle <- dbGetQuery(conn, "select rgb from rgb")[[1]]
array <- simplify(handle)
}
}