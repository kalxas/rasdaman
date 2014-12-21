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

#ifndef RASNET_SRC_SERVICE_SERVER_SERVERCONTROLLER_HH_
#define RASNET_SRC_SERVICE_SERVER_SERVERCONTROLLER_HH_

#include <google/protobuf/service.h>

namespace rasnet
{
/**
 * @see https://developers.google.com/protocol-buffers/docs/reference/cpp/google.protobuf.service
 */
class ServerController: public google::protobuf::RpcController
{
public:
    ServerController();

    virtual ~ServerController();

    virtual void Reset();

    virtual bool Failed() const;

    virtual std::string ErrorText() const;

    /**
     * Not implemented.
     */
    virtual void StartCancel();

    virtual void SetFailed(const std::string& reason);

    /**
     * Not implemented.
     */
    virtual bool IsCanceled() const;

    /**
     * Not implemented.
     */
    virtual void NotifyOnCancel(google::protobuf::Closure* callback);

private:
    bool failed;/*! Flag indicating whether the call has failed. */
    std::string failureReason;  /*!Flag indicating why the call has failed if it has failed. */
};

} /* namespace rnp */

#endif /* RASNET_SERVICE_SERVER_SERVERCONTROLLER_HH_ */
