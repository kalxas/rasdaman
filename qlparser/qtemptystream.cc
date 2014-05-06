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

#include "qlparser/qtemptystream.hh"

const QtNode::QtNodeType QtEmptyStream::nodeType = QtNode::QT_EMPTY_STREAM;

QtEmptyStream::QtEmptyStream()
    : emitted(false)
{
    dataStreamType = QtTypeTuple(0);
}

void
QtEmptyStream::printTree(int tab, std::ostream& s, QtChildType mode)
{
    s << SPACE_STR(tab).c_str() << "QtEmptyStream Object: " << std::flush;
    s << getEvaluationTime();
    s << std::endl;
}

void
QtEmptyStream::printAlgebraicExpression(std::ostream& s)
{
    s << "<empty set>";
}

void
QtEmptyStream::open()
{
    emitted = false;
}

QtNode::QtDataList*
QtEmptyStream::next()
{
    if (emitted) {
        return NULL;
    } else {
        emitted = true;
        QtNode::QtDataList *result = new QtDataList(1, NULL);
        return result;
    }
}

void
QtEmptyStream::close()
{
    /* no-op */
}

void
QtEmptyStream::reset()
{
    emitted = false;
}

const QtTypeTuple&
QtEmptyStream::checkType()
{
    return dataStreamType;
}
