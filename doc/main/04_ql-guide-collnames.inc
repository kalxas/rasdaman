With the following rasql statement, a list of the names of all
collections currently existing in the database is retrieved; both
versions below are equivalent: ::

    select RAS_COLLECTIONNAMES
    from RAS_COLLECTIONNAMES

    select r
    from RAS_COLLECTIONNAMES as r

Note that the meta collection name, ``RAS_COLLECTIONNAMES``, must be written in
upper case only. No operation in the select clause is permitted. The
result is a set of one-dimensional char arrays, each one holding the
name of a database collection. Each such char array, i.e., string is
terminated by a zero value ('\0').
