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

#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#include "raslib/template_inst.hh"
#endif

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sstream>
#include <fstream>
#include <vector>
#include <stdexcept>
#include <limits>
#include <iomanip>

#include "raslib/commonutil.hh"

using namespace std;

#ifdef __VISUALC__
#define __EXECUTABLE__
#endif

#include "rasodmg/transaction.hh"
#include "rasodmg/database.hh"

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

#include "raslib/rmdebug.hh"
#include "raslib/commonutil.hh"
#include "raslib/structuretype.hh"
#include "raslib/primitivetype.hh"

#include "../../commline/cmlparser.hh"

#include "loggingutils.hh"

#include "rasql_error.hh"

#include "globals.hh"

#ifdef __VISUALC__
#undef __EXECUTABLE__
#endif

// debug facility; relies on -DDEBUG at compile time
// tell debug that here is the place for the variables (to be done in the main() src file)
#define DEBUG_MAIN
#include "debug-clt.hh"

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

#include <logging.hh>

// global variables and default settings
// -------------------------------------

r_Database db;
r_Transaction ta;

bool dbIsOpen = false;
bool taIsOpen = false;

// suppress regular messages in log? (cmd line parameter '--quiet')
bool quietLog = false;

int  optionValueIndex = 0;

const char* serverName = DEFAULT_SERV;
r_ULong serverPort = DEFAULT_PORT;
const char* baseName = DEFAULT_DB;

const char* user = DEFAULT_USER;
const char* passwd = DEFAULT_PASSWD;

const char* fileName = NULL;
const char* queryString = NULL;

bool output = false;
bool displayType = false;

OUTPUT_TYPE outputType = DEFAULT_OUT;

const char* outFileMask = DEFAULT_OUTFILE;

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
printScalar(const r_Scalar& scalar);

void
printResult();

r_Marray_Type*
getTypeFromDatabase(const char* mddTypeName2);

void
doStuff(int argc, char** argv);

void
crashHandler(int sig, siginfo_t* info, void* ucontext);

void
cleanupHandler(int sig, siginfo_t* info, void* ucontext);

void
doNothingHandler(int sig, siginfo_t* info, void* ucontext);

void
instalRasqlSignalHandlers();

void
parseParams(int argc, char** argv)
{
    CommandLineParser&    cmlInter      = CommandLineParser::getInstance();

    CommandLineParameter& clp_help      = cmlInter.addFlagParameter(PARAM_HELP_FLAG, PARAM_HELP, HELP_HELP);

    CommandLineParameter& clp_query         = cmlInter.addStringParameter(PARAM_QUERY_FLAG, PARAM_QUERY, HELP_QUERY);
    CommandLineParameter& clp_file      = cmlInter.addStringParameter(PARAM_FILE_FLAG, PARAM_FILE, HELP_FILE);

    CommandLineParameter& clp_content   = cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_CONTENT, HELP_CONTENT);
    CommandLineParameter& clp_out       = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_OUT, HELP_OUT, DEFAULT_OUT_STR);
    CommandLineParameter& clp_outfile   = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_OUTFILE, HELP_OUTFILE, DEFAULT_OUTFILE);
    CommandLineParameter& clp_mddDomain     = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_DOMAIN, HELP_DOMAIN);
    CommandLineParameter& clp_mddType       = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_MDDTYPE, HELP_MDDTYPE, DEFAULT_MDDTYPE);
    CommandLineParameter& clp_type      = cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_TYPE, HELP_TYPE);

    CommandLineParameter& clp_server    = cmlInter.addStringParameter(PARAM_SERV_FLAG, PARAM_SERV, HELP_SERV, DEFAULT_SERV);
    CommandLineParameter& clp_port      = cmlInter.addStringParameter(PARAM_PORT_FLAG, PARAM_PORT, HELP_PORT, DEFAULT_PORT_STR);
    CommandLineParameter& clp_database      = cmlInter.addStringParameter(PARAM_DB_FLAG, PARAM_DB, HELP_DB, DEFAULT_DB);
    CommandLineParameter& clp_user      = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_USER, HELP_USER, DEFAULT_USER);
    CommandLineParameter& clp_passwd    = cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_PASSWD, HELP_PASSWD, DEFAULT_PASSWD);
    CommandLineParameter& clp_quiet     = cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_QUIET, HELP_QUIET);

