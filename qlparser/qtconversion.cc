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
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#include "config.h"
#include "qlparser/qtconversion.hh"
#include "conversion/convertor.hh"
#include "conversion/convfactory.hh"
#include "conversion/mimetypes.hh"
#include "conversion/convutil.hh"
#include "raslib/basetype.hh"
#include "qlparser/qtmdd.hh"
#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/structtype.hh"
#include "relcatalogif/chartype.hh"

#include <easylogging++.h>
#include <boost/algorithm/string.hpp>
#include <iostream>
#include <string>

using namespace std;

class ConvUtil;

const QtNode::QtNodeType QtConversion::nodeType = QtNode::QT_CONVERSION;

QtConversion::QtConversion(QtOperation* newInput, QtConversionType
                           newConversionType, const char* paramStrArg)
    : QtUnaryOperation(newInput), conversionType(newConversionType), paramStr(paramStrArg), gdalConversion(false)
{
}

QtConversion::QtConversion(QtOperation* newInput, QtConversionType
                           newConversionType, const std::string& formatArg, const char* paramStrArg)
    : QtUnaryOperation(newInput), conversionType(newConversionType), format(formatArg), paramStr(paramStrArg), gdalConversion(true)
{
    if (r_MimeTypes::isMimeType(format))
    {
        format = r_MimeTypes::getFormatName(format);
    }
    r_Data_Format dataFormat = ConvUtil::getDataFormat(format);
    if (isInternalFormat(dataFormat))
    {
        gdalConversion = false;
        string internalFormat;
        if (conversionType == QT_FROMGDAL)
        {
            internalFormat = "inv_" + format;
        }
        else
        {
            internalFormat = format;
        }
        setConversionTypeByName(internalFormat);
    }
}

bool QtConversion::isInternalFormat(r_Data_Format dataFormat)
{
    bool ret = dataFormat == r_CSV;
    ret = ret || dataFormat == r_JSON;
#ifdef HAVE_NETCDF
    ret = ret || dataFormat == r_NETCDF;
#endif
#ifdef HAVE_GRIB
    if (conversionType == QT_FROMGDAL) // QT_TOGRIB isn't implemented, so in this case try with GDAL
    {
        ret = ret || dataFormat == r_GRIB;
    }
#endif
    return ret;
}

void
QtConversion::setConversionTypeByName(string formatName)
{
    boost::algorithm::to_lower(formatName);
    if (string("hdf") == formatName)
    {
        conversionType = QtConversion::QT_TOHDF;
    }
    else if (string("tiff") == formatName)
    {
        conversionType = QtConversion::QT_TOTIFF;
    }
    else if (string("csv") == formatName)
    {
        conversionType = QtConversion::QT_TOCSV;
    }
    else if (string("json") == formatName)
    {
        conversionType = QtConversion::QT_TOJSON;
    }
    else if (string("dem") == formatName)
    {
        conversionType = QtConversion::QT_TODEM;
    }
    else if (string("netcdf") == formatName)
    {
        conversionType = QtConversion::QT_TONETCDF;
    }
    else if (string("gdal") == formatName)
    {
        conversionType = QtConversion::QT_TOGDAL;
    }
    else if (string("inv_hdf") == formatName)
    {
        conversionType = QtConversion::QT_FROMHDF;
    }
    else if (string("inv_csv") == formatName)
    {
        conversionType = QtConversion::QT_FROMCSV;
    }
    else if (string("inv_json") == formatName)
    {
        conversionType = QtConversion::QT_FROMJSON;
    }
    else if (string("inv_tiff") == formatName)
    {
        conversionType = QtConversion::QT_FROMTIFF;
    }
    else if (string("inv_dem") == formatName)
    {
        conversionType = QtConversion::QT_FROMDEM;
    }
    else if (string("inv_netcdf") == formatName)
    {
        conversionType = QtConversion::QT_FROMNETCDF;
    }
    else if (string("inv_grib") == formatName)
    {
        conversionType = QtConversion::QT_FROMGRIB;
    }
    else if (string("inv_gdal") == formatName)
    {
        conversionType = QtConversion::QT_FROMGDAL;
    }
    else
    {
        conversionType = QtConversion::QT_UNKNOWN;
    }
}

