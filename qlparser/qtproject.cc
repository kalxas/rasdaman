/*************************************************************
 *
 * SOURCE: qtproject.cc
 *
 * MODULE: qlparser
 * CLASS:  QtProject
 *
 * PURPOSE: Represent a (coordinate system) Projection operation.
 *
 * CHANGE HISTORY (append further entries):
 * when         who                what
 * ----------------------------------------------------------
 * 2010-01-31   Aiordachioaie      created
 *
 * COMMENTS:
 *
 * Copyright (C) 2010 Dr. Peter Baumann
 *
 ************************************************************/
#include "raslib/rmdebug.hh"
#include "debug.hh"

#include "config.h"
#include "qlparser/qtproject.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtmintervaldata.hh"
#include "raslib/error.hh"
#include "raslib/type.hh"
#include "raslib/primitivetype.hh"
#include "raslib/structuretype.hh"
#include "catalogmgr/ops.hh"
#include "conversion/convutil.hh"
#include "relcatalogif/basetype.hh"
#include "tilemgr/tile.hh"
#include "mddmgr/mddobj.hh"


#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

const QtNode::QtNodeType QtProject::nodeType = QtNode::QT_PROJECT;

QtProject::QtProject(QtOperation* mddOpArg, const char* initBounds, const char* crsIn, const char* crsOut) throw (r_Error)
    : QtUnaryOperation(mddOpArg), xmin(-1), ymin(-1), xmax(-1), ymax(-1), wktCrsIn(NULL), wktCrsOut(NULL)
{
    initialBounds = std::string(initBounds);
    initialCrsIn = std::string(crsIn);
    initialCrsOut = std::string(crsOut);
#ifdef HAVE_GDAL
    GDALAllRegister();
    parseNumbers(initBounds);
    testCrsTransformation(crsIn, crsOut);
#endif
}

QtProject::QtProject(QtOperation* mddOpArg, const char* crsIn, const char* crsOut) throw (r_Error)
    : QtUnaryOperation(mddOpArg), xmin(-1), ymin(-1), xmax(-1), ymax(-1), wktCrsIn(NULL), wktCrsOut(NULL)
{
    initialCrsIn = std::string(crsIn);
    initialCrsOut = std::string(crsOut);
#ifdef HAVE_GDAL
    GDALAllRegister();
    testCrsTransformation(crsIn, crsOut);
#endif
}

QtProject::~QtProject()
{
}

#ifdef HAVE_GDAL
void QtProject::parseNumbers(const char* str) throw (r_Error)
{
    char* split = strtok(const_cast<char*>(str), ", ");
    xmin = parseOneNumber(split);
    split = strtok(NULL, ", ");
    ymin = parseOneNumber(split);
    split = strtok(NULL, ", ");
    xmax = parseOneNumber(split);
    split = strtok(NULL, ", ");
    ymax = parseOneNumber(split);
}

float QtProject::parseOneNumber(char* str) throw (r_Error)
{
    char* end;
    float f = strtof(str, &end);
    if (end != strlen(str) + str)
    {
        LERROR << "Invalid number as project bounds: '" << str << "'";
        throw r_Error(r_Error::r_Error_InvalidBoundsStringContents);
    }
    return f;
}

void QtProject::testCrsTransformation(const char* in, const char* out) throw (r_Error)
{
    if (setCrsWKT(in, wktCrsIn) == false)
    {
        LERROR << "Input string '" << in << "' is not a valid Coordinate Reference System that GDAL can understand";
        throw r_Error(r_Error::r_Error_InvalidSourceCRS);
    }
    if (setCrsWKT(out, wktCrsOut) == false)
    {
        LERROR << "Input string '" << out << "' is not a valid Coordinate Reference System that GDAL can understand";
        throw r_Error(r_Error::r_Error_InvalidTargetCRS);
    }
}
#endif // HAVE_GDAL

QtData* QtProject::evaluate(QtDataList* inputList)
{

    QtData* returnValue = NULL;
    QtData* operand = NULL;

    operand = input->evaluate(inputList);

    if (operand)
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand->getDataType() != QT_MDD)
        {
            LERROR << "runtime type checking failed (not an MDD).";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }
            return 0;
        }
#endif

        // Perform the actual evaluation
        QtMDD*  qtMDD         = (QtMDD*) operand;
        returnValue = evaluateMDD(qtMDD);

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }

        return returnValue;
    }
    else
    {
        LERROR << "operand is not provided.";
    }

    return returnValue;
}


QtNode::QtAreaType
QtProject::getAreaType()
{
    return QT_AREA_MDD;
}


