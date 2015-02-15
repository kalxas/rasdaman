#ifndef RASMGR_X_SRC_CONFIGURATION_HH
#define RASMGR_X_SRC_CONFIGURATION_HH

#include <string>

#include <boost/cstdint.hpp>


#include "../../commline/cmlparser.hh"

/// host/domain name size (See man gethostname)
#define HOSTNAME_SIZE   255
#define RASMGR_LOG_PREFIX "rasmgr"
#define MAXMSGOUTBUFF 20000
// status flags that rasmgr understands

namespace rasmgr
{

class Configuration
{
public:
    Configuration();

    virtual ~Configuration();

    bool parseCommandLineParameters(int argc, char** argv);

    void printHelp();

    boost::uint32_t getPort() const;

    std::string getHostName() const;

    std::string getName() const;

    bool isQuiet() const;

    std::string getLogConfigFile() const;

private:
    //interface program
    CommandLineParser    &cmlInter;
    CommandLineParameter &cmlHelp, &cmlHostName, &cmlPort, &cmlPollFrequ;
    CommandLineParameter &cmlName, &cmlQuiet, &cmlLog, &cmlLogConf;

    bool quiet;
    std::string name; /*!< symbolic name of this rasmgr  */
    std::string hostName;/*!< the advertized host name (master only, default: same as UNIX command 'hostname')" */
    boost::uint32_t port;/*!< Port number */
    std::string logConfigFile;

};
}

#endif // CONFIGURATION_HH
