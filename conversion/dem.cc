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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/**
 * SOURCE: dem.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_DEM
 *
 * PURPOSE:
 * Provides interface to convert data from/to Array format to/from DEM format.
 *
 * COMMENTS:
 * For further support send a mail to comanl@yahoo.com
 * - convertTo() writes a temp file; this should be omitted for performance reasons
*/

#include "conversion/dem.hh"

#include <float.h>
#include <string>
#include <cstring>
#include <sstream>
#include <algorithm>

using std::istringstream;
using std::string;
using namespace std;

#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "mymalloc/mymalloc.h"
#include <logging.hh>

const r_Dimension r_Conv_DEM::srcIntervDim = 1;
const r_Dimension r_Conv_DEM::destIntervDim = 2;
const r_ULong r_Conv_DEM::paramMin = 6;
const char *r_Conv_DEM::paramSep = ",";
const char *r_Conv_DEM::paramEq = "=";
const char *r_Conv_DEM::paramFlipX = "flipx";
const char *r_Conv_DEM::paramFlipY = "flipy";
const char *r_Conv_DEM::paramStartX = "startx";
const char *r_Conv_DEM::paramEndX = "endx";
const char *r_Conv_DEM::paramResX = "resx";
const char *r_Conv_DEM::paramStartY = "starty";
const char *r_Conv_DEM::paramEndY = "endy";
const char *r_Conv_DEM::paramResY = "resy";

const r_Double r_Conv_DEM::NULL_DB = 0.;
const r_Double r_Conv_DEM::ZERO_DB = FLT_MIN;
const r_Double r_Conv_DEM::ZERO_DEM = 0.;

r_Conv_DEM::~r_Conv_DEM(void)
{
    //nothing to care for
}

void r_Conv_DEM::initGeoBBox(r_GeoBBox &cBBox)
{
    //flipy is selected by default
    cBBox.flipy = 1;

    //flipx is not selected by default
    cBBox.flipx = 0;

    //geo information are initialized by default to DBL_MAX
    // FIXME: better defaults res=1, min=-MAX?
    cBBox.startx = DBL_MAX;
    cBBox.endx = DBL_MAX;
    cBBox.resx = DBL_MAX;
    cBBox.starty = DBL_MAX;
    cBBox.endy = DBL_MAX;
    cBBox.resy = DBL_MAX;
}

r_Conv_DEM::r_Conv_DEM(const char *source, const r_Minterval &lengthordomain, const r_Type *tp)
    : r_Convertor(source, lengthordomain, tp, true)
{
    initGeoBBox(collBBox);
}

r_Conv_DEM::r_Conv_DEM(const char *source, const r_Minterval &lengthordomain, int tp)
    : r_Convertor(source, lengthordomain, tp)
{
    initGeoBBox(collBBox);
}

