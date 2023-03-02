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
#include "point.hh"
#include "raslib/error.hh"
#include <logging.hh>  // for Writer, CFATAL, LOG
#include <string.h>    // for strchr, strdup
#include <iostream>    // for operator<<, basic_ostream<>::__ost...
#include <stdexcept>   // for runtime_error
#include <string>      // for basic_string

r_Point::r_Point(char *stringRep)
{
    // for parsing the string
    std::istringstream str(stringRep);

    // calculate dimensionality
    size_t dim = 0;
    char *p = stringRep;
    while ((p = strchr(++p, ',')))
        dim++;
    points.reserve(dim);

    char charToken;
    str >> charToken;
    if (charToken != '[')
        throw r_Error(NOPOINT);

    for (r_Dimension i = 0; i < dim; i++)
    {
        r_Range valueToken;
        str >> valueToken;
        points.push_back(valueToken);

        if (i < dim - 1)
        {
            str >> charToken;
            if (charToken != ',')
                throw r_Error(NOPOINT);
        }
    }
}

r_Point::r_Point(r_Range p1)
    : points{p1}, streamIndex{1}
{
}

r_Point::r_Point(r_Dimension dim)
    : points(dim, value_type{})
{
}

r_Point::r_Point(r_Range p1, r_Range p2)
    : points{p1, p2}, streamIndex{2}
{
}

r_Point::r_Point(r_Range p1, r_Range p2, r_Range p3)
    : points{p1, p2, p3}, streamIndex{3}
{
}

r_Point::r_Point(r_Range p1, r_Range p2, r_Range p3, r_Range p4)
    : points{p1, p2, p3, p4}, streamIndex{4}
{
}

r_Point::r_Point(r_Range p1, r_Range p2, r_Range p3, r_Range p4, r_Range p5)
    : points{p1, p2, p3, p4, p5}, streamIndex{5}
{
}

r_Point::r_Point(std::vector<r_Range> pointArg)
    : points{std::move(pointArg)}, streamIndex{points.size()}
{
}

r_Point &r_Point::operator<<(r_Range newElement)
{
    if (streamIndex < dimension())
    {
        points[streamIndex++] = newElement;
    }
    else
    {
        LERROR << "cannot add new element to point, already fully initialized.";
        throw r_Einit_overflow();
    }
    return *this;
}

r_Range r_Point::operator[](r_Dimension i) const
{
    if (i < points.size())
        return points[i];
    else
    {
        LERROR << "dimension (" << i << ") out of bounds (" << points.size() << ")";
        throw r_Eindex_violation(0l, static_cast<r_Range>(points.size()) - 1, i);
    }
}

r_Range &r_Point::operator[](r_Dimension i)
{
    if (i < points.size())
        return points[i];
    else
    {
        LERROR << "dimension (" << i << ") out of bounds (" << points.size() << ")";
        throw r_Eindex_violation(0l, static_cast<r_Range>(points.size()) - 1, i);
    }
}

r_Range r_Point::at(r_Dimension i) const
{
    if (i < points.size())
        return points[i];
    else
    {
        LERROR << "dimension (" << i << ") out of bounds (" << points.size() << ")";
        throw r_Eindex_violation(0l, static_cast<r_Range>(points.size()) - 1, i);
    }
}

r_Range &r_Point::at(r_Dimension i)
{
    if (i < points.size())
        return points[i];
    else
    {
        LERROR << "dimension (" << i << ") out of bounds (" << points.size() << ")";
        throw r_Eindex_violation(0l, static_cast<r_Range>(points.size()) - 1, i);
    }
}

bool r_Point::operator==(const r_Point &pt) const
{
    return pt.points == points;
}

bool r_Point::operator!=(const r_Point &pt) const
{
    return !operator==(pt);
}

bool r_Point::operator<(const r_Point &pt) const
{
    checkDimensionMatch(pt);
    bool returnValue = true;
    for (r_Dimension dim = 0; dim < dimension(); dim++)
        returnValue &= this->points[dim] < pt[dim];
    return returnValue;
}
bool r_Point::operator>(const r_Point &pt) const
{
    checkDimensionMatch(pt);
    bool returnValue = true;
    for (r_Dimension dim = 0; dim < dimension(); dim++)
        returnValue &= this->points[dim] > pt[dim];
    return returnValue;
}

bool r_Point::operator>=(const r_Point &pt) const
{
    checkDimensionMatch(pt);
    bool returnValue = true;
    for (r_Dimension dim = 0; dim < dimension(); dim++)
        returnValue &= this->points[dim] >= pt[dim];
    return returnValue;
}

bool r_Point::operator<=(const r_Point &pt) const
{
    checkDimensionMatch(pt);
    bool returnValue = true;
    for (r_Dimension dim = 0; dim < dimension(); dim++)
        returnValue &= this->points[dim] <= pt[dim];
    return returnValue;
}

r_Point r_Point::operator+(const r_Point &pt) const
{
    checkDimensionMatch(pt);
    r_Point result(dimension());
    for (r_Dimension i = 0; i < dimension(); i++)
        result[i] = points[i] + pt[i];
    return result;
}

r_Point r_Point::operator-(const r_Point &pt) const
{
    checkDimensionMatch(pt);
    r_Point result(dimension());
    for (r_Dimension i = 0; i < dimension(); i++)
        result[i] = points[i] - pt[i];
    return result;
}

r_Point r_Point::operator*(const r_Point &pt) const
{
    checkDimensionMatch(pt);
    r_Point result(dimension());
    for (r_Dimension i = 0; i < dimension(); i++)
        result[i] = points[i] * pt[i];
    return result;
}

r_Point
r_Point::operator*(const r_Range newElement) const
{
    r_Point result(dimension());
    for (r_Dimension i = 0; i < dimension(); i++)
        result[i] = points[i] * newElement;
    return result;
}

std::vector<r_Range>
r_Point::get_coordinates() const
{
    std::vector<r_Range> returnVal;
    returnVal.reserve(dimension());
    for (size_t i = 0; i < dimension(); i++)
        returnVal.emplace_back(points[i]);
    return returnVal;
}

void r_Point::print_status(std::ostream &s) const
{
    s << "[";
    for (r_Dimension i = 0; i < dimension(); i++)
    {
        if (i > 0) s << ",";
        s << points[i];
    }
    s << "]";
}

char *r_Point::get_string_representation() const
{
    auto ret = to_string();
    return strdup(ret.c_str());
}

std::string
r_Point::to_string(bool wkt) const
{
    std::string ret;
    for (auto p: points)
    {
        if (!ret.empty())
            ret += wkt ? " " : ",";
        ret += std::to_string(p);
    }
    return !wkt ? "[" + ret + "]" : ret;
}

void r_Point::checkDimensionMatch(const r_Point &pt) const
{
    if (dimension() != pt.dimension())
    {
        LERROR << "dimension of given point (" << dimension()
               << ") does not match dimension of this point (" << dimension() << ").";
        throw r_Edim_mismatch(dimension(), pt.dimension());
    }
}

std::ostream &operator<<(std::ostream &s, const r_Point &d)
{
    d.print_status(s);
    return s;
}

r_Dimension
r_Point::dimension() const
{
    return static_cast<r_Dimension>(points.size());
}

int r_Point::compare_with(const r_Point &p) const
{
    if (dimension() != p.dimension())
        return -2;

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        if (points[i] > p[i])
            return 1;
        if (points[i] < p[i])
            return -1;
    }
    return 0;
}
