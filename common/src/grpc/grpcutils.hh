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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef COMMON_SRC_GRPC_GRPCUTILS_HH
#define COMMON_SRC_GRPC_GRPCUTILS_HH

#include <string>

#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>

#include <grpc++/grpc++.h>

#include "messages/healthservice.grpc.pb.h"

namespace common
{

/**
 * @brief The GrpcUtils class Functions that can be used to easily setup GRPC connections.
 */
class GrpcUtils
{
public:
    /**
     * @brief AddressToString Convert an address represented by a host and a port to a string.
     * @param host
     * @param port
     * @return String of the format host:port
     */
    static std::string constructAddressString(const std::string& host, boost::uint32_t port);

    /**
     * @brief convertExceptionToStatus Convert a given a std::exception object or one of its subclasses to a grpc::Status
     * @param exception
     * @return grpc::Status object with the error code UNKNOWN and the error_message represented
     * by the serialized exception information.
     */
    static grpc::Status convertExceptionToStatus(std::exception& exception);

    /**
     * @brief convertExceptionToStatus
     * @param errorMessage Message that will be set as the error_message of the created status object.
     * @return grpc::Status object with the error code UNKNOWN.
     */
    static grpc::Status convertExceptionToStatus(const std::string& errorMessage);

    /**
     * @brief convertStatusToExceptionAndThrow Create the appropriate type of exception that is represented by the given status message
     * and throw it. If status.ok() returns true, no exception will be thrown.
     * @param status The error_message must contained a string representation of an ErrorMessage.F
     */
    static void convertStatusToExceptionAndThrow(const grpc::Status& status);


    /**
     * @brief isServerAlive Utility function used to check if the server is alive.
     * @param healthService Initialized HealthService::Stub connected to the server.
     * @param timeoutMilliseconds The number of milliseconds for which the client will wait for
     * a reply from the server before declaring it unresponsive and returning false.
     * @return TRUE if the server responds with a valid message within the given timeout, FALSE otherwise.
     */
    static bool isServerAlive(const boost::shared_ptr<HealthService::Stub>& healthService, boost::uint32_t timeoutMilliseconds);
};

}


#endif // GRPCUTILS_HH
