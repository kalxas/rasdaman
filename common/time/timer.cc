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

#include <stdexcept>

#include <boost/cstdlib.hpp>

#include "../exceptions/invalidargumentexception.hh"
#include "timer.hh"

namespace common
{
Timer::Timer(boost::int32_t periodArg)
{
  if (periodArg < 0)
  {
      throw InvalidArgumentException("period");
  }

  gettimeofday(&this->start, NULL);
  gettimeofday(&this->current, NULL);
  timerclear(&this->timeout);
  //Get the number of full seconds
  this->timeout.tv_sec = periodArg / 1000;
  //Get the number of microseconds
  this->timeout.tv_usec = (periodArg % 1000) * 1000;
  timeradd(&this->start, &this->timeout, &this->end);
}

Timer::~Timer()
{}

boost::int32_t Timer::getPeriod() const
{
  return this->period;
}

bool Timer::hasExpired()
{
  gettimeofday(&this->current, NULL);
  return timercmp(&this->current, &this->end, >);
}

void Timer::reset()
{
  gettimeofday(&this->start, NULL);
  gettimeofday(&this->current, NULL);
  timeradd(&this->start, &this->timeout, &this->end);
}

}
