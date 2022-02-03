The rasdaman-geo frontend (petascope) can be configured via changing
settings in ``/opt/rasdaman/etc/petascope.properties``.
For changes to take effect, system Tomcat (if deployment is ``external``)
or rasdaman (if deployment is ``embedded``) needs to be restarted after
editing this file.

.. _conf-database:

Database
^^^^^^^^

-   ``spring.datasource.url`` set the connectivity string to
    the database administered by rasdaman-geo. Supported databases are 
    PostgreSQL, H2, HSQLDB; for more details, see :ref:`this section
    <petascope-database-connection>`.

    - Default: ``jdbc:postgresql://localhost:5432/petascopedb``

    - Need to change: **YES** when DMBS other than PostgreSQL is used


-   ``spring.datasource.username`` set the username for connecting to the above 
    database.

    - Default: ``petauser``

    - Need to change: **YES** when changed in the above database


-   ``spring.datasource.password`` set the password for the user specified by
    ``spring.datasource.username``.

    - Default: ``randomly generated password``

    - Need to change: **YES** when changed in the above database


-   ``spring.datasource.jdbc_jar_path`` absolute path to the JDBC jar file for
    connecting to the database configured in setting ``spring.datasource.url``.
    If left empty, the default PostgreSQL JDBC driver will be used. 
    To use a different DBMS (e.g. H2), please download the corresponding JDBC
    driver, and set the path to it.

    - Default: empty

    - Need to change: **YES** when a DMBS other than PostgreSQL is used


-   ``metadata_url`` set the connectivity string to the database administered by
    rasdaman-geo. This setting is only used for *database migration* from one
    DBMS to another (e.g. PostgreSQL to H2) with 
    :ref:`migrate_petascopedb.sh <executables-migrate-petascopedb>`; in this
    case ``metadata_url`` is used to connect to the *source database*, while
    ``spring.datasource.url`` is used to connect to the *target database*.

    - Default:  ``jdbc:postgresql://localhost:5432/petascopedb``

    - Need to change: **YES** when migrating from a DMBS different from PostgreSQL


-   ``metadata_user`` set the username for the above database

    - Default: ``petauser``

    - Need to change: **YES** when different in the above database


-   ``metadata_pass`` set the password for the user specified by
    ``metadata_user``

    - Default: ``petapasswd``

    - Need to change: **YES** when different in the above database


-   ``metadata_jdbc_jar_path`` absolute path to the JDBC jar file for
    connecting to the database configured in setting ``metadata_url``.
    If left empty, the default PostgreSQL JDBC driver will be used. 
    To use a different DBMS (e.g. H2), please download the corresponding JDBC
    driver, and set the path to it.

    - Default: empty

    - Need to change: **YES** when a DMBS other than PostgreSQL is used


General
^^^^^^^

-   ``server.contextPath`` when rasdaman-geo is running in embedded mode
    (setting :ref:`java_server <conf-deployment>`), this setting allows to
    control the prefix in the deployed web application URL, e.g. the
    ``/rasdaman`` in ``http://localhost:8080/rasdaman/ows``.

    - Default: ``/rasdaman``

    - Need to change: NO


-   ``secore_urls`` set SECORE endpoints to be used by rasdaman-geo. Multiple
    endpoints for fail-safety can be specified as a comma-separated list,
    attempted in order as listed. By default, ``internal`` indicates that
    rasdaman-geo should use its own ``SECORE``, which is more efficient as it
    avoids external HTTP requests.

    - Default: ``internal``

    - Need to change: NO


-   ``xml_validation`` if set to ``true``, WCS ``POST/SOAP`` XML requests will
    be validated against ``OGC WCS 2.0.1`` schema definitions; when starting
    Petascope it will take around 1-2 minutes to load the schemas from the OGC
    server.

    .. NOTE::

        Passing the *OGC CITE* tests requires this parameter to be set to ``false``.

    - Default: ``false``

    - Need to change: NO


-   ``ogc_cite_output_optimization`` if ``true``, rasdaman-geo will optimize
    responses in order to pass a couple of invalid *OGC CITE* test cases.
    Indentation of ``WCS GetCoverage`` and ``WCS DescribeCoverage`` results,
    for example, will be trimmed.

    - Default: ``false``

    - Need to change: NO, except when executing *OGC CITE* tests

.. _conf-petascope-servlet-url:

-   ``petascope_servlet_url`` set the service endpoint in ``<ows:HTTP>`` elements
    of the result of ``GetCapabilities``. Change to your public service URL if
    rasdaman-geo runs behind a proxy; if not set then it will be automatically
    derived, usually to ``http://localhost:8080/rasdaman/ows``.

    - Default: empty

    - Need to change: **YES** when rasdaman-geo runs behind a proxy

