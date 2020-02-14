#!/usr/bin/env python3

from multiprocessing import Process
import os
import time
import glob


OUTPUTS_PATH = "outputs"
QUERIES_PATH = "queries"
TESTDATA_PATH = "testdata"
TESTFILE = TESTDATA_PATH + "/char_10000x10000.tif"

TESTCOLL_2D = "test_concurrent_ingest_2d"
TESTCOLL_3D = "test_concurrent_ingest_3d"

MAX_TASKS = 6


def setup_outputs_path():
  """Make sure we have an empty outputs/ dir before running the tests."""
  if os.path.exists(OUTPUTS_PATH):
    out_files = glob.glob(OUTPUTS_PATH + "/*")
    for f in out_files:
      os.unlink(f)
  else:
    os.mkdir(OUTPUTS_PATH)


def exec_query(query, extra_options):
  """Execute rasql query with admin user (e.g. insert, create, etc). The extra
     options are appended at the end of the rasql command."""
  cmd = "rasql -q '" + query + "' " + extra_options
  os.system(cmd)


def exec_query_as_admin(query, extra_options="--quiet > /dev/null 2>&1"):
  """Execute rasql query with admin user (e.g. insert, create, etc). The extra
     options are appended at the end of the rasql command."""
  cmd = "rasql -q '" + query + "' --user rasadmin --passwd rasadmin " + extra_options
  os.system(cmd)


def prepare_testdata():
  """The testdata is big so needs to be generated instead of versioned. The test
     collections are created and initialized with a single-cell array."""
  if not os.path.exists(TESTDATA_PATH):
    os.mkdir(TESTDATA_PATH)
  if not os.path.exists(TESTFILE):
    testfilename = os.path.splitext(TESTFILE)[0]
    print("Generating " + TESTFILE)
    exec_query("select encode(marray i in [0:9999,0:9999] values (char) i[0], \"tiff\")",
               "--quiet --out file --outfile " + testfilename)

  for c in [TESTCOLL_2D, TESTCOLL_3D]:
    print("Dropping and reinitializing collection " + c)
    exec_query_as_admin("drop collection " + c)

  exec_query_as_admin("create collection " + TESTCOLL_2D + " GreySet")
  exec_query_as_admin("create collection " + TESTCOLL_3D + " GreySet3")
  exec_query_as_admin("insert into " + TESTCOLL_2D + " values <[0:0,0:0] 0c>")
  exec_query_as_admin("insert into " + TESTCOLL_3D + " values <[0:0,0:0,0:0] 0c>")


def task():
  """Execute all queries from the files in queries/*"""
  pid = str(os.getpid())
  query_files = glob.glob(QUERIES_PATH + "/*")
  query_files.sort()

  for query_file in query_files:
    with open(query_file, 'r') as file:
      cmd = file.read().strip()

    query_name = os.path.basename(query_file)
    out_file = OUTPUTS_PATH + "/" + query_name + "." + pid
    cmd += " > " + out_file #+ " 2>&1"
    print("[" + pid + "] executing: " + cmd)
    os.system(cmd)


setup_outputs_path()
prepare_testdata()

# start MAX_TASKS tasks in parallel
active_processes = []
for i in range(0, MAX_TASKS):
  p = Process(target = task)
  active_processes.append(p)
  p.start()

# wait until they are finished
for p in active_processes:
  p.join()

print("Done.")
