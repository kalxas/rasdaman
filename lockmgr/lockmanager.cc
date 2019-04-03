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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/*************************************************************
 *
 * INCLUDE: lockmanager.cc
 *
 * MODULE: lockmgr
 *
 * PURPOSE: Contains the C++-part of the lockmanager implementation.
 *
 ************************************************************/

#include "mymalloc/mymalloc.h"
#include <string.h>
#include "lockmanager.hh"
#include "reladminif/adminif.hh"
#include "reladminif/databaseif.hh"
#include "raslib/error.hh"
#include "server/rasserver_config.hh"
#include <stdlib.h>
#include "debug.hh"
#include "raslib/rmdebug.hh"
#include <logging.hh>

/**
 * This is the C++-part implementation of the lock manager.
 */

// Global private static pointer used to ensure a single instance of the class,
// originally set to NULL
LockManager* LockManager::LM_instance = NULL;

/**
 * Private copy constructor such that it cannot be called from the outside.
 *
 * This constructor calls the default constructor which does not do anything.
 *
 * @param mgr
 *     object of the class as parameter of the copy constructor
 */
LockManager::LockManager(__attribute__((unused)) LockManager const& mgr)
{
    LockManager();
}

/**
 * Private destructor such that it cannot be called from the outside.
 *
 * This destructor disconnects the lockmanager from the database.
 */
LockManager::~LockManager()
{
    LM_instance->disconnect();
}

/**
 * Private default constructor such that it cannot be called from the outside.
 *
 * The constructor itself does not do anything besides setting the connection
 * name of the lockmanager to "lockmgrConn".
 */
LockManager::LockManager()
{
    connectionName  = "lockmgrConn";
}

/**
 * Function for creating and returning an instance of the class.
 *
 * This function is called to create an instance of the class.
 * Calling the constructor publicly is not allowed. The constructor
 * is private and is only called by this Instance function.
 * This function is also responsible for establishing the lockmanager's
 * connection to the database.
 *
 * @return a pointer to the created instance of the class
 */
LockManager* LockManager::Instance()
{
    RMTIMER("LockManager", "Instance");
    // Allow only one instance of class to be generated
    if (!LM_instance)
    {
        LDEBUG << "Lock manager: new instance";
        LM_instance = new LockManager();
        LM_instance->ecpg_LockManager = ECPG_LockManager::Instance();
        LM_instance->connect();
    }
    return LM_instance;
}

/**
 * Function for implementing the connection to the database,
 * where the lock table will be stored.
 *
 * If the connection cannot be established a corresponding error
 * will be thrown.
 */
void LockManager::connect()
{
    const char* dbConnectionId;
    const char* dbUser;
    const char* dbPassword;
    if (configuration.getDbConnectionID() != NULL)
    {
        dbConnectionId = configuration.getDbConnectionID();
        dbUser = configuration.getDbUser();
        dbPassword = configuration.getDbPasswd();
    }
    else
    {
        dbConnectionId = static_cast<const char*>("RASBASE:5432");
        dbUser = NULL;
        dbPassword = NULL;
    }
    bool connect_ok = ecpg_LockManager->connect(dbConnectionId, connectionName, dbUser, dbPassword);
    if (!connect_ok)
    {
        LERROR << "Error: Lock manager -- Database is not connected.";
        throw r_Error(r_Error::r_Error_DatabaseClosed, 211);
    }
}

/**
 * Function for implementing the disconnection from the database,
 * where the lock table will be stored.
 *
 * If the disconnection cannot be realized a corresponding error
 * will be thrown.
 */
void LockManager::disconnect()
{
    bool disconnect_ok = ecpg_LockManager->disconnect(connectionName);
    if (!disconnect_ok)
    {
        LERROR << "Error: Lock manager -- Database cannot be disconnected.";
        throw r_Error(r_Error::r_Error_DatabaseClosed, 211);
    }
}

/**
 * Function for beginning a transaction within the database via the lockmanager's own connection.
 *
 * Calls the corresponding function from the ECPG class with the connectionName as parameter.
 */
