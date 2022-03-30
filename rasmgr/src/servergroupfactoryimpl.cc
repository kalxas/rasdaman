#include "databasehostmanager.hh"
#include "servergroupfactoryimpl.hh"
#include "servergroupimpl.hh"

namespace rasmgr
{

ServerGroupFactoryImpl::ServerGroupFactoryImpl(std::shared_ptr<DatabaseHostManager> m, std::shared_ptr<ServerFactory> f)
{
    this->dbhManager = m;
    this->serverFactory = f;
}

std::shared_ptr<ServerGroup> ServerGroupFactoryImpl::createServerGroup(const ServerGroupConfigProto &config)
{
    std::shared_ptr<ServerGroup> result(new ServerGroupImpl(config, this->dbhManager, this->serverFactory));
    return result;
}

}
