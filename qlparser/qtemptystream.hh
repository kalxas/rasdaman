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

//@ManMemo: Module: {\bf qlparser}

/**

 The class QtEmptyStream represents an omitted FROM clause in a query.
 It returns one empty tuple to the output stream.

*/

#ifndef _QTEMPTYSTREAM_
#define _QTEMPTYSTREAM_

#include "qlparser/qtoncstream.hh"

class QtEmptyStream : public QtONCStream
{
public:
    QtEmptyStream();

    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    virtual void open();
    QtDataList *next();
    virtual void close();
    virtual void reset();

    inline virtual QtNodeType getNodeType() const;
    virtual const QtTypeTuple &checkType();

private:
    bool emitted;

    static const QtNodeType nodeType;
};

#include "qlparser/qtemptystream.icc"

#endif // _QTEMPTYSTREAM_
