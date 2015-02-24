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

#ifndef RASNET_SRC_UTIL_ZMQUTIL_HH_
#define RASNET_SRC_UTIL_ZMQUTIL_HH_

#include <google/protobuf/message.h>
#include <boost/algorithm/string.hpp>

namespace rasnet
{

class ZmqUtil
{
public:
    static const std::string ALL_LOCAL_INTERFACES;

    static void freeByteArray(void *data, void *hint)
    {
        delete[] ((::google::protobuf::uint8 *) data);
    }

    static std::string toTcpAddress(std::string address)
    {
        return addPrefixIfMissing(address, ZmqUtil::TCP_PREFIX);
    }

    static std::string toInprocAddress(std::string address)
    {
        return addPrefixIfMissing(address, ZmqUtil::INPROC_PREFIX);
    }

private:
    static const std::string TCP_PREFIX;
    static const std::string INPROC_PREFIX;

    static std::string addPrefixIfMissing(std::string str, std::string prefix)
    {
        if (!boost::algorithm::starts_with(str, prefix))
        {
            str = prefix + str;
        }

        return str;
    }
};

} /* namespace rasnet */

#endif /* COMMON_SRC_ZEROMQ_ZMQUTIL_HH_ */
