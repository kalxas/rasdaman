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

#ifndef _ALGEBRAOPS_HH_
#define _ALGEBRAOPS_HH_

#include "catalogmgr/ops.hh"

#include <vector>
#include <string>

// forward declarations
class QtOperation;
class QtData;
class QtMDD;
class r_Minterval;

//@ManMemo: Module: {\bf catalogif}

/*@Doc:

  Operation object for marray contstructor of the query language.

*/
/**
  * \defgroup Catalogmgrs Catalogmgr Classes
  */

/**
  * \ingroup Catalogmgrs
  */
class QLMarrayOp : public MarrayOp
{
public:
    /// constructor
    QLMarrayOp(QtOperation *newCellExpression, std::vector<QtData *> *newDataList,
               std::string &newIteratorName,
               const BaseType *newResType, unsigned int newResOff = 0);
    /**
      Constructor gets cell expression pointer, data vector for bounded variables,
      cell type, and type offset
    */

    /// virtual destructor
    virtual ~QLMarrayOp();

    /// operator that carries out the cell expression on point <tt>p</tt>.
    virtual void operator()(char *result, const r_Point &p);

private:
    /// pointer to the cell expression
    QtOperation *cellExpression;

    /// pointer to data vector
    std::vector<QtData *> *dataList;

    /// name of the iterator
    std::string iteratorName;
};

//@ManMemo: Module: {\bf catalogif}

/*@Doc:

  Operation object for condenser operation of the query language.

*/

class QLCondenseOp : public GenCondenseOp
{
public:
    /// constructor
    QLCondenseOp(QtOperation *newCellExpression,
                 QtOperation *newCondExpression,
                 std::vector<QtData *> *newDataList,
                 std::string &newIteratorName,
                 const BaseType *newResType,
                 unsigned int newResOff,
                 BinaryOp *newAccuOp,
                 char *newInitVal = 0);
    /**
      Constructor gets cell expression pointer, cell condition expression pointer,
      data vector for bounded variables, cell type, and type offset
    */

    /// virtual destructor
    virtual ~QLCondenseOp();

    /// operator that carries out the cell expression on point <tt>p</tt>.
    virtual void operator()(const r_Point &p);

private:
    /// pointer to the cell expression
    QtOperation *cellExpression;

    /// pointer to the condition expression
    QtOperation *condExpression;

    /// pointer to data vector
    std::vector<QtData *> *dataList;

    /// name of the iterator
    std::string iteratorName;
};

//@ManMemo: Module: {\bf catalogif}

/*@Doc:

  Operation object for induced condenser operation of the query language.

*/
class QLInducedCondenseOp
{
public:
    ///constructor
    QLInducedCondenseOp(QtOperation *cellExpression, QtOperation *condExpression,
                        std::vector<QtData *> *dataList,
                        Ops::OpType op, const BaseType *newResBaseType, const BaseType *cellBaseType, std::string iteratorName);

    /// operator that carries out the cell expression on point <tt>p</tt>.
    virtual void operator()(const r_Point &p);

    /// executes general condense operation <tt>myOp</tt> in area <tt>areaOp</tt> (const)
    static QtMDD *execGenCondenseInducedOp(QLInducedCondenseOp *myOp, const r_Minterval &areaOp);

    /// getter for the accumulated value
    QtMDD *getAccumulatedValue();

    /// virtual destructor
    virtual ~QLInducedCondenseOp();

private:
    /// pointer to the cell expression
    QtOperation *cellExpression{NULL};

    /// pointer to the condition expression
    QtOperation *condExpression{NULL};

    /// pointer to data vector
    std::vector<QtData *> *dataList{NULL};

    const BaseType *resBaseType{NULL};

    BinaryOp *myOp{NULL};
    BinaryOp *myInitialOp{NULL};

    QtMDD *accumulatedValue{NULL};
};

#endif
