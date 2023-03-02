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

#include "conversion/formatparams.hh"
#include "conversion/formatparamkeys.hh"

#include <limits>
#include <boost/algorithm/string/replace.hpp>  // for replace_all
#include <boost/algorithm/string/trim.hpp>     // for trim_left
#include <logging.hh>

using std::make_pair;
using std::map;
using std::numeric_limits;
using std::pair;
using std::string;
using std::unordered_map;
using std::vector;

r_Format_Params::r_Format_Params()
    : xmin{numeric_limits<double>::max()},
      xmax{numeric_limits<double>::max()},
      ymin{numeric_limits<double>::max()},
      ymax{numeric_limits<double>::max()}
{
}

bool r_Format_Params::parse(const string &options)
{
    bool ret = false;
    if (!options.empty())
    {
        if (isJson(options))
        {
            string json{options};
            LDEBUG << "parsing json format parameters: " << json;
            // rasql transmits \" from the cmd line literally; this doesn't work
            // in json, so we unescape them below
            boost::algorithm::replace_all(json, "\\\"", "\"");

            Json::CharReaderBuilder rbuilder;
            rbuilder["strictRoot"] = false;
            rbuilder["collectComments"] = false;
            rbuilder["allowComments"] = true;
            rbuilder["allowSpecialFloats"] = true;
            std::istringstream iss{json};
            std::string errs;
            ret = Json::parseFromStream(rbuilder, iss, &params, &errs);
            if (ret)
            {
                parseJson();
            }
            else
            {
                throw r_Error(r_Error::r_Error_Conversion,
                              "failed parsing the JSON format options, " + errs);
            }
        }
    }
    return ret;
}

bool r_Format_Params::isJson(string options) const
{
    boost::trim_left(options);
    // it's json if the options string starts with a '{'
    return !options.empty() && options[0] == '{';
}

void r_Format_Params::parseJson()
{
    parseTranspose();
    parseColorMap();
    parseVariables();
    parseFilepaths();
    parseStringKeyValuesList(FormatParamKeys::General::FORMAT_PARAMETERS, formatParameters);
    parseStringKeyValuesList(FormatParamKeys::General::CONFIG_OPTIONS, configOptions);
    parseStringKeyValuesList(FormatParamKeys::General::OPEN_OPTIONS, openOptions);
    parseSubsetDomain();
    parseNodata();
    parseMetadata();
    parseGeoReference();
}

void r_Format_Params::parseTranspose()
{
    const string &key = FormatParamKeys::General::TRANSPOSE;
    if (params.isMember(key))
    {
        const Json::Value &val = params[key];
        if (val.size() != 2 || !val.isArray())
        {
            throw r_Error(r_Error::r_Error_Conversion, "parameter '" + key +
                                                           "' has an invalid value, expected an array with two index positions");
        }
        transposePair = make_pair(val[0].asInt(), val[1].asInt());
        transpose = true;
    }
}

void r_Format_Params::parseColorMap()
{
    const string &key = FormatParamKeys::General::COLORMAP;
    if (!params.isMember(key))
    {
        return;
    }

    const Json::Value &val = params[key];

    const string &type = FormatParamKeys::Encode::ColorMap::TYPE;
    const string &colorTable = FormatParamKeys::Encode::ColorMap::COLORTABLE;

    if (val.size() != 2 || !val.isMember(type) || !val.isMember(colorTable))
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "invalid value(s) specified for format parameter " + key);
    }

    const auto valType = val[type].asString();
    if (valType == "values")
    {
        colorMapTable.setColorMapType(r_ColorMap::Type::VALUES);
    }
    else if (valType == "intervals")
    {
        colorMapTable.setColorMapType(r_ColorMap::Type::INTERVALS);
    }
    else if (valType == "ramp")
    {
        colorMapTable.setColorMapType(r_ColorMap::Type::RAMP);
    }
    else
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "invalid colorMap type " + valType);
    }

    const Json::Value &table = val[colorTable];
    if (table.empty())
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "empty colorTable provided");
    }

    std::map<double, std::string> pixelValuesMap;
    std::vector<double> pixelValues;
    unsigned int i = 0;
    for (Json::ValueConstIterator a = table.begin(); a != table.end(); a++, i++)
    {
        try
        {
            pixelValues.push_back(stod(a.key().asString()));
        }
        catch (...)
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "cannot transform value of format parameter '" +
                              a.key().asString() + "' to double.");
        }
        pixelValuesMap[pixelValues.back()] = a.key().asString();
    }
    sort(pixelValues.begin(), pixelValues.end());

    std::map<double, std::vector<unsigned char>> colorTableMap;
    std::unordered_map<double, std::vector<unsigned char>> uColorTableMap;
    bool nrCompSet{false};
    size_t nrComp{};
    for (unsigned int n = 0; n < i; n++)
    {
        double it = pixelValues[n];
        for (Json::Value x: table[pixelValuesMap[it]])
        {
            if (x.asInt() >= 0 && x.asInt() <= 255)
            {
                colorTableMap[pixelValues[n]].push_back(static_cast<unsigned char>(x.asInt()));
                uColorTableMap[pixelValues[n]].push_back(static_cast<unsigned char>(x.asInt()));
            }
            else
            {
                throw r_Error(r_Error::r_Error_Conversion,
                              "color table entry " + std::to_string(x.asInt()) +
                                  " is not whithin the interval [0, 255]");
            }
        }
        if (!nrCompSet)
        {
            nrComp = colorTableMap[pixelValues[n]].size();
        }
        else if (nrComp != colorTableMap[pixelValues[n]].size())
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "all entries in the color table must have the same number of components.");
        }
    }
    colorMapTable.setColorTable(colorTableMap);
    colorMapTable.setUColorTable(uColorTableMap);
    colorMapFlag = true;
}

