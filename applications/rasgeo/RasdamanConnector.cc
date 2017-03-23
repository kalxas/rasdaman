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

#include "config.h"
#include "RasdamanConnector.hh"
#include <fstream>
#include <unistd.h>

const std::string RasdamanConnector::ctx = "RasdamanConnector::";

RasdamanConnector::RasdamanConnector()
{
    // this is the private unparameterised constructor
    // and is purposely not available to the user
}

RasdamanConnector::RasdamanConnector(int rasport, int pgport,
                                     std::string hostname1,
                                     std::string hostname2,
                                     std::string RasDbName, std::string PetaDbName,
                                     std::string RasDbuser, std::string RasDbPasswd,
                                     std::string RasUser, std::string RasPasswd,
                                     std::string PetaUser, std::string PetaPasswd) :
    m_iRasPort(rasport), m_iPgPort(pgport),
    m_sHostName(hostname1), m2_sHostName(hostname2), m_RasDbName(RasDbName),
    m_PetaDbName(PetaDbName), m_RasDbUser(RasDbuser),
    m_RasDbPasswd(RasDbPasswd), m_RasUser(RasUser),
    m_RasPasswd(RasPasswd), m_PetaUser(PetaUser),
    m_PetaPasswd(PetaPasswd)
{
    m_db.set_servername(hostname1.c_str(), rasport);
    m_db.set_useridentification(RasUser.c_str(), RasPasswd.c_str());

    this->m_petaconn = 0;
#ifdef BASEDB_PGSQL
    this->m_rasconn = 0;
#endif
}

RasdamanConnector::RasdamanConnector(std::string configfile)
{
    parseConfig(configfile);
    m_db.set_servername(this->m_sHostName.c_str(), this->m_iRasPort);
    m_db.set_useridentification(this->m_RasUser.c_str(), this->m_RasPasswd.c_str());

    this->m_petaconn = 0;
#ifdef BASEDB_PGSQL
    this->m_rasconn = 0;
#endif
}

RasdamanConnector::~RasdamanConnector()
{
    PQfinish(this->m_petaconn);
#ifdef BASEDB_PGSQL
    PQfinish(this->m_rasconn);
#endif

    if (m_db.get_status() == r_Database::not_open)
    {
        return;
    }
    m_db.close();
}

int RasdamanConnector::parseConfig(std::string configfile) throw (r_Error)
{
    // check filepath
    if (access(configfile.c_str(), R_OK) != 0)
    {
        throw r_Error(r_Error::r_Error_NameInvalid);
        return 0;
    }

    ifstream confinfo(configfile.c_str());
    if (!confinfo.good())
    {
        throw r_Error(r_Error::r_Error_NameInvalid);
        return 0;
    }


    // parse file content
    std::string linestr;
    std::string key;
    std::string value;
    std::string::size_type pos;

    while (getline(confinfo, linestr))
    {
        pos = linestr.find("=", 0);
        if (pos == std::string::npos)
        {
            continue;
        }

        key = removeWhiteSpaces(linestr.substr(0, pos));
        value = removeWhiteSpaces(linestr.substr(pos + 1, linestr.size() - 1));

        if (key == "host1")
        {
            this->m_sHostName = value;    
        }
        else if (key == "host2")
        {
            this->m2_sHostName = value;
        }
        else if (key == "rasport")
        {
            this->m_iRasPort = atoi(value.c_str());
        }
        else if (key == "pgport")
        {
            this->m_iPgPort = atoi(value.c_str());
        }
        else if (key == "rasdbname")
        {
            this->m_RasDbName = value;
        }
        else if (key == "petadbname")
        {
            this->m_PetaDbName = value;
        }
        else if (key == "rasuser")
        {
            this->m_RasDbUser = value;
        }
        else if (key == "raspassword")
        {
            this->m_RasDbPasswd = value;
        }
        else if (key == "rasloginuser")
        {
            this->m_RasUser = value;
        }
        else if (key == "petauser")
        {
            this->m_PetaUser = value;
        }
        else if (key == "rasloginpassword")
        {
            this->m_RasPasswd = value;
        }
        else if (key == "petapassword")
        {
            this->m_PetaPasswd = value;
        }
    }

    return 1;
}

