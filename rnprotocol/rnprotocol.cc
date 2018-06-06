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
/****************************************************************************
 *
 *
 * COMMENTS:
 *
 ****************************************************************************/

#include "config.h"
#include <rnprotocol.hh>
#include <assert.h>

using namespace rnp;
using namespace std;

#include "debug.hh"

#include <logging.hh>


const RnpQuark Rnp::rnpProtocolId = 25112001;


const char* Rnp::endiannessNames[2] =
{
    "big endian", "little endian"
};

const char* Rnp::fragmentTypeNames[Rnp::fgt_HowMany] =
{
    "(fgt_none)", "Command", "OkAnswer", "ErrorAnswer"
};

const char* Rnp::dataTypeNames[Rnp::dtt_HowMany] =
{
    "(dtt_none)", "Asciiz", "Int32", "Float", "Double", "Opaque", "NullPtr"
};

const char* Rnp::errorTypeNames[Rnp::ert_HowMany] =
{
    "(ert_unknown)", "StlException", "OtherException"
};

const char* Rnp::errorParamNames[Rnp::erp_HowMany] =
{
    "(erp_none)", "StlWhatValue"
};

const char* Rnp::undefValue = "(undef)";

const char* Rnp::getFragmentTypeName(RnpQuark fType) noexcept
{
    if (0 <= fType && fType < fgt_HowMany)
    {
        return fragmentTypeNames[fType];
    }
    return undefValue;
}

const char* Rnp::getDataTypeName(RnpQuark dType) noexcept
{
    if (0 <= dType && dType < dtt_HowMany)
    {
        return dataTypeNames[dType];
    }
    return undefValue;
}

const char* Rnp::getErrorTypeName(RnpQuark eType) noexcept
{
    if (0 <= eType && eType < ert_HowMany)
    {
        return errorTypeNames[eType];
    }
    return undefValue;
}

const char* Rnp::getErrorParamName(RnpQuark eName) noexcept
{
    if (0 <= eName && eName < erp_HowMany)
    {
        return errorParamNames[eName];
    }
    return undefValue;
}

const char* Rnp::getEndiannessName(Rnp::Endianness endianness) noexcept
{
    return endiannessNames[endianness];
}

Rnp::Endianness Rnp::detectHostEndianness() noexcept
{
    unsigned int uInteger = 1;

    char* ptr = (char*)&uInteger;

    return *ptr == 1 ? littleEndian : bigEndian;
}

RnpQuark Rnp::swapBytes(RnpQuark orig) noexcept
{
    RnpQuark result = orig;

    char* buf = (char*)&result;

    char tmp = buf[0];
    buf[0] = buf[3];
    buf[3] = tmp;

    tmp    = buf[1];
    buf[1] = buf[2];
    buf[2] = tmp;

    return result;
}

//############## RNP Header #################################

bool RnpHeader::isRnpMessage() const noexcept
{
    Rnp::Endianness hostEndianness = Rnp::detectHostEndianness();

    RnpQuark cProtocolId = hostEndianness == Rnp::littleEndian ? protocolId : Rnp::swapBytes(protocolId);

    return cProtocolId == Rnp::rnpProtocolId ? true : false;
}

Rnp::Endianness RnpHeader::getEndianness() const noexcept
{
    return static_cast<Rnp::Endianness>(messageEndianness);
}

RnpQuark RnpHeader::getTotalLength() const noexcept
{
    Rnp::Endianness endianness = static_cast<Rnp::Endianness>(messageEndianness);
    return endianness == Rnp::detectHostEndianness() ? totalMessageLength : Rnp::swapBytes(totalMessageLength);
}

