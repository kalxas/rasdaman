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

#include <stdexcept>
#include <algorithm>
#include <vector>

#include <boost/thread.hpp>

#include "../logging/easylogging++.hh"

#include "fixedthreadpool.hh"

namespace common
{
using std::runtime_error;
using std::vector;
using std::string;
using std::map;
using std::pair;
using boost::unique_lock;
using boost::mutex;
using boost::shared_ptr;
using boost::thread;
using std::exception;

FixedThreadPool::FixedThreadPool(boost::uint32_t poolSize):
    work(new boost::asio::io_service::work(ioService))
{
    int workerThreadNo;

    if (poolSize == 0)
    {
        workerThreadNo = boost::thread::hardware_concurrency();
        if(poolSize == 0)
        {
            LINFO<<"Could not detect the number of hardware threads."<<"Setting the default to 1";
            workerThreadNo = 1;
        }
    }
    else
    {
        workerThreadNo = poolSize;
    }

    for(int i=1; i<=workerThreadNo; i++)
    {
        threadGroup.add_thread(new boost::thread(
                                   boost::bind(&boost::asio::io_service::run, &ioService)
                               ));
    }
}

FixedThreadPool::~FixedThreadPool()
{
    try
    {
        this->awaitTermination();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch(...)
    {
        LERROR<<"FixedThreadPool destructor failed.";
    }
}


void FixedThreadPool::submit(boost::function< void(void) > task)
{
    ioService.post(task);
}

void FixedThreadPool::awaitTermination()
{
    work.reset();
    threadGroup.join_all();
    ioService.stop();
}

} /* namespace common */
