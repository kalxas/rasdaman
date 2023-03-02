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

#include <string>

/**
 * Encapsulate locking operations and checks on a single file.
 */
class LockFile
{
    friend class TestBlobFSTransactionLock;

public:
    explicit LockFile(const std::string &lockFilePath);

    /**
     * Unlocks file (with unlock()) if necessary.
     */
    ~LockFile();

    /**
     * Create file if it doesn't exist, and lock it for writing.
     * @return true if successful, false otherwise
     */
    bool lock();

    /**
     * @return true if lock file exists and is locked
     */
    bool isLocked();

    /**
     * @return false if lock file exists and is not locked, otherwise true
     */
    bool isValid();

    /**
     * Unlock and remove lock file.
     * @return true if successful, false otherwise
     */
    bool unlock();

private:
    void logWarning(const char *msg) const;

    std::string lockFilePath;
    int fd;
};
