//
// Created by Dimitar Misev
// Copyright (c) 2018 rasdaman GmbH. All rights reserved.
//

#include "signalhandler.hh"
#include "stacktrace.hh"
#include <sys/types.h>
#include <unistd.h>
#include <memory>
#include <logging.hh>

namespace common {

void SignalHandler::ignoreSignals(const std::initializer_list<int> &signals)
{
    for (auto s: signals)
    {
        signal(s, SIG_IGN);
    }
}

void SignalHandler::ignoreStandardSignals()
{
    ignoreSignals({SIGHUP, SIGPIPE, SIGCONT, SIGTSTP, SIGTTIN, SIGTTOU, SIGWINCH});
}

void SignalHandler::handleSignals(const std::initializer_list<int> &signals, SignalHandlerFunction handler)
{
    for (auto signal: signals)
    {
        installSignalHandler(handler, signal);
    }
}

void SignalHandler::handleShutdownSignals(SignalHandlerFunction handler)
{
    handleSignals({SIGINT, SIGTERM, SIGQUIT}, handler);
}

void SignalHandler::handleAbortSignals(SignalHandlerFunction handler)
{
    handleSignals({SIGSEGV, SIGABRT, SIGFPE, SIGILL, SIGSYS, SIGXCPU, SIGXFSZ}, handler);
}

void SignalHandler::installSignalHandler(void (*handler)(int, siginfo_t *, void *), int signal)
{
    struct sigaction sigact;

    //setup the handling function
    sigact.sa_sigaction = handler;

    sigact.sa_flags = SA_RESTART | SA_SIGINFO;
    sigemptyset(&sigact.sa_mask);

    errno = 0;
    int retVal = sigaction(signal , &sigact, (struct sigaction*)NULL);
    if (retVal != 0)
    {
        LERROR << "Installing handler for signal " << signal << " failed: " << strerror(errno);
    }
}

std::string SignalHandler::toString(siginfo_t *info)
{
    if (info == NULL)
        return "";

    std::string ret = signalName(info->si_signo);
    ret += " (";
    ret += signalInfo(info);
    ret += ")";
    return ret;
}

std::string SignalHandler::getStackTrace()
{
    common::stacktrace::StackTrace currTrace;
    // skip current line, plus the caller's line
    return currTrace.toString(2);
}

std::string SignalHandler::signalName(int signalNumber)
{
    switch (signalNumber)
    {
        // Default action: Abnormal termination of the process.
        case SIGABRT: return "SIGABRT";
        case SIGILL:  return "SIGILL";
        case SIGBUS:  return "SIGBUS";
        case SIGFPE:  return "SIGFPE";
        case SIGSEGV: return "SIGSEGV";
        case SIGQUIT: return "SIGQUIT";
        case SIGTRAP: return "SIGTRAP";
        case SIGSYS:  return "SIGSYS";
        case SIGXCPU: return "SIGXCPU";
        case SIGXFSZ: return "SIGXFSZ";
        // Default action: Abnormal termination of the process. The process is terminated with all
        // the consequences of _exit() except that the status made available to
        // wait() and waitpid() indicates abnormal termination by the specified signal.
        case SIGALRM: return "SIGALRM";
        case SIGHUP:  return "SIGHUP";
        case SIGINT:  return "SIGINT";
        case SIGKILL: return "SIGKILL";
        case SIGTERM: return "SIGTERM";
        case SIGPIPE: return "SIGPIPE";
        case SIGUSR1: return "SIGUSR1";
        case SIGUSR2: return "SIGUSR2";
        case SIGPOLL: return "SIGPOLL";
        case SIGPROF: return "SIGPROF";
        case SIGVTALRM: return "SIGVTALRM";
        // Default action: Stop the process.
        case SIGSTOP: return "SIGSTOP";
        case SIGTSTP: return "SIGTSTP";
        case SIGTTIN: return "SIGTTIN";
        case SIGTTOU: return "SIGTTOU";
        // Default action: Continue the process.
        case SIGCONT: return "SIGCONT";
        // Default action: Ignore the signal.
        case SIGCHLD: return "SIGCHLD";
        case SIGURG:  return "SIGURG";
        case SIGWINCH:return "SIGWINCH";
        // Shouldn't happen
        default: return "";
    }
}

std::string SignalHandler::signalInfo(siginfo_t *info)
{
    if (info == NULL)
        return "";
    std::string ret = basicSignalInfo(info);
    if (!ret.empty())
        ret += ": ";
    ret += extraSignalInfo(info);
    return ret;
}

std::string SignalHandler::basicSignalInfo(siginfo_t *info)
{
    if (info == NULL)
        return "";

    switch (info->si_signo)
    {
        case SIGABRT: return "Abnormal termination";
        case SIGILL:  return "Illegal instruction";
        case SIGBUS:  return "Access to an undefined portion of a memory object";
        case SIGFPE:  return "Erroneous arithmetic operation";
        case SIGSEGV: return "";
        case SIGQUIT: return "Terminal quit signal";
        case SIGTRAP: return "";
        case SIGSYS:  return "Bad system call";
        case SIGXCPU: return "CPU time limit exceeded";
        case SIGXFSZ: return "File size limit exceeded";
        case SIGALRM: return "Alarm clock";
        case SIGHUP:  return "Hangup";
        case SIGINT:  return "Terminal interrupt signal";
        case SIGKILL: return "Kill, cannot be caught or ignored";
        case SIGTERM: return "Terminate execution";
        case SIGPIPE: return "Write on a pipe with no one to read it";
        case SIGUSR1: return "User-defined signal 1";
        case SIGUSR2: return "User-defined signal 2";
        case SIGPOLL: return "Pollable event";
        case SIGPROF: return "Profiling timer expired";
        case SIGVTALRM: return "Virtual timer expired";
        case SIGSTOP: return "Stop executing, cannot be caught or ignored";
        case SIGTSTP: return "Terminal stop signal";
        case SIGTTIN: return "Background process attempting read";
        case SIGTTOU: return "Background process attempting write";
        case SIGCONT: return "Continue execution, if stopped";
        case SIGCHLD: return "";
        case SIGURG:  return "High bandwidth data is available at a socket";
        case SIGWINCH:return "Window size change";
        // Shouldn't happen
        default: return "";
    }
}

std::string SignalHandler::extraSignalInfo(siginfo_t *info)
{
    if (info == NULL)
        return "";

    std::string ret;

    switch (info->si_signo)
    {
    case SIGILL:
    {
        switch (info->si_code)
        {
            case ILL_ILLOPC: ret = "Illegal opcode"; break;
            case ILL_ILLOPN: ret = "Illegal operand"; break;
            case ILL_ILLADR: ret = "Illegal addressing mode"; break;
            case ILL_ILLTRP: ret = "Illegal trap"; break;
            case ILL_PRVOPC: ret = "Privileged opcode"; break;
            case ILL_PRVREG: ret = "Privileged register"; break;
            case ILL_COPROC: ret = "Coprocessor error"; break;
            case ILL_BADSTK: ret = "Internal stack error"; break;
            default: break;
        }
        if (!ret.empty()) ret += " ";
        ret += "at address ";
        ret += pointerToString(info->si_addr);
        break;
    }
    case SIGBUS:
    {
        switch (info->si_code)
        {
            case BUS_ADRALN: ret = "Invalid address alignment"; break;
            case BUS_ADRERR: ret = "Nonexistent physical address"; break;
            case BUS_OBJERR: ret = "Object-specific hardware error"; break;
            default: break;
        }
        break;
    }
    case SIGFPE:
    {
        switch (info->si_code)
        {
            case FPE_INTDIV: ret = "Integer divide by zero"; break;
            case FPE_INTOVF: ret = "Integer overflow"; break;
            case FPE_FLTDIV: ret = "Floating-point divide by zero"; break;
            case FPE_FLTOVF: ret = "Floating-point overflow"; break;
            case FPE_FLTUND: ret = "Floating-point underflow"; break;
            case FPE_FLTRES: ret = "Floating-point inexact result"; break;
            case FPE_FLTINV: ret = "Invalid floating-point operation"; break;
            case FPE_FLTSUB: ret = "Subscript out of range"; break;
            default: break;
        }
        break;
    }
    case SIGSEGV:
    {
        switch (info->si_code)
        {
            case SEGV_ACCERR: ret = "Invalid permissions for memory access"; break;
            case SEGV_MAPERR:
            default:          ret = "Invalid memory access"; break;
        }
        if (!ret.empty()) ret += " ";
        ret += "at address ";
        ret += pointerToString(info->si_addr);
        break;
    }
    case SIGTRAP:
    {
        switch (info->si_code)
        {
            case TRAP_BRKPT: ret = "Process breakpoint"; break;
            case TRAP_TRACE: ret = "Process trace trap"; break;
            default: break;
        }
        break;
    }
    case SIGPOLL:
    {
        switch (info->si_code)
        {
            case POLL_IN:  ret = "Data input available, band event " + std::to_string(info->si_band); break;
            case POLL_OUT: ret = "Output buffers available, band event " + std::to_string(info->si_band); break;
            case POLL_MSG: ret = "Input message available, band event " + std::to_string(info->si_band); break;
            case POLL_ERR: ret = "I/O error"; break;
            case POLL_PRI: ret = "High priority input available"; break;
            case POLL_HUP: ret = "Device disconnected"; break;
            default: break;
        }
        break;
    }
    case SIGCHLD:
    {
        switch (info->si_code)
        {
            case CLD_EXITED:  ret = "Child exited"; break;
            case CLD_KILLED:  ret = "Child aborted and did not create a core file"; break;
            case CLD_DUMPED:  ret = "Child aborted and created a core file"; break;
            case CLD_TRAPPED: ret = "Child trapped"; break;
            case CLD_STOPPED: ret = "Child stopped"; break;
            case CLD_CONTINUED: ret = "Child continued"; break;
            default: ret = "Child process terminated, stopped, or continued"; break;
        }
        ret += " [child pid: ";
        ret += std::to_string(info->si_pid);
        ret += ", exit code: ";
        ret += std::to_string(info->si_status);
        ret += ", uid: ";
        ret += std::to_string(info->si_uid);
        ret += "]";
        break;
    }
    default: break;
    }

    {
        std::string tmp;
        switch (info->si_code)
        {
            case SI_USER:   tmp = "sent by kill()"; break;
            case SI_QUEUE:  tmp = "sent by sigqueue()"; break;
            case SI_TIMER:  tmp = "timer set by timer_settime() expired"; break;
            case SI_ASYNCIO:tmp = "asynchronous I/O request completed"; break;
            case SI_MESGQ:  tmp = "a message arrived on an empty message queue"; break;
            case SI_KERNEL: tmp = "sent by kernel"; break;
            default: break;
        }
        if (!tmp.empty())
        {
            if (!ret.empty())
                ret += ", ";
            ret += tmp;
        }
    }

    if (info->si_signo != SIGCHLD &&
        (info->si_code == SI_USER || info->si_code == SI_QUEUE || info->si_code < 0) &&
        info->si_pid > 0)
    {
        if (!ret.empty())
            ret += "; ";
        if (info->si_pid == getpid())
        {
            ret += "Raised internally by this process";
        }
        else
        {
            ret += "Sent by process with pid ";
            ret += std::to_string(info->si_pid);
            ret += " run by user with uid ";
            ret += std::to_string(info->si_uid);
        }
    }

    if (info->si_errno != 0)
    {
        if (!ret.empty())
            ret += "; ";
        ret += "Associated error: ";
        ret += strerror(info->si_errno);
    }

    return ret;
}

std::string SignalHandler::pointerToString(const void *p)
{
    std::ostringstream ss;
    ss << p;
    auto ret = ss.str();
    if (ret.size() > 2 && ret[0] != '0' && ret[1] != 'x')
        ret = "0x" + ret;
    return ret;
}

}
