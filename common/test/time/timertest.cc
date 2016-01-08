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

#include <boost/cstdint.hpp>

#include "../../src/unittest/gtest.h"
#include "../../src/time/timer.hh"

namespace common
{
namespace test
{

static int32_t period = 300;

class TimerTest : public ::testing::Test
{
protected:
    TimerTest() :
        timer(period)
    {
    }

    Timer timer;
};

TEST_F(TimerTest, CheckIfTimerIsNotExpiredWhenTimeHasNotPassed)
{
    ASSERT_FALSE(timer.hasExpired());
}

TEST_F(TimerTest, SleepUntilTimerExpires)
{
    //Test if the timer has expired
    usleep((uint32_t)period * 1000u);
    ASSERT_TRUE(timer.hasExpired());
}

TEST_F(TimerTest, SleepUntilTimerExpiresAndReset)
{
    //Test if the timer has expired
    usleep((uint32_t)period * 1000u);
    timer.reset();
    ASSERT_FALSE(timer.hasExpired());
}
}
}
