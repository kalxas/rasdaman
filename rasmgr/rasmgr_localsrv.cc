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
/**
 * SOURCE: rasmgr_localsrv.cc
 *
 * MODULE: rasmgr
 * CLASS:  LocalServer, LocalServerManager
 *
 * PURPOSE:
 *   management of rasserver executables
 *
 * COMMENTS:
 *          None
 *
*/

using namespace std;

#include "config.h"
#include "rasmgr_localsrv.hh"
#include "rasmgr_master.hh"
#include "rasmgr_srv.hh"
#include <signal.h>
#include <unistd.h>
#include <time.h>

#ifdef __APPLE__
#include <limits.h>
#else
#include <linux/limits.h>   // ARG_MAX
#endif
// fix for missing ARG_MAX; workaround for glibc-2.8 and above
#if defined(_SC_ARG_MAX)
# if defined(ARG_MAX)
#    undef ARG_MAX
# endif
# define ARG_MAX sysconf (_SC_ARG_MAX)
#endif


#include "debug-srv.hh"
#include <logging.hh>


// aux function for now() to avoid a compiler warning (see 'man strftime')
size_t my_strftime(char* s, size_t max, const char* fmt, const struct tm* tm)
{
    return strftime(s, max, fmt, tm);
}

// now(): aux function returning, as a static string, the current time
// keep in sync with same function in rasserver
const char* now()
{
    size_t strfResult = 0;          // return value of strftime()
    static char timestring[50];     // must hold 20+1 chars

    time_t t = time(NULL);          // get time
    struct tm* tm = localtime(&t);  // break down time
    strfResult = my_strftime(timestring, sizeof(timestring), "[%F %T]", tm);        // format time
    if (strfResult == 0)            // bad luck? then take fallback message
    {
        (void) strncpy(timestring, "[-no time available-]", sizeof(timestring));
    }
    return (timestring);
}

LocalServer::LocalServer()
{
    serverName[0] = 0;
    valid = false;
    serverPid = 0;
}

void LocalServer::init(const char* name, pid_t p)
{
    strcpy(serverName, name);
    serverPid = p;
    valid = true;
}
const char* LocalServer::getName()
{
    return serverName;
}

pid_t LocalServer::getPID()
{
    return serverPid;
}
bool LocalServer::isValid()
{
    return valid;
}

//#######################################
void catch_SIGCHLD(int)
{
    localServerManager.childSignalIn();
}
//#######################################

LocalServerManager::LocalServerManager()
{
    wasSignal = false;

    signal(SIGCHLD, catch_SIGCHLD);
}
LocalServerManager::~LocalServerManager()
{
}
bool LocalServerManager::startNewServer(const char* commandline)
{
    char* localcomm;
    localcomm = new char[ARG_MAX];

    if (static_cast<int>(strlen(commandline)) >= ARG_MAX)
    {
        LDEBUG << "Error: rasserver launch command line too long: " << commandline;
        if (localcomm)
        {
            delete [] localcomm;
            localcomm = NULL;
        }
        return false;
    }

    strcpy(localcomm, commandline);

    int i;
    const int maxarg = 50;
    char* argv[maxarg];     // rasserver command line
    char* fileName;         // name of executable, e.g., "rasserver"
    char* serverName;       // symbolic server name, e.g., "S1"

    char* pos = localcomm;

    for (i = 0; i < maxarg; i++)
    {
#define WHITESPACE " \t\r\n"
        argv[i] = strtok(pos, WHITESPACE);
        pos = NULL; // for subsequent calls to strtok
        if (argv[i] == NULL)
        {
            break;
        }
    }
    argv[maxarg - 1] = 0; // for security reasons

    serverName = argv[0];
    fileName  = argv[1];

    LocalServer& lcs = operator[](serverName);
    if (lcs.isValid())
    {
        LDEBUG << "Server " << serverName << " is already up.";
        return false;
    }

    // return false;
    pid_t pid = fork();

    if (pid != 0)
    {
        //parent
        LocalServer temp;
        temp.init(serverName, pid);
        srvList.push_back(temp);
        LDEBUG << "LocalServerManager::startNewServer: leave. parent process. result=true.";
        LDEBUG << now() << " starting server " << serverName << ", executable " << fileName << "; pid " << pid << "...";

    }
    else
    {
        //child

        LDEBUG << "LocalServerManager::startNewServer: leave. child process, fileName=" << fileName;

        masterCommunicator.closeForcedAllSockets();

        execvp(fileName, argv + 2);
        int execErrno = errno;
        LERROR << "Error: cannot fork server " << fileName << ": " << strerror(execErrno);
        LDEBUG << "LocalServerManager::startNewServer: cannot fork server " << fileName << ": " << strerror(execErrno);
        exit(1); // if return from exec...
    }
    delete[] localcomm;
    return true;
}
int  LocalServerManager::countStartedServers()
{
    return srvList.size();
}

