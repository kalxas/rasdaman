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
 * SOURCE: overloadedDoubleVector.cc
 *
 * MODULE: raslib
 * CLASS:  r_PointDouble
 *
 * COMMENTS:
 *
*/

#include "pointdouble.hh"
#include "raslib/error.hh"

#include <logging.hh>

#include <string.h>
#include <iomanip>
#include <sstream>
#include <math.h>


using namespace std;

r_PointDouble::r_PointDouble(r_Dimension dim)
{
    points.reserve(dim);

    for (r_Dimension i = 0; i < dim; i++)
    {
        points.push_back(0);
    }
}

r_PointDouble::r_PointDouble(r_Dimension dim, double value)
{
    points.reserve(dim);

    for (r_Dimension i = 0; i < dim; i++)
    {
        points.push_back(value);
    }
}
r_PointDouble::r_PointDouble(const r_Point &pt)
{
    points.reserve(pt.dimension());

    for (r_Dimension i = 0; i < pt.dimension(); i++)
    {
        points.push_back(pt[i]);
    }
}

r_PointDouble::r_PointDouble(const vector<double> &content)
{
    points = content;
}

r_PointDouble::r_PointDouble(const r_PointDouble &vectorArg)
{
    points = vectorArg.getVectorContent();
}

/// destructor: cleanup dynamic memory
r_PointDouble::~r_PointDouble()
{
}

double &
r_PointDouble::operator[](size_t i)
{
    return points[i];
}

double
r_PointDouble::operator[](size_t i) const
{
    return points[i];
}

const r_PointDouble &
r_PointDouble::operator=(const r_PointDouble &pt)
{
    if (this != &pt)
    {
        this->points.clear();
        this->points = pt.getVectorContent();
    }

    return *this;
}

bool r_PointDouble::operator==(const r_PointDouble &pt) const
{
    return this->points == pt.getVectorContent();
}

bool r_PointDouble::operator!=(const r_PointDouble &pt) const
{
    return this->points != pt.getVectorContent();
}

bool r_PointDouble::operator<(const r_PointDouble &pt) const
{
    if (points.size() != pt.dimension())
    {
        throw r_Edim_mismatch(dimension(), pt.dimension());
    }

    for (size_t i = 0; i < points.size(); i++)
    {
        if (points[i] < pt.getVectorContent()[i])
        {
            return true;
        }
        else if (points[i] > pt.getVectorContent()[i])
        {
            return false;
        }
    }
    //all indices equal
    return false;
}


r_PointDouble
r_PointDouble::operator+(const r_PointDouble &pt) const
{
    if (points.size() != pt.dimension())
    {
        throw r_Edim_mismatch(dimension(), pt.dimension());
    }

    r_PointDouble result(points);

    for (size_t it = 0; it < result.dimension(); it++)
    {
        result[it] += pt[it];
    }

    return result;
}

r_PointDouble
r_PointDouble::operator-(const r_PointDouble &pt) const
{
    if (points.size() != pt.dimension())
    {

        throw r_Edim_mismatch(dimension(), pt.dimension());
    }

    r_PointDouble result(points);
    for (size_t it = 0; it < result.dimension(); it++)
    {
        result[it] -= pt[it];
    }

    return result;
}

r_PointDouble
r_PointDouble::operator*(const r_PointDouble &pt) const
{
    if (points.size() != pt.dimension())
    {
        throw r_Edim_mismatch(dimension(), pt.dimension());
    }

    r_PointDouble result(points);
    for (size_t it = 0; it < result.dimension(); it++)
    {
        result[it] *= pt[it];
    }

    return result;
}

r_PointDouble
r_PointDouble::operator*(const double scalarArg) const
{
    r_PointDouble result(points);
    for (size_t it = 0; it < result.dimension(); it++)
    {
        result[it] *= scalarArg;
    }

    return result;
}

double
r_PointDouble::dotProduct(const r_PointDouble &pt) const
{
    if (points.size() != pt.dimension())
    {
        throw r_Edim_mismatch(dimension(), pt.dimension());
    }

    double result = 0;

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        result += points[i] * pt[i];
    }

    return result;
}

void r_PointDouble::print_status(std::ostream &s) const
{
    s << "[";

    if (points.size() > 0)
    {
        for (size_t i = 0; i < points.size() - 1; i++)
        {
            s << std::setprecision(4) << points[i] << ",";
        }

        s << std::setprecision(4) << points[dimension() - 1];
    }

    s << "]";
}

r_Point r_PointDouble::toIntPoint() const
{
    r_Point returnValue(dimension());
    for (size_t i = 0; i < dimension(); i++)
    {
        returnValue[i] = static_cast<r_Range>(std::round(points[i]));
    }
    return returnValue;
}
