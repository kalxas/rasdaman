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
#pragma once

#include <chrono>

namespace common {

/**
 * Creating the object automatically starts the timer. Calling restart() resets
 * the timer. You can get the elapsed time with the elapsed* methods.
 */
class Stopwatch {

 public:
  Stopwatch() : startTime{getCurrentTimestamp()} {}

  double elapsedS() const {
    return double(elapsedNs()) * 1e-9;
  }

  double elapsedMs() const {
    return double(elapsedNs()) * 1e-6;
  }

  long elapsedNs() const {
    return getCurrentTimestamp() - startTime;
  }

  void restart() { startTime = getCurrentTimestamp(); }

 private:
  static long getCurrentTimestamp() {
    return std::chrono::high_resolution_clock::now().time_since_epoch().count();
  }

  long startTime;
};

}

// Enabled only if REPORT_PERF is defined
#ifdef REPORT_PERF
#include <logging.hh>
#define START_TIMER common::Stopwatch perfTimer;
#define LINFO_ELAPSED(msg) LINFO << msg << ": " << perfTimer.elapsedMs() << " ms.";
#define LDEBUG_ELAPSED(msg) LDEBUG << msg << ": " << perfTimer.elapsedMs() << " ms.";
#else
#define START_TIMER
#define LINFO_ELAPSED(msg)
#define LDEBUG_ELAPSED(msg)
#endif
