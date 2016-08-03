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

#ifndef _COMMON_UTIL_H_
#define _COMMON_UTIL_H_

#define SEGFAULT_EXIT_CODE 11

#include <signal.h>
#include <cxxabi.h>
#include <execinfo.h>
#include <vector>
#include <iostream>

/**
 * Print stack trace.
 * @param fault_address address where the segfault happened.
 */
void print_stacktrace(void *ucontext);



/**
 * installs POSIX standard signal handler for SIGSEGV.
 * returns the value of sigaction() function call. 0 for success. More details: man sigaction
 * @arg cleanUpHandler is the handler for SIGSEGV. Read man sigaction -> sa_sigaction
 */

void
installSigSegvHandler(void (*cleanUpHandler)(int, siginfo_t* , void* ));

void
installSigHandler(void (*cleanUpHandler)(int, siginfo_t* , void* ), int signal);


/* This structure mirrors the one found in /usr/include/asm/ucontext.h
	defined here because /include/asm might not always be the path*/
typedef struct _sig_ucontext {
 unsigned long     uc_flags;
 struct ucontext   *uc_link;
 stack_t           uc_stack;
 struct sigcontext uc_mcontext;
 sigset_t          uc_sigmask;
} sig_ucontext_t;


/**
 * takes the sig_ucontext_t as argument and returns address where segmentation fault occured.
 * TODO: add support for other architectures
 */

void* getFaultAddress(sig_ucontext_t * uc);

/**
 * Read the contents of fp, and return it as a string. The returned result
 * must be freed.
 */
char* read_file(FILE* fp);

/**
 * Execute a system command, e.g. 'ls -l'.
 * @param cmd the command to execute
 * @return NULL if successful (the command returned 0), otherwise its output
 * (including stderr), in which case the returned string must be freed.
 */
char* execute_system_command(char* cmd);

/**
 * Returns the current resident set size (physical memory use) measured
 * in bytes, or zero if the value cannot be determined on this OS.
 * http://stackoverflow.com/a/14927379
 */
size_t get_current_rss();

/**
 * Prints the stack trace from the line where this class is used.
 * Use in an output stream, e.g.
 *
 * LINFO << "Stack trace: " << StackTrace();
 *
 * Note: Copied from easylogging, the 8.91 version we use doesn't contain
 * this functionality (https://github.com/easylogging/easyloggingpp)
 */
class StackTrace
{
public:
    static const int kMaxStack = 64;
    static const int kStackStart = 2;

    class StackTraceEntry
    {
    public:

        StackTraceEntry(int index, const char* loc, const char* demang, const char* hex, const char* addr)
        {
            m_index = index;
            m_location = std::string(loc);
            m_demangled = std::string(demang);
            m_hex = std::string(hex);
            m_addr = std::string(addr);
        }

        StackTraceEntry(int index, char* loc)
        {
            m_index = index;
            m_location = std::string(loc);
        }
        int m_index;
        std::string m_location;
        std::string m_demangled;
        std::string m_hex;
        std::string m_addr;

        friend std::ostream& operator<<(std::ostream& ss, const StackTraceEntry& si)
        {
            ss << "[" << si.m_index << "] " << si.m_location << (si.m_demangled.empty() ? "" : ":") << si.m_demangled
                << (si.m_hex.empty() ? "" : "+") << si.m_hex << si.m_addr;
            return ss;
        }
    private:
        StackTraceEntry(void);
    };

    StackTrace(void)
    {
        generateNew();
    }

    virtual
    ~StackTrace(void)
    {
    }

    inline std::vector<StackTraceEntry>&
    getLatestStack(void)
    {
        return m_stack;
    }

    friend inline std::ostream& operator<<(std::ostream& os, const StackTrace& st)
    {
        std::vector<StackTraceEntry>::const_iterator it = st.m_stack.begin();
        while (it != st.m_stack.end())
        {
            os << " " << *it++ << "\n";
        }
        return os;
    }
private:
    std::vector<StackTraceEntry> m_stack;

    void
    generateNew(void)
    {
        m_stack.clear();
        void* stack[kMaxStack];
        int size = backtrace(stack, kMaxStack);
        char** strings = backtrace_symbols(stack, size);
        if (size > kStackStart)
        { // Skip StackTrace c'tor and generateNew
            for (int i = kStackStart; i < size; ++i)
            {
                char* mangName = NULL;
                char* hex = NULL;
                char* addr = NULL;
                for (char* c = strings[i]; *c; ++c)
                {
                    switch (*c)
                    {
                        case '(':
                            mangName = c;
                            break;
                        case '+':
                            hex = c;
                            break;
                        case ')':
                            addr = c;
                            break;
                        default:
                            break;
                    }
                }
                // Perform demangling if parsed properly
                if (mangName != NULL && hex != NULL && addr != NULL && mangName < hex)
                {
                    *mangName++ = '\0';
                    *hex++ = '\0';
                    *addr++ = '\0';
                    int status = 0;
                    char* demangName = abi::__cxa_demangle(mangName, 0, 0, &status);
                    // if demangling is successful, output the demangled function name
                    if (status == 0)
                    {
                        // Success (see http://gcc.gnu.org/onlinedocs/libstdc++/libstdc++-html-USERS-4.3/a01696.html)
                        StackTraceEntry entry(i - 1, strings[i], demangName, hex, addr);
                        m_stack.push_back(entry);
                    }
                    else
                    {
                        // Not successful - we will use mangled name
                        StackTraceEntry entry(i - 1, strings[i], mangName, hex, addr);
                        m_stack.push_back(entry);
                    }
                    free(demangName);
                }
                else
                {
                    StackTraceEntry entry(i - 1, strings[i]);
                    m_stack.push_back(entry);
                }
            }
        }
        free(strings);
    }
};

#endif
