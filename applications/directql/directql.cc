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

/**
 * directql
 *
 * PURPOSE:
 *   Provides a command line interpreter for rasql queries, with
 *   options for displaying results or storing them to file(s)
 *
 * COMMENTS:
 *
 * BUGS:
 * - query filename "" is interpreted as stdin
 */



#include "version.h"
#include "config.h"
#ifndef RMANVERSION
#error "Please specify RMANVERSION variable!"
#endif

#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#define DEBUG_MAIN

#include "template_inst.hh"
#include "raslib/template_inst.hh"
#endif

const char* myExecArgv0 = "";
int tiling = 1;
unsigned long maxTransferBufferSize = 4000000;
char* dbSchema = 0;
int noTimeOut = 0;

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sstream>
#include <fstream>
#include <vector>
#include <stdexcept>

using namespace std;

#ifdef __VISUALC__
#define __EXECUTABLE__
#endif

#include "rasodmg/ref.hh"
#include "raslib/marraytype.hh"
#include "rasodmg/set.hh"
#include "rasodmg/marray.hh"
#include "rasodmg/iterator.hh"
#include "rasodmg/oqlquery.hh"
#include "rasodmg/storagelayout.hh"
#include "rasodmg/alignedtiling.hh"

#include "raslib/type.hh"

#include "raslib/minterval.hh"

#include "raslib/primitive.hh"
#include "raslib/complex.hh"
#include "raslib/structure.hh"

#include "common/logging/signalhandler.hh"
#include "raslib/structuretype.hh"
#include "raslib/primitivetype.hh"

#include "commline/cmlparser.hh"

#include "../rasql/rasql_error.hh"

#include "relcatalogif/complextype.hh"
#include "servercomm/servercomm.hh"
#include "relblobif/tilecache.hh"
#include "lockmgr/lockmanager.hh"
#include "mymalloc/mymalloc.h"
#include "raslib/mddtypes.hh"

#include "globals.hh"

#include "loggingutils.hh"

#ifdef __VISUALC__
#undef __EXECUTABLE__
#endif

// debug facility; relies on -DDEBUG at compile time
// tell debug that here is the place for the variables (to be done in the main() src file)
#define DEBUG_MAIN
#include "debug-clt.hh"
#include "storagemgr/sstoragelayout.hh"

#define SECURE_FREE_PTR(ptr) \
    if (ptr) { \
        free(ptr); \
        ptr = NULL; }
#define SECURE_DELETE_PTR(ptr) \
    if (ptr) { \
        delete ptr; \
        ptr = NULL; }

const int MAX_STR_LEN = 255;
const int MAX_QUERY_LEN = 10240;

// possible  types of output

typedef enum
{
    OUT_UNDEF,
    OUT_FILE,
    OUT_NONE,
    OUT_STRING,
    OUT_HEX,
    OUT_FORMATTED
} OUTPUT_TYPE;

// rasdaman MDD type for byte strings (default type used for file format reading)
#define MDD_STRINGTYPE  "GreyString"

#ifdef EXIT_FAILURE
#undef EXIT_FAILURE
#endif
/// program exit codes
#define EXIT_SUCCESS    0
#define EXIT_USAGE      2
#define EXIT_FAILURE    -1

#define DQ_CLIENT_ID 1000000
#define DQ_TIMEOUT 1000000
#define DQ_MANAGEMENT_INTERVAL 1000000
#define DQ_LISTEN_PORT 8001
#define DQ_SERVER_NAME "NT1"
#define DQ_CAPABILITY "$I1$ERW$BRASBASE$T1:3:2008:23:39:24$NNT1$D3839d047344677ddb1ff1a24dada286e$K"

#define STATUS_MORE_ELEMS 0
#define STATUS_MDD 0
#define STATUS_SCALAR 1
#define STATUS_EMPTY 2

// parameter names, defaults, and help texts

#define PARAM_HELP_FLAG 'h'
#define PARAM_HELP  "help"
#define HELP_HELP   "show command line switches"

#define PARAM_SERV_FLAG 's'
#define PARAM_SERV  "server"
#define HELP_SERV   "<host-name> rasdaman server"
#define DEFAULT_SERV    "localhost"

#define PARAM_PORT_FLAG 'p'
#define PARAM_PORT  "port"
#define HELP_PORT   "<p> rasmgr port number"
#define DEFAULT_PORT    7001
#define DEFAULT_PORT_STR "7001"

#define PARAM_DB_FLAG   'd'
#define PARAM_DB    "database"
#define HELP_DB     "<db-name> name of database"
#define DEFAULT_DB  "RASBASE"

#define PARAM_USER  "user"
#define HELP_USER   "<user-name> name of user"
#define DEFAULT_USER    "rasguest"

