#ifndef _COMMON_STRINGUTIL_HH_
#define _COMMON_STRINGUTIL_HH_

#include <string>
#include <vector>

namespace common {

class StringUtil {
 public:
  static std::string concat(const std::vector<std::string> &v, char sep = ',');

  static std::string concat(const std::vector<std::string> &v, std::string sep);

  static std::vector<std::string> split(const std::string &v, char sep = ',');
  
  /**
   * Splits a string into pieces based on a separator
   * @param s the string to be split
   * @param sep the separator to split on
   */
  static std::vector<std::string> split(const std::string &s, const std::string &sep);

  static std::string toLowerCase(std::string s);

  static std::string toUpperCase(std::string s);
  
  /**
   * Capitalizes the string, i.e. makes first letter uppercase
   */
  static std::string capitalize(std::string str);
  
  /**
  * Trims all space characters from the left of the string
  * @param str the string to be trimmed
  * @return the trimmed string
  */
  static std::string trimLeft(const std::string &str);

  /**
  * Trims all space characters from the right of the string
  * @param str the string to be trimmed
  * @return the trimmed string
  */
  static std::string trimRight(const std::string &str);

  /**
  * Trims all space characters from both left and right of the string.
  * @param str the string to be trimmed
  * @return the trimmed string
  */
  static std::string trim(const std::string &str);
  
  /**
   * Check if a string s starts with string prefix, ignoring whitespace 
   * (isspace) and case of s. Preconditions:
   *  - s and prefix must be NUL-delimited
   *  - prefix must be lower-case and must not be NULL; s is converted to 
   *    lower-case while checking.
   *
   * @return true if s starts with prefix, otherwise false.
   */
  static bool startsWith(const char *s, const char *prefix);
  
  /**
   * Check if a string s ends with string suffix. Preconditions:
   *  - s and suffix must be NUL-delimited
   *  - suffix must not be NULL
   *
   * @return true if s ends with suffix, otherwise false.
   */
  static bool endsWith(const char *s, const char *suffix);
  
  /**
   * Check if a string a is equal to b (case-insensitive).
   * a and b must be NUL-delimited and must not be NULL.
   *
   * @return true if a is equal to b, false otherwise.
   */
  static bool equalsCaseInsensitive(const std::string &a, const std::string &b);
  
  /**
   * Check if a string s starts with string prefix, ignoring whitespace 
   * (isspace) in s. Preconditions:
   *  - s and prefix must be NUL-delimited
   *  - prefix must not be NULL
   *
   * @return true if s starts with prefix, otherwise false.
   */
  static bool startsWithExactCase(const char *s, const char *prefix);

  /**
   * @return number of digits in n (maximum 19).
   */
  static int countDigits(long long n);
  
  /**
  * Converts a string to an integer. Defaults to 0 if the string is incorrect;
  * @param str
  * @return integer containing the result
  */
  static int stringToInt(const std::string& str);

  /**
   * Serialize (positive) value into dst.
   * @return pointer to the next address following the serialized number, e.g.
   * 123 is serialized as ['1','2','3',end,...] and the returned pointer points to 
   * the address at end.
   */
  static char* uintToString(long long value, char* dst);
  
  /**
   * Transforms an integer into a std::string
   * @param number - the integer(int) you want to convert
   * @return the string representation
   */
  static std::string intToString(const int number);
  
  /**
   * Generate a random alphanumeric string
   * @param length the length of the string
   * @return the generated string
   */
  static std::string getRandomAlphaNumString(const int length);
  
  /**
   * Remove all characters in s matching c in place.
   */
  static void removeCharacters(std::string &s, char c);
  
  /**
   * Remove any escape characters '\' from s in place.
   */
  static void unescapeCharacters(std::string &s);
  
  /**
   * @return true if s contains subString, false otherwise.
   */
  static bool contains(const std::string &s, const std::string &subString);
  
  /**
   * Fill dest with a pattern of bytes, in contrast to standard memset which
   * allows a single byte.
   * 
   * The destSize MUST be divisible by patternSize, otherwise calling this 
   * function is undefined behavior.
   */
  static void memsetPattern(char *dest, size_t destSize,
                            const char *pattern, size_t patternSize);
};

} // namespace common

#endif
