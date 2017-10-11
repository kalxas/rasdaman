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
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *
 ************************************************************/

using namespace std;

#include "config.h"
#include "qlparser/qtmshapedata.hh"
#include <cstring>
#include <cmath>

QtMShapeData::QtMShapeData(vector<r_Point> &mShape)
    : QtData(), polytopePoints(mShape), midPoint(NULL)
{
    // save the coordinate points with double precision to reduce rounding errors
    for(r_Dimension dim = 0; dim < polytopePoints.size(); dim++)
    {
        r_PointDouble pt(polytopePoints[dim]);
        polytopePointsDouble.push_back(pt);
    }

    computeDimensionality();
}

QtMShapeData::QtMShapeData(vector<QtMShapeData*> &mShapeEdges )
    :QtData(), midPoint(NULL), polytopeEdges(mShapeEdges)
{
    for(size_t i = 0; i < mShapeEdges.size(); i++)
    {
        // does not take into consideration multiplicities, so the same vertices can be repeated several times for polytopes
        polytopePointsDouble.insert(polytopePointsDouble.end(), mShapeEdges[i]->getMShapeData().begin(), mShapeEdges[i]->getMShapeData().end());
    }

    //polytopePoints now contains all vertices provided from the user. 

    // dimensionality is computed in the same way as in the constructor when only
    // polytope vertices are provided.
    computeDimensionality();
}

QtMShapeData::~QtMShapeData()
{
}

QtDataType
QtMShapeData::getDataType() const
{
    return QT_MSHAPE;
}

bool QtMShapeData::equal(const QtData *obj) const
{
    //P && TRUE == P; P && FALSE == FLASE
    int returnValue = true;

    if (obj->getDataType() == QT_MSHAPE)
    {
        QtMShapeData *pt = static_cast<QtMShapeData *>(const_cast<QtData *>(obj));

        // vertice points do not have the same dimension
        if ((pt->getMShapeData()).size() != polytopePointsDouble.size())
        {
            return false;
        }
        std::vector<r_PointDouble>::const_iterator iter1 = polytopePointsDouble.begin();
        std::vector<r_PointDouble>::const_iterator iter2 = pt->getMShapeData().begin();
        for (; iter1 != polytopePointsDouble.end(); iter1++, iter2++)
        {
            returnValue &= ((*iter1) == (*iter2));
        }
    }

    return returnValue;
}

std::string
QtMShapeData::getSpelling() const
{
    std::string result;

    // buffer
    r_Dimension bufferLen = polytopePoints.size() * 50; // on the safe side for one integers per dimension plus colon and brackets
    char *buffer = new char[bufferLen];
    // replaced deprecated ostrstream -- PB 2005-jan-14
    // ostrstream bufferStream( buffer, bufferLen );
    ostringstream bufferStream(buffer);

    //mabufferStream << MShapeData << std::ends;

    result.append(std::string(buffer));

    delete[] buffer;
    buffer = NULL;
    return result;
}

char *QtMShapeData::getTypeStructure() const
{
    return strdup("MShape");
}

void QtMShapeData::printStatus(std::ostream &stream) const
{
    stream << "MShape, value: " << std::flush;
    //stream << MShapeData << std::flush;
    QtData::printStatus(stream);
}

r_PointDouble* QtMShapeData::computeMidPoint()
{
    /* Computes the midpoint of the given set of points as vertices
       in the multidimensional shape
    */
    if (this->midPoint != NULL)
    {
        return this->midPoint;
    }
    
    midPoint = new r_PointDouble(polytopePointsDouble[0].dimension());

    for(size_t i =0; i< polytopePointsDouble.size(); i++)
    {
        *midPoint = *midPoint + polytopePointsDouble[i];
    }

    *midPoint = *midPoint * (1.0 / polytopePointsDouble.size());

    return midPoint;
}

