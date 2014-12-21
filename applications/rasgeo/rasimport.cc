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

// general includes
#include "config.h"
#include <cstdlib>
#include <cstdio>
#include <iostream>
#include <iomanip>
#include <string>
#include <ctime>
#include <cmath>
#include "dirent.h"
#include <vector>
#include "errno.h"
#include "sys/types.h"
#include "unistd.h"
#include <limits>
#include <algorithm>

#include "raslib/commonutil.hh"


#include "rasimport.hh"
#include "include/globals.hh"

#define DEBUG_MAIN
#include "debug-clt.hh"

#include "common/src/logging/easylogging++.hh"

using namespace std;

// global string constants
const string ctx = "rasimport::";

#define CRS_RESOLVER_PREFIX "%SECORE_URL%"

// ---------------------------------------------------- HELPERs ----------

int
getDirContent(string path, string suffix, vector<string>& names)
{
    DIR *dp;
    struct dirent *dirp;
    if ((dp = opendir(path.c_str())) == NULL)
    {
        cerr << ctx << "::getDirContent(): "
        << "could not read directory '" << path << "'!"
        << endl;
        return 0;
    }

    string filename;
    while ((dirp = readdir(dp)) != NULL)
    {
        //skip directories
        if (dirp->d_type == DT_DIR)
            continue;

        if (hasSuffix(dirp->d_name, suffix))
        {
            filename = path + "/";
            filename += dirp->d_name;
            if (access(filename.c_str(), R_OK) == 0)
            {
                names.push_back(filename);
            }
            else
            {
                cerr << ctx << "getDirContent(): "
                << "file access failure - skip image '"
                << filename << "'!" << endl;
                continue;
            }
        }
    }
    closedir(dp);
    return 1;
}

bool
hasSuffix(string name, string suffix)
{
    string::size_type dpos = name.find_last_of(".", name.length() - 1);
    if (dpos == string::npos)
        return false;

    if (suffix.compare("*") == 0 || suffix.empty())
        return true;

    string test = name.substr(dpos + 1, name.length() - dpos + 1);
    string given = suffix;

    transform(test.begin(), test.end(), test.begin(), ::tolower);
    transform(given.begin(), given.end(), given.begin(), ::tolower);

    return given.compare(test) == 0 ? true : false;
}

void
readImageInformation(vector<string>& vnames, Header& header, vector<double>& bnd,
                     vector<string>& vvalidtiles, bool b3D, double cellsizez)
{
    ENTER(ctx << "readImageInformation()");
    Header tileHeader;
    vector<string>::const_iterator iter;

    int numfiles = 0;
    for (iter = vnames.begin(); iter != vnames.end(); iter++)
    {
        resetHeader(tileHeader);
        if (!readTileInformation(*iter, tileHeader))
        {
            cerr << ctx << "readImageInformation(): "
            << "read tile error: skipped file '" << (*iter)
            << "'!" << endl;
            continue;
        }

        // filter tiles
        if (bnd.size() >= 4)
        {
            if (!tileOverlaps(tileHeader, bnd))
                continue;
        }
        vvalidtiles.push_back(*iter);

        // if we get a set of files, we assume that they share the
        // same data type, cell size, number of bands, and spatial
        // reference system, so we only read this info from the
        // first of all given files
        if (numfiles == 0)
        {
            if (tileHeader.crs_uris.size() == 1)
            {
                if (header.crs_uris.size() == 0)
                    header.crs_uris.push_back(tileHeader.crs_uris[0]);
            }
            header.nbands = tileHeader.nbands;

            header.cellsize.x = tileHeader.cellsize.x;
            header.cellsize.y = tileHeader.cellsize.y;
            header.rmantype = tileHeader.rmantype;
            header.gdaltype = tileHeader.gdaltype;
        }

        unionRegions2D(header, tileHeader);

        // statistics: we can only take over min and max
        if (tileHeader.stats_min < header.stats_min)
            header.stats_min = tileHeader.stats_min;
        if (tileHeader.stats_max > header.stats_max)
            header.stats_max = tileHeader.stats_max;

        numfiles++;
    }

    if (numfiles > 0)
    {

        // update info we only use, when we import a single file
        if (numfiles == 1)
        {
            header.stats_mean = tileHeader.stats_mean;
            header.stats_stddev = tileHeader.stats_stddev;
        }

        // intersect joint file header with given import region (boundary)
        // and snap user boundary to closest pixel borders
        int pixoffset;
        if (bnd.size() >= 4)
        {
            if (bnd[0] > header.xmin)
            {
                pixoffset = ((bnd[0] - header.xmin) / header.cellsize.x) + 0.5;
                header.xmin = header.xmin + (header.cellsize.x * pixoffset);
            }

            if (bnd[1] < header.xmax)
            {
                pixoffset = ((bnd[1] - header.xmin) / header.cellsize.x) + 0.5;
                header.xmax = header.xmin + (header.cellsize.x * pixoffset);
            }

            if (bnd[2] > header.ymin)
            {
                pixoffset = ((bnd[2] - header.ymin) / header.cellsize.y) + 0.5;
                header.ymin = header.ymin + (header.cellsize.y * pixoffset);
            }

            if (bnd[3] < header.ymax)
            {
                pixoffset = ((bnd[3] - header.ymin) / header.cellsize.y) + 0.5;
                header.ymax = header.ymin + (header.cellsize.y * pixoffset);
            }
        }


        // update the origin
        header.origin.x = header.xmin; //deprecated: remove in due course
        header.origin.y = header.ymax; //dperecated: remove in due course
        header.ncols = ((header.xmax - header.xmin) / header.cellsize.x) + 0.5;
        header.nrows = ((header.ymax - header.ymin) / header.cellsize.y) + 0.5;

        if (b3D)
        {
            if (cellsizez > 0)
                header.cellsize.z = cellsizez;
            else
                header.cellsize.z = header.cellsize.x;

            // set the initial zmin coordinate (boundary) to 0;
            // note this might be overriden by the user specified
            // --shift value
            header.zmin = 0;

            header.origin.z = header.zmin; // deprecated: remove in due course
        }
    }

    TALK("rasimport successfully analysed " << numfiles << " files." << endl << endl);
    LEAVE(ctx << "readImageInformation()");

}

void
printImageInformation(Header& header, RasdamanHelper2& helper)
{
    cout.precision(15);
    cout << "Update Region:" << endl;
    cout << "-------------" << endl << endl;
    cout << "EPSG-code: " << header.epsg_code << endl;
    cout << "bands:     " << header.nbands << endl;
    cout << "data type: " << helper.getDataTypeString(header.rmantype) << endl;
    cout << "cellsize:  " << header.cellsize.x << ", " << header.cellsize.y
         << ", " << header.cellsize.z << endl;
    cout << "columns:   " << header.ncols << endl;
    cout << "rows:      " << header.nrows << endl;
    cout << "layers:    " << header.nlayers << endl;
    cout << "xmin:      " << header.xmin << endl;
    cout << "xmax:      " << header.xmax << endl;
    cout << "ymin:      " << header.ymin << endl;
    cout << "ymax:      " << header.ymax << endl;
    cout << "zmin:      " << header.zmin << endl;
    cout << "zmax:      " << header.zmax << endl;
    cout << endl;
}

void
resetHeader(Header& header)
{
    header.crs_uris.clear();
    header.crs_order.clear();
    header.isRegular = true;
    header.epsg_code = -1;
    header.origin.x = numeric_limits<double>::max();
    header.origin.y = numeric_limits<double>::max() * -1;
    header.origin.z = 0;
    header.xmin = numeric_limits<double>::max();
    header.xmax = numeric_limits<double>::max() * -1;
    header.ymin = numeric_limits<double>::max();
    header.ymax = numeric_limits<double>::max() * -1;
    header.zmin = numeric_limits<double>::max() * -1;
    header.zmax = numeric_limits<double>::max();
    header.cellsize.x = 1;
    header.cellsize.y = 1;
    header.cellsize.z = 0;
    header.ncols = -1;
    header.nrows = -1;
    header.nlayers = 1;
    header.stats_max = numeric_limits<double>::max() * -1;
    header.stats_min = numeric_limits<double>::max() * -1;
    header.stats_mean = numeric_limits<double>::max() * -1;
    header.stats_stddev = numeric_limits<double>::max() * -1;
    header.rmantype = r_Type::UNKNOWNTYPE;
    header.gdaltype = GDT_Unknown;
    header.nbands = 1;
    header.rat_avail = false;
}

