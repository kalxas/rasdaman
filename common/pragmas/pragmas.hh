#pragma once

/// Transform the given argument into a string.
#if ! defined(STRINGIFY)
#define STRINGIFY(a) #a
#endif

/// Use in conditionals to signal that the condition is very unlikely
#if ! defined(unlikely)
#define unlikely(x)    __builtin_expect(!!(x), 0)
#endif
/// Use in conditionals to signal that the condition is very likely
#if ! defined(likely)
#define likely(x)      __builtin_expect(!!(x), 1)
#endif

#define UNUSED __attribute__((unused))

/// Conditionally include the appropriate file depending on the compiler version.
#if defined(__clang__)
#include "clang/clangpragmas.hh"
#elif defined(__GNUC__) || defined(__GNUG__)
#include "gcc/gccpragmas.hh"
#endif
