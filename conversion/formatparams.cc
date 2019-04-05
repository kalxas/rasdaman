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

#include "config.h"

#include "conversion/formatparams.hh"
#include "conversion/formatparamkeys.hh"

#include <limits>
#include <boost/algorithm/string.hpp>
#include <logging.hh>

using std::string;
using std::vector;
using std::pair;
using std::make_pair;
using std::numeric_limits;

r_Format_Params::r_Format_Params() :
    xmin{numeric_limits<double>::max()},
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
                LERROR << "failed parsing the JSON format options: " << errs;
                LERROR << "original options string: '" << options << "'.";
                throw r_Error(INVALIDFORMATPARAMETER);
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
    parseVariables();
    parseFilepaths();
    parseStringKeyValuesList(FormatParamKeys::General::FORMAT_PARAMETERS, formatParameters);
    parseStringKeyValuesList(FormatParamKeys::General::CONFIG_OPTIONS, configOptions);
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
            LERROR << "parameter '" << key << "' has an invalid value, expected an array with two index positions.";
            throw r_Error(INVALIDFORMATPARAMETER);
        }
        transposePair = make_pair(val[0].asInt(), val[1].asInt());
        transpose = true;
    }
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
                else
                {
                    variables.push_back(val[i].asString());
                }
            }
        }
        else if (val.isObject())
        {
            for (const string &varName : val.getMemberNames())
            {
                variables.push_back(varName);
            }
        }
        else
        {
            LERROR << "parameter '" << key << "' has an invalid value, expected an array/object with dataset/band identifiers.";
            throw r_Error(INVALIDFORMATPARAMETER);
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
            LERROR << "parameter '" << key << "' has an invalid value, expected an array with file paths.";
            throw r_Error(INVALIDFORMATPARAMETER);
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
            LERROR << "parameter '" << key << "' has an invalid value, expected an object with key/value pairs.";
            throw r_Error(INVALIDFORMATPARAMETER);
        }

        // todo take care of the xmin/xmax/..
        for (const string &fkey : val.getMemberNames())
        {
            string fval = val[fkey].asString();
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
            LERROR << "parameter '" << key << "' has an invalid value, expected a subset minterval.";
            throw r_Error(INVALIDFORMATPARAMETER);
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
                    LERROR << "parameter '" << key << "' has an invalid value, expected an array of double values.";
                    throw r_Error(INVALIDFORMATPARAMETER);
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
            LERROR << "parameter '" << key << "' has an invalid value, expected double or an array of double values.";
            throw r_Error(INVALIDFORMATPARAMETER);
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
            for (string &fkey : val.getMemberNames())
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
            LERROR << "parameter '" << key << "' has an invalid value, expected string or an object of key/value string pairs.";
            throw r_Error(INVALIDFORMATPARAMETER);
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
            LWARNING << "parameter '" << key << "' has an invalid value, it must contain a crs.";
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
                LWARNING << "parameter '" << keyBbox << "' has an invalid value, it must contain xmin, ymin, xmax and ymax parameters.";
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

const vector<string> &r_Format_Params::getVariables() const
{
    return variables;
}

vector<int> r_Format_Params::getBandIds() const
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
