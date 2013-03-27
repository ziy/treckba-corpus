#!/bin/sh
# Configure
PROJECT_HOME=/bos/tmp4/ziy/treckba-corpus
CORPUS_ROOT=/bos/tmp19/spalakod/kba-stream-corpus-2012
INDEX_ROOT=$PROJECT_HOME/index
LOG_ROOT=$PROJECT_HOME/log
DIR=2011-12-31-23
# Execute
cd $PROJECT_HOME
mvn -Dtreckba-corpus.collection.root=$CORPUS_ROOT \
    -Dtreckba-corpus.collection.dir=$DIR \
    -Dtreckba-corpus.index.root=$INDEX_ROOT \
    -Dtreckba-corpus.index.dir=$DIR \
    exec:java > $LOG_ROOT/$DIR.log
gzip $LOG_ROOT/$DIR.log
