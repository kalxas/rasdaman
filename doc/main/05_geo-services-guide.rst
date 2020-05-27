.. highlight:: text

.. _sec_geo-services-guide:

##################
Geo Services Guide
##################

This guide covers the details of geo data management and retrieval in rasdaman.
The rasdaman Array DBMS is domain agnostic; the specific semantics of space and time
is provided through a layer on top of rasdaman, historically known as *petascope*.
It offers spatio-temporal access and analytics through APIs based on the OGC data standard
*Coverage Implementation Schema* (CIS) and the OGC service standards
*Web Map Service* (WMS), *Web Coverage Service* (WCS), and *Web Coverage Processing Service* (WCPS).

.. NOTE::
   While the name petascope addresses a specific component we frequently use the name *rasdaman*
   to refer to the complete system, including petascope.

OGC Coverage Standards Overview
===============================

For operating rasdaman geo services as well as for accessing such geo services through these APIs
it is important to understand the mechanics of the relevant standards.
In particular, the concept of OGC / ISO *coverages* is important.

In standardization, coverages are used to represent space/time varying phenomena, concretely:
regular and irregular grids, point clouds, and general meshes. The coverage standards offer data and service models for dealing with those.
In rasdaman, focus is on multi-dimensional gridded ("raster") coverages.

In rasdaman, the OGC standards WMS, WCS, and WCPS are supported, being reference implementation for WCS.
These APIs serve different purposes:
- WMS delivers a 2D map as a visual image, suitable for consunmption by humans
- WCS delivers n-D data, suitable for further processing and analysis
- WCPS performs flexible server-side processing, filtering, analytics, and fusion on coverages.

These coverage data and service concepts are summarized briefly below;
for specific details on coordinate reference system handling see also :ref:`CRS definition management <crs-def-management>`.
Ample material is also available on the Web for familiarization with coverages (best consult in this sequence):
- `hands-on demos <https://standards.rasdaman.org>`_ for multi-dimensional coverage services provided by `Jacobs University <https://www.jacobs-university.de/lsis>`;
- a series of `webinars and tutorial slides <https://www.earthserver.xyz/webinars>`_ provided by `EarthServer <https://www.earthserver.xyz>`;
- a `range of background information <http://myogc.org/go/coveragesDWG>`_ on these standards provided by OGC;
- the official standards documents maintained by `OGC <http://www.opengeospatial.org>`_:

 - `WCS 2.0.1 <https://portal.opengeospatial.org/files/09-110r4>`_
 - `WCS-T 2.0 <http://docs.opengeospatial.org/is/13-057r1/13-057r1.html>`_
 - `WCPS 1.0 <https://portal.opengeospatial.org/files/08-059r4>`_
 - `WMS 1.3.0 <http://portal.opengeospatial.org/files/?artifact_id=4756&passcode=4hy072w9zerhjyfbqfhq>`_


Coverage Data
-------------

OGC CIS specifies an interoperable, conformance-testable coverage structure
independent from any particular format encoding.
Encodings are defined in OGC in GML, JSON, RDF,
as well as a series of binary formats including GeoTIFF, netCDF, JPEG2000,
and GRIB2).

By separating the data definition (CIS) from the service definition (WCS)
it is possible for coverages to be served throuigh a variety of APIs, such
as WMS, WPS, and SOS.
However, WCS and WCPS have coverage-specific functionality
making them particularly suitable for flexible coverage acess, analytics,
and fusion.

Coverage Services
-----------------

OGC WMS delivers 2D maps generated from styled layers stacked up.
As such, WMS is a visualization service sitting at the end of processing
pipelines, geared towards human consumption.

OGC WCS, on the other hand, rpovides data suitable for further processing
(including visualization); as such, it is suitable also for machine-to-machine
communication as it appears in the middle of longer processing pipelines.
WCS is a modular suite of service functionality on coverages.
WCS Core defines download of coverages and parts thereof, through *subsetting*
directives, as well as delivery in some output format requested by the client.
A set of WCS Extensions adds further functionality facets.

One of those is WCS Processing; it defines the ``ProcessCoverages`` request
which allows sending a coverage analytics request through the WCPS spatio-temporal
analytics language. WCPS supports extraction, analytics, and fusion of
multi-dimensional coverage expressed in a high-level, declarative,
and safe language.


OGC Web Services Endpoint
=========================

Once the petascope servlet is deployed (see :ref:`rasdaman installation guide <sec-system-install-packages>`) coverages can be accessed through service endpoint ``/rasdaman/ows``.

.. NOTE::

   Endpoint ``/rasdaman/rasql``, which by default is also available after deploying
   rasdaman, does not know about coverages and their services, but only knows domain-agnostic rasql.

For example, assuming that the service's IP address is ``123.456.789.1`` and the
service port is ``8080``, the following request URLs would deliver the
Capabilities documents for OGC WMS and WCS, respectively:

.. code-block:: text

    http://123.456.789.1:8080/rasdaman/ows?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.3.0
    http://123.456.789.1:8080/rasdaman/ows?SERVICE=WCS&REQUEST=GetCapabilities&VERSION=2.0.1


Coverage Implementation Schema (CIS)
====================================

A coverage consists mainly of:

    - *domain set*: provides information about where data sit in space and time.
      All coordinates expressed there are relative to the coverage's
      Coordinate Reference System or *Native CRS*. Both CRS and its axes, units
      of measure, etc. are indiciated in the domain set.
      Petascope currently supports grid topologies whose axes are aligned with the
      axes of the CRS. Along these axes, grid lines can be spaced regularly
      or irregularly.
    - *range set*: the "pixel payload", ie: the values (which can be atomic,
      like in a DEM, or records of values, like in hyperspectral imagery).
    - *range type*: the semantics of the range values, given by type information,
      null values, accuracy, etc.
    - *metadata*: a black bag which can contain any data: the coverage will not
      understand these, but duly transport them along so that the connection
      between data and metadata is not lost.

Further components include ``Envelope`` which gives a rough, simplified overview
on the coverage's location in space and time and ``CoverageFunction`` which is
unused by any implementation known to us.


Coordinate Reference Systems in Coverages
-----------------------------------------

Every coverage, as per OGC CIS, must have exactly one Native CRS. Sometimes
definitions for such CRSs are readily available, such as with the EPSG registry
where 2-D WGS84 is readily available under its code EPSG:4326. In particular
spatio-temporal CRSs, however, are not always readily available, at least not
in all combinations of spatial and temporal axes. To this end, composition
of CRS is supported so that the single Native CRS can be built from
"ingredient" CRSs by concatenating these CRSs into a composite one.

For instance, a time-series of WGS84 images would have the following Native CRS:

.. code-block:: text

   http://localhost:8080/def/crs-compound?
         1=http://localhost:8080/def/crs/EPSG/0/4326
        &2=http://localhost:8080/def/crs/OGC/0/AnsiDate

Coordinate tuples in this CRS represent an ordered composition of a geospatial
CRS with Latitude followed by Longitude, as per EPSG:4326, followed by
a temporal coordinate expressed in ISO 8601 syntax,
such as: ``2012-01-01T00:01:20Z``.

Several ways exist for determining the Native CRS of coverage domain set:

    * in a WCS ``GetCapabilities`` response, check the ``wcs:CoverageSummary/ows:BoundingBox@crs`` attribute;
    * in a WCS ``DescribeCoverage`` response, check the ``@srsName`` attribute in the ``gml:domainSet``;
    * in WCPS, use function ``crsSet(e)`` to determine the CRS of a coverage expression *e*;

.. NOTE::

   In a coverage also consider the ``axisLabels`` attributes giving the axis
   names as used in the coverage, in proper sequence as per CRS; 
   the ``uomLabels`` attribute contains the units of measure for each axis.

The following graphics illustrates, on the example of an image timeseries,
how dimension, CRS, and axis labels affect the domain set in
a CIS 1.0 ``RectifiedGridCoverage``.

.. IMAGE:: media/geo-services-guide/GridDomainSetAxes.png
    :align: center
    :scale: 50%

.. NOTE::

   This handling of coordinates in CIS 1.0 bears some legacy burden from GML;
   in the ``GeneralGridCoverage`` introduced with CIS 1.1 coordinate handling is much simplified.


.. _crs-def-management:

CRS Management
--------------

the Native CRS of a coverage is given by a URL, as per OGC convention.
Resolving this URL should deliver the CRS definition.
The `OGC CRS resolver <http://external.opengeospatial.org/twiki_public/CRSdefinitionResolver>`_ is one such service. Its implementation is running SECORE which is part of rasdaman community.

By providing the source code of the OGC resolver it is possible to deploy
one's own resolver under an own URL, such as http://rasdaman.org:8080/def/crs/EPSG/0/27700.


Range Type
----------

Range values can be atomic or (possibly nested) records over atomic values,
described by the range type. In rasdaman the following atomic data types
are supported; all of these can be combined freely in records of values,
such as in hyperspectral images or climate variables.

.. TABLE:: Mapping of rasdaman base types to SWE Quantity types

    +--------------------+------------+------------------------------------------+
    | **rasdaman type**  | **size**   | **Quantity types**                       |
    +====================+============+==========================================+
    | ``boolean``        | 8 bit      | unsignedByte                             |
    +--------------------+------------+------------------------------------------+
    | ``octet``          | 8 bit      | signedByte                               |
    +--------------------+------------+------------------------------------------+
    | ``char``           | 8 bit      | unsignedByte                             |
    +--------------------+------------+------------------------------------------+
    | ``short``          | 16 bit     | signedShort                              |
    +--------------------+------------+------------------------------------------+
    | ``unsigned short`` | 16 bit     | unsignedShort                            |
    | = ``ushort``       |            |                                          |
    +--------------------+------------+------------------------------------------+
    | ``long``           | 32 bit     | signedInt                                |
    +--------------------+------------+------------------------------------------+
    | ``unsigned long``  | 32 bit     | unsignedInt                              |
    | = ``ulong``        |            |                                          |
    +--------------------+------------+------------------------------------------+
    | ``float``          | 32 bit     | float32                                  |
    +--------------------+------------+------------------------------------------+
    | ``double``         | 64 bit     | float64                                  |
    +--------------------+------------+------------------------------------------+
    | ``complex``        | 64 bit     | cfloat32                                 |
    +--------------------+------------+------------------------------------------+
    | ``complexd``       | 128 bit    | cfloat64                                 |
    +--------------------+------------+------------------------------------------+


Nil Values
----------
Nil (**"null"**) values, as per SWE, are supported by rasdaman in an extended way:

    - null values can be defined over any data type
    - nulls can be single values
    - nulls can be intervals
    - a null definnition in a coverage can be a list of all of the above alternatives.

.. NOTE::

   It is highly recommended to NOT define null values **over floating-point numbers**
   as this causes numerical problems well known in mathematics.
   This is not related to rasdaman, but intrinsic to the nature and handling of
   floating-point numbers in computers. If really desired, **a floating-point
   interval should be defined** around the desired float null value
   (this corresponds to interval arithmetics in numerical mathematics).


OGC Web Coverage Service
========================

WCS Core offers request types:

    - ``GetCapabilities`` for obtaining a list of coverages offered together
      with an overall service description;
    - ``DescribeCoverage`` for obtaining information about a coverage without downloading it;
    - ``GetCoverage`` for downloading, extracting, and reformatting of coverages;
      this is the central workhorse of WCS.

WCS Extensions in part enhance ``GetCoverage`` with additional functionality
controlled by further parameters, and in part establish new request types,
such as:

    - WCS-T defining ``InsertCoverage``, ``DeleteCoverage``, and
      ``UpdateCoverage`` requests;
    - WCS Processing defining ``ProcessCoverages`` for submitting WCPS
      analytics code.

You can use ``http://localhost:8080/rasdaman/ows`` as service endpoints to which to
send WCS requests, for example:

.. code-block:: text

    http://localhost:8080/rasdaman/ows?service=WCS&version=2.0.1&request=GetCapabilities

See `example queries <http://rasdaman.org/browser/systemtest/testcases_services/test_wcs/queries>`_
in the WCS systemtest which send KVP (key value pairs) GET request and XML POST
request to Petascope.

CIS 1.0 to 1.1 Transformation
-----------------------------

Under WCS 2.1 - ie: with ``SERVICE=2.1.0`` - both ``DescribeCoverage``and ``GetCoverage``
requests understand the proprietary parameter ``OUTPUTTYPE=GeneralGridCoverage``
which formats the result as CIS 1.1 ``GeneralGridCoverage`` even if it has been
imported into the server as a CIS 1.0 coverage, for example:

.. code-block:: text

   http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.1.0
       &REQUEST=DescribeCoverage
       &COVERAGEID=test_mean_summer_airtemp
       &OUTPUTTYPE=GeneralGridCoverage

   http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.1.0
       &REQUEST=GetCoverage
       &COVERAGEID=test_mean_summer_airtemp
       &FORMAT=application/gml+xml
       &OUTPUTTYPE=GeneralGridCoverage

Polygon/Raster Clipping
-----------------------

WCS and WCPS support clipping of polygons expressed in the
`WKT format <https://en.wikipedia.org/wiki/Well-known_text>`_ format.
Polygons can be ``MultiPolygon (2D)``, ``Polygon (2D)`` and ``LineString (1D+)``.
The result is always a 2D coverage in case of MultiPolygon and Polygon, and
is a 1D coverage in case of ``LineString``.

Further clipping patterns include ``curtain`` and ``corridor`` on 3D+ coverages
from ``Polygon (2D)`` and ``Linestring (1D)``.
The result of ``curtain`` clipping has the same dimensionality as the input coverage
whereas the result of ``corridor`` clipping is always a 3D coverage,
with the first axis being the *trackline* of the corridor by convention.

Below some examples are presented expaining the mimics for WCS.

Syntactically, clipping is expressed by adding a ``&CLIP=`` parameter to the request.
If the ``SUBSETTINGCRS`` parameter is specified then this CRS also applies
to the clipping WKT, otherwise it is assumed that the WKT is in the
Native coverage CRS.

Clipping Examples
^^^^^^^^^^^^^^^^^

-  Polygon clipping on coverage with Native CRS ``EPSG:4326``, for example:

   .. hidden-code-block:: text

        http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1&
          &REQUEST=GetCoverage
          &COVERAGEID=test_wms_4326
          &CLIP=POLYGON((55.8 -96.6, 15.0 -17.3))
          &FORMAT=image/png

-  Polygon clipping with coordinates in ``EPSG:3857`` (from ``subsettingCRS`` parameter)
   on coverage with Native CRS ``EPSG:4326``, for example:

   .. hidden-code-block:: text

        http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
          &REQUEST=GetCoverage
          &COVERAGEID=test_wms_4326
          &CLIP=POLYGON((13589894.568 -2015496.69612, 15086830.0246 -1780682.3822))
          &SUBSETTINGCRS=http://opengis.net/def/crs/EPSG/0/3857
          &FORMAT=image/png

-  Linestring clipping on a 3D coverage with axes ``X``, ``Y``, ``ansidate``,
   for example:

   .. hidden-code-block:: text

        http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
          &REQUEST=GetCoverage
          &COVERAGEID=test_irr_cube_2
          &CLIP=LineStringZ(75042.7273594 5094865.55794 "2008-01-01T02:01:20.000Z", 705042.727359 5454865.55794 "2008-01-08T00:02:58.000Z")
          &FORMAT=text/csv

