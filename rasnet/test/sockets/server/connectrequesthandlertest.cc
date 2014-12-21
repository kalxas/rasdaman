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

#include <boost/cstdint.hpp>

#include "../../../../common/src/unittest/gtest.h"

#include "../../../src/util/proto/protozmq.hh"
#include "../../../src/sockets/server/connectrequesthandler.hh"
#include "../../../src/messages/communication.pb.h"

using namespace std;
using namespace base;
using namespace rasnet;


TEST(ConnectRequestHandler, Constructor)
{
    boost::shared_ptr<ClientPool> clienPool(new ClientPool());
    boost::int32_t retries = 1;
    boost::int32_t period = 10;
    ConnectRequestHandler* handler;

    ASSERT_NO_THROW(handler = new ConnectRequestHandler(clienPool, retries, period));

    ASSERT_NO_THROW(delete handler);
}

TEST(ConnectRequestHandler, canHandle)
{
    boost::shared_ptr<ClientPool> clienPool(new ClientPool());
    int32_t retries = 3;
    int32_t period = 3000;
    ExternalConnectRequest req;
    req.set_period(period);
    req.set_retries(retries);

    BaseMessage envelope;
    envelope.set_data(req.SerializeAsString());
    envelope.set_type(req.GetTypeName());

    ConnectRequestHandler handler(clienPool, retries, period);

    ASSERT_TRUE(handler.canHandle(envelope));
    envelope.Clear();
    ASSERT_FALSE(handler.canHandle(envelope));
}


TEST(ConnectRequestHandler, handle)
{
    boost::shared_ptr<ClientPool> clienPool(new ClientPool());
    int32_t retries = 3;
    int32_t period = 3000;
    int32_t c_period = 2000;
    int32_t c_retries = 2;
    string id;
    BaseMessage envelope;
    ConnectRequestHandler handler(clienPool, retries, period);

    string identity = "my_id";
    const char* addr = "inproc://proto_test";
    zmq::context_t context;
    zmq::socket_t server(context, ZMQ_ROUTER);
    zmq::socket_t client(context, ZMQ_DEALER);
    client.setsockopt(ZMQ_IDENTITY, identity.c_str(), strlen(identity.c_str()));
    server.bind(addr);
    client.connect(addr);

    ExternalConnectRequest req;
    req.set_period(c_period);
    req.set_retries(c_retries);
    ProtoZmq::zmqSend(client, req);

    ProtoZmq::zmqReceiveFromPeer(server, id, envelope);

    EXPECT_NO_THROW(handler.handle(envelope, id, server));
    EXPECT_TRUE(clienPool->isClientAlive(id));

    envelope.Clear();
    EXPECT_ANY_THROW(handler.handle(envelope, id, server));
}
