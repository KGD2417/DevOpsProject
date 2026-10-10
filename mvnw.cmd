@echo off
rem Maven Wrapper for Windows
set MAVEN_PROJECTBASE=%~dp0
set MAVEN_WRAPPER_JAR=%~dp0..\.mvn\wrapper\maven-wrapper.jar

rem Default Maven version from properties
set MAVEN_VERSION=3.9.6

rem Find the wrapper jar
if not exist "%MAVEN_WRAPPER_JAR%" (
    echo ERROR: Maven wrapper jar not found at %MAVEN_WRAPPER_JAR%
    echo Ensure .mvn\wrapper\maven-wrapper.jar exists
    goto EOF
)

rem Execute the wrapper
java -jar "%MAVEN_WRAPPER_JAR%" "%*"
goto EOF