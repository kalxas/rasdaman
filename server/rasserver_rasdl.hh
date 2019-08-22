#ifndef RASSERVER_RASDL_HH
#define RASSERVER_RASDL_HH

#include "reladminif/adminif.hh"
#include "reladminif/databaseif.hh"
#include "reladminif/transactionif.hh"

// error texts
#define      ERROR_NOACTION "EDL000 Error: no action specified."
#define ERROR_UNKNOWNACTION "EDL001 Error: unknown action type: "
#define         ERROR_PANIC "EDL002 panic: unexpected internal exception."
#define      ERROR_RASDAMAN "EDL003 rasdaman error: "

extern std::string baseName;

// pointers representing O2, database, ta and session
extern AdminIf*       admin;
extern DatabaseIf*    db;
extern TransactionIf* ta;

extern const char* dbSchema;
extern const char* dbVolume;

namespace rasserver
{
namespace rasdl
{

int runRasdl(int argc, char* argv[]);
void disconnectDB(bool commitTa);
void connectDB(const char* baseName2, bool openDb, bool openTa, bool createDb = false);

} // rasdl
} // rasserver

#endif
