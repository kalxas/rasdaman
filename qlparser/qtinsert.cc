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
 * Copyright (C) 2003 Dr. Peter Baumann
 *
 * SOURCE: qtinsert.cc
 *
 * MODULE: qlparser
 * CLASS:  QtInsert
 *
 * PURPOSE:
 *
 * CHANGE HISTORY (append further entries):
 * when         who         what
 * ----------------------------------------------------------
 * 17-01-98     Ritsch      created
 * 2003-aug-25  PB          "insert into" type compatibility bug fixed (from K.Hahn)
 * 2008-nov-10  Shams       added storagelayout to the expression
 * 09-April-14  uadhikari   bug fix for 'area of interest' tiling

 * COMMENTS:
 *
 ************************************************************/



#include "config.h"
#include <vector>
#include "raslib/dlist.hh"
#include "rasodmg/interesttiling.hh"
#include "rasodmg/dirdecompose.hh"


static const char rcsid[] = "@(#)qlparser, QtInsert: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtinsert.cc,v 1.27 2003/12/27 20:40:21 rasdev Exp $";

#include "qlparser/qtinsert.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"

#include "mddmgr/mddcoll.hh"
#include "mddmgr/mddobj.hh"

#include <iostream>
#include <exception>

#include "servercomm/servercomm.hh"
#include "qlparser/qtmintervaldata.hh"
#include "raslib/basetype.hh"
#include "raslib/collectiontype.hh"

#include "../common/src/logging/easylogging++.hh"


extern ServerComm::ClientTblElt* currentClientTblElt;

const QtNode::QtNodeType QtInsert::nodeType = QtNode::QT_INSERT;

QtInsert::QtInsert(const std::string& initCollectionName, QtOperation* initSource)
    : QtExecute(), source(initSource), dataToInsert(NULL), stgLayout(NULL), collectionName(initCollectionName) {
    source->setParent(this);
}

QtInsert::QtInsert(const std::string& initCollectionName, QtOperation* initSource, QtOperation* storage)
    : QtExecute(), source(initSource), dataToInsert(NULL), stgLayout(storage), collectionName(initCollectionName) {
    source->setParent(this);
}

/// constructor getting name of collection and data to insert
QtInsert::QtInsert (const std::string& initCollectionName, QtData* data )
    : QtExecute(), source(NULL), dataToInsert(data), stgLayout(NULL), collectionName(initCollectionName) {
}

QtInsert::~QtInsert()
{
    if (source)
    {
        delete source;
        source = NULL;
    }
    if (stgLayout)
    {
        delete stgLayout;
        stgLayout = NULL;
    }
    if (dataToInsert)
    {
        dataToInsert = NULL;
    }
}