bool RnpHeader::changeEndianness(Rnp::Endianness newEndianness) noexcept
{
    Rnp::Endianness endianness = static_cast<Rnp::Endianness>(messageEndianness);

    if (newEndianness == endianness)
    {
        return false;    // no change necessary
    }

    messageEndianness = newEndianness;

    totalMessageLength = Rnp::swapBytes(totalMessageLength);
    nrFragments        = Rnp::swapBytes(nrFragments);
    serverType         = Rnp::swapBytes(serverType);
    authInfoStart      = Rnp::swapBytes(authInfoStart);
    authInfoLength     = Rnp::swapBytes(authInfoLength);
    comprInfoStart     = Rnp::swapBytes(comprInfoStart);
    comprInfoLength    = Rnp::swapBytes(comprInfoLength);
    dataStart          = Rnp::swapBytes(dataStart);
    dataLength         = Rnp::swapBytes(dataLength);

    return true;
}

RnpFragmentHeader* RnpHeader::getFirstFragment() const noexcept
{
    return (RnpFragmentHeader*)((char*)const_cast<rnp::RnpHeader*>(this) + dataStart);
}

//############ RNP Fragment ##################################

RnpFragmentHeader* RnpFragmentHeader::getNextFragment() const noexcept
{
    char* ptr = (char*)const_cast<rnp::RnpFragmentHeader*>(this);

    return (RnpFragmentHeader*)(ptr + totalLength);
}

RnpParameter* RnpFragmentHeader::getFirstParameter() const noexcept
{
    return (RnpParameter*)(const_cast<rnp::RnpFragmentHeader*>(this) + 1);
}

void RnpFragmentHeader::changeEndianness() noexcept
{
    // there is no info about the initial endianness so be carefull
    fragmType   = Rnp::swapBytes(fragmType);
    command     = Rnp::swapBytes(command);
    nrParams    = Rnp::swapBytes(nrParams);
    totalLength = Rnp::swapBytes(totalLength);
}

//############ RNP Parameter ##################################
RnpParameter* RnpParameter::getNextParameter() const noexcept
{
    char* ptr = (char*)const_cast<rnp::RnpParameter*>(this);
    return (RnpParameter*)(ptr + totalLength);
}

void* RnpParameter::getData() const noexcept
{
    return static_cast<void*>(const_cast<rnp::RnpParameter*>(this + 1));
}

RnpQuark RnpParameter::getDataLength() const noexcept
{
    return dataLength;
}

RnpQuark RnpParameter::computeTotalAlignedLength() noexcept
{
    totalLength = (sizeof(RnpParameter) + static_cast<unsigned int>(dataLength) + 3) & 0xFFFFFFFC;
    return totalLength;
}

RnpQuark RnpParameter::getPaddLength() const noexcept
{
    return static_cast<unsigned int>(totalLength) - (sizeof(RnpParameter) + static_cast<unsigned int>(dataLength));
}

void RnpParameter::changeToHostEndianness() noexcept
{
    // there is no info about the initial endianness so be carefull
    paramType   = Rnp::swapBytes(paramType);
    dataType    = Rnp::swapBytes(dataType);
    dataLength  = Rnp::swapBytes(dataLength);
    totalLength = Rnp::swapBytes(totalLength);

    RnpQuark* valPtr = static_cast<RnpQuark*>(getData());
    switch (dataType)
    {
    case Rnp::dtt_Int32:
    case Rnp::dtt_Float32:
    {
        *valPtr = Rnp::swapBytes(*valPtr);
        break;
    }
    case Rnp::dtt_Double64:
    {
        RnpQuark temp = Rnp::swapBytes(*valPtr);
        *valPtr       = Rnp::swapBytes(*(valPtr + 1));
        *(valPtr + 1)   = temp;
        break;
    }
    default:
        break;
    }
}

void RnpParameter::changeToPartnerEndianness() noexcept
{
    // there is no info about the initial endianness so be careful

    RnpQuark* valPtr = static_cast<RnpQuark*>(getData());
    switch (dataType)
    {
    case Rnp::dtt_Int32:
    case Rnp::dtt_Float32:
    {
        *valPtr = Rnp::swapBytes(*valPtr);
        break;
    }
    case Rnp::dtt_Double64:
    {
        RnpQuark temp = Rnp::swapBytes(*valPtr);
        *valPtr       = Rnp::swapBytes(*(valPtr + 1));
        *(valPtr + 1)   = temp;
        break;
    }
    default:
        break;
    }
    paramType   = Rnp::swapBytes(paramType);
    dataType    = Rnp::swapBytes(dataType);
    dataLength  = Rnp::swapBytes(dataLength);
    totalLength = Rnp::swapBytes(totalLength);
}

