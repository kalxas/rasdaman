/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef _QTCLIPPINGFUNC_
#define _QTCLIPPINGFUNC_

#include <string>

#include "qlparser/qtbinaryoperation.hh"
#include "qlparser/qtnaryoperation.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtmshapedata.hh"
#include "qlparser/qtclippingutil.hh"
#include "qlparser/qtpolygonclipping.hh"
#include "qlparser/qtgeometryop.hh"
#include "qlparser/qtgeometrydata.hh"
#include <map>

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The polytope clipping supports right now the following operation/s.
  1. Subspacing

  TODO: Future supported operations or future operations it will be useful to depending on implementation decision:
  1. Polytope Clipping
  2. Multiline 
  3. Multitope
  4. Multisubspacing? e.g. oblique curtains?

  ## Subspacing:
  The subspacing operation extracts a suspace of dimensionality n-1 or lower (where n is the dimension of the 
  given dataset). For the moment there is an implementation of operation only as a stand alone operation. The purpose of the subspacing operation is to be used in the future as a basis for implementing the above operations as well.
  The subspacing operation is currently exectued when the user makes the following query type:

  'select clip(c, subspace(1-stpoint, n-thPoint)) from collectionName as c'
  where the point coordinates are defined by simple integer values separated by spaces and the points are separated by commas.

  ## Subspacing Result:
  The subspace operation result is the an m-dimensional object, where m is the dimension of the space that is defined by the set of user defined points. This object is the projection into the m  biggest dimensions of the bounding box of the subspace (only the part inside the bounding box). 
  The restriction "only the part of the subspace inside the bounding box" is due to the initial idea why the operation was implemented, i.e. polytope clipping.
 
*/

class QtClipping : public QtBinaryOperation
{
  public:
    /// constructor getting the mdd op, where we define the object we are going to do operations on.
    /// mshapeOp where the multidimensional shape operation is defined and also the geometry type of clipping operation is defined.
    QtClipping(QtOperation* mddOp, QtOperation* geometryOp);
    
    ~QtClipping();

    void setWithCoordinates(bool withCoordinates);

        /// returns FALSE saying that the operation IS NOT commutative
    virtual bool isCommutative() const;

    /// In case the user defined points all define a line than we use a generalization of the Bresenham line in n-dimensions.
    MDDObj* extractBresenhamLine(const MDDObj* op, 
            const r_Minterval& areaOp, 
            QtMShapeData* mshape, 
            const r_Dimension dim);

    /// This function is called in case the set of points in the subsbase operation is of dimensionality bigger than one. The parameters passed to the function are MDDObj* which holds the infomration of the dataset we are going to operate on. areaOp is the r_Minterval of the MDDobj. 
    /// "polytope" points @ the multidimensional shape constructed from the set of user-defined points. The second MDDObj pointer points to the result object. 
    MDDObj* extractSubspace(const MDDObj* op, 
            const r_Minterval& areaOp, 
            QtMShapeData* polytope);
    
    MDDObj* extractLinestring(const MDDObj* op, 
            const QtMShapeData* linestring, 
            const r_Dimension dim);

    MDDObj* extractMultipolygon(const MDDObj* op,
            const r_Minterval& areaOp,
            vector<QtPositiveGenusClipping>& clipVector,
            QtGeometryData::QtGeometryType geomType);

    MDDObj* extractCurtain(const MDDObj* op, 
            const r_Minterval& areaOp, 
            const vector<r_Dimension>& maskDims, 
            const std::pair< std::shared_ptr<char>, std::shared_ptr<r_Minterval> >& mask);

    //if the bool operand is true, then the integration uses the counting measure, otherwise it defaults to lebesgue
    MDDObj* extractCorridor(const MDDObj* op, 
            const r_Minterval& areaOp, 
            QtMShapeData* lineStringData, 
            const std::vector<r_Dimension>& maskDims,
            const std::pair< std::shared_ptr<char>, std::shared_ptr<r_Minterval> >& mask,
            QtGeometryData::QtGeometryFlag geomFlagArg = QtGeometryData::QtGeometryFlag::NONE);    
       
    /// either the extractBresenhamLine or the extractSubspace function based on the dimensionality of the dataset and the multidimensional shape
    QtData* computeOp(QtMDD* operand, QtGeometryData* geomData);
    
    /// method for evaluating the node
    QtData* evaluate(QtDataList *inputList);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

    /// debugging method
    virtual void printTree(int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES);
    
protected:
        
    BaseType* getTypeWithCoordinates(const BaseType* valuesType, const r_Dimension dim) const;
        
    /// computes the result mask domain for the mshapeList
    std::shared_ptr<r_Minterval> buildResultDom(const r_Minterval& areaOp, 
            vector<QtPositiveGenusClipping>& clipVector);
    
    /// takes the result of buildResultDom and builds the result mask from the stored mshapeList (polygons w/ interiors)
    /// one can pass other resultDom's to this method, if needed, but the intersection needs to be nonempty (unknown prior to the method called)
    /// or else a segfault will occur!
    std::shared_ptr<char> buildResultMask(std::shared_ptr<r_Minterval> resultDom, 
            vector<QtPositiveGenusClipping>& clipVector,
            QtGeometryData::QtGeometryType geomType);

    /// uses the internal mshapeList only to build a result mask and a specified domain
    std::pair< std::shared_ptr<char>, std::shared_ptr<r_Minterval> > buildAbstractMask(
            std::vector<QtPositiveGenusClipping>& clipVector, 
            QtGeometryData::QtGeometryType geomType);

    std::vector<r_Minterval> computeMaskEmbedding(
            const std::vector< std::vector<r_Point> >& pointListArg, 
            const r_Minterval& convexHullArg, 
            r_Range outputLength,
            std::vector<r_Dimension> maskDims);
    
    /// for checking errors before performing data extraction
    void computeOpErrorChecking(r_Dimension opDim, 
            const r_Minterval& areaOp, 
            QtMShapeData* shapeOp, 
            QtGeometryData::QtGeometryType geomType);    
    
    //check the mask projection dimensions
    void checkProjDims(r_Dimension opDim, const vector<r_Dimension>& maskDims);
    
    void checkMaskDim(r_Dimension maskDim, 
            const vector<r_Dimension>& maskDims);
    
    vector< QtPositiveGenusClipping > buildMultipoly(const vector< vector <QtMShapeData*> >& polygonData, 
            QtGeometryData::QtGeometryType geomType);
    
  private:
    bool withCoordinates{false};

    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

//checks if an r_Minterval has more than single cell
bool isSingleton(const r_Minterval& interval );

#include "qlparser/qtclippingfunc.icc"

#endif
