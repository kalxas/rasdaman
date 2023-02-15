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
#ifndef RASMGR_X_SRC_CLIENTCREDENTIALS_HH_
#define RASMGR_X_SRC_CLIENTCREDENTIALS_HH_

#include <string>

namespace rasmgr
{

/**
 * Credentials specified by the client when it wants to connect to the server
 * in the ClientManager with a `connectClient(...)` call.
 */
class ClientCredentials
{
public:

    /**
     * @param userName User name
     * @param passwordHash Password hash
     * @param token user token
     */
    ClientCredentials(const std::string &userName = "",
                      const std::string &passwordHash = "",
                      const std::string &token = "");

    ~ClientCredentials() = default;

    /**
     * @return the password hash.
     */
    const std::string &getPasswordHash() const;

    /**
     * Set the password hash.
     */
    void setPasswordHash(const std::string &passwordHash);

    /**
     *
     * @return the user name
     */
    const std::string &getUserName() const;

    /**
     * Set the user token
     */
    void setToken(const std::string &token);

    /**
     *
     * @return the user token
     */
    const std::string &getToken() const;

  /**
   * Set the user name
   */
  void setUserName(const std::string &userName);

private:
    std::string userName;/*! User name used for authenticating the client*/
    std::string passwordHash;/*!Password hash used for authenticating the client */
    std::string token;/*!JWT used for authenticating the client*/
};
} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_CLIENTCREDENTIALS_HH_ */
