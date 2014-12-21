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

#include <unistd.h>

#include "../../../../common/src/unittest/gtest.h"
#include "../../../../common/src/logging/easylogging++.hh"

#include "../../../src/sockets/server/clientpool.hh"

using namespace rasnet;

TEST(ClientPool, Constructor)
{
    ClientPool* pool;
    ASSERT_NO_THROW(pool=new ClientPool());
    ASSERT_NO_THROW(delete pool);
}

TEST(ClientPool, addClient)
{
    ClientPool pool;
    std::string clientId="test";

    ASSERT_NO_THROW(pool.addClient(clientId, 1,1));

    ASSERT_TRUE(pool.isClientAlive(clientId));

    //Sleep so that the client is declared dead.
    usleep(1000);
    pool.removeDeadClients();

    ASSERT_FALSE(pool.isClientAlive(clientId));
}

TEST(ClientPool, getMinimumPollPeriod)
{
    ClientPool pool;
    std::string clientId="test";

    ASSERT_EQ(pool.getMinimumPollPeriod(), -1);

    pool.addClient(clientId, 1,1);

    ASSERT_EQ(pool.getMinimumPollPeriod(), 1);
}

TEST(ClientPool, resetClientStatus)
{
    ClientPool pool;
    std::string clientId="test";

    ASSERT_NO_THROW(pool.addClient(clientId, 1,2));

    ASSERT_TRUE(pool.isClientAlive(clientId));

    //Sleep so that the client's life counter decreases by one.
    usleep(1000);
    pool.removeDeadClients();
    ASSERT_TRUE(pool.isClientAlive(clientId));

    pool.resetClientStatus(clientId);

    usleep(1000);
    pool.removeDeadClients();
    ASSERT_TRUE(pool.isClientAlive(clientId));

    usleep(1000);
    pool.removeDeadClients();
    ASSERT_FALSE(pool.isClientAlive(clientId));
}

TEST(ClientPool, removeClient)
{
    ClientPool pool;
    std::string clientId="test";

    ASSERT_NO_THROW(pool.addClient(clientId, 1,2));

    pool.removeClient(clientId);

    ASSERT_FALSE(pool.isClientAlive(clientId));
}

TEST(ClientPool, removeDeadClients)
{
    ClientPool pool;
    std::string clientId="test";

    pool.addClient(clientId, 1,1);
    usleep(1000);

    pool.removeDeadClients();
    ASSERT_FALSE(pool.isClientAlive(clientId));
}

TEST(ClientPool, isClientAlive)
{
    ClientPool pool;
    std::string clientId="test";

    pool.addClient(clientId, 1,1);

    ASSERT_TRUE(pool.isClientAlive(clientId));

    usleep(1000);
    pool.removeDeadClients();

    ASSERT_FALSE(pool.isClientAlive(clientId));
}

TEST(ClientPool, removeAllClients)
{
    ClientPool pool;
    std::string clientId="test";

    pool.addClient(clientId, 1,1);

    pool.removeAllClients();

    ASSERT_FALSE(pool.isClientAlive(clientId));
}
