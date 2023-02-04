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

#include "grpcutils.hh"

#include "../network/networkresolverfactory.hh"
#include "../exceptions/exception.hh"
#include "../exceptions/runtimeexception.hh"
#include <logging.hh>

#include <grpc/support/log.h>
#include <stdexcept>
#include <chrono>
#include <cstring>

namespace common
{

using std::string;
using grpc::Status;
using std::chrono::system_clock;
using std::chrono::milliseconds;

std::string GrpcUtils::constructAddressString(const std::string &host,
                                              std::uint32_t port) {
    return host + ":" + std::to_string(port);
}

grpc::Status GrpcUtils::convertExceptionToStatus(std::exception &exception) {
    ErrorMessage errorMessage;

    //The type is STL
    errorMessage.set_type(ErrorMessage::STL);
    errorMessage.set_error_text(exception.what());

    Status status(grpc::StatusCode::UNKNOWN, errorMessage.SerializeAsString());
    return status;
}

grpc::Status GrpcUtils::convertExceptionToStatus(common::Exception &exception) {
    ErrorMessage errorMessage;
    //The type is STL
    errorMessage.set_type(ErrorMessage::RERROR);
    errorMessage.set_error_text(exception.what());

    Status status(grpc::StatusCode::UNKNOWN, errorMessage.SerializeAsString());
    return status;
}

grpc::Status GrpcUtils::convertExceptionToStatus(const std::string &errorMessage) {
    ErrorMessage message;

    //The type is UNKNOWN
    message.set_type(ErrorMessage::UNKNOWN);
    message.set_error_text(errorMessage);

    Status status(grpc::StatusCode::UNKNOWN, message.SerializeAsString());
    return status;
}

void GrpcUtils::convertStatusToExceptionAndThrow(const grpc::Status &status) {
  if (status.error_code() == grpc::StatusCode::UNKNOWN) {
    //We might be able to handle this
    ErrorMessage message;
    if (message.ParseFromString(status.error_message())) {
      switch (message.type()) {
        case ErrorMessage::STL:
          throw Exception(message.error_text());
        case ErrorMessage::RERROR:
          throw Exception(message.error_text());
        case ErrorMessage::UNKNOWN:
          throw Exception(message.error_text());
        default:
          //Throw a generic exception.
          throw Exception(message.error_text());
      }
    } else {
      //Throw a generic exception.
      throw Exception(status.error_message());
    }
  }
    //Throw an exception only if the status is invalid
  else if (!status.ok()) {
    throw RuntimeException(status.error_message());
  }
}

string GrpcUtils::convertStatusToString(const grpc::Status &status)
{
#define HANDLE_CASE(c) \
    case grpc::StatusCode::c: code = #c; break;
  
  std::string code;
  switch (status.error_code())
  {
    HANDLE_CASE(OK)
    HANDLE_CASE(CANCELLED)
    HANDLE_CASE(UNKNOWN)
    HANDLE_CASE(INVALID_ARGUMENT)
    HANDLE_CASE(DEADLINE_EXCEEDED)
    HANDLE_CASE(NOT_FOUND)
    HANDLE_CASE(ALREADY_EXISTS)
    HANDLE_CASE(PERMISSION_DENIED)
    HANDLE_CASE(UNAUTHENTICATED)
    HANDLE_CASE(RESOURCE_EXHAUSTED)
    HANDLE_CASE(FAILED_PRECONDITION)
    HANDLE_CASE(ABORTED)
    HANDLE_CASE(OUT_OF_RANGE)
    HANDLE_CASE(UNIMPLEMENTED)
    HANDLE_CASE(INTERNAL)
    HANDLE_CASE(UNAVAILABLE)
    HANDLE_CASE(DATA_LOSS)
    default:
      code = std::to_string(int(status.error_code()));
      break;
  }
  std::string msg = status.error_message();
  std::string details = status.error_details();
  
  std::string ret;
  ret.reserve(code.size() + 2 + msg.size() + details.size() + (details.size() > 0 ? 2 : 0));
  ret += code;
  ret += ", ";
  ret += msg;
  if (!details.empty()) {
    ret += ", ";
    ret += details;
  }
  
  return ret;
}

bool GrpcUtils::isServerAlive(const std::shared_ptr<HealthService::Stub> &healthService,
                              uint32_t timeoutMilliseconds) {
  HealthCheckRequest request;
  HealthCheckResponse response;

  system_clock::time_point
      deadline = system_clock::now() + milliseconds(timeoutMilliseconds);

  grpc::ClientContext context;
  context.set_deadline(deadline);

  grpc::Status status = healthService->Check(&context, request, &response);

  return status.ok() && response.status() == HealthCheckResponse::SERVING;
}

bool GrpcUtils::isPortBusy(const std::string &host, std::uint32_t port) {
  return NetworkResolverFactory::getNetworkResolver(host, port)->isPortBusy();
}

grpc::ChannelArguments GrpcUtils::getDefaultChannelArguments() {
  grpc::ChannelArguments args;
  args.SetMaxReceiveMessageSize(-1); // unlimited
  args.SetMaxSendMessageSize(-1); // unlimited
  return args;
}

void gpr_replacement_log(gpr_log_func_args *args);
void gpr_replacement_log(gpr_log_func_args *args) {
  string prefix = "GRPC: ";
  string separator = " ";

  switch (args->severity) {
    case GPR_LOG_SEVERITY_DEBUG: {
      LDEBUG << prefix
             << args->file << separator
             << args->line << separator
             << args->message;
    }
      break;
    case GPR_LOG_SEVERITY_INFO: {
      LINFO << prefix
            << args->file << separator
            << args->line << separator
            << args->message;
    }
      break;
    case GPR_LOG_SEVERITY_ERROR: {
      LERROR << prefix
             << args->file << separator
             << args->line << separator
             << args->message;
    }
      break;
    default: {
      LERROR << prefix
             << args->file << separator
             << args->line << separator
             << args->message;
    }
  }
}

void GrpcUtils::redirectGRPCLogToEasyLogging() {
  gpr_set_log_function(gpr_replacement_log);
}

void GrpcUtils::setDeadline(grpc::ClientContext &context, size_t deadlineInMs) {
  static const size_t noDeadline = 0;
  if (deadlineInMs != noDeadline)
  {
      auto deadline = system_clock::now() + milliseconds(deadlineInMs);
      context.set_deadline(deadline);
  }
}

bool GrpcUtils::errorIsDeadlineExceeded(const Exception &ex) {
  return ex.what() == "DeadlineExceeded";
}

}