std::string RasdamanConnector::removeWhiteSpaces(std::string str)
{
    std::string::size_type pos;
    bool found = true;
    while (found)
    {
        if (str.find(" ") != std::string::npos)
        {
            pos = str.find(" ");
            str.erase(pos, 1);
        }
        else if (str.find("\t") != std::string::npos)
        {
            pos = str.find("\t");
            str.erase(pos, 1);
        }
        else
        {
            found = false;
        }
    }

    return str;
}

void RasdamanConnector::connect()
{
    // connect to RASBASE the rasdaman way (i.e. via rasmgr)
    if (m_db.get_status() != r_Database::read_only &&
            m_db.get_status() != r_Database::read_write)
    {
        m_db.open(m_RasDbName.c_str());
    }

    // get a direct connection to the petascope data base, but, before
    // we do anything, check, whether there is already a connection alive
    if (PQstatus(this->m_petaconn) != CONNECTION_OK)
    {
        this->m_petaconn = PQconnectdb(this->getPetaPGConnectString().c_str());
        if (PQstatus(this->m_petaconn) != CONNECTION_OK)
        {
            std::cerr << ctx << "connect(): "
                      << "connection with '" << this->getPetaDbName() << "' failed!" << std::endl;
        }
    }

    // get a direct connection to the rasdaman data base (in PostgreSQL), but, before
    // we do anything, check, whether there is already a connection alive
#ifdef BASEDB_PGSQL
    if (PQstatus(this->m_rasconn) != CONNECTION_OK)
    {
        this->m_rasconn = PQconnectdb(this->getRasPGConnectString().c_str());
        if (PQstatus(this->m_rasconn) != CONNECTION_OK)
        {
            std::cerr << ctx << "connect(): "
                      << "connection with '" << this->getRasDbName() << "' failed!" << std::endl;
        }
    }
#endif
}

void RasdamanConnector::disconnect()
{
    if (m_db.get_status() != r_Database::not_open)
    {
        m_db.close();
    }

    PQfinish(this->m_petaconn);
#ifdef BASEDB_PGSQL
    PQfinish(this->m_rasconn);
#endif
}

const PGconn* RasdamanConnector::getPetaConnection()
{
    if (PQstatus(this->m_petaconn) != CONNECTION_OK)
    {
        return 0;
    }
    else
    {
        return this->m_petaconn;
    }
}

const PGconn* RasdamanConnector::getRasConnection()
{
#ifdef BASEDB_PGSQL
    if (PQstatus(this->m_rasconn) != CONNECTION_OK)
    {
        return 0;
    }
    else
    {
        return this->m_rasconn;
    }
#else
    // store rat table in petascopedb, rather than RASBASE in case SQLite backend is used
    return this->m_petaconn;
#endif
}

std::string RasdamanConnector::getPetaPGConnectString(void)
{
    std::stringstream connstr;
    connstr << "host=" << this->m2_sHostName <<
            " port=" << this->m_iPgPort <<
            " dbname=" << this->m_PetaDbName <<
            " user=" << this->m_PetaUser <<
            " password=" << this->m_PetaPasswd;

    return connstr.str();
}

std::string RasdamanConnector::getRasPGConnectString(void)
{
    std::stringstream connstr;
#ifdef BASEDB_PGSQL
    connstr << "host=" << this->m_sHostName <<
            " port=" << this->m_iPgPort <<
            " dbname=" << this->m_RasDbName <<
            " user=" << this->m_RasDbUser <<
            " password=" << this->m_RasDbPasswd;
#else
    // use petascopedb rather than RASBASE
    connstr << "host=" << this->m_sHostName <<
            " port=" << this->m_iPgPort <<
            " dbname=" << this->m_PetaDbName <<
            " user=" << this->m_PetaUser <<
            " password=" << this->m_PetaPasswd;
#endif

    return connstr.str();
}