#define PARAM_PASSWD    "passwd"
#define HELP_PASSWD "<user-passwd> password of user"
#define DEFAULT_PASSWD  "rasguest"

#define PARAM_FILE_FLAG 'f'
#define PARAM_FILE  "file"
#define HELP_FILE   "<f> file name for upload through $i parameters within queries; each $i needs its own file parameter, in proper sequence. Requires --mdddomain and --mddtype"

#define PARAM_DOMAIN    "mdddomain"
#define HELP_DOMAIN "<mdd-domain> domain of marray, format: \'[x0:x1,y0:y1]\' (required only if --file specified and file is in data format r_Array)"

#define PARAM_MDDTYPE   "mddtype"
// this is for display only; internally MDD_STRINGTYPE is used
#define DEFAULT_MDDTYPE "byte string"
#define HELP_MDDTYPE    "<mdd-type> type of marray (required only if --file specified and file is in data format r_Array)"

#define PARAM_QUERY_FLAG 'q'
#define PARAM_QUERY "query"
#define HELP_QUERY  "<q> query string to be sent to the rasdaman server for execution"

#define PARAM_OUT   "out"
#define HELP_OUT    "<t> use display method t for cell values of result MDDs where t is one of none, file, formatted, string, hex. Implies --content"
#define DEFAULT_OUT OUT_NONE
#define PARAM_OUT_FILE  "file"
#define PARAM_OUT_STRING "string"
#define PARAM_OUT_HEX   "hex"
#define PARAM_OUT_FORMATTED "formatted"
#define PARAM_OUT_NONE  "none"
#define DEFAULT_OUT_STR PARAM_OUT_NONE

#define PARAM_CONTENT   "content"
#define HELP_CONTENT    "display result, if any (see also --out and --type for output formatting)"

#define PARAM_TYPE  "type"
#define HELP_TYPE   "display type information for results"

#define PARAM_OUTFILE_FLAG 'o'
#define PARAM_OUTFILE   "outfile"
#define HELP_OUTFILE    "<of> file name template for storing result images (ignored for scalar results). Use '%d' to indicate auto numbering position, like with printf(1). For well-known file types, a proper suffix is appended to the resulting file name. Implies --out file."
#define DEFAULT_OUTFILE "rasql_%d"

#define PARAM_QUIET "quiet"
#define HELP_QUIET  "print no ornament messages, only results and errors"

#define PARAM_DEBUG "debug"
#define HELP_DEBUG  "generate diagnostic output"


char globalConnectId[255] = {0};
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};
ServerComm* server;

// global variables and default settings
// -------------------------------------

bool dbIsOpen = false;
bool taIsOpen = false;

// suppress regular messages in log? (cmd line parameter '--quiet')
bool quietLog = false;

// TODO: remove this and use easylogging macros (LINFO)
// logging mechanism that respects 'quiet' flag:
#define INFO(a) { if (!quietLog) std::cout << a; }

