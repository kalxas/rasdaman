
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

#include "qlparser/qtclippingutil.hh"

#include <logging.hh>

#include <sstream>
#ifndef CPPSTDLIB
//#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

#include <iostream>
#include <cmath>

using namespace std;

bool classcomp::operator()(const r_Point &x, const r_Point &y) const
{
    if (x.dimension() < y.dimension())
        return true;
    if (x.dimension() > y.dimension())
        return false;
    for (unsigned int i = 0; i < x.dimension(); i++)
    {
        if (x[i] < y[i])
            return true;
        if (x[i] > y[i])
            return false;
    }
    return false;
}

r_Minterval
computeProjectedMinterval(QtMShapeData *mshape, BoundingBox *bBox, r_PointDouble* indexToRemove, std::set<r_Dimension, std::less<r_Dimension>>& projectionDimensionSet)
{
    // Remove k smallest dimensions. Save the indices of the dimensions to be removed in indexToRemove. 
    // add the dimensions to be projected in projectionDimensionSet
    r_Minterval resultMinterval(mshape->getDimension());

    r_Dimension datasetDimension = bBox->minPoint.dimension();
    r_Dimension mshapeDim = mshape->getDimension();

    r_Dimension coDimension = datasetDimension - mshapeDim;

    if(coDimension == 0)
    {
        LERROR << "QtClipping::computeProjectedMinterval() - The subspace defined by the set of points provided has dimension equal to the dimension of the stored data.";        
        throw r_Error(SUBSPACEDIMSAMEASMDDOBJ);
    }
    std::vector<r_Dimension> k_smallest;
    k_smallest.reserve(coDimension);

    // initialize k_smallest to the first k values of bBoxSizes

    r_Dimension maxValueIndex = 0;
    // k_smallest stores the indices of the k-many smallest values in the bBox extents
    for (r_Dimension d = 0; d < coDimension; d++)
    {
        k_smallest.emplace_back(d);
        maxValueIndex = bBox->bBoxSizes[d] > bBox->bBoxSizes[k_smallest[maxValueIndex]] ? d : maxValueIndex;
    }

    for (r_Dimension d = coDimension; d < bBox->bBoxSizes.dimension(); d++)
    {
        // keep track of the index with the maximum value in k_smallest so that it is used to compare with 
        // the current value. No updates necessary in case it is bigger than the max value. In case it is smaller substitute
        // and update the maximal value.
        if (bBox->bBoxSizes[d] < bBox->bBoxSizes[k_smallest[maxValueIndex]])
        {
            k_smallest[maxValueIndex] = d;
            for (r_Dimension k = 0; k < coDimension; k++)
            {
                maxValueIndex = bBox->bBoxSizes[k_smallest[k]] > bBox->bBoxSizes[k_smallest[maxValueIndex]] ? k : maxValueIndex;
            }
        }
    }
    // set the indices to remove to -1
    for (r_Dimension d = 0; d < coDimension; d++)
    {
        (*indexToRemove)[k_smallest[d]] = -1;
    }

    for (r_Dimension i = 0; i < datasetDimension; i++)
    {
        if ((*indexToRemove)[i] == -1)
        {
            // add it to set such that it can be used for tile projection.
            projectionDimensionSet.insert(i);
        }
        else
        {
            // compute the minterval.
            r_Sinterval rs(static_cast<r_Range>(bBox->minPoint[i]), static_cast<r_Range>(bBox->maxPoint[i]));
            resultMinterval << rs;
        }
    }

    return resultMinterval;
}

BoundingBox* computeBoundingBox(const QtMShapeData* mShape)
{
    /* Given a multidimensional shape, this function computes the bounding box
       that fully contains the shape. It goes throught the point coordinates 
       defining the shape and gets the minimum coordinates & max coordinates.
       Furthermore it computes the extends of the box created by the to extremas.

       Result type is a struct (bounding box defined in the computeutile.hh)
    */
    auto polytope = mShape->getMShapeData();
    r_PointDouble minPoint(polytope[0].dimension());
    r_PointDouble maxPoint(polytope[0].dimension());
    r_PointDouble boundingBoxSize(polytope[0].dimension());

    for (r_Dimension i = 0; i < polytope[0].dimension(); i++)
    {
        minPoint[i] = polytope[0][i];
        maxPoint[i] = polytope[0][i];
    }
    for (r_Dimension i = 0; i < polytope.size(); i++)
    {
        for (r_Dimension j = 0; j < polytope[i].dimension(); j++)
        {
            minPoint[j] = minPoint[j] > polytope[i][j] ? polytope[i][j] : minPoint[j];
            maxPoint[j] = maxPoint[j] < polytope[i][j] ? polytope[i][j] : maxPoint[j];
            boundingBoxSize[j] = maxPoint[j] - minPoint[j] + 1;
        }
    }

    return new BoundingBox(minPoint, maxPoint, boundingBoxSize);
}