//###########################################################

RnpProtocolEncoder::RnpProtocolEncoder() noexcept
{
    commBuffer    = NULL;

    rnpHeader     = NULL;
    currFragment  = NULL;
    currParameter = NULL;
    allocated     = false;
    carrierHeaderSize = 0;
    finalEndianness = Rnp::detectHostEndianness();
}

RnpProtocolEncoder::~RnpProtocolEncoder() noexcept
{
    if (commBuffer != NULL && allocated == true)
    {
        delete commBuffer;
    }
    // the other pointers are not pointing to allocated memory!!
}

void RnpProtocolEncoder::setBuffer(akg::CommBuffer* buffer) noexcept
{
    if (commBuffer != NULL && allocated == true)
    {
        delete commBuffer;
    }

    commBuffer = buffer;
    allocated  = false;
}

bool RnpProtocolEncoder::allocateBuffer(int maxMessageLength) noexcept
{
    if (commBuffer != NULL && allocated == true)
    {
        delete commBuffer;
    }

    commBuffer = new akg::CommBuffer(maxMessageLength);
    allocated = true;
    return true;
}

bool RnpProtocolEncoder::adjustBufferSize(int differenceSize) noexcept
{
    if (commBuffer == 0)
    {
        LDEBUG << "RnpProtocolEncoder::adjustBufferSize(): warning: null commBuffer, assert would fire.";
        return false;
    }
    assert(commBuffer != 0);

    if (differenceSize <= 0)
    {
        LDEBUG << "RnpProtocolEncoder::adjustBufferSize(): warning: nonpositive differenceSize, assert would fire.";
        return false;
    }
    assert(differenceSize > 0);

    // we need to adjust the pointers to the new location
    char* orig = (char*)(commBuffer->getData());
    char* head = (char*)rnpHeader;
    char* frag = (char*)currFragment;
    char* para = (char*)currParameter;

    if (commBuffer->resize(commBuffer->getDataSize() + differenceSize + RNP_DEFAULTBUFFERSIZE) == true)
    {
        char* final  = (char*)(commBuffer->getData());
        rnpHeader    = (RnpHeader*)(final + (head - orig));
        currFragment = (RnpFragmentHeader*)(final + (frag - orig));
        currParameter = (RnpParameter*)(final + (para - orig));
        return true;
    }

    return false;
}

int RnpProtocolEncoder::getBufferSize() noexcept
{
    if (commBuffer == 0)
    {
        LDEBUG << "RnpProtocolEncoder::getBufferSize(): warning: null commBuffer, assert will fire.";
    }
    assert(commBuffer != 0);

    return commBuffer->getBufferSize();
}

void RnpProtocolEncoder::startMessage(RnpQuark serverType, int nCarrierHeaderSize) noexcept
{
    if (commBuffer == 0)
    {
        LDEBUG << "RnpProtocolEncoder::startMessage(): warning: null commBuffer, assert will fire.";
    }
    assert(commBuffer != NULL);

    carrierHeaderSize = nCarrierHeaderSize;

    commBuffer->clearToRead();

    commBuffer->reserve(sizeof(RnpHeader) + static_cast<unsigned int>(carrierHeaderSize));

    rnpHeader = (RnpHeader*)((char*)commBuffer->getData() + carrierHeaderSize);

    Rnp::Endianness hostEndianness = Rnp::detectHostEndianness();

    // the protocolID is always 25112001 little endian!!
    rnpHeader->protocolId         = hostEndianness == Rnp::littleEndian ? Rnp::rnpProtocolId : Rnp::swapBytes(Rnp::rnpProtocolId);

    rnpHeader->messageEndianness  = hostEndianness;
    rnpHeader->desiredEndianness  = hostEndianness;
    rnpHeader->majorVersion       = 1;
    rnpHeader->minorVersion       = 0;
    rnpHeader->totalMessageLength = sizeof(RnpHeader);
    rnpHeader->nrFragments        = 0;
    rnpHeader->serverType         = serverType;
    rnpHeader->authInfoStart      = 0;
    rnpHeader->authInfoLength     = 0;
    rnpHeader->comprInfoStart     = 0;
    rnpHeader->comprInfoLength    = 0;
    rnpHeader->dataStart          = sizeof(RnpHeader);
    rnpHeader->dataLength         = 0;

    currFragment = (RnpFragmentHeader*)(rnpHeader + 1);

}

