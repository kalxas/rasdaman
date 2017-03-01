#include "mymalloc/mymalloc.h"
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
/*************************************************************
 *
 *
 * PURPOSE
 * Contains the functions which really communicate with the server
 *
 *
 * COMMENTS:
 * - return values & their meaning see servercomm.hh
 *
 ************************************************************/

#include "config.h"
#include <openssl/evp.h>

#include "rnpclientcomm.hh"

#include "rasodmg/transaction.hh"
#include "rasodmg/database.hh"
#include "rasodmg/iterator.hh"
#include "rasodmg/set.hh"
#include "rasodmg/ref.hh"
#include "rasodmg/storagelayout.hh"
#include "rasodmg/tiling.hh"

#include "raslib/minterval.hh"
#include "raslib/primitivetype.hh"
#include "raslib/complextype.hh"
#include "raslib/structuretype.hh"
#include "raslib/primitive.hh"
#include "raslib/complex.hh"
#include "raslib/structure.hh"
#include "raslib/endian.hh"
#include "raslib/parseparams.hh"

#include <easylogging++.h>

#include "debug.hh"

void RnpClientComm::setTurbo(bool turbo)
{
    useTurbo = turbo;
}

void RnpClientComm::executeConnect()
{
    startRequest(RnpRasserver::cmd_connect);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, 0);
    encoder.addStringParameter(RnpRasserver::pmt_capability, capability);
    LDEBUG << "request RnpRasserver::cmd_connect with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    clientID = decoder.getDataAsInteger();
    LDEBUG << "executeConnect: Connected, clientID 0x" << hex << clientID << dec ;

    endianServer = decoder.getDesiredEndianness() == Rnp::bigEndian ? 0 : 1;
    endianClient = Rnp::detectHostEndianness()    == Rnp::bigEndian ? 0 : 1;

    clearAnswer();
}

void RnpClientComm::executeDisconnect()
{
    startRequest(RnpRasserver::cmd_disconnect);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_disconnect with clientID 0x" << hex << clientID << dec;

    clientID = -1;
    LDEBUG << "clientID now set to 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    clearAnswer();
}

void RnpClientComm::executeOpenDB(const char* lDatabaseName)
{
    startRequest(RnpRasserver::cmd_opendb);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_dbname, lDatabaseName);
    LDEBUG << "request RnpRasserver::cmd_opendb '" << lDatabaseName << "', with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    clearAnswer();
}

void RnpClientComm::executeCloseDB()
{
    startRequest(RnpRasserver::cmd_closedb);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_closedb with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    clearAnswer();
}

void RnpClientComm::executeBeginTA(bool rw)
{
    startRequest(RnpRasserver::cmd_beginta);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addInt32Parameter(RnpRasserver::pmt_accesmode, rw);
    LDEBUG << "request RnpRasserver::cmd_beginta with rw=" << rw << ", clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    clearAnswer();
}

void RnpClientComm::executeCommitTA()
{
    startRequest(RnpRasserver::cmd_committa);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_committa with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    clearAnswer();
}

void RnpClientComm::executeAbortTA()
{
    startRequest(RnpRasserver::cmd_abortta);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_abortta with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    clearAnswer();
}

void RnpClientComm::turboOpenDB(const char* lDatabaseName)
{
    clientID = 0;

    startRequest(RnpRasserver::cmd_connect);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, 0);   // = always clientID -- PB
    encoder.addStringParameter(RnpRasserver::pmt_capability, capability);
    encoder.endFragment();
    LDEBUG << "request RnpRasserver::cmd_connect with clientID 0x0, capability '" << capability << "'";

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_opendb);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_dbname, lDatabaseName);
    encoder.endFragment();
    LDEBUG << "adding fragment RnpRasserver::cmd_opendb with db '" << lDatabaseName << "', clientID 0x" << hex << clientID << dec;

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_closedb);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.endFragment();
    LDEBUG << "adding fragment RnpRasserver::cmd_closedb with clientID 0x" << hex << clientID << dec;

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_disconnect);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "adding fragment RnpRasserver::cmd_disconnect with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    //clientID = decoder.getDataAsInteger();

    clientID = -1; // we will disconnect so we force it here
    LDEBUG << "RnpClientComm::turboOpenDB(): clientID set to 0x" << hex << clientID << dec;

    endianServer = decoder.getDesiredEndianness() == Rnp::bigEndian ? 0 : 1;
    endianClient = Rnp::detectHostEndianness()    == Rnp::bigEndian ? 0 : 1;

    // open
    decoder.getNextFragment();
    detectErrors();
    // close
    decoder.getNextFragment();
    detectErrors();
    // disconnect
    decoder.getNextFragment();
    detectErrors();

    clearAnswer();
}

