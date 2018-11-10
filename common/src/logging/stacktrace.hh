//
// Created by Dimitar Misev
// Copyright (c) 2018 rasdaman GmbH. All rights reserved.
//

#pragma once

#include <cstddef>
#include <string>
#include <vector>

namespace common {
namespace stacktrace {
/**
 * Prints the stack trace from the line where this class is used.
 * Can be used in an output stream, e.g.
 *
 * LINFO << "Stack trace: " << StackTrace();
 *
 * Note: Adapted from easylogging (https://github.com/easylogging/easyloggingpp)
 */
class StackTrace {
 public:
  class StackTraceEntry {
   public:
    StackTraceEntry(std::size_t index,
                    const std::string& fileloc,
                    const std::string& demang,
                    const std::string& hex,
                    const std::string& addr,
                    const std::string& code = std::string{});

    StackTraceEntry(std::size_t index, const std::string& fileloc) :
      m_index(index),
      m_fileloc(fileloc) {
    }

    std::string getFileLoc() const;
    std::string toString() const;
    std::string toString(size_t offset) const;
    std::string toSingleLineString() const;

    std::size_t m_index;
    std::string m_fileloc;
    std::string m_demangled;
    std::string m_hex;
    std::string m_addr;
    std::string m_code;

   private:
    StackTraceEntry(void);
  };

  StackTrace(void) {
    generateNew();
  }

  ~StackTrace(void) = default;

  const std::vector<StackTraceEntry>& getStackTrace(void) const {
    return m_stack;

  }
  std::string getCaller() const;

  /**
   * Skip the n top-most stack entries
   */
  std::string toString(size_t offset = 0) const;

 private:

  static const unsigned int kMaxStack = 50;
  // skip c'tor and StackTrace::generateNew()
  static const unsigned int kStackStart = 2;

  std::vector<StackTraceEntry> m_stack;

  void generateNew(void);

  void splitMessage(const std::string &line,
    std::string &mangName, std::string &offset, std::string &address) const;

  // get absolute path to the source file and line in the source
  std::string getSourceLocation(const std::string &address) const;

  // get absolute path of the currently executing binary
  std::string getExecutablePath() const;

  // execute the given command and return output as string
  std::string executeCmd(const std::string &cmd) const;
};

}
}