bool r_Conv_DEM::decodeOptions(const char *options,
                               r_GeoBBox &cBBox) noexcept
{
    LINFO << "r_Conv_DEM::decodeOptions(" << (options ? options : "NULL") << ")";

    r_Parse_Params parseParams;

    initGeoBBox(cBBox);

    parseParams.add(paramFlipX, &cBBox.flipx, r_Parse_Params::param_type_int);
    parseParams.add(paramFlipY, &cBBox.flipy, r_Parse_Params::param_type_int);
    parseParams.add(paramStartX, &cBBox.startx, r_Parse_Params::param_type_double);
    parseParams.add(paramEndX, &cBBox.endx, r_Parse_Params::param_type_double);
    parseParams.add(paramResX, &cBBox.resx, r_Parse_Params::param_type_double);
    parseParams.add(paramStartY, &cBBox.starty, r_Parse_Params::param_type_double);
    parseParams.add(paramEndY, &cBBox.endy, r_Parse_Params::param_type_double);
    parseParams.add(paramResY, &cBBox.resy, r_Parse_Params::param_type_double);

    //process options
    r_Long processRet = parseParams.process(options);
    if (processRet < static_cast<int>(paramMin))
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: Some required options are missing!";
        return false;
    }

    //check if start,res,end are present
    if (cBBox.startx == DBL_MAX)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: startx is not present!";
        return false;
    }

    if (cBBox.starty == DBL_MAX)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: starty is not present!";
        return false;
    }

    if (cBBox.endx == DBL_MAX)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: endx is not present!";
        return false;
    }

    if (cBBox.endy == DBL_MAX)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: endy is not present!";
        return false;
    }

    if (cBBox.resx == DBL_MAX)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: resx is not present!";
        return false;
    }

    if (cBBox.resy == DBL_MAX)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: resy is not present!";
        return false;
    }

    //check res
    if (!cBBox.resx)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: resx is zero!";
        return false;
    }

    if (!cBBox.resy)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: resy is zero!";
        return false;
    }

    //check start >= end
    if (cBBox.startx >= cBBox.endx)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Error: startx >= endx!";
        return false;
    }

    if (cBBox.starty >= cBBox.endy)
    {
        LERROR << "r_Conv_DEM::decodeOptions(...) Erorr: starty >= endy!";
        return false;
    }

    //show parsed options
    LINFO << "r_Conv_DEM::decodeOptions(...) parsed options:\n"
          << " " << paramFlipX << paramEq << cBBox.flipx
          << " " << paramFlipY << paramEq << cBBox.flipy << "\n"
          << " " << paramStartX << paramEq << cBBox.startx
          << " " << paramEndX << paramEq << cBBox.endx
          << " " << paramResX << paramEq << cBBox.resx << "\n"
          << " " << paramStartY << paramEq << cBBox.starty
          << " " << paramEndY << paramEq << cBBox.endy
          << " " << paramResY << paramEq << cBBox.resy;
    return true;
}

string
r_Conv_DEM::encodeOptions(const r_GeoBBox &cBBox) noexcept
{
    std::ostringstream os;

    os.str("");
    os.setf(std::ios::fixed);
    os << paramFlipX << paramEq << cBBox.flipx
       << paramSep << paramFlipY << paramEq << cBBox.flipy
       << paramSep << paramStartX << paramEq << cBBox.startx
       << paramSep << paramEndX << paramEq << cBBox.endx
       << paramSep << paramResX << paramEq << cBBox.resx
       << paramSep << paramStartY << paramEq << cBBox.starty
       << paramSep << paramEndY << paramEq << cBBox.endy
       << paramSep << paramResY << paramEq << cBBox.resy;

    LINFO << "r_Conv_DEM::encodeOptions(" << os.str() << ")";

    return os.str();
}

void r_Conv_DEM::checkLimits()
{
    //show processed data
    LINFO << "r_Conv_DEM::checkLimits() processed data:\n"
          << " minx=" << min.x << " miny=" << min.y << " minh=" << min.h << "\n"
          << " maxx=" << max.x << " maxy=" << max.y << " maxh=" << max.h;
    // printf( "r_Conv_DEM::checkLimits() processed data: minx=%8G, miny=%8G, minh=%8G, maxx=%8G, maxy=%8G, maxh=%8G\n", min.x, min.y, min.h, max.x, max.y, max.h );

    if (collBBox.startx > min.x)
    {
        LERROR << "r_Conv_DEM::checkLimits() startx( " << collBBox.startx << ") > min.x (" << min.x << " )!";
        throw r_Error();
    }
    if (collBBox.endx < max.x)
    {
        LERROR << "r_Conv_DEM::checkLimits() endx( " << collBBox.endx << ") < max.x (" << max.x << " )!";
        throw r_Error();
    }

    if (collBBox.starty > min.y)
    {
        LERROR << "r_Conv_DEM::checkLimits() starty( " << collBBox.starty << ")  > min.y (" << min.y << " )!";
        throw r_Error();
    }

    if (collBBox.endy < max.y)
    {
        LERROR << "r_Conv_DEM::checkLimits() endy( " << collBBox.endy << ") < max.y (" << max.y << " )!";
        throw r_Error();
    }
}