r_Type::r_Type_Id
getRmanDataType(GDALDataType type)
{
    r_Type::r_Type_Id rtype;
    switch (type)
    {
    case GDT_Byte: //   Eight bit unsigned integer
        rtype = r_Type::CHAR;
        break;
    case GDT_UInt16: //     Sixteen bit unsigned integer
        rtype = r_Type::USHORT;
        break;
    case GDT_Int16: //  Sixteen bit signed integer
        rtype = r_Type::SHORT;
        break;
    case GDT_UInt32: //     Thirty two bit unsigned integer
        rtype = r_Type::ULONG;
        break;
    case GDT_Int32: //  Thirty two bit signed integer
        rtype = r_Type::LONG;
        break;
    case GDT_Float32: //    Thirty two bit floating point
        rtype = r_Type::FLOAT;
        break;
    case GDT_Float64: //    Sixty four bit floating point
        rtype = r_Type::DOUBLE;
        break;
    case GDT_CInt16: //     Complex Int16
    case GDT_CInt32: // Complex Int32
    case GDT_CFloat32: //   Complex Float32
    case GDT_CFloat64: //   Complex Float64
    case GDT_Unknown: //  Unknown or unspecified type
    default:
        rtype = r_Type::UNKNOWNTYPE;
        break;
    }

    return rtype;
}

bool readTileInformation(string filename, Header& header)
{
    ENTER(ctx << "readTileInformation()");
    GDALDataset* pDs = (GDALDataset*)GDALOpen(filename.c_str(), GA_ReadOnly);

    if (pDs == 0)
        return false;

    TALK("analysing file '" << filename << "' ("
        << pDs->GetDriver()->GetDescription() << "/" <<
        pDs->GetDriver()->GetMetadataItem(GDAL_DMD_LONGNAME) << ")");

    readTileInformation(pDs, header);

    GDALClose(pDs);
    LEAVE(ctx << "readTileInformation()");
    return true;
}

bool
readTileInformation(GDALDataset* pDS, Header& header)
{
    double xmin;
    double xmax;
    double ymin;
    double ymax;
    double czx;
    double czy;

    // get image information
    double affine[6];
    pDS->GetGeoTransform(affine);
    double pixelwidth = affine[1] < 0 ? affine[1] * -1 : affine[1];
    double pixelheight = affine[5] < 0 ? affine[5] * -1 : affine[5];

    // TODO: parse WKT crs description and extract the outer most
    // EPSG code from the authority parameter
    header.crs_uris.push_back(pDS->GetProjectionRef());
    header.nbands = pDS->GetRasterCount();
    header.ncols = pDS->GetRasterXSize();
    header.nrows = pDS->GetRasterYSize();

    // derive tile information
    xmin = affine[0];
    xmax = affine[0] + (pixelwidth * header.ncols);
    ymin = affine[3] - (pixelheight * header.nrows);
    ymax = affine[3];
    czx = pixelwidth;//(xmax - xmin) / (float) header.ncols;
    czy = pixelheight;//(ymax - ymin) / (float) header.nrows;

    // get some statistics information
    pDS->GetRasterBand(1)->GetStatistics(0, 0,
                                         &header.stats_min, &header.stats_max, &header.stats_mean,
                                         &header.stats_stddev);

    // update header information
    header.cellsize.x = czx;
    header.cellsize.y = czy;
    header.xmin = xmin;
    header.xmax = xmax;
    header.ymin = ymin;
    header.ymax = ymax;
    header.gdaltype = pDS->GetRasterBand(1)->GetRasterDataType();
    header.rmantype = getRmanDataType(header.gdaltype);
    header.origin.x = header.xmin;
    header.origin.y = header.ymax;

    return true;
}

void intersectRegions2D(Header& inoutRegion, Header& intersectRegion)
{
    Header& io = inoutRegion;
    Header& s = intersectRegion;

    io.xmin = io.xmin > s.xmin ? io.xmin : s.xmin;
    io.xmax = io.xmax < s.xmax ? io.xmax : s.xmax;
    io.ymin = io.ymin > s.ymin ? io.ymin : s.ymin;
    io.ymax = io.ymax < s.ymax ? io.ymax : s.ymax;

    io.ncols = ((io.xmax - io.xmin) / intersectRegion.cellsize.x) + 0.5;
    io.nrows = ((io.ymax - io.ymin) / intersectRegion.cellsize.y) + 0.5;
}

void intersectRegions2D(Header& inoutRegion, vector<double>& intersectRegion)
{
    Header& io = inoutRegion;
    vector<double>& s = intersectRegion;

    io.xmin = io.xmin > s[1] ? io.xmin : s[1];
    io.xmax = io.xmax < s[2] ? io.xmax : s[2];
    io.ymin = io.ymin > s[3] ? io.ymin : s[3];
    io.ymax = io.ymax < s[4] ? io.ymax : s[4];

}

void unionRegions2D(Header& inoutRegion, Header& unionRegion)
{
    Header& io = inoutRegion;
    Header& u = unionRegion;

    io.xmin = io.xmin < u.xmin ? io.xmin : u.xmin;
    io.xmax = io.xmax > u.xmax ? io.xmax : u.xmax;
    io.ymin = io.ymin < u.ymin ? io.ymin : u.ymin;
    io.ymax = io.ymax > u.ymax ? io.ymax : u.ymax;

    io.ncols = ((io.xmax - io.xmin) / unionRegion.cellsize.x) + 0.5;
    io.nrows = ((io.ymax - io.ymin) / unionRegion.cellsize.y) + 0.5;
}

void unionRegions2D(Header& inoutRegion, vector<double>& unionRegion)
{
    Header& io = inoutRegion;
    vector<double>& u = unionRegion;

    io.xmin = io.xmin < u[0] ? io.xmin : u[0];
    io.xmax = io.xmax > u[1] ? io.xmax : u[1];
    io.ymin = io.ymin < u[2] ? io.ymin : u[2];
    io.ymax = io.ymax > u[3] ? io.ymax : u[3];
}

void copyRegion2D(Header& outRegion, Header& inRegion)
{
    outRegion.xmin = inRegion.xmin;
    outRegion.xmax = inRegion.xmax;
    outRegion.ymin = inRegion.ymin;
    outRegion.ymax = inRegion.ymax;
}

void copyRegion2D(Header& outRegion, vector<double>& inRegion)
{
    outRegion.xmin = inRegion[0];
    outRegion.xmax = inRegion[1];
    outRegion.ymin = inRegion[2];
    outRegion.ymax = inRegion[3];
}

bool checkCRSOrderSequence(vector<double>& sequence, vector<int>& order)
{
    // need at least two axes
    if (sequence.size() < 2)
        return false;

    // temp copy for maintaining user specified axis order
    vector<double> vtmp;
    for (int i=0; i < sequence.size(); ++i)
        vtmp.push_back(sequence[i]);

    // check for proper sequence
    sort(vtmp.begin(), vtmp.end());

    // sequence has to be zero-based
    bool bsound = vtmp[0] == 0 ? true : false;
    int pos = 0;
    while (pos < vtmp.size()-1 && bsound)
    {
        if (vtmp[pos] + 1 != vtmp[pos+1])
            bsound = false;
        ++pos;
    }

    if (!bsound)
        return false;

    order.clear();
    for (int t=0; t < sequence.size(); ++t)
        order.push_back(static_cast<int>(sequence[t]));

    return true;
}

bool checkZCoords(std::vector<double>& coords)
{
    bool inorder = true;
    int pos = 0;
    while (pos < coords.size()-1 && inorder)
    {
        if (coords[pos+1] <= coords[pos])
            inorder = false;
        ++pos;
    }

    return inorder;
}