-  Multipolygon clipping on 2D coverage, for example:

   .. hidden-code-block:: text

        http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
          &REQUEST=GetCoverage
          &COVERAGEID=test_mean_summer_airtemp
          &CLIP=Multipolygon( ((-23.189600 118.432617, -27.458321 117.421875,
                                -30.020354 126.562500, -24.295789 125.244141)),
                              ((-27.380304 137.768555, -30.967012 147.700195,
                                -25.491629 151.259766, -18.050561 142.075195)) )
          &FORMAT=image/png

-  Curtain clipping by a Linestring on 3D coverage, for example:

   .. hidden-code-block:: text

        http://localhost:8080/rasdaman/ows?SERVICE=WCSVERSION=2.0.1
          &REQUEST=GetCoverage
          &COVERAGEID=test_eobstest
          &CLIP=CURTAIN( projection(Lat, Long), linestring(25 41, 30 41, 30 45, 30 42) )
          &FORMAT=text/csv

-  Curtain clipping by a Polygon on 3D coverage, for example:

   .. hidden-code-block:: text

        http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
          &REQUEST=GetCoverage
          &COVERAGEID=test_eobstest
          &CLIP=CURTAIN(projection(Lat, Long), Polygon((25 40, 30 40, 30 45, 30 42)))
          &FORMAT=text/csv

-  Corridor clipping by a Linestring on 3D coverage, for example:

   .. hidden-code-block:: text

        http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
          &REQUEST=GetCoverage
          &COVERAGEID=test_irr_cube_2
          &CLIP=CORRIDOR( projection(E, N),
               &LineString(75042.7273594  5094865.55794 "2008-01-01T02:01:20.000Z",
                          &75042.7273594 5194865.55794 "2008-01-01T02:01:20.000Z"),
               &LineString(75042.7273594 5094865.55794, 75042.7273594 5094865.55794,
                          &85042.7273594 5194865.55794, 95042.7273594 5194865.55794)
              &)
          &FORMAT=application/gml+xml

-  Corridor clipping by a Polygon on 3D coverage, for example:

   .. hidden-code-block:: text

        http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
          &REQUEST=GetCoverage
          &COVERAGEID=test_eobstest
          &CLIP=corridor( projection(Lat, Long),
                          LineString(26 41 "1950-01-01", 28 41 "1950-01-02"),
                          Polygon((25 40, 30 40, 30 45, 25 45)), discrete )
          &FORMAT=application/gml+xml

WCS-T
-----

Currently, WCS-T supports coverages in GML format for importing. The metadata of
the coverage is thus explicitly specified, while the raw cell values can be
stored either explicitly in the GML body, or in an external file linked in the
GML body, as shown in the examples below. The format of the file storing the
cell values must be one
`supported by the GDAL library <http://www.gdal.org/formats_list.html>`_,
such as TIFF / GeoTIFF, JPEG, JPEG2000, PNG etc.

In addition to the WCS-T standard parameters petascope supports additional
proprietary parameters.

.. NOTE::

   For coverage management normally WCS-T is not used directly.
   Rather, the more convenient ``wcst_import`` Python importing tool
   is recommended for :ref:`data-import`.

Inserting coverages
^^^^^^^^^^^^^^^^^^^

Inserting a new coverage into the server's WCS offerings is done using the
``InsertCoverage`` request.

.. table:: WCS-T Standard Parameters

    +------------------+------------------------+----------------------------------------------------------+-----------------------------+
    |Request           |Value                   |Description                                               |Required                     |
    |Parameter         |                        |                                                          |                             |
    +==================+========================+==========================================================+=============================+
    |SERVICE           |WCS                     |service standard                                          |Yes                          |
    +------------------+------------------------+----------------------------------------------------------+-----------------------------+
    |VERSION           |2.0.1 or later          |WCS version used                                          |Yes                          |
    +------------------+------------------------+----------------------------------------------------------+-----------------------------+
    |REQUEST           |InsertCoverage          |Request type to be performed                              |Yes                          |
    +------------------+------------------------+----------------------------------------------------------+-----------------------------+
    |INPUTCOVERAGEREF  |{url}                   |URl pointing to the coverage to be inserted               |One of inputCoverageRef or   |
    |                  |                        |                                                          |inputCoverage is required    |
    +------------------+------------------------+----------------------------------------------------------+-----------------------------+
    |INPUTCOVERAGE     |{coverage}              |A coverage to be inserted                                 |One of inputCoverageRef or   |
    |                  |                        |                                                          |inputCoverage is required    |
    +------------------+------------------------+----------------------------------------------------------+-----------------------------+
    |USEID             |new | existing          |Indicates wheter to use the coverage's id ("existing")    |No (default: existing)       |
    |                  |                        |or to generate a new unique one ("new")                   |                             |
    +------------------+------------------------+----------------------------------------------------------+-----------------------------+

.. table:: WCS-T Proprietary Enhancements

    +-------------+-------------------------------------------------+----------------------------------------------------------+--------+
    |Request      |Value                                            |Description                                               |Required|
    |Parameter    |                                                 |                                                          |        |
    +=============+=================================================+==========================================================+========+
    |PIXELDATATYPE|GDAL supported base data type (eg: "Float32") or |In cases where range values are given in the GML body     |No      |
    |             |comma-separated concatenated data types, (eg:    |the datatype can be indicated through this parameter.     |        |
    |             |"Float32,Int32,Float32")                         |Default: Byte.                                            |        |
    +-------------+-------------------------------------------------+----------------------------------------------------------+--------+
    |TILING       |rasdaman tiling clause, see                      |Indicates the array tiling to be applied during insertion |No      |
    |             |`wiki:Tiling <http://rasdaman.org/wiki/Tiling>`_ |                                                          |        |
    +-------------+-------------------------------------------------+----------------------------------------------------------+--------+

The response of a successful coverage request is the coverage id of the newly
inserted coverage. For example: The coverage available at
http://schemas.opengis.net/gmlcov/1.0/examples/exampleRectifiedGridCoverage-1.xml
can be imported with the following request:

.. code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
        &REQUEST=InsertCoverage
        &COVERAGEREF=http://schemas.opengis.net/gmlcov/1.0/examples/exampleRectifiedGridCoverage-1.xml

The following example shows how to insert a coverage stored on the
server on which rasdaman runs. The cell values are stored in a TIFF file
(attachment:myCov.gml), the coverage id is generated by the server and
aligned tiling is used for the array storing the cell values:

.. code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
        &REQUEST=InsertCoverage
        &COVERAGEREF=file:///etc/data/myCov.gml
        &USEID=new
        &TILING=aligned[0:500,0:500]


Updating Coverages
^^^^^^^^^^^^^^^^^^

.. _update-coverage:

Updating an existing coverage into the server's WCS offerings is done using the ``UpdateCoverage`` request.

.. table:: WCS-T Standard Parameters

    +------------------+----------------------------------------------+----------------------------------------------------------+-----------------------------+
    |Request           |Value                                         |Description                                               |Required                     |
    |Parameter         |                                              |                                                          |                             |
    +==================+==============================================+==========================================================+=============================+
    |SERVICE           |WCS                                           |service standard                                          |Yes                          |
    +------------------+----------------------------------------------+----------------------------------------------------------+-----------------------------+
    |VERSION           |2.0.1 or later                                |WCS version used                                          |Yes                          |
    +------------------+----------------------------------------------+----------------------------------------------------------+-----------------------------+
    |REQUEST           |UpdateCoverage                                |Request type to be performed                              |Yes                          |
    +------------------+----------------------------------------------+----------------------------------------------------------+-----------------------------+
    |COVERAGEID        |{string}                                      |Identifier of the coverage to be updated                  |Yes                          |
    +------------------+----------------------------------------------+----------------------------------------------------------+-----------------------------+
    |INPUTCOVERAGEREF  |{url}                                         |URl pointing to the coverage to be inserted               |One of inputCoverageRef or   |
    |                  |                                              |                                                          |inputCoverage is required    |
    +------------------+----------------------------------------------+----------------------------------------------------------+-----------------------------+
    |INPUTCOVERAGE     |{coverage}                                    |A coverage to be updated                                  |One of inputCoverageRef or   |
    |                  |                                              |                                                          |inputCoverage is required    |
    +------------------+----------------------------------------------+----------------------------------------------------------+-----------------------------+
    |SUBSET            |AxisLabel(geoLowerBound,geoUpperBound)        |Trim or slice expression, one per updated                 |No                           |
    |                  |                                              |coverage dimension                                        |                             |
    +------------------+----------------------------------------------+----------------------------------------------------------+-----------------------------+

The following example shows how to update an existing coverage ``test_mr_metadata``
from a generated GML file by ``wcst_import`` tool:

.. code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WCS&version=2.0.1
        &REQUEST=UpdateCoverage
        &COVRAGEID=test_mr_metadata
        &SUBSET=i(0,60)
        &subset=j(0,40)
        &INPUTCOVERAGEREF=file:///tmp/4514863c_55bb_462f_a4d9_5a3143c0e467.gml


Deleting Coverages
^^^^^^^^^^^^^^^^^^

.. _delete-coverage:

The ``DeleteCoverage`` request type serves to delete a coverage (consisting of
the underlying rasdaman collection, the associated WMS layer (if exists)
and the petascope metadata).
For example: The coverage ``test_mr`` can be deleted as follows:

.. code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1
      &REQUEST=DeleteCoverage
      &COVERAGEID=test_mr

.. _petascope-update-coverage-metadata:

Coverage Metadata Update
------------------------

Coverage metadata can be updated through the interactive rasdaman WSClient
by selecting a text file (MIME type one of: ``text/xml``, ``application/json``,
``text/plain``) containing new metadata and upload it to petascope.
Then, petascope will read the content of the text file and update corresponding
coverage's metadata.

.. NOTE::

   This WSClient feature is login protected: **OGC WCS > Describe Coverage tab**
   when one is already **logged in** with petascope admin user in **Admin tab**.

The service URL for this feature is ``http://localhost:8080/rasdaman/ows/admin/UpdateCoverageMetadata``
which operates through multipart/form-data POST requests. The request should
contain 2 parts: the first part is coverageId to update, the second part is a
path to a local text file to be uploaded to server.

Alternatively, one can use REST API to update a coverage metadata with
petascope admin user's credentials *via basic authentication headers method*
by *curl* tool. For example: Metadata of coverage ``test_mr_metadata``
will be updated from the local XML file at ``/home/rasdaman/Downloads/test_metadata.xml``:

.. code-block:: text

   curl --user petauser:PETASCOPE_ADMIN_PASSWORD 
               -F "coverageId=test_mr_metadata" 
               -F "file=@/home/rasdaman/Downloads/test_metadata.xml" 
               "http://localhost:8080/rasdaman/ows/admin/UpdateCoverageMetadata"


Web Coverage Processing Service (WCPS)
======================================

The OGC Web Coverage Processing Service (WCPS) standard defines a
protocol-independent language for the extraction, processing, analysis,
and fusion of multi-dimensional gridded coverages, often called
`datacubes <https://en.wikipedia.org/wiki/Data_cube>`_.

General
-------

WCPS requests can be submitted in both
abstract syntax (`example <http://rasdaman.org/browser/systemtest/testcases_services/test_wcps/queries/233-extra_params_merge_new_metadata.test>`_)
and in XML (`example <http://rasdaman.org/browser/systemtest/testcases_services/test_wcps/queries/245-test_enqoute_cdata_greate_less_character.xml>`_).

For example, using the WCS GET/KVP protocol binding a WCPS request can be sent
through the following ``ProcessCoverages`` request:

.. code-block:: text

    http://localhost:8080/rasdaman/ows?service=WCS&version=2.0.1
      &REQUEST=ProcessCoverage&QUERY=<wcps-query>

Polygon/Raster Clipping
-----------------------

The proprietary ``clip()`` function with the same effect as clipping is available with WCPS.
The signature is as follows: 

.. code-block:: text

    clip( coverageExpression, wkt [, subsettingCrs ] )

where
-  ``coverageExpression`` is an expression of result type coverage, eg: ``dem + 10``;
-  ``wkt`` is a valid WKT (Well-Known Text) expression, e.g. ``POLYGON((...))``, ``LineString(...)``;
-  ``subsettingCrs`` is an optional CRS in which the ``wkt``coordinates are expressed, eg: ``http://opengis.net/def/crs/EPSG/0/4326``.

Clipping Examples
^^^^^^^^^^^^^^^^^

- Polygon clipping with coordinates in ``EPSG:4326`` on coverage with Native CRS ``EPSG:3857``:

  .. hidden-code-block:: text

    for c in (test_wms_3857)
    return
      encode(
        clip( c,
              POLYGON((
                -17.8115 122.0801, -15.7923 135.5273,
                -24.8466 151.5234, -19.9733 137.4609,
                -33.1376 151.8750, -22.0245 135.6152,
                -37.5097 145.3711, -24.4471 133.0664,
                -34.7416 135.8789, -25.7207 130.6934,
                -31.8029 130.6934, -26.5855 128.7598,
                -32.6949 125.5078, -26.3525 126.5625,
                -35.0300 118.2129, -25.8790 124.2773,
                -30.6757 115.4004, -24.2870 122.3438,
                -27.1374 114.0820, -23.2413 120.5859,
                -22.3501 114.7852, -21.4531 118.5645
              )),
              "http://opengis.net/def/crs/EPSG/0/4326"
        ),
        "image/png"
      )

- Linestring clipping on 3D coverage with axes ``X``, ``Y``, ``datetime``.

  .. hidden-code-block:: text

    for c in (test_irr_cube_2)
    return
      encode(
        clip( c,
              LineStringZ(75042.7273594 5094865.55794 "2008-01-01T02:01:20.000Z", 705042.727359 5454865.55794 "2008-01-08T00:02:58.000Z")
        ),
        "text/csv"
      )

- Linestring clipping on 2D coverage with axes ``X``, ``Y``.

  .. hidden-code-block:: text

    for c in (test_mean_summer_airtemp)
    return
      encode(
        clip( c, LineString(-29.3822 120.2783, -19.5184 144.4043) ),
        "text/csv"
      )

   In this case the geo coordinates of the values on the linestring will be
   included as well in the result. The first band of the result will hold the
   X coordinate, the second band the Y coordinate, and the remaining bands the
   original cell values. Example output for the above query: ::

   .. code-block:: text

    "-28.975 119.975 90","-28.975 120.475 84","-28.475 120.975 80", ...

-  Multipolygon clipping on 2D coverage.

   .. hidden-code-block:: text

    for c in (test_mean_summer_airtemp)
    return
      encode(
        clip( c, Multipolygon(
                   (( -20.4270 131.6931, -28.4204 124.1895,
                      -27.9944 139.4604, -26.3919 129.0015 )),
                   (( -20.4270 131.6931, -19.9527 142.4268,
                      -27.9944 139.4604, -21.8819 140.5151 ))
                 )
        ),
        "image/png"
      )

- Curtain clipping by a Linestring on 3D coverage

  .. hidden-code-block:: text

    for c in (test_eobstest)
    return
      encode(
        clip( c, CURTAIN(projection(Lat, Long), linestring(25 40, 30 40, 30 45, 30 42) ) ),
        "text/csv"
      )

- Curtain clipping by a Polygon on 3D coverage

  .. hidden-code-block:: text

    for c in (test_eobstest)
    return
      encode(
        clip( c, CURTAIN(projection(Lat, Long), Polygon((25 40, 30 40, 30 45, 30 42)) ) ),
        "text/csv"
      )