void LockManager::beginTransaction()
{
    RMTIMER("LockManager", "beginTransaction");
    ecpg_LockManager->beginTransaction(connectionName);
}

/**
 * Function for ending a transaction within the database via the lockmanager's own connection.
 *
 * Calls the corresponding function from the ECPG class with the connectionName as parameter.
 */
void LockManager::endTransaction()
{
    RMTIMER("LockManager", "endTransaction");
    ecpg_LockManager->endTransaction(connectionName);
}

/**
 * Function for creating a lock in the lock table.
 *
 * Depending on the lock type either lockTileExclusive or lockTileShared
 * from the ECPG class is called.
 * If the locking is unsuccessful then a corresponding error will be thrown.
 *
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileId
 *     the id corresponding to the tile to be locked
 * @param pLockType
 *     the type of the lock which is either shared or exclusive
 */
void LockManager::lockTileInternal(const char* pRasServerId, OId::OIdCounter pTileId, enum Lock pLockType)
{
    RMTIMER("LockManager", "lockTileInternal");
    bool result = true;
    if (pLockType == EXCLUSIVE_LOCK)
    {
        RMTIMER("LockManager", "lockTileInternal, exclusive");
        ecpg_LockManager->lockTileExclusive(connectionName, pRasServerId, pTileId);
        result = ecpg_LockManager->isTileLockedExclusive(connectionName, pRasServerId, pTileId);
    }
    else if (pLockType == SHARED_LOCK)
    {
        RMTIMER("LockManager", "lockTileInternal, shared");
        ecpg_LockManager->lockTileShared(connectionName, pRasServerId, pTileId);
        result = ecpg_LockManager->isTileLockedShared(connectionName, pRasServerId, pTileId);
    }
    if (!result)
    {
        LERROR << "Error: Lock manager -- Tile cannot be locked.";
        throw r_Error(r_Error::r_Error_TileCannotBeLocked, 4000);
    }
}

/**
 * Function for deleting a lock from the lock table for a specific tile.
 *
 * The corresponding function from the ECPG class is called.
 *
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileId
 *     the id corresponding to the tile to be unlocked
 */
void LockManager::unlockTileInternal(const char* pRasServerId, OId::OIdCounter pTileId)
{
    ecpg_LockManager->unlockTile(connectionName, pRasServerId, pTileId);
}

/**
 * Function for deleting all locks from the lock table corresponding to a specific rasserver.
 *
 * The corresponding function from the ECPG class is called.
 *
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 */
void LockManager::unlockAllTilesInternal(const char* pRasServerId)
{
    RMTIMER("LockManager", "unlockAllTilesInternal");
    ecpg_LockManager->unlockAllTiles(connectionName, pRasServerId);
}

/**
 * Function for checking if a specific tile is locked or not with a specific lock type.
 *
 * The corresponding functions isTileLockedExclusive and isTileLockedShared from
 * the ECPG class is called.
 *
 * @param pTileId
 *     the id corresponding to the tile to be checked
 *
 * @return a bool value corresponding to the fact that the tile is locked or not
 */
bool LockManager::isTileLockedInternal(OId::OIdCounter pTileId, enum Lock pLockType)
{
    RMTIMER("LockManager", "isTockTiledInternal");
    bool result = true;
    if (pLockType == EXCLUSIVE_LOCK)
    {
        ecpg_LockManager->isTileLockedExclusive(connectionName, (char*)NULL, pTileId);
    }
    else if (pLockType == SHARED_LOCK)
    {
        ecpg_LockManager->isTileLockedShared(connectionName, (char*)NULL, pTileId);
    }
    return result;
}

/**
 * Function for clearing the locks from the lock table corresponding to a specific rasserver.
 *
 * The corresponding function from the ECPG class is called.
 *
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 */
void LockManager::clearLockTableInternal(const char* pRasServerId)
{
    ecpg_LockManager->clearLockTable(connectionName, pRasServerId);
}

