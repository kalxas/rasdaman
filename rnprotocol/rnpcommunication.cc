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
 * - FIXME: uses assert() !!! -- PB 2003-nov-22
 *
 ****************************************************************************/

#include "config.h"
#include <assert.h>
#include <rnpcommunication.hh>

#ifdef AFTERV52
#include <rnpexception.hh>
#endif

#include "debug.hh"
#include "raslib/rminit.hh" // for RNP_COMM_TIMEOUT

#include <easylogging++.h>

using namespace rnp;

RnpClientJob::RnpClientJob() throw()
{
}

void RnpClientJob::init(CommBuffer* transmitterBuffer, RnpBaseClientComm* newClientComm) throw()
{
    if (!(transmitterBuffer != 0))
    {
        LDEBUG << "RnpClientJob::init(): warning: assert will fire.";
    }
    assert(transmitterBuffer != 0);
    if (!(newClientComm != 0))
    {
        LDEBUG << "RnpClientJob::init(): warning: assert will fire.";
    }
    assert(newClientComm != 0);

    rnpReceiver.reset();
    answerOk = false;
    currentBufferPtr = transmitterBuffer;
    clientCommPtr    = newClientComm;
    invalidFormat    = false;

    status = wks_notdefined;
}

void RnpClientJob::clearAnswerBuffer() throw()
{
    rnpReceiver.reset();
}

void RnpClientJob::resetState() throw()
{
    clearConnection();

    clientCommPtr->jobIsReady();

    status = wks_notdefined;
}
void RnpClientJob::processRequest() throw()
{
    answerOk = true;

    invalidFormat = false;

    resetState();
}

bool RnpClientJob::validateMessage() throw()
{
    bool validated = rnpReceiver.validateMessage();

    currentBufferPtr = rnpReceiver.getCurrentBuffer();

    if (validated == true)
    {
        status = wks_processing;
        return true;
    }

    if (rnpReceiver.isDiscarding())
    {
        LDEBUG << "RnpClientJob::validateMessage - discarding message";
        resetState();
        answerOk = false;
        invalidFormat = true;
    }
    answerOk = false;

    return false;
}

void RnpClientJob::executeOnWriteReady() throw()
{
    rnpReceiver.reset();

    currentBufferPtr->freeBuffer();

    currentBufferPtr = rnpReceiver.getCurrentBuffer();

    readyToReadAnswer();
}

void RnpClientJob::specificCleanUpOnTimeout() throw()
{
    answerOk = false;
    resetState();
}

void RnpClientJob::executeOnReadError() throw()
{
    answerOk = false;
    resetState();
}

void RnpClientJob::executeOnWriteError() throw()
{
    answerOk = false;
    resetState();
}

CommBuffer* RnpClientJob::getAnswerBuffer() throw()
{
    return rnpReceiver.getMessageBuffer();
}

bool RnpClientJob::isAnswerOk() throw()
{
    return answerOk;
}

bool RnpClientJob::isInvalidFormat() throw()
{
    return invalidFormat;
}


//###################################################

RnpBaseClientComm::RnpBaseClientComm(RnpQuark theServerType,  RnpTransport::CarrierProtocol theProtocol) throw()
{
    serverHost = NULL;
    serverPort = 0;
    serverType = theServerType;
    carrierProtocol =  theProtocol;

    initDefaultCommunication();
    maxRetry = 0;   // # of RE-tries -- PB 2005-aug-31
}

RnpBaseClientComm::RnpBaseClientComm(const char* theServerHost, int theServerPort, RnpQuark theServerType,  RnpTransport::CarrierProtocol theProtocol) throw()
{
    if (!(theServerHost != 0))
    {
        LDEBUG << "RnpBaseClientComm::RnpBaseClientComm(): warning: assert will fire.";
    }
    assert(theServerHost != 0);
    if (!(theServerPort > 0))
    {
        LDEBUG << "RnpBaseClientComm::RnpBaseClientComm(): warning: assert will fire.";
    }
    assert(theServerPort > 0);

    serverHost = theServerHost;
    serverPort = static_cast<unsigned int>(theServerPort);
    serverType = theServerType;
    carrierProtocol =  theProtocol;

    initDefaultCommunication();

    maxRetry = 0;   // # of RE-tries -- PB 2005-aug-31
}
RnpBaseClientComm::~RnpBaseClientComm() throw()
{
}

