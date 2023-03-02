/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/**
 * SOURCE: sinterval.cc
 *
 * MODULE: raslib
 * CLASS:  r_Sinterval
 *
 * COMMENTS:
 *
*/

#include "raslib/sinterval.hh"
#include "raslib/mddtypes.hh"  // for r_Range, r_Bytes
#include "raslib/error.hh"

#include <logging.hh>  // for Writer, CFATAL, LOG
#include <string.h>    // for strdup
#include <cassert>
#include <algorithm>  // for max, min
#include <stdexcept>  // for runtime_error
#include <string>     // for basic_string

using namespace std;

using Bound = r_Sinterval::BoundType;
using Offset = r_Sinterval::OffsetType;

r_Sinterval::r_Sinterval(const char *stringRep)
{
    if (!stringRep)
    {
        throw r_Error(NOINTERVAL, "given null string");
    }

    // for parsing the string
    std::istringstream str(stringRep);

    char charToken;
    str >> charToken;
    if (charToken == '*')
    {
        set_low('*');
    }
    else
    {
        str.putback(charToken);
        r_Range valueToken;
        str >> valueToken;
        set_low(valueToken);
    }

    str >> charToken;
    if (charToken != ':')
    {
        LERROR << "Cannot create interval from string (" << stringRep << "), expected pattern a:b";
        throw r_Error(NOINTERVAL, stringRep);
    }

    str >> charToken;
    if (charToken == '*')
    {
        set_high('*');
    }
    else
    {
        str.putback(charToken);
        r_Range valueToken;
        str >> valueToken;
        set_high(valueToken);
    }
}

r_Sinterval::r_Sinterval(r_Range lo, r_Range hi)
    : lower_bound(lo), upper_bound(hi), low_fixed(true), high_fixed(true)
{
    if (lo > hi)
    {
        throw r_Einvalid_interval_bounds(lo, hi);
    }
}

r_Sinterval::r_Sinterval(r_Range point)
    : lower_bound(point), upper_bound(point), low_fixed(true), high_fixed(true), slice(true)
{
}

r_Sinterval::r_Sinterval(char, r_Range newHigh)
    : upper_bound(newHigh), high_fixed(true)
{
}

r_Sinterval::r_Sinterval(r_Range newLow, char)
    : lower_bound(newLow), low_fixed(true)
{
}

r_Sinterval::r_Sinterval(char, char)
{
}

bool r_Sinterval::operator==(const r_Sinterval &o) const
{
#define BOUNDS_EQUAL(bound, fixed) \
    (fixed ? (o.fixed && bound == o.bound) : !o.fixed)

    return BOUNDS_EQUAL(lower_bound, low_fixed) &&
           BOUNDS_EQUAL(upper_bound, high_fixed) &&
           slice == o.slice;

#undef BOUNDS_EQUAL
}

bool r_Sinterval::operator!=(const r_Sinterval &o) const
{
    return !operator==(o);
}

Offset r_Sinterval::get_extent() const
{
    assert(upper_bound >= lower_bound);

    if (low_fixed && high_fixed)
    {
        return static_cast<Offset>(upper_bound - lower_bound + 1);
    }
    else
    {
        // TODO: eliminate this check into an assert
        throw r_Error(INTERVALOPEN, "cannot get extent of interval " + to_string());
    }
}

bool r_Sinterval::is_fixed() const noexcept
{
    return low_fixed && high_fixed;
}

void r_Sinterval::set_low(r_Range newLow)
{
    if (high_fixed && newLow > upper_bound)
    {
        throw r_Einvalid_interval_bounds(newLow, upper_bound);
    }
    lower_bound = newLow;
    low_fixed = true;
}

void r_Sinterval::set_high(r_Range newHigh)
{
    if (low_fixed && newHigh < lower_bound)
    {
        throw r_Einvalid_interval_bounds(lower_bound, newHigh);
    }
    upper_bound = newHigh;
    high_fixed = true;
}

