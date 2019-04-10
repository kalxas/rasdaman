#include "rasserver_rasdl.hh"

#include "loggingutils.hh"
#include "globals.hh"
#include "rasserver_error.hh"
#include "rasserver_config.hh"



using namespace std;

AdminIf*       admin = NULL;
DatabaseIf*    db = NULL;
TransactionIf* ta = NULL;

const char* dbSchema = "";
const char* dbVolume = "";


int runRasdl(int argc, char* argv[])
{

    int result = EXIT_FAILURE;  // program exit code


    try
    {
        ProgModes progMode = configuration.getProgMode();
        switch (progMode)
        {
        case M_CREATEDATABASE:
            LDEBUG << "command is: M_CREATEDATABASE";
            cout << "Creating base " << baseName;
#ifdef BASEDB_O2
            cout << " with schema " << dbSchema;
#endif
            if (strcasecmp(dbVolume, ""))
            {
                cout << " on volume " << dbVolume;
            }
            cout << "..." << flush;
            connectDB(baseName.c_str(), false, false, true);
            db = new DatabaseIf();
            if (!strlen(dbVolume))
            {
                db->createDB(baseName.c_str(), dbSchema, dbVolume);
            }
            else
            {
                db->createDB(baseName.c_str(), dbSchema);
            }
            disconnectDB(true);
            cout << "ok" << endl;
            break;
        case M_DELDATABASE:
            LDEBUG << "command is: M_DELDATABASE";
            cout << "Deleting database " << baseName << "...";
            LDEBUG << "connecting";
            connectDB(baseName.c_str(), false, false);
            LDEBUG << "creating new DatabaseIf";
            db = new DatabaseIf();
            LDEBUG << "destroying db";
            db->destroyDB(baseName.c_str());
            disconnectDB(true);
            cout << "ok" << endl;
            break;
        case M_INVALID:
            LDEBUG << "command is: M_INVALID";
            cerr << ERROR_NOACTION << endl;
            break;
        default:
            cerr << ERROR_UNKNOWNACTION << static_cast<int>(progMode) << endl;
        }
        result = EXIT_SUCCESS;
    }
    catch (RasqlError& e)
    {
        cout << argv[0] << ": " << e.what() << endl;
        result = EXIT_FAILURE;
    }
    catch (const r_Error& e)
    {
        cout << ERROR_RASDAMAN << e.get_errorno() << ": " << e.what() << endl;
        result = EXIT_FAILURE;
    }
    catch (...)
    {
        cout << argv[0] << " " << ERROR_PANIC << endl;
        result = EXIT_FAILURE;
    }

    if (result != EXIT_SUCCESS)
    {
        disconnectDB(false);
    }

    cout << "rasdl: done." << endl;

    return (result); 
}

void
connectDB(const char* baseName2, bool openDb, bool openTa, bool createDb)
{
    admin = AdminIf::instance(createDb);
    if (!admin)
    {
        LDEBUG << "cannot create adminIf instance";
        throw RasqlError(NOCONNECTION);
    }

    if (openDb)
    {
        // connect to the database
        db = new DatabaseIf();
        // LDEBUG << "adding dbf to adminif";
        // admin->setCurrentDatabaseIf( db );
        LDEBUG << "opening db";
        db->open(baseName2);
    }

    if (openTa)
    {
        // start transaction
        ta = new TransactionIf();
        LDEBUG << "opening ta";
        ta->begin(db);
    }
}


void
disconnectDB(bool commitTa)
{
    if (ta)
    {
        if (commitTa)
        {
            ta->commit();
            LDEBUG << "TA committed.";
        }
        else
        {
            ta->abort();
            LDEBUG << "TA aborted.";
        }

        delete ta;
        ta = NULL;

        if (db)
        {
            db->close();
            LDEBUG << "DB closed.";
            delete db;
            db = NULL;
        }

        if (admin)
        {
            delete admin;
            admin = NULL;
        }
    }
}