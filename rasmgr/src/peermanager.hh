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

#ifndef RASMGR_X_SRC_PEERMANAGER_HH_
#define RASMGR_X_SRC_PEERMANAGER_HH_

#include "clientserverrequest.hh"
#include "clientserversession.hh"
#include "remoteclientsession.hh"

#include "rasmgr/src/messages/rasmgrmess.pb.h"

#include <list>
#include <map>
#include <functional>
#include <memory>
#include <mutex>

namespace rasmgr
{
class InPeer;
class OutPeer;

/**
 * Allow adding/removing inpeers/outpeers.
 */
class PeerManager
{
public:
    PeerManager() = default;

    virtual ~PeerManager() = default;

    /**
     * Create a new InPeer and insert it into the list of 
     * InPeers if a InPeer with the same time doesn't exist.
     * @param peerHostName The name of the host on which the peer is running
     * @throws An exception is thrown if an InPeer with the same name exists
     */
    virtual void defineInPeer(const std::string &peerHostName);

    /**
     * Remove the InPeer with the given host name from the list of InPeers.
     * @param peerHostName Name of the host on which the rasmgr is running
     * @throws An exception is thrown if there is no InPeer with the given host.
     */
    virtual void removeInPeer(const std::string &peerHostName);

    /**
     * Create a new OutPeer and insert it into the list of 
     * OutPeers if a OutPeer with the same time doesn't exist.
     * @param peerHostName The name of the host on which the peer is running
     * @param port the port on which the peer is listening
     * @throws An exception is thrown if an InPeer with the same name exists
     */
    virtual void defineOutPeer(const std::string &peerHostName, const std::uint32_t port);

    /**
     * Remove the OutPeer with the given host name from the list of OutPeers.
     * @param peerHostName Name of the host on which the rasmgr is running
     * @throws An exception is thrown if there is no OutPeer with the given host.
     */
    virtual void removeOutPeer(const std::string &peerHostName);

    /**
     * Try to acquire a remote server for a client.
     * @param request credentials/dbname for the remote server connection
     * @param out_reply Struct containing information identifying the connection
     * @return TRUE if a server was found, FALSE otherwise.
     */
    virtual bool tryGetRemoteServer(const ClientServerRequest &request,
                                    ClientServerSession &out_reply);

    /**
     * Check if this is a remote client session.
     * @return TRUE if we have a remote client session, FALSE otherwise
     */
    virtual bool isRemoteClientSession(const RemoteClientSession &clientSession);

    /**
     * Release the acquired server.
     */
    virtual void releaseServer(const RemoteClientSession &clientSession);

    /**
     * Serialize the data contained by this object into a format which can be
     * later used for presenting information to the user or saved to disk.
     */
    virtual PeerMgrProto serializeToProto();

private:
    std::mutex mut; /*!< Mutex used to synchronize access to the resources managed by this class.*/

    std::list<std::shared_ptr<InPeer>> inPeers;
    std::list<std::shared_ptr<OutPeer>> outPeers;

    std::map<std::string, std::shared_ptr<OutPeer>> remoteSessions; /*!< Mapping between a string identifying a remote session and the peer on which the remote session is active*/

    /**
     * @brief remoteClientSessionToString Generate a string uniquely identifying 
     * a remote session from a RemoteClientSession struct.
     */
    std::string remoteClientSessionToString(const RemoteClientSession &clientSession);
};
}  // namespace rasmgr

#endif  // PEERMANAGER_HH
