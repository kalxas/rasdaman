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

#include "srptindexlogic.hh"
#include "sdirindexlogic.hh"        // for SDirIndexLogic
#include "hierindexds.hh"           // for HierIndexDS
#include "indexds.hh"               // for IndexDS
#include "keyobject.hh"             // for KeyObject, operator<<
#include "reladminif/oidif.hh"      // for OId, operator<<, OId::OIdPrimi...
#include "reladminif/dbobject.hh"   // for DBObjectId
#include "relindexif/hierindex.hh"  // for DBHierIndex
#include "relindexif/indexid.hh"    // for DBHierIndexId
#include "raslib/error.hh"          // for r_Error, INDEXEXHAUSTEDAREA
#include "raslib/sinterval.hh"      // for r_Sinterval
#include "logging.hh"               // for LTRACE, LERROR

#include <math.h>  // for fabs
#include <memory>  // for allocator_traits<>::value_type
#include <vector>  // for vector

using std::vector;

// removes all entries from a index and inserts them into a vector
void clear(KeyObjectVector &keyvec, HierIndexDS *node)
{
    unsigned int i = 0;
    unsigned int nodeSize = node->getSize();

    keyvec.reserve(nodeSize);
    while (!keyvec.empty())
    {
        keyvec.erase(keyvec.begin());
    }
    for (i = 0; i < nodeSize; i++)
    {
        keyvec.push_back(node->getObject(0));
        node->removeObject(static_cast<unsigned int>(0));
    }
}

/*this may be usefull when getting rid of the extendFaces method
unsigned int
findNearestNode(IndexDS* whereToLook, const r_Minterval& theEntryDomain)
    {
    r_Minterval sum(theEntryDomain.dimension());
//this code is not used -> no pror
    r_Area smallestArea = (r_Area)0xFFFFFFFF;
    unsigned int smallestAreaAt = 0;
    unsigned long currentArea = 0;
    unsigned int i = 0;
    unsigned int numElems = whereToLook->getSize();

    for (i = 0; i < numElems; i++)
        {
        currentArea = sum.closure_of(whereToLook->getObjectDomain(i), theEntryDomain).cell_count();
        if (currentArea <= smallestArea)
            {
            smallestArea = currentArea;
            smallestAreaAt = i;
            }
        }
    return smallestAreaAt;
    }
*/

bool SRPTIndexLogic::insertObject(IndexDS *ixDS, const KeyObject &newKeyObject,
                                  const StorageLayout &sl)
{
    IndexPVector leafNodes2Split;
    r_Minterval newKeyObjectDom;
    r_Minterval cd;
    bool extend = false;
    bool *facesToExtendLo = nullptr;
    bool *facesToExtendHi = nullptr;
    r_Dimension i = 0;
    r_Dimension dim = 0;

    if (ixDS->getSize() == 0)
    {
        LTRACE << "Index is empty.  only set domain";
        ixDS->setAssignedDomain(newKeyObject.getDomain());
    }
    else
    {
        // initialize facesToExtend
        newKeyObjectDom = newKeyObject.getDomain();
        cd = ixDS->getCoveredDomain();
        dim = cd.dimension();
        extend = false;
        facesToExtendLo = new bool[dim];
        facesToExtendHi = new bool[dim];
        for (i = 0; i < dim; i++)
        {
            if (newKeyObjectDom[i].low() < cd[i].low())
            {
                facesToExtendLo[i] = true;
                extend = true;
            }
            else
                facesToExtendLo[i] = false;

            if (newKeyObjectDom[i].high() > cd[i].high())
            {
                facesToExtendHi[i] = true;
                extend = true;
            }
            else
                facesToExtendHi[i] = false;
        }

        // Implementation note:
        //   the extension of faces could be integrated with the insertObject()
        //   function and result in a more efficient but more complex implementation
        //   (a nonrecursive extendFaces() woul be needed  and insertObject()
        //   changed to have additional parameters oldCurrDom, facesToExtendLo,
        //   facesToExtendHi).
        //   The simple solution was chosen, where the whole tree is first updated
        //   for the new borders and then the insertion is made.
        //   Another solution would be the implementation of the external borders as
        //   infinite. Each node would have to have both a current domain and a domain.

        if (extend)
        {
            SRPTIndexLogic::extendFaces(static_cast<HierIndexDS *>(ixDS),
                                        newKeyObjectDom, cd, facesToExtendLo,
                                        facesToExtendHi);
        }
        else
        {
            LTRACE << "no need to extend faces";
        }
        delete[] facesToExtendLo;
        facesToExtendLo = nullptr;
        delete[] facesToExtendHi;
        facesToExtendHi = nullptr;
    }

    // call recursive insertObject()
    SRPTIndexLogic::insertObject(newKeyObject, static_cast<HierIndexDS *>(ixDS),
                                 leafNodes2Split, sl);
    LTRACE << "number of leaf overflows " << leafNodes2Split.size();

    if (!leafNodes2Split.empty())
        SRPTIndexLogic::splitNodes(static_cast<HierIndexDS *>(ixDS), leafNodes2Split,
                                   sl);
    // there should be a check here : )
    return true;
}

