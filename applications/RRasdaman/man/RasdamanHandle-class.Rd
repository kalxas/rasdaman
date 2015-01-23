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
\name{RasdamanHandle-class}
\docType{class}
\alias{RasdamanHandle-class}
\alias{RasdamanHandle}
\alias{RasdamanArrayHandle-class}
\alias{RasdamanArrayHandle}

\title{Class RasdamanHandle}

\description{Objects of classes RasdamanHandle and RasdamanArrayHandle represent
handles to the retrieved results and allow converting into R types
only by explicit request.
}

\section{Generators}{
Objects of these types are returned by \code{\link[RRasdaman]{dbGetQuery}} and
\code{\link{dbFetch}}.
}

\section{Extends}{
    \code{RasdamanHandle}: \code{RasdamanObject}.

    \code{RasdamanArrayHandle}: \code{RasdamanHandle}.
 }
\section{Methods}{
    \describe{
    \item{\link[RRasdaman]{simplify}}{}
    \item{\link[RRasdaman]{getCell}}{}
    \item{\link[RRasdaman]{dbGetInfo}}{}
    \item{\link{summary}}{}
    }
}
\seealso{
    Rasdaman classes and methods:

    \code{\link{RasdamanObject}}
    \code{\link{RasdamanDriver}}
    \code{\link{RasdamanConnection}}
    \code{\link{RasdamanResult}}
    \code{\link[RRasdaman]{dbGetQuery}}
    \code{\link{dbFetch}}
    \code{\link[RRasdaman]{simplify}}
}
\examples{
\dontrun{
conn <- dbConnect(Rasdaman())
handles <- dbGetQuery(conn, "select sdom(x) from rgb as x")
domain <- simplify(handles[[1]])
dbDisconnect(conn)
}
}