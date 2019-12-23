#ifndef RASMGR_X_SRC_SERVERGROUPFACTORY_HH
#define RASMGR_X_SRC_SERVERGROUPFACTORY_HH

#include "rasmgr/src/messages/rasmgrmess.pb.h"
#include <memory>

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

    virtual std::shared_ptr<ServerGroup> createServerGroup(const ServerGroupConfigProto &config) = 0;
};
}

#endif // RASMGR_X_SRC_SERVERGROUPFACTORY_HH
