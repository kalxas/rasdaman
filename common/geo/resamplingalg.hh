//
// Created by Dimitar Misev
// Copyright (c) 2017 rasdaman GmbH. All rights reserved.
//

#pragma once

#include <string>

namespace common {

//
// Copied from GDAL, changed GRA_ prefix to RA_ to avoid any name clash.
//
/* Note: values are selected to be consistent with GDALResampleAlg */
/*! RA = Resampling Algorithm */
typedef enum {
  /*! Nearest neighbour (select on one input pixel) */
  RA_NearestNeighbour=0,
  /*! Bilinear (2x2 kernel) */                         
  RA_Bilinear=1,
  /*! Cubic Convolution Approximation (4x4 kernel) */  
  RA_Cubic=2,
  /*! Cubic B-Spline Approximation (4x4 kernel) */     
  RA_CubicSpline=3,
  /*! Lanczos windowed sinc interpolation (6x6 kernel) */ 
  RA_Lanczos=4,
  /*! Average (computes the average of all non-NODATA contributing pixels) */ 
  RA_Average=5,
  /*! Mode (selects the value which appears most often of all the sampled points) */ 
  RA_Mode=6,
  /*  RA_Gauss=7 reserved. */
  /*! Max (selects maximum of all non-NODATA contributing pixels) */ 
  RA_Max=8,
  /*! Min (selects minimum of all non-NODATA contributing pixels) */ 
  RA_Min=9,
  /*! Med (selects median of all non-NODATA contributing pixels) */ 
  RA_Med=10,
  /*! Q1 (selects first quartile of all non-NODATA contributing pixels) */ 
  RA_Q1=11,
  /*! Q3 (selects third quartile of all non-NODATA contributing pixels) */ 
  RA_Q3=12
} ResampleAlg;

// Default resampling algorithm in gdalwarp
static const ResampleAlg defaultResampleAlg{RA_NearestNeighbour};

// Default error threshold in gdalwarp
static const double defaultErrorThreshold{0.125};

std::string raToString(ResampleAlg alg);
std::string raToDescription(ResampleAlg alg);

} // namespace common
