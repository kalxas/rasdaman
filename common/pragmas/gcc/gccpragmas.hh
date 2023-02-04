#pragma once

/**
 * https://gcc.gnu.org/onlinedocs/gnat_rm/Pragma-Loop_005fOptimize.html
 * This pragma must appear immediately within a loop statement.
 * It allows the programmer to specify optimization hints for the enclosing
 * loop.
 */
 // TODO
//#define OPTIMIZE_LOOP _Pragma(STRINGIFY(Loop_Optimize(Ivdep, Unroll, Vector)));

#define OPTIMIZE_LOOP

#define DIAGNOSTIC_PUSH   _Pragma("GCC diagnostic push")
#define IGNORE_WARNING(x) _Pragma(STRINGIFY(GCC diagnostic ignored x))
#define DIAGNOSTIC_POP    _Pragma("GCC diagnostic pop")

#define NOINLINE          __attribute__((noinline))
