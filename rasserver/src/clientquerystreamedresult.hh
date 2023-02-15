#ifndef CLIENTQUERYRESULT_HH
#define CLIENTQUERYRESULT_HH

#include <string>
#include <cstdint>
#include <memory>

namespace rasserver
{

/**
 * A chunk of the whole data that contains the partial data and null mask.
 */
struct DataChunk
{
    char* bytes = nullptr;
    std::uint64_t length{};
    bool* nullMask = nullptr;
    std::uint64_t nullMaskLength{};
};

class ClientQueryStreamedResult
{
public:
    /**
     * The maximum size which can be served from the data.
     */
    static const std::uint64_t CHUNK_SIZE = 50 * 1024 * 1024; // 50 MB

    /**
     * Class streaming an array given the address and length.
     *
     * IMPORTANT: this class takes ownership over the data and nullMask
     * pointers, and frees them in the destructor.
     *
     * @param data The address where the array is stored.
     * @param length The length of the array.
     * @param clientUUID The unique identifier of the client used to cleanup
     *        the memory. (@see clientmanager.cc)
     * @param nullMask The null mask
     * @param nullMaskLength The size of the null mask
     */
    ClientQueryStreamedResult(char* data,
                              std::uint64_t length,
                              std::int32_t clientUUID,
                              bool* nullMask = nullptr,
                              std::uint64_t nullMaskLength = 0);

    /**
     * Retrieves the next chunk of data from the array.
     * @return A pair containing the address of the chunk and the length of it.
     */
    DataChunk getNextChunk();

    /**
     * Returns the client unique identifier used for cleanup. (@see clientmanager.cc)
     * @return A string representing the client uuid.
     */
    std::int32_t getClientUUID() const;

    /**
     * Computes the remaining bytes left to be served from the array.
     * @return An integer representing the array bytes left to be served.
     */
    std::uint64_t getRemainingBytesLength() const;

    /**
     * Computes the remaining bytes left to be served from the null mask.
     * @return An integer representing the null mask bytes left to be served.
     */
    std::uint64_t getRemainingNullMaskBytesLength() const;

    /**
     * Destructor responsible with deleting the data pointer which was split into chunks.
     */
    ~ClientQueryStreamedResult() = default;

private:
    /// The client uuid which requested a char array to be split into chunks.
    /// Used for cleanup in @see clientmanager.cc
    std::int32_t clientUUID;
    
    /// The raw data pointer.
    std::unique_ptr<char[]> data;
    /// The length of the array in bytes.
    std::uint64_t length;    
    /// The offset where the nextChunk begins.
    std::uint64_t dataOffset{};

    /// Optional pointer to the null mask.
    std::unique_ptr<bool[]> nullMask;
    /// The length of the null mask in bytes.
    std::uint64_t nullMaskLength;
    /// The offset where the nextChunk of nulls begins.
    std::uint64_t nullMaskOffset{};

};

} //namespace rasserver

#endif // CLIENTQUERYRESULT_HH
