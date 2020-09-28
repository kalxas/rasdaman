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
 * SOURCE: Minterval.cc
 *
 * MODULE: raslib
 * CLASS:   r_Minterval
 *
 * COMMENTS:
 *
*/

#include "minterval.hh"
#include "raslib/error.hh"
#include "raslib/mddtypes.hh"           // for r_Dimension, r_Area, r_Range, r_Bytes
#include "odmgtypes.hh"

#include <logging.hh>

#include <cmath>
#include <cstring>
#include <cstdlib>
#include <cassert>               // for assert
#include <sstream>
#include <memory>
#include <ostream>              // for operator<<, basic_ostream, char_traits
#include <vector>
#include <deque>

using BoundType = r_Sinterval::BoundType;

using namespace std;

r_Minterval::r_Minterval(const r_Point &low, const r_Point &high)
{
    const auto dim = low.dimension();
    if (dim != high.dimension())
    {
        throw r_Edim_mismatch(dim, high.dimension());
    }

    intervals.reserve(dim);
    for (r_Dimension i = 0; i < dim; ++i)
    {
        if (low[i] > high[i])
        {
            throw r_Elimits_mismatch(low[i], high[i]);
        }
        intervals.emplace_back(low[i], high[i]);
    }
}

r_Minterval::r_Minterval(r_Dimension dim)
    : intervals(dim)
{
}

void r_Minterval::constructorinit(char *mIntStr)
{
    if (!mIntStr)
    {
        LERROR << "cannot create minterval from null string.";
        throw r_Eno_interval();
    }

    // calculate dimensionality
    char *p = mIntStr; // for counting ','
    r_Dimension dimensionality = 1;
    while ((p = strchr(++p, ',')))
    {
        dimensionality++;
    }

    // for parsing the string
    std::istringstream str(mIntStr);
    
    // check for left bracket '['
    char c = 0;
    str >> c;
    if (c != '[')
    {
        LERROR << "cannot create minterval from string (" << mIntStr
               << ") that is not of the pattern [a:b,c:d,..].";
        throw r_Eno_interval();
    }

    // for each dimension: get sinterval
    r_Sinterval sint;
    r_Range b = 0; // bound for Sinterval
    for (r_Dimension i = 0; i < dimensionality; i++)
    {
        // --- evaluate lower bound ------------------------------
        str >> c;      // test read first char
        if (c == '*')  // low bound is '*'
        {
            sint.set_low('*');
        }
        else  // low bound must be a number
        {
            str.putback(c);
            str >> b;  // read type r_Range
            if (!str)  // check for proper int recognition
            {
                LERROR << "minterval constructor from string (" << mIntStr
                       << ") failed on lower bound of dimension " << i;
                throw r_Eno_interval();
            }
            sint.set_low(b);  // store lo bound
        }

        // --- check for ':' between lower and upper bound -------
        str >> c;
        if (c != ':')
        {
            LERROR << "cannot create minterval from string (" << mIntStr
                   << ") that is not of the pattern [a:b,c:d,..].";
            throw r_Eno_interval();
        }

        // --- evaluate upper bound ------------------------------
        str >> c;
        if (c == '*')
        {
            sint.set_high('*');
        }
        else
        {
            str.putback(c);
            str >> b;
            if (!str)
            {
                LERROR << "minterval constructor from string (" << mIntStr
                       << ") failed on upper bound of dimension " << i;
                throw r_Eno_interval();
            }
            sint.set_high(b);
        }
        str >> c;

        // --- next dimension needs either ',' separator or ']' end tag
        if ((i != dimensionality - 1 && c != ',') || 
            (i == dimensionality - 1 && c != ']'))
        {
            LERROR << "cannot create minterval from string (" << mIntStr
                   << ") that is not of the pattern [a:b,c:d,..].";
            throw r_Eno_interval();
        }

        intervals.push_back(sint);
        sint.set_interval('*', '*');
    }
}

r_Minterval::r_Minterval(char *mIntStr)
{
    constructorinit(mIntStr);
}

r_Minterval::r_Minterval(std::vector<r_Sinterval> intervalsArg)
    : intervals{std::move(intervalsArg)}
{
    assert(!intervals.empty() && "Invalid minterval without sintervals.");
}

r_Minterval::r_Minterval(const char *mIntStr)
{
    char *temp = strdup(mIntStr);
    try
    {
        constructorinit(temp);
        free(temp);
        temp = nullptr;
    }
    catch (...)
    {
        free(temp);
        throw;
    }
}

