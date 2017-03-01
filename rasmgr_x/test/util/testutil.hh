#ifndef RASMGR_X_TEST_UTIT_TESTUTIL_HH
#define RASMGR_X_TEST_UTIT_TESTUTIL_HH

#include <cstdlib>

namespace rasmgr
{
namespace test
{
class TestUtil
{
public:
    template <typename T>
    static T generateRandomElement(T min, T max)
    {
        return  min + (rand() % (int)(max - min + 1));
    }

    static bool randomBool()
    {
        return rand() % 2;
    }
};
}
}

#endif // RASMGR_X_TEST_UTIT_TESTUTIL_HH