pair<double, bool> isInNSubspace(const r_Point& position, QtMShapeData *mshape)
{
    std::vector<r_PointDouble> *directionVectors = mshape->getDirectionVectors();

    r_PointDouble midPoint = *(mshape->computeMidPoint());

    // compute the inscribed and circumscribed Radiuses
    double inscribedSphereRadius = 0.5;
    double circumscribedSphereRadius = std::sqrt(midPoint.dimension() * 0.25);

    r_PointDouble currentPosition(position);
    r_PointDouble u_i((*directionVectors)[0].dimension());
    // compute the distance vector to the subspace by performing a projection 
    // of the distance to a point in the subspace on the vectors defining the subspace
    // and then subtracting it from the initial distance vector
    // a Gram-schmidt like procedure
    u_i = currentPosition - midPoint;
    for (size_t j = 0; j < mshape->getDimension(); j++)
    {
        u_i = u_i - ((*directionVectors)[j] * u_i.dotProduct((*directionVectors)[j]));
    }

    // computing distance and checking if we are inside the pixel or not.
    double distance = std::sqrt(u_i.dotProduct(u_i));

    // if true we are definitely inside the pixel and inscribed circle

    bool addPixel = (distance <= inscribedSphereRadius);
    if (addPixel)
        return std::make_pair(distance, addPixel);

    if (distance <= circumscribedSphereRadius)
    {
        addPixel = true;
        // check in case we are inside the pixel.
        for (size_t i = 0; i < u_i.dimension(); i++)
        {
            // TODO: Change this to a relevant value dependent on the dimensionality of the data, 
            // Since the circumscribed sphere volume and the inscribed sphere volume changes based on 
            // the dimensionality.
            if (std::abs(u_i[i]) > 0.6)
            {
                addPixel = false;
                break;
            }
        }
        return std::make_pair(distance, addPixel);
    }
    return std::make_pair(distance, addPixel);
}

