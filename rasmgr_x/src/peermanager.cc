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


#include "../../common/src/exceptions/rasexceptions.hh"

#include "exceptions/rasmgrexceptions.hh"

#include "inpeer.hh"
#include "outpeer.hh"

#include "peermanager.hh"

namespace rasmgr
{

using std::list;
using std::string;
using boost::shared_ptr;
using boost::mutex;
using boost::unique_lock;

using common::InvalidArgumentException;
using common::ResourceBusyException;

PeerManager::PeerManager()
{}

PeerManager::~PeerManager()
{}

void PeerManager::defineInPeer(const std::string &peerHostName)
{
    if(peerHostName.empty())
    {
        throw InvalidArgumentException("Invalid peer host name.");
    }

    unique_lock<mutex> lock ( this->mut );

    // Check for duplicates
    for(const auto& inPeer: this->inPeers)
    {
        if(inPeer->getHostName() == peerHostName)
        {
            throw InPeerAlreadyExistsException(peerHostName);
        }
    }

    // Create the peer
    boost::shared_ptr<InPeer> peer = boost::make_shared<InPeer>(peerHostName);
    this->inPeers.push_back(peer);
}

void PeerManager::removeInPeer(const std::string &peerHostName)
{
    if(peerHostName.empty())
    {
        throw InvalidArgumentException("Invalid peer host name.");
    }

    unique_lock<mutex> lock ( this->mut );

    bool removed = false;
    for(auto inPeer = this->inPeers.begin(); inPeer!= this->inPeers.end(); ++inPeer)
    {
        if((*inPeer)->getHostName() == peerHostName)
        {
            this->inPeers.erase(inPeer);
            removed = true;

            break;
        }
    }

    if(!removed)
    {
        throw InexistentInPeerException(peerHostName);
    }
}

void PeerManager::defineOutPeer(const std::string &peerHostName, const boost::uint32_t port)
{
    if(peerHostName.empty())
    {
        throw InvalidArgumentException("Invalid peer host name.");
    }

    unique_lock<mutex> lock ( this->mut );

    // Check for duplicates
    for(const auto& outPeer: this->outPeers)
    {
        if(outPeer->getHostName() == peerHostName)
        {
            throw OutPeerAlreadyExistsException(peerHostName, port);
        }
    }

    // Create the peer
    boost::shared_ptr<OutPeer> peer = boost::make_shared<OutPeer>(peerHostName, port);
    this->outPeers.push_back(peer);
}

void PeerManager::removeOutPeer(const std::string &peerHostName)
{
    if(peerHostName.empty())
    {
        throw InvalidArgumentException("Invalid peer host name.");
    }

    unique_lock<mutex> lock ( this->mut );

    bool removed = false;
    for(auto outPeer = this->outPeers.begin(); outPeer!= this->outPeers.end(); ++outPeer)
    {
        if((*outPeer)->getHostName() == peerHostName)
        {
            // If the peer is busy, throw an exception
            if((*outPeer)->isBusy())
            {
                throw ResourceBusyException("The peer has active client sessions.");
            }

            this->outPeers.erase(outPeer);
            removed = true;

            break;
        }
    }

    if(!removed)
    {
        throw InexistentOutPeerException(peerHostName);
    }
}

bool PeerManager::tryGetRemoteServer(const ClientServerRequest &request, ClientServerSession &out_reply)
{
    unique_lock<mutex> lock ( this->mut );

    for(const auto& outpeer:this->outPeers)
    {
        if(outpeer->tryGetRemoteServer(request, out_reply))
        {
            RemoteClientSession clientSession(out_reply.clientSessionId, out_reply.dbSessionId);

            std::string remoteSessionId = this->remoteClientSessionToString(clientSession);
            this->remoteSessions[remoteSessionId] = outpeer;

            return true;
        }
    }

    return false;
}

bool PeerManager::isRemoteClientSession(const RemoteClientSession &clientSession)
{
    unique_lock<mutex> lock ( this->mut );

    string sessionKey = this->remoteClientSessionToString(clientSession);

    return this->remoteSessions.find(sessionKey)!=this->remoteSessions.end();
}

void PeerManager::releaseServer(const RemoteClientSession &clientSession)
{
    unique_lock<mutex> lock ( this->mut );

    string sessionKey = this->remoteClientSessionToString(clientSession);

    auto session = this->remoteSessions.find(sessionKey);
    if(session!=this->remoteSessions.end())
    {
        session->second->releaseServer(clientSession);
        this->remoteSessions.erase(session);
    }
}

PeerMgrProto PeerManager::serializeToProto()
{
    unique_lock<mutex> lock ( this->mut );

    PeerMgrProto result;

    for(const auto& outPeer: this->outPeers)
    {
        OutPeerProto outPeerProto;
        outPeerProto.set_host_name(outPeer->getHostName());
        outPeerProto.set_port(outPeer->getPort());

        result.add_outpeers()->CopyFrom(outPeerProto);
    }

    for(const auto& inPeer: this->inPeers)
    {
        InPeerProto inPeerProto;
        inPeerProto.set_host_name(inPeer->getHostName());

        result.add_inpeers()->CopyFrom(inPeerProto);
    }

    return result;
}

std::string PeerManager::remoteClientSessionToString(const RemoteClientSession &clientSession)
{
    return clientSession.getClientSessionId()
           +":"+clientSession.getDbSessionId();
}
}
