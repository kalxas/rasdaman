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
/**
 * SOURCE: mitera.cc
 *
 * MODULE: raslib
 * CLASS:  r_MiterArea
 *
*/

#include "raslib/miterf.hh"
#include "raslib/minterval.hh"
#include <ostream>

const int     r_FixedPointNumber::FIXPREC  = 30;
const r_Range r_FixedPointNumber::carryPos = 1 << FIXPREC;
const r_Range r_FixedPointNumber::fracMask = carryPos - 1;
const double  r_FixedPointNumber::fixOne   = pow(2, static_cast<double>(FIXPREC));

r_MiterFloat::r_MiterFloat(r_Bytes srcCellSize, const char* srcTile,
                           const r_Minterval &tileDomain,
                           const r_Minterval &srcDomain, const r_Minterval &destDomain)
{
    dim = srcDomain.dimension();
    iterDesc = new iter_desc[dim];
    firstCell        = srcTile;

    auto step = static_cast<r_Range>(srcCellSize);
    auto *id = iterDesc + dim - 1;

    for (int j = static_cast<int>(dim) - 1; j >= 0; j--, id--)
    {
        const auto i = static_cast<r_Dimension>(j);
        const auto srcLow = static_cast<double>(srcDomain[i].low());
        const auto destExtent = destDomain[i].high() - destDomain[i].low() + 1;
        id->min       = srcLow;
        id->step      = (srcDomain[i].high() - srcLow + 1) / destExtent;
        id->maxSteps  = destExtent;
        id->dimStep   = step;
        id->scaleStep = step * id->step.getIntPart();
        firstCell     += step * (srcDomain[i].low() - tileDomain[i].low());
        step          *= tileDomain[i].high() - tileDomain[i].low() + 1;
    }
    reset();
}

r_MiterFloat::~r_MiterFloat()
{
    delete[] iterDesc;
    iterDesc = NULL;
}

void r_MiterFloat::reset()
{
    for (r_Dimension i = 0; i < dim; i++)
    {
        iterDesc[i].pos  = iterDesc[i].min;
        iterDesc[i].cell = const_cast<char *>(firstCell);
        iterDesc[i].countSteps = iterDesc[i].maxSteps;
    }
    done = false;
}


r_FixedPointNumber::r_FixedPointNumber(const double &d)
{
    init(d);
}
r_FixedPointNumber &r_FixedPointNumber::operator=(const double &d)
{
    init(d);
    return *this;
}
void r_FixedPointNumber::init(const double &d)
{
    intPart = static_cast<r_Range>(d);
    fracPart = static_cast<r_Range>(fmod(fixOne * d, fixOne));
}

std::ostream &operator<<(std::ostream &os, r_FixedPointNumber &f)
{
    os << '(' << f.intPart << ':' << f.fracPart << ')';
    return os;
}
