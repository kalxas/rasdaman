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

#ifndef COMMON_SRC_TIME_TIMER_HH_
#define COMMON_SRC_TIME_TIMER_HH_

#include <sys/time.h>
#include <boost/cstdint.hpp>
namespace common
{
class Timer
{
public:
    /**
    * Create a Timer object with a given lifetime. The timer automatically starts ticking.
    * @param period The number of milliseconds until the timer will expire.
    * @throws std::runtime_error is thrown if the period is nevative.
    */
    Timer(boost::int32_t period);

    /**
    * Check if the timer has expired.
    * @return true if the period of time passed to the constructor has passed since the creation
    * of the object or since the last reset.
    */
    bool hasExpired();

    /**
    * Reset the timer. The timer will start counting down from the initial period passed to the constructor.
    */
    void reset();

    virtual ~Timer();

private:
    struct timeval start;
    /*!< Timeval representing the time the Timer started */
    struct timeval end;
    /*!< Timeval representing the time when the Timer will expire*/
    struct timeval current;
    /*!< Timeval used to get the current time when checking for expiration*/
    struct timeval timeout;/*!< Timeval representing the period the Timer measures*/
};
}
#endif /* COMMON_TIME_TIMER_HH_ */
