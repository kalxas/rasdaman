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
-- |                 constants for database scripting                 |
-- ####################################################################
--
-- PURPOSE
-- Sets constant variables for database SQL scripts which
--
-- PREREQUISITES:
--   - `utilities.sql' has been imported for `cset()'
-----------------------------------------------------------------------

-- set constants, parametrizing the prefix of the _new_ tables (see further comments below)

CREATE TABLE IF NOT EXISTS ps_string_constants (
   key   text PRIMARY KEY,
   value text
);

CREATE TABLE IF NOT EXISTS ps_numeric_constants (
   key   text PRIMARY KEY,
   value numeric
);


    -- General
INSERT INTO ps_string_constants VALUES ('GMLCOV_METADATA_TYPE', 'gmlcov');
INSERT INTO ps_string_constants VALUES ('ID_FIELD',             'id');
INSERT INTO ps_string_constants VALUES ('OCTET_STREAM_MIME',    'application/x-octet-stream');
INSERT INTO ps_string_constants VALUES ('SWE_QUANTITY_FIELD',   'Quantity');
INSERT INTO ps_string_constants VALUES ('UOM_PURE_NUM',         '10^0');
    -- Regex patterns
INSERT INTO ps_string_constants VALUES ('NAME_PATTERN',            '^[_A-Za-z][-._A-Za-z0-9]*$');
INSERT INTO ps_string_constants VALUES ('SERVICE_VERSION_PATTERN', '^\\d+\\.\\d?\\d\\.\\d?\\d$');
INSERT INTO ps_string_constants VALUES ('UOM_PATTERN',             '^[^: \\n\\r\\t]+$');
INSERT INTO ps_string_constants VALUES ('INDEX_ND_PATTERN',        'Index\\dD'); -- Used when converting CRS:1 to Index CRSs during migration.
INSERT INTO ps_string_constants VALUES ('CRS_CODE_PATTERN',         '([^/:]*)$');
INSERT INTO ps_string_constants VALUES ('EPSG_PATTERN',             'EPSG|epsg');
    -- Service and service provider metadata
INSERT INTO ps_string_constants VALUES ('WCS_SERVICE_TYPE',             'OGC WCS');
INSERT INTO ps_string_constants VALUES ('WCS_SERVICE_TYPE_VERSIONS',    '2.0.1'); -- use commas for different supported versions
INSERT INTO ps_string_constants VALUES ('WCS_SERVICE_TITLE',            'rasdaman');
INSERT INTO ps_string_constants VALUES ('WCS_SERVICE_ABSTRACT',         'rasdaman server - free download from www.rasdaman.org');
INSERT INTO ps_string_constants VALUES ('WCS_PROVIDER_NAME',            'Jacobs University Bremen');
INSERT INTO ps_string_constants VALUES ('WCS_PROVIDER_SITE',            'http://rasdaman.org/');
INSERT INTO ps_string_constants VALUES ('WCS_PROVIDER_CONTACT_NAME',    'Prof. Dr. Peter Baumann');
INSERT INTO ps_string_constants VALUES ('WCS_PROVIDER_CONTACT_CITY',    'Bremen');
INSERT INTO ps_string_constants VALUES ('WCS_PROVIDER_CONTACT_PCODE',   '28717');
INSERT INTO ps_string_constants VALUES ('WCS_PROVIDER_CONTACT_COUNTRY', 'Germany');
INSERT INTO ps_string_constants VALUES ('WCS_PROVIDER_CONTACT_EMAIL',   'p.baumann@jacobs-university.de');
INSERT INTO ps_string_constants VALUES ('WCS_PROVIDER_CONTACT_ROLE',    'Project Leader');
    -- CRSs
