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
#include "mymalloc/mymalloc.h"


#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

void deleteGDALDataset(GDALDataset* dataset)
{
  if (dataset)
  {
    GDALClose(dataset);
  }
}

const QtNode::QtNodeType QtProject::nodeType = QtNode::QT_PROJECT;

QtProject::QtProject(QtOperation* mddOpArg, const char* initBounds, const char* crsIn, const char* crsOut)
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

QtProject::QtProject(QtOperation* mddOpArg, const char* crsIn, const char* crsOut)
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
void QtProject::parseNumbers(const char* str)
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

float QtProject::parseOneNumber(char* str)
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

void QtProject::testCrsTransformation(const char* in, const char* out)
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
        returnValue = evaluateMDD(static_cast<QtMDD*>(operand));

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


QtData* QtProject::evaluateMDD(QtMDD* qtMDD)
{
#ifdef HAVE_GDAL

    MDDObj* currentMDDObj = qtMDD->getMDDObject();
    if (currentMDDObj->getDimension() != 2)
    {
        LERROR << "MDD dimension is not 2D. Aborting CRS transformation.";
        throw r_Error(r_Error::r_Error_ObjectInvalid);
    }
    else if (xmin == -1 && ymin == -1 && xmax == -1 && ymax == -1)
    {
        const r_Minterval& sourceDomain = qtMDD->getLoadDomain();
        xmin = sourceDomain[0].low();
        ymin = sourceDomain[1].low();
        xmax = sourceDomain[0].high();
        ymax = sourceDomain[1].high();
    }

    std::unique_ptr<vector<boost::shared_ptr<Tile>>> tiles;
    if (qtMDD->getLoadDomain().is_origin_fixed() && qtMDD->getLoadDomain().is_high_fixed())
    {
        // get relevant tiles
        tiles.reset(currentMDDObj->intersect(qtMDD->getLoadDomain()));
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

    // get type structure of the operand base type
    char*  typeStructure = qtMDD->getCellType()->getTypeStructure();
    // convert structure to r_Type
    std::unique_ptr<r_Type> baseSchema(r_Type::get_any_type(typeStructure));
    free(typeStructure);
    typeStructure = NULL;

    int numBands = 0;
    std::unique_ptr<r_Type> bandType;

    if (baseSchema->isPrimitiveType())      // = one band
    {
        numBands = 1;
        bandType = std::move(baseSchema);
    }
    else if (baseSchema->isStructType())    // = multiple bands
    {
        r_Structure_Type* myStruct = static_cast<r_Structure_Type*>(baseSchema.get());
        numBands = static_cast<int>(myStruct->count_elements());
        r_Structure_Type::attribute_iterator iter(myStruct->defines_attribute_begin());
        while (iter != myStruct->defines_attribute_end())
        {
            // check the band types, they have to be of the same type
            if ((*iter).type_of().isPrimitiveType())
            {
                const auto pt = static_cast<const r_Primitive_Type&>((*iter).type_of());
                if (bandType)
                {
                    if (bandType->type_id() != pt.type_id())
                    {
                        LERROR << "Can not handle bands of different types.";
                        throw r_Error(r_Error::r_Error_General);
                    }
                }
                else
                {
                    bandType.reset(pt.clone());
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

    // create one single tile with the load domain
    std::unique_ptr<Tile> sourceTile;
    sourceTile.reset(new Tile(tiles.get(), qtMDD->getLoadDomain()));

    // Convert tile "sourceTile" to GDAL format
    auto gdalSource = convertTileToDataset(sourceTile.get(), numBands, bandType.get());
    if (!gdalSource)
    {
        LERROR << "Could not convert tile to a GDAL dataset. ";
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }

    // Perform GDAL reprojection
    auto gdalResult = performGdalReprojection(gdalSource);
    if (!gdalResult)
    {
        LERROR << "GDAL reprojection result is a null dataset.";
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }

    auto resultTile = convertDatasetToTile(gdalResult, numBands, sourceTile.get(), bandType.get());
    if (!resultTile)
    {
        LERROR << "Could not read the projection results from GDAL.";
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }
    sourceTile.release();

    // create a transient MDD object for the query result
    MDDBaseType* mddBaseType = const_cast<MDDBaseType*>(qtMDD->getMDDObject()->getMDDBaseType());
    MDDObj* resultMDD = new MDDObj(mddBaseType, resultTile->getDomain(), currentMDDObj->getNullValues());
    resultMDD->insertTile(resultTile.get());
    resultTile.release();

    // create a new QtMDD object as carrier object for the transient MDD object
    return new QtMDD((MDDObj*)resultMDD);
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

GDALDatasetPtr QtProject::convertTileToDataset(Tile* tile, int nBands, r_Type* bandType)
{
    r_Minterval domain = tile->getDomain();
    r_Range width = domain[0].high() - domain[0].low() + 1;
    r_Range height = domain[1].high() - domain[1].low() + 1;

    /* Create a in-memory dataset */
    GDALDriver* hMemDriver = (GDALDriver*) GDALGetDriverByName("MEM");
    if (hMemDriver == NULL)
    {
        LERROR << "Could not init GDAL driver.";
        return GDALDatasetPtr(nullptr, deleteGDALDataset);
    }

    // convert rasdaman type to GDAL type
    GDALDataType gdalBandType = ConvUtil::rasTypeToGdalType(bandType);

    GDALDatasetPtr hMemDS(
        hMemDriver->Create("in_memory_image", (int)width, (int)height, nBands, gdalBandType, NULL),
        deleteGDALDataset);

    const size_t w = static_cast<size_t>(width);
    const size_t h = static_cast<size_t>(height);
    const size_t n = static_cast<size_t>(nBands);
    const size_t cellBandSize = static_cast<size_t>(((r_Primitive_Type*) bandType)->size());
    const size_t cellSize = n * cellBandSize;

    const char* tileCells = tile->getContents();
    size_t tileSize = cellBandSize * h * w;
    char* datasetCells RAS_ALIGNED = static_cast<char*>(mymalloc(w * h * cellBandSize));

    bool isBoolean = ((r_Primitive_Type*) bandType)->type_id() == r_Type::BOOL;
    size_t colOffset{};

    // for all bands, convert data from column-major form (from Rasdaman) to row-major form (GDAL)
    // and then write the data to GDAL datasets
    for (int band = 0; band < nBands; ++band)
    {
        const char* src = tileCells + (static_cast<size_t>(band) * cellBandSize);
        char* dst = datasetCells;

        if (!isBoolean) {
          for (size_t row = 0; row < h; ++row) {
            for (size_t col = 0; col < w; ++col, dst += cellBandSize) {
              colOffset = (row + h * col) * cellSize;
              memcpy(dst, src + colOffset, cellBandSize);
            }
          }
        } else {
          for (size_t row = 0; row < h; ++row)
            for (size_t col = 0; col < w; ++col, dst += cellBandSize) {
              colOffset = (row + h * col) * cellSize;
              dst[0] = src[colOffset] == 0 ? 0 : std::numeric_limits<char>::max();
            }
        }

        GDALRasterBand* gdalBand = hMemDS->GetRasterBand(band + 1);
        CPLErr error = gdalBand->RasterIO(GF_Write, 0, 0, width, height,
                                          (void*) datasetCells, width, height,
                                          gdalBandType, 0, 0);
        if (error != CE_None)
        {
            LERROR << "failed writing data to GDAL raster band, reason: " << CPLGetLastErrorMsg();;
            free(datasetCells);
            datasetCells = NULL;
            return GDALDatasetPtr(nullptr, deleteGDALDataset);
        }
    }

    free(datasetCells);
    datasetCells = NULL;

    return hMemDS;
}


std::unique_ptr<Tile> QtProject::convertDatasetToTile(
    const GDALDatasetPtr &gdalResultPtr, int nBands, Tile* sourceTile, r_Type* bandType)
{
    auto *gdalResult = gdalResultPtr.get();

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

    const size_t w = static_cast<size_t>(width);
    const size_t h = static_cast<size_t>(height);
    const size_t n = static_cast<size_t>(nBands);
    const size_t cellBandSize = static_cast<size_t>(((r_Primitive_Type*) bandType)->size());
    const size_t cellSize = n * cellBandSize;
    /* And init rasdaman data structures */

    char* tileCells RAS_ALIGNED =
        static_cast<char*>(mymalloc(w * h * n * cellBandSize));
    /* Allocate memory */
    char* bandCells RAS_ALIGNED = NULL;
    try
    {
        bandCells = static_cast<char*>(mymalloc(w * h * cellBandSize));
    }
    catch (std::bad_alloc &e)
    {
        free(tileCells);
        tileCells = NULL;
        return NULL;
    }

    // convert rasdaman type to GDAL type
    GDALDataType gdalBandType = ConvUtil::rasTypeToGdalType(bandType);
    bool isBoolean = ((r_Primitive_Type*) bandType)->type_id() == r_Type::BOOL;
    size_t colOffset{};

    /* Copy data from all GDAL bands to rasdaman */
    for (int band = 0; band < nBands; ++band)
    {
        GDALRasterBand* gdalBand = gdalResult->GetRasterBand(band + 1);
        CPLErr error = gdalBand->RasterIO(GF_Read, 0, 0, width, height,
                                          (void*) bandCells, width, height,
                                          gdalBandType, 0, 0);
        if (error != CE_None)
        {
            LERROR << "failed reading raster band data from GDAL, reason: " << CPLGetLastErrorMsg();
            return NULL;
        }

        /* Convert data from GDAL to rasdaman format.
           Rasdaman stores data in column-major form. So walk through the rasdaman
           1-D target array (so we walk along the columns), and copy data from the
           row-major image that GDAL returns.
           Multi-band data is stored pixel interleaved in rasdaman.
        */

        const char* src RAS_ALIGNED = bandCells;
        char* dst RAS_ALIGNED = tileCells + (static_cast<size_t>(band) * cellBandSize);

        if (!isBoolean) {
          for (size_t row = 0; row < h; ++row) {
            for (size_t col = 0; col < w; ++col, src += cellBandSize) {
              colOffset = (row + h * col) * cellSize;
              memcpy(dst + colOffset, src, cellBandSize);
            }
          }
        } else {
          for (size_t row = 0; row < h; ++row)
            for (size_t col = 0; col < w; ++col, src += cellBandSize) {
              colOffset = (row + h * col) * cellSize;
              dst[colOffset] = src[0] == 0 ? 0 : 1;
            }
        }
    }

    /* And init rasdaman data structures */
    r_Minterval resDomain = r_Minterval(2);
    resDomain << r_Sinterval((r_Range)0, ((r_Range)width) - 1);
    resDomain << r_Sinterval((r_Range)0, ((r_Range)height) - 1);

    /* And finally build the tile */
    std::unique_ptr<Tile> resultTile;
    resultTile.reset(new Tile(resDomain, sourceTile->getType(), true, tileCells, (r_Bytes)0, r_Array));
    return resultTile;
}

GDALDatasetPtr QtProject::performGdalReprojection(const GDALDatasetPtr &hSrcDS)
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
    GDALDatasetPtr resultDS(
        static_cast<GDALDataset*>(GDALAutoCreateWarpedVRT(hSrcDS.get(), wktCrsIn, wktCrsOut, GRA_Bilinear, 0, NULL)),
        deleteGDALDataset);

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

void QtProject::setBounds(const GDALDatasetPtr &dataset)
{
    double adfGeoTransform[6];
    int nRasterXSize = GDALGetRasterXSize(dataset.get());
    int nRasterYSize = GDALGetRasterYSize(dataset.get());

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

void
QtProject::optimizeLoad(QtTrimList* trimList)
{
    if (trimList)
    {
        for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
        {
            delete *iter;
            *iter = NULL;
        }
        // delete list
        delete trimList;
        trimList = NULL;
    }
}
