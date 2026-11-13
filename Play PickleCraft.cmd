@echo off
setlocal
pushd "%~dp0" || exit /b 1
if exist "local-jdk.cmd" call "local-jdk.cmd"
call gradlew.bat runClient
set "result=%errorlevel%"
popd
if not "%result%"=="0" (
    echo.
    echo PickleCraft could not start. See the error above and the setup steps in README.md.
    pause
)
exit /b %result%
