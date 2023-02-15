#include <gtest/gtest.h>
#include "../src/clientquerystreamedresult.hh"
#include <cstring>
#include <cstdint>

namespace rasserver
{
namespace test
{
TEST(ClientQueryStreamedResultTest, streamSmallSize) {

    std::uint64_t dataSize = 100;

    char* data = new char[dataSize];
    for (size_t i = 0; i < dataSize; ++i)
    {
        data[i] = (char)i;
    }

    ClientQueryStreamedResult* result = new ClientQueryStreamedResult(data, dataSize, 1);
    DataChunk nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.length, dataSize);
    ASSERT_EQ(result->getRemainingBytesLength(), 0);
    ASSERT_STREQ(data, nextChunk.bytes);

    delete result;
}

TEST(ClientQueryStreamedResultTest, streamBigSize)
{
    std::uint64_t dataSize = ClientQueryStreamedResult::CHUNK_SIZE + ClientQueryStreamedResult::CHUNK_SIZE + ClientQueryStreamedResult::CHUNK_SIZE / 2; // 2.5 chunks

    char* data = new char[dataSize];
    char* newData = new char[dataSize];

    for (size_t i = 0; i < dataSize; ++i)
    {
        data[i] = (char)i;
    }

    ClientQueryStreamedResult* result = new ClientQueryStreamedResult(data, dataSize, 1);
    DataChunk nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.length, ClientQueryStreamedResult::CHUNK_SIZE);
    ASSERT_EQ(result->getRemainingBytesLength(), dataSize - ClientQueryStreamedResult::CHUNK_SIZE);
    std::strcpy(newData, nextChunk.bytes);

    nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.length, ClientQueryStreamedResult::CHUNK_SIZE);
    ASSERT_EQ(result->getRemainingBytesLength(), dataSize - ClientQueryStreamedResult::CHUNK_SIZE - ClientQueryStreamedResult::CHUNK_SIZE);
    std::strcat(newData, nextChunk.bytes);

    nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.length, ClientQueryStreamedResult::CHUNK_SIZE / 2);
    ASSERT_EQ(result->getRemainingBytesLength(), 0);
    std::strcat(newData, nextChunk.bytes);

    ASSERT_STREQ(data, newData);

    delete result;
    delete[] newData;
}

TEST(ClientQueryStreamedResultTest, testZeroSize)
{
    std::uint64_t dataSize = 0;

    char* data = new char[dataSize];

    ClientQueryStreamedResult* result = new ClientQueryStreamedResult(data, dataSize, 1);
    DataChunk nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.length, 0);
    ASSERT_EQ(data, nextChunk.bytes);

    delete result;
}

}
}
