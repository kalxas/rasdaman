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

#include <sys/time.h>
#include <unistd.h>
#include <time.h>
#include "testing.h">

bool Test::test_result_ = 0;
int Test::tests_run_ = 0;
int Test::tests_passed_ = 0;
int Test::timer_sec_ = 0;
int Test::timer_usec_ = 0;
ostream& Test::log_ = cout;

void Test::startTimer()
{
    struct timeval tv;
    // Allow the application to build-up a time slice.
    sleep(1);
    gettimeofday(&tv, NULL);
    Test::timer_sec_ = tv.tv_sec;
    Test::timer_usec_ = tv.tv_usec;
}

double Test::stopTimer()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (tv.tv_sec - Test::timer_sec_) * 1.0 + (tv.tv_usec -
            Test::timer_usec_) * static_cast<double>(0.0000001);
}

int Test::getResult()
{
    LOG << tests_passed_ << "/" << tests_run_ << " tests passed!" << endl;
    return (tests_passed_ != tests_run_);
}
