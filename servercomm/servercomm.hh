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

#ifndef _SERVERCOMM_HH_
#define _SERVERCOMM_HH_

#include "raslib/oid.hh"
#include "raslib/minterval.hh"

#include <mutex>

// forward declarations
class AdminIf;
class BaseType;
class ClientTblElt;
class MDDColl;
struct RPCMarray;
struct RPCOIdEntry;
struct ExecuteQueryRes;
struct ExecuteUpdateRes;

//@ManMemo: Module: {\bf servercomm}

/*@Doc:
  The class servercomm describes the one and only server communication object
  that can exist in a RasDaMan RPC server. It manages listening for client and
  maps incoming calls to the respective remote procedures (which reside in the
  file manager.cc). These remote procedures are global functions
  which mainly concern with RPC call processing and finally call the methods
  of this servercomm class to forward client requests.
*/
class ServerComm
{
private:
    /// singleton
    static ServerComm *serverCommInstance;
    /// the client table which holds information about the calling clients
    static ClientTblElt *clientTbl;
    /// mutex to control write access to clientTbl
    static std::mutex clientTblMutex;
    
public:

    /// default constructor
    ServerComm();

    ServerComm(const ServerComm &) = delete;

    /// destructor
    virtual ~ServerComm();

    /// adds an entry to the client table (used in RasServerEntry)
    void addClientTblEntry(ClientTblElt *context);
    /**
      Adds the context entry passed to the client table.
      Throws an exception if context==NULL.
    */

    /// deletes an entry of the client table (must be public because it is used in the global garbage collection function)
    unsigned short deleteClientTblEntry(unsigned long ClientId);
    /**
      Deletes the entry of the client table corresponding to the given client id.
      If no corresponding id is found, false is returned.
    */

    /// returns a pointer to the context of the calling client, 0 it there is no context
    ClientTblElt *getClientContext(unsigned long ClientId, bool printErrors = true);
    /**
      Returns a pointer to the context of the calling client. This is done by
      searching the client table maintained by the server for the given client id.
      If there is no context corresponding to the client id, 0 is returned.

      Attention: After a client context was successfully received it has to be
                 released using its member function release();
    */

    // quick hack function used when stopping server to abort transaction and close db
    static void abortEveryThingNow();

    /// print server status with client table content to \c s
    virtual void printServerStatus();

    // -----------------------------------------------------------------------------------------
    // DB methods: open, close, create, destroy
    // -----------------------------------------------------------------------------------------

    ///
    /// open database
    virtual unsigned short openDB(unsigned long callingClientId, const char *dbName, const char *userName);
    /**
      The method opens the database with \c dbName. The return value means the following:

      \latexonly
      \latexonly
      \begin{tabular}{lll}
      0 && database successfully opened\\
      1 && client context not found\\
      2 && database does not exist\\
      3 && database is already open\\
      \end{tabular}
      \endlatexonly
    */

    /// close current database
    virtual unsigned short closeDB(unsigned long callingClientId);
    /**
      The return value has the following meaning:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      \end{tabular}
      \endlatexonly
    */

    /// create a database
    virtual unsigned short createDB(char *name);

    /// destroy a database
    virtual unsigned short destroyDB(char *name);

    // -----------------------------------------------------------------------------------------
    // Transaction (TA) methods: begin, commit, abort, isTAOpen
    // -----------------------------------------------------------------------------------------

    ///
    /// open transaction
    virtual unsigned short beginTA(unsigned long callingClientId, unsigned short readOnly = 0);
    /**
      The return value has the following meaning:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && other transaction already active\\
      \end{tabular}
      \endlatexonly
    */

    /// commit current transaction
    virtual unsigned short commitTA(unsigned long callingClientId);
    /**
      The return value has the following meaning:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      \end{tabular}
      \endlatexonly
    */

    /// abort current transaction
    virtual unsigned short abortTA(unsigned long callingClientId);
    /**
      The return value has the following meaning:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      \end{tabular}
      \endlatexonly
    */