std::pair<int, int> computeStepsToSkip(const r_Point& currentPosition, const r_Point& boundingPosition, QtMShapeData *mshape, r_Dimension boxDim)
{
    
    //Attempt 1 of bbell: computing offsets for the subspacing operation.
    
    // error tolerance for treating A_n[i] as 0
    // should we make this a function of the dimension or the data set size?
    double epsilon = 0.0000001;
    
    //result data
    std::pair<int, int> result;
    //assume there are no solutions
    result.first = -1;
    result.second = -1;
    
    // find the initial hyperplane equations
    vector< pair<r_PointDouble, double> > hyperplaneEquations = mshape->computeHyperplaneEquation();
    
    //for managing the case of A_n[i] = 0
    vector<bool> isCoefficientZero;
    isCoefficientZero.reserve(hyperplaneEquations.size());
    
    //for managing the rhs of the computations
    vector<double> rhs;
    rhs.reserve(hyperplaneEquations.size());
    for (size_t i = 0; i < hyperplaneEquations.size(); i++)
    {
        //subtract Ax from both sides (x = currentPosition)        
        rhs[i] = hyperplaneEquations[i].second - hyperplaneEquations[i].first.dotProduct(currentPosition);
        
        //divide by A_n (the last column of A) in each coordinate, if it is nonzero
        if(std::abs(hyperplaneEquations[i].first[boxDim-1]) > epsilon)
        {
            rhs[i] /= hyperplaneEquations[i].first[boxDim-1];
            isCoefficientZero[i] = false;
        }
        else
        {
            isCoefficientZero[i] = true;
        }
    }
    
    // first check if we fall into a situation of 0 = nonzero
    for(size_t i = 0; i < rhs.size(); i++)
    {
        if(isCoefficientZero[i])
        {
            if(std::abs(rhs[i]) > epsilon)
            {
                // in this case, the coefficient of t is zero, and we have 0*t = nonzero
                // hence we return a situation where we have no solutions
                return result;
            }
        }
    }
    
    double* someNonzeroCoefficient = NULL;
    bool allCoefficientsZero = true;
    
    for(size_t i = 0; i < hyperplaneEquations.size(); i++)
    {
        if(!isCoefficientZero[i])
        {
            allCoefficientsZero = false;
            
            if(!someNonzeroCoefficient)
            {
                //the first nonzero coefficient
                someNonzeroCoefficient = &rhs[i];
            }
            else if( !( std::abs(*someNonzeroCoefficient - rhs[i]) < epsilon ) )
            {
                // no solutions in case this result differs too greatly from the previous result
                return result;
            }
        }
    }
    
    //if all coefficients are 0, then at this point, as no 0 = nonzero situation arose, all cells in the row should be projected down
    if(allCoefficientsZero)
    {
        result.first = 0;
        result.second = std::numeric_limits<int>::max();
        return result;
    }
    
    //now we can set the initial offset to be the value of t
    result.first = std::floor(*someNonzeroCoefficient);
    
    //to get the 2nd value, we need only perform the same trick with a scan ray from the other end 
    //(sign change and change of base point), only this time, the solution existence is trivial.
    for (size_t i = 0; i < hyperplaneEquations.size(); i++)
    {
        //subtract Ax from both sides (x = currentPosition)        
        rhs[i] = -hyperplaneEquations[i].second + hyperplaneEquations[i].first.dotProduct(boundingPosition);
        
        //divide by A_n (the last column of A) in each coordinate, if it is nonzero
        if(std::abs(hyperplaneEquations[i].first[boxDim-1]) > epsilon)
        {
            rhs[i] /= hyperplaneEquations[i].first[boxDim-1];
            isCoefficientZero[i] = false;
        }
        else
        {
            isCoefficientZero[i] = true;
        }
    }   

    result.second = std::max(static_cast<int>(boundingPosition[ boundingPosition.dimension() - 1 ] - std::floor(*someNonzeroCoefficient) - currentPosition[ currentPosition.dimension() - 1 ]), result.first);
    
    return result;
}

int computePosition(const r_Point& boxSize, const r_Point& current)
{
    // given a box Size and a current Point, compute the number of steps we need to do in the 
    // last dimension in order to reach that point in the box.
    int position = 0;
    r_Range multiplyValue = 1;
    for (r_Dimension j = 0; j < current.dimension(); j++)
    {
        multiplyValue *= boxSize[j];
    }
    for (r_Dimension j = 0; j < current.dimension(); j++)
    {
        multiplyValue /= boxSize[j];
        position += (current[j] * multiplyValue);
    }

    return position;
}

int computeOffset(const r_Point& extents, const r_Point& pos1, const r_Point& pos2)
{
    int result = 0;
    r_Point difference = pos2 - pos1;
    for(size_t i = 0; i < extents.dimension(); i++)
    {
        result += extents[i]*difference[i];
    }
    return result;
}