void RnpClientComm::turboBeginTA(bool rw)
{
    clientID = 0;

    startRequest(RnpRasserver::cmd_connect);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, 0);
    encoder.addStringParameter(RnpRasserver::pmt_capability, capability);
    encoder.endFragment();
    LDEBUG << "request RnpRasserver::cmd_connect with clientID 0x" << hex << clientID << dec << ", capability '" << capability << "'";

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_opendb);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_dbname, databaseName);
    encoder.endFragment();
    LDEBUG << "adding fragment RnpRasserver::cmd_opendb '" << databaseName << "', with clientID 0x" << hex << clientID << dec;

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_beginta);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addInt32Parameter(RnpRasserver::pmt_accesmode, rw);
    LDEBUG << "adding fragment RnpRasserver::cmd_beginta with rw=" << rw << ", clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    clientID = decoder.getDataAsInteger();
    LDEBUG << "rceived clientID 0x" << hex << clientID << dec;
    endianServer = decoder.getDesiredEndianness() == Rnp::bigEndian ? 0 : 1;
    endianClient = Rnp::detectHostEndianness()    == Rnp::bigEndian ? 0 : 1;

    // open
    decoder.getNextFragment();
    detectErrors();

    // beginTA
    decoder.getNextFragment();
    detectErrors();

    clearAnswer();
}

void RnpClientComm::turboCommitTA()
{
    startRequest(RnpRasserver::cmd_committa);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.endFragment();
    LDEBUG << "request RnpRasserver::cmd_committa with clientID 0x" << hex << clientID << dec;

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_closedb);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.endFragment();
    LDEBUG << "adding fragment RnpRasserver::cmd_closedb with clientID 0x" << hex << clientID << dec;

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_disconnect);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "adding fragment RnpRasserver::cmd_disconnect with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    // close
    decoder.getNextFragment();
    detectErrors();

    // disconnect
    decoder.getNextFragment();
    detectErrors();

    clientID = -1;
    LDEBUG << "resetting: clientID 0x" << hex << clientID << dec;
    clearAnswer();
}
void RnpClientComm::turboAbortTA()
{
    startRequest(RnpRasserver::cmd_abortta);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.endFragment();
    LDEBUG << "request RnpRasserver::cmd_abortta with clientID 0x" << hex << clientID << dec;

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_closedb);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.endFragment();
    LDEBUG << "adding fragment RnpRasserver::cmd_closedb with clientID 0x" << hex << clientID << dec;

    encoder.startFragment(Rnp::fgt_Command, RnpRasserver::cmd_disconnect);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "adding fragment RnpRasserver::cmd_disconnect with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    // close
    decoder.getNextFragment();
    detectErrors();

    // disconnect
    decoder.getNextFragment();
    detectErrors();

    clientID = -1;
    LDEBUG << "resetting: clientID 0x" << hex << clientID << dec;

    clearAnswer();
}

//---------------------------------------------------------------------------------
int RnpClientComm::executeStartInsertPersMDD(const char* collName, r_GMarray* mar)
{
    startRequest(RnpRasserver::cmd_startinsPmdd);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_collname, collName);

    char* domain = mar->spatial_domain().get_string_representation();
    encoder.addStringParameter(RnpRasserver::pmt_domain, domain);

    encoder.addInt32Parameter(RnpRasserver::pmt_typelength, static_cast<int>(mar->get_type_length()));
    encoder.addStringParameter(RnpRasserver::pmt_typename,  mar->get_type_name());
    encoder.addStringParameter(RnpRasserver::pmt_oidstring, mar->get_oid().get_string_representation());

    LDEBUG << "request RnpRasserver::cmd_startinsPmdd with collname '" << collName << "', domain " << domain << ", typelength " << mar->get_type_length() << ", typename '" << mar->get_type_name() << ", oid " << mar->get_oid().get_string_representation() << ", clientID 0x" << hex << clientID << dec;

    free(domain);

    int result = sendAndReturnStatus();

    return result;
}