void r_Conv_DEM::readFromSrcStream()
{
    istringstream iFile(desc.src);
    string currStrRow;
    r_Long rowNo = 0;
    r_Double noResx, noResy;
    DEMRow currRow, prevRow;

    min.x = min.y = min.h = DBL_MAX;
    max.x = max.y = max.h = -DBL_MAX;
    demRows.clear();

    //process the lines
    while (!iFile.eof())
    {
        getline(iFile, currStrRow);
        rowNo++;
        if (currStrRow.empty())
        {
            // LDEBUG << "r_Conv_DEM::readFromSrcStream() skipping empty line " << rowNo;
            continue;
        }
        else
        {
            // have an input stream for analysing the current line
            // (declaring this variable here allows to have a fresh one;
            // had a reentrance problem with followup lines -- PB 2005-sep-08)
            istringstream icurrRow;
            icurrRow.str(currStrRow);

            //decode x
            icurrRow >> currRow.x;
            if (!icurrRow)
            {
                LERROR << "Error in r_Conv_DEM::readFromSrcStream():: unable to decode x in line " << rowNo << ", skipping line: " << currStrRow;
                continue;
            }

            //decode y
            icurrRow >> currRow.y;
            if (!icurrRow)
            {
                LERROR << "Error in r_Conv_DEM::readFromSrcStream():: unable to decode y in line " << rowNo << ", skipping line: " << currStrRow;
                continue;
            }

            //decode h
            icurrRow >> currRow.h;
            if (!icurrRow)
            {
                LERROR << "Error in r_Conv_DEM::readFromSrcStream():: unable to decode h in line " << rowNo << ", skipping line: " << currStrRow;
                continue;
            }

            //update to support NULL value: 0. (real value) goes in FLT_MIN(db value)
            //because 0.(db value) represent NULL(real value). When we do export we skip NULL values.
            if (currRow.h == ZERO_DEM)
            {
                currRow.h = ZERO_DB;
            }

            //FIXME we ignore this check, because it may happen to have a incomplet dem
            /*
            //check if we have resx, resy
            noResx=currRow.x/collBBox.resx;
            if((currRow.x - noResx*collBBox.resx) > 0.)
            {
                LERROR << "r_Conv_DEM::readFromSrcStream() resolution for x on line " <<
                    rowNo << " is not " << collBBox.resx << " !";
                throw r_Error();
            }
            noResy=currRow.y/collBBox.resy;
            if((currRow.y - noResy*collBBox.resy) > 0.)
            {
                LERROR << "r_Conv_DEM::readFromSrcStream() resolution for y on line " <<
                    rowNo << " is not " << collBBox.resy << " !";
                throw r_Error();
            }
            */

            //compute min, max for x,y,z
            min.x = std::min<r_Double>(min.x, currRow.x);
            min.y = std::min<r_Double>(min.y, currRow.y);
            min.h = std::min<r_Double>(min.h, currRow.h);
            max.x = std::max<r_Double>(max.x, currRow.x);
            max.y = std::max<r_Double>(max.y, currRow.y);
            max.h = std::max<r_Double>(max.h, currRow.h);

            //store currRow
            demRows.push_back(currRow);
        }  //end if(currStrRow.empty())

    }  //end reading src stream

    if (demRows.empty())
    {
        // LDEBUG << "r_Conv_DEM::readFromSrcStream() desc.src stream empty.";
        throw r_Error();
    }

    // std::cout << "r_Conv_DEM::readFromSrcStream(): x=" << min.x << ":" << max.x << ", y=" << min.y << ":" << max.y << endl;

    //check limits
    checkLimits();
}

