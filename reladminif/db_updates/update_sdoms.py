import sys
import subprocess

def usage():
    print ("Usage: python3 " + sys.argv[0] + " $DIRECTQL $RASBASE")
    sys.exit(1)

def decode(input_str):
  return input_str.decode('UTF-8').rstrip()

def execute_query(query_command):
  result = []

  MAX_RETRY = 5
  i = 0
  output = ""
  
  while i < MAX_RETRY:
      try:
          output = decode(subprocess.check_output(query_command, shell=True))
          break   
      except Exception as ex:                                                                                                   
          print("Failed to run query '{}'. Error message '{}'.".format(query_command, decode(ex.output)))
          i += 1
          print("Retry {}/{} times".format(i, MAX_RETRY))

  if i == MAX_RETRY:
      print("Tried {} times. Exiting..".format(MAX_RETRY))
      exit(1)

  for line in output.split("\n"):
      if "Result object" in line or "Result element" in line:
          result.append(line[line.index(":") + 1:].strip().strip("\x00"))
  return result        

def get_collection_names(directql):
    command = directql + " --out string -q 'select m from RAS_COLLECTIONNAMES m'"
    return execute_query(command)

def get_sdoms(directql, collection):
    command = directql + " --out string -q 'select sdom(m) from " + collection + " m'"
    string_sdoms = execute_query(command)
    result = []
    for string_sdom in string_sdoms:
      result.append(sdom_to_dim_list(string_sdom))
    return result          

def sdom_to_dim_list(string_sdom):
    result = []
    parts = string_sdom.replace('[', '').replace(']', '').split(',')
    for part in parts:
        low = part.split(':')[0]
        high = part.split(':')[1]
        result.append([low, high])
    return result    

def get_mdd_ids(directql, collection):
    command = directql + " --out string -q 'select (oid(m) - 1) / 512 from " + collection + " m'"
    return execute_query(command)

def get_domain_id(rasbase, mddId):
    command = "sqlite3 " + rasbase + " \"select DomainId from RAS_MDDOBJECTS where MDDId=" + mddId + "\""
    return subprocess.getoutput(command)

def get_dim_low(rasbase, domain_id, dim_count):
    command = "sqlite3 " + rasbase + " \"select Low from RAS_DOMAINVALUES where DomainId = " + domain_id + " and DimensionCount = " + dim_count + "\""
    return subprocess.getoutput(command)

def get_dim_high(rasbase, domain_id, dim_count):
    command = "sqlite3 " + rasbase + " \"select High from RAS_DOMAINVALUES where DomainId = " + domain_id + " and DimensionCount = " + dim_count + "\""
    return subprocess.getoutput(command)

def update_dim_low(rasbase, domain_id, dim_count, new_low):
    command = "sqlite3 " + rasbase + " \"begin immediate transaction; update RAS_DOMAINVALUES set Low = " + new_low + " where DomainId = " + domain_id + " and DimensionCount = " + dim_count  + "; commit\""
    subprocess.getoutput(command)

def update_dim_high(rasbase, domain_id, dim_count, new_high):
    command = "sqlite3 " + rasbase + " \"begin immediate transaction; update RAS_DOMAINVALUES set High = " + new_high + " where DomainId = " + domain_id + " and DimensionCount = " + dim_count  + "; commit\""
    subprocess.getoutput(command)    

def main():
  if len(sys.argv) != 3:
      usage()

  directql = sys.argv[1]
  rasbase = sys.argv[2]
  
  collections = get_collection_names(directql)
  for collection in collections:
      # sdoms of all objects in this collection
      sdoms = get_sdoms(directql, collection)
      # all mdd ids in this collection
      mdd_ids = get_mdd_ids(directql, collection)
      for index, mdd_id in enumerate(mdd_ids):
          # the domain id of this mdd object
          domain_id =  get_domain_id(rasbase, mdd_id)
          # the sdom of this object
          sdom = sdoms[index]
          # for each dimension, check if low and high correspond to the db value
          for dim in range(len(sdom)):
              dim_count = str(dim)
              sdom_low = sdom[dim][0]
              sdom_high = sdom[dim][1]
              db_low = get_dim_low(rasbase, domain_id, dim_count)
              db_high = get_dim_high(rasbase, domain_id, dim_count)
              if db_low != sdom_low:
                  print("Found inconsistent low sdom value for object with oid %s of collection %s on dimension %s" % (int(mdd_id)*512 + 1, collection, dim_count))
                  update_dim_low(rasbase, domain_id, dim_count, sdom_low)
                  print("Updated value from %s to %s" % (db_low, sdom_low))
              if db_high != sdom_high:
                  print("Found inconsistent low sdom value for object %s of collection %s on dimension %s" % (int(mdd_id)*512 + 1, collection, dim_count))
                  update_dim_high(rasbase, domain_id, dim_count, sdom_high)
                  print("Updated value from %s to %s" % (db_high, sdom_high))



main()


