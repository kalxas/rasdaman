/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
// This is -*- C++ -*-

#include "oidif.hh"
#include "adminif.hh"
#include "sqlitewrapper.hh"
#include "raslib/error.hh"
#include <logging.hh>
#include <fmt/core.h>

void OId::initialize()
{
    loadedOk = false;
    {
        SQLiteQuery query("SELECT NextValue FROM RAS_COUNTERS ORDER BY CounterId");
        unsigned int i = 1;
        NNLTRACE << "Counters: ";
        while (query.nextRow() && i < OId::maxCounter)
        {
            *counterIds[i] = query.nextColumnLong();
            if (i != 1)
            {
                BLTRACE << ", ";
            }
            BLTRACE << counterNames[i] << " = " << *counterIds[i];
            ++i;
        }
        BLTRACE << "\n";
        if (i == OId::maxCounter)
        {
            loadedOk = true;
        }
        else
        {
            LERROR << "Expected " << OId::maxCounter << " rows in RAS_COUNTERS, but found only " << i << ". "
                    << "Most likely you need to stop rasdaman, run update_db.sh, and start rasdaman again.";
            throw r_Error(DATABASE_INCONSISTENT);
        }
    }
}

void OId::deinitialize()
{
    if (loadedOk && !AdminIf::isAborted() && !AdminIf::isReadOnlyTA())
    {
        for (unsigned int i = 1; i < OId::maxCounter; i++)
        {
            SQLiteQuery::execute(fmt::format(
                "UPDATE RAS_COUNTERS SET NextValue = {} WHERE CounterName = '{}'",
                *counterIds[i], counterNames[i]));
        }
    }
    loadedOk = false;
}