- Corridor clipping by a Linestring on 3D coverage

  .. hidden-code-block:: text

    for c in (test_irr_cube_2)
    return
      encode(
        clip( c,
              corridor(
                projection(E, N),
                LineString( 75042.7273594  5094865.55794 "2008-01-01T02:01:20.000Z",
                            75042.7273594 5194865.55794 "2008-01-01T02:01:20.000Z" ),
                Linestring( 75042.7273594 5094865.55794, 75042.7273594 5094865.55794,
                            85042.7273594 5194865.55794, 95042.7273594 5194865.55794 )
              )
        ),
        "application/gml+xml"
      )

- Corridor clipping by a Polygon on 3D coverage (geo CRS: ``EPSG:4326``)
  with input geo coordinates in ``EPSG:3857``. 

  .. hidden-code-block:: text

    for c in (test_eobstest)
    return
      encode(
        clip( c,
              corridor(
                projection(Lat, Long),
                LineString(4566099.12252 2999080.94347 "1950-01-01",
                           4566099.12252 3248973.78965 "1950-01-02"),
                Polygon((4452779.63173 2875744.62435, 4452779.63173 3503549.8435,
                         5009377.0857 3503549.8435, 5009377.0857 2875744.62435) )
              ),
              "http://localhost:8080/def/crs/EPSG/0/3857"
        ),
        "application/gml+xml"
      )

Auto-ratio for scaling X or Y axis in WCPS
------------------------------------------

As aproprietary extension, the ``scale()`` function in WCPS allows to specify
the target extent of only one of the spatial horizontal axes, instead of both.
In this case, the extent of the other axis will be determined automatically
while preserving the original ratio between the two spatial axes.

For example in the request below, petascope will automatically set
the extent  of ``Lat`` to a value that preserves the ratio in the output result: 

.. hidden-code-block:: text

   for $c in (test_mean_summer_airtemp)
   return
     encode( scale( $c, { Long:"CRS:1"(0:160) } ), "image/png" )


Automatic domain extraction
---------------------------

The domain interval can be extracted from a domain, including an 
``imageCrsDomain`` (in modern nomenclature: index domain).
Both the interval - ie: ``[lowerBound:upperBound]`` - and lower as well 
as upper bound can be retrieved for each axis.

Syntax: ::

   operator(.lo|.hi)?

with ``.lo`` or ``.hi`` returning the lower bound or upper bound of this interval.

.. code-block:: text

   Coverage test_eobstest has 3 dimensions with extent (0:5,0:29,0:39).
   Expression imageCrsdomain(c,Long) in this case returns 0:39
   whereas imageCrsdomain(c,Long).hi returns 39.

Further, the third argument of the ``domain()`` operator, the CRS URL,
is now optional. If not specified, ``domain()`` will use the CRS of the
selected axis (ie, the second argument) instead.


LET clause in WCPS
------------------

An optional ``LET`` clause is supported in WCPS queries.
It allows binding alias variables to valid WCPS query sub-expressions,
and subsequently make use of the variables in the ``return`` clause
instead of repeating the aliased sub-expressions.

The syntax is ::

   FOR-CLAUSE
   LET $variable := assignment [ , $variable := assignment ]
       ...
   [ WHERE-CLAUSE ]
   RETURN-CLAUSE

where ::

   assignment ::= coverageExpression | domainExpression


.. code-block:: text

  for $c in (test_mr) 
  let $a := $c[i(0:50), j(0:40)],  
      $b := avg($c) * 2 
  return
    encode( scale( $c, { imageCrsDomain( $c ) } ) + $b, "image/png" )

A special shorthand subset expression allows to conveniently specify domains.
The variable in the LET clause follows this syntax: ::
  
  LET $variable := [ dimensionalIntervalList ]

This can readily be used in a subset expression: ::

  coverageVariable[$variable1]

.. code-block:: text

  for $c in (test_mr) 
  let $a := [i(20), j(40)], 
      $b := 10 
  return
    encode( $c[ $a ] + $b, "itext/json" )


Case Distinction
----------------

As another proprietary extension, conditional evaluation is added to WCPS
following the overall XQuery-oriented syntax.

Syntax
^^^^^^

.. code-block:: text

  SWITCH
    CASE condExp RETURN resultExp
    [ CASE condExp RETURN resultExp ]*
    DEFAULT RETURN resultExpDefault

where ``condExp`` and ``resultExp`` are either scalar-valued or
coverage-valued expressions.

Constraints
^^^^^^^^^^^

- All condition expressions must return either boolean values or boolean coverages
- All result expressions must return either scalar values, or coverages
- The domain of all condition expressions must be the same
- The domain of all result expressions must be the same (that means same extent, 
  resolution/direct positions, crs)

Evaluation Rules
^^^^^^^^^^^^^^^^

If the result expressions return scalar values, the returned scalar value on a
branch is used in places where the condition expression on that branch evaluates
to ``True``. If the result expressions return coverages, the values of the returned
coverage on a branch are copied in the result coverage in all places where the
condition coverage on that branch contains pixels with value ``True``.   

The conditions of the statement are evaluated in a manner similar to the
IF-THEN-ELSE statement in programming languages such as Java or C++. This
implies that the conditions must be specified by order of generality, starting
with the least general and ending with the default result, which is the most
general one. A less general condition specified after a more general condition
will be ignored, as the expression meeting the less general expression will have
had already met the more general condition.

Furthermore, the following hold:

- ``domainSet(result)`` = ``domainSet(condExp1)``
- ``metadata(result)`` = ``metadata(condExp1)``
- ``rangeType(result)`` = ``rangeType(resultExp1)``. In case resultExp1
  is a scalar, the result range type is the range type describing the
  coverage containing the single pixel resultExp1. 

Examples
^^^^^^^^

.. code-block:: text

  switch
    case $c < 10 return {red: 0;   green: 0;   blue: 255}
    case $c < 20 return {red: 0;   green: 255; blue:   0}
    case $c < 30 return {red: 255; green: 0;   blue:   0}
    default      return {red: 0;   green: 0;   blue:   0}

The above example assigns blue to all pixels in the $c coverage having a value
less than 10, green to the ones having values at least equal to 10, but less
than 20, red to the ones having values at least equal to 20 but less than 30 and
black to all other pixels.

.. code-block:: text

  switch
    case $c > 0 return log($c)
    default     return 0

The above example computes log of all positive values in $c, and assigns 0 to
the remaining ones.

.. code-block:: text

  switch
    case $c < 10 return $c * {red: 0;   green: 0;   blue: 255}
    case $c < 20 return $c * {red: 0;   green: 255; blue: 0}
    case $c < 30 return $c * {red: 255; green: 0;   blue: 0}
    default      return      {red: 0;   green: 0;   blue: 0}

The above example assigns *blue: 255* multiplied by the original pixel value to all
pixels in the $c coverage having a value less than 10, *green: 255* multiplied by
the original pixel value to the ones having values at least equal to 10, but
less than 20, *red: 255* multiplied by the original pixel value to the ones having
values at least equal to 20 but less than 30 and black to all other pixels.


CIS 1.0 to CIS 1.1 Transformation
---------------------------------

For output format ``application/gml+xml`` WCPS supports delivery
as CIS 1.1 ``outputType=GeneralGridCoverage`` by specifying an additional
proprietary parameter ``outputType`` in the ``encode()`` function.

.. code-block:: text

    for c in (test_irr_cube_2)
    return encode( c, "application/gml+xml", 
                      "{\"outputType\":\"GeneralGridCoverage\"}" ) 


Web Map Service (WMS)
=====================

The OGC Web Map Service (WMS) standard provides a simple HTTP interface
for requesting overlays of geo-registered map images, ready for display.

With petascope, geo data can be served simultaneously via WMS, WCS,
and WCPS. Further information:

- :ref:`How to publish a WMS layer via WCST\_Import <wms-import>`.
- :ref:`Add WMS style queries to existing layers <style-creation>`.

WMS GetMap: Special Functionality
---------------------------------

Transparency
^^^^^^^^^^^^

By adding a parameter ``transparent=true`` to WMS requests the returned image
will have ``NoData Value=0`` in the metadata indicating to the client 
that all pixels with value *0* value should be considered transparent for PNG
encoding format.

.. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WMS&VERSION=1.3.0
        &REQUEST=GetMap&LAYERS=waxlake1
        &BBOX=618887,3228196,690885,3300195.0
        &CRS=EPSG:32615&WIDTH=600&HEIGHT=600&FORMAT=image/png
        &TRANSPARENT=true

.. _wms-interpolation:

Interpolation
^^^^^^^^^^^^^

If in a ``GetMap`` request the output CRS requested is different from
the coverage's Native CRS petascope will duly reproject the map applying
resampling and interpolation. The algorithm used can be controlled with
the proprietary ``GetMap`` parameter ``interpolation={method}``;
default is nearest-neighbour interpolation.
See :ref:`sec-geo-projection` for the methods available and their meaning.

.. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WMS
        &VERSION=1.3.0
        &REQUEST=GetMap
        &LAYERS=test_wms_3857
        &BBOX=-44.525,111.976,-8.978,156.274
        &CRS=EPSG:4326
        &WIDTH=60&HEIGHT=60
        &FORMAT=image/png
        &INTERPOLATION=bilinear

3D+ Coverages as WMS Layers
---------------------------

Petascope allows to import a 3D+ coverage as a WMS layer.
To this end, the ingrdients file used for ``wcst_import``
must contain ``wms_import": true``. This works for 3D+ coverages
with recipes *regular_time_series*, *irregular_time_series*, and *general_coverage* recipes.
`This example <http://rasdaman.org/browser/systemtest/testcases_services/test_all_wcst_import/testdata/wms_3d_time_series_irregular/ingest.template.json>`_
demonstrates how to define an *irregular_time_series* 3D coverage from 2D GeoTIFF files.

Once the coverage is ingested, a ``GetMap`` request
can use the additional (non-horizontal) axes for subsetting according
to the OGC WMS 1.3.0 standard.

.. TABLE:: WMS Subset Parameters for Different Axis Types

    +------------------++-------------------------------------------------+
    |Axis Type         |Subset parameter                                  |
    +==================+==================================================+
    |Time              |time=...                                          |
    +------------------+--------------------------------------------------+
    |Elevation         |elevation=...                                     |
    +------------------+--------------------------------------------------+
    |Other             |dim_AxisName=... (e.g dim_pressure=...)           |
    +------------------+--------------------------------------------------+


According to the WMS 1.3.0 specification, the subset
for non-geo-referenced axes can have these formats:

- Specific value (*value1*): time='2012-01-01T00:01:20Z, dim_pressure=20,...

- Range values (*min/max*): time='2012-01-01T00:01:20Z'/'2013-01-01T00:01:20Z,
  dim_pressure=20/30,...

- Multiple values (*value1,value2,value3,...*): time='2012-01-01T00:01:20Z,
  '2013-01-01T00:01:20Z, dim_pressure=20,30,60,100,...

- Multiple range values (*min1/max1,min2/max2,...*):
  dim_pressure=20/30,40/60,...

.. NOTE::

   A ``GetMap`` request always returns a 2D result. If a non-geo-referenced axis
   is omitted from the request it will be considered as a slice
   on the upper bound along this axis. For example, in a time-series 
   the youngest timeslice will be delivered).

  Examples:

  - Multiple values on `time axis of 3D coverage <http://rasdaman.org/browser/systemtest/testcases_services/test_wms/queries/29-get_map_on_3d_time_series_irregular_time_specified.test>`_.
  - Multiple values on `time, dim_pressure axes of 4d coverage <http://rasdaman.org/browser/systemtest/testcases_services/test_wms/queries/31-get_map_on_4d_coverage_dim_pressure_and_time_irregular_specified.test>`_.

WMS Layer Management
--------------------

Additional proprietary requests, beyond the WMS standard, allow for service maintenance.

Layers can be easily created from existing coverages in WCS in two ways:

- By specifying WMS setup during import coverage in the respective
  ingredients file; see :ref:`wms_import <wms-import>`;
- By sending an :ref:`InsertWCSLayer <insert-wcs-layer>` HTTP request
  to petascope.

The following proprietary WMS request types serve to manage the WMS offering
of rasdaman:

.. _insert-wcs-layer:

- ``InsertWCSLayer``: create a new WMS layer from an existing coverage.

.. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WMS&VERSION=1.3.0
           &REQUEST=InsertWCSLayer
           &WCSCOVERAGEID=MyCoverage

- ``UpdateWCSLayer``: update an existing WMS layer from an existing coverage
  which associates with this WMS layer.

.. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WMS&VERSION=1.3.0
            &REQUEST=UpdateWCSLayer
            &WCSCOVERAGEID=MyCoverage

- To remove a layer by :ref:`removing the associated WCS coverage <delete-coverage>`.

.. _style-creation:

WMS Style Management
--------------------

Styles can be created for layers using rasql and WCPS query fragments. This
allows users to define several visualization options for the same dataset in a
flexible way. Examples of such options would be color classification, NDVI
detection etc. The following HTTP request will create a style with the name,
abstract and layer provided in the KVP parameters below

.. NOTE::

    For Tomcat version 7+ it requires the query (WCPS/rasql fragment)
    to be URL-encoded correctly. `This site <http://meyerweb.com/eric/tools/dencoder/>`_ 
    offers such an encoding service.


Style Definition Variants
^^^^^^^^^^^^^^^^^^^^^^^^^

-  WCPS query fragment example (since rasdaman 9.5):

   .. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WMS&VERSION=1.3.0
        &REQUEST=InsertStyle
        &NAME=wcpsQueryFragment
        &LAYER=test_wms_4326
        &ABSTRACT=This style marks the areas where fires are in progress with the color red
        &WCPSQUERYFRAGMENT=switch case $c > 1000 return {red: 107; green:17; blue:68} default return {red: 150; green:103; blue:14})

   Variable ``$c`` will be replaced by a layer name when sending a ``GetMap``
   request containing this layer's style.

-  Rasql query fragment examples:

   .. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WMS&version=1.3.0&REQUEST=InsertStyle
        &NAME=FireMarkup
        &LAYER=dessert_area
        &ABSTRACT=This style marks the areas where fires are in progress with the color red
        &RASQLTRANSFORMFRAGMENT=case $Iterator when ($Iterator + 2) > 200 then {255, 0, 0} else {0, 255, 0} end

   Variable ``$Iterator`` will be replaced with the actual name of the rasdaman
   collection and the whole fragment will be integrated inside the regular
   ``GetMap`` request.

-  Multiple layers can be used in a style definition. 
   Besides the iterators ``$c`` in WCPS query fragments and ``$Iterator`` in rasql
   query fragments, which always refer to the current layer, other layers
   can be referenced by name using an iterator of the form ``$LAYER_NAME`` in the style expression. 
  
   Example: create a WCPS query fragment style referencing 2 layers
   (``$c`` refers to layer *sentinel2_B4* which defines the style):

   .. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WMS&VERSION=1.3.0
        &REQUEST=InsertStyle
        &NAME=BandsCombined
        &LAYER=sentinel2_B4
        &ABSTRACT=This style needs 2 layers
        &WCPSQUERYFRAGMENT=$c + $sentinel2_B8

   Then, in any ``GetMap`` request using this style
   the result will be obtained from the combination of the 2 layers
   *sentinel2_B4* and *sentinel2_B8*:

   .. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERViCE=WMS&VERSION=1.3.0
        &REQUEST=GetMap
        &LAYERS=sentinel2_B4
        &BBOX=-44.975,111.975,-8.975,155.975&CRS=EPSG:4326
        &WIDTH=800&HEIGHT=600
        &FORMAT=image/png&transparent=true
        &STYLES=BandsCombined

