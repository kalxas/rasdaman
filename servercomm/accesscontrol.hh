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

#include <string>

class AccessControl
{
public:
    AccessControl();
    ~AccessControl();

    void setServerName(const char *serverName);

    void resetForNewClient();

    /// parse the capability string; return 0 - ok, or 804 - capability refused
    int crunchCapability(const char *);

    void wantToRead();  // could throw r_Eno_permission
    void wantToWrite(); // could throw r_Eno_permission

    bool isClient();

private:
    /// create a digest from input into output, return the length of the result digest
    int messageDigest(const char *input, char *output, const char *mdName);

    void checkParam(char *&param, const char *paramName);

    static const unsigned long maxServerNameSize;
    static const unsigned long maxDigestBufferSize;
    static const unsigned long capabilityDigestSize;
    static const unsigned long maxCapabilityBufferSize;
    static const char *digestMethod;

    static const int capabilityOk;

    std::string serverName;

    bool okToRead{false};
    bool okToWrite{false};
    bool weHaveClient{false};
};

#endif
