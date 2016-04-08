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
 * Copyright 2003 - 2011 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/*
 * Contributed to rasdaman by Alexander Herzig, Landcare Research New Zealand
 */

/*!
 * \brief    Connector class for the rasdaman array data base.
 * \see      RasdamanHelper2
 *
 * The RasdamanConnector class is designed to work in conjunction with the
 * the RasdamanHelper2 class to facilitate data base connection and
 * transaction handling with regard to the rasdaman and petascope data bases.
 *
 */

#ifndef RASDAMANCONNECTOR_HH_
#define RASDAMANCONNECTOR_HH_

#include <string>
#include <iostream>
#include "rasdaman.hh"
#include "raslib/error.hh"

// postgresql
#include "libpq-fe.h"

class RasdamanConnector
{
public:

    /*! This constructor accepts individual connection parameters.

     *  \param rasport  rasmgr port (e.g. 7001)
     *  \param pgport   postgres server port (e.g. 5432)
     *  \param hostname1 hostname1 (e.g. localhost) for RASBASE only
     *  \param hostname2 hostname2 (e.g. localhost) for petascopedb only
     *  \param RasDbName rasdaman data base name (e.g. RASBASE)
     *  \param PetaDbName petascope data base name
     *  \param RasDbUser rasdaman data base user (e.g. PostgreSQL user)
     *  \param RasDbPasswd rasdaman data base user's password
     *  \param RasUser rasdaman user (rasmgr login)
     *  \param RasPasswd password of rasdaman (rasmgr) user
     *  \param PetaUser petascope data base user
     *  \param PetaPasswd password of petascope data base user
     */

    RasdamanConnector(int rasport, int pgport,
                      std::string hostname1,
		      std::string hostname2,
                      std::string RasDbName, std::string PetaDbName,
                      std::string RasDbuser, std::string RasDbPasswd,
                      std::string RasUser, std::string RasPasswd,
                      std::string PetaUser, std::string PetaPasswd);

    /*! This constructor allows parsing a configuration file, which
     *  contains the required connection parameters. The configuration
     *  file is expected to have the following structure (sample file): \newline \newline
     *
     *    rasport=7001 \newline
     *    pgport=5432    \newline
     *    rasdbname=RASBASE \ newline
     *    petadbname=petascopedb \newline
     *    rasdbuser=rasdaman \newline
     *    rasdbpasswd=rasdaman \newline
     *    rasuser=rasadmin    \newline
     *    raspassword=rasadmin    \newline
     *    petauser=rasdaman    \newline
     *    petapassword=rasdaman
     *
     *  \param configfile filename of the configuration file
     *
     */
    RasdamanConnector(std::string configfile);
    virtual ~RasdamanConnector();

    /*! Establishes a connection to the rasdaman and petascope data base(s). */
    void connect();
    /*! Closes the connection to the rasdaman and petascope data base(s). */
    void disconnect();

    /*! Yields access to the rasdaman data base object*/
    const r_Database& getDatabase()
    {
        return this->m_db;
    };
    /*! Returns the current rasdaman data base status. */
    r_Database::access_status getStatus()
    {
        return this->m_db.get_status();
    };
    /*! Returns the port on which rasdaman tries to connect to the postgres server.*/
    int getPgPort()
    {
        return this->m_iPgPort;
    };
    /*! Returns the rasmgr port */
    int getRasPort()
    {
        return this->m_iRasPort;
    };

    /*! get configured connection details for host1 */
    std::string getHostName()
    {
        return this->m_sHostName;
    };
    /*! get configured connection details for host2 */
    std::string getHostName2()
    {
        return this->m2_sHostName;
    };
    /*! get configured connection details */
    std::string getRasDbName()
    {
        return this->m_RasDbName;
    };
    /*! get configured connection details */
    std::string getPetaDbName()
    {
        return this->m_PetaDbName;
    };
    /*! get configured connection details */
    std::string getRasDbUser()
    {
        return this->m_RasDbUser;
    };
    /*! get configured connection details */
    std::string getRasUser()
    {
        return this->m_RasUser;
    };
    /*! get configured connection details */
    std::string getPetaUser()
    {
        return this->m_PetaUser;
    };


    /*! get a connection string for the petascope data base which can be
     *  used with PQconnectdb()  */
    std::string getPetaPGConnectString(void);
    /*! get a connection pointer to the petascope data base */
    const PGconn* getPetaConnection(void);

    /*! get a connection string for the rasdaman data base which can be
     *  used with PQconnectdb()  */
    std::string getRasPGConnectString(void);
    /*! get a connection pointer to the rasdaman data base */
    const PGconn* getRasConnection(void);

protected:
    /*! purposely non-available constructor */
    RasdamanConnector();

    /*! Parses the configuration file and sets member variables. If anything
     * goes wrong it throws an r_Error::r_Error_NameInvalid error. */
    int parseConfig(std::string configfile) throw (r_Error);

    /*! removes whitespaces and tabs from a given string */
    std::string removeWhiteSpaces(std::string str);

    /*! rasdaman port */
    int m_iRasPort;
    /*! Postgres port */
    int m_iPgPort;
    /*! hostname1 */
    std::string m_sHostName;
    /*! hostname2 */
    std::string m2_sHostName;
    /*! rasdaman data base name */
    std::string m_RasDbName;
    /*! petascope data base name */
    std::string m_PetaDbName;
    /*! rasdaman data base user */
    std::string m_RasDbUser;
    /*! password of rasdaman data base user */
    std::string m_RasDbPasswd;
    /*! rasmgr user */
    std::string m_RasUser;
    /*! rasmgr user's password */
    std::string m_RasPasswd;
    /*! petascope data base user */
    std::string m_PetaUser;
    /*! password of petascope data base user */
    std::string m_PetaPasswd;
    /*! rasdaman data base object */
    r_Database m_db;
    /*! pointer to a petascope data base connection */
    PGconn* m_petaconn;
    /*! pointer to a rasdaman data base connection */
#ifdef BASEDB_PGSQL
    PGconn* m_rasconn;
#endif

private:
    /*! string constant defining class context for debug output */
    static const std::string ctx;
};

#endif /* RASDAMANCONNECTOR_HH_ */
