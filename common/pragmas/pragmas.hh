#pragma once

/**
 * Transform the given argument into a string.
 */
#if ! defined(STRINGIFY)
#define STRINGIFY(a) #a
#endif

/**
 * Conditionally include the appropriate file depending on the compiler version.
 */
#if defined(__clang__)
/* Clang/LLVM. */

#include "clang/clangpragmas.hh"

#elif defined(__GNUC__) || defined(__GNUG__)

/* GNU GCC/G++. */
#include "gcc/gccpragmas.hh"

#endif