/**
 * Function for generating the id of the rasserver consisting of
 * the IP address of the corresponding rasmanager, its port, the name of
 * server and its port, all four separated by hyphen.
 *
 * The IP address, port, servername and port are fetched with the
 * help of the configuration object.
 *
 * @param pResultRasServerId
 *      generated rasserver id returned by reference
 */
void LockManager::generateServerId(char* pResultRasServerId)
{
    char* serverName;
    int port;
    char* rasmgrHost;
    int rasmgrPort;
    if (configuration.getServerName() != NULL)
    {
        serverName = const_cast<char*>(configuration.getServerName());
        port = configuration.getListenPort();
    }
    else
    {
        serverName = const_cast<char*>("defaultServer");
        port = 0;
    }
    if (configuration.getRasmgrHost() != NULL)
    {
        rasmgrHost = const_cast<char*>(configuration.getRasmgrHost());
        rasmgrPort = configuration.getRasmgrPort();
    }
    else
    {
        rasmgrHost = const_cast<char*>("defaultRasmgrHost");
        rasmgrPort = 0;
    }
    int return_code = snprintf(pResultRasServerId, 255, "%s-%d-%s-%d", rasmgrHost, rasmgrPort, serverName, port);
    if ((return_code >= 0) && (return_code < 255))
    {
        LDEBUG << "Lock manager, generateServerId: id = " << pResultRasServerId;
    }
    else if (return_code >= 255)
    {
        LDEBUG << "Lock manager, generateServerId: concatenation was successful but the result is too long and was truncated.";
    }
    else
    {
        LERROR << "Error: Lock manager, generateServerId -- concatenation of id components failed.";
        throw r_Error(r_Error::r_Error_General);
    }
}

/**
 * Function for returning the locktype of the current operation
 * by checking the variable readOnly from AdminIf.
 *
 * @return enum Lock which represents the locktype of the current operation
 */
enum Lock LockManager::generateLockType()
{
    enum Lock lockType;
    bool isTAReadOnly = AdminIf::isReadOnlyTA();
    if (isTAReadOnly)
    {
        lockType = SHARED_LOCK;
        LDEBUG << "Lock manager, lock type: shared";
    }
    else
    {
        lockType = EXCLUSIVE_LOCK;
        LDEBUG << "Lock manager, lock type: exclusive";
    }
    return lockType;
}

/**
 * Compare function to be passed to qsort from stdlib.h which sorts
 * the set of potentially different tile ids.
 *
 * @param a
 *     pointer to the first object to be compared
 * @param b
 *     pointer to the second object to be compared
 *
 * @return int representing -1 if first smaller and second,
 * 0 if equal and 1 if first greater than second
 */
int LockManager::compareIds(const void* a, const void* b)
{
    const long long* pa = static_cast<const long long*>(a);
    const long long* pb = static_cast<const long long*>(b);
    if (*pa < *pb)
    {
        return -1;
    }
    else if (*pa == *pb)
    {
        return 0;
    }
    return 1;
}

/*
 * Private function for locking multiple tile ids at once.
 * Instead of one query (one entry in the lock table) per lock,
 * the function determines all possible intervals of tile ids
 * (without holes) such that the whole interval will be locked
 * with one single query.
 *
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileIdsToLock
 *     pointer to a memory location storing a sequence of tile ids
 *     which have to be locked
 * @param dim
 *     integer dimension of the array of tile ids to be locked
 * @param pLockType
 *     enum type variable corresponding to the lock type (shared or exclusive)
 */
