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
\name{dbGetInfo-methods}
\docType{methods}
\alias{dbGetInfo}
\alias{dbGetInfo-methods}
\alias{dbGetInfo,RasdamanObject-method}
\alias{dbListConnections}
\alias{dbListConnections-methods}
\alias{dbListConnections,RasdamanDriver-method}
\alias{dbListResults}
\alias{dbListResults-methods}
\alias{dbListResults,RasdamanConnection-method}

\title{Database objects meta-data}

\description{Getting meta data from an object, implementing some functionality
in the interface to Rasdaman database. These methods are implementations of the
corresponding generic function from DBI package.}

\usage{
dbGetInfo(dbObj, \dots)
dbListConnections(drv, \dots)
dbListResults(conn, \dots)
}
\arguments{
    \item{dbObj}{an object, implementing some functionality in the interface to
    Rasdaman database, such as RasdamanConnection, RasdamanDriver,
    RasdamanResult}
    \item{drv}{RasdamanDriver object}
    \item{conn}{RasdamanConnection object}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\details{
    \code{dbListConnections} returns a list of loaded connections.

    \code{dbListResults} returns a list of fetched results, which were not
    cleared.
}
\seealso{
    \code{\link[DBI]{dbGetInfo}} \code{\link{summary}}
    \code{\link[DBI]{dbListResults}} \code{\link[DBI]{dbListConnections}}

    \code{\link{RasdamanDriver}} \code{\link{RasdamanConnection}}
    \code{\link{RasdamanResult}}
}
\examples{
\dontrun{
drv <- Rasdaman()
conn <- dbConnect(drv)
dbGetInfo(drv)
summary(conn)
dbListConnections(drv)
res <- dbSendQuery(conn, "select x from rgb as x")
dbListResults(drv)
}
}