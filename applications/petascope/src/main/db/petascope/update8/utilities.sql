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
-- |         SQL & PL/pgSQL utilities for database scripting          |
-- ####################################################################
--
-- PURPOSE
-- Miscellanea of functions to be imported and used by SQL scripts.
--
-- PREREQUISITES
--   - PL/pgSQL is installed.
--
-- VARIABLES NAMING STYLE
-- local variables : _<lowercase_name>
-- constants       : <UPPERCASE_NAME>
-- arguments       : <lowercase_name>
-----------------------------------------------------------------------


-- FUNCTION: ** array index ****************************************************
-- Returns the index of an element in the array.
-- REF: https://wiki.postgresql.org/wiki/Array_Index
CREATE OR REPLACE FUNCTION idx(anyarray, anyelement)
  RETURNS int AS
$$
  SELECT i FROM (
     SELECT generate_series(array_lower($1,1),array_upper($1,1))
  ) g(i)
  WHERE $1[i] = $2
  LIMIT 1;
$$ LANGUAGE sql IMMUTABLE;


-- FUNCTION: ** query_result ****************************************************
-- Returns the output of a generic query.
CREATE OR REPLACE FUNCTION query_result(
    this_query text
) RETURNS SETOF record AS
$$
    BEGIN
      RETURN QUERY EXECUTE this_query;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** selected_field **************************************************
