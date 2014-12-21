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
     * @param userName The user name the new user will have
     * @param password The password the new user will have
     * @param defaultRights The default database rights the new user will have
     * @param adminRights The administration rights the new user will have
     * @return If a user with the same name already exits, return FALSE, TRUE otherwise.
     */
    void defineUser ( const std::string& userName, const std::string& password, const UserDatabaseRights& defaultRights, const UserAdminRights& adminRights );

    /**
     * Remove the user with the given name from the list of users.
     * @param userName
     * @return FALSE if there was no user with this name, TRUE otherwise
     */
    void removeUser ( const std::string& userName );

    /**
     * Change the name of the user.
     * @param oldUserName
     * @param newUserName
     * @return FALSE if there was no user with this name, TRUE otherwise
     */
    void changeUserName ( const std::string& oldUserName, const std::string& newUserName );

    /**
     * Change the password of a user
     * @param oldUserName
     * @param newPassword
     * @return FALSE if there was no user with this name, TRUE otherwise
     */
    void changeUserPassword ( const std::string& userName, const std::string& newPassword );

    /**
     * Change the user's administration rights
     * @param userName
     * @param newRights
     * @return FALSE if there was no user with this name, TRUE otherwise
     */
    void changeUserAdminRights ( const std::string& userName, const UserAdminRights& newRights );

    /**
     * Change the users rights on a particular database.
     * @param userName
     * @param databaseName
     * @param newRights
     * @return FALSE if there was no user with this name, TRUE otherwise.
     */
    void changeUserDatabaseRights ( const std::string& userName, const UserDatabaseRights& newRights );

    /**
     * Get a copy of the user with the given name
     * @param userName
     * @return TRUE if there was a user with this name, FALSE otherwise
     */
    bool tryGetUser ( const std::string& userName, boost::shared_ptr<User>& out_user );

    /**
     * @return a copy of the user list.
     */
    std::list<boost::shared_ptr<User> > getUserList() const;


    /**
     * @brief Save the information stored by the user manager to the RASMGR_AUTH_FILE
     * @return void
	 * @throws std::runtime_error If the authentication information could not be saved.
     */
    void saveToAuthenticationFile();

private:
    std::list<boost::shared_ptr<User> > userList;
    boost::mutex mut;

    /**
     * Load the list of users from the authentication file or, if the file is not available, load the defaults.
     */
    void loadAuthenticationFile();

};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_USERMANAGER_HH_ */
