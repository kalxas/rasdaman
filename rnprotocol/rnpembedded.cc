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

#include "common/pragmas/pragmas.hh"

DIAGNOSTIC_PUSH
IGNORE_WARNING("-Wstringop-truncation")

#include "config.h"
#include <rnpembedded.hh>
#include <assert.h>

#include "debug.hh"

#include <logging.hh>

using namespace rnp;

const char* RnpTransport::carrierNames[] =
{
    "(unknown)", "RNP", "HTTP", "(bad)"
};

const char* RnpTransport::getCarrierName(RnpTransport::CarrierProtocol x) noexcept
{
    if (x < crp_Unknown || x >= crp_HowMany)
    {
        return carrierNames[0];
    }
    return carrierNames[x];
}


const int RnpReceiver::headerBufferLength = 1000;

RnpReceiver::RnpReceiver() noexcept
{
    headerBuffer.allocate(headerBufferLength);

    reset();
}

RnpReceiver::~RnpReceiver() noexcept
{
}

void RnpReceiver::reset() noexcept
{
    rnpMessageBuffer.freeBuffer();
    headerBuffer.clearToRead();
    status  = waitingHeader;
    carrier = RnpTransport::crp_Unknown;
}

akg::CommBuffer* RnpReceiver::getCurrentBuffer() noexcept
{
    return status == readingMessage ? &rnpMessageBuffer : &headerBuffer;
}

akg::CommBuffer* RnpReceiver::getMessageBuffer() noexcept
{
    return &rnpMessageBuffer;
}

RnpTransport::CarrierProtocol
RnpReceiver::getCarrierProtocol() const noexcept
{
    return carrier;
}

int RnpReceiver::getCarrierHeaderSize() const noexcept
{
    return carrierHeaderLength;
}

const void* RnpReceiver::getCarrierHeader() noexcept
{
    return headerBuffer.getData();
}

bool RnpReceiver::isDiscarding() const noexcept
{
    return status == discarding ? true : false;
}

bool RnpReceiver::validateMessage() noexcept
{
    if (status == waitingHeader)
    {
        rnpHeader = NULL;

        if (isHttpCarrier() || isRnpCarrier())
        {
            // a valid carrier header was detected
            if (rnpHeader != NULL)
            {
                // we can switch to reading the message
                if (prepareMessageBuffer())
                {
                    status = readingMessage;
                }
                else
                {
                    status = discarding;
                    return false;
                }
            }
            else
            {
                // status == readingHeader, but rnpHeader == NULL
                // so we wait for some more message
                return false;
            }
        }
        else
        {
            status = discarding;
            return false;
        }
    }

    if (status == readingMessage)
    {
        if (rnpMessageBuffer.getNotFilledSize() != 0)
        {
            return false;
        }
    }
    if (status == discarding)
    {
        LDEBUG << "RnpReceiver::validateMessage - discarding(3).";
        headerBuffer.clearToRead();
        return false;
    }
    return true;
}

/* the isXXXCarrier() functions have to:
    - return true if the message is or might be an XXX embedded RnpMessage
    - set rnpHeader only if there is a valid carrier header
    - set carrierHeaderLength in this case

   so:
    - invalid carrier returns false / rnpHeader == NULL
    - valid carrier but not rnp - false /rnpHeader != NULL
    - valid carrier but not enough data to be sure it's also valid rnp - true/rnpHeader == NULL
    - valid carrier & valid rnp header =>true/rnpHeader != NULL carrierHeaderLength set
*/

