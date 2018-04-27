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

#include "qlparser/qtpolygonutil.hh"

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

pair< r_Point, r_Point > getBoundingBox(const vector< r_Point >& polygon)
{
    r_Point minPoint( polygon[0].dimension() );
    r_Point maxPoint( polygon[0].dimension() );

    for( r_Dimension i=0; i<polygon[0].dimension(); i++ )
    {
        minPoint[i] = polygon[0][i];
        maxPoint[i] = polygon[0][i];
    }

    for( r_Dimension i=1; i<polygon.size(); i++ )
    {
        for( r_Dimension j=0; j<polygon[i].dimension(); j++ )
        {
            minPoint[j] = minPoint[j] > polygon[i][j] ? polygon[i][j] : minPoint[j];
            maxPoint[j] = maxPoint[j] < polygon[i][j] ? polygon[i][j] : maxPoint[j];
        }
    }

    return make_pair(minPoint, maxPoint);
}

void rasterizePolygon( vector< vector< char > >& mask, const vector< r_Point >& polygon, bool isPolygon )
{
    r_Dimension numIt = 0;
    
    // if we are not considering a polygon, then our mask will not be filled afterwards anyways.
    if(!isPolygon && polygon.size() > 0)
    {
        numIt = polygon.size() - 1;
    }
    else
    {
        numIt = polygon.size();
    }
    
    for( r_Dimension k = 0; k < numIt; k++ )
    {
        r_Point first = polygon[k];
        r_Point second;
        
        second = polygon[(k+1) % (polygon.size())];
        
        int x1 = first[0]; int y1 = first[1];
        int x2 = second[0]; int y2 = second[1];
        
        bool swapped = false;
        
        if( abs( y2 - y1 ) > abs( x2 - x1 ) )
        {
            swap(x1, y1);
            swap(x2, y2);
            swapped = true;
        }
        
        if( x1 > x2 )
        {
            swap(x1,x2);
            swap(y1,y2);
        }
        
        int dx = x2 - x1;
        int dy = abs( y2 - y1 );
        int x = x1; int y = y1; // start point
        int error = dx/2;
        int ystep = ( y1 < y2 ) ? 1:-1; // step in + or - y-direction

        for(int i = 0; i <= dx; i++)
        {
            if( swapped ) mask[static_cast<size_t>(y)][static_cast<size_t>(x)] = 1;
            else mask[static_cast<size_t>(x)][static_cast<size_t>(y)] = 1;
            
            error -= dy; 
            if( error < 0 )
            {
                y += ystep;
                error += dx;
            }
            x++;
        }
    }
}

//used for filling the first values of the mask which are inside the polygon
//this algorithm searches every point adjacent to the polygon boundary and checks which points are inside, and which are outside
void polygonInteriorFloodfill( vector< vector< char > >& mask, const vector< r_Point >& polygon)
{
    //if mask.size() = x and mask is roughly square, then floodfilling everything is O(x^2)
    //and checking isPointInsidePolygon() on the boundary is, for polygon.size() = p, O(6*p*x),
    //where 4*x is the approximate perimeter, 3 is the expected value of the # of neighbouring cells which are not initialized to 2 in the mask,
    //hence one half of that is the expectation *after* flood fill has occurred, and thus we have 4 * 1.5 * x checks, and the
    //overall complexity O(6 * p * x) since each check is O(p).
    //as such, it is better to flood fill everything when 6p > x.
    bool fillOutside = true;
    size_t x = sqrt(mask.size() * mask[0].size());
    
    if( 6*polygon.size() > x )
    {
        fillOutside = true;
    }
    
    //just in case the mask is precisely the boundary of our box, we will have no boundary points in the interior points.
    // Of course, with that much information, our check is trivial
    if(mask.size() != 1 && mask[0].size() != 1)
    {
        if(mask[0][0] == 1)
        {
            if(mask[1][1] == 2 && isPointInsidePolygon(1,1,polygon))
            {
                floodFillFromPoint(mask, 1, 1, 2, 0);
            }
        }
    }
    
    //loop over rows
    for(size_t i = 1; i + 1 < mask.size(); i++)
    {
        //loop over columns
        for(size_t j = 1; j + 1 < mask[i].size(); j++)
        {
            //if we are on a point just inside the polygon
            if(mask[i][j] == 1)
            {
                if(mask[i+1][j] == 2)
                {
                    if(isPointInsidePolygon(i+1,j,polygon))
                    {
                        floodFillFromPoint(mask, j, i+1, 2, 0);
                    }
                    else if(fillOutside) //else -> outside, fillOutside -> generally more optimal to fill
                    {
                        floodFillFromPoint(mask, j, i+1, 2, 3);
                    }
                }
                if(mask[i-1][j] == 2)
                {
                    if(isPointInsidePolygon(i-1,j,polygon))
                    {
                        floodFillFromPoint(mask, j, i-1, 2, 0);
                    }
                    else if(fillOutside)
                    {
                        floodFillFromPoint(mask, j, i-1, 2, 3);
                    }
                }
                if(mask[i][j+1] == 2)
                {
                    if(isPointInsidePolygon(i,j+1,polygon))
                    {
                        floodFillFromPoint(mask, j+1, i, 2, 0);
                    }
                    else if(fillOutside)
                    {
                        floodFillFromPoint(mask, j+1, i, 2, 3);
                    }
                }
                if(mask[i][j-1] == 2 && isPointInsidePolygon(i,j-1,polygon))
                {
                    if(isPointInsidePolygon(i,j-1,polygon))
                    {
                        floodFillFromPoint(mask, j-1, i, 2, 0);
                    }
                    else if(fillOutside)
                    {
                        floodFillFromPoint(mask, j-1, i, 2, 3);                        
                    }
                    
                }
            }
        }
    }
}

