@echo off
rem ***********************************************************************************
rem
rem	Filename:		cce_setup.bat
rem
rem	Description:	Initialise workspace environment for CCE
rem
rem ***********************************************************************************

set DIR=.metadata\.plugins\org.eclipse.core.runtime\.settings
mkdir %DIR% 2> NUL

set DST=%DIR%\org.eclipse.core.resources.prefs
set SRC_ROOT=C\:/satsw/projects/cc2520_app_ex/trunk/source
pushd ..\source
set SRC_ROOT=%CD:\=/%
popd

echo # %DATE% > %DST%
echo version=1 >> %DST%
echo eclipse.preferences.version=1 >> %DST%
echo pathvariable.TI_LPRF_SRC_ROOT=%SRC_ROOT%>> %DST%

echo *
echo *
echo * Generating Code Composer workspace settings
echo *
echo * TI_LPRF_SRC_ROOT=%SRC_ROOT%
echo *
echo *
echo.
echo.
set /P resp=Start Code Composer ? [Y/N] 

if %resp%==y start /d "C:\Program Files\Texas Instruments\CC Essentials v3\eclipse" eclipse.exe -data %CD% -nosplash

rem set /P=Hit any key to exit ... 
