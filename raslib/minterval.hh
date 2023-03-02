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

#ifndef D_MINTERVAL_HH
#define D_MINTERVAL_HH

#include "raslib/sinterval.hh"
#include "raslib/point.hh"
#include "raslib/mddtypes.hh"  // for r_Dimension, r_Area, r_Bytes, r_Range

#include <iosfwd>  // for ostream, cout
#include <string>  // for string
#include <vector>  // for vector

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**

 The spatial domain of an MDD is represented by an object
 of class r_Minterval. It specifies lower and upper bound
 of the point set for each dimension of an MDD. Internally,
 the class is realized through an array of intervals of type
 r_Sinterval.

 For the operations union, difference, and intersection the
 dimensionalties of the operands must be equal, otherwise an
 exception is raised. The semantics of the operations are
 defined as follows for each dimension:

    | ...  fixed bound \\
    * ...  open bound

 \code

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

  19     *--a--|          a        error       b
          |-b-|

  20     *--a--|          a        [a1,b1]     b
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

  47     *-b-* |-a-|       b        error       b

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

 \endcode

 Attention: The difference operation has to be reconsidered in future
 concerning a discrete interpretation of the intervals.

 The closure operation defines an interval which is the smallest
 interval containing the two operands.
 The method <tt>intersects_with()</tt> returns 0 in the error cases of the
 intersection operation and 1 otherwise.
*/
class r_Minterval
{
public:
    using DimType = r_Dimension;
    using AreaType = r_Area;
    using OffsetType = r_Sinterval::OffsetType;

    /// constructor getting a low, high pair
    r_Minterval(const r_Point &low, const r_Point &high);
    /// constructor getting dimensionality for stream initializing
    explicit r_Minterval(r_Dimension);
    /// constructor taking string representation (e.g. [ 1:255, *:200, *:* ])
    explicit r_Minterval(const char *);
    /// constructor taking string representation (e.g. [ 1:255, *:200, *:* ])
    explicit r_Minterval(char *);
    /// construct from interval axes
    explicit r_Minterval(std::vector<r_Sinterval> intervals);
    /// for stream initializing with intervals
    r_Minterval &operator<<(const r_Sinterval &);
    /// for stream initializing with point intervals
    r_Minterval &operator<<(r_Range);
    /// construct minterval of slices from the given point p
    static r_Minterval fromPoint(const r_Point &p);

    /// default constructor
    r_Minterval() = default;
    /// default destructor
    ~r_Minterval() = default;

    /// move constructor
    r_Minterval(r_Minterval &&other) = default;
    /// move assignment
    r_Minterval &operator=(r_Minterval &&other) = default;

    /// copy constructor
    r_Minterval(const r_Minterval &other) = default;
    /// copy assignment
    r_Minterval &operator=(const r_Minterval &other) = default;

    /// read access the i-th interval
    const r_Sinterval &operator[](r_Dimension) const;

    /// read access the i-th interval with bound checking
    const r_Sinterval &at(r_Dimension) const;

    /// write access the i-th interval
    r_Sinterval &operator[](r_Dimension);

    /// write access the i-th interval with bound checking
    r_Sinterval &at(r_Dimension);

    const r_Sinterval &at_unsafe(r_Dimension dim) const;
    r_Sinterval &at_unsafe(r_Dimension dim);

    /// Two domains are equal if they have the same number of dimensions and
    /// each dimension has the same lower and upper bounds.
    bool operator==(const r_Minterval &) const;

    /// non equal operator - negation of equal operator
    bool operator!=(const r_Minterval &) const;

    /// Return true if the extents of each dimension are equal, otherwise false.
    bool equal_extents(const r_Minterval &other) const;

    /// Does this interval cover the given point.
    /// Throws r_Edim_mismatch when dimensions do not match.
    bool covers(const r_Point &pnt) const;

    /// Does this interval cover the given interval.
    /// Throws r_Edim_mismatch when dimensions do not match.
    bool covers(const r_Minterval &inter) const;

    /// Check whether one interval is within another.
    bool inside_of(const r_Minterval &) const;

    /// Split into n smaller mintervals.
    std::vector<r_Minterval> split_equal(int n);

    /// get dimensionality
    r_Dimension dimension() const;

    /// @return true if the interval is empty
    bool is_scalar() const noexcept;

    /// Returns a point with the minimum coordinates in all dimensions.
    /// This is operation is only legal if all lower bounds are fixed!
    r_Point get_origin() const;

    /// Returns a point with the maximum coordinates in all dimensions.
    /// This is operation is only legal if all upper bounds are fixed!
    r_Point get_high() const;

    /// Get size of minterval as point. Returns a point with high() - low() + 1
    /// in each dimension when all bounds are fixed.
    r_Point get_extent() const;

