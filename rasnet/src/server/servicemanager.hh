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
#ifndef RASNET_SRC_SERVER_SERVICEMANAGER_HH_
#define RASNET_SRC_SERVER_SERVICEMANAGER_HH_

#include <map>

#include <google/protobuf/service.h>
#include <boost/smart_ptr.hpp>
#include <boost/cstdint.hpp>
#include <boost/exception/all.hpp>
#include <boost/bind.hpp>

#include "servicemanagerconfig.hh"
#include "servicerequesthandler.hh"

namespace rasnet
{
/**
 * @brief The ServiceManager class allows Service implementations to be
 * published and served on a public endpoint.
 * Once started, the ServiceManager will serve requests on the given endpoint
 * until it is destructed.
 * This class is not thread safe.
 */
class ServiceManager
{
public:
    /**
     * @brief ServiceManager
     * @param config Configuration object used for intializing the internals
     * of the ServiceManager @see ServiceManagerConfig
     */
    ServiceManager(const ServiceManagerConfig& config);

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
     * @param endpoint The host parameters specifies the interface and the protocol on
     * which to listen for incoming connections
     * This method will block for INTER_THREAD_COMMUNICATION_TIMEOUT milliseconds
     * to perform a timed_wait on the worker thread and catch any exeption
     * throw during the binding of the communication sockets
     */
    void serve(const std::string& endpoint);
private:
    const ServiceManagerConfig config; /*!< The configuration of this service manager*/
    zmq::context_t context;/*!< ZMQ context used internally by the ServiceManager for inter-thread communication
                             and communication with the clients */

    int linger; /*!< The number of milliseconds a socket should wait to send messages
                  before terminating */
    bool runnning;/*!< Flag indicating whether the service manager is running*/

    std::string bridgeAddr;/*!< Address of the bridge used for inter-thread communication*/
    std::string controlAddr;/*!< Address of the socket used for communicating with the worker thread*/
    std::string endpointAddr;/*!<Enpoint on which this class serves requests*/

    boost::scoped_ptr<ServiceRequestHandler> requestHandler;/*!< ServiceRequestHandler that will handle incoming service calls. */
    boost::scoped_ptr<boost::thread> serviceThread;
    boost::scoped_ptr<zmq::socket_t> controlSocket;/*!< Socket through which the worker thread is controlled*/
    boost::exception_ptr workerThreadExceptionPtr;/*!< Pointer to an exception that might be thrown from the worker thread.*/

    void stopWorkerThread();
    /**
     * This function will run in a separate thread and will wait for incoming request and process them.
     * @param parent Pointer to the ServiceManager object owning the thread in which this function is executed.
     */
    void run(boost::exception_ptr& exceptionPtr);
};

} /* namespace rasnet */

#endif /* RASNET_SERVICE_SERVER_SERVICEMANAGER_HH_ */
