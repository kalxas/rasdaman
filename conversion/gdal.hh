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
#include "config.h"

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
    r_Conv_GDAL(const char* src, const r_Minterval& interv, const r_Type* tp);
    /// constructor using convert_type_e shortcut
    r_Conv_GDAL(const char* src, const r_Minterval& interv, int tp);
    /// destructor
    ~r_Conv_GDAL(void) override;

    /// convert to format
    r_Conv_Desc& convertTo(const char* options = NULL,
                           const r_Range* nullValue = NULL) override;
    /// convert from format
    r_Conv_Desc& convertFrom(const char* options = NULL) override;

    r_Conv_Desc& convertFrom(r_Format_Params options) override;

    /// cloning
    r_Convertor* clone(void) const override;

    /// identification
    const char* get_name(void) const override;
    r_Data_Format get_data_format(void) const override;

private:

#ifdef HAVE_GDAL

    /**
     * Transforms the rasdaman array desc.src array to a GDAL dataset.
     *
     * @param gdalBandType band type, they have to be all equal
     * @param isBoolean indicate if the rasdaman data is boolean
     * @param bandTypeSize base type size of one band
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     * @param rasBandType rasdaman band type
     */
    void encodeImage(GDALDataType gdalBandType, r_Primitive_Type* rasBandType,
                     unsigned int width, unsigned int height, unsigned int numBands);

    /**
     * Transforms the rasdaman array desc.src array of a given band base type T to a GDAL dataset.
     *
     * @param gdalBandType band type, they have to be all equal
     * @param isBoolean indicate if the rasdaman data is boolean
     * @param bandTypeSize base type size of one band
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     */
    template<typename T>
    void encodeImage(GDALDataType gdalBandType, bool isBoolean,
                     unsigned int width, unsigned int height, unsigned int numBands, bool isComplex);

    /**
     * @return (width, height) of the rasdaman array, throw an error in case of
     * overflow or invalid image dimension.
     */
    std::pair<unsigned int, unsigned int> getImageSize();

    /**
     * Transforms the file read with GDAL to an internal rasdaman array.
     */
    char* decodeImage();

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
                    int width, int height, GDALDataType gdalBandType, bool signedByte = false);

    /**
     * Copy and transform a single band data read with GDAL to an internal rasdaman array.
     *
     * @param src source GDAL band data
     * @param dstIn target internal rasdaman array, this is in pixel interleaved format and
     *                  it is assumed to be properly offset to the current band.
     * @param tileBaseTypeSize size of the struct base type (sum of the base type sizes of all bands)
     * @param N image width; it is int as this is the type that GDAL returns
     * @param M image height; it is int as this is the type that GDAL returns
     */
    template<typename T>
    void transposeBand(const char* __restrict__ srcIn, char* __restrict__ dstIn,
                       size_t tileBaseTypeSize, size_t N, size_t M);

    /**
     * Determine the band ids (0-indexed) to be imported.
     *
     * @return if the format parameters specify the bands to be imported than those
     * are returned, otherwise the returned vector contains 0..bandNo-1, i.e.
     * all bands.
     */
    std::vector<int> getBandIds();

    /**
     * @return the base type of a single band (equals to baseType in case it is
     * a primitive type).
     */
    r_Primitive_Type* getBandType(const r_Type* baseType);

    /**
     * @param buffer the current buffer
     * @param bufferSize the current buffer size in bytes, updated if the buffer is changed.
     * @return a potentially bigger buffer if bufferSize is greater than the previousBufferSize.
     */
    char* upsizeBufferIfNeeded(char* buffer, size_t& bufferSize, size_t newBufferSize);

    /**
     * Set the result array domain from decode (desc.destInterv).
     */
    void setTargetDomain(bool transpose = true);

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
    void setEncodeParams();
    void setMetadata();
    void setNodata();
    void setGeoreference();
    void setGeotransform();
    void setGCPs(const Json::Value& gcpsJson);
    void setColorPalette();

    /**
     * @return the crs specified in the format parameters in WKT format, or NULL
     * otherwise.
     */
    std::string getCrsWkt();

    /**
     * @param paletteInterpVal one of Gray, RGB, CMYK, HSL.
     * @return the corresponding GDAL enum
     */
    GDALPaletteInterp getPaletteInterp(const std::string& paletteInterpVal);

    /**
     * @param stringList this parameter is populated with a list of key/value
     * format parameters.
     */
    void getFormatParameters(CPLStringList& stringList, r_Primitive_Type* rasBandType);

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

    GDALDataset* poDataset{NULL};

#endif // HAVE_GDAL

    static const std::string GDAL_KEY_METADATA;
    static const std::string GDAL_KEY_NODATA_VALUES;
    static const std::string GDAL_KEY_IMAGE_STRUCTURE;
    static const std::string GDAL_KEY_PIXELTYPE;
    static const std::string GDAL_VAL_SIGNEDBYTE;

    static const std::string PNG_COMPRESSION_PARAM;
    static const std::string PNG_DEFAULT_ZLEVEL;
    static const std::string PNG_FORMAT;

    std::vector<int> bandIds;
    /// set to true if a color map has been applied to the source array before
    /// encoding; this info is used in the destructor
    bool colorMapEvaluated{false};
};

#endif