-  WMS styling supports a ``ColorTable`` definition which
   allows to colorize the result of WMS GetMap request when the style is requested.
   A style can contain either one or both **query fragment** and **Color Table** definitions.
   The ``InsertStyle`` request supports two new **non-standard** 
   extra parameters ``colorTableType`` (valid values: ``ColorMap``, ``GDAL`` and ``SLD``)
   and ``colorTableDefintion`` containing corresponding definition, example:

   .. hidden-code-block:: text

    http://localhost:8080/rasdaman/ows?SERVICE=WMS&VERSION=1.3.0
        &REQUEST=InsertStyle
        &NAME=test
        &LAYER=test_wms_4326
        &ABSTRACT=This style marks the areas where fires are in progress with the color red
        &WCPSQUERYFRAGMENT=switch case $c > 1000 return {red: 107; green:17; blue:68} default return {red: 150; green:103; blue:14})
        &COLORTABLETYPE=ColorMap
        &COLORTABLEDEFINITION={"type": "intervals", "colorTable": {  "0": [0, 0, 255, 0], "100": [125, 125, 125, 255], "255": [255, 0, 0, 255] } }

   Below the supported color table definitions for each color table type are explained:

    * Rasdaman ``ColorMap``: check :ref:`coloring-arrays` for more details.
      The color table definition must be a JSON object, for example:

      .. hidden-code-block:: json

        { 
          "type": "intervals",  
          "colorTable": {  "0":   [0,     0, 255,   0],  
                           "100": [125, 125, 125, 255],  
                           "255": [255,   0,   0, 255]  
                        } 
        }

    * GDAL ``ColorPalette``: check :ref:`encode` for more details.
      The color table definition must be a JSON object and contains **256 color arrays**
      in ``colorTable`` array, example:

      .. hidden-code-block:: json

        {
           "colorTable": [
                          [255,  0,  0,255],
                          [216, 31, 30,255],
                          [216, 31, 30,255],
                          ...,
                          [ 43,131,186,255]
                         ]
        }

    * WMS ``Styled Layer Descriptor (SLD)``: The color table definition must be valid XML
      and contains ``ColorMap`` element. Check :ref:`coloring-arrays` for details about the supported types
      (``ramp`` (default), ``values``, ``intervals``), example ``ColorMap`` with ``type="values"``: 

      .. hidden-code-block:: xml

        <?xml version="1.0" encoding="UTF-8"?>
        <StyledLayerDescriptor xmlns="http://www.opengis.net/sld"
                               xmlns:gml="http://www.opengis.net/gml" 
                               xmlns:sld="http://www.opengis.net/sld"
                               xmlns:ogc="http://www.opengis.net/ogc"
                               version="1.0.0">
          <UserLayer>
            <sld:LayerFeatureConstraints>
              <sld:FeatureTypeConstraint/>
            </sld:LayerFeatureConstraints>
            <sld:UserStyle>
              <sld:Name>sqi_fig5_crop1</sld:Name>
              <sld:FeatureTypeStyle>
                <sld:Rule>
                  <sld:RasterSymbolizer>
                    </sld:ChannelSelection>
                    <sld:ColorMap type="values">
                       <ColorMapEntry color="#0000FF" quantity="150" />
                       <ColorMapEntry color="#FFFF00" quantity="200" />
                       <ColorMapEntry color="#FF0000" quantity="250" />
                    </sld:ColorMap>
                  </sld:RasterSymbolizer>
                </sld:Rule>
              </sld:FeatureTypeStyle>
            </sld:UserStyle>
          </UserLayer>
        </StyledLayerDescriptor>

WMS Style Removal
^^^^^^^^^^^^^^^^^

The proprietary ``DeleteStyle`` WMS request type allows to remove
a particular style of an existing WMS layer. ::

    http://localhost:8080/rasdaman/ows?SERVICE=WMS&VERSION=1.3.0
        &REQUEST=DeleteStyle
        &LAYER=dessert_area
        &STYLE=FireMarkup


Testing a WMS Setup
-------------------

A rasdaman WMS service can be tested with any conformant client through
a ``GetMap`` request like the following:

.. hidden-code-block:: text

    http://example.org/rasdaman/ows?service=WMS&version=1.3.0&request=GetMap
        &layers=MyLayer
        &bbox=618885.0,3228195.0,690885.0,3300195.0
        &crs=EPSG:32615
        &width=600
        &height=600
        &format=image/png


Errors and Workarounds
----------------------

**Cannot load new WMS layer in QGIS**
    In this case, the problem is due to QGIS caching the WMS GetCapabilities from the last
    request so the new layer does not exist (see
    `clear caching solution <http://osgeo-org.1560.x6.nabble.com/WMS-provider-Cannot-calculate-extent-td5250516.html>`_).

.. _wcs-t-non-standard-requests-wms:

WMS Pyramid Management
----------------------

The following proprietary WMS requests are used to create and delete
downscaled coverages. Internally they are used for efficient zooming in/out
in WMS, and downscaling when using the ``scale()`` function in WCPS
or scaling extension in WCS.

* ``InsertScaleLevel``: create a downscaled collection for a specific coverage
  and given level; e.g. to create a downscaled coverage
  of *test_world_map_scale_levels* that is *4x smaller*:

  .. hidden-code-block:: text

    http://localhost:8082/rasdaman/ows?service=WCS&version=2.0.1
    &request=InsertScaleLevel
    &coverageId=test_world_map_scale_levels
    &level=4

* ``DeleteScaleLevel``: delete an existing downscaled coverage
  at a given level; e.g. to delete the downscaled level 4 of coverage
  *test_world_map_scale_levels*:

  .. hidden-code-block:: text

    http://localhost:8082/rasdaman/ows?service=WCS&version=2.0.1
    &request=DeleteScaleLevel
    &coverageId=test_world_map_scale_levels
    &level=4

``wcst_import`` can send ``InsertScaleLevel`` requests automatically 
when importing data with it with ``scale_levels`` option
in the ingredients file, more details :ref:`here <data-import-intro>`.


.. _data-import:

Data Import
===========

Raster data in a variety of formats, such as TIFF, netCDF, GRIB, etc.
can be imported in petascope through the ``wcst_import.sh`` utility.
Internally it is based on ``WCS-T`` requests, but hides the complexity and
maintains the geo-related metadata in its so-called ``petascopedb``
while the raster data get ingested into the rasdaman array store.

Building large *timeseries/datacubes*, *mosaics*, etc. and keeping them up-to-date
as new data arrive available is supported for a large variety of data formats and
file/directory organizations.

The systemtest contains `many examples <http://rasdaman.org/browser/systemtest/testcases_services/test_all_wcst_import/testdata>`__
for importing different types of data.

.. _data-import-intro:

Introduction
------------

The ``wcst_import.sh`` tool introduces two concepts:

- **Recipe** - A recipe is a class implementing the *BaseRecipe* that based on a set of 
  parameters (*ingredients*) can import a set of files into WCS forming a well 
  defined coverage (mosaic, regular timeseries, irregular timeseries, etc);

- **Ingredients** - An *ingredients* file is a JSON file containing a set of parameters 
  that define how the recipe should behave (e.g. the WCS endpoint, the coverage name, etc.)

To execute an ingredients file in order to import some data: ::

    $ wcst_import.sh path/to/my_ingredients.json

Alternatively, ``wcst_import.sh`` tool can be started as a daemon as follows: ::

    $ wcst_import.sh path/to/my_ingredients.json --daemon start

or as a daemon that is "watching" for new data at some interval (in seconds): ::

    $ wcst_import.sh path/to/my_ingredients.json --watch <interval>

For further informations regarding ``wcst_import.sh`` commands and usage: ::

    $ wcst_import.sh --help

The workflow behind is depicted approximately on :numref:`wcst_import_workflow`.

.. _wcst_import_workflow:

.. figure:: media/geo-services-guide/wcst_import.png
   :align: center
   :scale: 40%

   Ingestion process with `wcst_import.sh`

An ingredients file showing all options possible can be found `here
<http://rasdaman.org/browser/applications/wcst_import/ingredients/possible_ingredients.json>`_;
in the `same directory <http://rasdaman.org/browser/applications/wcst_import/ingredients>`_
there are several examples of different recipes.

.. _data-import-recipes:

Recipes
-------

The following recipes are provided in the rasdaman repository:

* :ref:`Mosaic map <data-import-recipe-mosaic-map>`
* :ref:`Regular timeseries <data-import-recipe-regular-timeseries>`
* :ref:`Irregular timeseries <data-import-recipe-irregular-timeseries>`
* :ref:`General coverage <data-import-recipe-general-coverage>`
* :ref:`Import from external WCS <data-import-recipe-wcs_extract>`
* Specialized recipes
    - :ref:`Sentinel 1 <data-import-recipe-sentinel1>`
    - :ref:`Sentinel 2 <data-import-recipe-sentinel2>`

For each one of these there is an ingredients example under the
`ingredients/ <http://rasdaman.org/browser/applications/wcst_import/ingredients>`_
directory, together with an example for the available parameters
Further on each recipe type is described in turn.

.. _data-import-common-options:

Common Options
^^^^^^^^^^^^^^

Some options are commonly applicable to all recipes.

**config section**

* ``service_url`` - The endpoint of the WCS service with the WCS-T extension enabled

  .. code-block::json

      "service_url": "http://localhost:8080/rasdaman/ows"

* ``mock`` - Print WCS-T requests but do not execute anything if set to ``true``.
  Set to ``false`` by default.

* ``automated`` - Set to ``true`` to avoid any interaction during the ingestion
  process. Useful in production environments for automated deployment for example.
  By default it is ``false``, i.e. user confirmation is needed to execute the
  ingestion.
* ``blocking`` (since v9.8) - Set to ``false`` to analyze and import each file
  separately (**non-blocking mode**). By default blocking is set to ``true``,
  i.e. wcst_import will analyze all input files first to create corresponding
  coverage descriptions, and only then import them. The advantage of non-blocking
  mode is that the analyzing and importing happens incrementally
  (in blocking mode the analyzing step can take a long time, e.g. days,
  before the import can even begin).

  .. note::

        When importing in **non-blocking** import mode for coverages with irregular axes,
        it will *only rely on sorted files by filenames* and it can fail if these axes' coefficients
        are collected from input files' metadata (e.g: DateTime value in TIFF's tag or GRIB metadata)
        as they might not be consecutive. wcst_import will not analyze all files
        to collect metadata to be sorted by DateTime as in default **blocking** import mode.


* ``default_null_values`` - This parameter adds default null values for bands that
  do *not* have a null value provided by the file itself. The value for this
  parameter should be an array containing the desired null value either as a
  closed interval ``low:high`` or single values. E.g. for a coverage with 3 bands

  .. hidden-code-block:: json

      "default_null_values": [ "9995:9999", "-9, -10, -87", 3.14 ],

  Note, if set this parameter will override the null/nodata values present in
  the input files.

* ``tmp_directory`` - Temporary directory in which gml and data files are created;
  should be readable and writable by rasdaman, petascope and current user. By
  default this is ``/tmp``.

* ``crs_resolver`` - The crs resolver to use for generating WCS-T request. By
  default it is determined from the ``petascope.properties`` setting.

* ``url_root`` - In case the files are exposed via a web-server and not locally,
  you can specify the root file url here; the default value is ``"file://"``.

* ``skip`` - Set to ``true`` to ignore files that failed to import; by default it
  is ``false``, i.e. the ingestion is terminated when a file fails to import.

* ``retry`` - Set to ``true`` to retry a failed request. The number of retries is
  either 5, or the value of setting ``retries`` if specified. This is set to
  ``false`` by default.

* ``retries`` - Control how many times to retry a failed WCS-T request; set to 5
  by default.

* ``retry_sleep`` - Set number of seconds to wait before retrying after an error;
  a floating-point number can also be specified for sub-second precision.
  Default values is 1.

* ``track_files`` - Set to ``true`` to allow files to be tracked in order to avoid
  reimporting already imported files. This setting is enabled by default.

* ``resumer_dir_path`` - The directory in which to store the track file. By
  default it will be stored next to the ingredients file.

* ``slice_restriction`` - Limit the slices that are imported to the ones that fit
  in a specified bounding box. Each subset in the bounding box should be of form
  ``{ "low": 0, "high": <max> }``, where low/high are given in the axis format.
  Example:

  .. hidden-code-block:: json

      "slice_restriction": [
        { "low": 0, "high": 36000 },
        { "low": 0, "high": 18000 },
        { "low": "2012-02-09", "high": "2012-12-09", "type": "date" }
      ]

* ``description_max_no_slices`` - maximum number of slices (files) to show for
  preview before starting the actual ingestion.

* ``subset_correction`` (*deprecated* since rasdaman v9.6) - In some cases the
  resolution is small enough to affect the precision of the transformation from
  domain coordinates to grid coordinates. To allow for corrections that will
  make the import possible, set this parameter to ``true``.


**recipes/options section**

* ``import_order`` - Allow to sort the input files (``ascending`` (default)
  or ``descending``).Currently, it sorts by *datetime* which allows
  to import coverage from the first date or the recent date. Example:

  .. hidden-code-block:: json

      "import_order": "descending"

* ``tiling`` - Specifies the tile structure to be created for the coverage
  in rasdaman. You can set arbitrary tile sizes for the tiling option only
  if the tile name is ``ALIGNED``. Example:

  .. hidden-code-block:: json

      "tiling": "ALIGNED [0:0, 0:1023, 0:1023] TILE SIZE 5000000"

  For more information on tiling please check the :ref:`storage-layout`

.. _wms-import:

* ``wms_import`` - If set to ``true``, after importing data to coverage,
  it will also create a WMS layer from the imported coverage and populate
  metadata for this layer. After that, this layer will be available from
  *WMS GetCapabilties request*. Example:

  .. hidden-code-block:: json

      "wms_import": true

.. _scale-levels:

* ``scale_levels`` - Enable the :ref:`WMS pyramids <wms-image-pyramids>` feature.
  Level must be positive number and greater than 1.
  Syntax:

  .. hidden-code-block:: json

      "scale_levels": [ 1.5, 2, 4, ... ]

**hooks section**

Since v9.8, wcst_import allows to run bash commands *before/after ingestion*
by adding optional ``hooks`` configuration in an ingredient file.
There are 2 types of ingestion hooks:

* ``before_ingestion``: run bash commands before analyzing input file(s)
  (e.g: using **gdalwarp** to reproject input file(s) from EPSG:3857 CRS to
  EPSG:4326 CRS and import *projected EPSG:4326* input file(s)) to a coverage.

* ``after_ingestion``: run bash commands after importing input file(s)
  to coverage (e.g: clean all projected file(s) from **gdalwarp** command above).

When importing mode is set to non-blocking (``"blocking": false``),
wcst_import will run before/after hook(s) for the file which
is being used to update coverage, while the default blocking importing mode
will run before/after hook(s) for *all input files* before/after
they are updated to a coverage. Parameters are explained below.

.. hidden-code-block:: json

  "hooks": [
      {
        // Describe what this ingestion hook does
        "description": "reproject input files.",

        // Run bash command before importing file(s) to coverage
        "when": "before_ingestion",

        // Bash command which should be run for each input file
        "cmd": "gdalwarp -t_srs EPSG:4326 -tr 0.02 0.02 -overwrite \"${file:path}\" \"${file:path}.projected\"",

        // If set to *true*, when a bash command line returns any error, wcst_import terminates immediately.
        // **NOTE:** only valid for ``before`` hook.
        "abort_on_error": true,

        // wcst_import will consider the specified path(s) as the actual file(s)
        // to be ingested after running the hook, rather than the original file.
        // This is an array of paths where globbing is allowed (same as the "input":"paths" option).
        // **NOTE:** only valid for ``before`` hook.
        "replace_path": ["${file:path}.projected"]
      },

      {
        // Describe what this ingestion hook does
        "description": "Remove projected files.",

        // Run bash command after importing file(s) to coverage
        "when": "after_ingestion",

        // Bash command which should be run for each imported file(s)
        "cmd": "rm -rf \"${file:path}.projected\""
      },

      // more ``before`` and ``after`` hooks if needed
      ...
  ]


