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
\name{dbListCollections-methods}
\docType{methods}
\alias{dbListCollections}
\alias{dbListCollections-methods}
\alias{dbListCollections,RasdamanConnection-method}
\alias{dbExistsCollection-methods}
\alias{dbExistsCollection}
\alias{dbExistsCollection,RasdamanConnection-method}
\alias{dbListTables,RasdamanConnection-method}
\alias{dbExistsTable,RasdamanConnection-method}

\title{Getting information about collections}

\description{Listing existing collections (tables), checking collections for
existence.}

\usage{
dbListCollections(conn, \dots)
dbExistsCollection(conn, name, \dots)
}
\arguments{
    \item{conn}{connection object}
    \item{name}{collection name}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\seealso{
    \code{\link[RRasdaman]{dbConnect}} \code{\link{dbInsertCollection}}
    \code{\link{dbUpdateCollection}} \code{\link{dbReadCollection}}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman())
dbListCollections(conn)
dbExistsCollection(conn, "rgb")
}}