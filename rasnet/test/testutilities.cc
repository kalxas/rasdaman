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

#include "testutilities.hh"
#include "../src/common/zmqutil.hh"

namespace rasnet
{
namespace test
{
std::vector<zmq::message_t *> TestUtilities::protoToComposite(const rasnet::MessageType::Types &type, const google::protobuf::Message &message)
{
    std::vector<zmq::message_t *> result;

    rasnet::MessageType messageType;
    messageType.set_type(type);

    result.push_back(TestUtilities::protoToZmq(messageType));
    result.push_back(TestUtilities::protoToZmq(message));

    return result;
}

zmq::message_t *TestUtilities::protoToZmq(const google::protobuf::Message &message)
{
    std::string messageData = message.SerializeAsString();

    zmq::message_t* result = new zmq::message_t(messageData.size());
    memcpy(result->data(), messageData.data(), messageData.size());

    return result;
}

zmq::message_t *TestUtilities::typeToZmq(const rasnet::MessageType::Types &type)
{
    rasnet::MessageType messageType;
    messageType.set_type(type);
    return TestUtilities::protoToZmq(messageType);
}

zmq::message_t *TestUtilities::stringToZmq(const std::string &str)
{
    zmq::message_t* result = new zmq::message_t(str.size());
    memcpy(result->data(), str.data(), str.size());

    return result;
}
}
}