const QtTypeElement&
QtProject::checkType(QtTypeTuple* typeTuple)
{
#ifdef HAVE_GDAL

    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input types
        const QtTypeElement& inputType = input->checkType(typeTuple);
        if (inputType.getDataType() != QT_MDD)
        {
            LERROR << "operand must be multidimensional.";
            parseInfo.setErrorNo(353);
            throw parseInfo;
        }

        dataStreamType.setDataType(QT_MDD);
        dataStreamType.setType(const_cast<Type*>(inputType.getType()));
    }
    else
    {
        LERROR << "operand branch invalid.";
    }

    return dataStreamType;
#else // HAVE_GDAL
    LERROR << "GDAL support has been disabled, hence the project function is not available.";
    parseInfo.setErrorNo(499);
    throw parseInfo;
#endif // HAVE_GDAL
}


QtData* QtProject::evaluateMDD(QtMDD* qtMDD) throw (r_Error)
{
#ifdef HAVE_GDAL

    QtData* returnValue = NULL;
    MDDObj* currentMDDObj = qtMDD->getMDDObject();
    Tile*   sourceTile    = NULL;
    vector<boost::shared_ptr<Tile>>* tiles = NULL;

    if (currentMDDObj->getDimension() != 2)
    {
        LERROR << "MDD dimension is not 2D. Aborting CRS transformation.";
        throw r_Error(r_Error::r_Error_ObjectInvalid);
    }
    else
    {
        if (xmin == -1 && ymin == -1 && xmax == -1 && ymax == -1)
        {
            const r_Minterval& sourceDomain = qtMDD->getLoadDomain();
            xmin = sourceDomain[0].low();
            ymin = sourceDomain[1].low();
            xmax = sourceDomain[0].high();
            ymax = sourceDomain[1].high();
        }
    }

    if (qtMDD->getLoadDomain().is_origin_fixed() && qtMDD->getLoadDomain().is_high_fixed())
    {
        // get relevant tiles
        tiles = currentMDDObj->intersect(qtMDD->getLoadDomain());
    }
    else
    {
        LWARNING << "no tile available to project.";
        return qtMDD;
    }

    // check the number of tiles
    if (!tiles->size())
    {
        LWARNING << "no tile available to project.";
        return qtMDD;
    }

    // create one single tile with the load domain
    sourceTile = new Tile(tiles, qtMDD->getLoadDomain());

    // delete the tile vector
    delete tiles;
    tiles = NULL;

    // get type structure of the operand base type
    char*  typeStructure = qtMDD->getCellType()->getTypeStructure();
    // convert structure to r_Type
    r_Type* baseSchema = r_Type::get_any_type(typeStructure);
    r_Type* bandType = NULL;
    free(typeStructure);
    typeStructure = NULL;

    int numBands = 0;

    if (baseSchema->isPrimitiveType())      // = one band
    {
        numBands = 1;
        bandType = baseSchema;
    }
    else if (baseSchema->isStructType())    // = multiple bands
    {
        r_Structure_Type* myStruct = (r_Structure_Type*) baseSchema;
        r_Structure_Type::attribute_iterator iter(myStruct->defines_attribute_begin());
        while (iter != myStruct->defines_attribute_end())
        {
            r_Type* newType = (*iter).type_of().clone();
            numBands++;
            // check the band types, they have to be of the same type
            if ((*iter).type_of().isPrimitiveType())
            {
                const r_Primitive_Type pt = (const r_Primitive_Type&)(*iter).type_of();
                if (bandType != NULL)
                {
                    if (bandType->type_id() != pt.type_id())
                    {
                        LERROR << "Can not handle bands of different types.";
                        throw r_Error(r_Error::r_Error_General);
                    }
                }
                else
                {
                    bandType = pt.clone();
                }
            }
            else
            {
                LERROR << "Can not handle composite bands.";
                throw r_Error(r_Error::r_Error_General);
            }
            iter++;
        }
    }

    /* Now the core of the reprojection */

    // Convert tile "sourceTile" to GDAL format
    GDALDataset* gdalSource = convertTileToDataset(sourceTile, numBands, bandType);
    if (gdalSource == NULL)
    {
        LERROR << "Could not convert tile to a GDAL dataset. ";
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }

//      saveDatasetToFile(gdalSource, "gdalSource.tiff", "GTiff");

    // Perform GDAL reprojection
    GDALDataset* gdalResult = performGdalReprojection(gdalSource);
    if (gdalResult == NULL)
    {
        LERROR << "GDAL Reprojection Result is a null dataset.";
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }

//      saveDatasetToFile(gdalResult, "gdalResult.tiff", "GTiff");

    // Convert result of GDAL to a so-called "resultTile"
    Tile* resultTile = convertDatasetToTile(gdalResult, numBands, sourceTile, bandType);
    if (resultTile == NULL)
    {
        LERROR << "Could not read the projection results from GDAL.";
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }

    // create a transient MDD object for the query result
    MDDBaseType* mddBaseType = const_cast<MDDBaseType*>(qtMDD->getMDDObject()->getMDDBaseType());
    MDDObj* resultMDD = new MDDObj(mddBaseType, resultTile->getDomain(), currentMDDObj->getNullValues());
    resultMDD->insertTile(resultTile);

    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD((MDDObj*)resultMDD);

    // delete base type schema
    delete baseSchema;
    baseSchema = NULL;

    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD((MDDObj*)resultMDD);

    return returnValue;
#else // HAVE_GDAL
    LERROR << "GDAL support has been disabled, hence the project function is not available.";
    return NULL;
#endif // HAVE_GDAL
}

