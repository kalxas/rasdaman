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

#ifndef RASMGR_X_SRC_USERAUTHCONVERTER_HH
#define RASMGR_X_SRC_USERAUTHCONVERTER_HH

#include <fstream>
#include <string>

#include "messages/rasmgrmess.pb.h"
#include "randomgenerator.hh"

namespace rasmgr
{

/**
 * @brief The UserAuthConverter class This class is DEPRECATED.
 * It is only used for backwards compatibility with the old protocol. This class should only be used
 * in the UserManager.
 */
class UserAuthConverter
{
public:
    /**
     * @brief tryGetOldFormatAuthenticationData Will try to read the given authentication file.
     * If the reading is successful, it returns the data stored in the file.
     * @param oldFilePath
     * @return TRUE if the file represents authentication file stored in the old format, FALSE otherwise.
     */
    static bool tryGetOldFormatAuthData(const std::string& oldFilePath, UserMgrProto& out_userManagerData);
private:
    static RandomGenerator randomGenerator;

    static void initCrypt(int seed);

    static int verifyAuthFile(std::ifstream &ifs);

    static void crypt(void *vbuffer,int length);

    static rasmgr::UserDatabaseRightsProto convertDbRightsToProto(int right);

    static rasmgr::UserAdminRightsProto convertAdminRightsToProto(int adminRights);

    static void saveUserInformation(const rasmgr::UserMgrProto& userData, const std::string& filePath);
};

}
#endif // RASMGR_X_SRC_USERAUTHCONVERTER_HH
