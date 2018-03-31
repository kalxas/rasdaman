#pragma once

#define ELPP_FEATURE_CRASH_LOG
#define ELPP_DISABLE_DEFAULT_CRASH_HANDLING

#include "third_party/easyloggingpp/easylogging++.h"

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
#define LFATAL LOG(FATAL)
#endif
