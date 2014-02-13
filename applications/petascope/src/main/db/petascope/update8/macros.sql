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
-- |                     petascopedb 9.0 macros                       |
-- ####################################################################

-----------------------------------------------------------------------
-- Set of PL/pgSQL procedures for reading/writing the metadata of a
-- coverage at a higher level than database tables and fields.
--
-- HINT
-- Use `SELECT * FROM <macro(args)>` for optimal view of the output table,
-- while `SELECT <macro(args)>` aggregates all the columns.
--
-- PREREQUISITES
--   - PL/pgSQL is installed.
--   - `utilities.sql' and `global_const.sql' have been imported.
-----------------------------------------------------------------------


-- FUNCTION: ** getCrs() *******************************************************
-- Prints the native (compound) Coordinate Reference System (CRS) of a coverage as list
-- of single (spatial or temporal) CRSs.
-- NOTE: currently working for gridded rectilinear coverages.
--
-- Plain sample queries on `eobstest' + output:
--
-- petascopedb=# SELECT ps9_crs.id, ps9_crs.uri
-- petascopedb-# FROM   ps9_crs, ps9_domain_set
-- petascopedb-# WHERE    ARRAY[ps9_crs.id] <@ ps9_domain_set.native_crs_id
-- petascopedb-#   AND    ps9_domain_set.coverage_id = <_coverage_id>
-- petascopedb-# ORDER BY index_of(ps9_crs.id, ps9_domain_set.native_crs_id);
--
--   id |                                                        uri
--  ----+-------------------------------------------------------------------------------------------------------------------
--   13 | http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0.1/Temporal?epoch="1950-01-01T00:00:00"&uom="d"
--   12 | http://www.opengis.net/def/crs/EPSG/0/4326
--  (2 rows)
--
-- (Create ad-hoc type so that it need not be specified by the caller:)
DROP TYPE IF EXISTS getCrs_out_type CASCADE;
CREATE TYPE getCrs_out_type AS ("id" integer, "uri" text);
--
CREATE OR REPLACE FUNCTION getCrs (
    coverage_name text
) RETURNS SETOF getCrs_out_type AS
$$
    DECLARE
        -- Log
        ME constant text := 'getCrs()';
        -- Local variables
        _qry         text;
        _coverage_id integer;
        _griddeD_coverage_id integer;
    BEGIN
        -- Get the ID of the requested coverage
	_coverage_id := select_field(
                          cget('TABLE_PS9_COVERAGE'),
                          cget('PS9_COVERAGE_ID'), 0,
                          ' WHERE ' || quote_ident(cget('PS9_COVERAGE_NAME')) || '='
                                    || quote_literal(coverage_name)
        );
        RAISE DEBUG '%: % of coverage is ''%''', ME, cget('PS9_COVERAGE_ID'), _coverage_id;

        -- Check if TABLE_PS9_GRIDDED_DOMAINSET refers to his coverage
        RAISE DEBUG '%: verifying that this coverage has a gridded rectilinear domain-set...', ME;
        IF (NOT table_has_id(
               cget('TABLE_PS9_GRIDDED_DOMAINSET'),
               cget('PS9_GRIDDED_DOMAINSET_COVERAGE_ID'),
              _coverage_id)
        ) THEN
            RAISE EXCEPTION '%: ''%'' is not gridded.', ME, coverage_name
                  USING HINT    = 'Please request a gridded coverage.',
                        ERRCODE = 'feature_not_supported';
        ELSE
            RAISE DEBUG '%: % seems a gridded coverage, proceed.', ME, coverage_name;

            -- Get the list of CRSs associated to this coverage:
            _qry := ' SELECT ' || quote_ident(cget('TABLE_PS9_CRS')) || '.' || quote_ident(cget('PS9_CRS_ID'))  || ', '
                               || quote_ident(cget('TABLE_PS9_CRS')) || '.' || quote_ident(cget('PS9_CRS_URI')) ||
                    ' FROM '   || quote_ident(cget('TABLE_PS9_CRS')) || ',' ||
                                  quote_ident(cget('TABLE_PS9_DOMAINSET'))  ||
                    ' WHERE ARRAY['  || quote_ident(cget('TABLE_PS9_CRS'))  || '.' || quote_ident(cget('PS9_CRS_ID')) || '] <@ '
                               || quote_ident(cget('TABLE_PS9_DOMAINSET'))  || '.' || quote_ident(cget('PS9_DOMAINSET_CRS_IDS')) ||
                    ' AND '    || quote_ident(cget('TABLE_PS9_DOMAINSET'))         || '.'
                               || quote_ident(cget('PS9_DOMAINSET_COVERAGE_ID'))   || '=' || _coverage_id ||
                    ' ORDER BY idx(' || quote_ident(cget('TABLE_PS9_DOMAINSET'))  || '.' || quote_ident(cget('PS9_DOMAINSET_CRS_IDS')) || ','
                                     || quote_ident(cget('TABLE_PS9_CRS'))        || '.' || quote_ident(cget('PS9_CRS_ID'))            ||')';
            RAISE DEBUG '%: EXECUTING : %;', ME, _qry;

            -- Output table
            RETURN QUERY EXECUTE _qry;

            END IF;
        RETURN;
    END;
$$ LANGUAGE plpgsql;

-- FUNCTION: ** getDomainSet() ****************************************************
-- Prints a summary of the domain-set of a coverage.
-- NOTE: currently working for gridded rectilinear coverages.
--
-- Plain sample queries on `eobstest' + output:
--
-- petascopedb=# SELECT ps9_grid_axis.rasdaman_order,
-- petascopedb-#        ps9_gridded_domain_set.grid_origin,
-- petascopedb-#        ps9_rectilinear_axis.offset_vector
-- petascopedb-# FROM   ps9_gridded_domain_set,
-- petascopedb-#        ps9_grid_axis,
-- petascopedb-#        ps9_rectilinear_axis
-- petascopedb-# WHERE    ps9_gridded_domain_set.coverage_id = ps9_grid_axis.gridded_coverage_id
-- petascopedb-#   AND    ps9_grid_axis.id = ps9_rectilinear_axis.grid_axis_id
-- petascopedb-#   AND    ps9_gridded_domain_set.coverage_id = <_coverage_id>
-- petascopedb-# ORDER BY ps9_grid_axis.rasdaman_order;
--
--  rasdaman_order |  grid_origin  |  offset_vector
-- ----------------+---------------+-----------------
--               0 | {0,25,-40.5}  | {1,0,0}
--               1 | {0,25,-40.5}  | {0.0,0.5,0.0}
--               2 | {0,25,-40.5}  | {0.0,0.0,0.5}
-- (3 rows)
--
DROP TYPE IF EXISTS getDomainSet_out_type CASCADE;
CREATE TYPE getDomainSet_out_type AS ("rasdaman_order" integer, "grid_origin" numeric[], "offset_vector" numeric[]);
--
CREATE OR REPLACE FUNCTION getDomainSet(
    coverage_name text
) RETURNS SETOF getDomainSet_out_type AS
$$
    DECLARE
        -- Log
        ME constant text := 'getDomainSet()';
        -- Local variables
        _qry         text;
        _coverage_id integer;
        _griddeD_coverage_id integer;
    BEGIN
        -- Get the ID of the requested coverage
	_coverage_id := select_field(
                          cget('TABLE_PS9_COVERAGE'),
                          cget('PS9_COVERAGE_ID'), 0,
                          ' WHERE ' || quote_ident(cget('PS9_COVERAGE_NAME')) || '='
                                    || quote_literal(coverage_name)
        );
        RAISE DEBUG '%: % of coverage is ''%''', ME, cget('PS9_COVERAGE_ID'), _coverage_id;

        -- Check if TABLE_PS9_GRIDDED_DOMAINSET refers to his coverage
        RAISE DEBUG '%: verifying that this coverage has a gridded rectilinear domain-set...', ME;
        IF (NOT table_has_id(
               cget('TABLE_PS9_GRIDDED_DOMAINSET'),
               cget('PS9_GRIDDED_DOMAINSET_COVERAGE_ID'),
              _coverage_id)
        ) THEN
            RAISE EXCEPTION '%: ''%'' is not gridded.', ME, coverage_name
                  USING HINT    = 'Please request a gridded coverage.',
                        ERRCODE = 'feature_not_supported';
        ELSE
            RAISE DEBUG '%: % seems a gridded coverage, proceed.', ME, coverage_name;

            -- Geometric details per axis:
            _qry := ' SELECT ' || quote_ident(cget('TABLE_PS9_GRID_AXIS'))          || '.'
                               || quote_ident(cget('PS9_GRID_AXIS_RASDAMAN_ORDER')) || ', '
                               || quote_ident(cget('TABLE_PS9_GRIDDED_DOMAINSET'))  || '.'
                               || quote_ident(cget('PS9_GRIDDED_DOMAINSET_ORIGIN')) || ', '
                               || quote_ident(cget('TABLE_PS9_RECTILINEAR_AXIS'))   || '.'
                               || quote_ident(cget('PS9_RECTILINEAR_AXIS_OFFSET_VECTOR'))  ||
                    ' FROM  '  || quote_ident(cget('TABLE_PS9_GRID_AXIS'))          || ', '
                               || quote_ident(cget('TABLE_PS9_GRIDDED_DOMAINSET'))  || ', '
                               || quote_ident(cget('TABLE_PS9_RECTILINEAR_AXIS'))   ||
                    ' WHERE '  || quote_ident(cget('TABLE_PS9_GRIDDED_DOMAINSET'))  || '.'
                               || quote_ident(cget('PS9_GRIDDED_DOMAINSET_COVERAGE_ID'))   || '='
                               || quote_ident(cget('TABLE_PS9_GRID_AXIS')) ||  '.'  || quote_ident(cget('PS9_GRID_AXIS_COVERAGE_ID')) ||
                    ' AND '    || quote_ident(cget('TABLE_PS9_GRID_AXIS')) ||  '.'  || quote_ident(cget('PS9_GRID_AXIS_ID')) || '='
                               || quote_ident(cget('TABLE_PS9_RECTILINEAR_AXIS'))   || '.' || quote_ident(cget('PS9_RECTILINEAR_AXIS_ID')) ||
                    ' AND '    || quote_ident(cget('TABLE_PS9_GRIDDED_DOMAINSET'))  || '.'
                               || quote_ident(cget('PS9_GRIDDED_DOMAINSET_COVERAGE_ID')) || '=' || _coverage_id ||
                  ' ORDER BY ' || quote_ident(cget('TABLE_PS9_GRID_AXIS')) ||  '.'  || quote_ident(cget('PS9_GRID_AXIS_RASDAMAN_ORDER'));
            RAISE DEBUG '%: %', ME, _qry;

            -- Output table
            RETURN QUERY EXECUTE _qry;

            END IF;
        RETURN;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** getRangeSet() ****************************************************