// retrieval query
int RnpClientComm::executeExecuteQuery(const char* query, r_Set<r_Ref_Any>& result) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_queryrpc);
    encoder.adjustBufferSize(strlen(query));

    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_querystring, query);
    LDEBUG << "request RnpRasserver::cmd_queryrpc with query '" << query << ", clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    int status = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int errNo  = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int lineNo  = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int colNo   = decoder.getDataAsInteger();
    decoder.getNextParameter();
    const char* token  = decoder.getDataAsString();
    decoder.getNextParameter();
    const char* typeName  = decoder.getDataAsString();
    decoder.getNextParameter();
    const char* typeStructure = decoder.getDataAsString();

    if (status == 0 || status == 1)
    {
        result.set_type_by_name(typeName);
        result.set_type_structure(typeStructure);
    }
    // status == 2 - empty result

    if (status == 4 || status == 5)
    {
        r_Equery_execution_failed err(static_cast<unsigned int>(errNo), static_cast<unsigned int>(lineNo), static_cast<unsigned int>(colNo), token);
        clearAnswer();
        throw err;
    }
    clearAnswer();

    return status;
}

int RnpClientComm::executeEndTransfer()
{
    startRequest(RnpRasserver::cmd_endtransfer);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_endtransfer with clientID 0x" << hex << clientID << dec;

    int result = sendAndReturnStatus();

    return result;
}

GetElementRes* RnpClientComm::executeGetNextElement()
{
    startRequest(RnpRasserver::cmd_getnextelem);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_getnextelem with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    GetElementRes* result = new GetElementRes;
    result->data.confarray_val = NULL;

    result->status = decoder.getDataAsInteger();

    if (decoder.countParameters() == 2)
    {
        decoder.getNextParameter();
        result->data.confarray_len = static_cast<u_int>(decoder.getDataLength());
        result->data.confarray_val = new char[decoder.getDataLength()];

        memcpy(result->data.confarray_val, decoder.getData(), static_cast<size_t>(decoder.getDataLength()));

    }

    clearAnswer();

    return result;
}


int RnpClientComm::executeInsertMDD(const char* collName, r_GMarray* mar, RPCMarray* rpcMarray)
{
    int size = static_cast<int>(rpcMarray->data.confarray_len);
    startRequest(RnpRasserver::cmd_insertmdd, RNP_DEFAULTBUFFERSIZE + size);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid,      clientID);
    encoder.addStringParameter(RnpRasserver::pmt_collname,      collName);
    encoder.addStringParameter(RnpRasserver::pmt_typename,      mar->get_type_name());
    encoder.addStringParameter(RnpRasserver::pmt_oidstring,     mar->get_oid().get_string_representation());
    encoder.addStringParameter(RnpRasserver::pmt_domain,        rpcMarray->domain);
    encoder.addInt32Parameter(RnpRasserver::pmt_typelength,    rpcMarray->cellTypeLength);
    encoder.addInt32Parameter(RnpRasserver::pmt_currentformat, rpcMarray->currentFormat);
    encoder.addInt32Parameter(RnpRasserver::pmt_storageformat, rpcMarray->storageFormat);
    encoder.addOpaqueParameter(RnpRasserver::pmt_tiledata,      rpcMarray->data.confarray_val, size);
    LDEBUG << "request RnpRasserver::cmd_insertmdd with collection '" << collName << ", ..., clientID 0x" << hex << clientID << dec;

    int result = sendAndReturnStatus();

    return result;
}

int RnpClientComm::executeInsertCollection(const char* collName, const char* typeName, const r_OId& oid)
{
    startRequest(RnpRasserver::cmd_insertcoll);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_collname, collName);
    encoder.addStringParameter(RnpRasserver::pmt_typename, typeName);
    encoder.addStringParameter(RnpRasserver::pmt_oidstring, oid.get_string_representation());
    LDEBUG << "request RnpRasserver::cmd_insertcoll collection '" << collName << ", ..., with clientID 0x" << hex << clientID << dec;

    int result = sendAndReturnStatus();
    return result;
}

