#!/usr/bin/env python2
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

#
# Evaluate tests in the queries/ directory generated with rasqte.py, and compare
# to expected results stored in the oracles directory.
#

import ast
import argparse
import os
import subprocess
import time
import sys
import glob
import filecmp
import difflib
import shutil
import tarfile
import signal
from util.executor import execute
from util.log import log
from timeit import default_timer as timer

# directories
QUERIES_DIR = "queries/"
ORACLES_DIR = "oracles/"
OUTPUTS_DIR = "outputs/"
SEPARATOR = "==="

# special files
SETUP_TESTS = QUERIES_DIR + "setup"
TEARDOWN_TESTS = QUERIES_DIR + "teardown"
SPECIAL_FILES = ["setup", "teardown"]

# rasdaman install path
RMANHOME = ""
RMANHOME_BIN = ""
# rasql command
RASQL = ["rasql", "--user", "rasadmin", "--passwd", "rasadmin", "--type"]
# global timeout of 60 seconds, can be overriden in the test cases with "timeout: val"
TIMEOUT = 60

# test results
TEST_PASSED = "PASSED"
TEST_FAILED = "FAILED"

# global statistics
TOTAL_TESTS_COUNT = 0
TOTAL_QUERIES_COUNT = 0
# a list of failed tests
FAILED_TESTS = []
FAILED_TESTS_LOG = OUTPUTS_DIR + "test_failed.log"
IGNORED_TESTS = []
IGNORED_TESTS_LOG = OUTPUTS_DIR + "test_ignored.log"

def append_slash(path):
    """
    path -> path/, path/ -> path/
    """
    return path + "/" if path[-1] != "/" else path

def remove_slash(path):
    """
    path -> path, path/ -> path
    """
    return path[0:-1] if path[-1] == "/" else path

def remove_file(path):
    """
    Safely remove a file (only if it exists).
    """
    if os.path.isfile(path):
        os.remove(path)

def remove_files_with_prefix(prefix):
    remove = glob.glob(prefix + "*")
    for s in remove:
        remove_file(s)

def read_lines(path):
    with open(path, 'r') as f:
        return f.readlines()

def write_lines(lines, f):
    """
    Write the lines to file f separated by new line and ending with a new line.
    """
    with open(f, "w") as f:
        f.write('\n'.join(lines) + '\n')

def string_to_list(v):
    """
    Try to parse v into a list of strings.
    """
    astval = ast.literal_eval(v)
    if type(astval) is list:
        return [str(s) for s in astval]
    elif isinstance(astval, basestring):
        return [astval]
    else:
        log.error("Failed converting %s to a list of strings.", v)
        return []

def remove_lines_containing(s, delimiter, filters):
    """
    Split s by a delimiter, remove any lines that contain some filter, and join
    the remaining lines with the same delimiter.
    """
    lines = s.split(delimiter)
    reslist = filter(lambda p: all(f not in p for f in filters), lines)
    return delimiter.join(reslist)

class TestCase:

    def __init__(self):
        # list of queries to evaluate. If it's more than one query, stdout/stderr/exitcode
        # are aggregated (appended) to the outprogram file, while all outfiles have
        # to be equal when compared; this can be used to group different queries with
        # same expected file output, or several related error queries (which have 
        # empty file output).
        self.queries = []
        # query timeout
        self.timeout = TIMEOUT
        # python expression (string or list of strings) to allow filtering of 
        # lines from the result that match it (string comparison). E.g.
        # filter: ['Result element']
        # will filter all lines that contain this substring. This can be used to
        # remove output that is inconsistent across test runs.
        self.filters = []
        # if the test is marked as a known fail, it will not contribute 
        # to the final test result.
        self.knownfail = ""
        # flag allowing to skip test evaluation
        self.skip = ""
        # unique test id
        self.testid = ""
        # file that holds program stdout, stderr, and exitcode
        self.outprogram = ""
        # file that holds the "file" output (e.g. file from rasql --out file query)
        self.outfile = ""
        # if disableoutfile is specified, --out file will not be added to the rasql cmd
        self.disableoutfile = False

    def add_config_line(self, line):
        if line.strip() == "" or line.startswith("#"):
            return False
        if not ':' in line:
            log.warn("Invalid test case, expected line of "
                  "format 'key: value', but no ':' is found in:\n%s", line)
            return False
        kv = line.split(":", 1)
        self.add_config(kv[0].strip(), kv[1].strip())
        return True

    def add_config(self, k, v):
        """
        Add a key/value parameter to the test case configuration.
        """
        if k == "query":
            self.queries.append(v)
        elif k == "id":
            self.testid = v
            self.outprogram = self.testid
            self.outfile = self.testid + ".file"
        elif k == "timeout":
            self.timeout = int(v)
        elif k == "filter" or k == "filters":
            self.filters = string_to_list(v)
        elif k == "knownfail" or k == "known_fail" or k == "ignore_result":
            self.knownfail = v
        elif k == "disableoutfile":
            self.disableoutfile = True
        elif k == "skip":
            self.skip = v

    def validate(self):
        """
        Check that the test case is valid, e.g. it must have an id. Return
        True if valid, False otherwise.
        """
        ret = True
        if not self.testid:
            log.warn("Testcase missing an id.")
            ret = False
        return ret

    def __str__(self):
        ret = ""
        for q in self.queries:
            ret += "query: " + q + "\n"
        if self.testid:
            ret += "id: " + self.testid + "\n"
        if self.timeout != TIMEOUT:
            ret += "timeout: " + str(self.timeout) + "\n"
        return ret