void LockManager::lockTilesInternal(const char* pRasServerId, long long* pTileIdsToLock, int dim, enum Lock pLockType)
{
    if (dim == 1)
    {
        LDEBUG << "Lock manager, lock tiles internal: Only one tile to lock";
        lockTileInternal(pRasServerId, pTileIdsToLock[0], pLockType);
    }
    else if (dim > 1)
    {
        LDEBUG << "Lock manager, lock tiles internal: Multiple tiles to lock";
        // sort the array of tile ids to lock
        qsort(pTileIdsToLock, static_cast<size_t>(dim), sizeof(long long), LockManager::compareIds);
        int beginIndex, endIndex;
        beginIndex = 0;
        endIndex = beginIndex;
        // identify holes (i.e., identify intervals)
        for (int i = 0; i < dim - 1; i++)
        {
            if (pTileIdsToLock[i + 1] - pTileIdsToLock[i] > 1)
            {
                endIndex = i;
                // an interval has to have at least 10 elements to use bulk locking
                if (endIndex - beginIndex >= 10)
                {
                    LDEBUG << "Lock manager, lock tiles internal: performing interval locking";
                    long long begin = pTileIdsToLock[beginIndex];
                    long long end = pTileIdsToLock[endIndex];
                    if (pLockType == SHARED_LOCK)
                    {
                        ecpg_LockManager->lockTilesShared(connectionName, pRasServerId, begin, end);
                    }
                    else if (pLockType == EXCLUSIVE_LOCK)
                    {
                        ecpg_LockManager->lockTilesExclusive(connectionName, pRasServerId, begin, end);
                    }
                }
                else
                {
                    LDEBUG << "Lock manager, lock tiles internal: performing locking one by one";
                    for (int j = beginIndex; j <= endIndex; j++)
                    {
                        lockTileInternal(pRasServerId, pTileIdsToLock[j], pLockType);
                    }
                }
                beginIndex = i + 1;
                endIndex = beginIndex;
            }
        }
        // for the case of an array with consecutive tile ids and for the last consecutive block of ids within the array
        if (endIndex < dim - 1)
        {
            endIndex = dim - 1;
            if (endIndex - beginIndex >= 10)
            {
                LDEBUG << "Lock manager, lock tiles internal: performing interval locking";
                long long begin = pTileIdsToLock[beginIndex];
                long long end = pTileIdsToLock[endIndex];
                if (pLockType == SHARED_LOCK)
                {
                    ecpg_LockManager->lockTilesShared(connectionName, pRasServerId, begin, end);
                }
                else if (pLockType == EXCLUSIVE_LOCK)
                {
                    ecpg_LockManager->lockTilesExclusive(connectionName, pRasServerId, begin, end);
                }
            }
            else
            {
                LDEBUG << "Lock manager, lock tiles internal: performing locking one by one";
                for (int j = beginIndex; j <= endIndex; j++)
                {
                    lockTileInternal(pRasServerId, pTileIdsToLock[j], pLockType);
                }
            }
        }
    }
}

/*
 * Function for locking multiple tile ids at once.
 * This function calls the private function lockTilesInternal.
 *
 * @param pTileIdsToLock
 *     array of tile ids which need to be locked (quickly, not one to one)
 * @param dim
 *     dimension of the array of tile ids
 */
void LockManager::lockTiles(long long pTileIdsToLock[], int dim)
{
    RMTIMER("LockManager", "lockTiles");
    if (dim > 0)
    {
        LDEBUG << "Lock manager, lock tiles: locking...";
        char rasServerId[255];
        generateServerId(rasServerId);
        LDEBUG << "server id=" << rasServerId;
        enum Lock lockType = generateLockType();
        beginTransaction();
        lockTilesInternal(rasServerId, pTileIdsToLock, dim, lockType);
        endTransaction();
    }
    else
    {
        LDEBUG << "Lock manager, lock tiles: no tiles to lock";
    }
}

/**
 * Function for locking multiple tiles.
 *
 * The internal function for locking is called which
 * minimizes the number of queries needed to add multiple
 * locks into the lock table.
 *
 * @param tiles
 *     vector of tiles to be locked
 */