vector<r_Point> computeNDBresenhamLine(QtMShapeData* mshape)
{
    // segment endpoints -- already checked that the size is 2.
    vector<r_PointDouble> polytopeVertices = mshape->getMShapeData();
    // the dimension -- used for determining loop size below
    r_Dimension overallDimension = polytopeVertices[0].dimension();
    
    // determine the starting point of the algo -- the lowest wrt lexicographic order.
    // also compute the direction vector associated with the line.
    r_PointDouble directionVector(overallDimension);
    r_PointDouble* firstPoint = NULL;
    if(polytopeVertices[0] < polytopeVertices[1]){
        firstPoint = &polytopeVertices[0];
        directionVector = polytopeVertices[1] - polytopeVertices[0];
    }
    else
    {
        firstPoint = &polytopeVertices[1];
        directionVector = polytopeVertices[0] - polytopeVertices[1];
    }

    // determine std basis vector w/ direction vector's largest coefficient
    r_Dimension iterationDimension = 0;
    double currentMax = directionVector[0];
    for(size_t i = 1; i < overallDimension; i++)
    {
        if(std::abs(currentMax) < std::abs(directionVector[i]))
        {
            currentMax = directionVector[i];
            iterationDimension = i;
        }
    }
    
    // determine total number of steps in iteration
    size_t numSteps = std::abs(directionVector[iterationDimension]);
    
    // rescale direction vector s.t. largest coefficient in the std basis is 1
    for(size_t i = 0; i < overallDimension; i++)
    {
         directionVector[i] /=  std::abs(currentMax);
    }
    
    //initial error vector:
    r_PointDouble errorVector(overallDimension);
    for(size_t i = 0; i < overallDimension; i++)
    {
        errorVector[i] = 0;
    }
    
    
    // fill output vector with points in lattice which are to be used.
    
    // loop size: numSteps
    // dimension w/ fixed step size of 1: iterationDimension
    // error threshold: +/- 0.5 in each direction (standard BLA thresholds)
    // directional vector: directionVector
    // error vector: errorVector
    // current point: currentPoint
    // next point to be added: nextPoint
    vector<r_Point> nSubspace;
    nSubspace.reserve(numSteps);
    
    //we append the currentPoint to the result now to simplify the loop later.
    r_Point currentPoint(overallDimension);
    for(size_t i = 0; i < overallDimension; i++)
    {
        currentPoint[i] = (*firstPoint)[i];
    }
    nSubspace.emplace_back(currentPoint);
    
    for(size_t i = 0; i < numSteps; i++)
    {
        r_Point nextPoint(overallDimension);
        //loop over directions, skipping iterationDimension
        for(size_t j = 0; j < overallDimension; j++)
        {
            if(j != iterationDimension)
            {
                errorVector[j] += directionVector[j];
                
                if(0.5 < errorVector[j])
                {
                    nextPoint[j] = currentPoint[j] + 1; //accumulated error exceeds 0.5 in abs
                    errorVector[j] -= 1; //reset error in this direction
                }
                else if(errorVector[j] < -0.5)
                {
                    nextPoint[j] = currentPoint[j] - 1; //accumulated error exceeds 0.5 in abs
                    errorVector[j] += 1; //reset error in this direction
                }
                else
                {
                    nextPoint[j] = currentPoint[j]; //no incrementation in case -0.5 <= error <= 0.5
                }
            }
            else //always increment in the iteration dimension
            {
                if(currentMax > 0)
                {
                    nextPoint[j] = currentPoint[j] + 1;
                }
                else
                {
                    nextPoint[j] = currentPoint[j] - 1;
                }
            }
        }
        //update the current point for next iteration
        currentPoint = nextPoint;
        //append to the vector of solutions
        nSubspace.emplace_back(currentPoint);
    }
    return nSubspace;
}

std::vector< std::vector< r_PointDouble > > vectorOfPairsWithoutMultiplicity(const std::vector<r_PointDouble>& polytopeVertices, size_t numSteps)
{
    vector< vector< r_PointDouble > > vectorOfSegmentEndpointPairs;
    //max possible size
    size_t endPt = polytopeVertices.size();
    vectorOfSegmentEndpointPairs.reserve(numSteps);
    for(size_t i = 0; i + 1 < endPt; )
    {
        size_t k = i + 1;
        
        while( k < endPt 
                && polytopeVertices[i] == polytopeVertices[k] )
        {
            k++;
        }
        
        vectorOfSegmentEndpointPairs.emplace_back(vector<r_PointDouble>({polytopeVertices[i], polytopeVertices[k]}));
        
        i = k;
    }
    
    vectorOfSegmentEndpointPairs.shrink_to_fit();
    if(vectorOfSegmentEndpointPairs.size() != numSteps)
    {
        throw r_Error(POINTDIMENSIONDIFFERS);
    }
    return vectorOfSegmentEndpointPairs;
}

std::vector<r_Minterval> vectorOfResultTileDomains(const std::vector<r_Minterval>& bBoxes, const std::vector<r_Dimension>& projectionDims )
{
    std::vector<r_Minterval> resultMintervals;
    resultMintervals.reserve(bBoxes.size());
    bool firstInterval = true;
    r_Range currentOffset = 0;
    for(size_t i = 0; i < bBoxes.size(); i++)
    {
            r_Minterval nextInterval(1);
            //translate to currentOffset
            if(!firstInterval)
            {
                nextInterval[0] = r_Sinterval(currentOffset, currentOffset + static_cast<r_Range>( bBoxes[i][projectionDims[i]].get_extent() ) - 2);                
            }
            else
            {
                nextInterval[0] = r_Sinterval(currentOffset, currentOffset + static_cast<r_Range>( bBoxes[i][projectionDims[i]].get_extent() ) - 1);
            }

            resultMintervals.emplace_back(nextInterval);
            currentOffset = nextInterval[0].high() + 1;
            firstInterval = false;
    }
    return resultMintervals;
}