// common function using the dynamic parameter facility of RNP
r_Ref_Any RnpClientComm::executeGetCollByNameOrOId(const char* collName, const r_OId& oid) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_getcoll);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    if (collName != NULL)
    {
        encoder.addStringParameter(RnpRasserver::pmt_collname, collName);
    }
    else
    {
        encoder.addStringParameter(RnpRasserver::pmt_oidstring, oid.get_string_representation());
    }
    LDEBUG << "request RnpRasserver::cmd_getcoll with ..., clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    int status  =  decoder.getDataAsInteger();
    decoder.getNextParameter();
    const char* typeName      =  decoder.getDataAsString();
    decoder.getNextParameter();
    const char* typeStructure =  decoder.getDataAsString();
    decoder.getNextParameter();
    const char* oidstring     =  decoder.getDataAsString();
    decoder.getNextParameter();
    const char* collectionName =  decoder.getDataAsString();

    r_Set<r_Ref_Any>* set       = 0;

    if (status != 0 && status != 1)
    {
        r_Error err;
        switch (status)
        {
        case 2:
            err = r_Error(r_Error::r_Error_ObjectUnknown);
            break;
        case 3:
            err = r_Error(r_Error::r_Error_ClientUnknown);
            break;
        default:
            err = r_Error(r_Error::r_Error_TransferFailed);
            break;
        }
        clearAnswer();
        throw err;
    }

    // create the set
    r_OId rOId(oidstring);
    set = new(r_Database::actual_database, r_Object::read, rOId)  r_Set<r_Ref_Any>;

    // initialize data elements
    set->set_type_by_name(typeName);
    set->set_type_structure(typeStructure);
    set->set_object_name(collectionName);

    clearAnswer();

    // get collection elements
    if (status == 0)
    {
        getMDDCollection(*set, 0);
    }
    //  else rpcStatus == 1 -> Result collection is empty and nothing has to be got.

    r_Ref_Any result = r_Ref_Any(set->get_oid(), set);
    return result;
}


// common function using the dynamic parameter facility of RNP
r_Ref_Any RnpClientComm::executeGetCollOIdsByNameOrOId(const char* collName, const r_OId& oid) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_getcolloids);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    if (collName != NULL)
    {
        encoder.addStringParameter(RnpRasserver::pmt_collname, collName);
    }
    else
    {
        encoder.addStringParameter(RnpRasserver::pmt_oidstring, oid.get_string_representation());
    }
    LDEBUG << "request RnpRasserver::cmd_getcolloids with ..., clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    int status  =  decoder.getDataAsInteger();
    decoder.getNextParameter();
    const char* typeName      =  decoder.getDataAsString();
    decoder.getNextParameter();
    const char* typeStructure =  decoder.getDataAsString();
    decoder.getNextParameter();
    const char* oidstring     =  decoder.getDataAsString();
    decoder.getNextParameter();
    const char* collectionName =  decoder.getDataAsString();

    r_Set<r_Ref<r_GMarray>>* set = 0;

    if (status != 0 && status != 1)
    {
        r_Error err;
        switch (status)
        {
        case 2:
            err = r_Error(r_Error::r_Error_ObjectUnknown);
            break;
        case 3:
            err = r_Error(r_Error::r_Error_ClientUnknown);
            break;
        default:
            err = r_Error(r_Error::r_Error_TransferFailed);
            break;
        }
        clearAnswer();
        throw err;
    }

    // create the set
    r_OId rOId(oidstring);
    set = new(r_Database::actual_database, r_Object::read, rOId)  r_Set<r_Ref<r_GMarray>>;

    set->set_type_by_name(typeName);
    set->set_type_structure(typeStructure);
    set->set_object_name(collName);

    // fill set with oids
    if (status == 0)
    {
        while (decoder.getNextParameter() != 0)
        {
            r_OId roid(decoder.getDataAsString());
            set->insert_element(r_Ref<r_GMarray>(roid), 1);
        }
    }

    clearAnswer();

    r_Ref_Any result = r_Ref_Any(set->get_oid(), set);
    return result;
}


GetMDDRes* RnpClientComm::executeGetNextMDD()
{
    startRequest(RnpRasserver::cmd_getnextmdd);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_getnextmdd with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    GetMDDRes* result = new GetMDDRes;

    result->status  = decoder.getDataAsInteger();
    decoder.getNextParameter();
    result->domain        = strdup(decoder.getDataAsString());
    decoder.getNextParameter();
    result->typeName      = strdup(decoder.getDataAsString());
    decoder.getNextParameter();
    result->typeStructure = strdup(decoder.getDataAsString());
    decoder.getNextParameter();
    result->oid           = strdup(decoder.getDataAsString());
    decoder.getNextParameter();
    result->currentFormat = decoder.getDataAsInteger();

    clearAnswer();

    return result;
}

