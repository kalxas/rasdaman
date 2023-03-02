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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#include "rasserver_directql.hh"

#include "globals.hh"
#include "applications/rasql/rasql_error.hh"
#include "clientcomm/clientcomm.hh"
#include "raslib/minterval.hh"
#include "raslib/marraytype.hh"
#include "raslib/type.hh"
#include "raslib/mddtypes.hh"
#include "raslib/primitive.hh"
#include "raslib/complex.hh"
#include "raslib/structure.hh"
#include "raslib/structuretype.hh"
#include "raslib/primitivetype.hh"
#include "rasodmg/gmarray.hh"
#include "rasodmg/iterator.hh"
#include "rasodmg/oqlquery.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtscalardata.hh"
#include "relcatalogif/complextype.hh"
#include "relcatalogif/structtype.hh"
#include "servercomm/servercomm.hh"
#include "lockmgr/lockmanager.hh"
#include "servercomm/cliententry.hh"
#include "tilemgr/tile.hh"
#include "rasserver_config.hh"
#include "rasserver_entry.hh"
#include "servercomm/rpcif.h"
#include "servercomm/accesscontrol.hh"

#include <logging.hh>
#include "loggingutils.hh"

#include <iostream>
#include <iomanip>

using namespace std;

#define SECURE_FREE_PTR(ptr) \
    if (ptr)                 \
    {                        \
        free(ptr);           \
        ptr = NULL;          \
    }
#define SECURE_DELETE_PTR(ptr) \
    if (ptr)                   \
    {                          \
        delete ptr;            \
        ptr = NULL;            \
    }

// TODO: remove this and use easylogging macros (LINFO)
// logging mechanism that respects 'quiet' flag:
#define INFO(a)                                            \
    {                                                      \
        if (!configuration.isQuietLogOn()) std::cout << a; \
    }

#define DQ_CLIENT_ID 1000000
#define DQ_TIMEOUT 1000000
#define DQ_MANAGEMENT_INTERVAL 1000000
#define DQ_LISTEN_PORT 8001
#define DQ_SERVER_NAME "NT1"
#define DQ_CAPABILITY "$I1$ERW$BRASBASE$T1:3:2008:23:39:24$NNT1$D3839d047344677ddb1ff1a24dada286e$K"

// rasdaman MDD type for byte strings (default type used for file format reading)
#define MDD_STRINGTYPE "GreyString"

#define STATUS_MORE_TILES 1
#define STATUS_MORE_ELEMS 0
#define STATUS_MDD 0
#define STATUS_SCALAR 1
#define STATUS_EMPTY 2

#define HELP_OUT "<t> use display method t for cell values of result MDDs where t is one of none, file, formatted, string, hex. Implies --content"
#define DEFAULT_OUT OUT_NONE
#define PARAM_OUT_FILE "file"
#define PARAM_OUT_STRING "string"
#define PARAM_OUT_HEX "hex"
#define PARAM_OUT_FORMATTED "formatted"
#define PARAM_OUT_NONE "none"
#define DEFAULT_OUT_STR PARAM_OUT_NONE

#define DEFAULT_OUTFILE "rasql_%d"

extern char globalConnectId[256];

extern AccessControl accessControl;

std::string baseName;

const int MAX_STR_LEN = 255;
const int MAX_QUERY_LEN = 10240;

const char *user = DEFAULT_USER;
const char *passwd = DEFAULT_PASSWD;

const char *fileName = NULL;
const char *queryString = NULL;
const char *outFileMask = DEFAULT_OUTFILE;
const char *mddTypeName = NULL;

r_Minterval mddDomain;
bool mddDomainDef = false;
bool mddTypeNameDef = false;
bool dbIsOpen = false;
bool taIsOpen = false;
bool output = false;
bool displayType = false;