vector<r_Point> computeNDBresenhamSegment(const std::vector<r_PointDouble>& polytopeVertices)
{
    vector<r_Point> nSubspace;

    if(polytopeVertices.size() < 2)
    {
        LERROR << "computeNDBresenhamSegment() - Performing a Bresenham line algorithm on a line segment requires two endpoint vertices.";
        throw r_Error(SUBSPACEDIMSAMEASMDDOBJ);
    }
    // the dimension -- used for determining loop size below
    r_Dimension overallDimension = polytopeVertices[0].dimension();
    
    // determine the starting point of the algo -- the lowest wrt lexicographic order.
    // also compute the direction vector associated with the line.
    r_PointDouble directionVector(polytopeVertices[1] - polytopeVertices[0]);

    //we append the first point to the result now to simplify the loop later.
    r_Point currentPoint(polytopeVertices[0].toIntPoint());

    nSubspace.push_back(currentPoint);
    
    // determine std basis vector w/ direction vector's largest coefficient
    r_Dimension iterationDimension = 0;
    double currentMax = directionVector[0];
    for(size_t i = 1; i < overallDimension; i++)
    {
        if(std::abs(currentMax) < std::abs(directionVector[i]))
        {
            currentMax = directionVector[i];
            iterationDimension = i;
        }
    }
    
    // determine total number of steps in iteration
    size_t numSteps = std::abs(directionVector[iterationDimension]);
    //now we can allocate memory for the result vector.
    //the +1 is for the initial value (for two pixels X & Y, we define k := max_i(dist(proj_i(X), proj_i(Y))).
    //There are k+1 pixels in the result
    nSubspace.reserve(numSteps + 1);
    // rescale direction vector s.t. largest coefficient in the std basis is 1
    for(size_t i = 0; i < overallDimension; i++)
    {
         directionVector[i] /=  std::abs(currentMax);
    }
    
    //initial error vector:
    r_PointDouble errorVector(overallDimension);
    for(size_t i = 0; i < overallDimension; i++)
    {
        errorVector[i] = 0;
    }
    
    
    // fill output vector with points in lattice which are to be used.
    
    // loop size: numSteps
    // dimension w/ fixed step size of 1: iterationDimension
    // error threshold: +/- 0.5 in each direction (standard BLA thresholds)
    // directional vector: directionVector
    // error vector: errorVector
    // current point: currentPoint
    // next point to be added: nextPoint
    
    for(size_t i = 0; i < numSteps; i++)
    {
        r_Point nextPoint(overallDimension);
        //loop over directions, skipping iterationDimension
        for(size_t j = 0; j < overallDimension; j++)
        {
            if(j != iterationDimension)
            {
                errorVector[j] += directionVector[j];
                
                if(0.5 < errorVector[j])
                {
                    nextPoint[j] = currentPoint[j] + 1; //accumulated error exceeds 0.5 in abs
                    errorVector[j] -= 1; //reset error in this direction
                }
                else if(errorVector[j] < -0.5)
                {
                    nextPoint[j] = currentPoint[j] - 1; //accumulated error exceeds 0.5 in abs
                    errorVector[j] += 1; //reset error in this direction
                }
                else
                {
                    nextPoint[j] = currentPoint[j]; //no incrementation in case -0.5 <= error <= 0.5
                }
            }
            else //always increment in the iteration dimension
            {
                if(currentMax > 0)
                {
                    nextPoint[j] = currentPoint[j] + 1;
                }
                else
                {
                    nextPoint[j] = currentPoint[j] - 1;
                }
            }
        }
        //update the current point for next iteration
        currentPoint = nextPoint;
        //append to the vector of solutions
        nSubspace.push_back(currentPoint);  
    }
    
    return nSubspace;
}

