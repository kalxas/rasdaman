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

#include "clientcomm.hh"
#include "rasodmg/database.hh"
#include "rasodmg/transaction.hh"

void ClientComm::setTransaction(r_Transaction *transaction1)
{
    this->transaction = transaction1;
}

void ClientComm::setDatabase(r_Database *database1)
{
    this->database = database1;
}

void ClientComm::updateTransaction()
{
    if (!transaction)
        transaction = r_Transaction::actual_transaction;
    if (!database && transaction)
        database = transaction->getDatabase();
    if (!database)
        database = r_Database::actual_database;
}