void r_Format_Params::parseVariables()
{
    const string &key = FormatParamKeys::General::VARIABLES;
    if (params.isMember(key))
    {
        const Json::Value &val = params[key];
        if (val.isArray())
        {
            for (Json::ArrayIndex i = 0; i < val.size(); i++)
            {
                if (val[i].isInt())
                {
                    bandIds.push_back(val[i].asInt());
                }
                else if (val[i].isString())
                {
                    variables.push_back(val[i].asString());
                }
            }
        }
        else if (val.isObject())
        {
            for (const string &varName: val.getMemberNames())
            {
                variables.push_back(varName);
            }
        }
        else
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "format parameter " + key + " has an invalid value, "
                                                      "expected an array/object with dataset/band identifiers");
        }
    }
}

void r_Format_Params::parseFilepaths()
{
    const string &key = FormatParamKeys::Decode::FILEPATHS;
    if (params.isMember(key))
    {
        const Json::Value &val = params[key];
        if (!val.isArray())
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "format parameter " + key + " has an invalid value, "
                                                      "expected an array with file paths");
        }
        for (Json::ArrayIndex i = 0; i < val.size(); i++)
        {
            filePaths.push_back(val[i].asString());
        }
    }
}

void r_Format_Params::parseStringKeyValuesList(const string &key, std::vector<std::pair<std::string, std::string>> &targetVector)
{
    if (params.isMember(key))
    {
        const Json::Value &val = params[key];
        if (!val.isObject())
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "format parameter " + key + " has an invalid value, "
                                                      "expected an object with key/value pairs");
        }

        // todo take care of the xmin/xmax/..
        LDEBUG << "parsing " << key << " from format parameters";
        for (const string &fkey: val.getMemberNames())
        {
            string fval = val[fkey].asString();
            LDEBUG << fkey << ": " << fval;
            targetVector.push_back(make_pair(fkey, fval));
        }
    }
}

void r_Format_Params::parseSubsetDomain()
{
    const string &key = FormatParamKeys::Decode::SUBSET_DOMAIN;
    if (params.isMember(key))
    {
        const string &val = params[key].asString();
        try
        {
            subsetDomain = r_Minterval(val.c_str());
        }
        catch (r_Error &err)
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "format parameter " + key + " has an invalid value, "
                                                      "expected a subset minterval");
        }
    }
}

void r_Format_Params::parseNodata()
{
    const string &key = FormatParamKeys::Encode::NODATA;
    if (params.isMember(key))
    {
        const Json::Value &val = params[key];
        if (val.isArray())
        {
            for (Json::ArrayIndex i = 0; i < val.size(); i++)
            {
                if (!val[i].isDouble())
                {
                    throw r_Error(r_Error::r_Error_Conversion,
                                  "format parameter " + key + " has an invalid value, "
                                                              "expected an array of double values");
                }
                nodata.push_back(val[i].asDouble());
            }
        }
        else if (val.isConvertibleTo(Json::ValueType::realValue))
        {
            nodata.push_back(val.asDouble());
        }
        else
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "format parameter " + key + " has an invalid value, "
                                                      "expected double or an array of double values");
        }
    }
}

void r_Format_Params::parseMetadata()
{
    const string &key = FormatParamKeys::Encode::METADATA;
    if (params.isMember(key))
    {
        const Json::Value &val = params[key];
        if (val.isObject())
        {
            for (string &fkey: val.getMemberNames())
            {
                string fval = val[fkey].asString();
                metadataKeyValues.push_back(make_pair(fkey, fval));
            }
        }
        else if (val.isString())
        {
            metadata = val.asString();
        }
        else
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "format parameter " + key + " has an invalid value, "
                                                      "expected string or an object of key/value string pairs");
        }
    }
}