int
processImageFiles(vector<string>& filenames, const string& collname,
                  vector<double>& oids, Header& processRegion,
                  const string& mode3D, r_Point& shiftPt, RasdamanHelper2& helper,
                  const string& marraytypename, const string& tiling,
                  const string& coveragename, const vector<double>& zcoords,
                  const vector<bool>& axisIndexed)
{
    /* PROCEDURE
     * - read source geospatial region (srcGeoRegion)
     * - intersect with user specified boundary (i.e. processRegion) -> insertGeoRegion
     * - determine the source region in image (pixel) space to be read -> readGDALImgDOM
     * - shift the insertGeoRegion by the user specified shift vector (shiftPt)
     * - determine the appropriate shift vector (writeShift) for writing the source region into the
     *   rasdaman data base (only applies when in update mode)
     * - in 3D mode: determine the new z-value (for regularly spaced grids) depending on the specified parameter
     *   (i.e. top or bottom), the user specified shift vector, and whether we're in update
     *   mode or not
     * - for irregularly spaced grids along the z-axis, we determine whether we can append the given
     *   z-coordinate, if not we quit (note: we ever only append due to ps constraints and that 'cause don't
     *   want the hassle of shifting part of the image)
     * - import the image (importImage deals with sequential and band processing)
     * - write RATs
     * - write petascope meta data
     */


    ENTER(ctx << "processImageFiles()");

    bool b3D = (   !mode3D.empty()
                || shiftPt.dimension() == 3
                || zcoords.size() > 0
               ) ? true : false;

    Header newGeoRegion;

    long tilecounter = 1;
    vector<string>::const_iterator iter;
    for (iter = filenames.begin(); iter != filenames.end(); ++iter, ++tilecounter)
    {
        bool bUpdate = oids.size() == 0 ? false : true;

        GDALDataset* pDs = (GDALDataset*)GDALOpen((*iter).c_str(), GA_ReadOnly);
        if (pDs == 0)
        {
            cerr << ctx << "processImageFiles(): "
            << "failed opening data set '" << *iter << "'!"
            << endl;
            LEAVE(ctx << "processImageFiles()");
            return 0;
        }

        Header srcGeoRegion;
        resetHeader(srcGeoRegion);
        readTileInformation(pDs, srcGeoRegion);

        // are we in pixel space for xy?
        // -> then we prevent any 2d shifting by making the process region
        // equal to the source region of the current image
        if (axisIndexed[0] || axisIndexed[1])
        {
		    TALK("xy coordinate domain defined by index CRS!");
		    copyRegion2D(processRegion, srcGeoRegion);
		    // need to reset the cellsize here manually,
		    // to reinstall any possibly previously set different
		    // cellsizes (i.e. while processing the first tile)
		    processRegion.cellsize.x = srcGeoRegion.cellsize.x;
		    processRegion.cellsize.y = srcGeoRegion.cellsize.y;
        }

        printRegion(srcGeoRegion, "srcGeoRegion");
        printRegion(processRegion, "processRegion");

        Header insertGeoRegion;
        resetHeader(insertGeoRegion);
        copyRegion2D(insertGeoRegion, srcGeoRegion);

        intersectRegions2D(insertGeoRegion, processRegion);

        printRegion(insertGeoRegion, "insertGeoRegion");

        // determine the image region to be read from the src in pixel space
        r_Range read_scol = ((insertGeoRegion.xmin - srcGeoRegion.xmin) / srcGeoRegion.cellsize.x) + 0.5;
        r_Range read_srow = ((srcGeoRegion.ymax - insertGeoRegion.ymax) / srcGeoRegion.cellsize.y) + 0.5;
        r_Minterval readGDALImgDOM(2);
        readGDALImgDOM  << r_Sinterval(read_scol, (r_Range)(read_scol + insertGeoRegion.ncols - 1))
                        << r_Sinterval(read_srow, (r_Range)(read_srow + insertGeoRegion.nrows - 1));

        // when we fall back to indexed crs, we have to make sure
        // to operate in pixel space (0:ncols-1,0:nrows-1,0)
		if (axisIndexed[0] || axisIndexed[1])
		{
			insertGeoRegion.xmin = 0;
			insertGeoRegion.xmax = readGDALImgDOM[0].get_extent() - 1;
			insertGeoRegion.ymin = 0;
			insertGeoRegion.ymax = readGDALImgDOM[1].get_extent() - 1;
			insertGeoRegion.cellsize.x = 1;
			insertGeoRegion.cellsize.y = -1;

			// we use this to shift the insert region later, so adapt as well
			processRegion.cellsize.x = 1;
			processRegion.cellsize.y = -1;

			printRegion(insertGeoRegion, "XY insertGeoRegion converted to pixel space (index CRS)");
		}

		// get the current rasdaman pixel domain; need this for calculation of
        // pixel shift; in case of indexed 3D AND --shift specified, we have to
		// adjust the shiftPt[2] value accordingly (depending on top or bottom)
        r_Minterval aint;
        if (bUpdate)
        {
		aint = helper.getImageSdom(collname, oids[0]);
        }

		if (axisIndexed[2] && processRegion.isRegular)
		{
			insertGeoRegion.cellsize.z = 1;
			processRegion.cellsize.z = 1;
			if (bUpdate && shiftPt.dimension() == 3)
			{
				if (mode3D == "top")
				{
					shiftPt[2] = aint[2].high() + 1;
				}
				else if (mode3D == "bottom")
				{
					shiftPt[2] = aint[2].low() - 1;
				}
			}
		}

        TALK("src img size:     " << srcGeoRegion.ncols << " x " << srcGeoRegion.nrows);
        TALK("readGDALImgDOM:   " << readGDALImgDOM.get_string_representation() << endl);

        // shift the insertGeoRegion by the user specified shiftVector; account for (geo) spatial
        // image domains with negative values: positive shift (e.g. 150:200) shifts to the north east
        // and negative shift values, e.g. -150:-200, shift to the south west (applies to most
        // projected coordinate reference systems)
        if (shiftPt.dimension() >= 2)
        {
            double xfactor = fabs(insertGeoRegion.xmin) > fabs(insertGeoRegion.xmax) ? -1 : 1;
            double yfactor = fabs(insertGeoRegion.ymin) > fabs(insertGeoRegion.ymax) ? -1 : 1;

			insertGeoRegion.xmin += (shiftPt[0] * xfactor * processRegion.cellsize.x);
			insertGeoRegion.xmax += (shiftPt[0] * xfactor * processRegion.cellsize.x);
			insertGeoRegion.ymin += (shiftPt[1] * yfactor * processRegion.cellsize.y);
			insertGeoRegion.ymax += (shiftPt[1] * yfactor * processRegion.cellsize.y);
        }

        printRegion(insertGeoRegion, "shifted insertGeoRegion");

        // determine the shift vector (relative to the current (present) image domain)
        // to apply while writing the src image into the rasdaman data base
        // -> retrieve geospatial domain in default xyz axis order from petascope
        vector<double> isdom = helper.getMetaGeoDomain(oids.size() >= 1 ? oids[0] : -1);
        printRegion(isdom, "current image petascope geo domain");

        resetHeader(newGeoRegion);
        copyRegion2D(newGeoRegion, insertGeoRegion);

        // we only do shifts in meaningful coordinate space, not with index CRS!
        if (!axisIndexed[0] && !axisIndexed[1])
        {
		    unionRegions2D(newGeoRegion, isdom);
        }

        /////////////////////////////////////////////////////////////////////////////////////
        // calc the write shift (pixel domain) to put the source in the right place of
        // the rasdaman target array

        r_Point writeShift;
        int zshift = 0;
        double minZ = 0;
        double maxZ = 0;
        double zCoeff = 0;
        // ++++++++++++++++++++++++++++++++++++ UPDATE +++++++++++++++++++++++++++++++++++++++++
        if (bUpdate)
        {
            // calc coordinate shift in x- and y- direction;
            // prepare rounding to whole pixels by adding or subtracting 0.5
            double xshift = (insertGeoRegion.xmin - isdom[0]) / processRegion.cellsize.x;
            xshift = xshift > 0 ? xshift + 0.5 : xshift - 0.5;

            double yshift = (isdom[3] - insertGeoRegion.ymax) / processRegion.cellsize.y;
            yshift = yshift > 0 ? yshift + 0.5 : yshift - 0.5;


            if (axisIndexed[0] || axisIndexed[1])
            {
		        xshift = 0;
		        yshift = 0;
            }

            TALK("current img sdom: " << aint.get_string_representation());

            // ================================== 3D UPDATE ======================================
            if (b3D)
            {
		        // -------------------------- IRREGULAR  3D UPDATE --------------------------------
                // if we're processing an irregular z-axis
                if (!processRegion.isRegular && zcoords.size() > 0)
                {
                    long idx = -1;
                    if (tilecounter >=1 && tilecounter <= zcoords.size())
                    {
						std::vector<double> vcsz = helper.getMetaCellSize(oids[0], true);
						if (vcsz.size() < 2)
						{
							cerr << ctx << "processImageFiles(): "
							<< "Couldn't find z-axis offset for irregularly spaced image cube! Abort."
							<< endl;
							LEAVE(ctx << "processImageFiles()");
							return 0;
						}
                        zCoeff = (zcoords[tilecounter-1]-isdom[4]) / vcsz[2];
                        idx = helper.canAppendReferencedZCoeff(oids[0], zCoeff);

                        TALK("z-coeff=(" << zcoords[tilecounter-1] << " - " << isdom[4] << ") / " << vcsz[2]
                                 << " = "<< zCoeff << " -> vector_coefficients_idx=" << idx);
                    }
                    else
                    {
                        cerr << ctx << "processImageFiles(): "
                        << "number of input files and specified z-coordinates don't match!"
                        << endl;
                        LEAVE(ctx << "processImageFiles()");
                        return 0;
                    }

                    // check, whether suggested new idx (coefficient_order) makes sense;
                    // note: negative idx returned from RasdamanHelper2::canAppendReferencedZCoeff
                    // indicate that zCoeff is not strictly increasing with respect to available zCoeffs
                    if (idx >= 0 && idx == aint[2].high()+1)
                        zshift = idx;
                    else
                    {
                        cerr << ctx << "processImageFiles(): "
                        << "cannot append z-coordinate: " << zcoords[tilecounter-1] << " !"
                        << endl;
                        LEAVE(ctx << "processImageFiles()");
                        return 0;
                    }
                }
                // ------------------------ REGULAR 3D UPDATE --------------------------------
                else
                {
					// if we've got a z-shift given, we always shift relative to
					// z = 0
					if (shiftPt.dimension() == 3)
					{
						if (mode3D == "top")
							zshift = shiftPt[2] + tilecounter - 1;
						else if (mode3D == "bottom")
							zshift = shiftPt[2] - (tilecounter - 1);
					}
					else
					{
						if (mode3D == "top")
							zshift = aint[2].high() + 1;
						else if (mode3D == "bottom")
							zshift = aint[2].low() - 1;
					}
                }

                writeShift = r_Point(3) << (aint[0].low() + xshift) << (aint[1].low() + yshift)
							 << zshift;

            }
            // ============================== 2D UPDATE =========================================
            // is always regular
            else
            {
                writeShift = r_Point(2)
                             << aint[0].low() + xshift
                             << aint[1].low() + yshift;
            }
        }
        // ++++++++++++++++++++++++++++++++++++ FIRST TIME INSERTION/CREATION +++++++++++++++++++
        else
        {
		    // ============================== 3D INSERTION ========================================
            if (b3D)
            {
		        // ------------------------- IRREGULAR 3D INSERTION -------------------------------
                if (!processRegion.isRegular)
                {
                    // we ever only append
			        zshift = 0;
			        //zCoeff = zcoords[0]-zcoords[0] / processRegion.cellsize.z;
                }
                // ------------------------- REGULAR 3D INSERTION ---------------------------------
                else
                {
					// if we've got a z-shift given, we always shift relative to
					// z = 0
					if (shiftPt.dimension() == 3)
					{
						if (mode3D == "top")
							zshift = shiftPt[2] + tilecounter - 1;
						else if (mode3D == "bottom")
							zshift = shiftPt[2] - (tilecounter - 1);
					}
					else
					{
						if (mode3D == "top")
							zshift = tilecounter -1;
						else if (mode3D == "bottom")
							zshift = -1 * (tilecounter -1);
					}
                }

                writeShift = r_Point(3) << 0 << 0 << zshift;
            }
            // ====================================== 2D INSERTION =====================================
            // 2d is always regular
            else
            {
                writeShift = r_Point(2) << 0 << 0;
            }
        }
        // ////////////////////////////////////////////////////////////////////////////////////////////

        // calculate z-boundary for current layer (i.e. single voxel layer)
        if (b3D)
        {
			if (processRegion.isRegular)
			{
				if (bUpdate)
				{
					// rounding issues might occur when user_shift % cellsize_z != 0;
					// => zshift has been snapped to the nearest pixel, so here, we calc
					// the relative distance to origin in pixel domain to calc the boundary
					// for this image slice
					int diffshift = zshift - aint[2].low();
					TALK("pixel shift relative to origin for import slice: " << diffshift);

					minZ = isdom[4] + (diffshift * processRegion.cellsize.z); //(zshift * processRegion.cellsize.z);
					maxZ = minZ + processRegion.cellsize.z;
				}
				// upon import of first slice, we use specified shift parameter, if any
				else
				{
					minZ = processRegion.zmin;
					maxZ = minZ + processRegion.cellsize.z;
				}
			}
			// first imported z-coordinate determines the origin of the irregular z-axis
			else
			{
				if (!bUpdate)
				{
					minZ = maxZ = zcoords[0];
				}
				else
				{
					if (zcoords.size() > 0 && tilecounter-1 <= zcoords.size())
					{
						minZ = isdom[4];
						maxZ = zcoords[tilecounter-1];
					}
				}
			}
        }

        TALK("writeShift: " << writeShift.get_string_representation() << endl);

        // determine the new image region (valid after the src has been written into
        // the data base) by union the present image region with the region to
        // be inserted; furthermore copy all other info from the processRegion into
        // the newGeoRegion

        // determine z min and max
        if (b3D || shiftPt.dimension() == 3)
        {
            if (   minZ < newGeoRegion.zmin || minZ > newGeoRegion.zmax
                || maxZ < newGeoRegion.zmin || maxZ > newGeoRegion.zmax
               )
            {
                TALK("import layer outside z boundary, abortion!");
                continue;
            }

            if (!bUpdate)
            {
                newGeoRegion.zmin = minZ;
                newGeoRegion.zmax = maxZ;
                TALK("inserting 1st 3D slice at zcoord: " << minZ);
            }
            else
            {
                TALK("inserting 2nd+ 3D slice | minZ: " << minZ);
                newGeoRegion.zmin = (minZ < isdom[4]) ? minZ : isdom[4];
                TALK("new zmin: " << newGeoRegion.zmin);

                newGeoRegion.zmax = (maxZ > isdom[5]) ? maxZ : isdom[5];
                TALK("new zmax: " << newGeoRegion.zmax);
            }
        }

        newGeoRegion.cellsize.x = processRegion.cellsize.x;
        newGeoRegion.cellsize.y = processRegion.cellsize.y;
        newGeoRegion.cellsize.z = processRegion.cellsize.z;
        newGeoRegion.ncols = ((newGeoRegion.xmax - newGeoRegion.xmin) / newGeoRegion.cellsize.x) + 0.5;
        newGeoRegion.nrows = ((newGeoRegion.ymax - newGeoRegion.ymin) / newGeoRegion.cellsize.y) + 0.5;
        newGeoRegion.nlayers = ((newGeoRegion.zmax - newGeoRegion.zmin) / newGeoRegion.cellsize.z) + 0.5;
        newGeoRegion.stats_max = processRegion.stats_max;		// deprecated, are never used
        newGeoRegion.stats_min = processRegion.stats_min;       // deprecated, are never used
        newGeoRegion.stats_mean = processRegion.stats_mean;     // deprecated, are never used
        newGeoRegion.stats_stddev = processRegion.stats_stddev; // deprecated, are never used
        newGeoRegion.epsg_code = processRegion.epsg_code;
        newGeoRegion.crs_uris = processRegion.crs_uris;
        newGeoRegion.rmantype = processRegion.rmantype;
        newGeoRegion.gdaltype = srcGeoRegion.gdaltype;
        newGeoRegion.nbands = processRegion.nbands;
        newGeoRegion.isRegular = processRegion.isRegular;
        newGeoRegion.crs_order = processRegion.crs_order;

        printRegion(newGeoRegion, "newGeoRegion");

        importImage(helper, pDs, collname, oids, readGDALImgDOM, writeShift,
                    newGeoRegion, b3D, marraytypename, tiling, coveragename,
                    zCoeff, axisIndexed);

        // release data set
        GDALClose(pDs);

        // since we don't support merging of RATs, we stay on the safe side and
        // don't import RAT at all in mosaicing mode
        if (filenames.size() == 1 && !bUpdate)
        {
            // one image per band -> oids index = band index -1
            if (oids.size() > 1)
            {
                for (int v=0; v < oids.size(); ++v)
                {
                    helper.writeRAT(*iter, oids[v], v+1);
                }
            }
            // one image, possibly multiple bands ...
            else
            {
                for (int b=1; b <= processRegion.nbands; ++b)
                {
                    helper.writeRAT(*iter, oids[0], b);
                }
            }
        }
    }

    LEAVE(ctx << "processImageFiles()");
    return 1;
}