QtData*
QtConversion::evaluate(QtDataList* inputList)
{
    startTimer("QtConversion");

    QtData* returnValue = NULL;
    QtData* operand = NULL;
    r_Minterval* nullValues = NULL;

    if (conversionType == QT_UNKNOWN)
    {
        LFATAL << "Unknown conversion format.";
        parseInfo.setErrorNo(382);
        throw parseInfo;
    }

    operand = input->evaluate(inputList);
    if (operand)
    {
        char* typeStructure = NULL;
        unique_ptr<Tile> sourceTile = NULL;

        if ((conversionType == QT_TOCSV || conversionType == QT_TOJSON) && operand->isScalarData())
        {
            QtScalarData* qtScalar = static_cast<QtScalarData*>(operand);
            r_Minterval domain = r_Minterval(2) << r_Sinterval(0LL, 0LL) << r_Sinterval(0LL, 0LL);
            sourceTile.reset(new Tile(domain, qtScalar->getValueType(), qtScalar->getValueBuffer(), (r_Bytes)0, r_Array));
            typeStructure = qtScalar->getTypeStructure();
        }
        else
        {
#ifdef QT_RUNTIME_TYPE_CHECK
            if (operand->getDataType() != QT_MDD)
            {
                LERROR << "Internal error in QtConversion::evaluate() - "
                       << "runtime type checking failed (MDD).";

                // delete old operand
                if (operand)
                {
                    operand->deleteRef();
                }
                return 0;
            }
#endif

            QtMDD* qtMDD = static_cast<QtMDD*>(operand);
            MDDObj* currentMDDObj = qtMDD->getMDDObject();
            nullValues = currentMDDObj->getNullValues();
            vector<boost::shared_ptr<Tile>>* tiles = NULL;
            if (qtMDD->getLoadDomain().is_origin_fixed() && qtMDD->getLoadDomain().is_high_fixed())
            {
                // get relevant tiles
                tiles = currentMDDObj->intersect(qtMDD->getLoadDomain());
            }
            if (!tiles || tiles->empty())
            {
                LERROR << "no tile available to convert.";
                throw r_Error(r_Error::r_Error_General);
            }

            // create one single tile with the load domain
            sourceTile.reset(new Tile(tiles, qtMDD->getLoadDomain()));
            delete tiles;
            tiles = NULL;

            // get type structure of the operand base type
            typeStructure = qtMDD->getCellType()->getTypeStructure();
        }

        // convert structure to r_Type
        unique_ptr<r_Type> baseSchema(r_Type::get_any_type(typeStructure));
        free(typeStructure);
        typeStructure = NULL;

        //
        // real conversion
        //

        r_Conv_Desc convDesc;
        r_Minterval tileDomain = sourceTile->getDomain();

        r_Data_Format convType = r_Array;   // convertor type
        r_Data_Format convFormat = r_Array; // result type from convertor
        setConversionTypeAndResultFormat(convType, convFormat);

        try
        {
            unique_ptr<r_Convertor> convertor;
            convertor.reset(r_Convertor_Factory::create(convType, sourceTile->getContents(), tileDomain, baseSchema.get()));
            if (gdalConversion)
            {
                convertor->set_format(format);
            }
            if (conversionType < QT_FROMTIFF)
            {
                LDEBUG << "convertor '" << convType << "' converting to format '" << format << "'.";
                convDesc = convertor->convertTo(paramStr);
            }
            else
            {
                LDEBUG << "convertor '" << convType << "' converting from format '" << format << "'.";
                convDesc = convertor->convertFrom(paramStr);
            }
        }
        catch (r_Error& err)
        {
            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }

            if (err.get_kind() == r_Error::r_Error_FeatureNotSupported)
            {
                parseInfo.setErrorNo(218);
            }
            else
            {
                parseInfo.setErrorNo(381);
            }
            throw parseInfo;
        }

        //
        // done
        //

        sourceTile.reset();

        // create a transient tile for the compressed data
        const BaseType* baseType = rasTypeToBaseType(convDesc.destType);

        // here we have to update the dataStreamType.getType(), as it has changed since checkType
        if (strcasecmp(dataStreamType.getType()->getTypeName(), baseType->getTypeName()))
        {
            MDDBaseType* mddBaseType = new MDDBaseType("tmp", baseType);
            TypeFactory::addTempType(mddBaseType);
            dataStreamType.setType(mddBaseType);
#ifdef DEBUG
            LDEBUG << " QtConversion::evaluate() for conversion " << conversionType << " real result is ";
            dataStreamType.printStatus(RMInit::logOut);
#endif
        }

        r_Bytes convResultSize = static_cast<r_Bytes>(convDesc.destInterv.cell_count()) * static_cast<r_Bytes>(baseType->getSize());

        Tile* resultTile = new Tile(convDesc.destInterv, baseType, true, convDesc.dest, convResultSize, convFormat);

        // delete destination type
        if (convDesc.destType)
        {
            delete convDesc.destType;
            convDesc.destType = NULL;
        }

        // create a transient MDD object for the query result
        MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(const_cast<Type*>(dataStreamType.getType()));
        MDDObj* resultMDD = new MDDObj(mddBaseType, convDesc.destInterv, nullValues);
        resultMDD->insertTile(resultTile);

        // create a new QtMDD object as carrier object for the transient MDD object
        returnValue = new QtMDD(resultMDD);
        (static_cast<QtMDD*>(returnValue))->setFromConversion(true);

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }
    }
    else
    {
        LERROR << "operand is not provided.";
    }

    stopTimer();

    return returnValue;
}

