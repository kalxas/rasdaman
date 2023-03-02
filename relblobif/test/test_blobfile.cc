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

#include "config.h"
#include "version.h"
#include <string>
#include <cstdlib>
#include <sys/stat.h>
#include <unistd.h>

#include "relblobif/blobfile.hh"
#include "relblobif/blobfscommon.hh"
#include "testing.h"

#include "loggingutils.hh"

using namespace std;

// define external vars
char globalConnectId[256] = "/tmp/rasdata/RASBASE";
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};

class MDDColl;
MDDColl *mddConstants = 0;  // used in QtMDD

INITIALIZE_EASYLOGGINGPP

class TestBlobFile
{
public:
    void testGetBlobId()
    {
        string filePath = "/some/test/123456";
        BlobFile blobFile(filePath);
        long long expectedBlobId = 123456;
        long long resultBlobId = blobFile.getBlobId();
        EXPECT_EQ(resultBlobId, expectedBlobId);
    }

    void testGetBlobIdInvalid()
    {
        {
            string filePath = "/some/test/transaction.00";
            BlobFile blobFile(filePath);
            try
            {
                long long resultBlobId = blobFile.getBlobId();
                TEST_FAIL();
            }
            catch (...)
            {
            }
        }
        {
            string filePath = "/some/test/transaction.new";
            BlobFile blobFile(filePath);
            long long resultBlobId = blobFile.getBlobId();
            EXPECT_EQ(resultBlobId, INVALID_BLOB_ID);
        }
    }
};

int main(int argc, char **argv)
{
#ifndef BASEDB_SQLITE
    cerr << "testsuite runs only on SQLite / Filestorage rasdaman." << endl;
    return 0;
#endif
    common::LogConfiguration defaultConf;
    defaultConf.configClientLogging();

    TestBlobFile test;

    RUN_TEST(test.testGetBlobId());
    RUN_TEST(test.testGetBlobIdInvalid());

    return Test::getResult();
}