void r_Sinterval::set_interval(r_Range newLow, r_Range newHigh)
{
    if (newLow > newHigh)
    {
        throw r_Einvalid_interval_bounds(newLow, newHigh);
    }
    lower_bound = newLow;
    upper_bound = newHigh;
    low_fixed = true;
    high_fixed = true;
}

void r_Sinterval::set_interval(char, r_Range newHigh) noexcept
{
    lower_bound = 0;
    upper_bound = newHigh;
    low_fixed = false;
    high_fixed = true;
}

void r_Sinterval::set_interval(r_Range newLow, char) noexcept
{
    lower_bound = newLow;
    upper_bound = 0;
    low_fixed = true;
    high_fixed = false;
}

void r_Sinterval::set_interval(char, char) noexcept
{
    lower_bound = 0;
    upper_bound = 0;
    low_fixed = false;
    high_fixed = false;
}

void r_Sinterval::set_slice() noexcept
{
    slice = true;
}

const string &r_Sinterval::get_axis_name() const
{
    return axis_name;
}

void r_Sinterval::set_axis_name(const std::string &axis_name_arg)
{
    axis_name = axis_name_arg;
}

bool r_Sinterval::has_axis_name() const
{
    return !axis_name.empty();
}

Offset r_Sinterval::get_offset_to(r_Sinterval::BoundType o) const noexcept
{
    assert(o >= lower_bound);
    assert(low_fixed);
    return static_cast<Offset>(o - lower_bound);
}

Offset r_Sinterval::get_offset_to(const r_Sinterval &o) const noexcept
{
    assert(o.is_low_fixed());
    return get_offset_to(o.low());
}

r_Sinterval r_Sinterval::translate_by(Bound offset) const
{
    assert(low_fixed && high_fixed);
    return slice ? r_Sinterval(lower_bound + offset)
                 : r_Sinterval(lower_bound + offset, upper_bound + offset);
}

bool r_Sinterval::intersects_with(const r_Sinterval &interval) const
{
    int classnr = classify(*this, interval);

    return classnr != 1 && classnr != 6 && classnr != 16 && classnr != 21 &&
           classnr != 26 && classnr != 31 && classnr != 34 && classnr != 37;
}

r_Sinterval &r_Sinterval::union_of(const r_Sinterval &a, const r_Sinterval &b)
{
    *this = calc_union(a, b);
    return *this;
}
r_Sinterval &r_Sinterval::union_with(const r_Sinterval &interval)
{
    *this = calc_union(interval, *this);
    return *this;
}
r_Sinterval &r_Sinterval::operator+=(const r_Sinterval &interval)
{
    *this = calc_union(interval, *this);
    return *this;
}
r_Sinterval r_Sinterval::create_union(const r_Sinterval &interval) const
{
    return calc_union(interval, *this);
}
r_Sinterval r_Sinterval::operator+(const r_Sinterval &interval) const
{
    return calc_union(interval, *this);
}

r_Sinterval &r_Sinterval::difference_of(const r_Sinterval &a, const r_Sinterval &b)
{
    *this = calc_difference(a, b);
    return *this;
}
r_Sinterval &r_Sinterval::difference_with(const r_Sinterval &interval)
{
    *this = calc_difference(interval, *this);
    return *this;
}
r_Sinterval &r_Sinterval::operator-=(const r_Sinterval &interval)
{
    *this = calc_difference(interval, *this);
    return *this;
}
r_Sinterval r_Sinterval::create_difference(const r_Sinterval &interval) const
{
    return calc_difference(interval, *this);
}
r_Sinterval r_Sinterval::operator-(const r_Sinterval &interval) const
{
    return calc_difference(interval, *this);
}

