/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/* Name: testing.h
 * Purpose: Unit testing framework. Provides the functions/MACROS to facilitate
 *      Testing.
 * Author: Sorin Stancu-Mara
 */

#ifndef RELBLOBIF_TESTING_H__
#define RELBLOBIF_TESTING_H__

#include <iostream>
#include <string>

using namespace std;


class Test
{
public:
    static ostream& log_;
    static bool test_result_;
    static int tests_run_;
    static int tests_passed_;
    static int getResult();
    static void startTimer();
    static double stopTimer();
    static string charPtrToString(char* ptr, unsigned int size);
private:
    static int timer_sec_, timer_usec_;
};

//disable the log from the modules
#ifdef LOG
#undef LOG
#endif  // LOG

#define LOG Test::log_ << __FILE__ << ":" << __LINE__ << " "

#undef TEST_FAIL
#define TEST_FAIL() \
    Test::test_result_ = false,\
                         LOG << "Test failed at " << __FILE__ << ":" << __LINE__ << endl

#undef EXPECT_TRUE
#define EXPECT_TRUE(a) \
    if (!(a)) Test::test_result_ = false, \
                                       LOG << "Expected " << #a << " to evaluate to true but found " \
                                       "otherwise" << endl \
                                       << "In file " << __FILE__ << " at line " << __LINE__ << endl

#undef EXPECT_FALSE
#define EXPECT_FALSE(a) \
    if (a) Test::test_result_ = false, \
                                    LOG << "Expected " << #a << " to evaluate to false but found " \
                                    "otherwise" << endl \
                                    << "In file " << __FILE__ << " at line " << __LINE__ << endl

#undef EXPECT_NOT_EQ
#define EXPECT_NOT_EQ(a,b) \
    if ((a) == (b)) Test::test_result_ = false, \
                                             LOG << "Expected different values but found otherwise" <<endl \
                                             << #a << " : '" << (a) << "'" << endl \
                                             << #b << " : '" << (b) << "'" << endl \
                                             << "In file " << __FILE__ << " at line " << __LINE__ << endl

#undef EXPECT_EQ
#define EXPECT_EQ(a,b) \
    if ((a) != (b)) Test::test_result_ = false, \
                                             LOG << "Expected equality but found otherwise" <<endl \
                                             << #a << " : '" << (a) << "'" << endl \
                                             << #b << " : '" << (b) << "'" << endl \
                                             << "In file " << __FILE__ << " at line " << __LINE__ << endl

#undef EXPECT_EQ_STR
#define EXPECT_EQ_STR(a,b) \
    if (strcmp((a),(b)) != 0) Test::test_result_ = false, \
                LOG << "Expected equality but found otherwise" <<endl \
                << "First string : '" << a << "'." << endl \
                << "Second string: '" << b << "'." << endl \
                << "In file " << __FILE__ << " at line " << __LINE__ << endl

#undef EXPECT_EQ_MEM
#define EXPECT_EQ_MEM(a,b,size) \
    if (memcmp((a),(b),(size)) != 0) Test::test_result_ = false, \
                LOG << "Expected equality but found otherwise" <<endl \
                << "First string : '" << Test::charPtrToString(a, size) << "'." << endl \
                << "Second string: '" << Test::charPtrToString(b, size) << "'." << endl \
                << "In file " << __FILE__ << " at line " << __LINE__ << endl

#undef RUN_TEST
#define RUN_TEST(method) \
    {LOG << "Running " << #method << endl; \
        Test::test_result_ = true; \
        Test::tests_run_ ++; \
        try { method; } \
        catch(...) { \
            Test::test_result_ = false;\
            LOG << "Test failed because of unknown exception!" << endl; \
        } \
        if (!Test::test_result_) { \
            LOG << "Test " << #method << " failed!" << endl;\
        } else { \
            LOG << "Test " << #method << " passed!" <<endl;\
            Test::tests_passed_ ++; \
        } \
        LOG << endl; }

#undef TEST_SUMMARY
#define TEST_SUMMARY() \
    LOG << "Run " << Test::tests_run_ << " tests" << endl; \
    LOG << Test::tests_passed_ << " tests passed!" << endl; \
    if (Test::tests_passed_ != Test::tests_run_) \
        return 1;

#endif  // TESTING_H__