void floodFillFromPoint( vector< vector< char > >& mask, size_t x, size_t y, char oldColor, char newColor )
{
    //check colours
    if(oldColor == newColor)
    {
        return;
    }
    //create a pair for the starting point of the flood fill operation
    std::pair<size_t&,size_t&> xy(x, y);
    size_t w = mask[0].size();
    size_t h = mask.size();
    
    size_t x1{};
    bool spanAbove{}, spanBelow{};
    
    std::vector< std::pair<size_t, size_t> > linesToScanfill{};
    linesToScanfill.push_back(xy);
    
    while(!linesToScanfill.empty())
    {
        xy = linesToScanfill.back();
        linesToScanfill.pop_back();
        x1 = xy.first;
        
        while(x1 > 0 && mask[y][x1-1] == oldColor)
        {
            --x1;
        }  
        
        spanAbove = spanBelow = false;
        while(x1 < w && mask[y][x1] == oldColor)
        {
            mask[y][x1] = newColor;
            
            if(!spanAbove && y > 0 && mask[y-1][x1] == oldColor)
            {
                linesToScanfill.emplace_back(x1, y-1);
                spanAbove = true;
            }
            else if(spanAbove && y > 0 && mask[y - 1][x1] != oldColor)
            {
                spanAbove = false;
            }
            if(!spanBelow && y + 1 < h && mask[y + 1][x1] == oldColor)
            {
                linesToScanfill.emplace_back(x1, y+1);
                spanBelow = true;
            }
            else if(spanBelow && y + 1 < h && mask[y + 1][x1] != oldColor)
            {
                spanBelow = false;
            }
            x1++;
        }
    }
}

// from geeksforgeeks
// To find orientation of ordered triplet (p, q, r).
// The function returns following values
// 0 --> p, q and r are colinear
// 1 --> Clockwise
// 2 --> Counterclockwise
int orientation(const r_Point& p, const r_Point& q, const r_Point& r)
{
    int val = (q[1] - p[1]) * (r[0] - q[0]) -
              (q[0] - p[0]) * (r[1] - q[1]);
    
    if (val == 0) return 0;  // colinear
    return (val > 0)? 1: 2; // clock or counterclock wise
}

int orientation(const r_Point& p, const r_Point& q, const double x, const double y)
{
    double val = ((double)q[1] - (double)p[1]) * (x - (double)q[0]) -
              ((double)q[0] - (double)p[0]) * (y - (double)q[1]);

    long long int val1 = val*10000000000;
    val = (double)val1 / 10000000000.;
    
    if (val == 0) return 0;  // colinear
    return (val > 0)? 1: 2; // clock or counterclock wise
}