const BaseType* QtConversion::rasTypeToBaseType(r_Type* type)
{
    const BaseType* result = NULL;
    if (type->isPrimitiveType())
    {
        result = TypeFactory::mapType(type->name());
        if (!result)
        {
            LFATAL << "no base type for ODMG primitive type '"
                   << type->name() << "' was found";
            throw r_Error(BASETYPENOTSUPPORTED);
        }
    }
    else if (type->isStructType())
    {
        r_Structure_Type* structType = static_cast<r_Structure_Type*>(const_cast<r_Type*>(type));
        StructType* restype = new StructType("tmp_struct_type", structType->count_elements());
        r_Structure_Type::attribute_iterator iter(structType->defines_attribute_begin());
        while (iter != structType->defines_attribute_end())
        {
            try
            {
                r_Attribute attr = (*iter);
                const r_Base_Type& attr_type = attr.type_of();
                restype->addElement(attr.name(), rasTypeToBaseType((r_Type*) & attr_type));
            }
            catch (r_Error& e)
            {
                LERROR << "failed converting band type: " << e.what();
                delete restype;
                throw;
            }
            ++iter;
        }
        TypeFactory::addTempType(restype);
        result = restype;
    }
    return result;
}

void
QtConversion::setConversionTypeAndResultFormat(r_Data_Format& convType, r_Data_Format& convFormat)
{
    switch (conversionType)
    {
    case QT_TOTIFF:
        convType = r_TIFF;
        convFormat = r_TIFF;
        break;
    case QT_FROMTIFF:
        convType = r_TIFF;
        convFormat = r_Array;
        break;
    case QT_TOHDF:
        convType = r_HDF;
        convFormat = r_HDF;
        break;
    case QT_TONETCDF:
        convType = r_NETCDF;
        convFormat = r_NETCDF;
        break;
    case QT_TOGDAL:
        convType = r_GDAL;
        convFormat = ConvUtil::getDataFormat(format);
        break;
    case QT_TOCSV:
        convType = r_CSV;
        convFormat = r_CSV;
        break;
    case QT_TOJSON:
        convType = r_JSON;
        convFormat = r_JSON;
        break;
    case QT_FROMHDF:
        convType = r_HDF;
        convFormat = r_Array;
        break;
    case QT_FROMNETCDF:
        convType = r_NETCDF;
        convFormat = r_Array;
        break;
    case QT_FROMGDAL:
        convType = r_GDAL;
        convFormat = ConvUtil::getDataFormat(format);
        break;
    case QT_FROMGRIB:
        convType = r_GRIB;
        convFormat = r_Array;
        break;
    case QT_FROMCSV:
        convType = r_CSV;
        convFormat = r_Array;
        break;
    case QT_FROMJSON:
        convType = r_JSON;
        convFormat = r_Array;
        break;
    case QT_TODEM:
        convType = r_DEM;
        convFormat = r_DEM;
        break;
    case QT_FROMDEM:
        convType = r_DEM;
        convFormat = r_Array;
        break;
    default:
        LFATAL << "Error: QtConversion::evaluate(): unsupported format " << conversionType;
        throw r_Error(CONVERSIONFORMATNOTSUPPORTED);
        break;
    }
}

