#include "clientquerystreamedresult.hh"

namespace rasserver
{

const std::uint64_t ClientQueryStreamedResult::CHUNK_SIZE;

ClientQueryStreamedResult::ClientQueryStreamedResult(char* dataArg, std::uint64_t lengthArg, const std::string& clientUUIDArg)
    : data(dataArg), length(lengthArg), clientUUID(clientUUIDArg), offset(0)
{

}

DataChunk ClientQueryStreamedResult::getNextChunk()
{
    DataChunk chunk;

    chunk.length = this->getRemainingBytesLength() < CHUNK_SIZE ? this->getRemainingBytesLength() : CHUNK_SIZE;
    chunk.bytes = this->data + this->offset;

    this->offset += chunk.length;

    return chunk;
}

std::string ClientQueryStreamedResult::getClientUUID() const
{
    return this->clientUUID;
}

std::uint64_t ClientQueryStreamedResult::getRemainingBytesLength() const
{
    return this->length - this->offset;
}

ClientQueryStreamedResult::~ClientQueryStreamedResult()
{
    if (this->data) {
        free(this->data);
        this->data = NULL;
    }
}

}
