#include <gtest/gtest.h>
#include "../src/clientquerystreamedresult.hh"
#include <cstring>

namespace rasserver
{
namespace test
{
TEST(ClientQueryStreamedResultTest, streamSmallSize)
{
    int dataSize = 100;

    char* data = new char[dataSize];
    for (int i = 0; i < dataSize; ++i)
    {
        data[i] = (char)i;
    }

    ClientQueryStreamedResult *result = new ClientQueryStreamedResult(data, dataSize, "");
    std::pair<int, char*> nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.first, dataSize);
    ASSERT_EQ(result->getRemainingBytesLength(), 0);
    ASSERT_STREQ(data, nextChunk.second);

    delete result;
}

TEST(ClientQueryStreamedResultTest, streamBigSize)
{
    int dataSize = ClientQueryStreamedResult::CHUNK_SIZE + ClientQueryStreamedResult::CHUNK_SIZE + ClientQueryStreamedResult::CHUNK_SIZE / 2; // 2.5 chunks

    char* data = new char[dataSize];
    char* newData = new char[dataSize];

    for (int i = 0; i < dataSize; ++i)
    {
        data[i] = (char)i;
    }

    ClientQueryStreamedResult *result = new ClientQueryStreamedResult(data, dataSize, "");
    std::pair<int, char*> nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.first, ClientQueryStreamedResult::CHUNK_SIZE);
    ASSERT_EQ(result->getRemainingBytesLength(), dataSize - ClientQueryStreamedResult::CHUNK_SIZE);
    std::strcpy(newData, nextChunk.second);

    nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.first, ClientQueryStreamedResult::CHUNK_SIZE);
    ASSERT_EQ(result->getRemainingBytesLength(), dataSize - ClientQueryStreamedResult::CHUNK_SIZE - ClientQueryStreamedResult::CHUNK_SIZE);
    std::strcat(newData, nextChunk.second);

    nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.first, ClientQueryStreamedResult::CHUNK_SIZE / 2);
    ASSERT_EQ(result->getRemainingBytesLength(), 0);
    std::strcat(newData, nextChunk.second);

    ASSERT_STREQ(data, newData);

    delete result;
    delete[] newData;
}

TEST(ClientQueryStreamedResultTest, testZeroSize)
{
    int dataSize = 0;

    char* data = new char[dataSize];

    ClientQueryStreamedResult *result = new ClientQueryStreamedResult(data, dataSize, "");
    std::pair<int, char*> nextChunk = result->getNextChunk();
    ASSERT_EQ(nextChunk.first, 0);
    ASSERT_EQ(data, nextChunk.second);

    delete[] data;
}

}
}