void
QtProject::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(tab).c_str() << "QtProject object" << endl;

    QtUnaryOperation::printTree(tab, s, mode);
}

#ifdef HAVE_GDAL
void QtProject::saveDatasetToFile(GDALDataset* ds, const char* filename, const char* driverName)
{
    GDALAllRegister();
    GDALDriver* driver = GetGDALDriverManager()->GetDriverByName(driverName);
    GDALDataset* copyDS = driver->CreateCopy(filename, ds, FALSE, NULL, NULL, NULL);
    if (copyDS != NULL)
    {
        GDALClose(copyDS);
    }
}

GDALDataset* QtProject::convertTileToDataset(Tile* tile, int nBands, r_Type* bandType)
{

    r_Bytes typeSize = ((r_Primitive_Type*) bandType)->size();
    bool isNotBoolean = ((r_Primitive_Type*) bandType)->type_id() != r_Type::BOOL;
    r_Minterval domain = tile->getDomain();
    r_Range width = domain[0].high() - domain[0].low() + 1;
    r_Range height = domain[1].high() - domain[1].low() + 1;

    /* Create a in-memory dataset */
    GDALDriver* hMemDriver = (GDALDriver*) GDALGetDriverByName("MEM");
    if (hMemDriver == NULL)
    {
        LERROR << "Could not init GDAL driver.";
        return NULL;
    }

    // convert rasdaman type to GDAL type
    GDALDataType gdalBandType = ConvUtil::rasTypeToGdalType(bandType);

    GDALDataset* hMemDS = hMemDriver->Create("in_memory_image", (int)width, (int)height, nBands, gdalBandType, NULL);

    char* tileCells = tile->getContents();
    size_t tileSize = typeSize * (size_t)height * (size_t)width;
    char* datasetCells = (char*) malloc(tileSize);
    if (datasetCells == NULL)
    {
        LERROR << "Could not allocate memory.";
        return NULL;
    }
    char* dst;
    char* src;
    int col_offset;

    // for all bands, convert data from column-major form (from Rasdaman) to row-major form (GDAL)
    // and then write the data to GDAL datasets
    for (int band = 0; band < nBands; band++)
    {
        dst = (char*) datasetCells;

        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++, dst += typeSize)
            {
                col_offset = ((row + height * col) * nBands * typeSize + band);
                src = tileCells + col_offset;
                if (isNotBoolean)
                {
                    memcpy(dst, src, typeSize);
                }
                else
                {
                    if (src[0] == 1)
                    {
                        dst[0] = std::numeric_limits<char>::max();
                    }
                    else
                    {
                        dst[0] = 0;
                    }
                }
            }

        CPLErr error =
            hMemDS->GetRasterBand(band + 1)->
            RasterIO(GF_Write, 0, 0, width, height, datasetCells,
                     width, height, gdalBandType, 0, 0);
        if (error != CE_None)
        {
            LERROR << "Could not write data to GDAL raster band " << band;
            free(datasetCells);
            return NULL;
        }
    }

    free(datasetCells);

    return hMemDS;
}