INSERT INTO ps_string_constants VALUES ('CRS_1',                'CRS:1'); -- resolution formula is different on CRS:1 grids (-1 term is dropped).
INSERT INTO ps_string_constants VALUES ('CRS_4326',             '4326'); -- resolution formula is different on CRS:1 grids (-1 term is dropped).
INSERT INTO ps_string_constants VALUES ('SECORE_ENTRY',         '%SECORE_URL%'); -- [!] This shall match with Petascope constant to replace it with the configured resolver.
INSERT INTO ps_string_constants VALUES ('TIME_AXIS_TYPE',       't'); -- CRS becomes CRS_ANSI for temporal axis on pre-upgrade 8.X coverages.
INSERT INTO ps_string_constants VALUES ('Y_AXIS_TYPE',          'y'); -- Origin is Upper-Left corner
INSERT INTO ps_string_constants VALUES ('CRS_ANSI',         '%SECORE_URL%/crs/OGC/0/AnsiDate');
INSERT INTO ps_string_constants VALUES ('CRS_INDEX_1D',     '%SECORE_URL%/crs/OGC/0/Index1D');
INSERT INTO ps_string_constants VALUES ('CRS_INDEX_2D',     '%SECORE_URL%/crs/OGC/0/Index2D');
INSERT INTO ps_string_constants VALUES ('CRS_INDEX_3D',     '%SECORE_URL%/crs/OGC/0/Index3D');
INSERT INTO ps_string_constants VALUES ('CRS_INDEX_4D',     '%SECORE_URL%/crs/OGC/0/Index4D');
INSERT INTO ps_string_constants VALUES ('CRS_INDEX_5D',     '%SECORE_URL%/crs/OGC/0/Index5D');
INSERT INTO ps_string_constants VALUES ('CRS_EOBSTEST_T',   '%SECORE_URL%/crs/OGC/0/Temporal?epoch="1950-01-01T00:00:00"&uom="d"');
INSERT INTO ps_string_constants VALUES ('CRS_WGS84_2D',     '%SECORE_URL%/crs/EPSG/0/4326');
INSERT INTO ps_string_constants VALUES ('CRS_WGS84_3D',     '%SECORE_URL%/crs/EPSG/0/4327');
    -- GML coverage types
INSERT INTO ps_string_constants VALUES ('GML_ABSTRACT_COV',              'AbstractCoverage');
INSERT INTO ps_string_constants VALUES ('GML_ABSTRACT_DISCRETE_COV',     'AbstractDiscreteCoverage');
INSERT INTO ps_string_constants VALUES ('GML_GRID_COV',                  'GridCoverage');
INSERT INTO ps_string_constants VALUES ('GML_RECTIFIED_GRID_COV',        'RectifiedGridCoverage');
INSERT INTO ps_string_constants VALUES ('GML_REFERENCEABLE_GRID_COV',    'ReferenceableGridCoverage');
INSERT INTO ps_string_constants VALUES ('GML_MULTIPOINT_COV',            'MultiPointCoverage');
INSERT INTO ps_string_constants VALUES ('GML_MULTICURVE_COV',            'MultiCurveCoverage');
INSERT INTO ps_string_constants VALUES ('GML_MULTISURFACE_COV',          'MultiSurfaceCoverage');
INSERT INTO ps_string_constants VALUES ('GML_MULTISOLID_COV',            'MultiSolidCoverage');
    -- Extra metadata types (internal naming)
INSERT INTO ps_string_constants VALUES ('METADATA_TYPE_OWS',          'ows');
INSERT INTO ps_string_constants VALUES ('METADATA_TYPE_GMLCOV',       'gmlcov');
INSERT INTO ps_string_constants VALUES ('METADATA_TYPE_ATTRTABLE',    'attrtable_name');
    -- Range data types
INSERT INTO ps_string_constants VALUES ('PRIMITIVE',    'primitive'); -- these types/quantities will not be dropped (CASCADE)
INSERT INTO ps_string_constants VALUES ('DT_BOOLEAN',   'boolean');
INSERT INTO ps_string_constants VALUES ('DT_CHAR',      'char');
INSERT INTO ps_string_constants VALUES ('DT_UCHAR',     'unsigned char');
INSERT INTO ps_string_constants VALUES ('DT_SHORT',     'short');
INSERT INTO ps_string_constants VALUES ('DT_USHORT',    'unsigned short');
INSERT INTO ps_string_constants VALUES ('DT_INT',       'int');
INSERT INTO ps_string_constants VALUES ('DT_UINT',      'unsigned int');
INSERT INTO ps_string_constants VALUES ('DT_LONG',      'long');
INSERT INTO ps_string_constants VALUES ('DT_ULONG',     'unsigned long');
INSERT INTO ps_string_constants VALUES ('DT_FLOAT',     'float');
INSERT INTO ps_string_constants VALUES ('DT_DOUBLE',    'double');
INSERT INTO ps_string_constants VALUES ('DT_COMPLEX',   'complex');
INSERT INTO ps_string_constants VALUES ('DT_COMPLEX2',  'complex2');
    -- Range data types meanings
