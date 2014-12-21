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
/***********************************************************
*
* PURPOSE:
*  Header file for the stringutils namespace. See the namespace
* description for more details.
*
* BUGS:
*
* AUTHORS:
*         Alex Dumitru <alex@flanche.net>
*         Vlad Merticariu <vlad@flanche.net>
*
************************************************************/

#ifndef STRINGUTILS_HH
#define    STRINGUTILS_HH

#include <algorithm>
#include <functional>
#include <cctype>
#include <locale>
#include <string>
#include <sstream>
#include <vector>


/**
* Contains utility functions that are not included in stdlib for dealing
* with strings
*/
namespace stringutils {

/**
* Trims all space characters from the left of the string
* @param s the string to be trimmed
* @return the trimmed string
*/
    std::string trimLeft(const std::string &str);

/**
* Trims all space characters from the right of the string
* @param s the string to be trimmed
* @return the trimmed string
*/
    std::string trimRight(const std::string &str);

/**
* Trims all space characters from both left and right of the string.
* @param s the string to be trimmed
* @return the trimmed string
*/
    std::string trim(const std::string &str);

/**
* Converts a string to an integer. Defaults to 0 if the string is incorrect;
* @param str
* @return integer containing the result
*/
    int stringToInt(const std::string &str);

/**
* Transforms an integer into a std::string
* @param number - the integer(int) you want to convert
* @return the string representation
*/
    std::string intToString(const int number);


/**
* Generate a random alphanumeric string
* @param length the length of the string
* @return the generated string
*/
    std::string getRandomAlphaNumString(const int length);


/**
* Splits a string into pieces based on a separator
* @param containerStr the string to be split
* @param delimitor the separator to split on
* @param results a container in which the results can be deposited
*/
    void explode(std::string containserStr, const std::string &delimitor, std::vector<std::string> &results);

/**
* Capitalizes the string, i.e. makes first letter uppercase
* @param str the string to capitalize
* @return
*/
    std::string capitalize(const std::string &str);

}//end stringutils;


#endif	/* STRINGUTILS_HH */
