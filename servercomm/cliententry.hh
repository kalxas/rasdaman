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

#ifndef _CLIENTENTRY_
#define _CLIENTENTRY_

#include "raslib/mddtypes.hh"
#include "reladminif/transactionif.hh"
#include "reladminif/databaseif.hh"
#include <vector>

class QtData;
class MDDObj;
class MDDCollIter;
class MDDColl;
class Tile;
class RMTimer;
class r_Parse_Params;

/// the class defines an entry of the client table
class ClientTblElt
{
public:
    /// default constructor
    ClientTblElt(const char *clientIdText, unsigned long clientId);
    /**
      Default constructor that takes the information to be placed in the
    clientIdText field of the client table entry and the unique ID to
    be placed in the clientId field.
    */

    ClientTblElt(const ClientTblElt &) = delete;

    /// destructor
    ~ClientTblElt();

    /// release client context
    void release();

    /**
      Releasing the client context means to decrease the currentUsers counter
      and to update lastActionTime.
    */

    /// releases transfer collection/iterator
    void releaseTransferStructures();
    /**
      The method releases transfer collection and iterator. As the collection is a
      persistent one, care has to be taken that creation and deletion is done
      within the same transaction.
    */

    /// unique client identification assigned by the server
    unsigned long clientId;

    /// counter indicating the number of current users
    unsigned int currentUsers{0};

    /// binding information about the client (IP address and TCP port number)
    char *clientIdText{NULL};

    /// Name of the client user name (if available)
    char *userName{NULL};

    /// Name of the actual database (if one is open)
    char *baseName{NULL};

    /// time when the database was opened (for curiosity purposes)
    unsigned long creationTime{0};

    /// time of the client's last action (for garbage collection purposes)
    unsigned long lastActionTime{0};

    /// convert raw array data to this data format before transfer
    r_Data_Format transferFormat{r_Array};
    char *transferFormatParams{0};
    /// send data to client in the exact transfer format
    int exactFormat{1};
    /// store array data in this data format in the database
    r_Data_Format storageFormat{r_Array};
    char *storageFormatParams{0};

    /// the tile data converted into the transfer format, if required
    void *encodedData{0};
    unsigned long encodedSize{0};
    /// for establishing the compression ratio
    unsigned long totalRawSize{0};
    unsigned long totalTransferedSize{0};

    /// pointer to an MDD collection
    MDDColl *transferColl{0};
    /**
       For collection of MDD constants with an update query.
    */

    /// pointer to an iterator for collection transferColl
    MDDCollIter *transferCollIter{0};

    /// pointer to the query result which is currently in transfer
    std::vector<QtData *> *transferData{0};
    /**
       For the result of the last query (NULL if the result is completely delivered to the client).
    */

    /// point to an iterator for transfer data
    std::vector<QtData *>::iterator *transferDataIter{0};

    /// pointer to a persistent MDD object for tile based transfers
    MDDObj *assembleMDD{0};

    /// pointer to an MDD object for tile base transfer
    MDDObj *transferMDD{0};

    /// std::vector storing tiles of actual MDD for transfer
    std::vector<Tile *> *transTiles{0};

    /// iterator for the std::vector above
    std::vector<Tile *>::iterator *tileIter{0};

    /// std::vector storing pointers to transient tiles
    std::vector<Tile *> *deletableTiles{0};
    /**
      The tiles referenced by these pointers are border tiles dynamically created in getNextMDD().
      They do not belong to any MDD object, and, therefore, they have to be deleted explicitly.
    */

    /// bytes to transfer in actual tile (valid only if tile is larger than {\tt MAXTRANSBYTES})
    unsigned long bytesToTransfer{0};

    /// std::vector of persistent MDD collections in use
    std::vector<MDDColl *> *persMDDCollections{0};

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

    /// parameter object
    r_Parse_Params *clientParams{0};

};

#endif
