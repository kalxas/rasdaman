#include "servergroupfactory.hh"
#include "servergroup.hh"
#include "databasehostmanager.hh"

namespace rasmgr
{

ServerGroupFactory::ServerGroupFactory(std::shared_ptr<DatabaseHostManager> m,
                                       std::shared_ptr<ServerFactory> f)
    : dbhManager{m}, serverFactory{f}

{
}

std::shared_ptr<ServerGroup> ServerGroupFactory::createServerGroup(
    const ServerGroupConfigProto &config)
{
    return std::make_shared<ServerGroup>(config, dbhManager, serverFactory);
}

}  // namespace rasmgr
