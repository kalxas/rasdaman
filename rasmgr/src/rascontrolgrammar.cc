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

using namespace std;

#include "globals.hh"
#include "rascontrolgrammar.hh"
#include <logging.hh>

namespace rasmgr
{

RasControlGrammar::RasControlGrammar(
    boost::shared_ptr<RasControl> rascontrolArg) :
    rascontrol{rascontrolArg}
{}

void RasControlGrammar::parse(const std::string &reqMessage)
{
    LDEBUG << "Parsing command '" << reqMessage << "'...";
    this->tokens.clear();
    char *token = strdup(reqMessage.c_str());
    token = strtok(token, " \r\n\t\0");
    while (token)
    {
        if (token[0] == '#')
        {
            break;    // done, disregard comment til end of line
        }
        this->tokens.emplace_back(token);
        token = strtok(NULL, " \r\n\t\0");
    }
    LDEBUG << "Done, " << tokens.size() << " tokens parsed.";
    free(token);
}

std::string RasControlGrammar::processRequest()
{
    auto command = tokens.size() ? tokens[0] : commentPrefix;
    try
    {
        LDEBUG << "Processing rascontrol command: '" << command << "'...";

        if (isCommand(helloLit))
        {
            return helloCommand();
        }
        else if (isCommand(loginLit))
        {
            return loginCommand();
        }
        else if (isCommand(helpLit))
        {
            return helpCommand();
        }
        else if (isCommand(listLit))
        {
            return listCommand();
        }
        else if (isCommand(defineLit))
        {
            return defineCommand();
        }
        else if (isCommand(removeLit))
        {
            return removeCommand();
        }
        else if (isCommand(upLit))
        {
            return upCommand();
        }
        else if (isCommand(downLit))
        {
            return downCommand();
        }
        else if (isCommand(changeLit))
        {
            return changeCommand();
        }
        else if (isCommand(saveLit))
        {
            return saveCommand();
        }
        else if (isCommand(exitLit))
        {
            return exitCommand();
        }
        else if (isCommand(commentPrefix))
        {
            return empty;
        }
        else
        {
            LERROR << "Invalid command '" << command << "'; try HELP.";
            return error("Invalid command '" + command + "'; try HELP.");
        }
    }
    catch (RCError &e)
    {
        return error(e.getString());
    }
}

bool RasControlGrammar::isCommand(const std::string &key)
{
    return tokens.empty() ? commentPrefix == key : strieq(tokens[0], key);
}

bool RasControlGrammar::isServerAdminCommand()
{
    return isCommand(upLit) || isCommand(downLit);
}

bool RasControlGrammar::isUserAdminCommand()
{
    bool userChangeCommand = isCommand(defineLit) || isCommand(changeLit) ||
                             isCommand(removeLit) || isCommand(listLit);
    return userChangeCommand && tokens.size() > 1 && strieq(tokens[1], userLit);
}
bool RasControlGrammar::isSystemConfigCommand()
{
    return isCommand(defineLit) || isCommand(changeLit) ||
           isCommand(removeLit) || isCommand(saveLit);
}

bool RasControlGrammar::isInfoCommand()
{
    return isCommand(listLit);
}

bool RasControlGrammar::isLoginCommand()
{
    return isCommand(loginLit);
}

// -----------------------------------------------------------------------------

std::string RasControlGrammar::helloCommand()
{
    return helloLit;
}

std::string RasControlGrammar::loginCommand()
{
    return rascontrol->login();
}

std::string RasControlGrammar::exitCommand()
{
    return rascontrol->exit();
}

std::string RasControlGrammar::listCommand()
{
    const auto &what = tokens.size() == 1 ? empty : tokens[1];

    if (strieq(what, srvLit))
    {
        return listRasServers();
    }
    else if (strieq(what, versionLit))
    {
        return listVersion();
    }
    else if (strieq(what, inpeerLit))
    {
        return listInPeers();
    }
    else if (strieq(what, outpeerLit))
    {
        return listOutPeers();
    }
    else if (strieq(what, userLit))
    {
        return listUsers();
    }
    else if (strieq(what, hostLit))
    {
        return listRasHosts();
    }
    else if (strieq(what, dbhLit))
    {
        return listDBHosts();
    }
    else if (strieq(what, dbLit))
    {
        return listDatabases();
    }
    else if (strieq(what, helpLit))
    {
        return listHelp();
    }
    else
    {
        return error("Invalid LIST command '" + what + "'.");
    }
}

//list <<version>>
std::string RasControlGrammar::listVersion()
{
    return rascontrol->listVersion();
}

//list <<users>>
std::string RasControlGrammar::listUsers()
{
    listUser.Clear();
    if (isFlag(_rightsLit))
    {
        listUser.set_diplay_rights(isFlag(_rightsLit));
    }
    return rascontrol->listUser(listUser);
}

//list <<inpeer>>
std::string RasControlGrammar::listInPeers()
{
    return rascontrol->listInpeer();
}

//list <<outpeer>>
std::string RasControlGrammar::listOutPeers()
{
    return rascontrol->listOutpeer();
}

//list <<host>>
std::string RasControlGrammar::listRasHosts()
{

    return rascontrol->deprecatedCommand();
}

//list <<dbh>>
std::string RasControlGrammar::listDBHosts()
{
    return rascontrol->listDbHost();
}

//list <<srv [ s | -host h | -all ] [-p]>>
std::string RasControlGrammar::listRasServers()
{
    listServerGroup.Clear();

    auto srvName = getValueOf(srvLit);
    if (!srvName.empty())
    {
        listServerGroup.set_group_name(srvName);
    }
    else
    {
        auto hostName = getValueOptionalFlag(_hostLit);
        if (!hostName.empty())
        {
            listServerGroup.set_host(hostName);
        }
    }

    if (isFlag(_pLit))
    {
        listServerGroup.set_extra_info(true);
    }

    return rascontrol->listServerGroup(listServerGroup);
}

//list db [ d | -dbh h | -all ]
std::string RasControlGrammar::listDatabases()
{
    listDb.Clear();

    auto dbName = getValueOf(dbLit);
    if (!dbName.empty())
    {
        listDb.set_db_name(dbName);
    }
    else
    {
        auto dbh = getValueOptionalFlag(_dbhLit);
        if (!dbh.empty())
        {
            listDb.set_dbh_name(dbh);
        }
    }

    return rascontrol->listDb(listDb);
}

//
//*****************************************************************************
//

std::string RasControlGrammar::defineCommand()
{
    const auto &what = tokens.size() == 1 ? empty : tokens[1];

    if (strieq(what, srvLit))
    {
        return defineRasServers();
    }
    else if (strieq(what, hostLit))
    {
        return defineRasHosts();
    }
    else if (strieq(what, dbhLit))
    {
        return defineDBHosts();
    }
    else if (strieq(what, dbLit))
    {
        return defineDatabases();
    }
    else if (strieq(what, userLit))
    {
        return defineUsers();
    }
    else if (strieq(what, helpLit))
    {
        return defineHelp();
    }
    else if (strieq(what, inpeerLit))
    {
        return defineInPeers();
    }
    else if (strieq(what, outpeerLit))
    {
        return defineOutPeers();
    }
    else
    {
        return error("Invalid DEFINE command '" + what + "'.");
    }
}

//define <<user u [-passwd p] [-rights r]>>
std::string RasControlGrammar::defineUsers()
{
    defUser.Clear();

    const auto &userName = getValueMandatoryFlag(userLit);
    for (size_t i = 0; i < userName.size(); i++)
    {
        auto c = userName[i];
        if (c == ':' || c == '\'' || c == '"')
            return error("Invalid character ('" + string{c} + "') in user name.");
    }
    defUser.set_user_name(userName);

    const auto &plainPass = getValueOptionalFlag(_passwdLit);
    if (!plainPass.empty())
    {
        defUser.set_passwd(plainPass);
    }

    const auto &rights = getValueOptionalFlag(_rightsLit, true);
    if (rights == "-")
    {
        defUser.set_config_rights(false);
        defUser.set_access_rights(false);
        defUser.set_server_admin_rights(false);
        defUser.set_info_rights(false);
        defUser.set_dbread_rights(false);
        defUser.set_dbwrite_rights(false);
    }
    else if (!rights.empty())
    {
        LINFO << "rights: '" << rights << "'";
        for (size_t i = 0; i < rights.size(); i++)
        {
            switch (rights[i])
            {
            case 'C':
                defUser.set_config_rights(true);
                break;
            case 'A':
                defUser.set_access_rights(true);
                break;
            case 'S':
                defUser.set_server_admin_rights(true);
                break;
            case 'I':
                defUser.set_info_rights(true);
                break;
            case 'R':
                defUser.set_dbread_rights(true);
                break;
            case 'W':
                defUser.set_dbwrite_rights(true);
                break;
            default:
                return error("Invalid character '" + string{rights[i]} +
                             "' in -rights parameter.");
            }
        }
    }

    return rascontrol->defineUser(defUser);
}

//define << dbh h -connect c [-user u -passwd p] >>
std::string RasControlGrammar::defineDBHosts()
{
    defDbHost.Clear();

    const auto &dbhName = getValueMandatoryFlag(dbhLit);
    defDbHost.set_host_name(dbhName);
    const auto &connStr = getValueMandatoryFlag(_connectLit, true);
    defDbHost.set_connect(connStr);

    const auto &userStr = getValueOf(_userLit, true);
    if (!userStr.empty())
    {
        defDbHost.set_user(userStr);
    }
    const auto &passwdStr = getValueOf(_passwdLit, true);
    if (!passwdStr.empty())
    {
        defDbHost.set_passwd(passwdStr);
    }

    return rascontrol->defineDbHost(defDbHost);
}

//define <<db d –dbh db>>
std::string RasControlGrammar::defineDatabases()
{
    defDb.Clear();

    const auto &dbName = getValueMandatoryFlag(dbLit);
    defDb.set_db_name(dbName);
    const auto &dbhName = getValueMandatoryFlag(_dbhLit);
    defDb.set_dbhost_name(dbhName);

    return rascontrol->defineDb(defDb);
}

//define <<host h -net n [-port p]>>
std::string RasControlGrammar::defineRasHosts()
{
//    const auto &hostName = getValueMandatoryFlag(hostLit);
//    const auto &netName = getValueMandatoryFlag("-net");
//    const auto &portStr = getValueOptionalFlag(_portLit);

    return rascontrol->deprecatedCommand();
}

// define srv 'srvname' -host 'hostname' -dbh 'dbhname' -port 'portnumber'
//            [-autorestart on|off] [-countdown 'number'] [-xp 'options']
//            [-size 'group_size'] [-alive 'min_alive'] [-available 'min_available']
//            [-idle 'max_idle']
std::string RasControlGrammar::defineRasServers()
{
    defServerGroup.Clear();

    const auto &serverName = getValueMandatoryFlag(srvLit);
    defServerGroup.set_group_name(serverName);

    const auto &hostName = getValueMandatoryFlag(_hostLit);
    defServerGroup.set_host(hostName);

    const auto &dbhName = getValueMandatoryFlag(_dbhLit);
    defServerGroup.set_db_host(dbhName);

    // TODO: port parsing needs to be more sophisticated
    const auto &portStr = getValueMandatoryFlag(_portLit);
    auto listenPort = convertToULong(portStr, "port");
    defServerGroup.add_ports(listenPort);

    const auto &autoRestart = getValueOptionalFlag(_autorestartLit);
    if (!autoRestart.empty())
    {
        if (strieq(autoRestart, onLit))
        {
            changeServerGroup.set_n_autorestart(true);
        }
        else if (strieq(autoRestart, offLit))
        {
            changeServerGroup.set_n_autorestart(false);
        }
        else
            return error("Invalid autorestart option '" +
                         autoRestart + "', use one of [on|off].");
    }
    else
    {
        changeServerGroup.set_n_autorestart(true); // default
    }

    const auto &count = getValueOptionalFlag(_countdownLit);
    if (!count.empty())
    {
        defServerGroup.set_countdown(convertToULong(count, "countdown"));
    }
    const auto &size = getValueOptionalFlag(_sizeLit);
    if (!size.empty())
    {
        defServerGroup.set_group_size(convertToULong(size, "size"));
    }
    const auto &alive = getValueOptionalFlag(_aliveLit);
    if (!alive.empty())
    {
        defServerGroup.set_min_alive_servers(convertToULong(alive, "alive"));
    }
    const auto &available = getValueOptionalFlag(_availableLit);
    if (!available.empty())
    {
        defServerGroup.set_min_available_servers(convertToULong(available, "alive"));
    }
    const auto &idle = getValueOptionalFlag(_idleLit);
    if (!idle.empty())
    {
        defServerGroup.set_max_idle_servers(convertToULong(idle, "idle"));
    }
    const auto &xp = getExtraParams(_xpLit);
    if (!xp.empty())
    {
        defServerGroup.set_options(xp);
    }

    return rascontrol->defineServerGroup(defServerGroup);
}

//define <<inpeer hostname>>
std::string RasControlGrammar::defineInPeers()
{
    return rascontrol->defineInpeer(getValueMandatoryFlag(inpeerLit));
}

//define <<outpeer hostname [-port portnumber]>>
std::string RasControlGrammar::defineOutPeers()
{
    defOutpeer.Clear();

    const auto &hostName = getValueMandatoryFlag(outpeerLit);
    defOutpeer.set_host_name(hostName);

    const auto &portStr = getValueOptionalFlag(_portLit);
    unsigned long listenPort = DEFAULT_PORT;
    if (!portStr.empty())
    {
        listenPort = convertToULong(portStr, "port");
    }
    defOutpeer.set_port(listenPort);

    return rascontrol->defineOutpeer(defOutpeer);
}

//----------------------------------------

std::string RasControlGrammar::removeCommand()
{
    const auto &what = tokens.size() == 1 ? empty : tokens[1];

    if (strieq(what, srvLit))
    {
        return removeRasServers();
    }
    else if (strieq(what, hostLit))
    {
        return removeRasHosts();
    }
    else if (strieq(what, dbhLit))
    {
        return removeDBHosts();
    }
    else if (strieq(what, dbLit))
    {
        return removeDatabases();
    }
    else if (strieq(what, inpeerLit))
    {
        return removeInPeers();
    }
    else if (strieq(what, outpeerLit))
    {
        return removeOutPeers();
    }
    else if (strieq(what, userLit))
    {
        return removeUsers();
    }
    else if (strieq(what, helpLit))
    {
        return removeHelp();
    }
    else
    {
        return error("Invalid REMOVE command '" + what + "'.");
    }
}

// remove user 'username'
std::string RasControlGrammar::removeUsers()
{
    remUser.Clear();
    remUser.set_user_name(getValueMandatoryFlag(userLit));
    return rascontrol->removeUser(remUser);
}

std::string RasControlGrammar::removeRasHosts()
{
    return rascontrol->deprecatedCommand();
}

//remove <<srv s>>
std::string RasControlGrammar::removeRasServers()
{
    return rascontrol->removeServerGroup(getValueMandatoryFlag(srvLit));
}

//remove <<inpeer hostname>>
std::string RasControlGrammar::removeInPeers()
{
    return rascontrol->removeInpeer(getValueMandatoryFlag(inpeerLit));
}

//remove <<outpeer hostname>>
std::string RasControlGrammar::removeOutPeers()
{
    return rascontrol->removeOutpeer(getValueMandatoryFlag(outpeerLit));
}

//remove <<dbh h>>
std::string RasControlGrammar::removeDBHosts()
{
    removeDbHost.Clear();
    removeDbHost.set_host_name(getValueMandatoryFlag(dbhLit));
    return rascontrol->removeDbHost(removeDbHost);
}

//remove <<db d –dbh db>>
std::string RasControlGrammar::removeDatabases()
{
    removeDb.Clear();
    removeDb.set_db_name(getValueMandatoryFlag(dbLit));
    removeDb.set_dbhost_name(getValueMandatoryFlag(_dbhLit));
    return rascontrol->removeDb(removeDb);
}

//--------------------------------------------

std::string RasControlGrammar::changeCommand()
{
    const auto &what = tokens.size() == 1 ? empty : tokens[1];

    if (strieq(what, userLit))
    {
        return changeUserCmd();
    }
    else if (strieq(what, srvLit))
    {
        return changeRasServer();
    }
    else if (strieq(what, dbhLit))
    {
        return changeDBHost();
    }
    else if (strieq(what, dbLit))
    {
        return changeDB();
    }
    else if (strieq(what, hostLit))
    {
        return changeHost();
    }
    else if (strieq(what, helpLit))
    {
        return changeHelp();
    }
    else
    {
        return error("Invalid CHANGE command '" + what + "'.");
    }
}

//change <<host h [-name n] [-net x] [-port p] [-uselocalhost [on|off] ] >>
std::string RasControlGrammar::changeHost()
{
    return rascontrol->deprecatedCommand();
}

//change << dbh h [-name n] [-connect c] [-user u -passwd p]>>
std::string RasControlGrammar::changeDBHost()
{
    const auto &connString = getValueOptionalFlag(_connectLit);

    changeDbHost.Clear();

    changeDbHost.set_host_name(getValueMandatoryFlag(dbhLit));
    const auto &newName = getValueOptionalFlag(_nameLit);
    if (!newName.empty())
    {
        changeDbHost.set_n_name(newName);
    }
    if (!connString.empty())
    {
        changeDbHost.set_n_name(connString);
    }

    const auto &userString = getValueOptionalFlag(_userLit);
    if (!userString.empty())
    {
        changeDbHost.set_n_user(userString);
    }
    const auto &passwdString = getValueOptionalFlag(_passwdLit);
    if (!passwdString.empty())
    {
        changeDbHost.set_n_passwd(passwdString);
    }

    return rascontrol->changeDbHost(changeDbHost);
}

//change db 'dbname' [-name 'newname']
std::string RasControlGrammar::changeDB()
{
    changeDb.Clear();

    changeDb.set_db_name(getValueMandatoryFlag(dbLit));
    const auto &dbNewName = getValueOptionalFlag(_nameLit);
    if (!dbNewName.empty())
    {
        changeDb.set_n_db_name(dbNewName);
    }

    return rascontrol->changeDb(changeDb);
}

// change srv 'srvname' [-name 'newname'] [-dbh 'dbhname']
//            [-host 'hostname'] [-port 'portnumber'] [-autorestart on|off]
//            [-countdown 'number'] [-available 'min_available']
//            [-size 'group_size'] [-alive 'min_alive']
//            [-idle 'max_idle'] [-xp 'options']
std::string RasControlGrammar::changeRasServer()
{
    changeServerGroup.Clear();

    changeServerGroup.set_group_name(getValueMandatoryFlag(srvLit));
    const auto &newServerName = getValueOptionalFlag(_nameLit);
    if (!newServerName.empty())
    {
        changeServerGroup.set_n_group_name(newServerName);
    }

    // TODO: handle port ranges
    const auto &portString = getValueOptionalFlag(_portLit);
    if (!portString.empty())
    {
        changeServerGroup.add_n_ports(convertToULong(portString, "port"));
    }

    const auto &hostName = getValueOf(_hostLit);
    if (!hostName.empty())
    {
        changeServerGroup.set_n_host(hostName);
    }

    const auto &autoRestart = getValueOptionalFlag(_autorestartLit);
    if (!autoRestart.empty())
    {
        if (strieq(autoRestart, onLit))
        {
            changeServerGroup.set_n_autorestart(true);
        }
        else if (strieq(autoRestart, offLit))
        {
            changeServerGroup.set_n_autorestart(false);
        }
        else
        {
            return error("Invalid autorestart option '" +
                         autoRestart + "', use one of [on|off].");
        }
    }
    else
    {
        changeServerGroup.set_n_autorestart(true);
    }

    const auto &dbhName = getValueOptionalFlag(_dbhLit);
    if (!dbhName.empty())
    {
        changeServerGroup.set_n_db_host(dbhName);
    }
    const auto &count = getValueOptionalFlag(_countdownLit);
    if (!count.empty())
    {
        changeServerGroup.set_n_countdown(convertToULong(count, "countdown"));
    }
    const auto &size = getValueOptionalFlag(_sizeLit);
    if (!size.empty())
    {
        changeServerGroup.set_n_group_size(convertToULong(size, "size"));
    }
    const auto &alive = getValueOptionalFlag(_aliveLit);
    if (!alive.empty())
    {
        changeServerGroup.set_n_min_alive_servers(convertToULong(alive, "alive"));
    }
    const auto &available = getValueOptionalFlag(_availableLit);
    if (!available.empty())
    {
        changeServerGroup.set_n_min_alive_servers(convertToULong(available, "alive"));
    }
    const auto &idle = getValueOptionalFlag(_idleLit);
    if (!idle.empty())
    {
        changeServerGroup.set_n_max_idle_servers(convertToULong(idle, "idle"));
    }
    const auto &xp = getExtraParams(_xpLit);
    if (!xp.empty())
    {
        changeServerGroup.set_n_options(xp);
    }

    return rascontrol->changeServerGroup(changeServerGroup);
}

// change user 'username' [-name 'newname'] [-passwd 'newpasswd] [-rights 'rightsstring']
std::string RasControlGrammar::changeUserCmd()
{
    changeUser.Clear();

    changeUser.set_user_name(getValueMandatoryFlag(userLit));
    const auto &newName = getValueOptionalFlag(_nameLit);
    if (!newName.empty())
    {
        changeUser.set_n_name(newName);
    }
    const auto &newPasswd = getValueOptionalFlag(_passwdLit);
    if (!newPasswd.empty())
    {
        changeUser.set_n_passwd(newPasswd);
    }

    const auto &rights = getValueOptionalFlag(_rightsLit);
    if (rights == "-")
    {
        changeUser.set_n_config_rights(false);
        changeUser.set_n_access_rights(false);
        changeUser.set_n_server_admin_rights(false);
        changeUser.set_n_info_rights(false);
        changeUser.set_n_dbread_rights(false);
        changeUser.set_n_dbwrite_rights(false);
    }
    else if (!rights.empty())
    {
        for (size_t i = 0; i < rights.size(); i++)
        {
            switch (rights[i])
            {
            case 'C':
                changeUser.set_n_config_rights(true);
                break;
            case 'A':
                changeUser.set_n_access_rights(true);
                break;
            case 'S':
                changeUser.set_n_server_admin_rights(true);
                break;
            case 'I':
                changeUser.set_n_info_rights(true);
                break;
            case 'R':
                changeUser.set_n_dbread_rights(true);
                break;
            case 'W':
                changeUser.set_n_dbwrite_rights(true);
                break;
            default:
                return error("Invalid character '" + string{rights[i]} +
                             "' in -rights parameter.");
            }
        }
    }

    return rascontrol->changeUser(changeUser);
}

//---------------------------------------------

std::string RasControlGrammar::upCommand()
{
    const auto &what = tokens.size() == 1 ? empty : tokens[1];
    if (strieq(what, srvLit))
    {
        return upRasServers();
    }
    else if (strieq(what, helpLit))
    {
        return upHelp();
    }
    else
    {
        return error("Invalid UP command '" + what + "'.");
    }
}

// up srv [ s | -host h | -all]
std::string RasControlGrammar::upRasServers()
{
    upSrv.Clear();

    const auto &srvName = getValueOf(srvLit);
    if (!srvName.empty())
    {
        upSrv.set_group_name(srvName);
    }
    else
    {
        const auto &hostName = getValueOptionalFlag(_hostLit);
        if (!hostName.empty())
        {
            upSrv.set_host_name(hostName);
        }
        else if (isFlag(_allLit))
        {
            upSrv.set_all(true);
        }
        else
        {
            return error("Invalid UP SRV command; try HELP UP.");
        }
    }

    return rascontrol->startServerGroup(upSrv);
}

//--------------------------------------------------------

std::string RasControlGrammar::downCommand()
{
    const auto &what = tokens.size() == 1 ? empty : tokens[1];
    if (strieq(what, hostLit))
    {
        return downRasHosts();
    }
    else if (strieq(what, srvLit))
    {
        return downRasServers();
    }
    else if (strieq(what, helpLit))
    {
        return downHelp();
    }
    else
    {
        return error("Invalid DOWN command '" + what + "'.");
    }
}

// down srv [ s | -host h | -all] [ -force] [-kill]
//   - stops 'server s' or 'all started servers on host h' or 'all started servers'
std::string RasControlGrammar::downRasServers()
{
    downSrv.Clear();


    const auto &srvName = getValueOf(srvLit);
    if (!srvName.empty())
    {
        downSrv.set_group_name(srvName);
    }
    else
    {
        const auto &hostName = getValueOptionalFlag(_hostLit);
        if (!hostName.empty())
        {
            downSrv.set_host_name(hostName);
        }
        else if (isFlag(_allLit))
        {
            downSrv.set_all(true);
        }
        else
        {
            return error("Invalid DOWN SRV command; try HELP DOWN.");
        }
    }

    if (isFlag(_forceLit))
    {
        downSrv.set_kill_level(FORCE);
    }
    if (isFlag(_killLit))
    {
        downSrv.set_kill_level(KILL);
    }

    return rascontrol->stopServerGroup(downSrv);
}

// down host [ h | -all]
std::string RasControlGrammar::downRasHosts()
{
    return rascontrol->stopRasMgr();
}

//----------------------------------------------------

std::string RasControlGrammar::saveCommand()
{
    rascontrol->save();
    return {};
}

// ----------------------------------------------------------------------------

std::string RasControlGrammar::helpCommand()
{
    const auto &what = tokens.size() == 1 ? helpLit : tokens[1];

    if (strieq(what, listLit))
    {
        return listHelp();
    }
    else if (strieq(what, defineLit))
    {
        return defineHelp();
    }
    else if (strieq(what, removeLit))
    {
        return removeHelp();
    }
    else if (strieq(what, changeLit))
    {
        return changeHelp();
    }
    else if (strieq(what, upLit))
    {
        return upHelp();
    }
    else if (strieq(what, downLit))
    {
        return downHelp();
    }
    else if (strieq(what, saveLit))
    {
        return saveHelp();
    }
    else if (strieq(what, exitLit) or strieq(what, quitLit) or strieq(what, byeLit))
    {
        return exitHelp();
    }
    else
    {
        return helpHelp();
    }
}

std::string RasControlGrammar::helpHelp()
{
    return "Help for rascontrol command language\r\n"
           "rasdaman uses the following terms:\r\n"
           "  host (server host)    - a computer running a RasManager (rasmgr), with or without currently active servers\r\n"
           "  srv  (server)         - the rasdaman server (rasserver)\r\n"
           "  dbh  (data base host) - a computer running the database software\r\n"
           "  db   (database)       - the rasdaman database, hosted by the underlying database instance\r\n"
           "  user                  - a person registered by rasdaman through user name and password\r\n"
           "  inpeer                - a peer which can forward client requests to the current RasManager\r\n"
           "  outpeer               - a peer to which the current RasManager can forward client requests\r\n"
           "\r\nThe rascontrol utility allows to configure and do run-time administration work for the rasdaman system\r\n"
           "Commands:\r\n"
           "   >help       ...this help\r\n"
           "   >exit       ...exit rascontrol\r\n"
           "   >list       ...list information about the current status of the system\r\n"
           "   >up         ...start servers\r\n"
           "   >down       ...stop servers and rasmanagers\r\n"
           "   >define     ...define a new object\r\n"
           "   >remove     ...remove an object\r\n"
           "   >change     ...change parameters of objects\r\n"
           "   >save       ...make changes permanent\r\n"
           "Type 'help command' to get specific information about command\r\n";
}

std::string RasControlGrammar::listHelp()
{
    return "   The list command:\r\n"
           "list srv [ s | -host h | -all ] [-p] \r\n"
           "       - list information about 'server s' or 'all servers on host h' or 'all defined servers' (default)\r\n"
           "         '-p' prints configuration information; default: runtime status information\r\n"
           "list host\r\n"
           "       - list information about server hosts\r\n"
           "list dbh\r\n"
           "       - list information about database hosts\r\n"
           "list db [ d | -dbh h | -all ] \r\n"
           "       - list information about 'database s' or all 'databases on database host h' or 'all defined databases'\r\n"
           "list user [ -rights]\r\n"
           "       - list the defined users\r\n"
           "         '-rights' additionally lists each user's rights\r\n"
           "list version\r\n"
           "       - list version information"
           "list inpeer\r\n"
           "       - list information about inpeers\r\n"
           "list outpeer\r\n"
           "       - list information about outpeers\r\n";
}

std::string RasControlGrammar::defineHelp()
{
    return "  The define command:\r\n"
           "define dbh 'dbhname' -connect 'connectstring'\r\n"
           "       - define database host with symbolic name 'dbhname'\r\n"
           "         'connectstring' is the string used to connect a client to the underlying database instance\r\n"
           "         (example: user/passwd@hostaddress)\r\n"
           "define db 'dbname' -dbh 'dbhname'\r\n"
           "       - define database 'dbname' on database host 'dbhname'\r\n"
           "         ('dbname' is not a symbolic name, it is the real name of the rasdaman database)\r\n"
           "define host 'hostname' -net 'netaddress' [-port 'portnumber']\r\n"
           "       - define server host with symbolic name 'hostname', located at address 'netaddress:portnumber'\r\n"
           "         ('portnumber' defaults to 7001)\r\n"
           "define srv 'srvname' -host 'hostname' -dbh 'dbhname' -type 'servertype' -port 'portnumber' \r\n"
           "                                [-autorestart on|off] [-countdown 'number'] [-xp 'options']\r\n"
           "       - define server with symbolic name 'srvname' on server host 'hostname' connected to database host 'dbhname'\r\n"
           "         'servertype' can be 'r' (RPC) or 'h' (HTTP) or 'n' (RNP)\r\n"
           "         'portnumber' is the IP port number for HTTP servers / the 'prognum' for RPC/RNP servers\r\n"
           "         -autorestart (default: on): the server will autorestart after an unexpected termination\r\n"
           "         -countdown 'number' (default: 1000): the server will be restarted after 'number' transactions\r\n"
           "         -xp 'options': extra parameter string 'options' that will be passed to the server at startup \r\n"
           "          (default: \"\", see documentation for valid 'options')\r\n"
           "          this option has to be the last, because anything after it and until end of line is considered to be 'options'\r\n"
           "define user 'username' [-passwd 'password'] [-rights 'rightsstring']\r\n"
           "       - define user account with symbolic name 'username'\r\n"
           "         'password' defaults to 'username' (use the raspasswd utility to change)\r\n"
           "         -rights 'rightsstring': the rights granted to the user (default: none; see documentation for valid rights)\r\n"
           "define inpeer 'hostname'\r\n"
           "       - define inpeer with the host name 'hostname'\r\n"
           "define outpeer 'hostname' [-port 'portnumber']\r\n"
           "       - define outpeer with the host name 'hostname'\r\n"
           "         ('portnumber' defaults to 7001)\r\n";
}

std::string RasControlGrammar::removeHelp()
{
    return "   The remove command:\r\n"
           "remove dbh 'dbhname'\r\n"
           "       - remove database host 'dbhname'\r\n"
           "remove db 'dbname' -dbh 'dbhname'\r\n"
           "       - remove database 'dbname' from database host 'dbhname'\r\n"
           "         (the database itself is not deleted, only the name is removed from the config tables)\r\n"
           "remove host 'hostname' \r\n"
           "       - remove server host 'hostname'\r\n"
           "remove srv 'srvname'\r\n"
           "       - remove server 'srvname'\r\n"
           "remove user 'username'\r\n"
           "       - remove the user 'username'\r\n"
           "remove inpeer 'hostname'\r\n"
           "       - remove inpeer with host name 'hostname'\r\n"
           "remove outpeer 'hostname'\r\n"
           "       - remove outpeer with host name 'hostname'\r\n";
}

std::string RasControlGrammar::changeHelp()
{
    return "   The change command:\r\n"
           "change dbh 'dbhname' [-name 'newname'] [-connect 'newconnectstring']\r\n"
           "change db 'dbname' [-name 'newname']\r\n"
           "change host 'hostname' [-name 'newname'] [-net 'newnetaddress'] [-port 'newportnumber']\r\n"
           "change srv 'servername' [-name 'newname'][-dbh 'newdbhname'] [-type 'newservertype'] [-port 'newportnumber'] [-autorestart on|off] [-countdown 'newnumber'] [-xp 'newoptions']\r\n"
           "change user 'username' [-name 'newname'] [-passwd 'newpasswd] [-rights 'rightsstring']\r\n"
           "       - see the help for the define command for option description\r\n";
}

std::string RasControlGrammar::upHelp()
{
    return "   The up command:\r\n"
           "up srv [ s | -host h | -all]\r\n"
           "       - start 'server s' or 'all servers on host h' or 'all defined servers'\r\n";
}

std::string RasControlGrammar::downHelp()
{
    return "   The down command:\r\n"
           "down srv [ s | -host h | -all] [ -force] [-kill]\r\n"
           "       - stops 'server s' or 'all started servers on host h' or 'all started servers'\r\n"
           "         -force: stops the 'server s' without waiting to complete the current transaction (using SIGTERM)\r\n"
           "         -kill:  instantly stops the 'server s' (using SIGKILL)\r\n"
           "          (without -force or -kill the server completes the current transaction and exits)\r\n"
           "down host [ h | -all]\r\n"
           "       - stops the rasmgr on 'host h' or all started rasmgr\r\n";
}

std::string RasControlGrammar::saveHelp()
{
    return "   The save command\r\n"
           "save\r\n"
           "    - saves the current configuration information\r\n"
           "      (upon changes the files will be saved automatically to rescue files next to the config files when exiting rasmgr)\r\n";
}

std::string RasControlGrammar::exitHelp()
{
    return "   The exit command\r\n"
           "exit | quit | bye\r\n"
           "    - finish this rascontrol session\r\n";
}

// ----------------------------------------------------------------------------

std::string RasControlGrammar::error(const std::string &errText)
{
    return "Error: " + errText;
}

bool RasControlGrammar::isFlag(const std::string &flag, int pos)
{
    if (pos < 0) // doesn't matter
    {
        for (size_t i = 1; i < tokens.size(); ++i) // flags are from 1->, 0 is the command itself
            if (strieq(flag, tokens[i]))
            {
                return true;
            }
    }
    if (pos > 1 && pos < static_cast<int>(tokens.size()))
    {
        if (strieq(flag, tokens[static_cast<size_t>(pos)]))
        {
            return true;
        }
    }
    return false;
}

std::string RasControlGrammar::getValueOf(const std::string &flag, bool acceptMinus)
{
    for (size_t i = 1; i < tokens.size() - 1; i++)
    {
        if (strieq(flag, tokens[i]))
        {
            auto value = tokens[i + 1];
            if (acceptMinus)
            {
                if (value[0] == '-' && value.size() > 1)
                {
                    return empty;
                }
            }
            else if (value[0] == '-')
            {
                return empty;
            }
            return value;
        }
    }
    return empty;
}

std::string RasControlGrammar::getValueOptionalFlag(const std::string &flag,
        bool acceptMinus)
{
    if (!isFlag(flag))
    {
        return empty;
    }
    auto value = getValueOf(flag, acceptMinus);
    if (value == empty)
    {
        throw RCErrorMissingParam(flag);
    }
    return value;
}

std::string RasControlGrammar::getValueMandatoryFlag(const std::string &flag,
        bool acceptMinus)
{
    auto ret = getValueOptionalFlag(flag, acceptMinus);
    if (ret == empty)
    {
        throw RCErrorMissingParam(flag);
    }
    return ret;
}

unsigned long RasControlGrammar::convertToULong(const std::string &stringValue,
        const std::string &errMsg)
{
    char *end;
    unsigned long ret = strtoul(stringValue.c_str(), &end, 0);
    if (strlen(end) != 0)
    {
        throw RCErrorIncorNumberValue(errMsg);
    }
    return ret;
}

std::string RasControlGrammar::getExtraParams(const std::string &key)
{
    std::string ret{};
    bool found = false;
    for (size_t i = 0; i < tokens.size(); ++i)
    {
        if (found)
        {
            if (!ret.empty())
            {
                ret += " ";
            }
            ret += tokens[i];
        }
        if (strieq(key, tokens[i]))
        {
            found = true;
        }
    }
    return ret;
}

bool strieq(const std::string &left, const std::string &right)
{
    return strcasecmp(left.c_str(), right.c_str()) == 0;
}

const std::string RasControlGrammar::defineLit = "define";
const std::string RasControlGrammar::hostLit = "host";
const std::string RasControlGrammar::srvLit = "srv";
const std::string RasControlGrammar::_portLit = "-port";
const std::string RasControlGrammar::_networkLit = "-net";
const std::string RasControlGrammar::_typeLit = "-type";
const std::string RasControlGrammar::_dbhLit = "-dbh";
const std::string RasControlGrammar::dbhLit = "dbh";
const std::string RasControlGrammar::_connectLit = "-connect";
const std::string RasControlGrammar::_userLit = "-user";
const std::string RasControlGrammar::_passwdLit = "-passwd";
const std::string RasControlGrammar::dbLit = "db";
const std::string RasControlGrammar::userLit = "user";
const std::string RasControlGrammar::inpeerLit = "inpeer";
const std::string RasControlGrammar::outpeerLit = "outpeer";
const std::string RasControlGrammar::_nameLit = "-name";
const std::string RasControlGrammar::changeLit = "change";
const std::string RasControlGrammar::removeLit = "remove";
const std::string RasControlGrammar::listLit = "list";
const std::string RasControlGrammar::_allLit = "-all";
const std::string RasControlGrammar::_useLocalHostLit = "-uselocalhost";
const std::string RasControlGrammar::onLit = "on";
const std::string RasControlGrammar::offLit = "off";
const std::string RasControlGrammar::_autorestartLit = "-autorestart";
const std::string RasControlGrammar::_xpLit = "-xp";
const std::string RasControlGrammar::_countdownLit = "-countdown";
const std::string RasControlGrammar::_hostLit = "-host";
const std::string RasControlGrammar::_pLit = "-p";
const std::string RasControlGrammar::_rightsLit = "-rights";
const std::string RasControlGrammar::_aliveLit = "-alive";
const std::string RasControlGrammar::_availableLit = "-available";
const std::string RasControlGrammar::_idleLit = "-idle";
const std::string RasControlGrammar::_sizeLit = "-size";
const std::string RasControlGrammar::saveLit = "save";
const std::string RasControlGrammar::exitLit = "exit";
const std::string RasControlGrammar::versionLit = "version";
const std::string RasControlGrammar::helpLit = "help";
const std::string RasControlGrammar::helloLit = "hello";
const std::string RasControlGrammar::byeLit = "bye";
const std::string RasControlGrammar::quitLit = "quit";
const std::string RasControlGrammar::loginLit = "login";
const std::string RasControlGrammar::upLit = "up";
const std::string RasControlGrammar::downLit = "down";
const std::string RasControlGrammar::_forceLit = "-force";
const std::string RasControlGrammar::_killLit = "-kill";

const std::string RasControlGrammar::commentPrefix = "#";

//
// Exceptions
//

RCError::RCError()
{
}

RCErrorUnexpToken::RCErrorUnexpToken(const std::string &token)
    : pcc(token)
{
}

std::string RCErrorUnexpToken::getString()
{
    return "Unexpected token '" + pcc + "' in command.";
}

RCErrorNoPermission::RCErrorNoPermission()
{
}

std::string RCErrorNoPermission::getString()
{
    return "You don't have permission for this operation.";
}

RCErrorInvalidName::RCErrorInvalidName(const std::string &name)
    : pcc(name)
{
}

std::string RCErrorInvalidName::getString()
{
    return "Invalid name '" + pcc + "'.";
}

RCErrorMissingParam::RCErrorMissingParam(const std::string &what)
    : pcc(what)
{
}

std::string RCErrorMissingParam::getString()
{
    return "Missing parameter '" + pcc + "'.";
}

RCErrorIncorNumberValue::RCErrorIncorNumberValue(const std::string &what)
    : pcc(what)
{
}

std::string RCErrorIncorNumberValue::getString()
{
    return "Incorrect number value for parameter '" + pcc + "'.";
}

}
