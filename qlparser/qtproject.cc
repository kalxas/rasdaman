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

#include "qlparser/gdalincludes.hh"

#include "config.h"
#include "qlparser/qtproject.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtmintervaldata.hh"
#include "raslib/error.hh"
#include "raslib/type.hh"
#include "raslib/primitivetype.hh"
#include "raslib/structuretype.hh"
#include "catalogmgr/ops.hh"
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

QtProject::QtProject( QtOperation *mddOp, const char *initBounds, const char* crsIn, const char* crsOut) throw (r_Error)
    : QtUnaryOperation(mddOp), xmin(-1), ymin(-1), xmax(-1), ymax(-1), wktCrsIn(NULL), wktCrsOut(NULL)
{
    initialBounds = std::string(initBounds);
    initialCrsIn = std::string(crsIn);
    initialCrsOut = std::string(crsOut);  
    GDALAllRegister();
    parseNumbers(initBounds);
    testCrsTransformation(crsIn, crsOut);
}

QtProject::QtProject( QtOperation *mddOp, const char* crsIn, const char* crsOut) throw (r_Error)
    : QtUnaryOperation(mddOp), xmin(-1), ymin(-1), xmax(-1), ymax(-1), wktCrsIn(NULL), wktCrsOut(NULL)
{
    initialCrsIn = std::string(crsIn);
    initialCrsOut = std::string(crsOut);
    GDALAllRegister();
    testCrsTransformation(crsIn, crsOut);
}

void QtProject::parseNumbers(const char* str) throw (r_Error)
{
    char* split = strtok((char*)str, ", ");
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
    char *end;
    float f = strtof(str, &end);
    if (end != strlen(str) + str)
    {
        RMInit::logOut<<"Invalid number as project bounds: '"<<str<<"'"<<endl;
        throw r_Error(r_Error::r_Error_InvalidBoundsStringContents);
    }
    return f;
}

QtProject::~QtProject()
{
}

void QtProject::testCrsTransformation(const char* in, const char *out) throw (r_Error)
{
    if (setCrsWKT(in, wktCrsIn) == false)
    {
        RMInit::logOut<< "Error: Input string '"<<in<<"' is not a valid Coordinate Reference System that GDAL can understand" << endl;
        throw r_Error(r_Error::r_Error_InvalidSourceCRS);
    }
    if (setCrsWKT(out, wktCrsOut) == false)
    {
        RMInit::logOut<< "Error: Input string '"<<out<<"' is not a valid Coordinate Reference System that GDAL can understand" << endl;
        throw r_Error(r_Error::r_Error_InvalidTargetCRS);
    }
}

QtData* QtProject::evaluate(QtDataList* inputList)
{
    RMDBCLASS( "QtProject", "evaluate( QtDataList* )", "qlparser", __FILE__, __LINE__ )

    QtData* returnValue = NULL;
    QtData* operand = NULL;

    operand = input->evaluate( inputList );

    if (operand)
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        if( operand->getDataType() != QT_MDD )
        {
            RMInit::logOut << "Internal error in QtProject::evaluate() - "
                           << "runtime type checking failed (MDD)." << std::endl;

            // delete old operand
            if( operand ) operand->deleteRef();
            return 0;
        }
#endif

        // Perform the actual evaluation
        QtMDD*  qtMDD         = (QtMDD*) operand;
        returnValue = evaluateMDD(qtMDD);

        // delete old operand
        if( operand ) operand->deleteRef();

        return returnValue;
    }
    else
        RMInit::logOut << "Error: QtProject::evaluate() - operand is not provided." << std::endl;

    return returnValue;
}


QtNode::QtAreaType
QtProject::getAreaType()
{
    return QT_AREA_MDD;
}