int importImage(RasdamanHelper2& helper, GDALDataset* pDs,
                const string& collname, vector<double>& oids,
                r_Minterval& readGDALImgDOM, r_Point& writeShift,
                Header& newGeoRegion, bool asCube,
                const string& marraytypename, const string& tiling,
                const string& coveragename, double irregularZ,
                const vector<bool>& axisIndexed)
{
    ENTER(ctx << "importImage()");

    // get the pixel's data type length (in bytes)
    unsigned int pixelsize = helper.getTypeSize(newGeoRegion.rmantype);

    // if we've got a struct type specified, we adjust the pixelsize, because we're going
    // to process all bands at the same time
    if (!marraytypename.empty())
        pixelsize *= newGeoRegion.nbands;

    // determine parameters for sequential processing
    unsigned long chunksize = helper.getMaxImgSize() /
                              (readGDALImgDOM[0].get_extent() * pixelsize);
    if (chunksize > readGDALImgDOM[1].get_extent())
        chunksize = readGDALImgDOM[1].get_extent();

    // prepare sequential processing variables
    unsigned long niter = chunksize == 0 ? readGDALImgDOM[1].get_extent() : readGDALImgDOM[1].get_extent() / chunksize;
    unsigned long rest = readGDALImgDOM[1].get_extent() - (niter * chunksize);
    unsigned long imgsize_bytes = pixelsize * readGDALImgDOM[0].get_extent() * readGDALImgDOM[1].get_extent();
    double imgsize_mib = (imgsize_bytes /  (1024.0 * 1024.0));

    // some debug output
    TALK("...size of import region: " << readGDALImgDOM[0].get_extent() << "x" << readGDALImgDOM[1].get_extent()
               << "x" << pixelsize << " = " << imgsize_mib << " MiB");
    TALK("...processing scheme: " << niter << " x " << chunksize << " + " << rest
               << " rows");

    // process individual bands
    for (unsigned int b=1; b <= newGeoRegion.nbands; ++b)
    {
        // prepare a sequentially updated writeShift vector accounting for row-wise input
        r_Point seqWriteShift = r_Point(writeShift);

        // calc the sequential read variables
        r_Range startcolumn = readGDALImgDOM[0].low();
        r_Range startrow = readGDALImgDOM[1].low();
        r_Range endrow = startrow + chunksize -1;
        r_Range rowstoread = chunksize;

        for (r_Range iter=0; iter <= niter; iter++)
        {
            TALK("importing chunk " << iter+1 << " of " << (rest > 0 ? niter+1 : niter)
                 << ": row " << seqWriteShift[1] << " to " << seqWriteShift[1] + rowstoread-1 << endl);

            // create the interval object for writing the image buffer to rasdaman
            r_Minterval rint;
            if (asCube)
            {
                rint = r_Minterval(3) << r_Sinterval((r_Range)0, (r_Range)readGDALImgDOM[0].get_extent()-1)
                       << r_Sinterval((r_Range)0, (r_Range)rowstoread-1)
                       << r_Sinterval((r_Range)0, (r_Range)0);
            }
            else
            {
                rint = r_Minterval(2) << r_Sinterval((r_Range)0, (r_Range)readGDALImgDOM[0].get_extent()-1)
                       << r_Sinterval((r_Range)0, (r_Range)rowstoread-1);
            }

            // allocate the memory for the 'transfer buffer'
            void* gdalbuf = CPLMalloc(pixelsize * readGDALImgDOM[0].get_extent() * rowstoread);
            if (gdalbuf == NULL)
            {
                cerr << ctx << "importImage(): "
                << "memory allocation failed!" << endl;
                GDALClose(pDs);
                LEAVE(ctx << "importImage()");
                return 0;
            }

            // depending whether the pixel type is composite or not, we pass
            // different parameters
            // --> right here: multi-band image gets imported into different individual images
            if (marraytypename.empty())
            {
                // read image buffer from file
                GDALRasterBand* pBand = pDs->GetRasterBand(b);
                pBand->RasterIO(GF_Read, startcolumn, startrow, readGDALImgDOM[0].get_extent(), rowstoread,
                                gdalbuf, readGDALImgDOM[0].get_extent(), rowstoread, newGeoRegion.gdaltype, 0, 0);

                if (iter == 0 && oids.size() < b)
                    oids.push_back(helper.insertImage(collname, (char*)gdalbuf, seqWriteShift, rint, true,
                                                      marraytypename, tiling));
                else
                    helper.updateImage(collname, oids[b-1], (char*)gdalbuf, seqWriteShift, rint, true,
                                       marraytypename);
            }
            // --> here: we import a multi-band image as image with a composite pixel type
            else
            {
                // calc offset parameters to match interleave type
                int linesize = readGDALImgDOM[0].get_extent() * pixelsize;
                int bandoffset = pixelsize / newGeoRegion.nbands; // -> INTERLEAVE_PIXEL
                // read image buffer from file
                pDs->RasterIO(GF_Read, startcolumn, startrow, readGDALImgDOM[0].get_extent(), rowstoread,
                              gdalbuf, readGDALImgDOM[0].get_extent(), rowstoread, newGeoRegion.gdaltype,
                              newGeoRegion.nbands, NULL, pixelsize, linesize, bandoffset);

                if (iter == 0 && oids.size() == 0)
                {
                    oids.push_back(helper.insertImage(collname, (char*)gdalbuf, seqWriteShift, rint, true,
                                                      marraytypename, tiling));
                }
                else
                {
                    helper.updateImage(collname, oids.at(0), (char*)gdalbuf, seqWriteShift, rint, true,
                                       marraytypename);
                }

                // we don't need to iterate over any bands more, 'cause we've processed them all at once
                b = newGeoRegion.nbands + 1;
            }

            // release memory of the reading buffer
            free(gdalbuf);

            // adjust the shift vector by the just written number of rows;
            // (will be only effective while sequential processing
            seqWriteShift[1] += rowstoread;

            // calc new iteration vars for sequential buffer reading and writing
            startrow = endrow + 1;
            if (iter == niter-1)
            {
                if (rest > 0)
                {
                    endrow += rest;
                    rowstoread = rest;
                }
                else
                    break;
            }
            else
                endrow += chunksize;

        } // end sequential processing

        double procoid = oids[b-1];
        string nmdatatype = helper.getDataTypeString(newGeoRegion.rmantype);
        if (!marraytypename.empty())
        {
            procoid = oids.at(0);
            nmdatatype = marraytypename;
        }

        // note one assumptions here:
        // 1. we only ever append slices, we do not support
        //    insertions!
        long zidx = 0;
        if (asCube)
            zidx = writeShift[2];

        helper.writePSMetadata(
                procoid,
                collname,
                coveragename,
                newGeoRegion.crs_uris,
                newGeoRegion.crs_order,
                nmdatatype,
                newGeoRegion.xmin, newGeoRegion.xmax,
                newGeoRegion.ymin, newGeoRegion.ymax,
                newGeoRegion.zmin, newGeoRegion.zmax,
                newGeoRegion.cellsize.x,
                newGeoRegion.cellsize.y,
                newGeoRegion.cellsize.z,
                newGeoRegion.isRegular,
                zidx,
                irregularZ,
                axisIndexed);


    } // end band processing

    LEAVE(ctx << "importImage()");
    return 1;
}