std::string getDefaultDb()
{
    string rasmgrConfFilePath(string(CONFDIR) + "/" + string(RASMGR_CONF_FILE));
    if (rasmgrConfFilePath.length() >= PATH_MAX)
    {
        throw runtime_error("The path to the configuration file is longer than the maximum file system path.");
    }
    std::ifstream ifs(rasmgrConfFilePath);    // open config file

    if (!ifs)
    {
        throw runtime_error("Failed opening '" + rasmgrConfFilePath + "'.");
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
    return string(DEFAULT_DB);
}

int optionValueIndex = 0;

const char* serverName = DEFAULT_SERV;
r_ULong serverPort = DEFAULT_PORT;
auto baseName = getDefaultDb();

const char* user = DEFAULT_USER;
const char* passwd = DEFAULT_PASSWD;

const char* fileName = NULL;
const char* queryString = NULL;

bool output = false;
bool displayType = false;

OUTPUT_TYPE outputType = DEFAULT_OUT;

const char* outFileMask = DEFAULT_OUTFILE;
ServerComm::ClientTblElt* r;

r_Minterval mddDomain;
bool mddDomainDef = false;

const char* mddTypeName = NULL;
bool mddTypeNameDef = false;

// query result set.
// we define it here because on empty results the set seems to be corrupt which kills the default destructor
r_Set<r_Ref_Any> result_set;

// end of globals

//function prototypes:

void
parseParams(int argc, char** argv);

void
openDatabase();

void
closeDatabase();

void
openTransaction(bool readwrite);

void
closeTransaction(bool doCommit);

void
printScalar(char* buffer, QtData* data, unsigned int resultIndex);


void
printResult(Tile* tile, int resultIndex);

void
printOutput(unsigned short status, ExecuteQueryRes* result);

r_Marray_Type*
getTypeFromDatabase(const char* mddTypeName);

void
freeResult(ExecuteQueryRes* result);

void
printError(unsigned short status, ExecuteQueryRes* result);

void
printError(unsigned short status, ExecuteUpdateRes* result);

void
doStuff();

void
crashHandler(int sig, siginfo_t* info, void* ucontext);

void
cleanupHandler(int sig, siginfo_t* info, void* ucontext);

void
instalDirectqlSignalHandlers();



void
parseParams(int argc, char** argv)
{
    CommandLineParser& cmlInter = CommandLineParser::getInstance();

    CommandLineParameter& clp_help = cmlInter.addFlagParameter(PARAM_HELP_FLAG, PARAM_HELP, HELP_HELP);

    CommandLineParameter& clp_query = cmlInter.addStringParameter(PARAM_QUERY_FLAG, PARAM_QUERY, HELP_QUERY);
    CommandLineParameter& clp_file = cmlInter.addStringParameter(PARAM_FILE_FLAG, PARAM_FILE, HELP_FILE);

    CommandLineParameter& clp_content = cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_CONTENT, HELP_CONTENT);
    CommandLineParameter& clp_out = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_OUT, HELP_OUT, DEFAULT_OUT_STR);
    CommandLineParameter& clp_outfile = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_OUTFILE, HELP_OUTFILE, DEFAULT_OUTFILE);
    CommandLineParameter& clp_mddDomain = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_DOMAIN, HELP_DOMAIN);
    CommandLineParameter& clp_mddType = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_MDDTYPE, HELP_MDDTYPE, DEFAULT_MDDTYPE);
    CommandLineParameter& clp_type = cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_TYPE, HELP_TYPE);

    CommandLineParameter& clp_server = cmlInter.addStringParameter(PARAM_SERV_FLAG, PARAM_SERV, HELP_SERV, DEFAULT_SERV);
    CommandLineParameter& clp_port = cmlInter.addStringParameter(PARAM_PORT_FLAG, PARAM_PORT, HELP_PORT, DEFAULT_PORT_STR);
    CommandLineParameter& clp_database = cmlInter.addStringParameter(PARAM_DB_FLAG, PARAM_DB, HELP_DB, baseName.c_str());
    CommandLineParameter& clp_user = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_USER, HELP_USER, DEFAULT_USER);
    CommandLineParameter& clp_passwd = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_PASSWD, HELP_PASSWD, DEFAULT_PASSWD);
    CommandLineParameter& clp_quiet = cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_QUIET, HELP_QUIET);

#ifdef DEBUG
    CommandLineParameter& clp_debug = cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_DEBUG, HELP_DEBUG);
