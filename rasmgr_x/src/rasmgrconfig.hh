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

#ifndef RASMGR_X_SRC_RASMGRCONFIG_HH_
#define RASMGR_X_SRC_RASMGRCONFIG_HH_

#include <boost/cstdint.hpp>
#include <boost/thread/mutex.hpp>

namespace rasmgr
{

class RasMgrConfig
{
public:
    virtual ~RasMgrConfig();

    /**
     * Get the RasMgrInstance.
     * @return Unique instance of the RasMgr class.
     */
    static boost::shared_ptr<RasMgrConfig> getInstance();

    /**
     * @return the maximum number of milliseconds between two required
     * pings from the client before it is removed from RasMgr.
     */
    boost::int32_t getClientLifeTime();

    void setClientLifeTime(boost::int32_t value);

    /**
     * @return the number of milliseconds between each successive pass
     * of the garbage collection thread that removes dead clients
     * from RasMgr's memory.
     */
    boost::int32_t getClientManagementGarbageCollectionInterval();

    void setClientManagementGarbageCollectionInterval(boost::int32_t value);

    /**
     * Get the number of milliseconds between two successive runs of the
     * garbage collection thread.
     * @return
     */
    boost::int32_t getServerManagementGarbageCollectionInterval();

    /**
     * Get the number of milliseconds that RasMgr will wait for a RasServer
     * to start before removing its entry from the starting servers
     * @return
     */
    boost::int32_t getRasServerTimeout();

    void setRasServerTimeout(boost::int32_t value);

    /**
     * Get the port on which this RasMgr instance is running
     * @return
     */
    boost::int32_t getRasMgrPort();

    //TODO-AT: Merge RasmgrConfig with Configuration.
    void setRasMgrPort(boost::int32_t value);

    /**
     * Get the host on which RasMgr resides
     * @return
     */
    std::string getConnectHostName();

    /**
     * The path to the RasServer executable
     * @return
     */
    std::string getRasServerExecPath();

    /**
     * Get the maximum number of clients for each RasServer
     * @return
     */
    boost::uint32_t getMaximumNumberOfClientsPerServer();

    boost::uint32_t getClientGetServerRetryNo();

    boost::int32_t getClientGetServerRetryTimeout();

private:
    boost::uint32_t maximumNumberOfClientsPerServer;
    boost::int32_t clientLifeTime;
    boost::int32_t clientManagementGarbageCollectionInterval;
    boost::int32_t serverManagementGarbageCollectionInterval;
    boost::int32_t rasServerTimeout;
    boost::int32_t rasMgrPort;
    boost::uint32_t clientGetServerRetryNo;
    boost::int32_t clientGetServerRetryTimeout;

    std::string connectHostName;
    std::string rasServerExecPath;
    std::string rasServerExecName;

    static boost::mutex instanceMutex;
    static boost::shared_ptr<RasMgrConfig> instance;

    RasMgrConfig();
};

} /* namespace rasmgr */

#endif /* RASMGRCONFIG_HH_ */