int orientation(const vector< double >& p, const vector< double >& q, const vector< double >& r)
{
    double val = (q[1] - p[1]) * (r[0] - q[0]) -
              (q[0] - p[0]) * (r[1] - q[1]);
 
    long long int val1 = val*10000000000;
    val = (double)val1 / 10000000000.;
    
    if (val == 0) return 0;  // colinear
    return (val > 0)? 1: 2; // clock or counterclock wise
}

int checkPointInsidePolygon( const double x, const double y, const vector< r_Point >& polygon )
{
    unsigned int next;
    int count = 0;
    
    bool checkColinear = false;
    for( unsigned int i=0; i<polygon.size(); i++ )
    {
        next = (i+1) % polygon.size();
        
        if( orientation( polygon[i], polygon[next], x, y ) == 0 )
        {
            // the three points are colinear
            int minX = min(polygon[i][0], polygon[next][0]);
            int maxX = max(polygon[i][0], polygon[next][0]);
            
            if( x >= minX && x <= maxX ) checkColinear = true;
        }
    }
    if( checkColinear ) return 1;
    
    for( unsigned int i=0; i<polygon.size(); i++ )
    {
        next = (i+1) % polygon.size();
            
        if( (y <= polygon[i][1] && y >= polygon[next][1]) || 
                ( y >= polygon[i][1] && y <= polygon[next][1] ) )
        {
            if( x <= polygon[i][0] && x <= polygon[next][0] ) count++;
            else if( x < polygon[i][0] )
            {
                if( polygon[i][1] <= polygon[next][1] )
                {
                    // in this case the orientation of the points next, i, x (in this order) should be clockwise
                    if( orientation(polygon[next], polygon[i], x, y) == 1 ) count++;
                }
                else
                {
                    // in this case the orientation of the points i, next, x (in this order) should be clockwise
                    if( orientation(polygon[i], polygon[next], x, y) == 1 ) count++;
                }
            }
            else if( x < polygon[next][0] )
            {
                if( polygon[i][1] <= polygon[next][1] )
                {
                    // in this case the orientation of the points next, i, x (in this order) should be clockwise
                    if( orientation(polygon[next], polygon[i], x, y) == 1 ) count++;
                }
                else
                {
                    // in this case the orientation of the points i, next, x (in this order) should be clockwise
                    if( orientation(polygon[i], polygon[next], x, y) == 1 ) count++;
                }
            }
        }
    }
    return count;
}

int checkPointInsidePolygon( const r_Point& x, const vector< r_Point >& polygon )
{
    unsigned int next;
    int count = 0;
    
    bool checkColinear = false;
    for( unsigned int i=0; i<polygon.size(); i++ )
    {
        next = (i+1) % polygon.size();
        
        if( orientation( polygon[i], polygon[next], x ) == 0 )
        {
            // the three points are colinear
            int minX = min(polygon[i][0], polygon[next][0]);
            int maxX = max(polygon[i][0], polygon[next][0]);
            
            if( x[0] >= minX && x[0] <= maxX ) checkColinear = true;
        }
    }
    
    if( checkColinear ) return 1;
    
    for( unsigned int i=0; i<polygon.size(); i++ )
    {
        next = (i+1) % polygon.size();
            
        if( x[1] == polygon[i][1] || x[1] == polygon[next][1] )
            return checkPointInsidePolygon( (double)x[0], (double)x[1]+0.01, polygon );
        
        if( (x[1] <= polygon[i][1] && x[1] >= polygon[next][1]) || 
                ( x[1] >= polygon[i][1] && x[1] <= polygon[next][1] ) )
        {
            if( x[0] <= polygon[i][0] && x[0] <= polygon[next][0] ) count++;
            else if( x[0] <= polygon[i][0] )
            {
                if( polygon[i][1] <= polygon[next][1] )
                {
                    // in this case the orientation of the points next, i, x (in this order) should be clockwise
                    if( orientation(polygon[next], polygon[i], x) == 1 ) count++;
                }
                else
                {
                    // in this case the orientation of the points i, next, x (in this order) should be clockwise
                    if( orientation(polygon[i], polygon[next], x) == 1 ) count++;
                }
            }
            else if( x[0] <= polygon[next][0] )
            {
                if( polygon[i][1] <= polygon[next][1] )
                {
                    // in this case the orientation of the points next, i, x (in this order) should be clockwise
                    if( orientation(polygon[next], polygon[i], x) == 1 ) count++;
                }
                else
                {
                    // in this case the orientation of the points i, next, x (in this order) should be clockwise
                    if( orientation(polygon[i], polygon[next], x) == 1 ) count++;
                }
            }
        }
    }
    return count;
}