int RnpProtocolEncoder::getCarrierHeaderSize() noexcept
{
    return carrierHeaderSize;
}

void RnpProtocolEncoder::setDesiredEndianness(Rnp::Endianness desiredEndianness) noexcept
{
    rnpHeader->desiredEndianness  = desiredEndianness;
}
void RnpProtocolEncoder::setFinalEndianness(Rnp::Endianness endianness) noexcept
{
    finalEndianness = endianness;
}

void RnpProtocolEncoder::startFragment(Rnp::FragmentType fType, RnpQuark command) noexcept
{
    commBuffer->reserve(sizeof(RnpFragmentHeader));

    currFragment->fragmType   = fType;
    currFragment->command     = command;
    currFragment->nrParams    = 0;
    currFragment->totalLength = sizeof(RnpFragmentHeader);

    currParameter = currFragment->getFirstParameter();
    rnpHeader->nrFragments++;

}

void RnpProtocolEncoder::addStringParameter(RnpQuark parameterType, const char* str) noexcept
{
    if (str != 0)
    {
        addParameter(parameterType, Rnp::dtt_Asciiz, str, strlen(str) + 1);
    }
    else
    {
        addParameter(parameterType, Rnp::dtt_NullPtr, str, 0);
    }
}

void RnpProtocolEncoder::addInt32Parameter(RnpQuark parameterType, int par) noexcept
{
    addParameter(parameterType, Rnp::dtt_Int32, &par, sizeof(par));
}

void RnpProtocolEncoder::addFloat32Parameter(RnpQuark parameterType, float par) noexcept
{
    addParameter(parameterType, Rnp::dtt_Float32, &par, sizeof(par));
}
void RnpProtocolEncoder::addDouble64Parameter(RnpQuark parameterType, double par) noexcept
{
    addParameter(parameterType, Rnp::dtt_Double64, &par, sizeof(par));
}

void RnpProtocolEncoder::addOpaqueParameter(RnpQuark parameterType, const void* buf, int size) noexcept
{
    if (buf != 0)
    {
        addParameter(parameterType, Rnp::dtt_Opaque, buf, size);
    }
    else
    {
        addParameter(parameterType, Rnp::dtt_NullPtr, buf, 0);
    }
}

void RnpProtocolEncoder::addParameter(RnpQuark parameterType, Rnp::DataType dtt, const void* data, int length) noexcept
{
    commBuffer->reserve(sizeof(RnpParameter));

    currParameter->paramType   = parameterType;
    currParameter->dataType    = dtt;
    currParameter->dataLength  = length;
    currParameter->totalLength = currParameter->computeTotalAlignedLength();

    int paddlen = currParameter->getPaddLength();

    if (data != 0)
    {
        commBuffer->read(data, length);
    }
    if (paddlen)
    {
        commBuffer->reserve(paddlen);
    }

    currFragment->nrParams++;
    currFragment->totalLength += currParameter->totalLength;
    currParameter = currParameter->getNextParameter();
}

void RnpProtocolEncoder::endFragment() noexcept
{
    rnpHeader->totalMessageLength += currFragment->totalLength;
    rnpHeader->dataLength         += currFragment->totalLength;
    currFragment = currFragment->getNextFragment();
}