void RnpBaseClientComm::setConnectionParameters(const char* theServerHost, int theServerPort) throw()
{
    if (!(theServerHost != 0))
    {
        LDEBUG << "RnpBaseClientComm::setConnectionParameters(): warning: assert will fire.";
    }
    assert(theServerHost != 0);
    if (!(theServerPort > 0))
    {
        LDEBUG << "RnpBaseClientComm::setConnectionParameters(): warning: assert will fire.";
    }
    assert(theServerPort > 0);

    serverHost = theServerHost;
    serverPort = static_cast<unsigned int>(theServerPort);
}

void RnpBaseClientComm::setCarrierProtocol(RnpTransport::CarrierProtocol theProtocol) throw()
{
    carrierProtocol =  theProtocol;
}

RnpTransport::CarrierProtocol RnpBaseClientComm::getCarrierProtocol() throw()
{
    return carrierProtocol;
}

void RnpBaseClientComm::initDefaultCommunication() throw()
{
    communicatorPtr = &internalCommunicator;

    communicatorPtr->initJobs(1);
    communicatorPtr->setTimeout(RNP_COMM_TIMEOUT, 0);   // defined in raslib/rminit.hh -- PB 2005-sep-09

    communicatorPtr->attachJob(clientJob);

    // not necessary? transmitterBuffer.allocate(RNP_DEFAULTBUFFERSIZE);
}


void RnpBaseClientComm::jobIsReady() throw()
{
    communicatorPtr->shouldExit();
}

void RnpBaseClientComm::startRequest(RnpQuark command, int transmitterBufferSize)
{
    transmitterBuffer.allocate(transmitterBufferSize);

    clientJob.init(&transmitterBuffer, this);

    encoder.setBuffer(&transmitterBuffer);

    encoder.startRequest(serverType, carrierProtocol);
    encoder.startFragment(Rnp::fgt_Command, command);
}

bool RnpBaseClientComm::sendRequestGetAnswer()
{
    if (!(serverHost != NULL))
    {
        LDEBUG << "RnpBaseClientComm::sendRequestGetAnswer(): warning: assert will fire.";
    }
    assert(serverHost != NULL);
    if (!(serverPort > 0))
    {
        LDEBUG << "RnpBaseClientComm::sendRequestGetAnswer(): warning: assert will fire.";
    }
    assert(serverPort > 0);

    encoder.endFragment();
    encoder.endMessage();

    bool connected = false;
    for (unsigned int retry = 0; retry < maxRetry + 1 && !connected; retry++) // NB: first attempt + RE-tries! -- PB 2005-aug-31
    {
        connected = clientJob.connectToServer(serverHost, static_cast<int>(serverPort));
    }

    if (connected == false)
    {
#ifdef AFTERV52
        throw RnpIOException(clientJob.getErrno());
#endif
        return false;
    }

    communicatorPtr->runClient();

    if (clientJob.isAnswerOk() == false)
    {
#ifdef AFTERV52
        if (clientJob.isInvalidFormat())
        {
            throw RnpInvalidFormatException();
        }
        else
        {
            throw RnpIOException(clientJob.getErrno());
        }
#endif
        return false;
    }

    CommBuffer* receiverBuffer = clientJob.getAnswerBuffer();
    decoder.decode(receiverBuffer);
    decoder.getFirstFragment();

    return true;
}

bool RnpBaseClientComm::checkForExceptions()
{
    if (decoder.getFragmentType() != Rnp::fgt_Error)
    {
        return false;
    }
    return true;
}

void RnpBaseClientComm::clearAnswer() throw()
{
    clientJob.clearAnswerBuffer();
}

void RnpBaseClientComm::setMaxRetry(unsigned int newMaxRetry)
{
    maxRetry = newMaxRetry;
}

