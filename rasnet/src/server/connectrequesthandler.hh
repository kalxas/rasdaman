#ifndef RASNET_SRC_CONNECTREQUESTHANDLER_HH
#define RASNET_SRC_CONNECTREQUESTHANDLER_HH

#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>

#include "../messages/communication.pb.h"
#include "clientpool.hh"

namespace rasnet
{
class ConnectRequestHandler
{
public:
    /**
     * Create a ClientRequestHandler
     * @param pool Reference to the pool of clients owned by the ServerProxy using this handler.
     * @param retries The number of times a client should try to contact the server before giving up
     * @param period The number of milliseconds between each consecutive retry.
     */
    ConnectRequestHandler(zmq::socket_t& socket, boost::shared_ptr<ClientPool> clientPool, boost::int32_t retries, boost::int32_t lifetime);

    virtual ~ConnectRequestHandler();

    /**
     * Decide if the given message can be processed by this handler.
     * @param message
     * @return true if the message contains a ConnectRequest message, false otherwise
     */
    bool canHandle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message);

    /**
     * Handle the ConnectRequest message contained in the BaseMessage by adding the client to the pool
     * and sending back a ConnectReply message.
     * @param message
     * @param peer_id
     * @param socket
     */
    void handle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message, const std::string& peerId);
private:
    zmq::socket_t& socket;
    boost::shared_ptr<ClientPool> clientPool;/*!< Pool of clients.*/
    ConnectReply connectReply;
};

}
#endif // RASNET_SRC_CONNECTREQUESTHANDLER_HH
