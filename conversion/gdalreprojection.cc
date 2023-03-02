#include "config.h"  // for HAVE_GDAL
#include "conversion/gdalreprojection.hh"
#include "conversion/convutil.hh"
#include "common/exceptions/runtimeexception.hh"
#include <common/types/service/basetypeservice.hh>
#include "common/types/model/typedescriptor.hh"  // for TypeDesc
#include <common/tilemgr/tilemgr.hh>             // for TileMgr
#include <common/util/tileutil.hh>
#include <raslib/minterval.hh>               // for Domain
#include "raslib/sinterval.hh"               // for Interval
#include "common/exceptions/exception.hh"    // for Exception
#include "common/result/model/arraytile.hh"  // for ArrayTile
#include <logging.hh>

#include <limits>   // for numeric_limits
#include <cstddef>  // for size_t, NULL
#include <cstdlib>  // for strtof
#include <cstring>  // for strtok, NULL

#ifdef HAVE_GDAL
#include <gdalwarper.h>      // for GDALAutoCreateWa...
#include <ogr_core.h>        // for OGRERR_NONE, OGRErr
#include <ogr_spatialref.h>  // for OGRSpatialReference
#include <cpl_error.h>       // for CPLGetLastErrorMsg
#include <gdal.h>            // for GDALGetDriverByName
#include <gdal_priv.h>       // for GDALClose, GDALD...
#endif

namespace conversion
{

using namespace common;
using std::make_shared;

GdalReprojection::GdalReprojection(const common::TypeDesc &typeArg,
                                   const std::string &boundsIn, const std::string &crsIn,
                                   const std::string &boundsOut, const std::string &crsOut,
                                   int widthOut, int heightOut, common::ResampleAlg ra, double et)
    : type{typeArg},
      in{crsIn, boundsIn, static_cast<int>((*type.getExtent())[0].get_extent()),
         static_cast<int>((*type.getExtent())[1].get_extent())},
      out{crsOut, boundsOut, widthOut, heightOut},
      resampleAlg{ra}, errThreshold{et}
{
#ifdef HAVE_GDAL
    GDALAllRegister();
#endif
}

GdalReprojection::GdalReprojection(const common::TypeDesc &typeArg,
                                   const std::string &boundsIn, const std::string &crsIn,
                                   const std::string &boundsOut, const std::string &crsOut,
                                   double xres, double yres, common::ResampleAlg ra, double et)
    : type{typeArg},
      in{crsIn, boundsIn, static_cast<int>((*type.getExtent())[0].get_extent()),
         static_cast<int>((*type.getExtent())[1].get_extent())},
      out{crsOut, boundsOut, xres, yres},
      resampleAlg{ra}, errThreshold{et}
{
#ifdef HAVE_GDAL
    GDALAllRegister();
#endif
}

#ifdef HAVE_GDAL
void throwError(const char *msg, GDALDataset *ds1 = nullptr, GDALDataset *ds2 = nullptr);

void throwError(const char *msg, GDALDataset *ds1, GDALDataset *ds2)
{
    auto msgStr = std::string(msg) + ": " + std::string(CPLGetLastErrorMsg());
    LERROR << msgStr;
    if (ds1)
    {
        GDALClose(ds1);
    }
    if (ds2)
    {
        GDALClose(ds2);
    }
    throw Exception(msgStr);
}
#endif

ArrayTilePtr GdalReprojection::reproject(const ArrayTilePtr &tile)
{
#ifdef HAVE_GDAL
    assert(tile);

    LRDEBUG("Reprojecting:\n in  - " << in.toString() << "\n out - " << out.toString());

    const auto &domain = tile->getDomain();
    const auto cellCount = domain.cell_count();
    // suffix i = int; the extra variables are for convenience as GDAL expects int
    // no suffix = size_t
    auto w = domain[0].get_extent();
    int wi = static_cast<int>(w);
    auto h = domain[1].get_extent();
    int hi = static_cast<int>(h);
    auto n = tile->getBandCount();
    int ni = static_cast<int>(n);
    // rasdaman and GDAL band type
    const auto rBandType = type.getFieldTypes().front().baseType;
    const auto gBandType = ConvUtil::rasTypeToGdalType(rBandType);
    const auto bandCellSz = BaseTypeService::getBaseTypeSizeInBytes(rBandType);

    //
    // Create src dataset from rasdaman tile
    //
    LRDEBUG("Tile with domain " << domain.to_string() << " -> GDAL dataset...");
    CPLErrorReset();
    auto *driver = (GDALDriver *)GDALGetDriverByName("MEM");
    if (driver == NULL)
        throw common::RuntimeException{"Could not init GDAL driver."};
    auto *srcDs = driver->Create("mem_in", wi, hi, ni, gBandType, NULL);
    {
        const auto dst = TileMgr::getTile<char>(cellCount * bandCellSz);
        //        memset(dst.get(), 0, cellCount * bandCellSz);
        for (size_t i = 0; i < n; ++i)
        {
            LDEBUG << "transposing band " << i << " of tile with dimensions " << w << " x " << h;
            TileUtil::transpose(tile->getTileBand(i).data(), dst.get(), h, w, rBandType);
            if (srcDs->GetRasterBand(static_cast<int>(i) + 1)->RasterIO(GF_Write, 0, 0, wi, hi, dst.get(), wi, hi, gBandType, 0, 0) != CE_None)
            {
                throwError("Failed converting tile to a GDAL dataset");
            }
        }
        srcDs->SetGeoTransform(in.gt);
        srcDs->SetProjection(in.wkt.c_str());
        GDALReferenceDataset(srcDs);
    }

    //
    // Reproject to result dataset
    //
    LDEBUG << "Reprojecting dataset...";
    auto interpolation = static_cast<GDALResampleAlg>(resampleAlg);

    GDALDataset *dstDs{nullptr};
    if (!out.bounds.empty())
    {
        // output bounds are set, use GDALReprojectImage in this case
        dstDs = driver->Create("mem_out", out.width, out.height, ni, gBandType, NULL);
        dstDs->SetGeoTransform(out.gt);
        dstDs->SetProjection(out.wkt.c_str());

#ifdef USE_GDALWARP_OPERATION
        //
        // The gdalwarp way: the code below works if replaced instead of the
        //                   the GDALReprojectImage method. Keeping it in case it
        //                   is needed for more advanced use case in future.

        void *hTransformArg = GDALCreateGenImgProjTransformer2(srcDs, nullptr, nullptr);
        void *phTransformArg = hTransformArg;
        GDALSetGenImgProjTransformerDstGeoTransform(phTransformArg, out.gt);
        GDALTransformerFunc pfnTransformer = GDALGenImgProjTransform;

        GDALWarpOptions *psWO = GDALCreateWarpOptions();
        psWO->eWorkingDataType = gBandType;
        psWO->eResampleAlg = interpolation;
        psWO->hSrcDS = srcDs;
        psWO->hDstDS = dstDs;
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
                throwError("Failed warping image", srcDs, dstDs);
            }
        }
        else
        {
            throwError("Failed initializing warp operation", srcDs, dstDs);
        }

