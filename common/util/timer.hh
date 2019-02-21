//
// Created by Dimitar Misev
// Copyright (c) 2017 rasdaman GmbH. All rights reserved.
//

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
