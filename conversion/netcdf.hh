/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/**
 * INCLUDE: netcdf.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_NETCDF
 *
 * COMMENTS:
 *
 * Provides functions to convert data to NETCDF and back.
 *
 * Limitations:
 *  1. Dimension data is not preserved when importing netcdf, so when exporting
 *     it back to netcdf the dimension is written as a series 1..dimSize
 *  2. Metadata is not preserved
 */

#ifndef _R_CONV_NETCDF_HH_
#define _R_CONV_NETCDF_HH_

#include "conversion/convertor.hh"
#include "conversion/netcdf_cf_constants.h"
#include "config.h"

#ifdef HAVE_NETCDF
#include <netcdf.h>
#endif
#ifdef HAVE_GDAL
#include <gdal/ogr_srs_api.h>
#endif

#include <json/json.h>
#include <string>
#include <vector>
#include <memory>

//@ManMemo: Module {\bf conversion}

/*@Doc:
 NETCDF convertor class.

  No compression method is supported yet

 */
class r_Conv_NETCDF : public r_Convert_Memory
{
public:
    /// constructor using an r_Type object. Exception if the type isn't atomic.
    r_Conv_NETCDF(const char *src, const r_Minterval &interv, const r_Type *tp);
    /// constructor using convert_type_e shortcut
    r_Conv_NETCDF(const char *src, const r_Minterval &interv, int tp);
    /// destructor
    ~r_Conv_NETCDF(void);

    /// convert to NETCDF
    virtual r_Conv_Desc &convertTo(const char *options = NULL,
                                   const r_Range *nullValue = NULL);
    /// convert from NETCDF
    virtual r_Conv_Desc &convertFrom(const char *options = NULL);
    /// convert data in a specific format to array
    virtual r_Conv_Desc &convertFrom(r_Format_Params options);

    /// cloning
    virtual r_Convertor *clone(void) const;

    /// identification
    virtual const char *get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;

private:
#ifdef HAVE_NETCDF
    struct RasType
    {
        RasType(unsigned int cellSizeArg, std::string cellTypeArg, convert_type_e ct)
            : cellType(std::move(cellTypeArg)), cellSize(cellSizeArg), convertType{ct} {}

        std::string cellType;
        unsigned int cellSize;
        convert_type_e convertType;
    };

    /**
     * Read data from tmpFile into desc.dest and return the file size.
     */
    void parseDecodeOptions(const std::string &options);

    void validateDecodeOptions();

    void parseEncodeOptions(const std::string &options);

    void validateJsonEncodeOptions();

    /**
     * read single variable data
     */
    void readDimSizes();

    /**
     * read variable data into a struct
     */
    void readVars();

    /**
     * read variable data
     *
     * @param bandOffset offset bytes at the current variable.
     */
    template <class T>
    void readVarData(int var, size_t cellSize, size_t &bandOffset, bool isStruct);

    /**
     * Build struct type
     */
    size_t buildCellType();

    /**
     * Get a rasdaman type from a netcdf variable type.
     */
    RasType getRasType(int var);

    /**
     * If the format parameters contain a geoReference, e.g.
     * 
     *     "geoReference":{"crs":"EPSG:4326","bbox":{"xmin":111.975,
     *     "ymin":-44.474999999999987,"xmax":156.475,"ymax":-8.974999999999987}}
     * 
     * then this method will try to add a `crs` variable in the netcdf output:
     * 
     *      char crs ;
            crs:grid_mapping_name = "latitude_longitude" ;
            crs:longitude_of_prime_meridian = 0. ;
            crs:semi_major_axis = 6378137. ;
            crs:inverse_flattening = 298.257223563 ;
            crs:spatial_ref = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[...
            crs:GeoTransform = "111.975 0.5 0 -8.974999999999987 0 -0.5 " ;
     * 
     */
    void addCrsVariable();

    /**
     * write single variable data
     */
    void writeSingleVar(const std::vector<int> &dims);

    /**
     * write multiple variables from a struct
     */
    void writeMultipleVars(const std::vector<int> &dims);

    /**
     * write extra metadata (specified by json parameters)
     */
    void addMetadata();

    /**
     * add metadata attributes to var if not null, otherwise to dataFile.
     */
    void addJsonAttributes(const Json::Value &metadata, int var = NC_GLOBAL);

    /// add valid_min, valid_max, and _FillValue; return true if _FillValue was
    /// set, and set it in the outFillValue
    template <class T>
    void addVarAttributes(int var, nc_type nctype, T validMin, T validMax,
                          size_t dimNum);

