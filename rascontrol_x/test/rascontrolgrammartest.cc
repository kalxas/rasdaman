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

//TODO-AT: This test should be moved to rasmgr and modified to work.
#include "common/src/logging/easylogging++.hh"
#include "common/src/unittest/gtest.h"

#include "rascontrol_x/src/controlrasmgrrasnet.hh"

namespace qi    = boost::spirit::qi;
namespace ascii = boost::spirit::ascii;
namespace phx   = boost::phoenix;

typedef std::string::const_iterator iterator_type;
typedef rascontrol::RasControlGrammar<iterator_type> r_grammar;

class RasControlGrammarTest: public ::testing::Test
{
public:
    RasControlGrammarTest():grammar(dummy)
    {

    }
protected:
    void SetUp()
    {

    }
    void TearDown()
    {

    }

    boost::shared_ptr<rascontrol::ControlRasMgrComm> dummy;
    r_grammar grammar;

};

TEST_F(RasControlGrammarTest, defineLitTest)
{
    std::string input = "define";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.defineLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeLitTest)
{
    std::string input = "change";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, removeLitTest)
{
    std::string input = "remove";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.removeLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, listLitTest)
{
    std::string input = "list";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.listLit, ascii::space);

    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, hostLitTest)
{
    std::string input = "host";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.hostLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, onLitTest)
{
    std::string input = "on";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.onLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, offLitTest)
{
    std::string input = "off";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.offLit, ascii::space);

    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, _autorestartLitTest)
{
    std::string input = "-autorestart";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._autorestartLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _xpLitTest)
{
    std::string input = "-xp";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._xpLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _useLocalHostLitTest)
{
    std::string input = "-uselocalhost";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._useLocalHostLit, ascii::space);

    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, _netLitTest)
{
    std::string input = "-net";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._networkLit, ascii::space);

    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, _portLitTest)
{
    std::string input = "-port";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._portLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, defineHostSubRuleTest)
{
    std::string input = "host h -net n -port 2021";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.defineHostSubRule, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _nameLitTest)
{
    std::string input = "-name";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._nameLit, ascii::space);

    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, _typeLitTest)
{
    std::string input = "-type";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._typeLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _userLitTest)
{
    std::string input = "-user";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._userLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, dbhLitTest)
{
    std::string input = "dbh";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.dbhLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _connectLitTest)
{
    std::string input = "-connect";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._connectLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _passwdLitTest)
{
    std::string input = "-passwd";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._passwdLit, ascii::space);

    ASSERT_EQ(first, last);
}



TEST_F(RasControlGrammarTest, changeHostSubRuleTestFull)
{
    std::string input = "host hostName -name myName -net myNetwork -port 2303";

    iterator_type first = input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeHostSubRule, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeHostSubRuleTestWOName)
{
    std::string input = "host hostName -net myNetwork -port 2303";

    iterator_type first = input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeHostSubRule, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeHostSubRuleTestWOPort)
{
    std::string input = "host hostName -name myName -net myNetwork";

    iterator_type first = input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeHostSubRule, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeHostSubRuleTestWONetwork)
{
    std::string input = "host hostName -name myName -port 2303";

    iterator_type first = input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeHostSubRule, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeHostSubRuleTestBasic)
{
    std::string input = "host hostName";

    iterator_type first = input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeHostSubRule, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, removeHostSubRuleTest)
{
    std::string input = "host hostName";

    iterator_type first = input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.removeHostSubRule, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, listHostSubRuleTest)
{
    std::string input = "host";

    iterator_type first = input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.listHostSubRule, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, srvLitTest)
{
    std::string input = "srv";

    iterator_type first = input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.srvLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _dbhLitTest)
{
    std::string input = "-dbh";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._dbhLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _countdownLitTest)
{
    std::string input = "-countdown";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._countdownLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, _hostLitTest)
{
    std::string input = "-host";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar._hostLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, dbLitTest)
{
    std::string input = "db";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.dbLit, ascii::space);

    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, defineServerSubRuleTest)
{
    //srv s -host h -type t -port p -dbh d
    //[-autorestart [on|off] [-countdown c]
    //[-xp options]
    std::string input = "srv serverName -host hostName -type r -port 2034 -dbh databaseHost -autorestart on -countdown 1000 -xp something";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.defineServerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeServerSubRuleTest)
{
    //srv s -host h -type t -port p -dbh d
    //[-autorestart [on|off] [-countdown c]
    //[-xp options]
    std::string input = "srv s -name n -port 2045 -dbh d -autorestart off -countdown 1020 -xp options";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeServerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, removeServerSubRuleTest)
{
    //srv s
    std::string input = "srv s";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.removeServerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, listServerSubRuleTest)
{
    //srv s
    std::string input = "srv -all -p";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.listServerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, defineDbhSubRule1Test)
{
    //dbh h -connect c [-user u -passwd p]
    std::string input = "dbh h -connect c ";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.defineDbhSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, defineDbhSubRule2Test)
{
    //dbh h -connect c [-user u -passwd p]
    std::string input = "dbh h -connect c -user user -passwd password";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.defineDbhSubRule, ascii::space);
    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, changeDbhSubRule1Test)
{
    //dbh h [-name n] [-connect c]
    std::string input = "dbh h -name n -connect c";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeDbhSubRule, ascii::space);
    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, changeDbhSubRule2Test)
{
    //dbh h [-name n] [-connect c]
    std::string input = "dbh h -name n";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeDbhSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeDbhSubRule3Test)
{
    //dbh h [-name n] [-connect c]
    std::string input = "dbh h";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeDbhSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeDbhSubRule4Test)
{
    //dbh h [-name n] [-connect c]
    std::string input = "dbh h -user user -passwd password";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeDbhSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, removeDbhSubRuleTest)
{
    //dbh h [-name n] [-connect c]
    std::string input = "dbh h";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.removeDbhSubRule, ascii::space);
    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, listDbhSubRuleTest)
{
    //dbh h [-name n] [-connect c]
    std::string input = "dbh";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.listDbhSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, defineDbSubRuleTest)
{
    //db d –dbh db
    std::string input = "db d -dbh db";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.defineDbSubRule, ascii::space);
    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, changeDbSubRuleTest)
{
    //db d –dbh db
    std::string input = "db d -name n";

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.changeDbSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, removeDbSubRuleTest)
{
    //db d –dbh db
    std::string input = "db d -dbh db";//–dbh db

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.removeDbSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, listDbSubRuleTest)
{
    //db [ d | -dbh h | -all ]
    std::string input ;
    iterator_type first, last;

    input = "db -all";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.listDbSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "db testName";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.listDbSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "db -dbh test";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.listDbSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, defineUserSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "user u -passwd p -rights R";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.defineUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "user u -passwd p";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.defineUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "user u -rights R";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.defineUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "user u -rights R -passwd p";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.defineUserSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, removeUserSubRuleTest)
{
    std::string input = "user u";//–dbh db

    iterator_type first=input.begin();
    iterator_type last = input.end();

    qi::phrase_parse(first, last, grammar.removeUserSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, changeUserSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "user u";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.changeUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "user u -name testName";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.changeUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "user u -passwd testPassword";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.changeUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "user u -rights R";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.changeUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "user u -rights R -name testName";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.changeUserSubRule, ascii::space);
    ASSERT_NE(first, last);
}

