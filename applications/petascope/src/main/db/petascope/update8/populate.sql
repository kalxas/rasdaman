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
-- |                    petascopedb 9.0 population                     |
-- ####################################################################

-----------------------------------------------------------------------
-- Insert necessary data in the database for coverage and service description.
--
-- PREREQUISITES
--   - PL/pgSQL is installed.
--   - Schema has been created (`schema9.sql')
--   - `global_const.sql' has been imported.
-----------------------------------------------------------------------


-- Default service and service provider metadata:
INSERT INTO ps9_description (titles, abstracts) VALUES (
       ARRAY[cget('WCS_SERVICE_TITLE')],
       ARRAY[cget('WCS_SERVICE_ABSTRACT')]);
INSERT INTO ps9_service_identification (type, type_versions, description_id) VALUES (
              cget('WCS_SERVICE_TYPE'),
        ARRAY[cget('WCS_SERVICE_TYPE_VERSIONS')],
       (SELECT id FROM  ps9_description
                  WHERE    titles=ARRAY[cget('WCS_SERVICE_TITLE')]
                  AND   abstracts=ARRAY[cget('WCS_SERVICE_ABSTRACT')])
       );
INSERT INTO ps9_service_provider
       (name, site, contact_individual_name, contact_city, contact_postal_code, contact_country, contact_email_addresses, contact_role)
       VALUES (cget('WCS_PROVIDER_NAME'),
               cget('WCS_PROVIDER_SITE'),
               cget('WCS_PROVIDER_CONTACT_NAME'),
               cget('WCS_PROVIDER_CONTACT_CITY'),
               cget('WCS_PROVIDER_CONTACT_PCODE'),
               cget('WCS_PROVIDER_CONTACT_COUNTRY'),
         ARRAY[cget('WCS_PROVIDER_CONTACT_EMAIL')],
               cget('WCS_PROVIDER_CONTACT_ROLE'));

--
-- CRSs for systemtest datasets for ANSI-Date (rasdaman 8.4 legacy)
--
-- TODO: init9.sql.in to replace with configure %SECORE% host
INSERT INTO ps9_crs (uri) VALUES (cget('CRS_ANSI'));
INSERT INTO ps9_crs (uri) VALUES (cget('CRS_INDEX_1D'));
INSERT INTO ps9_crs (uri) VALUES (cget('CRS_INDEX_2D'));
INSERT INTO ps9_crs (uri) VALUES (cget('CRS_INDEX_3D'));
INSERT INTO ps9_crs (uri) VALUES (cget('CRS_EOBSTEST_T'));
INSERT INTO ps9_crs (uri) VALUES (cget('CRS_WGS84_2D'));
INSERT INTO ps9_crs (uri) VALUES (cget('CRS_WGS84_3D'));

--
-- GML coverage types:
--
INSERT INTO ps9_gml_subtype (subtype) VALUES (cget('GML_ABSTRACT_COV'));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent)
       VALUES (cget('GML_ABSTRACT_DISCRETE_COV'),
              (SELECT id FROM ps9_gml_subtype WHERE subtype=cget('GML_ABSTRACT_COV')));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent)
       VALUES (cget('GML_GRID_COV'),
              (SELECT id FROM ps9_gml_subtype WHERE subtype=cget('GML_ABSTRACT_DISCRETE_COV')));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent)
       VALUES (cget('GML_RECTIFIED_GRID_COV'),
              (SELECT id FROM ps9_gml_subtype WHERE subtype=cget('GML_ABSTRACT_DISCRETE_COV')));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent)
       VALUES (cget('GML_REFERENCEABLE_GRID_COV'),
              (SELECT id FROM ps9_gml_subtype WHERE subtype=cget('GML_ABSTRACT_DISCRETE_COV')));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent)
       VALUES (cget('GML_MULTIPOINT_COV'),
              (SELECT id FROM ps9_gml_subtype WHERE subtype=cget('GML_ABSTRACT_DISCRETE_COV')));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent)
       VALUES (cget('GML_MULTICURVE_COV'),
              (SELECT id FROM ps9_gml_subtype WHERE subtype=cget('GML_ABSTRACT_DISCRETE_COV')));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent)
       VALUES (cget('GML_MULTISURFACE_COV'),
              (SELECT id FROM ps9_gml_subtype WHERE subtype=cget('GML_ABSTRACT_DISCRETE_COV')));
INSERT INTO ps9_gml_subtype (subtype, subtype_parent)
       VALUES (cget('GML_MULTISOLID_COV'),
              (SELECT id FROM ps9_gml_subtype WHERE subtype=cget('GML_ABSTRACT_DISCRETE_COV')));

