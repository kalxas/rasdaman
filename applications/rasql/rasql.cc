/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/**
* rasql
*
* PURPOSE:
*    Provides a command line interpretter for rasql queries, with
*   options for displaying results or storing them to file(s)
*
* COMMENTS:
*
* BUGS:
* - query filename "" is interpreted as stdin
* - mafmt does not passed correctly
*/


#include "version.h"
#include "config.h"
#ifndef RMANVERSION
#error "Please specify RMANVERSION variable!"
#endif

#include "rasql_error.hh"
#include "raslib/type.hh"
#include "raslib/error.hh"
#include "raslib/marraytype.hh"
#include "raslib/minterval.hh"
#include "raslib/primitive.hh"
#include "raslib/complex.hh"
#include "raslib/structure.hh"
#include "raslib/structuretype.hh"
#include "raslib/stringdata.hh"
#include "raslib/basetype.hh"

#include "rasodmg/transaction.hh"
#include "rasodmg/database.hh"
#include "rasodmg/ref.hh"
#include "rasodmg/set.hh"
#include "rasodmg/iterator.hh"
#include "rasodmg/oqlquery.hh"
#include "rasodmg/storagelayout.hh"
#include "rasodmg/alignedtiling.hh"
#include "rasodmg/gmarray.hh"

#include "common/logging/signalhandler.hh"
#include "clientcomm/clientcomm.hh"
#include "common/commline/cmlparser.hh"
#include "common/util/fileutils.hh"
#include "loggingutils.hh"
#include "globals.hh"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sstream>
#include <fstream>
#include <vector>
#include <stdexcept>
#include <limits>
#include <iomanip>
#include <memory>
#include <complex>

using namespace std;

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

// parameter names, defaults, and help texts

#define PARAM_HELP_FLAG 'h'
#define PARAM_HELP  "help"
#define HELP_HELP   "show command line switches"

#define PARAM_SERV_FLAG 's'
#define PARAM_SERV  "server"
#define HELP_SERV   "<host-name> rasdaman server"

#define PARAM_PORT_FLAG 'p'
#define PARAM_PORT  "port"
#define HELP_PORT   "<p> rasmgr port number"

#define PARAM_DB_FLAG   'd'
#define PARAM_DB    "database"
#define HELP_DB     "<db-name> name of database"

#define PARAM_USER  "user"
#define HELP_USER   "<user-name> name of user"

#define PARAM_PASSWD    "passwd"
#define HELP_PASSWD "<user-passwd> password of user"

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

#define PARAM_QUERYFILE "queryfile"
#define HELP_QUERYFILE  "<file> file containing the query string to be sent to the rasdaman server for execution"

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

#include <logging.hh>

// global variables and default settings
// -------------------------------------

r_Database db;
r_Transaction ta{&db};

bool dbIsOpen = false;
bool taIsOpen = false;

// suppress regular messages in log? (cmd line parameter '--quiet')
bool quietLog = false;

int  optionValueIndex = 0;

const char *serverName = DEFAULT_HOSTNAME;
r_ULong serverPort = DEFAULT_PORT;
const char *baseName = DEFAULT_DBNAME;

const char *user = DEFAULT_USER;
const char *passwd = DEFAULT_PASSWD;

const char *fileName = NULL;
const char *queryString = NULL;
std::string queryStringFromFile;

bool output = false;
bool displayType = false;

OUTPUT_TYPE outputType = DEFAULT_OUT;

const char *outFileMask = DEFAULT_OUTFILE;

r_Minterval mddDomain;
bool mddDomainDef = false;

const char *mddTypeName = NULL;
bool mddTypeNameDef = false;

// query result set.
// we define it here because on empty results the set seems to be corrupt which kills the default destructor
r_Set<r_Ref_Any> result_set;

// end of globals

//function prototypes:

void
parseParams(int argc, char **argv);

bool
openDatabase();

bool
closeDatabase();

bool
openTransaction(bool readwrite);

bool
closeTransaction(bool doCommit);

void
cleanConnection();

void
printScalar(const r_Scalar &scalar);

void
writeScalarToFile(const r_Scalar &scalar, unsigned int fileNum);

void
writeScalarToFileStream(const r_Scalar &scalar, std::ofstream &file);

void
writeStructToFileStream(const r_Structure *const structValue, std::ofstream &file);

void
printResult();

void
writeStringToFile(const std::string &str, unsigned int fileNum);

r_Marray_Type *
getTypeFromDatabase(const char *mddTypeName2);

void
doStuff(int argc, char **argv);

void
shutdownHandler(int sig, siginfo_t *info, void *ucontext);

void
crashHandler(int sig, siginfo_t *info, void *ucontext);

