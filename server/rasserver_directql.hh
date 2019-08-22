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

#ifndef RASSERVER_DIRECTQL_HH
#define RASSERVER_DIRECTQL_HH

#include <string>

class QtData;
class Tile;
class r_Marray_Type;
struct ExecuteQueryRes;
struct ExecuteUpdateRes;

namespace rasserver
{
namespace directql
{

void openDatabase();
void openTransaction(bool readwrite);

void closeDatabase();
void closeTransaction(bool doCommit);

void doStuff();
r_Marray_Type* getTypeFromDatabase(const char* mddTypeName);
void freeResult(ExecuteQueryRes* result);

void printScalar(char* buffer, QtData* data, unsigned int resultIndex);
void printResult(Tile* tile, int resultIndex);
void printOutput(unsigned short status, ExecuteQueryRes* result);
void printError(unsigned short status, ExecuteQueryRes* result);
void printError(unsigned short status, ExecuteUpdateRes* result);

std::string getDefaultDb();

} // directql
} // rasserver

#endif // RASSERVER_DIRECTQL_HH
