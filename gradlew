#!/usr/bin/env bash
#
# سكربت تشغيل Gradle Wrapper.
# يحدّد مسار المشروع بشكل مطلق، ويحترم JAVA_HOME إن كان معرّفًا.

set -e

APP_HOME=$(cd -P "$(dirname "$0")" > /dev/null && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "خطأ: ملف gradle-wrapper.jar غير موجود في $WRAPPER_JAR" >&2
    exit 1
fi

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if ! command -v "$JAVACMD" > /dev/null 2>&1 && [ ! -x "$JAVACMD" ]; then
    echo "خطأ: تعذّر العثور على Java. ثبّت JDK 17 أو اضبط JAVA_HOME." >&2
    exit 1
fi

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

exec "$JAVACMD" $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=gradlew" \
    -classpath "$WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain "$@"
