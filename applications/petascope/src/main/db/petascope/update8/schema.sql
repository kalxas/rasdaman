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
--
-- PREREQUISITES
--   - PL/pgSQL is installed.
--   - `triggers9.sql' has been imported.
-----------------------------------------------------------------------

-- TABLE: **ps9_dbupdates** =====================================================
-- This table stores latest database update
CREATE TABLE ps9_dbupdates (
    id      SERIAL    PRIMARY KEY,
    update  integer   NOT NULL
);
CREATE TRIGGER single_dbupdate_trigger BEFORE INSERT ON ps9_dbupdates
       FOR EACH ROW EXECUTE PROCEDURE single_dbupdate();
-- Initialize it to `0': first update script will be update1.sql
INSERT INTO ps9_dbupdates (update) VALUES (0);


-- COVERAGE MODEL (WCS/WCPS) --------------------------------------------------
-------------------------------------------------------------------------------

-- TABLE: **ps9_keyword** =====================================================
-- Keywords for the OWS data description (/ows:Description/ows:Keywords)
CREATE TABLE ps9_keyword (
    id           serial    PRIMARY KEY,
    value        text      NOT NULL, -- /ows:Keywords/ows:Keyword
    language     text      NULL,     -- /ows:Keywords/ows:Keyword/@xml:lang
    -- Constraints and FKs
    UNIQUE (value, language)
);


-- TABLE: **ps9_keyword_group** ================================================
-- Keywords for the OWS data description (/ows:Description/ows:Keywords)
CREATE TABLE ps9_keyword_group (
    id             serial    PRIMARY KEY,
    keyword_ids    integer[] NOT NULL, -- /ows:Keywords/ows:Keyword = string [+ @xml:lang]
    type           text      NULL,     -- /ows:Keywords/ows:Type
    type_codespace text      NULL,     -- /ows:Keywords/ows:Type/@codeSpace
    -- Constraints and FKs
    UNIQUE (keyword_ids, type, type_codespace),
    CONSTRAINT keyword_ids_is_1D        CHECK (array_lower(keyword_ids, 2) IS NULL),
    CONSTRAINT keyword_ids_is_not_empty CHECK (array_lower(keyword_ids, 1) IS NOT NULL) -- Contains at least 1 element
);
CREATE TRIGGER keyword_integrity_trigger AFTER INSERT OR UPDATE ON ps9_keyword_group
       FOR EACH ROW EXECUTE PROCEDURE keyword_ref_integrity();


-- TABLES: **ps9_description** =================================================
-- Table for ows:Description elements, used both in coverage summaries and service identification.
CREATE TABLE ps9_description (
    id                serial    PRIMARY KEY,
    titles            text[]    NULL,  -- /ows:Description/ows:Title
    abstracts         text[]    NULL,  -- /ows:Description/ows:Abstract
    keyword_group_ids integer[] NULL,  -- /ows:Description/ows:Keywords
    -- Constraints and FKs
    CONSTRAINT titles_is_1D            CHECK (array_lower(titles, 2)            IS NULL),
    CONSTRAINT abstracts_is_1D         CHECK (array_lower(abstracts, 2)         IS NULL),
    CONSTRAINT keyword_group_ids_is_1D CHECK (array_lower(keyword_group_ids, 2) IS NULL)
);
CREATE TRIGGER keyword_group_integrity_trigger AFTER INSERT OR UPDATE ON ps9_description
       FOR EACH ROW EXECUTE PROCEDURE keyword_group_ref_integrity();


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
    --description_id   integer      NULL,
    -- Constraints and FKs
    FOREIGN KEY (gml_type_id)      REFERENCES ps9_gml_subtype  (id) ON DELETE RESTRICT,
    --FOREIGN KEY (description_id)   REFERENCES ps9_description  (id) ON DELETE RESTRICT, -- no descriptions by now
    FOREIGN KEY (native_format_id) REFERENCES ps9_mime_type    (id) ON DELETE RESTRICT
);
CREATE TRIGGER coverage_name_trigger BEFORE INSERT OR UPDATE ON ps9_coverage
       FOR EACH ROW EXECUTE PROCEDURE coverage_name_pattern();

-- TABLE: **ps9_bbox** =====================================================
-- This table stores the mins and maxs for each dimension.

CREATE TABLE ps9_bounding_box (
    id             serial PRIMARY KEY,
    coverage_id    integer NOT NULL,
    lower_left     numeric[] NOT NULL,
    upper_right    numeric[] NOT NULL,
    -- Constraints and FKs
    CONSTRAINT lower_left_is_1D         CHECK (array_lower(lower_left, 2)  IS NULL),
    CONSTRAINT lower_left_is_not_empty  CHECK (array_lower(lower_left, 1)  IS NOT NULL),
    CONSTRAINT upper_right_is_1D        CHECK (array_lower(upper_right, 2) IS NULL),
    CONSTRAINT upper_right_is_not_empty CHECK (array_lower(upper_right, 1) IS NOT NULL),
    CONSTRAINT ll_ur_integrity          CHECK (array_dims(lower_left) = array_dims(upper_right)),
    FOREIGN KEY (coverage_id) REFERENCES ps9_coverage (id) ON DELETE CASCADE
);

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
CREATE TRIGGER storage_integrity_trigger BEFORE INSERT OR UPDATE ON ps9_range_set
       FOR EACH ROW EXECUTE PROCEDURE storage_ref_integrity();
