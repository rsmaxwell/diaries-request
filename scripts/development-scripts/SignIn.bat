@echo off
setLocal EnableDelayedExpansion

set BASEDIR=%~dp0

pushd %BASEDIR%
set DEV_SCRIPT_DIR=%CD%
popd

pushd %DEV_SCRIPT_DIR%\..
set SCRIPT_DIR=%CD%
popd

pushd %SCRIPT_DIR%\..
set SUBPROJECT_DIR=%CD%
popd

pushd %SUBPROJECT_DIR%\..
set PROJECT_DIR=%CD%
popd




cd %PROJECT_DIR%

set CLASSPATH="%SUBPROJECT_DIR%\bin\main
set CLASSPATH=%CLASSPATH%;%SUBPROJECT_DIR%\src\main\resources
for /R %SUBPROJECT_DIR%\runtime %%a in (*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH=%CLASSPATH%"


java -classpath %CLASSPATH% com.rsmaxwell.diaries.request.SignInRequest ^
 --config %USERPROFILE%\.diaries\responder.json ^
 --username jblog --password 123456

