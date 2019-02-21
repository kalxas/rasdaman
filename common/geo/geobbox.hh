#pragma once

#include <string>

namespace common {

struct GeoBbox {
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
   * Update the geotransform (gt).
   */
  void updateGeoTransform();

  std::string toString() const;

  double gt[6];
  float xmin{};
  float ymin{};
  float xmax{};
  float ymax{};
  int width{};
  int height{};
  std::string crs;
  std::string wkt;
  std::string bounds;
};

}