GetTileRes* RnpClientComm::executeGetNextTile()
{
    startRequest(RnpRasserver::cmd_getnexttile);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_getnexttile with clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    GetTileRes* result = new GetTileRes;
    result->marray    = new RPCMarray;

    result->status = decoder.getDataAsInteger();
    if (decoder.getNextParameter() != 0)
    {
        result->marray->domain = strdup(decoder.getDataAsString());
        decoder.getNextParameter();
        result->marray->cellTypeLength =  static_cast<u_long>(decoder.getDataAsInteger());
        decoder.getNextParameter();
        result->marray->currentFormat  =  decoder.getDataAsInteger();
        decoder.getNextParameter();
        result->marray->storageFormat  =  decoder.getDataAsInteger();
        decoder.getNextParameter();
        unsigned int length = static_cast<unsigned int>(decoder.getDataLength());
        result->marray->data.confarray_len = length;
        result->marray->data.confarray_val = static_cast<char*>(mymalloc(length));
        memcpy(result->marray->data.confarray_val, decoder.getData(), length);
    }
    else
    {
        result->marray->domain = 0;
        result->marray->data.confarray_val = 0;
    }
    clearAnswer();

    return result;
}

int RnpClientComm::executeInitUpdate()
{

    startRequest(RnpRasserver::cmd_initupdate);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "adding fragment XXX with clientID 0x" << hex << clientID << dec;

    int result = sendAndReturnStatus();
    return result;
}

int RnpClientComm::executeStartInsertTransMDD(r_GMarray* mdd)
{
    startRequest(RnpRasserver::cmd_startinsTmdd);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid,   clientID);
    encoder.addStringParameter(RnpRasserver::pmt_domain,     mdd->spatial_domain().get_string_representation());
    encoder.addInt32Parameter(RnpRasserver::pmt_typelength, static_cast<int>(mdd->get_type_length()));
    encoder.addStringParameter(RnpRasserver::pmt_typename,   mdd->get_type_name());
    LDEBUG << "request RnpRasserver::cmd_startinsTmdd with ..., clientID 0x" << hex << clientID << dec;

    int result = sendAndReturnStatus();
    return result;
}

int RnpClientComm::executeInsertTile(bool persistent, RPCMarray* tile)
{
    int size = static_cast<int>(tile->data.confarray_len);
    startRequest(RnpRasserver::cmd_inserttile, RNP_DEFAULTBUFFERSIZE + size);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid,      clientID);
    encoder.addInt32Parameter(RnpRasserver::pmt_ispersistent,  persistent ? 1 : 0);
    encoder.addStringParameter(RnpRasserver::pmt_domain,        tile->domain);
    encoder.addInt32Parameter(RnpRasserver::pmt_typelength,    tile->cellTypeLength);
    encoder.addInt32Parameter(RnpRasserver::pmt_currentformat, tile->currentFormat);
    encoder.addInt32Parameter(RnpRasserver::pmt_storageformat, tile->storageFormat);
    encoder.addOpaqueParameter(RnpRasserver::pmt_tiledata,      tile->data.confarray_val, size);
    LDEBUG << "request RnpRasserver::cmd_inserttile with ..., clientID 0x" << hex << clientID << dec;

    int result = sendAndReturnStatus();

    return result;
}

void RnpClientComm::executeEndInsertMDD(bool persistent)
{
    startRequest(RnpRasserver::cmd_endinsmdd);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addInt32Parameter(RnpRasserver::pmt_ispersistent, persistent ? 1 : 0);
    LDEBUG << "request RnpRasserver::cmd_endinsmdd with persistent " << persistent << ", clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    clearAnswer();
}

// update
int  RnpClientComm::executeExecuteUpdateQuery(const char* query) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_updaterpc);
    encoder.adjustBufferSize(strlen(query));
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_querystring, query);
    LDEBUG << "request RnpRasserver::cmd_updaterpc with query '" << query << "', clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    int status = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int errNo  = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int lineNo  = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int colNo   = decoder.getDataAsInteger();
    decoder.getNextParameter();
    std::string token  = decoder.getDataAsString();

    clearAnswer();

    if (status == 2 || status == 3)
    {
        LDEBUG << "Exception, status = " << status;
        throw r_Equery_execution_failed(static_cast<unsigned int>(errNo), static_cast<unsigned int>(lineNo), static_cast<unsigned int>(colNo), token.c_str());
    }

    if (status == 1)
    {
        LDEBUG << "Exception, status = " << status;
        throw r_Error(r_Error::r_Error_ClientUnknown);
    }

    if (status > 3)
    {
        LDEBUG << "Exception, status = " << status;
        throw r_Error(r_Error::r_Error_TransferFailed);
    }

    return status;
}