-- Aux function to get the id of a coverage from its name
-- usage: SELECT select_field('ps_uom', 'uom', ''::text, 'WHERE);
CREATE OR REPLACE FUNCTION select_field (
    selected_table    text,
    selected_field    text,
    field_type_sample anyelement,
    where_clause      text DEFAULT ''  -- including "WHERE"
) RETURNS anyelement AS $$
    DECLARE
	-- Log
	ME  constant text := 'selected_field()';
	-- Local variables
        _qry          text;
        _result_value ALIAS FOR $0;
    BEGIN
        _qry := 'SELECT ' || quote_ident(selected_field) ||
                 ' FROM ' || quote_ident(selected_table) ||
                      ' ' || where_clause;
        RAISE DEBUG '%: %', ME, _qry;

        EXECUTE _qry INTO STRICT _result_value;
        RETURN  _result_value;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** table_exists (table_name) *************************************
-- Checks that a table exists in the current schema.
CREATE OR REPLACE FUNCTION table_exists (
    this_table text
) RETURNS boolean AS
$$
    BEGIN
        -- check if the table exists
        PERFORM *  FROM pg_tables
                  WHERE tablename  = this_table
                    AND schemaname = current_schema();
        RETURN FOUND;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** table_has_id(table_name, id_field, id_value) ******************
-- Checks that a certain id is contained in the specified table.
-- This function is used when a table imports FKs from different tables.
CREATE OR REPLACE FUNCTION table_has_id (
    this_table text,
    id_field   text,
    id_value   integer
) RETURNS boolean AS
$$
    DECLARE
        -- Log
	ME constant text := 'table_has_id()';
        -- Local variables
        _qry text;
        _tup record;
    BEGIN
        -- check if the table exists
        IF NOT table_exists(this_table) THEN
            RAISE EXCEPTION '%: table ''%'' does not exist.', ME, this_table;
        END IF;

        -- check for referential integrity
	_qry := 'SELECT * FROM ' || quote_ident(this_table) ||
                       ' WHERE ' || quote_ident(id_field)   || '=' || quote_literal(id_value);
	RAISE DEBUG 'Executing: %', _qry;
	FOR _tup IN EXECUTE _qry LOOP -- EXECUTE does not affect FOUND
            RETURN true; -- the tuple has been found
        END LOOP;
        RETURN false; -- no tuple with specified id
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** matches_pattern(string, pattern) ******************************
-- Returns true if the pattern matches the specified string
CREATE OR REPLACE FUNCTION matches_pattern (
    this_string  text,
    this_pattern text
) RETURNS boolean AS
$$
    BEGIN
        -- check that the UoM code is like <pattern value="^[^: \n\r\t]+"/>
        PERFORM regexp_matches(this_string, this_pattern);
        RETURN FOUND;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** numeric_column2array (table, numeric_column, where_clause, order_by_column) ****
-- Builds an array from a query response with single numeric values
CREATE OR REPLACE FUNCTION numeric_column2array (
    this_table     text,
    numeric_column text,
    where_clause   text,
    orderby_column text
) RETURNS numeric[] AS
$$
    DECLARE
        -- Log
        ME constant text := 'numeric_column2array()';
        -- Local variables
        _qry       text;
        _tup       record;
        _out_array numeric[];
    BEGIN
        _qry := 'SELECT ARRAY[CAST(' || quote_ident(numeric_column) || ' AS numeric)] AS row FROM '
                || quote_ident(this_table)           || ' '
                || where_clause      || ' ORDER BY ' || quote_ident(orderby_column);
	RAISE DEBUG 'Executing: %', _qry;
	FOR _tup IN EXECUTE _qry LOOP
            -- Extract the numeric column value
            _out_array := array_append(_out_array, _tup.row[1]);
        END LOOP;
        RETURN _out_array;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** array_sort (anyarray) *****************************************
-- Sorts an array (usage: SELECT sort_array('{3,2,1}'::integer[]);)
CREATE OR REPLACE FUNCTION array_sort (anyarray) RETURNS anyarray
LANGUAGE SQL
AS
$$
    SELECT ARRAY(
      SELECT $1[s.i] AS "element"
      FROM generate_series(array_lower($1,1), array_upper($1,1)) AS s(i)
      ORDER BY element
    );
$$;


-- FUNCTION: ** array_sort_distinct (anyarray) ********************************
-- Sorts an array (usage: SELECT sort_array_distinct('{3,2,1,2}'::integer[]);)
CREATE OR REPLACE FUNCTION array_sort_distinct (anyarray)
RETURNS anyarray AS
$$
    SELECT ARRAY(
      SELECT DISTINCT $1[s.i] AS "element"
      FROM
          generate_series(array_lower($1,1), array_upper($1,1)) AS s(i)
      ORDER BY element
    );
$$ LANGUAGE 'sql';


-- FUNCTION: ** is_sequential (array, origin) *********************************
-- Verify that an array is strictly sequential from an origin index.
CREATE OR REPLACE FUNCTION is_sequential (
    indexes_array  integer[],
    indexes_origin integer = NULL
) RETURNS boolean AS
$$
    DECLARE
        -- Local variables
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
$$ LANGUAGE plpgsql;


-- FUNCTION: ** is_increasing (array, is_strict) *******************************
-- Verify that an array is strictly sequential from 0.
CREATE OR REPLACE FUNCTION is_increasing (
    numeric_array numeric[],
    is_strict     boolean
) RETURNS boolean AS
$$
    DECLARE
        -- Local variables
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
$$ LANGUAGE plpgsql;


-- FUNCTION: ** table_is_empty (table) ****************************************
-- Returns true if a table is empty.
CREATE OR REPLACE FUNCTION table_is_empty (
    this_table text
) RETURNS boolean AS
$$
    DECLARE
        -- Log
        ME constant text := 'table_is_empty()';
        -- Local variables
        _qry text;
        _tup record;
    BEGIN
        -- check if the table exists
        IF NOT table_exists(this_table) THEN
            RAISE EXCEPTION '%: table ''%'' does not exist.', ME, this_table;
        END IF;

        -- check that it is empty
        _qry := 'SELECT * FROM ' || quote_ident(this_table);
	FOR _tup IN EXECUTE _qry LOOP -- EXECUTE does not affect FOUND
            RETURN false; -- the tuple has been found
        END LOOP;
        RETURN true; -- table is empty
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** resolution2vector  **********************************************
-- Turns scalar resolution to aligned offset vector, with non-zero component
-- in accordance to the order of the axis within the coverage dimensions.
CREATE OR REPLACE FUNCTION resolution2vector (
    resolution numeric,
    axis_order integer,
    dimensions integer
) RETURNS numeric[] AS
$$
    DECLARE
        -- Log
        ME constant text := 'resolution2vector()';
        -- Local variables
        _out_array numeric[];
    BEGIN
        -- Check input
        IF dimensions <= 0 THEN
            RAISE EXCEPTION '%: illegal dimensionality (%), it must me positive.', ME, dimensions;
        ELSIF axis_order < 0 OR axis_order > (dimensions-1) THEN
            RAISE EXCEPTION '%: illegal axis order (%), it must be between 0 and %.', ME, axis_order, (dimensions-1);
        END IF;

        -- Create vector
        FOR i IN 0..(dimensions-1) LOOP
            IF i <> axis_order THEN
                _out_array = array_append(_out_array, 0.0);
            ELSE
                _out_array = array_append(_out_array, resolution);
            END IF;
        END LOOP;
        RETURN _out_array;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** get_axis_type (int)  **********************************************
-- Returns the type of an axis, given its FK on ps_axistype.
-- @deprecated: this function is used during migration to retrieve information in the old schema (<update8).
CREATE OR REPLACE FUNCTION get_axis_type (
    axis_type_id   integer
) RETURNS text AS
$$
    BEGIN
        RETURN select_field(
            cget('TABLE_PS_AXISTYPE'),
            cget('PS_AXISTYPE_TYPE'), ''::text,
            ' WHERE ' || cget('PS_AXISTYPE_ID') || '=' || axis_type_id
            );
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** gridaxis_crs  **********************************************
-- Retrieves the CRS associated with a grid axis.
-- @deprecated: this function is used during migration to retrieve information in the old schema (<update8).
CREATE OR REPLACE FUNCTION gridaxis_crs (
    coverage_id  integer,
    axis_order   integer
) RETURNS text AS
$$
    DECLARE
        -- Log
        ME constant text := 'gridaxis_crs()';
        -- Local variables
        _qry text;
        _tup record;
    BEGIN
        -- sql query
        _qry := ' SELECT ARRAY[' || quote_ident(cget('TABLE_PS_CRS')) || '.'
                                 || quote_ident(cget('PS_CRS_NAME'))  || '] AS row ' ||
                  ' FROM ' || quote_ident(cget('TABLE_PS_CRS'))    || ', '
                           || quote_ident(cget('TABLE_PS_DOMAIN')) || ', '
                           || quote_ident(cget('TABLE_PS_CRSSET')) ||
                 ' WHERE ' || quote_ident(cget('TABLE_PS_CRS'))       || '.' || quote_ident(cget('PS_CRS_ID'))      || '='
                           || quote_ident(cget('TABLE_PS_CRSSET'))    || '.' || quote_ident(cget('PS_CRSSET_CRS'))  ||
                   ' AND ' || quote_ident(cget('TABLE_PS_CRSSET'))    || '.' || quote_ident(cget('PS_CRSSET_AXIS')) || '='
                           || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.' || quote_ident(cget('PS_DOMAIN_ID'))   ||
                   ' AND ' || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.'
                           || quote_ident(cget('PS_DOMAIN_I'))        || '=' || axis_order ||
                   ' AND ' || quote_ident(cget('TABLE_PS_DOMAIN'))    || '.'
                           || quote_ident(cget('PS_DOMAIN_COVERAGE')) || '=' || coverage_id
                   ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
	FOR _tup IN EXECUTE _qry LOOP
            RETURN _tup.row[1];
        END LOOP;
        RETURN '';
    END;
$$ LANGUAGE plpgsql;



-- FUNCTION: ** change_prefix (text, text) *****************************************
-- Changes prefix (<prefix>_<name>) of a table. If there is no prefix, name is not changed.
CREATE OR REPLACE FUNCTION change_table_prefix (
    table_name text,
    new_prefix text
) RETURNS VOID AS
$$
    DECLARE
        -- Log
        ME constant text := 'change_prefix()';
        -- Local variables
        _qry text;
    BEGIN
        IF char_length(substring(table_name from '_.*')) > 0 THEN
            -- this table /has/ a prefix: replace it (do nothing otherwise)
            _qry := ' ALTER TABLE ' || quote_ident(table_name) || ' RENAME TO ' ||
                         new_prefix || substring(table_name from '_.*')
                         ;
            RAISE DEBUG '%: EXECUTE %', ME, _qry;
            BEGIN
                EXECUTE _qry;
            EXCEPTION
                WHEN duplicate_table THEN
                    RAISE DEBUG 'Table % is already prefixed by %: skip it.', table_name, new_prefix;
            END;
        END IF;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** change_prefixes (text) ********************************************
-- Changes prefix (<prefix>_<name>) of all tables in the selected schema, from `old_prefix' to `new_prefix'.
CREATE OR REPLACE FUNCTION change_prefixes (
    this_schema text,
    old_prefix  text,
    new_prefix  text
) RETURNS VOID AS
$$
    DECLARE
        -- Log
        ME constant text := 'change_prefixes()';
        -- Local variables
        _qry        text;
        _tup        record;
    BEGIN
        -- Loop through all the table in this schema
        _qry := ' SELECT ARRAY[tablename] AS row FROM pg_tables WHERE schemaname = ' || quote_literal(this_schema) ||
                ' AND tablename ILIKE ' || quote_literal(old_prefix || '_%')
                ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            _qry := 'SELECT change_table_prefix(' || quote_literal(_tup.row[1]) || ', ' || quote_literal(new_prefix) || ') ';
            RAISE DEBUG '%: EXECUTE %', ME, _qry;
            EXECUTE _qry;
        END LOOP;

        -- indexes
        _qry := ' SELECT ARRAY[relname] AS row FROM pg_class WHERE relkind = ''i'' ' ||
                ' AND relname ILIKE ' || quote_literal(old_prefix || '_%')
                ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            _qry := ' ALTER INDEX ' || quote_ident(_tup.row[1]) ||
                    ' RENAME TO '   || new_prefix || substring(_tup.row[1] from '_.*')
                    ;
            RAISE DEBUG '%: EXECUTE %', ME, _qry;
            EXECUTE _qry;
        END LOOP;

        -- sequences
        _qry := ' SELECT ARRAY[relname] AS row FROM pg_class WHERE relkind = ''S'' ' ||
                ' AND relname ILIKE ' || quote_literal(old_prefix || '_%')
                ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            _qry := ' ALTER SEQUENCE ' || quote_ident(_tup.row[1]) ||
                    ' RENAME TO '      || new_prefix || substring(_tup.row[1] from '_.*')
                    ;
            RAISE DEBUG '%: EXECUTE %', ME, _qry;
            EXECUTE _qry;
        END LOOP;

        -- primary/unique constraints (foreign keys and checks cannot be renamed, would need DROP/ADD)
        _qry := ' SELECT ARRAY[conname] AS row FROM pg_constraint WHERE contype IN (''u'',''p'') ' ||
                ' AND conname ILIKE ' || quote_literal(old_prefix || '_%')
                ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            _qry := ' ALTER INDEX ' || quote_ident(_tup.row[1]) ||
                    ' RENAME TO '   || new_prefix || substring(_tup.row[1] from '_.*')
                    ;
            RAISE DEBUG '%: EXECUTE %', ME, _qry;
            EXECUTE _qry;
        END LOOP;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** is_primitive_quantity (integer) ********************************************
-- TRUE if the quantity with the specified ID is a primitive.
CREATE OR REPLACE FUNCTION is_primitive_quantity (
    quantity_id  integer
) RETURNS boolean AS
$$
    DECLARE
        -- Log
        ME constant text := 'is_primitive_quantity()';
        -- Local variables
        _qry  text;
        _tup  record;
    BEGIN
        -- Check if the `description' attribute equals to `primitive'
        _qry := ' SELECT ' || quote_ident(cget('PS9_QUANTITY_DESCRIPTION')) || ' = '
                           || quote_literal(cget('PRIMITIVE'))              || ' AS is_primitive ' ||
                  ' FROM ' || quote_ident(cget('TABLE_PS9_QUANTITY'))       ||
                 ' WHERE ' || quote_ident(cget('PS9_QUANTITY_ID'))   || '=' || quantity_id
                ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            RETURN _tup.is_primitive;
        END LOOP;
        RETURN FALSE;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** can_drop_interval (integer) ********************************************
-- TRUE if the interval (allowed values) with the specified ID is not referenced by any quantity.
CREATE OR REPLACE FUNCTION can_drop_interval (
    interval_id  integer
) RETURNS boolean AS
$$
    DECLARE
        -- Log
        ME constant text := 'can_drop_interval()';
        -- Local variables
        _qry  text;
        _tup  record;
    BEGIN
        -- Check if there is any primitive in the referencing quantities
        _qry := ' SELECT COUNT(*) AS referencing_quantities ' ||
                  ' FROM ' || quote_ident(cget('TABLE_PS9_QUANTITY'))       || ','
                           || quote_ident(cget('TABLE_PS9_INTERVAL'))       || ','
                           || quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL'))   ||
                 ' WHERE ' || quote_ident(cget('TABLE_PS9_QUANTITY'))       || '.'
                           || quote_ident(cget('PS9_QUANTITY_ID'))          || '='
                           || quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL'))     || '.'
                           || quote_ident(cget('PS9_QUANTITY_INTERVAL_QID'))       ||
                   ' AND ' || quote_ident(cget('TABLE_PS9_INTERVAL'))       || '.'
                           || quote_ident(cget('PS9_INTERVAL_ID'))          || '='
                           || quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL'))     || '.'
                           || quote_ident(cget('PS9_QUANTITY_INTERVAL_IID'))||
                   ' AND ' || quote_ident(cget('TABLE_PS9_INTERVAL'))       || '.'
                           || quote_ident(cget('PS9_INTERVAL_ID'))   || '=' || interval_id
                           ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            IF _tup.referencing_quantities <> 0 THEN
                -- this interval is bound to 1+ quantity: cannot DROP
                RETURN FALSE;
            END IF;
        END LOOP;
        -- otherwise:
        RETURN TRUE;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** can_drop_uom (integer) ********************************************
-- TRUE if the UoM with the specified ID is not referenced by any quantity.
CREATE OR REPLACE FUNCTION can_drop_uom (
    uom_id  integer
) RETURNS boolean AS
$$
    DECLARE
        -- Log
        ME constant text := 'can_drop_uom()';
        -- Local variables
        _qry  text;
        _tup  record;
    BEGIN
        -- Check if there is any primitive in the referencing quantities
        _qry := ' SELECT COUNT(*) AS referencing_quantities '          ||
                  ' FROM ' || quote_ident(cget('TABLE_PS9_QUANTITY'))  || ','
                           || quote_ident(cget('TABLE_PS9_UOM'))       ||
                 ' WHERE ' || quote_ident(cget('TABLE_PS9_QUANTITY'))  || '.'
                           || quote_ident(cget('PS9_QUANTITY_UOM_ID')) || '='
                           || quote_ident(cget('TABLE_PS9_UOM'))       || '.'
                           || quote_ident(cget('PS9_UOM_ID'))          ||
                   ' AND ' || quote_ident(cget('TABLE_PS9_QUANTITY'))  || '.'
                           || quote_ident(cget('PS9_QUANTITY_UOM_ID')) || '=' || uom_id
                           ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            IF _tup.referencing_quantities <> 0 THEN
                -- this uom is bound to 1+ quantity: cannot DROP
                RETURN FALSE;
            END IF;
        END LOOP;
        -- otherwise:
        RETURN TRUE;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** drop_quantity (integer) ********************************************
-- Drops a quantity if not a primitive, and if not: cascade drop to interval(s) and UoM
-- referenced by primitive quantities
CREATE OR REPLACE FUNCTION drop_quantity (
    quantity_id  integer
) RETURNS VOID AS
$$
    DECLARE
        -- Log
        ME constant text := 'drop_quantity()';
        -- Local variables
        _interval_id integer;
        _uom_id      integer;
        _qry         text;
        _tup         record;
    BEGIN
        IF NOT is_primitive_quantity(quantity_id) THEN

            -- If it defines allowed values, need to drop the association and as well the interval(s)
            IF table_has_id(
              quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL')),
              quote_ident(cget('PS9_QUANTITY_INTERVAL_QID')), quantity_id) THEN

                -- Get interval(s) id
                _qry := ' SELECT ARRAY[' || quote_ident(cget('PS9_QUANTITY_INTERVAL_IID'))   || '] AS row ' ||
                                ' FROM ' || quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL')) ||
                               ' WHERE ' || quote_ident(cget('PS9_QUANTITY_INTERVAL_QID'))   || '=' || quantity_id
                         ;

                RAISE DEBUG '%: EXECUTE %', ME, _qry;
                FOR _tup IN EXECUTE _qry LOOP
                    _interval_id = _tup.row[1];

                    -- Drop association with interval
                    RAISE DEBUG '%: delete {quantity,interval} association {%,%}.', ME, quantity_id, _interval_id;
                    _qry := ' DELETE FROM ' || quote_ident(cget('TABLE_PS9_QUANTITY_INTERVAL')) ||
                                  ' WHERE ' || quote_ident(cget('PS9_QUANTITY_INTERVAL_QID'))   || '=' ||  quantity_id ||
                                    ' AND ' || quote_ident(cget('PS9_QUANTITY_INTERVAL_IID'))   || '=' || _interval_id
                        ;
                    RAISE DEBUG '%: EXECUTE %', ME, _qry;
                    EXECUTE _qry;

                    -- Drop interval, if it is not tied with a primitive quantity (primitive interval)
                    IF can_drop_interval(_interval_id) THEN
                        -- this interval is not bound to a quantity: can CASCADE
                        RAISE DEBUG '%: delete interval with ID #%', ME, _interval_id;
                        _qry := ' DELETE FROM ' || quote_ident(cget('TABLE_PS9_INTERVAL')) ||
                                      ' WHERE ' || quote_ident(cget('PS9_INTERVAL_ID'))    || '='
                                                || _interval_id
                                                ;
                       RAISE DEBUG '%: EXECUTE %', ME, _qry;
                       EXECUTE _qry;
                   END IF;
                END LOOP;

            END IF; -- ~#table_has_id

            -- Possible allowed values have been dropped: now can safely drop the quantity.
            -- Get UoM id first:
            _uom_id = select_field(
                          quote_ident(cget('TABLE_PS9_QUANTITY')),
                          quote_ident(cget('PS9_QUANTITY_UOM_ID')), 0,
                          ' WHERE ' || quote_ident(cget('PS9_QUANTITY_ID')) || '=' || quantity_id)
                          ;
            -- now drop:
            _qry := ' DELETE FROM ' || quote_ident(cget('TABLE_PS9_QUANTITY')) ||
                          ' WHERE ' || quote_ident(cget('PS9_QUANTITY_ID'))    || '='
                                    || quantity_id
                                    ;
            RAISE DEBUG '%: EXECUTE %', ME, _qry;
            EXECUTE _qry;

            -- Finally, drop the UoM is not primitive and if not referenced by other quantities.
            IF can_drop_uom(_uom_id) THEN
                -- Drop UoM
                RAISE DEBUG '%: delete UoM {%}.', ME, _uom_id;
                _qry := ' DELETE FROM ' || quote_ident(cget('TABLE_PS9_UOM')) ||
                              ' WHERE ' || quote_ident(cget('PS9_UOM_ID'))    || '=' || _uom_id
                              ;
                RAISE DEBUG '%: EXECUTE %', ME, _qry;
                EXECUTE _qry;
            END IF; -- ~#drop UoM

        ELSE
            RAISE DEBUG '%: quantity with ID #% is primitive (or does not exist): will not be dropped.', ME, quantity_id;
        END IF; -- ~#is_primitive_quantity
        RETURN;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** can_drop_uom (integer) ********************************************
-- TRUE if the UoM with the specified ID is not referenced by any quantity.
CREATE OR REPLACE FUNCTION can_drop_uom (
    uom_id  integer
) RETURNS boolean AS
$$
    DECLARE
        -- Log
        ME constant text := 'can_drop_uom()';
        -- Local variables
        _qry  text;
        _tup  record;
    BEGIN
        -- Check if there is any primitive in the referencing quantities
        _qry := ' SELECT COUNT(*) AS referencing_quantities '          ||
                  ' FROM ' || quote_ident(cget('TABLE_PS9_QUANTITY'))  || ','
                           || quote_ident(cget('TABLE_PS9_UOM'))       ||
                 ' WHERE ' || quote_ident(cget('TABLE_PS9_QUANTITY'))  || '.'
                           || quote_ident(cget('PS9_QUANTITY_UOM_ID')) || '='
                           || quote_ident(cget('TABLE_PS9_UOM'))       || '.'
                           || quote_ident(cget('PS9_UOM_ID'))          ||
                   ' AND ' || quote_ident(cget('TABLE_PS9_QUANTITY'))  || '.'
                           || quote_ident(cget('PS9_QUANTITY_UOM_ID')) || '=' || uom_id
                           ;
        RAISE DEBUG '%: EXECUTE %', ME, _qry;
        FOR _tup IN EXECUTE _qry LOOP
            IF _tup.referencing_quantities <> 0 THEN
                -- this uom is bound to 1+ quantity: cannot DROP
                RETURN FALSE;
            END IF;
        END LOOP;
        -- otherwise:
        RETURN TRUE;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** array_sort_distinct (anyarray) ********************************
-- Sorts an array (usage: SELECT sort_array_distinct('{3,2,1,2}'::integer[]);)
CREATE OR REPLACE FUNCTION array_sort_distinct (anyarray)
RETURNS anyarray AS
$$
    SELECT ARRAY(
      SELECT DISTINCT $1[s.i] AS "element"
      FROM
          generate_series(array_lower($1,1), array_upper($1,1)) AS s(i)
      ORDER BY element
    );
$$ LANGUAGE 'sql';


-- FUNCTION: ** index_crs_uri (integer) ********************************************
-- Returns an Index CRS of the specified dimensionality
CREATE OR REPLACE FUNCTION index_crs_uri (
    dimensionality  integer
) RETURNS text AS
$$
    BEGIN
        RETURN regexp_replace(
            cget('CRS_INDEX_1D'),
            cget('INDEX_ND_PATTERN'),
            regexp_replace(cget('INDEX_ND_PATTERN'), '\\d', dimensionality::text))
            ;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** translate_crs (text, text, integer) ****************************************
-- Returns a valid URI for an axis: ANSI Date if temporal, IndexND if CRS:1 and no action on geo-axis.
CREATE OR REPLACE FUNCTION translate_crs (
    crs_label   text,
    axis_type   text,
    axis_count  integer
) RETURNS text AS
$$
    DECLARE
        -- Log
        ME constant text := 'translate_crs()';
        -- Local variables
        _translated_crs_label  text;
    BEGIN
        _translated_crs_label := crs_label;
        IF axis_type = cget('TIME_AXIS_TYPE') THEN
            -- temporal axis: ANSI date CRS (see wiki:PetascopeTimeHandling)
            _translated_crs_label := cget('CRS_ANSI');
            RAISE DEBUG '%: % replaced with % CRS URI.', ME, crs_label, _translated_crs_label;
        ELSIF crs_label = cget('CRS_1') THEN
            -- CRS:1 axes: rename to appropriate Index CRS
            _translated_crs_label := index_crs_uri(axis_count);
            RAISE DEBUG '%: % replaced with % CRS URI.', ME, crs_label, _translated_crs_label;
            -- NOTE: currently SECORE stores Index CRSs for max 3D
        ELSE
            RAISE DEBUG '%: % not to be translated.', ME, _translated_crs_label;
        END IF;
        RETURN _translated_crs_label;
    END;
$$ LANGUAGE plpgsql;


-- FUNCTION: ** get_crs_code (text) ********************************************
-- Extracts the code from a CRS label, matching both URI and AUTH:CODE notation.
-- petascopedb=# SELECT substring('EPSG:4326' from '([^/:]*)$');
-- petascopedb=# SELECT substring('http://www.opengis.net/def/crs/EPSG/0/4326' from '([^/:]*)$');
CREATE OR REPLACE FUNCTION get_crs_code (
    crs_label text
) RETURNS text AS
$$
    BEGIN
        RETURN substring( crs_label, cget('CRS_CODE_PATTERN'));
    END;
$$ LANGUAGE plpgsql;


-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ --
-- Functions for the management of constants:
--
--   :: check_constants_tables_exists()
--   :: cget(key)           -> constant getter
--   :: cset(key, value)    -> constant setter
--
-- IMPORTANT
-- Tables names are `ps_string_constants'/`ps_numeric_constants' with fields 'key' and 'value'.
-- NOTE: some numbers are better written via operators, hence not automatically
-- not castable from string to numeric.
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ --

--- Make sure the *_constants tables exist ------------------
CREATE OR REPLACE FUNCTION check_constants_tables_exists() RETURNS VOID AS
$$
    DECLARE
        -- Log
        ME constant text := 'check_constants_tables_exists()';
    BEGIN
        -- String constants -------------------------
        PERFORM * FROM pg_catalog.pg_class
        WHERE relname = 'ps_string_constants';

        IF NOT FOUND THEN
            CREATE TABLE ps_string_constants (
               key   text PRIMARY KEY,
               value text
            );
            RAISE DEBUG '%: created table for string constants.', ME;
        END IF;

        -- Numeric constants -------------------------
        PERFORM * FROM pg_catalog.pg_class
        WHERE relname = 'ps_numeric_constants';

        IF NOT FOUND THEN
            CREATE TABLE ps_numeric_constants (
               key   text PRIMARY KEY,
               value numeric
            );
            RAISE DEBUG '%: created table for numeric constants.', ME;
        END IF;
    END;
$$ LANGUAGE plpgsql;

-- Set a variable -----------------------------------------------------
-- String -------------------------
CREATE OR REPLACE FUNCTION cset (
    IN xKey   text,
    IN xValue text
) RETURNS void AS
$$
DECLARE
    -- Log
    ME constant text := 'cset()';
BEGIN
    -- Check if table exists (create it otherwise)
    PERFORM check_constants_tables_exists();

    -- Has this constant already been defined previously?
    PERFORM * FROM ps_string_constants
             WHERE key = xKey;

    IF FOUND THEN
        RAISE WARNING '%: constant ''%'' was already defined and will be overwritten.', ME, xKey;
        UPDATE ps_string_constants SET value = xValue WHERE key = xKey;
    ELSE
        INSERT INTO ps_string_constants (key, value) VALUES (xKey, xValue);
    END IF;

    RETURN;
END;
$$ LANGUAGE plpgsql;
-- Numeric -------------------------
CREATE OR REPLACE FUNCTION cset (
    IN xKey   text,
    IN xValue numeric
) RETURNS void AS
$$
    DECLARE
        -- Log
        ME constant text := 'cset()';
    BEGIN
        -- Check if table exists (create it otherwise)
        PERFORM check_constants_tables_exists();

        -- Has this constant already been defined previously?
        PERFORM * FROM ps_numeric_constants
                 WHERE key = xKey;

        IF FOUND THEN
            RAISE WARNING '%: constant ''%'' was already defined and will not be overwritten.', ME, xKey;
        ELSE
            INSERT INTO ps_numeric_constants (key, value) VALUES (xKey, xValue);
        END IF;
        RETURN;
    END;
$$ LANGUAGE plpgsql;

-- Get a variable's value ---------------------------------------------
-- String -------------------------
CREATE OR REPLACE FUNCTION cget(
    IN  xKey   text,
    OUT xValue text
) AS
$$
    DECLARE
        -- Log
        ME constant text := 'cget()';
    BEGIN
        -- Check if table exists (create it otherwise)
        PERFORM check_constants_tables_exists();

        -- Has this constant already been defined previously?
        PERFORM value FROM ps_string_constants
                     WHERE key = xKey;

        IF NOT FOUND THEN
            RAISE EXCEPTION '%: variable ''%'' does not exist.', ME, xKey;
            RETURN;
        END IF;

        SELECT value INTO xValue FROM ps_string_constants WHERE key = xKey;
        RETURN;
END;
$$ LANGUAGE plpgsql;
-- Numeric -------------------------
CREATE OR REPLACE FUNCTION ncget(
    IN  xKey   text,
    OUT xValue numeric
) AS
$$
    DECLARE
        -- Log
        ME constant text := 'ncget()';
    BEGIN
        -- Check if table exists (create it otherwise)
        PERFORM check_constants_tables_exists();

        -- Has this constant already been defined previously?
        PERFORM value FROM ps_numeric_constants
                     WHERE key = xKey;

        IF NOT FOUND THEN
            RAISE EXCEPTION '%: variable ''%'' does not exist.', ME, xKey;
            RETURN;
        END IF;

        SELECT value INTO xValue FROM ps_numeric_constants WHERE key = xKey;
        RETURN;
    END;
$$ LANGUAGE plpgsql;
-----------------------------------------------------------------------