const std::vector<std::string> &r_Minterval::getAxisNames() const
{
    return axisNames;
}

void r_Minterval::setAxisNames(std::vector<std::string> newAxisNames) {
  axisNames = std::move(newAxisNames);
}

r_Minterval &r_Minterval::operator<<(const r_Sinterval &newInterval)
{
    // TODO: should be assert
    if (streamInitCnt >= dimension())
    {
        LERROR << "cannot add interval (" << newInterval << "), domain is already full";
        throw r_Einit_overflow();
    }

    intervals[streamInitCnt++] = newInterval;
    return *this;
}

r_Minterval &r_Minterval::operator<<(r_Range p)
{
    // TODO: should be assert
    if (streamInitCnt >= dimension())
    {
        LERROR << "cannot add interval (" << p << ":" << p << "), domain is already full";
        throw r_Einit_overflow();
    }

    intervals[streamInitCnt++] = r_Sinterval(p, p);
    return *this;
}

r_Minterval r_Minterval::fromPoint(const r_Point &p)
{
    std::vector<r_Sinterval> axes;
    axes.reserve(p.dimension());
    for (DimType i = 0; i < p.dimension(); ++i)
      axes.emplace_back(p[i]);
    return r_Minterval{std::move(axes)};
}

bool r_Minterval::intersects_with(const r_Minterval &minterval) const
{
    if (dimension() != minterval.dimension())
    {
#ifdef RASDEBUG
        LDEBUG << "cannot check if " << this << " and " << minterval <<
               " intersect, mintervals do not share the same dimension.";
#endif
        return false;
    }

    // none of the interval pairs are allowed to be disjoint
    for (r_Dimension i = 0; i < dimension(); ++i)
        if (!intervals[i].intersects_with(minterval[i]))
            return false;

    return true;
}

bool r_Minterval::touches(const r_Minterval &right) const {
  assert(is_fixed() && right.is_fixed() && "Domains with invalid bounds.");
  assert(dimension() == right.dimension());

  if (intersects_with(right)) {
    for (DimType i = 0; i < dimension(); ++i) {
      const auto llo = intervals[i].low();
      const auto lhi = intervals[i].high();
      const auto rlo = right[i].low();
      const auto rhi = right[i].high();
      // touches if no lo/hi is between the lo/hi of the other sdom for all dims
      if ((llo > rlo && llo < rhi) || (lhi > rlo && lhi < rhi) ||
          (rlo > llo && rlo < lhi) || (rhi > llo && rhi < lhi) ||
          (llo == rlo && lhi == rhi)) {
        ;
      } else {
        return true;
      }
    }
  }
  return false;
}

const r_Sinterval &r_Minterval::at_unsafe(r_Dimension dim) const
{
    return intervals[dim];
}
r_Sinterval &r_Minterval::at_unsafe(r_Dimension dim)
{
    return intervals[dim];
}

// todo: remove bound checking so that it has same semantics as std::vector
const r_Sinterval &r_Minterval::operator[](r_Dimension i) const
{
    if (i < dimension())
    {
        return intervals[i];
    }
    else
    {
        LERROR << "interval index " << i << " out of bounds on minterval " << *this;
        throw r_Eindex_violation(0, dimension() - 1, i);
    }
}

// todo: remove bound checking so that it has same semantics as std::vector
r_Sinterval &r_Minterval::operator[](r_Dimension i)
{
    if (i < dimension())
    {
        return intervals[i];
    }
    else
    {
        LERROR << "interval index " << i << " out of bounds on minterval " << *this;
        throw r_Eindex_violation(0, dimension() - 1, i);
    }
}

const r_Sinterval &r_Minterval::at(r_Dimension i) const
{
    if (i < dimension())
    {
        return intervals[i];
    }
    else
    {
        LERROR << "interval index " << i << " out of bounds on minterval " << *this;
        throw r_Eindex_violation(0, dimension() - 1, i);
    }
}

r_Sinterval &r_Minterval::at(r_Dimension i)
{
    if (i < dimension())
    {
        return intervals[i];
    }
    else
    {
        LERROR << "interval index " << i << " out of bounds on minterval " << *this;
        throw r_Eindex_violation(0, dimension() - 1, i);
    }
}

bool r_Minterval::operator==(const r_Minterval &o) const
{
    return (dimension() == o.dimension()) &&
           std::equal(intervals.begin(), intervals.end(), o.intervals.begin());
}

bool r_Minterval::operator!=(const r_Minterval &mint) const
{
    return !operator==(mint);
}