QtData*
QtInsert::evaluate()
{
    startTimer("QtInsert");
      
    // allocate a new oid within the current db
    OId oid;
    long long myoid = 0;
    QtMddCfgOp* configOp = NULL;
    QtMDDConfig* mddConfig = NULL;
    QtData* sourceData = NULL;
    QtNode::QtDataList* nextTupel = NULL;

    r_Minterval* defaultCfg = NULL;
    QtData* returnValue = NULL;

    if (dataToInsert) {
        sourceData = dataToInsert;
    }
    else
    {
        // empty data list for evaluation of insert expression including constant
        nextTupel = new QtNode::QtDataList(0);
        if (stgLayout)
        {
            configOp = static_cast<QtMddCfgOp*>(stgLayout);
            mddConfig = configOp->getMddConfig();
        }
        // get the operands
        sourceData = source->evaluate(nextTupel);
    }

    if (sourceData)
    {
        QtMDD* sourceMDD = static_cast<QtMDD*>(sourceData);
        MDDObj* sourceObj = sourceMDD->getMDDObject();

        MDDColl* persColl = NULL;
        MDDColl* almost = NULL;

        try
        {
            almost = MDDColl::getMDDCollection(collectionName.c_str());
        }
        catch (...)
        {

            LFATAL << "Error: QtInsert::evaluate() - collection name not found";

            // delete the operand
            if (sourceData) sourceData->deleteRef();

            parseInfo.setErrorNo(355);
            throw parseInfo;
        }
        if (!almost->isPersistent())
        {
            LFATAL << "QtInsert: User tries to insert into system table";
            if (sourceData) sourceData->deleteRef();

            parseInfo.setErrorNo(355);
            throw parseInfo;
        }
        else
        {
            persColl = static_cast<MDDColl*>(almost);
        }

        //
        // check MDD and collection type for compatibility
        //
        const MDDBaseType *sourceBaseType = sourceObj->getMDDBaseType();
        const MDDType *targetMDDType = persColl->getCollectionType()->getMDDType();

        int cellSize;
#ifdef DEBUG
        char* collTypeStructure = persColl->getCollectionType()->getTypeStructure();
        char* mddTypeStructure = sourceObj->getMDDBaseType()->getTypeStructure();
        LTRACE << "Collection type structure.: " << collTypeStructure << "\n"
               << "MDD type structure........: " << mddTypeStructure << "\n"
               << "MDD domain................: " << sourceObj->getDefinitionDomain();
        free(collTypeStructure); collTypeStructure = NULL;
        free(mddTypeStructure); mddTypeStructure = NULL;
#endif
        cellSize = static_cast<int>(sourceObj->getMDDBaseType()->getBaseType()->getSize());
        
        // bug fix: "insert into" found claimed non-existing type mismatch -- PB 2003-aug-25, based on fix by K.Hahn
        // if( !persColl->getCollectionType()->compatibleWith( (Type*) sourceObj->getMDDBaseType() ) )
//        if (!((MDDType*) sourceObj->getMDDBaseType())->compatibleWith(persColl->getCollectionType()->getMDDType())) {
        
        // fix PB's bug fix (above) - the else is the old code, which is wrong but removing it
        // will break backwards compatibility - rasql always inserts GreyString data when inv_* functions are used.
        // so to fix this in QtMDD there's a flag which tells if the data is from a conversion function -- DM 2011-aug-08
        
        // check if the types of the MDD to be inserted and the target collection are compatible
        bool compatible = false;
        if (dataToInsert)
        {
            compatible = targetMDDType->compatibleWith(sourceBaseType);
        }
        else
        {
            if (sourceMDD->isFromConversion())
            {
                compatible = true;
            }
            else
            {
                compatible = targetMDDType->compatibleWith(sourceBaseType);
            }
        }

        if (!compatible) {
            // free resources
            persColl->releaseAll();
            delete persColl;
            persColl = NULL;
            if (sourceData) sourceData->deleteRef(); // delete the operand

            // return error
            LFATAL << "Error: QtInsert::evaluate() - MDD and collection types are incompatible";
            parseInfo.setErrorNo(959);
            throw parseInfo;
        }

        r_Minterval definitionDomain = sourceObj->getDefinitionDomain();
        if (!persColl->getCollectionType()->getMDDType()->compatibleWithDomain(&definitionDomain))
        {
            // free resources
            persColl->releaseAll();
            delete persColl;
            persColl = NULL;
            if (sourceData) sourceData->deleteRef(); // delete the operand

            // return error
            LFATAL << "Error: QtInsert::evaluate() - MDD and collection domains are incompatible";
            parseInfo.setErrorNo(959);
            throw parseInfo;
        }

        //
        // convert a transient MDD object to a persistent one
        //

#ifdef BASEDB_O2
        if (!OId::allocateMDDOId(&oid))
        {
#else
        OId::allocateOId(oid, OId::MDDOID);
#endif
        // cast to external format
        myoid = static_cast<long long>(oid);
#ifdef DEBUG
        LINFO << "QtInsert::evaluate() - allocated oid:" << myoid << " counter:" << oid.getCounter();
#endif
            // get all tiles
            vector<boost::shared_ptr<Tile> >* sourceTiles = sourceObj->getTiles();

            // get a persistent type pointer
            MDDBaseType* persMDDType = static_cast<MDDBaseType*>(const_cast<Type*>(TypeFactory::ensurePersistence(static_cast<Type*>(const_cast<MDDBaseType*>(sourceObj->getMDDBaseType())))));

            if (!persMDDType)
            {
                LFATAL << "Error: QtInsert::evaluate() - type not persistent";

                // delete dynamic data
                if (sourceData)
                    sourceData->deleteRef();
                delete sourceTiles;
                sourceTiles = NULL;
                if (nextTupel)
                {
                    delete nextTupel;
                    nextTupel = NULL;
                }
                persColl->releaseAll();
                delete persColl;
                persColl = NULL;
                parseInfo.setErrorNo(964);
                throw parseInfo;
            }

            // create a persistent MDD object
            // need a StorageLayout here
            if(mddConfig!= NULL)
            {
                if(mddConfig->getBorderThreshold() < 0)
                    mddConfig->setBorderThreshold(r_Stat_Tiling::DEF_BORDER_THR);
            }
            r_Index_Type ri = getIndexType(mddConfig);
            StorageLayout tempStorageLayout;
            tempStorageLayout.setDataFormat(getDataFormat(mddConfig));
            r_Tiling_Scheme scheme = getTilingScheme(mddConfig);
            tempStorageLayout.setIndexType(ri);
            tempStorageLayout.setTilingScheme(scheme);
            // Base Information has been set
            tempStorageLayout.setTileSize
            ((mddConfig != NULL && mddConfig->getTileSize() > 0) ? static_cast<unsigned int>(mddConfig->getTileSize()) :
             StorageLayout::DefaultTileSize);
            if(mddConfig!= NULL)
            {
                tempStorageLayout.setInterestThreshold(mddConfig->getInterestThreshold());
                tempStorageLayout.setBorderThreshold(static_cast<unsigned int>(mddConfig->getBorderThreshold()));
                if(mddConfig->getDirDecomp() != NULL)
                    tempStorageLayout.setDirDecomp(mddConfig->getDirDecomp());
                vector<r_Minterval>intervals = getIntervals(mddConfig);
                tempStorageLayout.setCellSize(cellSize);
                if(mddConfig->getTilingType() == QtMDDConfig::r_DRLDECOMP_TLG)
                {
                    tempStorageLayout.resetSubTiling();
                }
                else if(mddConfig->getTilingType() == QtMDDConfig::r_DRLDECOMPSUBTILE_TLG)
                {
                    tempStorageLayout.setSubTiling();
                }
                if(intervals.size() > 0)
                {
                    tempStorageLayout.setBBoxes(intervals);
                }

                //uadhikari
                r_Interest_Tiling::Tilesize_Limit AOI_tileSizeControl;
                switch(mddConfig->getTilingType())
                {
                    case QtMDDConfig::r_AREAOFINTERESTNOLIMIT_TLG:
                        AOI_tileSizeControl = r_Interest_Tiling::NO_LIMIT ;
                        break;
                    case QtMDDConfig::r_AREAOFINTERESTREGROUP_TLG:
                        AOI_tileSizeControl = r_Interest_Tiling::REGROUP;
                        break;
                    case QtMDDConfig::r_AREAOFINTERESTSUBTILING_TLG:
                        AOI_tileSizeControl = r_Interest_Tiling::SUB_TILING;
                        break;
                    case QtMDDConfig::r_AREAOFINTERESTREGROUPANDSUBTILING_TLG:
                        AOI_tileSizeControl = r_Interest_Tiling::REGROUP_AND_SUBTILING;
                        break;
                    default: //r_AREAOFINTEREST_TLG:
                        AOI_tileSizeControl = r_Interest_Tiling::SUB_TILING;
                        break;
                }
                tempStorageLayout.setTilingSizeStrategy_AOI(AOI_tileSizeControl);
            }
            r_Dimension sourceDimension = sourceObj->getDefinitionDomain().dimension();
            r_Minterval tileCfg = getTileConfig(mddConfig, cellSize, sourceDimension);

            if (sourceDimension ==
                    tileCfg.dimension())
            {
                tempStorageLayout.setTileConfiguration(tileCfg);
            }

            MDDObj* persMDDObj = new MDDObj(persMDDType, sourceObj->getDefinitionDomain(), oid,
                                            tempStorageLayout);
            persMDDObj->cloneNullValues(sourceObj);

            // iterate over source tiles
            for (vector< boost::shared_ptr<Tile> >::iterator sourceIt = sourceTiles->begin(); sourceIt != sourceTiles->end(); sourceIt++)
            {
                // create a new persistent tile, copy the transient data, and insert it into the target mdd object
                Tile* newPersTile = new Tile((*sourceIt)->getDomain(), persMDDType->getBaseType(), (*sourceIt)->getDataFormat());
                newPersTile->copyTile((*sourceIt)->getDomain(), sourceIt->get(), (*sourceIt)->getDomain());
                persMDDObj->insertTile(newPersTile);
            }

            // delete tile vector
            delete sourceTiles;
            sourceTiles = NULL;
            persColl->insert(persMDDObj);

#ifdef BASEDB_O2
        }
        else
        {
            LFATAL << "Error: QtInsert::evaluate() - allocation of oid failed";

            // delete dynamic data
            if (sourceData) sourceData->deleteRef();
            if (nextTupel)
            {
                delete nextTupel;
                nextTupel = NULL;
            }
            persColl->releaseAll();
            delete persColl;
            persColl = NULL;
            parseInfo.setErrorNo(958);
            throw parseInfo;
        }
#else
#endif
        // free transient memory
        persColl->releaseAll();
        delete persColl;
        persColl = NULL;
    }
    else
        LERROR << "Error: QtInsert::evaluate() - insert data is invalid.";

    // delete source operand
    if (sourceData)
        sourceData->deleteRef();

    // delete dummy tupel vector
    if (nextTupel)
    {
        delete nextTupel;
        nextTupel = NULL;
    }

    stopTimer();

   // return the generated OID
#ifdef DEBUG
    LDEBUG << "QtInsert::evaluate() - returning oid:" << myoid;
#endif
   returnValue = new QtAtomicData( static_cast<r_Long>(myoid), static_cast<unsigned short>(sizeof(r_Long)) );
   return returnValue;
}

