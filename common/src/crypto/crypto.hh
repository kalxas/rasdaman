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

#ifndef COMMON_SRC_CRYPTO_CRYPTO_HH
#define COMMON_SRC_CRYPTO_CRYPTO_HH

#include <string>

namespace common
{
class Crypto
{
public:
    /**
       * @brief isMessageDigestAvailable Check if a given digest is available
       * @param mdName Name of the digest we are checking for (e.g. MD5)
       * @return TRUE if the digest is available, FALSE otherwise
       */
    static bool isMessageDigestAvailable(const std::string& mdName);

    /**
     * @brief messageDigest Encrypt the given message using the given digest.
     * @param message Message to encrypt
     * @param mdName Digest to use
     * @return Encrypted message
     * @throws std::exception
     */
    static std::string messageDigest(const std::string& message, const std::string& mdName);
};
}

#endif // COMMON_SRC_CRYPTO_CRYPTO_HH
