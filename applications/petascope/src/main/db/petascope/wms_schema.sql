-- TABLE: **ps_services** =============================================
-- NOTE: petascope.wms.DatabaseConfigAdapter.java#67: --
--	log.warn("Finding service...");	// NB: Only 1 service supported currently!
--	Table_Service serviceInfo = new Table_Service();
--	...
-- Problem in Petascope to load NULL optional parameters: set an `undefined' default
--  like initgeo does for layers.
CREATE FUNCTION create_if_exists_ps_services() RETURNS VOID AS
$$
DECLARE
    -- Log
    ME  constant text := 'create_if_exists_ps_services';
BEGIN
    IF EXISTS (
        SELECT *
        FROM   pg_catalog.pg_tables
        WHERE  schemaname = CURRENT_SCHEMA
        AND    tablename  = quote_ident(cget('TABLE_PS_SERVICES')))
    THEN
        RAISE NOTICE '%: table %.% already exists.', ME, CURRENT_SCHEMA, quote_ident(cget('TABLE_PS_SERVICES'));
    ELSE
	CREATE TABLE ps_services
	(	serviceId	 	SERIAL NOT NULL UNIQUE,
		contactPerson	 	varchar(200) DEFAULT 'undefined',
		contactOrganization  	varchar(200) DEFAULT 'undefined',
		addressType      	varchar(200) DEFAULT 'undefined',
		address          	varchar(200) DEFAULT 'undefined',
		city             	varchar(200) DEFAULT 'undefined',
		stateorprovince  	varchar(200) DEFAULT 'undefined',
		postcode         	varchar(200) DEFAULT 'undefined',
		country          	varchar(200) DEFAULT 'undefined',
		contactvoicetelephone   	varchar(200) DEFAULT 'undefined',
		contactfacsimiletelephone 	varchar(200) DEFAULT 'undefined',
		contactelectronicmailaddress 	varchar(200) DEFAULT 'undefined',
		updateSequence	 	int DEFAULT 0,
		availability	 	int NOT NULL,
		name		 	varchar(200) NOT NULL UNIQUE,
		title		 	varchar(200) NOT NULL,
		abstract	 	varchar(100) DEFAULT 'undefined',
		keywords		varchar(100) DEFAULT 'undefined',
		fees		 	varchar(100) DEFAULT 'undefined',
		accessConstraints	varchar(300) DEFAULT 'undefined',
		hostName	 	varchar(200) NOT NULL,
		port		 	int NOT NULL,
		path		 	varchar(200) NOT NULL,
		formats		 	varchar(200) NOT NULL,
		baseLayerName	 	varchar(200) NOT NULL,
		vendorCapabilities	varchar(200) DEFAULT 'undefined',
		PRIMARY KEY ( serviceId ),
		UNIQUE( hostName, port, path ),
		CHECK( length(hostname) != 0 AND port > 0 AND length(formats) != 0 )
	);
    END IF;
END;
$$
LANGUAGE plpgsql;


-- TABLE: **ps_layers** =============================================
-- Layer of a WMS service
CREATE FUNCTION create_if_exists_ps_layers() RETURNS VOID AS
$$
DECLARE
    -- Log
    ME  constant text := 'create_if_exists_ps_layers';
BEGIN
    IF EXISTS (
        SELECT *
        FROM   pg_catalog.pg_tables
        WHERE  schemaname = CURRENT_SCHEMA
        AND    tablename  = quote_ident(cget('TABLE_PS_LAYERS')))
    THEN
        RAISE NOTICE '%: table %.% already exists.', ME, CURRENT_SCHEMA, quote_ident(cget('TABLE_PS_LAYERS'));
    ELSE
	CREATE TABLE ps_layers
	(	layerId		 SERIAL NOT NULL UNIQUE,
		name		 varchar(200) UNIQUE,
		title		 varchar(200) NOT NULL,
		srs		 varchar(200) NOT NULL,
		authority	 varchar(200),
		latLonBoxXmin	 double precision,
		latLonBoxXmax	 double precision,
		latLonBoxYmin	 double precision,
		latLonBoxYmax	 double precision,
		bBoxXmin	 double precision,
		bBoxXmax	 double precision,
		bBoxYmin	 double precision,
		bBoxYmax	 double precision,
		attributionURL	 varchar(300),
		attributionTitle varchar(200),
		logoWidth	 int,
		logoHeight	 int,
		logoFormat	 varchar(200),
		logoURL		 varchar(300),
		featureURL	 varchar(300),
		resolution	 double precision,
		maptype		 varchar(200),
		PRIMARY KEY ( layerId ),
		CHECK( length(srs) != 0
		   AND logoWidth >= 0 AND logoHeight >= 0
		   AND latLonBoxXmin < latLonBoxXmax AND latLonBoxYmin < latLonBoxYmax
		   AND bBoxXmin < bBoxXmax AND bBoxYmin < bBoxYmax )
	);
    END IF;
END;
$$
LANGUAGE plpgsql;

-- TABLE: **ps_servicelayer** =============================================
-- n:m association table between ps_services and ps_layers
CREATE FUNCTION create_if_exists_ps_servicelayer() RETURNS VOID AS
$$
DECLARE
    -- Log
    ME  constant text := 'create_if_exists_ps_servicelayer';
BEGIN
    IF EXISTS (
        SELECT *
        FROM   pg_catalog.pg_tables
        WHERE  schemaname = CURRENT_SCHEMA
        AND    tablename  = quote_ident(cget('TABLE_PS_SERVICELAYER')))
    THEN
        RAISE NOTICE '%: table %.% already exists.', ME, CURRENT_SCHEMA, quote_ident(cget('TABLE_PS_SERVICELAYER'));
    ELSE
	CREATE TABLE ps_serviceLayer
	(	serviceId        int NOT NULL,
		layerId          int NOT NULL,
		layerSeq         int NOT NULL,
		FOREIGN KEY ( serviceId ) REFERENCES PS_Services,
		FOREIGN KEY ( layerId ) REFERENCES PS_Layers,
		UNIQUE( serviceId, layerId ),
		CHECK( layerSeq >= 0 )
	);
    END IF;
END;
$$
LANGUAGE plpgsql;


-- TABLE: **ps_styles** =============================================
-- A layer can be associated with N styles.
-- Must be compatible with a RGB set, e.g.:
-- Negative filter: name = 'negative', rasqlOp = '{255c,255c,255c} - negative*{1c,1c,1c}'
CREATE FUNCTION create_if_exists_ps_styles() RETURNS VOID AS
$$
DECLARE
    -- Log
    ME  constant text := 'create_if_exists_ps_styles';
BEGIN
    IF EXISTS (
        SELECT *
        FROM   pg_catalog.pg_tables
        WHERE  schemaname = CURRENT_SCHEMA
        AND    tablename  = quote_ident(cget('TABLE_PS_STYLES')))
    THEN
        RAISE NOTICE '%: table %.% already exists.', ME, CURRENT_SCHEMA, quote_ident(cget('TABLE_PS_STYLES'));
    ELSE
	CREATE TABLE ps_styles
	(	styleId		 SERIAL NOT NULL,
		layerId		 int NOT NULL,
		name		 varchar(200) NOT NULL,
		title		 varchar(200) NOT NULL,
		abstract	 varchar( 4096 ),
		legendWidth	 int,
		legendHeight	 int,
		legendURL	 varchar(300),
		sheetURL	 varchar(300),
		rasqlOp		 text,
		PRIMARY KEY ( styleId, layerId ),
		FOREIGN KEY ( layerId ) REFERENCES PS_Layers,
		UNIQUE( layerId, name ),
		CHECK( length(name) != 0
		   AND legendWidth >= 0 AND legendHeight >= 0
		   )
	);
    END IF;
END;
$$
LANGUAGE plpgsql;

-- TABLE: **ps_pyramidlevels** =============================================
-- A WMS layer is usually made up of a pyramid of images
-- One actual collection in Rasdaman for each scale step.
CREATE FUNCTION create_if_exists_ps_pyramidlevels() RETURNS VOID AS
$$
DECLARE
    -- Log
    ME  constant text := 'create_if_exists_ps_pyramidlevels';
BEGIN
    IF EXISTS (
        SELECT *
        FROM   pg_catalog.pg_tables
        WHERE  schemaname = CURRENT_SCHEMA
        AND    tablename  = quote_ident(cget('TABLE_PS_PYRAMIDLEVELS')))
    THEN
        RAISE NOTICE '%: table %.% already exists.', ME, CURRENT_SCHEMA, quote_ident(cget('TABLE_PS_PYRAMIDLEVELS'));
    ELSE
	CREATE TABLE ps_pyramidLevels
	(	levelId		 SERIAL NOT NULL,
		layerId		 int NOT NULL,
		collectionName	 varchar(200) NOT NULL,
		scaleFactor	 float NOT NULL,
		PRIMARY KEY( levelId ),
		FOREIGN KEY ( layerId ) REFERENCES PS_Layers,
		CHECK( scaleFactor >= 1
		   AND length(collectionName) != 0 )
	);
    END IF;
END;
$$
LANGUAGE plpgsql;
