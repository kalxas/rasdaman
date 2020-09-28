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
 * INCLUDE: sinterval.hh
 *
 * MODULE:  raslib
 * CLASS:   r_Sinterval
 *
 * COMMENTS:
 *
*/

#ifndef D_SINTERVAL_HH
#define D_SINTERVAL_HH

#include "raslib/mddtypes.hh"  // for r_Range, r_Bytes
#include <iosfwd>     // for ostream, cout

//@ManMemo: Module: {\bf raslib}

/*@Doc:

 The class represents an interval with lower and upper bound.
 Operations on the interval are defined according to the
 ODMG-93 standard.
 The operations union, difference, and intersection are
 defined according to the following table:

    | ...  fixed bound \\
    * ...  open bound


 \begin{verbatim}

 class   orientation       union    difference  intersection
 -----------------------------------------------------------
   1     |-a-| |-b-|       error    a           error

   2     |-a-|             [a1,b2]  [a1,b1]     [b1,a2]
            |-b-|

   3     |--a--|           a        error       b
          |-b-|

   4     |-b-|             [b1,a2]  [b2,a2]     [a1,b2]
            |-a-|

   5     |--b--|           b        error       a
          |-a-|

   6     |-b-| |-a-|       error    a           error

   7     |-a-|-b-|         [a1,b2]  a           [a2,a2]

   8     |-b-|-a-|         [b1,a2]  a           [b2,b2]

   9     |--a--|           a        [a1,b1]     b
           |-b-|

  10     |--a--|           a        [b2,a2]     b
         |-b-|

  11     |-a-|             a        error       a
         |-b-|

  12     |--b--|           b        error       a
         |-a-|

  13     |--b--|           b        error       a
           |-a-|

  -----------------------------------------------------

  14     |--a--*           a        error       b
          |-b-|

  15     |--a--*           a        [b2,a2]     b
         |-b-|

  16     |-b-| |-a-*       error    a           error

  17     |-b-|-a-*         [b1,a2]  a           [b2,b2]

  18      |--a--*          [b1,a2]  [b2,a2]     [a1,b2]
         |-b-|

  -----------------------------------------------------

  19     *--a--|           a        error       b
          |-b-|

  20     *--a--|           a        [a1,b1]     b
           |-b-|

  21     *-a-| |-b-|       error    a           error

  22     *-a-|-b-|         [a1,b2]  a           [a2,a2]

  23     *--a--|           [a1,b2]  [a1,b1]     [b1,a2]
            |-b-|

  -----------------------------------------------------

  24     |--b--*           b        error       a
          |-a-|

  25     |--b--*           b        error       a
         |-a-|

  26     |-a-| |-b-*       error    a           error

  27     |-a-|-b-*         [a1,b2]  a           [a2,a2]

  28      |--b--*          [a1,b2]  [a1,b1]     [b1,a2]
         |-a-|

  -----------------------------------------------------

  29     *--b--|           b        error       a
          |-a-|

  30     *--b--|           b        error       a
           |-a-|

  31     *-b-| |-a-|       error    a           error

  32     *-b-|-a-|         [b1,a2]  a           [b2,b2]

  33     *--b--|           [b1,a2]  [b2,a2]     [a1,b2]
            |-a-|

  -----------------------------------------------------

  34     *-a-| |-b-*       error    a           error

  35     *-a-|-b-*         [a1,b2]  a           [a2,a2]

  36     *-a-|             [a1,b2]  [a1,b1]     [b1,a2]
            |-b-*

  -----------------------------------------------------

  37     *-b-| |-a-*       error    a           error

  38     *-b-|-a-*         [b1,a2]  a           [b2,b2]

  39     *-b-|             [b1,a2]  [a1,b1]     [a1,b2]
            |-a-*

  -----------------------------------------------------

  40     *-a-|             b        error       a
          *-b-|

  41     *-a-|             a        error       a
         *-b-|

  42     *-b-|             a        [b2,a2]     b
          *-a-|

  -----------------------------------------------------

  43     |-a-*             a        [a1,b1]     b
          |-b-*

  44     |-a-*             a        error       a
         |-b-*

  45     |-b-*             b        error       a
          |-a-*

  -----------------------------------------------------
  46     *-a-* |-b-|       a        error       b

  47     *-b-* |-a-|       b        error       a

  48     *-a-*             a        [b2,a2]     b
          *-b-|

  49     *-a-*             a        [a1,b1]     b
          |-b-*

  50     *-b-*             b        error       a
          *-a-|

  51     *-b-*             b        error       a
          |-a-*

  52     *-a-*             a        error       a
         *-b-*

 \end{verbatim}

 Attention: The difference operation has to be reconsidered in future
 concerning a discrete interpretation of the intervals.

 The closure operation defines an interval which is the smallest
 interval containing the two operands.
 The method <tt>intersects_with()</tt> returns 0 in the error cases of the
 intersection operation and 1 otherwise.

*/

class r_Sinterval
{
public:
    using BoundType = r_Range;
    using OffsetType = size_t;
    
    /// default constructor creates an interval with open bounds
    r_Sinterval() = default;
    /// constructor taking string representation (e.g. *:200 )
    r_Sinterval(const char *);
    /// constructor for an interval with fixed bounds
    r_Sinterval(r_Range low, r_Range high);
    /// constructor for a slice (TODO)
    explicit r_Sinterval(r_Range point);

