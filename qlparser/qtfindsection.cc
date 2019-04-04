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

#include "qlparser/qtfindsection.hh"

FindSection::FindSection(const vector< std::pair< r_PointDouble, double >> &linEqnArg, const vector<r_Dimension> &keptDimsArg)
    : keptDims(keptDimsArg)
{
    //target type       std::pair< vector< vector<double> >, vector<double> >
    //argument type     vector< std::pair< r_PointDouble, double >
    linEqn.first.reserve(linEqnArg.size());
    linEqn.second.reserve(linEqnArg.size());
    //initialization tool for a vector of vectors
    std::vector<double> emptyVector;
    for (size_t i = 0; i < linEqnArg.size(); i++) //iterate over the rows
    {
        //add a row
        linEqn.second.emplace_back(linEqnArg[i].second);
        linEqn.first.emplace_back(emptyVector);
        //resize row
        linEqn.first[i].reserve(linEqnArg[0].first.dimension());
        //initialize row
        for (size_t j = 0; j < linEqnArg[0].first.dimension(); j++)
        {
            linEqn.first[i].emplace_back(linEqnArg[i].first[j]);
        }
    }
}

void
FindSection::prepareSection()
{
    //determine the lost dimensions, for simplifying loops later
    size_t keptDimPos = 0;
    lostDims.reserve(linEqn.first[0].size() - keptDims.size());
    //assumes increasing order for keptDims
    for (r_Dimension i = 0; i < linEqn.first[0].size(); i++)
    {
        if (i != keptDims[keptDimPos])
        {
            lostDims.emplace_back(i);
        }
        else
        {
            keptDimPos++;
        }
    }
    //decompose the (k,n) matrix into a pair of (k, k) and (k, n-k) matrices
    decomposeAIJ();
    //factor the (k,k) matrix into a product of lower left triangular
    //and upper right triangular matrices.
    factorAI();
}
void
FindSection::decomposeAIJ()
{
    //#rows = codimension = lostDims.size()
    matAJ.reserve(lostDims.size());
    matAI.reserve(lostDims.size());

    std::vector<double> emptyVector;
    //reserve data for each row
    for (size_t i = 0; i < lostDims.size(); i++)
    {
        matAJ.emplace_back(emptyVector);
        matAJ[i].reserve(keptDims.size());
        matAI.emplace_back(emptyVector);
        matAI[i].reserve(lostDims.size());
    }

    for (size_t j = 0; j < linEqn.first.size(); j++) //iterate over rows
    {
        size_t keptDimPos = 0;
        for (size_t i = 0; i < linEqn.first[j].size(); i++) //iterate over columns
        {
            if (i == keptDims[keptDimPos])
            {
                matAJ[j].emplace_back(linEqn.first[j][i]);
                keptDimPos++;
            }
            else
            {
                matAI[j].emplace_back(linEqn.first[j][i]);
            }
        }
    }
}

void
FindSection::factorAI()
{
    //allocate and initialize L and U
    L.reserve(lostDims.size());
    U.reserve(lostDims.size());

    std::vector<double> emptyVector;
    for (size_t i = 0; i < lostDims.size(); i++)
    {
        L.emplace_back(emptyVector);
        L[i].reserve(lostDims.size());
        U.emplace_back(emptyVector);
        U[i].reserve(lostDims.size());
        for (size_t j = 0; j < lostDims.size(); j++)
        {
            L[i].emplace_back(0);
            if (j != i)
            {
                U[i].emplace_back(0);
            }
            else
            {
                U[i].emplace_back(1);
            }
        }
    }

    //LU factorization of matAI into L and U
    for (size_t i = 0; i < lostDims.size(); i++)
    {
        for (size_t j = 0; j < i; j++)
        {
            L[j][i] = 0;
            U[i][j] = 0;
        }
        for (size_t j = i; j < lostDims.size(); j++)
        {
            L[j][i] = matAI[j][i];
            for (size_t k = 0; k < i; k++)
            {
                L[j][i] = L[j][i] - L[j][k] * U[k][i];
            }
        }
        for (size_t j = i + 1; j < lostDims.size(); j++)
        {
            U[i][j] = matAI[i][j] / L[i][i];

            for (size_t k = 0; k < i; k++)
            {
                U[i][j] = U[i][j] - ((L[i][k] * U[k][j]) / L[i][i]);
            }
        }
    }
}

r_PointDouble
FindSection::findRHS(const r_Point &arg)
{
    //allocate the # dims for the result r_Point
    r_PointDouble result(lostDims.size());

    //loop over the rows of b and A_J
    for (size_t i = 0; i < lostDims.size(); i++)
    {
        result[i] = linEqn.second[i];
        //loop over the columns of A_J
        for (size_t j = 0; j < keptDims.size(); j++)
        {
            result[i] -= matAJ[i][j] * arg[j];
        }
    }

    return result;
}

r_PointDouble
FindSection::solveLU(const r_Point &arg)
{
    //first, we find the RHS of A_I (x) = b - A_J(arg)
    r_PointDouble rhs = findRHS(arg);

    //using forward substitution, we find z in L(z) = b - A_J(arg)
    //this z will become the new RHS of U(x) = z, where this x is the same
    //as that in the central eq'n
    r_PointDouble nextrhs(lostDims.size());

    for (size_t i = 0; i < lostDims.size(); i++)
    {
        double sum = 0;
        for (size_t p = 0; p < i; p++)
        {
            sum += L[i][p] * nextrhs[p];
        }
        nextrhs[i] = (rhs[i] - sum) / L[i][i];
    }

    //using reverse substitution, we find x in U(x) = z
    //this is the result of A_I(x) = b - A_J(arg)

    //TODO(bbell): there seems to be a problem with r_PointDouble in this situation, which causes segfaults...
    r_PointDouble preresult(lostDims.size());

    //funny loop allows us to descend with unsigned and halt while still running the 0 case
    for (size_t i = lostDims.size(); i-- > 0;)
    {
        double sum = 0;
        for (size_t p = lostDims.size() - 1; p > i; p--)
        {
            sum += U[i][p] * preresult[p];
        }
        preresult[i] = (nextrhs[i] - sum) / U[i][i];
    }

    //we actually want our final result to appear as a vector in $\mathbb{R}^n$
    //so we assemble it from the original arg & the resulting x.
    r_PointDouble result(linEqn.first[0].size());
    size_t keptDimPos = 0;
    r_Dimension argIndex = 0;
    r_Dimension preresultIndex = 0;
    for (size_t i = 0; i < linEqn.first[0].size(); i++)
    {
        if (keptDims[keptDimPos] != i) //this optimization assumes codimension < overall dimension
        {
            result[i] = preresult[preresultIndex];
            preresultIndex++;
        }
        else
        {
            result[i] = arg[argIndex];
            argIndex++;
            keptDimPos++;
        }
    }
    return result;
}