INSERT INTO ps_string_constants VALUES ('DT_BOOLEAN_MEANING',    'Boolean');
INSERT INTO ps_string_constants VALUES ('DT_CHAR_MEANING',       '8-bit signed integer');
INSERT INTO ps_string_constants VALUES ('DT_UCHAR_MEANING',      '8-bit unsigned integer');
INSERT INTO ps_string_constants VALUES ('DT_SHORT_MEANING',      '16-bit signed integer');
INSERT INTO ps_string_constants VALUES ('DT_USHORT_MEANING',     '16-bit unsigned integer');
INSERT INTO ps_string_constants VALUES ('DT_INT_MEANING',        '32-bit signed integer');
INSERT INTO ps_string_constants VALUES ('DT_UINT_MEANING',       '32-bit unsigned integer');
INSERT INTO ps_string_constants VALUES ('DT_LONG_MEANING',       '64-bit signed integer');
INSERT INTO ps_string_constants VALUES ('DT_ULONG_MEANING',      '64-bit unsigned integer');
INSERT INTO ps_string_constants VALUES ('DT_FLOAT_MEANING',      'Single precision floating point number');
INSERT INTO ps_string_constants VALUES ('DT_DOUBLE_MEANING',     'Double precision floating point number');
INSERT INTO ps_string_constants VALUES ('DT_COMPLEX_MEANING',    'Single precision complex number');
INSERT INTO ps_string_constants VALUES ('DT_COMPLEX2_MEANING',   'Double precision complex number');
    -- Range data types definition URIs
INSERT INTO ps_string_constants VALUES ('DT_CHAR_DEFINITION',       'http://www.opengis.net/def/dataType/OGC/0/signedByte');
INSERT INTO ps_string_constants VALUES ('DT_UCHAR_DEFINITION',      'http://www.opengis.net/def/dataType/OGC/0/unsignedByte');
INSERT INTO ps_string_constants VALUES ('DT_SHORT_DEFINITION',      'http://www.opengis.net/def/dataType/OGC/0/signedShort');
INSERT INTO ps_string_constants VALUES ('DT_USHORT_DEFINITION',     'http://www.opengis.net/def/dataType/OGC/0/unsignedShort');
INSERT INTO ps_string_constants VALUES ('DT_INT_DEFINITION',        'http://www.opengis.net/def/dataType/OGC/0/signedInt');
INSERT INTO ps_string_constants VALUES ('DT_UINT_DEFINITION',       'http://www.opengis.net/def/dataType/OGC/0/unsignedInt');
INSERT INTO ps_string_constants VALUES ('DT_LONG_DEFINITION',       'http://www.opengis.net/def/dataType/OGC/0/signedLong');
INSERT INTO ps_string_constants VALUES ('DT_ULONG_DEFINITION',      'http://www.opengis.net/def/dataType/OGC/0/unsignedLong');
INSERT INTO ps_string_constants VALUES ('DT_FLOAT_DEFINITION',      'http://www.opengis.net/def/dataType/OGC/0/float32');
INSERT INTO ps_string_constants VALUES ('DT_DOUBLE_DEFINITION',     'http://www.opengis.net/def/dataType/OGC/0/float64');
    -- Data intervals
