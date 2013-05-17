-- ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=
-- This file is part of rasdaman community.
--
-- Rasdaman community is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- Rasdaman community is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
--
-- Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
-- rasdaman GmbH.
--
-- For more information please see <http://www.rasdaman.org>
-- or contact Peter Baumann via <baumann@rasdaman.com>.
-- ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

-- ####################################################################
-- |                    petascopedb 9.0 schema                        |
-- ####################################################################

-----------------------------------------------------------------------
-- PREFACE
-- ~~~~~~~
-- New database schema for `petascopedb` in order to loose the previously 
-- inherent constraint that forced coverage axes to be aligned with CRS axes.
-- This new schema reflects the GML 3.3 ReferenceableGrid* types so that
-- irregular (spatiotemporal) coverages can be handled by Petascope.
-- 
-- The CRS metadata previously stored in the tables (axes, labels, UoM, etc.) 
-- is now available via resolvable URI, unique for a coverage. The CRS resolver
-- (SECORE) will provide the necessary CRS info to Petascope.
--
-- Structure of this file:
--       i. Shared functions
--      ii. Creation of the SQL schema + triggers
--     iii. Population of some dictionary tables (e.g. formats, MIME types, GML types, etc.)
--
-- Note. RASBASE::nm_meta now merged into petascopedb and renamed to ps9_rasgeo (see #169).
--
-- PL/pgSQL ref.: http://www.commandprompt.com/ppbook/c19610.htm
--                http://postgres.cz/wiki/PL/pgSQL_(en)
--
-- DBMS IMPLEMENTATION CHOICES:
--   :: surrogate keys over natural keys, with label "id"
--   :: <table-name> = <prefix>_<table-label>       (e.g. ps9_coverage)
--   :: <fk-name>    = <table-label>_id             (e.g. coverage_id)
--   :: singular names over plural names for tables (e.g. ps9_coverage, and not ps9_coverages)
--   :: composite names are separated by `_'        (e.g. ps9_domain_set)
-- ~~~~~~~
-- New database schema for `petascopedb` in order to loose the previously 
-----------------------------------------------------------------------

-- ######################################################################### --
--                          i) SHARED FUNCTIONS                              --
-- ######################################################################### --

-- FUNCTION: ** table_exists (table_name) *************************************
-- Checks that a table exists in the current schema.
CREATE OR REPLACE FUNCTION table_exists(text) RETURNS boolean AS $BODY$
    DECLARE
        -- Arguments
        this_table ALIAS FOR $1;
    BEGIN
        -- check if the table exists
        PERFORM * FROM pg_tables WHERE tablename = this_table AND schemaname = current_schema();
        RETURN FOUND;
    END;
$BODY$ LANGUAGE plpgsql;

-- FUNCTION: ** table_has_id(table_name, id_field, id_value) ******************
-- Checks that a certain id is contained in the specified table.
-- This function is used when a table imports FKs from different tables.
CREATE OR REPLACE FUNCTION table_has_id(text, text, integer) RETURNS boolean AS $BODY$
    DECLARE
        -- Arguments
        this_table ALIAS FOR $1;
        id_field   ALIAS FOR $2;
        id_value   ALIAS FOR $3;

        -- Local variables
	ME text := current_query();
        qry text;
        tup record;
    BEGIN
        -- check if the table exists
        IF NOT table_exists(this_table) THEN
            RAISE EXCEPTION '%: table ''%'' does not exist.', ME, this_table;
        END IF;

        -- check for referential integrity
	qry := 'SELECT * FROM ' || this_table || ' WHERE ' || id_field || '=' || id_value || ';';
	-- RAISE NOTICE 'Executing: %', qry; --Debug
	FOR tup IN EXECUTE qry LOOP -- EXECUTE does not affect FOUND
            RETURN true; -- the tuple has been found
        END LOOP;
        RETURN false; -- no tuple with specified id
    END;
$BODY$ LANGUAGE plpgsql;

-- FUNCTION: ** matches_pattern(string, pattern) ******************************
-- Returns true if the pattern matches the specified string
CREATE OR REPLACE FUNCTION matches_pattern(text, text) RETURNS boolean AS $BODY$
    DECLARE
        -- Arguments
	this_string  ALIAS FOR $1;
	this_pattern ALIAS FOR $2;
    BEGIN
        -- check that the UoM code is like <pattern value="^[^: \n\r\t]+"/>
        PERFORM regexp_matches(this_string, this_pattern);
        RETURN FOUND;
    END;
$BODY$ LANGUAGE plpgsql;

-- FUNCTION: ** numeric_column2array (table, numeric_column, where_clause, order_by_column) ****
-- Builds an array from a query response with single numeric values
CREATE OR REPLACE FUNCTION numeric_column2array(text, text, text, text) RETURNS numeric[] AS $BODY$
    DECLARE
        -- Arguments
        this_table     ALIAS FOR $1;
        numeric_column ALIAS FOR $2;
        where_clause   ALIAS FOR $3;
        orderby_column ALIAS FOR $4;

        -- Local variables
        ME text := current_query();
        qry       text;    
        tup       record;
        out_array numeric[];
    BEGIN
        qry := 'SELECT ARRAY[CAST(' || numeric_column || ' AS numeric)] AS a FROM ' || this_table || ' ' 
               || where_clause      || ' ORDER BY '   || orderby_column || ';';
	--RAISE NOTICE 'Executing: %', qry; --Debug
	FOR tup IN EXECUTE qry LOOP 
            -- Extract the numeric column value
            out_array := array_append(out_array, tup.a[1]);
        END LOOP;
        RETURN out_array;
    END;
$BODY$ LANGUAGE plpgsql;

-- FUNCTION: ** array_sort (anyarray) ***********************
-- Sorts an array (usage: SELECT sort_array('{3,2,1}'::integer[]);)
CREATE OR REPLACE FUNCTION array_sort(anyarray) RETURNS anyarray LANGUAGE SQL AS $$
    SELECT ARRAY(
      SELECT $1[s.i] AS "foo"
      FROM
          generate_series(array_lower($1,1), array_upper($1,1)) AS s(i)
      ORDER BY foo
    );
$$;


-- FUNCTION: ** is_sequential (array, origin) ***********************
-- Verify that an array is strictly sequential from an origin index.
CREATE OR REPLACE FUNCTION is_sequential(integer[], integer = NULL) RETURNS boolean AS $BODY$
    DECLARE
        -- Arguments and local variables
        indexes_array  ALIAS FOR $1;
        indexes_origin ALIAS FOR $2;
        position integer := array_lower(indexes_array, 1); -- usually 1 is the starting index for arrays in psql
    BEGIN
        IF array_length(indexes_array, 1) > 0 THEN
            -- check first element is origin
            IF indexes_origin IS NOT NULL AND indexes_array[position] <> indexes_origin THEN
                RETURN false;
            ELSE 
                -- check for sequential ordering
                WHILE (position+1) <= array_length(indexes_array, 1) LOOP
                    IF indexes_array[position+1] <> indexes_array[position]+1 THEN
                        RETURN false;
                    END IF;
                    position := position + 1;
                END LOOP;
            END IF;
        END IF;
        RETURN true; -- 0-element arrays as well are considered sequential
    END;
$BODY$ LANGUAGE plpgsql;

-- FUNCTION: ** is_increasing (array, is_strict) *******************************
-- Verify that an array is strictly sequential from 0.
CREATE OR REPLACE FUNCTION is_increasing(numeric[], boolean) RETURNS boolean AS $BODY$
    DECLARE
        -- Arguments and local variables
        numeric_array ALIAS FOR $1;
        is_strict     ALIAS FOR $2;
        position integer := array_lower(numeric_array, 1);
    BEGIN
        IF array_length(numeric_array, 1) > 0 THEN
            -- check for sequential ordering
            WHILE (position+1) <= array_length(numeric_array, 1) LOOP
                IF      (is_strict  AND numeric_array[position+1] <= numeric_array[position]) OR
                   ((NOT is_strict) AND numeric_array[position+1] <  numeric_array[position]) THEN
                    RETURN false;
                END IF;
                position := position + 1;
            END LOOP;
        END IF;
        RETURN true; -- 0-element arrays as well are returning true
    END;
$BODY$ LANGUAGE plpgsql;

-- FUNCTION: ** table_is_empty (table) ****************************************
-- Returns true if a table is empty.
CREATE OR REPLACE FUNCTION table_is_empty(text) RETURNS boolean AS $BODY$
    DECLARE
        -- Arguments
        this_table ALIAS FOR $1;

        -- Local variables
        ME text := current_query();
        qry   text;
        tup   record;
    BEGIN
        -- check if the table exists
        IF NOT table_exists(this_table) THEN
            RAISE EXCEPTION '%: table ''%'' does not exist.', ME, this_table;
        END IF;

        -- check that it is empty
        qry := 'SELECT * FROM ' || this_table || ';';
	FOR tup IN EXECUTE qry LOOP -- EXECUTE does not affect FOUND
            RETURN false; -- the tuple has been found
        END LOOP;
        RETURN true; -- table is empty
    END;
$BODY$ LANGUAGE plpgsql;

-- ######################################################################### --
--                        ii) SCHEMA AND TRIGGERS                            --
-- ######################################################################### --

-- COVERAGE MODEL (WCS/WCPS) --------------------------------------------------
-------------------------------------------------------------------------------

-- TABLE: **ps9_gml_type** =====================================================
-- Catalogue table storing the available GML grid types.
CREATE TABLE ps9_gml_subtype (
    id             serial PRIMARY KEY,
    subtype        text   UNIQUE NOT NULL,
    subtype_parent int    REFERENCES ps9_gml_subtype (id)
);

-- TABLES: **ps9_format/ps9_mime_type/ps9_gdal_id** ==============================
-- These tables describe the encoding formats known to WCPS, as well as their mappings to mimetypes. 
-- If you add any, make sure that rasdaman can encode in the 
-- format specified by `name', or encoding to that format will not work.
-- Note: format names and MIME types can have aliases:
-- {1 MIME -> N formats} (e.g. tif/tiff) // {1 MIME -> N gdal_id} (JPEG/JPEGLS).
CREATE TABLE ps9_mime_type (
    id              serial           PRIMARY KEY,
    mime_type       varchar(255)     NOT NULL         -- http://tools.ietf.org/html/rfc4288#section-4.2
);
CREATE TABLE ps9_gdal_format (
    id              serial           PRIMARY KEY,
    gdal_id         text             UNIQUE NOT NULL,
    description     text             NULL
);
CREATE TABLE ps9_format (
    id              serial       PRIMARY KEY,
    name            text         UNIQUE NOT NULL,
    mime_type_id    integer      NOT NULL,
    gdal_id         integer      NULL, 
    -- Constraints and FKs
    FOREIGN KEY (mime_type_id) REFERENCES ps9_mime_type   (id) ON DELETE RESTRICT,
    FOREIGN KEY (gdal_id)      REFERENCES ps9_gdal_format (id) ON DELETE RESTRICT
);

-- TABLE: **ps9_coverage** =====================================================
-- The core of the coverage model. It is composed, as per GML model, by 
-- a domain (the topology), and a range (the payload).
CREATE TABLE ps9_coverage (
    id               serial       PRIMARY KEY,
    name             text         UNIQUE NOT NULL,
    gml_type_id      integer      NOT NULL,
    native_format_id integer      NOT NULL,
    -- Constraints and FKs
    FOREIGN KEY (gml_type_id)      REFERENCES ps9_gml_subtype  (id) ON DELETE RESTRICT,
    FOREIGN KEY (native_format_id) REFERENCES ps9_mime_type (id) ON DELETE RESTRICT
);
-- TRIGGER: **coverage_name_trigger********************************************
-- A coverage name must start with a char and must not contain colons: [\i-[:]][\c-[:]]*
--   \i matches any character that may be the first character of an XML name, i.e. [_:A-Za-z]
--   \c matches any character that may occur after the first character in an XML name, i.e. [-._:A-Za-z0-9]
-- (http://www.schemacentral.com/sc/xsd/t-xsd_NCName.html).
CREATE OR REPLACE FUNCTION coverage_name_pattern() RETURNS trigger AS $BODY$
    DECLARE 
        -- Constants
	NAME_PATTERN  constant text := E'^[_A-Za-z][-._A-Za-z0-9]*$';

        -- Local variables
	ME text := current_query();
        matches integer;
    BEGIN
        -- check that the coverage name ~ [\i-[:]][\c-[:]]*
        IF NOT matches_pattern(NEW.name, NAME_PATTERN) THEN
            RAISE EXCEPTION '%: ''%'' does not follow a valid naming pattern (%).', ME, NEW.name, NAME_PATTERN;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER coverage_name_trigger BEFORE INSERT OR UPDATE ON ps9_coverage
       FOR EACH ROW EXECUTE PROCEDURE coverage_name_pattern();

-- TABLE: **ps9_rasdaman_collection** ==========================================
-- This table collects all the rasdaman collections, which are then referenced in ps9_range_set.
CREATE TABLE ps9_rasdaman_collection (
    id          serial       PRIMARY KEY,
    name      	varchar(254) NOT NULL,  -- RASBASE::ras_mddcollnames.mddcollname
    oid         numeric(20)  NOT NULL,  -- bigint is 8 bytes signed int: OID is 
    base_type   text         NULL,	-- ex RASBASE::nm_meta.pixel_type
    -- Constraints and FKs
    UNIQUE (name, oid),
    CHECK(oid >= 0 AND oid < 2.0^64)
);

-- TABLE: **ps9_range_set** ====================================================
-- This table links the coverage to the either internal (PostgreSQL) or 
-- external (e.g. rasdaman) storage of the range values.
CREATE TABLE ps9_range_set (
    id            serial    PRIMARY KEY,
    coverage_id   integer   UNIQUE NOT NULL,
    storage_table text      NOT NULL DEFAULT 'ps9_rasdaman_collection', -- instead of managing an enum of types, use table names (see trigger)
    storage_id    integer   NOT NULL,
    -- Constraints and FKs
    UNIQUE (storage_table, storage_id),
    FOREIGN KEY (coverage_id) REFERENCES ps9_coverage (id) ON DELETE CASCADE
);
-- TRIGGER: **storage_type_trigger*********************************************
-- Check referencial integrity on multiple references tables
CREATE OR REPLACE FUNCTION storage_ref_integrity() RETURNS trigger AS $BODY$
    DECLARE
        -- constants
        ID_FIELD constant text := 'id';
        -- Local variables
	ME text := current_query();
    BEGIN
        -- check for referential integrity
        IF NOT table_has_id(NEW.storage_table, ID_FIELD, NEW.storage_id) THEN
            RAISE EXCEPTION '%: invalid reference to ''%'', no row has id ''%''.', ME, NEW.storage_table, NEW.storage_id;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER storage_integrity_trigger BEFORE INSERT OR UPDATE ON ps9_range_set
       FOR EACH ROW EXECUTE PROCEDURE storage_ref_integrity();

-- TABLE: **ps9_range_type_component** =========================================
-- Catalogue for range types.
CREATE TABLE ps9_range_type_component (
    id               serial     PRIMARY KEY,
    coverage_id      integer    NOT NULL,
    component_order  integer    NOT NULL,
    field_id         integer    NOT NULL,
    field_table      text       DEFAULT 'ps9_quantity',
    -- Constraints and FKs
    CHECK (component_order >= 0),
    UNIQUE (coverage_id, component_order),
    UNIQUE (coverage_id, field_id, field_table),
    FOREIGN KEY (coverage_id) REFERENCES ps9_coverage (id) ON DELETE CASCADE
);
-- TRIGGER: **field_type_trigger***********************************************
-- Check referencial integrity on multiple references tables
CREATE OR REPLACE FUNCTION field_ref_integrity() RETURNS trigger AS $BODY$
    DECLARE
        -- Constants
        ID_FIELD constant text := 'id';
        -- Local variables
	ME text := current_query();
    BEGIN
        -- check for referential integrity
        IF NOT table_has_id(NEW.field_table, ID_FIELD, NEW.field_id) THEN
            RAISE EXCEPTION '%: invalid reference to ''%'', no row has % ''%''.', ME, NEW.field_table, ID_FIELD, NEW.field_id;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER field_integrity_trigger BEFORE INSERT OR UPDATE ON ps9_range_type_component
       FOR EACH ROW EXECUTE PROCEDURE field_ref_integrity();
-- TRIGGER: **range_component_order_trigger************************************
-- Checks that the components inserted for this coverage by now follow a sequential order from 0.
CREATE OR REPLACE FUNCTION range_component_order_integrity() RETURNS trigger AS $BODY$
    DECLARE
        -- Constants
        TABLE_RANGE_TYPE_COMP          constant text := 'ps9_range_type_component';
          RANGE_TYPE_COMP_COVERAGE_ID  constant text := 'coverage_id';
          RANGE_TYPE_COMP_ORDER        constant text := 'component_order';
        -- Local variables
	ME text := current_query();
    BEGIN
        -- check the component orders of this coverage are in a sequence from 0
        IF NOT (SELECT is_sequential((
              numeric_column2array(
                TABLE_RANGE_TYPE_COMP,   -- table
                RANGE_TYPE_COMP_ORDER,   -- numeric column to check
                ' WHERE ' || RANGE_TYPE_COMP_COVERAGE_ID || ' = ' || NEW.coverage_id, -- select /this/ coverage
                RANGE_TYPE_COMP_ORDER    -- order-by column
              )::integer[]), 0)) -- the integer sequence must start from 0
            THEN RAISE EXCEPTION '%: last inserted component order (%) is not valid. ''%'' must be in a ordered sequence starting from 0.', 
                            ME, NEW.component_order, RANGE_TYPE_COMP_ORDER;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER range_component_order_trigger AFTER INSERT OR UPDATE ON ps9_range_type_component
       FOR EACH ROW EXECUTE PROCEDURE range_component_order_integrity();

-- TABLE: **ps9_uom** ==========================================================
-- Catalogue table with Unit Of Measures (UoMs) for the rangeSet of a coverage.
-- Note: pure numbers should have a UoM code = 10⁰ [http://unitsofmeasure.org/ucum.html#section-Derived-Unit-Atoms, Tab.3]
CREATE TABLE ps9_uom (
    id      serial      PRIMARY KEY,
    code    text        UNIQUE NOT NULL  -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:uom[@code]a
);
-- TRIGGER: **uom_code_trigger*************************************************
-- This type specifies a character string of length at least one, and restricted 
-- such that it must not contain any of the following characters: ":" (colon), " " (space), 
-- (newline), (carriage return), (tab). This allows values corresponding to familiar 
-- abbreviations, such as "kg", "m/s", etc. It is also required that the symbol be an 
-- identifier for a unit of measure as specified in the "Unified Code of Units of Measure" 
-- (UCUM) (http://aurora.regenstrief.org/UCUM). [From http://schemas.opengis.net/sweCommon/2.0/basic_types.xsd - "UomSymbol"]
CREATE OR REPLACE FUNCTION uom_code_pattern() RETURNS trigger AS $BODY$
    DECLARE
        -- Constants
	UOM_PATTERN   constant text := E'^[^: \\n\\r\\t]+$';
 
        -- Local variables
	ME text := current_query();
        matches integer;
    BEGIN
        -- check that the UoM code is like <pattern value="[^: \n\r\t]+"/>
        IF NOT matches_pattern(NEW.code, UOM_PATTERN) THEN
            RAISE EXCEPTION '%: ''%'' does not follow a valid UoM pattern (%).', ME, NEW.code, UOM_PATTERN;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER uom_code_trigger BEFORE INSERT OR UPDATE ON ps9_uom
       FOR EACH ROW EXECUTE PROCEDURE uom_code_pattern();

-- TABLE: **ps9_interval** =====================================================
-- Allowed interval values for `quantity' and `count' field types.
CREATE TABLE ps9_interval (
    id       serial      PRIMARY KEY,
    min      numeric     NOT NULL,
    max      numeric     NOT NULL,
    -- Constraints and FKs
    UNIQUE (min, max)
);

-- TABLE: **ps9_quantity** =====================================================
-- Independent collection of continuous quantities
CREATE TABLE ps9_quantity (
    id                  serial      PRIMARY KEY,
    name                text        NOT NULL,  -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:name
    uom_id              integer     NOT NULL,  -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:uom
    description         text        NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:description
    definition_uri      text        NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity[@definition]
    significant_figures integer     NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/swe:AllowedValues/significantFigures
    -- Constraints and FKs
    UNIQUE (name)
);

-- TABLE: **ps9_interval_quantity** ============================================
-- n:m Association table between ps9_quantity and ps9_interval: a quantity can 
-- be constraint by multiple allowed intervals, which exist independently.
CREATE TABLE ps9_interval_quantity (
    quantity_id     integer,
    interval_id     integer,
    -- Constraints and FKs
    PRIMARY KEY (quantity_id, interval_id),
    FOREIGN KEY (quantity_id) REFERENCES ps9_quantity (id) ON DELETE RESTRICT,
    FOREIGN KEY (interval_id) REFERENCES ps9_interval (id) ON DELETE RESTRICT
);

-- TABLE: **ps9_crs** ==========================================================
-- Catalogue table storing CRS URIs.
CREATE TABLE ps9_crs (
    id     serial    PRIMARY KEY,
    uri    text      UNIQUE NOT NULL -- either single or compound CRSs go in a single URI
);

-- TABLE: **ps9_domain_set** ===================================================
-- Table which stores shared information on the geometry of a coverage,
-- independently of its type.
CREATE TABLE ps9_domain_set (
    coverage_id     integer    PRIMARY KEY,
    native_crs_id   integer[]  NOT NULL,   -- compound CRSs to be stored as array of FKs
    -- Constraints and FKs
    CONSTRAINT native_crs_is_1D CHECK (array_lower(native_crs_id, 2) IS NULL), 
    FOREIGN KEY (coverage_id)   REFERENCES ps9_coverage (id) ON DELETE CASCADE
);
-- TRIGGER: **crs_integrity*****************************************************
-- Check referencial integrity on ps_crs.
CREATE OR REPLACE FUNCTION crs_ref_integrity() RETURNS trigger AS $BODY$
    DECLARE
        -- constants
        TABLE_CRS constant text := 'ps9_crs';
           CRS_ID constant text := 'id';
        -- Local variables
	ME text := current_query();
        i integer;
    BEGIN
        -- check for referential integrity
        FOR i IN array_lower(NEW.native_crs_id, 1)..array_upper(NEW.native_crs_id, 1) LOOP
            IF NOT table_has_id(TABLE_CRS, CRS_ID, NEW.native_crs_id[i]) THEN
                RAISE EXCEPTION '%: invalid reference to ''%'', no row has id ''%''.', ME, TABLE_CRS, NEW.native_crs_id[i];
            END IF;
        END LOOP;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER crs_integrity BEFORE INSERT OR UPDATE ON ps9_domain_set
       FOR EACH ROW EXECUTE PROCEDURE crs_ref_integrity();
-- TRIGGER: **crs_compound_integrity***********************************************
-- Check referencial integrity on ps_crs.
CREATE OR REPLACE FUNCTION crs_compound_components() RETURNS trigger AS $BODY$
    DECLARE
        -- Local variables
	ME text := current_query();
        i integer;
    BEGIN
        -- check that there are no duplicated in the CRS compound
        IF NOT (SELECT is_increasing((array_sort(NEW.native_crs_id), true)))
            THEN RAISE EXCEPTION '%: invalid compound CRS (%). The single composing CRSs must be unique.', 
                            ME, NEW.native_crs_id;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER crs_compound_integrity BEFORE INSERT OR UPDATE ON ps9_domain_set
       FOR EACH ROW EXECUTE PROCEDURE crs_compound_components();



-- TABLE: **ps9_gridded_domain_set** ===========================================
-- Separating metadata shared by /any/ coverage model (multi* coverages, grids, etc.)
-- from type-specific metadata, *GridCoverage-specific in this case.
-- The geometry (domainSet) tables will refer to this one, and not to ps9_coverage.
CREATE TABLE ps9_gridded_domain_set (
    coverage_id     integer    PRIMARY KEY,
    grid_origin     numeric[]  NOT NULL,
    -- Constraints and FKs
    CONSTRAINT origin_lower_bound_is_1 CHECK (array_lower(grid_origin, 1) = 1),     -- Indexing starts from 1
    CONSTRAINT origin_is_1D            CHECK (array_lower(grid_origin, 2) IS NULL), -- Cannot have more than 1 tuple of coordinates 
    FOREIGN KEY (coverage_id) REFERENCES ps9_domain_set (coverage_id) ON DELETE CASCADE
);
-- TRIGGER: **unique_domain_subtype_trigger************************************
-- Check here if already an other ps9_coverage.id has been used by other tables? (e.g. Multipoint). ?
-- ...

-- TABLE: **ps9_grid_axis** ====================================================
-- Table representing information regarding each axis of the gridded coverage.
-- Depending on how this table is filled, it can represent a regular/irregular 
-- rectilinear/curvilinear grid axis.
CREATE TABLE ps9_grid_axis (
    id                  serial      PRIMARY KEY,
    gridded_coverage_id integer     NOT NULL,
    rasdaman_order      integer     NOT NULL,
    -- Constraints and FKs
    CHECK (rasdaman_order >= 0),
    FOREIGN KEY (gridded_coverage_id) REFERENCES ps9_gridded_domain_set (coverage_id) ON DELETE CASCADE
);
-- TRIGGER: **rasdaman_axis_order_trigger**************************************
-- Checks that the rasdaman orders inserted for this coverage by now, follow a sequential order from 0.
CREATE OR REPLACE FUNCTION rasdaman_axis_order_integrity() RETURNS trigger AS $BODY$
    DECLARE
        -- Constants
        TABLE_GRID_AXIS             constant text := 'ps9_grid_axis';
          GRID_AXIS_COVERAGE_ID     constant text := 'gridded_coverage_id';
          GRID_AXIS_RASDAMAN_ORDER  constant text := 'rasdaman_order';

        -- Local variables
	ME text := current_query();
    BEGIN
        -- check the rasdaman orders of this coverage are in a sequence from 0
        IF NOT (SELECT is_sequential((
              numeric_column2array(
                TABLE_GRID_AXIS,          -- table
                GRID_AXIS_RASDAMAN_ORDER, -- numeric column to check
                ' WHERE ' || GRID_AXIS_COVERAGE_ID || ' = ' || NEW.gridded_coverage_id, -- select /this/ coverage
                GRID_AXIS_RASDAMAN_ORDER  -- order-by column
              )::integer[]), 0)) -- the integer sequence must start from 0
            THEN RAISE EXCEPTION '%: last inserted rasdaman order (%) is not valid. ''%'' must be in a ordered sequence starting from 0.', 
                            ME, NEW.rasdaman_order, GRID_AXIS_RASDAMAN_ORDER;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER rasdaman_axis_order_trigger AFTER INSERT OR UPDATE ON ps9_grid_axis
       FOR EACH ROW EXECUTE PROCEDURE rasdaman_axis_order_integrity();

-- TABLE: **ps9_rectilinear_axis** =============================================
-- This table represents a rectilinear (not curvilinear) axis of a gridded coverage.
-- Depending on how this table is filled, it can represent either a regular or an irregular 
-- rectilinear axis.
-- [In PSQL an array of N elements starts with array[1] and ends with array[N]]
CREATE TABLE ps9_rectilinear_axis (
    grid_axis_id       integer    PRIMARY KEY,
    offset_vector      numeric[]  NOT NULL, -- directional resolution (point location relative to the origin)
    -- Constraints and FKs
    FOREIGN KEY (grid_axis_id) REFERENCES ps9_grid_axis (id) ON DELETE CASCADE,
    CONSTRAINT offsetvector_lower_bound_is_1 CHECK (array_lower(offset_vector, 1) = 1),    -- Indexing starts from 1
    CONSTRAINT offsetvector_is_1D            CHECK (array_lower(offset_vector, 2) IS NULL) -- Cannot have more than 1 tuple of coordinates 
);

-- TRIGGER: **offsetvector_origin_trigger**************************************
-- This trigger checks coherence of dimensions of the referenced offset vector with
-- the origin of the coverage.
CREATE OR REPLACE FUNCTION offset_vector_coherence() RETURNS trigger AS $BODY$
    DECLARE 
        -- Constants
        TABLE_GRIDDED_DOMAIN_SET       constant text := 'ps9_gridded_domain_set';
          GRIDDED_DOMAIN_SET_ID        constant text := 'coverage_id';
          GRIDDED_DOMAIN_SET_ORIGIN    constant text := 'grid_origin';
        TABLE_GRID_AXIS            constant text := 'ps9_grid_axis';
          GRID_AXIS_ID             constant text := 'id';
          GRID_AXIS_COVERAGE_ID    constant text := 'gridded_coverage_id';

        -- Local variables
	ME text := current_query();
        grid_origin numeric[];
        qry text;
    BEGIN
        -- Check if origin has been inserted
	qry :=     'SELECT ' || TABLE_GRIDDED_DOMAIN_SET || '.' || GRIDDED_DOMAIN_SET_ORIGIN
		||  ' FROM ' || TABLE_GRIDDED_DOMAIN_SET || ',' || TABLE_GRID_AXIS
                || ' WHERE ' || TABLE_GRIDDED_DOMAIN_SET || '.' || GRIDDED_DOMAIN_SET_ID 
                ||       '=' || TABLE_GRID_AXIS   || '.' || GRID_AXIS_COVERAGE_ID
                ||   ' AND ' || TABLE_GRID_AXIS   || '.' || GRID_AXIS_ID || '=' || NEW.grid_axis_id || ';';
	--RAISE NOTICE '%: EXECUTE %', ME, qry; -- Debug
	EXECUTE qry INTO STRICT grid_origin;  -- error if not exactly one row is returned
        IF grid_origin IS NULL THEN
            RAISE EXCEPTION '%: Trying to set a reference vector for a coverage without origin.', ME;
        ELSE
            -- Check if the dimensions of origin and offset vector match (lower bound is in CHECK contraint already, for both)
            IF array_upper(NEW.offset_vector, 1) <> array_upper(grid_origin, 1) THEN
                RAISE EXCEPTION '%: offset vector (%) is not compatible with coverage origin (%)', ME, NEW.offset_vector, grid_origin;
            END IF;
	END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER offsetvector_origin_trigger BEFORE INSERT OR UPDATE ON ps9_rectilinear_axis
       FOR EACH ROW EXECUTE PROCEDURE offset_vector_coherence();

-- TABLE: **ps9_vector_coefficients** ==========================================
-- Irregularly-spaced rectilinear axis will be represented ``by vectors'', 
-- and the coefficients of each grid point along this axis are stored in this table.
CREATE TABLE ps9_vector_coefficients (
    grid_axis_id      integer    NOT NULL,
    coefficient       numeric    NOT NULL,
    coefficient_order integer    NOT NULL,
    -- Constraints and FKs
    UNIQUE (grid_axis_id, coefficient),
    CHECK (coefficient_order >= 0),
    FOREIGN KEY (grid_axis_id) REFERENCES ps9_rectilinear_axis (grid_axis_id) ON DELETE CASCADE
);
-- INDEX on multiple columns for query: ***************************************
-- ``SELECT coefficient_order FROM ps9_vector_coefficients WHERE grid_axis_id=<id> AND coefficient <|> <value>''
CREATE INDEX coefficients_idx ON ps9_vector_coefficients (grid_axis_id, coefficient);

-- TRIGGER: **coefficients_integrity_trigger***********************************
-- Checks that the coefficients are ordered (0,1,2,...) and their values are strictly increasing.
-- TRIGGER: **coefficients_integrity_trigger***********************************
-- Checks that the coefficients are ordered (0,1,2,...) and their values are strictly increasing.
CREATE OR REPLACE FUNCTION coefficients_integrity() RETURNS trigger AS $BODY$
    DECLARE
        -- Constants
        TABLE_VECTOR_COEFFICIENTS    constant text := 'ps9_vector_coefficients';
          VECTOR_COEFFICIENTS_ID     constant text := 'grid_axis_id';
          VECTOR_COEFFICIENTS_VALUE  constant text := 'coefficient';
          VECTOR_COEFFICIENTS_ORDER  constant text := 'coefficient_order';
        -- Local variables
        ME text := current_query();
    BEGIN
        -- check the component orders of this coverage are in a sequence from 0
        IF NOT (SELECT is_sequential((
              numeric_column2array(
                TABLE_VECTOR_COEFFICIENTS,   -- table
                VECTOR_COEFFICIENTS_ORDER,   -- numeric column to check
                ' WHERE ' || VECTOR_COEFFICIENTS_ID || ' = ' || NEW.grid_axis_id, -- select /this/ axis
                VECTOR_COEFFICIENTS_ORDER    -- order-by column
              )::integer[]), 0)) -- the integer sequence must start from 
            THEN RAISE EXCEPTION '%: last inserted coefficient order (%) is not valid. ''%'' must be in a ordered sequence starting from 0.', 
                                 ME, NEW.coefficient_order, VECTOR_COEFFICIENTS_ORDER;
        END IF; 

        -- check that the coefficients are *strictly* increasing
        IF NOT (SELECT is_increasing((
            SELECT numeric_column2array(
                TABLE_VECTOR_COEFFICIENTS,   -- table
                VECTOR_COEFFICIENTS_VALUE,   -- numeric column to check
                ' WHERE ' || VECTOR_COEFFICIENTS_ID || ' = ' || NEW.grid_axis_id, -- select /this/ axis
                VECTOR_COEFFICIENTS_ORDER    -- order-by column
            )), true)) -- is_strict
            THEN RAISE EXCEPTION '%: last inserted coefficient (%) is not valid. ''%'' must be in a strictly increasing sequence.', 
                                 ME, NEW.coefficient, VECTOR_COEFFICIENTS_VALUE;
        END IF; 
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER coefficients_integrity_trigger AFTER INSERT OR UPDATE ON ps9_vector_coefficients
       FOR EACH ROW EXECUTE PROCEDURE coefficients_integrity();


-- TABLE: **ps9_extra_metadata_type** ==========================================
-- Catalogue table of metadata types for a coverage.
CREATE TABLE ps9_extra_metadata_type (
    id     serial      PRIMARY KEY,
    type   text        UNIQUE NOT NULL
);

-- TABLE: **ps9_extra_metadata** ===============================================
-- Additional (optional) metadata that can be specified for a coverage.
-- For extendability, a catalogue table of metadata types is referenced.
CREATE TABLE ps9_extra_metadata (
    id               serial      PRIMARY KEY,
    coverage_id      integer     NOT NULL,
    metadata_type_id integer     NOT NULL,
    value            text        NOT NULL,
    -- Constraints and FKs
    UNIQUE (coverage_id, metadata_type_id),
    FOREIGN KEY (coverage_id)      REFERENCES ps9_coverage            (id) ON DELETE CASCADE,
    FOREIGN KEY (metadata_type_id) REFERENCES ps9_extra_metadata_type (id) ON DELETE RESTRICT
);

-- TABLE: **ps9_service_identification** =======================================
-- Metadata for the WCS service (/wcs:Capabilities/ows:ServiceIdentification)
CREATE TABLE ps9_service_identification (
    id         serial    PRIMARY KEY,
    title      text      NULL,
    abstract   text      NULL
);
-- TRIGGER: **single_service_trigger*******************************************
-- Checks that no second service is inserted.
CREATE OR REPLACE FUNCTION single_service() RETURNS trigger AS $BODY$
    DECLARE
        -- Constants
        TABLE_SERVICE_IDENTIFICATION constant text := 'ps9_service_identification';

        -- Local variables
	ME text := current_query();
    BEGIN
        -- check there is no other service there
        IF NOT (SELECT table_is_empty(TABLE_SERVICE_IDENTIFICATION)) THEN
            RAISE EXCEPTION '%: cannot insert more than one WCS service.''', ME;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER single_service_trigger BEFORE INSERT ON ps9_service_identification
       FOR EACH ROW EXECUTE PROCEDURE single_service();

-- TABLE: **ps9_keywords** =====================================================
-- Keywords for the WCS service identification (/wcs:Capabilities/ows:ServiceIdentification/ows:keywords)
CREATE TABLE ps9_keyword (
    id             serial    PRIMARY KEY,
    value          text      NOT NULL,
    type           text      NULL,
    type_codespace text      NULL,
    -- Constraints and FKs
    UNIQUE (value, type, type_codespace)
);

-- TABLE: **ps9_service_keyword** ==============================================
-- n:m Association table between ps9_service_identification and ps9_keyword, although now only one service is allowed.
CREATE TABLE ps9_service_keyword (
    service_id     integer,
    keyword_id     integer,
    -- Constraints and FKs
    PRIMARY KEY (service_id, keyword_id),
    FOREIGN KEY (service_id) REFERENCES ps9_service_identification (id) ON DELETE CASCADE,
    FOREIGN KEY (keyword_id) REFERENCES ps9_keyword                (id) ON DELETE RESTRICT
);

-- TABLE: **ps9_service_provider** =============================================
-- Metadata for the WCS service provider (/wcs:Capabilities/ows:ServiceProvider)
CREATE TABLE ps9_service_provider (
    id          serial PRIMARY KEY,
    name        text     NOT NULL,   -- //ows:ServiceProvider/ows:ProviderName
    site        text     NULL,       -- //ows:ServiceProvider/ows:ProviderSite[@xlink:href]
    contact_individual_name     text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:IndividualName
    contact_position_name       text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:PositionName
    contact_phone               text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone
    contact_delivery_point      text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:DeliveryPoint
    contact_city                text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:City
    contact_administrative_area text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:AdministrativeArea
    contact_postal_code         text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:PostalCode
    contact_country             text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:Country
    contact_email_address       text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:ElectronicMailAddress
    contact_hours_of_service    text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:HoursOfService
    contact_instructions        text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:ContactInstructions
    contact_role                text NULL  -- //ows:ServiceProvider/ows:ServiceContact/ows:Role
);
-- TRIGGER: **single_service_provider_trigger**********************************
-- Checks that no second service is inserted.
CREATE OR REPLACE FUNCTION single_service_provider() RETURNS trigger AS $BODY$
    DECLARE
        -- Constants
        TABLE_SERVICE_PROVIDER constant text := 'ps9_service_provider';

        -- Local variables
	ME text := current_query();
    BEGIN
        -- check there is no other service there
        IF NOT (SELECT table_is_empty(TABLE_SERVICE_PROVIDER)) THEN
            RAISE EXCEPTION '%: cannot insert more than one WCS service provider.''', ME;
        END IF;
        RETURN NEW;
    END;
$BODY$ LANGUAGE plpgsql;
CREATE TRIGGER single_service_provider_trigger BEFORE INSERT ON ps9_service_provider
       FOR EACH ROW EXECUTE PROCEDURE single_service_provider();

-- MAP MODEL (WMS) ------------------------------------------------------------
-- ...

-- ######################################################################### --
--                         iii) SCHEMA POPULATION                            --
-- ######################################################################### --

-- Default service and service provider metadata:
INSERT INTO ps9_service_identification (title, abstract) VALUES ('rasdaman', 'rasdaman server - free download from www.rasdaman.org');
INSERT INTO ps9_service_provider
       (name, site, contact_individual_name, contact_city, contact_postal_code, contact_country, contact_email_address, contact_role) 
       VALUES ('Jacobs University Bremen', 'http://www.petascope.org/', 'Prof. Dr. Peter Baumann', 
               'Bremen', '28717', 'Germany', 'p.baumann@jacobs-university.de', 'Project Leader');

-- CRS for ANSI-Date (rasdaman 8.4 legacy)
-- TODO: init9.sql.in to replace with configure %SECORE% host
INSERT INTO ps9_crs (uri) VALUES ('http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0.1/ANSI-Date');

-- GML coverage types:
INSERT INTO ps9_gml_subtype (subtype) VALUES ('AbstractCoverage');
INSERT INTO ps9_gml_subtype (subtype, subtype_parent) 
       VALUES ('AbstractDiscreteCoverage', (SELECT id FROM ps9_gml_subtype WHERE subtype='AbstractCoverage'));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent) 
       VALUES ('GridCoverage',             (SELECT id FROM ps9_gml_subtype WHERE subtype='AbstractDiscreteCoverage'));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent) 
       VALUES ('RectifiedGridCoverage',    (SELECT id FROM ps9_gml_subtype WHERE subtype='AbstractDiscreteCoverage'));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent) 
       VALUES ('ReferenceableGridCoverage',(SELECT id FROM ps9_gml_subtype WHERE subtype='AbstractDiscreteCoverage'));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent) 
       VALUES ('MultiPointCoverage',       (SELECT id FROM ps9_gml_subtype WHERE subtype='AbstractDiscreteCoverage'));

