#ifndef _LOGGING_HH
#define _LOGGING_HH

#include "easylogging++.h"
// Default logging macros, automatically add a new line at the end
#ifndef LINFO
#define LINFO LOG(INFO)
#endif
#ifndef LDEBUG
#define LDEBUG LOG(DEBUG)
#endif
#ifndef LTRACE
#define LTRACE LOG(TRACE)
#endif
#ifndef LWARNING
#define LWARNING LOG(WARNING)
#endif
#ifndef LERROR
#define LERROR LOG(ERROR)
#endif
#ifndef LFATAL
#define LFATAL LOG(ERROR)
#endif
#ifndef LFLUSH
#define LFLUSH el::Loggers::getLogger("default")->flush();
#endif

// Same as above but does not automatically print a new line
#ifndef NNLINFO
#define NNLINFO CLOG(INFO, "nnl")
#endif
#ifndef NNLDEBUG
#define NNLDEBUG CLOG(DEBUG, "nnl")
#endif
#ifndef NNLTRACE
#define NNLTRACE CLOG(TRACE, "nnl")
#endif
#ifndef NNLWARNING
#define NNLWARNING CLOG(WARNING, "nnl")
#endif
#ifndef NNLERROR
#define NNLERROR CLOG(ERROR, "nnl")
#endif
#ifndef NNLFATAL
#define NNLFATAL CLOG(ERROR, "nnl")
#endif
#ifndef NNLFLUSH
#define NNLFLUSH el::Loggers::getLogger("nnl")->flush();
#endif

// "Bare" logs: only print the log message and nothing else (no new line either)
#ifndef BLFLUSH
#define BLFLUSH el::Loggers::getLogger("bare")->flush();
#endif
#ifndef BLINFO
#define BLINFO CLOG(INFO, "bare")
#endif
#ifndef BLDEBUG
#define BLDEBUG CLOG(DEBUG, "bare")
#endif
#ifndef BLTRACE
#define BLTRACE CLOG(TRACE, "bare")
#endif
#ifndef BLWARNING
#define BLWARNING CLOG(WARNING, "bare")
#endif
#ifndef BLERROR
#define BLERROR CLOG(ERROR, "bare")
#endif
#ifndef BLFATAL
#define BLFATAL CLOG(ERROR, "bare")
#endif

// Enabled only if RASDEBUG is defined
#ifdef RASDEBUG
#define LRDEBUG(msg) LDEBUG << msg;
#define LRTRACE(msg) LTRACE << msg;
#else
#define LRDEBUG(msg)
#define LRTRACE(msg)
#endif

#endif
