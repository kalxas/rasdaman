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
-- |                   petascopedb 9.0 triggers                       |
-- ####################################################################

-----------------------------------------------------------------------
-- PREREQUISITES
--   - PL/pgSQL is installed.
--   - `utilities.sql' and `global_const.sql' have been imported.
-----------------------------------------------------------------------


-- TRIGGER: **service_version_pattern_trigger********************************************
-- The string value shall contain one x.y.z "version" value (e.g., "2.1.3").
-- A version number shall contain three non-negative integers separated by decimal points,
-- in the form "x.y.z". The integers y and z shall not exceed 99.
-- (http://schemas.opengis.net/ows/2.0/owsCommon.xsd).
CREATE OR REPLACE FUNCTION service_version_pattern ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'service_version_pattern()';
    BEGIN
        -- check that each service type version follows a valid pattern x.y.z, (y,z < 99).
        FOR i IN array_lower(NEW.type_versions, 1) .. array_upper(NEW.type_versions, 1) LOOP
            IF NOT matches_pattern(NEW.type_versions[i], cget('SERVICE_VERSION_PATTERN')) THEN
                RAISE EXCEPTION '%: ''%'' does not follow a valid pattern (%).', ME, NEW.type_versions[i], cget('SERVICE_VERSION_PATTERN');
            END IF;
        END LOOP;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **coverage_name_trigger********************************************
-- A coverage name must start with a char and must not contain colons: [\i-[:]][\c-[:]]*
--   \i matches any character that may be the first character of an XML name, i.e. [_:A-Za-z]
--   \c matches any character that may occur after the first character in an XML name, i.e. [-._:A-Za-z0-9]
-- (http://www.schemacentral.com/sc/xsd/t-xsd_NCName.html).
CREATE OR REPLACE FUNCTION coverage_name_pattern ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'coverage_name_pattern()';
    BEGIN
        -- check that the coverage name ~ [\i-[:]][\c-[:]]*
        IF NOT matches_pattern(NEW.name, cget('NAME_PATTERN')) THEN
            RAISE EXCEPTION '%: ''%'' does not follow a valid naming pattern (%).', ME, NEW.name, cget('NAME_PATTERN');
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **storage_type_trigger*********************************************
-- Check referential integrity on multiple references tables
CREATE OR REPLACE FUNCTION storage_ref_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'storage_ref_integrity()';
    BEGIN
        -- check for referential integrity
        IF NOT table_has_id(NEW.storage_table, cget('ID_FIELD'), NEW.storage_id) THEN
            RAISE EXCEPTION '%: invalid reference to ''%'', no row has id ''%''.', ME, NEW.storage_table, NEW.storage_id;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **keyword_integrity_trigger***********************************************
-- Check referential integrity on ps9_keyword.
CREATE OR REPLACE FUNCTION keyword_ref_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'keyword_ref_integrity()';
    BEGIN
        -- check for referential integrity
        FOR i IN array_lower(NEW.keyword_ids, 1)..array_upper(NEW.keyword_ids, 1) LOOP
            IF NOT table_has_id(cget('TABLE_PS9_KEYWORD'), cget('PS9_KEYWORD_ID'), NEW.keyword_ids[i]) THEN
                RAISE EXCEPTION '%: invalid reference to ''%'', no row has id ''%''.',
                                ME, cget('TABLE_PS9_KEYWORD'), NEW.keyword_ids[i];
            END IF;
        END LOOP;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **keyword_group_integrity_trigger***********************************************
-- Check referential integrity on ps9_keyword_group.
CREATE OR REPLACE FUNCTION keyword_group_ref_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'keyword_group_ref_integrity()';
    BEGIN
        -- check for referential integrity
        IF NEW.keyword_group_ids IS NOT NULL THEN -- 1+ keyword group is not mandatory
            FOR i IN array_lower(NEW.keyword_group_ids, 1)..array_upper(NEW.keyword_group_ids, 1) LOOP
                IF NOT table_has_id(cget('TABLE_PS9_KEYWORD_GROUP'), cget('PS9_KEYWORD_GROUP_ID'), NEW.keyword_group_ids[i]) THEN
                    RAISE EXCEPTION '%: invalid reference to ''%'', no row has id ''%''.',
                                    ME, cget('TABLE_PS9_KEYWORD_GROUP'), NEW.keyword_group_ids[i];
                END IF;
            END LOOP;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **field_type_trigger***********************************************
-- Check referential integrity on multiple references tables
CREATE OR REPLACE FUNCTION field_ref_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'field_ref_integrity()';
    BEGIN
        -- check for referential integrity
        IF NOT table_has_id(NEW.field_table, cget('ID_FIELD'), NEW.field_id) THEN
            RAISE EXCEPTION '%: invalid reference to ''%'', no row has % ''%''.',
                            ME, NEW.field_table, cget('ID_FIELD'), NEW.field_id;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **range_component_order_trigger************************************
-- Checks that the components inserted for this coverage by now follow a sequential order from 0.
CREATE OR REPLACE FUNCTION range_component_order_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'range_component_order_integrity()';
    BEGIN
        -- check the component orders of this coverage are in a sequence from 0
        IF NOT (SELECT is_sequential((
              numeric_column2array(
                cget('TABLE_PS9_RANGETYPE_COMPONENT'),   -- table
                cget('PS9_RANGETYPE_COMPONENT_ORDER'),   -- numeric column to check
                ' WHERE ' || quote_ident(cget('PS9_RANGETYPE_COMPONENT_COVERAGE_ID')) || ' = '
                          || NEW.coverage_id, -- select /this/ coverage
                cget('PS9_RANGETYPE_COMPONENT_ORDER')    -- order-by column
              )::integer[]), 0)) -- the integer sequence must start from 0
            THEN RAISE EXCEPTION '%: last inserted component order (%) is not valid. ''%'' must be in a ordered sequence starting from 0.',
                            ME, NEW.component_order, cget('PS9_RANGETYPE_COMPONENT_ORDER');
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **range_component_drop************************************
-- Emulate DROP CASCADE on ps_quantity, but cascading the drop only if no other tuple
-- in ps_range_component is referencing
CREATE OR REPLACE FUNCTION range_component_drop ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'range_component_drop()';
        -- Local variables
        _qry  text;
        _tup  record;
    BEGIN
        -- Check if the referenced quantity exists:
        --> this is ensured on INSERT/UPDATE by field_integrity_trigger()

        -- Check if this rangeType component is the last one referencing this quantity
        _qry := ' SELECT * FROM ' || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT')) ||
                        ' WHERE ' || quote_ident(cget('PS9_RANGETYPE_COMPONENT_FIELD_ID'))
                                  ||    '='    || OLD.field_id ||
                          ' AND ' || quote_ident(cget('PS9_RANGETYPE_COMPONENT_FIELD_TABLE'))
                                  || ' ILIKE ' || quote_literal(OLD.field_table)
                                  ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            -- This is not the last rangeType component referencing this SWE field: won't cascade.
            RETURN OLD;
        END LOOP;

        -- This is the last rangeType component referencing this SWE field:
        -- cascade-drop it, if it is not a primitive (keep persistent mapping of allowed values for standard primitive types)
        IF OLD.field_table = cget('TABLE_PS9_QUANTITY') THEN
            PERFORM drop_quantity(OLD.field_id); -- drop (if not primitive) quantity and associated UoM and allowed values
        END IF;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **range_set_drop**********************************************************
-- Emulate DROP CASCADE on ps_quantity, but cascading the drop only if no other tuple
-- in ps_range_component is referencing
CREATE OR REPLACE FUNCTION range_set_drop ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'range_set_drop()';
        -- Local variables
        _qry  text;
        _tup  record;
    BEGIN
        -- Check if the referenced collection exists:
        --> this is ensured on INSERT/UPDATE by storage_ref_integrity_trigger()

        -- Check if this rangeSet component is the last one referencing this collection
        _qry := ' SELECT * FROM ' || quote_ident(cget('TABLE_PS9_RANGESET')) ||
                        ' WHERE ' || quote_ident(cget('PS9_RANGESET_STORAGE_ID'))
                                  ||    '='    || OLD.storage_id ||
                          ' AND ' || quote_ident(cget('PS9_RANGESET_STORAGE_TABLE'))
                                  || ' ILIKE ' || quote_literal(OLD.storage_table)
                                  ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            -- This is not the last rangeSet component referencing this collection: won't cascade.
            RETURN OLD;
        END LOOP;

        -- This is the last rangeSet component referencing this collection:
        IF  OLD.storage_table = quote_ident(cget('TABLE_PS9_RASDAMAN_COLLECTION')) THEN
            RAISE DEBUG '%: delete rasdaman collection tuple with ID #%.', ME, OLD.storage_id;
            _qry := ' DELETE FROM ' || OLD.storage_table ||
                          ' WHERE ' || quote_ident(cget('PS9_RASDAMAN_COLLECTION_ID')) ||
                                '=' || OLD.storage_id
                                    ;
            RAISE DEBUG '%: EXECUTE %', ME, _qry;
            EXECUTE _qry;
        END IF;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **uom_code_trigger*************************************************
-- This type specifies a character string of length at least one, and restricted
-- such that it must not contain any of the following characters: ":" (colon), " " (space),
-- (newline), (carriage return), (tab). This allows values corresponding to familiar
-- abbreviations, such as "kg", "m/s", etc. It is also required that the symbol be an
-- identifier for a unit of measure as specified in the "Unified Code of Units of Measure"
-- (UCUM) (http://aurora.regenstrief.org/UCUM). [From http://schemas.opengis.net/sweCommon/2.0/basic_types.xsd - "UomSymbol"]
CREATE OR REPLACE FUNCTION uom_code_pattern ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'uom_code_pattern()';
    BEGIN
        -- check that the UoM code is like <pattern value="[^: \n\r\t]+"/>
        IF NOT matches_pattern(NEW.code,  cget('UOM_PATTERN')) THEN
            RAISE EXCEPTION '%: ''%'' does not follow a valid UoM pattern (%).',
                            ME, NEW.code, cget('UOM_PATTERN');
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **nils_ref_integrity*****************************************************
-- Check referential integrity on ps_nil_value.
CREATE OR REPLACE FUNCTION nils_ref_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'nils_ref_integrity()';
    BEGIN
        -- check for referential integrity
        IF NEW.nil_ids IS NOT NULL THEN
            FOR i IN array_lower(NEW.nil_ids, 1)..array_upper(NEW.nil_ids, 1) LOOP
                IF NOT table_has_id(cget('TABLE_PS9_NIL_VALUE'), cget('PS9_NIL_VALUE_ID'), NEW.nil_ids[i]) THEN
                    RAISE EXCEPTION '%: invalid reference to ''%'', no row has id ''%''.',
                                    ME, cget('TABLE_PS9_NIL_VALUE'), NEW.nil_ids[i];
                END IF;
            END LOOP;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **crs_integrity*****************************************************
-- Check referential integrity on ps_crs.
CREATE OR REPLACE FUNCTION crs_ref_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'crs_ref_integrity()';
    BEGIN
        -- check for referential integrity
        FOR i IN array_lower(NEW.native_crs_ids, 1)..array_upper(NEW.native_crs_ids, 1) LOOP
            IF NOT table_has_id(cget('TABLE_PS9_CRS'), cget('PS9_CRS_ID'), NEW.native_crs_ids[i]) THEN
                RAISE EXCEPTION '%: invalid reference to ''%'', no row has id ''%''.',
                                ME, cget('TABLE_PS9_CRS'), NEW.native_crs_ids[i];
            END IF;
        END LOOP;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **crs_compound_integrity***********************************************
-- Check referential integrity on ps_crs.
CREATE OR REPLACE FUNCTION crs_compound_components ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'crs_compound_components()';
    BEGIN
        -- check that there are no duplicated in the CRS compound
        IF NOT (SELECT is_increasing(array_sort(NEW.native_crs_ids)::integer[], true))
            THEN RAISE EXCEPTION '%: invalid compound CRS (%). The single composing CRSs must be unique.',
                            ME, NEW.native_crs_ids;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **rasdaman_axis_order_trigger**************************************
-- Checks that the rasdaman orders inserted for this coverage by now, follow a sequential order from 0.
CREATE OR REPLACE FUNCTION rasdaman_axis_order_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'rasdaman_axis_order_integrity()';
    BEGIN
        -- check the rasdaman orders of this coverage are in a sequence from 0
        IF NOT (SELECT is_sequential((
              numeric_column2array(
                cget('TABLE_PS9_GRID_AXIS'),          -- table
                cget('PS9_GRID_AXIS_RASDAMAN_ORDER'), -- numeric column to check
                ' WHERE ' || quote_ident(cget('PS9_GRID_AXIS_COVERAGE_ID')) || ' = '
                          || NEW.gridded_coverage_id, -- select /this/ coverage
                cget('PS9_GRID_AXIS_RASDAMAN_ORDER')  -- order-by column
              )::integer[]), 0)) -- the integer sequence must start from 0
            THEN RAISE EXCEPTION '%: last inserted rasdaman order (%) is not valid. ''%'' must be in a ordered sequence starting from 0.',
                            ME, NEW.rasdaman_order, cget('PS9_GRID_AXIS_RASDAMAN_ORDER');
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **offsetvector_origin_trigger**************************************
-- This trigger checks coherence of dimensions of the referenced offset vector with
-- the origin of the coverage.
CREATE OR REPLACE FUNCTION offset_vector_coherence ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'offset_vector_coherence()';
        -- Local variables
        _grid_origin numeric[];
        _qry text;
    BEGIN
        -- Check if origin has been inserted
	_qry :=    'SELECT ' || quote_ident(cget('TABLE_PS9_GRIDDED_DOMAINSET'))  || '.'
                             || quote_ident(cget('PS9_GRIDDED_DOMAINSET_ORIGIN')) ||
                    ' FROM ' || quote_ident(cget('TABLE_PS9_GRIDDED_DOMAINSET'))  || ','
                             || quote_ident(cget('TABLE_PS9_GRID_AXIS'))          ||
                   ' WHERE ' || quote_ident(cget('TABLE_PS9_GRIDDED_DOMAINSET'))  || '.'
                             || quote_ident(cget('PS9_GRIDDED_DOMAINSET_COVERAGE_ID')) ||
                         '=' || quote_ident(cget('TABLE_PS9_GRID_AXIS'))          || '.'
                             || quote_ident(cget('PS9_GRID_AXIS_COVERAGE_ID'))    ||
                     ' AND ' || quote_ident(cget('TABLE_PS9_GRID_AXIS'))          || '.'
                             || quote_ident(cget('PS9_GRID_AXIS_ID'))      || '=' || NEW.grid_axis_id;
	RAISE DEBUG '%: EXECUTE %', ME, _qry;
	EXECUTE _qry INTO STRICT _grid_origin;  -- error if not exactly one row is returned
        IF _grid_origin IS NULL THEN
            RAISE EXCEPTION '%: Trying to set a reference vector for a coverage without origin.', ME;
        ELSE
            -- Check if the dimensions of origin and offset vector match (lower bound is in CHECK contraint already, for both)
            IF array_upper(NEW.offset_vector, 1) <> array_upper(_grid_origin, 1) THEN
                RAISE EXCEPTION '%: offset vector (%) is not compatible with coverage origin (%)',
                                ME, NEW.offset_vector, _grid_origin;
            END IF;
	END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **coefficients_integrity_trigger***********************************
-- Checks that the coefficients are ordered (0,1,2,...) and their values are strictly increasing.
CREATE OR REPLACE FUNCTION coefficients_integrity ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
        ME constant text := 'coefficients_integrity()';
    BEGIN
        -- check the component orders of this coverage are in a sequence from 0
        IF NOT (SELECT is_sequential((
              numeric_column2array(
                cget('TABLE_PS9_VECTOR_COEFFICIENTS'),   -- table
                cget('PS9_VECTOR_COEFFICIENTS_ORDER'),   -- numeric column to check
                ' WHERE ' || quote_ident(cget('PS9_VECTOR_COEFFICIENTS_ID')) || ' = ' || NEW.grid_axis_id, -- select /this/ axis
                cget('PS9_VECTOR_COEFFICIENTS_ORDER')    -- order-by column
              )::integer[]), 0)) -- the integer sequence must start from
            THEN RAISE EXCEPTION '%: last inserted coefficient order (%) is not valid. ''%'' must be in a ordered sequence starting from 0.',
                                 ME, NEW.coefficient_order, cget('PS9_VECTOR_COEFFICIENTS_ORDER');
        END IF;

        -- check that the coefficients are *strictly* increasing
        IF NOT (SELECT is_increasing((
            SELECT numeric_column2array(
                cget('TABLE_PS9_VECTOR_COEFFICIENTS'),   -- table
                cget('PS9_VECTOR_COEFFICIENTS_VALUE'),   -- numeric column to check
                ' WHERE ' || quote_ident(cget('PS9_VECTOR_COEFFICIENTS_ID')) || ' = ' || NEW.grid_axis_id, -- select /this/ axis
                cget('PS9_VECTOR_COEFFICIENTS_ORDER')    -- order-by column
            )), true)) -- is_strict
            THEN RAISE EXCEPTION '%: last inserted coefficient (%) is not valid. ''%'' must be in a strictly increasing sequence.',
                                 ME, NEW.coefficient, cget('PS9_VECTOR_COEFFICIENTS_VALUE');
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **single_service_provider_trigger**********************************
-- Checks that no second service is inserted.
CREATE OR REPLACE FUNCTION single_service_provider ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'single_service_provider()';
    BEGIN
        -- check there is no other service there
        IF NOT (SELECT table_is_empty(cget('TABLE_PS9_SERVICE_PROVIDER'))) THEN
            RAISE EXCEPTION '%: cannot insert more than one WCS service provider.''', ME;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;


-- TRIGGER: **single_dbupdate**************************************************
-- Checks that no second service is inserted.
CREATE OR REPLACE FUNCTION single_dbupdate ()
RETURNS trigger AS
$$
    DECLARE
        -- Log
	ME constant text := 'single_dbupdate()';
    BEGIN
        -- check there is no other tuple in the table
        IF NOT (SELECT table_is_empty(cget('TABLE_PS9_DBUPDATES'))) THEN
            RAISE EXCEPTION '%: cannot insert more than one dbupdate ID.''', ME;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;
