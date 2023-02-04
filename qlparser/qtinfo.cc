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

#include "qlparser/qtinfo.hh"
#include "qlparser/qtvariable.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtmdd.hh"

#include "mddmgr/mddobj.hh"
#include "raslib/type.hh"
#include "relcatalogif/typefactory.hh"
#include "relmddif/dbmddobj.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/mddbasetype.hh"
#include "relcatalogif/collectiontype.hh"
#include "tilemgr/tile.hh"

#include "raslib/oid.hh"
#include "raslib/parseparams.hh"
#include <logging.hh>

#include <string>
#include <sstream>
#include <vector>

#define COLOR_PALET_SIZE 15

using namespace std;

const QtNode::QtNodeType QtInfo::nodeType = QtNode::QT_INFO;

QtInfo::QtInfo(QtVariable *newInput)
    : QtUnaryOperation(newInput),
      printTiles(NONE)
{
}

QtInfo::QtInfo(QtVariable *newInput, const char *paramsStr)
    : QtUnaryOperation(newInput),
      printTiles(NONE)
{
    r_Parse_Params *params = new r_Parse_Params(20);
    params->add("printtiles", &printParam, r_Parse_Params::param_type_string);

    // process params
    if (paramsStr)
    {
        printParam = NULL;
        params->process(paramsStr);
        if ((printParam != NULL) && (printParam[0] != '\0'))
        {
            if (!strcmp(printParam, "embedded\0") || !strcmp(printParam, "1\0"))
            {
                printTiles = EMBEDDED;
            }
            else if (!strcmp(printParam, "json"))
            {
                printTiles = JSON;
            }
            else if (!strcmp(printParam, "svg"))
            {
                printTiles = SVG;
            }
        }
        else{
            LERROR << "Error: QtInfo::QtInfo() - printtiles argument is not supported.";
            throw r_Error(INFO_PRINTTILESNOTSUPPORTED);
        }
        if (printParam != NULL)
        {
            delete [] printParam;
        }
    }
    delete params;
}

QtData *
QtInfo::evaluate(QtDataList *inputList)
{
    startTimer("QtInfo");

    QtData *returnValue = NULL;
    QtData *operand = NULL;

    operand = input->evaluate(inputList);

    if (operand)
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        if (operand->getDataType() == QT_MDD)
        {
            LERROR << "Internal error in QtInfo::evaluate() - "
                   << "runtime type checking failed (MDD).";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }

            return 0;
        }
