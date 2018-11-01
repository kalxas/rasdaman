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
 * COMMENTS:
 *
 ************************************************************/

#ifndef _BLOBFILE_HH_
#define _BLOBFILE_HH_

#include <string>
#include <sys/types.h>
#include "raslib/mddtypes.hh"
#include "raslib/error.hh"

namespace blobfs
{

/**
 * Contents of a blob identified with a blobId.
 */
class BlobData
{
public:
    long long blobId;
    r_Bytes size;
    char* data;

    inline
    BlobData(long long blobIdArg)
        : blobId(blobIdArg), size(0), data(NULL)
    {
    }

    inline
    BlobData(long long blobIdArg, r_Bytes sizeArg, char* dataArg)
        : blobId(blobIdArg), size(sizeArg), data(dataArg)
    {
    }
};

/**
 * Encapsulates functionality that can be performed on a blob file. Also
 * provides some general file operations (moving, checking existencet, etc).
 */
class BlobFile
{

public:

    BlobFile(const std::string& filePath);
    ~BlobFile();

    /**
     * Insert blob data, handling possible error conditions.
     */
    void insertData(BlobData& blobData);

    /**
     * Update blob data, handling possible error conditions.
     */
    void updateData(BlobData& blobData);

    /**
     * Read blob data, handling possible error conditions.
     * Data is directly read into blobData; blobData.data is allocated
     * automatically.
     */
    void readData(BlobData& blobData);

    /**
     * Return the size (bytes) of filePath
     */
    off_t getSize();

    /**
     * Returns the blob id of the blob file
     */
    long long getBlobId();

    // -- static utility functions

    /**
     * Return true if filePath exists
     */
    static bool fileExists(const std::string& filePath);

    static void moveFile(const std::string& fromFilePath, const std::string& toFilePath);

    static void removeFile(const std::string& filePath);

private:

    void prepareForInserting();
    void prepareForUpdating();
    void prepareForReading();

    /**
     * Clear fd to an empty file.
     */
    void clearFileDescriptor();
    void closeFileDescriptor();

    /**
     * Helper for generating an exception.
     */
    void generateError(const char* message, int errorCode);

    const std::string& filePath;
    int fd;

};

}

#endif
