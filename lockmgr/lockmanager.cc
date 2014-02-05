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
#include "raslib/rminit.hh"
#include "reladminif/adminif.hh"
#include "reladminif/databaseif.hh"
#include "raslib/error.hh"
#include "server/rasserver_config.hh"
#include <string.h>
#include <stdlib.h>
#include "debug.hh"

#define MSG_OK          "ok"
#define MSG_FAILED      "failed"

/**
 * This is the C++-part implementation of the lock manager.
 */

// Global private static pointer used to ensure a single instance of the class,
// originally set to NULL
LockManager * LockManager::LM_instance = NULL;

/**
 * Private copy constructor such that it cannot be called from the outside.
 *
 * This constructor calls the default constructor which does not do anything.
 *
 * @param mgr
 *     object of the class as parameter of the copy constructor
 */
LockManager::LockManager(LockManager const& mgr)
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
 * Private = operator such that it cannot be called from the outside.
 *
 * The overloading of this operator calls the default constructor which does not do anything.
 *
 * @param mgr
 *     object of the class as parameter of the = operator
 */
LockManager& LockManager::operator=(LockManager const& mgr)
{
    LockManager();
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
LockManager * LockManager::Instance()
{
    // Allow only one instance of class to be generated
    if (!LM_instance)
    {
        TALK( "Lock manager: new instance" );
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
    const char * dbConnectionId;
    const char * dbUser;
    const char * dbPassword;
    if (configuration.getDbConnectionID() != NULL)
    {
        dbConnectionId = configuration.getDbConnectionID();
        dbUser = configuration.getDbUser();
        dbPassword = configuration.getDbPasswd();
    }
    else
    {
        dbConnectionId = (const char *)"RASBASE:5432";
        dbUser = NULL;
        dbPassword = NULL;
    }
    bool connect_ok = ecpg_LockManager->connect(dbConnectionId, connectionName, dbUser, dbPassword);
    if (!connect_ok)
    {
        TALK( "Lock manager: Database is not connected!" );
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
        TALK( "Lock manager: Database cannot be disconnected!" );
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
    ENTER( "Lock manager: begin transaction" );
    ecpg_LockManager->beginTransaction(connectionName);
    LEAVE( "Lock manager: begin transaction" );
}

/**
 * Function for ending a transaction within the database via the lockmanager's own connection.
 *
 * Calls the corresponding function from the ECPG class with the connectionName as parameter.
 */
void LockManager::endTransaction()
{
    ENTER( "Lock manager: end transaction" );
    ecpg_LockManager->endTransaction(connectionName);
    LEAVE( "Lock manager: end transaction" );
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
 * @param pRasClientId
 *     the string corresponding to the id of the corresponding client to the rasserver
 * @param pTileId
 *     the id corresponding to the tile to be locked
 * @param pLockType
 *     the type of the lock which is either shared or exclusive
 */
void LockManager::lockTileInternal(const char * pRasServerId, const char * pRasClientId, OId::OIdCounter pTileId, enum Lock pLockType)
{
    ENTER( "Lock manager: lock tile" );
    bool result;
    if(pLockType == EXCLUSIVE_LOCK)
    {
        ecpg_LockManager->lockTileExclusive(connectionName, pRasServerId, pRasClientId, pTileId);
        result = ecpg_LockManager->isTileLockedExclusive(connectionName, pRasServerId, pRasClientId, pTileId);
    }
    else if(pLockType == SHARED_LOCK)
    {
        ecpg_LockManager->lockTileShared(connectionName, pRasServerId, pRasClientId, pTileId);
        result = ecpg_LockManager->isTileLockedShared(connectionName, pRasServerId, pRasClientId, pTileId);
    }
    if (!result)
    {
        TALK( "Lock manager, lock tile: Tile cannot be locked!" );
        throw r_Error(r_Error::r_Error_TileCannotBeLocked, 4000);
    }
    LEAVE( "Lock manager: lock tile" );
}

/**
 * Function for deleting a lock from the lock table for a specific tile.
 *
 * The corresponding function from the ECPG class is called.
 *
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pRasClientId
 *     the string corresponding to the id of the corresponding client to the rasserver
 * @param pTileId
 *     the id corresponding to the tile to be unlocked
 */
void LockManager::unlockTileInternal(const char * pRasServerId, const char * pRasClientId, OId::OIdCounter pTileId)
{
    ENTER( "Lock manager: unlock tile" );
    ecpg_LockManager->unlockTile(connectionName, pRasServerId, pRasClientId, pTileId);
    LEAVE( "Lock manager: unlock tile" );
}

/**
 * Function for deleting all locks from the lock table corresponding to a specific rasserver and specific client.
 *
 * The corresponding function from the ECPG class is called.
 *
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pRasClientId
 *     the string corresponding to the id of the corresponding client to the rasserver
 */
void LockManager::unlockAllTilesInternal(const char * pRasServerId, const char * pRasClientId)
{
    ENTER( "Lock manager: unlock all tiles" );
    ecpg_LockManager->unlockAllTiles(connectionName, pRasServerId, pRasClientId);
    LEAVE( "Lock manager: unlock all tiles" );
}

/**
 * Function for checking if a specific tile is locked or not with a specific lock type.
 *
 * The corresponding functions isTileLockedExclusive and isTileLockedShared from
 * the ECPG class is called.
 *
 * @param pTileId
 *     the id corresponding to the tile to be checked
 * @return a bool value corresponding to the fact that the tile is locked or not
 */
bool LockManager::isTileLockedInternal(OId::OIdCounter pTileId, enum Lock pLockType)
{
    ENTER( "Lock manager, is tile locked" );
    bool result;
    if(pLockType == EXCLUSIVE_LOCK)
    {
        ecpg_LockManager->isTileLockedExclusive(connectionName, (char *)NULL, (char *)NULL, pTileId);
    }
    else if(pLockType == SHARED_LOCK)
    {
        ecpg_LockManager->isTileLockedShared(connectionName, (char *)NULL, (char *)NULL, pTileId);
    }
    LEAVE( "Lock manager, is tile locked" );
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
void LockManager::clearLockTableInternal(const char *pRasServerId)
{
    ENTER( "Lock manager: clear lock table" );
    ecpg_LockManager->clearLockTable(connectionName, pRasServerId);
    LEAVE( "Lock manager: clear lock table" );
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
void LockManager::generateServerId(char * pResultRasServerId)
{
    char * serverName;
    int port;
    char * rasmgrHost;
    int rasmgrPort;
    if (configuration.getServerName() != NULL)
    {
        serverName = (char *)configuration.getServerName();
        port = configuration.getListenPort();
    }
    else
    {
        serverName = (char *)"defaultServer";
        port = 0;
    }
    if (configuration.getRasmgrHost() != NULL)
    {
        rasmgrHost = (char *)configuration.getRasmgrHost();
        rasmgrPort = configuration.getRasmgrPort();
    }
    else
    {
        rasmgrHost = (char *)"defaultRasmgrHost";
        rasmgrPort = 0;
    }
    int return_code = snprintf(pResultRasServerId, 255, "%s-%d-%s-%d", rasmgrHost, rasmgrPort, serverName, port);
    if ((return_code >= 0) && (return_code<255))
    {
        TALK( "Lock manager, generateServerId: id = " << pResultRasServerId );
    }
    else if (return_code >= 255)
    {
        TALK( "Lock manager, generateServerId: concatenation was successful but the result is too long and was truncated!" );
    }
    else
    {
        TALK( "Lock manager, generateServerId: concatenation of id components failed!" );
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
        TALK( "Lock manager, lock type: shared" );
    }
    else
    {
        lockType = EXCLUSIVE_LOCK;
        TALK( "Lock manager, lock type: exclusive" );
    }
    return lockType;
}

/**
 * Function for returning by reference a string as client id.
 *
 * The function converts an unsigned long value into a string.
 *
 * @param pClientId
 *     unsigned long values as the id of the client
 * @param pResultClientId
 *     the client id as string returned by reference
 */
void LockManager::generateClientId(unsigned long pClientId, char * pResultClientId)
{
    sprintf(pResultClientId, "%024ld", pClientId);
}

/**
 * Function for locking multiple tiles.
 *
 * The internal function for locking is called.
 *
 * @param pClientId
 *     unsigned long value representing the id of the locking client
 * @param tiles
 *     vector of tiles to be locked
 */
void LockManager::lockTiles(unsigned long pClientId, std::vector <Tile *> * tiles)
{
    ENTER( "Lock manager, lock tiles" );
    if (tiles)
    {
        TALK( "Lock manager, lock tiles: locking..." );
        char rasServerId[255];
        generateServerId(rasServerId);
        char clientId[255];
        generateClientId(pClientId, clientId);
        TALK( "server id=" << rasServerId << ", client id=" << clientId );
        enum Lock lockType = generateLockType();
        beginTransaction();
        // this iterates over the tiles of an object
        // if objects consists of one tile like in mr, mr2, rgb then this for is executed once
        for (std::vector<Tile*>::iterator tileIterator=tiles->begin(); tileIterator!=tiles->end(); tileIterator++)
        {
            TALK( "Lock manager, lock tiles: Tile found to lock in iterator." );
            DBTileId dbTileId = (*tileIterator)->getDBTile();
            OId::OIdCounter oid = dbTileId.getObjId().getCounter();
            if (oid > 0)
            {
                TALK( "Lock manager, lock tiles: Locking tile " << oid.getCounter() );
                lockTileInternal(rasServerId, clientId, oid, lockType);
            }
        }
        endTransaction();
    }
    else
    {
        TALK( "Lock manager, lock tiles: no tiles to lock" );
    }
    LEAVE( "Lock manager, lock tiles" );
}

/**
 * Function for locking a tile by a client.
 *
 * The internal locking function is called.
 *
 * @param pClientId
 *     unsigned long value representing the id of the locking client
 * @param pTile
 *     pointer to the tile to be locked
 */
void LockManager::lockTile(unsigned long pClientId, Tile * pTile)
{
    ENTER( "Lock manager, lock tile" );
    DBTileId dbTileId = pTile->getDBTile();
    if (dbTileId)
    {
        TALK( "Lock manager, lock tile: DB tile found to lock." );
        OId oid = dbTileId->getOId();
        if (oid)
        {
            TALK( "Lock manager, lock tile: Locking tile " << oid.getCounter() );
            char rasServerId[255];
            generateServerId(rasServerId);
            char clientId[255];
            generateClientId(pClientId, clientId);
            enum Lock lockType = generateLockType();
            beginTransaction();
            lockTileInternal(rasServerId, clientId, oid.getCounter(), lockType);
            endTransaction();
        }
    }
    LEAVE( "Lock manager, lock tile" );
}

/**
 * Function for unlocking a tile locked by a specific client.
 *
 * The internal function for unlocking a specific tile is called.
 *
 * @param pClientId
 *     unsigned long value representing the id of the locking client
 * @param pTile
 *     pointer to the tile to be unlocked
 */
void LockManager::unlockTile(unsigned long pClientId, Tile * pTile)
{
    ENTER( "Lock manager, unlock tile" );
    DBTileId dbTileId = pTile->getDBTile();
    if (dbTileId)
    {
        TALK( "Lock manager, unlock tile: DB tile found to lock." );
        OId oid = dbTileId->getOId();
        if (oid)
        {
            TALK("Lock manager, unlock tile: Locking tile " << oid.getCounter());
            char rasServerId[255];
            generateServerId(rasServerId);
            char clientId[255];
            generateClientId(pClientId, clientId);
            beginTransaction();
            unlockTileInternal(rasServerId, clientId, oid.getCounter());
            endTransaction();
        }
    }
    LEAVE( "Lock manager, unlock tile" );
}

/**
 * Function for unlocking all tiles locked by a specific client.
 *
 * The internal function for unlocking all tiles is called.
 *
 * @param pClientId
 *     unsigned long value representing the id of the locking client
 */
void LockManager::unlockAllTiles(unsigned long pClientId)
{
    ENTER( "Lock manager, unlock all tiles" );
    char rasServerId[255];
    generateServerId(rasServerId);
    char clientId[255];
    generateClientId(pClientId, clientId);
    TALK( "Lock manager, unlock all tiles: unlocking all tiles for " << ", server id=" << rasServerId << ", client id=" << clientId );
    beginTransaction();
    unlockAllTilesInternal(rasServerId, clientId);
    endTransaction();
    LEAVE( "Lock manager, unlock all tiles" );
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
bool LockManager::isTileLocked(Tile * pTile, enum Lock lockType)
{
    ENTER( "Lock manager, is tile locked" );
    bool locked = false;
    DBTileId dbTileId = pTile->getDBTile();
    if (dbTileId)
    {
        TALK( "Lock manager, is tile locked: DB tile found to check lock." );
        OId oid = dbTileId->getOId();
        if (oid)
        {
            TALK( "Lock manager, is tile locked: Checking lock for " << oid.getCounter() );
            beginTransaction();
            bool locked = isTileLockedInternal(oid.getCounter(), lockType);
            endTransaction();
        }
    }
    LEAVE( "Lock manager, is tile locked" );
    return locked;
}

/**
 * Function for deleting all locks from the lock table corresponding to a rasserver.
 *
 * The internal function for clearing the lock table is called.
 */
void LockManager::clearLockTable()
{
    ENTER( "Lock manager, clear lock table" );
    try
    {
        beginTransaction();
        char rasServerId[255];
        generateServerId(rasServerId);
        clearLockTableInternal(rasServerId);
        endTransaction();
        TALK( MSG_OK )
    }
    catch(...)
    {
        TALK( MSG_FAILED );
        TALK( "Error: Unspecified exception." );
    }
    LEAVE( "Lock manager, clear lock table" );
}
