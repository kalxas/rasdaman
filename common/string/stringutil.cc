#include "stringutil.hh"
#include <sstream>
#include <iterator>
#include <algorithm>
#include <cassert>
#include <cstring>  // memcpy

namespace common
{

std::string StringUtil::concat(const std::vector<std::string> &v, char sep)
{
    return concat(v, std::string(1, sep));
}

std::string StringUtil::concat(const std::vector<std::string> &v, std::string sep)
{
    if (v.size() == 1)
        return v.front();
    else
    {
        std::string ret{};
        for (const std::string &item: v)
        {
            if (!ret.empty()) ret += sep;
            ret += item;
        }
        return ret;
    }
}

std::vector<std::string> StringUtil::split(const std::string &v, char sep)
{
    std::vector<std::string> ret;
    std::stringstream ss(v);
    std::string item;
    while (std::getline(ss, item, sep))
    {
        ret.push_back(item);
    }
    return ret;
}

std::vector<std::string> StringUtil::split(const std::string &s, const std::string &sep)
{
    std::vector<std::string> ret;
    auto start = 0U;
    auto end = s.find(sep);
    while (end != std::string::npos)
    {
        ret.push_back(s.substr(start, end - start));
        start = end + sep.length();
        end = s.find(sep, start);
    }
    ret.push_back(s.substr(start, end));
    return ret;
}

std::string StringUtil::toLowerCase(std::string s)
{
    std::transform(s.begin(), s.end(), s.begin(), ::tolower);
    return s;
}

std::string StringUtil::toUpperCase(std::string s)
{
    std::transform(s.begin(), s.end(), s.begin(), ::toupper);
    return s;
}

std::string StringUtil::capitalize(std::string s)
{
    s[0] = toupper(s[0]);
    return s;
}

std::string StringUtil::trimLeft(const std::string &str)
{
    std::string s = std::string(str);
    s.erase(s.begin(), std::find_if(s.begin(), s.end(), std::not1(std::ptr_fun<int, int>(std::isspace))));
    return s;
}

std::string StringUtil::trimRight(const std::string &str)
{
    std::string s = std::string(str);
    s.erase(std::find_if(s.rbegin(), s.rend(), std::not1(std::ptr_fun<int, int>(std::isspace))).base(), s.end());
    return s;
}

std::string StringUtil::trim(std::string s)
{
    s.erase(s.begin(), std::find_if(s.begin(), s.end(), std::not1(std::ptr_fun<int, int>(std::isspace))));
    s.erase(std::find_if(s.rbegin(), s.rend(), std::not1(std::ptr_fun<int, int>(std::isspace))).base(), s.end());
    return s;
}

bool StringUtil::startsWith(const char *s, const char *prefix)
{
    if (!s)
        return false;

    assert(prefix);

    while (s[0] != '\0' && prefix[0] != '\0')
    {
        if (isspace(s[0]))
        {
            ++s;
        }
        else
        {
            if (!isalpha(prefix[0]))
            {
                if (s[0] != prefix[0])
                    return false;
            }
            else
            {
                assert(islower(prefix[0]));
                if (tolower(s[0]) != prefix[0])
                    return false;
            }
            ++s;
            ++prefix;
        }
    }
    return true;
}

bool StringUtil::endsWith(const char *s, const char *suffix)
{
    if (!s)
        return false;

    assert(suffix);

    auto sn = strlen(s);
    auto suffn = strlen(suffix);
    if (suffn > sn)
        return false;

    for (size_t i = sn - suffn, j = 0; i < sn; ++i, ++j)
        if (s[i] != suffix[j])
            return false;
    return true;
}

bool StringUtil::equalsCaseInsensitive(const std::string &a, const std::string &b)
{
    if (a.size() != b.size())
        return false;
    for (size_t i = 0; i < a.size(); ++i)
        if (tolower(a[i]) != tolower(b[i]))
            return false;
    return true;
}

bool StringUtil::startsWithExactCase(const char *s, const char *prefix)
{
    if (!s)
        return false;

    assert(prefix);

    while (s[0] != '\0' && prefix[0] != '\0')
    {
        if (!isspace(s[0]))
        {
            if (s[0] != prefix[0])
                return false;
            ++prefix;
        }
        ++s;
    }
    return true;
}

int StringUtil::countDigits(long long n)
{
    if (n < 10ll) return 1;
    if (n < 100ll) return 2;
    if (n < 1000ll) return 3;
    if (n < 10000ll) return 4;
    if (n < 100000ll) return 5;
    if (n < 1000000ll) return 6;
    if (n < 10000000ll) return 7;
    if (n < 100000000ll) return 8;
    if (n < 1000000000ll) return 9;
    if (n < 10000000000ll) return 10;
    if (n < 100000000000ll) return 11;
    if (n < 1000000000000ll) return 12;
    if (n < 10000000000000ll) return 13;
    if (n < 100000000000000ll) return 14;
    if (n < 1000000000000000ll) return 15;
    if (n < 10000000000000000ll) return 16;
    if (n < 100000000000000000ll) return 17;
    if (n < 1000000000000000000ll) return 18;
    return 19;
}

int StringUtil::stringToInt(const std::string &str)
{
    int Result;                      //number which will contain the result
    std::stringstream convert(str);  // stringstream used for the conversion initialized with the contents of Text

    if (!(convert >> Result))  //give the value to Result using the characters in the string
    {
        Result = 0;  //if that fails set Result to 0
    }
    return Result;
}

// numbers 00-99
const char digits100[] =
    "00010203040506070809101112131415161718192021222324252627282930313233343536373839"
    "40414243444546474849505152535455565758596061626364656667686970717273747576777879"
    "8081828384858687888990919293949596979899";

char *StringUtil::uintToString(long long value, char *dst)
{
    if (value < 10)
    {
        *dst = static_cast<char>('0' + value);
        return dst + 1;
    }

    dst += countDigits(value);
    char *ret = dst;

    while (value >= 100)
    {
        const auto index = static_cast<unsigned int>((value % 100) * 2);
        value /= 100;
        *--dst = static_cast<char>(digits100[index + 1]);
        *--dst = static_cast<char>(digits100[index]);
    }
    if (value < 10)
    {
        *--dst = static_cast<char>('0' + value);
        return ret;
    }
    const auto index = static_cast<unsigned int>(value * 2);
    *--dst = static_cast<char>(digits100[index + 1]);
    *--dst = static_cast<char>(digits100[index]);
    return ret;
}

std::string StringUtil::intToString(const int number)
{
    return std::to_string(number);
}

std::string StringUtil::getRandomAlphaNumString(const int length)
{
    std::string s(static_cast<size_t>(length), 0);
    static const char alphanum[] =
        "0123456789"
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "abcdefghijklmnopqrstuvwxyz";

    for (size_t i = 0; i < static_cast<size_t>(length); ++i)
        s[i] = alphanum[static_cast<size_t>(rand()) % (sizeof(alphanum) - 1)];

    return s;
}

void StringUtil::removeCharacters(std::string &s, char c)
{
    s.erase(remove_if(s.begin(), s.end(),
                      [c](const char &sc)
                      {
                          return sc == c;
                      }),
            s.end());
}

void StringUtil::unescapeCharacters(std::string &s)
{
    removeCharacters(s, '\\');
}

bool StringUtil::contains(const std::string &s, const std::string &subString)
{
    return s.find(subString) != std::string::npos;
}

void StringUtil::memsetPattern(char *dest, size_t destSize,
                               const char *pattern, size_t patternSize)
{
    assert(dest);
    assert(pattern);
    assert(destSize > 0);
    assert(patternSize > 0);
    assert(destSize >= patternSize);
    assert(destSize % patternSize == 0);

    size_t offset = 0;
    while (offset < destSize)
    {
        memcpy(dest, pattern, patternSize);
        dest += patternSize;
        offset += patternSize;
    }
}

}  // namespace common
