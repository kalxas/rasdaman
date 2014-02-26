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
-- |          petascopedb migration script for rasdaman 9.0           |
-- ####################################################################

-----------------------------------------------------------------------
-- PREFACE
-- This script migrates the pre-existing coverages stored in `petascopedb'
-- to fit in the new schema for irregular grids, which can be created running
-- the `./init9.sql' script.
--
-- PREREQUISITES
--   - PL/pgSQL and `dblink' PostgreSQL extension are installed.
--   - `utilities.sql' and `global_const.sql' have been imported within this session.
--
-- VARIABLES NAMING STYLE
-- local variables : _<lowercase_name>
-- constants       : <UPPERCASE_NAME>
-- arguments       : <lowercase_name>
-----------------------------------------------------------------------

--
-- Utility to check whether a CRS (EPSG code) defines northing coordinate first (see north_first_crss.sql).
--
CREATE OR REPLACE FUNCTION defines_northing_first (
    crs_label text
)
RETURNS boolean AS
$$
    DECLARE
        -- Log
        ME constant text := 'defines_northing_first()';
        -- Local variables
        _qry      text;
        _tup      record;
        _crs_code text;
        _isNorthfirst boolean := false;
    BEGIN
        IF matches_pattern(crs_label, cget('EPSG_PATTERN')) THEN
            _crs_code = get_crs_code(crs_label);
            _qry := ' SELECT ' || quote_ident(cget('PS9_NORTH_FIRST_CRSS_CODE'))  ||
                      ' FROM ' || quote_ident(cget('TABLE_PS9_NORTH_FIRST_CRSS')) ||
                     ' WHERE ' || quote_ident(cget('PS9_NORTH_FIRST_CRSS_CODE'))  || '::text = ' || quote_literal(_crs_code)
                    ;
            RAISE DEBUG '%: EXECUTE %', ME, _qry;
            FOR _tup IN EXECUTE _qry LOOP
                RAISE DEBUG '%: CRS ''%'' defines northings first.', ME, crs_label;
                _isNorthfirst := true;
            END LOOP;
        END IF;
        RETURN _isNorthfirst;
    END;
$$ LANGUAGE plpgsql;


--
-- Copy the existing CRSs URIs to ps9_crs
--
CREATE OR REPLACE FUNCTION migrate_crss ()
RETURNS void AS
$$
    DECLARE
        -- Log
        ME constant text := 'migrate_crss()';
        -- Local variables
        _qry text;
        _tup record;
    BEGIN
        -- Select all the CRSs (starting with http)
        _qry := ' SELECT DISTINCT ARRAY[' || quote_ident(cget('PS_CRS_NAME'))  || '] AS row ' ||
                                 ' FROM ' || quote_ident(cget('TABLE_PS_CRS')); -- ||
--                                ' WHERE ' || cget('PS_CRS_NAME')  || ' ILIKE ''http%''';
        FOR _tup IN EXECUTE _qry LOOP
            BEGIN
                -- Add them to ps9_crs
                _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_CRS')) ||
                                  ' ( ' || quote_ident(cget('PS9_CRS_URI'))   || ' ) ' ||
                            ' VALUES (' || quote_literal(_tup.row[1])          || ')';
                RAISE DEBUG '%: EXECUTING : %;', ME, _qry;
                EXECUTE _qry;
                RAISE LOG '%: CRS ''%'' has been migrated', ME, _tup.row[1];
            EXCEPTION
                WHEN unique_violation THEN  -- ignore: if the CRS is already stored, this is not a problem.
                    RAISE DEBUG '%: CRS ''%'' is already stored in %.', ME, _tup.row[1], cget('TABLE_PS9_CRS');
            END;
        END LOOP;
        RAISE LOG '%: available CRSs have been migrated.', ME;
        RETURN;
    END;
$$ LANGUAGE plpgsql;

--
-- Copy the UoM codes possibly inserted by the user
--
-- TODO : migrate_quantities() instead and insert allowed intervals as well!
--
CREATE OR REPLACE FUNCTION migrate_uoms ()
RETURNS void AS
$$
    DECLARE
        -- Log
        ME constant text := 'migrate_uoms()';
        -- Local variables
        _qry text;
        _tup record;
    BEGIN
        -- Select all the UoM (except )
        _qry := ' SELECT DISTINCT ARRAY[' || quote_ident(cget('PS_UOM_UOM')) || ','
                          || quote_ident(cget('PS_UOM_LINK')) || '] AS row ' ||
                ' FROM '  || quote_ident(cget('TABLE_PS_UOM')) ||
                ' WHERE ' || quote_ident(cget('PS_UOM_UOM'))   || '!~''10'   || chr(x'2070'::int) || '''' ||
                ' AND '   || quote_ident(cget('PS_UOM_UOM'))   || '!=''1''' ;
	RAISE DEBUG '%: EXECUTING %', ME, _qry;
	FOR _tup IN EXECUTE _qry LOOP
            BEGIN
                -- Avoid NULL from being concatenated
                IF _tup.row[2] IS NULL THEN
                   _tup.row[2] = '';
                END IF;

                RAISE LOG '%: migrating (%,%) unit of measure...', ME, _tup.row[1], _tup.row[2];
                -- UoM
                _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_UOM')) ||
                                  ' ( ' || quote_ident(cget('PS9_UOM_CODE'))  || ' ) ' ||
                           ' VALUES ( ' || quote_literal( _tup.row[1])        || ' ) ';
	        RAISE DEBUG '%: EXECUTING %', ME, _qry;
		EXECUTE _qry;
                -- Quantity
                _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_QUANTITY'))  ||
                                  ' ( ' || quote_ident(cget('PS9_QUANTITY_UOM_ID')) || ','
                                        || quote_ident(cget('PS9_QUANTITY_URI'))    || ' ) ' ||
                        ' VALUES ( '    ||
                                        select_field(
                                          cget('TABLE_PS9_UOM'),
                                          cget('PS9_UOM_ID'), 0,
                                          ' WHERE ' || quote_ident(cget('PS9_UOM_CODE')) || '='
                                                    || quote_literal(_tup.row[1]) )
                                          ||  ' , ' || quote_literal(_tup.row[2])        || ')';
	        RAISE DEBUG '%: EXECUTE %', ME, _qry;
                EXECUTE _qry;
            EXCEPTION
                -- There is a trigger in ps9_uom checking the UoM has a legal pattern (no white spaces, newlines, etc.)
                WHEN raise_exception THEN
                    RAISE WARNING '%: ''%'' is not a valid UoM (%), it will not be migrated.', ME, cget('UOM_PATTERN'), _tup.row[1];
                WHEN unique_violation THEN
                    RAISE WARNING '%: ''%'' is already present in %, will ignore it.', ME, _tup.row[1], cget('TABLE_PS9_UOM');
            END;
        END LOOP;
        RAISE LOG '%: UoMs have been migrated.', ME;
        RETURN;
    END;
$$ LANGUAGE plpgsql;
-- CHECK: SELECT DISTINCT u.id, u.code, q.definition_uri FROM ps9_uom AS u, ps9_quantity AS q WHERE u.id=q.uom_id;


