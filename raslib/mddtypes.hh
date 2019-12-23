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
 * INCLUDE: mddtypes.hh
 *
 * MODULE:  raslib
 *
 * PURPOSE:
 *      The file cotains MDD type definitions.
 *
 * COMMENTS:
 * - always append new data formats to remain compatible with earlier compiled code
 *
*/

#ifndef _D_MDDTYPES_
#define _D_MDDTYPES_

#include <ostream>
#include <cstdint>

/// number of bytes in an tile or mdd or type.
using r_Bytes = size_t;

/// for conversion from pointers to integer variables on 64bit arch.
using r_Ptr = unsigned long;

/// number of cells in an mdd object or tile.
using r_Area = std::uint64_t;

/// for axis indexing, e.g. lower/upper bounds of r_Sinterval, projection value
/// and coordinate values of r_Point.
using r_Range = long long;

/// number of dimensions in r_Point and r_Minterval.
using r_Dimension = unsigned int;

/**
  \begin{tabular}{ll}
  {\tt r_Array}              && no compression, row-major memory representation\\

  {\tt r_TIFF}               && TIFF format (see r_Conv_TIFF)\\
  {\tt r_JPEG}               && JPEG format (see r_Conv_JPEG)\\
  {\tt r_JP2}                && JPEG2000 format (see r_Conv_JP2)\\
  {\tt r_HDF}                && HDF  format (see r_Conv_HDF)\\
  {\tt r_PNG}                && PNG  format (see r_Conv_PNG)\\
  {\tt r_BMP}                && BMP  format (see r_Conv_BMP)\\
  {\tt r_PPM}                && PPM  format (see r_Conv_PPM)\\
  {\tt r_DEM}                && DEM  format (see r_Conv_DEM)\\
  {\tt r_ECW}                && ECW  format (see r_Conv_ECW)\\
  {\tt r_NITF}               && NITF  format (see r_Conv_NITF)\\
  {\tt r_NETCDF}             && NETCDF  format (see r_Conv_NETCDF)\\
  {\tt r_GRIB}               && GRIB  format (see r_Conv_GRIB)\\
  {\tt r_GDAL}               && GDAL supported format (see r_Conv_GDAL)\\

  {\tt r_Auto_Compression}   && automatic compression\\
  {\tt r_ZLib}               && ZLIB compresion  (see r_Tile_Comp_RLE)\\
  {\tt r_Pack_Bits}          && Packbits rle compresion  (see r_Tile_Comp_Packbits)\\
  {\tt r RLE}                && RLE compression  (see r_Tile_Comp_RLE)\\
  {\tt r_Wavelet_Haar}       && Haar Wavelet compression  (see r_Haar_Wavelet_Compression)\\
  {\tt r_Wavelet_Daubechies} && Daubechies 4-tap Wavelet compression  (see r_Daubechies_Wavelet_Compression)\\
  {\tt r_Sep_ZLib}           && ZLIB compression, compress base types separately  (see r_Tile_Separate_ZLIB)\\
  {\tt r_Sep_RLE}            && RLE compression, compress base types separately  (see r_Tile_Separate_RLE)\\
  {\tt r_Wavelet_Daub#n}     && Daubechies n-tap Wavelet compression, n=6,8,...,18,20  (see r_Ortho_Wavelet_Factory)\\
  {\tt r_Wavelet_Least#n}    && Least asymmetric n-tap Wavelet comp., n=8,10,...,18,20  (see r_Ortho_Wavelet_Factory)\\
  {\tt r_Wavelet_Coiflet#n}  && Coiflet n-tap Wavelet compression, n=6,12,18,24,30  (see r_Ortho_Wavelet_Factory)\\
  {\tt r_Wavelet_QHaar}      && Lossy Haar Wavelet compression  (see r_Haar_QWavelet_Compression)\\

  \end{tabular}
*/
enum r_Data_Format
{
    r_Array,
    r_TIFF,
    r_JPEG,
    r_JP2,
    r_HDF,
    r_NETCDF,
    r_CSV,
    r_JSON,
    r_PNG,
    r_ZLib,
    r_Auto_Compression,
    r_BMP,
    r_RLE,
    r_Wavelet_Haar,
    r_Wavelet_Daubechies,
    r_Sep_ZLib,
    r_Sep_RLE,
    r_Wavelet_Daub6,
    r_Wavelet_Daub8,
    r_Wavelet_Daub10,
    r_Wavelet_Daub12,
    r_Wavelet_Daub14,
    r_Wavelet_Daub16,
    r_Wavelet_Daub18,
    r_Wavelet_Daub20,
    r_Wavelet_Least8,
    r_Wavelet_Least10,
    r_Wavelet_Least12,
    r_Wavelet_Least14,
    r_Wavelet_Least16,
    r_Wavelet_Least18,
    r_Wavelet_Least20,
    r_Wavelet_Coiflet6,
    r_Wavelet_Coiflet12,
    r_Wavelet_Coiflet18,
    r_Wavelet_Coiflet24,
    r_Wavelet_Coiflet30,
    r_Wavelet_QHaar,
    r_PPM,
    r_DEM,
    r_Pack_Bits,
    r_ECW,
    r_TMC,
    r_NITF,
    r_GRIB,
    r_GDAL,
    r_Data_Format_NUMBER
};

