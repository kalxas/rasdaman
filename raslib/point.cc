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
 * SOURCE: point.cc
 *
 * MODULE: raslib
 * CLASS:  r_Point
 *
 * COMMENTS:
 *
*/

static const char rcsid[] = "@(#)raslib, r_Point: $Id: point.cc,v 1.22 2002/08/28 11:58:13 coman Exp $";

#include "config.h"
#include "point.hh"

#include <string.h>
#include <sstream>

#include <logging.hh>

#include "raslib/error.hh"

r_Point::r_Point(char* stringRep)
    : points(NULL), dimensionality(1), streamInitCnt(0)
{
    char    charToken = 0;
    r_Range valueToken = 0;

    // for parsing the string
    std::istringstream str(stringRep);

    // calculate dimensionality
    char* p = stringRep;
    while ((p = strchr(++p, ',')))
    {
        dimensionality++;
    }

    // allocate space for intervals
    points = new r_Range[ dimensionality ];

    str >> charToken;
    if (charToken != '[')
    {
        // error
        dimensionality = 0;
        delete[] points;
        points = NULL;
        throw r_Error(NOPOINT);
    }

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        str >> valueToken;
        points[i] = valueToken;

        if (i < dimensionality - 1)
        {
            str >> charToken;
            if (charToken != ',')
            {
                // error
                dimensionality = 0;
                delete[] points;
                points = NULL;
                throw r_Error(NOPOINT);
            }
        }
    }
}


r_Point::r_Point(r_Dimension dim)
    : dimensionality(dim),
      streamInitCnt(0)
{
    points = new r_Range[ dimensionality ];

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        points[i] = 0;
    }
}


r_Point& r_Point::operator<<(r_Range newElement)
{
    if (streamInitCnt >= dimensionality)
    {
        LFATAL << "r_Point::operator<<(" << newElement << ") already fully initialised";
        throw r_Einit_overflow();
    }

    points[streamInitCnt++] = newElement;
    return *this;
}


r_Point::r_Point(r_Range p1, r_Range p2)
    : dimensionality(2),
      streamInitCnt(2)
{
    points    = new r_Range[dimensionality];
    points[0] = p1;
    points[1] = p2;
}


r_Point::r_Point(r_Range p1, r_Range p2, r_Range p3)
    : dimensionality(3),
      streamInitCnt(3)
{
    points    = new r_Range[dimensionality];
    points[0] = p1;
    points[1] = p2;
    points[2] = p3;
}


r_Point::r_Point(r_Range p1, r_Range p2, r_Range p3, r_Range p4)
    : dimensionality(4),
      streamInitCnt(4)
{
    points    = new r_Range[dimensionality];
    points[0] = p1;
    points[1] = p2;
    points[2] = p3;
    points[3] = p4;
}


r_Point::r_Point(r_Range p1, r_Range p2, r_Range p3, r_Range p4, r_Range p5)
    : dimensionality(5),
      streamInitCnt(5)
{
    points    = new r_Range[dimensionality];
    points[0] = p1;
    points[1] = p2;
    points[2] = p3;
    points[3] = p4;
    points[4] = p5;
}

r_Point::r_Point(const std::vector<r_Range>& pointArg)
    : dimensionality(pointArg.size()),
      streamInitCnt(dimensionality)
{
    points = new r_Range[dimensionality];
    for(size_t i = 0; i < dimensionality; i++)
    {
        points[i] = pointArg[i];
    }
}


r_Point::r_Point()
    : points(NULL),
      dimensionality(0),
      streamInitCnt(0)
{
}


r_Point::r_Point(const r_Point& pt)
    :   points(new r_Range[pt.dimensionality]),
        dimensionality(pt.dimensionality),
        streamInitCnt(pt.streamInitCnt)
{
    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        points[i] = pt[i];
    }
}


r_Point::~r_Point()
{
    if (points)
    {
        delete[] points;
        points = NULL;
    }
}


r_Range
r_Point::operator[](r_Dimension i) const
{
    if (i >= dimensionality)
    {
        LFATAL << "r_Point::operator[](" << i << ") const dimension out of bounds (" << dimensionality << ")";
        throw r_Eindex_violation(0, dimensionality - 1, i);
    }

    return points[i];
}


r_Range&
r_Point::operator[](r_Dimension i)
{
    if (i >= dimensionality)
    {
        LFATAL << "r_Point::operator[](" << i << ") dimension out of bounds (" << dimensionality << ")";
        throw r_Eindex_violation(0, dimensionality - 1, i);
    }

    return points[i];
}


const r_Point&
r_Point::operator=(const r_Point& pt)
{
    if (this != &pt)
    {
        if (points && dimensionality != pt.dimension())
        {
            delete[] points;
            points = NULL;
        }

        dimensionality = pt.dimension();
        streamInitCnt  = dimensionality;

        if (!points)
        {
            points = new r_Range[ dimensionality ];
        }

        for (r_Dimension i = 0; i < dimensionality; i++)
        {
            points[i] = pt[i];
        }

    }

    return *this;
}



bool
r_Point::operator==(const r_Point& pt) const
{
    bool returnValue = false;

    if (dimensionality == pt.dimensionality)
    {
        returnValue = true;

        for (r_Dimension i = 0; i < dimensionality && returnValue ; i++)
        {
            if (points[i] != pt[i])
            {
                returnValue = false;
                break;
            }
        }
    }

    return returnValue;
}



