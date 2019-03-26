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
#include <logging.hh>

#define _XOPEN_SOURCE
#include <time.h>  // for strptime
#include <cstring>
#include <openssl/evp.h>


AccessControl accessControl;

AccessControl::AccessControl()
{
    initDeltaT = 0;
    resetForNewClient();
}

AccessControl::~AccessControl()
{
}

void AccessControl::setServerName(const char *newServerName)
{
    strcpy(this->serverName, newServerName);
}

void AccessControl::initSyncro(const char *syncroString)
{
    struct tm brokentime;
    strptime(syncroString, "%d:%m:%Y:%H:%M:%S", &brokentime);
    initDeltaT = difftime(time(NULL), mktime(&brokentime));
    // cout<<"DeltaT="<<initDeltaT<<endl;
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

int AccessControl::crunchCapability(const char *capability)
{
    // verify capability is original
    char capaQ[200];
    strcpy(capaQ, "$Canci");
    strcat(capaQ, capability);

    char *digest = strstr(capaQ, "$D");
    if (digest == NULL)
        return CAPABILITY_REFUSED;

    *digest = 0;
    digest += 2;
    digest[32] = 0;
    LDEBUG << "Digest=" << digest;

    char testdigest[50];
    messageDigest(capaQ, testdigest, "MD5");
    LDEBUG << "testdg=" << testdigest;
    if (strcmp(testdigest, digest) != 0)
        return CAPABILITY_REFUSED;

    char *rights = strstr(capaQ, "$E") + 2;
    char *timeout = strstr(capaQ, "$T") + 2;
    char *cServerName = strstr(capaQ, "$N") + 2;
    // end of cServername is $D, $->0 by digest

    struct tm brokentime;
    memset(&brokentime, 0, sizeof(struct tm));
    strptime(timeout, "%d:%m:%Y:%H:%M:%S", &brokentime);
    double DeltaT = difftime(mktime(&brokentime), time(NULL));

    //for the  moment, DEC makes trouble
    // if(DeltaT < initDeltaT) return CAPABILITY_REFUSED; //!!! Capability too old
    //  cout<<"DeltaT="<<DeltaT<<"  initDeltaT="<<initDeltaT<<(DeltaT >= initDeltaT ? " ok":" fail")<<endl;

    if (strcmp(serverName, cServerName) != 0)
    {
        return CAPABILITY_REFUSED; //!!! Call is not for me
    }

    okToRead = false;  // looks like a 'true' never gets reset: -- PB 2006-jan-02
    okToWrite = false;  // -dito-
    for (int i = 0; *rights != '$' && *rights && i < 2; rights++, i++)
    {
        //We only have 2 rights defined now
        if (*rights == 'R')
        {
            okToRead = true;
        }
        if (*rights == 'W')
        {
            okToWrite = true;
        }
    }

    weHaveClient = true;

    LDEBUG << "capability crunched: digest=" << digest << ", rights=" << rights << ", timeout=" << timeout
           << "(remaining time: " << DeltaT << "), cServerName=" << cServerName << ", okToRead=" << okToRead
           << ", okToWrite=" << okToWrite
           << "";

    return 0; // OK for now
}

int AccessControl::messageDigest(const char *input, char *output, const char *mdName)
{
    const EVP_MD *md;
    unsigned int md_len, i;
    unsigned char md_value[100];

    OpenSSL_add_all_digests();

    md = EVP_get_digestbyname(mdName);

    if (!md)
    {
        return 0;
    }

#if OPENSSL_VERSION_NUMBER < 0x10100000L
    EVP_MD_CTX mdctx;
    EVP_DigestInit(&mdctx, md);
    EVP_DigestUpdate(&mdctx, input, strlen(input));
    EVP_DigestFinal(&mdctx, md_value, &md_len);
#else
    EVP_MD_CTX *mdctx = EVP_MD_CTX_new();
    EVP_DigestInit(mdctx, md);
    EVP_DigestUpdate(mdctx, input, strlen(input));
    EVP_DigestFinal(mdctx, md_value, &md_len);
    EVP_MD_CTX_free(mdctx);
#endif

    for (i = 0; i < md_len; i++)
    {
        sprintf(output + i + i, "%02x", md_value[i]);
    }

    return strlen(output);
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

