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
 \name{RasdamanConnection-class}
 \docType{class}
 \alias{RasdamanConnection-class}
 \alias{RasdamanConnection}
 \title{Class RasdamanConnection}
 \description{RasdamanConnection class implementing DBI API. Represents a
 connection to Rasdaman database, allows transaction management (commit and
 rollback operations), reading and writing data.
 }
 \section{Generators}{
    To create this class object use \code{\link{dbConnect}}.
 }
 \section{Extends}{
    Directly: \code{DBIConnection}, \code{RasdamanObject}.

    Indirectly: \code{DBIObject} by class \code{RasdamanObject}.
 }
 \section{Methods}{
    \describe{
    \item{\link[RRasdaman]{dbDisconnect}}{}
    \item{\link[RRasdaman]{dbCommit}}{}
    \item{\link[RRasdaman]{dbRollback}}{}
    \item{\link[RRasdaman]{dbExistsCollection}}{}
    \item{\link[RRasdaman]{dbListCollections}}{}
    \item{\link[RRasdaman]{dbSendQuery}}{}
    \item{\link[RRasdaman]{dbGetQuery}}{}
    \item{\link[RRasdaman]{dbListResults}}{}
    \item{\link[RRasdaman]{dbReadCollection}}{}
    \item{\link[RRasdaman]{dbInsertCollection}}{}
    \item{\link[RRasdaman]{dbUpdateCollection}}{}
    \item{\link[RRasdaman]{dbRemoveCollection}}{}
    \item{\link[RRasdaman]{dbGetInfo}}{}
    \item{\link{summary}}{}
    }
 }
\seealso{
    DBI classes:

    \code{\link[DBI]{DBIObject-class}}
    \code{\link[DBI]{DBIDriver-class}}
    \code{\link[DBI]{DBIConnection-class}}
    \code{\link[DBI]{DBIResult-class}}

    Rasdaman classes:

    \code{\link{RasdamanObject}}
    \code{\link{RasdamanDriver}}
    \code{\link{RasdamanConnection}}
    \code{\link{RasdamanResult}}
    \code{\link{RasdamanHandle}}
}
\examples{\dontrun{
# Create connection
drv <- Rasdaman()
con <- dbConnect(drv, user="rasguest", password="rasguest")

# Retrieve data
results <- dbGetQuery(conn, "select x.green from rgb as x")
dbListCollections(con)

# Close connection
dbDisconnect(con)
dbUnloadDriver(drv)
}}

\keyword{database}
\keyword{classes}