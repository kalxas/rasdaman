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

#include "databasehost.hh"

#include <string>
#include <memory>
#include <cstdint>

namespace rasmgr
{

/**
 * Configuration object that needs to be passed to a Server for initialization:
 * hostname, port, options, DatabaseHost.
 */
class ServerConfig
{
public:
    ServerConfig(const std::string &hostName, const std::uint32_t &port,
                 std::shared_ptr<DatabaseHost> dbHost);
    ~ServerConfig() = default;

    const std::string &getHostName() const;
    void setHostName(const std::string &value);

    std::uint32_t getPort() const;
    void setPort(const std::uint32_t &value);

    std::shared_ptr<DatabaseHost> getDbHost() const;
    void setDbHost(const std::shared_ptr<DatabaseHost> &value);

    const std::string &getOptions() const;
    void setOptions(const std::string &value);

private:
    std::string hostName;
    std::uint32_t port;
    std::shared_ptr<DatabaseHost> dbHost;
    std::string options;
};

}  // namespace rasmgr
#endif  // RASMGR_X_SRC_SERVERCONFIG_HH