    // return true if attribute att exists, false otherwise
    bool attExists(int var, const char *att) const;

    /**
     * Convert type to a nc_type; returns ncNoType in case of invalid type.
     */
    nc_type stringToNcType(std::string type);

    /**
     * Add json array values to a netCDF variable.
     */
    void jsonArrayToNcVar(int var, int dimid, Json::Value jsonArray);

    template <class T>
    void writeData(const std::string &varName, const std::vector<int> &dims,
                   const char *src, nc_type nctype,
                   T validMin, T validMax, size_t dimNum = 0);

    /**
     * write struct variable data
     *
     * @param bandOffset offset bytes in the rasdaman struct at the current variable.
     */
    template <class T>
    void writeDataStruct(const std::string &varName,
                         const std::vector<int> &dims,
                         size_t structSize, size_t bandOffset, nc_type nctype,
                         T validMin, T validMax, size_t dimNum = 0);

    /**
     * @return dimension name given it's index
     */
    std::string getDimName(unsigned int dimId);

    /**
     * @return single variable name for exporting to netcdf
     */
    const std::string &getVariableName();

    /**
     * @return the variable object corresponding to variable name
     */
    Json::Value getVariableObject(const std::string &varName) const;

    /// close the netCDF dataFile
    void closeDataFile();

    Json::Value encodeOptions;

    /// variable names
    std::vector<std::string> varNames;
    /// dimension names
    std::vector<std::string> dimNames;
    /// dimension variables
    std::vector<std::string> dimVarNames;
    /// non-data variables
    std::vector<std::string> nondataVarNames;
    /// length of each dimension
    std::vector<size_t> dimSizes;
    /// offset at each dimension (subset read/write)
    std::vector<size_t> dimOffsets;
    /// number of dimensions
    size_t numDims;
    /// cell count
    size_t dataSize;
    /// ID of the crs variable was added to the output with addCrsVariable()
    int crsVarId{-1};
    /// name of the crs variable was added to the output with addCrsVariable()
    std::string crsVarName;
    /// ID of the opened netcdf file
    int dataFile{invalidDataFile};

    static const int invalidDataFile;
    static const std::string DEFAULT_VAR;
    static const std::string DEFAULT_DIM_NAME_PREFIX;
    static const std::string VAR_SEPARATOR_STR;
    static const std::string VARS_KEY;
    static const std::string VALID_MIN;
    static const std::string VALID_MAX;
    static const std::string MISSING_VALUE;
    static const std::string FILL_VALUE;
    static const std::string GRID_MAPPING;

#endif
};

// TODO put in a separate header file
#ifdef HAVE_GDAL

#define NCDF_CRS_WKT "crs_wkt"
#define ROTATED_POLE_VAR_NAME "rotated_pole"

/* Following are a series of mappings from CF-1 convention parameters
 * for each projection, to the equivalent in OGC WKT used internally by GDAL.
 * See: http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.5/apf.html
 */

/* A struct allowing us to map between GDAL(OGC WKT) and CF-1 attributes */
typedef struct
{
    const char *CF_ATT;
    const char *WKT_ATT;
    // TODO: mappings may need default values, like scale factor?
    // double defval;
} oNetcdfSRS_PP;

// default mappings, for the generic case
/* These 'generic' mappings are based on what was previously in the
   poNetCDFSRS struct. They will be used as a fallback in case none
   of the others match (i.e. you are exporting a projection that has
   no CF-1 equivalent).
   They are not used for known CF-1 projections since there is not a
   unique 2-way projection-independent
   mapping between OGC WKT params and CF-1 ones: it varies per-projection.
*/

static const oNetcdfSRS_PP poGenericMappings[] = {
    /* scale_factor is handled as a special case, write 2 values */
    {CF_PP_STD_PARALLEL_1, SRS_PP_STANDARD_PARALLEL_1},
    {CF_PP_STD_PARALLEL_2, SRS_PP_STANDARD_PARALLEL_2},
    {CF_PP_LONG_CENTRAL_MERIDIAN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_LONG_CENTRAL_MERIDIAN, SRS_PP_LONGITUDE_OF_CENTER},
    {CF_PP_LON_PROJ_ORIGIN, SRS_PP_LONGITUDE_OF_ORIGIN},
    // Multiple mappings to LAT_PROJ_ORIGIN
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_ORIGIN},
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_CENTER},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr},
};

