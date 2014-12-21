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

#include <string>
#include "../../../../common/src/unittest/gtest.h"
#include "../../../src/util/proto/protozmq.hh"
#include "../../../src/sockets/server/serverpinghandler.hh"
#include "../../../src/messages/communication.pb.h"

using namespace base;
using namespace std;
using namespace rasnet;

TEST(ServerPingHandler, Constructor)
{
    ServerPingHandler* handler;
    EXPECT_NO_THROW(handler =  new ServerPingHandler());
    delete handler;
}


TEST(ServerPingHandler, canHandle)
{
    ServerPingHandler handler;
    AlivePing ping;
    BaseMessage envelope;
    envelope.set_data(ping.SerializeAsString());
    envelope.set_type(ping.GetTypeName());
    EXPECT_TRUE(handler.canHandle(envelope));

    envelope.Clear();
    EXPECT_FALSE(handler.canHandle(envelope));

}

TEST(ServerPingHandler, handle)
{
    ServerPingHandler handler;
    string identity = "my_id";
    const char* addr = "inproc://proto_test";
    zmq::context_t context;
    zmq::socket_t server(context, ZMQ_ROUTER);
    zmq::socket_t client(context, ZMQ_DEALER);
    client.setsockopt(ZMQ_IDENTITY, identity.c_str(), strlen(identity.c_str()));
    server.bind(addr);
    client.connect(addr);
    AlivePing ping;
    BaseMessage envelope;
    string id;
    ProtoZmq::zmqSend(client, ping);
    ProtoZmq::zmqReceiveFromPeer(server, id, envelope);

    EXPECT_NO_THROW(handler.handle(envelope, id, server));
    BaseMessage response;
    ProtoZmq::zmqReceive(client, response);
    EXPECT_EQ(AlivePong::default_instance().GetTypeName(), response.type());

    BaseMessage phony;
    EXPECT_ANY_THROW(handler.handle(phony, id, server));

}
