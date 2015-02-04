/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef RASMGR_X_SRC_USERMANAGER_HH_
#define RASMGR_X_SRC_USERMANAGER_HH_

#include <list>
#include <boost/thread.hpp>
#include <boost/shared_ptr.hpp>

#include "user.hh"
#include "useradminrights.hh"
#include "userdatabaserights.hh"
#include "messages/rasmgrmess.pb.h"

namespace rasmgr
{

class UserManager
{
public:
    UserManager();

    virtual ~UserManager();

    /**
     * Insert a new user into the list of users.
     * @return If a user with the same name already exits, return FALSE, TRUE otherwise.
     */
    virtual void defineUser ( const UserProto& userInfo);

    /**
     * @brief changeUser Change the value of any property of the user that is set
     * in newUserInfo
     * @param userName
     * @param newUserInfo
     */
    virtual void changeUser (const std::string& userName, const UserProto& newUserInfo);

    /**
     * Remove the user with the given name from the list of users.
     * @param userName
     * @return FALSE if there was no user with this name, TRUE otherwise
     */
    virtual void removeUser ( const std::string& userName );

    /**
     * Get a copy of the user with the given name
     * @param userName
     * @return TRUE if there was a user with this name, FALSE otherwise
     */
    virtual bool tryGetUser ( const std::string& userName, boost::shared_ptr<User>& out_user );

    /**
     * @brief Save the information stored by the user manager to the RASMGR_AUTH_FILE
     * @return void
     * @throws std::runtime_error If the authentication information could not be saved.
     */
    virtual void saveUserInformation();

    /**
     * @brief loadUserInformation Load user information from authentication file rasmgr.conf
     * If the file does not exist or is corrupted, default users are loaded accordint to
     * the documentation
     * THIS METHOD IS NOT THREAD SAFE
     */
    virtual void loadUserInformation();

    virtual UserMgrProto serializeToProto();

private:
    std::list<boost::shared_ptr<User> > userList;
    boost::mutex mut;
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_USERMANAGER_HH_ */
