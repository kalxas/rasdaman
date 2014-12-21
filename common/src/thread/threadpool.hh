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

#ifndef COMMON_SRC_THREAD_THREADPOOL_HH_
#define COMMON_SRC_THREAD_THREADPOOL_HH_

#include <boost/smart_ptr.hpp>
#include <boost/function.hpp>

namespace common
{

class ThreadPool
{
public:

    /**
    * Submit a task to be executed in the thread pool at a later point in time.
    * @param task The task to execute
    */
    virtual void submit(boost::function< void(void) > task) = 0;

    /**
    * Synchronously wait for all the running tasks to finish execution.
    */
    virtual void awaitTermination() = 0;

    /**
     * Destruct the instance of the ThreadPool object.
     */
    virtual ~ThreadPool();
};

} /* namespace rnp */

#endif /* COMMON_SRC_THREAD_THREADPOOL_HH_ */
