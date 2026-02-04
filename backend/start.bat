@echo off
REM ============================================
REM SoIce MES Backend Start Script
REM ============================================
REM This script starts the backend server with Java 21

echo ============================================
echo SoIce MES Backend Server
echo ============================================
echo.

REM Set Java 21 path
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Check if JAR exists
if not exist "target\soice-mes-backend-0.1.0-SNAPSHOT.jar" (
    echo ERROR: JAR file not found!
    echo Please run build.bat first.
    echo.
    pause
    exit /b 1
)

REM Verify Java version
echo Java version:
java -version
echo.

REM Start server
echo Starting SoIce MES Backend Server...
echo Server will start on port 8080
echo Press Ctrl+C to stop the server
echo.
echo Log file: backend.log
echo ============================================
echo.

java -jar target\soice-mes-backend-0.1.0-SNAPSHOT.jar

pause
