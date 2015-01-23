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
\name{dbRemoveCollection-methods}
\alias{dbRemoveCollection}
\alias{dbRemoveCollection-methods}
\alias{dbRemoveCollection,RasdamanConnection-method}
\alias{dbRemoveTable,RasdamanConnection-method}

\title{Remove a collection by name}

\description{This method is a part of the implementation of the database
interface.}

\usage{
dbRemoveCollection(conn, name, \dots)
}
\arguments{
    \item{conn}{connection object}
    \item{name}{name of the collection to remove}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\seealso{
    \code{\link{RasdamanConnection}}

    \code{\link{dbExistsCollection}} \code{\link{dbListCollections}}
    \code{\link{dbReadCollection}} \code{\link{dbInsertCollection}}
    \code{\link{dbUpdateCollection}}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman())
if (dbExistsCollection(conn, "rgb")) {
    dbRemoveCollection(conn, "rgb")
}
dbCommit(conn)
dbDisconnect(conn)
}}