//@ManMemo: Module: {\bf raslib}
/**
   The names of all data types, to avoid redundant storage and inconsistencies.
   The variable name convention is the prefix `format_name_` followed by the name
   of the data format in lower case without the `r_` prefix, i.e. for `r_Wavelet_Haar`
   `format_name_wavelet_haar`.
   In addition there's an array of names all_data_format_names where the data format
   can be used as index to get the name.
*/

extern const char *format_name_array;
extern const char *format_name_tiff;
extern const char *format_name_jpeg;
extern const char *format_name_jp2;
extern const char *format_name_hdf;
extern const char *format_name_netcdf;
extern const char *format_name_csv;
extern const char *format_name_json;
extern const char *format_name_png;
extern const char *format_name_zlib;
extern const char *format_name_auto_compression;
extern const char *format_name_bmp;
extern const char *format_name_ppm;
extern const char *format_name_rle;
extern const char *format_name_wavelet_haar;
extern const char *format_name_wavelet_daubechies;
extern const char *format_name_sep_zlib;
extern const char *format_name_sep_rle;
extern const char *format_name_wavelet_daub6;
extern const char *format_name_wavelet_daub8;
extern const char *format_name_wavelet_daub10;
extern const char *format_name_wavelet_daub12;
extern const char *format_name_wavelet_daub14;
extern const char *format_name_wavelet_daub16;
extern const char *format_name_wavelet_daub18;
extern const char *format_name_wavelet_daub20;
extern const char *format_name_wavelet_least8;
extern const char *format_name_wavelet_least10;
extern const char *format_name_wavelet_least12;
extern const char *format_name_wavelet_least14;
extern const char *format_name_wavelet_least16;
extern const char *format_name_wavelet_least18;
extern const char *format_name_wavelet_least20;
extern const char *format_name_wavelet_coiflet6;
extern const char *format_name_wavelet_coiflet12;
extern const char *format_name_wavelet_coiflet18;
extern const char *format_name_wavelet_coiflet24;
extern const char *format_name_wavelet_coiflet30;
extern const char *format_name_dem;
extern const char *format_name_pack_bits;
extern const char *format_name_wavelet_qhaar;
extern const char *format_name_tmc;
extern const char *format_name_nitf;
extern const char *format_name_grib;
extern const char *format_name_gdal;

extern const char *all_data_format_names[r_Data_Format_NUMBER];

//@ManMemo: Module: {\bf raslib}
/**
   Get a data format name for a data format
*/
const char *get_name_from_data_format(r_Data_Format fmt);

//@ManMemo: Module: {\bf raslib}
/**
  Get a data format for a data format name
*/
r_Data_Format get_data_format_from_name(const char *name);


//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type {\tt const} r_Data_Format.
*/
extern std::ostream &operator<<(std::ostream &s, const r_Data_Format &d);

enum r_Scale_Function
{
    r_SubSampling,
    r_BitAggregation,
    r_Scale_Function_NUMBER
};

extern const char *scale_function_name_subsampling;
extern const char *scale_function_name_bitaggregation;

extern const char *all_scale_function_names[r_Scale_Function_NUMBER];

//@ManMemo: Module: {\bf raslib}
/**
   Get a scale function name for a scale  function
*/
const char *get_name_from_scale_function(r_Scale_Function func);

//@ManMemo: Module: {\bf raslib}
/**
  Get a scale function from a scale function name
*/
r_Scale_Function get_scale_function_from_name(const char *name);

//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type {\tt const} r_Scale_Function.
*/
extern std::ostream &operator<<(std::ostream &s, const r_Scale_Function &d);


enum r_Index_Type
{
    r_Invalid_Index = -1,
    r_Auto_Index = 0,
    r_Directory_Index = 1,
    r_Reg_Directory_Index = 2,
    r_RPlus_Tree_Index = 3,
    r_Reg_RPlus_Tree_Index = 4,
    r_Tile_Container_Index = 5,
    r_Reg_Computed_Index = 6,
    r_Index_Type_NUMBER = 7
};

