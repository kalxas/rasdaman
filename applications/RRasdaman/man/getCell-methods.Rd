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
\name{getCell-methods}
\alias{getCell-methods}
\docType{methods}
\alias{getCell}
\alias{getCell,RasdamanArrayHandle-method}

\title{Getting a cell from an array}

\description{Retrieving a cell value from \code{\link{RasdamanArrayHandle}}
without converting the latter into \code{\link{RasdamanArray}}.}

\usage{
getCell(handle, point, \dots)
}
\arguments{
    \item{handle}{an array handle}
    \item{point}{a vector with point coordinates}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\value{
    Result is converted into R type according to the conversion rules, described
    in \code{\link[RRasdaman]{simplify}}.
}
\seealso{
    \code{\link{RasdamanHandle}} \code{\link{RasdamanArrayHandle}}

    \code{\link[RRasdaman]{simplify}} \code{\link[RRasdaman]{dbGetQuery}}
    \code{\link[RRasdaman]{dbSendQuery}}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman())
handles <- dbGetQuery(conn, "select x from rgb as x")
cell <- getCell(handles[[1]], c(42,42))
dbDisconnect(conn)
}}