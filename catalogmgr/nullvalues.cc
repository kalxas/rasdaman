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
/**
  * INCLUDE: nullvalues.hh
 *
 * MODULE:  rasodmg
 * CLASS:   NullValuesHandler
 *
 * COMMENTS:
 *      None
*/

#include "nullvalues.hh"
#include "typeenum.hh"
#include "raslib/nullvalues.hh"
#include "relcatalogif/basetype.hh"     // for BaseType
#include "relcatalogif/structtype.hh"   // for StructType
#include <logging.hh>
#include <stack>
#include <assert.h>

NullValuesHandler::NullValuesHandler()
    : nullValues(NULL), nullValuesCount(0)
{
}

NullValuesHandler::NullValuesHandler(r_Nullvalues *newNullValues)
    : nullValues(newNullValues), nullValuesCount(0)
{
}

NullValuesHandler::~NullValuesHandler() noexcept(false)
{
}

r_Nullvalues *
NullValuesHandler::getNullValues() const
{
//    if (nullValues != NULL)
//    {
//        LDEBUG << "returning null values " << nullValues->toString();
//    }
    return nullValues;
}

r_Double
NullValuesHandler::getNullValue() const
{
    if (nullValues != NULL)
    {
        const auto &nulls = nullValues->getNullvalues();
        if (!nulls.empty())
        {
            return nulls[0].first;
        }
    }
    return r_Double{};
}

void
NullValuesHandler::setNullValues(r_Nullvalues *newNullValues)
{
        nullValues = newNullValues;
}

unsigned long
NullValuesHandler::getNullValuesCount() const
{
    return nullValuesCount;
}

void
NullValuesHandler::setNullValuesCount(unsigned long count)
{
    nullValuesCount = count;
}

void
NullValuesHandler::cloneNullValues(const NullValuesHandler *obj)
{
    if (this != obj)
    {
        nullValues = obj->nullValues;
        nullValuesCount = obj->nullValuesCount;
    }
}

bool compareInterval(std::pair<r_Double, r_Double> i1, std::pair<r_Double, r_Double> i2) 
{ 
    return (i1.first < i2.first); 
}

r_Nullvalues *
NullValuesHandler::unionNullValues(r_Nullvalues *nullValues1, r_Nullvalues *nullValues2)
{
    if (!nullValues1&&!nullValues2)
    {
        return NULL;
    }
    else if (!nullValues1)
    {
        return nullValues2;
    }
    else if (!nullValues2)
    {
        return nullValues1;
    }
    auto tempNullValuesData = nullValues1->getNullvalues();
    auto tempNullValues2Data = nullValues2->getNullvalues();
    std::stack<std::pair<r_Double,r_Double>> stackNullValues;
    std::vector<std::pair<r_Double,r_Double>> resNullValuesData;
    r_Nullvalues *resNullValues = NULL;
    tempNullValuesData.insert(tempNullValuesData.end(),tempNullValues2Data.begin(),tempNullValues2Data.end());
    sort(tempNullValuesData.begin(),tempNullValuesData.end(),compareInterval);
    stackNullValues.push(tempNullValuesData[0]);
    for (size_t i =1; i < tempNullValuesData.size();i++)
    {
        std::pair<r_Double,r_Double> top = stackNullValues.top();
        if (top.second < tempNullValuesData[i].first)
        {
            stackNullValues.push(tempNullValuesData[i]);
        }
        else if (top.second < tempNullValuesData[i].second)
        {
            top.second = tempNullValuesData[i].second;
            stackNullValues.pop();
            stackNullValues.push(top);
        }
    }
    while(!stackNullValues.empty())
    {
        std::pair<r_Double,r_Double> tempPair = stackNullValues.top();
        stackNullValues.pop();
        resNullValuesData.push_back(tempPair);
    }
    return new r_Nullvalues(std::move(resNullValuesData));
}

/// template functions used below
template <class T>
void fillTile(T fillValArg, size_t cellCount, char *startPointArg)
{
    T *startPoint = reinterpret_cast<T *>(startPointArg);
    std::fill(startPoint, startPoint + cellCount, fillValArg);
}

/// template functions used below
template <typename T>
void fillBand(r_Double nullValue, size_t cellCount, char *dst, unsigned int cellTypeSize)
{
    const auto nullValueT = static_cast<T>(nullValue);
    for (size_t i = 0; i < cellCount; ++i, dst += cellTypeSize)
    {
        *reinterpret_cast<T *>(dst) = nullValueT;
    }
}

void NullValuesHandler::fillTileWithNullvalues(char *resDataPtr, size_t cellCount, const BaseType *cellType) const
{
  assert(resDataPtr);
  assert(cellType);
  assert(cellCount > 0);
  if (this->getNullValues())
  {
      if (cellType->getType() == STRUCT)
      {
          fillMultibandTileWithNullvalues(resDataPtr, cellCount, cellType);
      }
      else
      {
          fillSinglebandTileWithNullvalues(resDataPtr, cellCount, cellType->getType());
      }
  }
}

void NullValuesHandler::fillMultibandTileWithNullvalues(char *resDataPtr, size_t cellCount, const BaseType *cellType) const
{
  const auto *structType = dynamic_cast<const StructType *>(cellType);
  const auto numElems = structType->getNumElems();
  assert(numElems > 0);
  LDEBUG << "Initializing multi-band tile with " << numElems << " bands with null value";

  bool allBandsSameType = true;
  auto firstBandType = structType->getElemType(0u)->getType();
  for (unsigned int i = 0; i < numElems; ++i)
  {
      const auto bandType = structType->getElemType(i)->getType();
      if (bandType == STRUCT)
      {
          // cannot handle nested structs, fill with zeros
          LWARNING << "MDD type is a struct that contains struct bands; "
                   << "cannot initialize to null value, will be initialized to 0.";
          fillTile<r_Char>(0, cellCount * cellType->getSize(), resDataPtr);
          return;
      }
      allBandsSameType &= (firstBandType == bandType);
  }

  if (allBandsSameType)
  {
      // optimization: all bands are of the same type
      fillSinglebandTileWithNullvalues(resDataPtr, cellCount * numElems, firstBandType);
  }
  else
  {
      auto nullValue = this->getNullValue();
      // bands of varying types, this is quite inefficient
      const auto cellTypeSize = cellType->getSize();
      size_t bandOffset = 0;
      for (unsigned int i = 0; i < numElems; ++i)
      {
          LDEBUG << "  initializing band " << i << " with null value " << nullValue;
          char *dst = resDataPtr + bandOffset;

          MAKE_SWITCH_TYPEENUM(structType->getElemType(i)->getType(), T,
               CODE( // case T:
                   fillBand<T>(nullValue, cellCount, dst, cellTypeSize);
               ),
               CODE( // default:
                   LDEBUG << "Unknown base type: " << cellType->getName();
                   fillBand<r_Char>(nullValue, cellCount, dst, cellTypeSize);
               ));

          bandOffset += structType->getElemType(i)->getSize();
      }
  }
}

void NullValuesHandler::fillSinglebandTileWithNullvalues(char *resDataPtr, size_t cellCount, TypeEnum cellType) const
{
  auto nullValue = this->getNullValue();
  LDEBUG << "Initializing single-band tile with null value " << nullValue;

  MAKE_SWITCH_TYPEENUM(cellType, T,
       CODE( // case T:
           fillTile<T>(static_cast<T>(nullValue), cellCount, resDataPtr);
       ),
       CODE( // default:
           LDEBUG << "Unknown base type: " << cellType;
           fillTile<r_Char>(0, cellCount * size_t(typeSize(cellType)), resDataPtr);
       ));
}