void
parseParams(int argc, char **argv)
{
    const auto nsn = CommandLineParser::noShortName;
    
    CommandLineParser    &cmlInter      = CommandLineParser::getInstance();

    CommandLineParameter &clp_help      = cmlInter.addFlagParameter(PARAM_HELP_FLAG, PARAM_HELP, HELP_HELP);

    CommandLineParameter &clp_query     = cmlInter.addStringParameter(PARAM_QUERY_FLAG, PARAM_QUERY, HELP_QUERY);
    CommandLineParameter &clp_queryfile = cmlInter.addStringParameter(nsn, PARAM_QUERYFILE, HELP_QUERYFILE);
    CommandLineParameter &clp_file      = cmlInter.addStringParameter(PARAM_FILE_FLAG, PARAM_FILE, HELP_FILE);

    CommandLineParameter &clp_content   = cmlInter.addFlagParameter(nsn, PARAM_CONTENT, HELP_CONTENT);
    CommandLineParameter &clp_out       = cmlInter.addStringParameter(nsn, PARAM_OUT, HELP_OUT, DEFAULT_OUT_STR);
    CommandLineParameter &clp_outfile   = cmlInter.addStringParameter(nsn, PARAM_OUTFILE, HELP_OUTFILE, DEFAULT_OUTFILE);
    CommandLineParameter &clp_mddDomain = cmlInter.addStringParameter(nsn, PARAM_DOMAIN, HELP_DOMAIN);
    CommandLineParameter &clp_mddType   = cmlInter.addStringParameter(nsn, PARAM_MDDTYPE, HELP_MDDTYPE, DEFAULT_MDDTYPE);
    CommandLineParameter &clp_type      = cmlInter.addFlagParameter(nsn, PARAM_TYPE, HELP_TYPE);

    CommandLineParameter &clp_server    = cmlInter.addStringParameter(PARAM_SERV_FLAG, PARAM_SERV, HELP_SERV, DEFAULT_HOSTNAME);
    CommandLineParameter &clp_port      = cmlInter.addStringParameter(PARAM_PORT_FLAG, PARAM_PORT, HELP_PORT, STRINGIFY(DEFAULT_PORT));
    CommandLineParameter &clp_database  = cmlInter.addStringParameter(PARAM_DB_FLAG, PARAM_DB, HELP_DB, DEFAULT_DBNAME);
    CommandLineParameter &clp_user      = cmlInter.addStringParameter(nsn, PARAM_USER, HELP_USER, DEFAULT_USER);
    CommandLineParameter &clp_passwd    = cmlInter.addStringParameter(nsn, PARAM_PASSWD, HELP_PASSWD, DEFAULT_PASSWD);
    CommandLineParameter &clp_quiet     = cmlInter.addFlagParameter(nsn, PARAM_QUIET, HELP_QUIET);

#ifdef DEBUG
    CommandLineParameter &clp_debug     = cmlInter.addFlagParameter(nsn, PARAM_DEBUG, HELP_DEBUG);
#endif

    try
    {
        cmlInter.processCommandLine(argc, argv);

        if (clp_help.isPresent() || argc == 1)
        {
            cout << "rasql: rasdaman query tool " << RMANVERSION << "." << endl;
            cout << "usage: " << argv[0] << " [--query querystring|-q querystring] [options]" << endl;
            cout << "options:" << endl;
            cmlInter.printHelp();
            exit(EXIT_USAGE);        //  FIXME: exit no good style!!
        }

        // check mandatory parameters ====================================================

        // evaluate mandatory parameter collection --------------------------------------
        if (clp_query.isPresent())
        {
            queryString = clp_query.getValueAsString();
        }
        else if (clp_queryfile.isPresent())
        {
            const char *file = clp_queryfile.getValueAsString();
            queryStringFromFile = common::FileUtils::readFileToString(file);
            queryString = queryStringFromFile.c_str();
        }
        else
        {
            throw RasqlError(NOQUERY);
        }

        // check optional parameters ====================================================

        // evaluate optional parameter file --------------------------------------
        if (clp_file.isPresent())
        {
            fileName = clp_file.getValueAsString();
        }

        // evaluate optional parameter server --------------------------------------
        if (clp_server.isPresent())
        {
            serverName = clp_server.getValueAsString();
        }

        // evaluate optional parameter port --------------------------------------
        if (clp_port.isPresent())
        {
            serverPort = clp_port.getValueAsLong();
        }

        // evaluate optional parameter database --------------------------------------
        if (clp_database.isPresent())
        {
            baseName = clp_database.getValueAsString();
        }

        // evaluate optional parameter user --------------------------------------
        if (clp_user.isPresent())
        {
            user = clp_user.getValueAsString();
        }

        // evaluate optional parameter passwd --------------------------------------
        if (clp_passwd.isPresent())
        {
            passwd = clp_passwd.getValueAsString();
        }

        // evaluate optional parameter content --------------------------------------
        output = clp_content.isPresent();

        // evaluate optional parameter type --------------------------------------
        displayType = clp_type.isPresent();

        // evaluate optional parameter hex --------------------------------------
        if (clp_out.isPresent())
        {
            output = true;
            const char *val = clp_out.getValueAsString();
            if (val == 0)
                throw RasqlError(ILLEGALOUTPUTTYPE);
            if (strcmp(val, PARAM_OUT_STRING) == 0)
                outputType = OUT_STRING;
            else if (strcmp(val, PARAM_OUT_FILE) == 0)
                outputType = OUT_FILE;
            else if (strcmp(val, PARAM_OUT_FORMATTED) == 0)
                outputType = OUT_FORMATTED;
            else if (strcmp(val, PARAM_OUT_HEX) == 0)
                outputType = OUT_HEX;
            else if (strcmp(val, PARAM_OUT_NONE) == 0)
                outputType = OUT_NONE;
            else
                throw RasqlError(ILLEGALOUTPUTTYPE);
        }

        // evaluate optional parameter outfile --------------------------------------
        if (clp_outfile.isPresent())
        {
            outFileMask = clp_outfile.getValueAsString();
            outputType = OUT_FILE;
        }

        // evaluate optional parameter domain --------------------------------------
        if (clp_mddDomain.isPresent())
        {
            try
            {
                mddDomain = r_Minterval(clp_mddDomain.getValueAsString());
                mddDomainDef = true;
            }
            catch (r_Error &e)              // Minterval constructor had syntax problems
            {
                cerr << "invalid domain '" << clp_mddDomain.getValueAsString() << ": " << e.what();
                throw RasqlError(NOVALIDDOMAIN);
            }
        }
        
        // evaluate optional parameter MDD type name --------------------------------------
        if (clp_mddType.isPresent())
        {
            mddTypeName = clp_mddType.getValueAsString();
            mddTypeNameDef = true;
        }

        // evaluate optional parameter 'quiet' --------------------------------------------
        quietLog = clp_quiet.isPresent();

#ifdef DEBUG
        // evaluate optional parameter MDD type name --------------------------------------
        //SET_OUTPUT(cmlInter.isPresent(PARAM_DEBUG));
#endif

    }
    catch (CmlException &err)
    {
        cerr << err.what() << endl;
        throw RasqlError(ERRORPARSINGCOMMANDLINE);
    }
} // parseParams()