#endif

    try
    {
        cmlInter.processCommandLine(argc, argv);

        if (cmlInter.isPresent(PARAM_HELP_FLAG) || argc == 1)
        {
            cout << "usage: " << argv[0] << " [--query querystring|-q querystring] [options]" << endl;
            cout << "options:" << endl;
            cmlInter.printHelp();
            exit(EXIT_USAGE); //  FIXME: exit no good style!!
        }

        // check mandatory parameters ====================================================

        // evaluate mandatory parameter collection --------------------------------------
        if (cmlInter.isPresent(PARAM_QUERY))
        {
            queryString = cmlInter.getValueAsString(PARAM_QUERY);
        }
        else
        {
            throw RasqlError(NOQUERY);
        }

        // check optional parameters ====================================================

        // evaluate optional parameter file --------------------------------------
        if (cmlInter.isPresent(PARAM_FILE))
        {
            fileName = cmlInter.getValueAsString(PARAM_FILE);
        }

        // evaluate optional parameter server --------------------------------------
        if (cmlInter.isPresent(PARAM_SERV))
        {
            serverName = cmlInter.getValueAsString(PARAM_SERV);
        }

        // evaluate optional parameter port --------------------------------------
        if (cmlInter.isPresent(PARAM_PORT))
        {
            serverPort = cmlInter.getValueAsLong(PARAM_PORT);
        }

        // evaluate optional parameter database --------------------------------------
        if (cmlInter.isPresent(PARAM_DB))
        {
            baseName = string(cmlInter.getValueAsString(PARAM_DB));
        }

        // evaluate optional parameter user --------------------------------------
        if (cmlInter.isPresent(PARAM_USER))
        {
            user = cmlInter.getValueAsString(PARAM_USER);
        }

        // evaluate optional parameter passwd --------------------------------------
        if (cmlInter.isPresent(PARAM_PASSWD))
        {
            passwd = cmlInter.getValueAsString(PARAM_PASSWD);
        }

        // evaluate optional parameter content --------------------------------------
        if (cmlInter.isPresent(PARAM_CONTENT))
        {
            output = true;
        }

        // evaluate optional parameter type --------------------------------------
        if (cmlInter.isPresent(PARAM_TYPE))
        {
            displayType = true;
        }

        // evaluate optional parameter hex --------------------------------------
        if (cmlInter.isPresent(PARAM_OUT))
        {
            output = true;
            const char* val = cmlInter.getValueAsString(PARAM_OUT);
            if (val != 0 && strcmp(val, PARAM_OUT_STRING) == 0)
            {
                outputType = OUT_STRING;
            }
            else if (val != 0 && strcmp(val, PARAM_OUT_FILE) == 0)
            {
                outputType = OUT_FILE;
            }
            else if (val != 0 && strcmp(val, PARAM_OUT_FORMATTED) == 0)
            {
                outputType = OUT_FORMATTED;
            }
            else if (val != 0 && strcmp(val, PARAM_OUT_HEX) == 0)
            {
                outputType = OUT_HEX;
            }
            else if (val != 0 && strcmp(val, PARAM_OUT_NONE) == 0)
            {
                outputType = OUT_NONE;
            }
            else
            {
                throw RasqlError(ILLEGALOUTPUTTYPE);
            }
        }

        // evaluate optional parameter outfile --------------------------------------
        if (cmlInter.isPresent(PARAM_OUTFILE))
        {
            outFileMask = cmlInter.getValueAsString(PARAM_OUTFILE);
            outputType = OUT_FILE;
        }

        // evaluate optional parameter domain --------------------------------------
        if (cmlInter.isPresent(PARAM_DOMAIN))
        {
            try
            {
                mddDomain = r_Minterval(cmlInter.getValueAsString(PARAM_DOMAIN));
                mddDomainDef = true;
            }
            catch (r_Error& e) // Minterval constructor had syntax problems
            {
                throw RasqlError(NOVALIDDOMAIN);
            }
        }

        // evaluate optional parameter MDD type name --------------------------------------
        if (cmlInter.isPresent(PARAM_MDDTYPE))
        {
            mddTypeName = cmlInter.getValueAsString(PARAM_MDDTYPE);
            mddTypeNameDef = true;
        }

        // evaluate optional parameter 'quiet' --------------------------------------------
        if (cmlInter.isPresent(PARAM_QUIET))
        {
            quietLog = true;
        }

        // evaluate optional parameter MDD type name --------------------------------------
        SET_OUTPUT(cmlInter.isPresent(PARAM_DEBUG));

    }
    catch (CmlException& err)
    {
        cerr << err.what() << endl;
        throw RasqlError(ERRORPARSINGCOMMANDLINE);
    }
} // parseParams()

void
openDatabase()
{
    if (!dbIsOpen)
    {
        sprintf(globalConnectId, "%s", baseName.c_str());
        INFO("opening database " << baseName << " at " << serverName << ":" << serverPort << "..." << flush);
        server = new ServerComm(DQ_TIMEOUT, DQ_MANAGEMENT_INTERVAL, DQ_LISTEN_PORT,
                const_cast<char*>(serverName), serverPort, const_cast<char*>(DQ_SERVER_NAME));
        r = new ServerComm::ClientTblElt(user, DQ_CLIENT_ID);
        server->addClientTblEntry(r);
        accessControl.setServerName(DQ_SERVER_NAME);
        accessControl.crunchCapability(DQ_CAPABILITY);
        server->openDB(DQ_CLIENT_ID, baseName.c_str(), user);

        ObjectBroker::init();

        dbIsOpen = true;
        INFO(" ok" << endl << flush);
    }
} // openDatabase()

void
closeDatabase()
{
    if (dbIsOpen)
    {
        LDEBUG << "database was open, closing it";
        ObjectBroker::clearBroker();
        ObjectBroker::deinit();
        server->closeDB(DQ_CLIENT_ID);
        dbIsOpen = false;
    }

    return;
} // closeDatabase()

void
openTransaction(bool readwrite)
{
    if (!taIsOpen)
    {
        if (readwrite)
        {
            LDEBUG << "transaction was closed, opening rw...";
            server->beginTA(DQ_CLIENT_ID, false);
            LDEBUG << "ok";
        }
        else
        {
            LDEBUG << "transaction was closed, opening ro...";
            server->beginTA(DQ_CLIENT_ID, true);
            LDEBUG << "ok";
        }

        taIsOpen = true;
    }
} // openTransaction()

