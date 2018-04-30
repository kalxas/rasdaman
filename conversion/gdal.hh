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
 * INCLUDE: grib.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_GDAL
 *
 * COMMENTS:
 *
 * Provides functions to convert data from GRIB only.
 *
 */

#ifndef _R_CONV_GDAL_HH_
#define _R_CONV_GDAL_HH_

#include "conversion/convertor.hh"
#include "raslib/minterval.hh"
#include "conversion/gdalincludes.hh"

#include <json/json.h>
#include <string>
#include <stdio.h>
#include <float.h>

//@ManMemo: Module {\bf conversion}

/*@Doc:
 * Convertor class using GDAL for format conversion.
 */
class r_Conv_GDAL : public r_Convert_Memory
{
public:
    /// constructor using an r_Type object. Exception if the type isn't atomic.
    r_Conv_GDAL(const char* src, const r_Minterval& interv, const r_Type* tp) throw (r_Error);
    /// constructor using convert_type_e shortcut
    r_Conv_GDAL(const char* src, const r_Minterval& interv, int tp) throw (r_Error);
    /// destructor
    ~r_Conv_GDAL(void);

    /// convert to format
    virtual r_Conv_Desc& convertTo(const char* options = NULL,
                                   const r_Range* nullValue = NULL) throw (r_Error);
    /// convert from format
    virtual r_Conv_Desc& convertFrom(const char* options = NULL) throw (r_Error);

    virtual r_Conv_Desc& convertFrom(r_Format_Params options) throw(r_Error);

    /// cloning
    virtual r_Convertor* clone(void) const;

    /// identification
    virtual const char* get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;

private:

#ifdef HAVE_GDAL

    /**
     * Transforms the rasdaman array desc.src array to a GDAL dataset.
     *
     * @param poDataset the target GDAL dataset
     * @param gdalBandType band type, they have to be all equal
     * @param isBoolean indicate if the rasdaman data is boolean
     * @param bandTypeSize base type size of one band
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     * @param rasBandType rasdaman band type
     */
    void encodeImage(GDALDataset* poDataset, GDALDataType gdalBandType, r_Primitive_Type* rasBandType,
                     unsigned int width, unsigned int height, unsigned int numBands) throw (r_Error);

    /**
     * Transforms the rasdaman array desc.src array of a given band base type T to a GDAL dataset.
     *
     * @param poDataset the target GDAL dataset
     * @param gdalBandType band type, they have to be all equal
     * @param isBoolean indicate if the rasdaman data is boolean
     * @param bandTypeSize base type size of one band
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     */
    template<typename T>
    void encodeImage(GDALDataset* poDataset, GDALDataType gdalBandType, bool isBoolean,
                     unsigned int width, unsigned int height, unsigned int numBands) throw (r_Error);

    /**
     * @return (width, height) of the rasdaman array, throw an error in case of
     * overflow or invalid image dimension.
     */
    std::pair<unsigned int, unsigned int> getImageSize() throw (r_Error);

    /**
     * Transforms the file read with GDAL to an internal rasdaman array.
     *
     * @param poDataSet the dataset read with GDAL
     * @param bandIds typically all bands from a file are read, but with the format parameters
     *                a subset of the bands can be selected: this vector holds the ids (0-based)
     *                of the bands that should be read, whether all or a subset.
     */
    char* decodeImage(GDALDataset* poDataSet, const std::vector<int>& bandIds) throw (r_Error);

    /**
     * Copy and transform a single band data read with GDAL to an internal rasdaman array.
     *
     * @param bandCells source GDAL band data
     * @param tileCells target internal rasdaman array, this is in pixel interleaved format and
     *                  it is assumed to be properly offset to the current band.
     * @param tileBaseTypeSize size of the struct base type (sum of the base type sizes of all bands)
     * @param width image width; it is int as this is the type that GDAL returns
     * @param height image height; it is int as this is the type that GDAL returns
     * @param gdalBandType GDAL band type
     * @param signedByte optional argument indicating if GDT_Byte is to be interpreted as a signed char
     */
    void decodeBand(const char* bandCells, char* tileCells, size_t tileBaseTypeSize,
                    int width, int height, GDALDataType gdalBandType, bool signedByte = false) throw (r_Error);

