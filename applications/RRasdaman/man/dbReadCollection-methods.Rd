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
\name{dbReadCollection-methods}
\docType{methods}
\alias{dbReadCollection}
\alias{dbReadCollection-methods}
\alias{dbReadCollection,RasdamanConnection-method}

\title{Reading a Rasdaman collection}

\description{Reads a collection by its name.}

\usage{
dbReadCollection(conn, name, \dots)
}
\arguments{
    \item{conn}{a RasdamanConnection object}
    \item{name}{a collection name}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\value{
    The method returns a list of \code{\link{RasdamanArrayHandle}}.
}
\seealso{
    \code{\link{RasdamanArrayHandle}} \code{\link{RasdamanConnection}}

    \code{\link{dbGetQuery}} \code{\link{dbSendQuery}}
    \code{\link{dbInsertCollection}} \code{\link{dbUpdateCollection}}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman())
handles <- dbReadCollection(conn, "rgb")
dbDisconnect(conn)
}}
