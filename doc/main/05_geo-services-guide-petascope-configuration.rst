The rasdaman-geo Web frontend (petascope) can be configured via changing
settings in ``/opt/rasdaman/etc/petascope.properties``.
For changes to take effect, system Tomcat (if deployment is ``external``)
or rasdaman (if deployment is ``embedded``) needs to be restarted after
editing this file.

Database
^^^^^^^^

-   ``spring.datasource.url`` set the connectivity string to
    the database administered by rasdaman-geo

    - Default: ``jdbc:postgresql://localhost:5432/petascopedb``

    - Need to change: **NO** when PostgreSQL used, **YES** when other DMBS 
      (e.g. ``H2`` database) used


-   ``spring.datasource.username`` set the username for the above database

    - Default: ``petauser``

    - Need to change: **YES** when changed in the above database


-   ``spring.datasource.password`` set the password for the above database.
    Recommendation: change the password after rasdaman installation.

    - Default: ``petapasswd``

    - Need to change: **YES** when changed in the above database


-   ``spring.datasource.jdbc_jar_path`` set the path to JDBC jar file for Spring datasource.
    If left empty, the default PostgreSQL JDBC driver will be used. 
    To use a different DBMS (e.g. H2), please  download the corresponding JDBC driver,
    and set the path to it.

    - Default: 

    - Need to change: **YES** when diferent DMBS than PostgreSQL used for Spring datasource


-   ``metadata_url`` only used for *database migration*. Set the connectivity string to
    the database administered by rasdaman-geo. 
    This configuration is used as *source database* (while ``spring.datasource.url`` is *target database*)
    to either migrate ``petascopedb`` from one rasdaman version to a newer one
    (only used for ``v9.4`` to ``v9.5``), or in same DBMS or to different DBMS
    (e.g. ``postgres`` to ``H2``) when executing ``migrate_petascopedb.sh``.

    - Default:  ``jdbc:postgresql://localhost:5432/petascopedb``

    - Need to change: **YES** when migrating to a different DMBS


-   ``metadata_user`` set the username for the above database

    - Default: ``petauser``

    - Need to change: **YES** when changed in the above database


-   ``metadata_pass`` set the password for the above database

    - Default: ``petapasswd``

    - Need to change: **YES** when changed in the above database


-   ``metadata_jdbc_jar_path`` set the path to JDBC jar file for Spring datasource.
    If left empty, the default PostgreSQL JDBC driver will be used. 
    To use a different DBMS (e.g. H2), please  download the corresponding JDBC driver,
    and set the path to it.

    - Default: 

    - Need to change: **YES** when diferent DMBS than PostgreSQL used for Spring datasource


General
^^^^^^^

-   ``server.contextPath`` set the prefix for controller in web application URL routing,
    e.g. the ``/rasdaman`` in ``http://localhost:8080/rasdaman/ows``.

    .. NOTE::

        Work only when running rasdaman-geo in ``embedded`` mode.

    - Default: ``/rasdaman``

    - Need to change: NO


-   ``secore_urls`` set SECORE endpoints to be used by rasdaman-geo. Multiple endpoints
    (for fail safety) can be specified as comma-separated list, attempted in order as listed.
    By default, ``internal`` indicates that rasdaman-geo should use its own ``SECORE``.

    - Default: ``internal``

    - Need to change: NO


-   ``xml_validation`` if set to ``true``, WCS ``POST/SOAP`` XML requests will be validated against 
    ``OGC WCS 2.0.1`` schema definitions; when starting Petascope it will take around 
    1-2 minutes to load the schemas from the OGC server. Set to ``false`` by default;

    .. NOTE::

        Passing the *OGC CITE* tests also requires it to be set to ``false``.

    - Default: ``false``

    - Need to change: NO


-   ``ogc_cite_output_optimization`` optimize responses in order to pass a couple of broken *OGC CITE* test cases.
    Indentation of ``WCS GetCoverage`` and ``WCS DescribeCoverage`` results will be trimmed.

    .. NOTE::

        Only set to ``true`` when executing *OGC CITE* tests.

    - Default: ``false``

    - Need to change: NO


