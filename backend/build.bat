@echo off
REM ============================================
REM SDS MES Backend Build Script
REM ============================================
REM This script ensures Java 21 is used for building

echo ============================================
echo SDS MES Backend Build
echo ============================================
echo.

REM Set Java 21 path
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Verify Java version
echo Checking Java version...
java -version
echo.

REM Set Maven path
set "MAVEN_HOME=C:\apache-maven-3.9.11"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

REM Verify Maven version
echo Checking Maven version...
call "%MAVEN_HOME%\bin\mvn.cmd" -version
echo.

REM Build
echo Starting Maven build...
call "%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo BUILD SUCCESS
    echo ============================================
    echo JAR file: target\soice-mes-backend-0.1.0-SNAPSHOT.jar
    echo.
    echo To start the server, run: start.bat
    echo ============================================
) else (
    echo.
    echo ============================================
    echo BUILD FAILED
    echo ============================================
    echo Check the output above for errors
    echo ============================================
    exit /b 1
)