    /// is transaction open currently?
    virtual bool isTAOpen(unsigned long callingClientId);
    /**
      The return value has the following meaning:
      \latexonly
      \begin{tabular}{lll}
      true && a transaction is open\\
      false && no transaction is open\\
      \end{tabular}
      \endlatexonly
    */

    // -----------------------------------------------------------------------------------------
    // Execute rasql queries (select, update, insert)
    // -----------------------------------------------------------------------------------------

    ///
    /// executes a retrieval query and prepares the result for transfer with getNextMDD.
    virtual unsigned short
    executeQuery(unsigned long callingClientId, const char *query,
                 ExecuteQueryRes &returnStructure,
                 bool insert = false);
    /**
      Executes a query and puts the result in the actual transfer collection.
      The first parameter is the unique client id
      for which the query should be executed. The second parameter is the
      query itself represented as a string. Fourth parameter indicates if the
      query is an insert query (if true), otherwise a regular select query.

      Return values
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful - result collection holds MDD elements\\
      1 && operation was successful - result collection holds non-MDD elements\\
      2 && operation was successful - result collection has no elements\\
      3 && client context not found\\
      4 && parse errror\\
      5 && execution error\\
      \end{tabular}
      \endlatexonly

      Communication protocol (return value = 0)
      \latexonly
      \begin{tabular}{lll}
      executeQuery && \\
      ->                 && getNextMDD \\
                         && ->               && getNextTile \\
                         &&                  && : \\
                         && :\\
      endTransfer \\
      \end{tabular}
      \endlatexonly

      Communication protocol (return value = 1)
      \latexonly
      \begin{tabular}{lll}
      executeQuery && \\
      ->                 && getNextElement \\
                         && :\\
      endTransfer \\
      \end{tabular}
      \endlatexonly
    */

    ///
    /// prepares transfer of MDD constants and execution of update query
    virtual unsigned short initExecuteUpdate(unsigned long callingClientId);
    /**
      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      \end{tabular}
      \endlatexonly

      Communication protocol
      \latexonly
      \begin{tabular}{lll}
      initExecuteUpdate && \\
      ->                      && startInsertTransMDD \\
                              && ->                        && insertTile \\
                              &&                           && :\\
                              && endInsertMDD\\
                              && :\\
      executeUpdate     && \\
      \end{tabular}
      \endlatexonly

      Note: Method executeUpdate can be invoked without the initExecuteUpdate
            prolog in case of no constant MDD objects.
    */

    /// executes an update query
    virtual unsigned short
    executeUpdate(unsigned long callingClientId, const char *query, ExecuteUpdateRes &returnStructure);
    /**
      Executes an update query.
      The first parameter is the unique client id
      for which the query should be executed. The second parameter is the
      query itself represented as a string.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && parse errror\\
      3 && execution error\\
      \end{tabular}
      \endlatexonly
    */

    // insert query returning results
    virtual unsigned short
    executeInsert(unsigned long callingClientId, const char *query, ExecuteQueryRes &returnStructure);
    /**
      Executes a query and puts the result in the actual transfer collection.
      The first parameter is the unique client id
      for which the query should be executed. The second parameter is the
      query itself represented as a string.

      Return values
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful - result collection holds MDD elements\\
      1 && operation was successful - result collection holds non-MDD elements\\
      2 && operation was successful - result collection has no elements\\
      3 && client context not found\\
      4 && parse errror\\
      5 && execution error\\
      \end{tabular}
      \endlatexonly

      Communication protocol (return value = 0)
      \latexonly
      \begin{tabular}{lll}
      executeQuery && \\
      ->                 && getNextMDD \\
                         && ->               && getNextTile \\
                         &&                  && : \\
                         && :\\
      endTransfer \\
      \end{tabular}
      \endlatexonly

      Communication protocol (return value = 1)
      \latexonly
      \begin{tabular}{lll}
      executeQuery && \\
      ->                 && getNextElement \\
                         && :\\
      endTransfer \\
      \end{tabular}
      \endlatexonly
    */