.. _conf-allow-write-requests-from:

-   ``allow_write_requests_from`` configure from which IP addresses (as a comma-separated
    list) should the server accept write requests such as WCS-T
    ``InsertCoverage``, ``UpdateCoverage`` and ``DeleteCoverage``.
    ``127.0.0.1`` will allow locally generated requests, usually needed to
    import data with ``wcst_import.sh``; setting to empty will block all
    requests, while ``*`` will allow any IP address.

    .. NOTE::

        This setting (i.e. the origin IP) is ignored when a request contains basic auth 
        credentials for a valid rasdaman user with ``RW`` rights in the HTTP Authorization header.

    - Default: ``127.0.0.1``

    - Need to change: NO, unless more IP addresses should be allowed to execute
      write requests


-   ``max_wms_cache_size`` set the maximum amount of memory (in bytes) to use
    for caching WMS ``GetMap`` requests. This setting speeds up repeating WMS
    operaions over similar area/zoom level. 
    It is recommended to consider increasing the parameter
    if the system has more RAM, but make sure to correspondingly 
    update the ``-Xmx`` option for Tomcat as well. The cache evicts 
    least recently inserted data when it reaches the maximum limit specified here.

    - Default: ``100000000`` (100 MB)

    - Need to change: NO


-   ``uploaded_files_dir_tmp`` set an absolute path to a server directory where
    files uploaded to rasdaman-geo by a request will be temporarily stored; the
    user running rasdaman-geo (either tomcat or rasdaman) should have write
    permissions on the specified directory.

    - Default: ``/tmp/rasdaman_petascope/upload``

    - Need to change: NO


-   ``full_stacktraces`` log only stacktraces generated by rasdaman (``false``),
    or full stacktraces including all external libraries (``true``).
    It is recommended to keep this setting to ``false`` for shorter exception 
    stacktraces in ``petascope.log``.

    - Default: ``false``

    - Need to change: NO


.. _conf-deployment:

Deployment
^^^^^^^^^^

-   ``java_server`` specify how is rasdaman-geo deployed: ``embedded`` starts
    the Web application standalone with embedded Tomcat, listening on the
    ``server.port`` setting as configured below, while ``external`` indicates
    that ``rasdaman.war`` is deployed in the ``webapps`` dir of external
    Tomcat.

    It is recommended to set ``embedded``, as there is no dependency on
    external Tomcat server, ``petascope.log`` can be found in the rasdaman log
    directory ``/opt/rasdaman/log``, and start/stop of rasdaman-geo is in sync with
    starting/stopping the rasdaman service. Setting to ``external`` on the other
    hand can be preferred when there is already an existing Tomcat server
    running other Web applications.

    - Default: ``embedded``

    - Need to change: NO, unless rasdaman-geo is deployed in external Tomcat


-   ``server.port`` set the port on which ``embedded`` rasdaman-geo
    (``java_server=embedded`` above) will listen when rasdaman starts. This
    setting has no effect when ``java_server=external``.

    - Default: ``8080``

    - Need to change: **YES** when port ``8080`` is occupied by another process,
      e.g. external Tomcat


Rasdaman
^^^^^^^^

-   ``rasdaman_url`` set the connection URL to the rasdaman database. Normally
    rasdaman is installed on the same machine, so the bellow needs no changing
    (unless the default ``rasmgr`` port 7001 has changed).

    - Default: ``http://localhost:7001``

    - Need to change: NO, unless changed in rasdaman (not recommended)


-   ``rasdaman_database`` set the name of the rasdaman database 
    (configured in ``/opt/rasdaman/etc/rasmgr.conf``).

    - Default: ``RASBASE``

    - Need to change: NO, unless changed in rasdaman (not recommended)


-   ``rasdaman_user`` set the user for **unauthenticated** read-only access to
    rasdaman. Any request which does not provide credentials for a rasdaman user in
    basic authentication format in the HTTP Authorization header, will entail
    executing read-only operations with this user in rasdaman.
    It is best to limit this user to read-only access in rasdaman by granting
    the ``R`` permission to it.

    - Default: ``rasguest``

    - Need to change: **YES** when changed in rasdaman


-   ``rasdaman_pass`` set the password for the user set for ``rasdaman_user``.
    It is recommended to change the default password for ``rasguest`` user in
    rasdaman and update the value here.

    - Default: ``rasguest``

    - Need to change: **YES** when changed in rasdaman    


-   ``rasdaman_admin_user`` this user is used to map updating OGC requests 
    (e.g. during data import, or deleting coverages) to updating rasql queries, for
    any request which does not provide credentials for a rasdaman user in
    basic authentication format in the HTTP Authorization header.
    Additionally, these credentials are used internally for various tasks which require
    admin access rights in rasdaman.

    Generally, this user should be granted full admin permissions.

    - Default: ``rasadmin``

    - Need to change: **YES** when changed in rasdaman