void
closeTransaction(bool doCommit)
{
    if (taIsOpen)
    {
        if (doCommit)
        {
            LDEBUG << "transaction was open, committing it...";
            server->commitTA(DQ_CLIENT_ID);
            LDEBUG << "ok";
        }
        else
        {
            LDEBUG << "transaction was open, aborting it...";
            server->abortTA(DQ_CLIENT_ID);
            LDEBUG << "ok";
        }
        taIsOpen = false;
    }
    if (configuration.isLockMgrOn())
    {
        LockManager* lockManager = LockManager::Instance();
        lockManager->clearLockTable();
    }
    return;
} // closeTransaction()

void printScalar(char* buffer, QtData* data, unsigned int resultIndex)
{
    INFO("  Result element " << resultIndex << ": ");

    switch (data->getDataType())
    {
    case QT_BOOL:
        INFO((*((bool*) buffer) ? "t" : "f") << flush);
        break;

    case QT_CHAR:
        INFO(static_cast<int>(*((r_Char*) buffer)) << flush);
        break;

    case QT_OCTET:
        INFO(static_cast<int>(*((r_Octet*) buffer)) << flush);
        break;

    case QT_SHORT:
        INFO(*((r_Short*) buffer) << flush);
        break;

    case QT_USHORT:
        INFO(*((r_UShort*) buffer) << flush);
        break;

    case QT_LONG:
        INFO(*((r_Long*) buffer) << flush);
        break;

    case QT_ULONG:
        INFO(*((r_ULong*) buffer) << flush);
        break;

    case QT_FLOAT:
        INFO(std::setprecision(std::numeric_limits<float>::digits10 + 1) << *((r_Float*) buffer) << flush);
        break;

    case QT_DOUBLE:
        INFO(std::setprecision(std::numeric_limits<double>::digits10 + 1) << *((r_Double*) buffer) << flush);
        break;

    case QT_COMPLEXTYPE1:
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(data);
        ComplexType1* ct = static_cast<ComplexType1*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
        auto re = *((r_Float*) (buffer + ct->getReOffset()));
        auto im = *((r_Float*) (buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }
    case QT_COMPLEXTYPE2:
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(data);
        ComplexType2* ct = static_cast<ComplexType2*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
        auto re = *((r_Double*) (buffer + ct->getReOffset()));
        auto im = *((r_Double*) (buffer + ct->getImOffset()));
        INFO("(" << re << "," << im << ")" << flush)
        break;
    }

    case QT_COMPLEX:
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(data);
        StructType* st = static_cast<StructType*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
        INFO("{ ");
        for (unsigned int i = 0; i < st->getNumElems(); i++)
        {
            BaseType* bt = const_cast<BaseType*>(st->getElemType(i));
            if (i > 0)
            {
                INFO(", ");
            }
            bt->printCell(cout, buffer);

            buffer += bt->getSize();
        }
        INFO(" }" << flush);
    }
    break;

    case QT_STRING:
    case QT_INTERVAL:
    case QT_MINTERVAL:
    case QT_POINT:
        INFO(buffer << flush);
        break;
    default:
        INFO("scalar type not supported!" << endl);
        break;
    }
    INFO(endl << flush);
} // printScalar()


// result_set should be parameter, but is global -- see def for reason
void printResult(Tile* tile, int resultIndex)
{
    const char* theStuff = tile->getContents();
    r_Bytes numCells = tile->getSize();

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
    }
    break;
    case OUT_HEX:
    {
        INFO("  Result object " << resultIndex << ": ");
        cout << hex;
        for (r_Bytes cnt = 0; cnt < numCells; cnt++)
        {
            cout << setw(2) << (unsigned short)(0xff & theStuff[cnt]) << " ";
        }
        cout << dec << endl;
    }
    break;
    case OUT_FILE:
    {
        char defFileName[FILENAME_MAX];
        (void) snprintf(defFileName, sizeof(defFileName) - 1, outFileMask, resultIndex);
        LDEBUG << "filename for #" << resultIndex << " is " << defFileName;

        // special treatment only for DEFs
        r_Data_Format mafmt = tile->getDataFormat();
        switch (mafmt)
        {
        case r_TIFF:
            strcat(defFileName, ".tif");
            break;
        case r_JP2:
            strcat(defFileName, ".jp2");
            break;
        case r_JPEG:
            strcat(defFileName, ".jpg");
            break;
        case r_HDF:
            strcat(defFileName, ".hdf");
            break;
        case r_PNG:
            strcat(defFileName, ".png");
            break;
        case r_BMP:
            strcat(defFileName, ".bmp");
            break;
        case r_NETCDF:
            strcat(defFileName, ".nc");
            break;
        case r_CSV:
            strcat(defFileName, ".csv");
            break;
        case r_JSON:
            strcat(defFileName, ".json");
            break;
        case r_DEM:
            strcat(defFileName, ".dem");
            break;
        default:
            strcat(defFileName, ".unknown");
            break;
        }

        INFO("  Result object " << resultIndex << ": going into file " << defFileName << "..." << flush);
        FILE* tfile = fopen(defFileName, "wb");
        if (tfile == NULL)
        {
            throw RasqlError(NOFILEWRITEPERMISSION);
        }
        if (fwrite(static_cast<void*>(const_cast<char*>(theStuff)), 1, numCells, tfile) != numCells)
        {
            fclose(tfile);
            throw RasqlError(UNABLETOWRITETOFILE);
        };
        fclose(tfile);
        INFO("ok." << endl);
    }
    break;
    default:
        cerr << "Internal error: unknown output type, ignoring action: " << static_cast<int>(outputType) << endl;
        break;
    } // switch(outputType)

} // printResult()

void printOutput(unsigned short status, ExecuteQueryRes* result)
{
    switch (status)
    {
    case STATUS_MDD:
        INFO("holds MDD elements" << endl);
        break;
    case STATUS_SCALAR:
        INFO("holds non-MDD elements" << endl);
        break;
    case STATUS_EMPTY:
        INFO("holds no elements" << endl);
        break;
    default:
        break;
    };

    if (result)
    {
        if (output)
        {
            INFO("Getting result..." << flush);
            if (status == STATUS_MDD)
            {
                INFO("Getting MDD objects..." << endl << flush);

                char* typeName = NULL;
                char* typeStructure = NULL;
                r_OId oid;
                unsigned short currentFormat;

                int resultIndex = 0;
                while (server->getNextMDD(DQ_CLIENT_ID, mddDomain, typeName, 
                    typeStructure, oid, currentFormat) == STATUS_MORE_ELEMS)
                {
                    Tile* resultTile = new Tile(r->transTiles);
                    printResult(resultTile, ++resultIndex);
                    delete resultTile;

                    // cleanup
                    (*(r->transferDataIter))++;
                    if (*(r->transferDataIter) != r->transferData->end())
                    {
                        SECURE_DELETE_PTR(r->transTiles);
                        SECURE_DELETE_PTR(r->tileIter);
                    }
                    SECURE_FREE_PTR(typeStructure);
                    SECURE_FREE_PTR(typeName);
                }
            }
            else if (status == STATUS_SCALAR)
            {
                INFO("Getting scalars..." << endl << flush);

                unsigned int resultIndex = 0;
                char* buffer;
                unsigned int bufferSize;
                status = STATUS_MORE_ELEMS;
                while (status == STATUS_MORE_ELEMS)
                {
                    QtData* data = (**(r->transferDataIter));
                    status = server->getNextElement(DQ_CLIENT_ID, buffer, bufferSize);
                    printScalar(buffer, data, ++resultIndex);
                    if (buffer)
                    {
                        free(buffer);
                        buffer = NULL;
                    }
                }
            }
        }
        server->endTransfer(DQ_CLIENT_ID);
    }
}

/*
 * get database type structure from type name
 * returns ptr if an MDD type with the given name exists in the database, NULL otherwise
 * throws r_Error upon general database comm error
 * needs an open transaction
 */