    // -----------------------------------------------------------------------------------------
    // Insert MDD / tile
    // -----------------------------------------------------------------------------------------

    /// create a new persistent MDD object for tile based transfers
    virtual unsigned short startInsertPersMDD(unsigned long callingClientId,
            const char *collName, r_Minterval &domain,
            unsigned long typeLength, const char *typeName, r_OId &oid);
    /**
      Creates an object for tile based transfer with method insertTile to be
      inserted into the specified MDD collection.

      Parameters
      \latexonly
      \begin{tabular}{lll}
      \c callingClientId  && unique client id of the calling client\\
      \c collName         && name of the collection to insert the MDD object\\
      \c domain           && spatial domain\\
      \c typeLength       && size of base type in bytes\\
      \c typeName         && type structure as string representation\\
      \c oid              && object identifier\\
      \end{tabular}
      \endlatexonly

      Return values
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && MDD type name not found\\
      3 && types of MDD and collection are incompatible\\
      4 && MDD and its type are incompatible\\
      5 && collection does not exist\\
      6 && creation of persistent object failed\\
      \end{tabular}
      \endlatexonly

      Communication protocol
      \latexonly
      \begin{tabular}{lll}
      startInsertPersMDD && \\
      ->                       && insertTile \\
                               && :\\
      endInsertMDD && \\
      \end{tabular}
      \endlatexonly
    */

    ///
    /// prepares an MDD (transient) for transfer of tiles
    virtual unsigned short startInsertTransMDD(unsigned long callingClientId,
            r_Minterval &domain,
            unsigned long typeLength, const char *typeName);
    /**
      Creates an object for tile based transfer with method insertTile.

      The first parameter is the unique client id for which the MDD should be created.
      The second parameter is the
      name of the collection to insert the MDD object. The third parameter holds the
      spatial domain of the following MDD object and \c typeLength specifies the size of
      the base type in bytes. The last one gives the type structure as string representation.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && MDD type name not found\\
      3 && MDD and its type are incompatible\\
      \end{tabular}
      \endlatexonly
    */

    /// finishes the MDD creation and inserts the MDD into the collection
    virtual unsigned short endInsertMDD(unsigned long callingClientId,
                                        int isPersistent);
    /**
      Parameters
      \latexonly
      \begin{tabular}{lll}
      \c callingClientId  && unique client id of the calling client\\
      \c isPersistent     && determines wheather it is a persistent or a transient MDD\\
      \end{tabular}
      \endlatexonly
    */

    /// insert a tile into a persistent MDD object
    virtual unsigned short insertTile(unsigned long callingClientId,
                                      bool isPersistent, RPCMarray *rpcMarray, r_Minterval *tileSize = NULL);
    /**
      Splits (if tileSize != NULL) and inserts a tile into the current MDD object.

      Parameters
      \latexonly
      \begin{tabular}{lll}
      \c callingClientId  && unique client id of the calling client\\
      \c isPersistent     && determines wheather it is a persistent or a transient tile\\
      \c rpcMarray        && RPC representation of the tile\\
      \c tileSize         && r_Minterval specifying the tile-size\\
      \end{tabular}
      \endlatexonly

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && base type name of inserting tile is not supported\\
      3 && base type does not match MDD type\\
      \end{tabular}
      \endlatexonly
    */

    // -----------------------------------------------------------------------------------------
    // Fetch query results: next MDD, scalar, tile
    // -----------------------------------------------------------------------------------------

