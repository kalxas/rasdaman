#include "config.h"
#include "geobbox.hh"
#include "common/exceptions/exception.hh"
#include "common/string/stringutil.hh"
#include "raslib/error.hh"
#include "logging.hh"
#include <cstring>  // for strtok, NULL
#include <cassert>
#include <unordered_map>

#ifdef HAVE_GDAL
#include <ogr_core.h>        // for OGRERR_NONE, OGRErr
#include <ogr_spatialref.h>  // for OGRSpatialReference
#include <cpl_conv.h>        // for CPLFree
#endif

namespace common
{

using std::string;
using std::to_string;

double parseNumber(const char *input);
string crsToWkt(const string &crs);

double parseNumber(const char *input)
{
    char *end;
    double f = strtod(input, &end);
    if (end == input + strlen(input))
        return f;
    else
        throw common::Exception("Invalid number in geo bbox '" + string(input) + "'");
}

string crsToWkt(const string &crs)
{
#ifdef HAVE_GDAL
    // OGRSpatialReference is expensive, cache CRS
    static std::unordered_map<string, string> cachedCrs;
    auto it = cachedCrs.find(crs);
    if (it != cachedCrs.end())
        return it->second;

    OGRSpatialReference srs;
    if (srs.SetFromUserInput(crs.c_str()) != OGRERR_NONE)
        throw common::Exception("Invalid CRS '" + crs + "'.");
    char *tmp;
    srs.exportToWkt(&tmp);
    string ret{tmp};
    CPLFree(tmp);
    cachedCrs[crs] = ret;
    return ret;
#else
    return "";
#endif
}

GeoBbox::GeoBbox(const string &crsArg, const string &boundsArg, int w, int h)
    : gt{0, 0, 0, 0, 0}, width{w}, height{h}, crs{crsArg}, bounds{boundsArg}
{
    common::StringUtil::unescapeCharacters(crs);  // translate \" to "
    wkt = crsToWkt(crs);
    // parse bounds
    if (!bounds.empty())
    {
        char *str = new char[bounds.size() + 1];
        strcpy(str, bounds.c_str());
        char *split;
        split = strtok(str, ", ");
        xmin = parseNumber(split);
        split = strtok(NULL, ", ");
        ymin = parseNumber(split);
        split = strtok(NULL, ", ");
        xmax = parseNumber(split);
        split = strtok(NULL, ", ");
        ymax = parseNumber(split);
        delete[] str;

        updateGeoTransform();
    }
}

GeoBbox::GeoBbox(const string &crsArg, const string &boundsArg, double xres, double yres)
    : GeoBbox(crsArg, boundsArg, 0, 0)
{
    if (xres == 0.0 || yres == 0.0)
    {
        LERROR << "Invalid resolution, xres and yres must not be 0.";
        throw r_Error(454);
    }
    width = static_cast<int>((xmax - xmin + (xres / 2.0)) / xres);
    height = static_cast<int>(std::fabs(ymax - ymin + (yres / 2.0)) / yres);
    if (width <= 0)
    {
        LERROR << "Invalid X resolution " << xres << ", output width is <= 0.";
        throw r_Error(452);
    }
    if (height <= 0)
    {
        LERROR << "Invalid Y resolution " << yres << ", output height is <= 0.";
        throw r_Error(453);
    }
    gt[0] = xmin;
    gt[1] = xres;
    gt[3] = ymax;
    gt[5] = (ymax > ymin) ? -yres : yres;
}

void GeoBbox::updateGeoTransform()
{
    gt[0] = xmin;
    gt[1] = (xmax - xmin) / width;
    gt[2] = 0.0;
    gt[3] = ymax;
    gt[4] = 0.0;
    gt[5] = -1 * ((ymax - ymin) / height);
}

std::string GeoBbox::toString() const
{
    return crs + "; " +
           to_string(xmin) + "," + to_string(ymin) + "," +
           to_string(xmax) + "," + to_string(ymax) + "; " +
           to_string(width) + " x " + to_string(height) + "; GeoTransform: " +
           to_string(gt[0]) + "," + to_string(gt[1]) + "," +
           to_string(gt[2]) + "," + to_string(gt[3]) + "," +
           to_string(gt[4]) + "," + to_string(gt[5]);
}

}  // namespace common
