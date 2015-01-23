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
\name{dbCommit-methods}
\alias{dbCommit-methods}
\alias{dbRollback-methods}
\alias{dbCommit}
\alias{dbRollback}
\alias{dbCommit,RasdamanConnection-method}
\alias{dbRollback,RasdamanConnection-method}

\title{Transaction management with RRasdaman}

\description{Commits or rolls back the current transaction in a Rasdaman
    connection
}
\usage{
    dbCommit(conn, \dots)
    dbRollback(conn, \dots)
}
\arguments{
    \item{conn}{RasdamanConnection object}
    \item{\dots}{any additional arguments to be passed to the dispatched methods}
}
\examples{\dontrun{
conn <- dbConnect(Rasdaman())
dbRemoveCollection(conn, "rgb")
dbRollback(conn)
}
}