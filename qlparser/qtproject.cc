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
#include "qtproject.hh"
#include "common/util/scopeguard.hh"

#include <iostream>
#include <string>

using namespace std;

const QtNode::QtNodeType QtProject::nodeType = QtNode::QT_PROJECT;

#ifdef HAVE_GDAL
void deleteGDALDataset(GDALDataset *dataset)
{
    if (dataset)
    {
        GDALClose(dataset);
    }
}
#endif

QtProject::QtProject(QtOperation *mddOpArg, const char *boundsIn, const char *crsIn, const char *crsOut)
    : QtUnaryOperation(mddOpArg), in{crsIn, boundsIn, 0, 0}, out{crsOut, "", 0, 0}
{
#ifdef HAVE_GDAL
    GDALAllRegister();
#endif
}

QtProject::QtProject(QtOperation *mddOpArg, const char *boundsIn, const char *crsIn, const char *crsOut, int ra)
    : QtUnaryOperation(mddOpArg), in{crsIn, boundsIn, 0, 0}, out{crsOut, "", 0, 0},
      resampleAlg{static_cast<common::ResampleAlg>(ra)}
{
    (void) ra; // fix buggy warning on CentOS 7
#ifdef HAVE_GDAL
    GDALAllRegister();
#endif
}


QtProject::QtProject(QtOperation *mddOpArg, const char *boundsIn, const char *crsIn, const char *boundsOut,
                     const char *crsOut, int widthOut, int heightOut, int ra, double et)
    : QtUnaryOperation(mddOpArg), in{crsIn, boundsIn, 0, 0}, out{crsOut, boundsOut, widthOut, heightOut},
      resampleAlg{static_cast<common::ResampleAlg>(ra)}, errThreshold{et}
{
    (void) ra; // fix buggy warning on CentOS 7
#ifdef HAVE_GDAL
    GDALAllRegister();
#endif
}

QtProject::QtProject(QtOperation *mddOpArg, const char *boundsIn, const char *crsIn, const char *boundsOut,
                     const char *crsOut, double xres, double yres, int ra, double et)
    : QtUnaryOperation(mddOpArg), in{crsIn, boundsIn, 0, 0}, out{crsOut, boundsOut,0,0},
      resampleAlg{static_cast<common::ResampleAlg>(ra)}, errThreshold{et}
{
    (void) ra; // fix buggy warning on CentOS 7
    if (xres == 0 || yres == 0)
    {
        LERROR << "Invalid resolution, xres and yres must not be 0";
        throw r_Error(PROJECT_XY_INVALID);
    }
    out.width = static_cast<int>((out.xmax - out.xmin + (xres/2.0)) / xres);
    out.height = static_cast<int>(std::fabs(out.ymax - out.ymin + (yres/2.0)) / yres);
    if (out.width <= 0 )
    {
        LERROR << "Invalid X resolution " << xres << ", output width is 0.";
        throw r_Error(PROJECT_X_INVALID);
    }
     if (out.height <= 0)
    {
        LERROR << "Invalid Y resolution " << yres << ", output height is 0.";
        throw r_Error(PROJECT_Y_INVALID);
    }
    out.gt[0] = out.xmin;
    out.gt[3] = out.ymax;
    out.gt[1] = xres;
    out.gt[5] = (out.ymax > out.ymin) ? -yres : yres;
    
#ifdef HAVE_GDAL
    GDALAllRegister();
#endif
}

QtData *QtProject::evaluate(QtDataList *inputList)
{
    QtData *returnValue = NULL;
    QtData *operand = input->evaluate(inputList);
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
        returnValue = evaluateMDD(static_cast<QtMDD *>(operand));
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
    return returnValue;
}


