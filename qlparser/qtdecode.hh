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

#ifndef _QTDECODE_
#define	_QTDECODE_

#include "qlparser/gdalincludes.hh"

#include "raslib/error.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtunaryoperation.hh"
#include "tilemgr/tile.hh"
#include "raslib/type.hh"
#include "relcatalogif/mdddimensiontype.hh"


#ifndef PARAM_SEPARATOR
#define PARAM_SEPARATOR ";"
#endif

/**
 * The class allows to decode image files (png, tiff, jpeg, etc.) using the GDAL
 * library and processes them transforming them into rasdaman MDDs.
 *
 * To check the supported gdal types run: gdalinfo --formats
 * To check the information of a gdal type run: gdalinfo --format \<format-name\>
 *
 */
class QtDecode : public QtUnaryOperation{
public:
	QtDecode(QtOperation* newInput) throw (r_Error);
	QtDecode(QtOperation* newInput, char* format, char* gdalParams) throw(r_Error);

	virtual ~QtDecode();

	/**
	 * Transform the input file received by decode into rasdaman format and passes
	 * it for later processing (i.e. insert into database)
     */
	QtData* evaluate(QtDataList* inputList) throw (r_Error);


	const QtTypeElement& checkType(QtTypeTuple* typeTuple);


	virtual void printTree(int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES);


	virtual QtNodeType getNodeType() const;

private:

	static const QtNodeType nodeType;
	char* format;
	char** gdalParams;

	/**
	 * Creates a temporary file with the information received via the MDD object sent along
	 * with the query representing the import file.
	 *
	 * @param tmpFileName Temporary file name having the format /tmp/rasdaman-XXXXXX used by mkstemp.
	 * @param sourceTile Tile created from the MDD data received along with the query.
	 */
	void createTemporaryImageFile(char* tmpFileName, Tile* sourceTile);

	/**
	 * Initialize the gdal parameters tokenizing the string of parameters by the
	 * separator PARAM_SEPARATOR
     * @param params string representation of gdal parameters separated by PARAM_SEPARATOR
     */
	void initGdalParamas(char* params);


};

#endif	/* _QTDECODE_ */
