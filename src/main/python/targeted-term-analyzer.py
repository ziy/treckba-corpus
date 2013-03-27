#!/usr/bin/python

import sys
import os
import re
from collections import Counter


def count_terms(filepath):
  lines = (line.strip() for line in open(filepath) 
                        if line.startswith("{{Target}}"))
  counters = (Counter(line[12:-1].split(', ')) for line in lines)
  return sum(counters, Counter())


def align_expanded_terms(filepath, lower, terms):
  for line in open(filepath):
    term = normalize(line.split('\t')[0], lower)
    if term in terms:
      print '[%s]\t%s' % (term, line.strip())


def normalize(original, lower):
  text = original
  if lower.lower() == 'true': text = text.lower()
  text = re.sub('\s*\(.*?\)\s*', '', text)
  text = text.replace('Category:', '')
  text = text.replace('List of ', '')
  return text
  

def main():
  args = sys.argv[1:]
  if "-h" in args or "--help" in args:
    usage()
    sys.exit(2)
  term2freq = count_terms(args[0])
  print '# term freq'
  for k, v in term2freq.items(): print '%s: %s' % (k, v)
  terms = set([pair[0] for pair in term2freq.most_common(20)])
  print '# corresponding expanded term line'
  align_expanded_terms(args[1], args[2], terms)


def usage():
  print "Usage:  %s log_file_path expanded_keyterm_file_path lower_case" \
        % os.path.basename(sys.argv[0])


if __name__=='__main__':
  main()