r_Sinterval &r_Sinterval::intersection_of(const r_Sinterval &a, const r_Sinterval &b)
{
    *this = calc_intersection(a, b);
    return *this;
}
r_Sinterval &r_Sinterval::intersection_with(const r_Sinterval &interval)
{
    *this = calc_intersection(interval, *this);
    return *this;
}
r_Sinterval &r_Sinterval::operator*=(const r_Sinterval &interval)
{
    *this = calc_intersection(interval, *this);
    return *this;
}
r_Sinterval r_Sinterval::create_intersection(const r_Sinterval &interval) const
{
    return calc_intersection(interval, *this);
}
r_Sinterval r_Sinterval::operator*(const r_Sinterval &interval) const
{
    return calc_intersection(interval, *this);
}

bool r_Sinterval::inside_of(const r_Sinterval &interval) const
{
    int classnr = classify(*this, interval);

    return classnr == 5 || classnr == 11 || classnr == 12 || classnr == 13 || classnr == 15 ||
           classnr == 18 || classnr == 20 || classnr == 22 || classnr == 23 || classnr == 24 || classnr == 25 ||
           classnr == 29 || classnr == 30 || classnr == 36 || classnr == 39 || classnr == 40 || classnr == 41 ||
           (classnr >= 44 && classnr <= 52);
}

r_Sinterval &r_Sinterval::closure_of(const r_Sinterval &a, const r_Sinterval &b)
{
    *this = calc_closure(a, b);
    return *this;
}
r_Sinterval &r_Sinterval::closure_with(const r_Sinterval &interval)
{
    *this = calc_closure(interval, *this);
    return *this;
}
r_Sinterval r_Sinterval::create_closure(const r_Sinterval &interval) const
{
    return calc_closure(interval, *this);
}
void r_Sinterval::print_status(std::ostream &s) const
{
    if (has_axis_name())
    {
        s << axis_name << "(";
    }
    s << (low_fixed ? std::to_string(lower_bound) : "*");
    s << ":";
    s << (high_fixed ? std::to_string(upper_bound) : "*");
    if (has_axis_name())
    {
        s << ")";
    }
}

r_Bytes r_Sinterval::get_storage_size() const
{
    return (2 * (sizeof(r_Range) + sizeof(bool)));
}

r_Sinterval r_Sinterval::calc_union(const r_Sinterval &a, const r_Sinterval &b) const
{
    r_Sinterval result;

    switch (classify(a, b))
    {
    case 2:
    case 7:
    case 9:
    case 12:
    case 22:
    case 23:
    case 27:
    case 28:
    case 35:
    case 36:
        // result = [a1:b2]
        if (a.is_low_fixed())
            result.set_low(a.low());
        else
            result.set_low('*');
        if (b.is_high_fixed())
            result.set_high(b.high());
        else
            result.set_high('*');
        break;

    case 4:
    case 8:
    case 10:
    case 13:
    case 17:
    case 18:
    case 32:
    case 33:
    case 38:
    case 39:
        // result = [b1:a2]
        if (b.is_low_fixed())
            result.set_low(b.low());
        else
            result.set_low('*');
        if (a.is_high_fixed())
            result.set_high(a.high());
        else
            result.set_high('*');
        break;

    case 3:
    case 11:
    case 14:
    case 15:
    case 19:
    case 20:
    case 41:
    case 42:
    case 43:
    case 44:
    case 46:
    case 48:
    case 49:
    case 52:
        result = a;
        break;

    case 5:
    case 24:
    case 25:
    case 29:
    case 30:
    case 40:
    case 45:
    case 47:
    case 50:
    case 51:
        result = b;
        break;

    default:  // case in { 1, 6, 16, 21, 26, 31, 34, 37 }
    {
        throw r_Error(NOINTERVAL, "cannot calculate union of intervals " + a.to_string() + " and " + b.to_string());
    }
    }

    if (a.is_slice() && b.is_slice())
        result.set_slice();

    if (a.has_axis_name())
        result.set_axis_name(a.get_axis_name());

    return result;
}

