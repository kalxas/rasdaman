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
 * SOURCE:   database.cc
 *
 * MODULE:   rasodmg
 * CLASS:    r_Database
 *
 * COMMENTS:
 *      None
*/

#ifdef __VISUALC__
#ifndef __EXECUTABLE__
#define __EXECUTABLE__
#define DATABASE_NOT_SET
#endif
#endif

#include "config.h"
#include "rasodmg/database.hh"
#include "rasodmg/transaction.hh"
#include "clientcomm/clientcomm.hh"

#include <logging.hh>

#ifdef DATABASE_NOT_SET
#undef __EXECUTABLE__
#endif

#include <string.h>

// At the beginning, no database is actually opened.
r_Database* r_Database::actual_database = 0;


r_Database::r_Database()
    : communication(0),
      db_status(not_open),
      rasmgrName(0),
      userName(0),
      plainPass(0)
{
}

r_Database::r_Database(const char* name)
    : communication(0),
      db_status(not_open),
      userName(0),
      plainPass(0)
{
    if (!name)
    {
        LERROR << "null database name.";
        throw r_Error(r_Error::r_Error_NameInvalid);
    }
    this->rasmgrName = strdup(name);
}

r_Type*
r_Database::get_type_schema(const char* typeName, type_schema typeType)
{
    r_Type* retval = 0;

    if ((typeName == NULL) || (strlen(typeName) == 0))
    {
        throw r_Error(r_Error::r_Error_NameInvalid);
    }
    else if ((typeType != COLLECTION) && (typeType != MARRAY))
    {
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
    else if (this->get_status() == r_Database::not_open)
    {
        throw r_Error(r_Error::r_Error_DatabaseClosed);
    }
    else
    {
        ClientComm::r_Type_Type type;
        if (typeType == COLLECTION)
        {
            type = ClientComm::r_SetType_Type;
        }
        else
        {
            type = ClientComm::r_MDDType_Type;
        }
        char* temp = this->communication->getTypeStructure(typeName, type);
        retval = r_Type::get_any_type(temp);
        delete [] temp;
        temp = 0;
    }
    return retval;
}

r_Database::~r_Database()
{
    if (db_status != not_open)
    {
        close();
    }

    if (rasmgrName)
    {
        free(rasmgrName);
    }
    if (userName)
    {
        free(userName);
    }
    if (plainPass)
    {
        free(plainPass);
    }
}

void
r_Database::open(const char* database_name, access_status new_status)
{
    if (db_status != not_open)
    {
        r_Error err = r_Error(r_Error::r_Error_DatabaseOpen);
        throw err;
    }

    if (!database_name)
    {
        LERROR << "r_Database::open(name, new_status) name is null";
        throw r_Error(r_Error::r_Error_NameInvalid);
    }

    // While instantiating the communication object, the first connection to
    // the server is established. Any exception is given through to the caller
    // of open(...).
    try
    {
        communication = ClientComm::createObject(rasmgrName, rasmgrPort);
        if (userName && plainPass)
        {
            communication->setUserIdentification(userName, plainPass);
        }
    }
    catch (...)
    {
        if (communication)
        {
            delete communication;
        }
        throw;  // re-throw the exception (r_Error_HostInvalid, r_Error_ServerInvalid)
    }
    communication->setDatabase(this);
    // open database
    unsigned int status = 0;
    try
    {
        status = static_cast<unsigned int>(communication->openDB(const_cast<char*>(database_name)));
    }
    catch (...)
    {
        if (communication)
        {
            delete communication;
        }
        throw;
    }

    if (status)
    {
        // translate error values into exceptions
        r_Error err;

        switch (status)
        {
        case 1:
            err = r_Error(r_Error::r_Error_ClientUnknown);
            break;

        case 2:
            err = r_Error(r_Error::r_Error_DatabaseUnknown);
            break;

        case 3:
            err = r_Error(r_Error::r_Error_DatabaseOpen);
            break;

        case 4:
            err = r_Error(r_Error::r_Error_RpcInterfaceIncompatible);
            break;
        case CONNECTIONCLOSED:
            err = r_Error(CONNECTIONCLOSED);
            break;
        default:
            err = r_Error(r_Error::r_Error_General);
        }

        if (communication)
        {
            delete communication;
        }

        throw err;
    }
    //if no other database was set as default, make this one default.
    if (actual_database == 0)
    {
        actual_database = this;
    }
    db_status       = new_status;
}

void
r_Database::close()
{
    if (db_status != not_open)
    {
        // if a communication object exists, close and delete it
        if (communication)
        {
            // abort any open TA -- PB 2005-sep-02
            // This is quite a hack (borrowed from fastscale.cc):
            // Actual transaction is a pointer to this in a TA.
            // Since the TA was allocated by the application program
            // it should be save to use it like this.
            communication->closeDB();
            delete communication;
            communication = 0;
        }

        db_status = not_open;
        //remove the default database only if this is the same as this database object.
        if (this == actual_database)
        {
            actual_database = 0;
        }
}
}

void
r_Database::create(__attribute__((unused)) const char* name)
{
    // this operation is not supported through this interface; use rasdl
    throw (r_Error(803));  // Access denied, no permission
}

void
r_Database::destroy(__attribute__((unused)) const char* name)
{
    // this operation is not supported through this interface; use rasdl
    throw (r_Error(803));  // Access denied, no permission
}


void
r_Database::set_servername(const char* name, int port)
{
    //We let the name of the function as it is, but it's about the rasmgr name

    if (!name)
    {
        LERROR << "r_Database::set_servername(name, port) name is null";
        throw r_Error(r_Error::r_Error_NameInvalid);
    }

    if (rasmgrName)
    {
        free(rasmgrName);
    }

    rasmgrName = strdup(name);
    rasmgrPort = port;
}
void
r_Database::set_useridentification(const char* name, const char* plain_pass)
{
    if (!name)
    {
        LERROR << "r_Database::set_useridentification(name, plain_pass) name is null";
        throw r_Error(r_Error::r_Error_NameInvalid);
    }
    if (!plain_pass)
    {
        LERROR << "r_Database::set_useridentification(name, plain_pass) plain_pass is null";
        throw r_Error(r_Error::r_Error_NameInvalid);
    }

    if (userName)
    {
        free(userName);
    }
    if (plainPass)
    {
        free(plainPass);
    }
    userName  = strdup(name);
    plainPass = strdup(plain_pass);
}

void
r_Database::set_object_name(r_Object& obj, const char* name)
{
    obj.set_object_name(name);
}

r_Ref_Any
r_Database::lookup_object(const char* name) const
{
    r_Ref_Any returnValue;

    if (db_status == not_open)
    {
        r_Error err = r_Error(r_Error::r_Error_DatabaseClosed);
        throw err;
    }

    if (!name)
    {
        LERROR << "r_Database::lookup_object(name) name is null";
        throw r_Error(r_Error::r_Error_NameInvalid);
    }

    try
    {
        // get collection
        returnValue = communication->getCollOIdsByName(name);
    }
    catch (...)
    {
        throw;  // re-throw the exception
    }

    return returnValue;
}



r_Ref_Any
r_Database::lookup_object(const r_OId& oid) const
{
    r_Ref_Any returnValue;

    if (db_status == not_open)
    {
        r_Error err = r_Error(r_Error::r_Error_DatabaseClosed);
        throw err;
    }

    try
    {
        // determine type of object and get it
        if (communication->getObjectType(oid) == 1)
        {
            returnValue = communication->getMDDByOId(oid);
        }
        else
        {
            returnValue = communication->getCollOIdsByOId(oid);
        }
    }
    catch (...)
    {
        throw;  // re-throw the exception
    }

    return returnValue;
}


void
r_Database::set_transfer_format(r_Data_Format format, const char* formatParams)
{
    unsigned short result;

    if (db_status == not_open)
    {
        r_Error err = r_Error(r_Error::r_Error_DatabaseClosed);
        throw (err);
    }
    //keeps from crashing in rpc on linux
    if (formatParams == 0)
    {
        formatParams = "";
    }
    result = communication->setTransferFormat(format, formatParams);

    switch (result)
    {
    case 1:
    {
        r_Error err = r_Error(r_Error::r_Error_ClientUnknown);
        throw (err);
    }
    break;
    case 2:
    {
        r_Error err = r_Error(r_Error::r_Error_FeatureNotSupported);
        throw (err);
    }
    break;
    default:
        break;
    }
}

void
r_Database::set_storage_format(r_Data_Format format, const char* formatParams)
{
    unsigned short result;

    if (db_status == not_open)
    {
        r_Error err(r_Error::r_Error_DatabaseClosed);
        throw (err);
    }

    //keeps from crashing in rpc on linux
    if (formatParams == 0)
    {
        formatParams = "";
    }

    result = communication->setStorageFormat(format, formatParams);

    switch (result)
    {
    case 1:
    {
        r_Error err(r_Error::r_Error_ClientUnknown);
        throw (err);
    }
    break;
    case 2:
    {
        r_Error err(r_Error::r_Error_FeatureNotSupported);
        throw (err);
    }
    break;
    default:
        break;
    }
}



const r_OId
r_Database::get_new_oid(unsigned short objType) const
{
    return communication->getNewOId(objType);
}


void r_Database::insertColl(const char* collName, const char* typeName, const r_OId& oid)
{
    communication->insertColl(collName, typeName, oid);
}

void r_Database::removeObjFromColl(const char* name, const r_OId& oid)
{
    communication->removeObjFromColl(name, oid);
}

ClientComm* r_Database::getComm()
{
    return communication;
}

