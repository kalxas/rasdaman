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
 * INCLUDE: ecpg_lockmanager.pgc
 *
 * MODULE: lockmgr
 *
 * PURPOSE: Contains the ECPG-part of the lockmanager implementation.
 *
 ************************************************************/

#include "ecpg_lockmanager.hh"

/**
 * This class contains the SQL-specific implementation of the lock manager.
 */

// Global private static pointer used to ensure a single instance of the class,
// originally set to NULL
ECPG_LockManager * ECPG_LockManager::ECPG_LM_Instance = 0;

/**
 * Private default constructor such that it cannot be called from the outside.
 *
 * The constructor itself is empty, i.e., it does not do anything.
 */
ECPG_LockManager::ECPG_LockManager()
{
}

/**
 * Function for creating and returning an instance of the class.
 *
 * This function is called to create an instance of the class.
 * Calling the constructor publicly is not allowed. The constructor
 * is private and is only called by this Instance function.
 *
 * @return a pointer to the created instance of the class
 */
ECPG_LockManager * ECPG_LockManager::Instance()
{
   // Allow only one instance of class to be generated
   if (!ECPG_LM_Instance)
   {
        ECPG_LM_Instance = new ECPG_LockManager();
    }
   return ECPG_LM_Instance;
}

/**
 * Function for connecting to the database via the lockmanager's own connection.
 *
 * @param pDatabaseTarget
 *     the string corresponding to the name of the database
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pUserName
 *     the string corresponding to the username which should be used for the connection
 * @param pPassword
 *     the string corresponding to the password which should be used for the connection
 * @return a bool value corresponding to the success of the connection
 */
bool ECPG_LockManager::connect(const char * pDatabaseTarget, const char * pConnectionName, const char * pUsername, const char * pPassword)
{
    const char *target = pDatabaseTarget;
    const char *connectionName = pConnectionName;
    const char *user = pUsername;
    const char *password = pPassword;
//    if (pUsername && pPassword)
//    {
//        EXEC SQL CONNECT TO :target AS :connectionName USER :user USING :password;
//    }
//    else if (pUsername)
//    {
//        EXEC SQL CONNECT TO :target AS :connectionName USER :user;
//    }
//    else
//    {
//        EXEC SQL CONNECT TO :target AS :connectionName;
//    }
    bool connection_result = 0;
//    EXEC SQL AT :connectionName PREPARE insert_query1 FROM "INSERT INTO RAS_LOCKEDTILES (TileID, RasServerID, SharedLock) SELECT ?, ?, ? WHERE NOT EXISTS (SELECT TileID from RAS_LOCKEDTILES WHERE TileID = ? AND ExclusiveLock = ?);";
//    EXEC SQL AT :connectionName PREPARE insert_query2 FROM "INSERT INTO RAS_LOCKEDTILES (TileID, RasServerID, SharedLock, ExclusiveLock) SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT TileID from RAS_LOCKEDTILES WHERE TileID = ?);";
//    EXEC SQL AT :connectionName PREPARE select_query1 FROM "SELECT COUNT(*) FROM RAS_LOCKEDTILES WHERE (TileID = ?);";
//    EXEC SQL AT :connectionName PREPARE select_query4 FROM "SELECT COUNT(*) FROM RAS_LOCKEDTILES WHERE (TileID = ? AND RasServerID = ?);";
//    EXEC SQL AT :connectionName PREPARE select_query5 FROM "SELECT COUNT(*) FROM RAS_LOCKEDTILES WHERE (TileID = ? AND SharedLock = ?);";
//    EXEC SQL AT :connectionName PREPARE select_query8 FROM "SELECT COUNT(*) FROM RAS_LOCKEDTILES WHERE (TileID = ? AND RasServerID = ? AND SharedLock = ?);";
//    EXEC SQL AT :connectionName PREPARE select_query9 FROM "SELECT COUNT(*) FROM RAS_LOCKEDTILES WHERE (TileID = ? AND ExclusiveLock = ?);";
//    EXEC SQL AT :connectionName PREPARE select_query12 FROM "SELECT COUNT(*) FROM RAS_LOCKEDTILES WHERE (TileID = ? AND RasServerID = ? AND ExclusiveLock = ?);";
    return connection_result;
}

