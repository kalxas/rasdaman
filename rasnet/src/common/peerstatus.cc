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

/* SOURCE: PeerStatus.hh
 *
 * CLASS:  PeerStatus
 *
 *
 * COMMENTS:
 *        This class is used to maintain information about the state of a network peer.
 *
 *
 */
#include <stdexcept>

#include "peerstatus.hh"

namespace rasnet
{
PeerStatus::PeerStatus(boost::int32_t retries, boost::int32_t period) :
    timer(period)
{
    if(retries<0 || period < 0)
    {
        throw std::runtime_error("The number of retries and the period between retries must be positive.");
    }

    this->retries = retries;
    this->retriesBackup = retries;
}

bool PeerStatus::isAlive()
{
    return this->retries > 0;
}

bool PeerStatus::decreaseLiveliness()
{
    //TODO:Reduce liveliness by the number of milliseconds passed divided by the period
    if (this->timer.hasExpired())
    {
        if (this->retries > 0)
        {
            this->retries--;
            this->timer.reset();
        }
        return true;
    }
    else
    {
        return false;
    }
}

void PeerStatus::reset()
{
    this->retries = this->retriesBackup;
    this->timer.reset();

}

PeerStatus::~PeerStatus()
{}
}