QtNode::QtNodeList*
QtInsert::getChilds(QtChildType flag)
{
    QtNodeList* resultList = NULL;

    if (source)
    {
        // allocate resultList
        if (flag == QT_DIRECT_CHILDS){};

        resultList = new QtNodeList();

        if (flag == QT_LEAF_NODES || flag == QT_ALL_NODES)
            resultList = source->getChilds(flag);

        // add the nodes of the current level
        if (flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES)
            resultList->push_back(source);
    }
    else
        resultList = new QtNodeList();

    return resultList;
}

void
QtInsert::printTree(int tab, std::ostream& s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtInsert Object" << getEvaluationTime() << std::endl;

    if (mode != QtNode::QT_DIRECT_CHILDS)
    {
        if (source)
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "source : " << std::endl;
            source->printTree(tab + 2, s);
        }
        else if (dataToInsert)
        {
            s << SPACE_STR(static_cast<size_t>(tab)) << "data to insert" << std::endl;
        }
        else
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no source" << std::endl;

        s << std::endl;
    }
}

void
QtInsert::printAlgebraicExpression(std::ostream& s)
{
    s << "insert<";

    if (source)
        source->printAlgebraicExpression(s);
    else
        s << "<no source>";

    s << ">";
}

QtOperation*
QtInsert::getSource()
{
    return source;
}