void SRPTIndexLogic::intersect(const IndexDS *ixDS,
                               const r_Minterval &searchInter,
                               KeyObjectVector &intersectedObjs,
                               __attribute__((unused))
                               const StorageLayout &sl)
{
    r_Minterval dom = ixDS->getCoveredDomain();
    r_Area area = 0;

    // avoid exceptions from r_Minterval
    if (searchInter.intersects_with(dom))
    {
        // this is neccessary because intersectNoDuplicats would think there were
        // other index nodes that cover this area.
        r_Minterval searchDom = searchInter.create_intersection(dom);
        // needed this parent domain, or else indexes with one level only don't work
        area = searchDom.cell_count();
        SRPTIndexLogic::intersect(searchDom, dom, intersectedObjs,
                                  static_cast<const HierIndexDS *>(ixDS), area);
    }
}

void SRPTIndexLogic::containPointQuery(const IndexDS *ixDS,
                                       const r_Point &searchPoint,
                                       KeyObject &result,
                                       const StorageLayout &sl)
{
    SRPTIndexLogic::containPointQuery(
        searchPoint, static_cast<const HierIndexDS *>(ixDS), result, sl);
}

void SRPTIndexLogic::getObjects(const IndexDS *ixDS, KeyObjectVector &objs,
                                const StorageLayout &sl)
{
    // can be optimized !!!
    intersect(static_cast<const HierIndexDS *>(ixDS), ixDS->getCoveredDomain(), objs, sl);
}

int SRPTIndexLogic::insertObject(const KeyObject &newKeyObject, HierIndexDS *ix,
                                 IndexPVector &leafNodes2Split,
                                 const StorageLayout &sl)
{
    int overflowed = 0;

    if (ix->isLeaf())
    {
        LTRACE << "ix is Leaf";
        // this is new
        r_Minterval oldDom = ix->getCoveredDomain();
        SDirIndexLogic::insertObject(ix, newKeyObject, sl);
        ix->setAssignedDomain(oldDom);
        // no node overflow, simply insert newEntry
        if (ix->isOverFull())
        {
            // node overflow: add node to list of nodes to split
            overflowed = 1;
            leafNodes2Split.push_back(ix);
        }
        else
        {
            if (!ix->isRoot())
            {
                ix->destroy();
            }
        }
    }
    else
    {
        LTRACE << "ix is Node";
        KeyObjectVector intersectedNodes;
        SDirIndexLogic::intersect(ix, newKeyObject.getDomain(), intersectedNodes, sl);
        while (!intersectedNodes.empty())
        {
            overflowed = overflowed + insertObject(newKeyObject, DBHierIndexId((*(intersectedNodes.begin())).getObject()).ptr(), leafNodes2Split, sl);
            intersectedNodes.erase(intersectedNodes.begin());
        }
    }
    return overflowed;
}

void SRPTIndexLogic::extendFaces(HierIndexDS *ix,
                                 const r_Minterval &newKeyObjectDom,
                                 const r_Minterval &oldCurrDom,
                                 const bool *facesToExtendLo,
                                 const bool *facesToExtendHi)
{
    bool extendEntries = false;
    unsigned int numberElems = 0;
    r_Dimension dim = 0;
    r_Dimension i = 0;
    r_Dimension d = 0;
    r_Minterval entryDom;
    bool follow = false;
    r_Minterval ixDom = oldCurrDom;  // ix->getCoveredDomain();

    if (ix->isLeaf())
    {
        if (!(ix->isRoot()))  // nothing to do!!!
        {
            //this entry's domain was set in the previous call of extendFaces
            LTRACE << OId(ix->getIdentifier()) << " is Leaf and not Root - already updated";
            ix->destroy();
            ix = nullptr;
        }
        else  // ix is both leaf and root, one node only!! must update domain
        {
            LTRACE << OId(ix->getIdentifier()) << " is Leaf and Root - update domain";
            ixDom.closure_with(newKeyObjectDom);
            ix->setAssignedDomain(ixDom);
        }
    }
    else
    {
        LTRACE << OId(ix->getIdentifier()) << " is Node";
        dim = newKeyObjectDom.dimension();
        for (i = 0; i < dim; i++)
        {
            if (facesToExtendLo[i] && (ixDom[i].low() == oldCurrDom[i].low()))
            {
                ixDom[i].set_low(newKeyObjectDom[i].low());
                extendEntries = true;
            }
            if (facesToExtendHi[i] && (ixDom[i].high() == oldCurrDom[i].high()))
            {
                ixDom[i].set_high(newKeyObjectDom[i].high());
                extendEntries = true;
            }
        }
        if (extendEntries)
        {
            LTRACE << "must extend entries";
            numberElems = ix->getSize();
            for (i = 0; i < numberElems; i++)
            {
                follow = false;
                entryDom = ix->getObjectDomain(i);
                LTRACE << "Entry #" << i << " has domain " << entryDom;
                for (d = 0; d < dim; d++)
                {
                    if (facesToExtendLo[d] && (entryDom[d].low() == oldCurrDom[d].low()))
                    {
                        entryDom[d].set_low(newKeyObjectDom[d].low());
                        follow = true;
                    }
                    if (facesToExtendHi[d] && (entryDom[d].high() == oldCurrDom[d].high()))
                    {
                        entryDom[d].set_high(newKeyObjectDom[d].high());
                        follow = true;
                    }
                }
                if (follow)
                {
                    LTRACE << "Entry #" << i << " must be extended to " << entryDom;
                    HierIndexDS *child = convert(ix->getObject(i));
                    extendFaces(child, newKeyObjectDom, oldCurrDom, facesToExtendLo, facesToExtendHi);
                    ix->setObjectDomain(entryDom, i);
                }
            }
        }
        LTRACE << "new Domain of " << OId(ix->getIdentifier()) << " is "
               << ix->getCoveredDomain();
        if (!ix->isRoot())
        {
            ix->destroy();
            ix = nullptr;
        }
    }
}

