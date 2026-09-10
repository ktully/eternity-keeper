@echo off
setlocal

rem Run from this script's own directory so the relative paths below resolve no matter where
rem it was invoked from.
cd /d "%~dp0"

rem The bundled JCEF libraries are architecture specific and have to match the bitness of the
rem JVM rather than that of the OS. Pass win32 as the first argument for a 32-bit JDK.
set "ARCH=%~1"
if "%ARCH%"=="" set "ARCH=win64"

set "NATIVE=%CD%\lib\native\%ARCH%"
if not exist "%NATIVE%\jcef.dll" (
	echo Native libraries not found in "%NATIVE%".
	echo Valid options are win32 and win64, given as the first argument, e.g. run.bat win32
	exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
	echo Could not find java on PATH. A JDK 8 matching %ARCH% is needed.
	exit /b 1
)

rem Discover the jar rather than hard-coding it so this keeps working when the version in
rem pom.xml changes. The pattern already anchors at the start of the name, so the pre-shade
rem original-eternity-<version>.jar that maven-shade-plugin leaves behind is not a candidate;
rem the filter below is only belt and braces in case finalName ever changes.
set "JAR="
for /f "delims=" %%f in ('dir /b /a-d "target\eternity-*.jar" 2^>nul ^| findstr /v /b /c:"original-"') do set "JAR=target\%%f"

if not defined JAR (
	echo No built jar found in target\. Build one first with:
	echo     mvn install -P%ARCH%
	exit /b 1
)

rem java.library.path only covers the libraries the JVM loads itself. libcef.dll is pulled in
rem by the Windows loader as a dependency of jcef.dll, and that search consults PATH.
set "PATH=%NATIVE%;%PATH%"

java -Djava.library.path="%NATIVE%" -jar "%JAR%"