void r_Conv_DEM::readToSrcStream()
{
    r_Long x = 0, y = 0;
    r_Long xlow = 0, ylow = 0;
    r_Long xhigh = 0, yhigh = 0;
    DEMRow currRow;
    r_Bytes typeSize = 0;
    r_Long offset = 0;
    char *buffer = NULL;

    //initialize
    xlow = desc.srcInterv[0].low();
    ylow = desc.srcInterv[1].low();

    xhigh = desc.srcInterv[0].high();
    yhigh = desc.srcInterv[1].high();

    //compute min & max
    if (collBBox.flipx)
    {
        min.x = collBBox.endx - xhigh * collBBox.resx;
        max.x = collBBox.endx - xlow * collBBox.resx;
    }
    else
    {
        min.x = collBBox.startx + xlow * collBBox.resx;
        max.x = collBBox.startx + xhigh * collBBox.resx;
    }

    if (collBBox.flipy)
    {
        min.y = collBBox.endy - yhigh * collBBox.resy;
        max.y = collBBox.endy - ylow * collBBox.resy;
    }
    else
    {
        min.y = collBBox.starty + ylow * collBBox.resy;
        max.y = collBBox.starty + yhigh * collBBox.resy;
    }

    min.h = DBL_MAX;
    max.h = -DBL_MAX;

    //check limits
    checkLimits();

    //prepare container
    demRows.clear();
    typeSize = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->size();
    buffer = new char[typeSize];
    if (!buffer)
    {
        LERROR << "r_Conv_DEM::readToSrcStream() unable to claim memory !";
        throw r_Ememory_allocation();
    }
    for (y = ylow; y <= yhigh; y++)
    {
        if (collBBox.flipy)
        {
            currRow.y = collBBox.endy - y * collBBox.resy;
        }
        else
        {
            currRow.y = collBBox.starty + y * collBBox.resy;
        }

        for (x = xlow; x <= xhigh; x++)
        {
            if (collBBox.flipx)
            {
                currRow.x = collBBox.endx - x * collBBox.resx;
            }
            else
            {
                currRow.x = collBBox.startx + x * collBBox.resx;
            }
            offset = desc.srcInterv.cell_offset(r_Point(x, y)) * typeSize;
            memcpy(buffer, &desc.src[offset], typeSize);

            switch (desc.srcType->type_id())
            {
            case r_Type::BOOL:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_boolean(buffer);
                break;
            case r_Type::CHAR:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_char(buffer);
                break;
            case r_Type::OCTET:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_octet(buffer);
                break;
            case r_Type::SHORT:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_short(buffer);
                break;
            case r_Type::USHORT:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_ushort(buffer);
                break;
            case r_Type::LONG:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_long(buffer);
                break;
            case r_Type::ULONG:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_ulong(buffer);
                break;
            case r_Type::FLOAT:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_float(buffer);
                break;
            case r_Type::DOUBLE:
                currRow.h = (static_cast<r_Primitive_Type *>(const_cast<r_Type *>(desc.srcType)))->get_double(buffer);
                break;
            default:
                //write message to log
                LERROR << "r_Conv_DEM::readToSrcStream() srcType (" << desc.srcType->type_id() << ") unsupported !";
                //clean up
                if (buffer)
                {
                    delete[] buffer;
                    buffer = NULL;
                }
                //report error
                throw r_Error();
                break;
            }
            min.h = std::min<r_Double>(min.h, currRow.h);
            max.h = std::max<r_Double>(max.h, currRow.h);
            demRows.push_back(currRow);
        }
    }

    //clean up
    if (buffer)
    {
        delete[] buffer;
        buffer = NULL;
    }

    if (demRows.empty())
    {
        LERROR << "r_Conv_DEM::readToSrcStream() src stream is empty !";
        throw r_Error();
    }

    LINFO << "r_Conv_DEM::readToSrcStream() processed interval [" << xlow << ":" << xhigh << "," << ylow << ":" << yhigh << "]";
}

