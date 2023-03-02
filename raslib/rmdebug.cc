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
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/**
 * SOURCE: rmdebug.cc
 *
 * MODULE: raslib
 * CLASS:
 *
 * COMMENTS:
 *
*/

#include "raslib/rmdebug.hh"
#include <logging.hh>

using namespace std;

inline RMTimer::RMTimer(const char *newClass, const char *newFunc, int newBmLevel)
    : myClass(newClass), myFunc(newFunc), bmLevel(newBmLevel), running(0)
{
    start();
}

inline RMTimer::~RMTimer()
{
    stop();
}

void RMTimer::setOutput(int newOutput)
{
    output = newOutput;
}

void RMTimer::start()
{
    // reset accu
    accuTime = 0;
    // set output to TRUE
    output = 1;
    resume();
}

void RMTimer::pause()
{
    if (running)
    {
        fetchTime();
        // timer is not running
        running = 0;
    }
}

void RMTimer::resume()
{
    static struct timezone dummy;
    gettimeofday(&acttime, &dummy);
    // timer is running
    running = 1;
}

void RMTimer::stop()
{
    pause();

    if (output)
    {
        LDEBUG << "\nPerformanceTimer: " << myClass << " :: " << myFunc << " = "
               << accuTime << " usecs";
        // set output to FALSE
        output = 0;
    }
}

int RMTimer::getTime()
{
    fetchTime();
    return static_cast<int>(accuTime);
}

void RMTimer::fetchTime()
{
    // save start time
    oldsec = acttime.tv_sec;
    oldusec = acttime.tv_usec;

    // get stop time
    static struct timezone dummy;
    gettimeofday(&acttime, &dummy);

    // add new time to accu
    accuTime += (acttime.tv_sec - oldsec) * 1000000 + acttime.tv_usec - oldusec;
}
