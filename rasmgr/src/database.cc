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

#include "database.hh"
#include "exceptions/dbbusyexception.hh"
#include "exceptions/duplicatedbsessionexception.hh"
#include "common/exceptions/logicexception.hh"
#include <logging.hh>

#include <boost/thread/shared_lock_guard.hpp>

namespace rasmgr
{
using std::set;
using std::pair;
using std::string;
using std::runtime_error;

Database::Database(const std::string &db):
    dbName(db)
{}

void Database::addClientSession(const std::string &clientId, const std::string &sessionId)
{
    boost::lock_guard<boost::shared_mutex> lock(sessionListMutex);
    
    auto insertResult = this->sessionList.emplace(clientId, sessionId);
    if (!insertResult.second)
    {
        std::string sessionUID = "<" + clientId + ", " + sessionId + ">";
        throw DuplicateDbSessionException(this->getDbName(), sessionUID);
    }
}

int Database::removeClientSession(const std::string &clientId, const std::string &sessionId)
{
    pair<string, string> toRemove(clientId, sessionId);
    
    boost::lock_guard<boost::shared_mutex> lock(sessionListMutex);
    return this->sessionList.erase(toRemove);
}

bool Database::isBusy() const
{
    boost::lock_guard<boost::shared_mutex> lock(sessionListMutex);
    return !this->sessionList.empty();
}

DatabaseProto Database::serializeToProto(const Database &db)
{
    DatabaseProto result;
    result.set_name(db.getDbName());
    
    boost::shared_lock<boost::shared_mutex> lock(db.sessionListMutex);
    for (auto it = db.sessionList.begin(); it != db.sessionList.end(); ++it)
    {
        StringPair *session = result.add_sessions();
        session->set_first(it->first);
        session->set_second(it->second);
    }

    return result;
}

const std::string &Database::getDbName() const
{
    return dbName;
}

void Database::setDbName(const std::string &value)
{
    if (isBusy())
    {
        throw DbBusyException(this->dbName);
    }
    if (value.empty())
    {
        throw common::LogicException("Cannot set database name to an empty value.");
    }
    dbName = value;
}


} /* namespace rasmgr */
