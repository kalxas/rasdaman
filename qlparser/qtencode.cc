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
 * MERCHANTrABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
#include "raslib/rmdebug.hh"
#include "debug.hh"
#include <float.h>

#include "catalogmgr/typefactory.hh"
#include "qlparser/qtconversion.hh"
#include "qlparser/qtencode.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtmintervaldata.hh"
#include "raslib/error.hh"
#include "raslib/primitivetype.hh"
#include "raslib/structuretype.hh"
#include "catalogmgr/ops.hh"
#include "relcatalogif/basetype.hh"
#include "tilemgr/tile.hh"
#include "mddmgr/mddobj.hh"

// GDAL headers
#include "ogr_spatialref.h"
#include "cpl_conv.h"
#include "cpl_string.h"
#include "vrtdataset.h"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

#ifndef GDAL_PARAMS
#define GPDAL_PARAMS true

#define PARAM_XMIN "xmin"
#define PARAM_XMAX "xmax"
#define PARAM_YMIN "ymin"
#define PARAM_YMAX "ymax"

#define PARAM_CRS  "crs"
#define PARAM_METADATA "metadata"
#define PARAM_NODATA "nodata"
#define PARAM_CONFIG "config"

#define NODATA_VALUE_SEPARATOR " ,"
#define NODATA_DEFAULT_VALUE 0.0
#endif

const QtNode::QtNodeType QtEncode::nodeType = QtNode::QT_ENCODE;

QtEncode::QtEncode(QtOperation *mddOp, char* formatIn) throw (r_Error)
: QtUnaryOperation(mddOp), format(formatIn), fParams(NULL), builtinConvertor(NULL)
{
    GDALAllRegister();

    // If the format is not supported by GDAL, try to use builtin convertors
    r_Data_Format dataFormat = getDataFormat(format);
    if (dataFormat == r_CSV) {
        builtinConvertor = new QtConversion(mddOp, QtConversion::QT_TOCSV);
    }
}

QtEncode::QtEncode(QtOperation *mddOp, char* formatIn, char* paramsIn) throw (r_Error)
: QtUnaryOperation(mddOp), format(formatIn), builtinConvertor(NULL)
{
    GDALAllRegister();
    initParams(paramsIn);

    // If the format is not supported by GDAL, try to use builtin convertors
    r_Data_Format dataFormat = getDataFormat(format);
    if (dataFormat == r_CSV) {
        builtinConvertor = new QtConversion(mddOp, QtConversion::QT_TOCSV, paramsIn);
    }
}

QtEncode::~QtEncode()
{
  CSLDestroy(fParams);
  if (builtinConvertor) {
      delete builtinConvertor;
      builtinConvertor = NULL;
      input = NULL; // input is already freed in builtinConvertor
  }
}

QtData* QtEncode::evaluate(QtDataList* inputList) throw (r_Error)
{
    RMDBCLASS("QtEncode", "evaluate( QtDataList* )", "qlparser", __FILE__, __LINE__)
    ENTER("QtEncode::evaluate( QtDataList* )");
    startTimer("QtEncode");

    QtData* returnValue = NULL;
    QtData* operand = NULL;

    if (builtinConvertor)
    {
        returnValue = builtinConvertor->evaluate(inputList);
    }
    else
    {
        operand = input->evaluate(inputList);

        if (operand)
        {
#ifdef QT_RUNTIME_TYPE_CHECK
            if (operand->getDataType() != QT_MDD)
            {
                RMInit::logOut << "Internal error in QtEncode::evaluate() - "
                        << "runtime type checking failed (MDD)." << std::endl;

                // delete old operand
                if (operand) operand->deleteRef();
                return 0;
            }
#endif

            // Perform the actual evaluation
            QtMDD* qtMDD = (QtMDD*) operand;
            returnValue = evaluateMDD(qtMDD);

            // delete old operand
            if (operand) operand->deleteRef();
        }
        else
            RMInit::logOut << "Error: QtEncode::evaluate() - operand is not provided." << std::endl;
    }
    stopTimer();

    LEAVE("QtEncode::evaluate( QtDataList* )");
    return returnValue;
}