INSERT INTO ps_numeric_constants VALUES ('CHAR_MIN',                        -128);
INSERT INTO ps_numeric_constants VALUES ('CHAR_MAX',                         127);
INSERT INTO ps_numeric_constants VALUES ('UCHAR_MIN',                          0);
INSERT INTO ps_numeric_constants VALUES ('UCHAR_MAX',                        255);
INSERT INTO ps_numeric_constants VALUES ('SHORT_MIN',                     -32768);
INSERT INTO ps_numeric_constants VALUES ('SHORT_MAX',                      32767);
INSERT INTO ps_numeric_constants VALUES ('USHORT_MIN',                         0);
INSERT INTO ps_numeric_constants VALUES ('USHORT_MAX',                     65535);
INSERT INTO ps_numeric_constants VALUES ('INT_MIN',                  -2147483648);
INSERT INTO ps_numeric_constants VALUES ('INT_MAX',                   2147483647);
INSERT INTO ps_numeric_constants VALUES ('UINT_MIN',                           0);
INSERT INTO ps_numeric_constants VALUES ('UINT_MAX',                  4294967295);
INSERT INTO ps_numeric_constants VALUES ('LONG_MIN',        -9223372036854775808);
INSERT INTO ps_numeric_constants VALUES ('LONG_MAX',         9223372036854775807);
INSERT INTO ps_numeric_constants VALUES ('ULONG_MIN',                          0);
INSERT INTO ps_numeric_constants VALUES ('ULONG_MAX',       18446744073709551615);
INSERT INTO ps_numeric_constants VALUES ('FLOAT_MIN',              -(3.4028234E38));
INSERT INTO ps_numeric_constants VALUES ('FLOAT_MAX',               3.4028234E38);
INSERT INTO ps_numeric_constants VALUES ('DOUBLE_MIN',    -(1.7976931348623157E308));
INSERT INTO ps_numeric_constants VALUES ('DOUBLE_MAX',    1.7976931348623157E308);
    -- PS_ (COVERAGE)
INSERT INTO ps_string_constants VALUES ('PS_PREFIX','ps');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_AXISTYPE',      'ps_axistype');
INSERT INTO ps_string_constants VALUES ('PS_AXISTYPE_ID',         'id');
INSERT INTO ps_string_constants VALUES ('PS_AXISTYPE_TYPE',       'axistype');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_COVERAGE',             'ps_coverage');
INSERT INTO ps_string_constants VALUES ('PS_COVERAGE_ID',         'id');
INSERT INTO ps_string_constants VALUES ('PS_COVERAGE_NAME',       'name');
INSERT INTO ps_string_constants VALUES ('PS_COVERAGE_TYPE',       'type');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_CRS',                  'ps_crs');
INSERT INTO ps_string_constants VALUES ('PS_CRS_ID',              'id');
INSERT INTO ps_string_constants VALUES ('PS_CRS_NAME',            'name');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_CRSSET',               'ps_crsset');
INSERT INTO ps_string_constants VALUES ('PS_CRSSET_AXIS',         'axis');
INSERT INTO ps_string_constants VALUES ('PS_CRSSET_CRS',          'crs');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_DATATYPE',             'ps_datatype');
INSERT INTO ps_string_constants VALUES ('PS_DATATYPE_ID',         'id');
INSERT INTO ps_string_constants VALUES ('PS_DATATYPE_TYPE',       'datatype');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_DOMAIN',               'ps_domain');
INSERT INTO ps_string_constants VALUES ('PS_DOMAIN_ID',           'id');
INSERT INTO ps_string_constants VALUES ('PS_DOMAIN_COVERAGE',     'coverage');
INSERT INTO ps_string_constants VALUES ('PS_DOMAIN_NAME',         'name');
INSERT INTO ps_string_constants VALUES ('PS_DOMAIN_TYPE',         'type');
INSERT INTO ps_string_constants VALUES ('PS_DOMAIN_NUMLO',        'numlo');
INSERT INTO ps_string_constants VALUES ('PS_DOMAIN_NUMHI',        'numhi');
INSERT INTO ps_string_constants VALUES ('PS_DOMAIN_I',            'i');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_CELLDOMAIN',           'ps_celldomain');
INSERT INTO ps_string_constants VALUES ('PS_CELLDOMAIN_COVERAGE', 'coverage');
INSERT INTO ps_string_constants VALUES ('PS_CELLDOMAIN_I',        'i');
INSERT INTO ps_string_constants VALUES ('PS_CELLDOMAIN_LO',       'lo');
INSERT INTO ps_string_constants VALUES ('PS_CELLDOMAIN_HI',       'hi');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_METADATA',             'ps_metadata');
INSERT INTO ps_string_constants VALUES ('PS_METADATA_COVERAGE',   'coverage');
INSERT INTO ps_string_constants VALUES ('PS_METADATA_METADATA',   'metadata');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_RANGE',                'ps_range');
INSERT INTO ps_string_constants VALUES ('PS_RANGE_COVERAGE',      'coverage');
INSERT INTO ps_string_constants VALUES ('PS_RANGE_I',             'i');
INSERT INTO ps_string_constants VALUES ('PS_RANGE_NAME',          'name');
INSERT INTO ps_string_constants VALUES ('PS_RANGE_TYPE',          'type');
INSERT INTO ps_string_constants VALUES ('PS_RANGE_UOM',           'uom');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_UOM',                  'ps_uom');
INSERT INTO ps_string_constants VALUES ('PS_UOM_ID',              'id');
INSERT INTO ps_string_constants VALUES ('PS_UOM_UOM',             'uom');
INSERT INTO ps_string_constants VALUES ('PS_UOM_LINK',            'link');
    -- PS_ (WMS)
