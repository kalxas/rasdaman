#ifndef _COMMON_GEOBBOX_HH_
#define _COMMON_GEOBBOX_HH_

#include <string>

namespace common
{

struct GeoBbox
{
    /**
   * Initialize with crs, bounds, grid width/height.
   *
   * @param crsArg the CRS of bounds
   * @param boundsArg a comma-separated list of values: "xmin, ymin, xmax, ymax"
   * @param w grid width
   * @param h grid height
   */
    GeoBbox(const std::string &crsArg, const std::string &boundsArg, int w, int h);

    /**
   * Initialize with crs, bounds, output resolution.
   *
   * @param crsArg the CRS of bounds
   * @param boundsArg a comma-separated list of values: "xmin, ymin, xmax, ymax"
   * @param xres x resolution
   * @param yres y resolution
   */
    GeoBbox(const std::string &crsArg, const std::string &boundsArg, double xres, double yres);

    /**
   * Update the geotransform (gt).
   */
    void updateGeoTransform();

    std::string toString() const;

    double gt[6];
    double xmin{};
    double ymin{};
    double xmax{};
    double ymax{};
    int width{};
    int height{};
    std::string crs;
    std::string wkt;
    std::string bounds;
};

}  // namespace common

#endif