/**
 * Function for disconnecting from the database via the lockmanager's own connection.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @return a bool value corresponding to the success of disconnecting from the database
 */
bool ECPG_LockManager::disconnect(__attribute__ ((unused)) const char *pConnectionName)
{
//    EXEC SQL BEGIN DECLARE SECTION;
//    const char *connectionName = pConnectionName;
//    EXEC SQL END DECLARE SECTION;
//    EXEC SQL DISCONNECT :connectionName;
//    return SQLCODE==0;
    return 1;
}

/**
 * Function for beginning a transaction within the database via the lockmanager's own connection.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 */
void ECPG_LockManager::beginTransaction(__attribute__ ((unused)) const char * pConnectionName)
{
//    EXEC SQL BEGIN DECLARE SECTION;
//    const char *connectionName = pConnectionName;
//    EXEC SQL END DECLARE SECTION;
//    EXEC SQL AT :connectionName BEGIN TRANSACTION;
}

/**
 * Function for ending a transaction within the database via the lockmanager's own connection.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 */
void ECPG_LockManager::endTransaction(__attribute__ ((unused)) const char * pConnectionName)
{
//    EXEC SQL BEGIN DECLARE SECTION;
//    const char *connectionName = pConnectionName;
//    EXEC SQL END DECLARE SECTION;
//    EXEC SQL AT :connectionName COMMIT;
}

/**
 * Function for creating a shared lock in the lock table.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileId
 *     the id corresponding to the tile to be locked
 */
void ECPG_LockManager::lockTileShared(__attribute__ ((unused)) const char * pConnectionName, __attribute__ ((unused)) const char * pRasServerId, __attribute__ ((unused)) long long pTileId)
{
//    EXEC SQL BEGIN DECLARE SECTION;
//    const char *connectionName = pConnectionName;
//    long long tileId = pTileId;
//    const char *rasServerId = pRasServerId;
//    EXEC SQL END DECLARE SECTION;
//    EXEC SQL AT :connectionName EXECUTE insert_query1 USING :tileId, :rasServerId, 1, :tileId, 1;
}

/**
 * Function for shared locking an interval of tiles.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pBeginId
 *     the id corresponding to the first tile to be locked (lower bound of interval)
 * @param pEndId
 *     the id corresponding to the last tile to be locked (upper bound of interval)
*/
void ECPG_LockManager::lockTilesShared(__attribute__ ((unused)) const char * pConnectionName, __attribute__ ((unused)) const char * pRasServerId, __attribute__ ((unused)) long long pBeginId, __attribute__ ((unused)) long long pEndId)
{
/*    if ((pBeginId <= pEndId) && (pRasServerId) && (!areTilesLockedShared(pConnectionName, pRasServerId, pBeginId, pEndId)))*/
/*    {*/
/*        char insert_shared_statement[512];*/
/*        int return_code = snprintf(insert_shared_statement, 512, "WITH SEQ AS (SELECT generate_series AS TileID FROM generate_series(%lld, %lld)), STAT AS (SELECT 1 AS SharedLock, \'%s\' AS RasServerID), GENIDS AS (SELECT TileID, RasServerID, SharedLock FROM SEQ CROSS JOIN STAT) INSERT INTO RAS_LOCKEDTILES (TileID, RasServerID, SharedLock) SELECT * FROM GENIDS;", pBeginId, pEndId, pRasServerId);*/
/*        if ((return_code >= 0) && (return_code<512))*/
/*        {*/
/*            EXEC SQL BEGIN DECLARE SECTION;*/
/*            const char *connectionName = pConnectionName;*/
/*            const char * in_shared_statement = insert_shared_statement;*/
/*            EXEC SQL END DECLARE SECTION;*/
/*            EXEC SQL AT :connectionName PREPARE insert_shared_interval FROM :in_shared_statement;*/
/*            EXEC SQL AT :connectionName EXECUTE insert_shared_interval;*/
/*            EXEC SQL DEALLOCATE PREPARE insert_shared_interval;*/
/*        }*/
/*    }*/
}

