#ifndef RASMGR_X_SRC_CONFIGURATION_HH
#define RASMGR_X_SRC_CONFIGURATION_HH

#include <string>

#include <boost/cstdint.hpp>


#include "../../commline/cmlparser.hh"

namespace rasmgr
{
/**
 * @brief The Configuration class Configuration object used to initialize rasmgr
 * from the command line.
 */
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

    std::string getLogFile() const;

private:
    static const boost::uint32_t HOSTNAME_SIZE;
    static const std::string RASMGR_LOG_PREFIX;
    static const boost::uint32_t MAXMSGOUTBUFF;

    //interface program
    CommandLineParser    &cmlInter;
    CommandLineParameter &cmlHelp, &cmlHostName, &cmlPort;
    CommandLineParameter &cmlName, &cmlQuiet, &cmlLog;

    bool quiet;
    std::string name; /*!< symbolic name of this rasmgr  */
    std::string hostName;/*!< the advertized host name (master only, default: same as UNIX command 'hostname')" */
    boost::uint32_t port;/*!< Port number */
    std::string logFile;/*!< The file to which to output the log */

};
}

#endif // CONFIGURATION_HH
