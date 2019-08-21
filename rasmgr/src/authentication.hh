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

#ifndef RASMGR_X_SRC_AUTHENTICATION_HH
#define RASMGR_X_SRC_AUTHENTICATION_HH

/**
 * This file contains structures used for reading the old authentication file.
 * They should be removed in version 10.
 */
namespace rasmgr
{

struct AuthFileHeader
{
    long fileID;
    long fileVersion;
    long headerLength;
    long lastUserID;
    char hostName[100];
    long countUsers;
    unsigned char messageDigest[35];
    int  globalInitAdmR;
    int  globalInitDbsR;
    char _unused[100];
};

struct AuthUserRec
{
    long userID;
    char userName[100];
    char passWord[50];

    int  adminRight;
    int  databRight;
    long countRights;
    char _unused[32];
};

struct AuthDbRRec
{
    char dbName[100];
    int  right;
};

}
#endif // RASMGR_X_SRC_AUTHENTICATION_HH
