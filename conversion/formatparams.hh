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

#ifndef _FORMATPARAMS_HH_
#define _FORMATPARAMS_HH_

#include "config.h"

#include "raslib/minterval.hh"
#include "raslib/error.hh"

#include <json/json.h>
#include <vector>
#include <string>

//@ManMemo: Module {\bf conversion}

/*@Doc:
 * Conversion format <-> rasdaman array can be controlled via format parameters,
 * handled by this class which parses a JSON string and populates common parameter values.
 *
 * Full documentation at http://rasdaman.org/wiki/CommonFormatsInterface
 */
class r_Format_Params
{
public:

    r_Format_Params();

    /**
     * Parse the input format parameters into a JSON object and get the values for
     * parameters common across multiple converters, if the paramsStr is a
     * JSON string (starts with a '{').
     * 
     * @param paramsStr a format parameters string.
     * @return true if paramsStr is a JSON string and it was successfully parsed,
     * false otherwise. It throws an error if it is a JSON string and parsing
     * it failed.
     */
    bool parse(const std::string& paramsStr) throw (r_Error);

    /// get the JSON params
    Json::Value getParams() const;

    /**
     * @return true if the parameters have been parsed.
     */
    bool isValidJson() const;

    /// get paths to files to be decoded
    std::vector<std::string> getFilePaths() const;

    /// set the paths to files to be decoded
    void setFilePaths(const std::vector<std::string>& filePaths);

    /// get the path to a file to be decoded
    std::string getFilePath() const;

    /// (subset of) variable names to be decoded from the input file
    std::vector<std::string> getVariables() const;

    /// (subset of) band ids (0-indexed) to be decoded from the input file
    std::vector<int> getBandIds() const;

    /// subset region to be decoded from the input file, instead of the whole file
    r_Minterval getSubsetDomain() const;

    /// sets subset region to be decoded from the input file, instead of the whole file
    void setSubsetDomain(const r_Minterval& domain);

    /// extra format parameters, e.g. compression type; convertor-dependent
    std::vector<std::pair<std::string, std::string>> getFormatParameters() const;

    /// extra format parameters, e.g. compression type; convertor-dependent
    void addFormatParameter(const std::string& key, const std::string& val);

    /// Configuration options (string key/value pairs); details for GDAL: https://trac.osgeo.org/gdal/wiki/ConfigOptions
    std::vector<std::pair<std::string, std::string>> getConfigOptions() const;

    /// indicate dimensions (0-indexed) to be transposed
    std::pair<int, int> getTranspose() const;

    /// true if transposing dimensions is requested
    bool isTranspose() const;

    /// nodata values, if there is more than 1 they are applied correspondingly to each band.
    std::vector<double> getNodata() const;
    /// nodata values, if there is more than 1 they are applied correspondingly to each band.
    void addNodata(double val);

    /// extra metadata
    std::string getMetadata() const;
    /// extra metadata
    void setMetadata(const std::string& metadata);

    /// extra metadata represented as a vector of key/value pairs
    std::vector<std::pair<std::string, std::string>> getMetadataKeyValues() const;

    /// coordinate reference system
    std::string getCrs() const;
    /// coordinate reference system
    void setCrs(const std::string& crs);

    /// min X geo bound
    double getXmin() const;
    /// min X geo bound
    void setXmin(double val);

    /// max X geo bound
    double getXmax() const;
    /// max X geo bound
    void setXmax(double val);

    /// min Y geo bound
    double getYmin() const;
    /// min Y geo bound
    void setYmin(double val);

    /// max Y geo bound
    double getYmax() const;
    /// max Y geo bound
    void setYmax(double val);

private:

    /**
     * @return true if the string starts with a '{'.
     */
    bool isJson(std::string options) const;
    
    void parseJson() throw (r_Error);
    void parseTranspose() throw (r_Error);
    void parseVariables() throw (r_Error);
    void parseFilepaths() throw (r_Error);
    void parseStringKeyValuesList(const std::string& key, std::vector<std::pair<std::string, std::string>>& targetVector) throw (r_Error);
    void parseSubsetDomain() throw (r_Error);
    void parseNodata() throw (r_Error);
    void parseMetadata() throw (r_Error);
    void parseGeoReference() throw (r_Error);

    Json::Value params;

    /// specifying a path to file to be decoded
    std::vector<std::string> filePaths;

    /// (subset of) variable names to be decoded from the input file
    std::vector<std::string> variables;

    /// subset region to be decoded from the input file, instead of the whole file
    r_Minterval subsetDomain;

    /// (subset of) band ids (0-indexed) to be decoded from the input file
    std::vector<int> bandIds;

    /// extra format parameters, e.g. compression type; convertor-dependent
    std::vector<std::pair<std::string, std::string>> formatParameters;

    /// Configuration options (string key/value pairs); details for GDAL: https://trac.osgeo.org/gdal/wiki/ConfigOptions
    std::vector<std::pair<std::string, std::string>> configOptions;

    /// indicate dimensions (0-indexed) to be transposed
    std::pair<int, int> transposePair;

    /// true if transposing dimensions is requested
    bool transpose{false};

    /// coordinate reference system
    std::string crs;
    /// min X geo bound
    double xmin;
    /// max X geo bound
    double xmax;
    /// min Y geo bound
    double ymin;
    /// max Y geo bound
    double ymax;

    /// extra metadata
    std::string metadata;

    /// extra metadata represented as a vector of key/value pairs
    std::vector<std::pair<std::string, std::string>> metadataKeyValues;

    /// nodata values, if there is more than 1 they are applied correspondingly to each band.
    std::vector<double> nodata;

};

#endif