// insert
int  RnpClientComm::executeExecuteUpdateQuery(const char* query, r_Set<r_Ref_Any>& result) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_insertrpc);
    encoder.adjustBufferSize(strlen(query));
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_querystring, query);
    LDEBUG << "request RnpRasserver::cmd_insertrpc with query '" << query << "', clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();
    int status = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int errNo  = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int lineNo  = decoder.getDataAsInteger();
    decoder.getNextParameter();
    int colNo   = decoder.getDataAsInteger();
    decoder.getNextParameter();
    std::string token  = decoder.getDataAsString();
    decoder.getNextParameter();
    const char* typeName  = decoder.getDataAsString();
    decoder.getNextParameter();
    const char* typeStructure = decoder.getDataAsString();

    if (status == 0 || status == 1 || status == 2)
    {
        result.set_type_by_name(typeName);
        result.set_type_structure(typeStructure);
    }

    // status == 2 - empty result

    if (status == 4 || status == 5)
    {
        LDEBUG << "Exception, status = " << status;
        throw r_Equery_execution_failed(static_cast<unsigned int>(errNo), static_cast<unsigned int>(lineNo), static_cast<unsigned int>(colNo), token.c_str());
    }

    clearAnswer();

    return status;
}

r_OId RnpClientComm::executeGetNewOId(unsigned short objType) throw(r_Error)
{
    //cout<<" RnpClientComm::getNewOId: not implemented"<<endl;

    startRequest(RnpRasserver::cmd_getnewoid);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addInt32Parameter(RnpRasserver::pmt_objecttype, objType);
    LDEBUG << "request RnpRasserver::cmd_getnewoid with ..., clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    r_OId oid(decoder.getDataAsString());

    clearAnswer();

    return oid;
}

unsigned short RnpClientComm::executeGetObjectType(const r_OId& oid) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_getobjecttype);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_oidstring, oid.get_string_representation());
    LDEBUG << "request RnpRasserver::cmd_getobjecttype with ..., clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    int status = decoder.getDataAsInteger();
    switch (status)
    {
    case 0:
        break; //OK
    case 1:
        clearAnswer();
        LDEBUG << "Exception, status = " << status;
        throw r_Error(r_Error::r_Error_ClientUnknown);
        break;
    case 2:
        clearAnswer();
        LDEBUG << "Exception, status = " << status;
        throw r_Error(r_Error::r_Error_ObjectUnknown);
        break;
    default:
        clearAnswer();
        LDEBUG << "Exception, status = " << status;
        throw r_Error(r_Error::r_Error_TransferFailed);
        break;
    }

    decoder.getNextParameter();
    unsigned short objType = decoder.getDataAsInteger();

    clearAnswer();

    return objType;
}

char* RnpClientComm::executeGetTypeStructure(const char* typeName, r_Type_Type typeType) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_gettypestruct);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_typename, typeName);
    encoder.addInt32Parameter(RnpRasserver::pmt_typetype, typeType);
    LDEBUG << "request RnpRasserver::cmd_gettypestruct with ..., clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    int status = decoder.getDataAsInteger();
    switch (status)
    {
    case 0:
        break; //OK
    case 1:
        clearAnswer();
        LDEBUG << "Exception, status = " << status;
        throw r_Error(r_Error::r_Error_TransactionNotOpen);
        break;
    case 2:
        clearAnswer();
        LDEBUG << "Exception, status = " << status;
        throw r_Error(r_Error::r_Error_DatabaseClassUndefined);
        break;
    default:
        clearAnswer();
        LDEBUG << "Exception, status = " << status;
        throw r_Error(r_Error::r_Error_TransferFailed);
        break;
    }

    decoder.getNextParameter();
    char* typeStructure = new char [decoder.getDataLength() + 1];
    strcpy(typeStructure, decoder.getDataAsString());

    clearAnswer();

    return typeStructure;
}

int RnpClientComm::executeSetFormat(bool lTransferFormat, r_Data_Format format, const char* formatParams)
{
    startRequest(RnpRasserver::cmd_setformat);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid,     clientID);
    encoder.addInt32Parameter(RnpRasserver::pmt_whichformat,  lTransferFormat);
    encoder.addInt32Parameter(RnpRasserver::pmt_format,       format);
    encoder.addStringParameter(RnpRasserver::pmt_formatparams, formatParams);
    LDEBUG << "request RnpRasserver::cmd_setformat with ..., clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    int status = decoder.getDataAsInteger();

    clearAnswer();

    return status;
}