#endif

        QtMDD *qtMDD = static_cast<QtMDD *>(operand);
        MDDObj *mddObj = qtMDD->getMDDObject();

        if (mddObj->isPersistent())
        {
            MDDObj *persMDD = static_cast<MDDObj *>(mddObj);

            // get local oid and pass it as double
            OId localOId;
            if (persMDD->getOId(&localOId))
            {
                LERROR << "Error: QtInfo::evaluate() - could not get oid.";

                // delete old operand
                if (operand)
                {
                    operand->deleteRef();
                }

                parseInfo.setErrorNo(OID_OIDINVALID);
                throw parseInfo;
            }

            DBMDDObj *dbObj = persMDD->getDBMDDObjId().ptr();
            const auto *collType = qtMDD->getCollType();

            if (dbObj)
            {
                DBStorageLayout *storageLayout = dbObj->getDBStorageLayout().ptr();
                if (storageLayout)
                {
                    ostringstream info("");
                    auto *tiles = persMDD->getTiles();
                    if (printTiles == EMBEDDED || printTiles == NONE)
                    {
                        info << "{\n \"oid\": \"" << static_cast<double>(localOId);
                        info << "\",\n \"baseType\": \"" << dbObj->getMDDBaseType()->getTypeStructure();
                        if (collType)
                        {
                            info << "\",\n \"setTypeName\": \"" << collType->getName();
                        }
                        info << "\",\n \"mddTypeName\": \"" << dbObj->getMDDBaseType()->getTypeName();

                        info << "\",\n \"tileNo\": \"" << tiles->size();

                        long totalSize = 0;
                        for (unsigned int i = 0; i < tiles->size(); i++)
                        {
                            totalSize += static_cast<long>(tiles->at(i)->getSize());
                        }
                        info << "\",\n \"totalSize\": \"" << totalSize;

                        info << "\",\n \"tiling\": {\n";
                        info << "\t\"tilingScheme\": \"";
                        switch (storageLayout->getTilingScheme())
                        {
                        case r_NoTiling:
                            info << "no_tiling";
                            break;
                        case r_RegularTiling:
                            info << "regular";
                            break;
                        case r_StatisticalTiling:
                            info << "statistic";
                            break;
                        case r_InterestTiling:
                            info << "interest";
                            break;
                        case r_AlignedTiling:
                            info << "aligned";
                            break;
                        case r_DirectionalTiling:
                            info << "directional";
                            break;
                        case r_SizeTiling:
                            info << "size";
                            break;
                        default:
                            info << "unknown";
                            break;
                        }
                        info << "\",\n\t\"tileSize\": \"" << storageLayout->getTileSize();
                        auto tileConf = storageLayout->getTileConfiguration();
                        if (tileConf.has_axis_names())
                        {
                            std::vector<std::string> emptyAxisNames(tileConf.dimension());
                            tileConf.set_axis_names(emptyAxisNames);
                        }
                        info << "\",\n\t\"tileConfiguration\": \"" << tileConf << "\"";

                        if (printTiles == EMBEDDED)
                        {
                            info << ",\n\t\"tileDomains\":\n\t[";
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
                        case r_Invalid_Index:
                            info << "invalid";
                            break;
                        case r_Auto_Index:
                            info << "a_index";
                            break;
                        case r_Directory_Index:
                            info << "d_index";
                            break;
                        case r_Reg_Directory_Index:
                            info << "rd_index";
                            break;
                        case r_RPlus_Tree_Index:
                            info << "rpt_index";
                            break;
                        case r_Reg_RPlus_Tree_Index:
                            info << "rrpt_index";
                            break;
                        case r_Reg_Computed_Index:
                            info << "rc_index";
                            break;
                        case r_Index_Type_NUMBER:
                            info << "it_index";
                            break;
                        case r_Tile_Container_Index:
                            info << "tc_index";
                            break;
                        default:
                            info << "unknown";
                            break;
                        }
                        info << "\",\n\t\"PCTmax\": \"" << storageLayout->getPCTMax();
                        info << "\",\n\t\"PCTmin\": \"" << storageLayout->getPCTMin();
                        info << "\"\n }\n}";
                    }
                    else if (printTiles == JSON)
                    {
                        info << "[";
                        for (unsigned int i = 0; i < tiles->size(); i++)
                        {
                            info << "\"" << tiles->at(i)->getDomain() << "\"";
                            if (i < tiles->size() - 1)
                            {
                                info << ",";
                            }
                        }
                        info << "]\n";
                    }
                    else if (printTiles == SVG)
                    {
                        auto domain = persMDD->getCurrentDomain();
                        if (domain.dimension() != 2)
                        {
                            LERROR << "Error: QtInfo::evaluate() - Cannot export tile info to svg format for non-2D collection.";
                            // delete old operand
                            if (operand)
                            {
                                operand->deleteRef();
                            }

                            parseInfo.setErrorNo(INFO_TILEINFOSVGEXPORTERROR);
                            throw parseInfo;
                        }
                        std::string colors[COLOR_PALET_SIZE] = {"blue", "red", "brown", "grey", "black", "yellow", "purple", "pink", "olive", "gold", "aqua",
                                                                "darkorange", "indigo", "khaki", "seashell"};
                        r_Point origin = domain.get_origin();
                        r_Point high = domain.get_high();
                        info << "<svg width=\"" << high[0] - origin[0] << "\" height=\"" << high[1] - origin[1] << "\">";
                        for (unsigned int i = 0; i < tiles->size(); i++)
                        {
                            auto tileDomain = tiles->at(i)->getDomain();
                            r_Point originTile = tileDomain.get_origin();
                            r_Point highTile = tileDomain.get_high();
                            int x = originTile[0];
                            int y = originTile[1];
                            int width = highTile[0] - x;
                            int height = highTile[1] - y;
                            int id = (tiles->at(i).get())->getDBTile()->getOId().getCounter();
                            info << "\n\t <rect x=\"" << x << "\" y=\"" << y << "\" width=\"" << width << "\" height=\"" << height << "\" id=\"" << id << "\" style=\"fill:" << colors[i % COLOR_PALET_SIZE] << ";\" stroke=\"black\"></rect>";
                        }
                        info << "\n</svg>\n";
                    }
                    // result domain: it is now format encoded so we just consider it as a char array
                    r_Type *type = r_Type::get_any_type("char");
                    const BaseType *baseType = TypeFactory::mapType(type->name());

                    string infoString = info.str();
                    r_Bytes contentLength = static_cast<r_Bytes>(infoString.length());

                    r_Minterval mddDomain = r_Minterval(1) << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(contentLength) - 1);
                    Tile *resultTile = new Tile(mddDomain, baseType, infoString.c_str(), contentLength, r_Array);

                    // create a transient MDD object for the query result
                    MDDBaseType *mddBaseType = new MDDBaseType("tmp", baseType);
                    TypeFactory::addTempType(mddBaseType);
                    MDDObj *resultMDD = new MDDObj(mddBaseType, resultTile->getDomain());
                    resultMDD->insertTile(resultTile);

                    // create a new QtMDD object as carrier object for the transient MDD object
                    returnValue = new QtMDD(static_cast<MDDObj *>(resultMDD));
                }
                else
                {
                    LERROR << "Error: QtInfo::evaluate() - could not get storage layout object.";

                    // delete old operand
                    if (operand)
                    {
                        operand->deleteRef();
                    }

                    parseInfo.setErrorNo(INFO_OBJINFOFAIL);
                    throw parseInfo;
                }
            }
            else
            {
                LERROR << "Error: QtInfo::evaluate() - could not get database object.";

                // delete old operand
                if (operand)
                {
                    operand->deleteRef();
                }

                parseInfo.setErrorNo(INFO_PERSISTENTOBJINVALID);
                throw parseInfo;
            }
        }
        else
        {
            LERROR << "Error: QtInfo::evaluate() - operand is not a persistent MDD.";

            // delete old operand
            if (operand)
            {
                operand->deleteRef();
            }
            parseInfo.setErrorNo(INFO_OPERANDNOTPERSISTENT);
            throw parseInfo;
        }

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }
    }
    else
    {
        LERROR << "Error: QtInfo::evaluate() - operand is not provided.";
    }

    stopTimer();

    return returnValue;
}

void QtInfo::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtInfo Object: " << getEvaluationTime() << std::endl;

    QtUnaryOperation::printTree(tab, s, mode);
}

void QtInfo::printAlgebraicExpression(std::ostream &s)
{
    s << "info(" << std::flush;

    if (input)
    {
        input->printAlgebraicExpression(s);
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}

const QtTypeElement &
QtInfo::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input type
        const QtTypeElement &inputType = input->checkType(typeTuple);

        if (inputType.getDataType() != QT_MDD)
        {
            LERROR << "Error: QtInfo::checkType() - operand is not of type MDD.";
            parseInfo.setErrorNo(OID_PARAMETERINVALID);
            throw parseInfo;
        }

        dataStreamType.setDataType(QT_MDD);
    }
    else
    {
        LERROR << "Error: QtInfo::checkType() - operand branch invalid.";
    }

    return dataStreamType;
}
