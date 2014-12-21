/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <boost/scoped_ptr.hpp>

#include "common/src/logging/easylogging++.hh"
#include "common/src/unittest/gtest.h"

#include "globals.hh"

#include "rascontrol_x/src/rascontrolconfig.hh"

using rascontrol::RasControlConfig;

///TODO: For some reason, these tests don't work
/// My best guess is that the Cmlparser keeps some global variables.

//rascontrol [-h|--help] [--host h] [--port n] [--prompt n]
//[--quiet]
//[--login|--interactive|--execute cmd|--testlogin]



//TEST_F(RasControlConfigTest, parseCLPDefault)
//{
//    char** argv;
//    int argc;
//    std::vector<std::string> arguments;

//    arguments.push_back("rascontrol");

//    argc = arguments.size();
//    argv = new char*[argc];

//    for(int i=0; i<argc; i++)
//    {
//        argv[i]=new char[arguments[i].length()+1];
//        strcpy(argv[i], arguments[i].c_str());
//    }

//    bool result = config->parseCommandLineParameters(arguments.size(), argv);

//    ASSERT_TRUE(result);

//    ASSERT_EQ( rascontrol::RasControlConfig::LGIINTERACTIV,config->getLoginMode());
//    ASSERT_EQ( rascontrol::RasControlConfig::WKMINTERACTIV,config->getWorkMode());
//    ASSERT_FALSE(config->isHistoryRequested());
//    ASSERT_FALSE(config->isQuiet());
//    ASSERT_FALSE(config->isHelpRequested());

//    for(int i=0; i<arguments.size(); i++)
//    {
//        delete[] argv[i];
//    }

//    delete[] argv;

//}



//TEST(RasControlConfigTest, parseCLPHelpLong)
//{
//    RasControlConfig config;

//    char** argv;
//    std::vector<std::string> arguments;

//    arguments.push_back("rascontrol");
//    arguments.push_back("--help");

//    argv = new char*[arguments.size()];

//    for(int i=0; i<arguments.size(); i++)
//    {
//        argv[i]=new char[arguments[i].length()+1];
//        strcpy(argv[i], arguments[i].c_str());
//    }

//    bool result = config.parseCommandLineParameters(arguments.size(), argv);

//    ASSERT_TRUE(result);

//    ASSERT_TRUE(config.isHelpRequested());

//    for(int i=0; i<arguments.size(); i++)
//    {
//        delete[] argv[i];
//    }

//    delete[] argv;
//}

//TEST(RasControlConfigTest, parseCLPHelpShort)
//{
//    RasControlConfig config;

//    char** argv;
//    std::vector<std::string> arguments;

//    arguments.push_back("rascontrol");
//    arguments.push_back("-h");

//    argv = new char*[arguments.size()];

//    for(int i=0; i<arguments.size(); i++)
//    {
//        argv[i]=new char[arguments[i].length()+1];
//        strcpy(argv[i], arguments[i].c_str());
//    }

//    bool result = config.parseCommandLineParameters(arguments.size(), argv);

//    ASSERT_TRUE(result);

//    ASSERT_TRUE(config.isHelpRequested());

//    for(int i=0; i<arguments.size(); i++)
//    {
//        delete[] argv[i];
//    }

//    delete[] argv;
//}

//TEST_F(RasControlConfigTest, parseCLPHost)
//{
//    char** cmlArguments;
//    std::vector<std::string> arguments;

//    arguments.push_back("rascontrol");
//    arguments.push_back("--host");
//    arguments.push_back("hostName");

//    cmlArguments = new char*[arguments.size()];

//    for(int i=0; i<arguments.size(); i++)
//    {
//        cmlArguments[i]=new char[strlen(arguments[i].c_str())];
//        strcpy(cmlArguments[i], arguments[i].c_str());
//    }

//    bool result = config->parseCommandLineParameters(arguments.size()+1, cmlArguments);

//    ASSERT_TRUE(result);

////    ASSERT_EQ("hostName",config->getRasMgrHost());


//    for(int i=0; i<arguments.size(); i++)
//    {
//        delete[] cmlArguments[i];
//    }

//    delete[] cmlArguments;
//}
