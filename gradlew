#!/usr/bin/env bash

# Set the Gradle user home to avoid permission issues
export GRADLE_USER_HOME="$HOME/.gradle"

# The directory where this script is located
APP_HOME=$(dirname "$0")
exec java -cp "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"