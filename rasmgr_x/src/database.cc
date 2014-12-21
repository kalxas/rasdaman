/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <stdexcept>

#include "../../common/src/logging/easylogging++.hh"

#include "database.hh"

namespace rasmgr
{
using std::set;
using std::pair;
using std::string;
using std::runtime_error;

Database::Database(const std::string& dbName):dbName(dbName)
{}

Database::~Database()
{}

const std::string& Database::getDbName() const
{
    return this->dbName;
}

void Database::setDbName(const std::string& dbName)
{
    this->dbName = dbName;
}

void Database::increaseSessionCount(const std::string& clientId, const std::string& sessionId)
{
    pair<set<pair<string,string> >::iterator,bool> insertResult = this->sessionList.insert(std::make_pair(clientId,sessionId));
    if(!insertResult.second)
    {
        throw runtime_error("Database already contains session:<"+clientId+", "+ sessionId+">");
    }
}

int Database::decreaseSessionCount(const std::string& clientId, const std::string& sessionId)
{
    pair<string,string> toRemove(clientId, sessionId);
    return this->sessionList.erase(toRemove);
}

void Database::clearSessionCount()
{
    this->sessionList.clear();
}

int Database::getSessionCount()
{
    return this->sessionList.size();
}

bool Database::isBusy() const
{
    return !this->sessionList.empty();
}

} /* namespace rasmgr */
