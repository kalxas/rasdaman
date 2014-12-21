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

FixedThreadPool::FixedThreadPool(boost::uint32_t poolSize)
{
    if (poolSize == 0)
    {
        this->poolSize = boost::thread::hardware_concurrency();
        if(poolSize == 0)
        {
            LINFO<<"Could not detect the number of hardware threads."<<"Setting the default to 1";
            this->poolSize = 1;
        }
    }
    else
    {
        this->poolSize = poolSize;
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
    unique_lock<mutex> threadsLock(this->threadsMutex);
    if(this->runningThreads.size()==this->poolSize)
    {
        //We have reached capacity, queue the task
        unique_lock<mutex> tasksLock(this->tasksMutex);
        this->waitingTasks.push_back(task);
    }
    else
    {
        pair< map<thread::id, shared_ptr<thread> >::iterator,bool> ret;
        shared_ptr<thread> thr(new thread(&FixedThreadPool::runThread, this, task));
        ret=this->runningThreads.insert(std::make_pair(thr->get_id(), thr));
        if(ret.second==false)
        {
            LERROR<<thr->get_id()<<"Could not be inserted in the running threads set";
            map<thread::id, shared_ptr<thread> >::iterator it;
            for(it=this->runningThreads.begin(); it!=this->runningThreads.end(); ++it)
            {
                LERROR<<it->first;
            }
            //The element was not inserted. It shows that I must modify the set construction
            throw runtime_error("The element was not inserted. Please check the logs.");
        }
    }
}

void FixedThreadPool::awaitTermination()
{

    vector<shared_ptr<thread> > runningThreadsList;

    bool done = false;

    while (!done)
    {
        map<thread::id, shared_ptr<thread> >::iterator it;
        unique_lock<mutex> threadsLock(this->threadsMutex);
        //When a threads finishes, it removes its ID from the set of running threads
        //At this point, we copy the thread pointer so that we can wait on each of them
        //without any concurrency issues
        for ( it= this->runningThreads.begin();
                it != this->runningThreads.end(); it++)
        {
            runningThreadsList.push_back(it->second);
        }
        threadsLock.unlock();

        //Join all the threads. This will also run the waiting tasks.
        //When a thread finishes, it checks if there are any tasks waiting
        for (vector<shared_ptr<thread> >::iterator thr = runningThreadsList.begin();
                thr != runningThreadsList.end(); thr++)
        {
            (*thr)->join();
        }

        threadsLock.lock();
        //If the set is not empty, we have a problem, but we cannot do anything about it
        if (this->runningThreads.empty())
        {
            LINFO<<"The thread pool was successfully destructed.";
            done = true;
        }

        threadsLock.unlock();
    }

    runningThreadsList.clear();
}

void FixedThreadPool::runThread(boost::function< void(void) > task)
{
    boost::function< void(void) > nextTask;

    bool isNext = false;
    try
    {
        task();
    }
    catch (std::exception& ex)
    {
        LERROR << ex.what();
    }
    catch (...)
    {
        //We cannot allow the program to throw an exception in a thread
        LERROR << "An unexpected error was thrown.";
    }

    //the execution is complete so we can remove the thread from the set.
    unique_lock<mutex> threadLock(this->threadsMutex);
    this->runningThreads.erase(boost::this_thread::get_id());
    threadLock.unlock();

    //This thread should check if there are any tasks waiting.
    //If there are tasks waiting, execute them
    unique_lock<mutex> taskLock(this->tasksMutex);
    if(!this->waitingTasks.empty())
    {
        nextTask = this->waitingTasks.front();
        this->waitingTasks.pop_front();
        isNext=true;
    }
    taskLock.unlock();

    if (isNext)
    {
        //Execute the waiting task;
        this->submit(nextTask);
    }
}

} /* namespace common */