QtData* QtEncode::evaluateMDD(QtMDD* qtMDD) throw (r_Error)
{
    RMDBCLASS("QtEncode", "evaluateMDD( QtMDD* )", "qlparser", __FILE__, __LINE__)
    QtData* returnValue    = NULL;
    MDDObj* currentMDDObj  = qtMDD->getMDDObject();

    Tile*       sourceTile = NULL;
    vector< Tile* >* tiles = NULL;

    // get MDD tiles
    if (qtMDD->getLoadDomain().is_origin_fixed() && qtMDD->getLoadDomain().is_high_fixed())
    {
        // get relevant tiles
        tiles = currentMDDObj->intersect(qtMDD->getLoadDomain());
    }
    else
    {
        RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "evaluateMDD() - no tile available to encode.")
        return qtMDD;
    }
    if (!tiles->size())
    {
        RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "evaluateMDD() - no tile available to encode.")
        return qtMDD;
    }

    // create one single tile with the load domain
    sourceTile = new Tile(tiles, qtMDD->getLoadDomain());

    // delete the tile vector
    delete tiles;
    tiles = NULL;

    // get type structure of the operand base type
    char* typeStructure = qtMDD->getCellType()->getTypeStructure();

    // convert structure to r_Type
    r_Type* baseSchema = r_Type::get_any_type(typeStructure);
    r_Type* bandType   = NULL;
    free(typeStructure);
    typeStructure = NULL;

    // determine bands of MDD
    int numBands = 0;
    if (baseSchema->isPrimitiveType())   // = one band
    {
        RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "evaluateMDD() - encoding 1-band MDD.")
        numBands = 1;
        bandType = baseSchema;
    }
    else if (baseSchema->isStructType()) // = multiple bands
    {
        r_Structure_Type *myStruct = (r_Structure_Type*) baseSchema;
        r_Structure_Type::attribute_iterator iter(myStruct->defines_attribute_begin());
        while (iter != myStruct->defines_attribute_end())
        {
            numBands++;
            
            // check the band types, they have to be of the same type
            if ((*iter).type_of().isPrimitiveType())
            {
                r_Primitive_Type pt = (r_Primitive_Type&) (*iter).type_of();
                if (bandType != NULL)
                {
                    if (bandType->type_id() != pt.type_id())
                    {
                        RMInit::logOut << "QtEncode::evaluateMDD - Error: Can not handle bands of different types." << endl;
                        throw r_Error(r_Error::r_Error_General);
                    }
                }
                else {
                    bandType = pt.clone();
                }
            }
            else
            {
                RMInit::logOut << "QtEncode::evaluateMDD - Error: Can not handle composite bands." << endl;
                throw r_Error(r_Error::r_Error_General);
            }
            ++iter;
        }
        RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "evaluateMDD() - encoding " << numBands << "-band MDD.")
    }

    // Convert rasdaman tile to GDAL format
    GDALDataset* gdalSource = convertTileToDataset(sourceTile, numBands, bandType);
    
    // delete base type schema
    delete baseSchema;
    baseSchema = NULL;
    
    if (gdalSource == NULL)
    {
        RMInit::logOut << "QtEncode::evaluateMDD - Error: Could not convert tile to a GDAL dataset. " << endl;
        throw r_Error(r_Error::r_Error_General);
    }

    // get the right GDAL driver for the target format
    RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "evaluateMDD() - encoding to format " << format)
    GDALAllRegister();
    GDALDriver *driver = GetGDALDriverManager()->GetDriverByName(format);
    if (driver == NULL)
    {
        RMInit::logOut << "QtEncode::evaluateMDD - Error: Unsupported format: " << format << endl;
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
    }

    // temporary file to which the encoded result will be written
    const char TMPFILE_TEMPLATE[] = "/tmp/rasdaman-XXXXXX";
    char tmpFileName[sizeof(TMPFILE_TEMPLATE)];
    strcpy(tmpFileName, TMPFILE_TEMPLATE);
    
    int fd = mkstemp(tmpFileName);
    if (fd < 1)
    {
        RMInit::logOut << "QtEncode::evaluateMDD - Error: Creation of temp file failed with error:\n" << strerror(errno) << endl;
        throw r_Error(r_Error::r_Error_General);
    }

    // encode the MDD, writing result to a tmp file
    GDALDataset* gdalResult = driver->CreateCopy(tmpFileName, gdalSource, FALSE, fParams, NULL, NULL);
    if (!gdalResult)
    {
        RMInit::logOut << "QtEncode::evaluateMDD - Error: Could not convert MDD to format " << format << endl;
        throw r_Error(r_Error::r_Error_General);
    }

    GDALClose(gdalResult);

    //
    // read file back and return as a tile
    //
    FILE* fileD = fopen(tmpFileName, "r");
    if (fileD == NULL)
    {
        RMInit::logOut << "QtEncode::evaluateMDD - Error: failed opening temporary file:\n" << strerror(errno) << endl;
        throw r_Error(r_Error::r_Error_General);
    }

    fseek(fileD, 0, SEEK_END);
    long size = ftell(fileD);
    r_Char* fileContents = NULL;
    try
    {
        fileContents = new r_Char[size];
    }
    catch (std::bad_alloc)
    {
        RMInit::logOut << "QtEncode::evaluateMDD - Error: Unable to claim memory: " << size << " Bytes" << endl;
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }

    fseek(fileD, 0, SEEK_SET);
    fread(fileContents, 1, size, fileD);
    fclose(fileD);
    unlink(tmpFileName);

    // result domain: it is now format encoded so we just consider it as a char array
    r_Minterval mddDomain = r_Minterval(1) << r_Sinterval((r_Range) 0, (r_Range) size - 1);
    r_Type* type = r_Type::get_any_type("char");
    const BaseType* baseType = TypeFactory::mapType(type->name());
    
    Tile *resultTile = new Tile(mddDomain, baseType, (char*) fileContents, size, getDataFormat(format));
    RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "evaluateMDD() - Created result tile of size " << size)

    // create a transient MDD object for the query result
    MDDBaseType* mddBaseType = new MDDBaseType("tmp", baseType);
    TypeFactory::addTempType(mddBaseType);
    MDDObj* resultMDD = new MDDObj(mddBaseType, resultTile->getDomain());
    resultMDD->insertTile(resultTile);
    RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "evaluateMDD() - Created transient MDD object")

    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD((MDDObj*) resultMDD);

    return returnValue;
}

