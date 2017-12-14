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
\name{RasdamanResult-class}
\docType{class}
\alias{RasdamanResult-class}
\alias{RasdamanResult}

\title{Class RasdamanResult}
\description{RasdamanResult class which represents a
    set of retrieved values. Retrieved values could be accessed by method
    \code{\link[RRasdaman]{dbFetch}} and returned as objects of type
    \code{\link[RRasdaman]{RasdamanHandle}}.
}
\section{Note}{
    It is desirable to clear \code{RasdamanResult} objects with
    \code{\link{dbClearResult}} to release used resources.
}
\section{Generators}{
    Objects of this class are returned by function
    \code{\link[RRasdaman]{dbSendQuery}}.
}
\section{Extends}{
    Directly: \code{RasdamanObject}.
}
\section{Methods}{
    \describe{
    \item{\link[RRasdaman]{dbFetch}}{}
    \item{\link[RRasdaman]{dbClearResult}}{}
    \item{\link[RRasdaman]{dbHasCompleted}}{}
    \item{\link[RRasdaman]{dbGetStatement}}{}
    \item{\link[RRasdaman]{dbGetRowCount}}{}
    \item{\link[RRasdaman]{dbGetInfo}}{}
    \item{\link{summary}}{}
    }
}
\seealso{
    Rasdaman classes:

    \code{\link{RasdamanObject}}
    \code{\link{RasdamanDriver}}
    \code{\link{RasdamanConnection}}
    \code{\link{RasdamanResult}}
    \code{\link{RasdamanHandle}}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman())
res <- dbSendQuery(conn, "select sdom(x) from rgb as x")
handles <- dbFetch(res)
print(paste("Number of results:", length(handles)))
dbDisconnect(conn)
}
}