#!/usr/bin/python

import sys
import os
import os.path
import shutil


def check_job_complete(dir_list_file, log_root):
  complete = []
  incomplete = []
  for dir in [d.strip() for d in open(dir_list_file)]:
    print "Read logs for %s" % dir
    if find_log(dir, log_root) and not find_error(dir, log_root) \
       and find_build_success(dir, log_root):
      complete.append(dir)
    else:
      incomplete.append(dir)
  return (complete, incomplete)


def find_log(dir, log_root):
  log_file = os.path.join(log_root, "%s.log" % dir)
  return os.path.isfile(log_file)


def find_error(dir, log_root):
  err_file = os.path.join(log_root, "%s.err" % dir)
  return any("Exception" in line for line in open(err_file))


def find_build_success(dir, log_root):
  log_file = os.path.join(log_root, "%s.log" % dir)
  with open(log_file, "r") as f:
    f.seek (0, 2)           # Seek @ EOF
    fsize = f.tell()        # Get Size
    f.seek (max (fsize-1024, 0), 0) # Set pos @ last n chars
    lines = f.readlines()       # Read to end
  lines = lines[-10:]    # Get last 10 lines
  return any("BUILD SUCCESS" in line for line in lines)


def remove(dir, log_root, index_root, data_root):
  print "Remove %s" % dir
  remove_log(dir, log_root)
  remove_index(dir, log_root)
  remove_data(dir, data_root)


def remove_log(dir, log_root):
  log_file = os.path.join(log_root, "%s.log" % dir)
  if os.path.isfile(log_file): os.remove(log_file)
  err_file = os.path.join(log_root, "%s.err" % dir)
  if os.path.isfile(err_file): os.remove(err_file)


def remove_index(dir, index_root):
  index = os.path.join(index_root, dir)
  if os.path.isdir(index): shutil.rmtree(index)


def remove_data(dir, data_root):
  data_file = os.path.join(data_root, dir)
  if os.path.isfile(data_file): os.remove(data_file)
  

def main():
  args = sys.argv[1:]
  if "-h" in args or "--help" in args:
    usage()
    sys.exit(2)
  (complete, incomplete) = check_job_complete(args[0], args[1])
  print "Unfinished jobs: %s" % len(incomplete)
  for dir in incomplete: remove(dir, args[1], args[2], args[3])
  pass


def usage():
  print "Usage:  %s dir_list_file log_root index_root data_root" % \
        os.path.basename(sys.argv[0])


if __name__=='__main__':
  main()