void
QtEncode::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(tab).c_str() << "QtEncode Object: to " << format << getEvaluationTime() << endl;

    QtUnaryOperation::printTree(tab, s, mode);
}

GDALDataset* QtEncode::convertTileToDataset(Tile* tile, int nBands, r_Type* bandType)
{
    RMDBCLASS("QtEncode", "convertTileToDataset( Tile*, int, r_Type*  )", "qlparser", __FILE__, __LINE__)

    r_Bytes   typeSize = ((r_Primitive_Type*) bandType)->size();
    bool  isNotBoolean = ((r_Primitive_Type*) bandType)->type_id() != r_Type::BOOL;
    r_Minterval domain = tile->getDomain();
    if (domain.dimension() != 2)
    {
        // FIXME: some formats allow higher dimensionality, netCDF, HDF, etc.
        RMInit::logOut << "QtEncode::convertTileToDataset - Error: only 2D data can be encoded with GDAL." << endl;
        return NULL;
    }
    int  width = domain[0].high() - domain[0].low() + 1;
    int height = domain[1].high() - domain[1].low() + 1;
    
    RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "convertTileToDataset() - Converting tile of "
            << width << " x " << height << " x " << typeSize)

    /* Create a in-memory dataset */
    GDALDriver *hMemDriver = (GDALDriver*) GDALGetDriverByName("MEM");
    if (hMemDriver == NULL)
    {
        RMInit::logOut << "QtEncode::convertTileToDataset - Error: Could not init GDAL driver. " << endl;
        return NULL;
    }

    // convert rasdaman type to GDAL type
    GDALDataType gdalBandType = getGdalType(bandType);
    
    GDALDataset *hMemDS = hMemDriver->Create("in_memory_image", width, height, nBands, gdalBandType, NULL);
    RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "convertTileToDataset() - Created in-memory GDAL dataset")

    char* tileCells = tile->getContents();
    char* datasetCells = (char*) malloc(typeSize * height * width);
    char* dst;
    char* src;
    int col_offset, band_offset;
    if (datasetCells == NULL)
    {
        RMInit::logOut << "QtEncode::convertTileToDataset - Error: Could not allocate memory. " << endl;
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    RMDBGMIDDLE(2, RMDebug::module_qlparser, "QtEncode", "convertTileToDataset() - Allocated " << (typeSize * height * width) << " bytes for the dataset")

    // for all bands, convert data from column-major form (from Rasdaman) to row-major form (GDAL)
    // and then write the data to GDAL datasets
    for (int band = 0; band < nBands; band++)
    {
        dst = (char*) datasetCells;
        band_offset = band * typeSize;
        
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++, dst+=typeSize)
            {
                col_offset = ((row + height * col) * nBands * typeSize + band_offset);
                src = tileCells + col_offset;
                if (isNotBoolean)
                {
                    memcpy(dst, src, typeSize);
                }
                else
                {
                    if (src[0] == 1)
                        dst[0] = 255;
                    else
                        dst[0] = 0;
                }
            }

        CPLErr error =
                hMemDS->GetRasterBand(band + 1)->
                            RasterIO(GF_Write, 0, 0, width, height, datasetCells,
                                                     width, height, gdalBandType, 0, 0);
        if (error != CE_None)
        {
            RMInit::logOut << "QtEncode::convertTileToDataset - Error: Could not write data to GDAL raster band " << band << endl;
            free(datasetCells);
            return NULL;
        }
    }
    
    // set parameters
    setGDALParameters(hMemDS, width, height, nBands);

    free(datasetCells);
    return hMemDS;
}

