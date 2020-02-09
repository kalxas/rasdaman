.. highlight:: text

.. _sec_cheatsheets:

###########
Cheatsheets
###########

.. _cheatsheet-wcs:

WCS
===

The `OGC Web Coverage Service (WCS) standard 
<https://www.opengeospatial.org/standards/wcs>`_ defines support for modeling
and retrieval of geospatial data as *coverages* (e.g. sensor, image, or
statistics data).

WCS consists of a *Core* specification for basic operation support with regards
to coverage discovery and retreival, and various *Extension* specifications for
optional capabilities that a service could provide on offered coverage objects.

Core
----

The Core specification is agnostic of implementation details, hence, access 
syntax and mechanics are defined by *protocol extensions*:
`KVP/GET <https://portal.opengeospatial.org/files/09-147r3>`__,
`XML/POST <https://portal.opengeospatial.org/files/09-148r1>`__, and 
`XML/SOAP <https://portal.opengeospatial.org/files/09-149r1>`__.  Rasdaman 
supports all three, but further on the examples are in *KVP/GET* exclusively, as it
is the most straightforward way for constructing requests by appending a standard
`query string <https://en.wikipedia.org/wiki/Query_string>`__ to the
service endpoint URL. Commonly, for all operations the KVP/GET request will look
as follows: ::

  http(s)://<endpoint url>?service=WCS
                          &version=2.0.1
                          &request=<operation>
                          &...

Three fundamental operations are defined by the Core:

- **GetCapabilities** - returns overal service information and a list of available
  coverages; the request looks generally as above, with the `<operation>` being
  GetCapabilities:

  ::

    http(s)://<endpoint url>?service=WCS&version=2.0.1&request=GetCapabilities

- **DescribeCoverage** - detailed description of a specific coverage:

  ::

    http(s)://<endpoint url>?service=WCS&version=2.0.1&request=DescribeCoverage
                            &coverageId=<coverage id>

- **GetCoverage** - retreive a whole coverage, or arbitrarily restricted on any of
  its axes whether by new lower/upper bounds (*trimming*) or at a single index
  (*slicing*):

  ::

    http(s)://<endpoint url>?service=WCS&version=2.0.1&request=GetCoverage
                            &coverageId=<coverage id>
            [optional]      &subset=<axis>(<lower>:<upper>)
            [optional]      &subset=<axis>(<index>)
            [optional]      &format=<mime type>

  Example:

  ::

    http://ows.rasdaman.org/ows?service=WCS&version=2.0.1&request=DescribeCoverage
      &coverageId=My3DCov&subset=Lat(10.0:15.3)&subset=time("2020-02-11")&format=image/tiff


Updating
--------

The `Transaction extension (WCS-T) 
<http://docs.opengeospatial.org/is/13-057r1/13-057r1.html>`__ specifies the
following operations for constructing, maintenance, and removal of coverages on
a server: *InsertCoverage*, *UpdateCoverage*, and *DeleteCoverage*.

Rasdaman provides the `wcst_import tool
<http://doc.rasdaman.org/05_geo-services-guide.html#data-import>`__ to simplify
the ingestion of data into analysis-ready coverages (aka datacubes) by 
generating WCS-T requests as instructed by a simple configuration file.


Processing
----------

The `Processing extension <https://portal.opengeospatial.org/files/08-059r4>`__
enables advanced analytics on coverages through `WCPS <cheatsheet-wcps>`__
queries. The request format is as follows: ::

  http(s)://<endpoint url>?service=WCS&version=2.0.1&request=ProcessCoverages
                          &query=<wcps query>

E.g, calculate the average on the subset from the previous GetCoverage example: ::

  http://ows.rasdaman.org/ows?service=WCS&version=2.0.1&request=ProcessCoverages
    &query=for $c in (My3DCov) return avg($c[Lat(10.0:15.3), time("2020-02-11")])


Range subsetting
----------------

The cell values of some coverages consist of multiple components (also known as
ranges, bands, channels, fields, attributes). The `Range subsetting extension
<https://portal.opengeospatial.org/files/12-040>`__ specifies the extraction
and/or recombination in possibly different order of one or more bands. This is
done by listing the wanted bands or band intervals; e.g assuming `MyRGBCov` has
red, green, and blue bands, the following recombines the bands into a green,
blue, red order: ::

  http://ows.rasdaman.org/ows?service=WCS&version=2.0.1&request=GetCoverage
    &coverageId=MyRGBCov&rangesubset=green:blue,red


Scaling
-------

Scaling up or down is a common operation supported by the `Scaling extension
<https://portal.opengeospatial.org/files/12-039>`__. An additional GetCoverage
parameter indicates the scale factor in several possible ways: as a single 
number applying to all axes, multiple numbers applying to individual axes,
full target scale domain, or per-axis target scale domains. E.g. a single factor
downscale all axes by 2x: ::

  http://ows.rasdaman.org/ows?service=WCS&version=2.0.1&request=GetCoverage
    &coverageId=MyLargeCov&scaleFactor=0.5


Reprojection
------------

The `CRS extension <https://portal.opengeospatial.org/files/54209>`__ allows to
reproject a coverage before retreiving it: ::

  http://ows.rasdaman.org/ows?service=WCS&version=2.0.1&request=GetCoverage
    &coverageId=MyCov&outputCrs=http://www.opengis.net/def/crs/EPSG/0/4326

or change the CRS in which subset or scale coordinates are specified: ::

  http://ows.rasdaman.org/ows?service=WCS&version=2.0.1&request=GetCoverage
    &coverageId=MyCov&subsetCrs=http://www.opengis.net/def/crs/EPSG/0/4326


Interpolation
-------------

Scaling or reprojection can be performed with various interpolation methods as
enabled by the `Interpolation extension
<https://portal.opengeospatial.org/files/12-049>`__: ::

  http://ows.rasdaman.org/ows?service=WCS&version=2.0.1&request=GetCoverage
    &coverageId=MyCov&interpolation=http://www.opengis.net/def/interpolation/OGC/1/cubic

Rasdaman supports several interpolations as documented `here
<http://doc.rasdaman.org/04_ql-guide.html#the-project-function>`__.


.. _cheatsheet-wcps:

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


.. _cheatsheet-wms:

WMS
===

The `OGC Web Map Service (WMS) standard 
<https://www.opengeospatial.org/standards/wms>`_ defines map portrayal on
geo-spatial data. In rasdaman, a WMS service can be enabled on any coverage,
including 3-D or higher dimensional; the latest 1.3.0 version is supported.

WMS defines three operations: *GetCapabilities*, *GetMap*, and *GetFeatureInfo*.
We will not go into the details, as users do not normally hand-write WMS 
requests, but let a client tool or library generate them instead. Below we list
several widely used Web and Desktop tools with support for WMS 1.3:

- `OpenLayers <https://openlayers.org/>`__
- `NASA WebWorldWind <https://worldwind.arc.nasa.gov/web/>`__
- `Leaflet <https://leafletjs.com/examples/wms/wms.html>`__
- `QGIS <https://docs.qgis.org/3.4/en/docs/user_manual/working_with_ogc/ogc_client_support.html#wms-wmts-client>`__