bool
openDatabase()
{
    if (! dbIsOpen)
    {
        NNLINFO << "Opening database " << baseName << " at " << serverName << ":" << serverPort << "... ";
        db.set_servername(serverName, static_cast<int>(serverPort));
        db.set_useridentification(user, passwd);
        db.open(baseName);
        BLINFO << "ok.\n";
        dbIsOpen = true;
    }
    return dbIsOpen;
} // openDatabase()

bool
closeDatabase()
{
    if (dbIsOpen)
    {
        LDEBUG << "Closing database...";
        db.close();
        LDEBUG << "Successfully closed database.";
        dbIsOpen = false;
    }
    return !dbIsOpen;
} // closeDatabase()

bool
openTransaction(bool readwrite)
{
    if (! taIsOpen)
    {
        LDEBUG << "Opening " << (readwrite ? "rw" : "ro") << " transaction... ";
        if (readwrite)
        {
            ta.begin(r_Transaction::read_write);
        }
        else
        {
            ta.begin(r_Transaction::read_only);
        }

        LDEBUG << "Successfully opened transaction.";
        taIsOpen = true;
    }
    return taIsOpen;
} // openTransaction()

bool
closeTransaction(bool doCommit)
{
    if (taIsOpen)
    {
        if (doCommit)
        {
            LDEBUG << "Committing transaction... ";
            ta.commit();
            LDEBUG << "Transaction committed successfully.";
        }
        else
        {
            NNLINFO << "aborting transaction... ";
            ta.abort();
            BLINFO << "ok.\n";
        }

        taIsOpen = false;
    }
    return !taIsOpen;
} // closeTransaction()

void
cleanConnection()
{
    try
    {
        closeTransaction(false); // abort
    }
    catch (...)
    {
        // ignore
    }
    try
    {
        closeDatabase();
    }
    catch (...)
    {
        // ignore
    }
}

void printScalar(const r_Scalar &scalar)
{
    switch (scalar.get_type()->type_id())
    {
    case r_Type::BOOL:
        NNLINFO << (static_cast<const r_Primitive &>(scalar).get_boolean() ? "t" : "f");
        break;

    case r_Type::CHAR:
        NNLINFO << static_cast<int>(static_cast<const r_Primitive &>(scalar).get_char());
        break;

    case r_Type::OCTET:
        NNLINFO << static_cast<int>(static_cast<const r_Primitive &>(scalar).get_octet());
        break;

    case r_Type::SHORT:
        NNLINFO << static_cast<const r_Primitive &>(scalar).get_short();
        break;

    case r_Type::USHORT:
        NNLINFO << static_cast<const r_Primitive &>(scalar).get_ushort();
        break;

    case r_Type::LONG:
        NNLINFO << static_cast<const r_Primitive &>(scalar).get_long();
        break;

    case r_Type::ULONG:
        NNLINFO << static_cast<const r_Primitive &>(scalar).get_ulong();
        break;

    case r_Type::FLOAT:
        NNLINFO << std::setprecision(std::numeric_limits<float>::digits10 + 1) 
                << static_cast<const r_Primitive &>(scalar).get_float();
        break;

    case r_Type::DOUBLE:
        NNLINFO << std::setprecision(std::numeric_limits<double>::digits10 + 1)
                << static_cast<const r_Primitive &>(scalar).get_double();
        break;

    case r_Type::COMPLEXTYPE1:
    case r_Type::COMPLEXTYPE2:
        NNLINFO << "(" << static_cast<const r_Complex &>(scalar).get_re() << "," 
                       << static_cast<const r_Complex &>(scalar).get_im() << ")";
        break;
    case r_Type::CINT16:
    case r_Type::CINT32:
	      NNLINFO << "(" << static_cast<const r_Complex &>(scalar).get_re_long() << ","
                         << static_cast<const r_Complex &>(scalar).get_im_long() << ")";
        break;
    case r_Type::STRUCTURETYPE:
    {
        const auto &structValue = static_cast<const r_Structure &>(scalar);
        NNLINFO << "{ ";
        for (unsigned int i = 0; i < structValue.count_elements(); i++)
        {
            printScalar(structValue[i]);
            if (i < structValue.count_elements() - 1)
            {
                BLINFO << ", ";
            }
        }
        BLINFO << " }";
    }
    break;
    default:
        LWARNING << "scalar type '" << scalar.get_type()->type_id() <<  "' not supported!";
        break;
    }
} // printScalar()