GDALDataType
QtEncode::getGdalType(r_Type* rasType)
{    
    GDALDataType ret = GDT_Unknown;
    switch (rasType->type_id())
    {
    case r_Type::BOOL:
        ret = GDT_Byte;
        break;
    case r_Type::CHAR:
        ret = GDT_Byte;
        break;
    case r_Type::USHORT:
        ret = GDT_UInt16;
        break;
    case r_Type::SHORT:
        ret = GDT_Int16;
        break;
    case r_Type::ULONG:
        ret = GDT_UInt32;
        break;
    case r_Type::LONG:
        ret = GDT_Int32;
        break;
    case r_Type::FLOAT:
        ret = GDT_Float32;
        break;
    case r_Type::DOUBLE:
        ret = GDT_Float64;
        break;
    case r_Type::COMPLEXTYPE1:
        ret = GDT_CFloat32;
        break;
    case r_Type::COMPLEXTYPE2:
        ret = GDT_CFloat64;
        break;
    default:
        RMInit::logOut << "Error: Unable to convert rasdaman type " << 
                rasType->name() << " to GDAL type." << endl;
        throw r_Error(r_Error::r_Error_General);
    }
    return ret;
}

#ifndef STR_EQUAL
#define STR_EQUAL(a, b) (strcmp(a, b) == 0)
#endif