CREATE TRIGGER range_set_drop_trigger AFTER DELETE ON ps9_range_set
       FOR EACH ROW EXECUTE PROCEDURE range_set_drop();


-- TABLE: **ps9_data_type** ====================================================
-- Catalogue for data types of a coverages's range [WCPS rangeType() -- OGC 08-068r2 Tab.2].
CREATE TABLE ps9_range_data_type (
    id             serial     PRIMARY KEY,
    name           text       NOT NULL,
    meaning        text       NULL,
    -- Constraints and FKs
    UNIQUE (name)
);


-- TABLE: **ps9_range_type_component** =========================================
-- Field components (bands) of a coverage.
CREATE TABLE ps9_range_type_component (
    id               serial     PRIMARY KEY,
    coverage_id      integer    NOT NULL,
    name             text       NOT NULL,   -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/@name
    data_type_id     integer    NOT NULL,   -- WCPS rangeType()
    component_order  integer    NOT NULL,
    field_id         integer    NOT NULL,
    field_table      text       DEFAULT 'ps9_quantity',
    -- Constraints and FKs
    CHECK  (component_order >= 0),
    UNIQUE (coverage_id, component_order), -- no same order
    UNIQUE (coverage_id, name),            -- no same label
    FOREIGN KEY (coverage_id)  REFERENCES ps9_coverage        (id) ON DELETE CASCADE,
    FOREIGN KEY (data_type_id) REFERENCES ps9_range_data_type (id) ON DELETE CASCADE
);
CREATE TRIGGER field_integrity_trigger BEFORE INSERT OR UPDATE ON ps9_range_type_component
       FOR EACH ROW EXECUTE PROCEDURE field_ref_integrity();
CREATE TRIGGER range_component_order_trigger AFTER INSERT OR UPDATE ON ps9_range_type_component
       FOR EACH ROW EXECUTE PROCEDURE range_component_order_integrity();
CREATE TRIGGER range_component_drop_trigger AFTER DELETE ON ps9_range_type_component
       FOR EACH ROW EXECUTE PROCEDURE range_component_drop();

-- TABLE: **ps9_uom** ==========================================================
-- Catalogue table with Unit Of Measures (UoMs) for the rangeSet of a coverage.
-- Note: pure numbers should have a UoM code = 10⁰ [http://unitsofmeasure.org/ucum.html#section-Derived-Unit-Atoms, Tab.3]
CREATE TABLE ps9_uom (
    id      serial      PRIMARY KEY,
    code    text        UNIQUE NOT NULL  -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:uom[@code]
);
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


-- TABLE: **ps9_nil_value** =====================================================
-- Independent collection of NIL value/reason mappings.
-- Can be used by SWE Quantity and Count data components.
CREATE TABLE ps9_nil_value (
    id       serial    PRIMARY KEY,
    value    text      NOT NULL,
    reason   text      NOT NULL,
    -- Constraints and FKs
    UNIQUE (value, reason)
);

-- TABLE: **ps9_quantity** =====================================================
-- Independent collection of continuous quantities
CREATE TABLE ps9_quantity (
    id                  serial      PRIMARY KEY,
    uom_id              integer     NOT NULL,  -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:uom
    label               text        DEFAULT '',-- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:label
    description         text        DEFAULT '',-- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/gml:description
    definition_uri      text        NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/@definition
    significant_figures integer     NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/swe:AllowedValues/significantFigures
    nil_ids             integer[]   NULL,      -- /gmlcov:AbstractCoverage/gmlcov:rangeType/swe:field/swe:Quantity/swe:NilValues

    -- Constraints and FKs
    UNIQUE (uom_id, label, description),
    FOREIGN KEY (uom_id) REFERENCES ps9_uom (id) ON DELETE RESTRICT
);
CREATE TRIGGER nils_ref_integrity_trigger BEFORE INSERT OR UPDATE ON ps9_quantity
       FOR EACH ROW EXECUTE PROCEDURE nils_ref_integrity();


-- TABLE: **ps9_quantity_interval** ============================================
-- n:m Association table between ps9_quantity and ps9_interval: a quantity can
-- be constraint by multiple allowed intervals, which exist independently.
CREATE TABLE ps9_quantity_interval (
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
    native_crs_ids  integer[]  NOT NULL,   -- compound CRSs to be stored as array of FKs
    -- Constraints and FKs
    CONSTRAINT native_crs_ids_is_1D        CHECK (array_lower(native_crs_ids, 2) IS NULL),
    CONSTRAINT natice_crs_ids_is_not_empty CHECK (array_lower(native_crs_ids, 1) IS NOT NULL), -- Contains at least 1 element
    FOREIGN KEY (coverage_id)   REFERENCES ps9_coverage (id) ON DELETE CASCADE
);
CREATE TRIGGER crs_integrity BEFORE INSERT OR UPDATE ON ps9_domain_set
       FOR EACH ROW EXECUTE PROCEDURE crs_ref_integrity();
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
    CONSTRAINT origin_is_not_empty     CHECK (array_lower(grid_origin, 1) IS NOT NULL), -- Contains at least 1 element
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
    CONSTRAINT offsetvector_lower_bound_is_1 CHECK (array_lower(offset_vector, 1) = 1),     -- Indexing starts from 1
    CONSTRAINT offsetvector_is_1D            CHECK (array_lower(offset_vector, 2) IS NULL), -- Cannot have more than 1 tuple of coordinates
    CONSTRAINT offset_vector_is_not_empty    CHECK (array_lower(offset_vector, 1) IS NOT NULL) -- Contains at least 1 element
);

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
    CHECK  (coefficient_order >= 0),
    FOREIGN KEY (grid_axis_id) REFERENCES ps9_rectilinear_axis (grid_axis_id) ON DELETE CASCADE
);
-- INDEX on multiple columns for query: ***************************************
-- ``SELECT coefficient_order FROM ps9_vector_coefficients WHERE grid_axis_id=<id> AND coefficient <|> <value>''
CREATE INDEX coefficients_idx ON ps9_vector_coefficients (grid_axis_id, coefficient);
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
    UNIQUE (coverage_id, metadata_type_id, value), -- [0..*] possible metadata for OWS and GMLCOV
    FOREIGN KEY (coverage_id)      REFERENCES ps9_coverage            (id) ON DELETE CASCADE,
    FOREIGN KEY (metadata_type_id) REFERENCES ps9_extra_metadata_type (id) ON DELETE RESTRICT
);