#ifdef DEBUG
    CommandLineParameter& clp_debug     = cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_DEBUG, HELP_DEBUG);
#endif

    try
    {
        cmlInter.processCommandLine(argc, argv);

        if (cmlInter.isPresent(PARAM_HELP_FLAG) || argc == 1)
        {
            cout << "usage: " << argv[0] << " [--query querystring|-q querystring] [options]" << endl;
            cout << "options:" << endl;
            cmlInter.printHelp();
            exit(EXIT_USAGE);        //  FIXME: exit no good style!!
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
            baseName = cmlInter.getValueAsString(PARAM_DB);
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
            catch (r_Error& e)              // Minterval constructor had syntax problems
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

#ifdef DEBUG
        // evaluate optional parameter MDD type name --------------------------------------
        SET_OUTPUT(cmlInter.isPresent(PARAM_DEBUG));
#endif

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
    if (! dbIsOpen)
    {
        NNLINFO << "Opening database " << baseName << " at " << serverName << ":" << serverPort << "... ";
        db.set_servername(serverName, static_cast<int>(serverPort));
        db.set_useridentification(user, passwd);
        LDEBUG << "database was closed, opening database=" << baseName << ", server=" << serverName << ", port=" << serverPort << ", user=" << user << ", passwd=" << passwd << "...";
        db.open(baseName);
        LDEBUG << "ok";
        dbIsOpen = true;
        BLINFO << "ok.\n";
    }
} // openDatabase()

void
closeDatabase()
{
    if (dbIsOpen)
    {
        LDEBUG << "database was open, closing it";
        db.close();
        dbIsOpen = false;
    }
    return;
} // closeDatabase()

void
openTransaction(bool readwrite)
{
    if (! taIsOpen)
    {
        if (readwrite)
        {
            LDEBUG << "transaction was closed, opening rw...";
            ta.begin(r_Transaction::read_write);
            LDEBUG << "ok";
        }
        else
        {
            LDEBUG << "transaction was closed, opening ro...";
            ta.begin(r_Transaction::read_only);
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
            ta.commit();
            LDEBUG << "ok";
        }
        else
        {
            LDEBUG << "transaction was open, aborting it...";
            ta.abort();
            LDEBUG << "ok";
        }
        taIsOpen = false;
    }
    return;
} // closeTransaction()

void printScalar(const r_Scalar& scalar)
{
    switch (scalar.get_type()->type_id())
    {
    case r_Type::BOOL:
        NNLINFO << ((static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_boolean() ? "t" : "f");
        break;

    case r_Type::CHAR:
        NNLINFO << static_cast<int>((static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_char());
        break;

    case r_Type::OCTET:
        NNLINFO << static_cast<int>((static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_octet());
        break;

    case r_Type::SHORT:
        NNLINFO << (static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_short();
        break;

    case r_Type::USHORT:
        NNLINFO << (static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_ushort();
        break;

    case r_Type::LONG:
        NNLINFO << (static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_long();
        break;

    case r_Type::ULONG:
        NNLINFO << (static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_ulong();
        break;

    case r_Type::FLOAT:
        NNLINFO << std::setprecision(std::numeric_limits<float>::digits10 + 1) << (static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_float();
        break;

    case r_Type::DOUBLE:
        NNLINFO << std::setprecision(std::numeric_limits<double>::digits10 + 1) << (static_cast<r_Primitive*>(&const_cast<r_Scalar&>(scalar)))->get_double();
        break;

    case r_Type::COMPLEXTYPE1:
    case r_Type::COMPLEXTYPE2:
        NNLINFO << "(" << (static_cast<r_Complex*>(&const_cast<r_Scalar&>(scalar)))->get_re() << "," << (static_cast<r_Complex*>(&const_cast<r_Scalar&>(scalar)))->get_im() << ")";
        break;

    case r_Type::STRUCTURETYPE:
    {
        r_Structure* structValue = static_cast<r_Structure*>(&const_cast<r_Scalar&>(scalar));
        NNLINFO << "{ ";
        for (unsigned int i = 0; i < structValue->count_elements(); i++)
        {
            printScalar((*structValue)[i]);
            if (i < structValue->count_elements() - 1)
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

    /* The following can be used if the type is known and the element type is not atomic.

        r_Set< r_Ref< r_Point > >* set2 = (r_Set< r_Ref< r_Point > >*)&result_set;
        r_Iterator< r_Ref<r_Point> > iter2 = set2->create_iterator();
        for( iter2.reset(); iter2.not_done(); iter2++ )
            cout << **iter2 << endl;
    */

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
                const char* theStuff = r_Ref<r_GMarray>(*iter)->get_array();
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
                const char* theStuff = r_Ref<r_GMarray>(*iter)->get_array();
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
                size_t numCells = r_Ref<r_GMarray>(*iter)->get_array_size();
                const char* theStuff = r_Ref<r_GMarray>(*iter)->get_array();
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

                NNLINFO << "  Result object " << i << ": going into file " << defFileName << "... ";
                FILE* tfile = fopen(defFileName, "wb");
                if (tfile == NULL)
                {
                    throw RasqlError(NOFILEWRITEPERMISSION);
                }
                size_t count = r_Ref<r_GMarray>(*iter)->get_array_size();
                if (fwrite(static_cast<void*>(r_Ref<r_GMarray>(*iter)->get_array()), 1, count, tfile) != count)
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
            break;
        case r_Type::SINTERVALTYPE:
            NNLINFO << "  Result element " << i << ": ";
            BLINFO << *(r_Ref<r_Sinterval>(*iter)) << "\n";
            break;

        case r_Type::MINTERVALTYPE:
            NNLINFO << "  Result element " << i << ": ";
            BLINFO << *(r_Ref<r_Minterval>(*iter)) << "\n";
            break;

        case r_Type::OIDTYPE:
            NNLINFO << "  Result element " << i << ": ";
            BLINFO << *(r_Ref<r_OId>(*iter)) << "\n";
            break;

        default:
            NNLINFO << "  Result element " << i << ": ";
            printScalar(*(r_Ref<r_Scalar>(*iter)));
            BLINFO << "\n";
        } // switch
    }  // for(...)
} // printResult()


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
        typeStructure = db.getComm()->getTypeStructure(mddTypeName2, ClientComm::r_MDDType_Type);
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
            // LDEBUG << "using instead: " << typeStructure;
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
        r_Type* tempType = r_Type::get_any_type(typeStructure);
        LDEBUG << "get_any_type() for this type returns: " << tempType;
        if (tempType->isMarrayType())
        {
            retval = static_cast<r_Marray_Type*>(tempType);
            tempType = NULL;
            LDEBUG << "found MDD type: " << retval;
        }
        else
        {
            LDEBUG << "type is not an marray type: " << typeStructure;
            delete tempType;
            tempType = NULL;
            retval = NULL;
            throw RasqlError(MDDTYPEINVALID);
        }
    }
    catch (r_Error& err)
    {
        LDEBUG << "Error during retrieval of MDD type structure (" 
                << typeStructure << "): " << err.get_errorno() << " " << err.what();
        throw;
    }

    delete [] typeStructure;
    typeStructure = NULL;

    return retval;
} // getTypeFromDatabase()

void doStuff(__attribute__((unused)) int argc, __attribute__((unused)) char** argv)
{
    char* fileContents = NULL;                       // contents of file satisfying "$1" parameter in query
    r_Set<r_GMarray*>* fileContentsChunked = NULL; // file contents partitioned into smaller chunks
    r_Ref<r_GMarray> fileMDD = NULL;    // MDD to satisfy a "$1" parameter
    r_Marray_Type* mddType = NULL;      // this MDD's type

    r_OQL_Query query(queryString);
    LDEBUG << "query is: " << query.get_query();

    if (fileName != NULL)
    {
        openTransaction(false);

        // if no type name was specified then assume byte string (for encoded files)
        if (! mddTypeNameDef)
        {
            mddTypeName = MDD_STRINGTYPE;
        }

        NNLINFO << "fetching type information for " << mddTypeName 
                << " from database, using readonly transaction... ";
        mddType = getTypeFromDatabase(mddTypeName);
        closeTransaction(true);
        BLINFO << "ok.\n";

        NNLINFO << "reading file " << fileName << "... ";
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
        if (! mddDomainDef)
        {
            mddDomain = r_Minterval(1) << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(size) - 1);
            LDEBUG << "domain set to " << mddDomain;

            // compute tiles
            r_Storage_Layout* storage_layout = new r_Storage_Layout(new r_Aligned_Tiling(1));
            std::vector<r_Minterval>* tiles = storage_layout->decomposeMDD(mddDomain, 1);

            // read data in each tile chunk
            long offset = 0;
            fileContentsChunked = new r_Set<r_GMarray*>();
            std::vector<r_Minterval>::iterator chunkIt;
            for (chunkIt = tiles->begin(); chunkIt != tiles->end(); chunkIt++)
            {
                r_Minterval chunkDom = *chunkIt;
                r_Area chunkSize = chunkDom.cell_count();
                char* chunkData = new char[chunkSize];
                fseek(fileD, offset, SEEK_SET);
                size_t rsize = fread(chunkData, 1, chunkSize, fileD);
                r_GMarray* chunkMDD = new r_GMarray(chunkDom, 1, NULL, false);
                chunkMDD->set_array(chunkData);
                fileContentsChunked->insert_element(chunkMDD);
                offset += static_cast<long>(chunkSize);
            }

            // cleanup
            if (tiles)
            {
                delete tiles;
                tiles = NULL;
            }
            if (storage_layout)
            {
                delete storage_layout;
                storage_layout = NULL;
            }
        }
        else if (size != static_cast<long>(mddDomain.cell_count()) * static_cast<long>(mddType->base_type().size()))
        {
            throw RasqlError(FILESIZEMISMATCH);
        }
        else
        {
            try
            {
                fileContents = new char[size];
                fseek(fileD, 0, SEEK_SET);
                size_t rsize = fread(fileContents, 1, static_cast<size_t>(size), fileD);
            }
            catch (std::bad_alloc)
            {
                LERROR << "Failed to allocate memory of " << size << " bytes.";
                throw RasqlError(UNABLETOCLAIMRESOURCEFORFILE);
            }
        }

        fclose(fileD);

        BLINFO << "ok.\n";

        LDEBUG << "setting up MDD with domain " << mddDomain << " and base type " << mddTypeName;
        fileMDD = new(mddTypeName) r_GMarray(mddDomain, mddType->base_type().size(), 0, false);
        fileMDD->set_type_schema(mddType);
        fileMDD->set_array_size(mddDomain.cell_count() * mddType->base_type().size());
        if (fileContents != NULL)
        {
            fileMDD->set_array(fileContents);
        }
        else
        {
            fileMDD->set_tiled_array(fileContentsChunked);
        }

        query << *fileMDD;

        LINFO << "constants are:";
        r_Set<r_GMarray*>* myConstSet = const_cast<r_Set<r_GMarray*> *>(query.get_constants());
        r_Iterator<r_GMarray*> iter = myConstSet->create_iterator();
        int i;
        for (i = 1, iter.reset(); iter.not_done(); iter++, i++)
        {
            r_Ref<r_GMarray> myConstant = *iter;
            NNLINFO << "  constant " << i << ": ";
            if (!quietLog)
                myConstant->print_status(cout);
// the following can be used for sporadic debugging of input files, but beware: is very verbose!
#if 0
            cout << "  Contents: " << hex;
            const char* a = myConstant->get_array();
            for (int m = 0; m < myConstant->get_array_size(); m++)
            {
                cout << (unsigned short)(a[m] & 0xFF) << " ";
            }
            cout << dec << endl;
#endif
        }
    }

    if (query.is_insert_query())
    {
        openTransaction(true);

        r_Marray<r_ULong>* mddConst = NULL;

        NNLINFO << "Executing insert query... ";
        // third param is just to differentiate from retrieval
        r_oql_execute(query, result_set, 1);
        BLINFO << "ok.\n";

        // generate output only if explicitly requested
        if (output)
        {
            printResult(/* result_set */);
        }

        if (mddConst)
        {
            delete mddConst;
        }

        closeTransaction(true);
    }
    else if (query.is_update_query())
    {
        openTransaction(true);

        r_Marray<r_ULong>* mddConst = NULL;

        NNLINFO << "Executing update query... ";
        r_oql_execute(query);
        BLINFO << "ok.\n";

        if (mddConst)
        {
            delete mddConst;
        }

        closeTransaction(true);
    }
    else // retrieval query
    {
        openTransaction(false);

        // should be defined here, but is global; see def for reason
        // r_Set< r_Ref_Any > result_set;

        NNLINFO << "Executing retrieval query... ";
        r_oql_execute(query, result_set);
        BLINFO << "ok.\n";

        // generate output only if explicitly requested
        if (output)
        {
            printResult(/* result_set */);
        }

        closeTransaction(true);
    }

    if (fileContents != NULL)
    {
        delete [] fileContents;
    }
}

void
crashHandler(__attribute__((unused)) int sig, __attribute__((unused)) siginfo_t* info, void* ucontext)
{
    print_stacktrace(ucontext);
    // clean up connection in case of segfault
    closeTransaction(false);
    closeDatabase();
    exit(SEGFAULT_EXIT_CODE);
}

void
cleanupHandler(__attribute__((unused)) int sig, __attribute__((unused)) siginfo_t* info, void* ucontext)
{
    static bool handleSignal = true;    // prevent nested signals
    cerr << "Caught signal " << sig << ": ";
    if (handleSignal)
    {
        handleSignal = false;
        cerr << "terminating connection to server... ";
        closeTransaction(false);
        closeDatabase();
        cerr << "done, exiting." << endl;
        exit(sig);
    }
    else
    {
        cerr << "will be ignored." << endl;
    }
}

void
doNothingHandler(__attribute__((unused)) int sig, __attribute__((unused)) siginfo_t* info, void* ucontext)
{
}

void
instalRasqlSignalHandlers()
{
    installSigHandler(cleanupHandler, SIGINT);
    installSigHandler(cleanupHandler, SIGTERM);
    installSigHandler(cleanupHandler, SIGQUIT);

    installSigHandler(crashHandler, SIGSEGV);
    installSigHandler(crashHandler, SIGABRT);

    installSigHandler(doNothingHandler, SIGHUP);
    installSigHandler(doNothingHandler, SIGPIPE);
    installSigHandler(doNothingHandler, SIGCONT);
    installSigHandler(doNothingHandler, SIGTSTP);
    installSigHandler(doNothingHandler, SIGTTIN);
    installSigHandler(doNothingHandler, SIGTTOU);
    installSigHandler(doNothingHandler, SIGWINCH);
}

INITIALIZE_EASYLOGGINGPP

/*
 * returns 0 on success, -1 on error
 */
int main(int argc, char** argv)
{
    common::LogConfiguration logConf(string(CONFDIR), CLIENT_LOG_CONF);
    logConf.configClientLogging();

    SET_OUTPUT(false);          // inhibit unconditional debug output, await cmd line evaluation

    int retval = EXIT_SUCCESS;  // overall result status
    instalRasqlSignalHandlers();

    // unset the http_proxy env variable if it is set, otherwise rasql will most likely fail
    // more info at http://rasdaman.org/ticket/1716
    unsetenv("http_proxy");
    try
    {
        parseParams(argc, argv);
        
        if (quietLog)
            logConf.configClientLogging(true);

        // put INFO after parsing parameters to respect a '--quiet'
        LINFO << argv[0] << ": rasdaman query tool v1.0, rasdaman " << RMANVERSION << ".";

        openDatabase();
        doStuff(argc, argv);
        closeDatabase();
        retval = EXIT_SUCCESS;
    }
    catch (std::runtime_error& ex)
    {
        LERROR << ex.what();
        retval = EXIT_FAILURE;
    }
    catch (RasqlError& e)
    {
        LERROR << argv[0] << ": " << e.what();
        retval = EXIT_FAILURE;
    }
    catch (const r_Error& e)
    {
        LERROR << "rasdaman error " << e.get_errorno() << ": " << e.what();
        retval = EXIT_FAILURE;
    }
    catch (...)
    {
        LERROR << argv[0] << ": panic: unexpected internal exception.";
        retval = EXIT_FAILURE;
    }

    if (retval != EXIT_SUCCESS && (dbIsOpen || taIsOpen))
    {
        try
        {
            NNLINFO << "aborting transaction... ";
            closeTransaction(false);    // abort transaction and close database, ignore any further exceptions
            BLINFO << "ok.\n";
            closeDatabase();
        }
        catch (...)
        {
            LDEBUG << "Ignoring cleanup exceptions.";
        }
    }

    LINFO << argv[0] << " done.";
    return retval;
} // main()

// end of rasql.cc

