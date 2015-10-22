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

#include <stdexcept>
#include "../exceptions/rasexceptions.hh"

#include "messages/error.pb.h"
#include "grpcutils.hh"

namespace common
{

std::string GrpcUtils::convertAddressToString(const std::string &host, boost::uint32_t port)
{
    return host+":"+std::to_string(port);
}


grpc::Status GrpcUtils::convertExceptionToStatus(std::exception &exception)
{
    ErrorMessage errorMessage;

    //The type is STL
    errorMessage.set_type(ErrorMessage::STL);
    errorMessage.set_error_text(exception.what());

    grpc::Status status(grpc::StatusCode::UNKNOWN, errorMessage.SerializeAsString());

    return status;
}

grpc::Status GrpcUtils::convertExceptionToStatus(const std::string &errorMessage)
{
    ErrorMessage message;

    //The type is UNKNOWN
    message.set_type(ErrorMessage::UNKNOWN);
    message.set_error_text(errorMessage);

    grpc::Status status(grpc::StatusCode::UNKNOWN, message.SerializeAsString());

    return status;
}

void GrpcUtils::convertStatusToExceptionAndThrow(const grpc::Status &status)
{
    if(status.error_code()==grpc::StatusCode::UNKNOWN)
    {
        //We might be able to handle this
        ErrorMessage message;
        if(message.ParseFromString(status.error_message()))
        {
            switch(message.type())
            {
            case ErrorMessage::STL:
            {
                throw Exception(message.error_text());
            }
            break;

            case ErrorMessage::RERROR:
            {
                throw Exception(message.error_text());
            }
            break;

            case ErrorMessage::UNKNOWN:
            {
                throw Exception(message.error_text());
            }
            break;

            default:
            {
                //Throw a generic exception.
                throw Exception(message.error_text());
            }
            }
        }
        else
        {
            //Throw a generic exception.
            throw Exception(status.error_message());
        }
    }
    //Throw an exception only if the status is invalid
    else if(!status.ok())
    {
        throw ConnectionFailedException(status.error_message());
    }
}
}
