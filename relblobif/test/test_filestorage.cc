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
#include <cstdlib>
#include <sys/stat.h>
#include <unistd.h>

#undef FILEDATADIR
#define FILEDATADIR "/tmp/rasdata"

#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#include "../../applications/directql/template_inst.hh"
#include "../../raslib/template_inst.hh"
#endif

#include "relblobif/blobfilestorage.hh"
#include "reladminif/adminif.hh"
#include "reladminif/databaseif.hh"
#include "reladminif/transactionif.hh"
#include "testing.h"
#include "../../common/src/logging/easylogging++.hh"

using namespace std;

// define external vars
char globalConnectId[256] = "/tmp/rasdata/RASBASE";
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};
unsigned long maxTransferBufferSize = 4000000;
char* dbSchema = 0;
int noTimeOut = 0;
bool udfEnabled = true;

_INITIALIZE_EASYLOGGINGPP

class TestNestedFilestorage{

public:

    void testNestedRootPath()
    {
        const string expected("/tmp/rasdata/TILES/");
        EXPECT_TRUE(fileStorage->isNestedStorage());
        EXPECT_EQ(fileStorage->getFileStorageTilesPath(), expected);
    }

    void testGetPathNested()
    {
        try
        {
            fileStorage->getPath(-1);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }

        try
        {
            fileStorage->getPath(0);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }

        EXPECT_EQ(fileStorage->getPath(1000), "/tmp/rasdata/TILES/0/0/1000");
        EXPECT_EQ(fileStorage->getPath(16383), "/tmp/rasdata/TILES/0/0/16383");
        EXPECT_EQ(fileStorage->getPath(16384), "/tmp/rasdata/TILES/0/1/16384");
        EXPECT_EQ(fileStorage->getPath(32768), "/tmp/rasdata/TILES/0/2/32768");
        EXPECT_EQ(fileStorage->getPath(268435455), "/tmp/rasdata/TILES/0/16383/268435455");
        EXPECT_EQ(fileStorage->getPath(268435456), "/tmp/rasdata/TILES/1/16384/268435456");
        EXPECT_EQ(fileStorage->getPath(4294967296), "/tmp/rasdata/TILES/16/262144/4294967296");
        EXPECT_EQ(fileStorage->getPath(9223372036854775807ll), "/tmp/rasdata/TILES/34359738367/562949953421311/9223372036854775807");
    }

    void testGetPathFlat()
    {
        fileStorage->nestedStorage = false;
        string oldTilesPath = fileStorage->fileStorageTilesPath;
        fileStorage->fileStorageTilesPath = "/tmp/rasdata/";

        try
        {
            fileStorage->getPath(-1);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }

        try
        {
            fileStorage->getPath(0);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }

        EXPECT_EQ(fileStorage->getPath(1000), "/tmp/rasdata/1000");
        EXPECT_EQ(fileStorage->getPath(16383), "/tmp/rasdata/16383");
        EXPECT_EQ(fileStorage->getPath(16384), "/tmp/rasdata/16384");
        EXPECT_EQ(fileStorage->getPath(32768), "/tmp/rasdata/32768");
        EXPECT_EQ(fileStorage->getPath(268435455), "/tmp/rasdata/268435455");
        EXPECT_EQ(fileStorage->getPath(268435456), "/tmp/rasdata/268435456");
        EXPECT_EQ(fileStorage->getPath(4294967296), "/tmp/rasdata/4294967296");
        EXPECT_EQ(fileStorage->getPath(9223372036854775807ll), "/tmp/rasdata/9223372036854775807");

        fileStorage->fileStorageTilesPath = oldTilesPath;
        fileStorage->nestedStorage = true;
    }

