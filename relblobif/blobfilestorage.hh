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

#ifndef _BLOBFILESTORAGE_HH_
#define _BLOBFILESTORAGE_HH_

#include <vector>
#include <string>

/**
 * \ingroup Relblobifs
 */
class BlobFileStorage
{
    friend class TestNestedFilestorage;

public:

    // Initialize with a root file storage path determined from the
    // RASDATA env variable, or the --with-filedatadir configuration setting;
    BlobFileStorage() throw (r_Error);

    // Initialize with a given root file storage path
    BlobFileStorage(const std::string& rasdataPath) throw (r_Error);

    // Store the content of a new blob.
    void insert(const char* data, r_Bytes size, long long blobId) throw (r_Error);
    // Update the content of a blob. The blob should exist already.
    void update(const char* data, r_Bytes size, long long blobId) throw (r_Error);
    // Retrive the content of a previously stored blob; the data buffer to hold is
    // automatically allocated, and size is accordingly set.
    void retrieve(long long blobId, char** data, r_Bytes* size) throw (r_Error);
    // Delete a previously stored blob.
    void remove(long long blobId) throw (r_Error);

    // Return the tile root path
    const std::string getFileStorageTilesPath();

    // Determine root file storage path from the
    // RASDATA env variable, or the --with-filedatadir configuration setting;
    // Empty string is returned the file storage path cannot be determined.
    static const std::string getFileStorageRootPath();

    // Destructor
    virtual ~BlobFileStorage();

private:

    // Initialize
    void init() throw (r_Error);

    // Check if the organization of RASDATA is flat file (old) or nested (new)
    bool isNestedStorage();
    const std::string getNestedStorageRootPath();

    // Return file descriptor to blobPath for insertion
    int openFileForInserting(const std::string& blobPath) throw (r_Error);
    // Return file descriptor to blobPath for updating
    int openFileForUpdating(const std::string& blobPath) throw (r_Error);
    // Return file descriptor to blobPath for reading
    int openFileForReading(const std::string& blobPath) throw (r_Error);
    // Write data to file descriptor, handling possible error conditions and closing the descriptor
    void writeDataToFileDescriptor(int fd, const std::string& blobPath, r_Bytes dataSize, const char* data) throw (r_Error);
    // Write data to file descriptor, handling possible error conditions and closing the descriptor
    void readDataFromFileDescriptor(int fd, const std::string& blobPath, r_Bytes* dataSize, char** data) throw (r_Error);
    // Finalize file descriptor
    void closeFileDescriptor(int fd, const std::string& blobPath) throw (r_Error);

    // Create a directory at dirPath; ignore if directory already exists
    void createDirectory(const std::string& dirPath) throw (r_Error);
    // Return the size of blobPath
    r_Bytes getFileSize(const std::string& blobPath) throw (r_Error);
    // Check that the root storage path is valid (exists, is writable, etc) and throw an exception if it isn't
    void validateFileStorageRootPath() throw (r_Error);

    // Given a blob ID return its absolute file path
    const std::string getPath(long long blobId);

    // Helper for generating an error
    void generateError(const char* message, const std::string& path, int errorCode) throw (r_Error);

    // Path where tiles are stored. In the new tile organization this points to $RASDATA/TILES
    std::string fileStorageTilesPath;

    // Root file storage path, tiles can be organized in a flat-file scheme, or
    // nested in subdirectories in subdir TILES
    std::string fileStorageRootPath;

    // Is the rootTilesPath pointing to the new nested directory tile organization (true),
    // or old flat-file organization (false)?
    bool nestedStorage;

}; // class BlobFileStorage

#endif  // _BLOBFILESTORAGE_HH_