-   ``petascope_servlet_url`` set the service endpoint in ``<ows:HTTP>`` element of ``WCS GetCapabilities``.
    Change to your public service URL if rasdaman-geo runs behind a proxy; if not set then it
    will be automatically derived, usually to ``http://localhost:8080/rasdaman/ows``.

    - Default: 

    - Need to change: **YES** when rasdaman-geo runs behind a proxy


-   ``allow_write_requests_from`` accept write requests such as ``WCS-T`` 
    (``InsertCoverage``, ``UpdateCoverage`` and ``DeleteCoverage``) only 
    from the comma-separated list of IP addresses. By default ``127.0.0.1`` will allow
    locally generated requests, usually needed to import data with ``wcst_import.sh``.
    Setting to empty will block all requests, while ``*`` will allow any IP address.

    .. NOTE::

        This setting (i.e. the origin IP) is ignored when a request contains basic auth 
        credentials for a valid rasdaman user with ``RW`` rights in the HTTP Authorization header.

    - Default: ``127.0.0.1``

    - Need to change: **YES** when more IP addresses are allowed to send write requests


-   ``max_wms_cache_size`` set the maximum amount of memory (in bytes) to use
    for caching ``WMS GetMap`` requests. This setting speeds up repeating ``WMS``
    operaions over similar area/zoom level. 
    Recommendation: consider increasing the parameter
    if the system has more RAM, but make sure to correspondingly 
    update the ``-Xmx`` option for Tomcat as well. The cache evicts 
    last recently inserted data when it reaches the maximum limit specified here.

    - Default: ``100000000`` (100 MB)

    - Need to change: NO


-   ``uploaded_files_dir_tmp`` set server directory where files uploaded to rasdaman-geo 
    by a request will be temporarily stored.

    - Default: ``/tmp/rasdaman_petascope/upload``

    - Need to change: NO


-   ``full_stacktraces`` print only stacktraces generated by rasdaman (``false``),
    or full stacktraces including all external libraries (``true``).
    Recommendation: set to ``false`` for shorter exception stack traces in
    ``petascope.log``.

    - Default: ``false``

    - Need to change: NO


Deployment
^^^^^^^^^^

-   ``java_server``  specify how is petascope deployed: ``1. embedded``
    start standalone with embedded Tomcat, listening on ``server.port``
    setting as configured below. ``2. external`` - ``rasdaman.war``
    is deployed in ``webapps`` dir of external Tomcat. 
    Recommendation: ``embedded``, as there is no dependency on
    external Tomcat server, ``petascope.log`` can be found in ``/opt/rasdaman/log``,
    and ``start/stop`` of petascope is in sync with ``starting/stopping`` rasdaman service.

    - Default: ``embedded``

    - Need to change: **YES** when deploying rasdaman-geo to external tomcat


-   ``server.port`` set port on which ``embedded`` petascope (``java_server=embedded`` above)
    will listen when rasdaman starts. This setting has no effect when ``java_server=external``.

    - Default: ``8080``

    - Need to change: **YES** when port ``8080`` is occupied by another process (e.g. external Tomcat)


Rasdaman
^^^^^^^^

-   ``rasdaman_url`` set URL of the rasdaman database to which rasdaman connects. 
    Normally rasdaman is installed on the same machine, so the bellow needs no changing
    (unless the default ``rasmgr`` port ``7001`` has changed).

    - Default: ``http://localhost:7001``

    - Need to change: **YES** when changed in rasdaman


-   ``rasdaman_database`` set the name of the rasdaman database 
    (configured in ``/opt/rasdaman/etc/rasmgr.conf``).
    Recommendation: use rasdaman standard name, ``RASBASE``

    - Default: ``RASBASE``

    - Need to change: **YES** when changed in rasdaman


-   ``rasdaman_user`` this user is used to map read OGC requests to read-only rasql queries.
    Recommendation: specify a user with *read-only* access rights in rasdaman.

    - Default: ``rasguest``

    - Need to change: **YES** when changed in rasdaman


