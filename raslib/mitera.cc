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

#include "raslib/mitera.hh"
#include "raslib/minterval.hh"
#include "raslib/error.hh"
#include <logging.hh>

r_MiterArea::r_MiterArea(const r_Minterval *newIterDom,
                         const r_Minterval *newImgDom)
    : iterDom(newIterDom), imgDom(newImgDom), done(false)
{
    if (imgDom->dimension() != iterDom->dimension())
    {
        LERROR << "dimension mismatch between " << iterDom << " and " << imgDom;
        throw r_Error(INTERVALSWITHDIFFERENTDIMENSION);
    }
    if (!imgDom->is_origin_fixed() || !imgDom->is_high_fixed())
    {
        LERROR << imgDom << " is opened.";
        throw r_Error(INTERVALOPEN);
    }
    if (!iterDom->is_origin_fixed() || !iterDom->is_high_fixed())
    {
        LERROR << iterDom << " is opened.";
        throw r_Error(INTERVALOPEN);
    }

    // dimensionality of both iterDom and imgDom
    const auto dim = imgDom->dimension();
    // stores the increments
    incArrIter = new incArrElem[dim];

    for (r_Dimension i = 0; i < dim; i++)
    {
        // used for counting in iteration, initialize with 0
        incArrIter[i].curr = 0;
        // how often is the iterDom moved inside the imgDom
        const auto imgExtent = imgDom->get_extent()[i];
        const auto iterExtent = iterDom->get_extent()[i];
        incArrIter[i].repeat = static_cast<int>(
                    (imgExtent / iterExtent) + (imgExtent % iterExtent != 0));

        //LTRACE << "repeat dim " << i << ": " << incArrIter[i].repeat;
    }
    reset();
}


r_MiterArea::~r_MiterArea()
{
    delete [] incArrIter;
}

void
r_MiterArea::reset()
{
    done = false;
    for (unsigned int i = 0; i < iterDom->dimension(); i++)
        incArrIter[i].curr = 0;
}

r_Minterval
r_MiterArea::nextArea()
{
    if (done)
        return retVal;

    // calculate new result domain here
    r_Minterval currDom(iterDom->dimension());
    if (!done)
    {
        for (r_Dimension i = 0; i < iterDom->dimension(); i++)
        {
            const auto imgAxis = (*imgDom)[i];
            const auto iterExtent = iterDom->get_extent()[i];
            currDom << r_Sinterval(imgAxis.low() + incArrIter[i].curr * iterExtent,
                                   imgAxis.low() + (incArrIter[i].curr + 1) * iterExtent - 1);
        }
    }
    retVal = currDom.intersection_with(*imgDom);

    // increment dimensions
    r_Dimension i;
    for (i = 0; i < iterDom->dimension(); i++)
    {
        incArrIter[i].curr++;
        if (incArrIter[i].curr < incArrIter[i].repeat)
            break;                  // no overflow in this dimension
        else
            incArrIter[i].curr = 0; // overflow in this dimension
    }
    if (i == iterDom->dimension())
        done = true;                // overflow in last dimension

    return retVal;
}

bool
r_MiterArea::isDone()
{
    return done;
}
