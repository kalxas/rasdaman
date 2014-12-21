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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef RASCONTROL_X_SRC_RASCONTROLCONSTANTS_HH
#define RASCONTROL_X_SRC_RASCONTROLCONSTANTS_HH

namespace rascontrol
{
const int MAXMSG = 2048; /*!< Maximum length of a rascontrol message. */

const int MAX_USERNAME_LENGTH=100; /*!< Maximum length of a user name */

const int MAX_USERPASS_LENGTH=100; /*!< Maximum length of a user password */

const char EOS_CHAR ='\0'; /*!< Constant representing the end of a string */
}

#endif // RASCONTROL_X_SRC_RASCONTROLCONSTANTS_HH