INSERT INTO ps_string_constants VALUES ('TABLE_PS_SERVICES',           'ps_services');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_LAYERS',             'ps_layers');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_SERVICELAYER',       'ps_servicelayer');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_STYLES',             'ps_styles');
INSERT INTO ps_string_constants VALUES ('TABLE_PS_PYRAMIDLEVELS',      'ps_pyramidlevels');
    -- PS9_
    -- This prefix is set as argument since during upgrade these tables have a first
    -- interim prefix 'ps_' to avoid clash while co-existing with old tables,
    -- and then are renamed back to 'ps_*'.
INSERT INTO ps_string_constants VALUES ('PS9_PREFIX', 'ps');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_COVERAGE',                         'ps_coverage');
INSERT INTO ps_string_constants VALUES ('PS9_COVERAGE_ID',                     'id');
INSERT INTO ps_string_constants VALUES ('PS9_COVERAGE_NAME',                   'name');
INSERT INTO ps_string_constants VALUES ('PS9_COVERAGE_GML_TYPE_ID',            'gml_type_id');
INSERT INTO ps_string_constants VALUES ('PS9_COVERAGE_FORMAT_ID',              'native_format_id');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_CRS',                              'ps_crs');
INSERT INTO ps_string_constants VALUES ('PS9_CRS_ID',                          'id');
INSERT INTO ps_string_constants VALUES ('PS9_CRS_URI',                         'uri');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_DBUPDATES',                        'ps_dbupdates');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_DOMAINSET',                        'ps_domain_set');
INSERT INTO ps_string_constants VALUES ('PS9_DOMAINSET_COVERAGE_ID',           'coverage_id');
INSERT INTO ps_string_constants VALUES ('PS9_DOMAINSET_CRS_IDS',               'native_crs_ids');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_GRIDDED_DOMAINSET',                'ps_gridded_domain_set');
INSERT INTO ps_string_constants VALUES ('PS9_GRIDDED_DOMAINSET_COVERAGE_ID',   'coverage_id');
INSERT INTO ps_string_constants VALUES ('PS9_GRIDDED_DOMAINSET_ORIGIN',        'grid_origin');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_GRID_AXIS',                        'ps_grid_axis');
INSERT INTO ps_string_constants VALUES ('PS9_GRID_AXIS_ID',                    'id');
INSERT INTO ps_string_constants VALUES ('PS9_GRID_AXIS_COVERAGE_ID',           'gridded_coverage_id');
INSERT INTO ps_string_constants VALUES ('PS9_GRID_AXIS_RASDAMAN_ORDER',        'rasdaman_order');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_DESCRIPTION',                      'ps_description');
INSERT INTO ps_string_constants VALUES ('PS9_DESCRIPTION_KEYWORD_GROUP_IDS',    'keyword_group_ids');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_EXTRA_METADATA',                   'ps_extra_metadata');
INSERT INTO ps_string_constants VALUES ('PS9_EXTRA_METADATA_COVERAGE_ID',      'coverage_id');
INSERT INTO ps_string_constants VALUES ('PS9_EXTRA_METADATA_TYPE',             'metadata_type_id');
INSERT INTO ps_string_constants VALUES ('PS9_EXTRA_METADATA_VALUE',            'value');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_EXTRA_METADATA_TYPE',              'ps_extra_metadata_type');
INSERT INTO ps_string_constants VALUES ('PS9_EXTRA_METADATA_TYPE_ID',          'id');
INSERT INTO ps_string_constants VALUES ('PS9_EXTRA_METADATA_TYPE_TYPE',        'type');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_GML_SUBTYPE',                      'ps_gml_subtype');
INSERT INTO ps_string_constants VALUES ('PS9_GML_SUBTYPE_ID',                  'id');
INSERT INTO ps_string_constants VALUES ('PS9_GML_SUBTYPE_SUBTYPE',             'subtype');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_KEYWORD',                          'ps_keyword');
INSERT INTO ps_string_constants VALUES ('PS9_KEYWORD_ID',                      'id');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_KEYWORD_GROUP',                    'ps_keyword_group');
INSERT INTO ps_string_constants VALUES ('PS9_KEYWORD_GROUP_ID',                 'id');
INSERT INTO ps_string_constants VALUES ('PS9_KEYWORD_GROUP_KEYWORD_IDS',        'keyword_ids');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_INTERVAL',                         'ps_interval');
INSERT INTO ps_string_constants VALUES ('PS9_INTERVAL_ID',                     'id');
INSERT INTO ps_string_constants VALUES ('PS9_INTERVAL_MIN',                    'min');
INSERT INTO ps_string_constants VALUES ('PS9_INTERVAL_MAX',                    'max');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_NIL_VALUE',                        'ps_nil_value');
INSERT INTO ps_string_constants VALUES ('PS9_NIL_VALUE_ID',                     'id');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_QUANTITY_INTERVAL',                'ps_quantity_interval');
INSERT INTO ps_string_constants VALUES ('PS9_QUANTITY_INTERVAL_QID',           'quantity_id');
INSERT INTO ps_string_constants VALUES ('PS9_QUANTITY_INTERVAL_IID',           'interval_id');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_MIME_TYPE',                        'ps_mime_type');
INSERT INTO ps_string_constants VALUES ('PS9_MIME_TYPE_ID',                    'id');
INSERT INTO ps_string_constants VALUES ('PS9_MIME_TYPE_MIME',                  'mime_type');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_QUANTITY',                         'ps_quantity');
INSERT INTO ps_string_constants VALUES ('PS9_QUANTITY_ID',                     'id');
INSERT INTO ps_string_constants VALUES ('PS9_QUANTITY_UOM_ID',                 'uom_id');
INSERT INTO ps_string_constants VALUES ('PS9_QUANTITY_URI',                    'definition_uri');
INSERT INTO ps_string_constants VALUES ('PS9_QUANTITY_DESCRIPTION',            'description');
INSERT INTO ps_string_constants VALUES ('PS9_QUANTITY_LABEL',                  'label');
INSERT INTO ps_string_constants VALUES ('PS9_QUANTITY_NIL_IDS',                'nil_ids');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_RANGE_DATATYPE',                   'ps_range_data_type');
INSERT INTO ps_string_constants VALUES ('PS9_RANGE_DATATYPE_ID',               'id');
INSERT INTO ps_string_constants VALUES ('PS9_RANGE_DATATYPE_NAME',             'name');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_RANGESET',                         'ps_range_set');
INSERT INTO ps_string_constants VALUES ('PS9_RANGESET_COVERAGE_ID',            'coverage_id');
INSERT INTO ps_string_constants VALUES ('PS9_RANGESET_STORAGE_ID',             'storage_id');
INSERT INTO ps_string_constants VALUES ('PS9_RANGESET_STORAGE_TABLE',          'storage_table');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_RANGETYPE_COMPONENT',              'ps_range_type_component');
INSERT INTO ps_string_constants VALUES ('PS9_RANGETYPE_COMPONENT_ID',          'id');
INSERT INTO ps_string_constants VALUES ('PS9_RANGETYPE_COMPONENT_COVERAGE_ID', 'coverage_id');
INSERT INTO ps_string_constants VALUES ('PS9_RANGETYPE_COMPONENT_NAME',        'name');
INSERT INTO ps_string_constants VALUES ('PS9_RANGETYPE_COMPONENT_TYPE_ID',     'data_type_id');
INSERT INTO ps_string_constants VALUES ('PS9_RANGETYPE_COMPONENT_ORDER',       'component_order');
INSERT INTO ps_string_constants VALUES ('PS9_RANGETYPE_COMPONENT_FIELD_ID',    'field_id');
INSERT INTO ps_string_constants VALUES ('PS9_RANGETYPE_COMPONENT_FIELD_TABLE', 'field_table');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_RASDAMAN_COLLECTION',              'ps_rasdaman_collection');
INSERT INTO ps_string_constants VALUES ('PS9_RASDAMAN_COLLECTION_ID',          'id');
INSERT INTO ps_string_constants VALUES ('PS9_RASDAMAN_COLLECTION_NAME',        'name');
INSERT INTO ps_string_constants VALUES ('PS9_RASDAMAN_COLLECTION_OID',         'oid');
INSERT INTO ps_string_constants VALUES ('PS9_RASDAMAN_COLLECTION_BASETYPE',    'base_type');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_RECTILINEAR_AXIS',                 'ps_rectilinear_axis');
INSERT INTO ps_string_constants VALUES ('PS9_RECTILINEAR_AXIS_ID',             'grid_axis_id');
INSERT INTO ps_string_constants VALUES ('PS9_RECTILINEAR_AXIS_OFFSET_VECTOR',  'offset_vector');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_SERVICE_IDENTIFICATION',           'ps_service_identification');
INSERT INTO ps_string_constants VALUES ('PS9_SERVICE_IDENTIFICATION_ID',       'id');
INSERT INTO ps_string_constants VALUES ('PS9_SERVICE_IDENTIFICATION_TYPE',     'type');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_SERVICE_PROVIDER',                 'ps_service_provider');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_UOM',                              'ps_uom');
INSERT INTO ps_string_constants VALUES ('PS9_UOM_ID',                          'id');
INSERT INTO ps_string_constants VALUES ('PS9_UOM_CODE',                        'code');
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_VECTOR_COEFFICIENTS',              'ps_vector_coefficients');
INSERT INTO ps_string_constants VALUES ('PS9_VECTOR_COEFFICIENTS_ID',          'grid_axis_id');
INSERT INTO ps_string_constants VALUES ('PS9_VECTOR_COEFFICIENTS_VALUE',       'coefficient');
INSERT INTO ps_string_constants VALUES ('PS9_VECTOR_COEFFICIENTS_ORDER',       'coefficient_order');
    -- PS migration (dropped after migration)