bool RnpReceiver::isHttpCarrier() noexcept
{
    char* data = static_cast<char*>(headerBuffer.getData());

    rnpHeader = NULL;
    carrierHeaderLength = -1;

    bool isHttpReqHeader = strncmp(data, "POST RnpMessage HTTP/1.1", 24) ? false : true;
    bool isHttpAnsHeader = strncmp(data, "HTTP/1.1 200 OK"         , 15) ? false : true;

    if (isHttpReqHeader == false && isHttpAnsHeader == false)
    {
        return false;
    }

    char* eoHttp = strstr(data, "\r\n\r\n");

    if (eoHttp == NULL)
    {
        return false;
    }

    carrierHeaderLength = eoHttp - data + 4;

    if (carrierHeaderLength == -1)
    {
        return false;
    }

    if (carrierHeaderLength + static_cast<int>(sizeof(RnpHeader)) > headerBuffer.getDataSize())
    {
        // is HTTP carrier, but we need more data to say if it's an embedded RnpMessage
        return true;
    }

    rnpHeader = (RnpHeader*)(data + carrierHeaderLength);

    bool isRnp = rnpHeader->isRnpMessage();

    if (isRnp)
    {
        carrier = RnpTransport::crp_Http;
        LDEBUG << "RnpReceiver::isHttpCarrier - valid HTTP carrier detected.";
    }

    return isRnp;
}

bool RnpReceiver::isRnpCarrier() noexcept
{
    char* data = static_cast<char*>(headerBuffer.getData());

    rnpHeader = NULL;
    carrierHeaderLength = -1;

    if (static_cast<int>(sizeof(RnpHeader)) > headerBuffer.getDataSize())
    {
        // we need more data to say if it's an RnpMessage
        return true;
    }

    rnpHeader = (RnpHeader*)data;
    carrierHeaderLength = 0;

    bool isRnp = rnpHeader->isRnpMessage();

    if (isRnp)
    {
        carrier = RnpTransport::crp_Rnp;
        LDEBUG << "RnpReceiver::isRnpCarrier - valid RNP carrier detected!";
    }

    return isRnp;
}

bool RnpReceiver::prepareMessageBuffer() noexcept
{
    if (rnpMessageBuffer.allocate(rnpHeader->getTotalLength()) == false)
    {
        return false;
    }

    char* data = static_cast<char*>(headerBuffer.getData());

    rnpMessageBuffer.read(data + carrierHeaderLength, headerBuffer.getDataSize() - carrierHeaderLength);

    RnpHeader* nRnpHeader = static_cast<RnpHeader*>(rnpMessageBuffer.getData());

    return true;
}

//########################################################################################################

RnpTransmitter::RnpTransmitter() noexcept
{
    carrier     = NULL;
    carrierType = RnpTransport::crp_Http;
}

RnpTransmitter::~RnpTransmitter() noexcept
{
    if (carrier != NULL)
    {
        delete carrier;
    }
}

bool RnpTransmitter::startRequest(RnpQuark serverType, RnpTransport::CarrierProtocol desiredProtocol) noexcept
{
    getCarrierObject(desiredProtocol);

    if (carrier == NULL)
    {
        return false;
    }

    startMessage(serverType, carrier->getRequestHeaderLength());

    return true;
}

bool RnpTransmitter::startAnswer(RnpQuark serverType, RnpTransport::CarrierProtocol desiredProtocol) noexcept
{
    getCarrierObject(desiredProtocol);

    if (carrier == NULL)
    {
        return false;
    }

    startMessage(serverType, carrier->getAnswerHeaderLength());

    return true;
}

akg::CommBuffer* RnpTransmitter::endMessage() noexcept
{
    if (carrier == NULL)
    {
        return NULL;
    }

    akg::CommBuffer* theBuffer = RnpProtocolEncoder::endMessage();
    carrier->putHeader(theBuffer);

    return theBuffer;
}

RnpTransport::CarrierProtocol
RnpTransmitter::getCarrierProtocol() noexcept
{
    return carrierType;
}

RnpCarrier* RnpTransmitter::getCarrierObject(RnpTransport::CarrierProtocol desiredProtocol) noexcept
{
    carrierType = desiredProtocol;

    if (carrier != NULL)
    {
        delete carrier;
    }

    switch (carrierType)
    {
    case RnpTransport::crp_Rnp :
        carrier = new RnpCarrier;
        break;
    case RnpTransport::crp_Http:
        carrier = new HttpRnpCarrier;
        break;

    case RnpTransport::crp_BadCarrier:
        carrier = new BadRnpCarrier;
        break;

    default                    :
        carrier = NULL;
        break;
    }

    return carrier;
}