def read_next_test(f, separator):
    """
    Read the next test case from the tests file f, ending with separator line.
    """
    ret = TestCase()
    empty = True
    for line in f:
        if line.strip() == separator:
            # done when the separator is reached
            break
        if ret.add_config_line(line):
            empty = False

    return None if empty else ret

def append_program_output(f, out, err, rc, separator, filters):
    f.write("exitcode: {}\n\n".format(rc))
    out = out.replace(RMANHOME_BIN, "")
    err = err.replace(RMANHOME_BIN, "")
    if out.startswith("rasql: "):
        # remove first two lines of output which may be non-portable
        # (containing rasdaman version, and server/port)
        if out.count("\n") >= 2:
            out = out.split("\n", 2)[-1]

    for s in filters:
        if s in out:
            out = remove_lines_containing(out, '\n', filters)
            break
            
    f.write("stdout: {}\n".format(out))
    f.write("stderr: {}".format(err))
    if not err.endswith("\n"):
        f.write("\n")
    f.write(separator + "\n")

def diff_files(out, exp):
    """
    Print diff of out and exp on stderr.
    """
    outstr = read_lines(out)
    expstr = read_lines(exp)
    for line in difflib.unified_diff(outstr, expstr, fromfile=exp, tofile=out):
        log.warn(line.strip())

def cmp_files(out, exp, show_diff=False):
    """
    Compare output file (out) to expected file (exp).
    """
    if not os.path.isfile(exp):
        log.warn("Expected file '%s' not found, will be copied from output '%s'.", exp, out)
        shutil.copyfile(out, exp)
        return False
    ret = filecmp.cmp(out, exp)
    if not ret:
        log.error("Output file '%s' does not match expected file '%s'.%s",
                  out, exp, " Diff:" if show_diff else "")
        if show_diff:
            diff_files(out, exp)
    return ret

def cmp_allfiles(outlist, explist, show_diff=False):
    """
    Compare piecewise each file in outlist with the corresponding file in explist.
    """
    ret = True
    for out, exp in zip(outlist, explist):
        ret = cmp_files(out, exp, show_diff) and ret
    return ret

def pgrep(program):
    """
    @return [pid1, pid2, ...] if program is running on the system, [] otherwise
    """
    ps = subprocess.Popen(["pgrep", program], shell=False, stdout=subprocess.PIPE)
    ret = ps.stdout.read().strip()
    ps.stdout.close()
    ps.wait()
    return ret.split("\n") if ret else []

def kill_pids(pids, sig):
    """
    Kill the list of pids with the given signal.
    """
    pids = pgrep("rasserver")
    failed_pids = []
    for pid in pids:
        try:
            os.kill(int(pid), sig)
        except OSError as err:
            log.warn("Failed killing rasserver process with pid %d: %s.", pid, err.strerr)
            failed_pids.append(pid)
    return failed_pids

def restart_rasdaman_if_rasmgr_down():
    """
    if rasmgr is down: kill all rasservers, and run start_rasdaman.sh again
    """
    if len(pgrep("rasmgr")) == 0:
        log.warn("rasmgr is down. Killing all rasservers, and starting rasdaman again.")
        pids = pgrep("rasserver")
        not_killed = kill_pids(pids, signal.SIGTERM)
        not_killed = kill_pids(not_killed, signal.SIGKILL)
        if len(not_killed) == 0:
            out, err, rc = execute(["start_rasdaman.sh"])
            log.info("Started rasdaman\nexit code: %d\nstdout: %s\nstderr: %s\n", rc, out, err)
            return True
        else:
            log.error("Failed killing rasservers, cannot start rasdaman.")
    return False

