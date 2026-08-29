#!/usr/bin/env bash

# Set the Gradle user home to avoid permission issues
export GRADLE_USER_HOME="$HOME/.gradle"

# The directory where this script is located
PRG="$0"

# Resolve symlinks
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >&-
APP_HOME="`pwd -P`"
cd "$SAVED" >&-

APP_NAME="Gradle Wrapper"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/bin/java" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
  else
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVACMD="java"
  which java >/dev/null 2>&1 || { echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2; exit 1; }
fi

# Collect all arguments for the java command, following the shell quoting and querying rules
set --
for a in "$DEFAULT_JVM_OPTS" "$JAVA_OPTS" "$GRADLE_OPTS"; do
  # Add the arguments
  set -- "$@" "$a"
done
# Use the gradle wrapper jar
set -- "$@" -cp "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"

# Save IFS, for restoring
SAVED_IFS="$IFS"
IFS=$'\n'
# Split the JVM_OPTS and GRADLE_OPTS
set -- $JAVA_OPTS $GRADLE_OPTS $DEFAULT_JVM_OPTS "$@"
IFS="$SAVED_IFS"

exec "$JAVACMD" "$@"
EOF