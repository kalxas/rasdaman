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

#ifndef RMDEBUG_HH
#define RMDEBUG_HH

#include <sys/time.h>

// generate benchmark code only when RMANBENCHMARK is set
#ifdef RMANBENCHMARK
#define RMTIMER(class, func)  RMTimer localRMTimer = RMTimer(class, func);
#else
#define RMTIMER(class, func)
#endif

///Module: <b>raslib</b>.

/**
RMTimer is not strictly part of RasLib. It is a class used for taking
timing measurements if configuring with --benchmark-enabled. One way
of using it is to put the following at the beginning of a function:

<tt>RMTIMER("className", "functionName");</tt>

If RMANBENCHMARK is defined this is expanded to:

<tt>RMTimer localRMTimer = RMTimer("className", "functionName");</tt>

Time is taken between this line and exiting the block where this line
was. For more elaborate timing measurements an RMTimer object can be
used directly. All timing information is stored in the object, so
multiple RMTimer objects can be used at the same time.

If output is generated on RMInit::bmOut depends on the flag <tt>output</tt> and the benchmark level. Output is generated if <tt>output</tt>
is TRUE and <tt>bmLevel</tt> is lower than the global benchmark level
stored in RManBenchmark. The flag <tt>output</tt> can be changed with
setOutput(). The function start() sets <tt>output</tt> to TRUE, stop()
sets <tt>output</tt> to FALSE.

<b>Important</b>: If a RMTimer is used as a static variable, it must be
ensured that no output is generated in the destructor either by
calling stop() or by manually setting <tt>output</tt> to FALSE using
setOutput() before termination of the program. The reason is that
potentially RMInit::bmOut may be destructed before the RMTimer
destructor is called possibly causing a crash.
*/

class RMTimer
{
public:
    /// constructor, initializes members and starts timer.
    RMTimer(const char *newClass, const char *newFunc,
                   int newBmLevel = 4);
    /**
      The parameters newClass and newFunc have to be string literals. Just
      a pointer to them is stored. No output is generated if RManBenchmark
      < newBmLevel.
    */
    /// destructor, calls stop().
    ~RMTimer();
    /// switch output on RMInit::bmOut on and off.
    void setOutput(int newOutput);
    /**
      If newOutoutput is FALSE no output is created on RMInit::bmOut on
      the following calls to stop() and ~RMTimer() until the next start().
    */
    /// pauses timer.
    void pause();
    /// resumes timer.
    void resume();
    /// resets timer.
    void start();
    /**
      Also switches output to RMInit::bmOut on again.
    */
    /// prints time spent if output is TRUE.
    void stop();
    /**
      Time spent is the time since construction or last start() excluding
      the times between pause() and resume().
    */
    /// delivers current time count.
    int getTime();

private:
    /// name of class.
    const char *myClass;
    /// name of function (no parameters).
    const char *myFunc;
    /// flag, if stop() should print timing information
    int output;
    /// stores benchmark level, checked before output.
    int bmLevel;
    // reference parameter for gettimeofday().
    timeval acttime;
    /// accu for saving time in us
    long accuTime;
    /// flag indicating if the timer is currently running.
    unsigned short running;
    /// used to calculate time spent in function.
    long oldsec;
    /// used to calculate time spent in function.
    long oldusec;
    /// aux function to determine clock time elapsed so far.
    void fetchTime();
};
///Module: <b>raslib</b>.

#endif