def interpret_result(ret, test, separator):
    """
    Collect statistics, and log failed / known fails tests.
    """
    global TOTAL_TESTS_COUNT, TOTAL_TESTS_IGNORED, TOTAL_QUERIES_COUNT, FAILED_TESTS
    TOTAL_TESTS_COUNT += 1
    TOTAL_QUERIES_COUNT += len(test.queries)
    if test.skip:
        log.warn("Test evaluation skipped, reason: %s", test.skip)
        IGNORED_TESTS.append("{} (evaluation skipped, reason: {})".format(test.testid, test.skip))
        ret = True
    elif not ret and test.knownfail:
        log.warn("Test result ignored, reason: %s", test.knownfail)
        IGNORED_TESTS.append("{} (result ignored, reason: {})".format(test.testid, test.knownfail))
        ret = True
    elif ret and test.knownfail:
        log.warn("Test marked as known fail has been fixed (%s)", test.knownfail)
    elif not ret:
        FAILED_TESTS.append(test.testid)

    log.info(separator)
    return ret

def evaluate_test(test, outdir, expdir, separator, retries=1):
    """
    Evaluate a single TestCase.
    """

    log.info("Evaluating test: %s", test.testid)

    # handle rasql stdout output
    outprogram = outdir + test.outprogram
    remove_file(outprogram)
    expprogram = expdir + test.outprogram

    # handle rasql file output
    outfile = outdir + test.outfile
    cmd = ""
    if not test.disableoutfile:
        remove_files_with_prefix(outfile)
        cmd = RASQL + ["--out", "file", "--outfile", outfile]
    else:
        cmd = RASQL + ["--out", "string"]

    ret = True
    if not test.skip:
        with open(outprogram, "a") as outprogram_file:
            for q in test.queries:
                out, err, rc = execute(cmd + ["-q", q], timeout_sec=test.timeout)
                append_program_output(outprogram_file, out, err, rc, separator, test.filters)
                # if file output is requested (default)
                if not test.disableoutfile:
                    outfiles = glob.glob(outfile + "*")
                    expfiles = [s.replace(outdir, expdir) for s in outfiles]
                    if not cmp_allfiles(outfiles, expfiles, False):
                        log.error("File comparison: %s.", TEST_FAILED)
                        ret = False

        # show diff if these text files are different
        if not cmp_files(outprogram, expprogram, True):
            log.error("Program output comparison: %s.", TEST_FAILED)
            ret = False

        # try to evaluate the same test one more time after restarting rasdaman
        if not ret and retries == 1:
            if restart_rasdaman_if_rasmgr_down():
                return evaluate_test(test, outdir, expdir, separator, retries + 1)

    return interpret_result(ret, test, separator)


def evaluate_tests(tests_file, outdir, expdir, separator):
    """
    Evaluates the tests in tests_file (rendered output from rasqte.py). The
    outputs are saved in outdir, and then compared to the expected output in
    expdir.
    """
    ret = True

    if not os.path.exists(outdir):
        os.makedirs(outdir)

    log.title("Evaluating tests in '%s'...", tests_file)
    with open(tests_file, "r") as f:

        outdir = append_slash(outdir)
        expdir = append_slash(expdir)
        
        while True:
            test = read_next_test(f, separator)
            if test is None:
                break
            if not test.validate():
                log.error("Invalid test case, skipping evaluation:\n%s\n%s", test, separator)
                ret = False
                continue
            if not test.queries:
                log.error("Test case specifies no queries, skipping.")
                ret = False
                continue

            ret = evaluate_test(test, outdir, expdir, separator) and ret

    if ret:
        log.success("Done, %s %s.", tests_file, TEST_PASSED)
    else:
        log.error("Done, %s %s.", tests_file, TEST_FAILED)

    return ret

def evaluate_existing_tests(f):
    if os.path.isfile(f):
        return evaluate_tests(f, OUTPUTS_DIR, ORACLES_DIR, SEPARATOR)
    return True

