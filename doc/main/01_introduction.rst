.. highlight:: bash

############
Introduction
############


The goodies of database technology for building flexible, large-scale
information systems are well known:

* **information integration** bringing along better consistency and much
  easier administration
* **flexibility** by replacing static APIs by dynamic query languages
* **scalability** achieved through advanced storage and processing techniques,
  in particular: query optimization which is a powerful performance booster
* **maturity** of decades of development and functionality richness, as
  opposed to 1.0 versions of reinvented wheels (load balancing, indexing,
  transaction handling, catalog metadata management, ...)

Unfortunately, these advantages until now can be reaped only for alphanumeric
(and, more recently, also vectorial) data types. Raster data (also called
gridded data, sampled data, etc.) do not benefit, since traditional databases do
not support the information category of large, multidimensional arrays.

This gap is closed by the rasdaman technology which offers distinct array
management features. Its conceptual model supports arrays of any number of
dimensions and over virtually any cell ("pixel", "voxel") type. The rasdaman
query language, rasql, is crafted along standard SQL and gives high-level,
declarative access to any kind of raster data. Its architecture principle of
**tile stream processing**, together with highly effective optimizations, has
proven scalable into multi-Terabyte object sizes. Rasdaman has been developed
since 1996 and since has reached a high level of maturity itself being in
operational use since many years.

The rasdaman project strongly commits itself to open standards, in
particular those of the geo service community. We actively participate in the
development and maintenance of the `Open Geospatial Consortium
<http://www.opengeospatial.org>`__ open geo raster standards. Among other
activities, we have developed specification and reference implementation of OGC
WCS 2.0, WCPS, and WPS.

The free and open-source **rasdaman community** version is available for free
download; generally, rasdaman is available in a dual [wiki:License license
model]. If the **scientific background** of rasdaman is of interest, then check out our
`Publications <http://www.faculty.jacobs-university.de/pbaumann/iu-bremen.de_pbaumann/pubs.html>`__
...and cite our papers!

Finally, for the **legalese** see `Imprint and Disclaimer
<http://www.rasdaman.org/wiki/Legal>`__.

Features
========

**Technically**, rasdaman is a `domain independent Array DBMS
<http://en.wikipedia.org/wiki/Array_DBMS>`__, which makes it suitable for `all
applications where raster data management is an issue
<http://rasdaman.org/wiki/ApplicationDomains>`_. The `petascope
<http://rasdaman.org/wiki/Documentation>`_ component of rasdaman adds on geo
semantics for example, with full support for the `OGC
<http://www.opengeospatial.org>`__ standard interfaces `WCS
<http://www.opengeospatial.org/standards/wcs>`__, `WCPS
<http://www.opengeospatial.org/standards/wcps>`__, WCS-T, and WMS; see matrix
below for details and `EarthLook <http://www.earthlook.org>`__ for a
kaleidoscope of hands-on interactive demos.

**Historically**, rasdaman has pioneered the field of Array Databases, being the
first system of this kind. The rasdaman technology has been developed over a
series of EU funded prjects and then marketed by `rasdaman GmbH
<http://www.rasdaman.com>`__, a research spin-off dedicated to its commercial
support, since 2003. In 2008/2009, the company has teamed up with `Jacobs
University <http://www.jacobs-university.de/lsis>`__ for a code split to
establish *rasdaman community* (encompassing a complete Array DBMS) as an
open-source project. The original rasdaman code remains as *rasdaman
enterprise*. Both are kept in sync at any time, and both `rasdaman GmbH
<http://www.rasdaman.com>`__ and `Jacobs University
<http://www.jacobs-university.de/lsis>`__ contribute actively to the open-source
project. The features covered here concern the open-source community version;
a summary of *rasdaman community* versus *rasdaman enterprise* was
presented here earlier, but has been meanwhile abandoned as OSGeo frowned on this.

