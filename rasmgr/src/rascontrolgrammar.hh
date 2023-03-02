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

#ifndef RASMGR_RASCONTROL_HH
#define RASMGR_RASCONTROL_HH

#include "rascontrol.hh"

namespace rasmgr
{

/**
 * Parse a rascontrol command, e.g. `list srv -all`, and process it with a
 * RasControl instance.
 */
class RasControlGrammar
{
public:
    RasControlGrammar(std::shared_ptr<RasControl> rascontrol);

    // parse the given command; return true if successfull, false otherwise
    void parse(const std::string &command);

    // process the parsed command and return the response
    std::string processRequest();

    // return true if the first command token matches key, false otherwise
    bool isCommand(const std::string &key);
    bool isServerAdminCommand();
    bool isUserAdminCommand();
    bool isSystemConfigCommand();
    bool isInfoCommand();
    bool isLoginCommand();

private:
    std::string helloCommand();
    std::string loginCommand();
    std::string exitCommand();
    std::string helpCommand();
    std::string helpHelp();
    std::string exitHelp();
    std::string listCommand();
    std::string listRasServers();
    std::string listRasHosts();
    std::string listDBHosts();
    std::string listDatabases();
    std::string listUsers();
    std::string listVersion();
    std::string listHelp();
    std::string listInPeers();
    std::string listOutPeers();
    std::string defineCommand();
    std::string defineRasServers();
    std::string defineRasHosts();
    std::string defineDBHosts();
    std::string defineDatabases();
    std::string defineUsers();
    std::string defineHelp();
    std::string defineInPeers();
    std::string defineOutPeers();
    std::string removeCommand();
    std::string removeRasServers();
    std::string removeRasHosts();
    std::string removeDBHosts();
    std::string removeDatabases();
    std::string removeUsers();
    std::string removeHelp();
    std::string removeInPeers();
    std::string removeOutPeers();
    std::string upCommand();
    std::string upRasServers();
    std::string upHelp();
    std::string downCommand();
    std::string downRasServers();
    std::string downRasHosts();
    std::string downHelp();
    std::string changeCommand();
    std::string changeHost();
    std::string changeUserCmd();
    std::string changeRasServer();
    std::string changeDBHost();
    std::string changeDB();
    std::string changeHelp();
    std::string saveCommand();
    std::string saveHelp();

    std::string error(const std::string &);

    bool isFlag(const std::string &, int pos = -1);

    std::string getValueOf(const std::string &,
                           bool acceptMinus = false);  //'-' alone, only void right string
    std::string getValueOptionalFlag(const std::string &,
                                     bool acceptMinus = false);
    std::string getValueMandatoryFlag(const std::string &,
                                      bool acceptMinus = false);
    unsigned long convertToULong(const std::string &stringValue,
                                 const std::string &errMsg);

    // Get all the parameters following key as a concatenated string (with
    // empty spaces), e.g:
    // key = -xp, command = ... -xp -arg1 value1 -arg2 value2
    // result: "-arg1 value1 -arg2 value2"
    //
    // If key is not found, then return ""
    std::string getExtraParams(const std::string &key);

    std::string empty;
    std::vector<std::string> tokens;
    int argc;
    std::shared_ptr<RasControl> rascontrol;

    DefineUser defUser;
    RemoveUser remUser;
    ChangeUser changeUser;
    ListUser listUser;

    DefineDbHost defDbHost;
    ChangeDbHost changeDbHost;
    RemoveDbHost removeDbHost;

    StartServerGroup upSrv;
    StopServerGroup downSrv;

    DefineDb defDb;
    ChangeDb changeDb;
    RemoveDb removeDb;
    ListDb listDb;

    DefineServerGroup defServerGroup;
    ChangeServerGroup changeServerGroup;
    ListServerGroup listServerGroup;

    DefineOutpeer defOutpeer;

public:
    // Define literal strings used by the rules
    static const std::string defineLit;
    static const std::string hostLit;
    static const std::string srvLit;
    static const std::string _portLit;
    static const std::string _networkLit;
    static const std::string _typeLit;
    static const std::string _dbhLit;
    static const std::string dbhLit;
    static const std::string _connectLit;
    static const std::string _userLit;
    static const std::string _passwdLit;
    static const std::string dbLit;
    static const std::string userLit;
    static const std::string inpeerLit;
    static const std::string outpeerLit;
    static const std::string _nameLit;
    static const std::string changeLit;
    static const std::string removeLit;
    static const std::string listLit;
    static const std::string _allLit;
    static const std::string _useLocalHostLit;
    static const std::string onLit;
    static const std::string offLit;
    static const std::string _autorestartLit;
    static const std::string _xpLit;
    static const std::string _countdownLit;
    static const std::string _hostLit;
    static const std::string _pLit;
    static const std::string _rightsLit;
    static const std::string _aliveLit;
    static const std::string _availableLit;
    static const std::string _idleLit;
    static const std::string _sizeLit;
    static const std::string saveLit;
    static const std::string exitLit;
    static const std::string versionLit;
    static const std::string helpLit;
    static const std::string helloLit;
    static const std::string byeLit;
    static const std::string quitLit;
    static const std::string loginLit;
    static const std::string upLit;
    static const std::string downLit;
    static const std::string _forceLit;
    static const std::string _killLit;
    static const std::string anyLit;

    static const std::string commentPrefix;
};

// case-insensitive string comparison.
bool strieq(const std::string &left, const std::string &right);

//
// Exceptions
//

class RCError
{
public:
    RCError();
    explicit RCError(const std::string &what);
    virtual std::string getString();

private:
    std::string what;
};

class RCErrorUnexpToken : public RCError
{
public:
    explicit RCErrorUnexpToken(const std::string &);
    std::string getString() override;

private:
    std::string pcc;
};

class RCErrorNoPermission : public RCError
{
public:
    RCErrorNoPermission();
    std::string getString() override;

private:
};

class RCErrorInvalidName : public RCError
{
public:
    explicit RCErrorInvalidName(const std::string &);
    std::string getString() override;

private:
    std::string pcc;
};

class RCErrorMissingParam : public RCError
{
public:
    explicit RCErrorMissingParam(const std::string &);
    std::string getString() override;

private:
    std::string pcc;
};

class RCErrorIncorNumberValue : public RCError
{
public:
    explicit RCErrorIncorNumberValue(const std::string &);
    std::string getString() override;

private:
    std::string pcc;
};

}  // namespace rasmgr

#endif