    // Constructors for intervals with at least one open bound.
    //@{
    r_Sinterval(char, r_Range high);
    r_Sinterval(r_Range low, char);
    r_Sinterval(char, char);
    //@}
    
    // Equality comparison
    //@{
    /// Two intervals are equal if they have the same lower and upper bound.
    bool operator==(const r_Sinterval &) const;
    /// non equal operator - negation of equal operator
    bool operator!=(const r_Sinterval &) const;
    //@}

    // Read/Write methods:
    //@{
    r_Range low() const noexcept;
    r_Range high() const noexcept;
    /// get the size of one dimensional interval as range (high() - low() + 1)
    OffsetType get_extent() const;
    /// @return true if lower and upper bounds are fixed
    bool is_fixed() const noexcept;
    bool is_low_fixed() const noexcept;
    bool is_low_unbounded() const noexcept;
    bool is_high_fixed() const noexcept;
    bool is_high_unbounded() const noexcept;
    /// @return TRUE if the interval represents a single point, FALSE otherwise.
    bool is_slice() const noexcept;
    void set_low(r_Range low);
    void set_low(char) noexcept;
    void set_high(r_Range high);
    void set_high(char) noexcept;
    void set_interval(r_Range low, r_Range high);
    void set_interval(char, r_Range high) noexcept;
    void set_interval(r_Range low, char) noexcept;
    void set_interval(char, char) noexcept;
    void set_slice() noexcept;
    
    /// get distance to lower bound of this interval from o; this interval is
    /// assumed to be fixed in the lower bound, and o >= this.low()
    OffsetType get_offset_to(BoundType o) const noexcept;
    /// get distance to lower bound of this interval from lower bound of o; 
    /// intervals must be fixed in lower bound, and o.low() >= this.low()
    OffsetType get_offset_to(const r_Sinterval &o) const noexcept;
    /// translate this interval by a given offset; assumes that this interval 
    /// has fixed bounds
    r_Sinterval translate_by(BoundType offset) const;
    //@}

    // Methods/Operators for the union operation:
    //@{
    r_Sinterval &union_of(const r_Sinterval &, const r_Sinterval &);
    r_Sinterval &union_with(const r_Sinterval &);
    r_Sinterval &operator+=(const r_Sinterval &);
    r_Sinterval create_union(const r_Sinterval &) const;
    r_Sinterval operator+(const r_Sinterval &) const;
    /// @return true if this interval is inside of
    bool inside_of(const r_Sinterval &o) const;
    //@}

    // Methods/Operators for the difference operation:
    //@{
    r_Sinterval &difference_of(const r_Sinterval &, const r_Sinterval &);
    r_Sinterval &difference_with(const r_Sinterval &);
    r_Sinterval &operator-=(const r_Sinterval &);
    r_Sinterval create_difference(const r_Sinterval &) const;
    r_Sinterval operator-(const r_Sinterval &) const;
    //@}

    // Methods/Operators for the intersection operation:
    //@{
    r_Sinterval &intersection_of(const r_Sinterval &, const r_Sinterval &);
    r_Sinterval &intersection_with(const r_Sinterval &);
    r_Sinterval &operator*=(const r_Sinterval &);
    r_Sinterval create_intersection(const r_Sinterval &) const;
    r_Sinterval operator*(const r_Sinterval &) const;
    /// determines if the self interval intersects with the delivered one
    bool intersects_with(const r_Sinterval &) const;
    //@}

    // Methods/Operators for the closure operation:
    //@{
    r_Sinterval &closure_of(const r_Sinterval &, const r_Sinterval &);
    r_Sinterval &closure_with(const r_Sinterval &);
    r_Sinterval create_closure(const r_Sinterval &) const;
    //@}

    /// writes the state of the object to the specified stream
    void print_status(std::ostream &s) const;
    
    /// Returns a string representation of this sinterval as a pointer that
    /// should eventually be deallocated by the caller with `free()`.
    char *get_string_representation() const;
    
    /// Returns a string representation of this sinterval as a string object.
    std::string to_string() const;

    // Methods for internal use only:
    //@{
    /// calculate the size of the storage space occupied
    r_Bytes get_storage_size() const;
    ///
    //@}

private:
    // Calculation methods for the operations:
    //@{
    r_Sinterval calc_union(const r_Sinterval &a, const r_Sinterval &b) const;
    r_Sinterval calc_difference(const r_Sinterval &a, const r_Sinterval &b) const;
    r_Sinterval calc_intersection(const r_Sinterval &a, const r_Sinterval &b) const;
    r_Sinterval calc_closure(const r_Sinterval &a, const r_Sinterval &b) const;
    /// compute the class of the two operands
    int classify(const r_Sinterval &a, const r_Sinterval &b) const;
    //@}

    /// lower bound of the interval; invalid if low_fixed is false
    r_Range lower_bound{};
    /// upper bound of the interval; invalid if low_fixed is false
    r_Range upper_bound{};

    /// true if lower bound is fixed, false if it is '*'
    bool low_fixed{false};
    /// true if upper bound is fixed, false if it is '*'
    bool high_fixed{false};
    /// true if this is a slice rather than an interval; low == upper in this case
    bool slice{false};
};

//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type <tt>const r_Sinterval</tt>.
*/
extern std::ostream &operator<<(std::ostream &s, const r_Sinterval &d);

#endif