int checkPointInsidePolygon( const vector< double >& x, const vector< vector< double > >& polygon )
{
    unsigned int next;
    int count = 0;
    
    bool checkColinear = false;
    for( unsigned int i=0; i<polygon.size(); i++ )
    {
        next = (i+1) % polygon.size();
        
        if( orientation( polygon[i], polygon[next], x ) == 0 )
        {
            // the three points are colinear
            double minX = min(polygon[i][0], polygon[next][0]);
            double maxX = max(polygon[i][0], polygon[next][0]);

            if( x[0] >= minX && x[0] <= maxX ) checkColinear = true;
        }
    }
    if( checkColinear ) return 1;
    
    for( unsigned int i=0; i<polygon.size(); i++ )
    {
        next = (i+1) % polygon.size();
        
        if( x[1] == polygon[i][1] || x[1] == polygon[next][1] )
            return checkPointInsidePolygon( {x[0],x[1]+0.01}, polygon );   
            
        if( (x[1] <= polygon[i][1] && x[1] >= polygon[next][1]) || 
                ( x[1] >= polygon[i][1] && x[1] <= polygon[next][1] ) )
        {
            if( x[0] <= polygon[i][0] && x[0] <= polygon[next][0] ) count++;
            else if( x[0] <= polygon[i][0] )
            {
                if( polygon[i][1] <= polygon[next][1] )
                {
                    // in this case the orientation of the points next, i, x (in this order) should be clockwise
                    if( orientation(polygon[next], polygon[i], x) == 1 ) count++;
                }
                else
                {
                    // in this case the orientation of the points i, next, x (in this order) should be clockwise
                    if( orientation(polygon[i], polygon[next], x) == 1 ) count++;
                }
            }
            else if( x[0] <= polygon[next][0] )
            {
                if( polygon[i][1] <= polygon[next][1] )
                {
                    // in this case the orientation of the points next, i, x (in this order) should be clockwise
                    if( orientation(polygon[next], polygon[i], x) == 1 ) count++;
                }
                else
                {
                    // in this case the orientation of the points i, next, x (in this order) should be clockwise
                    if( orientation(polygon[i], polygon[next], x) == 1 ) count++;
                }
            }
        }
    }

    return count;
}

bool isPointInsidePolygon( const int testx, const int testy, const std::vector< r_Point >& polygon)
{
    /*
    Copyright (c) 1970-2003, Wm. Randolph Franklin

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
    to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
    and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
    2. Redistributions in binary form must reproduce the above copyright notice in the documentation and/or other materials provided with the distribution.
    3. The name of W. Randolph Franklin may not be used to endorse or promote products derived from this Software without specific prior written permission. 

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
    */
    
    size_t nvert = polygon.size();
    size_t i, j = 0;
    bool c = false;
    for (i = 0, j = nvert-1; i < nvert; j = i++)
    {
        if ( ((polygon[i][1] > testy) != (polygon[j][1] > testy)) )
        {
            int x = (polygon[j][1]-polygon[i][1]);
            if(x > 0)
            {
                if( x*( testx-polygon[i][0] ) < ( polygon[j][0]-polygon[i][0] )*( testy-polygon[i][1] ) )
                {
                    c = !c;
                }
            }
            else
            {
                if( x*( testx-polygon[i][0] ) > ( polygon[j][0]-polygon[i][0] )*( testy-polygon[i][1] ) )
                {
                    c = !c;
                }
            }
        }
    }
    return c;
}

/*
 * Two segments (p1,q1), (p2,q2) intersect if:
 * - (p1,q1,p2) and (p1,q1,q2) have different orientations and
 * - (p2,q2,p1) and (p2,q2,q1) have different orientations
 * OR
 * - if the segments are colinear and their projections on OX and OY respectively intersect
 */