-- Extra-metadata types:
INSERT INTO ps9_extra_metadata_type (type) VALUES ('ows');    -- /wcs:Capabilities/wcs:Contents/wcs:CoverageSummary/ows:Metadata
INSERT INTO ps9_extra_metadata_type (type) VALUES ('gmlcov'); -- /wcs:CoverageDescriptions/wcs:CoverageDescription/gmlcov:metadata
                                                              -- /gmlcov:AbstractCoverage/gmlcov:metadata
INSERT INTO ps9_extra_metadata_type (type) VALUES ('attrtable_name'); -- Rasters' attribute table name for rasimport

-- Allowed intervals (see petascope.util.WcsUtil.java)
INSERT INTO ps9_interval (min, max) VALUES (-128, 127); -- char_8
INSERT INTO ps9_interval (min, max) VALUES (   0, 255); -- uchar_8
INSERT INTO ps9_interval (min, max) VALUES (-32768, 32767); -- short_16
INSERT INTO ps9_interval (min, max) VALUES (     0, 65535); -- ushort_16
INSERT INTO ps9_interval (min, max) VALUES (-2147483648, 2147483647); -- int_32
INSERT INTO ps9_interval (min, max) VALUES (          0, 4294967295); -- uint_32
INSERT INTO ps9_interval (min, max) VALUES (-9223372036854775808,  9223372036854775807); -- long_64
INSERT INTO ps9_interval (min, max) VALUES (                   0, 18446744073709551615); -- ulong_64
INSERT INTO ps9_interval (min, max) VALUES (          -3.4028234^38,           3.4028234^38); -- float_32
INSERT INTO ps9_interval (min, max) VALUES (-1.7976931348623157^308, 1.7976931348623157^308); -- double_64