    /// Returns true if all lower bounds are fixed, otherwise false.
    bool is_origin_fixed() const noexcept;

    /// Returns true if all upper bounds are fixed, otherwise false.
    bool is_high_fixed() const noexcept;

    /// Returns true if all intervals are fixed
    bool is_fixed() const noexcept;

    /// @return the axis names of the underlying sintervals; this may be a
    /// vector of empty strings if no sinterval has an axis name.
    std::vector<std::string> get_axis_names() const;

    /// set new axis names to the underlying sintervals; the size of the
    /// axis_names vector must match the dimension of this minterval, otherwise
    /// an exception is thrown.
    void set_axis_names(std::vector<std::string> axis_names);

    /// copy the axis names from o to this minterval; minterval dimensions must
    /// match, otherwise an exception is thrown.
    void set_axis_names(const r_Minterval &o);

    /// @return true if the axis names of this minterval match the axis names of
    /// the other interval o.
    bool axis_names_match(const r_Minterval &o) const;

    /// @return true if this minterval has axis names, false otherwise.
    bool has_axis_names() const;

    /**
      This method checks if two r_Mintervals are "mergeable" side by side.
      For this to be possible, they have to have the same low() and high()
      values in all dimensions except in one where they differ by one point,
      this is, a.low()==b.high()+1 or b.low()==a.high()+1. For instance, the
      following two blocks are mergeable:

     +-------------+---------------------------------------+
     |      A      |                  B                    |
     +-------------|---------------------------------------|

      and the following two are not:

     +-------------+-------------------------+
     |             |            B            |
     |      A      +-------------------------+
     +-------------+
    */
    bool is_mergeable(const r_Minterval &other) const;

    // Methods for translation:
    //@{
    /// Subtracts respective coordinate of a point to the lower bounds of an
    /// interval. This operation is only legal if all bounds are fixed!
    r_Minterval &reverse_translate(const r_Point &);

    /// Returns new interval as translation of this by a point.
    /// Subtracts respective coordinate of a point to the lower bounds of an
    /// interval. This operation is only legal if all bounds are fixed!
    r_Minterval create_reverse_translation(const r_Point &) const;

    /// Translates this by a point.
    /// Adds respective coordinate of a point to the lower bounds of an
    /// interval. This operation is only legal if all bounds are fixed!
    r_Minterval &translate(const r_Point &);

    /// Returns new interval as translation of this by a point.
    /// Adds respective coordinate of a point to the lower bounds of an
    /// interval. This operation is only legal if all lower bounds are fixed!
    r_Minterval create_translation(const r_Point &) const;
    //@}

    // Methods for scaling:
    //@{
    /// Scales all extents by factor.
    r_Minterval &scale(const double &);
    /// Scales respective extents by vector of factors.
    r_Minterval &scale(const std::vector<double> &);
    /// Returns new interval as scaled from this by factor.
    r_Minterval create_scale(const double &) const;
    /// Returns new interval as scaled from this by vector of factors.
    r_Minterval create_scale(const std::vector<double> &) const;
    /// @return vector of each dimension's best-approximated scaling factor
    std::vector<double> scale_of(const r_Minterval &op) const;
    //@}

    // Methods/Operators for the union operation:
    //@{
    /// Return new minterval as union of the argument mintervals.
    r_Minterval &union_of(const r_Minterval &, const r_Minterval &);
    /// Union of argument with this minterval.
    r_Minterval &union_with(const r_Minterval &);
    /// Operator for union_with.
    r_Minterval &operator+=(const r_Minterval &);
    /// Same as union_with but return a new minterval instead of modifying this one.
    r_Minterval create_union(const r_Minterval &) const;
    /// Operator for create_union.
    r_Minterval operator+(const r_Minterval &) const;
    //@}

    // Methods/Operators for the difference operation:
    //@{
    /// Return new minterval as difference of the argument mintervals.
    r_Minterval &difference_of(const r_Minterval &, const r_Minterval &);
    /// Difference of argument with this minterval.
    r_Minterval &difference_with(const r_Minterval &);
    /// Operator for difference_with.
    r_Minterval &operator-=(const r_Minterval &);
    /// Same as difference_with but return a new minterval instead of modifying this one.
    r_Minterval create_difference(const r_Minterval &) const;
    /// Operator for difference_with.
    r_Minterval operator-(const r_Minterval &) const;
    //@}

    // Methods/Operators for the intersection operation:
    //@{
    /// Return new minterval as intersection of the argument mintervals.
    r_Minterval &intersection_of(const r_Minterval &, const r_Minterval &);
    /// Intersection of argument with this minterval.
    r_Minterval &intersection_with(const r_Minterval &);
    /// Operator for intersection_with.
    r_Minterval &operator*=(const r_Minterval &);
    /// Same as intersection_with but return a new minterval instead of modifying this one.
    r_Minterval create_intersection(const r_Minterval &) const;
    /// Operator for intersection_with.
    r_Minterval operator*(const r_Minterval &) const;
    /// Determines if the self minterval intersects with the argument one.
    bool intersects_with(const r_Minterval &) const;
    /// Determines if this minterval touches given minterval.
    bool touches(const r_Minterval &o) const;
    //@}