r_Data_Format
QtEncode::getDataFormat(char* format)
{
	r_Data_Format ret = r_Array;

	if (format)
	{
		char* f = strdup(format);
		for (int i = 0; format[i]; i++)
		{
			if (isalpha(format[i]))
				f[i] = tolower(format[i]);
		}

		if (STR_EQUAL(f, "png"))
			ret = r_PNG;
		else if (STR_EQUAL(f, "netcdf"))
			ret = r_NETCDF;
		else if (STR_EQUAL(f, "gtiff") || STR_EQUAL(f, "tiff"))
			ret = r_TIFF;
		else if (STR_EQUAL(f, "jpeg"))
			ret = r_JPEG;
		else if (STR_EQUAL(f, "jpeg2000") || STR_EQUAL(f, "jp2openjpeg"))
			ret = r_JP2;
		else if (STR_EQUAL(f, "nitf"))
			ret = r_NTF;
		else if (STR_EQUAL(f, "hdf") || STR_EQUAL(f, "hdf4") || STR_EQUAL(f, "hdf4image") || STR_EQUAL(f, "hdf5"))
			ret = r_HDF;
		else if (STR_EQUAL(f, "bmp"))
			ret = r_BMP;
		else if (STR_EQUAL(f, "csv"))
			ret = r_CSV;
	}
	return ret;
}

QtNode::QtAreaType
QtEncode::getAreaType()
{
    return QT_AREA_MDD;
}

const QtTypeElement&
QtEncode::checkType(QtTypeTuple* typeTuple)
{
    RMDBCLASS("QtEncode", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__)

    if (builtinConvertor)
        return builtinConvertor->checkType(typeTuple);

    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input types
        const QtTypeElement& inputType = input->checkType(typeTuple);

        RMDBGIF(3, RMDebug::module_qlparser, "QtEncode", \
                RMInit::dbgOut << "Class..: QtEncode" << endl; \
                RMInit::dbgOut << "Operand: " << flush; \
                inputType.printStatus(RMInit::dbgOut); \
                RMInit::dbgOut << endl;)

        if (inputType.getDataType() != QT_MDD)
        {
            RMInit::logOut << "Error: QtEncode::evaluate() - operand must be an MDD." << endl;
            parseInfo.setErrorNo(353);
            throw parseInfo;
        }

        dataStreamType.setDataType(QT_MDD);
    }
    else
        RMInit::logOut << "Error: QtEncode::checkType() - operand branch invalid." << endl;

    return dataStreamType;
}

const QtNode::QtNodeType
QtEncode::getNodeType() const
{
  return nodeType;
}

void
QtEncode::initParams(char* paramsIn)
{
	// replace escaped characters
	string params("");
	int i = 0;
	while (paramsIn[i] != '\0')
	{
		char curr = paramsIn[i];
		char next = paramsIn[i + 1];
		++i;

		if (curr == '\\' && (next == '"' || next == '\'' || next == '\\'))
			continue;
		params += curr;
	}

	fParams = CSLTokenizeString2(params.c_str(), ";",
			CSLT_STRIPLEADSPACES |
			CSLT_STRIPENDSPACES);

	setDouble(PARAM_XMIN, &gParams.xmin);
	setDouble(PARAM_XMAX, &gParams.xmax);
	setDouble(PARAM_YMIN, &gParams.ymin);
	setDouble(PARAM_YMAX, &gParams.ymax);
	setString(PARAM_CRS, &gParams.crs);
	setString(PARAM_METADATA, &gParams.metadata);

        // GDAL configuration options (config="key1 value1, key2 value2, ...")
        int ind;
        if ((ind = CSLFindName(fParams, PARAM_CONFIG)) != -1)
        {
            RMInit::logOut << "Found GDAL configuration parameters." << endl;
            // parse the KV-pairs and set them to GDAL environment
            const char* kvPairs = CSLFetchNameValue(fParams, PARAM_CONFIG);
            RMInit::logOut << " KV-PAIRS = '" << kvPairs << "'" << endl;
            char** kvPairsList =  CSLTokenizeString2(kvPairs, ",",
			CSLT_STRIPLEADSPACES |
			CSLT_STRIPENDSPACES);
            for (int iKvPair = 0; iKvPair < CSLCount(kvPairsList); iKvPair++)
            {
               // foreach KV pair in confParamList DO CPLSetConfigOption
               const char* kvPair = kvPairsList[iKvPair];
               char** kvPairList =  CSLTokenizeString2(kvPair, " ",
			CSLT_STRIPLEADSPACES |
			CSLT_STRIPENDSPACES);
               CPLString* keyString = new CPLString((const char*)(kvPairList[0]));
               CPLString* valueString = new CPLString((const char*)(kvPairList[1]));
               const char* confKey = keyString->c_str();
               const char* confValue = valueString->c_str();
               RMInit::logOut << " KEY = '" << confKey << "' VALUE ='" << confValue << "'" << endl;
               CPLSetConfigOption(confKey, confValue); // this option is then read by the CreateCopy() method of the GDAL format
            }
        }

	string nodata;
	setString(PARAM_NODATA, &nodata);

	if (!nodata.empty())
	{
		char* pch = (char*) nodata.c_str();
		pch = strtok(pch, NODATA_VALUE_SEPARATOR);
		while (pch != NULL)
		{
			double value = strtod(pch, NULL);
			gParams.nodata.push_back(value);
			pch = strtok(NULL, NODATA_VALUE_SEPARATOR);
		}
	}
}

