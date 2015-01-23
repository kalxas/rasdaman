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
\name{dbUnloadDriver-methods}
\docType{methods}
\alias{dbUnloadDriver}
\alias{dbUnloadDriver-methods}
\alias{dbUnloadDriver,RasdamanDriver-method}

\title{Unloading driver}

\description{
    Implementation of a function \code{\link[DBI]{dbUnloadDriver}} from database
    interface package DBI for Rasdaman.
}

\usage{
dbUnloadDriver(drv, \dots)
}
\arguments{
    \item{drv}{RasdamanDriver object}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\value{
    \code{dbUnloadDriver} returns \code{TRUE} on success and shows an error
    message if there are not closed connections.
}
\seealso{
    \code{\link{Rasdaman}} \code{\link{dbConnect}} \code{\link{RasdamanDriver}}
    \code{\link{RasdamanConnection}}
}
\examples{
\dontrun{
> drv <- Rasdaman()
> conn <- dbConnect(drv)
> dbDisconnect(conn)
[1] TRUE
> dbUnloadDriver(drv)
[1] TRUE
}
}