akg::CommBuffer* RnpProtocolEncoder::endMessage() noexcept
{
    changeToPartnerEndianness(finalEndianness);
    return commBuffer;
}

bool RnpProtocolEncoder::changeToPartnerEndianness(Rnp::Endianness newEndianness) noexcept
{
    if (newEndianness == rnpHeader->getEndianness())
    {
        return false;
    }
    // so newEndianness is the same as the message endiannes, no change necessary

    // don't forget that the endianness is now the host endianness so,
    // after changed, the data can't be used correctly any more!!

    RnpFragmentHeader* lCurrFragment = rnpHeader->getFirstFragment();

    for (int fragment = 0; fragment < rnpHeader->nrFragments; fragment++)
    {

        RnpParameter* lCurrParameter = lCurrFragment->getFirstParameter();
        for (int parameter = 0; parameter < lCurrFragment->nrParams; parameter++)
        {
            RnpParameter* nextParameter = lCurrParameter->getNextParameter();
            lCurrParameter->changeToPartnerEndianness();
            lCurrParameter = nextParameter;
        }

        RnpFragmentHeader* nextFragment = lCurrFragment->getNextFragment();
        lCurrFragment->changeEndianness();
        lCurrFragment = nextFragment;
    }

    rnpHeader->changeEndianness(newEndianness);

    return true;
}

//###### DECODER #######################

RnpProtocolDecoder::RnpProtocolDecoder() noexcept
{
    // this memory doesn't belong to us, so do not deallocate!!
    commBuffer    = NULL;
    rnpHeader     = NULL;
    currFragment  = NULL;
    currParameter = NULL;
}

bool RnpProtocolDecoder::decode(akg::CommBuffer* buffer) noexcept
{
    // Later, throw something intelligible!
    commBuffer = buffer;

    rnpHeader = static_cast<RnpHeader*>(commBuffer->getData());

    if (rnpHeader->isRnpMessage() == false)
    {
        return false;
    }

    originalEndianness = rnpHeader->getEndianness();

    if (originalEndianness == Rnp::detectHostEndianness())
    {
        // -- test validity of message --
        if (testIntegrity() == false)
        {
            return false;
        }
    }
    else
    {
        // -- endianess of message    --
        changeToHostEndianness();
    }
    return true;
}

bool RnpProtocolDecoder::testIntegrity() const noexcept
{
    // could be done better...

    if (rnpHeader->isRnpMessage() == false)
    {
        LERROR << "Communication error: received invalid RNP header.";
        return false;
    }

    bool ok = true;
    char* endOfMessage = (char*)rnpHeader + rnpHeader->getTotalLength();
    char* endOfHeader  = (char*)rnpHeader + sizeof(RnpHeader);
    int   maxLength    = commBuffer->getDataSize() - static_cast<int>(sizeof(RnpHeader));
    // max of every length

    RnpFragmentHeader* lCurrFragment = const_cast<RnpFragmentHeader*>(getFirstFragment());
    for (int fragment = 0; fragment < countFragments(); fragment++)
    {
        if (endOfHeader <= (char*)lCurrFragment && (char*)lCurrFragment < endOfMessage)
            ;
        else
        {
            ok = false;
            LERROR << "Communication error: RNP message corrupt: short header.";
            break;
        }

        if (lCurrFragment->totalLength > maxLength)
        {
            ok = false;
            LERROR << "Communication error: RNP message corrupt: actual length (" << lCurrFragment->totalLength << ") larger than foreseen (" << maxLength << ").";
            break;
        }

        char* startOfParameters = (char*)lCurrFragment + sizeof(RnpFragmentHeader);
        char* endOfParameters   = (char*)lCurrFragment + lCurrFragment->totalLength;

        RnpParameter* lCurrParameter = const_cast<RnpParameter*>(getFirstParameter());
        for (int parameter = 0; parameter < countParameters(); parameter++)
        {
            if (startOfParameters <= (char*)lCurrParameter && (char*)lCurrParameter < endOfParameters)
                ;
            else
            {
                ok = false;
                LERROR << "Communication error: RNP message corrupt: current parameter location outside parameter area.";
                break;
            }

            if (lCurrParameter->totalLength > lCurrFragment->totalLength)
            {
                ok = false;
                LERROR << "Communication error: RNP message corrupt: current parameter length (" << lCurrParameter->totalLength << ") larger than total fragment size (" << lCurrFragment->totalLength << ").";
                break;
            }

            lCurrParameter = const_cast<RnpParameter*>(getNextParameter());
        }

        if ((char*)lCurrParameter != endOfParameters)
        {
            ok = false;
// the counting seems to differ from the protocol specs;
// to avoid log flooding we disable it for the moment being -- PB 2005-aug-28
#ifdef RMANDEBUG
            LTRACE << "Communication warning: puzzled by message: parameter count too small, found extra parameter(s). (this message can be ignored)";
#endif
        }

        if (ok == false)
        {
            break;
        }

        // we found a valid fragment, proceed to next one
        lCurrFragment = const_cast<RnpFragmentHeader*>(getNextFragment());
    }

    return ok;
}

