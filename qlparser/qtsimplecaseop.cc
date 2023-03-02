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
 *      Class implementation. For further information see qlparser/qtsimplecaseop.hh
 *
 * COMMENTS:
 *
 ************************************************************/

#include "qlparser/qtsimplecaseop.hh"
#include "qlparser/qtcaseequality.hh"

/**
 * Constructor taking the operation list.
 *
 * @param opList the arguments of the operation.
 */
QtSimpleCaseOp::QtSimpleCaseOp(QtOperationList *opList)
    : QtCaseOp(opList) {}

/**
 * Destructor override.
 */
QtSimpleCaseOp::~QtSimpleCaseOp()
{
    //iterate through the operation list and mark the common
    //operand for the equality operation as deleted in all but the first
    //equality.
    // the conditions are on even positions in the operation list, except for the last operation
    for (size_t i = 0; i < operationList->size(); ++i)
    {
        if (i > 0 && i % 2 == 0 && i != operationList->size() - 1)
        {
            ((QtCaseEquality *)operationList->at(i))->setCommonOperandDeleted(true);
        }
    }
}
