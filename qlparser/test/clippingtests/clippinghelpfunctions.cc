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

//include statements
#include <gtest/gtest.h>
#include "qlparser/qtclippingutil.hh"
#include "catalogmgr/nullvalues.cc"
#include "raslib/pointdouble.cc"

using namespace std;

TEST(computerMinterval, clippingHelpFunctions)
{
    vector<r_Point> polytopePoints = {r_Point(0, 0, 0), r_Point( 2, 2, 0), r_Point(2, 0, 0)};
    QtMShapeData* mshape = new QtMShapeData(polytopePoints);

    boundingBox* bBox = computeBoundingBox(mshape);

    r_PointDouble expected1(3), bBoxRemoveDimension(3);
    expected1[2] = -1;

    std::set<r_Dimension, std::less<r_Dimension>> projectionDimensionSet;
    
    r_Minterval subspaceAreaOp = computeProjectedMinterval(mshape, bBox, &bBoxRemoveDimension, projectionDimensionSet);

    polytopePoints.clear();
    projectionDimensionSet.clear();
    delete mshape;
    delete bBox;

    EXPECT_EQ(expected1, bBoxRemoveDimension);
}


TEST(computerMinterval5d, clippingHelpFunctions)
{
    vector<r_Point> polytopePoints = {r_Point(0, 0, 0, 0, 0), r_Point( 2, 2, 1, 1, 0), r_Point(2, 2, 0, 0, 0)};
    QtMShapeData* mshape = new QtMShapeData(polytopePoints);

    boundingBox* bBox = computeBoundingBox(mshape);

   r_PointDouble expected1_5d(5), bBoxRemoveDimension5d(5);
    expected1_5d[4] = -1;
    expected1_5d[3] = -1;
    expected1_5d[2] = -1;

    std::set<r_Dimension, std::less<r_Dimension>> projectionDimensionSet;
    
    r_Minterval subspaceAreaOp5d = computeProjectedMinterval(mshape, bBox, &bBoxRemoveDimension5d, projectionDimensionSet);


    polytopePoints.clear();
    projectionDimensionSet.clear();
    delete mshape;
    delete bBox;

    EXPECT_EQ(expected1_5d, bBoxRemoveDimension5d);
}

TEST(computerMinterval5d_2, clippingHelpFunctions)
{
    std::set<r_Dimension, std::less<r_Dimension>> projectionDimensionSet;
    
    vector<r_Point> polytopePoints = {r_Point(0, 0, 0, 0, 0), r_Point( 0, 2, 1, 2, 2), r_Point(0, 2, 1, 0, 2)};
    QtMShapeData* mshape = new QtMShapeData(polytopePoints);
    boundingBox* bBox = computeBoundingBox(mshape);

    r_PointDouble expected2_5d(5), bBoxRemoveDim1_5d(5);
    expected2_5d[0] = -1;
    expected2_5d[1] = -1;
    expected2_5d[2] = -1;
    
    r_Minterval subspaceAreaOp2_5d = computeProjectedMinterval(mshape, bBox, &bBoxRemoveDim1_5d, projectionDimensionSet);

    polytopePoints.clear();
    projectionDimensionSet.clear();
    delete mshape;
    delete bBox;

    EXPECT_EQ(expected2_5d, bBoxRemoveDim1_5d);
}

TEST(computerMinterval5d_3, clippingHelpFunctions)
{
    std::set<r_Dimension, std::less<r_Dimension>> projectionDimensionSet;
    
    vector<r_Point> polytopePoints = {r_Point(0, 0, 0, 0, 0), r_Point( 0, 10, 5, 2, 7), r_Point(0, 2, 0, 0, 2)};
    QtMShapeData* mshape = new QtMShapeData(polytopePoints);
    boundingBox* bBox = computeBoundingBox(mshape);

    r_PointDouble expected2_5d(5), bBoxRemoveDim1_5d(5);
    expected2_5d[0] = -1;
    expected2_5d[3] = -1;
    expected2_5d[2] = -1;
    
    r_Minterval subspaceAreaOp2_5d = computeProjectedMinterval(mshape, bBox, &bBoxRemoveDim1_5d, projectionDimensionSet);

    polytopePoints.clear();
    projectionDimensionSet.clear();
    delete mshape;
    delete bBox;

    EXPECT_EQ(expected2_5d, bBoxRemoveDim1_5d);
}

// TEST if something is in the subspace or not. (Distance not tested here)
TEST(isInNSubspace, clippingHelpFunctions)
{
    // initially we generate the polytope 
    vector<r_Point> polytopePoints = {r_Point( 1, 2), r_Point(6, 1)};
    QtMShapeData* mshape = new QtMShapeData(polytopePoints);

    EXPECT_EQ(true, isInNSubspace(polytopePoints[0], mshape).second);
    EXPECT_EQ(true, isInNSubspace(polytopePoints[1], mshape).second);

    delete mshape;
    polytopePoints.clear();

    polytopePoints = {r_Point( 1, 1), r_Point(5, 5)};
    mshape = new QtMShapeData(polytopePoints);

    for(int i=0; i < 5; i++)
    {
        EXPECT_EQ(true, isInNSubspace(r_Point(i, i), mshape).second);
    }


}