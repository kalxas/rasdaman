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
/**
 * SOURCE: clientcomm.cc
 *
 * MODULE: clientcomm
 * CLASS:  ClientComm
 *
 * COMMENTS:
 *      None
*/

static const char rcsid[] = "@(#)clientcomm, ClientComm: $Id: clientcomm.cc,v 1.145 2006/01/03 21:39:21 rasdev Exp $";

using namespace std;

#include "config.h"
#include "clientcomm/rpcclientcomm.hh"
#include "rnprotocol/rnpclientcomm.hh"

#ifdef RMANRASNET
#include "rasnetprotocol/rasnetclientcomm.hh"
#endif


#include "raslib/endian.hh"
#include "clientcomm.hh"

int
ClientComm::changeEndianness(r_GMarray* mdd, const r_Base_Type* bt)
{
    const r_Base_Type* baseType;
    const r_Minterval& interv = mdd->spatial_domain();

    baseType = (bt == NULL) ? mdd->get_base_type_schema() : bt;

    if (baseType == NULL)
    {
        cerr << "ClientComm::changeEndianness: No base type information!" << endl;
        return 0;
    }

    r_Endian::swap_array(baseType, interv, interv, mdd->get_array(), mdd->get_array());

    return 1;
}


int
ClientComm::changeEndianness(const r_GMarray* mdd, void* newMdd, const r_Base_Type* bt)
{
    const r_Base_Type* baseType;
    const r_Minterval& interv = mdd->spatial_domain();

    // Get the base type...
    baseType = (bt == NULL) ? (const_cast<r_GMarray*>(mdd))->get_base_type_schema() : bt;

    if (baseType == NULL)
    {
        cerr << "ClientComm::changeEndianness: No base type information!" << endl;
        memcpy(newMdd, mdd->get_array(), mdd->get_array_size());
        return 0;
    }

    r_Endian::swap_array(baseType, interv, interv, mdd->get_array(), newMdd);

    return 1;
}

ClientComm::ClientComm()
{

}

ClientComm* ClientComm::createObject(const char* rasmgrName, int rasmgrPort)
{
    char* env = getenv("RMANPROTOCOL");

    CommunicationProtocol protocol = DEFAULT_PROTOCOL;

    if (env != 0)
    {
        if (strcmp(env, "RNP") == 0 || strcmp(env, "HTTP") == 0)
        {
            protocol = RNP;
        }
        if (strcmp(env, "RPC") == 0 || strcmp(env, "COMPAT") == 0)
        {
            protocol = RPC;
        }
        if (strcmp(env, "RASNET") == 0)
        {
            protocol = RASNET;
        }
        // rest is ignored
    }

    switch (protocol)
    {
    case RNP:
        return new RnpClientComm(rasmgrName, rasmgrPort);
        break;
    case RPC:
        return new RpcClientComm(rasmgrName, rasmgrPort);
        break;
    case RASNET:
#ifdef RMANRASNET
        return new RasnetClientComm(rasmgrName, rasmgrPort);
        break;
#endif
    default:
        return new RnpClientComm(rasmgrName, rasmgrPort);
    }
}

ClientComm::~ClientComm()
{
}

// default comm protocol to be used:
//  true    use RNP
//  false   use RPC
#ifdef RMANRASNET
ClientComm::CommunicationProtocol ClientComm::DEFAULT_PROTOCOL = ClientComm::RASNET;
#else
ClientComm::CommunicationProtocol ClientComm::DEFAULT_PROTOCOL = ClientComm::RNP;
#endif

void ClientComm::useRNP()
{
    DEFAULT_PROTOCOL = RNP;
}

void ClientComm::useRPC()
{
    DEFAULT_PROTOCOL = RPC;
}

void ClientComm::useRASNET()
{
    DEFAULT_PROTOCOL = RASNET;
}

bool ClientComm::internalSettingIsRNP()
{
    return DEFAULT_PROTOCOL == RNP;
}

void ClientComm::setTransaction(r_Transaction* transaction)
{
    this->transaction = transaction;
}

void ClientComm::setDatabase(r_Database* database)
{
    this->database = database;
}

void ClientComm::updateTransaction()
{
    if (!transaction)
        transaction = r_Transaction::actual_transaction;
    if (!database && transaction)
        database = transaction->getDatabase();
    if (!database)
        database = r_Database::actual_database;
}
