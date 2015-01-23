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
\name{simplify-methods}
\docType{methods}
\alias{simplify}
\alias{simplify-methods}
\alias{simplify,RasdamanHandle-method}

\title{Converting RasdamanHandle into R values}

\description{Converting RasdamanHandle values, retrieved by \code{dbGetQuery} or
    other retrieval functions, into R values.
}
\section{Conversion rules}{
    \describe{
        \item{Primitives}{Primitives are converted into corresponing values of
        primitive types in R, such as \code{integer}, \code{numeric}.}

        \item{Points}{Rasdaman points are converted into integer vectors.}

        \item{One-dimensional intervals}{Simple intervals, such as \code{0:7},
        are converted into named lists with members \code{low} and \code{high}.
        For example: \code{list(low=0, high=7)}.}

        \item{Multidimensional intervals}{Multidimensional intervals, such as
        \code{[0:7,1:5]} are converted into lists of one-dimensional intervals.}

        \item{Structures}{Structures are converted into named lists with members
        names corresponding to attributes names. For example, structure
        \code{{red = 10, green=255, blue=128}} is converted into
        \code{list(red=10, green=255, blue=128)}.}

        \item{Arrays}{Arrays are converted into \code{\link{RasdamanArray}}s}
    }
}
\usage{
simplify(handle, \dots)
}
\arguments{
    \item{handle}{RasdamanHandle, which value is needed to be converted}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\seealso{
    \code{\link[RRasdaman]{dbGetQuery}} \code{\link[RRasdaman]{dbSendQuery}}
    \code{\link{dbFetch}} \code{\link[RRasdaman]{getCell}}

    \code{\link{RasdamanHandle}} \code{\link{RasdamanArrayHandle}}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman())
handles <- dbGetQuery(conn, "select sdom(x) from rgb as x")
domain <- simplify(handles[[1]])
handles <- dbGetQuery(conn, "select x[0:10,0:10].green from rgb as x")
array <- simplify(handles[[1]])
dbDisconnect(conn)
}}