void r_Conv_DEM::writeFromDestStream()
{
    DEMRowVec::const_iterator iter, iterEnd;
    r_Long xdim, ydim, offset;
    r_Point currPt(destIntervDim);
    r_Bytes typeSize = 0;

    //FIXME here we should modify for other type support
    if (desc.destType->type_id() != r_Type::DOUBLE)
    {
        LERROR << "r_Conv_DEM::writeFromDestStream() destType (" << desc.destType->type_id()
               << ") is not " << r_Type::DOUBLE << " !";
        throw r_Error();
    }

    xdim = desc.destInterv[0].get_extent();
    ydim = desc.destInterv[1].get_extent();
    iter = demRows.begin();
    iterEnd = demRows.end();
    typeSize = (static_cast<r_Primitive_Type *>(desc.destType))->size();

    //FIXME correction for strange effect of r_Long cast with 1e-6
    while (iter != iterEnd)
    {
        if (collBBox.flipx)
        {
            currPt[0] = (collBBox.endx - iter->x) / collBBox.resx + 1e-6;
        }
        else
        {
            currPt[0] = (iter->x - collBBox.startx) / collBBox.resx + 1e-6;
        }
        if (collBBox.flipy)
        {
            currPt[1] = (collBBox.endy - iter->y) / collBBox.resy + 1e-6;
        }
        else
        {
            currPt[1] = (iter->y - collBBox.starty) / collBBox.resy + 1e-6;
        }
        (static_cast<r_Primitive_Type *>(desc.destType))->set_double(&desc.dest[desc.destInterv.cell_offset(currPt) * typeSize], iter->h);
        ++iter;
    }

    LINFO << "r_Conv_DEM::writeFromDestStream() processed " << xdim << " x " << ydim << " elements.";
}

void r_Conv_DEM::writeToDestStream(ofstream &oFile)
{
    DEMRowVec::const_iterator iter, iterEnd;
    r_Double currH;

    if (!oFile.is_open())
    {
        LERROR << "r_Conv_DEM::writeToDestStream() oFile is not opened !";
        throw r_Error();
    }
    oFile.setf(std::ios::fixed);

    iter = demRows.begin();
    iterEnd = demRows.end();
    while (iter != iterEnd)
    {
        //update to support NULL value: 0. (real value) goes in FLT_MIN(db value)
        //because 0.(db value) represent NULL(real value). When we do export we skip NULL values.
        currH = iter->h;
        if (currH != NULL_DB)
        {
            //FIXME we have to implement different here when we change server scale algorithm
            if (currH == ZERO_DB)
            {
                currH = ZERO_DEM;
            }
            oFile << iter->x << "\t" << iter->y << "\t" << currH << endl;
        }
        ++iter;
    }
}