bool checkSegmentsIntersect( const r_Point& p1, const r_Point& q1, const r_Point& p2, const r_Point& q2 )
{
    if( orientation(p1,q1,p2) == 0 && orientation(p1,q1,q2) == 0 )
    {
        // the points are colinear
        // check if p2 is inside (p1,q1)
        if( p2[0] >= min(p1[0],q1[0]) && p2[0] <= max(p1[0],q1[0]) &&
            p2[1] >= min(p1[1],q1[1]) && p2[1] <= max(p1[1],q1[1]) ) return true;
        // check if q2 is inside (p1,q1)
        if( q2[0] >= min(p1[0],q1[0]) && q2[0] <= max(p1[0],q1[0]) &&
            q2[1] >= min(p1[1],q1[1]) && q2[1] <= max(p1[1],q1[1]) ) return true;
        // check if p1 is inside (p2,q2)
        if( p1[0] >= min(p2[0],q2[0]) && p1[0] <= max(p2[0],q2[0]) &&
            p1[1] >= min(p2[1],q2[1]) && p1[1] <= max(p2[1],q2[1]) ) return true;
        return false;
    }
    if( orientation(p1,q1,p2) != orientation(p1,q1,q2) &&
        orientation(p2,q2,p1) != orientation(p2,q2,q1) ) return true;
    return false;
}

// take v1 as the left-down vertex of the square
// and v2 as the right-up vertex of the square !!!
void checkSquare( const r_Point& v1, const r_Point& v2, const vector< r_Point >& polygon, vector< vector< char > >& mask )
{
    if( abs(v1[0]-v2[0]) <=2 || abs(v1[1]-v2[1]) <= 2 )
    {
        for( auto i = v1[0]; i <= v2[0]; i++ )
        {
            for( auto j = v1[1]; j <= v2[1]; j++ )
            {
                if( checkPointInsidePolygon( r_Point(i,j), polygon ) % 2 )
                {
                    mask[static_cast<size_t>(i)][static_cast<size_t>(j)] = 1;
                }
            }
        }
        return;
    }
    
    bool doIntersect = false;
    
    // first we check if there is any edge of the square which intersects any edge of the polygon
    // if this happens then the square and the polygon intersect and we split the square in 4
    // otherwise the square is completely outside or inside the polygon
    // => it is enough to check only one vertex if it is inside or outside
    // if it is outside then we check if the polygon is completely inside the square and split the square again
    // otherwise the polygon and the square don't intersect and we do not count that square
    for( unsigned int i=0; i<polygon.size(); i++ )
    {
        unsigned int next = (i+1)%polygon.size();
        doIntersect = checkSegmentsIntersect(polygon[i], polygon[next], v1, r_Point(v2[0],v1[1])) ||
                      checkSegmentsIntersect(polygon[i], polygon[next], r_Point(v2[0],v1[1]), v2) ||
                      checkSegmentsIntersect(polygon[i], polygon[next], v2, r_Point(v1[0],v2[1])) ||
                      checkSegmentsIntersect(polygon[i], polygon[next], r_Point(v1[0],v2[1]), v1) ||
                      doIntersect;
    }
    
    if( doIntersect || (checkPointInsidePolygon( v1, polygon ) % 2 == 1) ||
            (checkPointInsidePolygon( polygon[0], {v1, r_Point(v2[0],v1[1]), v2, r_Point(v1[0],v2[1])}) % 2 == 1) )
    {
        // split the square in four and check each square
        int midX = v1[0] + (v2[0]-v1[0])/2;
        int midY = v1[1] + (v2[1]-v1[1])/2;
        
        checkSquare( v1, r_Point( midX, midY ), polygon, mask );
        checkSquare( r_Point( midX, v1[1] ), r_Point( v2[0], midY ), polygon, mask );
        checkSquare( r_Point( midX, midY ), v2, polygon, mask );
        checkSquare( r_Point( v1[0], midY ), r_Point( midX, v2[1] ), polygon, mask );
    }
}