// Albers equal area
//
// grid_mapping_name = albers_conical_equal_area
// WKT: Albers_Conic_Equal_Area
// EPSG:9822
//
// Map parameters:
//
//    * standard_parallel - There may be 1 or 2 values.
//    * longitude_of_central_meridian
//    * latitude_of_projection_origin
//    * false_easting
//    * false_northing
//
static const oNetcdfSRS_PP poAEAMappings[] = {
    {CF_PP_STD_PARALLEL_1, SRS_PP_STANDARD_PARALLEL_1},
    {CF_PP_STD_PARALLEL_2, SRS_PP_STANDARD_PARALLEL_2},
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_CENTER},
    {CF_PP_LONG_CENTRAL_MERIDIAN, SRS_PP_LONGITUDE_OF_CENTER},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Azimuthal equidistant
//
// grid_mapping_name = azimuthal_equidistant
// WKT: Azimuthal_Equidistant
//
// Map parameters:
//
//    * longitude_of_projection_origin
//    * latitude_of_projection_origin
//    * false_easting
//    * false_northing
//
static const oNetcdfSRS_PP poAEMappings[] = {
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_CENTER},
    {CF_PP_LON_PROJ_ORIGIN, SRS_PP_LONGITUDE_OF_CENTER},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Lambert azimuthal equal area
//
// grid_mapping_name = lambert_azimuthal_equal_area
// WKT: Lambert_Azimuthal_Equal_Area
//
// Map parameters:
//
//    * longitude_of_projection_origin
//    * latitude_of_projection_origin
//    * false_easting
//    * false_northing
//
static const oNetcdfSRS_PP poLAEAMappings[] = {
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_CENTER},
    {CF_PP_LON_PROJ_ORIGIN, SRS_PP_LONGITUDE_OF_CENTER},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Lambert conformal
//
// grid_mapping_name = lambert_conformal_conic
// WKT: Lambert_Conformal_Conic_1SP / Lambert_Conformal_Conic_2SP
//
// Map parameters:
//
//    * standard_parallel - There may be 1 or 2 values.
//    * longitude_of_central_meridian
//    * latitude_of_projection_origin
//    * false_easting
//    * false_northing
//
// See
// http://www.remotesensing.org/geotiff/proj_list/lambert_conic_conformal_1sp.html