const QtTypeElement&
QtProject::checkType( QtTypeTuple* typeTuple )
{
    RMDBCLASS( "QtProject", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__ )

    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    // check operand branches
    if( input )
    {

        // get input types
        const QtTypeElement& inputType = input->checkType( typeTuple );

        RMDBGIF(3, RMDebug::module_qlparser, "QtProject", \
                RMInit::dbgOut << "Class..: QtProject" << endl; \
                RMInit::dbgOut << "Operand: " << flush; \
                inputType.printStatus( RMInit::dbgOut ); \
                RMInit::dbgOut << endl; )

        if( inputType.getDataType() != QT_MDD )
        {
            RMInit::logOut << "Error: QtProject::evaluate() - operand must be multidimensional." << endl;
            parseInfo.setErrorNo(353);
            throw parseInfo;
        }

        dataStreamType.setDataType( QT_MDD );
    }
    else
        RMInit::logOut << "Error: QtProject::checkType() - operand branch invalid." << endl;

    return dataStreamType;
}


QtData* QtProject::evaluateMDD(QtMDD* qtMDD) throw (r_Error)
{
    QtData* returnValue = NULL;
    MDDObj* currentMDDObj = qtMDD->getMDDObject();
    Tile*   sourceTile    = NULL;
    vector< boost::shared_ptr<Tile> >* tiles = NULL;
    
    if (currentMDDObj->getDimension() != 2)
    {
        RMInit::logOut << "Error: MDD dimension is not 2D. Aborting CRS transformation." << endl;
        throw r_Error(r_Error::r_Error_ObjectInvalid);
    }
    else
    {
        if (xmin == -1 && ymin == -1 && xmax == -1 && ymax == -1)
        { 
            const r_Minterval& sourceDomain = qtMDD->getLoadDomain();
            xmin=sourceDomain[0].low();
            ymin=sourceDomain[1].low();
            xmax=sourceDomain[0].high();
            ymax=sourceDomain[1].high(); 
        }  
    }

    if (qtMDD->getLoadDomain().is_origin_fixed() && qtMDD->getLoadDomain().is_high_fixed())
    {
        // get relevant tiles
        tiles = currentMDDObj->intersect( qtMDD->getLoadDomain() );
    }
    else
    {
        RMDBGONCE(2, RMDebug::module_qlparser, "QtProject", "evaluate() - no tile available to project." )
        return qtMDD;
    }

    // check the number of tiles
    if( !tiles->size() )
    {
        RMDBGONCE(2, RMDebug::module_qlparser, "QtProject", "evaluate() - no tile available to project." )
        return qtMDD;
    }

    // create one single tile with the load domain
    sourceTile = new Tile( tiles, qtMDD->getLoadDomain() );

    // delete the tile vector
    delete tiles;
    tiles = NULL;

    // get type structure of the operand base type
    char*  typeStructure = qtMDD->getCellType()->getTypeStructure();
    // convert structure to r_Type
    r_Type* baseSchema = r_Type::get_any_type( typeStructure );
    r_Type* bandType = NULL;
    free( typeStructure );
    typeStructure = NULL;

    int numBands = 0;

    if (baseSchema->isPrimitiveType())      // = one band
    {
        RMInit::logOut << "Found 1-band MDD... ok. " << endl;
        numBands = 1;
        bandType = baseSchema;
    }
    else if (baseSchema->isStructType())    // = multiple bands
    {
        RMInit::logOut << "\nFound multi-band MDD... checking... ";
        r_Structure_Type *myStruct = (r_Structure_Type*) baseSchema;
        r_Structure_Type::attribute_iterator iter(myStruct->defines_attribute_begin());
        while (iter != myStruct->defines_attribute_end())
        {
            r_Type *newType = (*iter).type_of().clone();
            numBands++;
            RMInit::logOut << "band " << numBands << "...";
            // check the band types, they have to be of the same type
            if ((*iter).type_of().isPrimitiveType())
            {
                r_Primitive_Type pt = (r_Primitive_Type&) (*iter).type_of();
                if (bandType != NULL)
                {
                    if (bandType->type_id() != pt.type_id())
                    {
                        RMInit::logOut << "QtProject::evaluateMDD - Error: Can not handle bands of different types." << endl;
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
            iter++;
        }
        RMInit::logOut << " ok." << endl;
    }

    /* Now the core of the reprojection */

    // Convert tile "sourceTile" to GDAL format
    GDALDataset* gdalSource = convertTileToDataset(sourceTile, numBands, bandType);
    if (gdalSource == NULL)
    {
        RMInit::logOut << "Error: Could not convert tile to a GDAL dataset. " << endl;
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }

//      saveDatasetToFile(gdalSource, "gdalSource.tiff", "GTiff");

    // Perform GDAL reprojection
    GDALDataset* gdalResult = performGdalReprojection(gdalSource);
    if (gdalResult == NULL)
    {
        RMInit::logOut << "Error: GDAL Reprojection Result is a null dataset." << endl;
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }

//      saveDatasetToFile(gdalResult, "gdalResult.tiff", "GTiff");

    // Convert result of GDAL to a so-called "resultTile"
    Tile* resultTile = convertDatasetToTile(gdalResult, numBands, sourceTile, bandType);
    if (resultTile == NULL)
    {
        RMInit::logOut << "Error: Could not read the projection results from GDAL." << endl;
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }

    // create a transient MDD object for the query result
    MDDBaseType* mddBaseType = (MDDBaseType*) qtMDD->getMDDObject()->getMDDBaseType();
    MDDObj* resultMDD = new MDDObj( mddBaseType, resultTile->getDomain(), currentMDDObj->getNullValues() );
    resultMDD->insertTile( resultTile );

    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD( (MDDObj*)resultMDD );

    // delete base type schema
    delete baseSchema;
    baseSchema = NULL;

    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD( (MDDObj*)resultMDD );

    return returnValue;
}


void QtProject::saveDatasetToFile(GDALDataset *ds, const char* filename, const char* driverName)
{
    GDALAllRegister();
    GDALDriver *driver = GetGDALDriverManager()->GetDriverByName(driverName);
    GDALDataset* copyDS = driver->CreateCopy(filename, ds, FALSE, NULL, NULL, NULL);
    if (copyDS != NULL)
        GDALClose(copyDS);
}

void
QtProject::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(tab).c_str() << "QtProject object" << endl;

    QtUnaryOperation::printTree( tab, s, mode );
}

GDALDataType QtProject::getGdalType(r_Type* rasType)
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

GDALDataset* QtProject::convertTileToDataset(Tile* tile, int nBands, r_Type* bandType)
{

    r_Bytes typeSize = ((r_Primitive_Type*) bandType)->size();
    bool isNotBoolean = ((r_Primitive_Type*) bandType)->type_id() != r_Type::BOOL;
    r_Minterval domain = tile->getDomain();
    int width = domain[0].high() - domain[0].low() + 1;
    int height = domain[1].high() - domain[1].low() + 1;

    /* Create a in-memory dataset */
    GDALDriver *hMemDriver = (GDALDriver*) GDALGetDriverByName( "MEM" );
    if( hMemDriver == NULL )
    {
        RMInit::logOut << "ERROR: Could not init GDAL driver. " << endl;
        return NULL;
    }
    
    // convert rasdaman type to GDAL type
    GDALDataType gdalBandType = getGdalType(bandType);

    GDALDataset *hMemDS = hMemDriver->Create( "in_memory_image", width, height, nBands, gdalBandType, NULL );

    char* tileCells = tile->getContents();
    char* datasetCells = (char*) malloc(typeSize * height * width);
    if (datasetCells == NULL)
    {
        RMInit::logOut << "ERROR: Could not allocate memory. " << endl;
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
            for (int col = 0; col < width; col++, dst+=typeSize)
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

    free(datasetCells);

    return hMemDS;
}


Tile* QtProject::convertDatasetToTile(GDALDataset* gdalResult, int nBands, Tile *sourceTile, r_Type* bandType )
{
    /* Read image sizes from GDAL */
    int width = GDALGetRasterXSize( gdalResult );
    int height = GDALGetRasterYSize( gdalResult );

    // update geo bounding box in output
    double gt[6];
    if ( GDALGetGeoTransform( gdalResult, gt ) == CE_None )
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
    r_Bytes typeSize = ((r_Primitive_Type*) bandType)->size();
    bool isNotBoolean = ((r_Primitive_Type*) bandType)->type_id() != r_Type::BOOL;
    char* tileCells = (char*) malloc(width*height*typeSize*nBands);
    if (tileCells == NULL)
    {
        RMInit::logOut << "ERROR while allocating memory for the result data Tile !" << endl;
        return NULL;
    }

    char* gdalBand = (char*) malloc(width*height*typeSize);
    if (gdalBand == NULL)
    {
        RMInit::logOut << "ERROR while allocating memory for transfer between GDAL and rasdaman !" << endl;
        return NULL;
    }

    // convert rasdaman type to GDAL type
    GDALDataType gdalBandType = getGdalType(bandType);

    /* Copy data from all GDAL bands to rasdaman */
    for (int band = 0; band < nBands; band ++)
    {
        CPLErr error = gdalResult->GetRasterBand(band+1)->RasterIO(GF_Read, 0, 0, width, height, gdalBand, width, height, gdalBandType, 0, 0);
        if ( error != CE_None )
        {
            RMInit::logOut << endl<<endl << "Error reading the raster band data from GDAL ! " <<endl<<endl;
            CPLError(error, 0, "");
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
            for (int col = 0; col < width; col++, dst+=typeSize)
            {
                col_offset = ((row + height * col) * nBands * typeSize + band);
                src = tileCells + col_offset;
                if (isNotBoolean)
                {
                    memcpy(src, dst, typeSize);
                }
                else
                {
                    if (dst[0] == 0)
                        src[0] = 0;
                    else
                        src[0] = 1;
                }
            }
    }

    /* Close the GDAL dataset */
    GDALClose(gdalResult);
    free(gdalBand);

    /* And finally build the tile */
    Tile *resultTile = new Tile(testInterval, sourceTile->getType(), (char*) tileCells, 0, r_Array);
    return resultTile;
}

GDALDataset* QtProject::performGdalReprojection(GDALDataset* hSrcDS) throw (r_Error)
{
    if (xmin==-1 || ymin==-1 || xmax==-1 || ymax==-1)
    {
        RMInit::logOut << "FATAL error ! This should never happen. Bounds were not properly parsed. " << endl;
        throw r_Error(r_Error::r_Error_InvalidBoundsStringContents);
    }

    // Set source raster bounds
    hSrcDS->SetProjection(wktCrsIn);
    setBounds(hSrcDS);

    // Do the actual conversion
    GDALDataset *resultDS = (GDALDataset* )GDALAutoCreateWarpedVRT(
                                hSrcDS, wktCrsIn, wktCrsOut, GRA_Bilinear, 0, NULL );
    
    return resultDS;
}


bool QtProject::setCrsWKT(const char* srsin, char*& wkt)
{
    OGRSpatialReference srs;
    OGRErr err;
    // Setup input coordinate system. Try import from EPSG, Proj.4, ESRI and last, from a WKT string
    err = srs.SetFromUserInput( srsin );

    if (err != OGRERR_NONE)
    {
        RMInit::logOut << "error: " << err << endl;
        RMInit::logOut << "GDAL could not understand coordinate reference system from string '" << srsin << "'" << endl;
        return false;
    }

    srs.exportToWkt( &wkt);
    return true;
}

void QtProject::setBounds(GDALDataset* dataset)
{
    double adfGeoTransform[6];
    int nRasterXSize = GDALGetRasterXSize( dataset );
    int nRasterYSize = GDALGetRasterYSize( dataset );

    adfGeoTransform[0] = xmin;
    adfGeoTransform[1] = (xmax - xmin) / nRasterXSize;
    adfGeoTransform[2] = 0.0;
    adfGeoTransform[3] = ymax;
    adfGeoTransform[4] = 0.0;
    adfGeoTransform[5] = -1 * ((ymax - ymin) / nRasterYSize); 

    dataset->SetGeoTransform(adfGeoTransform);
}

float QtProject::getMinX() const
{
    return xmin;
}

float QtProject::getMaxX() const
{
    return xmax;
}

float QtProject::getMinY() const
{
    return ymin;
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
