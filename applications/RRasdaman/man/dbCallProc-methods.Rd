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
\name{dbCallProc-methods}
\alias{dbCallProc,RasdamanConnection-method}
\alias{dbColumnInfo,RasdamanResult-method}
\alias{dbGetException,RasdamanConnection-method}
\alias{dbListFields,RasdamanConnection-method}
\alias{dbGetRowsAffected,RasdamanResult-method}
\alias{dbReadTable,RasdamanConnection-method}
\alias{dbWriteTable,RasdamanConnection-method}
\alias{fetch,RasdamanResult-method}

\title{Methods unimplemented in RRasdaman}

\description{These methods are unimplemented or are not used in RRasdaman
package}
\section{Reasons}{
\describe{
    \item{dbCallProc}{There are no stored procedures in Rasdaman.}
    \item{dbColumnInfo}{There is no columns in Rasdaman.}
    \item{dbGetException}{All exceptions are thrown immediately.}
    \item{dbListFields}{There is no columns, no fields in Rasdaman.}
    \item{dbGetRowsAffected}{The underlying client library doesn't allow to know
        how many arrays were affected during query execution.}
    \item{dbReadTable}{\code{\link{dbReadCollection}} should be used instead.}
    \item{dbWriteTable}{\code{\link{dbInsertCollection}} and
        \code{\link{dbUpdateCollection}} should be used instead.}
    \item{fetch}{\code{\link{dbFetch}} should be used instead.}
}
}