r_Conv_Desc &
r_Conv_DEM::convertFrom(const char *options)
{
    bool hasSrcType = true;

    LINFO << "r_Conv_DEM::convertFrom(" << (options ? options : "NULL") << ")";

    if (!desc.srcType)
    {
        desc.srcType = get_external_type(desc.baseType);
        hasSrcType = false;
    }

    try
    {
        LINFO << "r_Conv_DEM::convertFrom(...) src interval=" << desc.srcInterv;
        LINFO << "r_Conv_DEM::convertFrom(...) src type=" << desc.srcType->type_id();

        //check options
        if (!decodeOptions(options, collBBox))
        {
            LERROR << "Error in r_Conv_DEM::convertFrom(): illegal option string: " << (options ? options : "(null)");
            throw r_Error();
        }

        //check desc.srcInterv.dimension
        if (desc.srcInterv.dimension() != srcIntervDim)
        {
            LERROR << "r_Conv_DEM::convertFrom(" << (options ? options : "NULL")
                   << ") desc.srcInterv dimension (" << desc.srcInterv.dimension()
                   << " != " << srcIntervDim << " !";
            throw r_Error();
        }

        //check srcType
        if (!desc.srcType->isPrimitiveType())
        {
            LERROR << "r_Conv_DEM::convertFrom(" << (options ? options : "NULL")
                   << ") desc.srcType (" << desc.srcType->type_id()
                   << ") not supported, only primitive types !";
            throw r_Error();
        }

        if (desc.srcType->isComplexType())
        {
            LERROR << "r_Conv_DEM::convertFrom(" << (options ? options : "NULL")
                   << ") desc.srcType (" << desc.srcType->type_id()
                   << ") not supported !";
            throw r_Error();
        }

        //read src stream
        readFromSrcStream();

        //convert from DEM to marray
        //--computing the marray domain
        desc.destInterv = r_Minterval(destIntervDim);

        //FIXME correction for strange efect of r_Long cast with 1e-6
        if (collBBox.flipx)
            desc.destInterv << r_Sinterval(static_cast<r_Range>((collBBox.endx - max.x) / collBBox.resx + 1e-6),
                                           static_cast<r_Range>((collBBox.endx - min.x) / collBBox.resx + 1e-6));
        else
            desc.destInterv << r_Sinterval(static_cast<r_Range>((min.x - collBBox.startx) / collBBox.resx + 1e-6),
                                           static_cast<r_Range>((max.x - collBBox.startx) / collBBox.resx + 1e-6));
        if (collBBox.flipy)
            desc.destInterv << r_Sinterval(static_cast<r_Range>((collBBox.endy - max.y) / collBBox.resy + 1e-6),
                                           static_cast<r_Range>((collBBox.endy - min.y) / collBBox.resy + 1e-6));
        else
            desc.destInterv << r_Sinterval(static_cast<r_Range>((min.y - collBBox.starty) / collBBox.resy + 1e-6),
                                           static_cast<r_Range>((max.y - collBBox.starty) / collBBox.resy + 1e-6));

        LINFO << "r_Conv_DEM::convertFrom(...) dest interval=" << desc.destInterv;

        //--creating the resulting type
        desc.destType = new r_Primitive_Type("Double", r_Type::DOUBLE);
        LINFO << "r_Conv_DEM::convertFrom(...) dest type=" << desc.destType->type_id();

        //--claim memory for result
        desc.dest = static_cast<char *>(mymalloc(desc.destInterv.cell_count() * (static_cast<r_Primitive_Type *>(desc.destType))->size()));
        if (desc.dest == NULL)
        {
            LERROR << "r_Conv_DEM::convertFrom(" << (options ? options : "NULL")
                   << ") unable to claim memory !";
            throw r_Ememory_allocation();
        }
        memset(desc.dest, 0, desc.destInterv.cell_count() * (static_cast<r_Primitive_Type *>(desc.destType))->size());

        //--write parsed data in desc.dest
        writeFromDestStream();
    }
    catch (r_Error &err)
    {
        //cleanup
        if (!hasSrcType)
        {
            delete desc.srcType;
            desc.srcType = NULL;
        }

        //desc.destType
        if (desc.destType)
        {
            delete desc.destType;
            desc.destType = NULL;
        }

        //desc.dest
        if (desc.dest)
        {
            free(desc.dest);
            desc.dest = NULL;
        }

        //report error
        throw;
    }

    //cleanup
    if (!hasSrcType)
    {
        delete desc.srcType;
        desc.srcType = NULL;
    }

    //return result
    return desc;
}