void QtMShapeData::computeDimensionality()
{

    // an affine hyperplane containing point $p$ with normal vector $n$ can be
    // expressed as the kernel of $f(x):= (p - x)\cdot n$. In case the affine
    // space is not of codimension k, k such expressions must be simultaneously
    // satisfied, as the intersection of k-many affine hyperplanes gives a codim-
    // k affine space.
    
    // We use the Gram-schmidt orthogonalization procedure for determining the
    // orthogonal complement of the space in question, and use the same basepoint
    // p to guarantee the intersection of our affine spaces via the normal vectors
    // which are the orthogonal complement's basis vectors. We store these as the
    // last vectors in directionVectors.
    
    // detect dimension and assign the direction vectors
    // considering the position of the points. It could be that
    // the user is trying to construct a polygon in 3-dim with 4 points, but the polygon
    // can be embedded in a 2 dim space.

    // Points are added one by one, checking if at any pont they are coplanar to find out
    // dimension of the mshape.

    // if we have only 2-points it is clear that we only have a line, a 1-dim object.
    // the polygon should have at least 2 points so we start from dim 1 and second point.

    // ALGORITHM: Gram-Schmidt

    // ERROR PREDICTION: due to computations of sqrt of longs, we are prone to 
    // precision errors. To avoid getting the incorrect result vectors, we 
    // require an error margin, given below by epsilon

    //vector of error margins
    double epsilon = 0.000001;

    //the first point in the list is the basepoint of the affine space
    r_PointDouble translationPoint(polytopePointsDouble[0]);

    //the current size of the directionVectors vector (starts out uninitialized)
    //at this point, we do not know the total dimension of the underlying space,
    //but we do know the final total number of vectors spanning the ambient space
    size_t numOfSubspaceVectors = 0;
    directionVectors.reserve(polytopePointsDouble[0].dimension());
    
    for (size_t i = 1; i < polytopePointsDouble.size(); i++)
    {
        r_PointDouble u_i = polytopePointsDouble[i] - translationPoint;
        for (size_t j = 0; j < numOfSubspaceVectors; j++)
        {
            u_i = u_i - (directionVectors[j] * (u_i.dotProduct(directionVectors[j])));
        }
        // normalize and add to the directionVector if u_i != 0
        if (!(u_i.dotProduct(u_i) < epsilon))
        {
            u_i = u_i * (1.0 / sqrtl(u_i.dotProduct(u_i)));
            directionVectors.push_back(u_i);
            numOfSubspaceVectors++;
        }
    }

    // set dimensionality of the affine subspace.
    this->dimensionality = numOfSubspaceVectors;

    //continue to find the rest of the n-m vectors
    //they form a basis of the orthogonal complement and are used for
    //determining the affine hyperplane equations defining the subspace
    
    //from here on, numOfSubspaceVectors will track the orthogonal complement.
    for (size_t i = 0; i < polytopePointsDouble[0].dimension(); i++)
    {
        //initializes e_i[i] to 0 for each i
        r_PointDouble e_i(polytopePointsDouble[0].dimension());
        //a standard basis vector
        e_i[i] = 1;

        for (size_t j = 0; j < numOfSubspaceVectors; j++)
        {
            //subtract the projection of the standard basis vector on the subspace
            e_i = e_i - (directionVectors[j] * e_i.dotProduct(directionVectors[j]));
        }
        // normalize and add to the directionVector if u_i != 0
        if (!(e_i.dotProduct(e_i) < epsilon))
        {
            e_i = e_i * (1.0 / sqrtl((e_i.dotProduct(e_i))));
            directionVectors.push_back(e_i);
            numOfSubspaceVectors++;
        }
    }
}

std::vector<std::pair< r_PointDouble, double> >
QtMShapeData::computeHyperplaneEquation()
{
    if(!hyperplaneEquations.empty())
    {
        return hyperplaneEquations;
    }
    
    // the number of equations defining the affine subspace in which the mShape 
    // lies is equal to the codimension of the affine subspace $S$.
    // These equations represent $Ax = b$ for some matrix $A$ and vector $b$.
    
    // $A$ is found by the completing the basis via gram schmidt to the full space.
    // Picking an arbitrary point in the subspace $S$ allows us to compute the 
    // RHS of $Ax = b$, such that $x \in S$ iff $Ax = b$.
    
    // $x \in S$ if $p \in S$ and $(x - p) \cdot n = 0$ $\forall n \in S^{\perp}$
    // where $S^{\perp}$ is the orthogonal complement of $S$
    // so if A's rows form a basis of $S^{\perp}$ then $A (x - p) = 0$ is a
    // sufficient condition for membership; hence, in $Ax = b$, $b = Ap$ for some
    // $p \in S$. We can guarantee polytopePointsDouble[0]$\in S$, which is enough.

    hyperplaneEquations.clear();
    for(r_Dimension d = this->dimensionality; d < directionVectors[0].dimension(); d++)
    {
        hyperplaneEquations.push_back(make_pair(directionVectors[d], directionVectors[d].dotProduct(polytopePointsDouble[0])));
    }

    return hyperplaneEquations;
}

r_Dimension
QtMShapeData::getDimension()
{   
    return dimensionality;
}