--
-- Extra-metadata types:
--
---- /wcs:Capabilities/wcs:Contents/wcs:CoverageSummary/ows:Metadata
INSERT INTO ps9_extra_metadata_type (type) VALUES (cget('METADATA_TYPE_OWS'));
---- /wcs:CoverageDescriptions/wcs:CoverageDescription/gmlcov:metadata  AND
---- /gmlcov:AbstractCoverage/gmlcov:metadata
INSERT INTO ps9_extra_metadata_type (type) VALUES (cget('METADATA_TYPE_GMLCOV'));
---- Rasters' attribute table name for rasimport
INSERT INTO ps9_extra_metadata_type (type) VALUES (cget('METADATA_TYPE_ATTRTABLE'));

--
-- Range data types (OGC 08-068r2, Tab.2)
--
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_BOOLEAN'),  cget('DT_BOOLEAN_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_CHAR'),     cget('DT_CHAR_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_UCHAR'),    cget('DT_UCHAR_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_SHORT'),    cget('DT_SHORT_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_USHORT'),   cget('DT_USHORT_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_INT'),      cget('DT_INT_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_UINT'),     cget('DT_UINT_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_LONG'),     cget('DT_LONG_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_ULONG'),    cget('DT_ULONG_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_FLOAT'),    cget('DT_FLOAT_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_DOUBLE'),   cget('DT_DOUBLE_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_COMPLEX'),  cget('DT_COMPLEX_MEANING'));
INSERT INTO ps9_range_data_type (name, meaning)
     VALUES (cget('DT_COMPLEX2'), cget('DT_COMPLEX2_MEANING'));

--
-- Allowed intervals (see petascope.util.WcsUtil.java)
--
INSERT INTO ps9_interval (min, max) VALUES (  ncget('CHAR_MIN'),   ncget('CHAR_MAX')); -- char_8
INSERT INTO ps9_interval (min, max) VALUES ( ncget('UCHAR_MIN'),  ncget('UCHAR_MAX')); -- uchar_8
INSERT INTO ps9_interval (min, max) VALUES ( ncget('SHORT_MIN'),  ncget('SHORT_MAX')); -- short_16
INSERT INTO ps9_interval (min, max) VALUES (ncget('USHORT_MIN'), ncget('USHORT_MAX')); -- ushort_16
INSERT INTO ps9_interval (min, max) VALUES (   ncget('INT_MIN'),    ncget('INT_MAX')); -- int_32
INSERT INTO ps9_interval (min, max) VALUES (  ncget('UINT_MIN'),   ncget('UINT_MAX')); -- uint_32
INSERT INTO ps9_interval (min, max) VALUES (  ncget('LONG_MIN'),   ncget('LONG_MAX')); -- long_64
INSERT INTO ps9_interval (min, max) VALUES ( ncget('ULONG_MIN'),  ncget('ULONG_MAX')); -- ulong_64
INSERT INTO ps9_interval (min, max) VALUES ( ncget('FLOAT_MIN'),  ncget('FLOAT_MAX')); -- float_32
INSERT INTO ps9_interval (min, max) VALUES (ncget('DOUBLE_MIN'), ncget('DOUBLE_MAX')); -- double_64

-- UoM for pure numbers
INSERT INTO ps9_uom (code) VALUES (cget('UOM_PURE_NUM'));

-- Insert quantities for each data type (allowed values determine a quantity)
-- Put keyword PRIMITIVE in the description to avoid DROP CASCADE (see range_type_drop() trigger).
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_CHAR'), cget('PRIMITIVE'), cget('DT_CHAR_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_UCHAR'), cget('PRIMITIVE'), cget('DT_UCHAR_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_SHORT'), cget('PRIMITIVE'), cget('DT_SHORT_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_USHORT'), cget('PRIMITIVE'), cget('DT_USHORT_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_INT'), cget('PRIMITIVE'), cget('DT_INT_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_UINT'), cget('PRIMITIVE'), cget('DT_UINT_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_LONG'),   cget('PRIMITIVE'), cget('DT_LONG_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_ULONG'),  cget('PRIMITIVE'), cget('DT_ULONG_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_FLOAT'),  cget('PRIMITIVE'), cget('DT_FLOAT_DEFINITION'));
INSERT INTO ps9_quantity (uom_id, label, description, definition_uri) VALUES (
      (SELECT id FROM ps9_uom WHERE code = cget('UOM_PURE_NUM')),
      cget('DT_DOUBLE'), cget('PRIMITIVE'), cget('DT_DOUBLE_DEFINITION'));
-- .. and their allowed values:
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_CHAR')   AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('CHAR_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_UCHAR')  AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('UCHAR_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_SHORT')  AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('SHORT_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_USHORT') AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('USHORT_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_INT')    AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('INT_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_UINT')   AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('UINT_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_LONG')   AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('LONG_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_ULONG')  AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('ULONG_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_FLOAT')  AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('FLOAT_MAX')));
INSERT INTO ps9_quantity_interval (quantity_id, interval_id)
       VALUES ((SELECT id FROM ps9_quantity WHERE label=cget('DT_DOUBLE') AND description=cget('PRIMITIVE')),
               (SELECT id FROM ps9_interval WHERE max=ncget('DOUBLE_MAX')));

-- Formats/MIME types/GDAL ids:
-- TODO string constants to global_const.sql
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
