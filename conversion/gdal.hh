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

//@ManMemo: Module {\bf conversion}

/*@Doc:
 * Convertor class using GDAL for format conversion.
 */
class r_Conv_GDAL : public r_Convert_Memory
{
public:
    /// constructor using an r_Type object. Exception if the type isn't atomic.
    r_Conv_GDAL(const char *src, const r_Minterval &interv, const r_Type *tp) throw (r_Error);
    /// constructor using convert_type_e shortcut
    r_Conv_GDAL(const char *src, const r_Minterval &interv, int tp) throw (r_Error);
    /// destructor
    ~r_Conv_GDAL(void);

    /// convert to format
    virtual r_Conv_Desc &convertTo(const char *options = NULL) throw (r_Error);
    /// convert from format
    virtual r_Conv_Desc &convertFrom(const char *options = NULL) throw (r_Error);

    /// cloning
    virtual r_Convertor *clone(void) const;

    /// identification
    virtual const char *get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;
    
private:

	struct GenericParams {
		double xmin;
		double xmax;
		double ymax;
		double ymin;

		std::string crs; // string representation of the coordinate reference system
		std::string metadata; // further metadata of the result
		std::vector<double> nodata; // nodata values of the result
	};
    
#ifdef HAVE_GDAL
    
	/**
	 * Transforms the file read with GDAL to a rasdaman Tile.
	 *
	 * WARNING: GDALDataConverter::getTileCells() closes the GDAL dataset.
	 *
	 * @param poDataSet The dataset read from the temporary file with GDAL.
     * @param size Out parameter representing the size of the read data.
     * @param contents Out parameter representing the read file contets casted to char*.
	 */
    static void getTileCells(GDALDataset* poDataSet, /* out */ r_Bytes& size, /* out */ char*& contents);
    
	/**
	 * Converts the GDAL dataset to rasdaman dataset, using type T for the rasdaman tile.
     *
     * @param poDataSet The dataset read from the temporary file with GDAL.
     * @param size Out parameter representing the size of the read data.
     * @param contents Out parameter representing the read file contets casted to char*.
     */
	template<typename T>
    static void resolveTileCellsByType(GDALDataset* poDataset, /* out */ r_Bytes& size, /* out */ char*& contents);
    
	/**
	 * Initialize the gdal parameters tokenizing the string of parameters by the
	 * separator PARAM_SEPARATOR
     * @param params string representation of gdal parameters separated by PARAM_SEPARATOR
     */
	void initDecodeParams(const char* paramsArg);
    
	void initEncodeParams(const char* params);
    
	void setGDALParameters(GDALDataset *gdalDataSet, int width, int height, int nBands);
    
	void setDouble(const char* paramName, double* value);
    
	void setString(const char* paramName, std::string* value);
    
#endif // HAVE_GDAL
    
	char** fParams;
	GenericParams gParams;
};

#endif
