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

#include "accesscontrol.hh"
#include "raslib/error.hh"
#include "common/crypto/crypto.hh"
#include "globals.hh"
#include <logging.hh>

#ifndef _XOPEN_SOURCE
#define _XOPEN_SOURCE
#endif
#include <time.h>  // for strptime
#include <cstring>
#include <openssl/evp.h>

const unsigned long AccessControl::maxServerNameSize = 100;
const unsigned long AccessControl::maxDigestBufferSize = 50;
const unsigned long AccessControl::capabilityDigestSize = 32;
const unsigned long AccessControl::maxCapabilityBufferSize = 200;
const int AccessControl::capabilityOk = 0;
const char *AccessControl::digestMethod = DEFAULT_DIGEST;

AccessControl accessControl;

AccessControl::AccessControl()
{
}

AccessControl::~AccessControl()
{
}

void AccessControl::setServerName(const char *newServerName)
{
    if (strlen(newServerName) < maxServerNameSize)
    {
        serverName = newServerName;
    }
    else
    {
        LERROR << "Server name length exceeds the maximum allowed length (" << maxServerNameSize << ").";
        throw r_Error(10000); // internal error
    }
}

void AccessControl::resetForNewClient()
{
    okToRead = false;
    okToWrite = false;
    weHaveClient = false;
}

bool AccessControl::isClient()
{
    return weHaveClient;
}

#define CHECK_PARAM(param, searchStart, paramName, paramPrefix) \
    char *param = strstr(capaQ, paramPrefix); \
    if (param) { \
        param += 2; \
    } else { \
        LERROR << paramName << " not found in capability string."; \
        return CAPABILITY_REFUSED; \
    }

int AccessControl::crunchCapability(const char *capability)
{
    LTRACE << "Parsing and verifying capability string...";

    auto capabilitySize = strlen(capability);
    // 7 - account for "$Canci" which is prepended to the capability below
    if (capabilitySize + 7 > maxCapabilityBufferSize)
    {
        LERROR << "Length of capability string exceeds the maximum buffer size of " << maxCapabilityBufferSize;
        return CAPABILITY_REFUSED;
    }

    char capaQ[maxCapabilityBufferSize];
    strcpy(capaQ, "$Canci");
    strcat(capaQ, capability);

    char *end = strstr(capaQ, "$K");
    if (end == NULL)
    {
        return CAPABILITY_REFUSED;
    }
    end[0] = '\0';

    // verify capability is original
    CHECK_PARAM(digest, capaQ, "Digest", "$D")
    *(digest - 2) = 0;
    digest[capabilityDigestSize] = 0;
    LTRACE << "Client digest = " << digest;

    char testdigest[maxDigestBufferSize];
    messageDigest(capaQ, testdigest, digestMethod);
    LTRACE << "Server digest = " << testdigest;
    if (strcmp(testdigest, digest) != 0)
    {
        LERROR << "Server digest of capability string does not match the client digest.";
        return CAPABILITY_REFUSED;
    }

    CHECK_PARAM(rights, digest, "Rights", "$E")
    CHECK_PARAM(timeout, rights, "Timeout", "$T")
    CHECK_PARAM(cServerName, timeout, "Server", "$N")
    if (strcmp(serverName.c_str(), cServerName) != 0)
    {
        LERROR << "Wrong server name in capability.";
        return CAPABILITY_REFUSED;
    }

    okToRead = false;
    okToWrite = false;
    for (size_t i = 0; *rights != '$' && *rights && i < 2; rights++, i++)
    {
        if (*rights == 'R')
        {
            okToRead = true;
        }
        else if (*rights == 'W')
        {
            okToWrite = true;
        }
    }
    weHaveClient = true;

    LTRACE << "capability crunched: digest=" << digest << ", rights=" << rights << ", timeout=" << timeout
           << ", okToRead=" << okToRead << ", okToWrite=" << okToWrite;

    return capabilityOk;
}

int AccessControl::messageDigest(const char *input, char *output, const char *mdName)
{
    std::string mdNameStr{mdName};
    std::string inputStr{input};
    
    auto result = common::Crypto::messageDigest(inputStr, mdNameStr);
    if (result.empty())
        return 0;
    
    strcpy(output, result.c_str());
    return int(result.size());
}

void AccessControl::wantToRead()
{
    if (okToRead == false)
    {
        LERROR << "No permission for read operation.";
        throw r_Eno_permission(); //r_Error(NO_PERMISSION_FOR_OPERATION);
    }
}

void AccessControl::wantToWrite()
{
    if (okToWrite == false)
    {
        LERROR << "No permission for write operation.";
        throw r_Eno_permission(); //r_Error(NO_PERMISSION_FOR_OPERATION);
    }
}

