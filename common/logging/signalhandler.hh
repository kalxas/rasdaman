//
// Created by Dimitar Misev
// Copyright (c) 2018 rasdaman GmbH. All rights reserved.
//

#ifndef _COMMON_SIGNALHANDLER_HH_
#define _COMMON_SIGNALHANDLER_HH_

#include <signal.h>
#include <string>
#include <initializer_list>

namespace common {

using SignalHandlerFunction = void (*)(int, siginfo_t*, void*);

/**
 * Functionality to help handling signals.
 */
class SignalHandler {
public:
    /**
     * Specified signals will be ignored.
     */
    static void ignoreSignals(const std::initializer_list<int>& signals);

    /**
     * "Standard" signals will be ignored:
     * SIGHUP, SIGPIPE, SIGCONT, SIGTSTP, SIGTTIN, SIGTTOU, SIGWINCH
     */
    static void ignoreStandardSignals();

    /**
     * Specified signals will be handled by the specified handler.
     */
    static void handleSignals(const std::initializer_list<int>& signals, SignalHandlerFunction handler);

    /**
     * "Shutdown" signals will be handled by the specified handler:
     * SIGINT, SIGTERM, SIGQUIT
     */
    static void handleShutdownSignals(SignalHandlerFunction handler);

    /**
     * "Abort" signals will be handled by the specified handler:
     * SIGSEGV, SIGABRT, SIGFPE, SIGBUS
     */
    static void handleAbortSignals(SignalHandlerFunction handler);

    /**
     * Install the specified handler for a specific signal.
     */
    static void installSignalHandler(void (*handler)(int, siginfo_t*, void*), int signal);

    /**
     * @return a string description of the caught signal info
     */
    static std::string toString(siginfo_t* info);

    /**
     * @return a stack trace as a string; the caller address is not included.
     */
    static std::string getStackTrace();

    static std::string signalName(int signalNumber);

    static std::string signalInfo(siginfo_t *info);

    static std::string basicSignalInfo(siginfo_t *info);

    static std::string extraSignalInfo(siginfo_t *info);

    static std::string pointerToString(const void *p);

};

}
#endif

