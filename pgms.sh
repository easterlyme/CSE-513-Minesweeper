#! /bin/sh
# Assumes jar file is relative to this file
COMMAND=`command -v "$0"`
JAR=`dirname "$COMMAND"`/pgms.jar

exec java -jar $JAR "$@"
