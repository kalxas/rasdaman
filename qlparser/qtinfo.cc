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
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#include <string>
#include <sstream>
#include <vector>

#include "config.h"
#include "qlparser/qtinfo.hh"
#include "qlparser/qtvariable.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtmdd.hh"

#include "mddmgr/mddobj.hh"
#include "raslib/type.hh"
#include "catalogmgr/typefactory.hh"
#include "relmddif/dbmddobj.hh"
#include "relcatalogif/basetype.hh"

#include "raslib/oid.hh"
#include "raslib/parseparams.hh"
#include <easylogging++.h>

using namespace std;

const QtNode::QtNodeType QtInfo::nodeType = QtNode::QT_INFO;


QtInfo::QtInfo( QtVariable* newInput )
    : QtUnaryOperation( newInput ),
      printTiles( 0 )
{
}

QtInfo::QtInfo( QtVariable* newInput, const char* paramsStr )
    : QtUnaryOperation( newInput ),
      printTiles( 0 )
{
    r_Parse_Params* params = new r_Parse_Params();
    params->add("printtiles", &printTiles, r_Parse_Params::param_type_int);
    
    // process params
    if (paramsStr)
    {
        params->process(paramsStr);
    }
    delete params;
}



QtData*
QtInfo::evaluate( QtDataList* inputList )
{
    startTimer("QtInfo");

    QtData* returnValue = NULL;
    QtData* operand = NULL;

    operand = input->evaluate( inputList );

    if( operand )
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        if( operand->getDataType() == QT_MDD )
        {
            LERROR << "Internal error in QtInfo::evaluate() - "
                           << "runtime type checking failed (MDD).";

            // delete old operand
            if( operand ) operand->deleteRef();

            return 0;
        }
#endif

        QtMDD*  qtMDD  = static_cast<QtMDD*>(operand);
        MDDObj* mddObj = qtMDD->getMDDObject();

        if( mddObj->isPersistent() )
        {
            MDDObj* persMDD = static_cast<MDDObj*>(mddObj);
            
            // get local oid and pass it as double
            OId localOId;
            if( persMDD->getOId( &localOId ) )
            {
                LFATAL << "Error: QtInfo::evaluate() - could not get oid.";

                // delete old operand
                if( operand ) operand->deleteRef();

                parseInfo.setErrorNo(384);
                throw parseInfo;
            }

            DBMDDObj* dbObj = persMDD->getDBMDDObjId().ptr();
            
            if( dbObj )
            {
                DBStorageLayout* storageLayout = dbObj->getDBStorageLayout().ptr();
                if (storageLayout)
                {
                    ostringstream info("");
                    info << "{\n \"oid\": \"" << static_cast<double>(localOId);
                    info << "\",\n \"baseType\": \"" << dbObj->getMDDBaseType()->getTypeStructure();
                    vector< boost::shared_ptr<Tile> >* tiles = persMDD->getTiles();
                    info << "\",\n \"tileNo\": \"" << tiles->size();
                    
                    long totalSize = 0;
                    for (unsigned int i = 0; i < tiles->size(); i++)
                    {
                        totalSize += tiles->at(i)->getSize();
                    }
                    info << "\",\n \"totalSize\": \"" << totalSize;
                    
                    info << "\",\n \"tiling\": {\n";
                    info << "\t\"tilingScheme\": \"";
                    switch (storageLayout->getTilingScheme())
                    {
                        case r_NoTiling:          info << "no_tiling";   break;
                        case r_RegularTiling:     info << "regular";     break;
                        case r_StatisticalTiling: info << "statistic";   break;
                        case r_InterestTiling:    info << "interest";    break;
                        case r_AlignedTiling:     info << "aligned";     break;
                        case r_DirectionalTiling: info << "directional"; break;
                        case r_SizeTiling:        info << "size";        break;
                        default:                  info << "unknown";     break;
                    }
                    info << "\",\n\t\"tileSize\": \"" << storageLayout->getTileSize();
                    info << "\",\n\t\"tileConfiguration\": \"" << storageLayout->getTileConfiguration() << "\"";
                    
                    if (printTiles)
                    {
                        info << "\",\n\t\"tileDomains\":\n\t[";
                        for (unsigned int i = 0; i < tiles->size(); i++)
                        {
                            info << "\n\t\t\"" << tiles->at(i)->getDomain() << "\"";
                            if (i < tiles->size() - 1)
                            {
                                info << ",";
                            }
                        }
                        info << "\n\t]";
                    }

                    info << "\n },\n \"index\": {\n\t\"type\": \"";
                    switch (storageLayout->getIndexType())
                    {
                        case r_Invalid_Index:        info << "invalid";     break;
                        case r_Auto_Index:           info << "a_index";     break;
                        case r_Directory_Index:      info << "d_index";     break;
                        case r_Reg_Directory_Index:  info << "rd_index";    break;
                        case r_RPlus_Tree_Index:     info << "rpt_index";   break;
                        case r_Reg_RPlus_Tree_Index: info << "rrpt_index";  break;
                        case r_Reg_Computed_Index:   info << "rc_index";    break;
                        case r_Index_Type_NUMBER:    info << "it_index";    break;
                        case r_Tile_Container_Index: info << "tc_index";    break;
                        default:                     info << "unknown";     break;
                    }
                    info << "\",\n\t\"PCTmax\": \"" << storageLayout->getPCTMax();
                    info << "\",\n\t\"PCTmin\": \"" << storageLayout->getPCTMin();
                    info << "\"\n }\n}";
                    
                    // result domain: it is now format encoded so we just consider it as a char array
                    r_Type* type = r_Type::get_any_type("char");
                    const BaseType* baseType = TypeFactory::mapType(type->name());
                    
                    string infoString = info.str();
                    int contentLength = infoString.length();
                    char* contents = strdup(infoString.c_str());

                    r_Minterval mddDomain = r_Minterval(1) << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(contentLength) - 1);
                    Tile *resultTile = new Tile(mddDomain, baseType, contents, static_cast<r_Bytes>(contentLength), r_Array);
                    
                    // create a transient MDD object for the query result
                    MDDBaseType* mddBaseType = new MDDBaseType("tmp", baseType);
                    TypeFactory::addTempType(mddBaseType);
                    MDDObj* resultMDD = new MDDObj(mddBaseType, resultTile->getDomain());
                    resultMDD->insertTile(resultTile);

                    // create a new QtMDD object as carrier object for the transient MDD object
                    returnValue = new QtMDD(static_cast<MDDObj*>(resultMDD));
                }
                else
                {
                    LFATAL << "Error: QtInfo::evaluate() - could not get storage layout object.";

                    // delete old operand
                    if (operand) operand->deleteRef();

                    parseInfo.setErrorNo(432);
                    throw parseInfo;
                }
            }
            else
            {
                LFATAL << "Error: QtInfo::evaluate() - could not get database object.";

                // delete old operand
                if( operand ) operand->deleteRef();

                parseInfo.setErrorNo(431);
                throw parseInfo;
            }
        }
        else
        {
            LFATAL << "Error: QtInfo::evaluate() - operand is not a persistent MDD.";

            // delete old operand
            if( operand ) operand->deleteRef();
            parseInfo.setErrorNo(430);
            throw parseInfo;
        }

        // delete old operand
        if( operand ) operand->deleteRef();
    }
    else
        LERROR << "Error: QtInfo::evaluate() - operand is not provided.";
    
    stopTimer();

    return returnValue;
}



void
QtInfo::printTree( int tab, std::ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtInfo Object: " << getEvaluationTime() << std::endl;

    QtUnaryOperation::printTree( tab, s, mode );
}



void
QtInfo::printAlgebraicExpression( std::ostream& s )
{
    s << "info(" << std::flush;

    if( input )
        input->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}



const QtTypeElement&
QtInfo::checkType( QtTypeTuple* typeTuple )
{
    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    // check operand branches
    if( input )
    {

        // get input type
        const QtTypeElement& inputType = input->checkType( typeTuple );

        if( inputType.getDataType() != QT_MDD )
        {
            LFATAL << "Error: QtInfo::checkType() - operand is not of type MDD.";
            parseInfo.setErrorNo(383);
            throw parseInfo;
        }

        dataStreamType.setDataType( QT_MDD );
    }
    else
        LERROR << "Error: QtInfo::checkType() - operand branch invalid.";

    return dataStreamType;
}