bool r_Minterval::equal_extents(const r_Minterval &other) const
{
    if (dimension() == other.dimension())
    {
        for (r_Dimension i = 0; i < dimension(); i++)
        {
            if (intervals[i].get_extent() != other[i].get_extent())
                return false;
        }
        return true;
    }
    return false;
}

r_Point r_Minterval::get_origin() const
{
    if (is_origin_fixed())
    {
        r_Point pt(dimension());
        for (r_Dimension i = 0; i < dimension(); i++)
        {
            pt[i] = intervals[i].low();
        }
        return pt;
    }
    else
    {
        LERROR << "cannot get origin of open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }
}

r_Point r_Minterval::get_high() const
{
    if (is_high_fixed())
    {
        r_Point pt(dimension());
        for (r_Dimension i = 0; i < dimension(); i++)
        {
            pt[i] = intervals[i].high();
        }
        return pt;
    }
    else
    {
        LERROR << "cannot get upper bounds of open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }
}

r_Point r_Minterval::get_extent() const
{
    if (is_origin_fixed() && is_high_fixed())
    {
        r_Point pt(dimension());
        for (r_Dimension i = 0; i < dimension(); i++)
        {
            pt[i] = static_cast<r_Range>(intervals[i].get_extent());
        }
        return pt;
    }
    else
    {
        LERROR << "cannot get extent of open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }
}

r_Minterval &r_Minterval::reverse_translate(const r_Point &t)
{
    if (dimension() != t.dimension())
    {
        LERROR << "cannot reverse_translate minterval " << *this
               << " by a point of mismatching dimension " << t;
        throw (r_Edim_mismatch(dimension(), t.dimension()));
    }
    if (!is_origin_fixed() || !is_high_fixed())
    {
        LERROR << "cannot reverse_translate open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].set_interval(intervals[i].low() - t[i], intervals[i].high() - t[i]);
    }

    return *this;
}

r_Minterval &r_Minterval::translate(const r_Point &t)
{
    if (dimension() != t.dimension())
    {
        LERROR << "cannot translate minterval " << *this
               << " by a point of mismatching dimension " << t;
        throw (r_Edim_mismatch(dimension(), t.dimension()));
    }

    if (!is_origin_fixed() || !is_high_fixed())
    {
        LERROR << "cannot translate open minterval " << *this;
        throw r_Error(INTERVALOPEN);
    }

    for (r_Dimension i = 0; i < dimension(); i++)
        intervals[i].set_interval(intervals[i].low() + t[i],
                                  intervals[i].high() + t[i]);

    return *this;
}

r_Minterval r_Minterval::create_reverse_translation(const r_Point &t) const
{
    r_Minterval result(*this);
    result.reverse_translate(t);
    return result;
}

r_Minterval r_Minterval::create_translation(const r_Point &t) const
{
    r_Minterval result(*this);
    result.translate(t);
    return result;
}

r_Minterval &r_Minterval::scale(const double &d)
{
    vector<double> scaleVec;
    // create scale vector
    for (r_Dimension i = 0; i < dimension(); i++)
    {
        scaleVec.push_back(d);
    }
    scale(scaleVec);

    return *this;
}

r_Minterval &r_Minterval::scale(const vector<double> &scaleVec)
{
    double high = 0., low = 0.;

    // if the size of scale vector is different from dimensionality, undefined behaviour
    if (scaleVec.size() != dimension())
    {
        throw r_Edim_mismatch(scaleVec.size(), dimension());
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        // do explicit rounding, because the cast down in set_interval doesn't do
        // the good rounding for negative values -- PB 2005-jun-19
        low = floor(scaleVec[i] * intervals[i].low());

        // correction by 1e-6 to avoid the strage bug when high was a
        // integer value and floor return value-1(e.g. query 47.ql)
        high = floor(scaleVec[i] * (intervals[i].high() + 1) + 0.000001) - 1;

        // apparently the above correction doesn't work for certain big numbers,
        // e.g. 148290:148290 is scaled to 74145:74144 (invalid) by factor 0.5 -- DM 2012-may-25
        if (high < low)
        {
            if (high > 0)
                high = low;
            else
                low = high;
        }

        intervals[i].set_interval(static_cast<r_Range>(low), static_cast<r_Range>(high));
    }
    return *this;
}

r_Minterval r_Minterval::create_scale(const double &d) const
{
    r_Minterval result(*this);

    result.scale(d);

    return result;
}

r_Minterval r_Minterval::create_scale(const vector<double> &scaleVec) const
{
    r_Minterval result(*this);

    result.scale(scaleVec);

    return result;
}

std::vector<double> r_Minterval::scale_of(const r_Minterval &op) const
{
    const auto &original = *this;
    assert(original.dimension() == op.dimension());
  
    const auto dim = original.dimension();
    std::vector<double_t> factor(dim);
  
    for (DimType i = 0; i < dim; i++) {
      if (op[i].is_slice()) {
        factor[i] = double(op[i].low());
      } else {
        const auto &interval = original[i];
        double source = double(interval.get_extent());
        if (source != 0.0) {
          double target = double(op[i].get_extent());
  
          auto f = target / source;
          auto low = BoundType(f * double(interval.low()));
          //correction by 1e-6 to avoid the strange bug when high was a
          //integer value and floor return value-1(e.g. query 47.ql)
          auto high = std::max(
              BoundType(f * (double(interval.high()) + 1) + 0.000001) - 1, low);
          // apparently the above correction doesn't work for certain big numbers,
          // e.g. 148290:148290 is scaled to 74145:74144 (invalid) by factor,
          // so we add a max above to make sure high isn't less than low.
  
          if (double(high - low + 1) != target) {
            f = f + (target - double(high - low + 1)) / source;
          }
          factor[i] = f;
        }
      }
    }
    return factor;
}

r_Minterval &
r_Minterval::union_of(const r_Minterval &mint1, const r_Minterval &mint2)
{
    if (mint1.dimension() != mint2.dimension())
    {
        LERROR << "cannot create union of mintervals of mismatching dimensions: " << mint1 << " and " << mint2;
        throw (r_Edim_mismatch(mint1.dimension(), mint2.dimension()));
    }

    // cleanup + initializing of this
    if (dimension() != mint1.dimension())
    {
        streamInitCnt = mint1.dimension();
        intervals = std::vector<r_Sinterval>(mint1.dimension());
    }
    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].union_of(mint1[i], mint2[i]);
    }

    return *this;
}

r_Minterval &r_Minterval::union_with(const r_Minterval &mint)
{
    if (dimension() != mint.dimension())
    {
        LERROR << "cannot create union of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].union_with(mint[i]);
    }

    return *this;
}

r_Minterval &r_Minterval::operator+=(const r_Minterval &mint)
{
    return union_with(mint);
}

r_Minterval r_Minterval::create_union(const r_Minterval &mint) const
{
    if (dimension() != mint.dimension())
    {
        LERROR << "cannot create union of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    r_Minterval result(dimension());

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        result << intervals[i].create_union(mint[i]);
    }

    return result;
}

r_Minterval r_Minterval::operator+(const r_Minterval &mint) const
{
    return create_union(mint);
}

r_Minterval &r_Minterval::difference_of(const r_Minterval &mint1,
                                        const r_Minterval &mint2)
{
    if (mint1.dimension() != mint2.dimension())
    {
        LERROR << "cannot create difference of mintervals of mismatching dimensions: " << mint1 << " and " << mint2;
        throw (r_Edim_mismatch(mint1.dimension(), mint2.dimension()));
    }

    if (dimension() != mint1.dimension())
    {
        streamInitCnt = mint1.dimension();
        intervals = std::vector<r_Sinterval>(mint1.dimension());
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].difference_of(mint1[i], mint2[i]);
    }

    return *this;
}