extern std::ostream &operator<<(std::ostream &in, r_Index_Type type);

//@ManMemo: Module: {\bf raslib}
/**
   The names of all index type, to avoid redundant storage and inconsistencies.
   The variable name convention is the prefix index_name_ followed by the name
   of the index type in lower case without the r_ prefix, i.e. for r_Auto_Index
   index_name_auto.
   In addition there's an array of names all_index_type_names where the index type
   can be used as index to get the name.
*/

extern const char *index_name_auto;
extern const char *index_name_directory;
extern const char *index_name_regdirectory;
extern const char *index_name_rplustree;
extern const char *index_name_regrplustree;
extern const char *index_name_tilecontainer;
extern const char *index_name_regcomputed;

extern const char *all_index_type_names[r_Index_Type_NUMBER];

//@ManMemo: Module: {\bf raslib}
/**
   Get a index type name for a index type
*/
const char *get_name_from_index_type(r_Index_Type it);

//@ManMemo: Module: {\bf raslib}
/**
   Get a index type  for a index type name
*/
r_Index_Type get_index_type_from_name(const char *name);

/**
    Tiling of the object:

    \begin{tabular}{ll}
    r_NoTiling       && no tiling is done unless the object is too big;
                        in that case, tiling is done along the first direction only;
                        for objects which are to be accessed always as a whole \\
    r_RegularTiling  && all tiles have the same scheme and size \\
    r_StatisticalTiling && based on statistics regarding access to this MDD object \\
    r_InterestTiling && based on specified areas of interest \\
    r_AlignedTiling  && like regular tiling, but tiles at the MDD edges are 
                        allowed to be different size / sdom \\
    r_DirectionalTiling && directional tiling \\
    r_SizeTiling    && tiles have a size smaller than the specified size
    \end{tabular}
*/
enum r_Tiling_Scheme
{
    r_NoTiling = 0,
    r_RegularTiling = 1,
    r_StatisticalTiling = 2,
    r_InterestTiling = 3,
    r_AlignedTiling = 4,
    r_DirectionalTiling = 5,
    r_SizeTiling = 6,
    r_Tiling_Scheme_NUMBER = 7
};

//@ManMemo: Module: {\bf raslib}
/**
   The names of all tiling schems, to avoid redundant storage and inconsistencies.
   The variable name convention is the prefix tiling_name_ followed by the name
   of the tiling scheme in lower case without the r_ prefix, i.e. for r_SizeTiling
   tiling_name_sizetiling.
   In addition there's an array of names all_tiling_scheme_names where the tile scheme
   can be used as index to get the name.
*/

extern const char *tiling_name_notiling;
extern const char *tiling_name_regulartiling;
extern const char *tiling_name_statisticaltiling;
extern const char *tiling_name_interesttiling;
extern const char *tiling_name_alignedtiling;
extern const char *tiling_name_directionaltiling;
extern const char *tiling_name_sizetiling;

extern const char *all_tiling_scheme_names[r_Tiling_Scheme_NUMBER];

//@ManMemo: Module: {\bf raslib}
/**
   Get a tiling scheme name for a tiling scheme
*/
const char *get_name_from_tiling_scheme(r_Tiling_Scheme ts);

//@ManMemo: Module: {\bf raslib}
/**
   Get a tiling scheme for a tiling scheme name
*/
r_Tiling_Scheme get_tiling_scheme_from_name(const char *name);

//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type {\tt const} r_Tiling_Scheme.
*/
extern std::ostream &operator<<(std::ostream &in, r_Tiling_Scheme type);

/**
Clustering of the tiles according to:

\begin{tabular}{lll}
r_Insertion_Order_Clustering    && the order of insertion of the tiles \\
r_Coords_Order_Clustering       &&  the coordinates of the tiles \\
r_Index_Cluster_Clustering      && the index structure \\
r_Based_Cluster_Stat_Clustering && statistics about access to the object
\end{tabular}

There is the additional {\bf PathCluster} mode, where clustering is
done according to a path of access to areas of interest.
The {\tt PathCluster} mode is indicated by setting the {\tt pathCluster}
attribute and a non - null value of the {\tt areasInterest}.
This mode is not an alternative mode in {\tt ClusteringScheme} because
it is compatible with the other modes.
*/
enum r_Clustering_Scheme
{
    r_Insertion_Order_Clustering = 1,
    r_Coords_Order_Clustering = 2,
    r_Index_Cluster_Clustering = 3,
    r_Based_Cluster_Stat_Clustering = 4
};
extern std::ostream &operator<<(std::ostream &in, r_Clustering_Scheme type);

#endif
