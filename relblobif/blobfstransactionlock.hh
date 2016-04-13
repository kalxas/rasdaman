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
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef _BLOBFILESTORAGETRANSACTIONLOCK_HH_
#define _BLOBFILESTORAGETRANSACTIONLOCK_HH_

#include <string>
#include "lockfile.hh"

namespace blobfs {

/**
 * Manage locking on a particular transaction.
 *
 * The transaction is continuously alive while the server is alive, and during
 * this time a general lock A is held.
 *
 * While the transaction is being committed, lock A is extended with lock B.
 * If the server crashes, and a cleanup function finds a lock B, then it should
 * complete the commit in order to restore to a valid database state.
 *
 * While the transaction is being aborted, lock A is extended with lock C.
 * If the server crashes, and a later cleanup function finds a lock C, then
 * it should complete the abort in order to restore to a valid database state.
 */
class BlobFSTransactionLock
{
    friend class TestBlobFS;
    friend class TestBlobFSTransaction;
    friend class TestBlobFSTransactionLock;

public:

    /**
     * Initialize lock file on fileStorageTransactionPath.
     *
     * @param fileStorageTransactionPath the root directory of the transaction.
     * @param check use this object for checking (do not clear locks in destructor)
     */
    BlobFSTransactionLock(const std::string& fileStorageTransactionPath,
                                   bool check = false);

    /**
     * Clears the transaction lock, which should be a general lock (A)
     */
    ~BlobFSTransactionLock();

    // -- general transaction lock (A)

    bool lockForTransaction();
    bool clearTransactionLock();
    bool lockedForTransaction();
    bool transactionLockValid();

    // -- commit transaction lock (B)

    bool lockForCommit();
    bool clearCommitLock();
    bool lockedForCommit();
    bool commitLockValid();

    // -- abort transaction lock (C)

    bool lockForAbort();
    bool clearAbortLock();
    bool lockedForAbort();
    bool abortLockValid();

private:

    // transaction root path
    std::string fileStorageTransactionPath;

    // check only locks, do not clear them in destructor
    bool checkOnly;

    // path to general transaction in progress lock (A)
    LockFile transactionGeneralLock;

    // commit transaction lock (B)
    LockFile transactionCommitLock;

    // abort transaction lock (C)
    LockFile transactionAbortLock;

    // general transaction in progress lock (A)
    static const std::string TRANSACTION_LOCK;
    // commit transaction lock (B)
    static const std::string TRANSACTION_COMMIT_LOCK;
    // abort transaction lock (C)
    static const std::string TRANSACTION_ABORT_LOCK;
};

}

#endif