unsigned int  RnpBaseClientComm::getMaxRetry()
{
    return maxRetry;
}
//############# Server side ################################################
//#######################################################################
//#######################################################################

RnpServerJob::RnpServerJob() throw()
{
}

void RnpServerJob::init(RnpBaseServerComm* theServerComm) throw()
{
    if (!(theServerComm != 0))
    {
        LDEBUG << "RnpServerJob::init(): warning: assert will fire.";
    }
    assert(theServerComm != 0);

    rnpReceiver.reset();
    currentBufferPtr = rnpReceiver.getCurrentBuffer();
    serverCommPtr    = theServerComm;

    status = wks_accepting;
}

void RnpServerJob::processRequest() throw()
{
    serverCommPtr->processRequest(currentBufferPtr, &transmiterBuffer, rnpReceiver.getCarrierProtocol(), this);

    rnpReceiver.reset();

    currentBufferPtr = &transmiterBuffer;

    readyToWriteAnswer();
}

bool RnpServerJob::validateMessage() throw()
{

    bool validated = false;

    if (rnpReceiver.validateMessage() == true)
    {
        status = wks_processing;
        validated = true;
    }

    if (rnpReceiver.isDiscarding())
    {
        resetJob();
        validated = false;
    }

    currentBufferPtr = rnpReceiver.getCurrentBuffer();

    return validated;
}

void RnpServerJob::executeOnWriteReady() throw()
{
    resetJob();
}

void RnpServerJob::executeOnAccept() throw()
{
}

void RnpServerJob::specificCleanUpOnTimeout() throw()
{
    // initial era gol, dar...
    // clearConnection face cine apeleaza: NbJob::cleanUpIfTimeout()
    rnpReceiver.reset();

    transmiterBuffer.freeBuffer();

    currentBufferPtr = rnpReceiver.getCurrentBuffer();

    currentBufferPtr->clearToRead();

    status = wks_accepting;
}

void RnpServerJob::executeOnReadError() throw()
{
    resetJob();
}

void RnpServerJob::executeOnWriteError() throw()
{
    resetJob();
}

void RnpServerJob::resetJob() throw()
{
    clearConnection();

    rnpReceiver.reset();

    transmiterBuffer.freeBuffer();

    currentBufferPtr = rnpReceiver.getCurrentBuffer();

    currentBufferPtr->clearToRead();

    status = wks_accepting;
}

//###################################################
RnpBaseServerComm::RnpBaseServerComm() throw()
{
    nrServerJobs = 1;

    transmitterBufferSize = RNP_DEFAULTBUFFERSIZE;

    communicator = NULL;
}

RnpBaseServerComm::~RnpBaseServerComm() throw()
{
    disconnectFromCommunicator();
}

bool RnpBaseServerComm::setServerJobs(int nrOfServerJobs) throw()
{
    if (communicator != 0)
    {
        return false;
    }

    nrServerJobs = nrOfServerJobs;

    return true;
}

int RnpBaseServerComm::countServerJobs() throw()
{
    return nrServerJobs;
}

void RnpBaseServerComm::connectToCommunicator(NbCommunicator& theCommunicator)
{
    // throws whatever 'new' throws
    if (!(communicator == NULL))
    {
        LDEBUG << "RnpServerJob::init(): warning: assert will fire.";
    }
    assert(communicator == NULL);

    communicator = &theCommunicator;

    for (int i = 0; i < nrServerJobs; i++)
    {
        RnpServerJob* job = createJob();

        job->init(this);

        communicator->attachJob(*job);

        serverJob.push_back(job);
    }
}

bool RnpBaseServerComm::disconnectFromCommunicator() throw()
{
    if (communicator == NULL)
    {
        return false;
    }

    for (unsigned int i = 0; i < static_cast<unsigned int>(nrServerJobs); i++)
    {
        communicator->deattachJob(*(serverJob[i]));

        delete serverJob[i];
    }

    serverJob.clear();

    communicator = NULL;

    return true;
}

RnpServerJob* RnpBaseServerComm::createJob()
{
    return new RnpServerJob;
}


void RnpBaseServerComm::setTransmitterBufferSize(int nSize) throw()
{
    transmitterBufferSize = nSize;
}

