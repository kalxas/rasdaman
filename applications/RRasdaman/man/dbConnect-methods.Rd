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
\name{dbConnect-methods}
\docType{methods}
\alias{dbConnect}
\alias{dbDisconnect}
\alias{dbConnect-methods}
\alias{dbDisconnect-methods}
\alias{dbConnect,RasdamanDriver-method}
\alias{dbDisconnect,RasdamanConnection-method}
\alias{CONN_READ_ONLY}
\alias{CONN_READ_WRITE}

\title{Connecting to Rasdaman}

\description{
These methods allow to connect and disconnect to and from Rasdaman DBMS.
}

\usage{
\S4method{dbConnect}{RasdamanDriver}(drv, host = "localhost", port = 7001,
          dbName = "RASBASE", user = "rasguest",
          password = "rasguest", mode = CONN_READ_ONLY)
\S4method{dbDisconnect}{RasdamanConnection}(conn, \dots)
}
\arguments{
    \item{drv}{\link{RasdamanDriver} object as returned from
        \code{\link[DBI]{dbDriver}} or \code{\link{Rasdaman}}}
    \item{host}{rasdaman server hostname}
    \item{port}{rasmgr port number}
    \item{dbName}{name of database}
    \item{user}{name of user}
    \item{password}{user password}
    \item{mode}{mode of creating connection, either CONN_READ_ONLY or
        CONN_READ_WRITE}
    \item{...}{any additional arguments to be passed to the dispatched methods}
    \item{conn}{\link{RasdamanConnection} object as returned from
        \code{\link[RRasdaman]{dbConnect}}}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman(), user="rasadmin", password="rasadmin", mode=CONN_READ_WRITE)
dbDisconnect(conn)
}}
\seealso{
    DBI classes and methods:
    \code{\link[DBI]{DBIDriver-class}}
    \code{\link[DBI]{DBIConnection-class}}
    \code{\link[DBI]{dbDriver}}
    \code{\link[DBI]{dbConnect}}
    \code{\link[DBI]{dbDisconnect}}

    Rasdaman classes:
    \code{\link{RasdamanDriver}}
    \code{\link{RasdamanConnection}}
    \code{\link{RasdamanResult}}
    \code{\link{RasdamanHandle}}
}
