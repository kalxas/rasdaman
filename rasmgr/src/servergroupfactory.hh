#ifndef RASMGR_X_SRC_SERVERGROUPFACTORY_HH
#define RASMGR_X_SRC_SERVERGROUPFACTORY_HH

#include "rasmgr/src/messages/rasmgrmess.pb.h"
#include <memory>

namespace rasmgr
{
class ServerGroup;
class DatabaseHostManager;
class ServerFactory;

/**
 * Factory for creating a server group given a server group configuration. It's
 * only necessary to allow testing with mocked objects.
 */
class ServerGroupFactory
{
public:
    ServerGroupFactory(std::shared_ptr<DatabaseHostManager> dbhManager,
                       std::shared_ptr<ServerFactory> serverFactory);
    
    virtual ~ServerGroupFactory() = default;
  
    virtual std::shared_ptr<ServerGroup> createServerGroup(const ServerGroupConfigProto &config);
private:
    std::shared_ptr<DatabaseHostManager> dbhManager;
    std::shared_ptr<ServerFactory> serverFactory;
};
}

#endif // RASMGR_X_SRC_SERVERGROUPFACTORY_HH
