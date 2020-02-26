rasdapy - Talk rasql using Python
=================================

**rasdapy is a client API for rasdaman that enables building and executing rasql
queries within python.**

Requirements
------------

-  numpy, grpcio, protobuf
-  a running rasdaman instance, see http://rasdaman.org/wiki/Download

Installation
------------

1) Make sure you have installed ``pip3`` (e.g. ``sudo apt install python-pip3``)

2) Install rasdapy3 with ``pip3 install rasdapy3``

2) Note that if you do not have setuptools, numpy, grpcio, and protobuf installed,
   they will be downloaded as dependencies.

Usage
-----

A `full client <http://rasdaman.org/browser/applications/rasdapy3/rasql.py>`__
with a similar interface as the C++ rasql client is available that demonstrates
how to use rasdapy to send queries to rasdaman and handle the results. Below the
most important details for using rasdapy are listed.


Import rasdapy core API
-----------------------

::

  >>> from rasdapy.db_connector import DBConnector
  >>> from rasdapy.query_executor import QueryExecutor

Connect to rasdaman
-------------------

The ``DBConnector`` maintains the connection to rasdaman. In order to connect it
is necessary to specify the host and port on which rasmgr is running, as well as
valid rasdaman username and password.

::

  >>> db_connector = DBConnector("localhost", 7001, "rasadmin", "rasadmin")

Create the query executor
-------------------------

``QueryExcutor`` is the interface through which rasql queries (create, insert,
update, delete, etc.) are executed.

::

  >>> query_executor = QueryExecutor(db_connector)

Open the connection to rasdaman
-------------------------------

::

  >>> db_connector.open()

Execute sample queries
----------------------

The query below returns a list of all the collections available in rasdaman.

::

  >>> colls = query_executor.execute_read("select c from RAS_COLLECTIONNAMES as c")
  >>> print(colls)

Calculate the average of all values in collection mr2.

::

  >>> result = query_executor.execute_read("select avg_cells(c) from mr2 as c")
  >>> type(result)

Depending on the query the result will have a different type (e.g. scalar value,
interval, array). Each data type is wrapped in a `corresponding class
<http://rasdaman.org/browser/applications/rasdapy3/rasdapy/models>`__.


Select a particular subset of each array in collection mr2. This query will
return raw array data that can be converted to a Numpy ndarray.

::

  >>> result = query_executor.execute_read("select m[0:10 ,0:10] from mr2 as m")
  >>> numpy_array = result.to_array()

Encode array subset to PNG format and write the result to a file.

::

  >>> result = query_executor.execute_read("select encode(m[0:10 ,0:10], \"png\") from mr2 as m")
  >>> with open("/tmp/output.png", "wb") as binary_file:
  >>>   binary_file.write(result.data)

Create a rasdaman collection. Note that you should be connected with a user that
has write permission; by default this is rasadmin/rasadmin in rasdaman, but this
can be managed by the administrator.

::

  >>> query_executor.execute_write("create collection test_rasdapy GreySet")

Insert data from a PNG image into the collection. Similarly you need to have
write permissions for this operation.

::

  >>> query_executor.execute_write("insert into test_rasdapy values decode($1)", "mr_1.png")

Alternatively, you can import data from a raw binary file; in this case it is
necessary to specify the spatial domain and array type.

::

  >>> query_executor.execute_update_from_file("insert into test_rasdapy values $1",
                                              "raw_array.bin", "[0:100]", "GreyString")

Further example queries and a general guide for rasql can be found in the 
`rasdaman documentation <http://doc.rasdaman.org/>`__.

Close the connection to rasdaman
--------------------------------

::

  >>> db_connector.close()

Best practices:
---------------

It is recommended to follow this template in order to avoid problems
with leaked transactions:

::

    from rasdapy.db_connector import DBConnector
    from rasdapy.query_executor import QueryExecutor

    db_connector = DBConnector("localhost", 7001, "rasadmin", "rasadmin")
    query_executor = QueryExecutor(db_connector)

    db_connector.open()

    try:
        query_executor.execute_read("...")
        query_executor.execute_write("...")
        # ... more Python code
    finally:
        db_connector.close()


Contributors
------------

-  Bang Pham Huu
-  Siddharth Shukla
-  Dimitar Misev
-  Jean-François Lecomte
-  Dragi Kamov

Thanks also to
--------------

-  Alex Mircea Dumitru
-  Vlad Merticariu
-  George Merticariu
-  Alex Toader
-  Peter Baumann
