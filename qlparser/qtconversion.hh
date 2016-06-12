#ifndef _QTCONVERSION_HH__
#define _QTCONVERSION_HH___

#include "qlparser/qtunaryoperation.hh"
#include "raslib/structuretype.hh"
#include "catalogmgr/typefactory.hh"
#include <string>
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
 * COMMENTS:
 *
 ************************************************************/


//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The class represents a format conversion. The conversion is described by
  an enumerator.

*/

const BaseType* constructBaseType( const r_Type *type );

class QtConversion : public QtUnaryOperation
{
public:
    enum QtConversionType
    {
        QT_UNKNOWN,
        QT_TOTIFF,
        QT_TOHDF,
        QT_TONETCDF,
        QT_TOGDAL,
        QT_TOCSV,
        QT_TODEM,
        QT_FROMTIFF,
        QT_FROMHDF,
        QT_FROMNETCDF,
        QT_FROMCSV,
        QT_FROMDEM,
        QT_FROMGRIB,
        QT_FROMGDAL
    };

    /// constructor getting operand and format conversion type
    QtConversion( QtOperation* newInput, QtConversionType newConversionType, const char* = NULL );

    /// constructor getting operand and format conversion type, along with a format id/mimetype
    QtConversion( QtOperation* newInput, QtConversionType newConversionType, const std::string& format, const char* params );

    /// set the format conversion type by name
    void setConversionTypeByName( std::string formatName );

    /// test if the two nodes have an equal meaning in a subtree
    virtual bool equalMeaning( QtNode* node );

    /// evaluates the node
    QtData* evaluate( QtDataList* inputList );

    /// prints the tree
    virtual void printTree( int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES );

    /// prints the algebraic expression
    virtual void printAlgebraicExpression( std::ostream& s = std::cout );

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking of the subtree
    virtual const QtTypeElement& checkType( QtTypeTuple* typeTuple = NULL );

private:
    
    /// set convType and convFormat based on the conversionType
    void setConversionTypeAndResultFormat(r_Data_Format& convType, r_Data_Format& convFormat);
    
    /// return true if internally implemented convertors should be used, otherwise
    /// false if GDAL should be used.
    bool isInternalFormat(r_Data_Format dataFormat);

    /// convert rasdaman type to base type
    const BaseType* rasTypeToBaseType(r_Type* rasType);
    
    /// attribute storing conversion type
    QtConversionType conversionType;

    /// attribute for identification of nodes
    static const QtNodeType nodeType;

    const char* paramStr;
    
    std::string format;
    
    bool gdalConversion;
};

extern std::ostream& operator<<(std::ostream& os, QtConversion::QtConversionType t);

#include "qlparser/qtconversion.icc"

#endif