*Example: Import GDAL subdatasets*

The example ingredients below contains a pre-hook which replaces the collected
file path into a GDAL subdataset form; in this particular case, with the GDAL
driver for NetCDF a single variable from the collected NetCDF files is imported.

.. hidden-code-block:: json

  "slicer": {
          "type": "gdal",
          ...
  },
  "hooks": [
      {
        "description": "Demonstrate import 1 variable for netCDF with subdataset",
        "when": "before_ingestion",
        "cmd": "",
        "abort_on_error": true,
        // GDAL netCDF subdataset variable file path
        "replace_path": ["NETCDF:${file:path}:area"]
      }
   ]


.. _data-import-recipe-mosaic-map:

Mosaic map
^^^^^^^^^^

Well suited for importing a tiled map, not necessarily continuous; it
will place all input files given under a single coverage and deal with
their position in space. Parameters are explained below.

.. hidden-code-block:: json

    {
      "config": {
        // The endpoint of the WCS service with the WCS-T extension enabled
        "service_url": "http://localhost:8080/rasdaman/ows",
        // If set to true, it will print the WCS-T requests and will not
        // execute them. To actually execute them set it to false.
        "mock": true,
        // If set to true, the process will not require any user confirmation.
        // This is useful for production environments when deployment is automated.
        "automated": false
      },
      "input": {
        // The name of the coverage; if the coverage already exists,
        // it will be updated with the new files
        "coverage_id": "MyCoverage",
        // Absolute or relative (to the ingredients file) path or regex that
        // would work with the ls command. Multiple paths separated by commas
        // can be specified.
        "paths": [ "/var/data/*" ]
      },
      "recipe": {
        // The name of the recipe
        "name": "map_mosaic",
        "options": {
          // The tiling to be applied in rasdaman
          "tiling": "ALIGNED [0:511, 0:511]"
        }
      }
    }


.. _data-import-recipe-regular-timeseries:

Regular timeseries
^^^^^^^^^^^^^^^^^^

Well suited for importing multiple 2-D slices created at regular
intervals of time (e.g sensor data, satelite imagery etc) as 3-D cube
with the third axis being a temporal one. Parameters are explained below

.. hidden-code-block:: json

    {
      "config": {
        // The endpoint of the WCS service with the WCS-T extension enabled
        "service_url": "http://localhost:8080/rasdaman/ows",
        // If set to true, it will print the WCS-T requests and will not
        // execute them. To actually execute them set it to false.
        "mock": true,
        // If set to true, the process will not require any user confirmation.
        // This is useful for production environments when deployment is automated.
        "automated": false
      },
      "input": {
        // The name of the coverage; if the coverage already exists,
        // it will be updated with the new files
        "coverage_id": "MyCoverage",
        // Absolute or relative (to the ingredients file) path or regex that
        // would work with the ls command. Multiple paths separated by commas
        // can be specified.
        "paths": [ "/var/data/*" ]
      },
      "recipe": {
        // The name of the recipe
        "name": "time_series_regular",
        "options": {
          // Starting date for the first spatial slice
          "time_start": "2012-12-02T20:12:02",
          // Format of the time provided above: `auto` to try to guess it,
          // otherwise use any combination of YYYY:MM:DD HH:mm:ss
          "time_format": "auto",
          // Distance between each slice in time, granularity seconds to days
          "time_step": "2 days 10 minutes 3 seconds",

          // CRS to be used for the time axis
          "time_crs": "http://localhost:8080/def/crs/OGC/0/AnsiDate",
          // The tiling to be applied in rasdaman
          "tiling": "ALIGNED [0:1000, 0:1000, 0:2]"
        }
      }
    }


.. _data-import-recipe-irregular-timeseries:

Irregular timeseries
^^^^^^^^^^^^^^^^^^^^

Well suited for importing multiple 2-D slices created at irregular intervals of
time into a 3-D cube with the third axis being a temporal one. There are two
types of time parameters in "options", one needs to be choosed according to the
particular use case:

- ``tag_name`` with ``TIFFTAG_DATETIME`` inside image's
  metadata (can be checked with gdalinfo filename, not every image has this
  parameters). `Here is an example with the "tag_name" option
  <http://www.rasdaman.org/attachment/wiki/WCSTImportGuide/ingredient_irregulartime_tag_name.txt>`_

- ``filename`` allows an arbitrary pattern to extract the time information
  from the data file paths. `Here is an example with the "filename" option
  <http://www.rasdaman.org/attachment/wiki/WCSTImportGuide/ingredient_irregulartime_filename.txt>`_

.. hidden-code-block:: json

    {
      "config": {
        // The endpoint of the WCS service with the WCS-T extension enabled
        "service_url": "http://localhost:8080/rasdaman/ows",
        // If set to true, it will print the WCS-T requests and will not
        // execute them. To actually execute them set it to false.
        "mock": true,
        // If set to true, the process will not require any user confirmation.
        // This is useful for production environments when deployment is automated.
        "automated": false
      },
      "input": {
        // The name of the coverage; if the coverage already exists,
        // it will be updated with the new files
        "coverage_id": "MyCoverage",
        // Absolute or relative (to the ingredients file) path or regex that
        // would work with the ls command. Multiple paths separated by commas
        // can be specified.
        "paths": [ "/var/data/*" ]
      },
      "recipe": {
        // The name of the recipe
        "name": "time_series_irregular",
        "options": {
          // Information about the time parameter, two options (pick one!)

          // 1. Get the date for the slice from a tag that can be read by GDAL
          "time_parameter": {
            "metadata_tag": {
              // The name of such a tag
              "tag_name": "TIFFTAG_DATETIME"
            },
            // The format of the datetime value in the tag
            "datetime_format": "YYYY:MM:DD HH:mm:ss"
          },

          // 2. Extract the date/time from the file name
          "time_parameter" :{
            "filename": {
              // The regex has to contain groups of tokens, separated by parentheses.
              "regex": "(.*)_(.*)_(.+?)_(.*)",
              // Which regex group to use for retrieving the time value
              "group": "2"
            },
          }

          // CRS to be used for the time axis
          "time_crs": "http://localhost:8080/def/crs/OGC/0/AnsiDate",
          // The tiling to be applied in rasdaman
          "tiling": "ALIGNED [0:1000, 0:1000, 0:2]"
        }
      }
    }


.. _data-import-recipe-general-coverage:

General coverage
^^^^^^^^^^^^^^^^

The general recipe aims to be a highly flexible recipe that can handle any kind
of data files (be it 2D, 3D or n-D) and model them in coverages of any
dimensionality. It does that by allowing users to define their own coverage
models with any number of bands and axes and fill the necesary coverage
information through the so called ingredient sentences inside the ingredients.

Ingredient Sentences
~~~~~~~~~~~~~~~~~~~~

An ingredient *expression* can be of multiple types:

- *Numeric* - e.g. ``2``, ``4.5``

- *Strings* - e.g. ``'Some information'``

- *Functions* - e.g. ``datetime('2012-01-01', 'YYYY-mm-dd')``