**Contributions** to the rasdaman community code come from a worldwide team of
collaborators. Notably, a significant extent of the fixes and new functionality
is coming from rasdaman GmbH (such as WMS recently). **All community
contributions submitted are made available in rasdaman community immediately
after checking them for correctness and coherence (eg, with the code guide); no
contribution whatsoever goes into rasdaman enterprise first**. The only action
the company undertakes is to keep both rasdaman variants in sync by merging the
rasdaman community tree into rasdaman enterprise, which typically occurs upon
release of versions so as to keep both in sync. Aside from that, rasdaman
enterprise is developed exclusively by the company and does not contain any
community code that rasdaman community does not contain. So rest assured that
your valuable contributions are to the benefit of the worldwide user community.

Array data model
----------------

Arrays are determined by their extent ("domain") and their cell
("pixel", "voxel"). Extents are given by a lower and upper bound taken
from the integer domain (so negative boundaries are possible as long as
the lower bound remains below the upper bound). For the cells, all base
and composite data types allowed in languages like C/C++ (except for
pointers and arrays) can be defined as cell types, including nested
structs.

Over such typed arrays, collections (ie, tables -
`ODMG <http://www.odmg.org>`__ style, again) are built. Collections have
two columns (attributes), a system-maintained object identifier (OID)
and the array itself. This allows to conveniently embed arrays into
relational modeling: foreign keys in conventional tables allow to
reference particular array objects, in connection with a domain
specification even parts of arrays.

As such, rasdaman is prepared for the forthcoming ISO SQL/MDA
("Multi-Dimensional Arrays") standard, which actually is crafted along
rasdaman array model and query language. This standard will define arras
as new attribute types, following an "array-as-an-attribute" approach
for optimal integration with relations (as opposed to an
"attribute-as-table" approach - as pursued, e.g., by SciDB and SciQL -
which has some remarkable shortcomings in practice).

Query language
--------------

The rasdaman query language, rasql, offers raster processing formulated
through expressions over raster operations in the style of
`SQL <http://www.sql.org>`__. Consider the following query: "*The
difference of red and green channel from all images from collection
LandsatImages where somewhere in the red channel intensity exceeds
127*". In rasql, it is expressed as

.. code-block:: rasql

    select ls.red - ls.green
    from LandsatImages as ls
    where max_cells( ls.red ) > 127

Rasql is a full query language, supporting *select*, *insert*, *update*,
and *delete*. Additionally, the concept of a partial update is
introduced which allows to selectively update parts of an array. In view
of the potentially large size of arrays this is a practically very
relevant feature, e.g., for updating satellite image maps with new
incoming imagery.

Query formulation is done in a declarative style (queries express what
the result should look like, not how to compute it). This allows for
extensive optimization on server side. Further, rasql is safe in
evaluation: every valid query is guaranteed to to terminate in finite
time.

C++ and Java API
----------------

Client development is supported by the C++ API, *raslib*, and the Java
API, *rasj*; both adhere to the `ODMG standard <http://www.odmg.org>`__.
Communication with a rasdaman database is simple: open a connection,
send the query string, receive the result set. Iterators allow
convenient acecss to query results.

Once installed, go into the share/rasdaman/examples subdirectory to find
sample code.

Tiled storage
-------------

On server side, arrays are stored inside a standard database. To this
end, arrays are partitioned into subarrays called *tiles*; each such
tile goes into a BLOB (binary large object) in a relational table. This
allows conventional relational database systems to maintain arrays of
unlimited size.

A spatial index allows to quickly locate the tiles required for
determining the tile set addressed by a query.

The partitioning scheme is open - any kind of tiling can be specified
during array instantiation. A set of tiling strategies is provided to
ease administrators in picking the most efficient tiling.

Tile streaming
--------------

Query evaluation in the server follows the principle of *tile
streaming*. Each operator node processes a set of incoming tiles and
generates an output tile stream itself. In many cases this allows to
keep only one database tile at a time in main memory. Query processing
becomes very efficient even on low-end server machines.

Server multiplexing
-------------------