-- Prints a summary of the range-set of a coverage.
--
-- Plain sample queries on `eobstest' + output:
--
-- petascopedb=# SELECT SELECT 'eobstest'::text, name::text, oid, base_type
-- petascopedb-# FROM ps9_rasdaman_collection
-- petascopedb-# WHERE id=11
--
--   coverage name | collection name | collection OID | base_type
--  ---------------+-----------------+----------------+-----------
--   eobstest      | eobstest        |          10753 |
--  (1 row)
--
-- NOTE: currently working only for (gridded) coverages stored as rasdaman collections.
DROP TYPE IF EXISTS getRangeSet_out_type CASCADE;
CREATE TYPE getRangeSet_out_type AS ("coverage name" text, "collection name" text, "collection OID" numeric, "base_type" text);
--
CREATE OR REPLACE FUNCTION getRangeSet(
    coverage_name text
) RETURNS SETOF getRangeSet_out_type AS
$$
    DECLARE
        -- Log
        ME constant text := 'getRangeSet()';
        -- Local variables
        _qry           text;
        _coverage_id   integer;
        _storage_table text;
        _storage_id    integer;
    BEGIN
        -- Get the ID of the requested coverage
        _coverage_id := select_field(
                          cget('TABLE_PS9_COVERAGE'),
                          cget('PS9_COVERAGE_ID'), 0,
                          ' WHERE ' || quote_ident(cget('PS9_COVERAGE_NAME')) || '='
                                    || quote_literal(coverage_name)
        );
        RAISE DEBUG '%: % of coverage is ''%''', ME, cget('PS9_COVERAGE_ID'), _coverage_id;

        -- Check if the storage table is TABLE_PS9_RASDAMAN_COLLECTION
        RAISE DEBUG '%: verifying that this coverage is stored as a rasdaman MDD...', ME;
        _storage_table := select_field(
                          cget('TABLE_PS9_RANGESET'),
                          cget('PS9_RANGESET_STORAGE_TABLE'), ''::text,
                          ' WHERE ' || quote_ident(cget('PS9_RANGESET_COVERAGE_ID')) || '='
                                    || quote_literal(_coverage_id)
        );
        RAISE DEBUG '%: storage table of the coverage is ''%''', ME, _storage_table;

        -- Proceed only if this coverage has been stored as rasdaman collection
	IF (_storage_table <> cget('TABLE_PS9_RASDAMAN_COLLECTION')) THEN
            RAISE EXCEPTION '%: ''%'' is not stored as a rasdaman MDD ', ME, coverage_name
                  USING HINT    = 'Please request an other coverage.',
                        ERRCODE = 'feature_not_supported';
        ELSE
            -- Get the correspondent ID in TABLE_PS9_RASDAMAN_COLLECTION
            -- (`storage_ref_integrity` trigger has made sure it is there)
            RAISE DEBUG '%: fetching the ID of the correspondent row in %...', ME, cget('TABLE_PS9_RASDAMAN_COLLECTION');
            _storage_id := select_field(
                          cget('TABLE_PS9_RANGESET'),
                          cget('PS9_RANGESET_STORAGE_ID'), 0,
                          ' WHERE ' || quote_ident(cget('PS9_RANGESET_COVERAGE_ID')) || '='
                                    || quote_literal(_coverage_id)
            );
            RAISE DEBUG '%: storage id of the coverage is ''%''', ME, _storage_id;

            -- Build the final query string
            _qry = ' SELECT ' || quote_literal(coverage_name) || '::text, '
                              || quote_ident(cget('PS9_RASDAMAN_COLLECTION_NAME'))      || '::text, '
                              || quote_ident(cget('PS9_RASDAMAN_COLLECTION_OID'))       || ', '
                              || quote_ident(cget('PS9_RASDAMAN_COLLECTION_BASETYPE'))  ||
                   ' FROM '   || quote_ident(cget('TABLE_PS9_RASDAMAN_COLLECTION'))     ||
                   ' WHERE '  || quote_ident(cget('PS9_RASDAMAN_COLLECTION_ID')) || '=' || _storage_id;
            RAISE DEBUG '%: %', ME, _qry;

            -- print the result
            RETURN QUERY EXECUTE _qry;

        END IF;
        RETURN;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** getRangeType() ****************************************************
-- Prints a list of the range-type components of a coverage.
--
-- Plain sample queries on `eobstest' + output:
--
-- petascopedb=# SELECT ps9_range_type_component.component_order,
-- petascopedb-#        ps9_range_type_component.name, 'Quantity'::text,
-- petascopedb-#        ps9_range_data_type.name,
-- petascopedb-#        ps9_uom.code,
-- petascopedb-#        '(' || ps9_interval.min || ',' || ps9_interval.max || ')' AS allowed_intervals
-- petascopedb-# FROM   ps9_range_type_component
-- petascopedb-# INNER JOIN ps9_range_data_type
-- petascopedb-#         ON ps9_range_data_type.id = ps9_range_type_component.data_type_id
-- petascopedb-# INNER JOIN ps9_quantity
-- petascopedb-#         ON ps9_quantity.id = ps9_range_type_component.field_id
-- petascopedb-# INNER JOIN ps9_uom
-- petascopedb-#         ON ps9_uom.id = ps9_quantity.uom_id
-- petascopedb-# LEFT OUTER JOIN ps9_quantity_interval
-- petascopedb-#              ON ps9_quantity_interval.quantity_id = ps9_quantity.id
-- petascopedb-# LEFT OUTER JOIN ps9_interval
-- petascopedb-#              ON ps9_interval.id = ps9_quantity_interval.interval_id
-- petascopedb-# WHERE ps9_range_type_component.coverage_id = <coverage_id>;
--
--   component_order | name  | ?column? | name  | code |    ?column?
--  -----------------+-------+----------+-------+------+----------------
--                 0 | value | Quantity | short | 10⁰  | (-32768,32767)
--  (1 row)
--
DROP TYPE IF EXISTS getRangeType_out_type CASCADE;
CREATE TYPE getRangeType_out_type AS
       ("component order" integer, "name" text, "SWE type" text, "data type" text, "UoM" text, "allowed interval(s)" text);