vector< double > changePointTo2D( const r_Point& x, const vector< r_Point >& polygon )
{
    r_Point a = polygon[0];
    r_Point b = polygon[1];
    r_Point c = polygon[2];
    
    double t = ((double)(c[0]-a[0])*(double)(b[0]-a[0]) + (double)(c[1]-a[1])*(double)(b[1]-a[1]) + (double)(c[2]-a[2])*(double)(b[2]-a[2])) /
                ((double)(b[0]-a[0])*(double)(b[0]-a[0]) + (double)(b[1]-a[1])*(double)(b[1]-a[1])+(double)(b[2]-a[2])*(double)(b[2]-a[2]));
    
    vector<double> origin;
    origin.push_back(a[0] + t*(double)(b[0]-a[0]));
    origin.push_back(a[1] + t*(double)(b[1]-a[1]));
    origin.push_back(a[2] + t*(double)(b[2]-a[2]));
    
    double moduleRight = sqrt((origin[0] - c[0])*(origin[0] - c[0]) + (origin[1] - c[1])*(origin[1] - c[1]) + (origin[2] - c[2])*(origin[2] - c[2]));
    double moduleUp = sqrt((origin[0] - a[0])*(origin[0] - a[0]) + (origin[1] - a[1])*(origin[1] - a[1]) + (origin[2] - a[2])*(origin[2] - a[2]));
    
    vector<double> right;
    vector<double> up;
    
    right.push_back( (origin[0]-c[0])/moduleRight );
    right.push_back( (origin[1]-c[1])/moduleRight );
    right.push_back( (origin[2]-c[2])/moduleRight );
    
    up.push_back( (origin[0]-a[0])/moduleUp );
    up.push_back( (origin[1]-a[1])/moduleUp );
    up.push_back( (origin[2]-a[2])/moduleUp );
    
    vector<double> d;
    d.push_back( x[0]-origin[0] );
    d.push_back( x[1]-origin[1] );
    d.push_back( x[2]-origin[2] );

    double u = d[0]*right[0] + d[1]*right[1] + d[2]*right[2];
    double v = d[0]*up[0] + d[1]*up[1] + d[2]*up[2];
    
    // I put a limit of 10 decimals for my results, because the double values might not be 100% exact
    long long int u1 = u*10000000000;
    u = (double)u1 / 10000000000.;
    long long int v1 = v*10000000000;
    v = (double)v1 / 10000000000.;
    
    vector<double> result = {u,v};
    return result;
}


vector< double > changePointTo2D( const vector< double >& x, const vector< r_Point >& polygon )
{
    r_Point a = polygon[0];
    r_Point b = polygon[1];
    r_Point c = polygon[2];
    
    double t = ((double)(c[0]-a[0])*(double)(b[0]-a[0]) + (double)(c[1]-a[1])*(double)(b[1]-a[1]) + (double)(c[2]-a[2])*(double)(b[2]-a[2])) /
                ((double)(b[0]-a[0])*(double)(b[0]-a[0]) + (double)(b[1]-a[1])*(double)(b[1]-a[1])+(double)(b[2]-a[2])*(double)(b[2]-a[2]));
    
    vector<double> origin;
    origin.push_back((double)a[0] + t*(double)(b[0]-a[0]));
    origin.push_back((double)a[1] + t*(double)(b[1]-a[1]));
    origin.push_back((double)a[2] + t*(double)(b[2]-a[2]));
        
    double moduleRight = sqrt((origin[0] - c[0])*(origin[0] - c[0]) + (origin[1] - c[1])*(origin[1] - c[1]) + (origin[2] - c[2])*(origin[2] - c[2]));
    double moduleUp = sqrt((origin[0] - a[0])*(origin[0] - a[0]) + (origin[1] - a[1])*(origin[1] - a[1]) + (origin[2] - a[2])*(origin[2] - a[2]));
    
    vector<double> right;
    vector<double> up;
    
    right.push_back( (origin[0]-c[0])/moduleRight );
    right.push_back( (origin[1]-c[1])/moduleRight );
    right.push_back( (origin[2]-c[2])/moduleRight );
    
    up.push_back( (origin[0]-a[0])/moduleUp );
    up.push_back( (origin[1]-a[1])/moduleUp );
    up.push_back( (origin[2]-a[2])/moduleUp );
    
    vector<double> d;
    d.push_back( x[0]-origin[0] );
    d.push_back( x[1]-origin[1] );
    d.push_back( x[2]-origin[2] );

    double u = d[0]*right[0] + d[1]*right[1] + d[2]*right[2];
    double v = d[0]*up[0] + d[1]*up[1] + d[2]*up[2];
    
    // I put a limit of 10 decimals for my results, because the double values might not be 100% exact
    long long int u1 = u*10000000000;
    u = (double)u1 / 10000000000.;
    long long int v1 = v*10000000000;
    v = (double)v1 / 10000000000.;
    
    vector<double> result = {u,v};
    return result;
}