r_Marray_Type* getTypeFromDatabase(const char* mddTypeName2)
{
    r_Marray_Type* retval = NULL;
    char* typeStructure = NULL;

    // first, try to get type structure from database using a separate r/o transaction
    try
    {
        server->getTypeStructure(DQ_CLIENT_ID, mddTypeName2, ClientComm::r_MDDType_Type, typeStructure);

        // above doesn't seem to work, so at least make it work with inv_* functions -- DM 2013-may-19
        if (!typeStructure)
        {
            typeStructure = strdup("marray<char>");
        }
        LDEBUG << "type structure is " << typeStructure;
    }
    catch (r_Error& err)
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
        else // unanticipated error
        {
            LDEBUG << "Error during type retrieval from database: " << err.get_errorno() << " " << err.what();
            throw;
        }
    }

    // next, find out whether it is an MDD type (and not a base or set type, eg)
    r_Type* tempType = NULL;
    try
    {
        tempType = r_Type::get_any_type(typeStructure);
        LDEBUG << "get_any_type() for this type returns: " << tempType;
        if (tempType->isMarrayType())
        {
            retval = (r_Marray_Type*) tempType;
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
    catch (r_Error& err)
    {
        LDEBUG << "Error during retrieval of MDD type structure (" << typeStructure << "): " << err.get_errorno() << " " << err.what();
        SECURE_FREE_PTR(typeStructure);
        SECURE_DELETE_PTR(tempType);
        throw;
    }

    SECURE_FREE_PTR(typeStructure);

    return retval;
} // getTypeFromDatabase()

void freeResult(ExecuteQueryRes* result)
{
    SECURE_FREE_PTR(result->typeStructure);
    SECURE_FREE_PTR(result->token);
    SECURE_FREE_PTR(result->typeName);
}

void printError(unsigned short status, ExecuteQueryRes* result)
{
    cerr << endl << "Error number: " << result->errorNo << " Token: '" << result->token <<
         "' Line: " << result->lineNo << " Column: " << result->columnNo << " (status: " << status << ")" << endl << flush;
}

void printError(unsigned short status, ExecuteUpdateRes* result)
{
    cerr << endl << "Error number: " << result->errorNo << " Token: '" << result->token <<
         "' Line: " << result->lineNo << " Column: " << result->columnNo << " (status: " << status << ")" << endl << flush;
}

void doStuff()
{
    char* fileContents = NULL; // contents of file satisfying "$1" parameter in query
    r_Marray_Type* mddType = NULL; // this MDD's type
    RPCMarray* marray = NULL;
    r_Bytes baseTypeSize = 0;

    r_OQL_Query query(queryString);
    LDEBUG << "query is: " << query.get_query();

    try
    {
        if (fileName != NULL)
        {
            openTransaction(false);

            // if no type name was specified then assume byte string (for encoded files)
            if (!mddTypeNameDef)
            {
                mddTypeName = MDD_STRINGTYPE;
            }

            INFO("fetching type information for " << mddTypeName << " from database, using readonly transaction..." << flush);
            mddType = getTypeFromDatabase(mddTypeName);
            closeTransaction(true);
            INFO("ok" << endl);

            INFO("reading file " << fileName << "..." << flush);
            FILE* fileD = fopen(fileName, "r");
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
                fileContents = static_cast<char*>(mymalloc(static_cast<size_t>(size)));
                fseek(fileD, 0, SEEK_SET);
                size_t rsize = fread(fileContents, 1, static_cast<size_t>(size), fileD);

                baseTypeSize = mddType->base_type().size();
                r_GMarray* fileMDD = new r_GMarray(mddDomain, baseTypeSize, 0, NULL, false);
                fileMDD->set_type_schema(mddType);
                fileMDD->set_array_size(baseTypeSize);
                query << *fileMDD;
                SECURE_DELETE_PTR(fileMDD);
                SECURE_DELETE_PTR(mddType);

                marray = (RPCMarray*) mymalloc(sizeof(RPCMarray));
                marray->cellTypeLength = baseTypeSize;
                marray->domain = mddDomain.get_string_representation();
                marray->currentFormat = r_Array;
                marray->storageFormat = r_Array;
                marray->data.confarray_len = mddDomain.cell_count() * baseTypeSize;
                marray->data.confarray_val = fileContents;
            }
            catch (std::bad_alloc)
            {
                LDEBUG << "Unable to claim memory: " << size << " Bytes";
                throw RasqlError(UNABLETOCLAIMRESOURCEFORFILE);
            }

            fclose(fileD);

            INFO("ok" << endl);
        }

        if (query.is_insert_query())
        {
            INFO("Executing insert query...\n" << flush);

            openTransaction(true);

            ExecuteQueryRes result;
            result.token = NULL;
            result.typeName = NULL;
            result.typeStructure = NULL;
            unsigned short status;

            if (fileContents != NULL)
            {
                server->initExecuteUpdate(DQ_CLIENT_ID);
                server->startInsertTransMDD(DQ_CLIENT_ID, mddDomain, baseTypeSize, mddTypeName);
                server->insertTile(DQ_CLIENT_ID, false, marray);
                server->endInsertMDD(DQ_CLIENT_ID, false);
                status = server->executeInsert(DQ_CLIENT_ID, query.get_query(), result);
                query.reset_query();

                server->endTransfer(DQ_CLIENT_ID);
            }
            else
            {
                status = server->executeInsert(DQ_CLIENT_ID, queryString, result);
            }

            if (status == 0 || status == 1 || status == 2)
            {
                printOutput(status, &result);
            }
            else
            {
                printError(status, &result);
            }
            freeResult(&result);
            closeTransaction(true);
        }
        else if (query.is_update_query())
        {
            INFO("Executing update query...\n" << flush);

            openTransaction(true);

            ExecuteUpdateRes result;
            result.token = NULL;
            unsigned short status;

            if (fileContents != NULL)
            {
                server->initExecuteUpdate(DQ_CLIENT_ID);
                server->startInsertTransMDD(DQ_CLIENT_ID, mddDomain, baseTypeSize, mddTypeName);
                server->insertTile(DQ_CLIENT_ID, false, marray);
                server->endInsertMDD(DQ_CLIENT_ID, false);
                status = server->executeUpdate(DQ_CLIENT_ID, query.get_query(), result);
                query.reset_query();

                server->endTransfer(DQ_CLIENT_ID);
            }
            else
            {
                status = server->executeUpdate(DQ_CLIENT_ID, queryString, result);
            }

            if (status != 0 && status != 1)
            {
                printError(status, &result);
            }
            SECURE_FREE_PTR(result.token);
            closeTransaction(true);
        }
        else // retrieval query
        {
            INFO("Executing retrieval query...\n" << flush);

            openTransaction(false);

            ExecuteQueryRes result;
            result.errorNo = 0;
            result.token = NULL;
            result.typeName = NULL;
            result.typeStructure = NULL;

            unsigned short status =
                server->executeQuery(DQ_CLIENT_ID, queryString, result);

            if (status >= 0 && status <= 2) {
                printOutput(status, &result);
            } else if (result.token != NULL) {
                r_Equery_execution_failed e(
                    result.errorNo, result.lineNo, result.columnNo, result.token);
                LERROR << "rasdaman error " << e.get_errorno() << ": " << e.what();
            }
            
            freeResult(&result);

            closeTransaction(true);
        }
    }
    catch (r_Error& err)
    {
        SECURE_FREE_PTR(fileContents);
        if (marray)
        {
            SECURE_FREE_PTR(marray->domain);
            SECURE_FREE_PTR(marray);
        }
        throw err;
    }

    if (marray)
    {
        SECURE_FREE_PTR(marray->domain);
        SECURE_FREE_PTR(marray);
    }
}