r_Minterval &r_Minterval::difference_with(const r_Minterval &mint)
{
    if (dimension() != mint.dimension())
    {
        LERROR << "cannot create difference of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].difference_with(mint[i]);
    }

    return *this;
}

r_Minterval &r_Minterval::operator-=(const r_Minterval &mint)
{
    return difference_with(mint);
}

r_Minterval r_Minterval::create_difference(const r_Minterval &mint) const
{
    if (dimension() != mint.dimension())
    {
        LERROR << "cannot create difference of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    r_Minterval result(dimension());

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        result << intervals[i].create_difference(mint[i]);
    }

    return result;
}

r_Minterval r_Minterval::operator-(const r_Minterval &mint) const
{
    return create_difference(mint);
}

r_Minterval &
r_Minterval::intersection_of(const r_Minterval &mint1, const r_Minterval &mint2)
{
    if (mint1.dimension() != mint2.dimension())
    {
        LERROR << "cannot create intersection of mintervals of mismatching dimensions: " << mint1 << " and " << mint2;
        throw (r_Edim_mismatch(mint1.dimension(), mint2.dimension()));
    }
    if (dimension() != mint1.dimension())
    {
        streamInitCnt = mint1.dimension();
        intervals = std::vector<r_Sinterval>(mint1.dimension());
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].intersection_of(mint1[i], mint2[i]);
    }
    return *this;
}

