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
#include "qtproject.hh"


#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

void deleteGDALDataset(GDALDataset *dataset)
{
    if (dataset)
    {
        GDALClose(dataset);
    }
}

const QtNode::QtNodeType QtProject::nodeType = QtNode::QT_PROJECT;

QtProject::QtProject(QtOperation *mddOpArg, const char *boundsIn, const char *crsIn, const char *crsOut)
    : QtUnaryOperation(mddOpArg), in{crsIn, boundsIn, 0, 0}, out{crsOut, "", 0, 0}
{
#ifdef HAVE_GDAL
    GDALAllRegister();
#endif
}

QtProject::QtProject(QtOperation *mddOpArg, const char *boundsIn, const char *crsIn, const char *boundsOut,
                     const char *crsOut, int widthOut, int heightOut, int ra, double et)
    : QtUnaryOperation(mddOpArg), in{crsIn, boundsIn, 0, 0}, out{crsOut, boundsOut, widthOut, heightOut},
      resampleAlg{static_cast<common::ResampleAlg>(ra)}, errThreshold{et}
{
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
    std::unique_ptr<vector<boost::shared_ptr<Tile>>> tiles;
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
        r_Structure_Type::attribute_iterator iter(stype->defines_attribute_begin());
        for (/* nop */ ; iter != stype->defines_attribute_end(); iter++)
        {
            // check the band types, they have to be of the same type
            if ((*iter).type_of().isPrimitiveType())
            {
                const auto pt = static_cast<const r_Primitive_Type &>((*iter).type_of());
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
    sourceTile.reset(new Tile(tiles.get(), qtMDD->getLoadDomain()));

    // Convert tile "sourceTile" to GDAL format
    auto resultTile = reprojectTile(sourceTile.get(), numBands, bandType.get());
    sourceTile.release();

    //
    // create a transient MDD object for the query result
    //
    const auto *mddBaseType = qtMDD->getMDDObject()->getMDDBaseType();
    auto *resultMDD = new MDDObj(mddBaseType, resultTile->getDomain(), currentMDDObj->getNullValues());
    resultMDD->insertTile(resultTile.get());
    resultTile.release();
    return new QtMDD(resultMDD);
#else // HAVE_GDAL
    LERROR << "GDAL support has been disabled, hence the project function is not available.";
    throw r_Error(r_Error::r_Error_RuntimeProjectionError);
#endif // HAVE_GDAL
}

#ifdef HAVE_GDAL

std::unique_ptr<Tile> QtProject::reprojectTile(Tile *srcTile, int ni, r_Primitive_Type *rBandType)
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
    GDALReferenceDataset(srcDs.get());

    const char *srcCells = srcTile->getContents();
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

    auto interpolation = static_cast<GDALResampleAlg>(resampleAlg);
    GDALDatasetPtr dstDs(nullptr, deleteGDALDataset);
    if (!out.bounds.empty())
    {
        // output bounds are set, use GDALReprojectImage in this case
        dstDs.reset(driver->Create("mem_out", out.width, out.height, ni, gBandType, NULL));
        dstDs->SetGeoTransform(out.gt);
        dstDs->SetProjection(out.wkt.c_str());

#ifdef USE_GDALWARP_OPERATION
        //
        // The gdalwarp way: the code below works if replaced instead of the
        //                   the GDALReprojectImage method. Keeping it in case it
        //                   is needed for more advanced use case in future.

        void *hTransformArg = GDALCreateGenImgProjTransformer2(srcDs.get(), nullptr, nullptr);
        void *phTransformArg = hTransformArg;
        GDALSetGenImgProjTransformerDstGeoTransform(phTransformArg, out.gt);
        GDALTransformerFunc pfnTransformer = GDALGenImgProjTransform;

        GDALWarpOptions *psWO = GDALCreateWarpOptions();
        psWO->eWorkingDataType = gBandType;
        psWO->eResampleAlg = interpolation;
        psWO->hSrcDS = srcDs.get();
        psWO->hDstDS = dstDs.get();
        psWO->nBandCount = n;
        psWO->panSrcBands = static_cast<int *>(CPLMalloc(n * sizeof(int)));
        psWO->panDstBands = static_cast<int *>(CPLMalloc(n * sizeof(int)));
        for (int i = 0; i < psWO->nBandCount; i++)
        {
            psWO->panSrcBands[i] = psWO->panDstBands[i] = i + 1;
        }
        hTransformArg = GDALCreateApproxTransformer(
                            GDALGenImgProjTransform, hTransformArg, errThreshold);
        pfnTransformer = GDALApproxTransform;
        GDALApproxTransformerOwnsSubtransformer(hTransformArg, TRUE);
        psWO->pfnTransformer = pfnTransformer;
        psWO->pTransformerArg = hTransformArg;

        GDALWarpOperation oWO;
        if (oWO.Initialize(psWO) == CE_None)
        {
            if (oWO.ChunkAndWarpImage(0, 0, out.width, out.height) != CE_None)
            {
                LERROR << "failed reprojecting image, reason: " << CPLGetLastErrorMsg();
                return GDALDatasetPtr(nullptr, deleteGDALDataset);
            }
        }
        else
        {
            LERROR << "failed initializing warp operation, reason: " << CPLGetLastErrorMsg();
            return GDALDatasetPtr(nullptr, deleteGDALDataset);
        }

        GDALDestroyTransformer(hTransformArg);
        GDALDestroyWarpOptions(psWO);
#else
        // Simple GDALReprojectImage version
        if (GDALReprojectImage(srcDs.get(), NULL, dstDs.get(), NULL, interpolation, 0.0,
                               errThreshold, NULL, NULL, NULL) != CE_None)
        {
            LERROR << "failed reprojecting image, reason: " << CPLGetLastErrorMsg();
            throw r_Error(r_Error::r_Error_RuntimeProjectionError);
        }
#endif
    }
    else
    {
        // output bounds are _not_ set, use GDALAutoCreateWarpedVRT
        dstDs.reset((GDALDataset *) GDALAutoCreateWarpedVRT(srcDs.get(), in.wkt.c_str(), out.wkt.c_str(),
                    interpolation, errThreshold, NULL));
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
    try
    {
        bandCells = static_cast<char *>(mymalloc(w * h * bandCellSz));
    }
    catch (std::bad_alloc &e)
    {
        free(tileCells);
        throw;
    }

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
    auto resDomain = r_Minterval(2) << r_Sinterval(0ll, static_cast<r_Range>(wi) - 1)
                     << r_Sinterval(0ll, static_cast<r_Range>(hi) - 1);
    // And finally build the tile
    std::unique_ptr<Tile> resultTile;
    resultTile.reset(new Tile(resDomain, srcTile->getType(), true, tileCells, static_cast<r_Bytes>(0), r_Array));
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
            parseInfo.setErrorNo(353);
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
    parseInfo.setErrorNo(499);
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
