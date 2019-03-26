#ifndef RNPCLIENTCOMM_HH
#define RNPCLIENTCOMM_HH
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
 * COMMENTS:
 *
 ************************************************************/

#include "clientcomm/clientcomm.hh"
#include "rnprotocol/rnpcommunication.hh"
#include "rnprotocol/rnprasserver.hh"
#include "clientcomm/rpcif.h"

using namespace rnp;

/**
  * \defgroup Rnprotocols Rnprotocol Classes
  */

/**
  * \ingroup Rnprotocols
  */

void freeGetTileRes(GetTileRes* ptr);

class RnpClientComm : public ClientComm, RnpBaseClientComm
{
public:
    /// constructor getting the host name of the rasmgr host and it's listening port (default 7001).
    RnpClientComm(const char* rasmgrHost, int rasmgrPort = RASMGRPORT);

    ~RnpClientComm() noexcept;

    bool effectivTypeIsRNP() noexcept;

    //@Man: Database methods
    //@{
    ///

    /// open database
    int openDB(const char* database);
    /// close current database
    int closeDB();
    /// create a database
    int createDB(const char* name);
    /// destroy a database
    int destroyDB(const char* name);

    ///
    //@}

    //@Man: Transaction methods
    //@{
    ///

    /// begin transaction
    int openTA(unsigned short readOnly = 0);
    /// commit current transaction
    int commitTA();
    /// abort current transaction
    int abortTA();

    ///
    //@}

    //@Man: MDD methods
    //@{
    ///

    /// inserts a MDD object in an existing MDD collection on the server
    void insertMDD(const char* collName, r_GMarray* mar);
    /// gets MDD object by oid
    r_Ref_Any getMDDByOId(const r_OId& oid);

    ///
    //@}

    //@Man: Collection methods
    //@{
    ///

    /// creates an empty MDD collection on the server
    void insertColl(const char* collName, const char* typeName, const r_OId& oid);
    /// deletes an MDD collection by name
    void deleteCollByName(const char* collName);
    /// deletes an object by oid (right now, objects are collection only)
    void deleteObjByOId(const r_OId& oid);
    /// removes an object from a collection
    void removeObjFromColl(const char* name, const r_OId& oid);
    /// gets collection by name
    r_Ref_Any getCollByName(const char* name);
    /// gets collection by oid
    r_Ref_Any getCollByOId(const r_OId& oid);
    /// gets collection references by name
    r_Ref_Any getCollOIdsByName(const char* name);
    /// gets collection references by oid
    r_Ref_Any getCollOIdsByOId(const r_OId& oid);

    ///
    //@}

    //@Man: Query methods
    //@{
    ///

    /// query execution
    void executeQuery(const r_OQL_Query& query, r_Set<r_Ref_Any>& result);
    /*@Doc:
      Executes a retrieval query of type \Ref{r_OQL_Query} and returns the result. Every
      MDD object of the MDD collection is fetched from the server and inserted
      in the resulting \Ref{r_Set}.
    */

    /// update execution
    void executeQuery(const r_OQL_Query& query);
    /*@Doc:
      Executes an update query of type \Ref{r_OQL_Query}.
    */

    /// insert execution
    void executeQuery(const r_OQL_Query& query, r_Set<r_Ref_Any>& result, int dummy);
    /*@Doc:
      Executes an insert query of type \Ref{r_OQL_Query}.
    */
    ///
    //@}


    //@Man: System methods
    //@{
    ///

    /// get new oid
    r_OId getNewOId(unsigned short objType);

    /// get oid type
    unsigned short getObjectType(const r_OId& oid);

    /// get type structure
    /// dallocate using delete []
    char* getTypeStructure(const char* typeName, r_Type_Type typeType);

    ///
    //@}

    /// provides read access to my clientID
    unsigned long getClientID() const;

    /// set the preferred transfer format
    int setTransferFormat(r_Data_Format format, const char* formatParams = NULL);

    /// set the preferred storage format
    int setStorageFormat(r_Data_Format format, const char* formatParams = NULL);


    /// get real server name (the dinamic one, assigned by the RasMGR)
    const char* getServerName();

    /// user identification for RasMGR
    void setUserIdentification(const char* userName, const char* plainTextPassword);

    /// set maximum retry to get a server
    void setMaxRetry(unsigned int newMaxRetry);

    /// get maximum retry to get a server
    unsigned int  getMaxRetry();

    void setTurbo(bool turbo);

    ///
    //@}

    // obsolete, but kept because of virtual functions in base class
    void triggerAliveSignal();
    void sendAliveSignal();

    void setTimeoutInterval(int seconds);
    int  getTimeoutInterval();

//#### secret, unofficial functions ###########

    r_OId  createCollection(const char* collName, const char* collTypeName);

    r_OId  createMDD(const char* collName, const char* mddTypeName, const char* definitionDomain, const char* tileDomain, bool rcindex = false);

    void   extendMDD(r_OId mddOId, const char* stripeDomain, const char* tileDomain);

    vector<r_OId> getOIdsFromCollection(const char* name);

    vector<r_OId> getOIdsFromCollection(const r_OId& oid);

    vector<r_Minterval> getTileDomains(r_OId mddOId, const char* stripeDomain);


    void preloadTiles(r_OId mddOId, const char* tileDomain);

