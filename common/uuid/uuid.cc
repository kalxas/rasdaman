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

 * SOURCE: UUID.hh
 *
 * MODULE: util
 * CLASS:  UUID
 *
 *
 * COMMENTS:
 *        The UUID class is used to generate a string UUID using the UNIX uuid library.
 *
 */

#include <boost/lexical_cast.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <ctime>

#include "uuid.hh"

namespace common {

boost::uuids::random_generator UUID::generator;

boost::mutex UUID::generatorMutex;
std::string UUID::generateUUID()
{
    boost::unique_lock<boost::mutex> lock(UUID::generatorMutex);
    return boost::lexical_cast<std::string>(UUID::generator());
}

const size_t UUID::UUID_LENGTH = 36;

std::string UUID::generate() {
  auto uuid = generator();
  return boost::lexical_cast<std::string>(uuid);
}

int UUID::generateIntId()
{
    static int counter = 0;
    int timeNow = time(NULL);
    int result = (timeNow & 0xFFFFFF) + (counter << 24);
    counter = (counter + 1) & 0x7F;

    return  result;
}

}