-   ``rasdaman_admin_pass`` set the password for the user set for
    ``rasdaman_admin_user``. It is recommended to change the default password
    for ``rasadmin`` user in rasdaman and update the value here.

    - Default: ``rasadmin``

    - Need to change: **YES** when changed in rasdaman


-   ``rasdaman_retry_attempts`` set the number of re-connect attempts to a 
    rasdaman server in case a connection fails.

    - Default: ``5``

    - Need to change: NO


-   ``rasdaman_retry_timeout`` set the wait time in seconds between re-connect
    attempts to a rasdaman server.

    - Default: ``10`` (seconds)

    - Need to change: NO


-   ``rasdaman_bin_path`` absolute path to the :ref:`rasdaman
    executables directory <sec-executables>`.

    - Default: ``/opt/rasdaman/bin``

    - Need to change: NO


HTTPS
^^^^^

-   ``security.require-ssl`` allow :ref:`embedded <conf-deployment>` petascope
    to work with HTTPS requests from its endpoint.

    - Default: ``false``

    - Need to change: NO


INSPIRE
^^^^^^^

-   ``inspire_common_url`` set the URL to an external catalog service 
    for the INSPIRE standard, to be provided by the user. 
    If not set then it will be automatically derived from the
    :ref:`petascope_servlet_url <conf-petascope-servlet-url>` setting.

    - Default: empty

    - Need to change: NO


Demo Web
^^^^^^^^

-   ``static_html_dir_path`` absolute path to a directory containing static demo
    Web pages (html/css/javascript). If set, rasdaman-geo will serve the
    ``index.html`` in this directory at the ``/rasdaman`` endpoint, e.g. 
    ``http://localhost:8080/rasdaman/``.
    Changes of files in this directory do not require a rasdaman-geo restart.
    The system user running Tomcat (if ``java_server=external``) or rasdaman 
    (if ``java_server=embedded``) must have read permission on this directory.

    - Default: empty

    - Need to change: **YES** when demo web pages required under radaman-geo's endpoint

Logging
^^^^^^^

rasdaman-geo uses the ``log4j`` library version ``1.2.17`` provided by Spring
Boot version ``1.5.2`` to log information/errors in a ``petascope.log`` file.
See the `log4j 1.2 document for more details
<https://logging.apache.org/log4j/1.2/manual.html>`__.

-   Configuration for petascope logging; by default only level ``INFO`` or 
    higher is logged to a file. The valid logging levels are 
    ``TRACE``, ``DEBUG``, ``INFO``, ``WARN``, ``ERROR`` and ``FATAL``.

    .. code-block:: ini

        log4j.rootLogger=INFO, rollingFile

-   Configuration for reducing logs from external libraries: Spring, Hibernate,
    Liquibase, GRPC and Netty.

    .. code-block:: ini

        log4j.logger.org.springframework=WARN
        log4j.logger.org.hibernate=WARN
        log4j.logger.liquibase=WARN
        log4j.logger.io.grpc=WARN
        log4j.logger.io.netty=WARN
        log4j.logger.org.apache=WARN


-   Configure ``file`` logging. The paths for ``file`` logging specified below
    should be write-accessible by the system user running Tomcat. If running
    embedded Tomcat, then the files should be write accessible by the system
    user running rasdaman, which is normally ``rasdaman``.

    .. code-block:: ini

        log4j.appender.rollingFile.layout=org.apache.log4j.PatternLayout  
        log4j.appender.rollingFile.layout.ConversionPattern=%6p [%d{yyyy-MM-dd HH:mm:ss}] %c{1}@%L: %m%n


-   Select one strategy for rolling files and comment out the other. 
    Default is rolling files by time interval.

    .. code-block:: ini

        # 1. Rolling files by maximum size and index
        #log4j.appender.rollingFile.File=@LOG_DIR@/petascope.log
        #log4j.appender.rollingFile.MaxFileSize=10MB
        #log4j.appender.rollingFile.MaxBackupIndex=10
        #log4j.appender.rollingFile=org.apache.log4j.RollingFileAppender

        # 2. Rolling files by time interval (e.g. once a day, or once a month)
        log4j.appender.rollingFile.rollingPolicy.ActiveFileName=@LOG_DIR@/petascope.log  
        log4j.appender.rollingFile.rollingPolicy.FileNamePattern=@LOG_DIR@/petascope.%d{yyyyMMdd}.log.gz
        log4j.appender.rollingFile=org.apache.log4j.rolling.RollingFileAppender  
        log4j.appender.rollingFile.rollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy
