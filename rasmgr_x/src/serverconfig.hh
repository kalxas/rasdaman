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
#ifndef RASMGR_X_SRC_SERVERCONFIG_HH
#define RASMGR_X_SRC_SERVERCONFIG_HH

#include <string>

#include <boost/shared_ptr.hpp>
#include <boost/cstdint.hpp>

#include "databasehost.hh"

namespace rasmgr
{
/**
 * @brief The ServerConfig class Configuration object that needs to be passed
 * to a Server for initialization.
 */
class ServerConfig
{
public:
    ServerConfig(const std::string &hostName, const boost::int32_t &port, boost::shared_ptr<DatabaseHost> dbHost);
    virtual ~ServerConfig();

    std::string getHostName() const;
    void setHostName(const std::string &value);

    boost::int32_t getPort() const;
    void setPort(const boost::int32_t &value);

    boost::shared_ptr<DatabaseHost> getDbHost() const;
    void setDbHost(const boost::shared_ptr<DatabaseHost> &value);

    std::string getOptions() const;
    void setOptions(const std::string &value);

private:
    std::string hostName;
    boost::int32_t port;
    boost::shared_ptr<DatabaseHost> dbHost;
    std::string options;
};
}
#endif // RASMGR_X_SRC_SERVERCONFIG_HH
