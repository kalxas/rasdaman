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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef RASMGR_X_SRC_RASCONTROLGRAMMAR_HH
#define RASMGR_X_SRC_RASCONTROLGRAMMAR_HH

#include <string>
#include <iostream>
#include <vector>

#define BOOST_SPIRIT_USE_PHOENIX_V3

#include <boost/spirit/include/qi.hpp>
#include <boost/spirit/include/phoenix.hpp>
#include <boost/function.hpp>
#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/any.hpp>
#include <boost/cstdint.hpp>
#include <boost/phoenix/operator.hpp>
#include <boost/fusion/container/vector.hpp>

#include "common/src/logging/easylogging++.hh"

#include "rascontrol.hh"
#include "rasctrlgrammarconstants.hh"

namespace rasmgr
{
namespace qi    = boost::spirit::qi;
namespace ascii = boost::spirit::ascii;
namespace phx   = boost::phoenix;

//TODO-AT: Explain how all this work and document it.
//TODO-AT: At this point the usage of member protobuf variables make the grammar non-reentrant. Make it thread safe at some point.F
//TODO-AT: Make boost::uint_t work.
template<class Iterator>
struct RasControlGrammar : qi::grammar<Iterator, std::string ( void ), ascii::space_type>
{

    RasControlGrammar ( boost::shared_ptr<RasControl> rascontrol ) : RasControlGrammar::base_type ( start )
{
    this->rascontrol = rascontrol;

    //Define literal strings used by the more complex rules bellow
    this->defineLit = qi::lit ( "define" );
    this->hostLit = qi::lit ( "host" );
    this->srvLit = qi::lit ( "srv" );
    this->_portLit = qi::lit ( "-port" );
    this->_networkLit = qi::lit ( "-net" );
    this->_typeLit = qi::lit ( "-type" );
    this->_dbhLit = qi::lit ( "-dbh" );
    this->dbhLit = qi::lit ( "dbh" );
    this->_connectLit = qi::lit ( "-connect" );
    this->_userLit = qi::lit ( "-user" );
    this->_passwdLit = qi::lit ( "-passwd" );
    this->dbLit = qi::lit ( "db" );
    this->userLit = qi::lit ( "user" );
    this->inpeerLit = qi::lit ( "inpeer" );
    this->outpeerLit = qi::lit ( "outpeer" );
    this->_nameLit = qi::lit ( "-name" );
    this->changeLit = qi::lit ( "change" );
    this->removeLit = qi::lit ( "remove" );
    this->listLit = qi::lit ( "list" );
    this->_allLit = qi::lit ( "-all" );
    this->_useLocalHostLit = qi::lit ( "-uselocalhost" );
    this->onLit = qi::lit ( "on" );
    this->offLit = qi::lit ( "off" );
    this->_typeLit = qi::lit ( "-type" );
    this->_autorestartLit = qi::lit ( "-autorestart" );
    this->_xpLit = qi::lit ( "-xp" );
    this->_countdownLit = qi::lit ( "-countdown" );
    this->_hostLit = qi::lit ( "-host" );
    this->_pLit = qi::lit ( "-p" );
    this->_rightsLit = qi::lit ( "-rights" );
    this->_aliveLit = qi::lit ( "-alive" );
    this->_availableLit = qi::lit ( "-available" );
    this->_idleLit = qi::lit ( "-idle" );
    this->_sizeLit = qi::lit ( "-size" );
    this->saveLit = qi::lit ( "save" );
    this->exitLit = qi::lit ( "exit" );
    this->versionLit = qi::lit ( "version" );
    this->helpLit = qi::lit ( "help" );
    this->byeLit = qi::lit ( "bye" );
    this->quitLit = qi::lit ( "quit" );
    this->upLit = qi::lit ( "up" );
    this->downLit = qi::lit ( "down" );


    //this->rightsOptions = ;

    //Rule describing a comment
    this->commentRule = qi::lit ( "#" ) >> *qi::char_;
    //Rule describing a space delimited string
    //TODO-AT: This rule should parse any valid c++ identifier
    this->strRule = qi::lexeme[ ( qi::alpha | qi::char_ ( '_' ) ) >> * ( qi::alnum | qi::char_ ( '_' ) )]; //qi::as_string[qi::lexeme[+qi::alnum]];

    //TODO-AT: Make this accept any host name
    this->hostNameRule  = qi::lexeme[ ( qi::alpha | qi::char_ ( '_' ) ) >> * ( qi::alnum | qi::char_ ( '_' ) | qi::char_ ( '-' ) )];

    //BEGIN:HOST COMMANDS
    //The host commands are DEPRECATED.
    //The commands did not provide any functionality and in this version they only return a message that the command is deprecated

    //define <<host h -net n -port p>>
    this->defineHostSubRule = ( this->hostLit >> this->strRule
                                >> this->_networkLit >> this->strRule
                                >> this->_portLit >> qi::int_ )
                              [qi::_val = boost::phoenix::bind ( &RasControl::deprecatedCommand, this->rascontrol.get() )];

    //change <<host h [-name n] [-net x] [-port p] [-uselocalhost [on|off] ] >>
    this->changeHostSubRule = ( this->hostLit >> this->strRule )
                              >> ( - ( ( this->_nameLit >> this->strRule )
                                       ^ ( ( this->_networkLit >> this->strRule ) )
                                       ^ ( ( this->_portLit >> qi::int_ ) )
                                       ^ ( ( this->_useLocalHostLit >> - ( this->onLit | this->offLit ) ) ) ) )
                              [qi::_val = boost::phoenix::bind ( &RasControl::deprecatedCommand, this->rascontrol.get() )];

    //remove << host h >>
    this->removeHostSubRule = ( this->hostLit >> this->strRule )
                              [qi::_val = boost::phoenix::bind ( &RasControl::deprecatedCommand, this->rascontrol.get() )];

    //list <<host>>
    this->listHostSubRule = ( this->hostLit )
                            [qi::_val = boost::phoenix::bind ( &RasControl::deprecatedCommand, this->rascontrol.get() )];

    //help <<host>>
    this->helpHostSubRule = ( this->hostLit )
                            [qi::_val = boost::phoenix::bind ( &RasControl::deprecatedCommand, this->rascontrol.get() )];

    //END: HOST COMMANDS


    //BEGIN: SERVER COMMANDS

    this->defineServerPorts = ( this->_portLit
                                >> (
                                    ( qi::uint_ >> qi::lit ( "-" ) >> qi::uint_ ) [qi::_pass = boost::phoenix::bind ( &RasControlGrammar::processDefPortRange, this, qi::_1, qi::_2 )]
                                    | ( qi::uint_[boost::phoenix::bind ( &DefineServerGroup::add_ports, &defServerGroup, qi::_1 )] ) )
                                >> * ( ","
                                       >> ( ( qi::uint_ >> qi::lit ( "-" ) >> qi::uint_ ) [qi::_pass = boost::phoenix::bind ( &RasControlGrammar::processDefPortRange, this, qi::_1, qi::_2 )]
                                            | ( qi::uint_[boost::phoenix::bind ( &DefineServerGroup::add_ports, &defServerGroup, qi::_1 )] ) ) )
                              ) [qi::_val = "ports"];

    this->defineServerAuxSubRule = ( this->_hostLit >> this->hostNameRule[boost::bind ( static_cast< void ( DefineServerGroup::* ) ( const ::std::string & ) > ( &DefineServerGroup::set_host ), &defServerGroup, _1 )] )
                                   >> - ( this->_typeLit >> ( qi::char_ ( 'r' ) | qi::char_ ( 'h' ) | qi::char_ ( 'n' ) ) ) //This option is ignored
                                   >> this->defineServerPorts
                                   >> ( this->_dbhLit >> this->strRule[boost::bind ( static_cast< void ( DefineServerGroup::* ) ( const ::std::string & ) > ( &DefineServerGroup::set_db_host ), &defServerGroup, _1 )] )
                                   >> -this->defineServerOptionsSubRule
                                   >> - ( this->_xpLit >> this->strRule[boost::bind ( static_cast< void ( DefineServerGroup::* ) ( const ::std::string & ) > ( &DefineServerGroup::set_options ), &defServerGroup, _1 )] );

    this->defineServerOptionsSubRule = ( this->_autorestartLit[boost::bind ( &ChangeServerGroup::set_n_autorestart, &changeServerGroup, true )] >> - ( this->onLit[boost::bind ( &ChangeServerGroup::set_n_autorestart, &changeServerGroup, true )] | this->offLit[boost::bind ( &ChangeServerGroup::set_n_autorestart, &changeServerGroup, false )] ) )
                                       ^ ( ( this->_countdownLit >> qi::uint_[boost::bind ( &DefineServerGroup::set_countdown, &defServerGroup, _1 )] ) )
                                       ^ ( this->_sizeLit >> qi::uint_[boost::bind ( &DefineServerGroup::set_group_size, &defServerGroup, _1 )] )
                                       ^ ( this->_aliveLit >> qi::uint_[boost::bind ( &DefineServerGroup::set_min_alive_servers, &defServerGroup, _1 )] )
                                       ^ ( this->_availableLit >> qi::uint_[boost::bind ( &DefineServerGroup::set_min_available_servers, &defServerGroup, _1 )] )
                                       ^ ( this->_idleLit >> qi::uint_[boost::bind ( &DefineServerGroup::set_max_idle_servers, &defServerGroup, _1 )] );

    //define <<srv s -host h -type t -port p -dbh d [-autorestart [on|off] [-countdown c] [-xp options]>>
    this->defineServerSubRule = ( this->srvLit[boost::bind ( &DefineServerGroup::Clear, &defServerGroup )] >> this->strRule[boost::bind ( static_cast< void ( DefineServerGroup::* ) ( const ::std::string & ) > ( &DefineServerGroup::set_group_name ), &defServerGroup, _1 )] )
                                >> this->defineServerAuxSubRule
                                [qi::_val = boost::phoenix::bind ( &RasControl::defineServerGroup, this->rascontrol.get(), boost::phoenix::ref ( this->defServerGroup ) )];

    // change <<srv s [-name n] [-port p] [-dbh d] [-autorestart [on|off] [-countdown c] [-xp options]>>
    this->changeServerSubRule = ( ( this->srvLit[boost::bind ( &ChangeServerGroup::Clear, &changeServerGroup )] >> this->strRule[boost::bind ( static_cast< void ( ChangeServerGroup::* ) ( const ::std::string & ) > ( &ChangeServerGroup::set_group_name ), &changeServerGroup, _1 )] )

                                  >> ( ( this->_nameLit >> this->strRule[boost::bind ( static_cast< void ( ChangeServerGroup::* ) ( const ::std::string & ) > ( &ChangeServerGroup::set_n_group_name ), &changeServerGroup, _1 )] )
                                       ^ ( this->_hostLit >> this->hostNameRule[boost::bind ( static_cast< void ( ChangeServerGroup::* ) ( const ::std::string & ) > ( &ChangeServerGroup::set_n_host ), &changeServerGroup, _1 )] )
                                       ^ ( this->_sizeLit >> qi::uint_[boost::bind ( &ChangeServerGroup::set_n_group_size, &changeServerGroup, _1 )] )
                                       ^ ( this->_aliveLit >> qi::uint_[boost::bind ( &ChangeServerGroup::set_n_min_alive_servers, &changeServerGroup, _1 )] )
                                       ^ ( this->_availableLit >> qi::uint_[boost::bind ( &ChangeServerGroup::set_n_min_available_servers, &changeServerGroup, _1 )] )
                                       ^ ( this->_idleLit >> qi::uint_[boost::bind ( &ChangeServerGroup::set_n_max_idle_servers, &changeServerGroup, _1 )] )
                                       ^ ( this->_portLit
                                           >> (
                                                   ( qi::uint_ >> qi::lit ( "-" ) >> qi::uint_ ) [qi::_pass = boost::phoenix::bind ( &RasControlGrammar::processChPortRange, this, qi::_1, qi::_2 )]
                                                   | ( qi::uint_[boost::phoenix::bind ( &ChangeServerGroup::add_n_ports, &changeServerGroup, qi::_1 )] ) )
                                           >> * ( ","
                                                   >> ( ( qi::uint_ >> qi::lit ( "-" ) >> qi::uint_ ) [qi::_pass = boost::phoenix::bind ( &RasControlGrammar::processChPortRange, this, qi::_1, qi::_2 )]
                                                           | ( qi::uint_[boost::phoenix::bind ( &ChangeServerGroup::add_n_ports, &changeServerGroup, qi::_1 )] ) ) )
                                         )

                                       ^ ( this->_dbhLit >> this->strRule[boost::bind ( static_cast< void ( ChangeServerGroup::* ) ( const ::std::string & ) > ( &ChangeServerGroup::set_n_db_host ), &changeServerGroup, _1 )] )

                                       ^ ( this->_autorestartLit[boost::bind ( &ChangeServerGroup::set_n_autorestart, &changeServerGroup, true )] >> - ( this->onLit[boost::bind ( &ChangeServerGroup::set_n_autorestart, &changeServerGroup, true )] | this->offLit[boost::bind ( &ChangeServerGroup::set_n_autorestart, &changeServerGroup, false )] ) )

                                       ^ ( this->_countdownLit >> qi::uint_[boost::bind ( &ChangeServerGroup::set_n_countdown, &changeServerGroup, _1 )] ) )

                                  >> - ( this->_xpLit >> this->strRule[boost::bind ( static_cast< void ( ChangeServerGroup::* ) ( const ::std::string & ) > ( &ChangeServerGroup::set_n_options ), &changeServerGroup, _1 )] ) )
                                [qi::_val = boost::phoenix::bind ( &RasControl::changeServerGroup, this->rascontrol.get(), boost::phoenix::ref ( this->changeServerGroup ) )];

    //remove <<srv s>>
    this->removeServerSubRule = ( this->srvLit >> this->strRule )
                                [qi::_val = boost::phoenix::bind ( &RasControl::removeServerGroup, this->rascontrol.get(), qi::_2 )];

    //TODO-AT: Add option for -x
    //list <<srv [ s | -host h | -all ] [-p]>>
    this->listServerSubRule = ( this->srvLit[boost::bind ( &ListServerGroup::Clear, &listServerGroup )]
                                >> ( this->_allLit
                                     | ( this->_hostLit >> this->strRule[boost::bind ( static_cast< void ( ListServerGroup::* ) ( const ::std::string & ) > ( &ListServerGroup::set_host ), &listServerGroup, _1 )] )
                                     | this->strRule[boost::bind ( static_cast< void ( ListServerGroup::* ) ( const ::std::string & ) > ( &ListServerGroup::set_group_name ), &listServerGroup, _1 )] )
                                >> - ( this->_pLit[boost::bind ( &ListServerGroup::set_extra_info, &listServerGroup, true )] ) )
                              [qi::_val = boost::phoenix::bind ( &RasControl::listServerGroup, this->rascontrol.get(), boost::phoenix::ref ( this->listServerGroup ) )];

    //help <<srv>>
    this->helpServerSubRule = this->srvLit
                              [qi::_val = boost::phoenix::bind ( &RasControl::helpServerGroup, this->rascontrol.get() )];


    ////-------BEGIN:Database host section -----////
    //define << dbh h [-connect c] [-user u -passwd p] >>
    //TODO-AT: Are the user and password required at the same time or is only one required?
    this->defineDbhSubRule = ( ( this->dbhLit[boost::bind ( &DefineDbHost::Clear, &defDbHost )] >> this->strRule[boost::bind ( static_cast< void ( DefineDbHost::* ) ( const ::std::string & ) > ( &DefineDbHost::set_host_name ), &defDbHost, _1 )] )
                               >> - ( this->_connectLit >> this->strRule[boost::bind ( static_cast< void ( DefineDbHost::* ) ( const ::std::string & ) > ( &DefineDbHost::set_connect ), &defDbHost, _1 )] )
                               >> - ( ( this->_userLit >> this->strRule[boost::bind ( static_cast< void ( DefineDbHost::* ) ( const ::std::string & ) > ( &DefineDbHost::set_user ), &defDbHost, _1 )] )
                                      >> ( this->_passwdLit >> this->strRule[boost::bind ( static_cast< void ( DefineDbHost::* ) ( const ::std::string & ) > ( &DefineDbHost::set_passwd ), &defDbHost, _1 )] ) ) )
                             [qi::_val = boost::phoenix::bind ( &RasControl::defineDbHost, this->rascontrol.get(), boost::phoenix::ref ( this->defDbHost ) )];

    //change << dbh h [-name n] [-connect c] [-user u -passwd p]>>
    this->changeDbhSubRule = ( ( this->dbhLit >> this->strRule[boost::bind ( static_cast< void ( ChangeDbHost::* ) ( const ::std::string & ) > ( &ChangeDbHost::set_host_name ), &changeDbHost, _1 )] )
                               >> - ( this->_nameLit >> this->strRule[boost::bind ( static_cast< void ( ChangeDbHost::* ) ( const ::std::string & ) > ( &ChangeDbHost::set_n_name ), &changeDbHost, _1 )] )
                               >> - ( this->_connectLit >> this->strRule[boost::bind ( static_cast< void ( ChangeDbHost::* ) ( const ::std::string & ) > ( &ChangeDbHost::set_n_connect ), &changeDbHost, _1 )] )
                               >> - ( ( this->_userLit >> this->strRule[boost::bind ( static_cast< void ( ChangeDbHost::* ) ( const ::std::string & ) > ( &ChangeDbHost::set_n_user ), &changeDbHost, _1 )] )
                                      >> ( this->_passwdLit >> this->strRule[boost::bind ( static_cast< void ( ChangeDbHost::* ) ( const ::std::string & ) > ( &ChangeDbHost::set_n_passwd ), &changeDbHost, _1 )] ) ) )
                             [qi::_val = boost::phoenix::bind ( &RasControl::changeDbHost, this->rascontrol.get(), boost::phoenix::ref ( this->changeDbHost ) )];

    //remove <<dbh h>>
    this->removeDbhSubRule = ( this->dbhLit >> this->strRule[boost::bind ( static_cast< void ( RemoveDbHost::* ) ( const ::std::string & ) > ( &RemoveDbHost::set_host_name ), &removeDbHost, _1 )] )
                             [qi::_val = boost::phoenix::bind ( &RasControl::removeDbHost, this->rascontrol.get(), boost::phoenix::ref ( this->removeDbHost ) )];

    //list <<dbh>>
    this->listDbhSubRule = this->dbhLit
                           [qi::_val = boost::phoenix::bind ( &RasControl::listDbHost, this->rascontrol.get() )];

    this->helpDbhSubRule = ( this->dbhLit )
                           [qi::_val = boost::phoenix::bind ( &RasControl::helpDbHost, this->rascontrol.get() )];
    ////-------END:Database host section -----////


    ////-------BEGIN:Database section -----////
    //define <<db d –dbh db>>
    this->defineDbSubRule = ( ( this->dbLit[boost::bind ( &DefineDb::Clear, &this->defDb )] >> this->strRule[boost::bind ( static_cast< void ( DefineDb::* ) ( const ::std::string & ) > ( &DefineDb::set_db_name ), &defDb, _1 )] )
                              >> ( this->_dbhLit >> this->strRule[boost::bind ( static_cast< void ( DefineDb::* ) ( const ::std::string & ) > ( &DefineDb::set_dbhost_name ), &defDb, _1 )] ) )
                            [qi::_val = boost::phoenix::bind ( &RasControl::defineDb, this->rascontrol.get(), boost::phoenix::ref ( this->defDb ) )];

    //change <<db d -name n>>
    this->changeDbSubRule = ( ( this->dbLit[boost::bind ( &ChangeDb::Clear, &this->changeDb )] >> this->strRule[boost::bind ( static_cast< void ( ChangeDb::* ) ( const ::std::string & ) > ( &ChangeDb::set_db_name ), &changeDb, _1 )] )
                              >> ( this->_nameLit >> this->strRule[boost::bind ( static_cast< void ( ChangeDb::* ) ( const ::std::string & ) > ( &ChangeDb::set_n_db_name ), &changeDb, _1 )] ) )
                            [qi::_val = boost::phoenix::bind ( &RasControl::changeDb, this->rascontrol.get(), boost::phoenix::ref ( this->changeDb ) )];

    //remove <<db d –dbh db>>
    this->removeDbSubRule = ( ( this->dbLit[boost::bind ( &RemoveDb::Clear, &this->removeDb )] >> this->strRule[boost::bind ( static_cast< void ( RemoveDb::* ) ( const ::std::string & ) > ( &RemoveDb::set_db_name ), &removeDb, _1 )] )
                              >> ( this->_dbhLit >> this->strRule[boost::bind ( static_cast< void ( RemoveDb::* ) ( const ::std::string & ) > ( &RemoveDb::set_dbhost_name ), &removeDb, _1 )] ) )
                            [qi::_val = boost::phoenix::bind ( &RasControl::removeDb, this->rascontrol.get(), boost::phoenix::ref ( this->removeDb ) )];

    //list db [ d | -dbh h | -all ]
    //TODO make -all the default and optional
    this->listDbSubRule = ( this->dbLit[boost::bind ( &ListDb::Clear, &listDb )] >> ( this->_allLit
                            | ( this->_dbhLit >> this->strRule[boost::bind ( static_cast< void ( ListDb::* ) ( const ::std::string & ) > ( &ListDb::set_dbh_name ), &listDb, _1 )] )
                            | this->strRule[boost::bind ( static_cast< void ( ListDb::* ) ( const ::std::string & ) > ( &ListDb::set_db_name ), &listDb, _1 )] ) )
                          [qi::_val = boost::phoenix::bind ( &RasControl::listDb, this->rascontrol.get(), boost::phoenix::ref ( this->listDb ) )];;

    // help <<dbh>>
    this->helpDbSubRule = this->dbLit
                          [qi::_val = boost::phoenix::bind ( &RasControl::helpDb, this->rascontrol.get() )];;
    ////-------END:Database  section -----////



    ////-------BEGIN:user section -----////
    //define <<user u [-passwd p] [-rights r]>>
    this->defineUserSubRule = ( ( this->userLit[boost::bind ( &DefineUser::Clear, &this->defUser )] >> this->strRule[boost::bind ( static_cast< void ( DefineUser::* ) ( const ::std::string & ) > ( &DefineUser::set_user_name ), &defUser, _1 )] )
                                >> - ( ( this->_passwdLit >> this->strRule[boost::bind ( static_cast< void ( DefineUser::* ) ( const ::std::string & ) > ( &DefineUser::set_passwd ), &defUser, _1 )] )
                                       ^ ( this->_rightsLit >> ( qi::lexeme[ ( qi::char_ ( 'C' ) [boost::bind ( &DefineUser::set_config_rights, &defUser, true )]
                                               ^ qi::char_ ( 'A' ) [boost::bind ( &DefineUser::set_access_rights, &defUser, true )]
                                               ^ qi::char_ ( 'S' ) [boost::bind ( &DefineUser::set_server_admin_rights, &defUser, true )]
                                               ^ qi::char_ ( 'I' ) [boost::bind ( &DefineUser::set_info_rights, &defUser, true )]
                                               ^ qi::char_ ( 'R' ) [boost::bind ( &DefineUser::set_dbread_rights, &defUser, true )]
                                               ^ qi::char_ ( 'W' ) [boost::bind ( &DefineUser::set_dbwrite_rights, &defUser, true )] )]
                                               | qi::lexeme[qi::char_ ( '-' ) [boost::bind ( &DefineUser::set_dbwrite_rights, &defUser, false ),
                                                       boost::bind ( &DefineUser::set_dbread_rights, &defUser, false ),
                                                       boost::bind ( &DefineUser::set_config_rights, &defUser, false ),
                                                       boost::bind ( &DefineUser::set_info_rights, &defUser, false ),
                                                       boost::bind ( &DefineUser::set_server_admin_rights, &defUser, false ),
                                                       boost::bind ( &DefineUser::set_access_rights, &defUser, false )]] ) ) ) )
                              [qi::_val = boost::phoenix::bind ( &RasControl::defineUser, this->rascontrol.get(), boost::phoenix::ref ( this->defUser ) )];

    //remove <<user u>>
    this->removeUserSubRule = ( this->userLit[boost::bind ( &RemoveUser::Clear, &this->remUser )] >> this->strRule[boost::bind ( static_cast< void ( RemoveUser::* ) ( const ::std::string & ) > ( &RemoveUser::set_user_name ), &remUser, _1 )] )
                              [qi::_val = boost::phoenix::bind ( &RasControl::removeUser, this->rascontrol.get(), boost::phoenix::ref ( this->remUser ) )];

    //change <<user u [-name n | -passwd p | -rights r]>>
    this->changeUserSubRule = ( ( this->userLit[boost::bind ( &ChangeUser::Clear, &this->changeUser )] >> this->strRule[boost::bind ( static_cast< void ( ChangeUser::* ) ( const ::std::string & ) > ( &ChangeUser::set_user_name ), &changeUser, _1 )] )
                                >> - ( ( this->_nameLit >> this->strRule[boost::bind ( static_cast< void ( ChangeUser::* ) ( const ::std::string & ) > ( &ChangeUser::set_n_name ), &changeUser, _1 )] )
                                       | ( this->_passwdLit >> this->strRule[boost::bind ( static_cast< void ( ChangeUser::* ) ( const ::std::string & ) > ( &ChangeUser::set_n_passwd ), &changeUser, _1 )] )
                                       | ( this->_rightsLit >> ( qi::lexeme[ ( qi::char_ ( 'C' ) [boost::bind ( &ChangeUser::set_n_config_rights, &changeUser, true )]
                                               ^ qi::char_ ( 'A' ) [boost::bind ( &ChangeUser::set_n_access_rights, &changeUser, true )]
                                               ^ qi::char_ ( 'S' ) [boost::bind ( &ChangeUser::set_n_server_admin_rights, &changeUser, true )]
                                               ^ qi::char_ ( 'I' ) [boost::bind ( &ChangeUser::set_n_info_rights, &changeUser, true )]
                                               ^ qi::char_ ( 'R' ) [boost::bind ( &ChangeUser::set_n_dbread_rights, &changeUser, true )]
                                               ^ qi::char_ ( 'W' ) [boost::bind ( &ChangeUser::set_n_dbwrite_rights, &changeUser, true )] )]
                                               | qi::lexeme[qi::char_ ( '-' ) [phx::bind ( &ChangeUser::set_n_config_rights, &changeUser, false ),
                                                       phx::bind ( &ChangeUser::set_n_access_rights, &changeUser, false ),
                                                       phx::bind ( &ChangeUser::set_n_server_admin_rights, &changeUser, false ),
                                                       phx::bind ( &ChangeUser::set_n_info_rights, &changeUser, false ),
                                                       phx::bind ( &ChangeUser::set_n_dbread_rights, &changeUser, false ),
                                                       phx::bind ( &ChangeUser::set_n_dbwrite_rights, &changeUser, false )]] ) ) ) )
                              [qi::_val = boost::phoenix::bind ( &RasControl::changeUser, this->rascontrol.get(), boost::phoenix::ref ( this->changeUser ) )];

    //list <<user [–rights]>>D
    this->listUserSubRule = ( this->userLit[boost::bind ( &ListUser::Clear, &this->listUser )]
                              >> - ( this->_rightsLit[boost::bind ( &ListUser::set_diplay_rights, &listUser, true )] ) )
                            [qi::_val = boost::phoenix::bind ( &RasControl::listUser, this->rascontrol.get(), boost::phoenix::ref ( this->listUser ) )];

    //help <<user>>
    this->helpUserSubRule = this->userLit
                            [qi::_val = boost::phoenix::bind ( &RasControl::helpUser, this->rascontrol.get() )];
    ////-------END:user section -----////

    ////-------BEGIN:inpeer section -----////
    //define <<inpeer hostname>>
    this->defineInpeerSubRule = this->inpeerLit >> this->strRule
                                [qi::_val = boost::phoenix::bind ( &RasControl::defineInpeer, this->rascontrol.get(), qi::_1 )];

    //list <<inpeer>>
    this->listInpeerSubRule = this->inpeerLit
                              [qi::_val = boost::phoenix::bind ( &RasControl::listInpeer, this->rascontrol.get() )];

    //remove <<inpeer hostname>>
    this->removeInpeerSubRule = this->inpeerLit >> this->strRule
                                [qi::_val = boost::phoenix::bind ( &RasControl::removeInpeer, this->rascontrol.get(), qi::_1 )];

    //help <<inpeer>>
    this->helpInpeerSubRule = this->inpeerLit
                              [qi::_val = boost::phoenix::bind ( &RasControl::helpInpeer, this->rascontrol.get() )];
    ////-------END:inpeer section -----////


    ////-------BEGIN:outpeer section -----////
    //define <<outpeer hostname [-port portnumber]>>
    this->defineOutpeerSubRule = ( this->outpeerLit[boost::bind ( &DefineOutpeer::Clear, &this->defOutpeer )] >> this->strRule[boost::phoenix::bind ( static_cast< void ( DefineOutpeer::* ) ( const ::std::string & ) > ( &DefineOutpeer::set_host_name ), &defOutpeer, qi::_1 )]
                                   >> - ( this->_portLit >> qi::int_[boost::phoenix::bind ( &DefineOutpeer::set_port, &defOutpeer, qi::_1 )] ) )
                                 [qi::_val = boost::phoenix::bind ( &RasControl::defineOutpeer, this->rascontrol.get(), boost::phoenix::ref ( defOutpeer ) )];
    //list <<outpeer>>
    this->listOutpeerSubRule = this->outpeerLit
                               [qi::_val = boost::phoenix::bind ( &RasControl::listOutpeer, this->rascontrol.get() )];

    //remove <<outpeer hostname>>
    this->removeOutpeerSubRule = this->outpeerLit >> this->strRule
                                 [qi::_val = boost::phoenix::bind ( &RasControl::removeOutpeer, this->rascontrol.get(), qi::_1 )];

    //help <<outpeer>>
    this->helpOutpeerSubRule = this->outpeerLit
                               [qi::_val = boost::phoenix::bind ( &RasControl::helpOutpeer, this->rascontrol.get() )];

    ////-------END:outpeer section -----////

    //      this->startSrvSubRule =

    this->listVersionSubRule = this->versionLit
                               [qi::_val = boost::phoenix::bind ( &RasControl::listVersion, this->rascontrol.get() )];

    this->upServerSubRule = ( this->upLit >> srvLit[boost::bind ( &StartServerGroup::Clear, &upSrv )]
                              >> ( ( _hostLit >> strRule[boost::bind ( static_cast< void ( StartServerGroup::* ) ( const ::std::string & ) > ( &StartServerGroup::set_host_name ), &upSrv, _1 )] )
                                   | _allLit[boost::bind ( ( &StartServerGroup::set_all ), &upSrv, true )]
                                   | strRule[boost::bind ( static_cast< void ( StartServerGroup::* ) ( const ::std::string & ) > ( &StartServerGroup::set_group_name ), &upSrv, _1 )] ) )
                            [qi::_val = boost::phoenix::bind ( &RasControl::startServerGroup, this->rascontrol.get(), boost::phoenix::ref ( upSrv ) )];

    this->downServerSubRule = ( srvLit[boost::bind ( &StopServerGroup::Clear, &downSrv )]
                                >> ( ( _hostLit >> strRule[boost::bind ( static_cast< void ( StopServerGroup::* ) ( const ::std::string & ) > ( &StopServerGroup::set_host_name ), &downSrv, _1 )] )
                                     | _allLit[boost::bind ( ( &StopServerGroup::set_all ), &downSrv, true )]
                                     | strRule[boost::bind ( static_cast< void ( StopServerGroup::* ) ( const ::std::string & ) > ( &StopServerGroup::set_group_name ), &downSrv, _1 )] )
                                >> - ( qi::lit ( "-force" ) [boost::bind ( ( &StopServerGroup::set_force ), &downSrv, true )] | qi::lit ( "-kill" ) [boost::bind ( ( &StopServerGroup::set_kill ), &downSrv, true )] ) )
                              [qi::_val = boost::phoenix::bind ( &RasControl::stopServerGroup, this->rascontrol.get(), boost::phoenix::ref ( downSrv ) )];

    //This rule is here only for backwards compatibility. It will be removed.
    this->downHostSubRule = ( hostLit >> ( this->_allLit |  this->strRule ) )
                            [qi::_val = boost::phoenix::bind ( &RasControl::stopRasMgr, this->rascontrol.get() )];

    this->downSubRule = this->downLit >> ( this->downServerSubRule | this->downHostSubRule );

    this->defineSubRule = this->defineLit >> ( this->defineHostSubRule
                          | this->defineServerSubRule
                          | this->defineDbhSubRule
                          | this->defineDbSubRule
                          | this->defineUserSubRule
                          | this->defineInpeerSubRule
                          | this->defineOutpeerSubRule
                                             );

    this->changeSubRule = this->changeLit >> ( this->changeHostSubRule
                          | this->changeServerSubRule
                          | this->changeDbhSubRule
                          | this->changeDbSubRule
                          | this->changeUserSubRule
                                             );

    this->removeSubRule = this->removeLit >> ( this->removeHostSubRule
                          | this->removeServerSubRule
                          | this->removeDbhSubRule
                          | this->removeDbSubRule
                          | this->removeUserSubRule
                          | this->removeInpeerSubRule
                          | this->removeOutpeerSubRule
                                             );

    this->listSubRule = this->listLit >> ( this->listHostSubRule
                                           | this->listServerSubRule
                                           | this->listDbhSubRule
                                           | this->listDbSubRule
                                           | this->listUserSubRule
                                           | this->listInpeerSubRule
                                           | this->listOutpeerSubRule
                                           | this->listVersionSubRule
                                         );

    //TODO-AT: Add default help rule
    this->helpSubRule = this->helpLit >> - ( this->helpHostSubRule
                        | this->helpServerSubRule
                        | this->helpDbhSubRule
                        | this->helpDbSubRule
                        | this->helpUserSubRule
                        | this->helpInpeerSubRule
                        | this->helpOutpeerSubRule );

    this->saveSubRule = this->saveLit
                        [qi::_val = boost::phoenix::bind ( &RasControl::save, this->rascontrol.get() )];

    this->exitSubRule = ( this->exitLit | this->byeLit | this->quitLit )
                        [qi::_val = boost::phoenix::bind ( &RasControl::exit, this->rascontrol.get() )];

    this->systemCommandRule = ( this->defineLit | this->changeLit | this->removeLit | this->saveLit ) >> +qi::char_;

    this->userAdminCommandRule = ( this->defineLit | this->changeLit | this->removeLit | this->listLit )
                                 >> this->userLit >> *qi::char_;

    this->serverCommandRule = ( this->upLit | this->downLit ) >> *qi::char_;

    this->infoCommandRule = ( this->listLit ) >> *qi::char_;

    this->loginRule = ( qi::lit ( RasCtrlGrammarConstants::login ) >>*qi::char_ )
                      [qi::_val = boost::phoenix::bind ( &RasControl::login, this->rascontrol.get() )];

    //Used to test if the given command is an exit command.
    this->exitRule = this->exitSubRule >> -this->commentRule;

    // | this->removeSubRule | this->listSubRule
    this->start = ( - ( this->saveSubRule
                        | this->exitSubRule
                        | this->defineSubRule
                        | this->changeSubRule
                        | this->removeSubRule
                        | this->upServerSubRule
                        | this->downSubRule
                        | this->listSubRule
                        | this->loginRule
                        | this->helpSubRule ) [qi::_val=qi::_1]
                    >> ( -this->commentRule ) );
}

bool isSystemConfigCommand ( Iterator begin, Iterator end )
{
    bool r = qi::phrase_parse ( begin, end, this->systemCommandRule, boost::spirit::ascii::space );

    return ( r && begin==end );
}

bool isInfoCommand ( Iterator begin, Iterator end )
{
    bool r = qi::phrase_parse ( begin, end, this->infoCommandRule, boost::spirit::ascii::space );

    return ( r && begin==end );
}

bool isServerAdminCommand ( Iterator begin, Iterator end )
{
    bool r = qi::phrase_parse ( begin, end, this->serverCommandRule, boost::spirit::ascii::space );

    return ( r && begin==end );
}

bool isUserAdminCommand ( Iterator begin, Iterator end )
{
    bool r = qi::phrase_parse ( begin, end, this->userAdminCommandRule, boost::spirit::ascii::space );

    return ( r && begin==end );
}

bool isLoginCommand ( Iterator begin, Iterator end )
{
    bool r = qi::phrase_parse ( begin, end, this->loginRule, boost::spirit::ascii::space );

    return ( r && begin==end );
}

protected:

DefineUser defUser;
/*!< Message used to define a new user*/
RemoveUser remUser;
/*!< Message used to remove a user*/
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

boost::shared_ptr<RasControl> rascontrol;
boost::function<std::string ( void ) > resultFunction;

qi::rule<Iterator, std::string(), ascii::space_type> hostNameRule;
qi::rule<Iterator, std::string(), ascii::space_type> serverCommandRule;
qi::rule<Iterator, std::string(), ascii::space_type> infoCommandRule;
qi::rule<Iterator, std::string(), ascii::space_type> userAdminCommandRule;
qi::rule<Iterator, std::string(), ascii::space_type> systemCommandRule;

qi::rule<Iterator, std::string(), ascii::space_type> loginRule;
qi::rule<Iterator, std::string(), ascii::space_type> downSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> downHostSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> upServerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> downServerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> startSrvSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> stopSrvSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> _aliveLit;
qi::rule<Iterator, std::string(), ascii::space_type> _availableLit;
qi::rule<Iterator, std::string(), ascii::space_type> upLit;
qi::rule<Iterator, std::string(), ascii::space_type> downLit;
qi::rule<Iterator, std::string(), ascii::space_type> _idleLit;
qi::rule<Iterator, std::string(), ascii::space_type> _sizeLit;
qi::rule<Iterator, std::string(), ascii::space_type> exitRule;
qi::rule<Iterator, std::string(), ascii::space_type> portRule;
qi::rule<Iterator, std::string(), ascii::space_type> byeLit;
qi::rule<Iterator, std::string(), ascii::space_type> quitLit;
qi::rule<Iterator, std::string(), ascii::space_type> helpUserSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> helpInpeerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> helpOutpeerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> helpLit;
qi::rule<Iterator, std::string(), ascii::space_type> helpSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> helpDbhSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> helpDbSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> helpHostSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> helpServerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> listVersionSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> versionLit;
qi::rule<Iterator, std::string(), ascii::space_type> saveLit;
qi::rule<Iterator, std::string(), ascii::space_type> saveSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> exitLit;
qi::rule<Iterator, std::string(), ascii::space_type> exitSubRule;

qi::rule<Iterator, std::string(), ascii::space_type> listInpeerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> removeInpeerSubRule;

qi::rule<Iterator, std::string(), ascii::space_type> listOutpeerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> removeOutpeerSubRule;

qi::rule<Iterator, std::string(), ascii::space_type> start;
qi::rule<Iterator, std::string(), ascii::space_type> defineLit;
qi::rule<Iterator, std::string(), ascii::space_type> hostLit;
qi::rule<Iterator, std::string(), ascii::space_type> _hostLit;
qi::rule<Iterator, std::string(), ascii::space_type> _typeLit;
qi::rule<Iterator, std::string(), ascii::space_type> _pLit;
qi::rule<Iterator, std::string(), ascii::space_type> _portLit;
qi::rule<Iterator, std::string(), ascii::space_type> srvLit;
qi::rule<Iterator, std::string(), ascii::space_type> _networkLit;
qi::rule<Iterator, std::string(), ascii::space_type> _dbhLit;
qi::rule<Iterator, std::string(), ascii::space_type> dbhLit;
qi::rule<Iterator, std::string(), ascii::space_type> dbLit;
qi::rule<Iterator, std::string(), ascii::space_type> _connectLit;
qi::rule<Iterator, std::string(), ascii::space_type> _userLit;
qi::rule<Iterator, std::string(), ascii::space_type> userLit;
qi::rule<Iterator, std::string(), ascii::space_type> _passwdLit;
qi::rule<Iterator, std::string(), ascii::space_type> _rightsLit;
qi::rule<Iterator, std::string(), ascii::space_type> inpeerLit;
qi::rule<Iterator, std::string(), ascii::space_type> outpeerLit;
qi::rule<Iterator, std::string(), ascii::space_type> _nameLit;
qi::rule<Iterator, std::string(), ascii::space_type> changeLit;
qi::rule<Iterator, std::string(), ascii::space_type> removeLit;
qi::rule<Iterator, std::string(), ascii::space_type> listLit;
qi::rule<Iterator, std::string(), ascii::space_type> _allLit;
qi::rule<Iterator, std::string(), ascii::space_type> _useLocalHostLit;
qi::rule<Iterator, std::string(), ascii::space_type> onLit;
qi::rule<Iterator, std::string(), ascii::space_type> offLit;
qi::rule<Iterator, std::string(), ascii::space_type> _autorestartLit;
qi::rule<Iterator, std::string(), ascii::space_type> _countdownLit;
qi::rule<Iterator, std::string(), ascii::space_type> _xpLit;
qi::rule<Iterator, std::string(), ascii::space_type> defineServerPorts;
qi::rule<Iterator, std::string(), ascii::space_type> defineServerOptionsSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> defineServerAuxSubRule;

qi::rule<Iterator, std::string(), ascii::space_type> commentRule;

qi::rule<Iterator, std::string(), ascii::space_type> defineInpeerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> defineOutpeerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> defineHostSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> defineServerSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> defineDbhSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> defineDbSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> defineUserSubRule;

qi::rule<Iterator, std::string(), ascii::space_type> changeHostSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> changeDbhSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> changeUserSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> changeDbSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> changeServerSubRule;

qi::rule<Iterator, std::string(), ascii::space_type> removeHostSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> removeUserSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> removeDbhSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> removeDbSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> removeServerSubRule;


qi::rule<Iterator, std::string(), ascii::space_type> listDbhSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> listHostSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> listUserSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> listDbSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> listServerSubRule;

qi::rule<Iterator, std::string(), ascii::space_type> defineSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> removeSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> changeSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> listSubRule;
qi::rule<Iterator, std::string(), ascii::space_type> strRule;

private:
//TODO-AT: Rewrite this so that you don't have two methods

bool processDefPortRange ( unsigned int begin, unsigned int end )
{
    std::vector<unsigned int> ports;
    bool result = this->processPortRange ( begin, end, ports );

    if ( result )
    {
        for ( size_t i = 0; i < ports.size(); i++ )
        {
            this->defServerGroup.add_ports ( ports[i] );
        }
    }
    return result;
}

bool processChPortRange ( unsigned int begin, unsigned int end )
{
    std::vector<unsigned int> ports;
    bool result = this->processPortRange ( begin, end, ports );

    if ( result )
    {
        for ( size_t i = 0; i < ports.size(); i++ )
        {
            this->changeServerGroup.add_n_ports ( ports[i] );
        }
    }

    return result;
}

bool processPortRange ( unsigned int begin, unsigned int end, std::vector<unsigned int> &ports )
{
    //TODO-AT: Should I check for valid port ranges here?
    if ( begin > end )
    {
        return false;
    }
    else
    {
        for ( unsigned int i = begin; i <= end; i++ )
        {
            ports.push_back ( i );
        }

        return true;
    }
}

};

}


#endif // RASCONTROLGRAMMAR_HH
