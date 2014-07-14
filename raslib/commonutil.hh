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

#endif