bool RnpProtocolDecoder::changeToHostEndianness() noexcept
{
    if (rnpHeader->changeEndianness(Rnp::detectHostEndianness()) == false)
    {
        return false;
    }
    // so host endianness is the same as the message endiannes, no change necessary

    currFragment = const_cast<RnpFragmentHeader*>(getFirstFragment());

    for (int fragment = 0; fragment < countFragments(); fragment++)
    {
        currFragment->changeEndianness();

        currParameter = const_cast<RnpParameter*>(getFirstParameter());
        for (int parameter = 0; parameter < countParameters(); parameter++)
        {
            currParameter->changeToHostEndianness();

            currParameter = const_cast<RnpParameter*>(getNextParameter());
        }

        currFragment = const_cast<RnpFragmentHeader*>(getNextFragment());
    }
    return true;
}

RnpQuark RnpProtocolDecoder::getDestinationServerType() const noexcept
{
    return rnpHeader->serverType;
}

Rnp::Endianness RnpProtocolDecoder::getDesiredEndianness() const noexcept
{
    return static_cast<Rnp::Endianness>(rnpHeader->desiredEndianness);
}

Rnp::Endianness RnpProtocolDecoder::getOriginalEndianness() const noexcept
{
    return originalEndianness;
}

int RnpProtocolDecoder::getMessageLength() const noexcept
{
    return rnpHeader->totalMessageLength;
}

int RnpProtocolDecoder::getMessageVersion() const noexcept
{
    return 1000 * rnpHeader->majorVersion + rnpHeader->minorVersion;
}

RnpQuark RnpProtocolDecoder::countFragments() const noexcept
{
    return rnpHeader->nrFragments;
}

const RnpFragmentHeader* RnpProtocolDecoder::getFirstFragment() const noexcept
{
    currFragmentIdx = 0;

    return currFragment = (currFragmentIdx < rnpHeader->nrFragments ? rnpHeader->getFirstFragment() : 0);
}

const RnpFragmentHeader* RnpProtocolDecoder::getNextFragment() const noexcept
{
    currFragmentIdx++;

    return currFragment = (currFragmentIdx < rnpHeader->nrFragments ? currFragment->getNextFragment() : 0);
}

RnpQuark RnpProtocolDecoder::getFragmentType() const noexcept
{
    return currFragment->fragmType;
}

const char* RnpProtocolDecoder::getFragmentTypeName() const noexcept
{
    return Rnp::getFragmentTypeName(currFragment->fragmType);
}
RnpQuark RnpProtocolDecoder::getCommand() const noexcept
{
    return currFragment->command;
}

int RnpProtocolDecoder::countParameters() const noexcept
{
    return currFragment->nrParams;
}