//----------------------------------------------------------
int RnpClientComm::sendAndReturnStatus() throw (r_Error)
{
    sendRequestGetAnswer();

    int status = decoder.getDataAsInteger();

    clearAnswer();

    return status;
}

void RnpClientComm::sendRequestGetAnswer() throw (r_Error)
{
    RnpBaseClientComm::setMaxRetry(RNP_MAX_RETRY);  // defined in raslib/rminit.hh -- PB 2005-sep-01
    if (RnpBaseClientComm::sendRequestGetAnswer() == false)
    {
        clearAnswer();
        LDEBUG << "Exception, sendRequestGetAnswer() == false";
        throw r_Error(r_Error::r_Error_TransferFailed);
    }

    detectErrors();
    if (decoder.countParameters() > 0)
    {
        decoder.getFirstParameter();
    }
}

void RnpClientComm::helper012d(__attribute__((unused)) const char* caller) throw (r_Error)
{
    int status = sendAndReturnStatus();

    switch (status)
    {
    case 0:
        break;
    case 1:
        LDEBUG << "RnpClientComm::helper012d( " << (caller ? caller : "(null)") << " ): error: status = " << status;
        throw r_Error(r_Error::r_Error_ClientUnknown);
        break;
    case 2:
        LDEBUG << "RnpClientComm::helper012d( " << (caller ? caller : "(null)") << " ): error: status = " << status;
        throw r_Error(r_Error::r_Error_ObjectUnknown);
        break;
    default:
        LDEBUG << "RnpClientComm::helper012d( " << (caller ? caller : "(null)") << " ): error: status = " << status;
        throw r_Error(r_Error::r_Error_General);
        break;
    }
}

bool RnpClientComm::detectErrors()
{
    if (decoder.getFragmentType() != Rnp::fgt_Error)
    {
        return false;
    }

    reassemble_r_Error() ;

    return true;
}

void RnpClientComm::reassemble_r_Error() throw (r_Error)
{
    decoder.getFirstParameter();
    if (decoder.getParameterType() != Rnp::ert_Other)
    {
        return;
    }

    decoder.getNextParameter();

    r_Error* temp = r_Error::getAnyError(const_cast<char*>(decoder.getDataAsString()));

    r_Error err = *temp;

    delete temp;

    LDEBUG << "npClientComm::reassemble_r_Error() throwing exception: " << decoder.getDataAsString();
    throw err;
}

void RnpClientComm::setTimeoutInterval(int seconds)
{
    akg::NbJob::setTimeoutInterval(seconds);
}

int  RnpClientComm::getTimeoutInterval()
{
    return akg::NbJob::getTimeoutInterval();
}

//## unofficial functions

r_OId RnpClientComm::createCollection(const char* collName, const char* collTypeName) throw(r_Error)
{
    checkForRwTransaction();
    startRequest(RnpRasserver::cmd_createcollection);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_collname, collName);
    encoder.addStringParameter(RnpRasserver::pmt_typename, collTypeName);
    LDEBUG << "request RnpRasserver::cmd_createcollection with ..., clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    r_OId oid(decoder.getDataAsString());

    clearAnswer();

    return oid;
}

r_OId RnpClientComm::createMDD(const char* collName, const char* mddTypeName, const char* definitionDomain, const char* tileDomain, bool rcindex) throw(r_Error)
{
    checkForRwTransaction();
    startRequest(RnpRasserver::cmd_createmdd);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_collname, collName);
    encoder.addStringParameter(RnpRasserver::pmt_typename, mddTypeName);
    encoder.addStringParameter(RnpRasserver::pmt_domain,   definitionDomain);
    encoder.addInt32Parameter(RnpRasserver::pmt_indextype, rcindex);
    encoder.addStringParameter(RnpRasserver::pmt_domain,   tileDomain);
    LDEBUG << "request RnpRasserver::cmd_createmdd with collName " << collName << ", mddTypeName " << mddTypeName << ", definitionDomain " << definitionDomain << ", rcindex " << rcindex << ", tileDomain " << tileDomain << ", clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    r_OId oid(decoder.getDataAsString());
    LDEBUG << "RnpClientComm::createMDD() receiving oid " << oid;

    clearAnswer();

    return oid;
}

void RnpClientComm::extendMDD(r_OId mddOId, const char* stripeDomain, const char* tileDomain) throw(r_Error)
{
    checkForRwTransaction();
    startRequest(RnpRasserver::cmd_extendmdd);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_oidstring, mddOId.get_string_representation());
    encoder.addStringParameter(RnpRasserver::pmt_domain,   stripeDomain);
    encoder.addStringParameter(RnpRasserver::pmt_domain,   tileDomain);
    LDEBUG << "request RnpRasserver::cmd_extendmdd with oid " << mddOId.get_string_representation() << ", stripeDomain " << stripeDomain << ", tileDomain " << tileDomain << ", clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    clearAnswer();
}

