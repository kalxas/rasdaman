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
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef TURBOQUERYRESULT_H
#define TURBOQUERYRESULT_H

/*************************************************************
*
 * Copyright (C) 2003 Dr. Peter Baumann
*
 * INCLUDE: turboqueryresult.hh
 *
 * MODULE:  raslib
 * CLASS:   TurboQueryResult
 *
 * CHANGE HISTORY (append further entries):
 * when         who         what
 * ----------------------------------------------------------
 *
 * COMMENTS:
 *          The class wraps the result of a query executed through  Servercomm::executeTurboQuery()
*/

#include <stdlib.h>
#include <string>
#include "raslib/mddtypes.hh"

class TurboQueryResult
{
public:
    TurboQueryResult(char *rawData, size_t rawDataSize, r_Data_Format data_format = r_Array, std::string domain = "");
    ~TurboQueryResult();

    std::string getDomain();

    char *getRawData();

    r_Data_Format getDataFormat();

    size_t getRawDataSize() const;

private:
    char *rawData;
    size_t rawDataSize;
    std::string domain;
    r_Data_Format data_format;

};

#endif // TURBOQUERYRESULT_H