def print_stats(elapsed):
    write_lines(FAILED_TESTS, FAILED_TESTS_LOG)
    write_lines(IGNORED_TESTS, IGNORED_TESTS_LOG)

    log.title("\nTest summary")
    log.title("  Tests executed    : %d", TOTAL_TESTS_COUNT)
    log.title("  Queries executed  : %d", TOTAL_QUERIES_COUNT)
    log.title("  Failed tests      : %d", len(FAILED_TESTS))
    log.title("  Ignored / skipped : %d\n", len(IGNORED_TESTS))
    if TOTAL_QUERIES_COUNT > 0:
        log.title("  Time / query (ms) : %s", (elapsed * 1000.0 / TOTAL_QUERIES_COUNT))
    log.title("  Total time (m)    : %s\n", elapsed / 60.0)
    log.title("  Failed tests log  : %s", FAILED_TESTS_LOG)
    log.title("  Ignored tests log : %s\n", IGNORED_TESTS_LOG)
    status_msg = "  Status            : %s"
    if len(FAILED_TESTS) == 0:
        log.success(status_msg, TEST_PASSED)
    else:
        log.error(status_msg, TEST_FAILED)

def uncompress_directory(d):
    """
    If directory d is not found, check for d.tar.gz and uncompress it if it exists.
    """
    if os.path.exists(d):
        return True
    dtargz = remove_slash(d) + ".tar.gz"
    if not os.path.exists(dtargz):
        log.warn("Directory '%s' or corresponding archive '%s' not found.", d, dtargz)
        return False
    with tarfile.open(dtargz, "r:gz") as tar:
        def is_within_directory(directory, target):
            
            abs_directory = os.path.abspath(directory)
            abs_target = os.path.abspath(target)
        
            prefix = os.path.commonprefix([abs_directory, abs_target])
            
            return prefix == abs_directory
        
        def safe_extract(tar, path=".", members=None, *, numeric_owner=False):
        
            for member in tar.getmembers():
                member_path = os.path.join(path, member.name)
                if not is_within_directory(path, member_path):
                    raise Exception("Attempted Path Traversal in Tar File")
        
            tar.extractall(path, members, numeric_owner=numeric_owner) 
            
        
        safe_extract(tar)

def parse_cmdline():
    """
    Setup a command line parser and return the parsed args object
    """
    parser = argparse.ArgumentParser(description="rasql query systemtest evaluator; "
        "without arguments it evaluates all tests in the queries/ directory, starting "
        "with any setup tests and ending with the teardown tests.")
    parser.add_argument("-d", "--drop", action="store_true",
                        help="Drop data (execute teardown queries) only and exit.")
    parser.add_argument("-t", "--testsfile",
                        help="Execute a specific tests file (with setup before and teardown after).")
    parser.add_argument("-r", "--rmanhome", default=os.environ['RMANHOME'],
                        help="Path to the rasdaman installation; by default "
                        "the RMANHOME env variable is considered.")
    args = parser.parse_args()
    return args

def main():
    args = parse_cmdline()

    global RMANHOME, RMANHOME_BIN
    RMANHOME = args.rmanhome
    if not RMANHOME:
        log.warn("RMANHOME has not been set; consider specifying it with the --rmanhome option.")
    else:
        RMANHOME = append_slash(RMANHOME)
        RMANHOME_BIN = RMANHOME + "bin/"
        RASQL[0] = RMANHOME_BIN + RASQL[0]
        log.info("rasql: %s", RASQL)

    log.title("Running rasql systemtest.")
    # queries and oracles directory are quite big, so they are distributed 
    # as compressed archives, which potentially need to be uncompressed (for a first run)
    for d in [QUERIES_DIR, ORACLES_DIR]:
        uncompress_directory(d)

    all_tests = None
    if args.testsfile:
        all_tests = [SETUP_TESTS, args.testsfile]
    elif args.drop:
        all_tests = []
    else:
        all_tests = [SETUP_TESTS] + sorted([QUERIES_DIR + f
                                            for f in os.listdir(QUERIES_DIR) \
                                                if not f in SPECIAL_FILES and
                                                   not f.endswith('.bak')])
    all_tests = all_tests + [TEARDOWN_TESTS]

    log.info("Tests files to execute: %s", all_tests)

    # run tests
    ret = True
    start_time = timer()
    for tests_file in all_tests:
        ret = evaluate_existing_tests(tests_file) and ret
    end_time = timer()

    print_stats(end_time - start_time)

    return ret

if __name__ == "__main__":
    sys.exit(main())