void LockManager::lockTiles(std::vector<boost::shared_ptr<Tile>>* tiles)
{
    RMTIMER("LockManager", "lockTiles");
    if (tiles)
    {
        LDEBUG << "Lock manager, lock tiles: locking...";
        char rasServerId[255];
        generateServerId(rasServerId);
        LDEBUG << "server id=" << rasServerId;
        enum Lock lockType = generateLockType();
        // this iterates over the tiles of an object
        // if objects consists of one tile like in mr, mr2, rgb then this for is executed once
        int dim = tiles->size();
        long long* tileIdsToLock = new long long[dim];
        int i = 0;
        for (std::vector<boost::shared_ptr<Tile>>::iterator tileIterator = tiles->begin(); tileIterator != tiles->end(); tileIterator++)
        {
            DBTileId dbTileId = (*tileIterator)->getDBTile();
            OId objId = dbTileId.getObjId();
            OId::OIdCounter oid = objId.getCounter();
            tileIdsToLock[i] = oid;
            i++;
        }
        lockTiles(tileIdsToLock, dim);
        delete[] tileIdsToLock;
    }
    else
    {
        LDEBUG << "Lock manager, lock tiles: no tiles to lock";
    }
}

/**
 * Function for locking a tile by a server.
 *
 * The internal locking function is called.
 *
 * @param pTile
 *     pointer to the tile to be locked
 */
void LockManager::lockTile(Tile* pTile)
{
    DBTileId dbTileId = pTile->getDBTile();
    OId::OIdCounter oid = dbTileId.getObjId().getCounter();
    if (oid > 0)
    {
        LDEBUG << "Lock manager, lock tile: Locking tile " << oid;
        char rasServerId[255];
        generateServerId(rasServerId);
        enum Lock lockType = generateLockType();
        beginTransaction();
        lockTileInternal(rasServerId, oid, lockType);
        endTransaction();
    }
}

/**
 * Function for unlocking a tile locked by a specific server.
 *
 * The internal function for unlocking a specific tile is called.
 *
 * @param pTile
 *     pointer to the tile to be unlocked
 */
void LockManager::unlockTile(Tile* pTile)
{
    DBTileId dbTileId = pTile->getDBTile();
    OId::OIdCounter oid = dbTileId.getObjId().getCounter();
    if (oid > 0)
    {
        LDEBUG << "Lock manager, unlock tile: Locking tile " << oid;
        char rasServerId[255];
        generateServerId(rasServerId);
        beginTransaction();
        unlockTileInternal(rasServerId, oid);
        endTransaction();
    }
}

/**
 * Function for unlocking all tiles locked by a specific server.
 *
 * The internal function for unlocking all tiles is called.
 */
void LockManager::unlockAllTiles()
{
    RMTIMER("LockManager", "unlockAllTiles");
    char rasServerId[255];
    generateServerId(rasServerId);
    LDEBUG << "Lock manager, unlock all tiles: unlocking all tiles for " << ", server id=" << rasServerId;
    beginTransaction();
    unlockAllTilesInternal(rasServerId);
    endTransaction();
}

/**
 * Function for checking if a specific tile has a specific lock set.
 *
 * The internal function isTileLockedInternal is called.
 *
 * @param pTile
 *     pointer to the tile to be checked for having a lock
 * @param lockType
 *     type of the lock to be checked for (shared or exclusive)
 * @return a bool value corresponding to locked or not locked
 */
bool LockManager::isTileLocked(Tile* pTile, enum Lock lockType)
{
    bool locked = false;
    DBTileId dbTileId = pTile->getDBTile();
    OId::OIdCounter oid = dbTileId.getObjId().getCounter();
    if (oid > 0)
    {
        LDEBUG << "Lock manager, is tile locked: Checking lock for " << oid;
        beginTransaction();
        locked = isTileLockedInternal(oid, lockType);
        endTransaction();
    }
    return locked;
}

/**
 * Function for deleting all locks from the lock table corresponding to a rasserver.
 *
 * The internal function for clearing the lock table is called.
 */
void LockManager::clearLockTable()
{
    try
    {
        beginTransaction();
        char rasServerId[255];
        generateServerId(rasServerId);
        clearLockTableInternal(rasServerId);
        endTransaction();
        LDEBUG << "ok";
    }
    catch (...)
    {
        LERROR << "Unspecified exception." ;
    }
}