r_Minterval &r_Minterval::intersection_with(const r_Minterval &mint)
{
    if (dimension() != mint.dimension())
    {
        LERROR << "cannot create intersection of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].intersection_with(mint[i]);
    }

    return *this;
}

r_Minterval &r_Minterval::operator*=(const r_Minterval &mint)
{
    return intersection_with(mint);
}

r_Minterval r_Minterval::create_intersection(const r_Minterval &mint) const
{
    if (dimension() != mint.dimension())
    {
        LERROR << "cannot create intersection of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    r_Minterval result(dimension());

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        result << intervals[i].create_intersection(mint[i]);
    }

    return result;
}

r_Minterval r_Minterval::operator*(const r_Minterval &mint) const
{
    return create_intersection(mint);
}

r_Minterval &
r_Minterval::closure_of(const r_Minterval &mint1, const r_Minterval &mint2)
{
    if (mint1.dimension() != mint2.dimension())
    {
        LERROR << "cannot create closure of mintervals of mismatching dimensions: " << mint1 << " and " << mint2;
        throw (r_Edim_mismatch(mint1.dimension(), mint2.dimension()));
    }
    if (mint1.dimension() != dimension())
    {
        streamInitCnt = mint1.dimension();
        intervals = std::vector<r_Sinterval>(mint1.dimension());
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].closure_of(mint1[i], mint2[i]);
    }

    return *this;
}

r_Minterval &r_Minterval::closure_with(const r_Minterval &mint)
{
    if (dimension() != mint.dimension())
    {
        LERROR << "cannot create closure of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        intervals[i].closure_with(mint[i]);
    }

    return *this;
}

r_Minterval r_Minterval::create_closure(const r_Minterval &mint) const
{
    if (dimension() != mint.dimension())
    {
        LERROR << "cannot create closure of mintervals of mismatching dimensions: " << mint << " and " << *this;
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    r_Minterval result(dimension());

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        result << intervals[i].create_closure(mint[i]);
    }

    return result;
}

std::vector<r_Minterval> r_Minterval::extension_of(const r_Minterval &big) const
{
    const auto &small = *this;
    const auto dim = big.dimension();
    assert(dim == dimension());
  
    // if big doesn't contain small then the only result domain is big
    if (!big.covers(*this))
      return {big};
  
    // left contains intervals [s_low,s_high] (small domain)
    // right contains intervals [b_low, b_high] (big domain)
    // result contains domains of the form
    // { left :: [b_low,s_low-1] :: right , left :: [s_high+1,b_high] :: right }
    std::vector<r_Minterval> result;
    std::deque<r_Sinterval> left, right;
  
    for (DimType i = 0; i < dim; i++)
      right.push_back(big[i]);
  
    for (DimType i = 0; i < dim; i++) {
      right.pop_front();
      if (intervals[i].high() < big[i].high()) {
        std::vector<r_Sinterval> tmp;
        tmp.reserve(dim);
        for (const auto &x: left) tmp.emplace_back(x);
        tmp.emplace_back(small[i].high() + 1, big[i].high());
        for (const auto &x: right) tmp.emplace_back(x);
  
        result.emplace_back(tmp);
      }
      if (small[i].low() > big[i].low()) {
        std::vector<r_Sinterval> tmp;
        tmp.reserve(dim);
        for (const auto &x: left) tmp.emplace_back(x);
        tmp.emplace_back(big[i].low(), small[i].low() - 1);
        for (const auto &x: right) tmp.emplace_back(x);
  
        result.emplace_back(tmp);
      }
      left.push_back(small[i]);
    }
  
    return result;
}

r_Minterval
r_Minterval::trim_along_slice(const r_Minterval &mint, const std::vector<r_Dimension> &projDims) const
{
    if (dimension() < mint.dimension())
    {
        LERROR << "r_Minterval:trim_along_slice(" << mint << ") dimensions (" 
               << dimension() << ") do not coincide";
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }
    else if (projDims.size() >= dimension())
    {
        LERROR << "r_Minterval:trim_along_slice(" << projDims.size() << ") dimensions (" 
               << dimension() << ") do not coincide";
        throw (r_Edim_mismatch(dimension(), mint.dimension()));
    }

    for (size_t i = 0; i < projDims.size(); i++)
    {
        if (projDims[i] >= dimension())
        {
            LERROR << "r_Minterval:trim_along_slice(" << projDims[i] << ") dimensions (" 
                   << dimension() << ") do not coincide";
            throw (r_Edim_mismatch(dimension(), projDims[i]));
        }
    }

    r_Minterval result(dimension());

    size_t projCtr = 0;
    for (r_Dimension i = 0; i < dimension(); i++)
    {
        if (projCtr < projDims.size() && projDims[projCtr] == i)
        {
            // trimming along the slice
            result << mint[projCtr].create_intersection(intervals[i]);
            projCtr++;
        }
        else
        {
            // no trimming happens -- dimension is kept
            result << intervals[i];
        }
    }

    return result;
}

