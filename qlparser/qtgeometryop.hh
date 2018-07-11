/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   QtGeometryOp.h
 * Author: bbell
 *
 * Created on June 26, 2018, 2:10 PM
 */
#include <config.h>
#ifndef QTGEOMETRYOP_HH
#define QTGEOMETRYOP_HH

using namespace std;

#include "qlparser/qtnaryoperation.hh"
#include "qlparser/qtgeometrydata.hh"
#include "qlparser/qtnode.hh"

class QtGeometryOp : public QtNaryOperation {
public:
    QtGeometryOp(QtOperationList* vectorListsArg, 
            const QtGeometryData::QtGeometryType geomTypeArg, 
            QtGeometryData::QtGeometryFlag geomFlagArg = QtGeometryData::QtGeometryFlag::NONE);
    virtual ~QtGeometryOp();
    
    /// method for evaluating the node
    QtData* evaluate(QtDataList* inputList);
    
    /// prints the tree
    void printTree(int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES) override;    

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const {return nodeType;};

    /// type checking of the subtree
    virtual const QtTypeElement& checkType(QtTypeTuple* typeTuple);
   
private:
    /// attribute for identification of nodes
    static const QtNodeType nodeType;
    
    /// attribute for identification of geometry type
    const QtGeometryData::QtGeometryType geomType;
    
    /// flag for geometry interpretation
    QtGeometryData::QtGeometryFlag geomFlag = QtGeometryData::QtGeometryFlag::NONE;
};

#endif /* QTGEOMETRYOP_H */