    ///
    /// get the domain of the next MDD of the actual transfer collection
    virtual unsigned short getNextMDD(unsigned long callingClientId,
                                      r_Minterval &mddDomain, std::string &typeName, std::string &typeStructure,
                                      r_OId &oid, unsigned short &currentFormat);
    /**
      The Method gets the domain of the next MDD of the actual transfer collection.
      The first parameter is the unique client id. The second parameter returns the
      domain of the MDD to be transfered. \c typeName returns the name of the
      MDD type and its structure.
      Transfer of MDD data is tile-based using the method getNextTile.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful, at least one MDD is left in the transfer collection\\
      1 && nothing left in the transfer collection\\
      2 && client context not found, no tiles in the MDD object, no actual transfer collection \\
      \end{tabular}
      \endlatexonly
    */

    /**
     * Called by getNextElement to help handling of struct elements. It works
     * for nested structs as well. Only used in case endianess needs changing.
     */
    virtual void swapScalarElement(char *buffer, const BaseType *baseType);

    /// get the next scalar element in the actual transfer collection.
    virtual unsigned short getNextElement(unsigned long callingClientId,
                                          char *&buffer, unsigned int &bufferSize);
    /**
      The Method gets the next non-MDD element in the actual transfer collection.
      The first parameter is the unique client id. The second parameter returns a
      pointer to the memory occupied by the next element and the third one delivers
      the size of the buffer.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful, at least one element is left in the transfer collection\\
      1 && operation succesful, nothing left in the transfer collection\\
      2 && client context not found, no tiles in the MDD object, no actual transfer collection \\
      \end{tabular}
      \endlatexonly
    */

    /// get next tile of the actual MDD of the actual transfer collection
    virtual unsigned short getNextTile(unsigned long callingClientId,
                                       RPCMarray **rpcMarray);
    /**
      The Method gets the next tile of the actual MDD of the actual transfer collection.
      The first parameter is the unique client id. The second parameter is the
      RPC representation of the Marray representing the tile. If a tile is too large to be
      transferred in one piece, the data is split. To get the rest of the data, consecutively
      use this method.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful, no further MDDs are left\\
      1 && operation was successful, at least one MDD is left in the transfer collection\\
      2 && operation was successful, at least one tile is left in the actual MDD\\
      3 && operation was successful, at least one block is left in the actual tile\\
      4 && client context not found, no tiles in the MDD object, no actual transfer collection \\
        && or nothing left in the collection\\
      \end{tabular}
      \endlatexonly

      Examples of valid return value chains:
      \latexonly
      \begin{itemize}
      \item To be transferred: 1 MDD consisting of 1 tile (which is too large)\\
      \begin{verbatim}
      3 ->...-> 3 -> 0
      \end{verbatim}
      \item To be transferred: 1 MDD consisting of 2 tiles (the first is too large)\\
      \begin{verbatim}
      3 ->...-> 3 -> 2 -> 0
      |--------------|    |
          1st tile     2nd tile
      \end{verbatim}
      \item To be transferred: 2 MDDs, each consisting of 1 tile (none too large)\\
      \begin{verbatim}
      1 -> 0
      \end{verbatim}
      \item To be transferred: 3 MDDs, the first (A) consisting of 1 tile (not too large),\\
      the second (B) consisting of 2 tiles (B1, B2, of which the first is too large),
      the third (C) consisting of 2 tiles (C1, C2, of which the second is too large),
      \begin{verbatim}
      1 -> 3 ->...-> 3 -> 2 -> 1 -> 2 -> 3 ->...-> 3 -> 0
      |    |--------------|    |    |    |--------------|
      |           B1          B2    C1          C2
      |    |-------------------|    |-------------------|
      A              B                        C
      \end{verbatim}
      \end{itemize}
      \endlatexonly
    */

    /// process the client's alive signal
    virtual unsigned short endTransfer(unsigned long client);
    /**
      The method terminates a transfer session and releases all transfer structures.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successfull\\
      1 && client context not found\\
      \end{tabular}
      \endlatexonly
    */
    
    virtual void reportExecutionTimes(ClientTblElt *context);

