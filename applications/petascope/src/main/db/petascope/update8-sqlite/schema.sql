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
-- New database schema for `petascopedb` in order to loose the previously
-- inherent constraint that forced coverage axes to be aligned with CRS axes.
-- This new schema reflects the GML 3.3 ReferenceableGrid* types so that
-- irregular (spatiotemporal) coverages can be handled by Petascope.
--
-- The CRS metadata previously stored in the tables (axes, labels, UoM, etc.)
-- is now available via resolvable URI, unique for a coverage. The CRS resolver
-- (SECORE) will provide the necessary CRS info to Petascope.
--
-- Note. RASBASE::nm_meta now merged into petascopedb and renamed to ps_rasgeo (see #169).
--
-- PL/pgSQL ref.: http://www.commandprompt.com/ppbook/c19610.htm
--                http://postgres.cz/wiki/PL/pgSQL_(en)
--
-- DBMS IMPLEMENTATION CHOICES:
--   :: surrogate keys over natural keys, with label "id"
--   :: <table-name> = <prefix>_<table-label>       (e.g. ps_coverage)
--   :: <fk-name>    = <table-label>_id             (e.g. coverage_id)
--   :: singular names over plural names for tables (e.g. ps_coverage, and not ps_coverages)
--   :: composite names are separated by `_'        (e.g. ps_domain_set)
--
-- PREREQUISITES
--   - PL/pgSQL is installed.
--   - `triggers9.sql' has been imported.
-----------------------------------------------------------------------

-- TABLE: **ps_dbupdates** =====================================================
-- This table stores latest database update
CREATE TABLE IF NOT EXISTS ps_dbupdates (
    id      integer PRIMARY KEY AUTOINCREMENT,
    dbupdate  integer NOT NULL
);
-- Initialize it to `7': first update script will be update8.sh
INSERT INTO ps_dbupdates (dbupdate) VALUES (7);


-- COVERAGE MODEL (WCS/WCPS) --------------------------------------------------
-------------------------------------------------------------------------------

-- TABLE: **ps_keyword** =====================================================
-- Keywords for the OWS data description (/ows:Description/ows:Keywords)
CREATE TABLE IF NOT EXISTS ps_keyword (
    id           integer PRIMARY KEY AUTOINCREMENT,
    value        text      NOT NULL, -- /ows:Keywords/ows:Keyword
    language     text      NULL,     -- /ows:Keywords/ows:Keyword/@xml:lang
    -- Constraints and FKs
    UNIQUE (value, language)
);


-- TABLE: **ps_keyword_group** ================================================
-- Keywords for the OWS data description (/ows:Description/ows:Keywords)
CREATE TABLE IF NOT EXISTS ps_keyword_group (
    id             integer PRIMARY KEY AUTOINCREMENT,
    keyword_ids    integer   NOT NULL, -- /ows:Keywords/ows:Keyword = string [+ @xml:lang]
    type           text      NULL,     -- /ows:Keywords/ows:Type
    type_codespace text      NULL,     -- /ows:Keywords/ows:Type/@codeSpace
    -- Constraints and FKs
    UNIQUE (keyword_ids, type, type_codespace)
);


-- TABLES: **ps_description** =================================================
-- Table for ows:Description elements, used both in coverage summaries and service identification.
CREATE TABLE IF NOT EXISTS ps_description (
    id                integer PRIMARY KEY AUTOINCREMENT,
    titles            text      NULL,  -- /ows:Description/ows:Title
    abstracts         text      NULL,  -- /ows:Description/ows:Abstract
    keyword_group_ids integer   NULL  -- /ows:Description/ows:Keywords
    -- Constraints and FKs
);


-- TABLE: **ps_gml_type** =====================================================
-- Catalogue table storing the available GML grid types.
CREATE TABLE IF NOT EXISTS ps_gml_subtype (
    id             integer PRIMARY KEY AUTOINCREMENT,
    subtype        text   UNIQUE NOT NULL,
    subtype_parent int    REFERENCES ps_gml_subtype (id)
);


-- TABLES: **ps_format/ps_mime_type/ps_gdal_id** ==============================
-- These tables describe the encoding formats known to WCPS, as well as their mappings to mimetypes.
-- If you add any, make sure that rasdaman can encode in the
-- format specified by `name', or encoding to that format will not work.
-- Note: format names and MIME types can have aliases:
-- {1 MIME -> N formats} (e.g. tif/tiff) // {1 MIME -> N gdal_id} (JPEG/JPEGLS).
CREATE TABLE IF NOT EXISTS ps_mime_type (
    id              integer PRIMARY KEY AUTOINCREMENT,
    mime_type       varchar(255)     NOT NULL         -- http://tools.ietf.org/html/rfc4288#section-4.2
);
CREATE TABLE IF NOT EXISTS ps_gdal_format (
    id              integer PRIMARY KEY AUTOINCREMENT,
    gdal_id         text             UNIQUE NOT NULL,
    description     text             NULL
);
CREATE TABLE IF NOT EXISTS ps_format (
    id              integer PRIMARY KEY AUTOINCREMENT,
    name            text         UNIQUE NOT NULL,
    mime_type_id    integer      NOT NULL,
    gdal_id         integer      NULL,
    -- Constraints and FKs
    FOREIGN KEY (mime_type_id) REFERENCES ps_mime_type   (id) ON DELETE RESTRICT,
    FOREIGN KEY (gdal_id)      REFERENCES ps_gdal_format (id) ON DELETE RESTRICT
);


-- TABLE: **ps_coverage** =====================================================
-- The core of the coverage model. It is composed, as per GML model, by
-- a domain (the topology), and a range (the payload).
CREATE TABLE IF NOT EXISTS ps_coverage (
    id               integer PRIMARY KEY AUTOINCREMENT,
    name             text         UNIQUE NOT NULL,
    gml_type_id      integer      NOT NULL,
    native_format_id integer      NOT NULL,
    --description_id   integer      NULL,
    -- Constraints and FKs
    FOREIGN KEY (gml_type_id)      REFERENCES ps_gml_subtype  (id) ON DELETE RESTRICT,
    --FOREIGN KEY (description_id)   REFERENCES ps_description  (id) ON DELETE RESTRICT, -- no descriptions by now
    FOREIGN KEY (native_format_id) REFERENCES ps_mime_type    (id) ON DELETE RESTRICT
);

-- TABLE: **ps_bbox** =====================================================
-- This table stores the mins and maxs for each dimension.

CREATE TABLE IF NOT EXISTS ps_bounding_box (
    id             integer PRIMARY KEY AUTOINCREMENT,
    coverage_id    integer NOT NULL,
    lower_left     numeric[] NOT NULL,
    upper_right    numeric[] NOT NULL,
    -- Constraints and FKs
    FOREIGN KEY (coverage_id) REFERENCES ps_coverage (id) ON DELETE CASCADE
);

-- TABLE: **ps_rasdaman_collection** ==========================================
-- This table collects all the rasdaman collections, which are then referenced in ps_range_set.
CREATE TABLE IF NOT EXISTS ps_rasdaman_collection (
    id          integer PRIMARY KEY AUTOINCREMENT,
    name      	varchar(254) NOT NULL,  -- RASBASE::ras_mddcollnames.mddcollname
    oid         numeric(20)  NOT NULL,  -- bigint is 8 bytes signed int: OID is
    base_type   text         NULL,	-- ex RASBASE::nm_meta.pixel_type
    -- Constraints and FKs
    UNIQUE (name, oid),
    CHECK(oid >= 0 AND oid < 1.84467440737e+19)
);


-- TABLE: **ps_range_set** ====================================================
-- This table links the coverage to the either internal (PostgreSQL) or
-- external (e.g. rasdaman) storage of the range values.
CREATE TABLE IF NOT EXISTS ps_range_set (
    id            integer PRIMARY KEY AUTOINCREMENT,
    coverage_id   integer   UNIQUE NOT NULL,
    storage_table text      NOT NULL DEFAULT 'ps_rasdaman_collection', -- instead of managing an enum of types, use table names (see trigger)
    storage_id    integer   NOT NULL,
    -- Constraints and FKs
    UNIQUE (storage_table, storage_id),
    FOREIGN KEY (coverage_id) REFERENCES ps_coverage (id) ON DELETE CASCADE
);


-- TABLE: **ps_data_type** ====================================================
-- Catalogue for data types of a coverages's range [WCPS rangeType() -- OGC 08-068r2 Tab.2].
CREATE TABLE IF NOT EXISTS ps_range_data_type (
    id             integer PRIMARY KEY AUTOINCREMENT,
    name           text       NOT NULL,
    meaning        text       NULL,
    -- Constraints and FKs
    UNIQUE (name)
);


-- TABLE: **ps_range_type_component** =========================================
-- Field components (bands) of a coverage.
CREATE TABLE IF NOT EXISTS ps_range_type_component (
    id               integer PRIMARY KEY AUTOINCREMENT,
    coverage_id      integer    NOT NULL,
    name             text       NOT NULL,   -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/@name
    data_type_id     integer    NOT NULL,   -- WCPS rangeType()
    component_order  integer    NOT NULL,
    field_id         integer    NOT NULL,
    field_table      text       DEFAULT 'ps_quantity',
    -- Constraints and FKs
    CHECK  (component_order >= 0),
    UNIQUE (coverage_id, component_order), -- no same order
    UNIQUE (coverage_id, name),            -- no same label
    FOREIGN KEY (coverage_id)  REFERENCES ps_coverage        (id) ON DELETE CASCADE,
    FOREIGN KEY (data_type_id) REFERENCES ps_range_data_type (id) ON DELETE CASCADE
);

-- TABLE: **ps_uom** ==========================================================
-- Catalogue table with Unit Of Measures (UoMs) for the rangeSet of a coverage.
-- Note: pure numbers should have a UoM code = 10⁰ [http://unitsofmeasure.org/ucum.html#section-Derived-Unit-Atoms, Tab.3]
CREATE TABLE IF NOT EXISTS ps_uom (
    id      integer PRIMARY KEY AUTOINCREMENT,
    code    text        UNIQUE NOT NULL  -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:uom[@code]
);


-- TABLE: **ps_interval** =====================================================
-- Allowed interval values for `quantity' and `count' field types.
CREATE TABLE IF NOT EXISTS ps_interval (
    id       integer PRIMARY KEY AUTOINCREMENT,
    min      numeric     NOT NULL,
    max      numeric     NOT NULL,
    -- Constraints and FKs
    UNIQUE (min, max)
);


-- TABLE: **ps_nil_value** =====================================================
-- Independent collection of NIL value/reason mappings.
-- Can be used by SWE Quantity and Count data components.
CREATE TABLE IF NOT EXISTS ps_nil_value (
    id       integer PRIMARY KEY AUTOINCREMENT,
    value    text      NOT NULL,
    reason   text      NOT NULL,
    -- Constraints and FKs
    UNIQUE (value, reason)
);

-- TABLE: **ps_quantity** =====================================================
-- Independent collection of continuous quantities
CREATE TABLE IF NOT EXISTS ps_quantity (
    id                  integer PRIMARY KEY AUTOINCREMENT,
    uom_id              integer     NOT NULL,  -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:uom
    label               text        DEFAULT '',-- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:label
    description         text        DEFAULT '',-- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:description
    definition_uri      text        NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/@definition
    significant_figures integer     NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/swe:AllowedValues/significantFigures
    nil_ids             integer[]   NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/swe:NilValues

    -- Constraints and FKs
    UNIQUE (uom_id, label, description),
    FOREIGN KEY (uom_id) REFERENCES ps_uom (id) ON DELETE RESTRICT
);


-- TABLE: **ps_quantity_interval** ============================================
-- n:m Association table between ps_quantity and ps_interval: a quantity can
-- be constraint by multiple allowed intervals, which exist independently.
CREATE TABLE IF NOT EXISTS ps_quantity_interval (
    quantity_id     integer,
    interval_id     integer,
    -- Constraints and FKs
    PRIMARY KEY (quantity_id, interval_id),
    FOREIGN KEY (quantity_id) REFERENCES ps_quantity (id) ON DELETE RESTRICT,
    FOREIGN KEY (interval_id) REFERENCES ps_interval (id) ON DELETE RESTRICT
);


-- TABLE: **ps_crs** ==========================================================
-- Catalogue table storing CRS URIs.
CREATE TABLE IF NOT EXISTS ps_crs (
    id     integer PRIMARY KEY AUTOINCREMENT,
    uri    text      UNIQUE NOT NULL -- either single or compound CRSs go in a single URI
);


-- TABLE: **ps_domain_set** ===================================================
-- Table which stores shared information on the geometry of a coverage,
-- independently of its type.
CREATE TABLE IF NOT EXISTS ps_domain_set (
    coverage_id     integer    PRIMARY KEY,
    native_crs_ids  integer[]  NOT NULL,   -- compound CRSs to be stored as array of FKs
    -- Constraints and FKs
    FOREIGN KEY (coverage_id)   REFERENCES ps_coverage (id) ON DELETE CASCADE
);


-- TABLE: **ps_gridded_domain_set** ===========================================
-- Separating metadata shared by /any/ coverage model (multi* coverages, grids, etc.)
-- from type-specific metadata, *GridCoverage-specific in this case.
-- The geometry (domainSet) tables will refer to this one, and not to ps_coverage.
CREATE TABLE IF NOT EXISTS ps_gridded_domain_set (
    coverage_id     integer    PRIMARY KEY,
    grid_origin     numeric[]  NOT NULL,
    -- Constraints and FKs
    FOREIGN KEY (coverage_id) REFERENCES ps_domain_set (coverage_id) ON DELETE CASCADE
);


-- TABLE: **ps_grid_axis** ====================================================
-- Table representing information regarding each axis of the gridded coverage.
-- Depending on how this table is filled, it can represent a regular/irregular
-- rectilinear/curvilinear grid axis.
CREATE TABLE IF NOT EXISTS ps_grid_axis (
    id                  integer PRIMARY KEY AUTOINCREMENT,
    gridded_coverage_id integer     NOT NULL,
    rasdaman_order      integer     NOT NULL,
    -- Constraints and FKs
    CHECK (rasdaman_order >= 0),
    FOREIGN KEY (gridded_coverage_id) REFERENCES ps_gridded_domain_set (coverage_id) ON DELETE CASCADE
);


-- TABLE: **ps_rectilinear_axis** =============================================
-- This table represents a rectilinear (not curvilinear) axis of a gridded coverage.
-- Depending on how this table is filled, it can represent either a regular or an irregular
-- rectilinear axis.
-- [In PSQL an array of N elements starts with array[1] and ends with array[N]]
CREATE TABLE IF NOT EXISTS ps_rectilinear_axis (
    grid_axis_id       integer    PRIMARY KEY,
    offset_vector      numeric[]  NOT NULL, -- directional resolution (point location relative to the origin)
    -- Constraints and FKs
    FOREIGN KEY (grid_axis_id) REFERENCES ps_grid_axis (id) ON DELETE CASCADE
);



-- TABLE: **ps_vector_coefficients** ==========================================
-- Irregularly-spaced rectilinear axis will be represented ``by vectors'',
-- and the coefficients of each grid point along this axis are stored in this table.
CREATE TABLE IF NOT EXISTS ps_vector_coefficients (
    grid_axis_id      integer    NOT NULL,
    coefficient       numeric    NOT NULL,
    coefficient_order integer    NOT NULL,
    -- Constraints and FKs
    UNIQUE (grid_axis_id, coefficient),
    CHECK  (coefficient_order >= 0),
    FOREIGN KEY (grid_axis_id) REFERENCES ps_rectilinear_axis (grid_axis_id) ON DELETE CASCADE
);
-- INDEX on multiple columns for query: ***************************************
-- ``SELECT coefficient_order FROM ps_vector_coefficients WHERE grid_axis_id=<id> AND coefficient <|> <value>''
CREATE INDEX IF NOT EXISTS coefficients_idx ON ps_vector_coefficients (grid_axis_id, coefficient);


-- TABLE: **ps_extra_metadata_type** ==========================================
-- Catalogue table of metadata types for a coverage.
CREATE TABLE IF NOT EXISTS ps_extra_metadata_type (
    id     integer PRIMARY KEY AUTOINCREMENT,
    type   text        UNIQUE NOT NULL
);


-- TABLE: **ps_extra_metadata** ===============================================
-- Additional (optional) metadata that can be specified for a coverage.
-- For extendability, a catalogue table of metadata types is referenced.
CREATE TABLE IF NOT EXISTS ps_extra_metadata (
    id               integer PRIMARY KEY AUTOINCREMENT,
    coverage_id      integer     NOT NULL,
    metadata_type_id integer     NOT NULL,
    value            text        NOT NULL,
    -- Constraints and FKs
    UNIQUE (coverage_id, metadata_type_id, value), -- [0..*] possible metadata for OWS and GMLCOV
    FOREIGN KEY (coverage_id)      REFERENCES ps_coverage            (id) ON DELETE CASCADE,
    FOREIGN KEY (metadata_type_id) REFERENCES ps_extra_metadata_type (id) ON DELETE RESTRICT
);


-- TABLE: **ps_service_identification** =======================================
-- Metadata for the WCS service (/wcs:Capabilities/ows:ServiceIdentification)
CREATE TABLE IF NOT EXISTS ps_service_identification (
    id                 integer PRIMARY KEY AUTOINCREMENT,
    description_id     integer   NULL,
    type               text      NOT NULL, -- //ows:ServiceIdentification/ows:ServiceType/
    type_codespace     text      NULL,     -- //ows:ServiceIdentification/ows:ServiceType/@codeSpace
    type_versions      text      NOT NULL, -- //ows:ServiceIdentification/ows:ServiceTypeVersion (Latest version first).
    fees               text      NULL,     -- //ows:ServiceIdentification/ows:Fees
    access_constraints text      NULL,     -- //ows:ServiceIdentification/ows:AccessConstraints
    -- Constraints and FKs
    UNIQUE (type),
    FOREIGN KEY (description_id) REFERENCES ps_description (id) ON DELETE RESTRICT
);


-- TABLE: **ps_service_provider** =============================================
-- Metadata for the WCS service provider (/wcs:Capabilities/ows:ServiceProvider)
CREATE TABLE IF NOT EXISTS ps_service_provider (
    id          integer PRIMARY KEY AUTOINCREMENT,
    name        text     NOT NULL,   -- //ows:ServiceProvider/ows:ProviderName
    site        text     NULL,       -- //ows:ServiceProvider/ows:ProviderSite[@xlink:href]
    contact_individual_name     text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:IndividualName
    contact_position_name       text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:PositionName
    contact_phone               text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone
    contact_delivery_points     text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:DeliveryPoint
    contact_city                text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:City
    contact_administrative_area text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:AdministrativeArea
    contact_postal_code         text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:PostalCode
    contact_country             text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:Country
    contact_email_addresses     text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:ElectronicMailAddress
    contact_hours_of_service    text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:HoursOfService
    contact_instructions        text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:ContactInstructions
    contact_role                text NULL -- //ows:ServiceProvider/ows:ServiceContact/ows:Role
);
