#!/usr/bin/python

import sys
import os
import re


def analyze_expanded_terms(filepath):
  short_terms = set()
  pairs = set()
  for line in open(filepath):
    segs = line.split('\t')
    term = normalize(segs[0])
    if not all(ord(c) < 128 for c in term): continue
    if len(term) > 20:
      short_terms.add(term)
    topic = segs[1]
    pairs.add(term)
    pairs.add(topic)
    pairs.add((term, topic))
  print len(pairs)
  print len(short_terms)


def normalize(original):
  text = original
  # text = text.lower()
  text = re.sub('\s*\(.*?\)\s*', '', text)
  text = text.replace('Category:', '')
  text = text.replace('List of ', '')
  return text
  

def main():
  args = sys.argv[1:]
  if "-h" in args or "--help" in args:
    usage()
    sys.exit(2)
  analyze_expanded_terms(args[0])


def usage():
  print "Usage:  %s expanded_keyterm_file_path" \
         % os.path.basename(sys.argv[0])


if __name__=='__main__':
  main()