QtData *QtProject::evaluateMDD(QtMDD *qtMDD)
{
#ifdef HAVE_GDAL

    MDDObj *currentMDDObj = qtMDD->getMDDObject();
    // verify op dimension
    if (currentMDDObj->getDimension() != 2)
    {
        LERROR << "MDD dimension is not 2D. Aborting CRS transformation.";
        throw r_Error(r_Error::r_Error_ObjectInvalid);
    }

    //
    // Load op tiles
    //
    std::unique_ptr<vector<std::shared_ptr<Tile>>> tiles;
    if (qtMDD->getLoadDomain().is_origin_fixed() && qtMDD->getLoadDomain().is_high_fixed())
    {
        tiles.reset(currentMDDObj->intersect(qtMDD->getLoadDomain()));
    }
    if (!tiles || tiles->empty())
    {
        LWARNING << "no tile available to project.";
        return qtMDD;
    }

    //
    // Get number of bands and band type
    //
    std::unique_ptr<r_Type> baseSchema;
    {
        char *typeStructure = qtMDD->getCellType()->getTypeStructure();
        baseSchema.reset(r_Type::get_any_type(typeStructure));
        free(typeStructure), typeStructure = NULL;
    }
    int numBands = 1;
    std::unique_ptr<r_Primitive_Type> bandType;
    if (baseSchema->isPrimitiveType())      // = one band
    {
        numBands = 1;
        bandType.reset(static_cast<r_Primitive_Type *>(baseSchema.release()));
    }
    else if (baseSchema->isStructType())    // = multiple bands
    {
        auto *stype = static_cast<r_Structure_Type *>(baseSchema.get());
        numBands = static_cast<int>(stype->count_elements());
        for (const auto &att: stype->getAttributes())
        {
            // check the band types, they have to be of the same type
            if (att.type_of().isPrimitiveType())
            {
                const auto pt = static_cast<const r_Primitive_Type &>(att.type_of());
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
                    bandType.reset(static_cast<r_Primitive_Type *>(pt.clone()));
                }
            }
            else
            {
                LERROR << "Can not handle composite bands.";
                throw r_Error(r_Error::r_Error_General);
            }
        }
    }

    //
    // Now the core of the reprojection
    //

    // create one single tile with the load domain
    std::unique_ptr<Tile> sourceTile;
    sourceTile.reset(new Tile(tiles.get(), qtMDD->getLoadDomain(), currentMDDObj));
    auto *nullValues = currentMDDObj->getNullValues();

    // Convert tile "sourceTile" to GDAL format
    auto resultTile = reprojectTile(sourceTile.get(), numBands, bandType.get(), nullValues);
    sourceTile.release();

    //
    // create a transient MDD object for the query result
    //
    const auto *mddBaseType = qtMDD->getMDDObject()->getMDDBaseType();
    auto *resultMDD = new MDDObj(mddBaseType, resultTile->getDomain(), nullValues);
    resultMDD->insertTile(resultTile.get());
    resultTile.release();
    return new QtMDD(resultMDD);
#else // HAVE_GDAL
    LERROR << "GDAL support has been disabled, hence the project function is not available.";
    throw r_Error(r_Error::r_Error_RuntimeProjectionError);
#endif // HAVE_GDAL
}

#ifdef HAVE_GDAL

