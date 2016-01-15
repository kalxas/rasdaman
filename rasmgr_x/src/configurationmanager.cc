#include <limits.h> //PATH_MAX

#include <cstdio>
#include <stdexcept>

#include "../../config.h"
#include "../../include/globals.hh"
#include <easylogging++.h>
#include "../../common/src/exceptions/missingresourceexception.hh"

#include "constants.hh"
#include "controlcommandexecutor.hh"
#include "usermanager.hh"

#include "configurationmanager.hh"

namespace rasmgr
{
using std::runtime_error;

ConfigurationManager::ConfigurationManager(boost::shared_ptr<ControlCommandExecutor> commandExecutor,
        boost::shared_ptr<UserManager> userManager)
{
    this->commandExecutor = commandExecutor;
    this->userManager = userManager;
}

ConfigurationManager::~ConfigurationManager()
{}

void ConfigurationManager::saveConfiguration()
{
    this->userManager->saveUserInformation();
}

void ConfigurationManager::loadConfiguration()
{
    //Will not throw an exception
    this->userManager->loadUserInformation();

    try
    {
        this->loadRasMgrConf();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch(...)
    {
        LERROR<<"Failed to load"<<RASMGR_CONF_FILE;
    }
}

void ConfigurationManager::loadRasMgrConf()
{
    char configFileName[PATH_MAX];
    char inBuffer[MAX_CONTROL_COMMAND_LENGTH];
    configFileName[0]=0;

    if ( strlen ( CONFDIR ) +strlen ( RASMGR_CONF_FILE ) + 2 > PATH_MAX )
    {
        throw runtime_error("The path to the configuration file is longer than the maximum file system path.");
    }

    sprintf ( configFileName, "%s/%s", CONFDIR, RASMGR_CONF_FILE );

    LDEBUG<<"Opening rasmanager configuration file:"<<std::string ( configFileName );

    std::ifstream ifs ( configFileName );   // open config file

    if ( !ifs )
    {
        throw common::MissingResourceException(std::string ( configFileName ));
    }
    else
    {
        while ( !ifs.eof() )
        {
            ifs.getline ( inBuffer , MAX_CONTROL_COMMAND_LENGTH );

            std::string result = commandExecutor->sudoExecuteCommand ( std::string ( inBuffer ) );

            //Only error messages are non-empty
            if ( !result.empty() )
            {
                LERROR<<result;
            }
        }
    }
}

}
