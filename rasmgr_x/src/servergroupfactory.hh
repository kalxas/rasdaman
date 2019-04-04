#ifndef RASMGR_X_SRC_SERVERGROUPFACTORY_HH
#define RASMGR_X_SRC_SERVERGROUPFACTORY_HH

#include <boost/shared_ptr.hpp>

#include "rasmgr_x/src/messages/rasmgrmess.pb.h"

namespace rasmgr
{
class ServerGroup;

/**
 * @brief The ServerGroupFactory class Abstract Factory for creating a server
 * group given a server group configuration.
 */
class ServerGroupFactory
{
public:
    virtual ~ServerGroupFactory();

    virtual boost::shared_ptr<ServerGroup> createServerGroup(const ServerGroupConfigProto &config) = 0;
};
}

#endif // RASMGR_X_SRC_SERVERGROUPFACTORY_HH
