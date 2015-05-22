#ifndef RASNET_SRC_SERVER_SERVERPONGHANDLER_HH
#define RASNET_SRC_SERVER_SERVERPONGHANDLER_HH

#include <boost/shared_ptr.hpp>

#include "clientpool.hh"

namespace rasnet
{
/**
 * @brief The ServerPongHandler class Handle PONG messages from
 * a peer by resetting its status
 */
class ServerPongHandler
{
public:
    ServerPongHandler(boost::shared_ptr<ClientPool> clientPool);

    virtual ~ServerPongHandler();

    /**
     * @brief canHandle Check if the message can be handled by this handler
     * @param message
     * @return TRUE if the messages can be handled, FALSE otherwise
     */
    bool canHandle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message);

    /**
     * @brief handle Handle the given message by resetting its status
     * @param message
     * @param peerId
     * @throws UnsupportedMessageException if an invalid message is passed in.
     * i.e. one for which canHandle returns false
     */
    void handle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message, const std::string& peerId);

private:
    boost::shared_ptr<ClientPool> clientPool; /*!< Pointer to the ClientPool that contains all the clients we can receive messages from */
};
}

#endif // RASNET_SRC_SERVER_SERVERPONGHANDLER_HH