    void testInsert()
    {
        const char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = 4294967296;

        fileStorage->insert(testData, testDataSize, blobId);

        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        struct stat status;
        EXPECT_TRUE(stat(expectedFilePath, &status) == 0);
        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testInvalidOidInsert()
    {
        const char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = -100;
        try
        {
            fileStorage->insert(testData, testDataSize, blobId);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testInvalidDataInsert()
    {
        const char* testData = NULL;
        r_Bytes testDataSize = 4;
        long long blobId = 100;
        try
        {
            fileStorage->insert(testData, testDataSize, blobId);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), FAILEDWRITINGTODISK);
        }
    }

    void testRetrieve()
    {
        const char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = 4294967296;

        fileStorage->insert(testData, testDataSize, blobId);

        char* data = NULL;
        r_Bytes dataSize;
        fileStorage->retrieve(blobId, &data, &dataSize);
        EXPECT_TRUE(memcmp(testData, data, testDataSize) == 0);
        free(data);
        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testNonExistingTileRetrieve()
    {
        long long blobId = 4294967296;
        char* data;
        r_Bytes dataSize;
        try
        {
            fileStorage->retrieve(blobId+1, &data, &dataSize);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testInvalidOidRetrieve()
    {
        long long blobId = -10000;
        char* data;
        r_Bytes dataSize;
        try
        {
            fileStorage->retrieve(blobId, &data, &dataSize);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testInvalidSizeOrDataRetrieve()
    {
        const char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = 4294967296;

        fileStorage->insert(testData, testDataSize, blobId);

        try
        {
            char* data = NULL;
            r_Bytes* dataSize = NULL;
            fileStorage->retrieve(blobId, &data, dataSize);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ((int)err.get_kind(), (int)r_Error::r_Error_General);
        }

        try
        {
            char** data = NULL;
            r_Bytes dataSize;
            fileStorage->retrieve(blobId, data, &dataSize);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ((int)err.get_kind(), (int)r_Error::r_Error_General);
        }
        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testUpdate()
    {
        const char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = 4294967296;

        fileStorage->insert(testData, testDataSize, blobId);

        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        struct stat status;
        EXPECT_TRUE(stat(expectedFilePath, &status) == 0);

        const char* newData = "test2";
        r_Bytes newDataSize = 5;
        fileStorage->update(newData, newDataSize, blobId);

        char* data = NULL;
        r_Bytes dataSize;
        fileStorage->retrieve(blobId, &data, &dataSize);
        EXPECT_TRUE(memcmp(newData, data, newDataSize) == 0);
        free(data);

        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testNonExistingOidUpdate()
    {
        const char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = 4294967295;

        try
        {
            const char* expectedFilePath = fileStorage->getPath(blobId).c_str();
            fileStorage->update(testData, testDataSize, blobId);
            struct stat status;
            EXPECT_TRUE(stat(expectedFilePath, &status) == 0);
            EXPECT_TRUE(unlink(expectedFilePath) == 0);
        }
        catch(r_Error & err)
        {
            TEST_FAIL();
        }
    }

    void testInvalidDataUpdate()
    {
        const char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = 4294967296;

        fileStorage->insert(testData, testDataSize, blobId);

        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        try
        {
            const char* newData = NULL;
            r_Bytes newDataSize = 5;
            fileStorage->update(newData, newDataSize, blobId);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), FAILEDWRITINGTODISK);
        }
        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testRemove()
    {
        const char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = 4294967296;

        fileStorage->insert(testData, testDataSize, blobId);

        fileStorage->remove(blobId);
        struct stat status;
        EXPECT_FALSE(stat("/tmp/rasdata/TILES/16/262144/4294967296", &status) == 0);
    }

    void testNonExistingTileRemove()
    {
        try
        {
            fileStorage->remove(1092893937192);
//            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testInvalidOidRemove()
    {
        try
        {
            fileStorage->remove(-1);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testGetPathPerformance()
    {
        Test::startTimer();
        for (long long blobId = 1; blobId < 10000000; blobId++)
        {
            const string blobPath = fileStorage->getPath(blobId);
        }
        double runtime = Test::stopTimer();
        LOG << "test running time: " << runtime << endl;
    }

    void prepareRun()
    {
        char* rasdataEnvVar = const_cast<char*>("RASDATA=/tmp/rasdata");
        putenv(rasdataEnvVar);
        mkdir("/tmp/rasdata", 0770);
        unlink(globalConnectId);
        db.createDB("", "", "");
        AdminIf* myAdmin = AdminIf::instance();
        db.open(globalConnectId);
        ta.begin(&db);
        fileStorage = new BlobFileStorage();
    }

    void finishRun()
    {
        delete fileStorage;
        ta.abort();
        db.close();
        unlink("/tmp/rasdata");
    }

private:
    BlobFileStorage* fileStorage;
    DatabaseIf db;
    TransactionIf ta;
};

int main(int argc, char **argv)
{
#ifndef BASEDB_SQLITE
    cerr << "testsuite runs only on SQLite / Filestorage rasdaman." << endl;
    return 0;
#endif
    //Logging configuration: to standard output, LDEBUG and LTRACE are not enabled
    easyloggingpp::Configurations defaultConf;
    defaultConf.setToDefault();
    defaultConf.set(easyloggingpp::Level::All,
                    easyloggingpp::ConfigurationType::Format, "%datetime [%level] %log");
    defaultConf.set(easyloggingpp::Level::All,
                    easyloggingpp::ConfigurationType::ToFile, "false");
    defaultConf.set(easyloggingpp::Level::All,
                    easyloggingpp::ConfigurationType::ToStandardOutput, "true");
    defaultConf.set(easyloggingpp::Level::Debug,
                    easyloggingpp::ConfigurationType::Enabled, "false");
    defaultConf.set(easyloggingpp::Level::Trace,
                    easyloggingpp::ConfigurationType::Enabled, "false");
    easyloggingpp::Loggers::reconfigureAllLoggers(defaultConf);
    defaultConf.clear();

    TestNestedFilestorage test;
    test.prepareRun();

    RUN_TEST(test.testNestedRootPath());
    RUN_TEST(test.testGetPathNested());
    RUN_TEST(test.testGetPathFlat());

    RUN_TEST(test.testInsert());
    RUN_TEST(test.testInvalidOidInsert());
    RUN_TEST(test.testInvalidDataInsert());

    RUN_TEST(test.testRetrieve());
    RUN_TEST(test.testNonExistingTileRetrieve());
    RUN_TEST(test.testInvalidOidRetrieve());
    RUN_TEST(test.testInvalidSizeOrDataRetrieve());

    RUN_TEST(test.testUpdate());
    RUN_TEST(test.testNonExistingOidUpdate());
    RUN_TEST(test.testInvalidDataUpdate());

    RUN_TEST(test.testRemove());
    RUN_TEST(test.testNonExistingTileRemove());
    RUN_TEST(test.testInvalidOidRemove());

//    RUN_TEST(test.testGetPathPerformance());

    test.finishRun();
    return Test::getResult();
}
