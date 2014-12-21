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
#ifndef RASNET_SRC_SERVICE_SERVER_SERVICEMANAGER_HH_
#define RASNET_SRC_SERVICE_SERVER_SERVICEMANAGER_HH_

#include <map>

#include <google/protobuf/service.h>
#include <boost/smart_ptr.hpp>
#include <boost/cstdint.hpp>

#include "../../../src/service/server/servicerequesthandler.hh"

namespace rasnet
{

class ServiceManager
{
public:
    /**
     * Create a ServiceManager by specifying the number of I/O threads
     * listening to incoming connections
     * @param ioThreads Number of I/O threads listening to incoming connections
     * and sending messages back to peers.
     * One thread should be used for each GB of data sent/received per second
     * @param cpuThreads Number of CPU threads used to process requests from clients.
     */
    ServiceManager(boost::int32_t ioThreads = 1, boost::uint32_t cpuThreads=1);

    /**
     * Stop the ServiceManager and cleanup.
     */
    virtual ~ServiceManager();

    /**
     * Add the service to the list of services provided by the ServiceManager.
     * Adding the service multiple times will throw an exception.
     * @param service Service provided by the ServiceManager
     */
    void addService(boost::shared_ptr<google::protobuf::Service> service);

    /**
     * Serve all the services added so far on the given host and port.
     * @param host The host parameters specifies the interface and the protocol on
     * which to listen for incoming connections
     * @param port The port number on which to listen for incoming connections
     */
    void serve(std::string host, boost::uint32_t port);
private:
    bool runnning;/*! Flag indicating whether the service manager is running*/
    boost::uint32_t serverPort;/*! Port on which the ServiceManager should listen for connections */
    std::string serverHost;/*! Host to which the ServiceManager will bind and listen for connections */
    boost::int32_t cpuThreads;
    boost::scoped_ptr<ServiceRequestHandler> requestHandler;/*! ServiceRequestHandler that will handle incoming service calls. */
    zmq::context_t context;
    std::string bridgeAddr;

    boost::scoped_ptr<boost::thread> serviceThread;
    /**
     * This function will run in a separate thread and will wait for incoming request and process them.
     * @param parent Pointer to the ServiceManager object owning the thread in which this function is executed.
     */
    void run();
};

} /* namespace rnp */

#endif /* RASNET_SERVICE_SERVER_SERVICEMANAGER_HH_ */