r_Sinterval
r_Sinterval::calc_difference(const r_Sinterval &a, const r_Sinterval &b) const
{
    r_Sinterval result;

    switch (classify(a, b))
    {
    case 2:
    case 9:
    case 20:
    case 23:
    case 28:
    case 36:
    case 39:
    case 43:
    case 49:
        // result = [a1:b1]
        if (a.is_low_fixed())
            result.set_low(a.low());
        else
            result.set_low('*');
        if (b.is_low_fixed())
            result.set_high(b.low());
        else
            result.set_high('*');
        break;

    case 1:
    case 6:
    case 7:
    case 8:
    case 16:
    case 17:
    case 21:
    case 22:
    case 26:
    case 27:
    case 31:
    case 32:
    case 34:
    case 35:
    case 37:
    case 38:
        result = a;
        break;

    case 4:
    case 10:
    case 15:
    case 18:
    case 33:
    case 42:
    case 48:
        // result = [b2:a2]
        if (b.is_high_fixed())
            result.set_low(b.high());
        else
            result.set_low('*');
        if (a.is_high_fixed())
            result.set_high(a.high());
        else
            result.set_high('*');
        break;

    default:  // case in { 3, 5, 11, 12, 13, 14, 19, 24, 25, 29, 30, 40, 41, 44, 45, 46, 47, 50, 51, 52 }
    {
        throw r_Error(NOINTERVAL, "cannot calculate difference of intervals " + a.to_string() + " and " + b.to_string());
    }
    }

    if (a.has_axis_name())
        result.set_axis_name(a.get_axis_name());

    return result;
}

r_Sinterval
r_Sinterval::calc_intersection(const r_Sinterval &a, const r_Sinterval &b) const
{
    r_Sinterval result;

    switch (classify(a, b))
    {
    case 4:
    case 18:
    case 33:
    case 39:
        // result = [a1:b2]
        if (a.is_low_fixed())
            result.set_low(a.low());
        else
            result.set_low('*');
        if (b.is_high_fixed())
            result.set_high(b.high());
        else
            result.set_high('*');
        break;

    case 2:
    case 23:
    case 28:
    case 36:
        // result = [b1:a2]
        if (b.is_low_fixed())
            result.set_low(b.low());
        else
            result.set_low('*');
        if (a.is_high_fixed())
            result.set_high(a.high());
        else
            result.set_high('*');
        break;

    case 5:
    case 11:
    case 12:
    case 13:
    case 24:
    case 25:
    case 29:
    case 30:
    case 40:
    case 41:
    case 44:
    case 45:
    case 47:
    case 50:
    case 51:
    case 52:
        result = a;
        break;

    case 3:
    case 9:
    case 10:
    case 14:
    case 15:
    case 19:
    case 20:
    case 42:
    case 43:
    case 46:
    case 48:
    case 49:
        result = b;
        break;

    case 7:
    case 22:
    case 27:
    case 35:
        // result = [a2:a2]
        if (a.is_high_fixed())
            result.set_interval(a.high(), a.high());
        else
            result.set_interval('*', '*');
        break;

    case 8:
    case 17:
    case 32:
    case 38:
        // result = [b2:b2]
        if (b.is_high_fixed())
            result.set_interval(b.high(), b.high());
        else
            result.set_interval('*', '*');
        break;

    default:  // case in { 1, 6, 16, 21, 26, 31, 34, 37 }
        throw r_Error(NOINTERVAL, "cannot calculate intersection of intervals " + a.to_string() + " and " + b.to_string());
    }

    if (a.is_slice() || b.is_slice())
        result.set_slice();

    if (a.has_axis_name())
        result.set_axis_name(a.get_axis_name());

    return result;
}

r_Sinterval
r_Sinterval::calc_closure(const r_Sinterval &a, const r_Sinterval &b) const
{
    r_Sinterval closure;
    if (!a.is_low_fixed() || !b.is_low_fixed())
        closure.set_low('*');
    else
        closure.set_low(std::min(a.low(), b.low()));

    if (!a.is_high_fixed() || !b.is_high_fixed())
        closure.set_high('*');
    else
        closure.set_high(std::max(a.high(), b.high()));

    if (a.has_axis_name())
        closure.set_axis_name(a.get_axis_name());

    return closure;
}

