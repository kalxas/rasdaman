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
* rasql_signal
*
* PURPOSE:  Provides signal handling
*
* COMMENTS:
*
*   No comments
*/

#include "config.h"
#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#include "raslib/template_inst.hh"
#endif

#include <iostream>
#include <string>
#include <cstdlib>
#include <signal.h>

#include <logging.hh>

#include "rasql_error.hh"
#include "rasql_signal.hh"

using namespace std;

//installSignalHandlers function should be called first in main function
//in order to receive a signal in your program
void
signalHandler(int sig)
{
    static bool handleSignal = true;    // sema to prevent nested signals

    cout << "Caught signal " << sig << ": ";
    switch (sig)
    {
    case SIGHUP:
        cout << "Hangup (POSIX).  ";
        break;
    case SIGINT:
        cout << "Interrupt (ANSI).";
        break;
    case SIGQUIT:
        cout << "Quit (POSIX).";
        break;
    case SIGILL:
        cout << "Illegal instruction (ANSI).";
        break;
    case SIGTRAP:
        cout << "Trace trap (POSIX).";
        break;
    case SIGABRT:
        cout << "Abort (ANSI) or IOT trap (4.2 BSD).";
        break;
    case SIGBUS:
        cout << "BUS error (4.2 BSD).";
        break;
    case SIGFPE:
        cout << "Floating-point exception (ANSI).";
        break;
    case SIGKILL:
        cout << "Kill, unblockable (POSIX).";
        break;
    case SIGUSR1:
        cout << "User-defined signal 1 (POSIX).";
        break;
    case SIGSEGV:
        cout << "Segmentation violation (ANSI).";
        break;
    case SIGUSR2:
        cout << "User-defined signal 2 (POSIX).";
        break;
    case SIGPIPE:
        cout << "Broken pipe (POSIX).";
        break;
    case SIGALRM:
        cout << "Alarm clock (POSIX).";
        break;
    case SIGTERM:
        cout << "Termination (ANSI).";
        break;
#ifndef SOLARIS
#ifndef DECALPHA
#ifndef __APPLE__
    case SIGSTKFLT:
        cout << "Stack fault.";
        break;
#endif
#endif
#endif
    case SIGCONT:
        cout << "Continue (POSIX).";
        break;
    case SIGSTOP:
        cout << "Stop, unblockable (POSIX).";
        break;
    case SIGTSTP:
        cout << "Keyboard stop (POSIX). Continuing operation.";
        break;
    case SIGTTIN:
        cout << "Background read from tty (POSIX).";
        break;
    case SIGTTOU:
        cout << "Background write to tty (POSIX). Continuing operation";
        break;
    case SIGURG:
        cout << "Urgent condition on socket (4.2 BSD).";
        break;
    case SIGXCPU:
        cout << "CPU limit exceeded (4.2 BSD).";
        break;
    case SIGXFSZ:
        cout << "File size limit exceeded (4.2 BSD).";
        break;
    case SIGVTALRM:
        cout << "Virtual alarm clock (4.2 BSD).";
        break;
    case SIGPROF:
        cout << "Profiling alarm clock (4.2 BSD).";
        break;
    case SIGWINCH:
        cout << "Window size change (4.3 BSD, Sun). Continuing operation.";
        break;
#ifndef __APPLE__
    case SIGCLD:
        cout << "SIGCHLD (System V) or child status has changed (POSIX).";
        break;
    case SIGPOLL:
        cout << "Pollable event occurred (System V) or I/O now possible (4.2 BSD).";
        break;
    case SIGPWR:
        cout << "Power failure restart (System V).";
        break;
#endif
    case SIGSYS:
        cout << "Bad system call.";
        break;
    default:
        cout << "Unknown signal.";
        break;
    }
    cout << endl << flush;

    // no repeated signals
    if (handleSignal)
    {
        handleSignal = false;
    }

    if (sig == SIGCONT || sig == SIGTSTP || sig == SIGTTIN || sig == SIGTTOU || sig == SIGWINCH)
    {
        return;
    }
    else
    {
        LDEBUG << "fatal signal, exiting.";
        exit(sig);
    }
}

void
installSignalHandlers()
{
    struct sigaction signal;
    memset(&signal, 0, sizeof(signal));
    signal.sa_handler = signalHandler;

    sigaction(SIGINT, &signal, NULL);
    sigaction(SIGTERM, &signal, NULL);
    sigaction(SIGHUP, &signal, NULL);
    sigaction(SIGPIPE, &signal, NULL);
    sigaction(SIGHUP, &signal, NULL);
    sigaction(SIGINT, &signal, NULL);
    sigaction(SIGQUIT, &signal, NULL);
    sigaction(SIGILL, &signal, NULL);
    sigaction(SIGTRAP, &signal, NULL);
    sigaction(SIGABRT, &signal, NULL);
    sigaction(SIGIOT, &signal, NULL);
    sigaction(SIGBUS, &signal, NULL);
    sigaction(SIGFPE, &signal, NULL);
    sigaction(SIGKILL, &signal, NULL);
    sigaction(SIGUSR1, &signal, NULL);
    sigaction(SIGSEGV, &signal, NULL);
    sigaction(SIGUSR2, &signal, NULL);
    sigaction(SIGPIPE, &signal, NULL);
    sigaction(SIGALRM, &signal, NULL);
    sigaction(SIGTERM, &signal, NULL);
#ifndef SOLARIS
#ifndef DECALPHA
#ifndef __APPLE__
    sigaction(SIGSTKFLT, &signal, NULL);
#endif
#endif
#endif
    sigaction(SIGCHLD, &signal, NULL);
    sigaction(SIGCONT, &signal, NULL);
    sigaction(SIGSTOP, &signal, NULL);
    sigaction(SIGTSTP, &signal, NULL);
    sigaction(SIGTTIN, &signal, NULL);
    sigaction(SIGTTOU, &signal, NULL);
    sigaction(SIGURG, &signal, NULL);
    sigaction(SIGXCPU, &signal, NULL);
    sigaction(SIGXFSZ, &signal, NULL);
    sigaction(SIGVTALRM, &signal, NULL);
    sigaction(SIGPROF, &signal, NULL);
    sigaction(SIGWINCH, &signal, NULL);
#ifndef __APPLE__
    sigaction(SIGCLD, &signal, NULL);
    sigaction(SIGPOLL, &signal, NULL);
    sigaction(SIGPWR, &signal, NULL);
#endif
    sigaction(SIGIO, &signal, NULL);
    sigaction(SIGSYS, &signal, NULL);
#if !defined SOLARIS
#if !defined DECALPHA
#ifndef __APPLE__
    sigaction(SIGUNUSED, &signal, NULL);
#endif
#endif
#endif
}

