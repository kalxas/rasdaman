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

#include <cstring>

#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

#include <openssl/evp.h>
#include <easylogging++.h>

#include "userauthconverter.hh"
#include "authentication.hh"

#define RC_OK            0
#define ERRAUTHFNOTF    -1
#define ERRAUTHFCORR    -2
#define ERRAUTHFWRHOST  -3
#define ERRAUTHFVERS    -4

#define AUTHFILEID   26012001
#define AUTHFILEVERS 2;

#define MAXBUFF 500

namespace rasmgr
{

using google::protobuf::io::CodedInputStream;
using google::protobuf::io::CodedOutputStream;
using google::protobuf::io::IstreamInputStream;
using google::protobuf::io::OstreamOutputStream;


RandomGenerator UserAuthConverter::randomGenerator;

bool UserAuthConverter::tryGetOldFormatAuthData(const std::string &oldFilePath, UserMgrProto &out_userManagerData)
{
    // Clear the output data.
    out_userManagerData.Clear();

    int result = RC_OK;

    LDEBUG << "Inspecting authorization file '"<<oldFilePath<< "'...";
    std::ifstream ifs(oldFilePath.c_str());

    if(!ifs)
        result = ERRAUTHFNOTF;

    if (result == RC_OK)
    {
        int verificationResult=verifyAuthFile(ifs);
        if(verificationResult)
        {
            result = verificationResult;
            LDEBUG << "Failed to verify old authentication file." << result;
        }
    }

    if (result == RC_OK)
    {
        AuthFileHeader header;
        ifs.read((char*)&header,sizeof(header));

        // not necessary, done by verify  if(header.fileID != AUTHFIELID) return ERRAUTHFCORR;

        // this is needed
        if(!randomGenerator.setFileVersion(header.fileVersion)) return ERRAUTHFVERS;

        initCrypt(header.lastUserID);

        for(int i=0; i<header.countUsers; i++)
        {
            AuthUserRec uRec;
            ifs.read((char*)&uRec,sizeof(uRec));
            crypt(&uRec,sizeof(uRec));

            UserProto user;
            user.set_name(uRec.userName);
            user.set_password(uRec.passWord);
            user.mutable_admin_rights()->CopyFrom(convertAdminRightsToProto(uRec.adminRight));
            user.mutable_default_db_rights()->CopyFrom(convertDbRightsToProto(uRec.databRight));

            out_userManagerData.add_users()->CopyFrom(user);
        }
    }

    if (result != ERRAUTHFNOTF)
        ifs.close();

    switch(result)
    {
    case RC_OK:
        LDEBUG << "ok";
        break;
    case  ERRAUTHFNOTF:
        LDEBUG << "Warning: User authorization file not found, using default user settings.";
        break;
    case  ERRAUTHFCORR:
        LDEBUG<<"Error: User authorization file is corrupt, aborting.";
        break;
    case  ERRAUTHFWRHOST:
        LDEBUG<<"Error: User authorization file is not for this host.";
        break;
    case  ERRAUTHFVERS:
        LDEBUG<<"Error: User authorization file is incompatible due to different encryption used - see migration documentation.";
        break;
    default:                            // should not occur, internal enum mismatch
        LDEBUG<<"Error: Internal evaluation error.";
        break;
    }

    return result==RC_OK;
}

void UserAuthConverter::initCrypt(int seed)
{
    randomGenerator.init(static_cast<unsigned int>(seed));
}

int UserAuthConverter::verifyAuthFile(std::ifstream &ifs)
{
    EVP_MD_CTX mdctx;
    const EVP_MD *md;
    unsigned int md_len;
    unsigned char md_value[50];

    OpenSSL_add_all_digests();
    md = EVP_get_digestbyname("MD5");
    if(!md)
        return false;

    EVP_DigestInit(&mdctx, md);

    AuthFileHeader header;
    ifs.read((char*)&header,sizeof(header));

    if(header.fileID != AUTHFILEID)
        return ERRAUTHFCORR;

    if(!randomGenerator.setFileVersion(header.fileVersion))
        return ERRAUTHFVERS;

    initCrypt(header.lastUserID);


    unsigned char buff[MAXBUFF];
    long cpos = ifs.tellg();
    ifs.seekg(0,std::ios::end);
    long endpos=ifs.tellg();
    ifs.seekg(cpos,std::ios::beg);

    for(;;)
    {
        int r = endpos-cpos > MAXBUFF ? MAXBUFF : endpos-cpos;
        if(r==0)
            break;

        ifs.read((char*)buff,r);
        if(!ifs)
            break;

        cpos +=r;

        crypt(buff,r);

        EVP_DigestUpdate(&mdctx,buff,static_cast<size_t>(r));
    }

    EVP_DigestFinal(&mdctx, md_value, &md_len);

    ifs.seekg(0,std::ios::beg);

    for(unsigned int i=0; i<md_len; i++)
    {
        if(md_value[i]!=header.messageDigest[i])
            return ERRAUTHFCORR;
    }

    return 0;
}

void UserAuthConverter::crypt(void *vbuffer, int length)
{
    unsigned char *buff=static_cast<unsigned char*>(vbuffer);
    for(int i=0; i<length; i++) buff[i]^=randomGenerator(); //rand();
}

UserDatabaseRightsProto UserAuthConverter::convertDbRightsToProto(int right)
{
    UserDatabaseRightsProto adminRightsProto;

    //dbR_read   = 1<<8,   // R
    //dbR_write  = 2<<8    // W

    bool readRights= right & (1<<8);
    bool writeRights= right & (2<<8);

    adminRightsProto.set_read(readRights);
    adminRightsProto.set_write(writeRights);

    return adminRightsProto;
}

UserAdminRightsProto UserAuthConverter::convertAdminRightsToProto(int adminRights)
{
    UserAdminRightsProto result;
    /*
    admR_config=  1,   // C
    admR_acctrl=  2,   // A
    admR_sysup =  4,   // S   - up-down
    admR_info  =  8,   // I
    admR_full  =255
    */

    result.set_system_config_rights(adminRights & 1);
    result.set_access_control_rights(adminRights & 2 );
    result.set_server_admin_rights(adminRights & 4);
    result.set_info_rights(adminRights & 8);

    return result;
}
}
