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
 * INCLUDE: parseparams.cc
 *
 * MODULE:  raslib
 * CLASS:   r_Parse_Params
 *
 * COMMENTS:
 *
*/

#include "raslib/parseparams.hh"
#include "mymalloc/mymalloc.h"
#include <logging.hh>

#include <cstdio>
#include <cstdlib>
#include <cctype>
#include <cstring>
#include <cerrno>
#include <string>

const unsigned int r_Parse_Params::granularity = 4;

r_Parse_Params::r_Parse_Params(unsigned int num)
    : maxnum{num}
{
    params = static_cast<parse_params_t *>(mymalloc(maxnum * sizeof(parse_params_t)));
}

r_Parse_Params::~r_Parse_Params(void)
{
    if (params != NULL)
    {
        free(params);
        params = NULL;
    }
}

int r_Parse_Params::add(const char *key, void *store, parse_param_type type)
{
    if (number >= maxnum)
    {
        maxnum += granularity;
        if (params == NULL)
            params = static_cast<parse_params_t *>(mymalloc(maxnum * sizeof(parse_params_t)));
        else
            params = static_cast<parse_params_t *>(realloc(params, maxnum * sizeof(parse_params_t)));

        if (params == NULL)
        {
            maxnum = 0;
            return -1;
        }
    }
    params[number].key = key;
    params[number].store = store;
    params[number].type = type;
    number++;

    return 0;
}
int r_Parse_Params::add(const std::string &key, void *store, parse_param_type type)
{
    return add(key.c_str(), store, type);
}

int r_Parse_Params::process(const char *str) const
{
    return this->process(str, ',', false);
}
int r_Parse_Params::process(const char *str, char separator, bool withWhiteSpaces) const
{
    static const int lenBuff = 256;
    static char buff[lenBuff];
    int numkeys = 0;
    const char *b = str;

    LTRACE << "process('" << (str ? str : "NULL") << ")";

    if (number == 0 || str == NULL || !strcmp(str, ""))
        return 0;

    while (*b != '\0')
    {
        while (isspace(static_cast<int>(*b)) || *b == separator)
            b++;
        if (*b == '\0')
            break;

        if (!isalpha(static_cast<int>(*b)))
        {
            LERROR << "the string must start with alphabetic character";
            return -1;
        }
        const char *key = b;
        while (isalnum(static_cast<int>(*b)))
            b++;

        // store current item
        memset(buff, 0, lenBuff);
        memcpy(buff, key, static_cast<size_t>(b - key));
        unsigned int knum;
        for (knum = 0; knum < number; knum++)
            if (strcmp(buff, params[knum].key) == 0)
                break;

        // we actually understand this key
        if (knum < number)
        {
            int statval = 0;  // status: -1 error, 0 not found, 1 OK

            while (isspace(static_cast<int>(*b)))
                b++;

            if (*b == '=')
            {
                b++;
                while (isspace(static_cast<int>(*b)))
                    b++;

                if (*b != separator && *b != '\0')
                {
                    const char *aux = b;

                    switch (params[knum].type)
                    {
                    case param_type_int:
                    {
                        errno = 0;
                        long val = strtol(b, const_cast<char **>(&aux), 10);
                        if (b == aux || errno)
                        {
                            statval = -1;
                        }
                        else
                        {
                            *static_cast<int *>(params[knum].store) = static_cast<int>(val);
                            b = aux;
                            statval = 1;
                        }
                    }
                    break;
                    case param_type_double:
                    {
                        errno = 0;
                        double val = strtod(b, const_cast<char **>(&aux));
                        if (b == aux || errno)
                        {
                            statval = -1;
                        }
                        else
                        {
                            *static_cast<double *>(params[knum].store) = val;
                            b = aux;
                            statval = 1;
                        }
                    }
                    break;
                    case param_type_string:
                    {
                        long vlen = 0;

                        if (*b == '\"')
                        {
                            aux = ++b;
                            while (*b != '\"' && *b != '\0')
                                b++;

                            if (*b == '\0')
                            {
                                statval = -1;
                            }
                            else
                            {
                                vlen = b - aux;
                                b++;
                                statval = 1;
                            }
                        }
                        else
                        {
                            aux = b;
                            if (withWhiteSpaces == false)
                            {
                                while (!isspace(static_cast<int>(*b)) && (*b != '\0') && (*b != separator))
                                    b++;
                            }
                            else
                            {
                                while (*b != '\0' && *b != separator)
                                    b++;
                            }
                            vlen = b - aux;
                            statval = 1;
                        }
                        if (vlen > 0)
                        {
                            char **vptr = static_cast<char **>(params[knum].store);
                            if (*vptr != NULL)
                                delete[] * vptr;

                            *vptr = new char[vlen + 1];
                            strncpy(*vptr, aux, static_cast<size_t>(vlen));
                            (*vptr)[vlen] = '\0';
                        }
                    }
                    break;
                    default:
                        break;
                    }
                }
            }
            switch (statval)
            {
            case -1:
                LERROR << "error parsing value for keyword " << params[knum].key;
                return -1;
            case 0:
                LERROR << "keyword " << params[knum].key << " without value";
                return -1;
            case 1:
                numkeys++;
                break;
            default:
                break;
            }
        }
        int inquotes = 0;
        while ((*b != separator || inquotes != 0) && (*b != '\0'))
        {
            if (*b == '\"')
                inquotes ^= 1;
            b++;
        }
        if (inquotes != 0)
        {
            LERROR << "unterminated string";
            return -1;
        }
    }

    return numkeys;
}

std::ostream &operator<<(std::ostream &s, const r_Parse_Params::parse_param_type &d)
{
    switch (d)
    {
    case r_Parse_Params::param_type_int: s << "param_type_int"; break;
    case r_Parse_Params::param_type_double: s << "param_type_double"; break;
    case r_Parse_Params::param_type_string: s << "param_type_string"; break;
    default: s << "UNKNOWN r_Parse_Params::parse_paramt_type" << d; break;
    }
    return s;
}
