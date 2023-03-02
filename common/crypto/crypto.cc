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

#include <string.h>

#include <openssl/evp.h>

#include <stdexcept>

#include "crypto.hh"

namespace common
{

bool Crypto::isMessageDigestAvailable(const std::string &mdName)
{
    initDigests();
    const EVP_MD *md;
    md = EVP_get_digestbyname(mdName.c_str());
    return md != nullptr;
}

std::string Crypto::messageDigest(const std::string &message,
                                  const std::string &mdName)
{
    initDigests();
    unsigned int md_len, i;
    unsigned char md_value[100];
    char output[35];

    const EVP_MD *md = EVP_get_digestbyname(mdName.c_str());
    if (!md)
    {
        throw std::runtime_error("The '" + mdName + "' digest is not available.");
    }

#if OPENSSL_VERSION_NUMBER < 0x10100000L
    EVP_MD_CTX mdctx;
    EVP_DigestInit(&mdctx, md);
    EVP_DigestUpdate(&mdctx, message.c_str(), strlen(message.c_str()));
    EVP_DigestFinal(&mdctx, md_value, &md_len);
#else
    EVP_MD_CTX *mdctx = EVP_MD_CTX_new();
    EVP_DigestInit(mdctx, md);
    EVP_DigestUpdate(mdctx, message.c_str(), strlen(message.c_str()));
    EVP_DigestFinal(mdctx, md_value, &md_len);
    EVP_MD_CTX_free(mdctx);
#endif

    for (i = 0; i < md_len; i++)
    {
        sprintf(output + i + i, "%02x", md_value[i]);
    }

    return std::string(output);
}

void Crypto::initDigests()
{
#if OPENSSL_VERSION_NUMBER < 0x10100000L
    // OpenSSL_add_all_digests() needs to be executed only once:
    // static variable with block scope is a clean and fast way to do this
    static bool init = []()
    {
        OpenSSL_add_all_digests();
        return true;
    }();
#else
    // nothing to do in newer openssl
#endif
}
}  // namespace common
