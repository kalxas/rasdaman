#ifndef RASNET_SRC_CONNECTREQUESTHANDLER_HH
#define RASNET_SRC_CONNECTREQUESTHANDLER_HH

#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>

#include "../messages/communication.pb.h"
#include "clientpool.hh"

namespace rasnet
{
/**
 * @brief The ConnectRequestHandler class Handles an incoming connect request from a client
 * by responding with a connect reply containing connection parameters.
 */
class ConnectRequestHandler
{
public:
    /**
     * @brief ConnectRequestHandler
     * @param socket Socket through which replies to clients are sent.
     * @param clientPool ClientPool that keeps track of all the clients connected to this server.
     * @param retries The number of times a client should try to contact the server before giving up
     * @param lifetime The number of milliseconds between each consecutive retry.
     */
    ConnectRequestHandler(zmq::socket_t& socket, boost::shared_ptr<ClientPool> clientPool, boost::int32_t retries, boost::int32_t lifetime);

    virtual ~ConnectRequestHandler();

    /**
     * Decide if the given message can be processed by this handler.
     * @param message Composite message of the type:
     * | MessageType | ConnectRequest |
     * where MessageType.type() == MessageType::CONNECT_REQUEST
     * @return true if the message contains a ConnectRequest message(@see developer documentation for details),
     * false otherwise.
     */
    bool canHandle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message);

    /**
     * Handle the ConnectRequest message by adding the client to the pool
     * and sending back a ConnectReply message.
     * @param message
     * @param peerId
     */
    void handle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message, const std::string& peerId);
private:
    zmq::socket_t& socket;/*!<ZMQ_ROUTER socket representing the server*/
    boost::shared_ptr<ClientPool> clientPool;/*!< Pool of clients.*/
    ConnectReply connectReply;
};

}
#endif // RASNET_SRC_CONNECTREQUESTHANDLER_HH