-- UoM for pure numbers
INSERT INTO ps9_uom (code) VALUES ('10' || chr(x'2070'::int));

-- Formats/MIME types/GDAL ids:
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-octet-stream');
INSERT INTO ps9_format (name, mime_type_id) 
     VALUES ('raw', (SELECT currval('ps9_mime_type_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('text/plain');
INSERT INTO ps9_format (name, mime_type_id)
     VALUES ('csv', (SELECT currval('ps9_mime_type_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/jpeg');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JPEG', 'JPEG JFIF (.jpg)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jpg',  (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jpeg', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/png');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('PNG', 'Portable Network Graphics (.png)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('png', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/tiff');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GTiff', 'TIFF / BigTIFF / GeoTIFF (.tif)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('tif',   (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('tiff',  (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gtiff', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/x-aaigrid');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('AAIGrid', 'Arc/Info ASCII Grid');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('aaigrid', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ace2');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ACE2', 'ACE2');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ace2', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-adrg');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ADRG', 'ADRG/ARC Digitilized Raster Graphics (.gen/.thf)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('adrg', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-aig');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('AIG', 'Arc/Info Binary Grid (.adf)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('aig', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-airsar');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('AIRSAR', 'AIRSAR Polarimetric');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('airsar', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-blx');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('BLX', 'Magellan BLX Topo (.blx, .xlb)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('blx', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-bag');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('BAG', 'Bathymetry Attributed Grid (.bag)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('bag', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/bmp');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('BMP', 'Microsoft Windows Device Independent Bitmap (.bmp)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('bmp', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-bsb');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('BSB', 'BSB Nautical Chart Format (.kap)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('bsb', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-bt');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('BT', 'VTP Binary Terrain Format (.bt)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('bt', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ceos');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('CEOS', 'CEOS (Spot for instance)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ceos', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-coasp');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('COASP', 'DRDC COASP SAR Processor Raster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('coasp', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-cosar');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('COSAR', 'TerraSAR-X Complex SAR Data Product');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('cosar', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-cpg');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('CPG', 'Convair PolGASP data');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('cpg', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ctg');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('CTG', 'USGS LULC Composite Theme Grid');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ctg', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-dimap');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('DIMAP', 'Spot DIMAP (metadata.dim)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('dimap', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-dipex');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('DIPEx', 'ELAS DIPEx');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('dipex', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-dods');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('DODS', 'DODS / OPeNDAP');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('dods', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-doq1');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('DOQ1', 'First Generation USGS DOQ (.doq)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('doq1', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-doq2');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('DOQ2', 'New Labelled USGS DOQ (.doq)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('doq2', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-dted');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('DTED', 'Military Elevation Data (.dt0, .dt1, .dt2)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('dted', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-e00grid');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('E00GRID', 'Arc/Info Export E00 GRID');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('e00grid', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ecrgtoc');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ECRGTOC', 'ECRG Table Of Contents (TOC.xml)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ecrgtoc', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/x-imagewebserver-ecw');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ECW', 'ERDAS Compressed Wavelets (.ecw)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ecw', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ehdr');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('EHdr', 'ESRI .hdr Labelled');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ehdr', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-eir');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('EIR', 'Erdas Imagine Raw');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('eir', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-elas');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ELAS', 'NASA ELAS');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('elas', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-envi');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ENVI', 'ENVI .hdr Labelled Raster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('envi', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-epsilon');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('EPSILON', 'Epsilon - Wavelet compressed images');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('epsilon', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ers');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ERS', 'ERMapper (.ers)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ers', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-esat');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ESAT', 'Envisat Image Product (.n1)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('esat', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-fast');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('FAST', 'EOSAT FAST Format');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('fast', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-fit');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('FIT', 'FIT');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('fit', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-fits');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('FITS', 'FITS (.fits)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('fits', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-fujibas');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('FujiBAS', 'Fuji BAS Scanner Image');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('fujibas', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-genbin');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GENBIN', 'Generic Binary (.hdr Labelled)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('genbin', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-georaster');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GEORASTER', 'Oracle Spatial GeoRaster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('georaster', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-gff');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GFF', 'GSat File Format');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gff', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/gif');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GIF', 'Graphics Interchange Format (.gif)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gif', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-grib');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GRIB', 'WMO GRIB1/GRIB2 (.grb)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('grib', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-netcdf-gmt');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GMT', 'GMT Compatible netCDF');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gmt', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-grass');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GRASS', 'GRASS Rasters');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('grass', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-grass_asciigrid');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GRASSASCIIGrid', 'GRASS ASCII Grid');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('grassasciigrid', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-gsag');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GSAG', 'Golden Software ASCII Grid');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gsag', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-gsbg');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GSBG', 'Golden Software Binary Grid');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gsbg', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-gs7bg');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GS7BG', 'Golden Software Surfer 7 Binary Grid');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gs7bg', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-gsc');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GSC', 'GSC Geogrid');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gsc', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-gta');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GTA', 'Generic Tagged Arrays (.gta)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gta', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/x-gtx');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GTX', 'NOAA .gtx vertical datum shift');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gtx', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-gxf');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('GXF', 'GXF - Grid eXchange File');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('gxf', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-hdf4');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('HDF4', 'Hierarchical Data Format Release 4 (HDF4)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('hdf4', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-hdf5');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('HDF5', 'Hierarchical Data Format Release 5 (HDF5)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('hdf5', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-hf2');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('HF2', 'HF2/HFZ heightfield raster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('hf2', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-erdas-hfa');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('HFA', 'Erdas Imagine (.img)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('hfa', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ida');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('IDA', 'Image Display and Analysis (WinDisp)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ida', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ilwis');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ILWIS', 'ILWIS Raster Map (.mpr,.mpl)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ilwis', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ingr');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('INGR', 'Intergraph Raster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ingr', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-isis2');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ISIS2', 'USGS Astrogeology ISIS cube (Version 2)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('isis2', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-isis3');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ISIS3', 'USGS Astrogeology ISIS cube (Version 3)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('isis3', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-jaxapalsar');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JAXAPALSAR', 'JAXA PALSAR Product Reader (Level 1.1/1.5)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jaxapalsar', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-jdem');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JDEM', 'Japanese DEM (.mem)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jdem', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JPEGLS', 'JPEG-LS');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jpegls', (SELECT id FROM ps9_mime_type WHERE mime_type='image/jpeg'), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/jp2');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JPEG2000', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jpeg2000', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JP2ECW', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jp2ecw', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JP2KAK', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jp2kak', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JP2MrSID', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jp2mrsid', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JP2OpenJPEG', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jp2openjpeg', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/jpip-stream');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('JPIPKAK', 'JPIP (based on Kakadu)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('jpipkak', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-kmlsuperoverlay');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('KMLSUPEROVERLAY', 'KMLSUPEROVERLAY');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('kmlsuperoverlay', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-l1b');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('L1B', 'NOAA Polar Orbiter Level 1b Data Set (AVHRR)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('l1b', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-erdas-lan');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('LAN', 'Erdas 7.x .LAN and .GIS');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('lan', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-lcp');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('LCP', 'FARSITE v.4 LCP Format');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('lcp', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-leveller');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('Leveller', 'Daylon Leveller Heightfield');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('leveller', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-loslas');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('LOSLAS', 'NADCON .los/.las Datum Grid Shift');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('loslas', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-mbtiles');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('MBTiles', 'MBTiles');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('mbtiles', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-mem');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('MEM', 'In Memory Raster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('mem', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-mff');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('MFF', 'Vexcel MFF');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('mff', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-mff2');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('MFF2 (HKV)', 'Vexcel MFF2');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('mff2', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-mg4lidar');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('MG4Lidar', 'MG4 Encoded Lidar');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('mg4lidar', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/x-mrsid');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('MrSID', 'Multi-resolution Seamless Image Database');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('mrsid', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-msg');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('MSG', 'Meteosat Second Generation');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('msg', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-msgn');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('MSGN', 'EUMETSAT Archive native (.nat)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('msgn', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ndf');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('NDF', 'NLAPS Data Format');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ndf', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ngsgeoid');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('NGSGEOID', 'NOAA NGS Geoid Height Grids');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ngsgeoid', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-nitf');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('NITF', 'National Imagery Transmission Format (.ntf, .nsf, .gn?, .hr?, .ja?, .jg?, .jn?, .lf?, .on?, .tl?, .tp?, etc.)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('nitf', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/netcdf ');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('netCDF', 'NetCDF');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('netcdf', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ntv2');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('NTv2', 'NTv2 Datum Grid Shift');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ntv2', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-nwt_grc');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('NWT_GRC', 'Northwood/VerticalMapper Classified Grid Format .grc/.tab');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('nwt_grc', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-nwt_grd');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('NWT_GRD', 'Northwood/VerticalMapper Numeric Grid Format .grd/.tab');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('nwt_grd', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ogdi');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('OGDI', 'OGDI Bridge');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ogdi', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-ozi');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('OZI', 'OZI OZF2/OZFX3');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('ozi', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-paux');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('PAux', 'PCI .aux Labelled');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('paux', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-pcidsk');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('PCIDSK', 'PCI Geomatics Database File');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('pcidsk', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-pcraster');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('PCRaster', 'PCRaster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('pcraster', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-pdf');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('PDF', 'Geospatial PDF');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('pdf', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-pds');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('PDS', 'NASA Planetary Data System');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('pds', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-postgisraster');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('PostGISRaster', 'PostGIS Raster (previously WKTRaster)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('postgisraster', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-pnm');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('PNM', 'Netpbm (.ppm,.pgm)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('pnm', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('text/x-r');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('R', 'R Object Data Store');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('r', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-rasdaman');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('RASDAMAN', 'Rasdaman');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('rasdaman', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-rasterlite');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('Rasterlite', 'Rasterlite - Rasters in SQLite DB');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('rasterlite', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-rik');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('RIK', 'Swedish Grid RIK (.rik)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('rik', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-rmf');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('RMF', 'Raster Matrix Format (*.rsw, .mtw)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('rmf', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-rpftoc');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('RPFTOC', 'Raster Product Format/RPF (CADRG, CIB)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('rpftoc', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-rs2');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('RS2', 'RadarSat2 XML (product.xml)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('rs2', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-rst');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('RST', 'Idrisi Raster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('rst', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-saga');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('SAGA', 'SAGA GIS Binary format');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('saga', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-sar_ceos');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('SAR_CEOS', 'SAR CEOS');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('sar_ceos', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-sde');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('SDE', 'ArcSDE Raster');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('sde', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-sdts');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('SDTS', 'USGS SDTS DEM (*CATD.DDF)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('sdts', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/x-sgi');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('SGI', 'SGI Image Format');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('sgi', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-snodas');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('SNODAS', 'Snow Data Assimilation System');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('snodas', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-srp');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('SRP', 'Standard Raster Product (ASRP/USRP)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('srp', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-srtmhgt');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('SRTMHGT', 'SRTM HGT Format');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('srtmhgt', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-terragen');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('TERRAGEN', 'Terragen Heightfield (.ter)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('terragen', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-til');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('TIL', 'EarthWatch/DigitalGlobe .TIL');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('til', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-tsx');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('TSX', 'TerraSAR-X Product');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('tsx', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-usgsdem');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('USGSDEM', 'USGS ASCII DEM / CDED (.dem)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('usgsdem', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-vrt');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('VRT', 'GDAL Virtual Raster (.vrt)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('vrt', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-wcs');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('WCS', 'OGC Web Coverage Service');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('wcs', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-webp');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('WEBP', 'WEBP');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('webp', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-wms');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('WMS', 'OGC Web Map Service');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('wms', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('image/xpm');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('XPM', 'X11 Pixmap (.xpm)');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('xpm', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-xyz');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('XYZ', 'ASCII Gridded XYZ');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('xyz', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
INSERT INTO ps9_mime_type (mime_type)
     VALUES ('application/x-ogc-zmap');
INSERT INTO ps9_gdal_format (gdal_id, description)
     VALUES ('ZMap', 'ZMap Plus Grid');
INSERT INTO ps9_format (name, mime_type_id, gdal_id)
     VALUES ('zmap', (SELECT currval('ps9_mime_type_id_seq')), (SELECT currval('ps9_gdal_format_id_seq')));
----------------------------------------------------------------------