/*************************************************************
 * Method name...: classify
 *
 * Arguments.....: Two intervals for the classification.
 * Return value..: The classification class number (1..52).
 * Description...: The method classifies the two intervals into
 *                 one of 13 classes according to their spatial
 *                 relationship. Based on the classification, the
 *                 result of the operations union, difference,
 *                 and intersection can be calculated as shown
 *                 in the table in file sinterval.hh:
 ************************************************************/

int r_Sinterval::classify(const r_Sinterval &a, const r_Sinterval &b) const
{
    int classification = 0;

    if (a.is_low_fixed() && a.is_high_fixed() && b.is_low_fixed() && b.is_high_fixed())
    {
        // classification 1..13

        if (a.low() < b.low())
        {
            if (a.high() < b.high())
            {
                if (a.high() < b.low())
                {
                    classification = 1;
                }
                else if (a.high() == b.low())
                {
                    classification = 7;
                }
                else
                {
                    classification = 2;
                }
            }
            else if (a.high() == b.high())
            {
                classification = 9;
            }
            else
            {
                classification = 3;
            }
        }
        else if (a.low() == b.low())
        {
            if (a.high() < b.high())
            {
                classification = 12;
            }
            else if (a.high() == b.high())
            {
                classification = 11;
            }
            else
            {
                classification = 10;
            }
        }
        else if (a.high() < b.high())
        {
            classification = 5;
        }
        else if (a.high() == b.high())
        {
            classification = 13;
        }
        else
        {
            if (a.low() < b.high())
            {
                classification = 4;
            }
            else if (a.low() == b.high())
            {
                classification = 8;
            }
            else
            {
                classification = 6;
            }
        }
    }
    else if (a.is_low_fixed() && !a.is_high_fixed() && b.is_low_fixed() && b.is_high_fixed())
    {
        // classification 14..18

        if (a.low() < b.low())
        {
            classification = 14;
        }
        else if (a.low() == b.low())
        {
            classification = 15;
        }
        else
        {
            if (b.high() < a.low())
            {
                classification = 16;
            }
            else if (b.high() == a.low())
            {
                classification = 17;
            }
            else
            {
                classification = 18;
            }
        }
    }
    else if (!a.is_low_fixed() && a.is_high_fixed() && b.is_low_fixed() && b.is_high_fixed())
    {
        // classification 19..23

        if (a.high() > b.high())
        {
            classification = 19;
        }
        else if (a.high() == b.high())
        {
            classification = 20;
        }
        else
        {
            if (a.high() < b.low())
            {
                classification = 21;
            }
            else if (a.high() == b.low())
            {
                classification = 22;
            }
            else
            {
                classification = 23;
            }
        }
    }
    else if (a.is_low_fixed() && a.is_high_fixed() && b.is_low_fixed() && !b.is_high_fixed())
    {
        // classification 24..28

        if (b.low() < a.low())
        {
            classification = 24;
        }
        else if (b.low() == a.low())
        {
            classification = 25;
        }
        else
        {
            if (a.high() < b.low())
            {
                classification = 26;
            }
            else if (a.high() == b.low())
            {
                classification = 27;
            }
            else
            {
                classification = 28;
            }
        }
    }
    else if (a.is_low_fixed() && a.is_high_fixed() && !b.is_low_fixed() && b.is_high_fixed())
    {
        // classification 29..33

        if (b.high() > a.high())
        {
            classification = 29;
        }
        else if (b.high() == a.high())
        {
            classification = 30;
        }
        else
        {
            if (b.high() < a.low())
            {
                classification = 31;
            }
            else if (b.high() == a.low())
            {
                classification = 32;
            }
            else
            {
                classification = 33;
            }
        }
    }
    else if (!a.is_low_fixed() && a.is_high_fixed() && b.is_low_fixed() && !b.is_high_fixed())
    {
        // classification 34..36

        if (a.high() < b.low())
        {
            classification = 34;
        }
        else if (a.high() == b.low())
        {
            classification = 35;
        }
        else
        {
            classification = 36;
        }
    }
    else if (a.is_low_fixed() && !a.is_high_fixed() && !b.is_low_fixed() && b.is_high_fixed())
    {
        // classification 37..39

        if (b.high() < a.low())
        {
            classification = 37;
        }
        else if (b.high() == a.low())
        {
            classification = 38;
        }
        else
        {
            classification = 39;
        }
    }
    else if (!a.is_low_fixed() && a.is_high_fixed() && !b.is_low_fixed() && b.is_high_fixed())
    {
        // classification 40..42

        if (a.high() < b.high())
        {
            classification = 40;
        }
        else if (a.high() == b.high())
        {
            classification = 41;
        }
        else
        {
            classification = 42;
        }
    }
    else if (a.is_low_fixed() && !a.is_high_fixed() && b.is_low_fixed() && !b.is_high_fixed())
    {
        // classification 43..45

        if (a.low() < b.low())
        {
            classification = 43;
        }
        else if (a.low() == b.low())
        {
            classification = 44;
        }
        else
        {
            classification = 45;
        }
    }
    else if (!a.is_low_fixed() && !a.is_high_fixed() && b.is_low_fixed() && b.is_high_fixed())
    {
        classification = 46;
    }
    else if (a.is_low_fixed() && a.is_high_fixed() && !b.is_low_fixed() && !b.is_high_fixed())
    {
        classification = 47;
    }
    else if (!a.is_low_fixed() && !a.is_high_fixed() && !b.is_low_fixed() && b.is_high_fixed())
    {
        classification = 48;
    }
    else if (!a.is_low_fixed() && !a.is_high_fixed() && b.is_low_fixed() && !b.is_high_fixed())
    {
        classification = 49;
    }
    else if (!a.is_low_fixed() && a.is_high_fixed() && !b.is_low_fixed() && !b.is_high_fixed())
    {
        classification = 50;
    }
    else if (a.is_low_fixed() && !a.is_high_fixed() && !b.is_low_fixed() && !b.is_high_fixed())
    {
        classification = 51;
    }
    else  //   !a.is_low_fixed() && !a.is_high_fixed() && !b.is_low_fixed() && !b.is_high_fixed()
    {
        classification = 52;
    }

    return classification;
}