- *Expressions* - allows a user to collect information from inside the ingested
  file using a specific driver. An expression is of form
  ``${driverName:driverOperation}`` - e.g. ``${gdal:minX}``,
  ``${netcdf:variable:time:min``. You can find all the possible expressions
  :ref:`here <data-import-possible-expressions>`.

- *Any valid python expression* - You can combine the types below into a python
  expression; this allows you to do mathematical operations, some string parsing
  etc. - e.g. ``${gdal:minX} + 1/2 * ${gdal:resolutionX}`` or
  ``datetime(${netcdf:variable:time:min} * 24 * 3600)``


Parameters
~~~~~~~~~~

Using the ingredient sentences we can define any coverage model directly in the
options of the ingredients file. Each coverage model contains a

* ``CRS`` - the crs of the coverage to be constructed. Either a CRS url e.g.
  http://opengis.net/def/crs/EPSG/0/4326 or
  http://ows.rasdaman.org/def/crs-compound?1=http://ows.rasdaman.org/def/crs/EPSG/0/4326&2=http://ows.rasdaman.org/def/crs/OGC/0/AnsiDate
  or the shorthand notations ``CRS1@CRS2@CRS3``, e.g.
  ``EPSG/0/4326@OGC/0/AnsiDate``

* ``metadata`` - specifies in which format you want the metadata (json or xml).
  It can only contain characters and in petascope the datatype for this field
  is CLOB (Character Large Object). For postgresql (the default DBMS for petascopedb)
  this field is generated by Hibernate as LOB, for which the maximum size is 2GB
  (`source <https://giswiki.hsr.ch/PostgreSQL_-_Binary_Large_Objects>`__).

 * *global* - specifies fields which should be saved (e.g. the licence, the creator
   etc) once for the whole coverage. Example:

   .. hidden-code-block:: json

      "global": {
        "Title": "'Drought code'"
      },

 * *local* - specifies fields which are fetched from each input file
   to be stored in coverage's metadata. Then, when subsetting output coverage,
   only associated *local* metadata will be added to the result. Example:

   .. hidden-code-block:: json

        "local": {
		      "LocalMetadataKey": "${netcdf:metadata:LOCAL_METADATA}"
        }


 * *colorPaletteTable* - specifies the path to a Color Palette Table (.cpt)
   file which can be used internally when encoding coverage to PNG to
   colorize result. Example:

   .. hidden-code-block:: json

        "colorPaletteTable": "PATH/TO/color_palette_table.cpt"

   Since v10, general recipe with slicer ``gdal`` reads ``colorPaletteTable``
   automatically if the first input file (TIFF format with  Color Table
   (RGB with 256 entries)) contains this metadata when ``colorPaletteTable``
   is set to ``auto`` or not specified in the ingredients file. 
   If ``colorPaletteTable`` is set to empty, this metadata is ignored
   when creating coverage's global metadata.


* ``slicer`` - specifies the driver (**netcdf**, **gdal** or **grib**) to use to
  read from the data files and for each axis from the CRS how to obtain the
  bounds and resolution corresponding to each file.

    .. note::
        `"type": "gdal"` is used for TIFF, PNG, and other 2D formats.

An example for the **netCDF format** can be found `here
<http://rasdaman.org/browser/systemtest/testcases_services/test_all_wcst_import/testdata/wcps_irregular_time_nc/ingest.template.json>`_
and for **PNG** `here
<http://rasdaman.org/browser/systemtest/testcases_services/test_all_wcst_import/testdata/wcps_mr/ingest.template.json>`_.
Here's an example ingredient file for *grib* data:

.. hidden-code-block:: json

    "recipe": {
      "name": "general_coverage",
      "options": {
        // Provide the coverage description and the method of building it
        "coverage": {
          // The coverage has 4 axes by combining 3 CRSes (Lat, Long, ansi, ensemble)
          "crs": "EPSG/0/4326@OGC/0/AnsiDate@OGC/0/Index1D?axis-label=\"ensemble\"",

          // specify metadata in json format
          "metadata": {
            "type": "json",

            "global": {
              // We will save the following fields from the input file
              // for the whole coverage
              "MarsType": "'${grib:marsType}'",
              "Experiment": "'${grib:experimentVersionNumber}'"
            },

            // or automatically import metadata, netcdf/gdal only (!)
            "global": "auto"

            "local": {
              // and the following field for each file that will compose the final coverage
              "level": "${grib:level}"
            }
          },

          // specify the "driver" for reading each file
          "slicer": {
            // Use the grib driver, which gives access to grib and file expressions.
            "type": "grib",
            // The pixels in grib are considered to be 0D in the middle of the cell,
            // as opposed to e.g. GeoTiff, which considers pixels to be intervals
            "pixelIsPoint": true,
            // Define the bands to create from the files (1 band in this case)
            "bands": [
              {
                "name": "temp2m",
                "definition": "The temperature at 2 meters.",
                "description": "We measure temperature at 2 meters using sensors and
                                then we process the values using a sophisticated algorithm.",
                "nilReason": "The nil value represents an error in the sensor."
                "uomCode": "${grib:unitsOfFirstFixedSurface}",
                "nilValue": "-99999"
              }
            ],
            "axes": {
              // For each axis specify how to extract the spatio-temporal position
              // of each file that we ingest
              "Latitude": {
                // E.g. to determine at which Latitude the nth file will be positioned,
                // we will evaluate the given expression on the file
                "min": "${grib:latitudeOfLastGridPointInDegrees} +
                        (${grib:jDirectionIncrementInDegrees}
                         if bool(${grib:jScansPositively})
                         else -${grib:jDirectionIncrementInDegrees})",
                "max": "${grib:latitudeOfFirstGridPointInDegrees}",
                "resolution": "${grib:jDirectionIncrementInDegrees}
                               if bool(${grib:jScansPositively})
                               else -${grib:jDirectionIncrementInDegrees}",

                // This optional configuration is added since version 9.8.
                // The crs order specifies the order of the CRS axis in coverage
                // that will be created and allows to change standard abbreviation for axis label
                // from EPSG database to a different name (e.g: "Lat" -> "Latitude").
                "crsOrder": 0
                // The grid order specifies the order of the axis in the raster
                // that will be created
                "gridOrder": 3
              },
              "Long": {
                "min": "${grib:longitudeOfFirstGridPointInDegrees}",
                "max": "${grib:longitudeOfLastGridPointInDegrees} +
                         (-${grib:iDirectionIncrementInDegrees}
                          if bool(${grib:iScansNegatively})
                          else ${grib:iDirectionIncrementInDegrees})",
                "resolution": "-${grib:iDirectionIncrementInDegrees}
                               if bool(${grib:iScansNegatively})
                               else ${grib:iDirectionIncrementInDegrees}",
                "crsOrder": 1
                "gridOrder": 2
              },
              "ansi": {
                "min": "grib_datetime(${grib:dataDate}, ${grib:dataTime})",
                "resolution": "1.0 / 4.0",
                "type": "ansidate",
                "crsOrder": 2,
                "gridOrder": 1,
                // In case and axis does not natively belong to a file (e.g. as time),
                // then this property must set to false; by default it is true otherwise.
                "dataBound": false
              },
              "ensemble": {
                "min": "${grib:localDefinitionNumber}",
                "resolution": 1,
                "crsOrder": 3,
                "gridOrder": 0
              }
            }
          },

          "tiling": "REGULAR [0:0, 0:20, 0:1023, 0:1023]"
      }
    }


.. _data-import-possible-expressions:

Expressions
~~~~~~~~~~~

Each driver allows expressions to extract information from input files.
We will mark with capital letters things that vary in the expression.
E.g. ``${gdal:metadata:YOUR_FIELD}`` means that you can replace
``YOUR_FIELD`` with any valid gdal metadata tag (e.g. a ``TIFFTAG_DATETIME``)

**Netcdf**

Take a look at `this NetCDF example
<http://rasdaman.org/browser/applications/wcst_import/ingredients/general_coverage_netcdf.json>`_
for a general recipe ingredient file that uses many netcdf expressions.

+-----------+-----------------------------------------------------+-------------------------------+
|  **Type** |                **Description**                      |        **Examples**           |
+===========+=====================================================+===============================+
|Metadata   |                                                     |                               |
|information|``${netcdf:metadata:YOUR_METADATA_FIELD}``           |``${netcdf:metadata:title}``   |
+-----------+-----------------------------------------------------+-------------------------------+
|Variable   |``${netcdf:variable:VARIABLE_NAME:MODIFIER}``        |``${netcdf:variable:time:min}``|
|information|where ``VARIABLE_NAME`` can be any variable in the   |``${netcdf:variable:t:units}`` |
|           |file and ``MODIFIER`` can be one of:                 |                               |
|           |first|last|max|min; Any extra modifiers will return  |                               |
|           |the corresponding metadata field on the given        |                               |
|           |variable                                             |                               |
+-----------+-----------------------------------------------------+-------------------------------+
|Dimension  |``${netcdf:dimension:DIMENSION_NAME}``               |``${netcdf:dimension:time}``   |
|information|where ``DIMENSION_NAME`` can be any dimension in the |                               |
|           |file. This will return the value on the selected     |                               |
|           |dimension.                                           |                               |
+-----------+-----------------------------------------------------+-------------------------------+


**GDAL**

For TIFF, PNG, JPEG, and other 2D data formats we use GDAL. Take a look at `this GDAL example
<http://rasdaman.org/browser/applications/wcst_import/ingredients/general_coverage_gdal_3d.json>`_
for a general recipe ingredient file that uses many GDAL expressions.

+-----------+-----------------------------------------------------+-----------------------------+
|  **Type** |                **Description**                      |        **Examples**         |
+===========+=====================================================+=============================+
|Metadata   |                                                     |                             |
|information|``${gdal:metadata:METADATA_FIELD}``                  |${gdal:metadata:TIFFTAG_NAME}|
+-----------+-----------------------------------------------------+-----------------------------+
|Geo Bounds |``${gdal:BOUND_NAME}`` where ``BOUND_NAME`` can be   |``${gdal:minX}``             |
|           |one of the minX|maxX|minY|maxY                       |                             |
+-----------+-----------------------------------------------------+-----------------------------+
|Geo        |``${gdal:RESOLUTION_NAME}`` where ``RESOLUTION_NAME``|``${gdal:resolutionX}``      |
|Resolution |can be one of the resolutionX|resolutionY            |                             |
+-----------+-----------------------------------------------------+-----------------------------+
|Origin     |``${gdal:ORIGIN_NAME}`` where ``ORIGIN_NAME`` can be |``${gdal:originY}``          |
|           |one of the originX|originY                           |                             |
+-----------+-----------------------------------------------------+-----------------------------+

**GRIB**

Take a look at `this GRIB example
<http://rasdaman.org/browser/applications/wcst_import/ingredients/general_coverage_grib.json>`_
for a general recipe ingredient file that uses many grib expressions.

+-----------+------------------------------------------------+------------------------------------------+
|  **Type** |                **Description**                 |               **Examples**               |
+===========+================================================+==========================================+
|GRIB Key   |``${grib:KEY}`` where ``KEY`` can be any of the |``${grib:experimentVersionNumber}``       |
|           |keys contained in the GRIB file                 |                                          |
+-----------+------------------------------------------------+------------------------------------------+

**File**

+-----------------+--------------------------------------------------------------------+----------------+
|  **Type**       |                **Description**                                     |  **Examples**  |
+=================+====================================================================+================+
|File Information |``${file:PROPERTY}`` where property can be one of                   |                |
|                 |path|name|dir_path|original_path|original_dir_path                  |``${file:path}``|
|                 |original_* allows to get the original input file's path/directory   |                |
|                 |(used only when using **pre-hook** with ``replace_path``            |                |
|                 |to replace original input file paths with customized file paths).   |                |
+-----------------+--------------------------------------------------------------------+----------------+

**Special Functions**

A couple of special functions are available to deal with some more
complicated cases:

+----------------------------------+-------------------------------------------------+--------------------------------------------+
| **Function Name**                |             **Description**                     |             **Examples**                   |
+==================================+=================================================+============================================+
|``grib_datetime(date,time)``      |This function helps to deal with the usual grib  |``grib_datetime(${grib:dataDate},           |
|                                  |date and time format. It returns back a datetime |${grib:dataTime})``                         |
|                                  |string in ISO format.                            |                                            |
+----------------------------------+-------------------------------------------------+--------------------------------------------+
|``datetime(date, format)``        |This function helps to deal with strange date    |``datetime("20120101:1200",                 |
|                                  |time formats. It returns back a datetime string  |"YYYYMMDD:HHmm")``                          |
|                                  |in ISO format.                                   |                                            |
+----------------------------------+-------------------------------------------------+--------------------------------------------+
|``regex_extract(input, regex,     |This function extracts information from a string |``datetime(regex_extract('${file:name}',    |
|group)``                          |using regex; input is the string you parse, regex|'(.*)_(.*)_(.*)_(\\d\\d\\d\\d-\\d\\d)       |
|                                  |is the regular expression, group is the regex    |(.*)', 4), 'YYYY-MM')``                     |
|                                  |group you want to select                         |                                            |
+----------------------------------+-------------------------------------------------+--------------------------------------------+
|``replace(input, old, new)``      |Replaces all occurrences of a substring with     |``replace('${file:path}','.tiff', '.xml')`` |
|                                  |another substring in the input string            |                                            |
+----------------------------------+-------------------------------------------------+--------------------------------------------+


**Band's unit of measurement (uom) code for netCDF and GRIB recipes**

* In netCDF recipes you can add *uom* for each band by referencing the metadata
  key of the specific variable. For example, for variable ``LAI``:

.. hidden-code-block:: json

   "uomCode": "${netcdf:variable:LAI:units}"

* In GRIB recipes adding uom for bands is same as for netCDF, except that
  a *GRIB expression* is used to fetch this information from metadata in the
  GRIB file. Example:

  .. hidden-code-block:: json

    "bands": [
      {
        "name": "Temperature_isobaric",
        "identifier": "Temperature_isobaric",
        "description": "Bands description",
        "nilReason": "Nil value represents missing values.",
        "nilValue": 9999,
        "uomCode": "${grib:unitsOfFirstFixedSurface}"
      }
    ]

.. _local-metadata:

**Local metadata from input files**

Beside the *global metadata* of a coverage, you can add *local metadata*
for each file which is a part of the whole coverage (e.g a 3D time-series
coverage mosaiced from 2D GeoTiff files).

In ingredient file of *general recipe*, under the metadata section add a "local"
object with keys and values extracted by using format type expression. Example
of extracting an attribute from a netCDF input file:

.. hidden-code-block:: json

    "metadata": {
      "type": "xml",
      "global": {
        ...
      },
      "local": {
        "LocalMetadataKey": "${netcdf:metadata:LOCAL_METADATA}"
      }
    }

Afterwards, each file's envelope (geo domain) and its local metadata
will be added to the coverage metadata under ``<slice>...</slice>`` element
if coverage metadata is imported in XML format. Example of a coverage
containing local metadata in XML from 2 netCDF files:

.. hidden-code-block:: xml

    <slices>

      <!--- Begin Local Metadata from netCDF file 1 -->
      <slice>
        <boundedBy>
          <Envelope>
            <axisLabels>Lat Long ansi forecast</axisLabels>
            <srsDimension>4</srsDimension>
            <lowerCorner>34.4396675 29.6015625
                         "2017-01-10T00:00:00+00:00" 0</lowerCorner>
            <upperCorner>34.7208095 29.8828125
                         "2017-01-10T00:00:00+00:00" 0</upperCorner>
          </Envelope>
        </boundedBy>
        <LocalMetadataKey>FROM FILE 1</LocalMetadataKey>
        <fileReferenceHistory>
        /tmp/wcs_local_metadata_netcdf_in_xml/20170110_0_ecfire_fwi_dc.nc
        </fileReferenceHistory>
      </slice>
      <!--- End Local Metadata from netCDF file 1 -->


      <!--- Begin Local Metadata from netCDF file 2 -->
      <slice>
        <boundedBy>
          <Envelope>
            <axisLabels>Lat Long ansi forecast</axisLabels>
            <srsDimension>4</srsDimension>
            <lowerCorner>34.4396675 29.6015625
                         "2017-02-10T00:00:00+00:00" 3</lowerCorner>
            <upperCorner>34.7208095 29.8828125
                         "2017-02-10T00:00:00+00:00" 3</upperCorner>
          </Envelope>
        </boundedBy>
        <LocalMetadataKey>FROM FILE 2</LocalMetadataKey>
        <fileReferenceHistory>
        /tmp/wcs_local_metadata_netcdf_in_xml/20170210_3_ecfire_fwi_dc.nc
        </fileReferenceHistory>
      </slice>
      <!--- End Local Metadata from netCDF file 2 -->

    </slices>

Since v10.0, local metadata for input files can be fetched from corresponding 
external text files using the optional ``metadata_file`` setting. For example:

.. hidden-code-block:: json

     "local": {
          "local_metadata_key": "${gdal:metadata:local_metadata_key}",
          "metadata_file": {
               // The metadata from the external XML file will be created
               // as a child element of this root element
               "root_element": "INSPIRE",
               // Path to the external XML file corresponding to
               // the importing input file
               "path": "replace('${file:path}', '.tiff', '.xml')"
          }
      }


When subsetting a coverage which contains local metadata section
from input files (via WC(P)S requests), if the geo domains of subsetted
coverage intersect with some input files' envelopes, only local metadata of
these files will be added to the output coverage metadata.

For example: a ``GetCoverage`` request with a trim such that
crs axis subsets are within netCDF file 1:

.. hidden-code-block:: text

   http://localhost:8080/rasdaman/ows?service=WCS&version=2.0.1
          &request=GetCoverage
          &subset=ansi("2017-01-10T00:00:00+00:00")
          &subset=Lat(34.4396675,34.4396675)
          &subset=Long(29.6015625,29.6015625)
          &subset=forecast(0)

The coverage's metadata result will contain *only* local metadata from
netCDF file 1:

.. hidden-code-block:: xml

   <slices>
      <!--- Begin Local Metadata from netCDF file 1 -->
      <slice>
        <boundedBy>
          <Envelope>
            <axisLabels>Lat Long ansi forecast</axisLabels>
            <srsDimension>4</srsDimension>
            <lowerCorner>34.4396675 29.6015625
                         "2017-01-10T00:00:00+00:00" 0</lowerCorner>
            <upperCorner>34.7208095 29.8828125
                         "2017-01-10T00:00:00+00:00" 0</upperCorner>
          </Envelope>
        </boundedBy>
        <LocalMetadataKey>FROM FILE 1</LocalMetadataKey>
        <fileReferenceHistory>
        /tmp/wcs_local_metadata_netcdf_in_xml/20170110_0_ecfire_fwi_dc.nc
        </fileReferenceHistory>
      </slice>
      <!--- End Local Metadata from netCDF file 1 -->
   <slices>

.. _customized-axis-labels:

**Customized axis labels in coverage**

This feature is available since rasdaman version 9.8 for general recipe.
Before, axis labels for a coverage must match axis abbreviations in CRS's GML
definition when they are configured in the ingredient file under section ``"slicer"/"axes"``.
With this new feature, one can set **an arbitrary name** for each axis label by
adding optional configuration ``"crsOrder"`` for each axis accordingly the
position index which **starts from 0** of axis in coverage's CRS.

For example with below configuration, coverage will be created with
3 customized axes ``MyDateTimeAxis, MyLatAxis and MyLongAxis`` based on
coverage's CRS (*AnsiDate* (1 DateTime axis) and *EPSG:4326* (Lat and Long axes)):

.. hidden-code-block:: json

     "axes": {
          "MyDateTimeAxis": {
              // Match DateTime axis in AnsiDate CRS
              "crsOrder": 0,
               ...
            },
          "MyLongAxis": {
              // Match Long axis in EPSG:4326
              "crsOder": 2,
               ...
           },
           "MyLatAxis": {
              // Match Lat axis in EPSG:4326
              "crsOder": 1,
              ...
           }
      }


.. _slice-group-size:

**Group several coverage slices into a group**

Since v9.8+, wcst_import allows to group input files on irregular axes
(with ``"dataBound": false``) by optional ``sliceGroupSize: value (positive integer)``.
E.g:

.. hidden-code-block:: json

    "time": {
        "min": "datetime(regex_extract('${file:name}', '(.*)\\.(.*)',1), 'YYYYMMDD')",
        "gridOrder": 0,
        "type": "ansidate",
        "irregular": true,
        "sliceGroupSize": 7,
        "dataBound": false
    }

If each input slice corresponds to index *X*, and one wants to have slice
groups of size *N*, then the index would be translated with this option to
``X - (X % N)``.

Typical use case is importing 3D coverage from 2D satellite imageries where
time axis is irregular and its values are fetched from input files
by regex expression. Then, all input files which belong to 1 time window
(e.g: ``"sliceGroupSize"``: 7 (7 days in AnsiDate CRS) will have the same value
which is the first date of this week).

.. _band-and-dim-metadata:

**Band and dimension metadata in netCDF**

Metadata can be individually specified for each *band* and *axis* in the
ingredient file. This metadata is automatically added to the result output when
encoding to netCDF. Example:

.. hidden-code-block:: json

    "metadata": {
      "type": "xml",
      "global": {
        "description": "'3-band data.'",
        "resolution": "'1'"
      },
      "bands": {
        "red": {
          "metadata1": "metadata_red1",
          "metadata2": "metadata_red2"
        },
        "green": {
          "metadata3": "metadata_green3",
          "metadata4": "metadata_green4"
        },
        "blue": {
          "metadata5": "metadata_blue5"
        }
      },
      "axes": {
        "i": {
          "metadata_i_1": "metadata_1",
          "metadata_i_2": "metadata_2"
        },
        "j": {
          "metadata_j_1": "metadata_3"
        }
      }
    }

Since v9.7, for this metadata can be automatically derived from the input
netCDF files.

* **band** metadata:

  * If ``"bands"`` is set to ``"auto"`` or does not exist under ``"metadata"``
    in the ingredient file, all user-specified bands will have metadata which is
    fetched directly from the netCDF file.

  * Otherwise, the user could specify metadata explicitly by a dictionary of keys/values.
    Metadata for 1 band is **collected automatically** if: 1) band is not added.
    2) band is set to ``"auto"``.

* **axis** metadata:

  * If ``"axes"`` is set to ``"auto"`` or does not exist under ``"metadata"``
    in the ingredient file, all user-specified axes will have metadata which is
    fetched directly from the netCDF file. The axis label for variable is detected
    from the ``min`` or ``max`` value of CRS axis configuration under
    ``"slicer/axes"`` section. For example:

    .. hidden-code-block:: json

        "slicer": {
           ...
           "axes": {
              "Long": {
                 # 'lon' is variable name in netCDF file for CRS axis 'Long'.
                 "min": "${netcdf:variable:lon:min}"
                  ...
               }
            }
         }

  * Otherwise, the user could specify metadata explicitly by a dictionary of keys/values.
    Metadata for 1 axis is **collected automatically** if: 1) axis is not added. 2) axis
    is set to ``"auto"``. 3) axis is set with ``${netcdf:variable:DimensionName:metadata}``.

.. _data-import-recipe-wcs_extract:

Import from external WCS
^^^^^^^^^^^^^^^^^^^^^^^^

Allows to import a coverage from a remote petascope endpoint into the local
petascope. Parameters are explained below.

