#include "clientquerystreamedresult.hh"

namespace rasserver
{

const std::uint64_t ClientQueryStreamedResult::CHUNK_SIZE;

ClientQueryStreamedResult::ClientQueryStreamedResult(
    char* dataArg, std::uint64_t lengthArg, const std::string& clientUUIDArg,
    bool* nullMaskArg, std::uint64_t nullMaskLengthArg)
    : clientUUID(clientUUIDArg),
      data(dataArg), length(lengthArg),
      nullMask(nullMaskArg), nullMaskLength(nullMaskLengthArg)
{
}

DataChunk ClientQueryStreamedResult::getNextChunk()
{
    DataChunk chunk;

    chunk.length = std::min(this->getRemainingBytesLength(), CHUNK_SIZE);
    chunk.bytes = this->data.get() + this->dataOffset;
    this->dataOffset += chunk.length;

    if (this->nullMask != nullptr) {
      chunk.nullMaskLength = std::min(this->getRemainingNullMaskBytesLength(), CHUNK_SIZE);
      chunk.nullMask = this->nullMask.get() + this->nullMaskOffset;
      this->nullMaskOffset += chunk.nullMaskLength;
    }

    return chunk;
}

const std::string &ClientQueryStreamedResult::getClientUUID() const
{
    return this->clientUUID;
}

std::uint64_t ClientQueryStreamedResult::getRemainingBytesLength() const
{
    return this->length - this->dataOffset;
}

std::uint64_t ClientQueryStreamedResult::getRemainingNullMaskBytesLength() const
{
  return this->nullMask != nullptr 
      ? this->nullMaskLength - this->nullMaskOffset : 0;
}

}
