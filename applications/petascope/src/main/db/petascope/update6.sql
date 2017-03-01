-- Modify the ps_formats table to contain GDAL identifiers
-- Add gdalid column
ALTER TABLE ps_format ADD COLUMN gdalid varchar(64);

-- Add description column
ALTER TABLE ps_format ADD COLUMN description text;

-- Update existing columns
UPDATE ps_format SET gdalid = 'JPEG', description = 'JPEG JFIF (.jpg)' WHERE name = 'jpeg' OR name = 'jpg';
UPDATE ps_format SET gdalid = 'PNG', description = 'Portable Network Graphics (.png)' WHERE name = 'png';
UPDATE ps_format SET gdalid = 'GTiff', description = 'TIFF / BigTIFF / GeoTIFF (.tif)' WHERE name = 'tif' OR name = 'tiff';

-- Making sure that the auto-incremented id is up to date
SELECT setval('ps_format_id_seq', (SELECT MAX(id) FROM ps_format));

-- Add new entries
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gtiff', 'image/tiff', 'GTiff', 'TIFF / BigTIFF / GeoTIFF (.tif)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('aaigrid', 'image/x-aaigrid', 'AAIGrid', 'Arc/Info ASCII Grid');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ace2', 'application/x-ogc-ace2', 'ACE2', 'ACE2');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('adrg', 'application/x-ogc-adrg', 'ADRG', 'ADRG/ARC Digitilized Raster Graphics (.gen/.thf)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('aig', 'application/x-ogc-aig', 'AIG', 'Arc/Info Binary Grid (.adf)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('airsar', 'application/x-ogc-airsar', 'AIRSAR', 'AIRSAR Polarimetric');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('blx', 'application/x-ogc-blx', 'BLX', 'Magellan BLX Topo (.blx, .xlb)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('bag', 'application/x-ogc-bag', 'BAG', 'Bathymetry Attributed Grid (.bag)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('bmp', 'image/bmp', 'BMP', 'Microsoft Windows Device Independent Bitmap (.bmp)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('bsb', 'application/x-ogc-bsb', 'BSB', 'BSB Nautical Chart Format (.kap)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('bt', 'application/x-ogc-bt', 'BT', 'VTP Binary Terrain Format (.bt)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ceos', 'application/x-ogc-ceos', 'CEOS', 'CEOS (Spot for instance)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('coasp', 'application/x-ogc-coasp', 'COASP', 'DRDC COASP SAR Processor Raster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('cosar', 'application/x-ogc-cosar', 'COSAR', 'TerraSAR-X Complex SAR Data Product');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('cpg', 'application/x-ogc-cpg', 'CPG', 'Convair PolGASP data');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ctg', 'application/x-ogc-ctg', 'CTG', 'USGS LULC Composite Theme Grid');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('dimap', 'application/x-ogc-dimap', 'DIMAP', 'Spot DIMAP (metadata.dim)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('dipex', 'application/x-ogc-dipex', 'DIPEx', 'ELAS DIPEx');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('dods', 'application/x-ogc-dods', 'DODS', 'DODS / OPeNDAP');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('doq1', 'application/x-ogc-doq1', 'DOQ1', 'First Generation USGS DOQ (.doq)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('doq2', 'application/x-ogc-doq2', 'DOQ2', 'New Labelled USGS DOQ (.doq)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('dted', 'application/x-ogc-dted', 'DTED', 'Military Elevation Data (.dt0, .dt1, .dt2)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('e00grid', 'application/x-ogc-e00grid', 'E00GRID', 'Arc/Info Export E00 GRID');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ecrgtoc', 'application/x-ogc-ecrgtoc', 'ECRGTOC', 'ECRG Table Of Contents (TOC.xml)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ecw', 'image/x-imagewebserver-ecw', 'ECW', 'ERDAS Compressed Wavelets (.ecw)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ehdr', 'application/x-ogc-ehdr', 'EHdr', 'ESRI .hdr Labelled');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('eir', 'application/x-ogc-eir', 'EIR', 'Erdas Imagine Raw');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('elas', 'application/x-ogc-elas', 'ELAS', 'NASA ELAS');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('envi', 'application/x-ogc-envi', 'ENVI', 'ENVI .hdr Labelled Raster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('epsilon', 'application/x-ogc-epsilon', 'EPSILON', 'Epsilon - Wavelet compressed images');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ers', 'application/x-ogc-ers', 'ERS', 'ERMapper (.ers)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('esat', 'application/x-ogc-esat', 'ESAT', 'Envisat Image Product (.n1)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('fast', 'application/x-ogc-fast', 'FAST', 'EOSAT FAST Format');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('fit', 'application/x-ogc-fit', 'FIT', 'FIT');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('fits', 'application/x-ogc-fits', 'FITS', 'FITS (.fits)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('fujibas', 'application/x-ogc-fujibas', 'FujiBAS', 'Fuji BAS Scanner Image');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('genbin', 'application/x-ogc-genbin', 'GENBIN', 'Generic Binary (.hdr Labelled)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('georaster', 'application/x-ogc-georaster', 'GEORASTER', 'Oracle Spatial GeoRaster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gff', 'application/x-ogc-gff', 'GFF', 'GSat File Format');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gif', 'image/gif', 'GIF', 'Graphics Interchange Format (.gif)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('grib', 'application/x-ogc-grib', 'GRIB', 'WMO GRIB1/GRIB2 (.grb)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gmt', 'application/x-netcdf-gmt', 'GMT', 'GMT Compatible netCDF');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('grass', 'application/x-ogc-grass', 'GRASS', 'GRASS Rasters');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('grassasciigrid', 'application/x-ogc-grass_asciigrid', 'GRASSASCIIGrid', 'GRASS ASCII Grid');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gsag', 'application/x-ogc-gsag', 'GSAG', 'Golden Software ASCII Grid');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gsbg', 'application/x-ogc-gsbg', 'GSBG', 'Golden Software Binary Grid');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gs7bg', 'application/x-ogc-gs7bg', 'GS7BG', 'Golden Software Surfer 7 Binary Grid');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gsc', 'application/x-ogc-gsc', 'GSC', 'GSC Geogrid');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gta', 'application/x-ogc-gta', 'GTA', 'Generic Tagged Arrays (.gta)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gtx', 'image/x-gtx', 'GTX', 'NOAA .gtx vertical datum shift');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('gxf', 'application/x-ogc-gxf', 'GXF', 'GXF - Grid eXchange File');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('hdf4', 'application/x-hdf4', 'HDF4', 'Hierarchical Data Format Release 4 (HDF4)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('hdf5', 'application/x-hdf5', 'HDF5', 'Hierarchical Data Format Release 5 (HDF5)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('hf2', 'application/x-ogc-hf2', 'HF2', 'HF2/HFZ heightfield raster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('hfa', 'application/x-erdas-hfa', 'HFA', 'Erdas Imagine (.img)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ida', 'application/x-ogc-ida', 'IDA', 'Image Display and Analysis (WinDisp)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ilwis', 'application/x-ogc-ilwis', 'ILWIS', 'ILWIS Raster Map (.mpr,.mpl)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ingr', 'application/x-ogc-ingr', 'INGR', 'Intergraph Raster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('isis2', 'application/x-ogc-isis2', 'ISIS2', 'USGS Astrogeology ISIS cube (Version 2)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('isis3', 'application/x-ogc-isis3', 'ISIS3', 'USGS Astrogeology ISIS cube (Version 3)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jaxapalsar', 'application/x-ogc-jaxapalsar', 'JAXAPALSAR', 'JAXA PALSAR Product Reader (Level 1.1/1.5)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jdem', 'application/x-ogc-jdem', 'JDEM', 'Japanese DEM (.mem)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jpegls', 'image/jpeg', 'JPEGLS', 'JPEG-LS');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jpeg2000', 'image/jp2', 'JPEG2000', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jp2ecw', 'image/jp2', 'JP2ECW', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jp2kak', 'image/jp2', 'JP2KAK', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jp2mrsid', 'image/jp2', 'JP2MrSID', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jp2openjpeg', 'image/jp2', 'JP2OpenJPEG', 'JPEG2000 (.jp2, .j2k)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('jpipkak', 'image/jpip-stream', 'JPIPKAK', 'JPIP (based on Kakadu)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('kmlsuperoverlay', 'application/x-ogc-kmlsuperoverlay', 'KMLSUPEROVERLAY', 'KMLSUPEROVERLAY');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('l1b', 'application/x-ogc-l1b', 'L1B', 'NOAA Polar Orbiter Level 1b Data Set (AVHRR)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('lan', 'application/x-erdas-lan', 'LAN', 'Erdas 7.x .LAN and .GIS');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('lcp', 'application/x-ogc-lcp', 'LCP', 'FARSITE v.4 LCP Format');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('leveller', 'application/x-ogc-leveller', 'Leveller', 'Daylon Leveller Heightfield');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('loslas', 'application/x-ogc-loslas', 'LOSLAS', 'NADCON .los/.las Datum Grid Shift');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('mbtiles', 'application/x-ogc-mbtiles', 'MBTiles', 'MBTiles');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('mem', 'application/x-ogc-mem', 'MEM', 'In Memory Raster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('mff', 'application/x-ogc-mff', 'MFF', 'Vexcel MFF');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('mff2', 'application/x-ogc-mff2', 'MFF2 (HKV)', 'Vexcel MFF2');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('mg4lidar', 'application/x-ogc-mg4lidar', 'MG4Lidar', 'MG4 Encoded Lidar');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('mrsid', 'image/x-mrsid', 'MrSID', 'Multi-resolution Seamless Image Database');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('msg', 'application/x-ogc-msg', 'MSG', 'Meteosat Second Generation');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('msgn', 'application/x-ogc-msgn', 'MSGN', 'EUMETSAT Archive native (.nat)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ndf', 'application/x-ogc-ndf', 'NDF', 'NLAPS Data Format');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ngsgeoid', 'application/x-ogc-ngsgeoid', 'NGSGEOID', 'NOAA NGS Geoid Height Grids');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('nitf', 'application/x-ogc-nitf', 'NITF', 'National Imagery Transmission Format (.ntf, .nsf, .gn?, .hr?, .ja?, .jg?, .jn?, .lf?, .on?, .tl?, .tp?, etc.)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('netcdf', 'application/netcdf ', 'netCDF', 'NetCDF');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ntv2', 'application/x-ogc-ntv2', 'NTv2', 'NTv2 Datum Grid Shift');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('nwt_grc', 'application/x-ogc-nwt_grc', 'NWT_GRC', 'Northwood/VerticalMapper Classified Grid Format .grc/.tab');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('nwt_grd', 'application/x-ogc-nwt_grd', 'NWT_GRD', 'Northwood/VerticalMapper Numeric Grid Format .grd/.tab');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ogdi', 'application/x-ogc-ogdi', 'OGDI', 'OGDI Bridge');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('ozi', 'application/x-ogc-ozi', 'OZI', 'OZI OZF2/OZFX3');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('paux', 'application/x-ogc-paux', 'PAux', 'PCI .aux Labelled');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('pcidsk', 'application/x-ogc-pcidsk', 'PCIDSK', 'PCI Geomatics Database File');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('pcraster', 'application/x-ogc-pcraster', 'PCRaster', 'PCRaster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('pdf', 'application/x-ogc-pdf', 'PDF', 'Geospatial PDF');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('pds', 'application/x-ogc-pds', 'PDS', 'NASA Planetary Data System');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('postgisraster', 'application/x-ogc-postgisraster', 'PostGISRaster', 'PostGIS Raster (previously WKTRaster)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('pnm', 'application/x-ogc-pnm', 'PNM', 'Netpbm (.ppm,.pgm)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('r', 'text/x-r', 'R', 'R Object Data Store');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('rasdaman', 'application/x-ogc-rasdaman', 'RASDAMAN', 'Rasdaman');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('rasterlite', 'application/x-ogc-rasterlite', 'Rasterlite', 'Rasterlite - Rasters in SQLite DB');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('rik', 'application/x-ogc-rik', 'RIK', 'Swedish Grid RIK (.rik)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('rmf', 'application/x-ogc-rmf', 'RMF', 'Raster Matrix Format (*.rsw, .mtw)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('rpftoc', 'application/x-ogc-rpftoc', 'RPFTOC', 'Raster Product Format/RPF (CADRG, CIB)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('rs2', 'application/x-ogc-rs2', 'RS2', 'RadarSat2 XML (product.xml)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('rst', 'application/x-ogc-rst', 'RST', 'Idrisi Raster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('saga', 'application/x-ogc-saga', 'SAGA', 'SAGA GIS Binary format');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('sar_ceos', 'application/x-ogc-sar_ceos', 'SAR_CEOS', 'SAR CEOS');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('sde', 'application/x-ogc-sde', 'SDE', 'ArcSDE Raster');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('sdts', 'application/x-ogc-sdts', 'SDTS', 'USGS SDTS DEM (*CATD.DDF)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('sgi', 'image/x-sgi', 'SGI', 'SGI Image Format');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('snodas', 'application/x-ogc-snodas', 'SNODAS', 'Snow Data Assimilation System');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('srp', 'application/x-ogc-srp', 'SRP', 'Standard Raster Product (ASRP/USRP)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('srtmhgt', 'application/x-ogc-srtmhgt', 'SRTMHGT', 'SRTM HGT Format');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('terragen', 'application/x-ogc-terragen', 'TERRAGEN', 'Terragen Heightfield (.ter)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('til', 'application/x-ogc-til', 'TIL', 'EarthWatch/DigitalGlobe .TIL');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('tsx', 'application/x-ogc-tsx', 'TSX', 'TerraSAR-X Product');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('usgsdem', 'application/x-ogc-usgsdem', 'USGSDEM', 'USGS ASCII DEM / CDED (.dem)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('vrt', 'application/x-ogc-vrt', 'VRT', 'GDAL Virtual Raster (.vrt)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('wcs', 'application/x-ogc-wcs', 'WCS', 'OGC Web Coverage Service');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('webp', 'application/x-ogc-webp', 'WEBP', 'WEBP');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('wms', 'application/x-ogc-wms', 'WMS', 'OGC Web Map Service');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('xpm', 'image/xpm', 'XPM', 'X11 Pixmap (.xpm)');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('xyz', 'application/x-ogc-xyz', 'XYZ', 'ASCII Gridded XYZ');
INSERT INTO ps_format(name,mimetype,gdalid,description) VALUES ('zmap', 'application/x-ogc-zmap', 'ZMap', 'ZMap Plus Grid');

-- Add a table for metadata
CREATE TABLE ps_metadata (
  id serial NOT NULL,
  coverage int NOT NULL,
  metadata text,
	primary key (id),
	foreign key (coverage) references ps_coverage (id) on delete cascade
);