--
-- Function to migrate a single coverage
---
CREATE OR REPLACE FUNCTION migrate_coverage (
    coverage_name           text,
    native_format_id        integer,
    gmlcov_metadata_type_id integer
) RETURNS boolean AS
$$
    DECLARE
        -- Log
        ME constant text := 'migrate_coverage()';
        -- Local variables
        _coverage_id   integer; -- constant
        _coverage9_id  integer; -- constant
        _gml_type_id   integer; -- constant
        _native_crs    integer[];
        _grid_origin   numeric[];
        _abs_origin_offset  text;
        _offset_vector numeric[];
        _grid_axis_id  integer;
        _rascoll_id    integer;
        _quantity_id   integer;
        _datatype_id   integer;
        _crs_axis_counter  integer;
        _previous_crs      text;
        _previous_axistype text;
        _first_oid         numeric;
        --
        _qry text;
        _tup record;
        _log text;
    BEGIN
        -- Fetch the coverage (PS_) id
	_coverage_id := select_field(
                          cget('TABLE_PS_COVERAGE'),
                          cget('PS_COVERAGE_ID'), 0,
                          ' WHERE ' || quote_ident(cget('PS_COVERAGE_NAME')) || '='
                                    || quote_literal(coverage_name)
        );
        RAISE DEBUG '%: corresponding % in % is ''%''', ME, cget('PS_COVERAGE_ID'), cget('TABLE_PS_COVERAGE'), _coverage_id;

        -- Fetch the GML type of the coverage
        _qry := ' SELECT ARRAY[' || quote_ident(cget('PS_COVERAGE_TYPE'))  || '] AS row '
                        ' FROM ' || quote_ident(cget('TABLE_PS_COVERAGE')) ||
                       ' WHERE ' || quote_ident(cget('PS_COVERAGE_NAME'))  || '=' || quote_literal(coverage_name);
        RAISE DEBUG '%: EXECUTING %', ME, _qry;
        EXECUTE _qry INTO STRICT _tup;
        RAISE LOG '%: ''%'' is of type ''%''', ME, coverage_name, _tup.row[1];

        -- Additional check that this is a gridded coverage
        IF _tup.row[1] NOT LIKE '%Grid%' THEN
            _log := 'this dos not seem a gridded coverage; GML type is "' || _tup.row[1] || '". Please fix and retry.';
            RAISE WARNING '%: %', ME, _log;
            -- to temporary table for final report
            PERFORM cset(coverage_name, _log);
            RETURN false;
        END IF;

        -- Fetch the GML type id
        BEGIN
            _gml_type_id := select_field (
                              cget('TABLE_PS9_GML_SUBTYPE'),
                              cget('PS9_GML_SUBTYPE_ID'), 0,
                              ' WHERE ' || quote_ident(cget('PS9_GML_SUBTYPE_SUBTYPE'))
                                 || '=' || quote_literal(_tup.row[1])
            );
            RAISE DEBUG '%: ''%'' has id %.', ME, _tup.row[1], _gml_type_id;
        EXCEPTION
            WHEN no_data_found THEN
                _log := 'GML type "' || _tup.row[1] || '" does not exist, skipping coverage migration.';
                RAISE WARNING '%: %', ME, _log;
                -- to temporary table for final report
                PERFORM cset(coverage_name, _log);
                RETURN false;
        END; --~// ps9_gml_subtype

        -- Fill ps9_coverage with name, GML type and native format
        BEGIN
            _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_COVERAGE'))       ||
                              ' ( ' || quote_ident(cget('PS9_COVERAGE_NAME'))        || ','
                                    || quote_ident(cget('PS9_COVERAGE_GML_TYPE_ID')) || ','
                                    || quote_ident(cget('PS9_COVERAGE_FORMAT_ID'))   || ' ) '  ||
                        ' VALUES (' || quote_literal(coverage_name) || ', '
                                    || _gml_type_id     || ','
                                    || native_format_id || ')';
            RAISE DEBUG '%: EXECUTING %', ME, _qry;
            EXECUTE _qry;
        EXCEPTION
            -- If a coverage with the same name is already stored in the new tables
            WHEN unique_violation THEN
                _log := 'coverage "' || coverage_name || '" already exists in ' || cget('TABLE_PS9_COVERAGE') || ': discarding it.';
                RAISE WARNING '%: %', ME, _log;
                -- to temporary table for final report
                PERFORM cset(coverage_name, _log);
                RETURN false;
        END; --~// ps9_coverage

        -- Fetch (PS9_) id of newly inserted coverage
        _coverage9_id := select_field(
                           cget('TABLE_PS9_COVERAGE'),
                           cget('PS9_COVERAGE_ID'), 0,
                           ' WHERE ' || quote_ident(cget('PS9_COVERAGE_NAME')) || '='
                                     || quote_literal(coverage_name)
        );
        RAISE LOG '%: coverage ''%'' has been inserted in % with id %.', ME, coverage_name, cget('TABLE_PS9_COVERAGE'), _coverage9_id;

        -- Retrieve possible extra GMLCOV metadata:
        _qry := ' SELECT ARRAY[' || quote_ident(cget('PS_METADATA_METADATA')) || '] AS row '
                        ' FROM ' || quote_ident(cget('TABLE_PS_METADATA'))    ||
                       ' WHERE ' || quote_ident(cget('PS_METADATA_COVERAGE')) || '=' || _coverage_id;
        RAISE DEBUG '%: EXECUTING %', ME, _qry;

        -- Add (possibly multiple) gmlcov:metadata
        -- <element ref="gmlcov:metadata" minOccurs="0" maxOccurs="unbounded"/>
        FOR _tup IN EXECUTE _qry LOOP
            -- Add this metadata
            _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_EXTRA_METADATA'))       ||
                              ' ( ' || quote_ident(cget('PS9_EXTRA_METADATA_COVERAGE_ID')) ||  ','
                                    || quote_ident(cget('PS9_EXTRA_METADATA_TYPE'))        ||  ','
                                    || quote_ident(cget('PS9_EXTRA_METADATA_VALUE'))       || ' ) ' ||
                        ' VALUES (' || _coverage9_id           || ','
                                    || gmlcov_metadata_type_id || ','
                                    || quote_literal(_tup.row[1])     || ')';
            EXECUTE _qry;
            RAISE LOG '%: GMLCOV metadata ''%..'' of coverage ''%'' has been migrated.',
                         ME, substring(_tup.row[1],0,10), coverage_name;
        END LOOP; --~// ps9_extra_metadata

        -- Build the native CRS (can be compound):
        -- select CRS identifiers per each axis then need to turn CRS:1 to Index CRSS
        -- axis order/type  |   CRS   | CRS URI
        --       0/t        |   .*    |  1= %SECORE_URL%/crs/OGC/0/AnsiDate
        --       1/x        |   *4326 |  2= %SECORE_URL%/crs/EPSG/0/4326
        --       2/y        |   *4326 |
        --       3/other    |   CRS:1 |  3= %SECORE_URL%/crs/OGC/0/Index2D
        --       4/other    |   CRS:1 |
        _qry := ' SELECT ARRAY[' || quote_ident(cget('TABLE_PS_CRS'))      || '.'
                                 || quote_ident(cget('PS_CRS_NAME'))       || ','
                                 || quote_ident(cget('TABLE_PS_AXISTYPE')) || '.'
                                 || quote_ident(cget('PS_AXISTYPE_TYPE'))  || '] AS row ' ||
                  ' FROM ' || quote_ident(cget('TABLE_PS_DOMAIN'))   || ','
                           || quote_ident(cget('TABLE_PS_CRS'))      || ','
                           || quote_ident(cget('TABLE_PS_CRSSET'))   || ','
                           || quote_ident(cget('TABLE_PS_AXISTYPE')) ||
                 ' WHERE ' || quote_ident(cget('TABLE_PS_DOMAIN'))   || '.'  || quote_ident(cget('PS_DOMAIN_ID'))       || '='
                           || quote_ident(cget('TABLE_PS_CRSSET'))   || '.'  || quote_ident(cget('PS_CRSSET_AXIS'))     ||
                   ' AND ' || quote_ident(cget('TABLE_PS_CRS'))      || '.'  || quote_ident(cget('PS_CRS_ID'))          || '='
                           || quote_ident(cget('TABLE_PS_CRSSET'))   || '.'  || quote_ident(cget('PS_CRSSET_CRS'))      ||
                   ' AND ' || quote_ident(cget('TABLE_PS_DOMAIN'))   || '.'  || quote_ident(cget('PS_DOMAIN_TYPE'))     || '='
                           || quote_ident(cget('TABLE_PS_AXISTYPE')) || '.'  || quote_ident(cget('PS_AXISTYPE_ID'))     ||
                   ' AND ' || quote_ident(cget('TABLE_PS_DOMAIN'))   || '.'  || quote_ident(cget('PS_DOMAIN_COVERAGE')) || '='''
                           || _coverage_id                           || '''' ||
              ' ORDER BY ' || quote_ident(cget('TABLE_PS_DOMAIN'))   || '.'  || quote_ident(cget('PS_DOMAIN_I'))        || ' ASC'
              ;
        RAISE DEBUG '%: EXECUTING %', ME, _qry;
        -- Build the array of CRS FKs
        BEGIN
            -- init
            _crs_axis_counter := 1;
            _previous_crs := '';
            -- loop through the CRS names
            FOR _tup IN EXECUTE _qry LOOP
                -- first loop
                IF _previous_crs = '' THEN
                   _previous_crs := _tup.row[1];
                   _previous_axistype := _tup.row[2];
                -- further loops:
                ELSIF _previous_crs = _tup.row[1] AND _previous_axistype <> cget('TIME_AXIS_TYPE') THEN
                    -- continue to the next tuple and increase the counter: I need it to understand which Index CRS to assign (if CRS:1)
                    -- NOTE: temporal CRS have max 1 axis, but can have any CRS (also CRS:1) in the old schema
                    _crs_axis_counter := _crs_axis_counter + 1;
                ELSE
                    -- CRS/axis_type has changed: action
                    -- CRS:1 to IndexND and time axis to OGC:AnsiDate
                    _previous_crs := translate_crs(_previous_crs, _previous_axistype, _crs_axis_counter);
                    -- add CRS to array of CRS ids
                    RAISE DEBUG '%: appending ''%'' to native CRS..', ME, _previous_crs;
                    _native_crs := array_append(_native_crs, select_field(
                            cget('TABLE_PS9_CRS'),
                            cget('PS9_CRS_ID'), 0,
                            ' WHERE ' || quote_ident(cget('PS9_CRS_URI')) || '='
                                      || quote_literal(_previous_crs)
                        )
                    );
                    -- shift-tuple/reset
                    _previous_crs      := _tup.row[1];
                    _previous_axistype := _tup.row[2];
                    _crs_axis_counter  := 1;
                END IF;
            END LOOP; --~// native CRS array
            -- add CRS for the last tuple
            -- CRS:1 to IndexND and time axis to OGC:AnsiDate
            _previous_crs := translate_crs(_previous_crs, _previous_axistype, _crs_axis_counter);
            -- add to array of ids
            RAISE DEBUG '%: appending ''%''...', ME, _previous_crs;
            _native_crs := array_append(_native_crs, select_field(
                    cget('TABLE_PS9_CRS'),
                    cget('PS9_CRS_ID'), 0,
                    ' WHERE ' || quote_ident(cget('PS9_CRS_URI')) || '='
                              || quote_literal(_previous_crs)
                )
            );
        EXCEPTION
            WHEN no_data_found THEN
                _log := 'CRS "' || _previous_crs || '" has not been migrated (not an URI?). Please fix it then retry.';
                RAISE WARNING '%: %', ME, _log;
                -- to temporary table for final report
                PERFORM cset(coverage_name, _log);
                RETURN false;
        END;

        -- Finally, set the native CRS for this coverage
        _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_DOMAINSET'))       ||
                          ' ( ' || quote_ident(cget('PS9_DOMAINSET_COVERAGE_ID')) || ','
                                || quote_ident(cget('PS9_DOMAINSET_CRS_IDS'))      || ' ) '   ||
                    ' VALUES (' || _coverage9_id || ',''' || _native_crs::text    || ''')';
        RAISE DEBUG '%: EXECUTING %', ME, _qry;
        EXECUTE _qry;
        RAISE LOG '%: set CRS ''%'' for coverage ''%''.', ME, _native_crs, coverage_name;

        -- Determine the origin of the gridded coverage: order tuple of coordinates by their order in the _CRS_ definition
        -- so for instance in case of EPGS:4326 _x and y values are swapped.
        -- Grid points are pixel-centre so ORIGIN = numlo/numhi +/- 0.5*res
        -- In the indexed CRS space, the sample space (pixel) is reduced to a 0D point, so there is no origin shift.
        -- For time axis, CRS:1 is not to be account for, since it will be assigned AnsiDate CRS, which is not indexed.
        --
        -- Example of query for eobstest (t+WGS84):
        -- SELECT ARRAY[
        --   (ps_domain.i + (
        --     ((NOT defines_northing_first(gridaxis_crs(<cov_id>,ps_domain.i)))::int) * 0 +
        --     ((    defines_northing_first(gridaxis_crs(<cov_id>,ps_domain.i)))::int) *
        --      (-1^((get_axis_type(ps_domain.type)='y')::int))
        --   )),(
        --     CASE get_axis_type(ps_domain.type) WHEN 'y'
        --          THEN ps_domain.numhi - (%abs_origin_offset%)
        --          ELSE ps_domain.numlo + (%abs_origin_offset%)
        --     END
        -- )] AS row
        --     FROM ps_domain, ps_celldomain
        --    WHERE ps_domain.coverage     = <cov_id>
        --      AND ps_celldomain.coverage = <cov_id>
        -- ORDER BY row ASC;
        --
        --    row
        -- ----------
        --  {0,0}     -> t min
        --  {1,75.5}  -> y max
        --  {2,25}    -> x min
        -- (3 rows)
        --
        -- NOTE: CRS axis order needs to be the first element in the outpout ARRAY for ORDER BY to be effective.
        -- _abs_origin_offset = (((quote_literal(gridaxis_crs(<cov_id>,i)) = quote_literal('CRS:1') AND
        --                         get_axis_type(ps_domain.type)          <> 't')::int) * 0 +
        --                      ((quote_literal(gridaxis_crs(<cov_id>,i)) != quote_literal('CRS:1') OR
        --                         get_axis_type(ps_domain.type)           = 't')::int) * (
        --                      ((numhi - numlo) / (hi - lo + 1)) / 2))
        _abs_origin_offset := '(((quote_literal(gridaxis_crs(' || _coverage_id || ','
                               || quote_ident(cget('TABLE_PS_DOMAIN'))         || '.'
                               || quote_ident(cget('PS_DOMAIN_I'))
                               || ')) = quote_literal(''' || cget('CRS_1')|| ''') AND get_axis_type('
                               || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.'
                               || quote_ident(cget('PS_DOMAIN_TYPE'))     || ')<>'
                               || quote_literal(cget('TIME_AXIS_TYPE'))   || ')::int) * 0 + (('
                               || 'quote_literal(gridaxis_crs(' || _coverage_id  || ','
                               || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.' || quote_ident(cget('PS_DOMAIN_I'))
                               || ')) != quote_literal(''' || cget('CRS_1')      || ''')  OR get_axis_type('
                               || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.'
                               || quote_ident(cget('PS_DOMAIN_TYPE'))     || ')='
                               || quote_literal(cget('TIME_AXIS_TYPE'))   || ')::int) * ((('
                               || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.'
                               || quote_ident(cget('PS_DOMAIN_NUMHI'))    || '-'
                               || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.'
                               || quote_ident(cget('PS_DOMAIN_NUMLO'))    || ')/('
                               || quote_ident(cget('TABLE_PS_CELLDOMAIN'))|| '.'
                               || quote_ident(cget('PS_CELLDOMAIN_HI'))   || '-'
                               || quote_ident(cget('TABLE_PS_CELLDOMAIN'))|| '.'
                               || quote_ident(cget('PS_CELLDOMAIN_LO'))   || '+1))/2))'
                               ;
        RAISE DEBUG '%: absolute origin offset string %', ME, _abs_origin_offset;
        _qry := ' SELECT ARRAY[(' || quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                  || quote_ident(cget('PS_DOMAIN_I'))     ||
                         ' + ((( NOT defines_northing_first(gridaxis_crs('|| _coverage_id     || ','
                                  || quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                  || quote_ident(cget('PS_DOMAIN_I'))     || ')))::int) * 0 ' ||
                               ' + ((defines_northing_first(gridaxis_crs('|| _coverage_id     || ','
                                  || quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                  || quote_ident(cget('PS_DOMAIN_I'))     || ')))::int) * (-1^((get_axis_type('
                                  || quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                  || quote_ident(cget('PS_DOMAIN_TYPE'))  || ')='
                                  || quote_literal(cget('Y_AXIS_TYPE'))   || ')::int)))),' ||
                         '(CASE ' || quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                  || quote_ident(cget('PS_DOMAIN_NAME'))  || ' WHEN '
                                  || quote_literal(cget('Y_AXIS_TYPE'))   ||
                         ' THEN ' || quote_ident(cget('PS_DOMAIN_NUMHI')) || '-' || _abs_origin_offset ||
                         ' ELSE ' || quote_ident(cget('PS_DOMAIN_NUMLO')) || '+' || _abs_origin_offset || ' END)] AS row ' ||
                      ' FROM ' || quote_ident(cget('TABLE_PS_DOMAIN'))    || ','
                               || quote_ident(cget('TABLE_PS_CELLDOMAIN'))||
                     ' WHERE ' || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.'
                               || quote_ident(cget('PS_DOMAIN_COVERAGE')) || '=' || _coverage_id ||
                       ' AND ' || quote_ident(cget('TABLE_PS_CELLDOMAIN'))       || '.'
                               || quote_ident(cget('PS_CELLDOMAIN_COVERAGE'))    || '=' || _coverage_id ||
                       ' AND ' || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.'
                               || quote_ident(cget('PS_DOMAIN_I'))        || '='
                               || quote_ident(cget('TABLE_PS_CELLDOMAIN'))|| '.'
                               || quote_ident(cget('PS_CELLDOMAIN_I'))    || ' ORDER BY row ASC'
                               ;
        RAISE DEBUG '%: EXECUTING %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            _grid_origin := array_append(_grid_origin, _tup.row[2]::numeric);
        END LOOP; --~// grid origin array

        -- Finally, set the origin for this coverage
        _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_GRIDDED_DOMAINSET'))          ||
                           ' (' || quote_ident(cget('PS9_GRIDDED_DOMAINSET_COVERAGE_ID'))    || ','
                                || quote_ident(cget('PS9_GRIDDED_DOMAINSET_ORIGIN'))         || ') '  ||
                    ' VALUES (' || _coverage9_id || ',''' || _grid_origin::text || ''') ';
        RAISE DEBUG '%: EXECUTING %', ME, _qry;
        EXECUTE _qry;
        RAISE LOG '%: set origin ''%'' for coverage ''%''.', ME, _grid_origin, coverage_name;

        -- Get the resolution (offset vectors) of each axis of this coverage [DIM,i,res].
        -- This query needs to account for two special cases:
        --   1. CRS:1
        --      The formula for the axis resolution [(dom.hi-dom.lo)/(cdom.hi-cdom.lo+1)] is not valid for indexed coverages:
        --      the term `+1' in the denominator would need to be left out; in order to be more robust on
        --      possibly wrong values in ps_domain (rasimport was sometimes putting 0.0 and 1.0), when CRS:1
        --      is found, resolution is directly  set to -1^(axis==y) without computing the formula.
        --   2. CRSs which define northings first, then eastings (like EPSG:4326 and many other CRSs)
        --      In this case the order of rasdaman axis and CRS axis needs to be swapped, since latitude comes
        --      first in the CRS definition.
        --
        --      Example of query+output in case of 0.5-degree resolution latlong 2D coverage:
        --
        --      SELECT ARRAY[(SELECT COUNT(i) FROM ps_domain WHERE coverage=1), ps_domain.i,
        --      (
        --        ps_domain.i + (
        --        ((NOT defines_northing_first(gridaxis_crs(1,ps_domain.i)))::int) * 0 +
        --        ((    defines_northing_first(gridaxis_crs(1,ps_domain.i)))::int) *
        --         (-1^((get_axis_type(ps_domain.type)='y')::int))
        --      )),(
        --         (-1^((get_axis_type(ps_domain.type)='y')::int)) *
        --        ((quote_literal(gridaxis_crs(1,ps_domain.i))  = quote_literal('CRS:1'))::int) +
        --        ((-1^((get_axis_type(ps_domain.type)='y')::int)) * (numhi - numlo) / (hi - lo + 1)) *
        --        ((quote_literal(gridaxis_crs(1,ps_domain.i)) != quote_literal('CRS:1'))::int)
        --      )] AS row FROM ps_domain,ps_celldomain
        --               WHERE ps_domain.coverage     = <cov_id>
        --                 AND ps_celldomain.coverage = <cov_id>
        --                 AND ps_domain.i = ps_celldomain.i
        --      ORDER BY ps_domain.i ASC;
        --
        --        row
        --    ------------
        --     {2,0,1,0.5}
        --     {2,1,0,-0.5}
        --    (2 rows)
        -- OUTPUT: [#dimensions, gridaxis_order, crsaxis_order, offset_vector
        --
        -- NOTE: quote_literal needs to be explicitly in the query since gridaxis_crs is paremetrized on the ps_domain.i value
        --       hence cannot be computed here while building the query. When comparing string, *both* need quote_literal to
        --       make the comparison work. See e.g. ``SELECT quote_literal(0) = '0'; ''.
        _qry := ' SELECT ARRAY[' || '(SELECT COUNT(' || quote_ident(cget('PS_DOMAIN_I'))
                                 ||        ') FROM ' || quote_ident(cget('TABLE_PS_DOMAIN'))
                                 ||        ' WHERE ' || quote_ident(cget('PS_DOMAIN_COVERAGE')) || '=' || _coverage_id || '),'
                                 || quote_ident(cget('TABLE_PS_DOMAIN'))  || '.'
                                 || quote_ident(cget('PS_DOMAIN_I'))      || ', ('
                                 || quote_ident(cget('TABLE_PS_DOMAIN'))  || '.'
                                 || quote_ident(cget('PS_DOMAIN_I'))      || ' + (((NOT '
                                 || 'defines_northing_first(gridaxis_crs('|| _coverage_id || ','
                                 ||  quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                 ||  quote_ident(cget('PS_DOMAIN_I'))     || ')))::int) * 0 + (('
                                 || 'defines_northing_first(gridaxis_crs('|| _coverage_id || ','
                                 ||  quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                 ||  quote_ident(cget('PS_DOMAIN_I'))     || ')))::int) * (-1^((get_axis_type('
                                 || quote_ident(cget('TABLE_PS_DOMAIN'))  || '.'
                                 || quote_ident(cget('PS_DOMAIN_TYPE'))   || ')='
                                 || quote_literal(cget('Y_AXIS_TYPE'))    || ')::int)))), ((-1^((get_axis_type('
                                 || quote_ident(cget('TABLE_PS_DOMAIN'))  || '.'
                                 || quote_ident(cget('PS_DOMAIN_TYPE'))   || ')='
                                 || quote_literal(cget('Y_AXIS_TYPE'))    || ')::int)) * (('
                                 || 'quote_literal(gridaxis_crs('|| _coverage_id || ','
                                 ||  quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                 ||  quote_ident(cget('PS_DOMAIN_I'))     || ')) = '
                                 || 'quote_literal(''' || cget('CRS_1')   || '''))::int) + ((-1^((get_axis_type('
                                 || quote_ident(cget('TABLE_PS_DOMAIN'))  || '.'
                                 || quote_ident(cget('PS_DOMAIN_TYPE'))   || ')='
                                 || quote_literal(cget('Y_AXIS_TYPE'))    || ')::int)) * ' ||
                            ' (' || quote_ident(cget('PS_DOMAIN_NUMHI'))  || '-'
                                 || quote_ident(cget('PS_DOMAIN_NUMLO'))  || ') / '        ||
                             '(' || quote_ident(cget('PS_CELLDOMAIN_HI')) || '-'
                                 || quote_ident(cget('PS_CELLDOMAIN_LO')) || ' + 1)) * (('
                                 -- IF CRS:1 THEN domain=cellDomain --> `+1' term to be dropped
                                 || 'quote_literal(gridaxis_crs('|| _coverage_id || ','
                                 ||  quote_ident(cget('TABLE_PS_DOMAIN')) || '.'
                                 ||  quote_ident(cget('PS_DOMAIN_I'))     || ')) != '
                                 || 'quote_literal(''' || cget('CRS_1')   || '''))::int))] AS row ' || -- T->1, F->0
                        ' FROM ' || quote_ident(cget('TABLE_PS_DOMAIN'))        || ','
                                 || quote_ident(cget('TABLE_PS_CELLDOMAIN'))    ||
                       ' WHERE ' || quote_ident(cget('TABLE_PS_DOMAIN'))        || '.'
                                 || quote_ident(cget('PS_DOMAIN_COVERAGE'))     || '=' || _coverage_id ||
                         ' AND ' || quote_ident(cget('TABLE_PS_CELLDOMAIN'))    || '.'
                                 || quote_ident(cget('PS_CELLDOMAIN_COVERAGE')) || '=' || _coverage_id ||
                         ' AND ' || quote_ident(cget('TABLE_PS_DOMAIN')) || '.' || quote_ident(cget('PS_DOMAIN_I'))     || '='
                                 || quote_ident(cget('TABLE_PS_CELLDOMAIN'))    || '.' || quote_ident(cget('PS_CELLDOMAIN_I')) ||
                    ' ORDER BY ' || quote_ident(cget('TABLE_PS_DOMAIN')) || '.' || quote_ident(cget('PS_DOMAIN_I'))     || ' ASC';
        RAISE DEBUG '%: EXECUTING %', ME, _qry;
        -- Finally, set the offset vectors for the coverage
        FOR _tup IN EXECUTE _qry LOOP

            BEGIN
                -- Insert the axis in ps9_grid_axis
                _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_GRID_AXIS'))          ||
                                   ' (' || quote_ident(cget('PS9_GRID_AXIS_COVERAGE_ID'))    || ','
                                        || quote_ident(cget('PS9_GRID_AXIS_RASDAMAN_ORDER')) || ') ' ||
                            ' VALUES (' || _coverage9_id || ',' || _tup.row[2]   || ')';
                RAISE DEBUG '%: EXECUTING %', ME, _qry;
                EXECUTE _qry;
            EXCEPTION
                WHEN raise_exception THEN
                    _log := 'coverage axes order seems illegal (must be sequential from 0), please fix then retry.';
                    RAISE WARNING '%: %', ME, _log;
                    -- to temporary table for final report
                    PERFORM cset(coverage_name, _log);
                    RETURN false;
            END;

            -- Fetch the id of the previously inserted grid axis
            _grid_axis_id := select_field(
                               cget('TABLE_PS9_GRID_AXIS'),
                               cget('PS9_GRID_AXIS_ID'), 0,
                              ' WHERE ' || quote_ident(cget('PS9_GRID_AXIS_COVERAGE_ID'))    || '=' || _coverage9_id ||
                                ' AND ' || quote_ident(cget('PS9_GRID_AXIS_RASDAMAN_ORDER')) || '=' || _tup.row[2]
            );
            RAISE DEBUG '%: inserted axis has id %.', ME, _grid_axis_id;

            BEGIN
                -- Build the offset vector from the scalar resolution: res -> (0,__,res,__,0)
                _offset_vector := resolution2vector(
                                    _tup.row[4]::numeric,  -- resolution
                                    _tup.row[3]::integer,  -- axis order (in the CRS!)
                                    _tup.row[1]::integer); -- dimensions
            EXCEPTION
                WHEN raise_exception THEN
                    _log := 'failed to create the offset vector for axis ' || _tup.row[2] || ' of coverage "' || coverage_name || '".';
                    RAISE WARNING '%: %', ME, _log;
                    -- to temporary table for final report
                    PERFORM cset(coverage_name, _log);
                    RETURN false;
            END;

            BEGIN
                -- Finally, set the offset vector for this axis
                _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_RECTILINEAR_AXIS'))         ||
                                    '(' || quote_ident(cget('PS9_RECTILINEAR_AXIS_ID'))            || ','
                                        || quote_ident(cget('PS9_RECTILINEAR_AXIS_OFFSET_VECTOR')) || ') ' ||
                            ' VALUES (' || _grid_axis_id || ',''' || _offset_vector::text || ''')';
                RAISE DEBUG '%: EXECUTING %', ME, _qry;
                EXECUTE _qry;
                RAISE LOG '%: set offset vector ''%'' for axis % of coverage ''%''.',
                             ME, _offset_vector, _tup.row[2], coverage_name;
            EXCEPTION
                WHEN raise_exception THEN
                    _log := 'offset vector dimensionality seems incompatible with coverage origin.';
                    RAISE WARNING '%: %', ME, _log;
                    -- to temporary table for final report
                    PERFORM cset(coverage_name, _log);
                    RETURN false;
            END;
        END LOOP;

        --
        -- range set
        --
        -- Fetch the OID of this coverage from RASBASE = ras_mddobjects.mddid*512+1
        -- TODO: add installation of additional PG libraries for `dblink' function
        -- dblink usage: SELECT * FROM dblink('<connection>', '<query>') AS foo(<colname> <coltype>, ...);
        _qry := 'SELECT ARRAY[oid] AS row FROM dblink( ''dbname=' || cget('DB_RASBASE') || ''',$q$' ||
                  ' SELECT 1+(512*' || quote_ident(cget('TABLE_RAS_MDD_OBJECTS'))      || '.'
                                    || quote_ident(cget('RAS_MDD_OBJECTS_MDDID'))      || ') ' ||
                           ' FROM ' || quote_ident(cget('TABLE_RAS_MDD_OBJECTS'))      || ','
                                    || quote_ident(cget('TABLE_RAS_MDD_COLLNAMES'))    || ','
                                    || quote_ident(cget('TABLE_RAS_MDD_COLLECTIONS'))  ||
                          ' WHERE ' || quote_ident(cget('TABLE_RAS_MDD_OBJECTS'))      || '.'
                                    || quote_ident(cget('RAS_MDD_OBJECTS_MDDID'))      || '='
                                    || quote_ident(cget('TABLE_RAS_MDD_COLLECTIONS'))  || '.'
                                    || quote_ident(cget('RAS_MDD_COLLECTIONS_MDDID'))  ||
                            ' AND ' || quote_ident(cget('TABLE_RAS_MDD_COLLECTIONS'))  || '.'
                                    || quote_ident(cget('RAS_MDD_COLLECTIONS_MDDCOLLID')) || '='
                                    || quote_ident(cget('TABLE_RAS_MDD_COLLNAMES'))    || '.'
                                    || quote_ident(cget('RAS_MDD_COLLNAMES_COLLID'))   ||
                            ' AND ' || quote_ident(cget('RAS_MDD_COLLNAMES_COLLNAME')) || '='
                                    || quote_literal(coverage_name) || ' ' ||
                       ' ORDER BY ' || quote_ident(cget('TABLE_RAS_MDD_OBJECTS'))      || '.'
                                    || quote_ident(cget('RAS_MDD_OBJECTS_MDDID'))      || '$q$) AS rec(oid numeric)';
        RAISE DEBUG '%: EXECUTING %', ME, _qry;

        -- If >1 MDD per collection, I raise a warning: must be only one per /coverage/ (the lower one is taken)
        FOR _tup IN EXECUTE _qry LOOP

            -- If more than 1 OID send WARNING but the continue
            IF _first_oid IS NOT NULL THEN
                _log := 'collection ' || coverage_name || ' has multiple MDDs: only OID '
                                      || _first_oid    || ' will be set for the coverage.';
                RAISE WARNING '%: %', ME, _log;
                -- to temporary table for final report
                PERFORM cset(coverage_name, _log);
                EXIT;
            END IF;
            _first_oid := _tup.row[1];

            -- Insert the rasdaman collection in the database
            _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_RASDAMAN_COLLECTION')) ||
                               ' (' || quote_ident(cget('PS9_RASDAMAN_COLLECTION_NAME'))  || ','
                                    || quote_ident(cget('PS9_RASDAMAN_COLLECTION_OID'))   || ') ' ||
                        ' VALUES (' || quote_literal(coverage_name) || ','
                                    || _first_oid    || ')';
            RAISE DEBUG '%: EXECUTING %', ME, _qry;
            EXECUTE _qry;

            -- Fetch the ID of the inserted rasdaman collection tuple
            _rascoll_id := select_field(
                             cget('TABLE_PS9_RASDAMAN_COLLECTION'),
                             cget('PS9_RASDAMAN_COLLECTION_ID'), 0,
                             ' WHERE ' || quote_ident(cget('PS9_RASDAMAN_COLLECTION_NAME')) || '='
                                       || quote_literal(coverage_name)
            );

            -- Finally, set the range set for the coverage
            _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_RANGESET'))       ||
                               ' (' || quote_ident(cget('PS9_RANGESET_COVERAGE_ID')) || ','
                                    || quote_ident(cget('PS9_RANGESET_STORAGE_ID'))  || ') ' ||
                        ' VALUES (' || _coverage9_id || ',' || _rascoll_id || ')';
            RAISE DEBUG '%: EXECUTING %', ME, _qry;
            EXECUTE _qry;
            RAISE LOG '%: range-set linked to (%,%) rasdaman MDD for coverage ''%''.',
                         ME, coverage_name, _tup.row[1], coverage_name;
        END LOOP;

        --
        -- range type
        --
        -- Retrieve bands' details of the coverage
        -- NOTE: in the new schema, range type components are separate from independent quantities.
        --       since an independent quantity label was missing, the datatype has been migrated
        --       to the quantity `description', and this will be used to properly bind range component to the quantity.
        _qry := ' SELECT ARRAY[' || quote_ident(cget('TABLE_PS_RANGE'))    || '.' || quote_ident(cget('PS_RANGE_I'))       || '::text,'
                                 || quote_ident(cget('TABLE_PS_RANGE'))    || '.' || quote_ident(cget('PS_RANGE_NAME'))    || ','
                                 || quote_ident(cget('TABLE_PS_DATATYPE')) || '.' || quote_ident(cget('PS_DATATYPE_TYPE')) || ','
                                 || quote_ident(cget('TABLE_PS_UOM'))      || '.' || quote_ident(cget('PS_UOM_UOM'))       || '] as row ' ||
                        ' FROM ' || quote_ident(cget('TABLE_PS_RANGE')) ||
                               ' LEFT OUTER JOIN ' || quote_ident(cget('TABLE_PS_UOM'))   ||
                                            ' ON ' || quote_ident(cget('TABLE_PS_UOM'))   || '.'
                                                   || quote_ident(cget('PS_UOM_ID'))      || '='
                                                   || quote_ident(cget('TABLE_PS_RANGE')) || '.'
                                                   || quote_ident(cget('PS_RANGE_UOM'))   ||
                               ' INNER JOIN ' || quote_ident(cget('TABLE_PS_DATATYPE'))   ||
                                       ' ON ' || quote_ident(cget('TABLE_PS_DATATYPE'))   || '.'
                                              || quote_ident(cget('PS_DATATYPE_ID'))      || '='
                                              || quote_ident(cget('TABLE_PS_RANGE'))      || '.'
                                              || quote_ident(cget('PS_RANGE_TYPE'))       ||
                      ' WHERE ' || quote_ident(cget('TABLE_PS_RANGE'))    || '.'
                                || quote_ident(cget('PS_RANGE_COVERAGE')) || '=' || _coverage_id ||
                   ' ORDER BY ' || quote_ident(cget('TABLE_PS_RANGE'))    || '.' || quote_ident(cget('PS_RANGE_I'));
        RAISE DEBUG '%: EXECUTING %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP

            -- Get the ID of the range data type
            _datatype_id := select_field(
                              cget('TABLE_PS9_RANGE_DATATYPE'),
                              cget('PS9_RANGE_DATATYPE_ID'), 0,
                              ' WHERE ' || quote_ident(cget('PS9_RANGE_DATATYPE_NAME')) || '='
                                        || quote_literal(_tup.row[3])
            );

            -- Fetch the ID of this quantity
            BEGIN
                IF _tup.row[4] IS NULL THEN
                   _tup.row[4] := ''; -- avoiding a <NULL> _qry
                END IF;

                -- If it is a user defined ps_uom.uom, fetch its ID in ps9_uom:
                _qry := ' SELECT ' || quote_ident(cget('TABLE_PS9_QUANTITY'))  || '.'
                                   || quote_ident(cget('PS9_QUANTITY_ID'))     ||
                          ' FROM ' || quote_ident(cget('TABLE_PS9_UOM'))       || ','
                                   || quote_ident(cget('TABLE_PS9_QUANTITY'))  ||
                         ' WHERE ' || quote_ident(cget('TABLE_PS9_UOM'))       || '.'
                                   || quote_ident(cget('PS9_UOM_ID'))          || '='
                                   || quote_ident(cget('TABLE_PS9_QUANTITY'))  || '.'
                                   || quote_ident(cget('PS9_QUANTITY_UOM_ID')) ||
                           ' AND ' || quote_ident(cget('TABLE_PS9_UOM'))       || '.'
                                   || quote_ident(cget('PS9_UOM_CODE')) || '=' || quote_literal(_tup.row[4]);
                RAISE DEBUG '%: EXECUTING %', ME, _qry;
                EXECUTE _qry INTO STRICT _quantity_id;
            EXCEPTION
                WHEN no_data_found THEN
                    -- ...otherwise, it is a previously defined quantity: fetch its ID by description/datatype binding
                    _quantity_id := select_field(
                                      cget('TABLE_PS9_QUANTITY'),
                                      cget('PS9_QUANTITY_ID'), 0,
                                      ' WHERE ' || quote_ident(cget('PS9_QUANTITY_LABEL')) || '='
                                                || quote_literal(_tup.row[3])
                    );
            END;
            RAISE DEBUG '%: quantity ID for ''%'' UoM is %.', ME, _tup.row[4], _quantity_id;

            -- Finally, set this range type component for the coverage
            _qry := ' INSERT INTO ' || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT'))           ||
                               ' (' || quote_ident(cget('PS9_RANGETYPE_COMPONENT_COVERAGE_ID'))     || ','
                                    || quote_ident(cget('PS9_RANGETYPE_COMPONENT_ORDER'))           || ','
                                    || quote_ident(cget('PS9_RANGETYPE_COMPONENT_NAME'))            || ','
                                    || quote_ident(cget('PS9_RANGETYPE_COMPONENT_TYPE_ID'))         || ','
                                    || quote_ident(cget('PS9_RANGETYPE_COMPONENT_FIELD_ID'))        || ') '         ||
                        ' VALUES (' || _coverage9_id || ',' || _tup.row[1]  || ','
                                    || quote_literal(_tup.row[2])    || ',' || _datatype_id || ','  || _quantity_id || ')';
            RAISE DEBUG '%: EXECUTING %', ME, _qry;
            EXECUTE _qry;
            RAISE LOG '%: range-type component ''%'' has been set for coverage ''%''.',
                         ME, _tup.row[2], coverage_name;
        END LOOP;

        --
        -- NM_META migration?    See ~/update9.sql
        --
        -- ...


        RAISE LOG '%: end of ''%'' migration.', ME, coverage_name;
        -- to temporary table for final report
        PERFORM cset(coverage_name, '---');
        RETURN true;
    END;
$$ LANGUAGE plpgsql;


-- ######################################################################### --
-- ######################################################################### --
--
-- /Main/ procedure: fetches shared required info and loops through
--                   each coverage for migration.
--
CREATE OR REPLACE FUNCTION db_migration ()
RETURNS void AS
$$
    DECLARE
        -- Log
        ME constant text := 'db_migration()';
        -- Local variables
        _mime_id        integer;
        _gmlcov_type_id integer;
        _successful_migrations integer := 0;
        _failed_migrations     integer := 0;
        _qry           text;
        _tup           record;
        _report        text;
    BEGIN
        -- Migrate units of measures and CRSs
        PERFORM migrate_uoms();
        PERFORM migrate_crss();

        -- Fetch the native format id from PS9_MIME_TYPE
        BEGIN
            _mime_id := select_field(
                          cget('TABLE_PS9_MIME_TYPE'),
                          cget('PS9_MIME_TYPE_ID'), 0,
                          ' WHERE ' || quote_ident(cget('PS9_MIME_TYPE_MIME')) ||
                                '=' || quote_literal(cget('OCTET_STREAM_MIME'))
            );
            RAISE DEBUG '%: MIME type id for % is %.', ME, cget('OCTET_STREAM_MIME'), _mime_id;
        EXCEPTION
            WHEN no_data_found THEN
                RAISE WARNING '%: ''%'' MIME type has been removed from ''%'': please insert it then retry.',
                              ME, cget('OCTET_STREAM_MIME'), cget('TABLE_PS9_MIME_TYPE');
                RETURN;
        END;

        -- Fetch the ID for 'gmlcov' metadata type
        BEGIN
            _gmlcov_type_id := select_field (
                                 cget('TABLE_PS9_EXTRA_METADATA_TYPE'),
                                 cget('PS9_EXTRA_METADATA_TYPE_ID'), 0,
                                 ' WHERE ' || quote_ident(cget('PS9_EXTRA_METADATA_TYPE_TYPE')) ||
                                       '=' || quote_literal(cget('GMLCOV_METADATA_TYPE'))
            );
        EXCEPTION
            WHEN no_data_found THEN
                RAISE WARNING '%: ''%'' extra metadata type has been removed from ''%'': please insert it then retry.',
                              ME, cget('GMLCOV_METADATA_TYPE'), cget('TABLE_PS9_EXTRA_METADATA_TYPE');
                RETURN;
        END;

        -- Migrate coverage by coverage
        _qry := ' SELECT ARRAY[' || quote_ident(cget('PS_COVERAGE_ID'))   || '::text,'
                                 || quote_ident(cget('PS_COVERAGE_NAME')) || '] AS row ' ||
                        ' FROM ' || quote_ident(cget('TABLE_PS_COVERAGE'));
        BEGIN
            FOR _tup IN EXECUTE _qry LOOP
                RAISE LOG '%: migrating coverage % (ID %)...', ME, _tup.row[2], _tup.row[1];
                IF NOT migrate_coverage(_tup.row[2], _mime_id, _gmlcov_type_id) THEN
                    _failed_migrations := _failed_migrations+1;
                    RAISE WARNING '%: migration of coverage ''%'' was unsuccessful.', ME, _tup.row[2];
                ELSE
                    _successful_migrations := _successful_migrations+1;
                    RAISE LOG '%: coverage ''%'' successfully migrated.', ME, _tup.row[2];
                END IF;
            END LOOP;
        EXCEPTION
            WHEN undefined_function THEN
                RAISE EXCEPTION '%: Problem encountered while migrating coverage ''%''', ME, _tup.row[2]
                      USING HINT    = 'Please enable DEBUG logging level and contact community developers.',
                            ERRCODE = 'undefined_function';
        END;

        -- Add final report of migration
        PERFORM cset('TABLE_REPORT', 'tmp_report');
        PERFORM cset('REPORT_LOG',   'log');
        _qry := ' CREATE TEMPORARY TABLE ' || quote_ident(cget('TABLE_REPORT'))    || ' AS ' ||
                        ' SELECT ' || quote_ident(cget('TABLE_PS_COVERAGE'))       || '.'
                                   || quote_ident(cget('PS_COVERAGE_NAME'))        || ', '
                                   || quote_ident(cget('TABLE_PS9_GML_SUBTYPE'))   || '.'
                                   || quote_ident(cget('PS9_GML_SUBTYPE_SUBTYPE')) || ', '
                ' COUNT(DISTINCT ' || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT')) || '.'
                                   || quote_ident(cget('PS9_RANGETYPE_COMPONENT_ID'))    || ') AS bands, '
                ' COUNT(DISTINCT ' || quote_ident(cget('TABLE_PS9_GRID_AXIS'))           || '.'
                                   || quote_ident(cget('PS9_GRID_AXIS_ID'))              || ') AS dims, '
                   ' (SELECT ''''::text) AS ' || quote_ident(cget('REPORT_LOG'))               || ' FROM '
                                              || quote_ident(cget('TABLE_PS_COVERAGE'))        ||
                          ' LEFT OUTER JOIN ' || quote_ident(cget('TABLE_PS9_COVERAGE'))       ||
                                       ' ON ' || quote_ident(cget('TABLE_PS9_COVERAGE'))       || '.'
                                              || quote_ident(cget('PS9_COVERAGE_NAME'))        || '='
                                              || quote_ident(cget('TABLE_PS_COVERAGE'))        || '.'
                                              || quote_ident(cget('PS_COVERAGE_NAME'))         ||
                          ' LEFT OUTER JOIN ' || quote_ident(cget('TABLE_PS9_GML_SUBTYPE'))    ||
                                       ' ON ' || quote_ident(cget('TABLE_PS9_GML_SUBTYPE'))    || '.'
                                              || quote_ident(cget('PS9_GML_SUBTYPE_ID'))       || '='
                                              || quote_ident(cget('TABLE_PS9_COVERAGE'))       || '.'
                                              || quote_ident(cget('PS9_COVERAGE_GML_TYPE_ID')) ||
                          ' LEFT OUTER JOIN ' || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT'))       ||
                                       ' ON ' || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT'))       || '.'
                                              || quote_ident(cget('PS9_RANGETYPE_COMPONENT_COVERAGE_ID')) || '='
                                              || quote_ident(cget('TABLE_PS9_COVERAGE'   ))               || '.'
                                              || quote_ident(cget('PS9_COVERAGE_ID'))                     ||
                          ' LEFT OUTER JOIN ' || quote_ident(cget('TABLE_PS9_GRID_AXIS'))       ||
                                       ' ON ' || quote_ident(cget('TABLE_PS9_GRID_AXIS'))       || '.'
                                              || quote_ident(cget('PS9_GRID_AXIS_COVERAGE_ID')) || '='
                                              || quote_ident(cget('TABLE_PS9_COVERAGE'))        || '.'
                                              || quote_ident(cget('PS9_COVERAGE_ID'))           ||
                          ' GROUP BY ' || quote_ident(cget('TABLE_PS_COVERAGE'))       || '.'
                                       || quote_ident(cget('PS_COVERAGE_NAME'))        || ','
                                       || quote_ident(cget('TABLE_PS9_GML_SUBTYPE'))   || '.'
                                       || quote_ident(cget('PS9_GML_SUBTYPE_SUBTYPE'));
        RAISE DEBUG '%: EXECUTING : %;', ME, _qry;
        EXECUTE _qry;

        -- Update with coverage migration log
        _qry := ' SELECT ARRAY[' || cget('PS_COVERAGE_NAME') || '] AS row FROM ' || cget('TABLE_PS_COVERAGE');
        FOR _tup in EXECUTE _qry LOOP
            -- Update the report table with the exit log from coverage migration procedure
            _qry := ' UPDATE ' || quote_ident(cget('TABLE_REPORT')) || ' SET '
                               || quote_ident(cget('REPORT_LOG'))   || '='
                               || quote_literal(cget(_tup.row[1]))  ||
                       ' WHERE name=' || quote_literal(_tup.row[1]);
            RAISE DEBUG '%: EXECUTING : %;', ME, _qry;
            EXECUTE _qry;
        END LOOP;

        RAISE NOTICE '%:

        |===========================================|
          :: % coverage(s) successfully migrated
          :: % coverage(s) failed
        |===========================================|

        ', ME, _successful_migrations, _failed_migrations;

        RETURN;
    END;
$$ LANGUAGE plpgsql;

--
-- Run the migration and print the report:
--
SELECT db_migration();
SELECT * FROM query_result('SELECT * FROM ' || quote_ident(cget('TABLE_REPORT')))
         AS foo  (name text, "GML type" text, "#bands" bigint, "#dimensions" bigint, log text)
         ORDER BY name;
