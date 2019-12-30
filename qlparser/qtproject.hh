/*************************************************************
 *
 * HEADER: qtproject.hh
 *
 * MODULE: qlparser
 * CLASS:  QtProject
 *
 * PURPOSE:
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

#ifndef _QTPROJECT__
#define _QTPROJECT__

#include "conversion/gdalincludes.hh"

#include "raslib/error.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtunaryoperation.hh"
#include "raslib/primitivetype.hh"
#include "common/geo/geobbox.hh"
#include <common/geo/resamplingalg.hh>

#ifdef HAVE_GDAL
class GDALDataset;
using GDALDatasetPtr = std::unique_ptr<GDALDataset, void(*)(GDALDataset *)>;
/// Closes and frees the dataset
void deleteGDALDataset(GDALDataset *dataset);
#endif

class Tile;

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The class represents a (coordinate system) projection operation.

*/
class QtProject : public QtUnaryOperation
{
public:
    QtProject(QtOperation *mddOp, const char *boundsIn, const char *crsIn, const char *crsOut);

    QtProject(QtOperation *mddOp, const char *boundsIn, const char *crsIn, const char *crsOut, int ra);

    QtProject(QtOperation *mddOp, const char *boundsIn, const char *crsIn, const char *boundsOut, const char *crsOut,
              int widthOut = invalidExtent, int heightOut = invalidExtent,
              int ra = common::defaultResampleAlg, double et = common::defaultErrorThreshold);
    QtProject(QtOperation *mddOp, const char *boundsIn, const char *crsIn, const char *boundsOut, const char *crsOut,
              double xres = invalidResolution, double yres = invalidResolution,
              int ra = common::defaultResampleAlg, double et = common::defaultErrorThreshold);

    ~QtProject() = default;

    /// method for evaluating the node
    QtData *evaluate(QtDataList *inputList);

    /// test if the edge to the parent node is of type mdd or atomic
    virtual QtAreaType getAreaType();

    const QtTypeElement &checkType(QtTypeTuple *typeTuple);

    /// debugging method
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// method for evaluating the reprojection with a given operand
    QtData *evaluateMDD(QtMDD *mdd);

    /// optimizing load access
    void optimizeLoad(QtTrimList *trimList);

    /// method for identification of nodes
    virtual QtNodeType getNodeType() const;

private:

#ifdef HAVE_GDAL

    // Conversion methods between rasdaman and GDAL
    std::unique_ptr<Tile> reprojectTile(Tile *srcTile, int ni, r_Primitive_Type *rBandType);

#endif

    static constexpr int invalidExtent{-1};
    static constexpr double invalidResolution{0.0};

    QtOperation *mddOp;

    common::GeoBbox in;
    common::GeoBbox out;
    common::ResampleAlg resampleAlg{common::defaultResampleAlg};
    double errThreshold{common::defaultErrorThreshold};

    static const QtNodeType nodeType;

};
#endif  // _QTPROJECT_

