#ifndef RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH
#define RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH

#include "servergroupfactory.hh"

namespace rasmgr
{
class DatabaseHostManager;
class ServerFactory;

class ServerGroupFactoryImpl:public ServerGroupFactory
{
public:
    ServerGroupFactoryImpl(boost::shared_ptr<DatabaseHostManager> dbhManager, boost::shared_ptr<ServerFactory> serverFactory);
    virtual ~ServerGroupFactoryImpl();

    virtual boost::shared_ptr<ServerGroup> createServerGroup(const ServerGroupConfigProto& config);
private:
    boost::shared_ptr<DatabaseHostManager> dbhManager;
    boost::shared_ptr<ServerFactory> serverFactory;
};

}

#endif // RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH
