#ifndef _COMMON_DBINFO_HH_
#define _COMMON_DBINFO_HH_

#include <string>

namespace common
{

enum class PrintTiles
{
    NONE,
    EMBEDDED,
    JSON,
    SVG
};

std::string printTilesToString(PrintTiles arg);
PrintTiles stringToPrintTiles(const std::string &arg);

}  // namespace common

#endif
