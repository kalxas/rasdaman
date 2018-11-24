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

#include "healthserviceimpl.hh"

namespace common
{
using std::pair;
using std::map;

HealthServiceImpl::HealthServiceImpl() {}

HealthServiceImpl::~HealthServiceImpl() {}

void HealthServiceImpl::setStatus(const std::string &service,
                                  const HealthCheckResponse::ServingStatus &status) {
    boost::unique_lock<boost::mutex> lock(this->mutex);
    this->statuses[service] = status;
}

void HealthServiceImpl::clearStatus(const std::string &service) {
    boost::unique_lock<boost::mutex> lock(this->mutex);
    this->statuses.erase(service);
}

void HealthServiceImpl::clearAll() {
    boost::unique_lock<boost::mutex> lock(this->mutex);
    this->statuses.clear();
}

grpc::Status HealthServiceImpl::Check(__attribute__ ((unused)) grpc::ServerContext *context,
                                      const HealthCheckRequest *request,
                                      HealthCheckResponse *response) {
    boost::unique_lock<boost::mutex> lock(this->mutex);

    // If the service is empty we assume that the client wants to check the server's status.
    if (request->service().empty()) {
        response->set_status(HealthCheckResponse::SERVING);
    } else if (this->statuses.find(request->service()) != this->statuses.end()) {
        response->set_status(this->statuses[request->service()]);
    } else {
        response->set_status(HealthCheckResponse::UNKNOWN);
    }

    return grpc::Status::OK;
}
}
