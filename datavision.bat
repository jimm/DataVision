@echo off
rem datavision.bat -- script to run Datavision under Windows with an appropriate
rem                   classpath.  If first parameter is -v, will output java
rem                   command line before running it.
rem                   NOTE: This does NOT work under Windows 95/98/NT.
rem                   It DOES work under Windows 2000/XP/Vista.

setlocal
setlocal ENABLEDELAYEDEXPANSION

rem Find out drive and directory where this script is,
rem so we can find the jar files underneath it.
set SCRIPTPATH=%~d0%~p0

rem DVCLASSPATH must contain paths to all needed classes for DV,
rem separated by semicolons.  All .jar files under lib
rem below the directory the .bat file is in are automatically added.
rem If you need others, either drop them in that directory, or add them
rem here manually (dropping them in the directory is better!).
set DVCLASSPATH=
rem Build the classpath
for %%Y in ("%SCRIPTPATH%"lib\*.jar) do set DVCLASSPATH=!DVCLASSPATH!;%%Y
rem Get rid of unwanted initial semicolon
SET DVCLASSPATH=%DVCLASSPATH:~1%

rem Output what we will execute, for debugging, if we have a -v switch
if not "X-vX" == "X%1X" goto :endif
  shift
  echo java -classpath "%DVCLASSPATH%" -Dswing.aatext=true jimm.datavision.DataVision %*
:endif

rem Run the relevant Java code with the appropriate classpath
java -classpath "%DVCLASSPATH%" -Dswing.aatext=true jimm.datavision.DataVision %*
