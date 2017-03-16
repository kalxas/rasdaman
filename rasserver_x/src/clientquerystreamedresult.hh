#ifndef CLIENTQUERYRESULT_HH
#define CLIENTQUERYRESULT_HH

#include <string>
#include <boost/cstdint.hpp>

namespace rasserver
{

/**
 * @brief The ClientQueryStreamedResult class
 */
struct DataChunk
{
    char* bytes;
    boost::uint64_t length;
};

class ClientQueryStreamedResult
{
public:
    /**
     * @brief CHUNK_SIZE The maximum size which can be served from the data.
     */
    static const boost::uint64_t CHUNK_SIZE = 50 * 1024 * 1024; // 50 MB

    /**
     * @brief ClientQueryStreamedResult Class streaming an array given the address and length.
     *
     * IMPORTANT: THIS CLASS TAKES OWNERSHOP OVER THE "data" POINTER AND IT FREES IT.
     *
     * @param data The address where the array is storred.
     * @param length The length of the array.
     * @param clientUUID The unique identifier of the client used to cleanup the memory. (@see clientmanager.cc)
     */
    ClientQueryStreamedResult(char* data, boost::uint64_t length, const std::string& clientUUID);

    /**
     * @brief getNextChunk Retrieves the next chunk of data from the array.
     * @return A pair containing the address of the chunk and the lenght of it.
     */
    DataChunk getNextChunk();

    /**
     * @brief getClientUUID Returns the client unique identifier used for cleanup. (@see clientmanager.cc)
     * @return A string representing the client uuid.
     */
    std::string getClientUUID() const;

    /**
     * @brief getRemainingBytesLength Computes the remaining bytes left to be served from the array.
     * @return An integer representing the bytes left to be served.
     */
    boost::uint64_t getRemainingBytesLength() const;

    /**
     * Destructor responsible with deleting the data pointer which was split into chunks.
     */
    ~ClientQueryStreamedResult();

private:
    /**
     * @brief data The raw data pointer.
     */
    char* data;

    /**
     * @brief length The length of the array in bytes.
     */
    boost::uint64_t length;

    /**
     * @brief clientUUID The client uuid which requested a char array to be split into chunks. Used for cleanup in @see clientmanager.cc
     */
    std::string clientUUID;
    
    /**
     * @brief offset The offset where the nextChunk begins.
     */
    boost::uint64_t offset;

};

} //namespace rasserver

#endif // CLIENTQUERYRESULT_HH