/**
 * Function for creating an exclusive lock in the lock table.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileId
 *     the id corresponding to the tile to be locked
 */
void ECPG_LockManager::lockTileExclusive(__attribute__ ((unused)) const char * pConnectionName, __attribute__ ((unused)) const char * pRasServerId, __attribute__ ((unused)) long long pTileId)
{
//    EXEC SQL BEGIN DECLARE SECTION;
//    const char *connectionName = pConnectionName;
//    long long tileId = pTileId;
//    const char *rasServerId = pRasServerId;
//    EXEC SQL END DECLARE SECTION;
//    EXEC SQL AT :connectionName EXECUTE insert_query2 USING :tileId, :rasServerId, 0, 1, :tileId;
}

/**
 * Function for exclusive locking an interval of tiles.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pBeginId
 *     the id corresponding to the first tile to be locked (lower bound of interval)
 * @param pEndId
 *     the id corresponding to the last tile to be locked (upper bound of interval)
*/
void ECPG_LockManager::lockTilesExclusive(__attribute__ ((unused)) const char * pConnectionName, __attribute__ ((unused)) const char * pRasServerId, __attribute__ ((unused)) long long pBeginId, __attribute__ ((unused)) long long pEndId)
{
//    if ((pBeginId <= pEndId) && (pRasServerId) && (!areTilesLockedExclusive(pConnectionName, pRasServerId, pBeginId, pEndId)))
//    {
//        char insert_exclusive_statement[512];
//        int return_code = snprintf(insert_exclusive_statement, 512, "WITH SEQ AS (SELECT generate_series AS TileID FROM generate_series(%lld, %lld)), STAT AS (SELECT 0 AS SharedLock, 1 AS ExclusiveLock, \'%s\' AS RasServerID), GENIDS AS (SELECT TileID, RasServerID, SharedLock, ExclusiveLock FROM SEQ CROSS JOIN STAT) INSERT INTO RAS_LOCKEDTILES (TileID, RasServerID, SharedLock, ExclusiveLock) SELECT * FROM GENIDS;", pBeginId, pEndId, pRasServerId);
//        if ((return_code >= 0) && (return_code<512))
//        {
//            EXEC SQL BEGIN DECLARE SECTION;
//            const char *connectionName = pConnectionName;
//            const char * in_exclusive_statement = insert_exclusive_statement;
//            EXEC SQL END DECLARE SECTION;
//            EXEC SQL AT :connectionName PREPARE insert_exclusive_interval FROM :in_exclusive_statement;
//            EXEC SQL AT :connectionName EXECUTE insert_exclusive_interval;
//            EXEC SQL DEALLOCATE PREPARE insert_exclusive_interval;
//        }
//    }
}

/**
 * Function for deleting a lock from the lock table.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileId
 *     the id corresponding to the tile to be unlocked
 */
void ECPG_LockManager::unlockTile(__attribute__ ((unused)) const char * pConnectionName, __attribute__ ((unused)) const char * pRasServerId, __attribute__ ((unused)) long long pTileId)
{
//    EXEC SQL BEGIN DECLARE SECTION;
//    const char *connectionName = pConnectionName;
//    long long tileId = pTileId;
//    const char *rasServerId = pRasServerId;
//    EXEC SQL END DECLARE SECTION;
//    EXEC SQL AT :connectionName DELETE FROM RAS_LOCKEDTILES
//        WHERE (TileID = :tileId AND RasServerID = :rasServerId);
}

/**
 * Function for deleting all locks from the lock table corresponding to a specific rasserver.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 */
void ECPG_LockManager::unlockAllTiles(__attribute__ ((unused)) const char * pConnectionName, __attribute__ ((unused)) const char * pRasServerId)
{
//    EXEC SQL BEGIN DECLARE SECTION;
//    const char *connectionName = pConnectionName;
//    const char *rasServerId = pRasServerId;
//    EXEC SQL END DECLARE SECTION;
//    EXEC SQL AT :connectionName DELETE FROM RAS_LOCKEDTILES
//        WHERE (RasServerID = :rasServerId);
}