    /**
     * Copy and transform a single band data read with GDAL to an internal rasdaman array.
     *
     * @param bandCells source GDAL band data
     * @param tileCells target internal rasdaman array, this is in pixel interleaved format and
     *                  it is assumed to be properly offset to the current band.
     * @param tileBaseTypeSize size of the struct base type (sum of the base type sizes of all bands)
     * @param width image width; it is int as this is the type that GDAL returns
     * @param height image height; it is int as this is the type that GDAL returns
     */
    template<typename T>
    void decodeBand(const char* bandCells, char* tileCells, size_t tileBaseTypeSize, int width, int height);

    /**
     * Determine the band ids (0-indexed) to be imported.
     *
     * @param poDataset the dataset read with GDAL
     * @return if the format parameters specify the bands to be imported than those
     * are returned, otherwise the returned vector contains 0..bandNo-1, i.e.
     * all bands.
     */
    std::vector<int> getBandIds(GDALDataset* poDataset) throw (r_Error);

    /**
     * @return the base type of a single band (equals to baseType in case it is
     * a primitive type).
     */
    r_Primitive_Type* getBandType(const r_Type* baseType) throw (r_Error);

    /**
     * @param buffer the current buffer
     * @param bufferSize the current buffer size in bytes, updated if the buffer is changed.
     * @return a potentially bigger buffer if bufferSize is greater than the previousBufferSize.
     */
    char* upsizeBufferIfNeeded(char* buffer, size_t& bufferSize, size_t newBufferSize) throw (r_Error);

    /**
     * Set the result array domain from decode (desc.destInterv).
     * @param poDataset the dataset read with GDAL
     */
    void setTargetDomain(GDALDataset* poDataset) throw (r_Error);

    /**
     * Initialize the format parameters (third parameter of the encode function).
     *
     * @param params JSON or old-style key/value pairs
     */
    void initEncodeParams(const std::string& params);

    /**
     * Transfer format parameters (third parameter of the encode function) to the
     * output gdalDataSet.
     *
     * @param gdalDataSet GDAL dataset to be written
     */
    void setEncodeParams(GDALDataset* gdalDataSet);
    void setMetadata(GDALDataset* gdalDataSet);
    void setNodata(GDALDataset* gdalDataSet);
    void setGeoreference(GDALDataset* gdalDataSet);
    void setGeotransform(GDALDataset* gdalDataSet);
    void setGCPs(GDALDataset* gdalDataSet, const Json::Value& gcpsJson) throw (r_Error);
    void setColorPalette(GDALDataset* gdalDataSet) throw (r_Error);

    /**
     * @return the crs specified in the format parameters in WKT format, or NULL
     * otherwise.
     */
    std::string getCrsWkt();

    /**
     * @param paletteInterpVal one of Gray, RGB, CMYK, HSL.
     * @return the corresponding GDAL enum
     */
    GDALPaletteInterp getPaletteInterp(const std::string& paletteInterpVal) throw (r_Error);

    /**
     * @param stringList this parameter is populated with a list of key/value
     * format parameters.
     */
    void getFormatParameters(CPLStringList& stringList);

    /**
     * Convert the value of a given parameter to double.
     *
     * @param paramName parameter name
     * @param paramsList a list of key/value parameter pairs
     * @return the value of paramName as a double, a DBL_MAX otherwise.
     */
    double getDouble(const std::string& paramName, const CPLStringList& paramsList);

    /**
     * Convert the value of a given parameter to string.
     *
     * @param paramName parameter name
     * @param paramsList a list of key/value parameter pairs
     * @return the value of paramName as a string, an empty string otherwise.
     */
    std::string getString(const std::string& paramName, const CPLStringList& paramsList);

    /**
     * Set GDAL configuration parameters from any formatParameters key/values.
     */
    void setConfigOptions();

#endif // HAVE_GDAL

    static const std::string GDAL_KEY_METADATA;
    static const std::string GDAL_KEY_NODATA_VALUES;
    static const std::string GDAL_KEY_IMAGE_STRUCTURE;
    static const std::string GDAL_KEY_PIXELTYPE;
    static const std::string GDAL_VAL_SIGNEDBYTE;
};

#endif