    // -----------------------------------------------------------------------------------------
    // Collection mgmt, used by the rasodmg C++ API
    // -----------------------------------------------------------------------------------------

    ///
    /// create new MDD collection
    virtual unsigned short insertColl(unsigned long callingClientId,
                                      const char *collName, const char *typeName, r_OId &oid);
    /**
      Creates a new MDD collection.

      Parameters
      \latexonly
      \begin{tabular}{lll}
      \c callingClientId  && unique client id of the calling client\\
      \c collName         && name of the collection to be created\\
      \c typeName         && name of the collection type\\
      \c oid              && object identifier\\
      \end{tabular}
      \endlatexonly

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && collection type name not found\\
      3 && collection name exists already in the db\\
      4 && failed to create due to some other error
      \end{tabular}
      \endlatexonly
    */

    ///
    /// delete MDD collection
    virtual unsigned short deleteCollByName(unsigned long callingClientId,
                                            const char *collName);
    /**
      Deletes an MDD collection. The first parameter is the unique client id
      for which the collection should be deleted. The second parameter is the
      name for the collection to be deleted.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && collection with name does not exist\\
      \end{tabular}
      \endlatexonly
    */

    /// delete object by oid
    virtual unsigned short deleteObjByOId(unsigned long callingClientId, r_OId &oid);
    /**
      Deletes the object with \c oid.
      The first parameter is the unique client id for which the object should be
      deleted.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && object with oid does not exist\\
      \end{tabular}
      \endlatexonly
    */

    ///
    /// remove object specified by oid from collection specified by name
    virtual unsigned short removeObjFromColl(unsigned long callingClientId,
            const char *collName, r_OId &oid);
    /**
      The method removes the object with {\\t oid} from collection with \c collName.
      The first parameter is the unique client id for which the object should be removed.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && specified collection does not exist\\
      3 && specified object does not exist in the collection\\
      4 && general error\\
      \end{tabular}
      \endlatexonly
    */

    // -----------------------------------------------------------------------------------------
    // Get collection/MDD by name or oid
    // -----------------------------------------------------------------------------------------

    ///
    /// prepare an MDD collection for transfer with getNextMDD()
    virtual unsigned short getCollByName(unsigned long callingClientId,
                                         const char *collName,
                                         std::string &typeName, std::string &typeStructure, r_OId &oid);
    /**
      ATTENTION: This function is not used at the moment. It hast
      to be adapted to transferData.

      Prepares an MDD collection for transfer with getNextMDD().

      Parameters
      \latexonly
      \begin{tabular}{lll}
      \c callingClientId  && unique client id of the calling client\\
      \c collName         && name of the collection to be got\\
      \c typeName         && returns name of the collection type\\
      \c typeStructure    && returns structure of the collection type\\
      \c oid              && returns oid of the collection\\
      \end{tabular}
      \endlatexonly

      The first parameter is the unique client id. The second parameter is the
      name of the collection to get. \c typeName returns the name of the
      collection type and \c typeStructure its type structure.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful - collection has some elements\\
      1 && operation was successful - collection has no elements\\
      2 && collection is not known\\
      3 && client context not found\\
      \end{tabular}
      \endlatexonly
    */

    /// prepare an MDD collection for transfer with getNextMDD()
    virtual unsigned short getCollByOId(unsigned long callingClientId,
                                        r_OId &oid, std::string &typeName, std::string &typeStructure, std::string &collName);
    /**
      ATTENTION: This function is not used at the moment. It hast
      to be adapted to transferData.

      Prepares an MDD collection for transfer with getNextMDD.

      Parameters
      \latexonly
      \begin{tabular}{lll}
      \c callingClientId  && unique client id of the calling client\\
      \c oid              && oid of the collection to be got\\
      \c typeName         && returns name of the collection type\\
      \c typeStructure    && returns structure of the collection type\\
      \c collName         && returns name of collection\\
      \end{tabular}
      \endlatexonly

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful - collection has some elements\\
      1 && operation was successful - collection has no elements\\
      2 && collection is not known\\
      3 && client context not found\\
      \end{tabular}
      \endlatexonly
    */