-- TABLE: **ps9_service_identification** =======================================
-- Metadata for the WCS service (/wcs:Capabilities/ows:ServiceIdentification)
CREATE TABLE ps9_service_identification (
    id                 serial    PRIMARY KEY,
    description_id     integer   NULL,
    type               text      NOT NULL, -- //ows:ServiceIdentification/ows:ServiceType/
    type_codespace     text      NULL,     -- //ows:ServiceIdentification/ows:ServiceType/@codeSpace
    type_versions      text[]    NOT NULL, -- //ows:ServiceIdentification/ows:ServiceTypeVersion (Latest version first).
    fees               text      NULL,     -- //ows:ServiceIdentification/ows:Fees
    access_constraints text[]    NULL,     -- //ows:ServiceIdentification/ows:AccessConstraints
    -- Constraints and FKs
    UNIQUE (type),
    CONSTRAINT access_constraints_is_1D   CHECK (array_lower(access_constraints, 2) IS NULL),
    CONSTRAINT type_versions_is_1D        CHECK (array_lower(type_versions, 2)      IS NULL),
    CONSTRAINT type_versions_is_not_empty CHECK (array_lower(type_versions, 1)      IS NOT NULL),
    FOREIGN KEY (description_id) REFERENCES ps9_description (id) ON DELETE RESTRICT
);
CREATE TRIGGER service_version_pattern_trigger BEFORE INSERT OR UPDATE ON ps9_service_identification
       FOR EACH ROW EXECUTE PROCEDURE service_version_pattern();


-- TABLE: **ps9_service_provider** =============================================
-- Metadata for the WCS service provider (/wcs:Capabilities/ows:ServiceProvider)
CREATE TABLE ps9_service_provider (
    id          serial PRIMARY KEY,
    name        text     NOT NULL,   -- //ows:ServiceProvider/ows:ProviderName
    site        text     NULL,       -- //ows:ServiceProvider/ows:ProviderSite[@xlink:href]
    contact_individual_name     text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:IndividualName
    contact_position_name       text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:PositionName
    contact_phone               text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone
    contact_delivery_points   text[] NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:DeliveryPoint
    contact_city                text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:City
    contact_administrative_area text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:AdministrativeArea
    contact_postal_code         text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:PostalCode
    contact_country             text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:Country
    contact_email_addresses   text[] NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Address/ows:ElectronicMailAddress
    contact_hours_of_service    text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:HoursOfService
    contact_instructions        text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:ContactInstructions
    contact_role                text NULL, -- //ows:ServiceProvider/ows:ServiceContact/ows:Role
    CONSTRAINT delivery_points_is_1D CHECK (array_lower(contact_delivery_points, 2) IS NULL),
    CONSTRAINT email_addresses_is_1D CHECK (array_lower(contact_email_addresses, 2) IS NULL)
);
CREATE TRIGGER single_service_provider_trigger BEFORE INSERT ON ps9_service_provider
       FOR EACH ROW EXECUTE PROCEDURE single_service_provider();