// sendTerminateSignal: terminate server process.
// if name is in list of known servers, try to terminate; otherwise, complain & do nothing.
// returns:
//  true    iff server was found and killed successfully
//  false   on error
bool LocalServerManager::sendTerminateSignal(const char* serverName)
{
    bool found = false; // list entry pertaining to serverName found?
    bool result = false;    // function result

    list<LocalServer>::iterator iter = srvList.begin();
    for (unsigned int i = 0; i < srvList.size() && ! found; i++)
    {
        if (strcmp(iter->getName(), serverName) == 0)
        {
            found = true;
            LDEBUG <<  now() << " shutting down rasdaman server " << iter->getName() << ", pid " << iter->getPID() << "...";
            int killResult = kill(iter->getPID(), SIGTERM);
            if (killResult == -1)
            {
                LERROR << "Error: " << strerror(errno);
                result = false;
            }
            else
            {
                iter = srvList.erase(iter);
                LDEBUG << "ok";
                result = true;
                break;
            }
        }

        iter++;
    } // for

    if (!found)
    {
        LERROR << "failed: server unknown.";
        result = false;
    }

    return result;
}

// killServer: terminate server process.
// if name is in list of known servers, try to kill; otherwise, complain & do nothing.
// returns:
//  true    iff server was found and killed successfully
//  false   on error
bool LocalServerManager::killServer(const char* serverName)
{
    bool found = false; // list entry pertaining to serverName found?
    bool result = false;    // function result

    list<LocalServer>::iterator iter = srvList.begin();
    for (unsigned int i = 0; i < srvList.size(); i++)
    {
        if (strcmp(iter->getName(), serverName) == 0)
        {
            found = true;
            LDEBUG <<  now() << " killing rasdaman server " << iter->getName() << ", pid " << iter->getPID() << "...";

            // try graceful termination first
            int killResult = kill(iter->getPID(), SIGTERM);

            // force kill just in case after 30ms
            usleep(30000);
            killResult = kill(iter->getPID(), SIGKILL);

            if (killResult == -1)
            {
                LERROR << "Error: " << strerror(errno);
                result = false;
            }
            if (killResult >= 0)
            {
                iter = srvList.erase(iter);
                LDEBUG << "ok";
                result = true;
                break;
            }
        }
        iter++;
    }

    if (!found)
    {
        LERROR << "failed: server unknown.";
        result = false;
    }

    return result;
}

LocalServer& LocalServerManager::operator[](int x)
{
    list<LocalServer>::iterator iter = srvList.begin();
    for (int i = 0; i < x; i++)
    {
        iter++;
    }
    return *iter;

}
LocalServer& LocalServerManager::operator[](const char* srvName)
{
    list<LocalServer>::iterator iter = srvList.begin();
    for (unsigned int i = 0; i < srvList.size(); i++)
    {
        if (strcmp(iter->getName(), srvName) == 0)
        {
            return *iter;
        }
        iter++;
    }
    return protElem;
}

void LocalServerManager::childSignalIn() //only signal calls this
{
    wasSignal = true;
}
void LocalServerManager::cleanChild()
{

    if (wasSignal == false)
    {
        return;
    }

#define WAITFORANYCHILD -1

    while (1)
    {
        signal(SIGCHLD, catch_SIGCHLD);  // some SO requieres this, otherwise they reset to default

        int status = 0;
        int exitpid = waitpid(WAITFORANYCHILD, &status, WNOHANG);

        if (exitpid == 0)
        {
            break;    // no child died
        }

        // I'd love to put this code into a textbook as a negative example and cite you, Walter! -- PB 2003-nov-25
        if (exitpid == -1)
        {
            if (errno == EINTR)
            {
                continue;
            }
            break; // another error;
        }
        LDEBUG << "rasdaman server process with pid " << exitpid << " has terminated.";

        list<LocalServer>::iterator iter = srvList.begin();
        for (unsigned int i = 0; i < srvList.size(); i++)
        {
            LDEBUG << "LocalServerManager::cleanChild: inspecting rasdaman server " << iter->getName() << ".";
            if (iter->getPID() == exitpid)
            {
                LDEBUG << "LocalServerManager::cleanChild: rasdaman server " << iter->getName() << " terminated illegally, status=" << status;

                LERROR << "Error: rasdaman server " << iter->getName() << ", pid " << exitpid << " terminated illegally, reason: ";
                // see 'man waitpid': decoding of status variable
                if (WIFEXITED(status) != 0)
                {
                    LERROR << "exited with return code " << WEXITSTATUS(status);
                }
                else if (WIFSIGNALED(status))
                {
                    LERROR << "uncaught signal " << WTERMSIG(status);
                }
                else
                {
                    LERROR << "(unknown reason)";
                }

                // choices: restart silently the dead server or
                // just tell the manager about it
                // Not restart from here, because of sync problem for capabilities, master has to do that!!!
                LocalServer temp = *iter;
                srvList.erase(iter);

                reportDeadServer(temp);
                break;
            }
            iter++;
        } // for
    } //while

    wasSignal = false;
}

void LocalServerManager::reportDeadServer(LocalServer& srv)
{
    int dummy = -1;
    RasServer& r = rasManager[srv.getName()];

    if (r.isValid())
    {
        r.changeStatus(SERVER_CRASHED, dummy);
    }
}

