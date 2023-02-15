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

#ifndef _CLIENTENTRY_HH
#define _CLIENTENTRY_HH

#include "raslib/mddtypes.hh"
#include "reladminif/transactionif.hh"
#include "reladminif/databaseif.hh"
#include "common/util/timer.hh"
#include <vector>

class QtData;
class MDDObj;
class MDDCollIter;
class MDDColl;
class Tile;
class RMTimer;
class r_Parse_Params;

enum class ClientType
{
    Invalid, // invalid client
    Http,    // requests routed via HttpServer
    Regular  // requests go directly to ServerComm
};

/// the class defines an entry of the client table
class ClientTblElt
{
public:

    ClientTblElt(ClientType clientTypeArg, std::uint32_t clientId);

    ClientTblElt(const ClientTblElt &) = delete;

    ~ClientTblElt();

    /// releases transfer collection/iterator; as the collections are persistent,
    /// creation and deletion must be done within the same transaction.
    void releaseTransferStructures();

    /// unique client identification assigned by the server
    std::uint32_t clientId;

    /// client type
    ClientType clientType{ClientType::Invalid};

    /// Name of the client user name (if available)
    char *userName{NULL};

    /// Name of the actual database (if one is open)
    char *baseName{NULL};

    char *transferFormatParams{0};
    char *storageFormatParams{0};

    /// pointer to a collection of MDD constants with an update query
    MDDColl *transferColl{0};
    /// pointer to an iterator for collection transferColl
    MDDCollIter *transferCollIter{0};

    /// pointer to the query result which is currently in transfer;
    /// it is NULL if the result is completely delivered to the client.
    /// used in executeQuery, getNext* methods, and executeInsert (to hold the oid of inserted MDD)
    std::vector<QtData *> *transferData{0};
    /// point to an iterator for transfer data
    std::vector<QtData *>::iterator *transferDataIter{0};

    /// pointer to a persistent MDD object to be inserted, e.g. in startInsertPersMDD / insertTile
    MDDObj *assembleMDD{0};
    /// pointer to an MDD object to be transferred, e.g. with getMDDByOId(..), startInsertTransMDD / insertTile
    MDDObj *transferMDD{0};

    /// std::vector storing tiles of actual MDD for transfer
    std::vector<Tile *> *transTiles{0};
    /// iterator for the std::vector above
    std::vector<Tile *>::iterator *tileIter{0};

    /// the tiles referenced by these pointers are border tiles dynamically created in getNextMDD().
    /// They do not belong to any MDD object, and, therefore, they have to be deleted explicitly.
    std::vector<Tile *> *deletableTiles{0};

    /// std::vector of persistent collections in use during query evaluation, in particular in QtMDDAccess
    std::vector<MDDColl *> *persColls{0};

    /// object representing the actual database
    DatabaseIf database;

    /// object representing the actual transaction (only one at a time possible)
    TransactionIf transaction;

    /// pointer to a timer for recording transaction time
    RMTimer *taTimer{0};

    /// pointer to a timer for recording transfer time
    RMTimer *transferTimer{0};

    /// pointer to a timer for recording evaluation time
    RMTimer *evaluationTimer{0};

    /// timer for recording execution
    common::Stopwatch timer;
    //timer saved for evaluation
    double evaluationTime = 0;
    
    /// parameter object
    r_Parse_Params *clientParams{0};
    
    /// the tile data converted into the transfer format, if required
    void *encodedData{0};
    unsigned long encodedSize{0};
    /// bytes remaining to transfer from tile if it is larger than \c maxTransferBufferSize. used in getNextTile
    unsigned long bytesToTransfer{0};
    /// for establishing the compression ratio
    unsigned long totalRawSize{0};
    unsigned long totalTransferedSize{0};
    
#ifdef RASDEBUG
    /// time when the database was opened (for debugging purposes)
    unsigned long creationTime{0};
#endif
    
    /// send data to client in the exact transfer format
    int exactFormat{1};
    /// convert raw array data to this data format before transfer
    r_Data_Format transferFormat{r_Array};
    /// store array data in this data format in the database
    r_Data_Format storageFormat{r_Array};
    /// if true, feedback will be printed with info level in endTransfer
    bool reportTransferedSize{false};

};

#endif