-   ``rasdaman_pass`` set the password for the rasdaman user above.
    Recommendation: change the default password for ``rasguest`` user
    in rasdaman and update the value here.

    - Default: ``rasguest``

    - Need to change: **YES** when changed in rasdaman    


-   ``rasdaman_admin_user`` this user is used to map updating OGC requests 
    (e.g. during data import, or deleting coverages) to updating rasql queries.
    Additionally, these credentials are used internally for various tasks which require
    admin access rights in rasdaman.

    Generally, this user should be granted the ``admin`` rasdaman role.

    - Default: ``rasadmin``

    - Need to change: **YES** when changed in rasdaman


-   ``rasdaman_admin_pass`` set the password for the rasdaman admin user above.
    Recommendation: change the default password for ``rasadmin`` user
    in rasdaman and update the value here.

    - Default: ``rasadmin``

    - Need to change: **YES** when changed in rasdaman


-   ``rasdaman_retry_attempts`` set the number of re-connect attempts to a rasdaman server
    in case a connection fails.

    - Default: ``5``

    - Need to change: NO


-   ``rasdaman_retry_timeout`` set the wait time in seconds between re-connect attempts
    to a rasdaman server.

    - Default: ``10`` (seconds)

    - Need to change: NO


-   ``rasdaman_bin_path`` set thet path to rasdaman ``bin`` directory.

    - Default: ``/opt/rasdaman/bin``

    - Need to change: **YES** when changed in rasdaman


HTTPS
^^^^^

Used only for ``embedded`` rasdaman-geo deployment.

-   ``security.require-ssl`` allow ``embedded`` petascope to work with HTTPS
    from its endpoint

    - Default: ``false``

    - Need to change: NO


INSPIRE
^^^^^^^

-   ``inspire_common_url`` set the URL to an external catalog service 
    for the ``INSPIRE`` standard, to be provided by the user. 
    If not set then it will be automatically derived from the
    ``petascope_servlet_url`` setting.

    - Default:

    - Need to change: NO


Demo Web
^^^^^^^^

-   ``static_html_dir_path`` Absolute path to a directory containing demo web pages (``html/css/javascript``).
    If set, rasdaman-geo will serve the ``index.html``
    in this directory at its endpoint, e.g. ``http://localhost:8080/rasdaman/``.
    Changes of files in this directory do not require a rasdaman-geo restart.
    The system user running Tomcat (if ``java_server=external``) or rasdaman 
    (if ``java_server=embedded``) must have read permission on this directory.

    - Default:

    - Need to change: **YES** when demo web pages required under radaman-geo's endpoint

Logging
^^^^^^^

rasdaman-geo uses ``log4j`` library version ``1.2.17`` provided by Spring Boot
version ``1.5.2`` to log information/error in ``petascope.log`` file.
See `log4j 1.x document for more details <https://logging.apache.org/log4j/1.2/manual.html>`__.

-   Configuration for petascope logging; by default only level ``INFO`` or higher is
    logged, to both ``file`` and ``stdout``. The valid incremental logging levels are 
    ``TRACE``, ``DEBUG``, ``INFO``, ``WARN``, ``ERROR`` and ``FATAL``.

    .. code-block::

        log4j.rootLogger=INFO, rollingFile

-   Configuration for reducing logs from external libraries: Spring, Hibernate, Liquibase, GRPC and Netty.

    .. code-block::

        log4j.logger.org.springframework=WARN
        log4j.logger.org.hibernate=WARN
        log4j.logger.liquibase=WARN
        log4j.logger.io.grpc=WARN
        log4j.logger.io.netty=WARN
        log4j.logger.org.apache=WARN


-   Configure ``file`` logging. The paths for ``file`` logging
    specified below should be write-accessible by the system user running Tomcat.
    If running embedded Tomcat, then the files should be write accessible by the system user
    running rasdaman.

    .. code-block::

        log4j.appender.rollingFile.layout=org.apache.log4j.PatternLayout  
        log4j.appender.rollingFile.layout.ConversionPattern=%6p [%d{yyyy-MM-dd HH:mm:ss}] %c{1}@%L: %m%n


-   Select one strategy for rolling files and comment out the other. 
    Default it is rolling files by time interval.        

    .. code-block::

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
