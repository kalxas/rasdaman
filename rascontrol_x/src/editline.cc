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

#include <cstdio>
#include <cstring>
#include <iostream>
#include <cstdlib>
#include <editline/readline.h>

#include "editline.hh"

namespace rascontrol
{
EditLine::EditLine()
{
    line[0] = EOS_CHAR;

#ifdef READLINELIB
    using_history();
    rl_bind_key('\t', rl_insert);
#endif
}
EditLine::~EditLine()
{
}

const char* EditLine::interactiveCommand(const char* prompt)
{
#ifdef READLINELIB
    char* rasp = rl_gets(prompt);

#else
    std::cout << prompt << std::flush;
    char* rasp = fgets(line, MAXMSG - 1, stdin);

#endif

    if (rasp == 0)
    {
        return 0;
    }
    strcpy(line, rasp);
    return line;

}

const char* EditLine::fromStdinCommand(const char* prompt)
{
    if (prompt)
    {
        std::cout << prompt << std::flush;
    }
    char* rasp = fgets(line, MAXMSG - 1, stdin);

    if (rasp == 0)
    {
        return 0;
    }

    int i;
    for (i = 0; line[i]; i++)
    {
        if (line[i] == '\r' || line[i] == '\n')
        {
            line[i] = 0;
            break;
        }
    }

    for (i = 0; line[i]; i++)
    {
        if (line[i] == ' ' || line[i] == '\t')
        {
            continue;
        }
        break;
    }
    return line + i;
}

char* EditLine::rl_gets(const char* prompt)
{
#ifdef READLINELIB
    static char* line_read = (char*)NULL;

    /* If the buffer has already been allocated, return the memory
    to the free pool. */

    if (line_read)
    {
        free(line_read);
        line_read = (char*)NULL;
    }
    char* line = strdup(prompt);

    /* Get a line from the user. */
    line_read = readline(line);

    free(line);

    /* If the line has any text in it, save it on the history. */
    if (line_read && *line_read)
    {
        add_history(line_read);
    }

    return (line_read);
#else
    return strdup(" ");
#endif

}

}