bool
QtConversion::equalMeaning(QtNode* node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtConversion* convNode;
        convNode = static_cast<QtConversion*>(node);  // by force

        result = input->equalMeaning(convNode->getInput());

        result = result && conversionType == convNode->conversionType;
    };

    return (result);
}

void
QtConversion::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtConversion Object: ";

    switch (conversionType)
    {
    case QT_TOTIFF:
        s << "to TIFF";
        break;
    case QT_TOHDF:
        s << "to HDF";
        break;
    case QT_TONETCDF:
        s << "to NETCDF";
        break;
    case QT_TOGDAL:
        s << "to GDAL (" << format << ")";
        break;
    case QT_TOCSV:
        s << "to CSV";
        break;
    case QT_TOJSON:
        s << "to JSON";
        break;
    case QT_TODEM:
        s << "to DEM";
        break;
    case QT_FROMTIFF:
        s << "from TIFF";
        break;
    case QT_FROMHDF:
        s << "from HDF";
        break;
    case QT_FROMNETCDF:
        s << "from NETCDF";
        break;
    case QT_FROMGDAL:
        s << "from GDAL (" << format << ")";
        break;
    case QT_FROMGRIB:
        s << "from GRIB";
        break;
    case QT_FROMCSV:
        s << "from CSV";
        break;
    case QT_FROMJSON:
        s << "from JSON";
        break;
    case QT_FROMDEM:
        s << "from DEM";
        break;
    default:
        s << "unknown conversion";
        break;
    }

    s << getEvaluationTime();
    s << std::endl;

    QtUnaryOperation::printTree(tab, s, mode);
}

void
QtConversion::printAlgebraicExpression(ostream& s)
{
    s << conversionType << "(";

    if (input)
    {
        input->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}

const QtTypeElement&
QtConversion::checkType(QtTypeTuple* typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input type
        const QtTypeElement& inputType = input->checkType(typeTuple);

        if (conversionType != QT_TOCSV && inputType.getDataType() != QT_MDD)
        {
            LFATAL << "expected MDD operand in conversion operation.";
            parseInfo.setErrorNo(380);
            throw parseInfo;
        }

        //FIXME we set for every kind of conversion the result type char
        //for conversion from_DEF we don't know the result type until we parse the data
        MDDBaseType* mddBaseType = new MDDBaseType("Char", TypeFactory::mapType("Char"));
        TypeFactory::addTempType(mddBaseType);

        dataStreamType.setType(mddBaseType);
    }
    else
    {
        LERROR << "operand branch invalid.";
    }

    return dataStreamType;
}

std::ostream&
operator<<(std::ostream& os, QtConversion::QtConversionType type)
{
    switch (type)
    {
    case QtConversion::QT_TOTIFF:
        os << "tiff";
        break;
    case QtConversion::QT_TOHDF:
        os << "hdf";
        break;
    case QtConversion::QT_TOCSV:
        os << "csv";
        break;
    case QtConversion::QT_TOJSON:
        os << "json";
        break;
    case QtConversion::QT_TODEM:
        os << "dem";
        break;
    case QtConversion::QT_TONETCDF:
        os << "netcdf";
        break;
    case QtConversion::QT_TOGDAL:
        os << "gdal";
        break;
    case QtConversion::QT_FROMTIFF:
        os << "inv_tiff";
        break;
    case QtConversion::QT_FROMHDF:
        os << "inv_hdf";
        break;
    case QtConversion::QT_FROMCSV:
        os << "inv_csv";
        break;
    case QtConversion::QT_FROMJSON:
        os << "inv_json";
        break;
    case QtConversion::QT_FROMDEM:
        os << "inv_dem";
        break;
    case QtConversion::QT_FROMNETCDF:
        os << "inv_netcdf";
        break;
    case QtConversion::QT_FROMGDAL:
        os << "inv_gdal";
        break;
    case QtConversion::QT_FROMGRIB:
        os << "inv_grib";
        break;
    default:
        os << "unknown Conversion";
        break;
    }

    return os;
}