    /// gets oids of the collection specified by name
    virtual unsigned short getCollOIdsByName(unsigned long callingClientId,
            const char *collName,
            std::string &typeName, std::string &typeStructure, r_OId &oid,
            RPCOIdEntry *&oidTable, unsigned int &oidTableSize);
    /**
      Gets the collection of oids of the collection with \c collName.

      Parameters
      \latexonly
      \begin{tabular}{lll}
      \c callingClientId  && unique client id of the calling client\\
      \c collName         && name of the collection to be got\\
      \c typeName         && returns name of the collection type\\
      \c typeStructure    && returns structure of the collection type\\
      \c oid              && returns object identifier\\
      \c oidTable         && returns an array of pointers to oids\\
      \c oidTableSize     && returns the no of elements in the table\\
      \end{tabular}
      \endlatexonly

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful - collection has some elements\\
      1 && operation was successful - collection has no elements\\
      2 && collection is not known\\
      3 && client context not found\\
      \end{tabular}
      \endlatexonly
    */

    /// gets oids of the collection specified by name
    virtual unsigned short getCollOIdsByOId(unsigned long callingClientId,
                                            r_OId &oid, std::string &typeName, std::string &typeStructure,
                                            RPCOIdEntry *&oidTable, unsigned int &oidTableSize, std::string &collName);
    /**
      Gets the collection of oids of the collection with \c collName.

      Parameters
      \latexonly
      \begin{tabular}{lll}
      \c callingClientId  && unique client id of the calling client\\
      \c oid              && oid of the collection to be got\\
      \c typeName         && returns name of the collection type\\
      \c typeStructure    && returns structure of the collection type\\
      \c oidTable         && returns an array of pointers to oids\\
      \c oidTableSize     && returns the no of elements in the table\\
      \c collName         && returns name of collection\\
      \end{tabular}
      \endlatexonly

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful - collection has some elements\\
      1 && operation was successful - collection has no elements\\
      2 && collection is not known\\
      3 && client context not found\\
      \end{tabular}
      \endlatexonly
    */

    /// get an MDD by OId
    virtual unsigned short getMDDByOId(unsigned long callingClientId,
                                       r_OId &oid, r_Minterval &mddDomain,
                                       std::string &typeName, std::string &typeStructure, unsigned short &currentFormat);
    /**
      The Method gets an MDD by OId \c oid. If the MDD is found, it is initialized as transfer
      object and can be picked up by getNextTile calls (tile-based transfer).

      Additionally, the method returns domain, type name, and type structure of the found MDD
      object by reference parameters.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && object with this oid not found\\
      3 && object has no tiles
      \end{tabular}
      \endlatexonly

      Communication protocol
      \latexonly
      \begin{tabular}{lll}
      getMDDByOId \\
      ->                && getNextTile \\
                        && : \\
      endTransfer \\
      \end{tabular}
      \endlatexonly
    */

    // -----------------------------------------------------------------------------------------
    // Utility methods
    // -----------------------------------------------------------------------------------------

    ///
    /// get new object identifier
    virtual unsigned short getNewOId(unsigned long callingClientId,
                                     unsigned short objType, r_OId &oid);
    /**
      Creates a new oid and gives it back by the refernce parameter \c oid.
      \c objType determines the type of object for which that oid is allocated. The folowing
      values are supported: 1 = MDD,  2 = Collection.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && error while creating oid\\
      \end{tabular}
      \endlatexonly
    */

