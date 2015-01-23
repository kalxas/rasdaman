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
\name{dbFetch}
\docType{methods}
\alias{dbFetch-methods}
\alias{dbFetch,RasdamanResult-method}
\alias{dbClearResult,RasdamanResult-method}
\alias{dbHasCompleted,RasdamanResult-method}
\alias{dbGetRowCount,RasdamanResult-method}
\alias{dbGetStatement,RasdamanResult-method}
\alias{dbGetStatement}
\alias{dbGetRowCount}
\alias{dbHasCompleted}
\alias{dbClearResult}
\alias{dbFetch}

\title{Methods of RasdamanResult}

\description{These are methods allowing work with RasdamanResult objects.}

\usage{
dbFetch(res, n = -1, \dots)
dbClearResult(res, \dots)
dbGetStatement(res, \dots)
dbHasCompleted(res, \dots)
dbGetRowCount(res, \dots)
}
\details{
    \describe{
    \item{dbFetch}{Fetches at most n rows of result and returns list of
        \code{\link{RasdamanHandle}}.}
    \item{dbClearResult}{Clears state of RasdamanResult and releases internal
        data structures.}
    \item{dbGetStatement}{Returns character representation of the query, resulted
        in the current instance of RasdamanResult.}
    \item{dbHasCompleted}{Returns FALSE if there are rows to be fetched and
        TRUE otherwise.}
    \item{dbGetRowCount}{Returns number of already fetched rows.}
    }
}
\arguments{
    \item{res}{RasdamanResult object}
    \item{n}{maximum number of results to be fetched, or -1 to fetch all results}
    \item{...}{any additional arguments to be passed to the dispatched methods}
}
\seealso{
    Rasdaman classes:

    \code{\link{RasdamanDriver}}
    \code{\link{RasdamanConnection}}
    \code{\link{RasdamanResult}}
    \code{\link{RasdamanHandle}}
    \code{\link{RasdamanArrayHandle}}
}
\examples{
\dontrun{
> conn <- dbConnect(Rasdaman())
> res <- dbSendQuery(conn, "select avg_cell(x.green) from rgb as x")
> summary(res)
[1] "RasDriver{ total 6 elements, 0 emitted }"
RasdamanResult
> handles <- dbFetch(res, 2)
> dbGetRowCount(res)
[1] 2
> dbHasCompleted(res)
[1] FALSE
> dbGetStatement(res)
[1] "select avg_cell(x.green) from rgb as x"
> handles <- dbFetch(res)
> dbHasCompleted(res)
[1] TRUE
> dbClearResult(res)
> dbDisconnect(conn)
}
}