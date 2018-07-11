/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   QtGeometryData.cc
 * Author: bbell
 * 
 * Created on June 26, 2018, 3:18 PM
 */

#include "qlparser/qtgeometrydata.hh"
#include "qlparser/qtclippingutil.hh"

QtGeometryData::~QtGeometryData()
{
}

QtGeometryData::QtGeometryData(const vector< vector< QtMShapeData* > >& geomDataArg, 
                               const QtGeometryType geomTypeArg,
                               QtGeometryFlag geomFlagArg)
    : QtData(), projectionData(NULL), geomData(geomDataArg), geomType(geomTypeArg), 
        geomFlag(geomFlagArg)
{
    initializeData();
}

QtDataType 
QtGeometryData::getDataType() const
{
    return QtDataType::QT_GEOMETRY;
}

char* 
QtGeometryData::getTypeStructure() const
{
    return NULL;
}

bool 
QtGeometryData::equal(const QtData* obj) const
{
    bool retval = false;
    if(dynamic_cast< QtGeometryData* >(const_cast<QtData*>(obj))->getData() == geomData 
    && dynamic_cast<QtGeometryData*>(const_cast<QtData*>(obj))->getGeometryType() == geomType)
    {
        retval = true;
    }
    else
    {
        retval = false;
    }
    
    return retval;
}

std::string 
QtGeometryData::getSpelling() const
{
    return std::string();
}

QtMShapeData* 
QtGeometryData::getProjections()
{
    return projectionData;
}

vector< QtMShapeData* > 
QtGeometryData::getLinestrings()
{
    return multiLinestringData;
}

vector< vector< QtMShapeData* > >  
QtGeometryData::getPolygons()
{
    return multiPolygonData;
}

vector< vector< QtMShapeData* > >
QtGeometryData::getData()
{
    return geomData;
}


QtGeometryData::QtGeometryType 
QtGeometryData::getGeometryType()
{
    return geomType;
}

