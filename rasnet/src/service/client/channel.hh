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

#ifndef RASNET_SRC_SERVICE_CLIENT_CHANNEL_HH_
#define RASNET_SRC_SERVICE_CLIENT_CHANNEL_HH_

#include <boost/cstdint.hpp>
#include <boost/thread.hpp>
#include <boost/smart_ptr.hpp>

#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>

#include "../../../../common/src/zeromq/zmq.hh"
#include "../../sockets/common/peerstatus.hh"
#include "../../messages/communication.pb.h"
#include "../../messages/base.pb.h"

#include "channelconfig.hh"


namespace rasnet
{

class Channel: public google::protobuf::RpcChannel
{
public:
	Channel(std::string host, boost::uint32_t port);

	Channel(std::string host, boost::uint32_t port, ChannelConfig config);

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
    zmq::context_t context;/*!< ZeroMQ context that owns the socket communicating to the ClientProxy and the sockets used in the ClientProxy*/

    boost::scoped_ptr<zmq::socket_t> controlSocket;
    std::string controlPipeAddr;

    std::string messagePipeAddr;

    std::string serverAddr;
    std::string socketIdentity;

    boost::scoped_ptr<boost::thread>  proxyThread;

    boost::mutex counterMutex;
    boost::uint64_t counter;

    int linger;
    ChannelConfig config;

    AlivePing ping;
    AlivePong pong;

    void init(std::string host, boost::uint32_t port);

    void connectToServer();

    void disconnectFromServer();

    bool handleServerMessage(const base::BaseMessage& message,zmq::socket_t& replySocket);

    void listenerMethod();
};

} /* namespace rasnet */

#endif /* RASNET_SRC_SERVICE_CLIENT_CHANNEL_HH_ */
