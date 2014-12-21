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

#ifndef RASCONTROL_X_SRC_RASCONTROLCONFIG_HH
#define RASCONTROL_X_SRC_RASCONTROLCONFIG_HH

#include <string>

#include <boost/cstdint.hpp>

#include "../../commline/cmlparser.hh"

namespace rascontrol
{

class RasControlConfig
{
public:
    enum WorkMode
    {
        WKMINTERACTIV, //Interactive mode
        WKMBATCH, //Batch mode
        WKMLOGIN, //Login mode
        WKMTESTLOGIN, //Test login mode
    };

    enum LoginMode
    {
        LGIINTERACTIV, //Interactive mode. Take the login credentials from the console.
        LGIENVIRONM //Environment login. Take the login credentials from the environment variable.
    };


    /**
     * @brief RasControlConfig Initialize a new instance of the RasControlConfig class.
     */
    RasControlConfig();

    /**
     * @brief ~RasControlConfig Destruct the instance of the RasControlConfig object.
     */
    virtual ~RasControlConfig();

    /**
     * @brief parseCommandLineParameters Parse the command line parameters and initialize this object.
     * @param argc Number of input arguments including the program name.
     * @param argv Array of commands.
     * @return TRUE if the parsing was successful, FALSE otherwise
     */
    bool parseCommandLineParameters(int argc, char** argv);

    boost::int32_t getWorkMode() const;

    boost::int32_t getLoginMode() const;

    std::string getRasMgrHost() const;

    bool isHistoryRequested() const;

    bool isQuietMode() const;

    bool isHelpRequested() const;

    std::string getHistoryFileName() const;

    std::string getPrompt(const std::string& userName = "USER");

    std::string getCommand() const;

    boost::uint32_t getRasMgrPort() const;

    void displayHelp() const;

    std::string getLogConfigFile() const;

private:
    /**
     * @brief The PromptMode enum Different prompt settings
     */
    enum PromptMode
    {
        PROMPTSING=1,
        PROMPTRASC=2,
        PROMPTFULL=3
    };

    std::string rasMgrHost;/*!< Name of the rasmgr host to which we want to connect.*/
    std::string logConfigFile;/*!< File used to configure logging*/
    boost::int32_t rasMgrPort;/*!< Port of the rasmgr instance to which we want to connect.*/
    boost::int32_t promptMode; /*!< Used to configure the prompt displayed to the user. */
    LoginMode loginMode;
    WorkMode workMode;
    bool isHistoryRequired; /*!<True if the history is required */
    std::string historyFileName; /*!<Name of the file in which to save the history.*/
    bool quiet; /*!<True if rascontrol should run in the quiet mode*/
    bool isHelpReq;/*!<True if the help is requested.*/
    std::string prompt;/*!<String represeting the prompt*/

    std::string command;

    //-- parameters of this program
    CommandLineParser    &cmlInter;
    CommandLineParameter &cmlHelp, &cmlHost, &cmlPort, &cmlLogin;
    CommandLineParameter &cmlHist, &cmlLogFile;
    CommandLineParameter &cmlPrompt, &cmlTestLogin;
    CommandLineParameter &cmlInteractive, &cmlQuiet, &cmlExecute;

    /**
     * @brief paramError Display error message in case of invalid parameters.
     * @return
     */
    bool paramError();
};
}

#endif // RASCONTROL_X_SRC_RASCONTROLCONFIG_HH