bool
r_Point::operator!=(const r_Point& pt) const
{
    return !operator==(pt);
}

bool
r_Point::operator < (const r_Point& pt) const
{
    if(this->dimensionality != pt.dimension())
    {
        LFATAL << "r_Point::operator<(" << pt << ") dimensions (" << dimensionality << ") do not match";
        throw r_Edim_mismatch(dimensionality, pt.dimension());
    }
    bool returnValue = true;
    for(r_Dimension dim = 0; dim < dimensionality; dim++)
    {
        returnValue &= this->points[dim] < pt[dim];
    }

    return returnValue;
}
bool
r_Point::operator > (const r_Point& pt) const
{
    if(this->dimensionality != pt.dimension())
    {
        LFATAL << "r_Point::operator<(" << pt << ") dimensions (" << dimensionality << ") do not match";
        throw r_Edim_mismatch(dimensionality, pt.dimension());
    }
    bool returnValue = true;
    for(r_Dimension dim = 0; dim < dimensionality; dim++)
    {
        returnValue &= this->points[dim] > pt[dim];
    }

    return returnValue;
}

bool
r_Point::operator >= (const r_Point& pt) const
{
    if(this->dimensionality != pt.dimension())
    {
        LFATAL << "r_Point::operator<(" << pt << ") dimensions (" << dimensionality << ") do not match";
        throw r_Edim_mismatch(dimensionality, pt.dimension());
    }
    bool returnValue = true;
    for(r_Dimension dim = 0; dim < dimensionality; dim++)
    {
        returnValue &= this->points[dim] >= pt[dim];
    }

    return returnValue;
}

bool
r_Point::operator <= (const r_Point& pt) const
{
    if(this->dimensionality != pt.dimension())
    {
        LFATAL << "r_Point::operator<(" << pt << ") dimensions (" << dimensionality << ") do not match";
        throw r_Edim_mismatch(dimensionality, pt.dimension());
    }
    bool returnValue = true;
    for(r_Dimension dim = 0; dim < dimensionality; dim++)
    {
        returnValue &= this->points[dim] <= pt[dim];
    }

    return returnValue;
}

r_Point
r_Point::operator+(const r_Point& pt) const
{
    if (dimensionality != pt.dimension())
    {
        LFATAL << "r_Point::operator+(" << pt << ") dimensions (" << dimensionality << ") do not match";
        throw r_Edim_mismatch(dimensionality, pt.dimension());
    }

    r_Point result(dimensionality);

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result[i] = points[i] + pt[i];
    }

    return result;
}

r_Point
r_Point::operator-(const r_Point& pt) const
{
    if (dimensionality != pt.dimension())
    {
        LFATAL << "r_Point::operator-(" << pt << ") dimensions (" << dimensionality << ") do not match";
        throw r_Edim_mismatch(dimensionality, pt.dimension());
    }

    r_Point result(dimensionality);

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result[i] = points[i] - pt[i];
    }

    return result;
}

r_Point
r_Point::operator*(const r_Point& pt) const
{
    if (dimensionality != pt.dimension())
    {
        LFATAL << "r_Point::operator*(" << pt << ") dimensions (" << dimensionality << ") do not match";
        throw r_Edim_mismatch(dimensionality, pt.dimension());
    }

    r_Point result(dimensionality);

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result[i] = points[i] * pt[i];
    }

    return result;
}

r_Point
r_Point::operator*(const r_Range newElement) const
{
    r_Point result(dimensionality);

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result[i] = points[i] * newElement;
    }

    return result;
}

r_Point
r_Point::indexedMap(const std::vector<r_Dimension>& vecArg) const
{
    r_Point retVal(vecArg.size());
    for(size_t i = 0; i < vecArg.size(); i++)
    {
        retVal[i] = points[vecArg[i]];
    }
    return retVal;
}


r_Range
r_Point::dotProduct(const r_Point& pt) const
{
    if (dimensionality != pt.dimension())
    {
        LFATAL << "r_Point::operator*(" << pt << ") dimensions (" << dimensionality << ") do not match";
        throw r_Edim_mismatch(dimensionality, pt.dimension());
    }

    r_Range result = 0;

    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        result += points[i] * pt[i];
    }

    return result;
}

std::vector<r_Range>
r_Point::getVector() const
{
    std::vector<r_Range> returnVal;
    returnVal.reserve(dimensionality);
    for(size_t i = 0; i < dimensionality; i++)
    {
        returnVal.emplace_back(points[i]);
    }
    return returnVal;
}

void
r_Point::print_status(std::ostream& s) const
{
    s << "[";

    if (dimensionality > 0)
    {
        for (r_Dimension i = 0; i < dimensionality - 1; i++)
        {
            s << points[i] << ",";
        }

        s << points[dimensionality - 1];
    }

    s << "]";
}


char*
r_Point::get_string_representation() const
{
    // initialize string stream
    std::ostringstream domainStream;

    // write into string stream
    domainStream << (*this);

    // allocate memory taking the final string
    char* returnString = strdup(domainStream.str().c_str());

    return returnString;
}

std::string 
r_Point::to_string() const
{
    char* stringRep = this->get_string_representation();
    std::string returnValue(stringRep);
    std::free(stringRep);
    return returnValue;
}

std::ostream& operator<<(std::ostream& s, const r_Point& d)
{
    d.print_status(s);
    return s;
}

