//
// Created by Dimitar Misev
// Copyright (c) 2018 rasdaman GmbH. All rights reserved.
//

#include "stacktrace.hh"
#ifdef RASDEBUG
#include "backward.hpp"  // for ResolvedTrace, ResolvedTrace::SourceLoc, Sta...
#else
#include <cxxabi.h>
#include <execinfo.h>
#endif
#include <stdio.h>       // for feof, fgets, popen, pclose, FILE
#include <unistd.h>      // for getpid
#include <array>         // for array
#include <iomanip>       // for operator<<, setw
#include <iostream>      // for operator<<, basic_ostream, bas...
#include <sstream>       // for stringstream
#include <memory>        // for shared_ptr, allocator_traits<>::value_type
#include <utility>       // for pair

namespace common {
namespace stacktrace {

using std::string;
using std::stringstream;

#define MAX_MSG_LEN 500

StackTrace::StackTraceEntry::StackTraceEntry(
    std::size_t index, const string &fileloc, const string &demang,
    const string &hex, const string &addr, const string &code) :
    m_index(index),
    m_fileloc(fileloc),
    m_demangled(demang),
    m_hex(hex),
    m_addr(addr),
    m_code(code) {
}

string StackTrace::StackTraceEntry::toString() const {
  return toString(0);
}

std::string StackTrace::StackTraceEntry::toString(size_t offset) const {
  stringstream ss;
  ss << std::setw(5) << ("[" + std::to_string(m_index - offset) + "] ")
     << m_fileloc
     << (m_demangled.empty() ? "" : (" in " + m_demangled))
     << (m_hex.empty() ? "" : ("+" + m_hex))
     << (m_addr.empty() ? "" : (" [" + m_addr + "]"));
  if (!m_code.empty())
    ss << "\n" << std::setw(7) << "> " << m_code;
  return ss.str();
}

string StackTrace::StackTraceEntry::toSingleLineString() const {
  stringstream ss;
  string fileloc = m_fileloc;
  size_t pos = m_fileloc.size() - 1;
  size_t countSlashes = 0;
  while (pos-- > 0) {
    if (m_fileloc[pos] == '/') ++countSlashes;
    if (countSlashes == 3) fileloc = m_fileloc.substr(pos);
  }
  ss << fileloc
     << (m_demangled.empty() ? "" : (" in " + m_demangled))
     << (m_hex.empty() ? "" : ("+" + m_hex))
     << (m_addr.empty() ? "" : (" [" + m_addr + "]"));
  return ss.str();
}

string StackTrace::getCaller() const {
  if (m_stack.size() > 1) {
    return m_stack[1].toSingleLineString();
  }
  return "no caller function found in the stack trace.";
}

string StackTrace::toString(size_t offset) const {
  stringstream ss;
  auto it = m_stack.begin();
  for (it += offset; it != m_stack.end(); ++it) {
    ss << it->toString(offset) << "\n";
  }
  return ss.str();
}

void StackTrace::generateNew(void) {
  m_stack.clear();

#ifndef RASDEBUG

  void* addresses[kMaxStack];
  auto size = backtrace(addresses, kMaxStack);
  char** messages = backtrace_symbols(addresses, size);

  // Skip StackTrace c'tor and generateNew
  if (size > static_cast<int>(kStackStart)) {
    for (decltype(size) i = kStackStart, j = 1; i < size; ++i, ++j) {

      // e.g. test/common/tilemgr/test_common_tilemgr(_ZN4test31TileMgrTest_TestStackTrace_Test8TestBodyEv+0x22) [0x555da7ee7940]
      const string line(messages[i]);
      string mangName;
      string offset;
      string address;
      splitMessage(line, mangName, offset, address);
      if (mangName.empty() || offset.empty() || address.empty()) {
        m_stack.emplace_back(j, line);
        continue;
      }

      auto location = getSourceLocation(address);

      // Perform demangling if parsed properly
      int status = 0;
      char* demangName = abi::__cxa_demangle(mangName.c_str(), 0, 0, &status);
      // if demangling is successful, output the demangled function name
      if (status == 0) {
        // Success (see http://gcc.gnu.org/onlinedocs/libstdc++/libstdc++-html-USERS-4.3/a01696.html)
        StackTraceEntry entry(static_cast<size_t>(j), location, demangName, offset, address);
        m_stack.push_back(entry);
      } else {
        // Not successful - we will use mangled name
        StackTraceEntry entry(static_cast<size_t>(j), location, mangName, offset, address);
        m_stack.push_back(entry);
      }
      free(demangName);
    }
  }
  free(messages);

#else
  backward::StackTrace st;
  st.load_here(32);

  backward::SnippetFactory sf;
  backward::TraceResolver tr;
  tr.load_stacktrace(st);
  for (size_t i = 4; i < st.size(); ++i) {
    auto trace = tr.resolve(st[i]);
    string location = "??:0";
    string code = "";

    if (!trace.inliners.empty()) {
      for (size_t inliner_idx = trace.inliners.size(); inliner_idx > 0;
           --inliner_idx) {
        const backward::ResolvedTrace::SourceLoc
            &inliner_loc = trace.inliners[inliner_idx - 1];
        location =
            inliner_loc.filename + ":" + std::to_string(inliner_loc.line);
        auto lines = sf.get_snippet(inliner_loc.filename, inliner_loc.line, 1);
        for (const auto &line: lines) {
          code += std::to_string(line.first) + ": " + line.second;
          break;
        }
      }
    } else if (!trace.source.filename.empty()) {
      location =
          trace.source.filename + ":" + std::to_string(trace.source.line);
      auto lines = sf.get_snippet(trace.source.filename, trace.source.line, 1);
      for (const auto &line: lines) {
        code += std::to_string(line.first) + ": " + line.second;
        break;
      }
    }
    m_stack.emplace_back(i - 3, location, trace.object_function, "", "", code);
  }
#endif
}

void StackTrace::splitMessage(const string &line,
                              string &mangName,
                              string &offset,
                              string &address) const {

  // find parantheses and +address offset surrounding mangled name
  size_t mangled_name = 0, offset_begin = 0, offset_end = 0, address_begin = 0,
      address_end = 0;
  for (size_t p = 0; p < line.length(); ++p) {
    switch (line[p]) {
      case '(': mangled_name = p;
        break;
      case '+': offset_begin = p;
        break;
      case ')': offset_end = p;
        break;
      case '[': address_begin = p;
        break;
      case ']': address_end = p;
        break;
      default: break;
    }
  }
  // if the line could not be processed add as is and go on
  if (!mangled_name || !offset_begin || mangled_name >= offset_begin
      || !address_begin) {
    return;
  }

  mangName = line.substr(mangled_name + 1, offset_begin - mangled_name - 1);
  offset = line.substr(offset_begin + 1, offset_end - offset_begin - 1);
  address = line.substr(address_begin + 1, address_end - address_begin - 1);
}

string StackTrace::getSourceLocation(const std::string &address) const {
  auto linkname = getExecutablePath();
  auto cmd = "addr2line -i -s -e " + linkname + " " + address;
  auto ret = executeCmd(cmd);
  if (ret.empty())
    ret = "??:0";
  return ret;
}

string StackTrace::getExecutablePath() const {
  stringstream ret;
  ret << "/proc/" << getpid() << "/exe";
  return ret.str();
}

std::string StackTrace::executeCmd(const std::string &cmd) const {
  std::array<char, MAX_MSG_LEN> buffer;
  std::string result;
  std::shared_ptr<FILE> pipe(popen(cmd.c_str(), "r"), pclose);
  if (!pipe)
    return result;
  while (!feof(pipe.get())) {
    if (fgets(buffer.data(), MAX_MSG_LEN, pipe.get()) != nullptr)
      result += buffer.data();
  }
  auto newLinePos = result.find('\n');
  if (newLinePos != string::npos)
    result = result.substr(0, newLinePos);
  return result;
}

}
}