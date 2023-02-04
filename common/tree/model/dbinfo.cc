//
// Created by Dimitar Misev
// Copyright (c) 2020 rasdaman GmbH. All rights reserved.
//

#include "dbinfo.hh"
#include "common/pragmas/pragmas.hh"
#include "common/exceptions/invalidargumentexception.hh"

DIAGNOSTIC_PUSH
IGNORE_WARNING("-Wreturn-type")

namespace common {
using std::string;

string printTilesToString(PrintTiles op) {
  switch (op) {
    case PrintTiles::NONE: return string("none");
    case PrintTiles::EMBEDDED: return string("embedded");
    case PrintTiles::JSON: return string("json");
    case PrintTiles::SVG: return string("svg");
  }
}

PrintTiles stringToPrintTiles(const std::string &arg)
{
    if (arg == "printtiles=1" || arg == "printtiles=embedded")
      return PrintTiles::EMBEDDED;
    else if (arg == "printtiles=json")
      return PrintTiles::JSON;
    else if (arg == "printtiles=svg")
      return PrintTiles::SVG;
    else if (arg == "printtiles=none" || arg.empty())
      return PrintTiles::NONE;
    else
      throw InvalidArgumentException{"Invalid printtiles argument: " + arg};
}

}  // namespace common

DIAGNOSTIC_POP