bool
tileOverlaps(Header& header, vector<double>& bnd)
{
    // we only consider the 2D x/y case because GDAL only supports
    // 2D xy coordinates and all given regions are axis-parallel of course

    // layout of bnd
    // bnd[0] = xmin; bnd[1] = xmax;
    // bnd[2] = ymin; bnd[3] = ymax;
    // bnd[4] = zmin; bnd[5] = zmax;

    ENTER(ctx << "tileOverlaps()");

    TALK("testing the following regions ... ");
    printRegion(header, "input image:");
    printRegion(bnd, "user bnd:");

    bool xoverlap = false;
    bool yoverlap = false;

    if ( (header.xmin >= bnd[0] && bnd[1] >= header.xmin) ||
            (bnd[0] >= header.xmin && header.xmax >= bnd[0])    )
    {
        xoverlap = true;
    }

    if ( (header.ymin >= bnd[2] && bnd[3] >= header.ymin) ||
            (bnd[2] >= header.ymin && header.ymax >= bnd[2]))
    {
        yoverlap = true;
    }

    TALK("do regions overlap? -> " << ((xoverlap && yoverlap) ? "yes" : "no") << endl);

    LEAVE(ctx << "tileOverlaps()");
    return (xoverlap && yoverlap) ? true : false;
}

bool parseCoordinateString(string bndstr, vector<double>& bnd)
{
    size_t startpos = 0;
    size_t endpos = 0;
    string sub;
    while ((endpos = bndstr.find(':', startpos)) != string::npos)
    {
        sub = bndstr.substr(startpos, endpos-startpos);
        bnd.push_back(atof(sub.c_str()));
        startpos = endpos+1;
    }
    // get the last coordinate
    if (startpos != 0)
    {
        endpos = bndstr.size()-1;
        sub = bndstr.substr(startpos, endpos-startpos+1);
        bnd.push_back(atof(sub.c_str()));
    }
    else if (startpos == 0 && bndstr.size() != 0)
    {
        bnd.push_back(atof(bndstr.c_str()));
    }
    else
        return false;

    return true;
}


void printRegion(Header& reg, string descr)
{
#ifdef DEBUG
    if (debugOutput)
    {
        cout.precision(9);
        string s = "    ";

        cout << s << descr << endl;
        cout << s << reg.xmin << s << reg.xmax << endl;
        cout << s << reg.ymin << s << reg.ymax << endl;
        cout << s << reg.zmin << s << reg.zmax << endl;
        cout << endl;
    }
#endif
}

void printRegion(vector<double>& sdom, string descr)
{
#ifdef DEBUG
    if (debugOutput)
    {
        cout.precision(9);
        string s = "    ";

        cout << s << descr << endl;
        cout << s << sdom[0] << s << sdom[1] << endl;
        cout << s << sdom[2] << s << sdom[3] << endl;
        cout << s << sdom[4] << s << sdom[5] << endl;
        cout << endl;
    }
#endif
}



