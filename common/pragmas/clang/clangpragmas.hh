#pragma once

// TODO: If possible, find a pragma which allows the user to optimize loops in
// clang
#define OPTIMIZE_LOOP

// clang-format off
#define DIAGNOSTIC_PUSH   _Pragma("clang diagnostic push")
#define IGNORE_WARNING(x) _Pragma(STRINGIFY(clang diagnostic ignored x))
#define DIAGNOSTIC_POP    _Pragma("clang diagnostic pop")
// clang-format on

#define NOINLINE
