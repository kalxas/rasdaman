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
/***
 * INCLUDE: httpserver.h
 *
 * MODULE:  httpserver
 * CLASS:   none, C header
 *
 * COMMENTS:
 *
*/

#ifndef _HTTPSERVERC_
#define _HTTPSERVERC_

#include "servercomm/httpserver.hh"

//function prototypes:
void getMDDs(int binDataSize, char *binData, int Endianess, vector<HttpServer::MDDEncoding*> &resultVector);
int encodeAckn(char*&result, int ackCode);
void cleanExecuteQueryRes(ExecuteQueryRes& res);
int encodeError(char*&result, const r_ULong  errorNo, const r_ULong lineNo,const r_ULong columnNo, const char *text);

extern int init_httpserver( int argc, char *argv[] );

#endif