char *r_Sinterval::get_string_representation() const
{
    std::ostringstream domainStream;
    domainStream << (*this);
    return strdup(domainStream.str().c_str());
}

string r_Sinterval::to_string() const
{
    std::string ret;
    ret = low_fixed ? std::to_string(lower_bound) : "*";
    if (!slice)
    {
        ret += ":";
        ret += high_fixed ? std::to_string(upper_bound) : "*";
    }
    if (has_axis_name())
    {
        ret = axis_name + "(" + ret + ")";
    }
    return ret;
}

std::ostream &operator<<(std::ostream &s, const r_Sinterval &d)
{
    d.print_status(s);
    return s;
}

r_Range
r_Sinterval::low() const noexcept
{
    return lower_bound;
}

r_Range
r_Sinterval::high() const noexcept
{
    return upper_bound;
}

bool r_Sinterval::is_low_fixed() const noexcept
{
    return low_fixed;
}

bool r_Sinterval::is_low_unbounded() const noexcept
{
    return !low_fixed;
}

bool r_Sinterval::is_high_fixed() const noexcept
{
    return high_fixed;
}

bool r_Sinterval::is_high_unbounded() const noexcept
{
    return !high_fixed;
}

bool r_Sinterval::is_slice() const noexcept
{
    return slice;
}

void r_Sinterval::set_low(char) noexcept
{
    lower_bound = 0;
    low_fixed = false;
}

void r_Sinterval::set_high(char) noexcept
{
    upper_bound = 0;
    high_fixed = false;
}