vector< vector< double > > changePolygonTo2D( const vector< r_Point >& polygon )
{
    vector<vector<double>> result;
    for( unsigned int i = 0; i < polygon.size(); i++ )
    {
        result.push_back( changePointTo2D( polygon[i], polygon ) );
    }
    return result;
}

// take v1 as the left-down vertex of the cube
// and v2 as the right-up vertex of the cube !!!
void checkCube( const r_Point& v1, const r_Point& v2, const vector< r_Point >& polygon, map< r_Point, bool, classcomp >& result )
{   
    //require vector to have at least 3 points
    
    if(polygon.size() < 3){
        throw r_Error(INCORRECTPOLYGON);
    }
    int a = v1[0];
    int b = v1[1];
    int c = v1[2];
    int x = v2[0];
    int y = v2[1];
    int z = v2[2];
      
    // we will compute the equation of the plane using the points polygon[0], polygon[1], polygon[2]
    // and then we will remove all cubes that don't intersect the plane
    // https://www.maplesoft.com/support/help/maple/view.aspx?path=MathApps%2FEquationofaPlane3Points
    
    int x01 = polygon[1][0] - polygon[0][0];
    int y01 = polygon[1][1] - polygon[0][1];
    int z01 = polygon[1][2] - polygon[0][2];
    
    int x02 = polygon[2][0] - polygon[0][0];
    int y02 = polygon[2][1] - polygon[0][1];
    int z02 = polygon[2][2] - polygon[0][2];
    
    // The equation of the plane will be: x*eX + y*eY + z*eZ + eD = 0
    int eX = y01*z02 - z01*y02;
    int eY = z01*x02 - x01*z02;
    int eZ = x01*y02 - y01*x02;
    int eD = -(eX*polygon[0][0] + eY*polygon[0][1] + eZ*polygon[0][2]);
    
    //next, we verify that the points in our polygon are actually coplanar
    auto nextPoint = polygon.begin();
    while( nextPoint != polygon.end() ) {
        const r_Point & refToCoords = *nextPoint;
        if (abs(refToCoords[0] * eX + refToCoords[1] * eY + refToCoords[2] * eZ + eD) >= 2) {
            throw r_Error(GRIDVERTICESNOTCOPLANAR);
            break;
        }
        nextPoint++;
    }

    
    vector<vector<double>> polygon2D = changePolygonTo2D( polygon );
    
    if( abs(x-a) <= 1 || abs(y-b) <= 1 || abs(z-c) <= 1 )
    {
        for( int i = a; i <= x; i++ )
            for( int j = b; j <= y; j++ )
                for( int k = c; k <= z; k++ )
                {
                    if( i*eX + j*eY + k*eZ + eD == 0 )
                    {
                        // we know that the point (i,j,k) is in the same plane with the polygon
                        // and we want to check if it is inside the polygon
                        vector<double> point2D = changePointTo2D( r_Point(i,j,k), polygon );

                        if( checkPointInsidePolygon( point2D, polygon2D ) % 2 )
                        {
                            if( result.find(r_Point(i,j,k)) == result.end() ) result.insert(pair<r_Point,bool>(r_Point(i,j,k),true));
                        }
                    }
                    else 
                    {
                        // or we can check if it is at a distance smaller than 0.5 from the polygon on any of the axes
                        if( ((i*eX + j*eY + k*eZ + eD) * ((i+0.5)*eX + j*eY + k*eZ + eD) <= 0) ||
                            ((i*eX + j*eY + k*eZ + eD) * ((i-0.5)*eX + j*eY + k*eZ + eD) <= 0) || 
                            ((i*eX + j*eY + k*eZ + eD) * (i*eX + (j+0.5)*eY + k*eZ + eD) <= 0) || 
                            ((i*eX + j*eY + k*eZ + eD) * (i*eX + (j-0.5)*eY + k*eZ + eD) <= 0) ||
                            ((i*eX + j*eY + k*eZ + eD) * (i*eX + j*eY + (k+0.5)*eZ + eD) <= 0) ||
                            ((i*eX + j*eY + k*eZ + eD) * (i*eX + j*eY + (k-0.5)*eZ + eD) <= 0) )
                        {
                            // get the equation of the line that is perpendicular on the plane and goes through (i,j,k)
                            /*
                             * x = eX*t + i
                             * y = eY*t + j
                             * z = eZ*t + k
                             * => t = -(eX*i+eY*j+eZ*k+eD)/(eX^2+eY^2+eZ^2)
                             * => we find the point in the plane by replacing t
                             * And then we check for that point if it is inside the polygon or not
                             */
                            double t = -(double)(eX*i+eY*j+eZ*k+eD)/(double)(eX*eX+eY*eY+eZ*eZ);
                            vector<double> point;
                            point.push_back(eX*t+i);
                            point.push_back(eY*t+j);
                            point.push_back(eZ*t+k);

                            vector<double> point2D = changePointTo2D( point, polygon );
                            if( checkPointInsidePolygon( point2D, polygon2D ) % 2 )
                            {
                                if( result.find(r_Point(i,j,k)) == result.end() ) result.insert(pair<r_Point,bool>(r_Point(i,j,k),true));
                            }
                        }
                    }
                }
        return;
    }
    
    
    // check if all vertices of the cube are on the same side of the plane
    if( !(((a*eX + b*eY + c*eZ + eD > 0) == (x*eX + b*eY + c*eZ + eD > 0)) &&
         ((a*eX + b*eY + c*eZ + eD > 0) == (x*eX + b*eY + z*eZ + eD > 0)) &&
         ((a*eX + b*eY + c*eZ + eD > 0) == (a*eX + b*eY + z*eZ + eD > 0)) &&
         ((a*eX + b*eY + c*eZ + eD > 0) == (a*eX + y*eY + c*eZ + eD > 0)) &&
         ((a*eX + b*eY + c*eZ + eD > 0) == (x*eX + y*eY + c*eZ + eD > 0)) &&
         ((a*eX + b*eY + c*eZ + eD > 0) == (x*eX + y*eY + z*eZ + eD > 0)) &&
         ((a*eX + b*eY + c*eZ + eD > 0) == (a*eX + y*eY + z*eZ + eD > 0))) )
    {
        int midX = a + (x-a)/2;
        int midY = b + (y-b)/2;
        int midZ = c + (y-c)/2;
        
        checkCube( v1, r_Point(midX,midY,midZ), polygon, result );
        checkCube( r_Point(midX,b,c), r_Point(x,midY,midZ), polygon, result );
        checkCube( r_Point(midX,b,midZ), r_Point(x,midY,z), polygon, result );
        checkCube( r_Point(a,b,midZ), r_Point(midX,midY,z), polygon, result );
        
        checkCube( r_Point(a,midY,c), r_Point(midX,y,midZ), polygon, result );
        checkCube( r_Point(midX,midY,c), r_Point(x,y,midZ), polygon, result );
        checkCube( r_Point(midX,midY,midZ), r_Point(x,y,z), polygon, result );
        checkCube( r_Point(a,midY,midZ), r_Point(midX,y,z), polygon, result );
        return;
    }

    // check if any of the vertices is inside the plane
    if( (a*eX + b*eY + c*eZ + eD == 0) || (x*eX + b*eY + c*eZ + eD == 0) ||
        (x*eX + b*eY + z*eZ + eD == 0) || (a*eX + b*eY + z*eZ + eD == 0) ||
        (a*eX + y*eY + c*eZ + eD == 0) || (x*eX + y*eY + c*eZ + eD == 0) ||
        (x*eX + y*eY + z*eZ + eD == 0) || (a*eX + y*eY + z*eZ + eD == 0) )
    {
        int midX = a + (x-a)/2;
        int midY = b + (y-b)/2;
        int midZ = c + (y-c)/2;
        
        checkCube( v1, r_Point(midX,midY,midZ), polygon, result );
        checkCube( r_Point(midX,b,c), r_Point(x,midY,midZ), polygon, result );
        checkCube( r_Point(midX,b,midZ), r_Point(x,midY,z), polygon, result );
        checkCube( r_Point(a,b,midZ), r_Point(midX,midY,z), polygon, result );
        
        checkCube( r_Point(a,midY,c), r_Point(midX,y,midZ), polygon, result );
        checkCube( r_Point(midX,midY,c), r_Point(x,y,midZ), polygon, result );
        checkCube( r_Point(midX,midY,midZ), r_Point(x,y,z), polygon, result );
        checkCube( r_Point(a,midY,midZ), r_Point(midX,y,z), polygon, result );
        return;
    }
}