void
QtInsert::checkType()
{
    // check operand branches
    if (source)
    {

        // get input type
        const QtTypeElement& inputType = source->checkType();

        if (inputType.getDataType() != QT_MDD)
        {
            LFATAL << "Error: QtInsert::checkType() - insert expression must be of type r_Marray<T>";
            parseInfo.setErrorNo(960);
            throw parseInfo;
        }
    }
    else if (dataToInsert)
    {

        // get input type
        if (dataToInsert->getDataType() != QT_MDD) {
            LFATAL << "Error: QtInsert::checkType() - inserted data must be of type r_Marray<T>";
            parseInfo.setErrorNo(960);
            throw parseInfo;
        }
    }
    else
        LERROR << "Error: QtInsert::checkType() - operand branch invalid.";
}

r_Data_Format
QtInsert::getDataFormat(QtMDDConfig* config)
{
    if (!config)
        return StorageLayout::DefaultDataFormat;
    int dataType = config->getStorageType();
    switch (dataType)
    {
    case QtMDDConfig::r_DEFAULT_STG :
        return StorageLayout::DefaultDataFormat;
    case QtMDDConfig::r_ARRAY_STG :
        return r_Array;
    case QtMDDConfig::r_AUTO_STG :
        return r_Auto_Compression;
    case QtMDDConfig::r_BMP_STG :
        return r_BMP;
    case QtMDDConfig::r_DEM_STG :
        return r_DEM;
    case QtMDDConfig::r_HDF_STG :
        return r_HDF;
    case QtMDDConfig::r_NETCDF_STG :
        return r_NETCDF;
        //        case QtMDDConfig::r_HDF5_STG://need review
        //            return r_HDF;
    case QtMDDConfig::r_JPEG_STG :
        return r_JPEG;
    case QtMDDConfig::r_NTF_STG :
        return r_NTF;
    case QtMDDConfig::r_PACKBITS_STG :
        return r_Pack_Bits;
    case QtMDDConfig::r_PNG_STG :
        return r_PNG;
    case QtMDDConfig::r_PPM_STG :
        return r_PPM;
    case QtMDDConfig::r_RLE_STG :
        return r_RLE;
    case QtMDDConfig::r_RLESEP_STG :
        return r_Sep_RLE;
    case QtMDDConfig::r_TIFF_STG :
        return r_TIFF;
    case QtMDDConfig::r_TOR_STG :
        return r_TOR;
    case QtMDDConfig::r_VFF_STG :
        return r_VFF;
    case QtMDDConfig::r_WLTCOIFLETINT_STG :
        if (config->getWltValue() == 6)
            return r_Wavelet_Coiflet6;
        if (config->getWltValue() == 12)
            return r_Wavelet_Coiflet12;
        if (config->getWltValue() == 18)
            return r_Wavelet_Coiflet18;
        if (config->getWltValue() == 24)
            return r_Wavelet_Coiflet24;
        if (config->getWltValue() == 30)
            return r_Wavelet_Coiflet30;
        return StorageLayout::DefaultDataFormat; //may be null
    case QtMDDConfig::r_WLTDAUBECHIES_STG :
        return r_Wavelet_Daubechies;
    case QtMDDConfig::r_WLTDAUBECHIESINT_STG :
        if (config->getWltValue() == 6)
            return r_Wavelet_Daub6;
        if (config->getWltValue() == 8)
            return r_Wavelet_Daub8;
        if (config->getWltValue() == 10)
            return r_Wavelet_Daub10;
        if (config->getWltValue() == 12)
            return r_Wavelet_Daub12;
        if (config->getWltValue() == 14)
            return r_Wavelet_Daub14;
        if (config->getWltValue() == 16)
            return r_Wavelet_Daub16;
        if (config->getWltValue() == 18)
            return r_Wavelet_Daub18;
        if (config->getWltValue() == 20)
            return r_Wavelet_Daub20;
        return StorageLayout::DefaultDataFormat; //may be default
    case QtMDDConfig::r_WLTHAAR_STG :
        return r_Wavelet_Haar;
    case QtMDDConfig::r_WLTLEASTINT_STG :
        if (config->getWltValue() == 8)
            return r_Wavelet_Least8;
        if (config->getWltValue() == 10)
            return r_Wavelet_Least10;
        if (config->getWltValue() == 12)
            return r_Wavelet_Least12;
        if (config->getWltValue() == 14)
            return r_Wavelet_Least14;
        if (config->getWltValue() == 16)
            return r_Wavelet_Least16;
        if (config->getWltValue() == 18)
            return r_Wavelet_Least18;
        if (config->getWltValue() == 20)
            return r_Wavelet_Least20;
    case QtMDDConfig::r_WLTQHAAR_STG :
        return r_Wavelet_QHaar;
    case QtMDDConfig::r_ZLIB_STG :
        return r_ZLib;
    case QtMDDConfig::r_ZLIBSEP_STG :
        return r_Sep_ZLib;
    default:
        return StorageLayout::DefaultDataFormat;
    }

}