TEST_F(RasControlGrammarTest, listUserSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "user u";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.listUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "user u -rights";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.listUserSubRule, ascii::space);
    ASSERT_EQ(first, last);

    //Fail
    input = "user -rights";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.listUserSubRule, ascii::space);
    ASSERT_NE(first, last);

}


TEST_F(RasControlGrammarTest, defineInpeerSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "inpeer host";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.defineInpeerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, defineOutpeerSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "outpeer host";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.defineOutpeerSubRule, ascii::space);
    ASSERT_EQ(first, last);

    input = "outpeer host -port 2013";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.defineOutpeerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, listInpeerSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "inpeer";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.listInpeerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, listOutpeerSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "outpeer";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.listOutpeerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, removeInpeerSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "inpeer hostName";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.removeInpeerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, removeOutpeerSubRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "outpeer hostName";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.removeOutpeerSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, exitRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "exit";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.exitSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, exitLitTest)
{
    std::string input ;
    iterator_type first, last;

    input = "exit";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.exitLit, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, saveRuleTest)
{
    std::string input ;
    iterator_type first, last;

    input = "save";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.saveSubRule, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, saveLitTest)
{
    std::string input ;
    iterator_type first, last;

    input = "save";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.saveLit, ascii::space);
    ASSERT_EQ(first, last);
}

TEST_F(RasControlGrammarTest, startSaveRule)
{
    std::string input ;
    iterator_type first, last;

    input = "save";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.start, ascii::space);
    ASSERT_EQ(first, last);

    input = "save #something";
    first=input.begin();
    last = input.end();

    qi::phrase_parse(first, last, grammar.start, ascii::space);
    ASSERT_EQ(first, last);
}


TEST_F(RasControlGrammarTest, fullTest)
{
    std::string input ;
    iterator_type first, last;
    std::vector<std::string> commands;

    commands.push_back("exit");

    commands.push_back("save");

    commands.push_back("list version");

    commands.push_back("help");
    commands.push_back("help host");
    commands.push_back("help dbh");
    commands.push_back("help db");
    commands.push_back("help srv");
    commands.push_back("help user");
    commands.push_back("help inpeer");
    commands.push_back("help outpeer");

    commands.push_back("remove inpeer testAddress");
    commands.push_back("remove outpeer testAddress");
    commands.push_back("remove user userName");
    commands.push_back("remove db d -dbh db");
    commands.push_back("remove dbh h");
    commands.push_back("remove srv s");
    commands.push_back("remove host h");

    commands.push_back("list inpeer");
    commands.push_back("list outpeer");
    commands.push_back("list user usr");
    commands.push_back("list db -all");
    commands.push_back("list dbh");
    commands.push_back("list srv -all");
    commands.push_back("list host");

    for (int i=0; i<commands.size(); i++)
    {
        first=commands[i].begin();
        last = commands[i].end();

        qi::phrase_parse(first, last, grammar, ascii::space);

        ASSERT_EQ(first, last)<<commands[i];
    }
}
