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

#ifndef CLIENTCOMM_HH_
#define CLIENTCOMM_HH_

#include "raslib/mddtypes.hh"

class r_Database;
class r_Transaction;
class r_OId;
class r_OQL_Query;
class r_GMarray;
template <class T> class r_Set;
class r_Ref_Any;
class r_Base_Type;
class r_Parse_Params;

/**
*   @defgroup ClientComm ClientComm
*
*   @file clientcomm.hh
*
*   @ingroup ClientComm
*/
//  @Doc

/*  The class ClientComm represents {\bf one} connection between
    {\bf one} client entity (e.g. an object of class \Ref{r_Database}) and
    the server. Therefore, the host name has to be provided at
    the constructor.
*/
class ClientComm
{
public:

    /// destructor (closes the connection and releases resources)
    virtual ~ClientComm() = default;

    //@Man: Database methods
    //@{
    ///
    //RNP: all made pure

    /// open database
    virtual int openDB(const char* database) = 0;
    /// close current database
    virtual int closeDB() = 0;
    /// create a database
    virtual int createDB(const char* name) = 0;
    /// destroy a database
    virtual int destroyDB(const char* name) = 0;

    ///
    //@}

    //@Man: Transaction methods
    //@{
    ///

    /// begin transaction
    virtual int openTA(unsigned short readOnly = 0) = 0;
    /// commit current transaction
    virtual int commitTA() = 0;
    /// abort current transaction
    virtual int abortTA() = 0;

    ///
    //@}

    //@Man: MDD methods
    //@{
    ///

    /// inserts a MDD object in an existing MDD collection on the server
    virtual void insertMDD(const char* collName, r_GMarray* mar) = 0;
    /// gets MDD object by oid
    virtual r_Ref_Any getMDDByOId(const r_OId& oid) = 0;

    ///
    //@}

    //@Man: Collection methods
    //@{
    ///

    /// creates an empty MDD collection on the server
    virtual void insertColl(const char* collName, const char* typeName, const r_OId& oid) = 0;
    /// deletes an MDD collection by name
    virtual void deleteCollByName(const char* collName) = 0;
    /// deletes an object by oid (right now, objects are collection only)
    virtual void deleteObjByOId(const r_OId& oid) = 0;
    /// removes an object from a collection
    virtual void removeObjFromColl(const char* name, const r_OId& oid) = 0;
    /// gets collection by name
    virtual r_Ref_Any getCollByName(const char* name) = 0;
    /// gets collection by oid
    virtual r_Ref_Any getCollByOId(const r_OId& oid) = 0;
    /// gets collection references by name
    virtual r_Ref_Any getCollOIdsByName(const char* name) = 0;
    /// gets collection references by oid
    virtual r_Ref_Any getCollOIdsByOId(const r_OId& oid) = 0;

    ///
    //@}

    //@Man: Query methods
    //@{
    ///

    /// query execution
    virtual void executeQuery(const r_OQL_Query& query, r_Set<r_Ref_Any>& result) = 0;
    /*@Doc:
      Executes a retrieval query of type \Ref{r_OQL_Query} and returns the result. Every
      MDD object of the MDD collection is fetched from the server and inserted
      in the resulting \Ref{r_Set}.
    */

    /// update execution
    virtual void executeQuery(const r_OQL_Query& query) = 0;
    /*@Doc:
      Executes an update query of type \Ref{r_OQL_Query}.
    */

    /// insert returning oid, third parameter is dummy parameter
    virtual void executeQuery(const r_OQL_Query& query, r_Set<r_Ref_Any>& result, int dummy) = 0;
    /*@Doc:
      Executes an insert query of type \Ref{r_OQL_Query}.
    */

    ///
    //@}


    //@Man: System methods
    //@{
    ///

    /// get new oid
    virtual r_OId getNewOId(unsigned short objType) = 0;

    /// get oid type
    virtual unsigned short getObjectType(const r_OId& oid) = 0;

    enum r_Type_Type
    {
        r_SetType_Type = 1,
        r_MDDType_Type = 2
    };

    /// get type structure
    /// deallocate using delete []
    virtual char* getTypeStructure(const char* typeName, r_Type_Type typeType) = 0;

    ///
    //@}


    //@Man: Methods for asynchronious alive signal concept
    //@{

    /// triggers an alive signal
    virtual void triggerAliveSignal() = 0;
    /**
      First, it sets the switch {\tt aliveSignalRemaining} saying that an alive signal
      should be send to the server. Then it calls {\tt sendAliveSignal()} to send it
      immediately if possible.
    */

    /// send an alive signal if necessary and possible
    virtual void sendAliveSignal() = 0;
    /**
      Sends an alive signal to the server if the switch {\tt aliveSignalRemaining} is
      set and no other RPC is active. If a signal can be sent, {\tt aliveSignalRemaining}
      is set to 0 again.
    */

    /// set the preferred transfer format
    virtual int setTransferFormat(r_Data_Format format, const char* formatParams = NULL) = 0;

    /// set the preferred storage format
    virtual int setStorageFormat(r_Data_Format format, const char* formatParams = NULL) = 0;

    /// get extended error information
    const char* getServerName();

    /// user identification for RasMGR
    virtual void setUserIdentification(const char* userName, const char* plainTextPassword) = 0;

    /// set maximum retry to get a server
    virtual void setMaxRetry(unsigned int newMaxRetry) = 0;

    /// get maximum retry to get a server
    virtual unsigned int  getMaxRetry() = 0;

    /// set and get communication timeout interval. Only RNP really uses it
    virtual void setTimeoutInterval(int seconds) = 0;
    virtual int  getTimeoutInterval() = 0;

    /// sets the database that is using this client communicator
    void setDatabase(r_Database* database);

    /// sets the transaction that is using this client communicator
    void setTransaction(r_Transaction* transaction);

    /// resets to the global r_Transaction::actual_transaction if necessary
    virtual void updateTransaction();

    ///
    //@}
    
    /// provides read access to my clientID
    virtual unsigned long getClientID() const  = 0;

protected:
    /// constructor getting nothing
    ClientComm() = default;

    /// reference to the database that created this client communicator
    r_Database* database;

    /// reference to the transaction being used by this client communicator
    r_Transaction* transaction;
};

#endif
