#!/usr/bin/env bash

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
#
#   Gradle start up script for UN*X
#
##############################################################################

APP_BASE_NAME=${0##*/}
APP_HOME=$(cd "${0%/*}" && pwd)

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

osname=$(uname -s)

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
    APP_HOME=$(cygpath --unix "$APP_HOME")
fi

# Attempt to set APP_HOME
if [ -z "$APP_HOME" ] ; then
    APP_HOME=$(dirname "$0")
    APP_HOME=$(cd "$APP_HOME" && pwd)
fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVA_CMD="$JAVA_HOME/jre/sh/java"
    else
        JAVA_CMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVA_CMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVA_CMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$osname" = "SunOS" ] ; then
    if [ -x /usr/bin/prctl ] ; then
        MAX_FD=$(/usr/bin/prctl -n process.max-file-descriptor -i process $$ 2>/dev/null | awk '{print $2}')
    elif [ -x /usr/bin/ulimit ] ; then
        MAX_FD=$(/usr/bin/ulimit -n)
    fi
elif [ "$osname" = "Linux" ] ; then
    MAX_FD=$(ulimit -n)
fi

if [ "$MAX_FD" != "maximum" -a "$MAX_FD" != "" ] ; then
    ulimit -n $MAX_FD || warn "Could not set maximum file descriptor limit: $MAX_FD"
fi

# Collect all arguments for the java command, following the shell quoting and substitution rules
eval set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" -classpath "\"$CLASSPATH\"" org.gradle.wrapper.GradleWrapperMain "$@"

# by default we should be in the correct project dir, but when run from Finder on Mac, the cwd is wrong
if [ "$osname" = "Darwin" ] && [ "$HOME" = "$PWD" ]; then
    cd "$APP_HOME"
fi

exec "$JAVA_CMD" "$@"
