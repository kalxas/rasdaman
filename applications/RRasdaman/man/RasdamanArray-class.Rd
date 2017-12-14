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
\name{RasdamanArray-class}
\docType{class}
\alias{RasdamanArray-class}
\alias{RasdamanArray}
\alias{dim,RasdamanArray-method}

\title{Class RasdamanArray}

\description{
Class represents rasdaman arrays. Objects of this class are returned
by \code{\link[RRasdaman]{simplify}} on \code{\link{RasdamanArrayHandle}} or
manually by using \code{RasdamanArray} function.
}

\section{Slots}{
\describe{
    \item{array}{List of N-dimensional arrays.

    If the object contains primitives, slot \code{array} has exactly one
    element, one N-dimensional array.

    If the object contains structures of M attributes, this slot
    is a named list with M elements, each element is a N-dimensional array.
    Names of the list are the same as structure attributes names (see Examples
    section).
    }
    \item{origin}{Origin point of the array represented as an integer vector.}
}
}
\seealso{
    Rasdaman classes and methods:

    \code{\link{RasdamanObject}}
    \code{\link{RasdamanResult}}
    \code{\link{RasdamanArrayHandle}}
    \code{\link[RRasdaman]{dbGetQuery}}
    \code{\link{dbFetch}}
    \code{\link[RRasdaman]{simplify}}

    \code{\link{array}}
}
\examples{
\dontrun{
> conn <- dbConnect(Rasdaman())
# Arrays of primitives
> handles <- dbGetQuery(conn, "select x[202:204,203:204].green from rgb as x")
> simplify(handles[[1]])
An object of class "RasdamanArray"
Slot "array":
[[1]]
     [,1] [,2] [,3]
[1,]   27    0   27
[2,]    0   27   27


Slot "origin":
[1] 203 202

# Arrays of structures
> handles <- dbGetQuery(conn, "select x[202:204,203:204] from rgb as x")
> arr <- simplify(handles[[1]])
> dim(arr)
[1] 2 3
> arr
An object of class "RasdamanArray"
Slot "array":
$red
     [,1] [,2] [,3]
[1,]  242  204  242
[2,]  204  242  242

$green
     [,1] [,2] [,3]
[1,]   27    0   27
[2,]    0   27   27

$blue
     [,1] [,2] [,3]
[1,]  103   88  103
[2,]   88  103  103


Slot "origin":
[1] 203 202

> dbDisconnect(conn)
}
}