.. hidden-code-block:: json

    {
      "config": {
        "service_url": "http://localhost:8080/rasdaman/ows",
        "default_crs": "http://localhost:8080/def/crs/EPSG/0/4326",
        "automated": true
      },
      "input": {
        "coverage_id": "test_wcs_extract"
      },
      "recipe": {
        // name of recipe
        "name": "wcs_extract",
        "options": {
          // remote coverage id in remote petascope
          "coverage_id": "test_time3d",
          // remote petascope endpoint
          "wcs_endpoint" : "http://localhost:8080/rasdaman/ows",
          // the partitioning scheme as a list of the maximum number of pixels on each
          // axis dimension e.g. [500, 500, 1] will split the 3-D coverage in 2-D slices
          // of 500 by 500.
          "partitioning_scheme" : [0, 0, 500],
          // The tiling to be applied in rasdaman
          "tiling": "ALIGNED [0:2000, 0:2000]"
        }
      }
    }


.. _data-import-recipe-sentinel1:

Import Sentinel-1 Data
^^^^^^^^^^^^^^^^^^^^^^

This is a convenience recipe for importing Sentinel 1 data in particular;
**currently only GRD/SLC product types are supported**, and only geo-referenced
tiff files. Below is an example:

.. hidden-code-block:: json

    {
      "config": {
        "service_url": "http://localhost:8080/rasdaman/ows",
        "automated": true,
        "track_files": false
      },
      "input": {
        "coverage_id": "S1_GRD_${modebeam}_${polarisation}",

         // (e.g: a geo-referenced tiff file to CRS: EPSG:4326, mode beam IW,
         //  singler polarisation VH:
         // s1a-iw-grd-vh-20190226t171654-20190326t171719-026512-02f856-002.tiff)
        "paths": [ "*.tiff" ],

        // If not specified, default product is "GRD"
        "product": "SLC"

        "modebeams": ["EW", "IW"],
        "polarisations": ["HH", "HV", "VV", "VH"]
      },
      "recipe": {
        "name": "sentinel1",
        "options": {
          "coverage": {
            "metadata": {
              "type": "xml",
              "global": {
                "Title": "'Sentinel-1 GRD data served by rasdaman'"
              }
            }
          },
          "tiling": "ALIGNED [0:0, 0:1999, 0:1999] TILE SIZE 32000000",
          "wms_import": true
        }
      }
    }

The recipe extends `general_coverage <data-import-recipe-wcs_extract>`_ so
the ``"recipe"`` section has the same structure. However, a lot of information
is automatically filled in by the recipe now, so the ingredients file is much
simpler as the example above shows.

The other obvious difference is that the ``"coverage_id"`` is templated with
several variables enclosed in ``${`` and ``}`` which are automatically replaced
to generate the actual coverage name during import:

- ``modebeam`` - the mode beam of input files, e.g. ``IW/EW``.

- ``polarisation`` - single polarisation of input files, e.g: ``HH/HV/VV/VH``

If the files collected by ``"paths"`` are varying in any of these parameters,
the corresponding variables must appear somewhere in the ``"coverage_id"`` (as
for each combination a separate coverage will be constructed). Otherwise, the
ingestion will either fail or result in invalid coverages. E.g. if all data's mode beam
is  ``IW``, but still different polarisations, the
``"coverage_id"`` could be ``"MyCoverage_${polarisation}"``;

In addition, the data to be ingested can be optionall filtered with the
following options in the ``"input"`` section:

- ``modebeams`` - specify a subset of mode beams to ingest from the data,
  e.g. only the ``IW`` mode beam; if not specified, data of all supported
  mode beams will be ingested.

- ``polarisations`` - specify a subset of polarisations to ingest,
  e.g. only the ``HH`` polarisation; if not specified, data of all supported
  polarisations will be ingested.

**Limitations:**

- Only GRD/SLC products are supported.
- Data must be geo-referenced.
- Filenames are assumed to be of the format:
  ``s1[ab]-(.*?)-grd(.?)-(.*?)-(.*?)-(.*?)-(.*?)-(.*?)-(.*?).tiff`` or
  ``s1[ab]-(.*?)-slc(.?)-(.*?)-(.*?)-(.*?)-(.*?)-(.*?)-(.*?).tiff``.

.. _data-import-recipe-sentinel2:

Import Sentinel-2 Data
^^^^^^^^^^^^^^^^^^^^^^

This is a convenience recipe for importing Sentinel 2 data in particular. It
relies on support for Sentinel 2 in `more recent GDAL versions
<https://gdal.org/frmt_sentinel2.html>`__. Importing zipped Sentinel 2 is also
possible and automatically handled.

Below is an example:

.. hidden-code-block:: json

    {
      "config": {
        "service_url": "http://localhost:8080/rasdaman/ows",
        "automated": true
      },
      "input": {
        "coverage_id": "S2_${crsCode}_${resolution}_${level}",
        "paths": [ "S2*.zip" ],
        // Optional filtering settings
        "resolutions": ["10m", "20m", "60m", "TCI"],
        "levels": ["L1C", "L2A"],
        "crss": ["32757"] // remove or leave empty to ingest any CRS
      },
      "recipe": {
        "name": "sentinel2",
        "options": {
          "coverage": {
            "metadata": {
              "type": "xml",
              "global": {
                "Title": "'Sentinel-2 data served by rasdaman'"
              }
            }
          },
          "tiling": "ALIGNED [0:0, 0:1999, 0:1999] TILE SIZE 32000000",
          "wms_import": true
        }
      }
    }

The recipe extends `general_coverage <data-import-recipe-wcs_extract>`_ so
the ``"recipe"`` section has the same structure. However, a lot of information
is automatically filled in by the recipe now, so the ingredients file is much
simpler as the example above shows.

The other obvious difference is that the ``"coverage_id"`` is templated with
several variables enclosed in ``${`` and ``}`` which are automatically replaced
to generate the actual coverage name during import:

- ``crsCode`` - the CRS EPSG code of the imported files, e.g. ``32757`` for
  WGS 84 / UTM zone 57S.

- ``resolution`` - Sentinel 2 products bundle several subdatasets of different
  resolutions:

  - ``10m`` - bands B4, B3, B2, and B8 (base type unsigned short)

  - ``20m`` - bands B5, B6, B7, B8A, B11, and B12 (base type unsigned short)

  - ``60m`` - bands B1, B8, and B10 (base type unsigned short)

  - ``TCI`` - True Color Image (red, green, blue char bands); also 10m as it is
    derived from the B2, B3, and B4 10m bands.

- ``level`` - ``L1C`` or ``L2A``

If the files collected by ``"paths"`` are varying in any of these parameters,
the corresponding variables must appear somewhere in the ``"coverage_id"`` (as
for each combination a separate coverage will be constructed). Otherwise, the
ingestion will either fail or result in invalid coverages. E.g. if all data is
level ``L1C`` with CRS ``32757``, but still different resolutions, the
``"coverage_id"`` could be ``"MyCoverage_${resolution}"``; the other variables
can still be specified though, so  ``"MyCoverage_${resolution}_${crsCode}"`` is
valid as well.

In addition, the data to be ingested can be optionall filtered with the
following options in the ``"input"`` section:

- ``resolutions`` - specify a subset of resolutions to ingest from the data,
  e.g. only the "10m" subdataset; if not specified, data of all supported
  resolutions will be ingested.

- ``levels`` - specify a subset of levels to ingest, so that files of other
  levels will be fully skipped; if not specified, data of all supported levels
  will be ingested.

- ``crss`` - specify a list of CRSs (EPSG codes as strings) to ingest; if not
  specified or empty, data of any CRS will be ingested.


.. _wms-image-pyramids:

Image pyramids
^^^^^^^^^^^^^^

This feature (v9.7+) allows to create downscaled versions of a given coverage,
eventually achieving something like an image pyramid, in order to enable
faster WMS requests when zooming in/out.

By using the :ref:`scale_levels <scale-levels>` option of wcst_import 
when importing a coverage with WMS enabled, petascope will create downscaled
collections in rasdaman following this pattern: ``coverageId_<level>``.
If level is a float, then *the dot* is replaced with an *underscore*,
as dots are not permitted in a collection name. Some examples:

- MyCoverage, level 2 -> MyCoverage_2
- MyCoverage, level 2.45 -> MyCoverage_2_45

Example ingredients specification to create two downscaled levels which are
*8x* and *32x* smaller than the original coverage:

.. hidden-code-block:: json

    "options": {
      "scale_levels": [8, 32],
      ...
    }

Two new WCS-T non-standard requests are utilized by wcst_import for this feature,
see :ref:`here for more information <wcs-t-non-standard-requests-wms>`.


.. _data-import-recipe-create-own:

Creating your own recipe
^^^^^^^^^^^^^^^^^^^^^^^^

The recipes above cover a frequent but limited subset of what is possible to
model using a coverage. WCSTImport allows to define your own recipes in
order to fill these gaps. In this tutorial we will create a recipe that can
construct a 3D coverage from 2D georeferenced files. The 2D files that we want
to target have all the same CRS and cover the same geographic area. The time
information that we want to retrieve is stored in each file in a GDAL readable
tag. The tag name and time format differ from dataset to dataset so we want to
take this information as an option to the recipe. We would also want to be
flexible with the time crs that we require so we will add this option as well.

Based on this usecase, the following ingredient file seems to fulfill our need:

.. hidden-code-block:: json

    {
      "config": {
        "service_url": "http://localhost:8080/rasdaman/ows",
        "mock": false,
        "automated": false
      },
      "input": {
        "coverage_id": "MyCoverage",
        "paths": [ "/var/data/*" ]
      },
      "recipe": {
        "name": "my_custom_recipe",
        "options": {
          "time_format": "auto",
          "time_crs": "http://localhost:8080/def/crs/OGC/0/AnsiDate",
          "time_tag": "MY_SPECIAL_TIME_TAG",
        }
      }
    }

To create a new recipe start by creating a new folder in the recipes folder.
Let's call our recipe ``my_custom_recipe``:

.. hidden-code-block:: bash

    $ cd $RMANHOME/share/rasdaman/wcst_import/recipes_custom/
    $ mkdir my_custom_recipe
    $ touch __init__.py

The last command is needed to tell python that this folder is containing python
sources, if you forget to add it, your recipe will not be automatically
detected. Let's first create an example of our ingredients file so we get a
feeling for what we will be dealing with in the recipe. Our recipe will just
request from the user two parameters Let's now create our recipe, by creating a
file called ``recipe.py``

.. hidden-code-block:: bash

    $ touch recipe.py
    $ editor recipe.py

Use your favorite editor or IDE to work on the recipe (there are type
annotations for most WCSTImport classes so an IDE like PyCharm would give out of
the box completion support). First, let's add the skeleton of the recipe (please
note that in this tutorial, we will omit the import section of the files (your
IDE will help you auto import them)):

.. hidden-code-block:: python

    class Recipe(BaseRecipe):
        def __init__(self, session):
            """
            The recipe class for my_custom_recipe.
            :param Session session: the session for the import tun
            """
            super(Recipe, self).__init__(session)
            self.options = session.get_recipe()['options']

        def validate(self):
            super(Recipe, self).validate()
            pass

        def describe(self):
            """
            Implementation of the base recipe describe method
            """
            pass

        def ingest(self):
            """
            Ingests the input files
            """
            pass

        def status(self):
            """
            Implementation of the status method
            :rtype (int, int)
            """
            pass

        @staticmethod
        def get_name():
            return "my_custom_recipe"

The first thing you need to do is to make sure the ``get_name()`` method returns
the name of your recipe. This name will be used to determine if an ingredient file
should be processed by your recipe. Next, you will need to focus on the
constructor. Let's examine it. We get a single parameter called ``session`` which
contains all the information collected from the user plus a couple more useful
things. You can check all the available methods of the class in the session.py
file, for now we will just save the options provided by the user that are
available in ``session.get_recipe()`` in a class attribute.

In the ``validate()`` method, you will validate the options for the recipe
provided by the user. It's generally a good idea to call the super method to
validate some of the general things like the WCST Service availability and so on
although it is not mandatory. We also want to validate our custom recipe options
here. This is how the recipe looks like now:

.. hidden-code-block:: python

    class Recipe(BaseRecipe):
        def __init__(self, session):
            """
            The recipe class for my_custom_recipe.
            :param Session session: the session for the import tun
            """
            super(Recipe, self).__init__(session)
            self.options = session.get_recipe()['options']

        def validate(self):
            super(Recipe, self).validate()
            if "time_crs" not in self.options:
                raise RecipeValidationException(
                    "No valid time crs provided")

            if 'time_tag' not in self.options:
                raise RecipeValidationException(
                    "No valid time tag parameter provided")

            if 'time_format' not in self.options:
                raise RecipeValidationException(
                    "You have to provide a valid time format")

        def describe(self):
            """
            Implementation of the base recipe describe method
            """
            pass

        def ingest(self):
            """
            Ingests the input files
            """
            pass

        def status(self):
            """
            Implementation of the status method
            :rtype (int, int)
            """
            pass

        @staticmethod
        def get_name():
            return "my_custom_recipe"

Now that our recipe can validate the recipe options, let's move to the ``describe()``
method. This method allows you to let your users know any relevant information
about the ingestion before it actually starts. The irregular_timeseries recipe
prints the timestamp for the first couple of slices for the user to check if
they are correct. Similar behaviour should be done based on what your recipe has
to do.

