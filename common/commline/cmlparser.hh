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
/**
 * INCLUDE: cmlparser.hh
 *
 * MODULE:  command line interpreter
 *
 * PURPOSE:
 *      Tool for interpreting command line arguments
 *
 * COMMENTS:
 *          None
*/

/**
*   @defgroup Commline Commline
*
*   @file cmlparser.hh
*
*   @ingroup Commline
*/

#ifndef AK_CMLPARSER_HH
#define AK_CMLPARSER_HH



#include <list>
#include <string>
#include <exception>
#include <iostream>

// Command Line Parser version
extern const char *CommandLineParserVersion;

char *dupString(const char *cc);

// specific errors thrown by the parser
class CmlException : public std::exception
{
public:
    explicit CmlException(const std::string &whatString);
    virtual ~CmlException() noexcept;
    virtual const char *what() const noexcept;

protected:
    std::string  problem;
};


class CommandLineParameter
{
public:

    static const char *defaultTitle;
    static const char *descSep;
    static const char *descTab;
    static const char *descIndent;
    static const char *descLineSep;
    static const char  descOpen;
    static const char  descClose;
    static const char *descLeftDefault;
    static const char *descRightDefault;

    virtual ~CommandLineParameter();

    //interface for Parser
    void setDescription(const char *);

    bool doesMatch(char c);
    bool doesMatch(const char *s);

    char         getShortName() const;
    const char  *getLongName() const;

    virtual bool setPresent(char c) = 0;
    virtual bool setPresent(const char *s) = 0;

    virtual bool needsValue() = 0;
    virtual bool takeValue(const char *s) = 0;
    virtual void popValue() = 0;

    void virtual reset();
    const char *calledName();

    // has a (at least one) value been assigned?
    virtual bool isPresent() = 0;

    virtual const char *getValueAsString() = 0;
    virtual long        getValueAsLong() = 0;
    virtual int         getValueAsInt() = 0;
    virtual double      getValueAsDouble() = 0;

    virtual std::ostream &printStatus(std::ostream & = std::cout) = 0;
    std::ostream &printHelp(std::ostream & = std::cout);

protected:
    CommandLineParameter(char newShortName, const char *newLongName, const char *newDefaultValue);
    CommandLineParameter(char newShortName, const char *newLongName, long newDefaultValue);

protected:

    char  shortName;
    char *longName;
    bool  present;
    bool  wasLongName;

    char *defaultValue;
    char  shNameString[2];

    char *descriptionText;
    char *paramDescription;
};


class FlagParameter: public CommandLineParameter
{
public:
    FlagParameter(char nShortName, const char *nLongName);
    ~FlagParameter() override = default;

    bool setPresent(char c) override;
    bool setPresent(const char *s) override;
    bool isPresent() override;

    bool needsValue() override;
    bool takeValue(const char *s) override;
    void popValue() override;

    const char *getValueAsString() override;
    long        getValueAsLong() override;
    int         getValueAsInt() override;
    double      getValueAsDouble() override;

    std::ostream &printStatus(std::ostream & = std::cout) override;
};

class StringParameter: public CommandLineParameter
{
private:
    std::list<char *> value;

public:
    StringParameter(char nShortName, const char *nLongName, const char *newDefaultValue = NULL);
    StringParameter(char nShortName, const char *nLongName, long newDefaultValue = 0L);
    ~StringParameter() override;

    bool setPresent(char c) override;
    bool setPresent(const char *s) override;
    bool isPresent() override;

    bool needsValue() override;
    bool takeValue(const char *s) override;
    void popValue() override;

    const char *getValueAsString() override;
    long        getValueAsLong() override;
    int         getValueAsInt() override;
    double      getValueAsDouble() override;

    void reset() override;

    std::ostream &printStatus(std::ostream & = std::cout) override;
};

class DeprecatedParameter: public CommandLineParameter
{
private:
    std::list<char *> value;
    bool isFlag{false};

public:
    DeprecatedParameter(char nShortName, const char *nLongName, bool flag = false);
    ~DeprecatedParameter() override = default;
    
    bool setPresent(char c) override;
    bool setPresent(const char *s) override;
    bool isPresent() override;
    
    bool needsValue() override;
    bool takeValue(const char *s) override;
    void popValue() override;
    
    const char *getValueAsString() override;
    long        getValueAsLong() override;
    int         getValueAsInt() override;
    double      getValueAsDouble() override;

    void reset() override;
    
    std::ostream &printStatus(std::ostream & = std::cout) override;
};


class CommandLineParser
{
public:
    static const char noShortName;
    static const char *noLongName;
    static const char *ShortSign;
    static const char *LongSign;


    static CommandLineParser &getInstance();

    ~CommandLineParser();

    /*
    These functions take a parameter called description. This is a string used in printHelp
    The format of this string has to be:
        <name of parameter> line1\n\t\tline2...\n\t\tlineN
        brackets<> and space after are mandatory if there is a parameter!
        Otherwise no <>!
    */
    CommandLineParameter &addFlagParameter(char shortName, const char *longName, const char *description);
    CommandLineParameter &addStringParameter(char shortName, const char *longName,  const char *description, const char *newDefaultValue = NULL);
    CommandLineParameter &addLongParameter(char shortName, const char *longName,  const char *description, long newDefaultValue = 0L);
    CommandLineParameter &addDeprecatedParameter(char shortName, const char *longName, bool flag = false);

    bool isPresent(char shortName);
    bool isPresent(const char *longName);

    const char *getValueAsString(char shortName);
    long        getValueAsLong(char shortName);
    double      getValueAsDouble(char shortName);

    const char *getValueAsString(const char *longName);
    long        getValueAsLong(const char *longName);
    double      getValueAsDouble(const char *longName);

    void processCommandLine(int argc, char **argv);

    bool testProcessCommandLine(const char *test_cml);

    void printHelp();
    void printStatus();

private:
    static CommandLineParser *myself;

    std::list<CommandLineParameter *> cmlParameter;

    CommandLineParameter *lastParameter;
    bool nextTokenIsValue;

    CommandLineParser();

    CommandLineParameter &getParameter(char shortName);
    CommandLineParameter &getParameter(const char *longName);

    void setValue(const char *value);

    void longNameParameter(const char *nextToken);

    void shortNameParameter(const char *nextToken);
};

#endif
