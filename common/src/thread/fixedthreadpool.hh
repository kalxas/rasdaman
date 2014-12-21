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

#ifndef COMMON_SRC_THREAD_FIXEDTHREADPOOL_HH_
#define COMMON_SRC_THREAD_FIXEDTHREADPOOL_HH_

#include <map>
#include <deque>
#include <utility>

#include <boost/cstdint.hpp>
#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>

#include "threadpool.hh"

namespace common
{

class FixedThreadPool : public ThreadPool
{
public:
    /**
     * Create a ThreadPool with a maximum number of concurrent threads.
     * @param poolSize The maximum number of threads that can run at a particular time.
     * The default value is 0, which means: no_cpu_cores. If the number of hardware threads cannot be detected, the defaut is 1.
     */
    FixedThreadPool(boost::uint32_t poolSize=0);

    virtual ~FixedThreadPool();

    virtual void submit(boost::function< void(void) > task);

    virtual void awaitTermination();

private:
    boost::uint32_t poolSize;/*! Maximum number of thread that can run concurrently */
    std::map<boost::thread::id, boost::shared_ptr<boost::thread> > runningThreads; /*!Set of threads that are currently running */
    std::deque<boost::function< void(void) > > waitingTasks;/*! Tasks that have been queued and are awaiting execution */

    boost::mutex threadsMutex;/*! Mutex used to sync access to the runningThreads object */
    boost::mutex tasksMutex; /*! Mutex used to sync access to the waitingTasks object */

    /**
     *
     * @param task
     */
    void runThread(boost::function< void(void) > task);
};

} /* namespace common */

#endif /* COMMON_SRC_THREAD_FIXEDTHREADPOOL_HH_ */