namespace rasserver
{
namespace directql
{

void openDatabase()
{
    if (!dbIsOpen)
    {
        auto &instance = RasServerEntry::getInstance();
        accessControl.setServerName(DQ_SERVER_NAME);
        baseName = configuration.getBaseName() ? configuration.getBaseName() : getDefaultDb();
        strcpy(globalConnectId, baseName.c_str());
        instance.connectToRasbase();

        if (configuration.getUser() && strlen(configuration.getUser()) > 0)
            user = configuration.getUser();
        if (configuration.getPasswd() && strlen(configuration.getPasswd()) > 0)
            passwd = configuration.getPasswd();

        char capability[500];
        sprintf(capability, "%s$U%s$P%s$K", DQ_CAPABILITY, user, passwd);
        instance.connectNewClient(1, capability);

        INFO("opening database " << baseName << " at " << DQ_SERVER_NAME << "..." << flush);
        instance.openDB(baseName.c_str());
        dbIsOpen = true;
        INFO(" ok" << endl
                   << flush);
    }
}

void closeDatabase()
{
    if (dbIsOpen)
    {
        LDEBUG << "database was open, closing it";
        auto &instance = RasServerEntry::getInstance();
        instance.closeDB();
        instance.disconnectClient();
        dbIsOpen = false;
    }
}

void openTransaction(bool readwrite)
{
    if (!taIsOpen)
    {
        auto &instance = RasServerEntry::getInstance();
        LDEBUG << "transaction was closed, opening " << (readwrite ? "rw" : "ro") << "...";
        instance.beginTA(readwrite);
        LDEBUG << "ok";
        taIsOpen = true;
    }
}

void closeTransaction(bool doCommit)
{
    if (taIsOpen)
    {
        auto &instance = RasServerEntry::getInstance();
        if (doCommit)
        {
            LDEBUG << "transaction was open, committing it...";
            instance.commitTA();
        }
        else
        {
            LDEBUG << "transaction was open, aborting it...";
            instance.abortTA();
        }
        LDEBUG << "ok";
        taIsOpen = false;
    }
}

void printScalar(char *buffer, QtData *data, unsigned int resultIndex)
{
    INFO("  Result element " << resultIndex << ": ");
    switch (data->getDataType())
    {
    case QT_BOOL:
        INFO((*((bool *)buffer) ? "t" : "f") << flush);
        break;
    case QT_CHAR:
        INFO(static_cast<int>(*((r_Char *)buffer)) << flush);
        break;
    case QT_OCTET:
        INFO(static_cast<int>(*((r_Octet *)buffer)) << flush);
        break;
    case QT_SHORT:
        INFO(*((r_Short *)buffer) << flush);
        break;
    case QT_USHORT:
        INFO(*((r_UShort *)buffer) << flush);
        break;
    case QT_LONG:
        INFO(*((r_Long *)buffer) << flush);
        break;
    case QT_ULONG:
        INFO(*((r_ULong *)buffer) << flush);
        break;
    case QT_FLOAT:
        INFO(std::setprecision(std::numeric_limits<float>::digits10 + 1) << *((r_Float *)buffer) << flush);
        break;
    case QT_DOUBLE:
        INFO(std::setprecision(std::numeric_limits<double>::digits10 + 1) << *((r_Double *)buffer) << flush);
        break;
    case QT_COMPLEXTYPE1:
    {
        QtScalarData *scalarDataObj = static_cast<QtScalarData *>(data);
        ComplexType1 *ct = static_cast<ComplexType1 *>(const_cast<BaseType *>(scalarDataObj->getValueType()));
        auto re = *((r_Float *)(buffer + ct->getReOffset()));
        auto im = *((r_Float *)(buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }
    case QT_COMPLEXTYPE2:
    {
        QtScalarData *scalarDataObj = static_cast<QtScalarData *>(data);
        ComplexType2 *ct = static_cast<ComplexType2 *>(const_cast<BaseType *>(scalarDataObj->getValueType()));
        auto re = *((r_Double *)(buffer + ct->getReOffset()));
        auto im = *((r_Double *)(buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }

    case QT_CINT16:
    {
        QtScalarData *scalarDataObj = static_cast<QtScalarData *>(data);
        CInt16 *ct = static_cast<CInt16 *>(const_cast<BaseType *>(scalarDataObj->getValueType()));
        auto re = *((r_Short *)(buffer + ct->getReOffset()));
        auto im = *((r_Short *)(buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }
    case QT_CINT32:
    {
        QtScalarData *scalarDataObj = static_cast<QtScalarData *>(data);
        CInt32 *ct = static_cast<CInt32 *>(const_cast<BaseType *>(scalarDataObj->getValueType()));
        auto re = *((r_Long *)(buffer + ct->getReOffset()));
        auto im = *((r_Long *)(buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }
    case QT_COMPLEX:
    {
        QtScalarData *scalarDataObj = static_cast<QtScalarData *>(data);
        StructType *st = static_cast<StructType *>(const_cast<BaseType *>(scalarDataObj->getValueType()));
        INFO("{ ");
        for (unsigned int i = 0; i < st->getNumElems(); i++)
        {
            BaseType *bt = const_cast<BaseType *>(st->getElemType(i));
            if (i > 0)
                INFO(", ");
            bt->printCell(cout, buffer);
            buffer += bt->getSize();
        }
        INFO(" }" << flush);
        break;
    }
    case QT_STRING:
    case QT_INTERVAL:
    case QT_MINTERVAL:
    case QT_POINT:
        INFO(buffer << flush);
        break;
    default:
        INFO("scalar type not supported" << endl);
        break;
    }
    INFO(endl
         << flush);
}

void printResult(Tile *tile, int resultIndex)
{
    const char *theStuff = tile->getContents();
    r_Bytes numCells = tile->getSize();
    auto outputType = configuration.getOutputType();

    switch (outputType)
    {
    case OUT_NONE:
        break;
    case OUT_STRING:
    {
        INFO("  Result object " << resultIndex << ": ");
        for (r_Bytes cnt = 0; cnt < numCells; cnt++)
        {
            cout << theStuff[cnt];
        }
        cout << endl;
        break;
    }
    case OUT_HEX:
    {
        INFO("  Result object " << resultIndex << ": ");
        cout << hex;
        for (r_Bytes cnt = 0; cnt < numCells; cnt++)
        {
            cout << setw(2) << (unsigned short)(0xff & theStuff[cnt]) << " ";
        }
        cout << dec << endl;
        break;
    }
    case OUT_FILE:
    {
        char defFileName[FILENAME_MAX];
        (void)snprintf(defFileName, sizeof(defFileName) - 1, outFileMask, resultIndex);
        LDEBUG << "filename for #" << resultIndex << " is " << defFileName;

        switch (tile->getDataFormat())
        {
        case r_TIFF: strcat(defFileName, ".tif"); break;
        case r_JP2: strcat(defFileName, ".jp2"); break;
        case r_JPEG: strcat(defFileName, ".jpg"); break;
        case r_HDF: strcat(defFileName, ".hdf"); break;
        case r_PNG: strcat(defFileName, ".png"); break;
        case r_BMP: strcat(defFileName, ".bmp"); break;
        case r_NETCDF: strcat(defFileName, ".nc"); break;
        case r_CSV: strcat(defFileName, ".csv"); break;
        case r_JSON: strcat(defFileName, ".json"); break;
        case r_DEM: strcat(defFileName, ".dem"); break;
        default: strcat(defFileName, ".unknown"); break;
        }

        INFO("  Result object " << resultIndex << ": going into file " << defFileName << "..." << flush);
        FILE *tfile = fopen(defFileName, "wb");
        if (tfile == NULL)
            throw RasqlError(NOFILEWRITEPERMISSION);
        if (fwrite(static_cast<void *>(const_cast<char *>(theStuff)), 1, numCells, tfile) != numCells)
        {
            fclose(tfile);
            throw RasqlError(UNABLETOWRITETOFILE);
        }
        fclose(tfile);
        INFO("ok." << endl);
        break;
    }
    default:
        LERROR << "Unknown output type: " << static_cast<int>(outputType);
        break;
    }  // switch(outputType)

}  // printResult()

void printOutput(unsigned short status, ExecuteQueryRes *result)
{
    switch (status)
    {
    case STATUS_MDD: INFO("holds MDD elements" << endl); break;
    case STATUS_SCALAR: INFO("holds non-MDD elements" << endl); break;
    case STATUS_EMPTY: INFO("holds no elements" << endl); break;
    default: break;
    }

    if (result)
    {
        auto &instance = RasServerEntry::getInstance();
        if (configuration.isOutputOn())
        {
            INFO("Getting result..." << flush);
            if (status == STATUS_MDD)
            {
                INFO("Getting MDD objects..." << endl
                                              << flush);

                std::string typeName;
                std::string typeStructure;
                r_OId oid;
                unsigned short currentFormat;

                int resultIndex = 0;
                while (instance.compat_getNextMDD(
                           mddDomain, typeName, typeStructure, oid, currentFormat) == STATUS_MORE_ELEMS)
                {
                    auto *r = instance.getClientContext();
                    Tile *resultTile = new Tile(r->transTiles);
                    printResult(resultTile, ++resultIndex);
                    delete resultTile;
                    ++(*r->transferDataIter);
                    if (*(r->transferDataIter) == r->transferData->end())
                        break;
                }
            }
            else if (status == STATUS_SCALAR)
            {
                INFO("Getting scalars..." << endl
                                          << flush);

                auto *r = instance.getClientContext();
                unsigned int resultIndex = 0;
                char *buffer;
                unsigned int bufferSize;
                status = STATUS_MORE_ELEMS;
                while (status == STATUS_MORE_ELEMS)
                {
                    QtData *data = (**(r->transferDataIter));
                    status = instance.compat_getNextElement(buffer, bufferSize);
                    printScalar(buffer, data, ++resultIndex);
                    if (buffer)
                    {
                        delete[] buffer;
                        buffer = NULL;
                    }
                }
            }
        }
        instance.compat_endTransfer();
    }
}

/*
 * get database type structure from type name
 * returns ptr if an MDD type with the given name exists in the database, NULL otherwise
 * throws r_Error upon general database comm error
 * needs an open transaction
 */
r_Marray_Type *getTypeFromDatabase(const char *mddTypeName2)
{
    auto &instance = RasServerEntry::getInstance();
    r_Marray_Type *retval = NULL;
    std::string typeStructure;

    // first, try to get type structure from database using a separate r/o transaction
    try
    {
        int typeType = ClientComm::r_MDDType_Type;
        instance.compat_GetTypeStructure(mddTypeName2, typeType, typeStructure);

        // above doesn't seem to work, so at least make it work with inv_*
        // functions -- DM 2013-may-19
        if (typeStructure.empty())
            typeStructure = "marray<char>";

        LDEBUG << "type structure is " << typeStructure;
    }
    catch (r_Error &err)
    {
        if (err.get_kind() == r_Error::r_Error_DatabaseClassUndefined)
        {
            LDEBUG << "Type is not a well known type: " << typeStructure;
            typeStructure = new char[strlen(mddTypeName2) + 1];
            // earlier code tried this one below, but I feel we better are strict -- PB 2003-jul-06
            // strcpy(typeStructure, mddTypeName2);
            // LDEBUG <<  "using instead: " << typeStructure;
            throw RasqlError(MDDTYPEINVALID);
        }
        else  // unanticipated error
        {
            LDEBUG << "Error during type retrieval from database: "
                   << err.get_errorno() << " " << err.what();
            throw;
        }
    }

    // next, find out whether it is an MDD type (and not a base or set type, eg)
    r_Type *tempType = NULL;
    try
    {
        tempType = r_Type::get_any_type(typeStructure);
        LDEBUG << "get_any_type() for this type returns: " << tempType;
        if (tempType->isMarrayType())
        {
            retval = (r_Marray_Type *)tempType;
            tempType = NULL;
            LDEBUG << "found MDD type: " << retval;
        }
        else
        {
            LDEBUG << "type is not an marray type: " << typeStructure;
            SECURE_DELETE_PTR(tempType);
            retval = NULL;
            throw RasqlError(MDDTYPEINVALID);
        }
    }
    catch (r_Error &err)
    {
        LDEBUG << "Error during retrieval of MDD type structure ("
               << typeStructure << "): " << err.get_errorno() << " " << err.what();
        SECURE_DELETE_PTR(tempType);
        throw;
    }

    return retval;
}  // getTypeFromDatabase()

void freeResult(ExecuteQueryRes *result)
{
    SECURE_FREE_PTR(result->typeStructure);
    SECURE_FREE_PTR(result->token);
    SECURE_FREE_PTR(result->typeName);
}

void printError(unsigned short status, ExecuteQueryRes *result)
{
    cerr << endl
         << "Error number: " << result->errorNo << " Token: '" << result->token
         << "' Line: " << result->lineNo << " Column: " << result->columnNo
         << " (status: " << status << ")" << endl
         << flush;
}

void printError(unsigned short status, ExecuteUpdateRes *result)
{
    cerr << endl
         << "Error number: " << result->errorNo << " Token: '" << result->token
         << "' Line: " << result->lineNo << " Column: " << result->columnNo
         << " (status: " << status << ")" << endl
         << flush;
}

void doStuff()
{
    auto &instance = RasServerEntry::getInstance();

    char *fileContents = NULL;      // contents of file satisfying "$1" parameter in query
    r_Marray_Type *mddType = NULL;  // this MDD's type
    RPCMarray *marray = NULL;
    r_Bytes baseTypeSize = 0;

    queryString = configuration.getQueryString();
    mddTypeNameDef = configuration.isMddTypeNameDef();
    mddDomainDef = configuration.isMddDomainDef();
    mddDomain = configuration.getMddDomain();
    fileName = configuration.getFileName();
    if (configuration.getOutFileMask())
        outFileMask = configuration.getOutFileMask();

    r_OQL_Query query(queryString);
    LDEBUG << "query is: " << query.get_query();

    try
    {
        openDatabase();
        if (fileName != NULL)
        {
            openTransaction(false);

            // if no type name was specified then assume byte string (for encoded files)
            if (!mddTypeNameDef)
            {
                configuration.setMddTypeName(MDD_STRINGTYPE);
            }
            mddTypeName = configuration.getMddTypeName();

            INFO("fetching type information for " << mddTypeName << " from database, using readonly transaction..." << flush);
            mddType = getTypeFromDatabase(mddTypeName);
            closeTransaction(true);
            INFO("ok" << endl);

            INFO("reading file " << fileName << "..." << flush);
            FILE *fileD = fopen(fileName, "r");
            if (fileD == NULL)
            {
                throw RasqlError(FILEINACCESSIBLE);
            }

            fseek(fileD, 0, SEEK_END);
            long size = ftell(fileD);
            LDEBUG << "file size is " << size << " bytes";

            if (size == 0)
            {
                throw RasqlError(FILEEMPTY);
            }

            // if no domain specified (this is the case with encoded files), then set to byte stream
            if (!mddDomainDef)
            {
                mddDomain = r_Minterval(1) << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(size) - 1);
                LDEBUG << "domain set to " << mddDomain;
            }
            else if (size != static_cast<long>(mddDomain.cell_count() * mddType->base_type().size()))
            {
                throw RasqlError(FILESIZEMISMATCH);
            }

            try
            {
                fileContents = new char[size];
                fseek(fileD, 0, SEEK_SET);
                size_t rsize = fread(fileContents, 1, static_cast<size_t>(size), fileD);

                baseTypeSize = mddType->base_type().size();
                r_GMarray *fileMDD = new r_GMarray(mddDomain, baseTypeSize, 0, NULL, false);
                fileMDD->set_type_schema(mddType);
                fileMDD->set_array_size(baseTypeSize);
                query << *fileMDD;
                SECURE_DELETE_PTR(fileMDD);
                SECURE_DELETE_PTR(mddType);

                marray = new RPCMarray();
                marray->cellTypeLength = baseTypeSize;
                marray->domain = mddDomain.get_string_representation();
                marray->currentFormat = r_Array;
                marray->storageFormat = r_Array;
                marray->data.confarray_len = mddDomain.cell_count() * baseTypeSize;
                marray->data.confarray_val = fileContents;
                marray->bandLinearization = 0;
                marray->cellLinearization = 0;
            }
            catch (std::bad_alloc &e)
            {
                LDEBUG << "Unable to claim memory: " << size << " bytes";
                throw RasqlError(UNABLETOCLAIMRESOURCEFORFILE);
            }

            fclose(fileD);

            INFO("ok" << endl);
        }

        openTransaction(query.is_insert_query() || query.is_update_query());

        if (fileContents != NULL)
        {
            instance.compat_InitUpdate();
            auto mddDomainTmp = mddDomain.to_string();
            instance.compat_StartInsertTransMDD(mddDomainTmp.c_str(), baseTypeSize, mddTypeName);
            instance.compat_InsertTile(false, marray);
            instance.compat_EndInsertMDD(false);
        }

        if (query.is_insert_query())
        {
            INFO("Executing insert query...\n"
                 << flush);

            ExecuteQueryRes result;
            result.token = NULL;
            result.typeName = NULL;
            result.typeStructure = NULL;
            auto status = instance.compat_ExecuteInsertQuery(query.get_query(), result);

            if (status == 0 || status == 1 || status == 2)
                printOutput(status, &result);
            else
                printError(status, &result);

            freeResult(&result);
            closeTransaction(true);
        }
        else if (query.is_update_query())
        {
            INFO("Executing update query...\n"
                 << flush);

            ExecuteUpdateRes result;
            auto status = instance.compat_ExecuteUpdateQuery(query.get_query(), result);

            if (status != 0 && status != 1)
                printError(status, &result);

            SECURE_FREE_PTR(result.token);
            closeTransaction(true);
        }
        else  // retrieval query
        {
            INFO("Executing retrieval query...\n"
                 << flush);

            openTransaction(false);

            ExecuteQueryRes result;
            auto status = instance.compat_executeQueryRpc(query.get_query(), result, false);
            if (status <= 2)
            {
                printOutput(status, &result);
            }
            else if (result.token != NULL)
            {
                r_Equery_execution_failed e(
                    result.errorNo, result.lineNo, result.columnNo, result.token);
                LERROR << "rasdaman error " << e.get_errorno() << ": " << e.what();
            }

            freeResult(&result);

            closeTransaction(true);
        }

        if (fileContents != NULL)
        {
            query.reset_query();
            instance.compat_endTransfer();
        }
    }
    catch (r_Error &err)
    {
        if (marray)
        {
            SECURE_FREE_PTR(marray->domain);
            SECURE_DELETE_PTR(marray)
        }
        closeDatabase();
        throw err;
    }
    closeDatabase();

    if (marray)
    {
        SECURE_FREE_PTR(marray->domain);
        SECURE_DELETE_PTR(marray)
    }
}

std::string getDefaultDb()
{
    string rasmgrConfFilePath(string(CONFDIR) + "/" + string(RASMGR_CONF_FILE));
    std::ifstream ifs(rasmgrConfFilePath);
    if (!ifs)
    {
        LERROR << "Failed opening '" << rasmgrConfFilePath << "'.";
        throw RasqlError(FILEINACCESSIBLE);
    }
    else
    {
        static const size_t bufferSize = 1024;
        char inBuffer[bufferSize];
        while (!ifs.eof())
        {
            ifs.getline(inBuffer, bufferSize);
            if (strlen(inBuffer) == 0)
                continue;

            char *token = strtok(inBuffer, " ");
            if (strcmp(token, "define") != 0)
                continue;
            token = strtok(NULL, " ");
            if (strcmp(token, "dbh") != 0)
                continue;
            token = strtok(NULL, " ");
            token = strtok(NULL, " ");
            if (strcmp(token, "-connect") != 0)
                continue;
            token = strtok(NULL, " ");
            return string(token);
        }
    }
    return string(DEFAULT_DBNAME);
}

}  // namespace directql
}  // namespace rasserver
