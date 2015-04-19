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

#ifndef RASNET_SRC_CLIENT_CHANNEL_HH
#define RASNET_SRC_CLIENT_CHANNEL_HH

#include <string>
#include <deque>
#include <map>

#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>

#include <boost/cstdint.hpp>
#include <boost/thread.hpp>
#include <boost/smart_ptr.hpp>

#include "../../../common/src/zeromq/zmq.hh"

#include "../messages/communication.pb.h"
#include "../common/peerstatus.hh"

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

#include "channelconfig.hh"

namespace rasnet
{
class Channel : public google::protobuf::RpcChannel
{
public:
    /**
     * @brief Channel Initialize an instance of the Channel class.
     * @param serverAddress The server endpoint the channel to connect
     * @param config Configuration object that sets the properties of the channel
     */
    Channel(const std::string& serverAddress, const ChannelConfig& config);

    virtual ~Channel();

    /**
     * Forwards a service call request to the connected service provider and waits for the reply.
     * @param method MethodDescriptor representing the requested method.
     * @param controller Controller object used to represent errors or to control the service call.
     * @param request Protobuf message representing the input parameter of the method identified by the method descriptor
     * @param response Protobuf message representing the output parameter of the method identified by the method descriptor
     * @param done Closure that will be called before exiting the method.
     */
    virtual void CallMethod(const  google::protobuf::MethodDescriptor * method,
                            google::protobuf::RpcController * controller, const google::protobuf::Message * request,
                            google::protobuf::Message * response,  google::protobuf::Closure * done);
private:
    std::string serverAddress;/*!< The address of the server*/
    const ChannelConfig config; /*!< Channel configuration*/

    int linger;/*!< Number of milliseconds a socket should wait for pending messages before closing. 0 */
    //bool running;/*!< True if the connection to the server was made*/

    /**
     * @brief serviceRequests <CallId, <InputData, MethodName> >
     * Map of pending service requests.
     */
    boost::shared_ptr<std::map<std::string, std::pair<const google::protobuf::Message*, std::string> > > serviceRequests;

    /**
     * @brief serviceResponses <CallId, <Success, OutputData> >
     * A service response is identified by the call id
     * Success: TRUE if the call was successful, FALSE if an error was encountered
     * OutputData: String ErrorMessage if the call was unsuccessful, serialized Output Message otherwise
     */
    boost::shared_ptr<std::map<std::string, std::pair<bool,boost::shared_ptr<zmq::message_t> > > > serviceResponses;

    boost::shared_ptr<boost::mutex> serviceMutex; /*!< Mutex used to synchronize ccess to the serviceRequests and serviceResponses*/

    zmq::context_t context; /*!< ZMQ context for all the sockets in the channel*/
    std::string channelIdentity;/*!< The UID of the Channel */
    std::string controlAddress;/*!< The address of the control socket*/
    std::string bridgeAddress;/*!< The address of the bridge socket*/
    boost::scoped_ptr<zmq::socket_t> controlSocket;/*!< */

    boost::scoped_ptr<boost::thread> workerThread;/*!< Handle to the thread that handles communication*/

    /**
     * @brief connectToServer Send a message from the main thread to the worker thread
     * to initialize the connection to the server
     */
    void connectToServer();

    /**
     * @brief disconnectFromServer
     */
    void disconnectFromServer();

    /**
     * @brief tryConnectToServer Wait for an InternalConnectRequest,
     * send a ConnectRequest to the server and wait for a reply.
     * @param controlSocket
     * @param serverSocket
     * @param out_timeout The timeout that is used for the polling
     * @return TRUE if the connection was successdul, FALSE otherwise
     */
    bool tryConnectToServer(zmq::socket_t& controlSocket, zmq::socket_t& serverSocket,boost::shared_ptr<PeerStatus>& out_serverStatus, boost::int32_t& out_timeout);

    /**
     * @brief workerMethod
     */
    void workerMethod();

    boost::uint64_t callCounter;
    boost::mutex callCounterMutex;

};
}

#endif // RASNET_SRC_CLIENT_CHANNEL_HH
