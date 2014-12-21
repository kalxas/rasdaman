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
*  Implementation file for the stringutils namespace. See the namespace
* description for more details.
*
* BUGS:
*
* AUTHORS:
*         Alex Dumitru <alex@flanche.net>
*         Vlad Merticariu <vlad@flanche.net>
*
************************************************************/

#include <algorithm>
#include <functional>
#include <cctype>
#include <locale>
#include <string>
#include <sstream>
#include <vector>
#include "stringutils.hh"

using std::string;
using std::vector;

/**
* Contains utility functions that are not included in stdlib for dealing
* with strings
*/
namespace stringutils {

    string trimLeft(const string &str) {
        string s = string(str);
        s.erase(s.begin(), std::find_if(s.begin(), s.end(), std::not1(std::ptr_fun<int, int>(std::isspace))));
        return s;
    }

    std::string trimRight(const string &str) {
        string s = string(str);
        s.erase(std::find_if(s.rbegin(), s.rend(), std::not1(std::ptr_fun<int, int>(std::isspace))).base(), s.end());
        return s;
    }

    std::string trim(const string &str) {
        string s = string(str);
        return trimLeft(trimRight(s));
    }

    int stringToInt(const std::string &str) {
        int Result; //number which will contain the result
        std::stringstream convert(str); // stringstream used for the conversion initialized with the contents of Text

        if (!(convert >> Result)) {//give the value to Result using the characters in the string
            Result = 0; //if that fails set Result to 0
        }
        return Result;
    }

    string intToString(const int number) {
        std::stringstream stream;
        stream << number;
        string convertedString = stream.str();
        return convertedString;
    }

    string getRandomAlphaNumString(const int length) {
        srand(time(NULL));
        char *s = (char *) malloc(length);
        static const char alphanum[] =
                "0123456789"
                        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                        "abcdefghijklmnopqrstuvwxyz";

        for (int i = 0; i < length; ++i) {
            s[i] = alphanum[rand() % (sizeof(alphanum) - 1)];
        }

        s[length] = 0;
        return std::string(s);
    }

    void explode(std::string containserStr, const std::string &delimitor, std::vector<std::string> &results) {
        int found;
        found = containserStr.find_first_of(delimitor);
        while (found != std::string::npos) {
            if (found > 0) {
                results.push_back(containserStr.substr(0, found));
            }
            containserStr = containserStr.substr(found + 1);
            found = containserStr.find_first_of(delimitor);
        }
        if (containserStr.length() > 0) {
            results.push_back(containserStr);
        }
    }

    std::string capitalize(const std::string &str) {
        std::string newStr = std::string(str);
        newStr[0] = toupper(newStr[0]);
        return newStr;
    }

}
