/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef _ACCESSCONTROL_HH_
#define _ACCESSCONTROL_HH_

class AccessControl
{
public:
    AccessControl();

    ~AccessControl();

    void initSyncro(const char *);

    void setServerName(const char *serverName);

    void resetForNewClient();

    int crunchCapability(const char *);

    /*   0 - ok
       804 - capability refused
    */

    void wantToRead(); // both throw
    void wantToWrite();

    bool isClient();

private:
    int messageDigest(const char *input, char *output, const char *mdName);

    double initDeltaT;
    char serverName[100];

    bool okToRead;
    bool okToWrite;
    bool weHaveClient;
};

#endif
