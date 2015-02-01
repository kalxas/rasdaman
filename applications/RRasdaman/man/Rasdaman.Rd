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
\name{Rasdaman}
\alias{Rasdaman}
\alias{dbDriver-methods}
\alias{dbDriver,character-methods}

\title{
    Instantiate a Rasdaman driver object.
}
\description{
    The function creates and initializes a Rasdaman driver.
    Returns RasdamanDriver object, which later could be used for
    connection creation.
}
\usage{
Rasdaman()
}
\value{
    An object \code{RasdamanDriver}, which is required to create connections to
    Rasdaman database.
}
\details{
    To connect to a database use a call to
    \code{\link[RRasdaman]{dbConnect}} with the returned object and
    connection-specific parameters (host, user, password, database
    name, etc).
}
\seealso{
    Rasdaman classes:

    \code{\link{RasdamanObject}}
    \code{\link{RasdamanDriver}}
    \code{\link{RasdamanConnection}}

    Methods:
    \code{\link[RRasdaman]{dbConnect}}
    \code{\link[RRasdaman]{dbDisconnect}}
}
\examples{\dontrun{
driver <- Rasdaman()
conn <- dbConnect(driver)
dbListCollections(conn)
dbDisconnect(conn)
dbUnloadDriver(driver)
}}
\keyword{database}