r_Index_Type
QtInsert::getIndexType(QtMDDConfig* config)
{
    if (!config)
        return StorageLayout::DefaultIndexType;
    int indexType = config->getIndexType();

    switch (indexType)
    {
    case QtMDDConfig::r_A_INDEX :
        return r_Auto_Index;
    case QtMDDConfig::r_DEFAULT_INDEX :
        return StorageLayout::DefaultIndexType;
    case QtMDDConfig::r_D_INDEX :
        return r_Directory_Index;
    case QtMDDConfig::r_IT_INDEX :
        return r_Index_Type_NUMBER;
    case QtMDDConfig::r_RC_INDEX :
        return r_Reg_Computed_Index;
    case QtMDDConfig::r_RD_INDEX :
        return r_Reg_Directory_Index;
    case QtMDDConfig::r_RPT_INDEX :
        return r_RPlus_Tree_Index;
    case QtMDDConfig::r_RRPT_INDEX :
        return r_Reg_RPlus_Tree_Index;
    case QtMDDConfig::r_TC_INDEX :
        return r_Tile_Container_Index;
    default:
        return StorageLayout::DefaultIndexType;
    }
}

r_Tiling_Scheme
QtInsert::getTilingScheme(QtMDDConfig* cfg)
{
    if (!cfg)
        return StorageLayout::DefaultTilingScheme;
    int tileType = cfg->getTilingType();
    switch (tileType)
    {
    case QtMDDConfig::r_ALIGNED_TLG :
        return r_AlignedTiling;
    case QtMDDConfig::r_AREAOFINTEREST_TLG :
        return r_InterestTiling;
    case QtMDDConfig::r_AREAOFINTERESTNOLIMIT_TLG:
        return r_InterestTiling;
    case QtMDDConfig::r_AREAOFINTERESTREGROUP_TLG:
        return r_InterestTiling;
    case QtMDDConfig::r_AREAOFINTERESTSUBTILING_TLG:
        return r_InterestTiling;
    case QtMDDConfig::r_AREAOFINTERESTREGROUPANDSUBTILING_TLG:
        return r_InterestTiling;
    case QtMDDConfig::r_REGULAR_TLG :
        return r_RegularTiling;
    case QtMDDConfig::r_DRLDECOMPSUBTILE_TLG :
        return r_DirectionalTiling;
    case QtMDDConfig::r_DRLDECOMP_TLG :
        return r_DirectionalTiling;
    case QtMDDConfig::r_STATISTICSPARAM_TLG :
        return r_StatisticalTiling;
    case QtMDDConfig::r_STATISTICS_TLG :
        return r_StatisticalTiling;

    default:
        return StorageLayout::DefaultTilingScheme;
    }
}

