@echo off
setlocal
pushd "%~dp0" || exit /b 1
rem Sync committed work only. Fast-forward pulls preserve the local branch on divergence.
git pull --ff-only && git push
set "result=%errorlevel%"
popd
if not "%result%"=="0" pause
exit /b %result%