// Lambert conformal conic - 1SP
/* See bug # 3324
   It seems that the missing scale factor can be computed from
   standard_parallel1 and latitude_of_projection_origin. If both are equal (the
   common case) then scale factor=1, else use Snyder eq. 15-4. We save in the
   WKT standard_parallel1 for export to CF, but do not export scale factor. If a
   WKT has a scale factor != 1 and no standard_parallel1 then export is not CF,
   but we output scale factor for compat. is there a formula for that?
*/
static const oNetcdfSRS_PP poLCC1SPMappings[] = {
    {CF_PP_STD_PARALLEL_1, SRS_PP_STANDARD_PARALLEL_1},
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_ORIGIN},
    {CF_PP_LONG_CENTRAL_MERIDIAN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_SCALE_FACTOR_ORIGIN, SRS_PP_SCALE_FACTOR}, /* special case */
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Lambert conformal conic - 2SP
static const oNetcdfSRS_PP poLCC2SPMappings[] = {
    {CF_PP_STD_PARALLEL_1, SRS_PP_STANDARD_PARALLEL_1},
    {CF_PP_STD_PARALLEL_2, SRS_PP_STANDARD_PARALLEL_2},
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_ORIGIN},
    {CF_PP_LONG_CENTRAL_MERIDIAN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Lambert cylindrical equal area
//
// grid_mapping_name = lambert_cylindrical_equal_area
// WKT: Cylindrical_Equal_Area
// EPSG:9834 (Spherical) and EPSG:9835
//
// Map parameters:
//
//    * longitude_of_central_meridian
//    * either standard_parallel or scale_factor_at_projection_origin
//    * false_easting
//    * false_northing
//
// NB: CF-1 specifies a 'scale_factor_at_projection' alternative
//  to std_parallel ... but no reference to this in EPSG/remotesensing.org
//  ignore for now.
//
static const oNetcdfSRS_PP poLCEAMappings[] = {
    {CF_PP_STD_PARALLEL_1, SRS_PP_STANDARD_PARALLEL_1},
    {CF_PP_LONG_CENTRAL_MERIDIAN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Latitude-Longitude
//
// grid_mapping_name = latitude_longitude
//
// Map parameters:
//
//    * None
//
// NB: handled as a special case - !isProjected()

// Mercator
//
// grid_mapping_name = mercator
// WKT: Mercator_1SP / Mercator_2SP
//
// Map parameters:
//
//    * longitude_of_projection_origin
//    * either standard_parallel or scale_factor_at_projection_origin
//    * false_easting
//    * false_northing

// Mercator 1 Standard Parallel (EPSG:9804)
static const oNetcdfSRS_PP poM1SPMappings[] = {
    {CF_PP_LON_PROJ_ORIGIN, SRS_PP_CENTRAL_MERIDIAN},
    // LAT_PROJ_ORIGIN is always equator (0) in CF-1
    {CF_PP_SCALE_FACTOR_ORIGIN, SRS_PP_SCALE_FACTOR},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Mercator 2 Standard Parallel
static const oNetcdfSRS_PP poM2SPMappings[] = {
    {CF_PP_LON_PROJ_ORIGIN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_STD_PARALLEL_1, SRS_PP_STANDARD_PARALLEL_1},
    // From best understanding of this projection, only
    // actually specify one SP - it is the same N/S of equator.
    // {CF_PP_STD_PARALLEL_2, SRS_PP_LATITUDE_OF_ORIGIN},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Orthographic
// grid_mapping_name = orthographic
// WKT: Orthographic
//
// Map parameters:
//
//    * longitude_of_projection_origin
//    * latitude_of_projection_origin
//    * false_easting
//    * false_northing
//
static const oNetcdfSRS_PP poOrthoMappings[] = {
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_ORIGIN},
    {CF_PP_LON_PROJ_ORIGIN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Polar stereographic
//
// grid_mapping_name = polar_stereographic
// WKT: Polar_Stereographic
//
// Map parameters:
//
//    * straight_vertical_longitude_from_pole
//    * latitude_of_projection_origin - Either +90. or -90.
//    * Either standard_parallel or scale_factor_at_projection_origin
//    * false_easting
//    * false_northing

static const oNetcdfSRS_PP poPSmappings[] = {
    /* {CF_PP_STD_PARALLEL_1, SRS_PP_LATITUDE_OF_ORIGIN}, */
    /* {CF_PP_SCALE_FACTOR_ORIGIN, SRS_PP_SCALE_FACTOR},   */
    {CF_PP_VERT_LONG_FROM_POLE, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Rotated Pole
//
// grid_mapping_name = rotated_latitude_longitude
// WKT: N/A
//
// Map parameters:
//
//    * grid_north_pole_latitude
//    * grid_north_pole_longitude
//    * north_pole_grid_longitude - This parameter is optional (default is 0.).

// No WKT equivalent

// Stereographic
//
// grid_mapping_name = stereographic
// WKT: Stereographic (and/or Oblique_Stereographic??)
//
// Map parameters:
//
//    * longitude_of_projection_origin
//    * latitude_of_projection_origin
//    * scale_factor_at_projection_origin
//    * false_easting
//    * false_northing
//
// NB: see bug#4267 Stereographic vs. Oblique_Stereographic
//
static const oNetcdfSRS_PP poStMappings[] = {
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_ORIGIN},
    {CF_PP_LON_PROJ_ORIGIN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_SCALE_FACTOR_ORIGIN, SRS_PP_SCALE_FACTOR},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Transverse Mercator
//
// grid_mapping_name = transverse_mercator
// WKT: Transverse_Mercator
//
// Map parameters:
//
//    * scale_factor_at_central_meridian
//    * longitude_of_central_meridian
//    * latitude_of_projection_origin
//    * false_easting
//    * false_northing
//
static const oNetcdfSRS_PP poTMMappings[] = {
    {CF_PP_SCALE_FACTOR_MERIDIAN, SRS_PP_SCALE_FACTOR},
    {CF_PP_LONG_CENTRAL_MERIDIAN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_LAT_PROJ_ORIGIN, SRS_PP_LATITUDE_OF_ORIGIN},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    {nullptr, nullptr}};

// Vertical perspective
//
// grid_mapping_name = vertical_perspective
// WKT: ???
//
// Map parameters:
//
//    * latitude_of_projection_origin
//    * longitude_of_projection_origin
//    * perspective_point_height
//    * false_easting
//    * false_northing
//
// TODO: see how to map this to OGR

static const oNetcdfSRS_PP poGEOSMappings[] = {
    {CF_PP_LON_PROJ_ORIGIN, SRS_PP_CENTRAL_MERIDIAN},
    {CF_PP_PERSPECTIVE_POINT_HEIGHT, SRS_PP_SATELLITE_HEIGHT},
    {CF_PP_FALSE_EASTING, SRS_PP_FALSE_EASTING},
    {CF_PP_FALSE_NORTHING, SRS_PP_FALSE_NORTHING},
    /* { CF_PP_SWEEP_ANGLE_AXIS, .... } handled as a proj.4 extension */
    {nullptr, nullptr}};

/* Mappings for various projections, including netcdf and GDAL projection names
   and corresponding oNetcdfSRS_PP mapping struct.
   A NULL mappings value means that the projection is not included in the CF
   standard and the generic mapping (poGenericMappings) will be used. */
typedef struct
{
    const char *CF_SRS;
    const char *WKT_SRS;
    const oNetcdfSRS_PP *mappings;
} oNetcdfSRS_PT;

static const oNetcdfSRS_PT poNetcdfSRS_PT[] = {
    {CF_PT_AEA, SRS_PT_ALBERS_CONIC_EQUAL_AREA, poAEAMappings},
    {CF_PT_AE, SRS_PT_AZIMUTHAL_EQUIDISTANT, poAEMappings},
    {"cassini_soldner", SRS_PT_CASSINI_SOLDNER, nullptr},
    {CF_PT_LCEA, SRS_PT_CYLINDRICAL_EQUAL_AREA, poLCEAMappings},
    {"eckert_iv", SRS_PT_ECKERT_IV, nullptr},
    {"eckert_vi", SRS_PT_ECKERT_VI, nullptr},
    {"equidistant_conic", SRS_PT_EQUIDISTANT_CONIC, nullptr},
    {"equirectangular", SRS_PT_EQUIRECTANGULAR, nullptr},
    {"gall_stereographic", SRS_PT_GALL_STEREOGRAPHIC, nullptr},
    {CF_PT_GEOS, SRS_PT_GEOSTATIONARY_SATELLITE, poGEOSMappings},
    {"goode_homolosine", SRS_PT_GOODE_HOMOLOSINE, nullptr},
    {"gnomonic", SRS_PT_GNOMONIC, nullptr},
    {"hotine_oblique_mercator", SRS_PT_HOTINE_OBLIQUE_MERCATOR, nullptr},
    {"hotine_oblique_mercator_2P",
     SRS_PT_HOTINE_OBLIQUE_MERCATOR_TWO_POINT_NATURAL_ORIGIN, nullptr},
    {"laborde_oblique_mercator", SRS_PT_LABORDE_OBLIQUE_MERCATOR, nullptr},
    {CF_PT_LCC, SRS_PT_LAMBERT_CONFORMAL_CONIC_1SP, poLCC1SPMappings},
    {CF_PT_LCC, SRS_PT_LAMBERT_CONFORMAL_CONIC_2SP, poLCC2SPMappings},
    {CF_PT_LAEA, SRS_PT_LAMBERT_AZIMUTHAL_EQUAL_AREA, poLAEAMappings},
    {CF_PT_MERCATOR, SRS_PT_MERCATOR_1SP, poM1SPMappings},
    {CF_PT_MERCATOR, SRS_PT_MERCATOR_2SP, poM2SPMappings},
    {"miller_cylindrical", SRS_PT_MILLER_CYLINDRICAL, nullptr},
    {"mollweide", SRS_PT_MOLLWEIDE, nullptr},
    {"new_zealand_map_grid", SRS_PT_NEW_ZEALAND_MAP_GRID, nullptr},
    /* for now map to STEREO, see bug #4267 */
    {"oblique_stereographic", SRS_PT_OBLIQUE_STEREOGRAPHIC, nullptr},
    /* {STEREO, SRS_PT_OBLIQUE_STEREOGRAPHIC, poStMappings },  */
    {CF_PT_ORTHOGRAPHIC, SRS_PT_ORTHOGRAPHIC, poOrthoMappings},
    {CF_PT_POLAR_STEREO, SRS_PT_POLAR_STEREOGRAPHIC, poPSmappings},
    {"polyconic", SRS_PT_POLYCONIC, nullptr},
    {"robinson", SRS_PT_ROBINSON, nullptr},
    {"sinusoidal", SRS_PT_SINUSOIDAL, nullptr},
    {CF_PT_STEREO, SRS_PT_STEREOGRAPHIC, poStMappings},
    {"swiss_oblique_cylindrical", SRS_PT_SWISS_OBLIQUE_CYLINDRICAL, nullptr},
    {CF_PT_TM, SRS_PT_TRANSVERSE_MERCATOR, poTMMappings},
    {"TM_south_oriented", SRS_PT_TRANSVERSE_MERCATOR_SOUTH_ORIENTED, nullptr},
    {nullptr, nullptr, nullptr},
};

#endif  // HAVE_GDAL

#endif
