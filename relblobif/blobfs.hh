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

#pragma once

#include <string>  // for string
#include <vector>

#include "blobfile.hh"
#include "blobfscommon.hh"  // for BlobFSConfig
#include "blobfstransaction.hh"

/**
 * Main blob file storage class.
 */
class BlobFS
{
    friend class TestBlobFS;

public:
    static BlobFS &getInstance();

    /// Store the content of a new blob.
    void insert(BlobData &blob);
    /// Update the content of a blob. The blob should exist already.
    void update(BlobData &blob);
    /// Retrive the content of a previously stored blob; the data buffer to hold is
    /// automatically allocated, and size is accordingly set.
    void select(BlobData &blob);
    /// Delete a previously stored blob.
    void remove(BlobData &blob);

    /// To be called before commit to RASBASE
    void preRasbaseCommit();
    /// To be called after commit to RASBASE
    void postRasbaseCommit();
    /// To be called before abort to RASBASE
    void postRasbaseAbort();

    /// To be called once, cleans up any failed transaction (e.g. due to a crash)
    void finalizeUncompletedTransactions();

    /// return the file path for blob with the given blobId
    std::string getBlobFilePath(long long blobId) const;

    const BlobFSConfig &getConfig() const;

private:
    /// Initialize with a root file storage path determined from the -connect
    /// option in rasmgr.conf
    BlobFS();
    /// Initialize with a given root file storage path
    explicit BlobFS(const std::string &rasdataPath,
                    std::string transactionLocksDirArg = transactionLocksDir);

    /// Check that the root storage path is valid (exists, is writable, etc) and
    /// throw an exception if it isn't
    void validateFileStorageRootPath();

    /// Return root file storage path determined from the -connect
    /// option in rasmgr.conf
    static std::string getFileStorageRootPath();

    /// Helper for generating an error
    static void generateError(const char *msg, const std::string &path, int errCode);

    BlobFSConfig config;

    std::unique_ptr<BlobFSTransaction> insertTransaction;
    std::unique_ptr<BlobFSTransaction> updateTransaction;
    std::unique_ptr<BlobFSTransaction> removeTransaction;
    std::unique_ptr<BlobFSTransaction> selectTransaction;

    static const std::string tilesSubdir;          // TILES
    static const std::string transactionsSubdir;   // TRANSACTIONS
    static const std::string transactionLocksDir;  // /tmp/rasdaman_transaction_locks
};
