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

----------------------------------------------------------------------
-- Add comments on petascopedb (#ticket 212)
--
-- How to get comment on database object:
--
-- to view comments on database:
-- select description from pg_shdescription
-- join pg_database on objoid = pg_database.oid
-- where datname = '<database name>'
--
-- to view comments on table:
-- select description from pg_description
-- join pg_class on pg_description.objoid = pg_class.oid
-- where relname = '< table name>'
--
-- or take two steps:
-- select oid from pg_class where relname='<table_name>';
-- select obj_description(table_oid, 'pg_class');
--
-- also work with other db objs,
-- just change pg_class to their catalog name, like 'pg_trigger' for triggers.

-- to view comments on columns:
-- \d+ <table name>
----------------------------------------------------------------------

-- comment on database
COMMENT ON DATABASE petascopedb IS $$store coverage metadata and OWS information$$;

-- comment on ps_dbupdates
COMMENT ON TABLE ps_dbupdates IS $$This table stores latest database update, used by update_petascopedb.sh$$;
COMMENT ON TRIGGER single_dbupdate_trigger ON ps_dbupdates IS $$ensure there is only one row in table ps_dbupdates$$;

---------------------------- COVERAGE MODEL (WCS/WCPS) -----------------------
------------------------------------------------------------------------------
-- ps_keyword
COMMENT ON TABLE ps_keyword IS $$Keywords for the OWS data description (/ows:Description/ows:Keywords)$$;
COMMENT ON COLUMN ps_keyword.value IS $$/ows:Keywords/ows:Keyword$$;
COMMENT ON COLUMN ps_keyword.language IS $$/ows:Keywords/ows:Keyword/@xml:lang$$;

-- ps_keyword_group
COMMENT ON TABLE ps_keyword_group IS $$Keywords for the OWS data description (/ows:Description/ows:Keywords)$$;
COMMENT ON COLUMN ps_keyword_group.keyword_ids IS $$/ows:Keywords/ows:Keyword = string [+ @xml:lang]$$;
COMMENT ON COLUMN ps_keyword_group.type IS $$/ows:Keywords/ows:Type$$;
COMMENT ON COLUMN ps_keyword_group.type_codespace IS $$/ows:Keywords/ows:Type/@codeSpace$$;
COMMENT ON TRIGGER keyword_integrity_trigger ON ps_keyword_group IS $$Check referential integrity on Check referential integrity on ps9_keyword_group$$;

--ps_description
COMMENT ON TABLE ps_description IS $$Table for ows:Description elements, used both in coverage summaries and service identification.$$;
COMMENT ON COLUMN ps_description.titles IS $$/ows:Description/ows:Title$$;
COMMENT ON COLUMN ps_description.abstracts IS $$/ows:Description/ows:Abstract$$;
COMMENT ON COLUMN ps_description.keyword_group_ids IS $$/ows:Description/ows:Keywords$$;
COMMENT ON TRIGGER keyword_group_integrity_trigger ON ps_description IS $$Check referential integrity on ps_keyword_group.$$;

-- ps_gml_subtype
COMMENT ON TABLE ps_gml_subtype IS $$Catalogue table storing the available GML grid types.$$;

-- ps_mime_type
COMMENT ON TABLE ps_mime_type IS $$mimetypes$$;
COMMENT ON TABLE ps_gdal_format IS $$gdal types$$;
COMMENT ON TABLE ps_format IS $$encodings known to WCPS and mappings to mimetypes and gdal_id$$;

-- ps_coverage
COMMENT ON TABLE ps_coverage IS $$core table of Coverage model, all coverages registered here. $$;
COMMENT ON COLUMN ps_coverage.name IS $$coverage name referred in OWS services, not necessarily the same as the associated rasdaman collection name.$$;
COMMENT ON COLUMN ps_coverage.gml_type_id IS $$GMLCOV type, reference ps_gml_subtype.id$$;
COMMENT ON COLUMN ps_coverage.native_format_id IS $$the native format. application/octet-stream is the default for grid coverages ( = bytes from rasdaman db). referrence ps_mime_type.id$$;
COMMENT ON TRIGGER coverage_name_trigger ON ps_coverage IS $$A coverage name must start with a char and must not contain colons: '[\i-[:]][\c-[:]]*' $$;

-- ps_bounding_box
COMMENT ON TABLE ps_bounding_box IS $$stores the mins and maxs for each dimension. Now only meaningful with Multi*coverages, while the BBOX of grid coverages are deduced from domain set.$$;

-- ps_rasdaman_collection
COMMENT ON TABLE ps_rasdaman_collection IS $$collects all the rasdaman collections, which are then referenced in ps_range_set$$;
COMMENT ON COLUMN ps_rasdaman_collection.name IS $$RASBASE::ras_mddcollnames.mddcollname$$;
COMMENT ON COLUMN ps_rasdaman_collection.oid IS $$$$;
COMMENT ON COLUMN ps_rasdaman_collection.base_type IS $$RASBASE::nm_meta.pixel_type$$;

-- ps_range_set
COMMENT ON TABLE ps_range_set IS $$This table links the coverage to the either internal (PostgreSQL) or external (e.g. rasdaman) storage of the range values.$$;
COMMENT ON COLUMN ps_range_set.storage_table IS $$Currently only ps_rasdaman_collection is a legal table (it is also set by default).$$;

-- ps_range_data_type
COMMENT ON TABLE ps_range_data_type IS $$Catalogue for data types of a coverages range [WCPS rangeType() -- OGC 08-068r2 Tab.2].$$;

-- ps_range_type_component
COMMENT ON TABLE ps_range_type_component IS $$Field components (bands) of a coverage.$$;
COMMENT ON COLUMN ps_range_type_component.name IS $$band name$$;
COMMENT ON COLUMN ps_range_type_component.data_type_id IS $$data type of this band$$;
COMMENT ON COLUMN ps_range_type_component.component_order IS $$the order of this band in this coverage$$;
COMMENT ON COLUMN ps_range_type_component.field_id IS $$reference to the SWE field.$$;
COMMENT ON COLUMN ps_range_type_component.field_table IS $$reference(togerther with field_id) to the SWE field. As only continuous SWE quantities are currently supported, the table name defaults to ps_quantity.$$;

-- ps_uom
COMMENT ON TABLE ps_uom IS $$Catalogue table with Unit Of Measures (UoMs) for the rangeSet of a coverage.$$;

-- ps_interval
COMMENT ON TABLE ps_interval IS $$Allowed interval values for 'quantity' and 'count' field types.$$;

-- ps_nil_value
COMMENT ON TABLE ps_nil_value IS $$Catalog of NIL values for a data record; each value is associated with a reason, which shall be expressed via URI.$$;

-- ps_quantity
COMMENT ON TABLE ps_quantity IS $$Catalog of SWE quantities.$$;

-- ps_quantity_interval
COMMENT ON TABLE ps_quantity_interval IS $$ n to m association between ps_quantity and ps_interval: an SWE quantity can be constrained to have multiple allowed intervals, and a same interval can be used to constraint multiple quantities.$$;

-- ps_crs
COMMENT ON TABLE ps_crs IS $$Catalogue table storing CRS URIs.$$;

-- ps_domain_set
COMMENT ON TABLE ps_domain_set IS $$domain set metadata shared by coverages.$$;
COMMENT ON COLUMN ps_domain_set.native_crs_ids IS $$1D array stores the compound CRSs, refer to ps_crs.id$$;

-- ps_gridded_domain_set
COMMENT ON TABLE ps_gridded_domain_set IS $$stores the grid origin of grid coverage.The geometry of each grid axis is spread in the other related tables, i.e. ps_grid_axis, ps_rectilinear_axis and ps_vector_coefficients.$$;
COMMENT ON COLUMN ps_gridded_domain_set.grid_origin IS $$an ordered array of coordinates that must follow the order of axis definition inside the native CRS. $$;

-- ps9_grid_axis
COMMENT ON TABLE ps_grid_axis IS $$this table determines the order of the axis in the grid topology: it must reflect the position of each axis in the rasdaman marray$$;

-- ps_rectilinear_axis
COMMENT ON TABLE ps_rectilinear_axis IS $$the offset vector(the fixed relative geometric distance between grid points along a grid axis) $$;
COMMENT ON COLUMN ps_rectilinear_axis.offset_vector IS $$offset vector is stored as an array of coordinates which must match the order of axes in the native CRS definition$$;

-- ps_vector_coefficients
COMMENT ON TABLE ps_vector_coefficients IS $$the coefficients of each grid point along irregularly-spaced rectilinear axis.$$;
COMMENT ON COLUMN ps_vector_coefficients.grid_axis_id IS $$axis id$$;
COMMENT ON COLUMN ps_vector_coefficients.coefficient IS $$distance of the point along this axis to the origin$$;
COMMENT ON COLUMN ps_vector_coefficients.coefficient_order IS $$order of the points along this axis, start from 0.$$;

-- ps_extra_metadata_type
COMMENT ON TABLE ps_extra_metadata_type IS $$Catalogue table of metadata types for a coverage.$$;

-- ps_extra_metadata
COMMENT ON TABLE ps_extra_metadata IS $$Additional (optional) metadata that can be specified for a coverage.$$;
COMMENT ON COLUMN ps_extra_metadata.metadata_type_id IS $$REFERENCES ps9_extra_metadata_type.id.$$;

-- ps_service_identification
COMMENT ON TABLE ps_service_identification IS $$Metadata for the WCS service provider (/wcs:Capabilities/ows:ServiceProvider)$$;