void 
QtGeometryData::initializeData()
{
    //do we need to compute projections?
    bool needsProjection = false;
    //switch/case based on possible clip types to initialize & update the variables!
    switch(geomType) 
    {
        case GEOM_SUBSPACE :
        {
            //only one entry, first row, first column. trivial case
            projectionData = geomData[0][0];
            break;
        }
        case GEOM_POLYGON :
        {
            multiPolygonData = geomData;
            break;
        }
        case GEOM_LINESTRING :
        {
            //convention: only one entry, first row, first column. this is the basic case.
            multiLinestringData.reserve(1);
            multiLinestringData.emplace_back( geomData[0][0] );
            break;
        }
        case GEOM_MULTIPOLYGON :
        {
            //convention: each consecutive element is a positive genus polygon (row-vector)
            //            each row is a polygon, each column is a hole (n>0) or the boundary (n==0)
            multiPolygonData = geomData;
            break;
        }
        //case GEOM_MULTILINESTRING : 
        //not supported at the moment, but this is what it would look like
        //convention: each consecutive element is a linestring with no further entries (row-vector)
        //    multiLinestringData.reserve(geomData.size());
        //    for(auto iter = geomData.begin(); iter != geomData.end(); iter++)
        //    {
        //        multiLinestringData.emplace_back( (*iter)[0] );                
        //    }
        case GEOM_CURTAIN_POLYGON :
        {
            //convention: first row, first column is the projection.
            //            second row is a positive genus polygon (see POLYGON)
            projectionData = geomData[0][0];
            
            multiPolygonData.reserve(1);
            multiPolygonData.emplace_back( geomData[1] );
            break;
        }
        //case GEOM_CURTAIN_LINESTRING : 
        //not supported at the moment
        //    projectionData = geomData[0][0];
        //    
        //    multiPolygonData.reserve(1);
        //    multiPolygonData.emplace_back( geomData[1] );
        case GEOM_CURTAIN_LINESTRING_EMBEDDED :
        {
            //convention: first row, first column is the projection.
            //            second row is a linestring (see LINESTRING)
            projectionData = geomData[0][0];
            
            multiPolygonData.reserve(1);
            multiPolygonData.emplace_back( geomData[1] );
            break;
        }
        case GEOM_CURTAIN_MULTIPOLYGON :
        {
            //convention: first row, first column is the projection.
            //            second row and afterwards is a multipolygon (see MULTIPOLYGON)
            projectionData = geomData[0][0];
            
            multiPolygonData.reserve(static_cast<r_Dimension>(geomData.size() - 1));
            for(auto iter = next(geomData.begin()); iter != geomData.end(); iter++)
            {
                multiPolygonData.emplace_back( *iter );
            }
            break;
        }
        //case GEOM_CURTAIN_MULTILINESTRING : 
        //not supported at the moment
        //case GEOM_CURTAIN_MULTILINESTRING_EMBEDDED :
        //{
        //    //convention: first row, first column is the projection.
        //    //            second row and afterwards is a multilinestring (see MULTILINESTRING)
        //    projectionData = geomData[0][0];
        //    
        //    multiPolygonData.reserve(static_cast<r_Dimension>(geomData.size() - 1));
        //    for(auto iter = next(geomData.begin()); iter != geomData.end(); iter++)
        //    {
        //        multiPolygonData.emplace_back( *iter );
        //    }
        //    break;
        //}
        case GEOM_CORRIDOR_POLYGON :
        {
            //convention: first row, first column is the projection.
            //            second row is the linestring for integration
            //            third row is a positive genus polygon (see POLYGON)
            projectionData = geomData[0][0];
            
            multiLinestringData.reserve(1);
            multiLinestringData.emplace_back( geomData[1][0] );
            
            multiPolygonData.reserve(1);
            multiPolygonData.emplace_back( geomData[2] );
            break;
        }
        //case GEOM_CORRIDOR_LINESTRING : 
        //not supported at the moment
        case GEOM_CORRIDOR_LINESTRING_EMBEDDED :
        {
            //convention: first row, first column is the projection.
            //            second row is the linestring for integration            
            //            third row is a linestring for selection (see LINESTRING)
            projectionData = geomData[0][0];
            
            multiLinestringData.reserve(1);
            multiLinestringData.emplace_back( geomData[1][0] );
            
            multiPolygonData.reserve(1);
            multiPolygonData.emplace_back( geomData[2] );
            break;
        }
        case GEOM_CORRIDOR_MULTIPOLYGON :
        {
            //convention: first row, first column is the projection.
            //            second row and afterwards is a multipolygon (see MULTIPOLYGON)
            projectionData = geomData[0][0];
            
            multiLinestringData.reserve(1);
            multiLinestringData.emplace_back( geomData[1][0] );

            multiPolygonData.reserve(static_cast<r_Dimension>(geomData.size() - 2));
            auto iter = geomData.begin();
            std::advance(iter, 2);
            for (; iter != geomData.end(); ++iter)
            {
                multiPolygonData.emplace_back( *iter );
            }
            break;
        }
        //case GEOM_CORRIDOR_MULTILINESTRING : 
        //not supported at the moment
        //case GEOM_CORRIDOR_MULTILINESTRING_EMBEDDED :
        //{
        //    //convention: first row, first column is the projection.
        //    //            second row is a linestring
        //    //            third row and afterwards is a multilinestring
        //    projectionData = geomData[0][0];
        //    
        //    multiLinestringData.reserve(1);
        //    multiLinestringData.emplace_back( geomData[1][0] );
        //
        //    multiPolygonData.reserve(static_cast<r_Dimension>(geomData.size() - 2));
        //    auto iter = geomData.begin();
        //    std::advance(iter, 2);
        //    for (; iter != geomData.end(); ++iter)
        //    {
        //        multiPolygonData.emplace_back( *iter );
        //    }
        //    break;
        //}
        default :
        {
            multiPolygonData = geomData;
            break;
        }
    }   
}