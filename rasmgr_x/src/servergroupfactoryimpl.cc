#include "servergroupfactoryimpl.hh"
#include "servergroupimpl.hh"

namespace rasmgr
{

ServerGroupFactoryImpl::ServerGroupFactoryImpl(boost::shared_ptr<DatabaseHostManager> dbhManager, boost::shared_ptr<ServerFactory> serverFactory)
{
    this->dbhManager=dbhManager;
    this->serverFactory=serverFactory;
}

ServerGroupFactoryImpl::~ServerGroupFactoryImpl()
{

}

boost::shared_ptr<ServerGroup> ServerGroupFactoryImpl::createServerGroup(const ServerGroupConfigProto &config)
{
    boost::shared_ptr<ServerGroup> result(new ServerGroupImpl(config, this->dbhManager, this->serverFactory));

    return result;
}

}