/**
 * Function for checking if a specific tile is locked or not.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileId
 *     the id corresponding to the tile to be checked
 * @return a bool value corresponding to the fact that the tile is locked or not
 */
bool ECPG_LockManager::isTileLocked(const char * pConnectionName, const char * pRasServerId, long long pTileId)
{
    int result;
    if (!pRasServerId)
    {
        const char *connectionName = pConnectionName;
        long long tileId = pTileId;
        int rowCount = 0;
//        EXEC SQL AT :connectionName EXECUTE select_query1 INTO :rowCount USING :tileId;
        result=rowCount;
    }
    else
    {
        const char *connectionName = pConnectionName;
        const char * rasServerId = pRasServerId;
        long long tileId = pTileId;
        int rowCount = 0;
//        EXEC SQL AT :connectionName EXECUTE select_query4 INTO :rowCount USING :tileId, :rasServerId;
        result=rowCount;
    }
    if (result > 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

/**
 * Function for checking if a specific tile has a shared lock or not.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileId
 *     the id corresponding to the tile to be checked
 * @return a bool value corresponding to the fact that the tile has a shared locked or not
 */
bool ECPG_LockManager::isTileLockedShared(const char * pConnectionName, const char * pRasServerId, long long pTileId)
{
    int result;
    if (!pRasServerId)
    {
        const char *connectionName = pConnectionName;
        long long tileId = pTileId;
        int rowCount = 0;
//        EXEC SQL AT :connectionName EXECUTE select_query5 INTO :rowCount USING :tileId, 1;
        result=rowCount;
    }
    else
    {
        const char *connectionName = pConnectionName;
        const char * rasServerId = pRasServerId;
        long long tileId = pTileId;
        int rowCount = 0;
//        EXEC SQL AT :connectionName EXECUTE select_query8 INTO :rowCount USING :tileId, :rasServerId, 1;
        result=rowCount;
    }
    if (result > 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

/**
 * Function for checking if a specific tile has an exclusive lock or not.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 * @param pTileId
 *     the id corresponding to the tile to be checked
 * @return a bool value corresponding to the fact that the tile has an exclusive locked or not
 */
bool ECPG_LockManager::isTileLockedExclusive(const char * pConnectionName, const char * pRasServerId, long long pTileId)
{
    int result;
    if (!pRasServerId)
    {
        const char *connectionName = pConnectionName;
        long long tileId = pTileId;
        int rowCount = 0;
//        EXEC SQL AT :connectionName EXECUTE select_query9 INTO :rowCount USING :tileId, 1;
        result=rowCount;
    }
    else
    {
        const char *connectionName = pConnectionName;
        const char * rasServerId = pRasServerId;
        long long tileId = pTileId;
        int rowCount = 0;
//        EXEC SQL AT :connectionName EXECUTE select_query12 INTO :rowCount USING :tileId, :rasServerId, 1;
        result=rowCount;
    }
    if (result > 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

/**
 * Function for clearing the locks from the lock table corresponding to a specific rasserver.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 */
void ECPG_LockManager::clearLockTable(const char * pConnectionName, const char *pRasServerId)
{
    const char *connectionName = pConnectionName;
    const char *rasServerId = pRasServerId;
//    EXEC SQL AT :connectionName DELETE FROM RAS_LOCKEDTILES
//        WHERE (RasServerID = :rasServerId);
}

/**
 * Function for clearing the locks from the lock table corresponding to a specific rasserver without
 * having a spefication of the port.
 *
 * @param pConnectionName
 *     the string corresponding to the connection name of the lockmanager
 * @param pRasServerId
 *     the string corresponding to the id of the current rasserver
 */
void ECPG_LockManager::clearWLikeLockTable(const char * pConnectionName, const char *pRasServerId)
{
    const char *connectionName = pConnectionName;
    const char *rasServerId = pRasServerId;
//    EXEC SQL AT :connectionName DELETE FROM RAS_LOCKEDTILES
//        WHERE (RasServerID LIKE '%' || :rasServerId);
}