std::pair<int, int> endpointsSearch(const r_Minterval& domainArg, const std::vector<r_Point>& lineSegmentArg)
{
    //we first determine which band of values from the BLA result apply to this tile
    r_Point lowPoint = domainArg.get_origin();
    r_Point highPoint = domainArg.get_high();

    // since for each tile the intersectionDomain is different, we need to know which points of the line
    // sit inside the overall dataset, so we can assign it a global domain. 
    // startEndIndices keeps track of the segment of the line contained inside areaOp.
    pair<int, int> startEndIndices = make_pair(-1, -1);

    bool isFirstInitialized = false;
    
    for (size_t i = 0; i < lineSegmentArg.size(); i++)
    {
        if ((lineSegmentArg[i] >= lowPoint) && (lineSegmentArg[i] <= highPoint))
        {
            if (isFirstInitialized)
            {
                startEndIndices.second = i;
            }
            else
            {
                startEndIndices.first = i;
                startEndIndices.second = i;
                isFirstInitialized = true;
            }
        }
    }
    return startEndIndices;
}

r_Minterval localHull(const std::pair<int, int>& indices, const std::vector<r_Point>& lineSegmentArg)
{
    // domain of the relevant area of the actual dbobj corresponds to the bounding box of the start and end points.
    r_Minterval convexHull(lineSegmentArg[0].dimension());
    
    size_t firstIndex = 0;
    size_t secondIndex = 0;
    
    if(indices.first < 0)
    {
        firstIndex = 0;
    }
    else
    {
        firstIndex = static_cast<size_t>(indices.first);
    }
    
    if(indices.second < 0)
    {
        secondIndex = lineSegmentArg.size() - 1;
    }
    else
    {
        secondIndex = static_cast<size_t>(indices.second);
    }
    for(size_t i = 0; i < convexHull.dimension(); i++)
    {
        if(lineSegmentArg[firstIndex][i] <= lineSegmentArg[secondIndex][i])
        {
            convexHull[i].set_low(lineSegmentArg[firstIndex][i]);
            convexHull[i].set_high(lineSegmentArg[secondIndex][i]);
        }
        else
        {
            convexHull[i].set_low(lineSegmentArg[secondIndex][i]);
            convexHull[i].set_high(lineSegmentArg[firstIndex][i]);
        }
    }
    
    return convexHull;
}

r_Sinterval localHullByIndex(const std::pair<int, int>& indices, const std::vector<r_Point>& lineSegmentArg, size_t index)
{
    // domain of the relevant area of the actual dbobj corresponds to the bounding box of the start and end points.
    r_Sinterval convexHull;
    
    size_t firstIndex = 0;
    size_t secondIndex = 0;
    
    if(indices.first < 0)
    {
        firstIndex = 0;
    }
    else
    {
        firstIndex = static_cast<size_t>(indices.first);
    }
    
    if(indices.second < 0)
    {
        secondIndex = lineSegmentArg.size() - 1;
    }
    else
    {
        secondIndex = static_cast<size_t>(indices.second);
    }
    
    if(lineSegmentArg[firstIndex][index] <= lineSegmentArg[secondIndex][index])
    {
        convexHull.set_low(lineSegmentArg[firstIndex][index]);
        convexHull.set_high(lineSegmentArg[secondIndex][index]);
    }
    else
    {
        convexHull.set_low(lineSegmentArg[secondIndex][index]);
        convexHull.set_high(lineSegmentArg[firstIndex][index]);
    }
    
    return convexHull;
}

r_Minterval
computeProjectedDomain(r_Minterval intersectDom, std::set<r_Dimension, std::less<r_Dimension>> projectionDimensionSet, r_Dimension dim)
{
    vector<bool> valuesToKeep(intersectDom.dimension(), true);
    // find the values still part of the m_interval
    for (auto iter = projectionDimensionSet.begin(); iter != projectionDimensionSet.end(); iter++)
    {
        valuesToKeep[*iter] = false;
    }
    r_Minterval result(dim);
    r_Dimension d = 0;
    r_Dimension resultDim = 0;
    while (d < intersectDom.dimension())
    {
        if (!valuesToKeep[d])
        {
            d++;
            continue;
        }

        result[resultDim] = intersectDom[d];
        resultDim++;
        d++;
    }

    return result;
}

r_Point
computeProjectedPoint(const r_Point& pointOp, const std::vector<r_Dimension>& keptDims)
{
    r_Point result(keptDims.size());
    for(auto d: keptDims)
    {
        result << pointOp[d];
    }
    return result;    
}
