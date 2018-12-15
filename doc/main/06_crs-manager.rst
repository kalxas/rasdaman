.. highlight:: text

**************
CRS Management
**************

Introduction
============

`SECORE <http://link.springer.com/chapter/10.1007/978-3-642-29247-7_5/>`__
(Semantic Coordinate Reference System Resolver) is a server which
resolves CRS URLs into full CRS definitions represented in GML 3.2.1.
The implementation constitutes the official resolver of OGC, accessible
under ``http://www.opengis.net/def/crs/``

SECORE accepts axis, CRS, and CRS template identifiers as input URLs in GET-KVP
and RESTful syntax. Further, it accepts general XQuery requests on its CRS
database. It is accessible at the following service endpoint:

- ``http://www.opengis.net/def/axis`` for Axis Identifier URLs
- ``http://www.opengis.net/def/crs`` for CRS Identifier URLs and CRS Template URLs
- ``http://www.opengis.net/def/crs-compound`` for Compound CRS URLs
- ``http://www.opengis.net/def/equal`` for semantic CRS URL comparison
- ``http://www.opengis.net/def/crs-query`` for general XQuery requests

If deployed locally, then substitute the official opengis.net part with
localhost, or your own domain.


Service
=======

SECORE stores and queries XML data in a `BaseX <http://basex.org/>`__ XML
database. On the disk this database is stored in
``$CATALINA_HOME/webapps/secoredb``, this is the directory where the Tomcat
process will typically have write access. The database is created and maintained
automatically, so no action by the user is required regarding this.

There are two types of definition collections:

- ``gml`` collection which is fixed and cannot be modified; this is based on the
  EPSG dictionary.

- ``user`` collection where users can add/update/delete definitions.

Each definition has an **identifier** which is checked when updating/deleting
a particular definition. When inserting a definition, its identifier must not
exist in SECORE already.

::

    <gml:identifier codeSpace="EPSG">
      http://www.opengis.net/def/crs/EPSG/0/4326
    </gml:identifier>


User interface
--------------

The SECORE database tree can be viewed and (upon login) modified via graphical
web interface at ``http://your.server/def/index.jsp``.

More generally, any folder and definition can turn to EDIT mode by appending a
``/browse.jsp`` to its URI; e.g.

- ``http://your.server/def/uom/EPSG/0/9001/browse.jsp`` will let you view/edit
  EPSG:9001 unit of measure, whereas

- ``"http://your.server/def/uom/EPSG/0/browse.jsp"`` will let you either remove
  EPSG UoM definitions or add a new one, not necessarily under the EPSG branch:
  the ``"gml:identifier"`` of the new definition will determine its position in
  the tree.

`In this document <http://www.schemacentral.com/sc/niem21/e-gml32_AbstractSingleCRS.html>`__
you can find hints on how to to define new GML definitions of CRSs. Mind that
compounding is achieved at resolve-time by querying SECORE with a
``"crs-compound"`` path, so that only single CRS definitions should be added.