void showHelp()
{
    cout << endl << "rasimport v1.0" << endl << endl;

    cout << "Usage: rasimport {-f <image file name> | -d <image directory> "
              "[-s <tiff | img | jpeg | ... >]} --coll <collection name> "
              "[-t <ImageTypeName:CollectionTypeName>] "
              "[--conn <connection file>] [--3D <top | bottom> [--csz <z-axis cell size>]] "
              "[--bnd <xmin:xmax:ymin:ymax[:zmin:zmax]>] [--oid <local_image_OID[:local_image_OID[: ... ]]>] "
              "[--shift <x:y[:z]>] "
              "[--coverage-name <coverage name>] [--crs-order <2:0[:1[:...]]>] [--crs-uri <uri1[:uri2[:...]]>] "
              "[--geo-bbox <xmin:xmax:ymin:ymax[:zmin:zmax]>] "
              "[--metadata <key1=value1[:key2=value2[: ...]]>] "
              "[--z-coords <z1[:z2[:...]]>]" << endl << endl;

    cout << " -d    path pointing to image directory" << endl;
    cout << " -f    path to image file" << endl;
    cout << " -s    filter files in directory ('-d') by the given suffix; if omitted, all files are considered" << endl;
    cout << " -t    image and collection type (e.g. RGBImage:RGBSet)" << endl;
    cout << " --3D     mode for 2D (x/y) image slice import into (regularly spaced) 3D cubes" << endl;
    cout << " --bnd    spatial import boundary (i.e. sub-setting import file(s)) (e.g. xmin:xmax:ymin:ymax)" << endl;
    cout << " --coll   name of target rasdaman collection" << endl;
    cout << " --conn   connection file specifying rasdaman and postgres DB connection parameters" << endl;
    cout << " --csz    z-axis cell size; if omitted upon creation of regular cubes, rasimport assumes " << endl;
    cout << "          x-, y-, and z-cell sizes are identical; required upon creation of irregular cubes! " << endl;
    cout << " --oid    local object identifier(s) (OID(s)) specifying the target image(s) of an update operation" << endl;
    cout << " --shift  shifts the origin of the import image by the specified vector (e.g. x:y)" << endl;
    cout << " --tiling rasql-based specification of the tiling scheme to be applied to the imported data" << endl;
    cout << " --coverage-name  name under which the imported image is exposed as WCPS/WCS coverage" << endl;
    cout << " --crs-order      order in which CRSs are specified by the --crs-uri identifer(s)"<< endl;
    cout << "                  (e.g. '--crs-order 2:0:1' for axis order <z:x:y>)" << endl;
    cout << " --crs-uri        resolvable coordinate reference system (CRS) identifier(s)" << endl;
    cout << "                  (e.g. http://www.opengis.net/def/crs/OGC/0/Index2D)" << endl;
    cout << " --geo-bbox       geospatial boundary of the image (edges) (e.g. xmin:xmax:ymin:ymax)" << endl;
    cout << " --metadata       extra metadata to be stored with the coverage in the petascope database" << endl;
    cout << " --z-coords       (irregularly spaced) z-axis coordinate(s) of the 2D image slice(s) to be imported" << endl << endl;

    cout << "Note: Coordinates have to be given in the appropriate (geospatial) coordinate reference system of the image(s)!" << endl;

    cout << endl;
}

bool parseStringSequence(const string& sequence,
        vector<string>& items, int nelem,
        const vector<string>& vsep)
{
	size_t startpos = 0;
    size_t endpos = 0;
    string sub;
    string sep = ":";

    // does the sequence start with a valid substring?
    if (vsep.size() > 0)
    {
	startpos = sequence.size();
    }

    for (int s=0; s < vsep.size(); ++s)
    {
        size_t st = string::npos;
		if ((st = sequence.find(vsep[s])) != string::npos)
		{
			if (st < startpos)
				startpos = st;
		}
    }

    if (startpos == string::npos)
    {
	return false;
    }

    size_t itemstart = startpos;
    bool validsub = false;
    while ((endpos = sequence.find(sep, startpos)) != string::npos)
    {
        sub = sequence.substr(startpos, endpos-startpos);
        startpos = endpos + 1;

        string tmp = sequence.substr(endpos+1, sequence.size() - (endpos+1));

        for (int s=0; s < vsep.size(); ++s)
        {
		    if (tmp.find(vsep[s]) == (size_t)0)
		    {
			sub = sequence.substr(itemstart, endpos-itemstart);
			if (!sub.empty())
			{
				items.push_back(sub);
			}
		        itemstart = endpos+1;
		    }
        }

        if (vsep.size() == 0 && !sub.empty())
        {
		    items.push_back(sub);
        }
    }

    // if we account for 'validators' we have to adjust
    // the start position of the string
    if (vsep.size() > 0)
    {
	    startpos = itemstart;
    }

    // get the last item
    if (startpos != 0 && startpos != string::npos)
    {
        endpos = sequence.size()-1;
        sub = sequence.substr(startpos, endpos-startpos+1);
        if (!sub.empty())
        {
		items.push_back(sub);
        }
    }
    else if (startpos == 0 && sequence.size() != 0)
    {
        items.push_back(sequence);
    }
    else
        return false;

    // if nelem is meaningful, we evaluate, otherwise we just
    // claim everything was fine unless we've found at least one matching substring
    return nelem > 0 ? (items.size() == nelem ? true : false) : (items.size() > 0 ? true : false);
}

void getMetaURIs(Header& header, RasdamanHelper2& helper, bool b3D)
{
    string epsg;
    string uris;
    stringstream securi;
    securi << CRS_RESOLVER_PREFIX << "/crs/OGC/0/";
    if (header.crs_uris.size() > 0 && !header.crs_uris[0].empty())
    {

        uris = helper.getCRSURIfromWKT(header.crs_uris[0], b3D ? 3 : 2, epsg);

        if (epsg.empty())
        {
	        cout << "Couldn't find EPSG code in CRS WKT! Set CRS URI to:" << std::endl;
        }
    }
    else
    {
        cout << "Couldn't find any CRS info in file metadata! Set CRS URI to:" << std::endl;
        if (b3D)
            securi << "Index3D";
        else
            securi << "Index2D";

        uris = securi.str();

    }

    if (epsg.empty())
    {
           cout << uris << std::endl;
    }

    header.crs_uris.clear();
    header.crs_uris.push_back(uris);
}


void
crash_handler (int sig, siginfo_t* info, void * ucontext)
{

    ENTER( "crash_handler");


    print_stacktrace(ucontext);

    // clean up connection in case of segfault
    if (rasconn)
    {
        delete rasconn;
    }

    LEAVE("crash_handler");
    exit(SEGFAULT_EXIT_CODE);
}


// ----------------------------------------- MAIN ------------------------------------------------

#ifdef RMANRASNET
    _INITIALIZE_EASYLOGGINGPP
#endif

