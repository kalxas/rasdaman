/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2011 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/*
 * Contributed to rasdaman by Alexander Herzig, Landcare Research New Zealand
 */

/*!
 * \brief rasimport is a GDAL-based import utility for the
 *        rasdaman array data base.
 *
 * rasimport allows for easy import of 2D GDAL-readable image files
 * into the rasdaman array data base. In particular, it allows for
 * - storing geospatial coordinate reference information in the
 *   nm_meta table inside the data base
 * - the import of multiband images into a rasdaman collection
 * - the creation of 3D image cubes from 2D x/y image slices
 * - merging multiple image files into one image
 * - restricting the geospatial import region (i.e. subsetting)
 * - importing raster attribute tables (only in single file mode)
 * - importing any primitive data type (s. r_Primitive)
 * - writing petascope meta data
 * - sequential (i.e. row-wise) import of large image data
 */


#ifndef __rasimport_hh
#define __rasimport_hh

#include <cstring>
#include "conversion/gdalincludes.hh"

/// RASDAMAN includes
#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#ifdef __GNUG__
#include "raslib/template_inst.hh"
#include "rasgeo_template_inst.hh"
#endif
#endif

/// postgresql
#include "libpq-fe.h"

/// RasdamanHelper
#include "RasdamanHelper2.hh"

// required variables, if rasdaman is compiled with DEBUG enabled
//#ifdef RMANDEBUG
//int indentLevel;
//bool debugOutput;
//#endif

// db connector object
RasdamanConnector* rasconn;

/// DATA STRUCTURES
struct NMsize
{
    double x;
    double y;
    double z;
};

struct Header
{
    NMsize origin;      // origin (upper left corner of the upper left pixel) (x,y)
    long ncols;         // number of columns
    long nrows;         // number of rows
    long nlayers;       // number of layers (i.e. z-axis)

    NMsize cellsize; // cellsize (x,y,z)
    // for the following it is assumed that the image is north oriented
    double xmin;        // minimum x coordinate (real world coordinate)
    // -> x coord. of the left edge of the leftmost pixel
    double xmax;        // maximum ...
    // -> x coord. of the fight edge of the rightmost pixel
    double ymax;        // maximum y coordinate (real world coordinate)
    // -> y coord. of the upper edge of the uppermost pixel
    double ymin;        // minimum ....
    // -> y coord. of the lower edge of the lowermost pixel
    double zmin;
    double zmax;

    // some statistics if avaialable
    double stats_max;
    double stats_min;
    double stats_mean;
    double stats_stddev;

    int epsg_code;  // epsg coordinate reference system code
    std::vector<std::string> crs_uris;   // string representation of the coordinate reference system
    std::vector<int> crs_order;             // z-axis order (default: xyz = 0:1:2)
    bool isRegular;                         // indicates whether the image is rectified (true) or referencable (false)
    r_Type::r_Type_Id rmantype; // the rasdaman pixel type of the image
    GDALDataType gdaltype;          // the gdal pixel type of the image
    int nbands;         // number of bands of the image
    bool rat_avail; // denotes whether a raster attribute table is available (true) or not (false)
};

int getDirContent(string path, string suffix, vector<string>&);
bool hasSuffix(string name, string suffix);
void readImageInformation(vector<string>& vnames, Header& header,
                          vector<double>& bnd, vector<string>& vvalidtiles,
                          bool b3D, double cellsizez);
bool readTileInformation(string filename, Header& header);
bool readTileInformation(GDALDataset* pDs, Header& header);
void printImageInformation(Header& header, RasdamanHelper2& helper);
void resetHeader(Header& header);
r_Type::r_Type_Id getRmanDataType(GDALDataType type);
bool tileOverlaps(Header& header, vector<double>& bnd);
bool parseCoordinateString(string bndstr, vector<double>& bnd);

bool checkCRSOrderSequence(std::vector<double>& sequence, std::vector<int>& order);
bool checkZCoords(std::vector<double>& coords);

void getMetaURIs(Header& header, RasdamanHelper2& helper, bool b3D);
void crash_handler ( int sig,  siginfo_t* info, void * ucontext);

/*! parses a colon (':') separated sequence of strings and appends them to the vector 'items';
 *  \param sequence string to be parsed
 *  \param items string taking successully parsed substrings
 *  \param nelem a value of nelem > 0 specifies the minimum number of expected items to be found;
 *  \param vesp specifies 'validator' strings; i.e. the colon separator is only considered to be
 *              valid, if a 'validator' string follows immediately
 */
bool parseStringSequence(const std::string& sequence,
        std::vector<std::string>& items, int nelem,
        const std::vector<std::string>& vsep);
void showHelp();

int importImage(RasdamanHelper2& helper, GDALDataset* pDs, const string& collname, vector<double>& oids,
                r_Minterval& readGDALImgDOM, r_Point& writeShift, Header& newGeoRegion,
                bool asCube, const string& marraytypename, const string& tiling, const string& coveragename,
                double zcoord, const vector<bool>& axisIndexed);

int processImageFiles(vector<string>& filenames, const string& collname, vector<double>& oids,
                      Header& processRegion, const string& mode3D, r_Point& shiftPt, RasdamanHelper2& helper,
                      const string& marraytypename, const string& tiling, const string& coveragename,
                      const vector<double>& zcoords, const vector<bool>& axisIndexed);

void intersectRegions2D(Header& inoutRegion, Header& intersectRegion);
void intersectRegions2D(Header& inoutRegion, std::vector<double>& intersectRegions);
void unionRegions2D(Header& inoutRegion, Header& unionRegion);
void unionRegions2D(Header& inoutRegion, std::vector<double>& unionRegion);
void copyRegion2D(Header& outRegion, Header& inRegion);
void copyRegion2D(Header& outRegion, std::vector<double>& inRegion);

// DEBUG
void printRegion(Header& reg, string descr);
void printRegion(std::vector<double>& sdom, string descr);

#endif // __rasimport_hh
