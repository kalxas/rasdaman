/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * File:   QtGeometryData.hh
 * Author: bbell
 *
 * Created on June 26, 2018, 3:18 PM
 */

#ifndef QTGEOMETRYDATA_HH
#define QTGEOMETRYDATA_HH

#include "qlparser/qtmshapedata.hh"
#include "raslib/minterval.hh"
#include "raslib/point.hh"
#include "qlparser/qtpolygonclipping.hh"

class QtGeometryData : public QtData
{
public:
    enum QtGeometryType
    {
        GEOM_NONE,
        GEOM_SUBSPACE,
        GEOM_POLYGON,
        GEOM_POLYTOPE,
        GEOM_LINESTRING,
        GEOM_MULTIPOLYGON,
        GEOM_MULTILINESTRING,
        GEOM_CURTAIN_POLYGON,
        GEOM_CURTAIN_LINESTRING,
        GEOM_CURTAIN_LINESTRING_EMBEDDED,
        GEOM_CURTAIN_MULTIPOLYGON,
        GEOM_CURTAIN_MULTILINESTRING,
        GEOM_CURTAIN_MULTILINESTRING_EMBEDDED,
        GEOM_CORRIDOR_POLYGON,
        GEOM_CORRIDOR_LINESTRING,
        GEOM_CORRIDOR_LINESTRING_EMBEDDED,
        GEOM_CORRIDOR_MULTIPOLYGON,
        GEOM_CORRIDOR_MULTILINESTRING,
        GEOM_CORRIDOR_MULTILINESTRING_EMBEDDED
    };

    enum QtGeometryFlag
    {
        NONE,          //no flag
        DISCRETEPATH,  //discrete linestring extrapolation
        SPLINE         //interpret using spline conventions
    };

    QtGeometryData(const vector<vector<QtMShapeData *>> &geomDataArg, const QtGeometryType geomTypeArg, QtGeometryFlag geomFlagArg = NONE);
    virtual ~QtGeometryData();

    QtDataType getDataType() const;
    char *getTypeStructure() const;
    bool equal(const QtData *obj) const;
    std::string getSpelling() const;

    QtMShapeData *getProjections();
    vector<QtMShapeData *> getLinestrings();
    vector<vector<QtMShapeData *>> getPolygons();
    vector<vector<QtMShapeData *>> getData();
    QtGeometryData::QtGeometryType getGeometryType();

    inline QtGeometryFlag getGeomFlag()
    {
        return geomFlag;
    }

    void printStatus(ostream &s) const override;

protected:
    void initializeData();

private:
    void printMultiPolygon(ostream &s) const;
    void printLineString(ostream &s) const;
    void printProjection(ostream &s) const;

    //projection data
    QtMShapeData *projectionData;
    //linestring data
    vector<QtMShapeData *> multiLinestringData;
    //polygon data
    vector<vector<QtMShapeData *>> multiPolygonData;
    //all data
    vector<vector<QtMShapeData *>> geomData;
    //geometry type
    const QtGeometryType geomType;
    //declares discretization (i.e. skip extrapolation in mask-building or extraction methods)
    QtGeometryFlag geomFlag = NONE;
};

#endif /* QTGEOMETRYDATA_HH */
