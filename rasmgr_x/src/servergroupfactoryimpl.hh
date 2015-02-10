#ifndef RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH
#define RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH

#include "servergroupfactory.hh"

namespace rasmgr
{
class ServerGroupFactoryImpl:public ServerGroupFactory
{
public:
    //TODO:Implement
    ServerGroupFactoryImpl();
    virtual ~ServerGroupFactoryImpl();

    virtual boost::shared_ptr<ServerGroup> createServerGroup(const ServerGroupConfigProto& config);
};

}

#endif // RASMGR_X_SRC_SERVERGROUPFACTORYIMPL_HH