With regard to parametrized CRSs, you should mind that relative `XPaths
<http://www.w3schools.com/xpath/default.asp>`__ are not allowed (either start
with ``/`` or ``//`` when selecting nodes); non-numeric parameters must be
embraced by single or double *quotes* both when setting optional default values
in the definition or when setting custom values in the URI.


Configuration
-------------

The SECORE configuration can be found in ``$RMANHOME/etc/secore.properties``;
editing this file requires restarting Tomcat.

Security
^^^^^^^^

You should set the ``secore_admin_user``, ``secore_admin_pass`` options to
prevent unauthorized users from editing CRS definitions in the ``userdb`` CRS
collection. If these are not set or commented out, then the admin pages have
public access.


Standalone deployment
^^^^^^^^^^^^^^^^^^^^^

Instead of running SECORE in an external Tomcat (the default way), you can run
it through its embedded Tomcat which is included inside the SECORE java web
application (``def.war``). To do this, you need to change the ``java_server``
option to ``embedded``, and change the ``server.port`` to a port which is not
used in your system (e.g. ``server.port=8082``).

Then restart rasdaman and you can access SECORE at ``http://localhost:8082/def``
(if ``server.port=8082`` has been set).

Logging
^^^^^^^

At the end of ``secore.properties`` you will find the logging configuration. It
is recommended to adjust this, and make sure that Tomcat has permissions to
write the secore.log file.

Concepts
========

CRS templates
-------------

CRS templates are concrete definitions targeted by parameterized CRSs where one
or more named parameters allow the customization of one or more elements in the
template itself. As such, they describe (possibly infinite) sets of concrete
CRSs.

.. note::
    The term "parametrized" is generally avoided because it may
    lead to confusion with the term "parametric" in `OGC Abstract Topic
    2 <http://portal.opengeospatial.org/files/?artifact_id=39049>`__ / ISO
    19111-2:2009 which has a significantly different meaning.

Parameters can be resolved through values provided in the CRS URI, or through
defaults defined in the CRS Template definition. Additionally, expressions
("formulae") can be associated with a CRS Template which evaluate to values when
instantiated with parameter values. All values, whether instantiated in a URL
request or coming from a default or a formula, can be substituted in one or
several places in the concrete CRS definition associated with the CRS Template.

**Example**

The following URI defines the Auto Orthographic CRS 42003 specified in sub
clauses 6.7.3.4 and B.9 of `WMS 1.3.0
<http://portal.opengeospatial.org/files/?artifact_id=14416>`__ for "meter" as
unit of measure and centred at 100? West longitude and 45? North latitude:

::

    http://www.opengis.net/def/crs?
      authority=OGC&
      version=1.3&
      code=AUTO42003&
      UoM=m&
      CenterLongitude=-100&
      CenterLatitude=45&

.. note::
    Additional examples of not-completely-specified objects can
    be found in sub clauses B.7, B.8, B.10, and B.11 of the`WMS
    1.3.0 spec <http://portal.opengeospatial.org/files/?artifact_id=14416>`__,
    and in sub clauses 10.1 through 10.3 of OGC 05-096r1 (GML 3.1.1 grid
    CRSs profile).


Structure
^^^^^^^^^

Formally, a CRS Template is a GML document with root
``crsnts:AbstractCRSTemplate``. It contains an element ``crsnts:CrsDefinition``
of some instantiatable subtype of ``gml:AbstractCRS`` together with a list of
formal parameters.

Parameters are ``crsnts:Parameter`` elements listed in the ``crsnts:Parameters``
section. A formal parameter consists of a locally unique name, an XPath target
expression indicating one or a set of substitution points relative to the CRS
subnode, optionally a default value, and optionally a formula. Further, each
parameter has a type associated.

The ``crsnts:value`` element contains a well-formed formula adhering to the JSR
scripting syntax as specified in JSR-233 [5]. The type associated in the
formula's ``crsnts:Parameters`` element denotes the result type of the
expression. Names are enclosed in ``${`` and ``}``; when used in a formula they
shall contain only references to parameter names defined in the same CRS
Template, and no (direct or indirect) recursive references across formulae.

.. note::
    In particular, a formula cannot have its own parameter name as a free
    parameter. The target expression in crsnts:target indicates the places
    where, during request evaluation, the resulting parameter (obtained from URL
    input, or formula evaluation, or by using the default) gets applied to the
    CRS definition, assuming crsnts:CrsDefinition as the relative document root
    for XPath evaluation.

**Example**

The following XML snippet defines a geodetic Parametrized CRS with formal
parameter x substituting parameter values in all (fictitious) axisName elements
appearing the GeodeticCRS root of the CRS definition:

.. code-block:: xml

    <crsnts:ParameterizedCRS>
      <gml:identifier>...</gml:identifier>
      <gml:scope>...</gml:scope>
      <crsnts:parameters>
        <crsnts:parameter name="lon" >
          <crsnts:value>90</crsnts:value>
          <crsnts:target>//longitude | //Longitude</crsnts:target>
        </crsnts:parameter>
        <crsnts:parameter name="zone">
          <crsnts:target>//greenwichLongitude</crsnts:target>
          <crsnts:value>
            min(floor((${lon} + 180.0) / 6.0) + 1,60)
          </crsnts:value>
        </crsnts:parameter>
      </crsnts:parameters>
      <crsnts:targetReferenceSystem
        xlink:href="http://www.opengis.net/def/crs/EPSG/0/4326"/>
    </crsnts:ParameterizedCRS>


Resolution
^^^^^^^^^^

The result of a URI request against a Parametrized CRS depends on the degree of
parameter matching; it is GML document with its root being an instantiatable
subtype of either ``gml:AbstractCRS`` or ``crsnts:AbstractCRSTemplate``. The
response is:

- In case all formal parameters in the Parametrized CRS addressed are
  matched: a CRS definition where all parameters matched are resolved.

  **Example.** Assuming that the name of the above Parametrized CRS example
  is my-own-crs, a possible instantiation of this CRS to a concrete CRS
  Identifier is

  ::

      http://www.opengis.net/def/crs/my-own-crs?lon=47.6

  The response to this instantiation is

  .. code-block:: xml

      <gml:GeodeticCRS>
        ...
      <gml:GeodeticCRS>


- In case not all parameters are matched: a Parametrized CRS where all
  parameters matched are resolved, their corresponding crsnts:Parameter
  is removed, and only the non-matched parameters remain in the
  template.

  **Example.** Assuming the same example as above, the CRS itself can be
  obtained through

  ::

      http://www.opengis.net/def/crs/my-own-crs

  The response to this request is

  .. code-block:: xml

      <crsnts:ParameterizedCRS>
        <gml:identifier>...</gml:identifier>
        <gml:scope>...</gml:scope>
        <crsnts:parameters>
          ...
        </crsnts:parameters>
        <crsnts:targetReferenceSystem xlink:href="..."/>
      </crsnts:ParameterizedCRS>


CRS equality
------------

It is possible that one and the same CRS, axis, etc. is identified by a number
of syntactically different URLs, and it is not straightforward for applications
to decide about equivalence of two given URIs. To remedy this, a comparison
predicate is available in SECORE. A request sent to URL

::

  http://www.opengis.net/def/crs-equal?1=A&2=B

containing two URLs A and B listed as GET/KVP parameters with names 1 and 2,
respectively, will result in a response of true if and only if both URLs
identify the same concept, and false otherwise; the response is embedded in an
XML document.

**Example**

Comparing EPSG codes 4327 and 4326 can be done with this URL:

::

  http://www.opengis.net/def/equal?
    1=http://www.opengis.net/def/crs/EPSG/0/4327
   &2=http://www.opengis.net/def/crs/EPSG/0/4326

The response will look like this:

.. code-block:: xml

  <crsnts:comparisonResult xmlns='http://www.opengis.net/crs-nts/1.0'>
    <crsnts:equal>false</crsnts:equal>
    <crsnts:reason>
      <![CDATA[ ...description text... ]]>
    </crsnts:reason>
  </crsnts:comparisonResult>


Directly Querying SECORE
------------------------

An XQuery GET or POST request sent to URL http://www.opengis.net/def/crs-query
will result in a document obtained from evaluating the XQuery request according
to the XQuery standard.



