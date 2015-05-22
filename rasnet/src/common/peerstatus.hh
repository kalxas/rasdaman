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

#ifndef RASNET_SRC_COMMON_PEERSTATUS_HH
#define RASNET_SRC_COMMON_PEERSTATUS_HH

#include "../../../common/src/time/timer.hh"
namespace rasnet {
/**
 * @brief The PeerStatus class
 *  This class is used to maintain information about the state of a network peer.
 */
class PeerStatus {
public:
    /**
     * Initialize the PeerStatus object with a number of attempts to detect if the peer is alive
     * and a period after which, if a signal is not received from the peer, the number of retries is decreased.
     * @param retries The number of times we try to determine if the peer is alive
     * @param period The period in milliseconds after which, if a signal is not received, we decrease the number of retries
     */
    PeerStatus(boost::int32_t retries, boost::int32_t period);
    virtual ~PeerStatus();

    boost::int32_t getRetries() const;
    boost::int32_t getPeriod() const;

    /**
     * Check if the peer is alive.
     * @return true if the peer is alive i.e. the number of retries has not reached 0, false otherwise
     */
    virtual bool isAlive();

    /**
     * Decrease the number of retries by 1 if a time larger than the period
     * has passed from the last signal from the peer.
     * @return true if the number of retries has been decremented, false otherwise
     */
    virtual bool decreaseLiveliness();

    /**
     * Reset the status of the peer .
     */
    virtual void reset();
private:
    boost::int32_t retries;/*!< The number of retries left before the peer is declared dead*/
    boost::int32_t retriesBackup;/*!< The original number of retries the peer has until it is declared dead*/
    common::Timer timer; /*!<  Timer used to keep track of the period between two signals from the peer*/
};
}

#endif /* RASNET_SRC_COMMON_PEERSTATUS_HH */