int  RnpTransmitter::getBufferSize() const noexcept
{
    if (!(commBuffer != 0))
    {
        LDEBUG << "RnpTransmitter::getBufferSize(): warning: assert will fire.";
    }
    assert(commBuffer != 0);
    return commBuffer->getBufferSize();
}

int  RnpTransmitter::getNotFilledSize() const noexcept
{
    if (!(commBuffer != 0))
    {
        LDEBUG << "RnpTransmitter::getNotFilledSize(): warning: assert will fire.";
    }
    assert(commBuffer != 0);
    return commBuffer->getNotFilledSize();
}

int  RnpTransmitter::getDataSize() const noexcept
{
    if (!(commBuffer != 0))
    {
        LDEBUG << "RnpTransmitter::getDataSize(): warning: assert will fire.";
    }
    assert(commBuffer != 0);
    return commBuffer->getDataSize();
}

//################################################

RnpCarrier::RnpCarrier() noexcept
{
    type = RnpTransport::crp_Rnp;
}

RnpCarrier::~RnpCarrier() noexcept
{
}

RnpTransport::CarrierProtocol RnpCarrier::getType() noexcept
{
    return type;
}

int  RnpCarrier::getRequestHeaderLength() noexcept
{
    requestHeader = true;
    return 0;
}

int  RnpCarrier::getAnswerHeaderLength() noexcept
{
    requestHeader = false;
    return 0;
}

void RnpCarrier::putHeader(akg::CommBuffer*) noexcept
{
    // nothing!!
}


HttpRnpCarrier::HttpRnpCarrier() noexcept
{
    type = RnpTransport::crp_Http;
}


int HttpRnpCarrier::getRequestHeaderLength() noexcept
{
    requestHeader = true;
    return strlen(theRequestHeader);
}

int HttpRnpCarrier::getAnswerHeaderLength() noexcept
{
    requestHeader = false;
    return strlen(theAnswerHeader);
}

void HttpRnpCarrier::putHeader(akg::CommBuffer* messageBuffer) noexcept
{
    char* data = static_cast<char*>(messageBuffer->getData());

    const char* header  = requestHeader ? theRequestHeader : theAnswerHeader;
    int    headerLength = strlen(header);
    int     posOfLength = headerLength - 14;

    strncpy(data, header, static_cast<size_t>(posOfLength));

    sprintf(data + posOfLength, "%10d", messageBuffer->getDataSize() - headerLength);

    strncpy(data + headerLength - 4, "\r\n\r\n", 4); // it shouldn't be null terminated!
}

const char HttpRnpCarrier::theRequestHeader[] =
    "POST RnpMessage HTTP/1.1\r\nAccept: bin/rnp\r\nUserAgent: RnpClient/1.0\r\nContent-length: uxxxyyyzzz\r\n\r\n";
//                                                ^10 digits

const char HttpRnpCarrier::theAnswerHeader[] =
    "HTTP/1.1 200 OK\r\nContent-type: bin/rnp\r\nContent-length: uxxxyyyzzz\r\n\r\n";



BadRnpCarrier::BadRnpCarrier() noexcept
{
    type = RnpTransport::crp_BadCarrier;
}

int BadRnpCarrier::getRequestHeaderLength() noexcept
{
    requestHeader = true;
    return strlen(theHeader);
}

int BadRnpCarrier::getAnswerHeaderLength() noexcept
{
    requestHeader = false;
    return strlen(theHeader);
}

void BadRnpCarrier::putHeader(akg::CommBuffer* messageBuffer) noexcept
{
    char* data = static_cast<char*>(messageBuffer->getData());

    strncpy(data, theHeader, strlen(theHeader));
}

const char BadRnpCarrier::theHeader[] = "BadCarrier";


//DIAGNOSTIC_POP