Tile* QtProject::convertDatasetToTile(GDALDataset* gdalResult, int nBands, Tile* sourceTile, r_Type* bandType)
{
    /* Read image sizes from GDAL */
    int width = GDALGetRasterXSize(gdalResult);
    int height = GDALGetRasterYSize(gdalResult);

    // update geo bounding box in output
    double gt[6];
    if (GDALGetGeoTransform(gdalResult, gt) == CE_None)
    {
        xmin = gt[0];
        ymin = gt[3] + width * gt[4] + height * gt[5];
        xmax = gt[0] + width * gt[1] + height * gt[2];
        ymax = gt[3];
    }

    /* And init rasdaman data structures */
    r_Minterval testInterval = r_Minterval(2);
    testInterval << r_Sinterval((r_Range)1, (r_Range)width);
    testInterval << r_Sinterval((r_Range)1, (r_Range)height);

    /* Allocate memory */
    int typeSize = (int)((r_Primitive_Type*) bandType)->size();
    bool isNotBoolean = ((r_Primitive_Type*) bandType)->type_id() != r_Type::BOOL;
    size_t tileSize = (size_t)(width * height * typeSize * nBands);
    char* tileCells = (char*) malloc(tileSize);
    if (tileCells == NULL)
    {
        LERROR << "failed allocating memory for the result data Tile.";
        return NULL;
    }

    size_t gdalBandSize = (size_t)(width * height * typeSize);
    char* gdalBand = (char*) malloc(gdalBandSize);
    if (gdalBand == NULL)
    {
        LERROR << "failed allocating memory for transfer between GDAL and rasdaman.";
        if (tileCells)
        {
            free(tileCells);
            tileCells = NULL;
        }
        return NULL;
    }

    // convert rasdaman type to GDAL type
    GDALDataType gdalBandType = ConvUtil::rasTypeToGdalType(bandType);

    /* Copy data from all GDAL bands to rasdaman */
    for (int band = 0; band < nBands; band ++)
    {
        CPLErr error = gdalResult->GetRasterBand(band + 1)->RasterIO(GF_Read, 0, 0, width, height, gdalBand, width, height, gdalBandType, 0, 0);
        if (error != CE_None)
        {
            LERROR << "failed reading the raster band data from GDAL.";
            LERROR << "reason: " << CPLGetLastErrorMsg();
            GDALClose(gdalResult);
            free(gdalBand);
            return NULL;
        }

        /*** Convert data from GDAL to rasdaman format.
        Rasdaman stores data in column-major form. So walk through the rasdaman 1-D target array (so we walk along the columns), and copy data from the row-major image that GDAL returns.
        Multi-band data is stored pixel interleaved in rasdaman.  ***/

        char* dst = gdalBand;
        char* src;
        int col_offset;

        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++, dst += typeSize)
            {
                col_offset = ((row + height * col) * nBands * typeSize + band);
                src = tileCells + col_offset;
                if (isNotBoolean)
                {
                    memcpy(src, dst, (size_t)typeSize);
                }
                else
                {
                    if (dst[0] == 0)
                    {
                        src[0] = 0;
                    }
                    else
                    {
                        src[0] = 1;
                    }
                }
            }
    }

    /* Close the GDAL dataset */
    GDALClose(gdalResult);
    free(gdalBand);

    /* And finally build the tile */
    Tile* resultTile = new Tile(testInterval, sourceTile->getType(), true, tileCells, (r_Bytes)0, r_Array);
    return resultTile;
}

GDALDataset* QtProject::performGdalReprojection(GDALDataset* hSrcDS) throw (r_Error)
{
    if (xmin == -1 || ymin == -1 || xmax == -1 || ymax == -1)
    {
        LERROR << "Bounds were not properly parsed.";
        throw r_Error(r_Error::r_Error_InvalidBoundsStringContents);
    }

    // Set source raster bounds
    hSrcDS->SetProjection(wktCrsIn);
    setBounds(hSrcDS);

    // Do the actual conversion
    GDALDataset* resultDS = (GDALDataset*)GDALAutoCreateWarpedVRT(
                                hSrcDS, wktCrsIn, wktCrsOut, GRA_Bilinear, 0, NULL);

    return resultDS;
}


bool QtProject::setCrsWKT(const char* srsin, char*& wkt)
{
    OGRSpatialReference srs;
    OGRErr err;
    // Setup input coordinate system. Try import from EPSG, Proj.4, ESRI and last, from a WKT string
    err = srs.SetFromUserInput(srsin);

    if (err != OGRERR_NONE)
    {
        LERROR << "GDAL could not understand coordinate reference system from string '" << srsin << "'";
        return false;
    }

    srs.exportToWkt(&wkt);
    return true;
}

void QtProject::setBounds(GDALDataset* dataset)
{
    double adfGeoTransform[6];
    int nRasterXSize = GDALGetRasterXSize(dataset);
    int nRasterYSize = GDALGetRasterYSize(dataset);

    adfGeoTransform[0] = xmin;
    adfGeoTransform[1] = (xmax - xmin) / nRasterXSize;
    adfGeoTransform[2] = 0.0;
    adfGeoTransform[3] = ymax;
    adfGeoTransform[4] = 0.0;
    adfGeoTransform[5] = -1 * ((ymax - ymin) / nRasterYSize);

    dataset->SetGeoTransform(adfGeoTransform);
}
#endif // HAVE_GDAL

float QtProject::getMinX() const
{
    return xmin;
}

float QtProject::getMinY() const
{
    return ymin;
}

float QtProject::getMaxX() const
{
    return xmax;
}

float QtProject::getMaxY() const
{
    return ymax;
}

char* QtProject::getTargetCrs() const
{
    return wktCrsOut;
}

QtNode::QtNodeType
QtProject::getNodeType() const
{
    return nodeType;
}
