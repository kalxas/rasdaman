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
/*************************************************************
 *
 * PURPOSE:
 * The interface used by the file storage modules.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _BLOBFS_HH_
#define _BLOBFS_HH_

#include <vector>
#include <string>
#include "blobfile.hh"
#include "blobfstransaction.hh"

using blobfs::BlobData;
using blobfs::BlobFSTransaction;

namespace blobfs
{

/**
 * Main blob file storage class.
 */
class BlobFS
{
    friend class TestBlobFS;
public:

    static BlobFS& getInstance();

    // Store the content of a new blob.
    void insert(BlobData& blob);
    // Update the content of a blob. The blob should exist already.
    void update(BlobData& blob);
    // Retrive the content of a previously stored blob; the data buffer to hold is
    // automatically allocated, and size is accordingly set.
    void select(BlobData& blob);
    // Delete a previously stored blob.
    void remove(BlobData& blob);

    // To be called before commit to RASBASE
    void preRasbaseCommit();
    // To be called after commit to RASBASE
    void postRasbaseCommit();
    // To be called before abort to RASBASE
    void postRasbaseAbort();

    // To be called once, cleans up any failed transaction (e.g. due to a crash)
    void finalizeUncompletedTransactions();

    // Destructor
    virtual ~BlobFS();

private:

    // Initialize with a root file storage path determined from the
    // RASDATA env variable, or the --with-filedatadir configuration setting;
    BlobFS();
    // Initialize with a given root file storage path
    BlobFS(const std::string& rasdataPath);

    // Initialize
    void init();

    // Check that the root storage path is valid (exists, is writable, etc) and throw an exception if it isn't
    void validateFileStorageRootPath();

    // Check if the organization of RASDATA is flat file (old) or nested (new)
    bool isNestedStorage();

    const std::string getNestedStorageRootPath();
    const std::string getTransactionsRootPath();

    // Determine root file storage path from the
    // RASDATA env variable, or the --with-filedatadir configuration setting;
    // Empty string is returned the file storage path cannot be determined.
    static const std::string getFileStorageRootPath();

    // Helper for generating an error
    void generateError(const char* message, const std::string& path, int errorCode);

    BlobFSConfig config;

    BlobFSTransaction* insertTransaction;
    BlobFSTransaction* updateTransaction;
    BlobFSTransaction* removeTransaction;
    BlobFSTransaction* selectTransaction;
};

}

#endif
