#include "healthserviceimpl.hh"

namespace common
{
using std::pair;
using std::map;

HealthServiceImpl::HealthServiceImpl()
{}

HealthServiceImpl::~HealthServiceImpl()
{}

void HealthServiceImpl::setStatus(const std::string& service, const HealthCheckResponse::ServingStatus& status)
{
    boost::unique_lock<boost::mutex> lock(this->mutex);
    this->statuses[service] = status;
}

void HealthServiceImpl::clearStatus(const std::string& service)
{
    boost::unique_lock<boost::mutex> lock(this->mutex);
    this->statuses.erase(service);
}

void HealthServiceImpl::clearAll()
{
    boost::unique_lock<boost::mutex> lock(this->mutex);
    this->statuses.clear();
}

grpc::Status HealthServiceImpl::Check(__attribute__ ((unused)) grpc::ServerContext* context, const common::HealthCheckRequest* request, common::HealthCheckResponse* response)
{
    boost::unique_lock<boost::mutex> lock(this->mutex);

    // If the service is empty we assume that the client wants to check the server's status.
    if (request->service().empty())
    {
        response->set_status(HealthCheckResponse::SERVING);
    }
    else if (this->statuses.find(request->service()) != this->statuses.end())
    {
        response->set_status(this->statuses[request->service()]);
    }
    else
    {
        response->set_status(HealthCheckResponse::UNKNOWN);
    }

    return grpc::Status::OK;
}
}
