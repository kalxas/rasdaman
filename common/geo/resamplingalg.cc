//
// Created by Dimitar Misev
// Copyright (c) 2017 rasdaman GmbH. All rights reserved.
//

#include "resamplingalg.hh"

namespace common {

using std::string;

std::string raToString(ResampleAlg alg) {
  switch (alg) {
    case RA_NearestNeighbour: return "near";
    case RA_Bilinear: return "bilinear";
    case RA_Cubic: return "cubic";
    case RA_CubicSpline: return "cubicspline";
    case RA_Lanczos: return "lanczos";
    case RA_Average: return "average";
    case RA_Mode: return "mode";
    case RA_Max: return "max";
    case RA_Min: return "min";
    case RA_Med: return "med";
    case RA_Q1: return "q1";
    case RA_Q3: return "q3";
    default: return "unknown";
  }
}

std::string raToDescription(ResampleAlg alg) {
  switch (alg) {
    case RA_NearestNeighbour: return "Nearest neighbour (select on one input pixel)";
    case RA_Bilinear: return "Bilinear (2x2 kernel)";
    case RA_Cubic: return "Cubic Convolution Approximation (4x4 kernel)";
    case RA_CubicSpline: return "Cubic B-Spline Approximation (4x4 kernel)";
    case RA_Lanczos: return "Lanczos windowed sinc interpolation (6x6 kernel)";
    case RA_Average: return "Average (average of all non-NODATA contributing pixels)";
    case RA_Mode: return "Mode (value which appears most often of all the sampled points)";
    case RA_Max: return "Max (maximum of all non-NODATA contributing pixels)";
    case RA_Min: return "Min (minimum of all non-NODATA contributing pixels)";
    case RA_Med: return "Med (median of all non-NODATA contributing pixels)";
    case RA_Q1: return "Q1 (first quartile of all non-NODATA contributing pixels)";
    case RA_Q3: return "Q3 (third quartile of all non-NODATA contributing pixels)";
    default: return "Unknown";
  }
}

}  // namespace common