std::unique_ptr<Tile> QtProject::reprojectTile(Tile *srcTile, int ni, r_Primitive_Type *rBandType,
                                               const r_Nullvalues *nullValues)
{
    const auto &domain = srcTile->getDomain();
    auto wi = static_cast<int>(domain[0].high() - domain[0].low() + 1);
    auto w  = static_cast<size_t>(wi);
    auto hi = static_cast<int>(domain[1].high() - domain[1].low() + 1);
    auto h  = static_cast<size_t>(hi);
    auto n  = static_cast<size_t>(ni);
    // update input bbox
    in.width = wi;
    in.height = hi;
    in.updateGeoTransform();
    // convert rasdaman type to GDAL type
    const auto gBandType = ConvUtil::rasTypeToGdalType(rBandType);
    const auto bandCellSz = rBandType->size();
    const auto cellSz = n * bandCellSz;

    LRDEBUG("Reprojecting:\n in  - " << in.toString() << "\n out - " << out.toString());

    // ----------------------------------------------------------------------------------------
    // Create src dataset from rasdaman tile
    // ----------------------------------------------------------------------------------------

    LRDEBUG("Tile with domain " << domain.to_string() << " -> GDAL dataset...");
    CPLErrorReset();
    GDALDriver *driver = (GDALDriver *) GDALGetDriverByName("MEM");
    if (driver == NULL)
    {
        LERROR << "Could not init GDAL driver.";
        throw r_Error(r_Error::r_Error_RuntimeProjectionError);
    }
    GDALDatasetPtr srcDs(driver->Create("mem_in", wi, hi, ni, gBandType, NULL),
                         deleteGDALDataset);
    srcDs->SetGeoTransform(in.gt);
    srcDs->SetProjection(in.wkt.c_str());
    
    // set null values in the source dataset
    double nullValue = 0;
    if (nullValues != NULL && !nullValues->getNullvalues().empty()) {
      LDEBUG << "set null value to " << nullValue << " in " << ni << " source bands.";
      nullValue = nullValues->getFirstNullValue();
      for (int band = 1; band <= ni; ++band)
        srcDs->GetRasterBand(band)->SetNoDataValue(nullValue);
    }
    GDALReferenceDataset(srcDs.get());

    const char *srcCells = reinterpret_cast<const char*>(srcTile->getContents());
    char *dstCells RAS_ALIGNED = static_cast<char *>(mymalloc(w * h * bandCellSz));

    //
    // for all bands:
    //  1. transpose src tile data
    //  2. add transposed data to the GDAL dataset
    //
    for (int band = 0; band < ni; ++band)
    {
        const char *src = srcCells + (static_cast<size_t>(band) * bandCellSz);
        char *dst = dstCells;

        // 1. transpose
        if (bandCellSz == 1)
        {
            // optimization to avoid memcpy for char/octet/boolean
            for (size_t row = 0; row < h; ++row)
                for (size_t col = 0; col < w; ++col, ++dst)
                {
                    *dst = src[(row + h * col) * cellSz];
                }
        }
        else
        {
            for (size_t row = 0; row < h; ++row)
                for (size_t col = 0; col < w; ++col, dst += bandCellSz)
                {
                    memcpy(dst, src + ((row + h * col) * cellSz), bandCellSz);
                }
        }
        // 2. add to GDAL data set
        if (srcDs->GetRasterBand(band + 1)->RasterIO(
                    GF_Write, 0, 0, wi, hi, (void *) dstCells, wi, hi, gBandType, 0, 0) != CE_None)
        {
            LERROR << "failed writing data to GDAL raster band, reason: " << CPLGetLastErrorMsg();
            free(dstCells);
            throw r_Error(r_Error::r_Error_RuntimeProjectionError);
        }
    }
    free(dstCells);

    // ----------------------------------------------------------------------------------------
    // Reproject to result dataset
    // ----------------------------------------------------------------------------------------

    LDEBUG << "Reprojecting dataset...";
    
    GDALDatasetPtr dstDs(nullptr, deleteGDALDataset);

    auto interpolation = static_cast<GDALResampleAlg>(resampleAlg);
    GDALWarpOptions *psWO = GDALCreateWarpOptions();
    
    psWO->eWorkingDataType = gBandType;
    psWO->eResampleAlg = interpolation;
    psWO->hSrcDS = srcDs.get();
    psWO->nBandCount = n;
    psWO->panSrcBands = static_cast<int *>(CPLMalloc(n * sizeof(int)));
    psWO->panDstBands = static_cast<int *>(CPLMalloc(n * sizeof(int)));
    for (int i = 0; i < psWO->nBandCount; i++)
        psWO->panSrcBands[i] = psWO->panDstBands[i] = i + 1;
    
    if (nullValues != NULL && !nullValues->getNullvalues().empty()) {
      LDEBUG << "set null value to " << nullValue;
      psWO->padfSrcNoDataReal = static_cast<double *>(CPLMalloc(n*sizeof(double)));
      psWO->padfSrcNoDataImag = static_cast<double *>(CPLMalloc(n*sizeof(double)));
      psWO->padfDstNoDataReal = static_cast<double *>(CPLMalloc(n*sizeof(double)));
      psWO->padfDstNoDataImag = static_cast<double *>(CPLMalloc(n*sizeof(double)));
      for(size_t i = 0; i < n; i++) {
        psWO->padfSrcNoDataReal[i] = psWO->padfDstNoDataReal[i] = nullValue;
        psWO->padfSrcNoDataImag[i] = psWO->padfDstNoDataImag[i] = 0.0;
      }
      
      psWO->papszWarpOptions = CSLSetNameValue(psWO->papszWarpOptions,
                                               "INIT_DEST", "NO_DATA" );
    }
    psWO->papszWarpOptions = CSLSetNameValue(psWO->papszWarpOptions,
                                             "STRIP_VERT_CS", "YES" );
    
    const auto freeWarpOptions = common::make_scope_guard([psWO]() noexcept {
        GDALDestroyWarpOptions(psWO);
    });
    
    if (!out.bounds.empty())
    {
        // verify bounds are > 0
        if (out.width <= 0 || out.height <= 0)
        {
            LERROR << "Projection output must have width/height > 0.";
            throw r_Error(r_Error::r_Error_InvalidProjectionResultGridExtents);
        }

        // output bounds are set, use GDALReprojectImage in this case
        dstDs.reset(driver->Create("mem_out", out.width, out.height, ni, gBandType, NULL));
        dstDs->SetGeoTransform(out.gt);
        dstDs->SetProjection(out.wkt.c_str());
        if (nullValues != NULL && !nullValues->getNullvalues().empty()) {
          LDEBUG << "set null value to " << nullValue << " in " << ni << " target bands.";
          for (int band = 1; band <= ni; ++band)
            dstDs->GetRasterBand(band)->SetNoDataValue(nullValue);
        }
#define USE_GDALWARP_OPERATION
#ifdef USE_GDALWARP_OPERATION
        //
        // The gdalwarp way: the code below works if replaced instead of the
        //                   the GDALReprojectImage method. Keeping it in case it
        //                   is needed for more advanced use case in future.
        
        GDALTransformerFunc pfnTransformer = nullptr;
        void *hTransformArg = nullptr;
        void* hUniqueTransformArg = nullptr;
        
        hTransformArg = GDALCreateGenImgProjTransformer2(srcDs.get(), dstDs.get(), nullptr);
        if (hTransformArg == nullptr) {
          LERROR << "cannot reproject image, reason: " << CPLGetLastErrorMsg();
          throw r_Error(r_Error::r_Error_RuntimeProjectionError);
        }
        void *phTransformArg = hTransformArg;
        GDALSetGenImgProjTransformerDstGeoTransform(phTransformArg, out.gt);
        pfnTransformer = GDALGenImgProjTransform;

        hTransformArg = GDALCreateApproxTransformer(
                            GDALGenImgProjTransform, hTransformArg, errThreshold);
        pfnTransformer = GDALApproxTransform;
        GDALApproxTransformerOwnsSubtransformer(hTransformArg, TRUE);
        psWO->pfnTransformer = pfnTransformer;
        psWO->pTransformerArg = hTransformArg;
        psWO->hDstDS = dstDs.get();

        LDEBUG << "projecting with ChunkAndWarpImage";
        GDALWarpOperation oWO;
        if (oWO.Initialize(psWO) == CE_None)
        {
            if (oWO.ChunkAndWarpImage(0, 0, out.width, out.height) != CE_None)
            {
                LERROR << "failed reprojecting image, reason: " << CPLGetLastErrorMsg();
                throw r_Error(r_Error::r_Error_RuntimeProjectionError);
            }
        }
        else
        {
            LERROR << "failed initializing warp operation, reason: " << CPLGetLastErrorMsg();
            throw r_Error(r_Error::r_Error_RuntimeProjectionError);
        }

        GDALDestroyTransformer(hTransformArg);
#else
        LDEBUG << "Reprojecting with GDALReprojectImage";
        if (GDALReprojectImage(srcDs.get(), NULL, dstDs.get(), NULL, interpolation, 0.0,
                               errThreshold, NULL, NULL, psWO) != CE_None)
        {
            LERROR << "failed reprojecting image, reason: " << CPLGetLastErrorMsg();
            throw r_Error(r_Error::r_Error_RuntimeProjectionError);
        }
#endif
    }
    else
    {
        LDEBUG << "Reprojecting with GDALAutoCreateWarpedVRT as output bounds were not set";
        dstDs.reset((GDALDataset *) GDALAutoCreateWarpedVRT(srcDs.get(), in.wkt.c_str(), out.wkt.c_str(),
                    interpolation, errThreshold, psWO));
        if (!dstDs)
        {
            LERROR << "failed reprojecting image, reason: " << CPLGetLastErrorMsg();
            throw r_Error(r_Error::r_Error_RuntimeProjectionError);
        }
        out.width = GDALGetRasterXSize(dstDs.get());
        out.height = GDALGetRasterYSize(dstDs.get());
        out.updateGeoTransform();
    }

    // ----------------------------------------------------------------------------------------
    // Create result tile from reprojected GDAL result
    // ----------------------------------------------------------------------------------------

    wi = out.width;
    w = static_cast<size_t>(wi);
    hi = out.height;
    h = static_cast<size_t>(hi);

    char *tileCells RAS_ALIGNED = static_cast<char *>(mymalloc(w * h * n * bandCellSz));
    char *bandCells RAS_ALIGNED = NULL;
    
    const auto freeData = common::make_scope_guard(
                [&tileCells, &bandCells]() noexcept {
        if (tileCells) free(tileCells);
        if (bandCells) free(bandCells);
    });
    
    bandCells = static_cast<char *>(mymalloc(w * h * bandCellSz));

    // transpose GDAL bands into result tile
    for (int band = 0; band < ni; ++band)
    {
        if (dstDs->GetRasterBand(band + 1)->RasterIO(
                    GF_Read, 0, 0, wi, hi, (void *) bandCells, wi, hi, gBandType, 0, 0) != CE_None)
        {
            LERROR << "failed reading raster band data from GDAL, reason: " << CPLGetLastErrorMsg();
            throw r_Error(r_Error::r_Error_RuntimeProjectionError);
        }

        const char *src RAS_ALIGNED = bandCells;
        char *dst RAS_ALIGNED = tileCells + (static_cast<size_t>(band) * bandCellSz);

        // 1. transpose
        if (bandCellSz == 1)
        {
            // optimization to avoid memcpy for char/octet/boolean
            for (size_t row = 0; row < h; ++row)
                for (size_t col = 0; col < w; ++col, ++src)
                {
                    dst[(row + h * col) * cellSz] = *src;
                }
        }
        else
        {
            for (size_t row = 0; row < h; ++row)
                for (size_t col = 0; col < w; ++col, src += bandCellSz)
                {
                    memcpy(dst + ((row + h * col) * cellSz), src, bandCellSz);
                }
        }
    }

    // Init rasdaman data structures
    const auto wlo = domain[0].low();
    const auto hlo = domain[1].low();
    auto resDomain = r_Minterval(2)
        << r_Sinterval(wlo, wlo + r_Range(wi) - 1)
        << r_Sinterval(hlo, hlo + r_Range(hi) - 1);
    // And finally build the tile
    std::unique_ptr<Tile> resultTile;
    resultTile.reset(new Tile(resDomain, srcTile->getType(), true, reinterpret_cast<char*>(tileCells), static_cast<r_Bytes>(0), r_Array));
    tileCells = NULL; // important to avoid free by the freeData scope guard
    return resultTile;
}