void
QtEncode::setDouble(const char* paramName, double* value)
{
	int ind;
	if ((ind = CSLFindName(fParams, paramName)) != -1)
		*value = strtod(CSLFetchNameValue(fParams, paramName), NULL);
	else
		*value = DBL_MAX;
}

void
QtEncode::setString(const char* paramName, string* value)
{
	int ind;
	if ((ind = CSLFindName(fParams, paramName)) != -1)
		*value = CSLFetchNameValue(fParams, paramName);
	else
		*value = "";
}

void
QtEncode::setGDALParameters(GDALDataset *gdalDataSet, int width, int height, int nBands)
{
	if (gParams.xmin != DBL_MAX && gParams.xmax != DBL_MAX && gParams.ymin != DBL_MAX && gParams.ymax != DBL_MAX)
	{
		double adfGeoTransform[6];
		adfGeoTransform[0] = gParams.xmin;
		adfGeoTransform[1] = (gParams.xmax - gParams.xmin) / width;
		adfGeoTransform[2] = 0.0;
		adfGeoTransform[3] = gParams.ymax;
		adfGeoTransform[4] = 0.0;
		adfGeoTransform[5] = -(gParams.ymax - gParams.ymin) / height;
		gdalDataSet->SetGeoTransform(adfGeoTransform);
	}

	if (gParams.crs != "")
	{
		OGRSpatialReference srs;

		// setup input coordinate system. Try import from EPSG, Proj.4, ESRI and last, from a WKT string
		const char *crs = gParams.crs.c_str();
		char *wkt = NULL;

		OGRErr err = srs.SetFromUserInput(crs);
		if (err != OGRERR_NONE)
		{
			RMInit::logOut << "QtEncode::convertTileToDataset - Warning: GDAL could not understand coordinate reference system: '" << crs << "'" << endl;
		} else
		{
			srs.exportToWkt(&wkt);
			gdalDataSet->SetProjection(wkt);
		}
	}

	if (gParams.metadata != "")
	{
		char** metadata = NULL;
		metadata = CSLAddNameValue(metadata, "metadata", gParams.metadata.c_str());
		gdalDataSet->SetMetadata(metadata);
	}


	// set nodata value
	if (gParams.nodata.empty())
	{
		// if no nodata is specified, set default -- DM 2013-oct-01, ticket 477
		gParams.nodata.push_back(NODATA_DEFAULT_VALUE);
	}
	if (gParams.nodata.size() > 0)
	{
		for (int band = 0; band < nBands; band++)
		{
			GDALRasterBand* rasterBand = gdalDataSet->GetRasterBand(band + 1);

			// if only one value is provided use the same for all bands
			if (gParams.nodata.size() == 1)
			{
				rasterBand->SetNoDataValue(gParams.nodata.at(0));
			} else if (gParams.nodata.size() == nBands)
			{
				rasterBand->SetNoDataValue(gParams.nodata.at(band));
			} else
			{
				// warning, nodata value no != band no -- DM 2012-dec-10
				RMInit::logOut << "Warning: ignored setting NODATA value, number of NODATA values (" <<
						gParams.nodata.size() << ") doesn't match the number of bands (" << nBands << ")." << endl;
				break;
			}
		}
	}
}