Next, we should define the ingest behaviour. The framework does not make any
assumptions about how the correct method of ingesting is, however it offers a
lot of utility functionality that help you do it in a more standardized way. We
will continue this tutorial by describing how to take advantage of this
functionality, however, note that this is not required for the recipe to work.
The first thing that you need to do is to define an *importer* object. This
importer object, takes a *coverage* object and ingests it using WCST requests. The
object has two public methods, ``ingest()``, which ingests the coverage into the
WCS-T service (note: ingest can be an insert operation when the coverage was not
defined, or update if the coverage exists. The importer will handle both cases
for you, so you don't have to worry if the coverage already exists.) and
``get_progress()`` which returns a tuple containing the number of imported slices and
the total number of slices. After adding the importer, the code should look like
this:

.. hidden-code-block:: python

    class Recipe(BaseRecipe):
        def __init__(self, session):
            """
            The recipe class for my_custom_recipe.
            :param Session session: the session for the import tun
            """
            super(Recipe, self).__init__(session)
            self.options = session.get_recipe()['options']
            self.importer = None

        def validate(self):
            super(Recipe, self).validate()
            if "time_crs" not in self.options:
                raise RecipeValidationException(
                    "No valid time crs provided")

            if 'time_tag' not in self.options:
                raise RecipeValidationException(
                    "No valid time tag parameter provided")

            if 'time_format' not in self.options:
                raise RecipeValidationException(
                    "You have to provide a valid time format")

        def describe(self):
            """
            Implementation of the base recipe describe method
            """
            pass

        def ingest(self):
            """
            Ingests the input files
            """
            self._get_importer().ingest()

        def status(self):
            """
            Implementation of the status method
            :rtype (int, int)
            """
            pass

        def _get_importer():
          if self.importer is None:
            self.importer = Importer(self._get_coverage())
          return self.importer

        def _get_coverage():
          pass

        @staticmethod
        def get_name():
            return "my_custom_recipe"


In order to build the importer, we need to create a coverage object. Let's see
how we can do that. The coverage constructor requires a

* ``coverage_id``: the id of the coverage

* ``slices``: a list of slices that compose the coverage. Each slice defines
  the position in the coverage and the data that should be defined at the specified
  position

* ``range_fields``: the range fields for the coverage

* ``crs``: the crs of the coverage

* ``pixel_data_type``: the type of the pixel in gdal format, e.g. Byte, Float32 etc

The coverage object can be built in many ways, we will present one such method.
Let's start from the crs of the coverage.
For our recipe, we want a 3D crs, composed of the CRS of the 2D images and a time CRS
as indicated. The following lines of code give us exactly this:

.. hidden-code-block:: python

    # Get the crs of one of the images using a GDAL helper class.
    # We are assuming all images have the same CRS.
    gdal_dataset = GDALGmlUtil(self.session.get_files()[0].get_filepath())
    # Get the crs of the coverage by compounding the two crses
    crs = CRSUtil.get_compound_crs([gdal_dataset.get_crs(), self.options['time_crs']])

Let's also get the range fields for this coverage. We can extract them again
from the 2D image using a helper class that can use GDAL to get the relevant
information:

.. hidden-code-block:: python

  fields = GdalRangeFieldsGenerator(gdal_dataset).get_range_fields()

Let's also get the pixel base type, again using the gdal helper:

.. code-block:: python

  pixel_type = gdal_dataset.get_band_gdal_type()

Let's see what we have so far:

.. hidden-code-block:: python

    class Recipe(BaseRecipe):
        def __init__(self, session):
            """
            The recipe class for my_custom_recipe.
            :param Session session: the session for the import tun
            """
            super(Recipe, self).__init__(session)
            self.options = session.get_recipe()['options']
            self.importer = None

        def validate(self):
            super(Recipe, self).validate()
            if "time_crs" not in self.options:
                raise RecipeValidationException(
                    "No valid time crs provided")

            if 'time_tag' not in self.options:
                raise RecipeValidationException(
                    "No valid time tag parameter provided")

            if 'time_format' not in self.options:
                raise RecipeValidationException(
                    "You have to provide a valid time format")

        def describe(self):
            """
            Implementation of the base recipe describe method
            """
            pass

        def ingest(self):
            """
            Ingests the input files
            """
            self._get_importer().ingest()

        def status(self):
            """
            Implementation of the status method
            :rtype (int, int)
            """
            pass

        def _get_importer(self):
          if self.importer is None:
            self.importer = Importer(self._get_coverage())
          return self.importer

        def _get_coverage(self):
          # Get the crs of one of the images using a GDAL helper class.
          # We are assuming all images have the same CRS.
          gdal_dataset = GDALGmlUtil(self.session.get_files()[0].get_filepath())
          # Get the crs of the coverage by compounding the two crses
          crs = CRSUtil.get_compound_crs(
            [gdal_dataset.get_crs(), self.options['time_crs']])
          fields = GdalRangeFieldsGenerator(gdal_dataset).get_range_fields()
          pixel_type = gdal_dataset.get_band_gdal_type()
          coverage_id = self.session.get_coverage_id()
          slices = self._get_slices(crs)
          return Coverage(coverage_id, slices, fields, crs, pixel_type)

        def _get_slices(self, crs):
          pass

        @staticmethod
        def get_name():
            return "my_custom_recipe"

As you can notice, the only thing left to do is to implement the _get_slices()
method. To do so we need to iterate over all the input files and create a slice
for each. Here's an example on how we could do that

.. hidden-code-block:: python

    def _get_slices(self, crs):
      # Let's first extract all the axes from our crs
      crs_axes = CRSUtil(crs).get_axes()
        # Prepare a list container for our slices
        slices = []
        # Iterate over the files and create a slice for each one
        for infile in self.session.get_files():
          # We need to create the exact position in time and space in which to
          # place this slice # For the space coordinates we can use the GDAL
          # helper to extract it for us, which will return a list of subsets
          # based on the crs axes that we extracted # and will fill the
          # coordinates for the ones that it can (the easting and northing axes)
          subsets = GdalAxisFiller(
            crs_axes, GDALGmlUtil(infile.get_filepath())).fill()
          # fill the time axis as well and indicate the position in time
          for subset in subsets:
            # Find the time axis
            if subset.coverage_axis.axis.crs_axis.is_future():
              # Set the time position for it. Our recipe extracts it from
              # a GDAL tag provided by the user
              subset.interval.low = GDALGmlUtil(infile).get_datetime(
                self.options["time_tag"])
          slices.append(Slice(subsets, FileDataProvider(tpair.file)))
      return slices

And we are done we now have a valid coverage object. The last thing needed is to
define the status method. This method need to provide a status update to the
framework in order to display it to the user. We need to return the number of
finished work items and the number of total work items. In our case we can
measure this in terms of slices and the importer can already provide this for
us. So all we need to do is the following:

.. hidden-code-block:: python

    def status(self):
        return self._get_importer().get_progress()

We now have a functional recipe. You can try the ingredients file against it and
see how it works.

.. hidden-code-block:: python

    class Recipe(BaseRecipe):
        def __init__(self, session):
            """
            The recipe class for my_custom_recipe.
            :param Session session: the session for the import tun
            """
            super(Recipe, self).__init__(session)
            self.options = session.get_recipe()['options']
            self.importer = None

        def validate(self):
            super(Recipe, self).validate()
            if "time_crs" not in self.options:
                raise RecipeValidationException(
                    "No valid time crs provided")

            if 'time_tag' not in self.options:
                raise RecipeValidationException(
                    "No valid time tag parameter provided")

            if 'time_format' not in self.options:
                raise RecipeValidationException(
                    "You have to provide a valid time format")

        def describe(self):
            """
            Implementation of the base recipe describe method
            """
            pass

        def ingest(self):
            """
            Ingests the input files
            """
            self._get_importer().ingest()

        def status(self):
            """
            Implementation of the status method
            :rtype (int, int)
            """
            pass

        def _get_importer(self):
          if self.importer is None:
            self.importer = Importer(self._get_coverage())
          return self.importer

        def _get_coverage(self):
          # Get the crs of one of the images using a GDAL helper class.
          # We are assuming all images have the same CRS.
          gdal_dataset = GDALGmlUtil(self.session.get_files()[0].get_filepath())
          # Get the crs of the coverage by compounding the two crses
          crs = CRSUtil.get_compound_crs(
            [gdal_dataset.get_crs(), self.options['time_crs']])
          fields = GdalRangeFieldsGenerator(gdal_dataset).get_range_fields()
          pixel_type = gdal_dataset.get_band_gdal_type()
          coverage_id = self.session.get_coverage_id()
          slices = self._get_slices(crs)
          return Coverage(coverage_id, slices, fields, crs, pixel_type)

        def _get_slices(self, crs):
          # Let's first extract all the axes from our crs
          crs_axes = CRSUtil(crs).get_axes()
          # Prepare a list container for our slices
          slices = []
          # Iterate over the files and create a slice for each one
          for infile in self.session.get_files():
            # We need to create the exact position in time and space in which to
            # place this slice # For the space coordinates we can use the GDAL
            # helper to extract it for us, which will return a list of subsets
            # based on the crs axes that we extracted # and will fill the
            # coordinates for the ones that it can (the easting and northing axes)
            subsets = GdalAxisFiller(
                crs_axes, GDALGmlUtil(infile.get_filepath())).fill()
            # fill the time axis as well and indicate the position in time
            for subset in subsets:
                # Find the time axis
                if subset.coverage_axis.axis.crs_axis.is_future():
                # Set the time position for it. Our recipe extracts it from
                # a GDAL tag provided by the user
                subset.interval.low = GDALGmlUtil(infile).get_datetime(
                    self.options["time_tag"])
            slices.append(Slice(subsets, FileDataProvider(tpair.file)))
          return slices

        @staticmethod
        def get_name():
            return "my_custom_recipe"



Data export
===========

**WCS** formats are requested via the **format** KVP key (``<gml:format>``
elements for XML POST requests), and take a valid **MIME type** as value. Output
encoding is passed on to the the GDAL library, so the limitations on output
formats are devised accordingly by the `supported raster formats
<http://www.gdal.org/formats_list.html>`_ of GDAL. The valid MIME types which
Petascope may support can be checked from the WCS 2.0.1 GetCapabilities
response:

.. hidden-code-block:: xml

    <wcs:formatSupported>application/gml+xml</wcs:formatSupported>
    <wcs:formatSupported>image/jpeg</wcs:formatSupported>
    <wcs:formatSupported>image/png</wcs:formatSupported>
    <wcs:formatSupported>image/tiff</wcs:formatSupported>
    <wcs:formatSupported>image/bmp</wcs:formatSupported>
    <wcs:formatSupported>image/jp2</wcs:formatSupported>
    <wcs:formatSupported>application/netcdf</wcs:formatSupported>
    <wcs:formatSupported>text/csv</wcs:formatSupported>
    <wcs:formatSupported>application/json</wcs:formatSupported>
    <wcs:formatSupported>application/dem</wcs:formatSupported>
    ...

In case of *encode* processing expressions, besides MIME types **WCPS** (and
*rasql*) can also accept GDAL format identifiers or other commonly-used format
abbreviations like "CSV" for Comma-Separated-Values for instance.


rasdaman / petascope Geo Service Administration
===============================================

The petascope conpoment, which geo services contact through its OGC APIs,
uses rasdaman for storing the raster arrays; geo-related data parts
(such as geo-referencing), as per coverage standard, are maintained
by petascope itself.

Petascope is implemented as a war file of Java servlets.
Internally, incoming requests requiring coverage evaluation are translated
by petascope, with the help of the coverage metadata, into rasql queries
executed by rasdaman as the central workhorse. Results returned from rasdaman
are forwarded by petascope to the client.

.. NOTE::

   rasdaman can maintain arrays not visible via petascope
   (such as non-geo objects like human brain images).
   Data need to be imported via :ref:`data-import`, not rasql,
   for being visible as coverages.

For further internal documentation on petascope see
`Developer introduction to petascope and its metadata database <http://rasdaman.org/wiki/PetascopeDevGuide>`_.

Service Startup and Shutdown
----------------------------

- For external petascope and SECORE servlets, start/stop external tomcat
  which deploys these web applications.

- For :ref:`embedded petascope and secore servlets <start-stop-embedded-applications>` normally get started and stopped
  automatically through the standard scripts, ``start_rasdaman.sh``and
  ``stop_rasdaman.sh``.

petascope Configuration
-----------------------

The petascope services are configured in file ``$RMANHOME/etc/petascope.properties``.

.. NOTE::
   For changes to take effect Tomcat needs to be restarted after editing this file.

.. _petascope-database-connection:

Meta Database Connectivity
--------------------------

Non-array data of coverages (here loosely called metadata) are stored in
another database, separate from the rasdaman database.
This backend is configured in ``$RMANHOME/etc/petascope.properties``.

As a first action it is strongly recommended to substitute {db-username}
and {db-password} by some safe settings; keeping obvious values constitutes
a major security risk.

Note that the choice is exclusive: only one such database can be used at any time.
Changing to another database system requires a database migration
which is entirely the responsibility of the service operator
and involves substantially more effort than just changing these entries;
generally, it is strongly discouraged to change the meta database backend.

If necessary, add the path to the JDBC jar driver to ``petascope.properties``
using ``metadata_jdbc_jar_path`` and ``spring.datasource.jdbc_jar_path``.

Several different systems are supported as metadata backends.
Below is a list of ``petascope.properties`` settings for different systems
that have been tested successfully with rasdaman.

Postgresql (default)
^^^^^^^^^^^^^^^^^^^^

The following configuration in ``petascope.properties`` enables PostgreSQL
as metadata backend:

.. hidden-code-block:: text

    spring.datasource.url=jdbc:postgresql://localhost:5432/petascopedb
    spring.datasource.username={db-username}
    spring.datasource.password={db-password}
    spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

HSQLDB
^^^^^^

The following configuration in ``petascope.properties`` enables HSQLDB
as metadata backend:

.. hidden-code-block:: text

    spring.datasource.url=jdbc:hsqldb:file://{path-to-petascopedb}/petascopedb.db
    spring.datasource.username={db-username}
    spring.datasource.password={db-password}

H2
^^

The following configuration in ``petascope.properties`` enables H2
as metadata backend:

.. hidden-code-block:: text

    spring.datasource.url=jdbc:h2:file://{path-to-petascopedb}/petascopedb.db;DB_CLOSE_ON_EXIT=FALSE
    spring.datasource.username={db-username}
    spring.datasource.password={db-password}
    spring.h2.console.enabled=true

.. _start-stop-embedded-applications:

petascope Standalone Deployment
-------------------------------

The petascope and secore servlets can be deployed through any suitable
servlet container, or can be operated standalone using its built-in embedded container.
The embedded variant is activated through the directive ``java_server=embedded`` in the respective configuration file.

Below are excerpts from the two configuration files affected showing how to configure this mode.

- ``$RMANHOME/etc/petascope.properties``

   .. hidden-code-block:: ini
 
      java_server=embedded
      server.port=8080
      secore_urls=http://localhost:8081/def

- ``$RMANHOME/etc/secore.properties``

   .. hidden-code-block:: ini
 
      java_server=embedded
      server.port=8081
      secoredb.path={path-to-writable-directory}

In this standalone mode petascope and secore can be started individually
using the central **startup/shutdown** scripts of rasdaman: ::

    $ start_rasdaman.sh --service [secore | petascope]
    $ stop_rasdaman.sh --service [secore | petascope]

Both servlets can beven be started from the command line: ::

    $ java -jar rasdaman.war [ --petascope.confDir={path-to-properties-file} ]
    $ java -jar def.war

- The port required by the embedded tomcat will be fetched from the
  ``server.port`` setting in ``petascope.properties``.
  Assuming the port is set to 9009, petascope can be accessed
  via URL ``http://localhost:9009/rasdaman/ows``.
- For secore, the port required by the embedded tomcat will be fetched
  from the ``server.port`` setting in ``secore.properties``. 
  Assuming the port is set to 9010, secore can be accessed via URL
  ``http://localhost:9010/def``.

.. NOTE::

   Configuration parameter ``secoredb.path`` must be set in
   ``secore.properties`` file to a directoy where the effective
   **SECORE user has write access** for creating the XML database files.


petascope Serving Static Content 
--------------------------------

Serving external static content (such as HTML, CSS, and Javascript)
residing outside ``rasdaman.war`` through petascope can be enabled
with the following setting in ``petascope.properties``: ::

    static_html_dir_path={absolute-path-to-index.html}

with an absolute path to a readable directory containing an ``index.html``.
This will be served as the root, ie: at URL ``http://localhost:8080/rasdaman/``.


Logging
-------

Configuration file ``petascope.properties`` also defines logging.
The log level can be adjusted in verbosity.
Tomcat restart is required for new settings to become effective.

.. NOTE::

   Make sure that Tomcat has write permissions on the ``petascope.log`` file specified.


Geo Service Standards Compliance
================================

rasdaman community is OGC WCS reference implementation and supports the following conformance classes:

- OGC CIS:

 - CIS 1.0:

  - Class GridCoverage
  - Class RectifiedGridCoverage
  - Class ReferenceableGridCoverage
  - Class gml-coverage
  - Class multipart-coverage

 - CIS 1.1:

  - Class grid-regular
  - Class grid-irregular
  - Class gml-coverage
  - Class other-format-coverage

- OGC WCS

 - WCS 2.0:

  - WCS Core
  - WCS Range Subsetting
  - WCS Processing (supporting WCPS 1.0)
  - WCS Transaction
  - WCS CRS
  - WCS Scaling
  - WCS Interpolation

 - WMS 1.3.0:

   - all raster functionality, including SLD ``ColorMap`` styling

  .. NOTE::

     With WCS 2.1, petascope provides an additional proprietary parameter to request
     CIS 1.0 coverages to be returned as CIS 1.1 coverages.
     This is specified by adding parameter ``outputType=GeneralGridCoverage``.


