.. highlight:: text

.. _sec_cheatsheets:

###########
Cheatsheets
###########

WCPS
====

The `OGC Web Coverage Processing Service (WCPS) standard 
<https://www.opengeospatial.org/standards/wcps>`_ defines a
protocol-independent declarative query language for the extraction, processing,
and analysis of multi-dimensional coverages representing sensor, image, or
statistics data.

The overall execution model of WCPS queries is similar to XQuery FLOWR:

.. code-block:: rasql

    for $covIter1 in (covName, ...),
        $covIter2 in (covName, ...),
        ...
    let $aliasVar1 := covExpr,
        $aliasVar2 := covExpr,
        ...
    where booleanExpr
    return processingExpr

Any coverage listed in the WCS *GetCapabilities* response can be used in place
of ``covName``. Multiple ``$covIter`` essentially translate to nested loops.
For each iteration, the ``return`` clause is evaluated if the result of the
``where`` clause is ``true``. Coverage iterators and alias variables can be
freely used in where / return expressions.

Conforming WCPS queries can be submitted to rasdaman as `WCS ProcessCoverages
requests <https://portal.opengeospatial.org/files/08-059r4>`_, e.g: ::

    http://localhost:8080/rasdaman/ows?service=WCS&version=2.0.1
        &request=ProcessCoverages
        &query=for $covIter in (covName) ...

The *WCS-client* deployed with every rasdaman installation provides a convenient
console for interactively writing and executing WCPS queries: open
http://localhost:8080/rasdaman/ows in your Web browser and proceed to the
*ProcessCoverages* tab.

Operations can be categorized by the type of data they result in: scalar,
coverage, or metadata.

Scalar operations
-----------------

- **Standard operations** applied on scalar operands return scalar results:

  +------------------------------+-----------------------------------------+
  | Operation category           | Operations                              |
  +==============================+=========================================+
  | Arithmetic                   | ``+  -  *  /  abs  round``              |
  +------------------------------+-----------------------------------------+
  | Exponential                  | ``exp  log  ln  pow  sqrt``             |
  +------------------------------+-----------------------------------------+
  | Trigonometric                | | ``sin  cos  tan  sinh  cosh  tanh``   |
  |                              | | ``arcsin  arccos  arctan``            |
  +------------------------------+-----------------------------------------+
  | Comparison                   | ``>  <  >=  <=  =  !=``                 |
  +------------------------------+-----------------------------------------+
  | Logical                      | ``and  or  xor  not  bit  overlay``     |
  +------------------------------+-----------------------------------------+
  | Select field from multiband  | ``.``                                   |
  | value                        |                                         |
  +------------------------------+-----------------------------------------+
  | Create multiband value       | ``{ bandName: value; ..., bandName:     |
  |                              | value }``                               |
  +------------------------------+-----------------------------------------+
  | Type casting                 | ``(baseType) value``                    |
  |                              |                                         |
  |                              | | where baseType is one of: boolean,    |
  |                              | | [unsigned] char / short / int / long, |
  |                              | | float, double, complex, complex2      |
  +------------------------------+-----------------------------------------+

- **Aggregation operations** summarize coverages into a scalar value. 

  +-----------------------+------------------------------------------------------+
  | Aggregation type      | Function / Expression                                |
  +=======================+======================================================+
  | Of numeric coverages  | ``avg``, ``add``, ``min``, ``max``                   |
  +-----------------------+------------------------------------------------------+
  | Of boolean coverages  | | ``count`` number of true values;                   |
  |                       | | ``some``/``all`` = true if some/all values are true|
  +-----------------------+------------------------------------------------------+
  | General condenser     | | ``condense`` *op*                                  |
  |                       | | ``over`` $iterVar axis(lo:hi), ...                 |
  |                       | | [ ``where`` boolScalarExpr ]                       |
  |                       | | ``using`` scalarExpr                               |
  +-----------------------+------------------------------------------------------+

  The *general condenser* aggregates values across an iteration domain with a condenser 
  operation *op* (one of ``+``, ``*``, ``max``, ``min``, ``and``, or ``or``).
  For each coordinate in the iteration domain defined by the ``over`` clause, the
  scalar expression in the ``using`` clause is evaluated and added to the final
  aggregated result; the optional ``where`` clause allows to filter values from
  the aggregation.

Coverage operations
-------------------

- **Standard operations** applied on coverage (or mixed coverage and scalar)
  operands return coverage results. The operation is applied pair-wise on each
  cell from the coverage operands, or on the scalars and each cell from the
  coverage in case some of the operands are scalars. All coverage operands must
  have matching domains and CRS.

- **Subsetting** allows to select a part of a coverage (or crop it to a smaller
  domain): ::

    covExpr[ axis1(lo:hi), axis2(slice), axis3:crs(...), ... ]
  
  1. ``axis1`` in the result is reduced to span from coordinate ``lo`` to ``hi``.
     Either or both ``lo`` and ``hi`` can be indicated as ``*``, corresponding to
     the minimum or maximum bound of that axis.

  2. ``axis2`` is restricted to the exact slice coordinate and removed from the
     result.

  3. ``axis3`` is subsetted in coordinates specified in the given ``crs``. By
     default coordinates must be given in the native CRS of ``C``.

- **Extend** is similar to subsetting but can be used to enlarge a coverage with 
  null values as well, i.e. lo and hi can extend beyond the min/max bounds of a
  particular axis; only trimming is possible: ::

    extend( covExpr, { axis1(lo:hi), axis2:crs(lo:hi), ... } )

- **Scale** is like extend but it resamples the current coverage values to fit 
  the new domain: ::

    scale( covExpr, { axis1(lo:hi), axis2:crs(lo:hi), ... } )

- **Reproject** allows to change the CRS of the coverage: ::

    crsTransform( covExpr, { axis1:crs1, axis2:crs2, ... } )

- **Conditional evaluation** is possible with the ``switch`` statement:

  .. code-block:: rasql

    switch
      case boolCovExpr return covExpr
      case boolCovExpr return covExpr
      ...
      default return covExpr

- **General coverage constructor** allows to create a coverage given a domain,
  where for each coordinate in the domain the value is dynamically calculated
  from a value expression which potentially references the iterator variables:

  .. code-block:: rasql

    coverage covName
    over $iterVar axis(lo:hi), ...
    values scalarExpr

- **General condenser on coverages** is same as the scalar general condenser,
  except that in the ``using`` clause we have a coverage expression. The coverage 
  values produced in each iteration are cell-wise aggregated into a single
  result coverage.

  .. code-block:: rasql

    condense op
    over $iterVar axis(lo:hi), ...
    [ where boolScalarExpr ]
    values covExpr

- **Encode** allows to export coverages in a specified data format, e.g: ::

    encode(covExpr, "image/jpeg")


Metadata operations
-------------------

Several functions allow to extract metadata information about a coverage ``C``:

+---------------------------+-----------------------------------------+
| Metadata function         | Result                                  |
+===========================+=========================================+
| imageCrsDomain(C, a)      | Grid (lo, hi) bounds for axis a.        |
+---------------------------+-----------------------------------------+
| domain(C, a, c)           | Geo (lo, hi) bounds for axis a in CRS c.|
+---------------------------+-----------------------------------------+
| crsSet(C)                 | Set of CRS identifiers.                 |
+---------------------------+-----------------------------------------+
| nullSet(C)                | Set of null values.                     |
+---------------------------+-----------------------------------------+