RnpQuark RnpProtocolDecoder::getFragmentLength() const noexcept
{
    return currFragment->totalLength;
}

const RnpParameter* RnpProtocolDecoder::getFirstParameter() const noexcept
{
    currParameterIdx = 0;

    return currParameter = (currParameterIdx < currFragment->nrParams ? currFragment->getFirstParameter() : 0);
}

const RnpParameter* RnpProtocolDecoder::getNextParameter() const noexcept
{
    currParameterIdx++;

    return currParameter = (currParameterIdx < currFragment->nrParams ? currParameter->getNextParameter() : 0);
}

RnpQuark  RnpProtocolDecoder::getParameterType() const noexcept
{
    return currParameter->paramType;
}

RnpQuark  RnpProtocolDecoder::getDataType() const noexcept
{
    return currParameter->dataType;
}

const void* RnpProtocolDecoder::getData() const noexcept
{
    return currParameter->getData();
}

const char* RnpProtocolDecoder::getDataAsString() const noexcept
{
    if (!(currParameter->dataType == Rnp::dtt_Asciiz || currParameter->dataType == Rnp::dtt_NullPtr))
    {
        LDEBUG << "RnpProtocolEncoder::getDataAsString(): warning: assert will fire.";
    }
    assert(currParameter->dataType == Rnp::dtt_Asciiz || currParameter->dataType == Rnp::dtt_NullPtr);

    return currParameter->dataType == Rnp::dtt_Asciiz ? static_cast<const char*>(currParameter->getData()) : static_cast<const char*>(0);
}

int RnpProtocolDecoder::getDataAsInteger() const noexcept
{
    if (!(currParameter->dataType == Rnp::dtt_Int32))
    {
        LDEBUG << "RnpProtocolEncoder::getDataAsInteger(): warning: assert will fire.";
    }
    assert(currParameter->dataType == Rnp::dtt_Int32);

    return *static_cast<int*>(currParameter->getData());
}

float RnpProtocolDecoder::getDataAsFloat() const noexcept
{
    if (!(currParameter->dataType == Rnp::dtt_Float32))
    {
        LDEBUG << "RnpProtocolEncoder::getDataAsFloat(): warning: assert will fire.";
    }
    assert(currParameter->dataType == Rnp::dtt_Float32);

    return *static_cast<float*>(currParameter->getData());
}

double RnpProtocolDecoder::getDataAsDouble() const noexcept
{
    if (!(currParameter->dataType == Rnp::dtt_Double64))
    {
        LDEBUG << "RnpProtocolEncoder::getDataAsDouble(): warning: assert will fire.";
    }
    assert(currParameter->dataType == Rnp::dtt_Double64);

    return *static_cast<double*>(currParameter->getData());
}

const void* RnpProtocolDecoder::getDataAsOpaque() const noexcept
{
    if (!(currParameter->dataType == Rnp::dtt_Opaque || currParameter->dataType == Rnp::dtt_NullPtr))
    {
        LDEBUG << "RnpProtocolEncoder::getDataAsOpaque(): warning: assert will fire.";
    }
    assert(currParameter->dataType == Rnp::dtt_Opaque || currParameter->dataType == Rnp::dtt_NullPtr);

    return currParameter->dataType == Rnp::dtt_Opaque ? static_cast<const void*>(currParameter->getData()) : static_cast<const void*>(0);
}

int RnpProtocolDecoder::getDataLength() const noexcept
{
    return currParameter->getDataLength();
}

void RnpProtocolDecoder::printRnpHeader(RnpHeader* lRnpHeader) const noexcept
{
    cout << "RnpHeader ID=" << lRnpHeader->protocolId << endl;
    cout << "  total fragments=" << lRnpHeader->nrFragments << endl;
    cout << "  sizeof header=" << sizeof(RnpHeader) << endl;
    cout << endl;
}

bool RnpProtocolDecoder::isRnpMessage() const noexcept
{
    return rnpHeader->isRnpMessage();
}