int
main(int argc, char** argv)
{
    #ifdef RMANRASNET
        easyloggingpp::Configurations defaultConf;
        defaultConf.setToDefault();
        defaultConf.set(easyloggingpp::Level::Error,
                        easyloggingpp::ConfigurationType::Format,
                        "%datetime %level %loc %log %func ");
        easyloggingpp::Loggers::reconfigureAllLoggers(defaultConf);
    #endif

    installSigSegvHandler(crash_handler);

    rasconn = NULL;

    SET_OUTPUT(1);
    ENTER(ctx << "main()");

    // show help if no arguments are passed
    if (argc < 2)
    {
        showHelp();
        LEAVE(ctx << "main()");
        return EXIT_SUCCESS;
    }

    // data structure to hold selected image metadata
    // ToDo: sort out which data elements of the structure
    //       are still actually been used and ditch surplus ones
    Header header;
    resetHeader(header);

    // declare variables for command line arguments
    string filepath;                                        // -f
    string dirpath;                                         // -d
    string suffix;                                          // -s
    string collname;                                        // --coll
    string connfile;                                        // --conn
    string mode3D;                                          // --3D
    string tilingSpec;                                      // --tiling

    double cellsizez = -1;                                       // --csz
    vector<double> bnd;                                     // --bnd
    vector<double> oids;                                    // --oid
    bool bupdate = false;
    vector<double> shift;                                   // --shift
    vector<string>  usertype;                               // -t

    string usercovname;                                     // --coverage-name
    vector<string> crsuri;                                  // --crs-uri
    vector<double> zcoords;                                    // --z-coords
    vector<double> geobbox;                                 // --geo-bbox
    string metadata;                                        // --metadata

    // parse command line arguments
    int arg = 1;
    while (arg < argc-1)
    {
        string theArg = argv[arg];
        string __arg = theArg;
        transform(theArg.begin(), theArg.end(),
                       theArg.begin(), ::tolower);

        if (theArg == "-f")
        {
            filepath = argv[arg+1];
            //ToDo: anything to check here?
        }
        else if (theArg == "-d")
        {
            dirpath = argv[arg+1];
            // remove trailing slash
            if (dirpath.at(dirpath.length() - 1) == '/')
                dirpath = dirpath.substr(0, dirpath.length() - 1);
            DIR* dirp = opendir(dirpath.c_str());
            if (dirp == NULL)
            {
                cerr << ctx << "main(): "
                << "invalid parameter for -d: could not "
                << "access directory '" << dirpath << "'!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "-s")
        {
            suffix = argv[arg+1];
        }
        else if (theArg == "--coverage-name")
        {
            usercovname = argv[arg+1];
            if (usercovname.empty())
            {
                cerr << ctx << "main(): "
                  << "missing parameter for --coverage-name: please "
                << "specify a coverage name!"
                << endl;
                  LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--crs-uri")
        {
            string sequence = argv[arg+1];
            stringstream div;
            div << CRS_RESOLVER_PREFIX;
            vector<string> vsep;
            vsep.push_back(div.str());
            vsep.push_back("http://");

            if (!parseStringSequence(sequence, crsuri, -1 , vsep))
            {
                cerr << ctx << "main(): "
                  << "Missing or invalid parameter for --crs-uri: Please "
                << "specify a valid coordinate reference system identifier! "
                << "E.g.: " << CRS_RESOLVER_PREFIX << "/crs/EPSG/0/4326"
                << " or: http://www.opengis.net/def/crs/EPSG/0/27200" << endl;
                  LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--coll")
        {
            collname = argv[arg+1];
            if (collname.empty())
            {
                cerr << ctx << "main(): "
                << "missing parameter for --coll: please "
                << "specify a target collection!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--conn")
        {
            connfile = argv[arg+1];
            if (access(connfile.c_str(), R_OK) != 0)
            {
                cerr << ctx << "main(): "
                << "invalid parameter for --conn: could "
                << "not access connection file '"
                << connfile << "'!" << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--3d")
        {
            mode3D = argv[arg+1];
            transform(mode3D.begin(), mode3D.end(), mode3D.begin(),
                           ::tolower);
            if (mode3D != "top" && mode3D != "bottom")
            {
                cerr << ctx << "main(): "
                << "invalid parameter for -3D: valid "
                << "parameters are {top | bottom}!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--bnd")
        {
            string tmp = argv[arg+1];
            if (!parseCoordinateString(tmp, bnd))
            {
                cerr << ctx << "main(): "
                << "invalid parameter for --bnd: "
                << "failed reading spatial boundary!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--crs-order")
        {
            string tmp = argv[arg+1];
            vector<double> vtmp;
            if (    !parseCoordinateString(tmp, vtmp)
                ||  !checkCRSOrderSequence(vtmp, header.crs_order)
               )
            {
                cerr << ctx << "main(): "
                << "invalid parameter for --crs-order: please "
                << "specify an appropriate order, e.g. for yxz-order: --crs-order 1:0:2"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--z-coords")
        {
            string tmp = argv[arg+1];
            if (    !parseCoordinateString(tmp, zcoords)
                ||  !checkZCoords(zcoords)
               )
            {
                cerr << ctx << "main(): "
                << "invalid parameter for --z-coords: please "
                << "specify one or more z-axis coordinates in "
                << "strictly ascending order, e.g.: --z-coords 1000:1050:1080"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--geo-bbox")
        {
            string tmp = argv[arg+1];
            if (!parseCoordinateString(tmp, geobbox))
            {
                cerr << ctx << "main(): "
                << "invalid parameter for --geo-bbox: "
                << "failed reading spatial boundary!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "-t")
        {
            string tmp = argv[arg+1];
            vector<string> vsep;
            //vsep.push_back(":");
            if (!parseStringSequence(tmp, usertype, 2, vsep))
            {
                cerr << ctx << "main(): "
                << "invalid parameter for -t "
                << "(valid example: -t RGBImage:RGBSet)"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--oid")
        {
            string tmp = argv[arg+1];
            vector<double> tmpoids;
            if (!parseCoordinateString(tmp, tmpoids))
            {
                cerr << ctx << "main(): "
                << "invalid parameter for --oid: "
                << "failed reading object identifier(s)!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }

            // copy valid oids into final vector
            for (int t=0; t < tmpoids.size(); ++t)
            {
                if (tmpoids[t] > 0)
                    oids.push_back(tmpoids[t]);
            }
            bupdate = true;
        }
        else if (theArg == "--csz")
        {
            cellsizez = atof(argv[arg+1]);
            if (cellsizez <= 0)
            {
                cerr << ctx << "main(): "
                << "invalid parameter for --csz: "
                << "z-axis cell size must be numeric and > 0!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--shift")
        {
            string tmp = argv[arg+1];
            if (!parseCoordinateString(tmp, shift))
            {
                cerr << ctx << "main(): "
                << "invalid parameter for --shift: "
                << "failed reading shift coordinates!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--tiling")
        {
            tilingSpec = argv[arg+1];
        }
        else if (theArg == "--metadata")
        {
            // meta data is going to be checked later,
            // which is something we may want to change ...
            // here, we only check, whether the string is empty or not
            metadata = argv[arg+1];
            if (metadata.empty() ||
                metadata.find('=') == string::npos)
            {
                cerr << ctx << "main(): "
                << "empty or invalid parameter for --metadata!"
                << endl;
                LEAVE(ctx << "main()");
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--help")
        {
            showHelp();
            LEAVE(ctx << "main()");
            return EXIT_SUCCESS;
        }
        arg++;
    }

    string lastarg = argv[argc-1];
    if (lastarg == "--help")
    {
        showHelp();
        LEAVE(ctx << "main()");
        return EXIT_SUCCESS;
    }

    // ---------------------------------------------------------------------
    // let's see what we've got so far
    TALK("filepath: " << filepath);
    TALK("dirpath: " << dirpath);
    TALK("suffix: " << suffix);
    TALK("collname: " << collname);
    TALK("connfile: " << connfile);
    TALK("mode3D: " << mode3D);
    TALK("csz: " << cellsizez);
    TALK("coverage name: " << usercovname);

    stringstream debugstr;
    debugstr << "bnd: ";
    for (int i=0; i < bnd.size(); i++)
        debugstr<< bnd[i] << " ";
    TALK(debugstr.str());
    debugstr.str("");

    debugstr << "oid: ";
    for (int v=0; v < oids.size(); v++)
        debugstr<< oids[v] << " ";
    TALK(debugstr.str());
    debugstr.str("");

    debugstr << "shift: ";
    for (int s=0; s < shift.size(); s++)
        debugstr<< shift[s] << " ";
    TALK(debugstr.str());
    debugstr.str("");

    debugstr << "data types: ";
    for (int t=0; t < usertype.size(); t++)
        debugstr<< usertype.at(t) << " ";
    TALK(debugstr.str());
    debugstr.str("");

    debugstr << "geo-bbox: ";
    for (int i=0; i < geobbox.size(); i++)
        debugstr<< geobbox[i] << " ";
    TALK(debugstr.str());
    debugstr.str("");

    debugstr << "CRS order: ";
    for (int i=0; i < header.crs_order.size(); i++)
        debugstr<< header.crs_order[i] << " ";
    TALK(debugstr.str());
    debugstr.str("");

    debugstr << "CRS URIs: ";
    for (int i=0; i < crsuri.size(); i++)
        debugstr<< crsuri[i] << "\n          ";
    TALK(debugstr.str());
    debugstr.str("");

    TALK("tiling scheme: " << tilingSpec);
    TALK("metadata: " << metadata);

    debugstr << "z-axis coordinates: ";
    for (int i=0; i < zcoords.size(); i++)
        debugstr<< zcoords[i] << " ";
    TALK(debugstr.str());
    debugstr.str("");

    // -----------------------------------------------------------------------
    // EVALUATE ARGUMENTS
    // get the connection file and check readability
    if (connfile.empty())
    {
        connfile = string(getenv("HOME")) + "/" + RAS_USER_RESOURCEDIR + "/rasconnect";
        if (access(connfile.c_str(), R_OK) != 0)
        {
            cerr << ctx << "main(): "
            << "could not access connection file '"
            << connfile << "'!"
            << endl;
            LEAVE(ctx << "main()");
            return EXIT_FAILURE;
        }
    }

    // create the list of filenames
    vector< string > vnames;
    if (!filepath.empty())
    {
        vnames.push_back(filepath);
    }
    else if (!dirpath.empty())
    {
        getDirContent(dirpath, suffix, vnames);
    }

    if (vnames.size() == 0)
    {
        cerr << ctx << "main(): "
        << "no input files specified!"
        << endl;
        showHelp();
        LEAVE(ctx << "main()");
        return EXIT_FAILURE;
    }
    else
    {
        // sort filenames listed from a directory, so that we have some
        // consistent behavior and know what to expect -- DM 2012-mar-01
        sort(vnames.begin(), vnames.end());
    }

    // check 3d paramters
    bool b3D = (    !mode3D.empty()
                ||  shift.size() == 3
                ||  zcoords.size() > 0
               ) ? true : false;

    // check crs_order
    // if none has been specified at all (i.e. crs_order is empty)
    // we set the default;
    if (header.crs_order.size() == 0)
    {
        header.crs_order.push_back(0);
        header.crs_order.push_back(1);
        if (b3D)
        {
            header.crs_order.push_back(2);
        }
    }
    // if an order had been specified, it should fit
    // other user settings
    else if (b3D && header.crs_order.size() != 3)
    {
        cerr << ctx << "main(): "
        << "invalid CRS axis order specified! Double check number of specified axes!"
        << endl;
        showHelp();
        LEAVE(ctx << "main()");
        return EXIT_FAILURE;
    }

	// we set the image to 'irregular' when we've got
	// z-axis coordinates specified;
	if (zcoords.size() > 0)
		header.isRegular = false;


    // #############################################################################################################
    // FURTHER PARAMETER EVALUATION WITH HELP FROM RASDAMANHELPER
    try
    {
        rasconn = new RasdamanConnector(connfile);
        RasdamanHelper2 helper(rasconn);

        double toid;
        if (!usercovname.empty())
        {
		    TALK("checking, whether we've got a coverage '" << usercovname << "' ...");
			toid = helper.getOIDFromCoverageName(usercovname);
			if (toid != -1)
			{
				// do we have a collection specified, and if yes does the coverage belong to it?
				if (!collname.empty())
				{
					vector<double> imgids = helper.getImageOIDs(collname);
					vector<double>::iterator it = find(imgids.begin(), imgids.end(), toid);
					if (it == imgids.end())
					{
						cout << "Specified coverage is not part of the specified collection! Abort." << endl;
						LEAVE(ctx << "main()");
						return EXIT_FAILURE;
					}
				}



				TALK("found coverage '" << usercovname << "' with oid #" << toid);
				if (oids.size() == 0 || toid == oids[0])
				{
					TALK("coverage '" << usercovname << "' is set to be the update coverage");
					oids.push_back(toid);
				}
				else
				{
					cout << "Specified OID(s) and coverage name reference different images!" << endl;
					LEAVE(ctx << "main()");
					return EXIT_FAILURE;
				}
			}
        }

		// ////////////////////////////// PARAM COMBINATIONS //////////////////////////////////////////


        // when we're creating an irregular image cube, --z-coords AND --csz have to be specified
        if (!header.isRegular && oids.size() == 0 && cellsizez <= 0)
        {
			cout << "Please specify --csz upon creation of irregularly spaced image cubes!" << endl;
			LEAVE(ctx << "main()");
			return EXIT_FAILURE;
        }

        ////////////////////////////////// GET CRS INFO FROM GDAL /////////////////////////////////////////

	    // initiate gdal
	    GDALAllRegister();

        // if the user hasn't specified any crs uri, we try to derive a resolvable CRS identifier
        // from the GDAL WKT CRS definition of the first specified input file, if available;
	    // we notify the user if something DIDN't work,
        // if we found a description and did find an epsg code, we don't bother the user
	    Header crsHeader;
	    resetHeader(crsHeader);

	    if (crsuri.size() == 0)
        {
		if (vnames.size() > 0)
		{
			readTileInformation(vnames[0], crsHeader);
			getMetaURIs(crsHeader, helper, b3D);
		}
        }
        else
        {
		    crsHeader.crs_uris.clear();
            for (int c=0; c < crsuri.size(); ++c)
                crsHeader.crs_uris.push_back(crsuri[c]);
        }

        // ////////////////////////////// CHECK FOR INDEXED CRS /////////////////////////////////

        // if either the source image or the image to be updated has an indexed CRS,
        // operate in index mode (s. also ::processImages()):msa_c2_1
        // - no shifts
        // - no bnd subsetting
        // - no mosaicing (taken care of in ::processImages(...) )
        // however, more than one file can be processed, thereby coordinates are being
        // converted into their pixel domain and imported "as is"

        vector<bool> axisIndexed;
        for (int i=0; i < 3; ++i)
		axisIndexed.push_back(false);

        vector<string> uris;
        if (oids.size() > 0)
        {
			long covid = helper.getCoverageIdFromOID(oids[0]);
			if (covid >= 0)
			{
				uris = helper.getCRSURIsfromCoverageId(covid);
			}
        }
        else
        {
		    uris = crsHeader.crs_uris;
        }

		for (int d=0; d < uris.size(); ++d)
		{
			string u = uris[d];
			if (u.rfind("Index2D") != std::string::npos)
			{
				axisIndexed[0] = true ;
				axisIndexed[1] = true ;
				axisIndexed[2] = false;
				TALK("xy coords defined in index CRS!" << endl);
			}
			else if (u.rfind("Index3D") != std::string::npos)
			{
				axisIndexed[0] = true;
				axisIndexed[1] = true;
				axisIndexed[2] = header.isRegular ? true : false;
				TALK("xyz coords defined in index CRS!" << endl);
			}
		}

		// "adjust input params when indexed "
		if (axisIndexed[0] || axisIndexed[1])
		{
			if (bnd.size() > 0)
				cout << "--bnd ignored for index CRS!" << endl;
			if (shift.size() > 0)
				cout << "--shift x:y ignored for index CRS!" << endl;

			//TALK( --bnd and --shift x:y ignored!")
			bnd.clear();
			if (shift.size() >= 2)
			{
				shift[0] = 0;
				shift[1] = 0;
			}
		}
		if (axisIndexed[2] && shift.size() == 3)
		{
			cout << "--shift z ignored for index CRS!" << endl;
			shift[2] = 0;
		}

        ///////////////////////////////// ANALYSE INPUT FILES /////////////////////////////////

		// read input image(s) using GDAL --> populate Header structure
		// overall processing region (applies to multiple files)
		vector< string > vimportnames;
		readImageInformation(vnames, header, bnd,
							 vimportnames, b3D, cellsizez);


		if (vimportnames.size() == 0)
		{
			cerr << ctx << "main(): "
			<< "empty import region!"
			<< endl;
			LEAVE(ctx << "main()");
			return EXIT_FAILURE;
		}

		// copy crs info derived earlier into the processing header
		header.crs_uris = crsHeader.crs_uris;

		////////////////////////////////// CALC PIXEL SHIFT /////////////////////////////////////
		double collexists = helper.doesCollectionExist(collname);


        // note: shiftPt is in pixel coordinates
        r_Point shiftPt;
        double xshift, yshift, zshift;
        if (shift.size() == 2)
        {
            xshift = shift[0] / header.cellsize.x;
            xshift = xshift > 0 ? xshift + 0.5 : xshift - 0.5;

            yshift = shift[1] / header.cellsize.y;
            yshift = yshift > 0 ? yshift + 0.5 : yshift - 0.5;

            shiftPt = r_Point(2) << (r_Range)xshift
                      << (r_Range)yshift;
        }
        else if (shift.size() == 3)
        {
            xshift = shift[0] / header.cellsize.x;
            xshift = xshift > 0 ? xshift + 0.5 : xshift - 0.5;

            yshift = shift[1] / -header.cellsize.y;
            yshift = yshift > 0 ? yshift + 0.5 : yshift - 0.5;

            zshift = (shift[2] / header.cellsize.z);
		zshift = zshift > 0 ? zshift + 0.5 : zshift - 0.5;

            if (collexists >= 0 && oids.size() > 0)
            {
		vector<double> geodom = helper.getMetaGeoDomain(oids[0], true);
		r_Minterval idom = helper.getImageSdom(collname, oids[0]);
		double diff = (shift[2] - geodom[4]) / header.cellsize.z;
		int relshift = diff > 0 ? diff + 0.5 : diff - 0.5;
		zshift = idom[2].low() + relshift;
            }

            shiftPt = r_Point(3) << (r_Range)xshift
                      << (r_Range)yshift
                      << (r_Range)zshift;

            // note: this is only gonna be used upon insertion of
            // the first slice, afterwards, the petascope
            // boundary parameters of petascope are used
            // to determine new z-boundaries
            header.zmin = shift[2];
        }

        TALK("user pixel shift: " << shiftPt.get_string_representation());


		///////////////////////////////////////////////////////////////////////////
		//  DO ACTUAL IMPORT WORK NOW /////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////

        // check, whether the collection already exists
        if (helper.doesCollectionExist(collname) < 0)
        {
            // the user is always right, use his type!
            if (usertype.size() == 2)
                helper.insertUserCollection(collname, usertype.at(1));
            else
                helper.insertCollection(collname, header.rmantype,
                                        b3D);
        }

        // user specified collection and image type
        string marraytypename = "";
        if (usertype.size() == 2)
            marraytypename = usertype.at(0);

        // process input files
        if (!processImageFiles(vimportnames, collname, oids, header, mode3D,
                               shiftPt, helper, marraytypename,
                               tilingSpec, usercovname, zcoords, axisIndexed))
        {
            cerr << ctx << "main(): "
            << "failed processing image file(s)!"
            << endl;
            LEAVE(ctx << "main()");
            return EXIT_FAILURE;
        }

        // post-import update of metadata
        bool bDomvalid = false;
        if (geobbox.size() >= 4)
        {
            if (geobbox[1] > geobbox[0] && geobbox[3] > geobbox[2])
            {
                bDomvalid = true;
            }

            if (bDomvalid && geobbox.size() >= 6 && header.isRegular)
            {
                if (geobbox[5] > geobbox[4])
                    bDomvalid = false;
            }
        }

        for (int q=0; q < oids.size(); ++q)
        {
            if (bDomvalid)
            {
                helper.writePSMetadata(
                        oids[q],
                        collname,
                        usercovname,
                        header.crs_uris,
                        header.crs_order,
                        marraytypename,
                        geobbox[0], geobbox[1], geobbox[2], geobbox[3],
                        geobbox[4], geobbox[5], header.cellsize.x, header.cellsize.y,
                        header.cellsize.z,
                        true,
                        -1,
                        0.0,
                        axisIndexed);
            }

            if (metadata.size() > 0)
            {
                if (!helper.writeExtraMetadata(oids[q], metadata))
                {
                    cerr << ctx << "main(): "
                    << "ERROR: --metadata: Issue(s) writing extra metadata! See debug output for more info."
                    << endl;
                    LEAVE(ctx << main);
                    return EXIT_FAILURE;
                }
            }
        }
     }
    catch (r_Error& re)
    {
        if (rasconn)
        {
            delete rasconn;
        }
        cerr << ctx << "main(): "
        << re.what() << endl;
        LEAVE(ctx << "main()");
        return EXIT_FAILURE;
    }
    if (rasconn)
    {
        delete rasconn;
    }

    LEAVE(ctx << "main()");
    return EXIT_SUCCESS;
}