INSERT INTO ps_string_constants VALUES ('TABLE_PS9_NORTH_FIRST_CRSS',       'ps_north_first_crs');
INSERT INTO ps_string_constants VALUES ('PS9_NORTH_FIRST_CRSS_CODE', 'crs_code');
    -- RAS_
INSERT INTO ps_string_constants VALUES ('DB_RASBASE', 'RASBASE');
INSERT INTO ps_string_constants VALUES ('RAS_PREFIX', 'ras');
INSERT INTO ps_string_constants VALUES ('TABLE_RAS_MDD_COLLECTIONS',            'ras_mddcollections');
INSERT INTO ps_string_constants VALUES ('RAS_MDD_COLLECTIONS_MDDID',     'mddid');
INSERT INTO ps_string_constants VALUES ('RAS_MDD_COLLECTIONS_MDDCOLLID', 'mddcollid');
INSERT INTO ps_string_constants VALUES ('TABLE_RAS_MDD_COLLNAMES',              'ras_mddcollnames');
INSERT INTO ps_string_constants VALUES ('RAS_MDD_COLLNAMES_COLLID',      'mddcollid');
INSERT INTO ps_string_constants VALUES ('RAS_MDD_COLLNAMES_COLLNAME',    'mddcollname');
INSERT INTO ps_string_constants VALUES ('TABLE_RAS_MDD_OBJECTS',                'ras_mddobjects');
INSERT INTO ps_string_constants VALUES ('RAS_MDD_OBJECTS_MDDID',         'mddid');
