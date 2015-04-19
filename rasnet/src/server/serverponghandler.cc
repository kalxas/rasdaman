#include "../../../common/src/logging/easylogging++.hh"
#include "../exception/unsupportedmessageexception.hh"
#include "../common/zmqutil.hh"

#include "serverponghandler.hh"

namespace rasnet
{

ServerPongHandler::ServerPongHandler(boost::shared_ptr<ClientPool> clientPool):clientPool(clientPool)
{

}

ServerPongHandler::~ServerPongHandler()
{

}

bool ServerPongHandler::canHandle(const std::vector<boost::shared_ptr<zmq::message_t> > &message)
{
    MessageType messageType;

    if(message.size() == 1
            && messageType.ParseFromArray(message[0]->data(), message[0]->size())
            && messageType.type() == MessageType::ALIVE_PONG)
    {
        return true;
    }
    else
    {
        return false;
    }
}

void ServerPongHandler::handle(const std::vector<boost::shared_ptr<zmq::message_t> > &message, const std::string &peerId)
{
    if (this->canHandle(message))
    {
        this->clientPool->resetClientStatus(peerId);
    }
    else
    {
        throw UnsupportedMessageException();
    }
}

}