r_Minterval
r_Minterval::project_along_dims(const std::vector<r_Dimension> &projDims) const
{
    r_Minterval result(projDims.size());
    for (auto projDim: projDims)
    {
        if (projDim >= dimension())
        {
            LERROR << "cannot project axis " << projDim 
                   << " as it is outside the dimension " << dimension();
            throw r_Edim_mismatch(dimension(), projDim);
        }
        result << intervals[projDim];
    }
    return result;
}

void r_Minterval::print_status(std::ostream &s) const
{
    s << "[";
    for (r_Dimension i = 0; i < dimension(); i++)
    {
        if (i > 0)
            s << ",";
        s << intervals[i];
    }
    s << "]";
}

char *r_Minterval::get_string_representation() const
{
    // initialize string stream
    std::ostringstream domainStream;
    // write into string stream
    domainStream << (*this);

    // allocate memory taking the final string
    char *returnString = strdup(domainStream.str().c_str());

    return returnString;
}

std::string
r_Minterval::to_string() const
{
    std::string ret = "[";
    for (r_Dimension i = 0; i < dimension(); i++)
    {
        if (i > 0)
            ret += ",";
        ret += intervals[i].to_string();
    }
    ret += "]";
    return ret;
}

std::string
r_Minterval::get_named_axis_string_representation() const
{
    std::ostringstream ss;
    ss << "[";

    for (r_Dimension i = 0; i < dimension(); i++)
    {
        if (i > 0)
            ss << ",";

        ss << axisNames[i];

        if (intervals[i].is_low_fixed() || intervals[i].is_high_fixed())
            ss << "(" << intervals[i] << ")";
    }

    ss << "]";

    return ss.str();
}

r_Area r_Minterval::cell_count() const
{
    r_Area ret = 1;
    for (r_Dimension i = 0; i < dimension(); i++)
        ret *= static_cast<r_Area>(intervals[i].get_extent());
    return ret;
}

// offset in cells for linear access of the data element referred by point in the data memory area
// Lower dimensions are higher valued which means that the highest dimension is stored in a sequence.
r_Area r_Minterval::cell_offset(const r_Point &point) const
{
    if (dimension() != point.dimension())
    {
        LERROR << "cannot calculate cell offset, dimension of minterval ("
               << *this << ") does not match dimension of point " << point.dimension() << ".";
        throw r_Edim_mismatch(point.dimension(), dimension());
    }
    r_Range offset = 0;
    r_Point ptExt = get_extent();

    // calculate offset
    r_Dimension i = 0;
    for (i = 0; i < dimension() - 1; i++)
    {
        if (point[i] < intervals[i].low() || point[i] > intervals[i].high())
        {
            LERROR << "point " << point << " is out of range for minterval " << *this << ".";
            throw (r_Eindex_violation(point[i], intervals[i].low(), intervals[i].high()));
        }

        offset = (offset + (point[i] - intervals[i].low())) * ptExt[i + 1];
    }

    // now i = dimensionality - 1
    if (point[i] < intervals[i].low() || point[i] > intervals[i].high())
    {
        LERROR << "point " << point << " is out of range for minterval " << *this << ".";
        throw (r_Eindex_violation(point[i], intervals[i].low(), intervals[i].high()));
    }
    offset += (point[i] - intervals[i].low());

    return static_cast<r_Area>(offset);
}

r_Area r_Minterval::cell_offset_unsafe(const r_Point &point) const
{
    r_Area offset = 0;

    r_Dimension i = 0;
    // calculate offset
    for (; i < dimension() - 1; i++)
    {
        offset = (offset + 
                  static_cast<long long unsigned int>(point[i] - intervals[i].low())) *
                 intervals[i + 1].get_extent();
    }

    offset += static_cast<long long unsigned int>(point[i] - intervals[i].low());

    return offset;
}

