

"Null is a special marker used in Structured Query Language (SQL) to
indicate that a data value does not exist in the database. NULL is also
an SQL reserved keyword used to identify the Null special marker."
(`Wikipedia <http://en.wikipedia.org/wiki/Null_%28SQL%29>`__) In fact,
null introduces a three-valued logic where the result of a Boolean
operation can be null itself; likewise, all other operations have to
respect null appropriately. Said Wikipedia article also discusses issues
the SQL language has with this three-valued logic.

For sensor data, a Boolean null indicator is not enough as null values
can mean many different things, such as "no value given", "value cannot
be trusted", or "value not known". Therefore, rasdaman refines the SQL
notion of null:

-  Any value of the data type range can be chosen to act as a null
   value;

-  a set of cell values can be declared to act as null (in contrast to
   SQL where only one null per attribute type is foreseen).


**Caveat**

Note that defining values as nulls reduces the value range available for
known values. Additionally, computations can yield values inadvertently
(null values themselves are not changed during operations, so there is
no danger from this side). For example, if 5 is defined to mean null
then addition of two non-null values, such as 2+3, yields a null.

Every bit pattern in the range of a numeric type can appear in the
database, so no bit pattern is left to represent "null". If such a thing
is desired, then the database designer must provide, e.g., a separate
bit map indicating the status for each cell.

To have a clear semantics, the following rule holds:

**Uninitialized value handling**

A cell value not yet addressed, but within the current domain of an MDD
has a value of zero by definition; this extends in the obvious manner to
composite cells.

*Remark*

Note the limitation to the *current* domain of an MDD. While in the case
of an MDD with fixed boundaries this does not matter because always
*definition domain = current domain*, an MDD with variable boundaries
can grow and hence will have a varying current domain. Only cells inside
the current domain can be addressed, be they uninitialized/null or not;
addressing a cell outside the current domain will result in the
corresponding exception.

**Masks as alternatives to null**

For example, during piecewise import of satellite images into a large
map, there will be areas which are not written yet. Actually, also after
completely creating the map of, say, a country there will be untouched
areas, as normally no country has a rectangular shape with axis-parallel
boundaries. The outside cells will be initialized to 0 which may or may
not be defined as null. Another option is to define a Boolean mask array
of same size as the original array where each mask value contains *true*
for "cell valid" and *false* for "cell invalid. It depends on the
concrete application which approach benefits best.


Nulls in MDD-Valued Expressions
===============================

**Dynamically Set/Replace the Null Set**

The null set of an MDD value resulting from a sub-expression can be dynamically
changed on-the-fly with a postfix ``null values`` operator as follows: ::

    mddExp null values nullSet

As a result *mddExp* will have the null values specified by *nullSet*; if
*mddExp* already had a null set, it will be replaced.

**Null Set Propagation**

The null value set of an MDD is part of its type definition and, as
such, is carried along over the MDD's lifetime. Likewise, MDDs which are
generated as intermediate results during query processing have a null
value set attached. Rules for constructing the output MDD null set are
as follows:

-  The null value set of an MDD generated through an ``marray`` operation is
   empty [13]_.

-  The null value set of an operation with one input MDD object is
   identical to the null set of this input MDD.

-  The null value set of an operation with two input MDD objects is the
   union of the null sets of the input MDDs.

-  The null value set of an MDD expression with a postfix ``null values``
   operator is equal to the null set specified by it.

**Null Values in Operations**

Subsetting (trim and slice operations, as well as ``struct`` selection,
etc.) perform just as without nulls and deliver the original cell
values, be they null (relative to the MDD object on hand) or not. The
null value set of the output MDD is the same as the null value set of
the input MDD.

In MDD-generating operations with only one input MDD (such as marray and
unary induced operations), if the operand of a cell operation is null
then the result of this cell operation is null.

Generally, if somewhere in the input to an individual cell value
com­put­at­ion a null value is encountered then the overall result will
be null - in other words: *if at least one of the operands of a cell
operation is null then the overall result of this cell operation is
null*.

*Exceptions:*

-  Comparison operators (that is: ``==``, ``!=``, ``>``, ``>=``, ``<``, ``<=``)
   encountering a null value will *always* return a Boolean value; for example,
   both ``n == n`` and ``n != n`` (for any null value ``n``) will evaluate to
   ``false``.

-  In a cast operation, nulls are treated like regular values.

-  In a ``scale()`` operation, null values are treated like regular
   values [14]_.

-  Format conversion of an MDD object ignores null values. Conversion
   from some data format into an MDD likewise imports the actual cell
   values; however, during any eventual further processing of the target
   MDD as part of an **update** or **insert** statement, cell values
   listed in the null value set of the pertaining MDD definition will be
   interpreted as null and will not overwrite persistent non-null values.

**Choice of Null Value**

If an operation computes a null value for some cell, then the null value
effectively assigned is determined from the MDD's type definition.

If the overall MDD whose cell is to be set has exactly one null value,
then this value is taken. If there is more than one null value available
in the object's definition, then one of those null values is picked
non-deterministically. If the null set of the MDD is empty then no value
in the MDD is considered a null value.

**Example**

Assume an MDD ``a`` holding values ``<0, 1, 2, 3, 4, 5>`` and a null value set
of ``{2, 3}``. Then, ``a*2`` might return ``<0, 2, 2, 2, 8, 10>``. However,
``<0, 2, 3, 3, 8, 10>`` and ``<0, 2, 3, 2, 8, 10>`` also are valid results, as
the null value gets picked non-deterministically.


Nulls in Aggregation Queries
============================

In a condense operation, cells containing nulls do not contribute to the
overall result (in plain words, nulls are ignored).

If all values are null, then the result is the identity element in this case,
e.g. ``0`` for ``+``, ``true`` for ``and``, ``false`` for ``or``, maximum value
possible for the result base type for  ``min``, minimum value possible for the
result base type for ``max``, ``0`` for ``count_cells``.

The scalar value resulting from an aggregation query does not
carry a null value set like MDDs do; hence, during further processing it
is treated as an ordinary value, irrespective of whether it has
represented a null value in the MDD acting as input to the aggregation
operation.


Limitations
===========

All cell components of an MDD share the same same set of nulls, it is
currently not possible to assign individual nulls to cell type
components.


NaN Values
==========

NaN ("not a number") is the representation of a numeric value representing an
undefined or unrepresentable value, especially in floating-point calculations.
Systematic use of NaNs was introduced by the IEEE 754 floating-point standard
(`Wikipedia <http://en.wikipedia.org/wiki/NaN>`__).

In rasql, ``nan`` (double) and ``nanf`` (float) are symbolic floating point
constants that can be used in any place where a floating point value is allowed.
Arithmetic operations involving ``nan``\ s always result in ``nan``. Equality
and inequality involving nans work as expected, all other comparison operators
return false.

If the encoding format used supports NaN then rasdaman will encode/decode NaN
values properly.

**Example**

::

    select count_cells( c != nan ) from c
