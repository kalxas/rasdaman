#ifndef RASMGR_X_SRC_SERVERGROUPFACTORY_HH
#define RASMGR_X_SRC_SERVERGROUPFACTORY_HH

#include <boost/shared_ptr.hpp>

#include "messages/rasmgrmess.pb.h"
#include "servergroup.hh"

namespace rasmgr
{
/**
 * @brief The ServerGroupFactory class Abstract Factory for creating a server
 * group given a server group configuration.
 */
class ServerGroupFactory
{
public:
    virtual ~ServerGroupFactory();

    virtual boost::shared_ptr<ServerGroup> createServerGroup(const ServerGroupConfigProto& config) = 0;
};
}

#endif // RASMGR_X_SRC_SERVERGROUPFACTORY_HH