/*
 * writes the given scalar to the output file with file number
 * uses default file naming convention with file number for auto numbering
 * preserves high precision for float and double values
 * throws UNABLETOWRITETOFILE on write failure
 */
void writeScalarToFile(const r_Scalar &scalar, unsigned int fileNum)
{
    char defFileName[FILENAME_MAX];
    (void) snprintf(defFileName, sizeof(defFileName) - 1, outFileMask, fileNum);
    strcat(defFileName, ".txt");

    std::ofstream file(defFileName, std::ios::out);
    if (file.is_open())
    {
        NNLINFO << "  Result object " << fileNum << ": going into file " << defFileName << "... ";

        switch (scalar.get_type()->type_id())
        {
        case r_Type::BOOL:
        case r_Type::CHAR:
        case r_Type::OCTET:
        case r_Type::SHORT:
        case r_Type::USHORT:
        case r_Type::ULONG:
        case r_Type::LONG:
        case r_Type::DOUBLE:
        case r_Type::FLOAT:
        case r_Type::COMPLEXTYPE1:
        case r_Type::COMPLEXTYPE2:
        case r_Type::CINT16:
        case r_Type::CINT32:
            writeScalarToFileStream(scalar, file);
            break;

        // structure type oracles do not expect spaces - but string representation has spaces
        case r_Type::STRUCTURETYPE:
        {
            const r_Structure *const structValue = static_cast<r_Structure *>(&const_cast<r_Scalar &>(scalar));
            writeStructToFileStream(structValue, file);
            break;
        }

        default:
            LWARNING << "scalar type '" << scalar.get_type()->type_id() <<  "' not supported!";
            break;
        }

        file << std::endl;
        file.close();
    }
    else
    {
        throw RasqlError(UNABLETOWRITETOFILE);
    }
}

/*
 * Write the scalar value to file.
 * This function is needed because default string implementations print out spaces and test oracles do not accept spaces
 */