int RnpBaseServerComm::getTransmitterBufferSize() throw()
{
    return transmitterBufferSize;
}


void RnpBaseServerComm::processRequest(CommBuffer* receiverBuffer, CommBuffer* transmiterBuffer, RnpTransport::CarrierProtocol protocol, __attribute__((unused)) RnpServerJob* callingJob) throw()
{
    // use 'callingJob' to get info about the client (hostaddress, etc)

    decoder.decode(receiverBuffer);
    RnpQuark destServerType       = decoder.getDestinationServerType();
    Rnp::Endianness desEndianness = decoder.getDesiredEndianness();

    // test if servertype matches!

    transmiterBuffer->allocate(transmitterBufferSize);
    transmiterBuffer->clearToRead();

    encoder.setBuffer(transmiterBuffer);
    encoder.setFinalEndianness(desEndianness);
    encoder.startAnswer(destServerType, protocol);

    decoder.getFirstFragment();
    bool wasError = false;
    for (int fragment = 0; fragment < decoder.countFragments(); fragment++)
    {
        if (wasError == false)
        {
            try
            {
                decodeFragment();
            }
            catch (...)
            {
                wasError = true;
                answerUnknownError();
            }
        }
        else
        {
            discardFragment();
        }
        decoder.getNextFragment();
    }
    encoder.endMessage();
}

const char* RnpBaseServerComm::getNextAsString(__attribute__((unused)) RnpQuark parameterType) const
{
    decoder.getNextParameter();
    //if(decoder.getParameterType != parameterType) throw something
    return decoder.getDataAsString();
}

int RnpBaseServerComm::getNextAsInteger(__attribute__((unused)) RnpQuark parameterType) const
{
    decoder.getNextParameter();
    //if(decoder.getParameterType != parameterType) throw something
    return decoder.getDataAsInteger();
}

float RnpBaseServerComm::getNextAsFloat(__attribute__((unused)) RnpQuark parameterType) const
{
    decoder.getNextParameter();
    //if(decoder.getParameterType != parameterType) throw something
    return decoder.getDataAsFloat();
}

double RnpBaseServerComm::getNextAsDouble(__attribute__((unused)) RnpQuark parameterType) const
{
    decoder.getNextParameter();
    //if(decoder.getParameterType != parameterType) throw something
    return decoder.getDataAsDouble();
}

const void* RnpBaseServerComm::getNextAsOpaque(__attribute__((unused)) RnpQuark parameterType) const
{
    decoder.getNextParameter();
    //if(decoder.getParameterType != parameterType) throw something
    return decoder.getDataAsOpaque();
}

int RnpBaseServerComm::getCurrentParameterLength() const throw()
{
    return decoder.getDataLength();
}

void RnpBaseServerComm::answerSTLException(exception& ex) throw()
{
    encoder.startFragment(Rnp::fgt_Error, decoder.getCommand());
    encoder.addInt32Parameter(Rnp::ert_StlException, 0);
    encoder.addStringParameter(Rnp::erp_whatValue, ex.what());
    encoder.endFragment();
}

void RnpBaseServerComm::answerUnknownError() throw()
{
    encoder.startFragment(Rnp::fgt_Error, decoder.getCommand());
    encoder.addInt32Parameter(Rnp::ert_Unknown, 0);
    encoder.endFragment();
}

void RnpBaseServerComm::discardFragment() throw()
{
    encoder.startFragment(Rnp::fgt_DiscardedRequest, decoder.getCommand());

    encoder.endFragment();
}

void RnpBaseServerComm::startOkAnswer() throw()
{
    encoder.startFragment(Rnp::fgt_OkAnswer, decoder.getCommand());
}

void RnpBaseServerComm::endOkAnswer() throw()
{
    encoder.endFragment();
}

void RnpBaseServerComm::communicatorShouldExit() throw()
{
    if (!(communicator != NULL))
    {
        LDEBUG << "RnpServerJob::init(): warning: assert will fire.";
    }
    assert(communicator != NULL);

    communicator->shouldExit();
}

