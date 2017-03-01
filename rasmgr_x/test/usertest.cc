#include <gtest/gtest.h>

#include "rasmgr_x/src/user.hh"
#include "rasmgr_x/src/userdatabaserights.hh"
#include "rasmgr_x/src/useradminrights.hh"
#include "rasmgr_x/src/messages/rasmgrmess.pb.h"

#include "util/testutil.hh"

namespace rasmgr
{
namespace test
{
TEST(UserTest, serializeToProto)
{
    using rasmgr::UserDatabaseRights;
    using rasmgr::UserAdminRights;
    using rasmgr::User;
    using rasmgr::UserProto;
    using rasmgr::test::TestUtil;

    bool readRights = TestUtil::generateRandomElement(false, true);
    bool writeRights = TestUtil::generateRandomElement(false, true);
    UserDatabaseRights dbRights(readRights, writeRights);

    bool controlRights = TestUtil::generateRandomElement(false, true);
    bool infoRights = TestUtil::generateRandomElement(false, true);
    bool adminRights = TestUtil::generateRandomElement(false, true);
    bool configRights = TestUtil::generateRandomElement(false, true);

    UserAdminRights userAdminRights;
    userAdminRights.setAccessControlRights(controlRights);
    userAdminRights.setInfoRights(infoRights);
    userAdminRights.setServerAdminRights(adminRights);
    userAdminRights.setSystemConfigRights(configRights);

    std::string name = "name";
    std::string password = "passwd";

    User user(name, password, dbRights, userAdminRights);
    UserProto proto = User::serializeToProto(user);

    ASSERT_EQ(name, proto.name());
    ASSERT_EQ(password, proto.password());

    ASSERT_EQ(readRights, proto.default_db_rights().read());
    ASSERT_EQ(writeRights, proto.default_db_rights().write());

    ASSERT_EQ(controlRights, proto.admin_rights().access_control_rights());
    ASSERT_EQ(infoRights, proto.admin_rights().info_rights());
    ASSERT_EQ(adminRights, proto.admin_rights().server_admin_rights());
    ASSERT_EQ(configRights, proto.admin_rights().system_config_rights());
}

TEST(UserTest, parseFromProto)
{
    using rasmgr::UserAdminRights;
    using rasmgr::UserAdminRightsProto;
    using rasmgr::UserDatabaseRightsProto;
    using rasmgr::UserDatabaseRights;
    using rasmgr::UserProto;
    using rasmgr::User;
    using rasmgr::test::TestUtil;

    std::string name = "name";
    std::string password = "passwd";

    bool controlRights = TestUtil::generateRandomElement(false, true);
    bool infoRights = TestUtil::generateRandomElement(false, true);
    bool adminRights = TestUtil::generateRandomElement(false, true);
    bool configRights = TestUtil::generateRandomElement(false, true);

    UserAdminRightsProto* userAdminRights = new UserAdminRightsProto();
    userAdminRights->set_access_control_rights(controlRights);
    userAdminRights->set_info_rights(infoRights);
    userAdminRights->set_server_admin_rights(adminRights);
    userAdminRights->set_system_config_rights(configRights);

    bool readRights = TestUtil::generateRandomElement(false, true);
    bool writeRights = TestUtil::generateRandomElement(false, true);

    UserDatabaseRightsProto* userDbRights = new UserDatabaseRightsProto();
    userDbRights->set_read(readRights);
    userDbRights->set_write(writeRights);

    UserProto userProto;
    userProto.set_name(name);
    userProto.set_password(password);
    userProto.set_allocated_admin_rights(userAdminRights);
    userProto.set_allocated_default_db_rights(userDbRights);

    User user = User::parseFromProto(userProto);

    ASSERT_EQ(name, user.getName());
    ASSERT_EQ(password, user.getPassword());

    ASSERT_EQ(controlRights, user.getAdminRights().hasAccessControlRights());
    ASSERT_EQ(infoRights, user.getAdminRights().hasInfoRights());
    ASSERT_EQ(adminRights, user.getAdminRights().hasServerAdminRights());
    ASSERT_EQ(configRights, user.getAdminRights().hasSystemConfigRights());

    ASSERT_EQ(readRights, user.getDefaultDbRights().hasReadAccess());
    ASSERT_EQ(writeRights, user.getDefaultDbRights().hasWriteAccess());
}

}
}
