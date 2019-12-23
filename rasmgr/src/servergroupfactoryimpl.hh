#ifndef RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH
#define RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH

#include "servergroupfactory.hh"

namespace rasmgr
{
class DatabaseHostManager;
class ServerFactory;

class ServerGroupFactoryImpl: public ServerGroupFactory
{
public:
    ServerGroupFactoryImpl(std::shared_ptr<DatabaseHostManager> dbhManager, std::shared_ptr<ServerFactory> serverFactory);
    virtual ~ServerGroupFactoryImpl();

    virtual std::shared_ptr<ServerGroup> createServerGroup(const ServerGroupConfigProto &config);
private:
    std::shared_ptr<DatabaseHostManager> dbhManager;
    std::shared_ptr<ServerFactory> serverFactory;
};

}

#endif // RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH
