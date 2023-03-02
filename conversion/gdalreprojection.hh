#pragma once

#include <common/types/model/typedescriptor.hh>  // for TypeDesc
#include "common/geo/resamplingalg.hh"
#include "common/geo/geobbox.hh"

#include <memory>  // for shared_ptr
#include <string>  // for string

namespace common
{
class ArrayTile;
}

namespace conversion
{

class GdalReprojection
{
public:
    GdalReprojection(const common::TypeDesc &typeArg,
                     const std::string &boundsIn, const std::string &crsIn,
                     const std::string &boundsOut, const std::string &crsOut,
                     int wOut, int hOut, common::ResampleAlg ra, double et);

    GdalReprojection(const common::TypeDesc &typeArg,
                     const std::string &boundsIn, const std::string &crsIn,
                     const std::string &boundsOut, const std::string &crsOut,
                     double xres, double yres, common::ResampleAlg ra, double et);

    /**
     * @return a pair of reprojected array and reprojected array domain.
     */
    std::shared_ptr<common::ArrayTile> reproject(
        const std::shared_ptr<common::ArrayTile> &tile);

    const std::string &getCrsIn() const;
    const std::string &getCrsOut() const;

private:
    common::TypeDesc type;

    common::GeoBbox in;
    common::GeoBbox out;
    common::ResampleAlg resampleAlg;
    double errThreshold;
};

}  // namespace conversion
