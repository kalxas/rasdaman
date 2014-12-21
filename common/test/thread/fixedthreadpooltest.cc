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
#include <iostream>
#include <vector>

#include <boost/thread.hpp>
#include <boost/smart_ptr.hpp>
#include <boost/function.hpp>

#include "../../src/thread/fixedthreadpool.hh"
#include "../../src/logging/easylogging++.hh"
#include "../../src/unittest/gtest.h"

using common::FixedThreadPool;
using boost::shared_ptr;


class SimpleTaskFixed
{
public:
    SimpleTaskFixed();
    void run();
    bool hasRan();
private:
    bool ran_;
};

void SimpleTaskFixed::run()
{
    usleep(1000);
    ran_ = true;
}
bool SimpleTaskFixed::hasRan()
{
    return ran_;
}
SimpleTaskFixed::SimpleTaskFixed()
{
    ran_ = false;
}

class AdvancedTaskFixed
{
public:
    static int counter;

    void run()
    {
        boost::unique_lock<boost::mutex> lock(this->mutex);
        counter++;
    }

private:
    boost::mutex mutex;
};

int AdvancedTaskFixed::counter = 0;

TEST(FixedThreadPool, constructor)
{
    srand(time(NULL));
    FixedThreadPool* pool = new FixedThreadPool(10);
    delete pool;
}

TEST(FixedThreadPool, submitTask)
{
    FixedThreadPool* pool = new FixedThreadPool(10);
    std::vector<shared_ptr<SimpleTaskFixed> >  tasks;

    int task_no = rand() % 10 + 1;
    for (int i = 0; i < task_no; i++)
    {
        shared_ptr<SimpleTaskFixed> t(new SimpleTaskFixed());
        tasks.push_back(t);
        boost::function< void(void) > funcPoint=boost::bind(&SimpleTaskFixed::run, t.get());
        pool->submit(funcPoint);
    }
    pool->awaitTermination();

    delete pool;
    for (int i = 0; i < task_no; i++)
    {
        ASSERT_EQ(true, tasks[i]->hasRan());
    }
}



TEST(FixedThreadPool, submitAdvancedTask)
{
    FixedThreadPool* pool = new FixedThreadPool(10);
    std::vector<shared_ptr<AdvancedTaskFixed> > tasks;
    int task_no = rand() % 10 + 1;
    for (int i = 0; i < task_no; i++)
    {
        shared_ptr< AdvancedTaskFixed> t(new  AdvancedTaskFixed());
        boost::function< void(void) > funcPoint=boost::bind(&AdvancedTaskFixed::run, t.get());
        tasks.push_back(t);
        pool->submit(funcPoint);
    }

    delete pool;

    ASSERT_EQ(task_no, AdvancedTaskFixed::counter);
}

TEST(FixedThreadPool, awaitTermination)
{
    FixedThreadPool* pool = new FixedThreadPool(10);
    std::vector<shared_ptr<SimpleTaskFixed> > tasks;
    int task_no = rand() % 10 + 1;
    for (int i = 0; i < task_no; i++)
    {
        shared_ptr<SimpleTaskFixed> t(new SimpleTaskFixed());
        tasks.push_back(t);
        boost::function< void(void) > funcPoint=boost::bind(&SimpleTaskFixed::run, t.get());
        pool->submit(funcPoint);
    }
    pool->awaitTermination();
    delete pool;
    for (int i = 0; i < task_no; i++)
    {
        ASSERT_EQ(true, tasks[i]->hasRan());
    }
}

TEST(FixedThreadPool, threadOverflow)
{
    FixedThreadPool* pool = new FixedThreadPool(2);
    std::vector<shared_ptr<SimpleTaskFixed> > tasks;

    int task_no = 10;
    for (int i = 0; i < task_no; i++)
    {
        shared_ptr<SimpleTaskFixed> t(new SimpleTaskFixed());
        tasks.push_back(t);
        boost::function< void(void) > funcPoint=boost::bind(&SimpleTaskFixed::run, t.get());
        pool->submit(funcPoint);
    }
    pool->awaitTermination();
    delete pool;
    for (int i = 0; i < task_no; i++)
    {
        EXPECT_EQ(true, tasks[i]->hasRan())<<i;
    }
}