void writeScalarToFileStream(const r_Scalar &scalar, std::ofstream &file)
{
    const r_Base_Type *scalarType = scalar.get_type();
    switch (scalarType->type_id())
    {
    case r_Type::BOOL:
        file << ((static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_boolean() ? "t" : "f");
        break;

    case r_Type::CHAR:
        file << static_cast<int>((static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_char());
        break;

    case r_Type::OCTET:
        file << static_cast<int>((static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_octet());
        break;

    case r_Type::SHORT:
        file << (static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_short();
        break;

    case r_Type::USHORT:
        file << (static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_ushort();
        break;

    case r_Type::LONG:
        file << (static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_long();
        break;

    case r_Type::ULONG:
        file << (static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_ulong();
        break;

    case r_Type::DOUBLE:
        file << std::setprecision(std::numeric_limits<double>::digits10 + 1);
        file << (static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_double();
        break;

    case r_Type::FLOAT:
        file << std::setprecision(std::numeric_limits<float>::digits10 + 1);
        file << (static_cast<r_Primitive *>(&const_cast<r_Scalar &>(scalar)))->get_float();
        break;

    case r_Type::COMPLEXTYPE1:
    case r_Type::COMPLEXTYPE2:
        // default string representation of complex type has space - rejected by system test oracles.
        // eg: complex string representation: (34, 45) - oracle expected: (34,45)
        file << "(" << (static_cast<r_Complex *>(&const_cast<r_Scalar &>(scalar)))->get_re() << "," << (static_cast<r_Complex *>(&const_cast<r_Scalar &>(scalar)))->get_im() << ")";
        break;

    case r_Type::CINT16:
    case r_Type::CINT32:
        // default string representation of complex type has space - rejected by system test oracles.
        // eg: complex string representation: (34, 45) - oracle expected: (34,45)
        file << "(" << (static_cast<r_Complex *>(&const_cast<r_Scalar &>(scalar)))->get_re_long() << "," << (static_cast<r_Complex *>(&const_cast<r_Scalar &>(scalar)))->get_im_long() << ")";
        break;

    case r_Type::STRUCTURETYPE:
    {
        const r_Structure *const structValue = static_cast<r_Structure *>(&const_cast<r_Scalar &>(scalar));
        writeStructToFileStream(structValue, file);
        break;
    }

    default:
        LWARNING << "scalar type '" << scalar.get_type()->type_id() <<  "' not supported!";
        break;
    }
}

/*
 * Write the struct to the file stream.
 * Needed because of all test oracles with no spaces - but default string implementation has spaces
 */
void writeStructToFileStream(const r_Structure *const structValue, std::ofstream &file)
{
    file << "{ ";
    for (unsigned int i = 0; i < structValue->count_elements(); i++)
    {
        writeScalarToFileStream((*structValue)[i], file);
        if (i < structValue->count_elements() - 1)
        {
            file << ", ";
        }
    }
    file << " }";
}

void printScalar(const char *buf, r_Type::r_Type_Id typeId);

void printScalar(const char *buf, r_Type::r_Type_Id typeId)
{
#define PRINT_FLT(T) cout << std::setprecision(std::numeric_limits<T>::digits10 + 1) \
                          << *reinterpret_cast<const T*>(buf)
    switch (typeId)
    {
    case r_Type::BOOL:  cout << (*reinterpret_cast<const r_Boolean*>(buf) ? "t" : "f"); break;
    case r_Type::CHAR:  cout << int(*reinterpret_cast<const r_Char*>(buf)); break;
    case r_Type::OCTET: cout << int(*reinterpret_cast<const r_Octet*>(buf)); break;
    case r_Type::SHORT: cout << *reinterpret_cast<const r_Short*>(buf); break;
    case r_Type::USHORT:cout << *reinterpret_cast<const r_UShort*>(buf); break;
    case r_Type::LONG:  cout << *reinterpret_cast<const r_Long*>(buf); break;
    case r_Type::ULONG: cout << *reinterpret_cast<const r_ULong*>(buf); break;
    case r_Type::FLOAT: cout << std::setprecision(std::numeric_limits<r_Float>::digits10 + 1)
                             << *reinterpret_cast<const r_Float*>(buf); break;
    case r_Type::DOUBLE:cout << std::setprecision(std::numeric_limits<r_Double>::digits10 + 1)
                             << *reinterpret_cast<const r_Double*>(buf); break;
    case r_Type::COMPLEXTYPE1:cout << *reinterpret_cast<const std::complex<r_Float>*>(buf); break;
    case r_Type::COMPLEXTYPE2:cout << *reinterpret_cast<const std::complex<r_Double>*>(buf); break;
    case r_Type::CINT16:      cout << *reinterpret_cast<const std::complex<r_Short>*>(buf); break;
    case r_Type::CINT32:      cout << *reinterpret_cast<const std::complex<r_Long>*>(buf); break;
    default:                  cout << "?"; break;
    }
} // printScalar()

// result_set should be parameter, but is global -- see def for reason
void printResult(/* r_Set< r_Ref_Any > result_set */)
{
    LINFO << "Query result collection has " << result_set.cardinality() << " element(s):";

    if (displayType)
    {
        LINFO << "  Oid...................: " << result_set.get_oid();
        LINFO << "  Type Structure........: "
              << (result_set.get_type_structure() ? result_set.get_type_structure() : "<nn>");
        NNLINFO << "  Type Schema...........: ";
        if (result_set.get_type_schema())
        {
            result_set.get_type_schema()->print_status(cout);
        }
        else
        {
            BLINFO << "(no name)";
        }
        BLINFO << "\n";
        LINFO << "  Number of entries.....: " << result_set.cardinality();
        NNLINFO << "  Element Type Schema...: ";
        if (result_set.get_element_type_schema())
        {
            result_set.get_element_type_schema()->print_status(cout);
        }
        else
        {
            BLINFO << "(no name)";
        }
        BLINFO << "\n";
    }

    r_Iterator<r_Ref_Any> iter = result_set.create_iterator();
    // iter.not_done() seems to behave wrongly on empty set, therefore this additional check -- PB 2003-aug-16
    for (unsigned int i = 1 ; i <= result_set.cardinality() && iter.not_done(); iter++, i++)
    {
        switch (result_set.get_element_type_schema()->type_id())
        {
        case r_Type::MARRAYTYPE:
            switch (outputType)
            {
            case OUT_NONE:
                break;
            case OUT_STRING:
            {
                size_t numCells = r_Ref<r_GMarray>(*iter)->get_array_size();
                const char *theStuff = r_Ref<r_GMarray>(*iter)->get_array();
                NNLINFO << "  Result object " << i << ": ";
                for (unsigned int cnt = 0; cnt < numCells; cnt++)
                {
                    cout << theStuff[cnt];
                }
                cout << endl;
            }
            break;
            case OUT_HEX:
            {
                size_t numCells = r_Ref<r_GMarray>(*iter)->get_array_size();
                const char *theStuff = r_Ref<r_GMarray>(*iter)->get_array();
                NNLINFO << "  Result object " << i << ": ";
                cout << hex;
                for (unsigned int cnt = 0; cnt < numCells; cnt++)
                {
                    cout << setw(2) << static_cast<unsigned short>(0xff & theStuff[cnt]) << " ";
                }
                cout << dec << endl;
            }
            break;
            case OUT_FORMATTED:
            {
                NNLINFO << "  Result object " << i << ": ";
                const auto &dom = r_Ref<r_GMarray>(*iter)->spatial_domain();
                const auto cellSize = r_Ref<r_GMarray>(*iter)->get_type_length();
                size_t numCells = dom.cell_count();
                const char *data = r_Ref<r_GMarray>(*iter)->get_array();
                const r_Base_Type *type = r_Ref<r_GMarray>(*iter)->get_base_type_schema();
                auto lastDimSize = dom[dom.dimension() - 1].get_extent();
                if (type->isStructType())
                {
                    const auto *structType = static_cast<const r_Structure_Type *>(type);
                    const auto &atts = structType->getAttributes();
                    size_t bandNo = atts.size();
                    std::vector<r_Bytes> typeSizes;
                    std::vector<r_Type::r_Type_Id> typeIds;
                    for (const auto &att: atts)
                    {
                        typeIds.push_back(att.type_of().type_id());
                        typeSizes.push_back(att.type_of().size());
                    }
                    for (size_t j = 0; j < numCells; ++j)
                    {
                        if (j > 0) {
                            cout << ",";
                            if (j % lastDimSize == 0)
                                cout << "\n" << flush;
                        }
                        cout << "{";
                        for (size_t k = 0; k < bandNo; ++k)
                        {
                            if (k > 0) cout << ",";
                            printScalar(data, typeIds[k]);
                            data += typeSizes[k];
                        }
                        cout << "}";
                    }
                }
                else
                {
                    const auto typeId = type->type_id();
                    for (size_t j = 0; j < numCells; ++j, data += cellSize)
                    {
                        if (j > 0) {
                            cout << ",";
                            if (j % lastDimSize == 0)
                                cout << "\n" << flush;
                        }
                        printScalar(data, typeId);
                    }
                }
                cout << endl;
            }
            break;
            case OUT_FILE:
            {
                char defFileName[FILENAME_MAX];
                (void) snprintf(defFileName, sizeof(defFileName) - 1, outFileMask, i);
                LDEBUG << "filename for #" << i << " is " << defFileName;

                // special treatment only for DEFs
                r_Data_Format mafmt = r_Ref<r_GMarray>(*iter)->get_current_format();
                const char *suffix = ".unknown";
                switch (mafmt)
                {
                case r_TIFF:  suffix = ".tif"; break;
                case r_JP2:   suffix = ".jp2"; break;
                case r_JPEG:  suffix = ".jpg"; break;
                case r_HDF:   suffix = ".hdf"; break;
                case r_PNG:   suffix = ".png"; break;
                case r_BMP:   suffix = ".bmp"; break;
                case r_NETCDF:suffix = ".nc"; break;
                case r_CSV:   suffix = ".csv"; break;
                case r_JSON:  suffix = ".json"; break;
                case r_DEM:   suffix = ".dem"; break;
                default:      suffix = ".unknown"; break;
                }
                strcat(defFileName, suffix);

                NNLINFO << "  Result object " << i << ": going into file " << defFileName << "... ";
                FILE *tfile = fopen(defFileName, "wb");
                if (tfile == NULL)
                {
                    throw RasqlError(NOFILEWRITEPERMISSION);
                }
                size_t count = r_Ref<r_GMarray>(*iter)->get_array_size();
                if (fwrite(static_cast<void *>(r_Ref<r_GMarray>(*iter)->get_array()), 1, count, tfile) != count)
                {
                    fclose(tfile);
                    throw RasqlError(UNABLETOWRITETOFILE);
                };
                fclose(tfile);
                BLINFO << "ok.\n";
            }
            break;
            default:
                LERROR << "Unknown output type, ignoring action: " << static_cast<int>(outputType) << endl;
                break;
            } // switch(outputType)
            break;

        case r_Type::POINTTYPE:
            NNLINFO << "  Result element " << i << ": ";
            BLINFO << *(r_Ref<r_Point>(*iter)) << "\n";
            if (outputType == OUT_FILE)
            {
                writeStringToFile(r_Ref<r_Point>(*iter)->to_string(), i);
            }
            break;
        case r_Type::SINTERVALTYPE:
            NNLINFO << "  Result element " << i << ": ";
            BLINFO << *(r_Ref<r_Sinterval>(*iter)) << "\n";
            if (outputType == OUT_FILE)
            {
                std::string strRep(r_Ref<r_Sinterval>(*iter)->get_string_representation());
                writeStringToFile(strRep, i);
            }
            break;

        case r_Type::MINTERVALTYPE:
            NNLINFO << "  Result element " << i << ": ";
            BLINFO << *(r_Ref<r_Minterval>(*iter)) << "\n";
            if (outputType == OUT_FILE)
            {
                writeStringToFile(r_Ref<r_Minterval>(*iter)->to_string(), i);
            }
            break;

        case r_Type::OIDTYPE:
            NNLINFO << "  Result element " << i << ": ";
            BLINFO << *(r_Ref<r_OId>(*iter)) << "\n";
            break;
        
        case r_Type::STRINGTYPE: {
            const auto &value = r_Ref<r_String>(*iter)->get_value();
            NNLINFO << "  Result element " << i << ": " << value << "\n";
            if (outputType == OUT_FILE)
            {
                writeStringToFile(value, i);
            }
            break;
        }
        default:
            NNLINFO << "  Result element " << i << ": ";
            r_Ref<r_Scalar> scalar(*iter);
            printScalar(*scalar);
            if (outputType == OUT_FILE)
            {
                writeScalarToFile(*scalar, i);
            }
            BLINFO << "\n";
        } // switch
    }  // for(...)
} // printResult()

/*
 * writes the given string to file
 * used for intervals, and point results, or any class with a valid string representation defined
 * uses default file naming convention with the specified file number for auto numbering
 * throws UNABLETOWRITETOFILE on file write failure
 */
void writeStringToFile(const std::string &str, unsigned int fileNum)
{
    char defFileName[FILENAME_MAX];
    (void) snprintf(defFileName, sizeof(defFileName) - 1, outFileMask, fileNum);
    strcat(defFileName, ".txt");

    std::ofstream file(defFileName, std::ios::out);
    if (file.is_open())
    {
        NNLINFO << "  Result object " << fileNum << ": going into file " << defFileName << "... ";
        file << str;
        file.close();
    }
    else
    {
        throw RasqlError(UNABLETOWRITETOFILE);
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
    r_Marray_Type *retval = NULL;
    std::unique_ptr<char[]> typeStructure;

    // first, try to get type structure from database using a separate r/o transaction
    try
    {
        typeStructure.reset(db.getComm()->getTypeStructure(mddTypeName2, ClientComm::r_MDDType_Type));
        LDEBUG << "type structure is " << typeStructure.get();
    }
    catch (r_Error &err)
    {
        if (err.get_kind() == r_Error::r_Error_DatabaseClassUndefined)
        {
            LDEBUG << "Type is not a well known type: " << typeStructure.get();
            throw RasqlError(MDDTYPEINVALID);
        }
        else    // unanticipated error
        {
            LDEBUG << "Error during type retrieval from database: " << err.get_errorno() << " " << err.what();
            throw;
        }
    }

    // next, find out whether it is an MDD type (and not a base or set type, eg)
    try
    {
        std::unique_ptr<r_Type> tempType(r_Type::get_any_type(typeStructure.get()));
        if (tempType->isMarrayType())
        {
            retval = static_cast<r_Marray_Type *>(tempType.release());
        }
        else
        {
            throw RasqlError(MDDTYPEINVALID);
        }
    }
    catch (r_Error &err)
    {
        LDEBUG << "Error during retrieval of MDD type structure ("
               << typeStructure.get() << "): " << err.get_errorno() << " " << err.what();
        throw;
    }

    return retval;
} // getTypeFromDatabase()

void doStuff(__attribute__((unused)) int argc, __attribute__((unused)) char **argv)
{
    std::unique_ptr<char[]> fileContents;                       // contents of file satisfying "$1" parameter in query
    std::unique_ptr<r_Set<r_GMarray *>> fileContentsChunked; // file contents partitioned into smaller chunks
    r_Ref<r_GMarray> fileMDD;    // MDD to satisfy a "$1" parameter
    std::unique_ptr<r_Marray_Type> mddType;      // this MDD's type

    r_OQL_Query query(queryString);
    LDEBUG << "query is: " << query.get_query();

    if (fileName != NULL)
    {
        // if no type name was specified then assume byte string (for encoded files)
        if (! mddTypeNameDef)
        {
            mddTypeName = MDD_STRINGTYPE;
        }

        if (openTransaction(false))
        {
            NNLINFO << "fetching type information for " << mddTypeName << " from database... ";
            mddType.reset(getTypeFromDatabase(mddTypeName));
            if (closeTransaction(true))
            {
                BLINFO << "ok.\n";
            }
        }
        else
        {
            return; // no point to continue
        }

        NNLINFO << "reading file " << fileName << "... ";
        errno = 0;
        FILE *fileD = fopen(fileName, "r");
        if (fileD == NULL)
        {
            BLERROR << "failed: " << strerror(errno) << "\n";
            throw RasqlError(FILEREADERROR);
        }

        fseek(fileD, 0, SEEK_END);
        long size = ftell(fileD);
        if (size == 0)
        {
            fclose(fileD);
            throw RasqlError(FILEEMPTY);
        }
        LDEBUG << "file size is " << size << " bytes";

        // if no domain specified (this is the case with encoded files), then set to byte stream
        if (! mddDomainDef)
        {
            mddDomain = r_Minterval(1) << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(size) - 1);
            LDEBUG << "domain set to " << mddDomain;

            // compute tiles
            r_Storage_Layout storage_layout(new r_Aligned_Tiling(1));
            auto tiles = storage_layout.decomposeMDD(mddDomain, 1);

            // read data in each tile chunk
            long offset = 0;
            fileContentsChunked.reset(new r_Set<r_GMarray *>());
            for (const auto &chunkDom: tiles)
            {
                r_Area chunkSize = chunkDom.cell_count();
                char *chunkData = NULL;
                try
                {
                    chunkData = new char[chunkSize];
                }
                catch (const std::bad_alloc &e)
                {
                    throw RasqlError(UNABLETOCLAIMRESOURCEFORFILE);
                }
                fseek(fileD, offset, SEEK_SET);
                size_t rsize = fread(chunkData, 1, chunkSize, fileD);
                if (rsize != chunkSize)
                {
                    BLERROR << "failed, read only " << rsize << " bytes of " << chunkSize << " bytes at offset " << offset << ".\n";
                    throw RasqlError(FILEREADERROR);
                }
                r_GMarray *chunkMDD = new r_GMarray(chunkDom, 1, NULL, &ta, false);
                chunkMDD->set_array(chunkData);
                fileContentsChunked->insert_element(chunkMDD);
                offset += static_cast<long>(chunkSize);
            }
        }
        else if (size != static_cast<long>(mddDomain.cell_count()) *
                 static_cast<long>(mddType->base_type().size()))
        {
            throw RasqlError(FILESIZEMISMATCH);
        }
        else
        {
            try
            {
                fileContents.reset(new char[size]);
            }
            catch (const std::bad_alloc &e)
            {
                throw RasqlError(UNABLETOCLAIMRESOURCEFORFILE);
            }
            fseek(fileD, 0, SEEK_SET);
            size_t rsize = fread(fileContents.get(), 1, static_cast<size_t>(size), fileD);
            if (static_cast<long>(rsize) != size)
            {
                BLERROR << "failed, read only " << rsize << " bytes of " << size << " bytes.\n";
                throw RasqlError(FILEREADERROR);
            }
        }

        fclose(fileD);

        BLINFO << "ok.\n";

        LDEBUG << "setting up MDD with domain " << mddDomain << " and base type " << mddTypeName;
        fileMDD = new (mddTypeName) r_GMarray(mddDomain, mddType->base_type().size(), 0, &ta, false);
        fileMDD->set_array_size(mddDomain.cell_count() * mddType->base_type().size());
        fileMDD->set_type_schema(mddType.get());
//        mddType.release();
        if (fileContents)
        {
            fileMDD->set_array(fileContents.get());
        }
        else
        {
            fileMDD->set_tiled_array(fileContentsChunked.get());
            fileContentsChunked.release();
        }

        query << *fileMDD;

        if (!quietLog)
        {
            LINFO << "constants are:";
        }
        r_Set<r_GMarray *> *myConstSet = const_cast<r_Set<r_GMarray *> *>(query.get_constants());
        r_Iterator<r_GMarray *> iter = myConstSet->create_iterator();
        int i;
        for (i = 1, iter.reset(); iter.not_done(); iter++, i++)
        {
            auto myConstant = r_Ref<r_GMarray>(*iter, &ta);
            if (!quietLog)
            {
                NNLINFO << "  constant " << i << ": ";
                myConstant->print_status(cout);
// the following can be used for sporadic debugging of input files, but beware: is very verbose!
#if 0
                cout << "  Contents: " << hex;
                const char *a = myConstant->get_array();
                for (int m = 0; m < myConstant->get_array_size(); m++)
                {
                    cout << (unsigned short)(a[m] & 0xFF) << " ";
                }
                cout << dec << endl;
#endif
            }
        }
    }

    auto isInsert = query.is_insert_query();
    if (isInsert || query.is_update_query()) // insert/update
    {
        if (openTransaction(true))
        {
            NNLINFO << "Executing " << (isInsert ? "insert" : "update") << " query... ";
            // third param is just to differentiate from retrieval
            if (isInsert)
            {
                r_oql_execute(query, result_set, 1, &ta);
            }
            else
            {
                r_oql_execute(query, &ta);
            }

            BLINFO << "ok.\n";
            if (output)
            {
                printResult(/* result_set */);
            }
            closeTransaction(true);
        }
    }
    else // retrieval query
    {
        if (openTransaction(false))
        {
            NNLINFO << "Executing retrieval query... ";
            r_oql_execute(query, result_set, &ta);
            BLINFO << "ok.\n";
            if (output)
            {
                printResult(/* result_set */);
            }
            closeTransaction(true);
        }
    }
}

void
shutdownHandler(int sig, siginfo_t *, void *)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        NNLINFO << "\nrasql: Interrupted by signal " << common::SignalHandler::signalName(sig)
                << "\nClosing server connection... ";
        // cleanConnection();
        BLINFO << "done, exiting.";
        exit(sig);
    }
}

void
crashHandler(int sig, siginfo_t *info, void *)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        NNLERROR << "\nInterrupted by signal " << common::SignalHandler::toString(info)
                 << "... stacktrace:\n" << common::SignalHandler::getStackTrace()
                 << "\nClosing server connection... ";
        // cleanConnection();
        BLERROR << "done, exiting.";
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

INITIALIZE_EASYLOGGINGPP

/*
 * returns 0 on success, -1 on error
 */
int main(int argc, char **argv)
{
    // handle abort signals and ignore irrelevant signals
    common::SignalHandler::handleAbortSignals(crashHandler);
    common::SignalHandler::ignoreStandardSignals();
    // setup log config
    common::LogConfiguration logConf(string(CONFDIR), CLIENT_LOG_CONF);
    logConf.configClientLogging();
    // should come after the log config as it logs msgs
    common::SignalHandler::handleShutdownSignals(shutdownHandler);

    int retval = EXIT_SUCCESS;  // overall result status

    // unset the http_proxy env variable if it is set, otherwise rasql will most likely fail
    // more info at http://rasdaman.org/ticket/1716
    unsetenv("http_proxy");
    try
    {
        parseParams(argc, argv);

        if (quietLog)
        {
            logConf.configClientLogging(true);
        }

        // put INFO after parsing parameters to respect a '--quiet'
        LINFO << argv[0] << ": rasdaman query tool " << RMANVERSION << ".";

        if (openDatabase())
        {
            doStuff(argc, argv);
            closeDatabase();
        }
        else
        {
            retval = EXIT_FAILURE;
        }
    }
    catch (RasqlError &e)
    {
        LERROR << argv[0] << ": " << e.what();
        retval = EXIT_FAILURE;
    }
    catch (const r_Error &e)
    {
        LERROR << "rasdaman error " << e.get_errorno() << ": " << e.what();
        retval = EXIT_FAILURE;
    }
    catch (std::exception &e)
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
        cleanConnection();
    }

    LINFO << argv[0] << " done.";
    return retval;
} // main()

// end of rasql.cc

