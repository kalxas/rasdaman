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

#ifndef _QTSORT_
#define _QTSORT_

#include "qlparser/qtoperation.hh"
#include "raslib/minterval.hh"
#include "qlparser/qtmdd.hh"


#include <tuple>
#include <list>
#include <iterator>
#include <iostream>
#include <string>
#include <ostream>


/// MDDobj of a slice, also contains its minterval
using sliceMDD       = QtData;
/// rank of slice
using sliceRank      = double;
/// tuple containing slice and relevant attributes
using sliceTuple     = std::tuple<sliceMDD*, sliceRank>;
/// a list of all slices in a specific order
using listOfSlices   = std::list<sliceTuple>;


//@ManMemo: Module: {\bf qlparser}

/*@Doc:

This class is for the SORT node. A node to sort an n-Dimensional array.
The SORT constructors assign a rank to each slice, and sorts them in ASC||DESC order, by their ranks.
Constructors for the FLIP operator reverse the order of slices at a given axis.
Named axes are supported.
*/

class QtSort : public QtOperation
{
public:

    /// construct SORT with numbered axis
    QtSort(QtOperation *MDDtoSortInput, r_Dimension axis, bool order, QtOperation *ranksInput);
    /// construct SORT with named axis
    QtSort(QtOperation *MDDtoSortInput, const std::string &axis, bool order, QtOperation *ranksInput);

    /// for FLIP operator - order is descending - currently not in use - coming in future patch
    QtSort(QtOperation *MDDtoSortInput, r_Dimension axis);
    /// for FLIP operator - named axis - currently not in use - coming in future patch
    QtSort(QtOperation *MDDtoSortInput, const std::string &axis);

    /// destructor
    virtual ~QtSort();

    /// simplifies the tree
    virtual void simplify();

    /// return childs of the node
    virtual QtNodeList *getChilds(QtChildType flag);

    /// test if the edge to the parent node is of type mdd or atomic
    virtual QtAreaType getAreaType();

    /// test if the two nodes have an equal meaning in a subtree
    virtual bool equalMeaning(QtNode *node);

    /// method for evaluating the node
    QtData *evaluate(QtDataList *inputList);

    /// optimizing load access
    using QtOperation::optimizeLoad;
    void optimizeLoad(QtTrimList *trimList);

    /// type checking of the subtree - important to use
    virtual const QtTypeElement &checkType(QtTypeTuple *typeTuple = NULL);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s);

    /// returns string representation of internal state of slicesList as a string
    std::string toString();
    /**
        Prints all sliceTuples in the listOfSlices, i.e. their relevant tuple information.
    */

    //@Man: Read/Write methods
    //@{
    ///

    /// set ParseInfo for BY clause
    inline virtual void setBYClauseParseInfo(const ParseInfo &info);

    /// set ParseInfo for ALONG clause
    inline virtual void setALONGClauseParseInfo(const ParseInfo &info);

    /// get MDDtoSort member variable
    inline QtOperation *getMDDtoSort();

    /// get ranks member variable
    inline QtOperation *getRanks();

    ///
    //@}

protected:

    /// first operation operand - the array to be sorted (generalExp)
    QtOperation *MDDtoSort;

    /// optional operation operand. The array with ranks. default is: do NOT evaluate ranks.
    QtOperation *ranks = NULL;

private:

    //@Man: Read/Write methods:
    //@{
    ///

    /// access the sliceMDD of a slice (setter & getter)
    sliceMDD* accessSliceMDD(sliceTuple &sT);
    /// access the sliceRank of a slice (setter & getter)
    sliceRank& accessSliceRank(sliceTuple &sT);

    ///
    //@}

    /// add one slice tuple to end of listOfSlices, by giving its sliceMDD and sliceRank
    void appendSlice(sliceMDD *sliceInput, sliceRank rank);

    /// slice the mdd at an minterval
    QtData* slice(QtData *myMdd, r_Minterval myMinterval);
    /**
        The minterval of the result slice, is myMinterval.
        This code shall remain identical with the sectioning part of the QtDomainOperation class evaluate().
        evaluate() is not used directly because it requires housekeeping context which cannot be done inside this class but only in the parser.
    */

    /// used by QtSort::concatenate()
    void processOperand(unsigned int i, QtMDD *qtMDDObj, MDDObj *resultMDD,
                    const BaseType *baseType, const std::vector<r_Point> &tVector);
    /// concatenate all slices
    QtData *concatenate(unsigned int dimension);

    /// function to get slice ranks from the array that carries them and save them into the slicesList.
    void extractRanks(QtData* ranksOperand);

    /// get the axis number from a given axis name
    void getAxisFromName();

    /// sorting the slices in slicesList; by sliceRank, in ASC or DESC order
    void sort();

    /// number of slices at sortAxis
    r_Sinterval::BoundType sortAxisExtent;

    /// ASC (true, 1) OR DESC (false, 0)
    bool sortAsc;

    /// the axis along which we sort
    r_Dimension sortAxis;

    /// This flag determines whether the axis is a name or number. true if name, false if number.
    bool namedAxisFlag  = false;

    /// user-provided axis name
    std::string axisName;

    /// actual names of the axes in the array to be sorted - if applicable.
    std::vector<std::string> *axisNamesCorrect;

    /// apply ranking function or not.
    bool applyRankings;
    /**
      If ranking is not applied, sorting ASC keeps the original order (no change),
      and sorting DESC reverses the order of slices at that axis.
      See INVERSE operator.
    */

    /// get the ParseInfo for the BY clause - using default constructor.
    ParseInfo BYClauseParseInfo;

    /// get the ParseInfo for the ALONG clause
    ParseInfo ALONGClauseParseInfo;

    /// a list of sliceTuples that holds all slices
    listOfSlices slicesList;
    /**
      As per std::list documentation, slicesList is always initialized by the default empty constructor.
    */

    /// attribute for identification of nodes
    static const QtNodeType nodeType;

    /// prevent access to default constructor
    QtSort(): QtOperation() {};

};

#include "qlparser/qtsort.icc"

#endif