    int  getTileData(r_OId mddOId, const char* tileDomain, char*& data, bool preallocated = false);

    void replaceTileData(r_OId mddOId, const char* tileDomain, const char* newData, int dataLength, const char* alfaData, int alfaLength);

//#############################################
private:
    /// client ID assigned to me by the server
    int clientID;

    /// the name of the rasmgr host
    char* rasmgrHost;

    /// the listening port of the rasmgr
    int  rasmgrPort;

    /// the name of the server host
    char serverHost[100]; //can't be just a pointer, it's never stored elsewhere

    int  serverPort;

    // the name of the opened database, needed because it will be opened again and again, in a hidden operation
    char databaseName[100];

    // the capability
    char capability[100];

    /// user identification string
    char identificationString[100];

    /// requests a free server from the rasmgr, retrying maxRetry times
    int getFreeServer(bool readwrite, bool openDB);

    /// requests a free server from the rasmgr
    int executeGetFreeServer(bool readwrite, bool openDB);

    int readWholeMessage(int socket, char* destBuffer, int buffSize);

    int writeWholeMessage(int socket, char* destBuffer, int buffSize);

    // MD5 of password
    int messageDigest(const char* input, char* output, const char* mdName);

    /// internal function for client/server protocol handling of non-MDD collection transfer
    void getElementCollection(r_Set<r_Ref_Any>& result);

    /// internal function for client/server protocol handling of MDD collection transfer
    void getMDDCollection(r_Set<r_Ref_Any>& result, unsigned int isQuery);

    /// internal function for reading an MDD from the database
    unsigned short getMDDCore(r_Ref<r_GMarray>& mdd, GetMDDRes* thisResult, unsigned int isQuery);

    /// concatenate data to an array, making sure there are no overflows (used by getMDDCore())
    int concatArrayData(const char* source, unsigned long srcSize, char*& dest,
                        unsigned long& destSize, unsigned long& destLevel);

    /// internal function for converting a \Ref{r_GMarray} into its RPC representation
    void getMarRpcRepresentation(const r_GMarray* mar, RPCMarray*& rpcMarray,
                                 r_Data_Format initStorageFormat = r_Array,
                                 const r_Base_Type* bt = NULL);

    /// internal function for freeing data allocated by getMarRpcRepresentation()
    void freeMarRpcRepresentation(const r_GMarray* mar, RPCMarray* rpcMarray);

    /// internal function for sending query's mdd constants to a server
    void sendMDDConstants(const r_OQL_Query& query);

    /// endianness of client and server (0 means big endian)
    int endianServer;
    int endianClient;

    /// data format for transfer compression
    r_Data_Format transferFormat;
    /// storage format for inserting new tiles
    r_Data_Format storageFormat;
    /// transfer format parameters
    char* transferFormatParams;
    /// storage format parameters
    char* storageFormatParams;
    /// parameter object for configuration
    r_Parse_Params* clientParams;

    /// policy is compress-on-server
    int serverCompresses;
    /// policy is exact
    int exactFormat;

    // functions which really do the connection stuff
    void executeConnect();
    void executeDisconnect();
    void executeOpenDB(const char*);
    void executeCloseDB();
    void executeBeginTA(bool rw);
    void executeCommitTA();
    void executeAbortTA();
    int  executeExecuteQuery(const char* query, r_Set<r_Ref_Any>& result);
    GetElementRes* executeGetNextElement();
    int  executeEndTransfer();
    GetMDDRes*  executeGetNextMDD();
    GetTileRes* executeGetNextTile();
    int executeExecuteUpdateQuery(const char* query);
    int executeExecuteUpdateQuery(const char* query, r_Set<r_Ref_Any>& result);
    int  executeStartInsertTransMDD(r_GMarray* mdd);
    int  executeInsertTile(bool persistent, RPCMarray* tile);
    void executeEndInsertMDD(bool persistent);
    int  executeInitUpdate();
    int  executeStartInsertPersMDD(const char* collName, r_GMarray* mar);
    int  executeInsertMDD(const char* collName, r_GMarray* mar, RPCMarray* rpcMarray);
    int  executeInsertCollection(const char* collName, const char* typeName, const r_OId& oid);
    r_Ref_Any executeGetCollByNameOrOId(const char* collName, const r_OId& oid);
    r_Ref_Any executeGetCollOIdsByNameOrOId(const char* collName, const r_OId& oid);
    int   executeSetFormat(bool transferFormat, r_Data_Format format, const char* formatParams);
    r_OId executeGetNewOId(unsigned short objType);
    unsigned short executeGetObjectType(const r_OId& oid);
    char* executeGetTypeStructure(const char* typeName, r_Type_Type typeType);

    vector<r_OId> executeGetOIdsFromCollection(const char* collName, const r_OId& oid);

    void turboOpenDB(const char*);
    void turboBeginTA(bool rw);
    void turboCommitTA();
    void turboAbortTA();

    bool useTurbo;

    // returns only if transaction is open and rw, otherwise throws
    void checkForRwTransaction();

    // varianta locala
    void sendRequestGetAnswer();

    int sendAndReturnStatus();

    bool detectErrors();
    // doesn't return if there is an error
    void reassemble_r_Error();

    // a very internal helper for some functions
    void helper012d(const char* caller);
};

#endif
