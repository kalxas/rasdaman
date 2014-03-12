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
 * MERCHANTrABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

#ifndef _QTENCODE__
#define _QTENCODE__

#include "raslib/error.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtunaryoperation.hh"
#include "tilemgr/tile.hh"
#include "raslib/type.hh"

// GDAL headers
#include "gdal_priv.h"


class GDALDataset;

/**************************************************************
 *
 *
 * COMMENTS:
 *
 ************************************************************/

//@ManMemo: Module: {\bf qlparser}

/*@Doc:
 *
 * The class allows to encode MDDs to various formats as supported by the GDAL library.
 * From rasql it is invoked with the encode function with the following signature:
 * 
 *     encode( mddExpr, gdalFormat, formatParams )
 * 
 * - gdalFormat is the GDAL format identifier to which mddExpr will be encoded.
 *   To see the list of supported formats, run gdal_translate
 * - formatParams are parameters specific to each format driver, they should
 *   be specified in this format:
 * 
 *     name1=value1;name2=value2;...
*/

class QtEncode : public QtUnaryOperation
{
public:
    
    /// constructor getting the mdd operand and format to which to encode it
    QtEncode( QtOperation *mddOp, char* format ) throw(r_Error);
    
    /// constructor getting the mdd operand and format to which to encode it, along with format parameters
    QtEncode( QtOperation *mddOp, char* format, char* params ) throw(r_Error);

    /// destructor
    ~QtEncode();

    /// method for evaluating the node
    QtData* evaluate( QtDataList* inputList ) throw (r_Error);

    /// test if the edge to the parent node is of type mdd or atomic
    virtual QtAreaType getAreaType();

    /// type check
    const QtTypeElement& checkType( QtTypeTuple* typeTuple );

    /// debugging method
    virtual void printTree( int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES );

    /// method for identification of nodes
    virtual const QtNodeType getNodeType() const;

private:

    /// method for encoding the given MDD
    QtData* evaluateMDD(QtMDD *mdd) throw (r_Error);
    
    /// convert rasdaman type to GDAL type
    GDALDataType getGdalType(r_Type* rasType);
    

    // convert rasdaman tile to GDAL dataset
    GDALDataset* convertTileToDataset(Tile* sourceTile, int nBands, r_Type* bandType);

	r_Data_Format getDataFormat(char* format);
    
    /// attribute for identification of nodes
    static const QtNodeType nodeType;

	void initParams(char* params);
	void setDouble(const char* paramName, double* value);
	void setString(const char* paramName, std::string* value);

	void setGDALParameters(GDALDataset *gdalDataSet, int width, int height, int nBands);

	char* format;
	char** fParams;

	struct GenericParams {
		double xmin;
		double xmax;
		double ymax;
		double ymin;

		std::string crs; // string representation of the coordinate reference system
		std::string metadata; // further metadata of the result
		std::vector<double> nodata; // nodata values of the result
	};

	GenericParams gParams;
};

#endif  // _QTENCODE__