#endif // HAVE_GDAL


const QtTypeElement &
QtProject::checkType(QtTypeTuple *typeTuple)
{
#ifdef HAVE_GDAL
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);
    // check operand branches
    if (input)
    {
        // get input types
        const QtTypeElement &inputType = input->checkType(typeTuple);
        if (inputType.getDataType() != QT_MDD)
        {
            LERROR << "operand must be an array.";
            parseInfo.setErrorNo(QUANTIFIEROPERANDNOTMULTIDIMENSIONAL);
            throw parseInfo;
        }

        dataStreamType.setDataType(QT_MDD);
        dataStreamType.setType(const_cast<Type *>(inputType.getType()));
    }
    else
    {
        LERROR << "operand branch invalid.";
    }
    return dataStreamType;
#else // HAVE_GDAL
    LERROR << "GDAL support has been disabled, hence the project function is not available.";
    parseInfo.setErrorNo(FEATURENOTSUPPORTED);
    throw parseInfo;
#endif // HAVE_GDAL
}

void
QtProject::optimizeLoad(QtTrimList *trimList)
{
    if (trimList)
    {
        for (auto iter = trimList->begin(); iter != trimList->end(); iter++)
        {
            delete *iter, *iter = NULL;
        }
        delete trimList, trimList = NULL;
    }
}

void
QtProject::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(tab).c_str() << "QtProject object" << endl;
    QtUnaryOperation::printTree(tab, s, mode);
}

QtNode::QtAreaType
QtProject::getAreaType()
{
    return QT_AREA_MDD;
}

QtNode::QtNodeType
QtProject::getNodeType() const
{
    return nodeType;
}
