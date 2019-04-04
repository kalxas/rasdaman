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



/*
 * File:   qtFindSection.hh
 * Author: bbell
 *
 * Created on September 26, 2017, 4:32 PM
 */

#ifndef QTFINDSECTION_HH
#define QTFINDSECTION_HH

#include "raslib/point.hh"
#include "qlparser/qtpointdata.hh"
#include "raslib/pointdouble.hh"
#include "raslib/minterval.hh"

#include <iostream>
#include <string>
#include <vector>
#include <memory>

/*
    let $S \subset \mathbb{R}^n$ be a codimension $k$ affince subspace.

    the goal of this class is to compute $s: \mathbb{R}^{n-k} \rightarrow \mathbb{R}^n$
    where $s$ is the unique function such that $\pi: \mathbb{R}^n \rightarrow \mathbb{R}^{n-k}$
    commutes with $s$ with $Im(s) = S$.

    We require data defining $S$ to be of the form $Ax = b$ iff $x \in S$, where
    $A \in \mathrm{Mat}_{k, n}(\mathbb{R})$ and $b \in \mathbb{R}^k$. This is passed as
    a pair, linEqnArg, to the constructor.

    We also require data defining which dimensions are kept or which are projected away.
    By convenction, our constructor assumes that it receives the kept dimensions. It is passed
    as a vector of r_Dimension called keptDimsArg.
*/

/*
    Using this class:
        construct
        call prepareSection to initialize most data
        call solveLU on the points you want to compute
 */

class FindSection
{
public:
    //constructor
    FindSection(const std::vector< std::pair< r_PointDouble, double >> &linEqnArg, const vector<r_Dimension> &keptDimsArg);

    void prepareSection();

    //applies the function $(A_I)^{-1}( b - A_J(arg) )$ to get the vector of ints in the image
    //the employed method is to actually solve A_I(x) = b - A_J(arg) for x using an LU factorization of A_I.
    //the LU factorization of A_I is found during construction of this class object.
    r_PointDouble solveLU(const r_Point &arg);

private:
    //constructs the matrices $A_I$ and $A_J$ from $A$
    void decomposeAIJ();

    //inverts matrix $A_I \in \mathrm{Mat}_{k,k}(\mathbb{R})$
    void factorAI();

    //computes the RHS of the equation to $A_I(x) = (b - A_J(arg))$
    //the point of this is to simplify the appearance of applyFxn
    r_PointDouble findRHS(const r_Point &arg);

    //initialized as args of constructor

    //linEqn.first = LHS (A); linEqn.second = RHS (b)
    std::pair< vector< vector<double>>, vector<double>> linEqn; //Ax = b
    //the dimensions projected onto
    vector<r_Dimension> keptDims;

    //initialized during constructor call

    //the projected dimensions
    vector<r_Dimension> lostDims;

    //the matrices $A_I$ and $A_J$, respectively
    vector<vector<double>> matAI;
    vector<vector<double>> matAJ;

    //LU decomposition of A_I
    vector<vector<double>> L;
    vector<vector<double>> U;

    //for applying the function quickly
    vector<double> op1;
    vector< vector<double>> op2;
};

#endif  /* QTFINDSECTION_HH */