    /// get type of object by oid
    virtual unsigned short getObjectType(unsigned long callingClientId,
                                         r_OId &oid, unsigned short &objType);
    /**
      Determines the type of the object indicated by \c oid. The type is returned by the
      reference parameter \c objType. The folowing types are supported: 1 = MDD,  2 = Collection.

      Return values:
      \latexonly
      \begin{tabular}{lll}
      0 && operation was successful\\
      1 && client context not found\\
      2 && oid not found\\
      \end{tabular}
      \endlatexonly
    */

    /// get type structure of a type name
    virtual unsigned short getTypeStructure(unsigned long callingClientId,
                                            const char *typeName, unsigned short typeType, std::string &typeStructure);
    /**
      Determines the type structure of the type specified by \c typeName. The type
    either can be a set type (typeType=1), an mdd type (typeType=2), or a base type
    (typeType = 3).

           Return values:
           \latexonly
           \begin{tabular}{lll}
           0 && operation was successful\\
           1 && client context not found\\
           2 && type name not found\\
           \end{tabular}
           \endlatexonly
         */

    /// set the data format used for transferring data to the client
    virtual unsigned short setTransferMode(unsigned long callingClientId,
                                           unsigned short format, const char *formatParams);
    /**
    Sets the data format used by the server to transfer data to the client to
    format which is of type r_Data_Format.

    Return values:
    \latexonly
    \begin{tabular}{lll}
    0 && operation was successful\\
    1 && client context not found\\
    2 && unknown or unsupported data format\\
    \end{tabular}
    \endlatexonly
         */

    /// set the data format for storing data into the database
    virtual unsigned short setStorageMode(unsigned long callingClientId,
                                          unsigned short format, const char *formatParams);
    /**
    return values exactly like setTransferMode()
    */
    
    void setAdmin(AdminIf *newAdmin);

    static const int RESPONSE_ERROR;
    static const int RESPONSE_MDDS;
    static const int RESPONSE_SCALARS;
    static const int RESPONSE_INT;
    static const int RESPONSE_OID;
    static const int RESPONSE_OK_NEGATIVE;
    static const int RESPONSE_OK;

    static const unsigned short EXEC_RESULT_MDDS = 0;
    static const unsigned short EXEC_RESULT_SCALARS = 1;
    static const unsigned short EXEC_RESULT_EMPTY = 2;
    static const unsigned short EXEC_RESULT_PARSE_ERROR = 4;
    static const unsigned short EXEC_RESULT_EXEC_ERROR = 5;

    static constexpr unsigned short RC_OK = 0;
    static constexpr unsigned short RC_CLIENT_NOT_FOUND = 1;
    static constexpr unsigned short RC_ERROR = 2;

    static const int ENDIAN_BIG;
    static const int ENDIAN_LITTLE;

protected:
    /// make sure a tile has the correct data format, converting if necessary
    static int ensureTileFormat(r_Data_Format &hasFmt, r_Data_Format needFmt,
                                const r_Minterval &dom, const BaseType *type, char *&data, r_Bytes &size,
                                int repack, int owner, const char *params = NULL);

    // parse the query, return true if all fine
    bool parseQuery(const char *query);

    /// init fields of res to 0
    static void resetExecuteQueryRes(ExecuteQueryRes &res);
    static void resetExecuteUpdateRes(ExecuteUpdateRes &res);
    /// free fields of res
    static void cleanExecuteQueryRes(ExecuteQueryRes &res);
    /// return type name and type structure of the first transfer element in context
    std::pair<std::string, std::string> getTypeNameStructure(ClientTblElt *context) const;
    unsigned short handleExecuteQueryResult(ClientTblElt *context, unsigned short returnValue, 
                                            ExecuteQueryRes &returnStructure) const;
    unsigned short getTransferCollInfo(
        ClientTblElt *context, r_OId &oid, std::string &typeName, std::string &typeStructure, MDDColl *coll) const;

    /// pointer to the actual administration interface object
    AdminIf *admin{NULL};

    /// flag for active db transaction (stores the clientID of the owner of the active transaction,
    /// or 0 if none open)
    unsigned long transactionActive{0};
};

#endif
