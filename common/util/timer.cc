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

#include "timer.hh"
#include "common/time/date.h"
#include <chrono>
#include <ctime>
#include <string>

namespace common
{

std::string common::TimerUtil::getCurrentDateTime()
{
    using system_clock = std::chrono::system_clock;
    auto currTime = system_clock::to_time_t(system_clock::now());
    char buf[80];
    auto tstruct = *localtime(&currTime);
    strftime(buf, sizeof(buf), "%Y-%m-%d %X", &tstruct);
    return std::string(buf);
}

std::string TimerUtil::getCurrentDateTimeUTC()
{
    /// Uses MIT-licenced lib: https://howardhinnant.github.io/date/date.html
    return date::format("%F %T", std::chrono::system_clock::now());
}

uintmax_t TimerUtil::getSecondsSinceEpoch()
{
    auto result = time(NULL);
    return uintmax_t(result);
}

}  // namespace common