r_Conv_Desc &r_Conv_DEM::convertFrom(__attribute__((unused)) r_Format_Params options)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Conv_Desc &
r_Conv_DEM::convertTo(const char *options, const r_Range *)
{
    bool hasSrcType = true;

    char pTempFileName[] = "demtempXXXXXX";  // name of temp file
    string tempFileName;                     // duplicate of temp file name -- heaven knows why
    ofstream oFile;                          // for writing out file
    FILE *pFile = NULL;                      // for reading back file
    size_t lenFile = 0;                      // size of file as read
    int tempFD;                              // for the temp file

    LINFO << "r_Conv_DEM::convertTo(" << (options ? options : "NULL") << ")";

    try
    {
        if (!desc.srcType)
        {
            desc.srcType = get_external_type(desc.baseType);
            hasSrcType = false;
        }

        LINFO << "r_Conv_DEM::convertTo(...) src interval=" << desc.srcInterv;
        LINFO << "r_Conv_DEM::convertTo(...) src type=" << desc.srcType->type_id();

        //check options
        if (!decodeOptions(options, collBBox))
        {
            throw r_Error();
        }

        if (!desc.srcType->isPrimitiveType())
        {
            LERROR << "r_Conv_DEM::convertTo(" << (options ? options : "NULL")
                   << ") desc.srcType (" << desc.srcType->type_id()
                   << ") not supported, only primitive types !";
            throw r_Error();
        }
        if (desc.srcType->isComplexType())
        {
            LERROR << "r_Conv_DEM::convertTo(" << (options ? options : "NULL")
                   << ") desc.srcType (" << desc.srcType->type_id()
                   << ") not supported !";
            throw r_Error();
        }

        //read src data
        readToSrcStream();

        //convert from marray to DEM;
        //--create the temp file
        //FIXME for multithread application
        tempFD = mkstemp(pTempFileName);
        if (tempFD == -1)
        {
            LERROR << "r_Conv_DEM::convertTo(" << (options ? options : "NULL")
                   << ") desc.srcType (" << desc.srcType->type_id()
                   << ") unable to generate a tempory file !";
            throw r_Error();
        }

        tempFileName = pTempFileName;
        oFile.open(tempFileName.c_str());
        if (!oFile.is_open())
        {
            LERROR << "r_Conv_DEM::convertTo(" << (options ? options : "NULL")
                   << ") desc.srcType (" << desc.srcType->type_id()
                   << ") unable to open the tempory file !";
            throw r_Error();
        }

        LINFO << "r_Conv_DEM::convertTo(...) temp file=" << tempFileName;

        //--get DEM format
        writeToDestStream(oFile);
        oFile.close();

        //--accessing the temp file
        if ((pFile = fopen(tempFileName.c_str(), "rb")) == NULL)
        {
            LERROR << "r_Conv_DEM::convertTo(): unable to read back file.";
            throw r_Error(r_Error::r_Error_General);
        }
        fseek(pFile, 0, SEEK_END);
        lenFile = static_cast<size_t>(ftell(pFile));
        LINFO << "r_Conv_DEM::convertTo(...) dest len=" << lenFile;

        if (!lenFile)
        {
            LERROR << "r_Conv_DEM::convertTo(): source contains only NULL values.";
            throw r_Error(FORMATCONV_DEMAREA_VALUESNULL);
        }

        //--creating the resulting type
        desc.destType = new r_Primitive_Type("Char", r_Type::CHAR);

        //--computing the marray domain
        desc.destInterv = r_Minterval(srcIntervDim);
        desc.destInterv << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(lenFile) - 1);

        LINFO << "r_Conv_DEM::convertTo(...) dest interval=" << desc.destInterv;
        LINFO << "r_Conv_DEM::convertTo(...) dest type=" << desc.destType->type_id();

        //--claim memory for desc.dest
        desc.dest = static_cast<char *>(mymalloc(lenFile));
        if (desc.dest == NULL)
        {
            LERROR << "r_Conv_DEM::convertTo(" << (options ? options : "NULL")
                   << ") unable to claim memory !";
            throw r_Ememory_allocation();
        }
        memset(desc.dest, 0, lenFile);

        //--store data in desc.dest
        fseek(pFile, 0, SEEK_SET);
        size_t i = fread(desc.dest, 1, lenFile, pFile);

        //clean up
        fclose(pFile);
        pFile = NULL;
        remove(pTempFileName);
    }
    catch (r_Error &err)
    {
        //cleanup
        if (!hasSrcType)
        {
            delete desc.srcType;
            desc.srcType = NULL;
        }

        //desc.destType
        if (desc.destType)
        {
            delete desc.destType;
            desc.destType = NULL;
        }

        //desc.dest
        if (desc.dest)
        {
            free(desc.dest);
            desc.dest = NULL;
        }

        // close & remove file, if not done previously
        if (pFile)
        {
            fclose(pFile);
        }
        pFile = NULL;
        remove(pTempFileName);

        //rethrow error
        throw;
    }

    //clean up
    if (!hasSrcType)
    {
        delete desc.srcType;
        desc.srcType = NULL;
    }

    //return result
    return desc;
}

const char *
r_Conv_DEM::get_name() const noexcept
{
    return get_name_from_data_format(r_DEM);
}

r_Data_Format
r_Conv_DEM::get_data_format() const noexcept
{
    return r_DEM;
}

r_Convertor *
r_Conv_DEM::clone() const
{
    return new r_Conv_DEM(desc.src, desc.srcInterv, desc.srcType);
}