--
CREATE OR REPLACE FUNCTION getRangeType(
    coverage_name text
) RETURNS SETOF getRangeType_out_type AS
$$
    DECLARE
        -- Log
        ME constant text := 'getRangeType()';
        -- Local variables
        _qry         text;
        _coverage_id integer;
        _field_table text;
    BEGIN
        -- Get the ID of the requested coverage
        _coverage_id := select_field(
                          cget('TABLE_PS9_COVERAGE'),
                          cget('PS9_COVERAGE_ID'), 0,
                          ' WHERE ' || quote_ident(cget('PS9_COVERAGE_NAME')) || '='
                                    || quote_literal(coverage_name)
        );
        RAISE DEBUG '%: % of coverage is ''%''', ME, cget('PS9_COVERAGE_ID'), _coverage_id;

        BEGIN
            -- Check that range type is made of SWE `quantity` field(s):
            RAISE DEBUG '%: verifying that each component of the range type is a (SWE) `quantity` field...', ME;
            _field_table := select_field(
                              cget('TABLE_PS9_RANGETYPE_COMPONENT'),
                              cget('PS9_RANGETYPE_COMPONENT_FIELD_TABLE'), ''::text,
                              ' WHERE ' || quote_ident(cget('PS9_RANGETYPE_COMPONENT_COVERAGE_ID'))
                                        || '=' || _coverage_id ||
                           ' GROUP BY ' || quote_ident(cget('PS9_RANGETYPE_COMPONENT_FIELD_TABLE'))
             );
            RAISE DEBUG '%: field table of the coverage is ''%''', ME, _field_table;
        EXCEPTION
            WHEN no_data_found OR unique_violation THEN
                RAISE EXCEPTION '%: Some or all components of ''%'' are not of type `quantity`.', ME, coverage_name
                      USING HINT    = 'Please request an other coverage.',
                            ERRCODE = 'feature_not_supported';
        END;

        -- Build the final query string
        _qry := ' SELECT ' || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT')) || '.'
                           || quote_ident(cget('PS9_RANGETYPE_COMPONENT_ORDER')) || ', '
                           || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT')) || '.'
                           || quote_ident(cget('PS9_RANGETYPE_COMPONENT_NAME'))  || ', '
                           || quote_literal(cget('SWE_QUANTITY_FIELD'))          || '::text, '
                           || quote_ident(cget('TABLE_PS9_RANGE_DATATYPE'))      || '.'
                           || quote_ident(cget('PS9_RANGE_DATATYPE_NAME'))       || ', '
                           || quote_ident(cget('TABLE_PS9_UOM'))                 || '.'
                           || quote_ident(cget('PS9_UOM_CODE'))                  || ', '
               ' ''('' ||' || quote_ident(cget('TABLE_PS9_INTERVAL')) ||   '.'   || quote_ident(cget('PS9_INTERVAL_MIN')) || ' || '','' || '
                           || quote_ident(cget('TABLE_PS9_INTERVAL')) ||   '.'   || quote_ident(cget('PS9_INTERVAL_MAX')) || ' || '')''::text ' ||
                ' FROM '   || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT')) ||
               ' INNER JOIN ' || quote_ident(cget('TABLE_PS9_RANGE_DATATYPE'))   ||
                       ' ON ' || quote_ident(cget('TABLE_PS9_RANGE_DATATYPE'))         || '.'
                              || quote_ident(cget('PS9_RANGE_DATATYPE_ID'))            || '='
                              || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT'))    || '.'
                              || quote_ident(cget('PS9_RANGETYPE_COMPONENT_TYPE_ID'))  ||
               ' INNER JOIN ' || quote_ident(cget('TABLE_PS9_QUANTITY')) ||
                       ' ON ' || quote_ident(cget('TABLE_PS9_QUANTITY'))               || '.'
                              || quote_ident(cget('PS9_QUANTITY_ID'))                  || '='
                              || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT'))    || '.'
                              || quote_ident(cget('PS9_RANGETYPE_COMPONENT_FIELD_ID')) ||
               ' INNER JOIN ' || quote_ident(cget('TABLE_PS9_UOM'))      ||
                       ' ON ' || quote_ident(cget('TABLE_PS9_UOM'))      || '.' || quote_ident(cget('PS9_UOM_ID'))          || '='
                              || quote_ident(cget('TABLE_PS9_QUANTITY')) || '.' || quote_ident(cget('PS9_QUANTITY_UOM_ID')) ||
          ' LEFT OUTER JOIN ' || quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL')) ||
                       ' ON ' || quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL')) || '.'
                              || quote_ident(cget('PS9_QUANTITY_INTERVAL_QID'))   || '='
                              || quote_ident(cget('TABLE_PS9_QUANTITY'))          || '.'
                              || quote_ident(cget('PS9_QUANTITY_ID'))             ||
          ' LEFT OUTER JOIN ' || quote_ident(cget('TABLE_PS9_INTERVAL'))   ||
                       ' ON ' || quote_ident(cget('TABLE_PS9_INTERVAL'))          || '.'
                              || quote_ident(cget('PS9_INTERVAL_ID'))             || '='
                              || quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL')) || '.'
                              || quote_ident(cget('PS9_QUANTITY_INTERVAL_IID'))   ||
                    ' WHERE ' || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT'))        || '.'
                              || quote_ident(cget('PS9_RANGETYPE_COMPONENT_COVERAGE_ID'))  || '=' || _coverage_id ||
                 ' ORDER BY ' || quote_ident(cget('TABLE_PS9_RANGETYPE_COMPONENT')) || '.' || quote_ident(cget('PS9_RANGETYPE_COMPONENT_ORDER'));
        RAISE DEBUG '%: %', ME, _qry;

        -- print the result
        RETURN QUERY EXECUTE _qry;
        RETURN;
    END;
$$ LANGUAGE plpgsql;