vector<r_Minterval>
QtInsert::getIntervals(QtMDDConfig* cfg)
{
    vector<r_Minterval> intervals;
    if (!cfg)
        return intervals;
    QtNode::QtOperationList* oplist = cfg->getBboxList();
    if (!oplist)
        return intervals;
    QtNode::QtDataList* nextTupel = new QtNode::QtDataList(0);
    QtOperationList::iterator iter;
    for (iter = oplist->begin(); iter != oplist->end(); iter++)
    {
        QtData* data = (*iter)->evaluate(nextTupel);
        QtMintervalData* intervalData = static_cast<QtMintervalData*>(data);
        r_Minterval interval = intervalData->getMintervalData();
        intervals.push_back(interval);
    }
    return intervals;
}

r_Minterval
QtInsert::getTileConfig(QtMDDConfig* cfg, int baseTypeSize, r_Dimension sourceDimension)
{
    r_Minterval tileConfig;

    if (!cfg || !(cfg->getTilingType() == QtMDDConfig::r_ALIGNED_TLG ||
                  cfg->getTilingType() == QtMDDConfig::r_REGULAR_TLG))
    {
        return (StorageLayout::getDefaultTileCfg(baseTypeSize, sourceDimension));
    }

    QtOperation* op = cfg->getTileCfg();
    if (!op)
        return tileConfig;
    QtNode::QtDataList* nextTupel = new QtNode::QtDataList(0);
    QtData* data = op->evaluate(nextTupel);
    QtMintervalData* intervalData = static_cast<QtMintervalData*>(data);
    tileConfig = intervalData->getMintervalData();
    delete data;
    delete nextTupel;
    return tileConfig;
}