// Arguments.....: linear offset
// Return value..: point object which corresponds to the linear offset of the argument
// Description...: The method calucaltes the spatial domain coordinates as a point out 
//                 of an offset specification. Lower dimensions are higher valued which 
//                 means that the highest dimension is stored in a sequence.
r_Point r_Minterval::cell_point(r_Area offset) const
{
    r_Dimension i;
    unsigned int factor = 1;
    r_Point pt(dimension());

    for (i = 0; i < dimension(); i++)
    {
        factor *= intervals[i].get_extent();
    }

    for (i = 0; i < dimension(); i++)
    {
        factor /= intervals[i].get_extent();
        pt[i] = intervals[i].low() +
                static_cast<r_Range>((offset - (offset % factor)) / factor);
        offset %= factor;
    }

    return pt;
}

void r_Minterval::delete_dimension(r_Dimension dim)
{
    if (dim >= dimension())
    {
        LERROR << "cannot delete dimension " << dim << " from minterval " << *this << ", out of range.";
        throw r_Eindex_violation(0, dimension() - 1, dim);
    }

    intervals.erase(intervals.begin() + dim);
    streamInitCnt = dimension();
}

void r_Minterval::swap_dimensions(r_Dimension a, r_Dimension b)
{
    if (a >= dimension())
    {
        LERROR << "cannot swap intervals " << a << " and " << b << " in minterval " << *this << ", out of range.";
        throw r_Eindex_violation(0, dimension() - 1, a);
    }
    if (b >= dimension())
    {
        LERROR << "cannot swap intervals " << a << " and " << b << " in minterval " << *this << ", out of range.";
        throw r_Eindex_violation(0, dimension() - 1, b);
    }
    if (a != b)
    {
        r_Sinterval tmp = intervals[a];
        intervals[a] = intervals[b];
        intervals[b] = tmp;
    }
}

void r_Minterval::add_dimension()
{
    streamInitCnt +=1;
    intervals.emplace_back("*:*");
}
void
r_Minterval::delete_non_trims(const std::vector<bool> &trims)
{
    if (trims.size() != dimension())
    {
        return;
    }
    for (r_Dimension i = 0, j = 0; i < dimension(); ++i)
    {
        if (!trims[i])
        {
            this->delete_dimension(j);
        }
        else
        {
            ++j;
        }
    }
}

void r_Minterval::delete_slices()
{
    intervals.erase(
        std::remove_if(intervals.begin(), intervals.end(),
                       [](const r_Sinterval& i) { return i.is_slice(); }),
        intervals.end());
    streamInitCnt = dimension();
}

bool r_Minterval::is_point() const noexcept
{
    return std::all_of(intervals.begin(), intervals.end(),
                       [](const r_Sinterval& i) { return i.is_slice(); });
}

r_Bytes
r_Minterval::get_storage_size() const
{
    r_Bytes sz = sizeof(r_Sinterval *) + 2 * sizeof(r_Dimension);

    if (dimension() > 0)
    {
        sz += dimension() * intervals[0].get_storage_size();
    }

    return sz;
}

bool r_Minterval::compareDomainExtents(const r_Minterval &b) const
{
    const auto &a = *this;
    assert(a.is_fixed() && b.is_fixed() && "Cannot compare mintervals with invalid bounds.");
    if (a.dimension() != b.dimension())
        return false;
    for (DimType i = 0; i < a.dimension(); ++i) {
      if (a[i].get_extent() != b[i].get_extent())
          return false;
    }
    return true;
}

r_Minterval r_Minterval::computeDomainOfResult(const r_Minterval &b) const
{
    const auto &a = *this;
    const auto dim = a.dimension();
    
    if (dim != b.dimension())
    {
        LERROR << "cannot calculate cell offset, dimension of minterval ("
               << *this << ") does not match dimension of minterval " << b << ".";
        throw r_Edim_mismatch(dim, b.dimension());
    }
  
    if (a == b) {
      return a;
    } else {
      std::vector<r_Sinterval> ret;
      ret.reserve(dim);
      for (DimType i = 0; i < dim; ++i) {
        if (a[i].get_extent() == b[i].get_extent())
          ret.emplace_back(0ll, static_cast<BoundType>(a[i].get_extent() - 1));
        else
          throw r_Elimits_mismatch(r_Range(a[i].get_extent()), 
                                   r_Range(b[i].get_extent()));
      }
      return r_Minterval{std::move(ret)};
    }
}