void SRPTIndexLogic::splitNodes(HierIndexDS *ixDS,
                                IndexPVector &leafNodes2Split,
                                const StorageLayout &sl)
{
    HierIndexDS *parentIxDS = nullptr;
    HierIndexDS *leafNodeIxDS = nullptr;
    HierIndexDS *n1 = nullptr;
    HierIndexDS *n2 = nullptr;
    HierIndexDS *nln1 = nullptr;  // non leaf nodes
    HierIndexDS *nln2 = nullptr;  // non leaf nodes
    HierIndexDS *tempPar = nullptr;
    r_Dimension axis = 0;
    r_Range value = 0;
    int parentOverflowed = 0;
    KeyObjectVector keyvec;
    KeyObject nodekey1;
    KeyObject nodekey2;
    r_Minterval domain;
    bool wasroot = false;
    bool found = false;
    //  r_Dimension dim = ixDS->getDimension();
    unsigned int numElem = 0;
    unsigned int cur = 0;

    while (!leafNodes2Split.empty())
    {
        leafNodeIxDS = static_cast<HierIndexDS *>(leafNodes2Split[0]);
        leafNodes2Split.erase(leafNodes2Split.begin());

        wasroot = leafNodeIxDS->isRoot();
        if (wasroot)
            domain = leafNodeIxDS->getCoveredDomain();
        else
        {
            tempPar = leafNodeIxDS->getParent();
            KeyObject tkey;
            numElem = tempPar->getSize();
            for (cur = 0; cur < numElem; cur++)
            {
                tkey = tempPar->getObject(cur);
                if ((static_cast<OId::OIdPrimitive>(tkey.getObject().getOId())) ==
                    leafNodeIxDS->getIdentifier())
                {
                    domain = tkey.getDomain();
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                LERROR << "the leaf node to split was not found in its parent";
                throw r_Error(INDEXNOTFOUNDINPARENT);
            }
            tempPar->destroy();
            tempPar = nullptr;
        }

        calculatePartition(axis, value, leafNodeIxDS);
        clear(keyvec, leafNodeIxDS);
        n1 = static_cast<HierIndexDS *>(leafNodeIxDS->getNewInstance());
        if (wasroot)
        {
            parentIxDS = leafNodeIxDS;
            n2 = static_cast<HierIndexDS *>(leafNodeIxDS->getNewInstance());
            leafNodeIxDS->setIsNode(true);
            leafNodeIxDS = nullptr;
        }
        else
        {
            parentIxDS = leafNodeIxDS->getParent();
            n2 = leafNodeIxDS;
            leafNodeIxDS = nullptr;
        }
        splitLeaf(n1, n2, keyvec, axis, value, domain, sl);
        nodekey1 = convert(n1);
        nodekey2 = convert(n2);
        if (!wasroot)
        {
            parentIxDS->removeObject(nodekey2);
        }
        SDirIndexLogic::insertObject(parentIxDS, nodekey1, sl);
        SDirIndexLogic::insertObject(parentIxDS, nodekey2, sl);
        parentOverflowed = parentIxDS->isOverFull();

        n1->destroy();
        n1 = nullptr;
        n2->destroy();
        n2 = nullptr;

        while (parentOverflowed)  // split up
        {
            wasroot = parentIxDS->isRoot();
            domain = parentIxDS->getAssignedDomain();
            calculatePartition(axis, value, parentIxDS);
            clear(keyvec, parentIxDS);
            nln1 = static_cast<HierIndexDS *>(parentIxDS->getNewInstance());

            if (wasroot)
            {
                nln2 = static_cast<HierIndexDS *>(parentIxDS->getNewInstance());
            }
            else
            {
                nln2 = parentIxDS;
                parentIxDS = parentIxDS->getParent();
            }
            splitNonLeaf(nln1, nln2, keyvec, leafNodes2Split, axis, value, domain, sl);
            nodekey1 = convert(nln1);
            nodekey2 = convert(nln2);
            if (!wasroot)
            {
                parentIxDS->removeObject(nodekey2);
            }
            SDirIndexLogic::insertObject(parentIxDS, nodekey1, sl);
            SDirIndexLogic::insertObject(parentIxDS, nodekey2, sl);
            parentOverflowed = parentIxDS->isOverFull();

            nln1->destroy();
            nln1 = nullptr;
            nln2->destroy();
            nln2 = nullptr;
        }
        if (parentIxDS && (parentIxDS != ixDS))
        {
            parentIxDS->destroy();
            parentIxDS = nullptr;
        }
    }
}

void SRPTIndexLogic::splitLeaf(HierIndexDS *pd1, HierIndexDS *pd2,
                               KeyObjectVector &keyvec,
                               r_Dimension axis, r_Range value,
                               r_Minterval &domain,
                               const StorageLayout &sl)
{
    unsigned int i = 0;
    unsigned int leafSize = keyvec.size();

    r_Minterval cd(domain);
    r_Minterval nd1 = cd;
    r_Minterval nd2 = cd;

    KeyObject obj;

    nd1[axis].set_high(value - 1);
    nd2[axis].set_low(value);
    LTRACE << "old leaf domain " << cd << " partition 1 " << nd1 << " partition 2 " << nd2;
    //populate two nodes
    for (i = 0; i < leafSize; i++)
    {
        cd = keyvec[i].getDomain();
        LTRACE << "ObjectDomain of Object #" << i << " is " << cd;
        obj = keyvec[i];
        if (nd1.intersects_with(cd))
        {
            SDirIndexLogic::insertObject(pd1, obj, sl);
        }
        if (nd2.intersects_with(cd))
        {
            SDirIndexLogic::insertObject(pd2, obj, sl);
        }
// sanity check
#ifdef DEBUG
        if (!nd1.intersects_with(cd) && !nd2.intersects_with(cd))
        {
            LERROR << "SRPTIndexLogic::splitLeaf() the entry does not intersect with any node: node 1 "
                   << nd1 << " node 2 " << nd2 << " entry " << cd;
            throw r_Error(TILE_NOT_INSERTED_INTO_INDEX);
        }
#endif
    }
    pd1->setAssignedDomain(nd1);
    pd2->setAssignedDomain(nd2);
}

void SRPTIndexLogic::splitNonLeaf(HierIndexDS *pd1, HierIndexDS *pd2,
                                  KeyObjectVector &keyvec,
                                  IndexPVector &leafNodes2Split,
                                  r_Dimension axis,
                                  r_Range value,
                                  const r_Minterval &domain,
                                  const StorageLayout &sl)
{
    r_Dimension dim = domain.dimension();
    r_Minterval cd(domain);
    r_Minterval nd1(cd);
    r_Minterval nd2(cd);
    r_Minterval leafDomain(dim);
    r_Minterval nodeDomain(dim);
    KeyObjectVector listMinKO1;
    KeyObjectVector listMinKO2;
    KeyObjectVector keyvec2;
    IndexPVector newLeafsToSplit;
    HierIndexDS *entry = nullptr;
    HierIndexDS *n11 = nullptr;
    HierIndexDS *parentIxDS = nullptr;
    KeyObject tempKey;
    KeyObject k11;
    KeyObject k22;
    unsigned int i = 0;
    unsigned int a = 0;
    unsigned int nodeSize = keyvec.size();
    unsigned int leafSize = 0;

    nd1[axis].set_high(value - 1);
    nd2[axis].set_low(value);

    // repopulate node and pd1
    for (i = 0; i < nodeSize; i++)
    {
        LTRACE << "repopulating node (entry " << i << " of " << nodeSize << ")";
        tempKey = keyvec[i];
        entry = convert(tempKey);
        // entry's domain
        cd = keyvec[i].getDomain();
        if (nd1.covers(cd))
        {
            LTRACE << "entry #" << i << " " << cd << " covers node 1 " << nd1;
            // updates parent automatically
            SDirIndexLogic::insertObject(pd1, tempKey, sl);
            entry->destroy();
            entry = nullptr;
        }
        else
        {
            if (nd2.covers(cd))
            {
                LTRACE << "entry #" << i << " " << cd << " covers node 2 " << nd2;
                // updates parent automatically
                SDirIndexLogic::insertObject(pd2, tempKey, sl);
                entry->destroy();
                entry = nullptr;
            }
            else  // intersects both  -> split down
            {
                LTRACE << "entry #" << i << " " << cd << " intersects both " << nd1 << " " << nd2;
                n11 = nullptr;
                // k11 is not a pointer! it is an object
                // k11 = 0;
                if (entry->isLeaf())
                {
                    LTRACE << "entry is leaf ";
                    n11 = static_cast<HierIndexDS *>(entry->getNewInstance());
                    n11->setIsNode(false);
                    leafDomain = cd;
                    clear(keyvec2, entry);
                    splitLeaf(n11, entry, keyvec2, axis, value, leafDomain, sl);
                    // if this was one of the leaf nodes to split, remove it from the list
                    leafSize = leafNodes2Split.size();
                    newLeafsToSplit = vector<IndexDS *>();
                    for (a = 0; a < leafSize; a++)
                    {
                        if (leafNodes2Split[a]->isSameAs(entry))
                        {
                            LTRACE << "will not add entry " << a << " to leafNodes2Split.size " << leafSize;
                        }
                        else
                        {
                            LTRACE << "will add entry     " << a << " to leafNodes2Split.size " << leafSize;
                            newLeafsToSplit.push_back(leafNodes2Split[a]);
                        }
                    }
                    leafNodes2Split = newLeafsToSplit;
                    LTRACE << "new LeafsToSplit size:" << leafNodes2Split.size();
                }
                else  // nonleaf node to be split
                {
                    LTRACE << "entry is nonleaf ";
                    n11 = static_cast<HierIndexDS *>(entry->getNewInstance());
                    n11->setIsNode(true);
                    nodeDomain = cd;
                    parentIxDS = entry->getParent();
                    clear(keyvec2, entry);
                    splitNonLeaf(n11, entry, keyvec2, leafNodes2Split, axis, value, nodeDomain, sl);
                    k22 = convert(entry);
                    parentIxDS->removeObject(k22);
                    parentIxDS->destroy();
                    parentIxDS = nullptr;
                }
                // n11 and entry are allocated
                // n11 and entry are not inserted in a parent!
                k11 = convert(n11);
                if (n11->isUnderFull() || n11->isLeaf())
                {
                    // leaf or node with more than minfill entries
                    SDirIndexLogic::insertObject(pd1, k11, sl);
                    if (n11->isLeaf() && n11->isOverFull())
                    {
                        // very improbable that this happens
                        // leaf with more than maxfill entries
                        leafNodes2Split.push_back(n11);
                    }
                    else  // node or leaf with ok entries
                    {
                        n11->destroy();
                        n11 = nullptr;
                    }
                }
                else  // node with less than minfill entries
                {
                    listMinKO1.push_back(k11);
                    // k11 is deleted in redistribute
                    n11->destroy();
                    n11 = nullptr;
                }
                k22 = convert(entry);
                if (entry->isUnderFull() || entry->isLeaf())
                {
                    SDirIndexLogic::insertObject(pd2, k22, sl);
                    if (entry->isLeaf() && entry->isOverFull())
                    {
                        // very improbable that this happens
                        leafNodes2Split.push_back(entry);
                    }
                    else
                    {
                        entry->destroy();
                        entry = nullptr;
                    }
                }
                else
                {
                    listMinKO2.push_back(k22);
                    entry->destroy();
                    entry = nullptr;
                    // k22 is deleted in redistribute
                    // where is entry deleted
                }
            }
        }
        LTRACE << "ended repopulating node (entry " << i << " of " << nodeSize << ")";
    }
    redistributeEntries(pd1, listMinKO1, sl);
    redistributeEntries(pd2, listMinKO2, sl);
    pd1->setAssignedDomain(nd1);
    pd2->setAssignedDomain(nd2);
}

void SRPTIndexLogic::redistributeEntries(IndexDS *node, KeyObjectVector &listMinKO, const StorageLayout &sl)
{
    // not implemented. It could redistribute objects in case of too low fill factor
    unsigned int size = listMinKO.size();
    for (unsigned int i = 0; i < size; i++)
    {
        SDirIndexLogic::insertObject(node, listMinKO[i], sl);
    }

    listMinKO.clear();
}

bool SRPTIndexLogic::removeObject(IndexDS *ixDS, const KeyObject &objToRemove, const StorageLayout &sl)
{
    LTRACE << "Removing object " << objToRemove.toString() << " in index " << ixDS->getOId().getCounter();
    bool found = false;
    if (ixDS->getAssignedDomain().intersects_with(objToRemove.getDomain()))
    {
        if ((static_cast<HierIndexDS *>(ixDS))->isLeaf())
        {
            LTRACE << "node is a leaf.";
            if (ixDS->removeObject(objToRemove))
            {
                found = true;
            }
            else
            {
                LTRACE << "removeObject(" << ixDS->getAssignedDomain() << ", "
                       << objToRemove.getDomain() << ") object was not found";
            }
        }
        else
        {
            KeyObjectVector candidates;
            SDirIndexLogic::intersectUnOpt(ixDS, objToRemove.getDomain(), candidates);
            LTRACE << "node is not a leaf, checking " << candidates.size() << " children";
            for (auto it = candidates.begin(); it != candidates.end(); it++)
            {
                if (SRPTIndexLogic::removeObject(static_cast<HierIndexDS *>(DBHierIndexId((*it).getObject())), objToRemove, sl))
                {
                    found = true;
                }
                else
                {
                    LTRACE << "removeObject(" << ixDS->getAssignedDomain() << ", "
                           << objToRemove.getDomain() << ") did not remove an entry in a node";
                }
            }
        }
    }
    else
    {
        LTRACE << "removeObject(" << ixDS->getAssignedDomain() << ", "
               << objToRemove.getDomain() << ") did not intersect";
    }
    return found;
}

void SRPTIndexLogic::calculatePartition(r_Dimension &axis,
                                        r_Range &value,
                                        const HierIndexDS *node)
{
    float bestDist1 = 1;
    float bestDist2 = 1;
    double bestDistBal = 1;
    r_Dimension dim = node->getDimension();
    r_Dimension first = 0;  // rand()%dim;
    r_Dimension a = first;
    unsigned int elemCount = node->getSize();
    r_Range v = -1;
    float dist1 = 0;
    float dist2 = 0;
    double distBal = 0;

    while (true)
    {
        for (unsigned int i = 0; i < elemCount; i++)
        {
            v = node->getObjectDomain(i)[a].low();
            calculateDistribution(a, v, dist1, dist2, node);

            // balanced property of this split distribution: how close it
            // is to 50%, 50% distribution. Worst case: 1.
            distBal = fabs(dist1 - 0.5) + fabs(dist2 - 0.5);

            if (distBal < bestDistBal)  //  less overlapping in number of entries;add other conditions !!!
            {
                bestDist1 = dist1;
                bestDist2 = dist2;
                bestDistBal = fabs(bestDist1 - 0.5) + fabs(bestDist2 - 0.5);
                axis = a;
                value = v;
            }
        }
        a = (a + 1) % dim;
        if (a == first) break;
    }
}

void SRPTIndexLogic::calculateDistribution(r_Dimension axis,
                                           r_Range value, float &dist1,
                                           float &dist2,
                                           const HierIndexDS *node)
{
    dist1 = 0;
    dist2 = 0;
    unsigned int n = node->getSize();
    r_Minterval dom;
    for (unsigned int i = 0; i < n; i++)
    {
        dom = node->getObjectDomain(i);
        if (dom[axis].high() < value)
        {
            // entry will fall in first part only
            dist1 += 1;
            LTRACE << "Entry goes into the first";
        }
        else  // entry will fall in first part only
        {
            if (dom[axis].low() >= value)
            {
                dist2 += 1;
                LTRACE << "Entry goes into the second";
            }
            else  // entry will fall in both parts
            {
                dist1 += 1;
                dist2 += 1;
                LTRACE << "Entry goes into the first and second";
            }
        }
    }
    dist1 = dist1 / n;
    dist2 = dist2 / n;
}

void SRPTIndexLogic::intersect(const r_Minterval &searchInter,
                               const r_Minterval &parentEntryDomain,
                               KeyObjectVector &intersectedObjs,
                               const HierIndexDS *ix, r_Area &area)
{
    r_Minterval intersectArea;
    r_Minterval dom = ix->getCoveredDomain();
    KeyObjectVector intersectedNodes;
    HierIndexDS *tempIx = nullptr;
    r_Area nodeArea = 0;
    r_Area oldArea = area;
    unsigned int i = 0;
    unsigned int nodeSize = ix->getSize();

    if (ix->isLeaf())
    {
        // are there cells which belong into the result?
        if (searchInter.intersects_with(dom))
        {
            intersectArea = searchInter.create_intersection(dom);
            // may this leaf put cells into the result?
            if (intersectArea.intersects_with(parentEntryDomain))
            {
                LTRACE << "searchDom " << searchInter << " indexDom " << dom << " intersection " << intersectArea << " area " << area;
                binaryRegionSearch(ix, searchInter, area, intersectedObjs, 0, static_cast<int>(nodeSize) - 1, dom);  //parentEntryDomain);
            }
        }
    }
    else  // node is not a Leaf
    {
        if (searchInter.intersects_with(dom))
        {
            nodeArea = area;
            binaryRegionSearch(ix, searchInter, nodeArea, intersectedNodes, 0,
                               static_cast<int>(nodeSize) - 1, dom);
            for (i = 0; i < intersectedNodes.size(); i++)
            {
                if (area == 0)
                {
                    LTRACE << "intersect AREA IS ALREADY FOUND";
                }
                if (nodeArea > oldArea)
                {
                    LERROR << "the index found more cells than allowed";
                    throw r_Error(INDEXEXHAUSTEDAREA);
                }
                r_Minterval objDom(intersectedNodes[i].getDomain());
                tempIx = convert(intersectedNodes[i]);
                intersect(searchInter, objDom, intersectedObjs, tempIx, area);
                tempIx->destroy();
            }
        }
    }
}

bool SRPTIndexLogic::intersectNoDuplicates(const r_Minterval &searchInter,
                                           const r_Minterval &entryDomain,
                                           const r_Minterval &parentEntryDomain)
{
    bool retval = true;
    r_Dimension i = 0;
    r_Dimension dim = entryDomain.dimension();

    // This condition allows an early detection of duplicates in the
    // index structure for intersection operations.
    // An entry of a leaf node is only added to the list of entries
    // intersected by that node having it internally with respect to the
    // parent (since no other node may have it then) or if it crosses
    // the upper bounds of the parent node (it will be also crossed by
    // another parent at the upper bounds and it is added to the list then).

    // don't add the entry if it doesn't intersect the search area
    // or if it intersects a lower bound of the parent's
    // domain and that lower bound of the parent is higher than
    // that of the search region
    r_Range searchLo = 0;
    r_Range entryLo = 0;

    for (i = 0; i < dim; i++)
    {
        searchLo = searchInter[i].low();
        entryLo = entryDomain[i].low();
        // entry doesn't intersect search region
        if (entryLo > searchInter[i].high())
        {
            retval = false;
            break;
        }
        // entry doesn't intersect search region
        if (entryDomain[i].high() < searchLo)
        {
            retval = false;
            break;
        }
        // entry is also in another node where it intersects the higher bounds,
        // it should be included then, not here
        if (entryLo < parentEntryDomain[i].low() && searchLo < parentEntryDomain[i].low())
        {
            retval = false;
            break;
        }
    }
    return retval;
}

int SRPTIndexLogic::regionSearch(const HierIndexDS *ixNode,
                                 const r_Minterval &mint,
                                 r_Area &area,
                                 KeyObjectVector &intersectedObjects,
                                 const r_Minterval &parentEntryDomain)
{
    r_Minterval intersectedRegion;
    unsigned int endAt = ixNode->getSize();
    auto retval = static_cast<int>(endAt);
    KeyObject newObj;
    r_Minterval objDomain;
    unsigned int i = 0;
    r_Area oldArea = area;

    /*there must be something like or the map version
        DomainMap t;
        DomainMap::iterator it;
        for (i = 0; i < intersectedObjects->size(); i++)
            {
            DomainPair p((*intersectedObjects)[i]->getObject().getOId(), (*intersectedObjects)[i]->getDomain());
            t.insert(p);
            }
    */

    for (i = 0; i < endAt; i++)
    {
        objDomain = ixNode->getObjectDomain(i);
        // object intersects region
        // problem with calculation of complete area when the entry is not added.

        /*there must be something like or the map version
                if (objDomain.intersects_with(mint))
                    {
                    objDomain.intersection_with(mint);
                    area = area - objDomain.cell_count();
                    if (area <= 0)
                        {
                        retval = i;
                        break;
                        }
                    //insert intersectNoDuplicates here
                    }
        */

        if (intersectNoDuplicates(mint, objDomain, parentEntryDomain))
        {
            /*there must be something like or the map version
                        if ((it = t.find(ixNode->getObject(i)->getObject().getOId())) == t.end())
                            {
                            LTRACE << "adding " << ixNode->getObject(i)->getObject().getOId() << " intersected region " << intersectedRegion << " area " << area;
                            }
                        else    {
                            LTRACE << "not adding " << ixNode->getObject(i)->getObject().getOId() << " area " << area;
                            LERROR << "should never happen";
                            LERROR << "want to add " << ixNode->getObject(i)->getObject().getOId() << " at " << objDomain;
                            for (it = t.begin(); it != t.end(); it++)
                                LERROR << OId((*it).first) << " at " << (*it).second;
                            throw r_Error(TESTERROR);
                            }
            */

            objDomain.intersection_with(mint);
            area = area - objDomain.cell_count();
            newObj = ixNode->getObject(i);
            intersectedObjects.push_back(newObj);
            if (oldArea < area)
            {
                retval = static_cast<int>(i);
                LERROR << "the area was completely exhausted";
                throw r_Error(INDEXEXHAUSTEDAREA);
                break;
            }
            if (area == 0)
            {
                retval = static_cast<int>(i);
                break;
            }
        }
        else
        {
            LTRACE << "not adding " << ixNode->getObject(i).getObject().getOId()
                   << " dom " << objDomain << " does not intersect";
        }
    }
    return retval;
}

int SRPTIndexLogic::binaryRegionSearch(
    const HierIndexDS *ixNode, const r_Minterval &mint,
    r_Area &area, KeyObjectVector &intersectedObjects, int first,
    int last, const r_Minterval &parentEntryDomain)
{
    // code copied from DirIx::binaryRegionSearch (11.11.98)
    // and further adapted
    // assumes order according to the lowest corner of the objects

    int retval = 0;
    int middle = 0;
    int inc = 0;
    int ix = 0;
    int compResult = 0;
    r_Minterval t;
    r_Minterval objDomain;
    r_Minterval intersectedRegion;
    KeyObject newObj;
    r_Area oldArea = area;

    if (area == 0)
        retval = -1;
    else if (first > last)
        retval = -1;
    else
    {
        middle = (last + first) / 2;

        t = ixNode->getObjectDomain(static_cast<unsigned int>(middle));
        if (mint.get_high().compare_with(t.get_origin()) < 0)
        {
            // R.hi < tile.lo  no tiles after this one
            retval = binaryRegionSearch(ixNode, mint, area, intersectedObjects, first,
                                        middle - 1, parentEntryDomain);
        }
        else
        {
            if (t.get_high().compare_with(mint.get_origin()) < 0)
            {
                retval = binaryRegionSearch(ixNode, mint, area, intersectedObjects,
                                            middle + 1, last, parentEntryDomain);
                if (area > 0)
                {
                    retval = binaryRegionSearch(ixNode, mint, area, intersectedObjects,
                                                first, middle - 1, parentEntryDomain);
                }
            }
            else
            {
                inc = 1;
                for (ix = middle;; ix += inc)
                {
                    objDomain = ixNode->getObjectDomain(static_cast<unsigned int>(ix));
                    LTRACE << "cycle " << ix << " " << objDomain;
                    compResult = mint.get_high().compare_with(objDomain.get_origin());
                    // object intersects region
                    if (intersectNoDuplicates(mint, objDomain, parentEntryDomain))
                    {
                        intersectedRegion = objDomain;
                        intersectedRegion.intersection_with(mint).intersection_with(parentEntryDomain);
                        oldArea = area;
                        LTRACE << "interesected region " << intersectedRegion
                               << " intersection area " << intersectedRegion.cell_count()
                               << " area before " << area;
                        area = area - intersectedRegion.cell_count();
                        LTRACE << "area after " << area;
                        if (area > oldArea)
                        {
                            LERROR << "index found more cells than allowed";
                            throw r_Error(INDEXEXHAUSTEDAREA);
                        }
                        LTRACE << "intersectedRegion " << intersectedRegion << " area " << area;
                        newObj = ixNode->getObject(static_cast<unsigned int>(ix));
                        intersectedObjects.push_back(newObj);
                        if (area == 0)
                        {
                            retval = ix;
                            break;
                        }
                    }
                    if (inc != -1 && (ix == last || compResult < 0))
                    {
                        ix = middle;
                        inc = -1;
                    }
                    if (ix == first && inc == -1)
                    {
                        retval = ix;
                        break;
                    }
                }
            }
        }
    }
    return retval;
}

void SRPTIndexLogic::containPointQuery(const r_Point &searchPoint,
                                       const HierIndexDS *ix, KeyObject &result,
                                       const StorageLayout &sl)
{
    if (!ix)
    {
        LTRACE << "containPointQuery(" << searchPoint << ", Node, result) node is NULL";
    }
    else
    {
        if (ix->isLeaf())
        {
            LTRACE << "index " << OId(ix->getIdentifier()) << " is leaf";
            SDirIndexLogic::containPointQuery(ix, searchPoint, result, sl);
        }
        else
        {
            LTRACE << "index " << OId(ix->getIdentifier()) << " is node";
            KeyObject lowerNode;
            SDirIndexLogic::containPointQuery(ix, searchPoint, lowerNode, sl);
            containPointQuery(searchPoint, convert(lowerNode), result, sl);
        }
        if (result.isInitialised())
        {
            LTRACE << "containPointQuery(" << searchPoint << ", " << OId(ix->getIdentifier()) << ")" << result;
        }
        else
        {
            LTRACE << "containPointQuery(" << searchPoint << ", " << OId(ix->getIdentifier()) << ") nothing found";
        }
    }
}

HierIndexDS *SRPTIndexLogic::convert(const KeyObject &toConvert)
{
    HierIndexDS *retval = nullptr;
    if (toConvert.isInitialised())
        retval = static_cast<HierIndexDS *>(DBHierIndexId(toConvert.getObject()));
    return retval;
}

KeyObject SRPTIndexLogic::convert(HierIndexDS *toConvert)
{
    if (toConvert)
    {
        return KeyObject(DBObjectId(toConvert->getIdentifier()), toConvert->getAssignedDomain());
    }
    KeyObject retval;
    return retval;
}
