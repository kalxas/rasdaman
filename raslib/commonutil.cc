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
#include "raslib/rmdebug.hh"

#include <signal.h>
#include <execinfo.h>
#include <cxxabi.h>
#include <cstring>
#include <cstdlib>
#include <cstdio>

#include <sys/types.h>
#include <unistd.h>

#include "commonutil.hh"


#include "debug/debug.hh"   //ENTER and LEAVE

// max length of segfault backtrace output
#define BACKTRACE_TRUNC 50
#define MAX_MSG_LEN 500

void print_stacktrace(void *ucontext) {
  // code adapted from
  // http://stackoverflow.com/questions/77005/how-to-generate-a-stacktrace-when-my-gcc-c-app-crashes/2526298#2526298

  void* fault_address;
  sig_ucontext_t* uc;
  uc = (sig_ucontext_t*) ucontext;
  fault_address = getFaultAddress(uc);

  void * addresses[BACKTRACE_TRUNC];
  int size = backtrace(addresses, BACKTRACE_TRUNC);
  addresses[1] = fault_address;

  char ** messages = backtrace_symbols(addresses, size);

  // skip first stack frame (points here)
  RMInit::logOut << endl << endl << "Segmentation fault caught, stacktrace:" << endl;

  printf("\n\nSegmentation fault caught, stacktrace:\n");

  for (int i = 3, j = 1; i < size && messages != NULL; ++i, ++j) {
    char *mangled_name = 0, *offset_begin = 0, *offset_end = 0;


    // find parantheses and +address offset surrounding mangled name
    for (char *p = messages[i]; *p; ++p) {
      if (*p == '(') {
        mangled_name = p;
      } else if (*p == '+') {
        offset_begin = p;
      } else if (*p == ')') {
        offset_end = p;
        break;
      }
    }

    // if the line could be processed, attempt to demangle the symbol
    if (mangled_name && offset_begin && offset_end &&
            mangled_name < offset_begin ) {
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
          RMInit::logOut << endl << "Error getting binary path for print_stacktrace." << std::endl;
          printf("Error getting binary path for print_stackTrace.\n");
      }


      sprintf(cmd, "addr2line -i -s -e %s 0x%x", linkname, addresses[i]);

      FILE *fp = popen(cmd, "r");
      if (fp != NULL)
      {
        fgets(sourceFileLine, MAX_MSG_LEN, fp);
        char *pos = strchr(sourceFileLine, '\n');
        if (pos != NULL)
            *pos = '\0';
        pclose(fp);
      } else {
        strcpy(sourceFileLine, "??:0\0");
      }

      int status;
      char * real_name = abi::__cxa_demangle(mangled_name, 0, 0, &status);

      // if demangling is successful, output the demangled function name
      if (status == 0) {
        RMInit::logOut << "[bt]: (" << j << ") " << messages[i] << " (" << sourceFileLine << ") - "
                << real_name << "+" << offset_begin << offset_end
                << std::endl;
        fflush(stdout);
        printf("[bt]: (%d) %s (%s) - %s+%s%s\n", j, messages[i], sourceFileLine, real_name, offset_begin, offset_end);


      }        // otherwise, output the mangled function name
      else {
        RMInit::logOut << "[bt]: (" << j << ") " << messages[i] << " (" << sourceFileLine << ") - "
                << mangled_name << "+" << offset_begin << offset_end
                << std::endl;
        fflush(stdout);
        printf("[bt]: (%d) %s (%s) - %s+%s%s\n", j, messages[i], sourceFileLine, real_name, offset_begin, offset_end);

      }
      free(real_name);
    }      // otherwise, print the whole line
    else {
      RMInit::logOut << "[bt]: (" << j << ") " << messages[i] << std::endl;
      printf("[bt]: (%d) %s\n", j, messages[i]);
    }
  }

  free(messages);
}


void
installSigSegvHandler(void (*cleanUpHandler)(int, siginfo_t* , void* ) ){

    ENTER( "installSigSegvHandler" );

    struct sigaction sigact;

    //setup the handling function
    sigact.sa_sigaction = cleanUpHandler;


    sigact.sa_flags = SA_RESTART | SA_SIGINFO;


    int retVal = sigaction(SIGSEGV , &sigact, (struct sigaction *)NULL);

    if(retVal != 0)
    {
      RMInit::logOut << std::endl << "Installing SIGSEGV handler failed. " << std::endl;
    }

    LEAVE( "installSigSegvHandler");

}




void* getFaultAddress(sig_ucontext_t * uc)
{
    ENTER( "getFaultAddress");
    void *              caller_address;

     /* Get the address at the time the signal was raised */
#if defined(__i386__) // gcc specific
 caller_address = (void *) uc->uc_mcontext.eip; // EIP: x86 specific
#elif defined(__x86_64__) // gcc specific
 caller_address = (void *) uc->uc_mcontext.rip; // RIP: x86_64 specific
#else
#error Unsupported architecture. // TODO: Add support for other arch.
#endif



    LEAVE( "getFaultAddress");
    return caller_address;

}
