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

#ifndef RASMGR_X_SRC_SERVERGROUPCONFIG_HH
#define RASMGR_X_SRC_SERVERGROUPCONFIG_HH

#include <string>
#include <set>

#include <boost/cstdint.hpp>

namespace rasmgr
{

class ServerGroupConfig
{
public:
    ServerGroupConfig(const std::string& groupName,boost::uint32_t groupSize, const std::string& host, std::set<boost::int32_t> ports, const std::string& dbHost);

    std::string getGroupName() const;
    void setGroupName(const std::string &value);

    boost::uint32_t getGroupSize() const;
    void setGroupSize(const boost::uint32_t &value);

    boost::uint32_t getMinAliveServers() const;
    void setMinAliveServers(const boost::uint32_t &value);

    boost::uint32_t getMinAvailableServers() const;
    void setMinAvailableServers(const boost::uint32_t &value);

    boost::uint32_t getMaxIdleServersNo() const;
    void setMaxIdleServersNo(const boost::uint32_t &value);

    std::string getHost() const;
    void setHost(const std::string &value);

    std::set<boost::int32_t> getPorts() const;
    void setPorts(const std::set<boost::int32_t> &value);

    std::string getDbHost() const;
    void setDbHost(const std::string &value);

    bool getAutorestart() const;
    void setAutorestart(bool value);

    boost::uint32_t getCountdown() const;
    void setCountdown(const boost::uint32_t &value);

    std::string getServerOptions() const;
    void setServerOptions(const std::string &value);

private:
    std::string groupName; /*!< Name of the server group */
    boost::uint32_t groupSize; /*!< The number of servers in this group*/
    boost::uint32_t maxIdleServersNo;/*!< The maximum number of idle servers that this server group can have*/
    boost::uint32_t minAliveServers; /*!< The minimum number of servers that this group should have alive*/
    boost::uint32_t minAvailableServers; /*!< The minimum number of servers with leftover capacity*/
    std::string host; /*!< The host on which servers of this group will run */
    std::set<boost::int32_t> ports; /*!< The set of ports on which servers from this group will run*/
    std::string dbHost; /*!< The database host that will be used by servers of this group */

    bool autorestart; /*!< TRUE if the server should be restarted if it dies. */
    boost::uint32_t countdown; /*!< Restart the server after this number of transactions*/
    std::string serverOptions; /*!< Options to be passed to the server process on startup */
};
}

#endif // SERVERGROUPCONFIG_HH