vector<r_OId> RnpClientComm::getOIdsFromCollection(const char* collName) throw(r_Error)
{
    vector<r_OId> result = executeGetOIdsFromCollection(collName, r_OId());

    return result;
}

vector<r_OId> RnpClientComm::getOIdsFromCollection(const r_OId& oid) throw(r_Error)
{
    vector<r_OId> result = executeGetOIdsFromCollection(0, oid);

    return result;
}

vector<r_OId> RnpClientComm::executeGetOIdsFromCollection(const char* collName, const r_OId& oid) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_getcolloids);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    LDEBUG << "request RnpRasserver::cmd_getcolloids with clientID 0x" << hex << clientID << dec;
    if (collName != NULL)
    {
        encoder.addStringParameter(RnpRasserver::pmt_collname, collName);
        LDEBUG << "RnpClientComm::executeGetOIdsFromCollection() adding collName " << collName;
    }
    else
    {
        encoder.addStringParameter(RnpRasserver::pmt_oidstring, oid.get_string_representation());
        LDEBUG << "RnpClientComm::executeGetOIdsFromCollection() adding oid " << oid.get_string_representation();
    }

    sendRequestGetAnswer();

    int status  =  decoder.getDataAsInteger();
    // we have to read all this, but we don't use them here
    decoder.getNextParameter();
    const char* typeName      =  decoder.getDataAsString();
    LDEBUG << "RnpClientComm::executeGetOIdsFromCollection() received typeName " << typeName;
    decoder.getNextParameter();
    const char* typeStructure =  decoder.getDataAsString();
    LDEBUG << "RnpClientComm::executeGetOIdsFromCollection() received typeStructure " << typeStructure;
    decoder.getNextParameter();
    const char* oidstring     =  decoder.getDataAsString();
    LDEBUG << "RnpClientComm::executeGetOIdsFromCollection() received oid " << oidstring;
    decoder.getNextParameter();
    const char* collectionName =  decoder.getDataAsString();
    LDEBUG << "RnpClientComm::executeGetOIdsFromCollection() received collectionName " << collectionName;

    if (status != 0 && status != 1)
    {
        r_Error err;
        switch (status)
        {
        case 2:
            LDEBUG << "Exception, status = " << status;
            err = r_Error(r_Error::r_Error_ObjectUnknown);
            break;
        case 3:
            LDEBUG << "Exception, status = " << status;
            err = r_Error(r_Error::r_Error_ClientUnknown);
            break;
        default:
            LDEBUG << "Exception, status = " << status;
            err = r_Error(r_Error::r_Error_TransferFailed);
            break;
        }
        clearAnswer();
        throw err;
    }

    // create the set
    vector<r_OId> result;
    // fill set with oids
    if (status == 0)
    {
        while (decoder.getNextParameter() != 0)
        {
            r_OId roid(decoder.getDataAsString());
            LDEBUG << "RnpClientComm::executeGetOIdsFromCollection() received oid set component " << roid;

            result.push_back(roid);
        }
    }

    clearAnswer();

    return result;
}

vector<r_Minterval> RnpClientComm::getTileDomains(r_OId mddOId, const char* stripeDomain) throw(r_Error)
{
    startRequest(RnpRasserver::cmd_gettiledomains);
    encoder.addInt32Parameter(RnpRasserver::pmt_clientid, clientID);
    encoder.addStringParameter(RnpRasserver::pmt_oidstring, mddOId.get_string_representation());
    encoder.addStringParameter(RnpRasserver::pmt_domain, stripeDomain);
    LDEBUG << "request RnpRasserver::cmd_gettiledomains with oid " << mddOId.get_string_representation() << ", stripeDomain " << stripeDomain  << ", clientID 0x" << hex << clientID << dec;

    sendRequestGetAnswer();

    vector<r_Minterval> result;

    const RnpParameter* currParam = decoder.getFirstParameter();

    while (currParam)
    {
        r_Minterval interval(decoder.getDataAsString());
        LDEBUG << "RnpClientComm::getTileDomains() received minterval " << interval;

        result.push_back(interval);

        currParam = decoder.getNextParameter();
    }

    clearAnswer();

    return result;
}

