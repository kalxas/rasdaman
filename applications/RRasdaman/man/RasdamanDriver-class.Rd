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
\name{RasdamanDriver-class}
\docType{class}
\alias{RasdamanDriver-class}
\alias{RasdamanDriver}
\title{Class RasdamanDriver}
\description{
    Rasdaman driver class implementing DBI API. It allows connections opening,
    listing of already opened connections.
}
\section{Generators}{
    To create an instance use \code{\link[DBI]{dbDriver}("Rasdaman")}
    or \code{\link{Rasdaman}()}.
}
\section{Extends}{
    Directly: \code{DBIDriver}, \code{RasdamanObject}.

    Indirectly: \code{DBIObject} by class \code{RasdamanObject}.
}
\section{Methods}{
    \describe{
    \item{\link[RRasdaman]{dbConnect}}{}
    \item{\link{dbListConnections}}{}
    \item{\link{dbUnloadDriver}}{}
    \item{\link{dbGetInfo}}{}
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
    \code{\link{RasdamanArray}}
    \code{\link{RasdamanHandle}}
}

\examples{\dontrun{
# Start session
drv <- dbDriver("Rasdaman")
con <- dbConnect(drv, user="rasguest", password="rasguest")
# ...
# select the first connection among opened ones
con1 <- dbListConnections(Rasdaman())[[1]]
# ...
# Close session
dbDisconnect(con)
dbUnloadDriver(drv)
}}
\keyword{database}
\keyword{classes}