A rasdaman server installation can consist of an arbitrary number of
rasdaman server processes. A dynamic scheduler, *rasmgr*, receives
incoming connection requests and assigns a free server process. This
server process then is dedicated to the particular client until the
connection is closed. This allows for highly concurrent access and, at
the same time, increases overall safety as clients are isolated against
each other.

Rasdaman Application Domains
============================

Its features make rasdaman suitable for all applications where raster data
management is an issue, such as:

**earth sciences**

    1-D sensor time series; 2-D airborne/satellite image maps; 3-D satellite image time series; 3-D geo tomograms; 4-D climate and ocean data; ...
    At `EarthLook <http://www.earthlook.org>`_ there is a demonstration of services on 1-D to 4-D geo raster objects.
    The workhorse of the service stack is rasdaman, running on top of `PostgreSQL <http://www.postgresql.org>`_.

**space sciences**

    2-D visibility maps; x/y/frequency observation data cubes; 4-D cosmological simulation data; ...

**life sciences**

    3-D brain activation maps; 3-D/4-D gene expression maps; ...

**engineering**

    1-D measurement time series; 3-D/4-D simulation result data; ...

**multimedia**

    1-D audio; 2-D imagery; 3-D movies; ...

See the `publication list <http://www.peter-baumann.org/pubs.html>`_
for descriptions of a variety of projects where rasdaman has been
successfully used.


OGC geo standards support
=========================

While rasdaman itself is domain agnostic and supports any array
application, the *petascope* servlet, as part of rasdaman, adds in geo
semantics, such as dealing with geo coordinates. To this end, rasdaman
implements the `Open Geospatial
Consortium <http://www.opengeospatial.org>`__ standards for gridded
`coverages <http://en.wikipedia.org/wiki/Coverage_data>`__, i.e.,
multi-dimensional raster data. The OGC service interfaces supported are
- `Web Coverage Service <http://en.wikipedia.org/wiki/Web_Coverage_Service>`__: a versatile, modular suite for accessing and server-side processing of coverages,
- `Web Coverage Processing Service <http://en.wikipedia.org/wiki/Web_Coverage_Processing_Service>`__: OGC's Big Datacube Analytics language,
- `Web Map Service <http://en.wikipedia.org/wiki/Web_Map_Service>`__: for rendering coverage data into maps which can be displayed with a wide range of open-source and commercial clients.

The Princial Architect of rasdaman, Peter Baumann, is `chair of the OGC
WCS Standards Working Group (WCS.SWG) and editor of coverage model
(GMLCOV), WCPS, and most of the WCS
specifications <http://www.ogcnetwork.net/wcs>`__, rasdaman naturally
has become Reference Implementation for several of these standards and
usually implements them first and way ahead of other systems, even
before final adoption. Likewise, any changes to coverage-related
specifications usually are verified in rasdaman first and, hence, become
available early. The same holds for the OGC conformance testing of
coverage services where rasdaman code contributors have a lead. In
summary, rasdaman can be considered the most comprehensive and best
tested implementation of the OGC coverage standards.

How to Contribute
=================

There are lots of ways to get involved and help out with the rasdaman project:

**Help us spot & fix bugs.**

  Which software is perfect? We know there are some bugs in rasdaman, see the `open tickets <http://rasdaman.org/report/1>`_ (or the `low complexity tickets for beginners <http://rasdaman.org/report/14>`_. Whether you add a ticket or `provide a fix <http://rasdaman.org/wiki/ProvideFix>`_,  all is most welcome.

**Write documentation.**

    Users can always benefit from better documentation. Currently the documentation is in reStructuredText format, and HTML/PDF is automatically generated. We're eager for any documentation contributions.

**Contribute to the Wiki.**

    Of course you can also contribute to the wiki, for example by adding `HowTos and FAQs <http://rasdaman.org/wiki/FAQ>`_. Send a message with a change request to *patch* in the domain *rasdaman.org*.

**Help plan and design the next version.**

    Browse this section of the website, we use "Feature" tickets to hold ideas for new features; add your own and/or discuss a topic on the  `dev list <http://rasdaman.org/wiki/MailingLists>`_.