void
crashHandler(__attribute__((unused)) int sig, __attribute__((unused)) siginfo_t* info, void* ucontext)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        cerr << "\nInterrupted by signal " << common::SignalHandler::toString(info)
             << "... stacktrace:\n" << common::SignalHandler::getStackTrace()
             << "\nClosing server connection... ";
        // closeTransaction(false);
        // closeDatabase();
        cerr << "done, exiting." << endl;
    }
    else
    {
        // if a signal comes while the handler has already been invoked,
        // wait here for max 3 seconds, so that the handler above has some time
        // (hopefully) finish
        sleep(3);
    }
    exit(sig);
}

void
cleanupHandler(__attribute__((unused)) int sig, __attribute__((unused)) siginfo_t* info, void* ucontext)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        NNLINFO << "\ndirectql: Interrupted by signal " << common::SignalHandler::signalName(sig)
                << "\nClosing server connection... ";
        // closeTransaction(false);
        // closeDatabase();
        BLINFO << "done, exiting.";
        exit(sig);
    }
}

void
instalDirectqlSignalHandlers()
{
    common::SignalHandler::handleAbortSignals(crashHandler);
    common::SignalHandler::handleShutdownSignals(cleanupHandler);
    common::SignalHandler::ignoreStandardSignals();
}

INITIALIZE_EASYLOGGINGPP

/*
 * returns 0 on success, -1 on error
 */
int main(int argc, char** argv)
{
    common::LogConfiguration logConf(string(CONFDIR), CLIENT_LOG_CONF);
    logConf.configClientLogging();

    SET_OUTPUT(false); // inhibit unconditional debug output, await cmd line evaluation

    int retval = EXIT_SUCCESS; // overall result status

    instalDirectqlSignalHandlers();

    TileCache::cacheLimit = 0;

    try
    {
        parseParams(argc, argv);
        
        if (quietLog)
            logConf.configClientLogging(true);

        // put INFO after parsing parameters to respect a '--quiet'
        INFO(argv[0] << ": rasdaman query tool v1.0, rasdaman " << RMANVERSION << "." << endl);

        openDatabase();
        doStuff();
        closeDatabase();
 
        retval = EXIT_SUCCESS;
    }
    catch (RasqlError& e)
    {
        cerr << argv[0] << ": " << e.what() << endl;
        retval = EXIT_FAILURE;
    }

    catch (const r_Error& e)
    {
        cerr << "rasdaman error " << e.get_errorno() << ": " << e.what() << endl;
        retval = EXIT_FAILURE;
    }
    catch (std::exception& e)
    {
        LERROR << argv[0] << ": " << e.what();
        retval = EXIT_FAILURE;
    }
    catch (...)
    {
        LERROR << argv[0] << ": unexpected internal exception.";
        retval = EXIT_FAILURE;
    }

    if (retval != EXIT_SUCCESS && (dbIsOpen || taIsOpen))
    {
        INFO("aborting transaction..." << flush);
        closeTransaction(false); // abort transaction and close database, ignore any further exceptions
        INFO("ok" << endl);
        closeDatabase();
    }

    INFO(argv[0] << " done." << endl);
    return retval;
} // main()

// end of rasql.cc