bool r_Minterval::is_mergeable(const r_Minterval &b) const
{
    bool is_merg = true;
    // An alias to this object
    const r_Minterval &a = *this;

    // The blocks must have the same dimensionality to be mergeable
    if (a.dimension() != b.dimension())
    {
        is_merg = false;
    }
    else
    {
        // Count the number of adjacent frontiers
        int ones_differences = 0;

        // For all dimensions
        for (r_Dimension i = 0; i < dimension(); i++)
        {
            // Diferente origins
            if (a[i].low() != b[i].low())
            {
                if ((a[i].low() == b[i].high() + 1) || (b[i].low() == a[i].high() + 1))
                    // If borders are adjacent
                {
                    // Update counter
                    ++ones_differences;
                }
                else
                {
                    // Else non-mergeable blocks
                    is_merg = false;
                    break;
                }
            }
            else
            {
                // Check ending
                if (a[i].high() != b[i].high())
                {
                    is_merg = false;
                    // Not the same, can't be
                    break;
                    // mergeable
                }
            }
        }

        // Only one adjacent borded
        if (is_merg && (ones_differences != 1))
        {
            is_merg = false;
        }
        // allowed
    }
    return is_merg;
}

std::ostream &operator<<(std::ostream &s, const r_Minterval &d)
{
    d.print_status(s);
    return s;
}

std::ostream &operator<<(std::ostream &os, const std::vector<r_Minterval> &vec)
{
    os << " Vector { ";

    unsigned int size = vec.size();
    for (unsigned int i = 0; i < size; i++)
    {
        os << vec[i] << std::endl;
    }

    os << " } ";
    return os;
}

std::ostream &operator<<(std::ostream &s, const vector<double> &v)
{
    vector<double>::const_iterator iter, iterEnd;

    iter = v.begin();
    iterEnd = v.end();
    s << "{";
    while (iter != iterEnd)
    {
        s << *iter;
        ++iter;
        if (iter != iterEnd)
        {
            s << ", ";
        }
    }
    s << "}";

    return s;
}

bool r_Minterval::covers(const r_Point &pnt) const
{
    bool retval = true;
    if (dimension() == pnt.dimension())
    {
        for (r_Dimension i = 0; i < pnt.dimension(); i++)
        {
            if ((intervals[i].is_low_fixed() && pnt[i] < intervals[i].low()) ||
                (intervals[i].is_high_fixed() && pnt[i] > intervals[i].high()))
            {
                retval = false;
                break;
            }
        }
    }
    else
    {
        LERROR << "cannot check if minterval " << *this << " covers " << pnt
               << ", dimensions do not match.";
        retval = false;
    }

    return retval;
}

bool r_Minterval::covers(const r_Minterval &inter2) const
{
    bool retval = true;
    if (dimension() == inter2.dimension())
    {
        for (r_Dimension i = 0; i < dimension(); i++)
        {
            // first check if it is low fixed and the other isn't: false
            // both are low fixed
            // check if it is smaller than the other: false
            // second check if it is high fixed and the other isn't: false
            // both are high fixed
            // check if it is smaller than the other: false
            const auto &a = intervals[i];
            const auto &b = inter2[i];
            if ((a.is_low_fixed() && (!b.is_low_fixed() || a.low() > b.low())) ||
                (a.is_high_fixed() && (!b.is_high_fixed() || a.high() < b.high())))
            {
                retval = false;
                break;
            }
        }
    }
    else
    {
        LERROR << "cannot check if minterval " << *this << " covers " << inter2
               << ", dimensions do not match.";
        retval = false;
    }
    return retval;
}

bool r_Minterval::inside_of(const r_Minterval &minterval) const
{
    assert(dimension() == minterval.dimension());

    // none of the interval pairs are allowed to be disjoint
    for (r_Dimension i = 0; i < dimension(); ++i)
        if (!intervals[i].inside_of(minterval[i]))
            return false;

    return true;
}

r_Dimension
r_Minterval::dimension() const
{
    return static_cast<r_Dimension>(intervals.size());
}

bool
r_Minterval::is_origin_fixed() const noexcept
{
    assert(dimension() > 0);
    
    for (r_Dimension i = 0; i < dimension(); i++)
        if (!intervals[i].is_low_fixed())
            return false;
    
    return true;
}

bool
r_Minterval::is_high_fixed() const noexcept
{
    assert(dimension() > 0);
    
    for (r_Dimension i = 0; i < dimension(); i++)
        if (!intervals[i].is_high_fixed())
            return false;
    
    return true;
}

bool r_Minterval::is_fixed() const noexcept
{
    assert(dimension() > 0);
    
    for (r_Dimension i = 0; i < dimension(); i++)
        if (!intervals[i].is_fixed())
            return false;
    
    return true;
}

bool r_Minterval::is_scalar() const noexcept {
    return intervals.empty();
}