        GDALDestroyTransformer(hTransformArg);
        GDALDestroyWarpOptions(psWO);
#else
        // Simple GDALReprojectImage version
        if (GDALReprojectImage(srcDs, NULL, dstDs, NULL, interpolation, 0.0,
                               errThreshold, NULL, NULL, NULL) != CE_None)
        {
            throwError("Reprojection failed", srcDs, dstDs);
        }
        GDALClose(srcDs);
#endif
    }
    else
    {
        // output bounds are _not_ set, use GDALAutoCreateWarpedVRT
        dstDs = (GDALDataset *)GDALAutoCreateWarpedVRT(srcDs,
                                                       in.wkt.c_str(), out.wkt.c_str(), interpolation, errThreshold, NULL);
        if (!dstDs)
        {
            throwError("Reprojection failed", srcDs);
        }
        out.width = GDALGetRasterXSize(dstDs);
        out.height = GDALGetRasterYSize(dstDs);
        out.updateGeoTransform();
    }

    //
    // Create tile from reprojected GDAL result
    //
    wi = out.width;
    w = static_cast<size_t>(wi);
    hi = out.height;
    h = static_cast<size_t>(hi);
    LDEBUG << "GDAL dataset " << w << "x" << h << " -> tile...";

    auto result = make_shared<ArrayTile>(TypeDesc::makeArrayType(
        type.getFieldTypes(), r_Minterval{{{0ll, r_Range(wi - 1)},
                                           {0ll, r_Range(hi - 1)}}}));
    auto tileSz = w * h * BaseTypeService::getBaseTypeSizeInBytes(rBandType);
    auto src = TileMgr::getTile<char>(tileSz);
    //    memset(src.get(), 0, tileSz);

    for (int band = 0; band < ni; ++band)
    {
        if (dstDs->GetRasterBand(band + 1)->RasterIO(
                GF_Read, 0, 0, wi, hi, src.get(), wi, hi, gBandType, 0, 0) != CE_None)
        {
            throwError("Failed reading raster band data from GDAL dataset", dstDs);
        }
        auto tileData = TileMgr::getTile<char>(tileSz);
        //        memset(tileData.get(), 0, tileSz);
        TileUtil::transpose(src.get(), tileData.get(), w, h, rBandType);
        result->appendTileBand(std::move(tileData));
    }
    GDALClose(dstDs);
    return result;
#else
    throw InvalidBranchException("Rasdaman has been compiled without GDAL support.");
#endif
}

const std::string &GdalReprojection::getCrsIn() const
{
    return in.crs;
}
const std::string &GdalReprojection::getCrsOut() const
{
    return out.crs;
}

}  // namespace conversion