void r_Format_Params::parseGeoReference()
{
    const string &key = FormatParamKeys::Encode::GEO_REFERENCE;
    if (params.isMember(key))
    {
        const Json::Value &geoRef = params[key];
        if (geoRef.isMember(FormatParamKeys::Encode::CRS))
        {
            crs = geoRef[FormatParamKeys::Encode::CRS].asString();
        }
        else
        {
            LWARNING << "parameter '" << key
                     << "' has an invalid value, it must contain a crs.";
        }
        const string &keyBbox = FormatParamKeys::Encode::BBOX;
        if (geoRef.isMember(keyBbox))
        {
            const Json::Value &bbox = geoRef[keyBbox];
            if (bbox.isMember(FormatParamKeys::Encode::XMIN) &&
                bbox.isMember(FormatParamKeys::Encode::XMAX) &&
                bbox.isMember(FormatParamKeys::Encode::YMIN) &&
                bbox.isMember(FormatParamKeys::Encode::YMAX))
            {
                xmin = bbox[FormatParamKeys::Encode::XMIN].asDouble();
                xmax = bbox[FormatParamKeys::Encode::XMAX].asDouble();
                ymin = bbox[FormatParamKeys::Encode::YMIN].asDouble();
                ymax = bbox[FormatParamKeys::Encode::YMAX].asDouble();
            }
            else
            {
                LWARNING << "parameter '" << keyBbox
                         << "' has an invalid value, it must contain xmin, ymin, xmax and ymax parameters.";
            }
        }
    }
}

Json::Value r_Format_Params::getParams() const
{
    return params;
}

bool r_Format_Params::isValidJson() const
{
    return !params.isNull();
}

vector<string> r_Format_Params::getFilePaths() const
{
    return filePaths;
}

void r_Format_Params::setFilePaths(const std::vector<std::string> &filePathsArg)
{
    this->filePaths = filePathsArg;
}

const string &r_Format_Params::getFilePath() const
{
    return filePaths[0];
}

vector<string> &r_Format_Params::getVariables()
{
    return variables;
}

const vector<string> &r_Format_Params::getVariables() const
{
    return variables;
}

vector<int> &r_Format_Params::getBandIds()
{
    return bandIds;
}

const vector<int> &r_Format_Params::getBandIds() const
{
    return bandIds;
}

vector<pair<string, string>> r_Format_Params::getFormatParameters() const
{
    return formatParameters;
}

void r_Format_Params::addFormatParameter(const std::string &key, const std::string &val)
{
    formatParameters.push_back(make_pair(key, val));
}

vector<pair<string, string>> r_Format_Params::getConfigOptions() const
{
    return configOptions;
}

std::vector<std::pair<std::string, std::string>> r_Format_Params::getOpenOptions() const
{
    return openOptions;
}

const r_Minterval &r_Format_Params::getSubsetDomain() const
{
    return subsetDomain;
}

void r_Format_Params::setSubsetDomain(const r_Minterval &domain)
{
    this->subsetDomain = domain;
}

pair<int, int> r_Format_Params::getTranspose() const
{
    return transposePair;
}

bool r_Format_Params::isTranspose() const
{
    return transpose;
}

bool r_Format_Params::isColorMap() const
{
    return colorMapFlag;
}

std::vector<double> r_Format_Params::getNodata() const
{
    return nodata;
}

void r_Format_Params::addNodata(double val)
{
    nodata.push_back(val);
}

string r_Format_Params::getCrs() const
{
    return crs;
}

string r_Format_Params::getMetadata() const
{
    return metadata;
}

void r_Format_Params::setMetadata(const std::string &metadataArg)
{
    metadata = metadataArg;
}

vector<pair<string, string>> r_Format_Params::getMetadataKeyValues() const
{
    return metadataKeyValues;
}

double r_Format_Params::getXmin() const
{
    return xmin;
}

double r_Format_Params::getXmax() const
{
    return xmax;
}

double r_Format_Params::getYmin() const
{
    return ymin;
}

double r_Format_Params::getYmax() const
{
    return ymax;
}

void r_Format_Params::setCrs(const std::string &crsArg)
{
    crs = crsArg;
}

void r_Format_Params::setXmin(double val)
{
    xmin = val;
}

void r_Format_Params::setXmax(double val)
{
    xmax = val;
}

void r_Format_Params::setYmin(double val)
{
    ymin = val;
}

void r_Format_Params::setYmax(double val)
{
    ymax = val;
}

const string &r_Format_Params::getFormat() const
{
    return format;
}
void r_Format_Params::setFormat(const string &formatArg)
{
    r_Format_Params::format = formatArg;
}