    // Methods/Operators for the closure operation:
    //@{
    /// Return new minterval as closure of the argument mintervals.
    r_Minterval &closure_of(const r_Minterval &, const r_Minterval &);
    /// Closure of argument with this minterval.
    r_Minterval &closure_with(const r_Minterval &);
    /// Same as closure_with but return a new minterval instead of modifying this one.
    r_Minterval create_closure(const r_Minterval &) const;
    /// @return vector of domains so that the disjoint union of this and returned domains is big.
    /// preconditions: big must cover this domain (this.covers(big) == true).
    std::vector<r_Minterval> extension_of(const r_Minterval &big) const;
    //@}

    // Methods/Operators for dimension-specific operations involving projections:
    //@{
    /// the vector of projection dimensions cannot have more values than dimension()
    /// this should really be called "trim_wrt_slice" because the result dimension is dimension()
    r_Minterval trim_along_slice(const r_Minterval &, const std::vector<r_Dimension> &) const;
    /// the vector of projection dimensions can have more values than this->dimensionality
    r_Minterval project_along_dims(const std::vector<r_Dimension> &) const;
    //@}

    /// Serialize the object to the specified stream.
    void print_status(std::ostream &s) const;

    /// Returns a string representation of this minterval as a pointer that
    /// should eventually be deallocated by the caller with `free()`.
    char *get_string_representation() const;

    /// Returns a string representation of this minterval as a string object.
    std::string to_string() const;

    // Methods for internal use only:
    //@{
    /// calculate number of cells
    r_Area cell_count() const;
    /// calculate offset in cells for one dimensional access (dimension ordering is high first)
    r_Area cell_offset(const r_Point &) const;
    /// as above, but without error checking, for performance
    r_Area cell_offset_unsafe(const r_Point &) const;
    /// calculate point index out of offset
    r_Point cell_point(r_Area) const;

    /// add dimension with open bounds
    void add_dimension();
    /// delete the specified dimension
    void delete_dimension(r_Dimension);
    /// swap dimensions
    void swap_dimensions(r_Dimension d1, r_Dimension d2);
    /// delete slices (false values in trims); does nothing if trims size != dimension
    void delete_non_trims(const std::vector<bool> &trims);
    /// delete intervals which are slices (i.e. interval.is_slice() is true)
    void delete_slices();
    /// append mint's intervals to the end of this minterval, resulting in
    /// [ ..., mint[0], ..., mint[mint.dimension()-1] ]
    void append_axes(const r_Minterval &mint);
    /// append pnt's coordinates to the end of this minterval, resulting in
    /// [ ..., pnt[0]:pnt[0], ..., pnt[pnt.dimension()-1]:pnt[pnt.dimension()-1]]
    void append_axes(const r_Point &pnt);
    /// @return true if all intervals are slices
    bool is_point() const noexcept;

    /// calculate the size of the storage space occupied
    r_Bytes get_storage_size() const;

    /// @return the number of axes which are trims (i.e. dimension() - slices)
    /// if is_point() is true, then this method will return 0.
    r_Dimension get_trim_count() const;

    /// @return true if any axes are slices rather than trims.
    bool has_slices() const;

    /// @return true if domains are same after normalization to zero origin.
    bool compareDomainExtents(const r_Minterval &b) const;
    /// throw error if domains differ in the extents of any axis.
    void validateDomainExtents(const r_Minterval &b) const;

    /// @return a if a == b, otherwise normalize the result to [0, 0, ..].
    r_Minterval computeDomainOfResult(const r_Minterval &b) const;
    //@}

protected:
    /// axis intervals, intervals.size() == dimension()
    std::vector<r_Sinterval> intervals;

    /// The minterval can be initialized in all dimensions as follows:
    /// `minterval << sinterval1 << sinterval2 << ...` where number of
    /// sintervals cannot be more than `dimension()`. This variable tracks
    /// how many sintervals have been set so far with the stream operator.
    r_Dimension streamInitCnt{};

    /// initialization for constructors which take chars
    void constructorinit(char *);
};

//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type <tt>const</tt> r_Minterval.
*/
extern std::ostream &operator<<(std::ostream &s, const r_Minterval &d);
extern std::ostream &operator<<(std::ostream &s, const std::vector<r_Minterval> &d);
extern std::ostream &operator<<(std::ostream &s, const std::vector<double> &doubleVec);

#endif
