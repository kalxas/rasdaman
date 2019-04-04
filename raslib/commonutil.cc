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
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#include "config.h"

#include <signal.h>
#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <string>

#include <sys/types.h>
#include <sys/resource.h>
#include <unistd.h>
#include <stdio.h>

#include "commonutil.hh"

#include <logging.hh>

#include "debug/debug.hh"   //ENTER and LEAVE

// max length of segfault backtrace output
#define BACKTRACE_TRUNC 50
#define MAX_MSG_LEN 500

#define MAX_CMD_LENGTH FILENAME_MAX
#define MAX_ERROR_MSG_LENGTH 20000

void print_stacktrace(void *ucontext)
{
#ifndef WITH_DEBUG_SYMBOLS
    return;
#endif

    // code adapted from
    // http://stackoverflow.com/questions/77005/how-to-generate-a-stacktrace-when-my-gcc-c-app-crashes/2526298#2526298
    void *fault_address;
    sig_ucontext_t *uc;
    uc = static_cast<sig_ucontext_t *>(ucontext);
    fault_address = getFaultAddress(uc);
    void *addresses[BACKTRACE_TRUNC];
    int size = backtrace(addresses, BACKTRACE_TRUNC);
    addresses[1] = fault_address;

    char **messages = backtrace_symbols(addresses, size);

    // skip first stack frame (points here)
    LINFO << "Segmentation fault caught, stacktrace:";

    for (int i = 3, j = 1; i < size && messages != NULL; ++i, ++j)
    {
        char *mangled_name = 0, *offset_begin = 0, *offset_end = 0;

        // find parantheses and +address offset surrounding mangled name
        for (char *p = messages[i]; *p; ++p)
        {
            if (*p == '(')
            {
                mangled_name = p;
            }
            else if (*p == '+')
            {
                offset_begin = p;
            }
            else if (*p == ')')
            {
                offset_end = p;
                break;
            }
        }

        // if the line could be processed, attempt to demangle the symbol
        if (mangled_name && offset_begin && offset_end &&
                mangled_name < offset_begin)
        {
            *mangled_name++ = '\0';
            *offset_begin++ = '\0';
            *offset_end++ = '\0';

            // get source file name and line
            char cmd[MAX_MSG_LEN];
            char sourceFileLine[MAX_MSG_LEN];
            //get absolute path of the currently executing binary
            char linkname[64];
            pid_t pid = getpid();

            if (snprintf(linkname, sizeof(linkname), "/proc/%i/exe", pid) < 0)
            {
                LERROR << "Error getting binary path for print_stacktrace.";
            }


            sprintf(cmd, "addr2line -i -s -e %s 0x%x", linkname, ((unsigned int *)(addresses))[i]);
            FILE *fp = popen(cmd, "r");
            if (fp != NULL)
            {
                fgets(sourceFileLine, MAX_MSG_LEN, fp);
                char *pos = strchr(sourceFileLine, '\n');
                if (pos != NULL)
                {
                    *pos = '\0';
                }
                pclose(fp);
            }
            else
            {
                strcpy(sourceFileLine, "??:0\0");
            }

            int status;
            char *real_name = abi::__cxa_demangle(mangled_name, 0, 0, &status);

            // if demangling is successful, output the demangled function name
            if (status == 0)
            {
                LINFO << "[bt]: (" << j << ") " << messages[i] << " (" << sourceFileLine << ") - "
                      << real_name << "+" << offset_begin << offset_end;
                fflush(stdout);

            }        // otherwise, output the mangled function name
            else
            {
                LINFO << "[bt]: (" << j << ") " << messages[i] << " (" << sourceFileLine << ") - "
                      << mangled_name << "+" << offset_begin << offset_end;
                fflush(stdout);
            }
            free(real_name);
        }      // otherwise, print the whole line
        else
        {
            LINFO << "[bt]: (" << j << ") " << messages[i];
        }
    }

    free(messages);
}

void
installSigSegvHandler(void (*cleanUpHandler)(int, siginfo_t *, void *))
{
    installSigHandler(cleanUpHandler, SIGSEGV);
}

void
installSigHandler(void (*cleanUpHandler)(int, siginfo_t *, void *), int signal)
{
    struct sigaction sigact;

    //setup the handling function
    sigact.sa_sigaction = cleanUpHandler;


    sigact.sa_flags = SA_RESTART | SA_SIGINFO;
    sigemptyset(&sigact.sa_mask);


    int retVal = sigaction(signal, &sigact, (struct sigaction *)NULL);

    if (retVal != 0)
    {
        LERROR << "Installing handler for signal " << signal << " failed. ";
    }
}

void *getFaultAddress(sig_ucontext_t *uc)
{
    void               *caller_address;

    /* Get the address at the time the signal was raised */
#if defined(__i386__) // gcc specific
    caller_address = (void *) uc->uc_mcontext.eip; // EIP: x86 specific
#elif defined(__x86_64__) // gcc specific
    caller_address = (void *) uc->uc_mcontext.rip; // RIP: x86_64 specific
#elif defined(ARM)
    caller_address = (void *) uc->uc_mcontext.arm_pc; // ARM specific
#else
    caller_address = NULL;
#endif

    return caller_address;

}

char *read_file(FILE *fp)
{
    char buffer[MAX_ERROR_MSG_LENGTH];
    std::string tmp("");
    char *ret = NULL;
    while (!feof(fp))
    {
        if (fgets(buffer, MAX_ERROR_MSG_LENGTH, fp) != NULL)
        {
            tmp += buffer;
        }
    }
    if (tmp.length() > 0)
    {
        ret = strdup(tmp.c_str());
    }
    return ret;
}

char *execute_system_command(char *cmd)
{
    char *ret = NULL;
    char popenCmd[MAX_CMD_LENGTH];
    snprintf(popenCmd, MAX_CMD_LENGTH, "%s 2>&1", cmd);
    FILE *fp = popen(popenCmd, "r");
    if (!fp)
    {
        char buffer[MAX_CMD_LENGTH];
        snprintf(buffer, MAX_CMD_LENGTH, "Failed to execute command:\n%s\n", cmd);
        ret = strdup(buffer);
    }
    else
    {
        char *res = read_file(fp);
        char buffer[MAX_ERROR_MSG_LENGTH];
        memset(buffer, 0, MAX_ERROR_MSG_LENGTH);
        int rc = pclose(fp);
        if (rc != 0)
        {
            if (res)
            {
                snprintf(buffer, MAX_ERROR_MSG_LENGTH, "Failed to execute command:\n%s\nError: %s\n", cmd, res);
            }
            else
            {
                snprintf(buffer, MAX_ERROR_MSG_LENGTH, "Failed to execute command:\n%s\n", cmd);
            }
            ret = strdup(buffer);
        }
        if (res)
        {
            free(res);
            res = NULL;
        }
    }
    return ret;
}

size_t get_current_rss()
{
    long rss = 0L;
    FILE *fp = NULL;
    if ((fp = fopen("/proc/self/statm", "r")) == NULL)
    {
        return (size_t)0L;    /* Can't open? */
    }
    if (fscanf(fp, "%*s%ld", &rss) != 1)
    {
        fclose(fp);
        return (size_t)0L;      /* Can't read? */
    }
    fclose(fp);
    return ((size_t)rss * (size_t)sysconf(_SC_PAGESIZE) / 1048576);
}
