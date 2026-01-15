#!/bin/bash

##############################################################################
# Gradle Wrapper Bootstrap Script
##############################################################################

# Determine the script directory
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)

# Download Gradle wrapper jar if it doesn't exist
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "Downloading Gradle Wrapper..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -o "$GRADLE_WRAPPER_JAR"
fi

# Set default JVM options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Execute Gradle
exec "$JAVACMD" $DEFAULT_JVM_OPTS -jar "$GRADLE_WRAPPER_JAR" "$@"
