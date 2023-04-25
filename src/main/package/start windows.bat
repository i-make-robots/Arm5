:: special thanks to http://rgagnon.com/javadetails/java-0642.html
:: http://stackoverflow.com/questions/638301/discover-from-a-batch-file-where-is-java-installed
@ECHO off
cls
setlocal ENABLEEXTENSIONS
::
:: get the current java version
::

:CheckOS

IF EXIST "%PROGRAMFILES(X86)%" (GOTO 64bit) ELSE (GOTO 32bit)

:64bit
@ECHO 64 bit Windows
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\Wow6432Node\JavaSoft\JDK" /v CurrentVersion') DO set CurVer=%%B
IF DEFINED "%CurVer%" (GOTO 32in64) ELSE (GOTO 64in64)

:32in64
@ECHO 32 bit Java installed on 64 bit Windows.  Whoops?
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\Wow6432Node\JavaSoft\JDK\%CurVer%" /v JavaHome') DO set JAVA_HOME=%%B
GOTO endos

:64in64
@ECHO 64 bit Java installed.
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\JDK" /v CurrentVersion') DO set CurVer=%%B
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\JDK\%CurVer%" /v JavaHome') DO set JAVA_HOME=%%B
GOTO endos

:32bit
@ECHO 32 bit Windows
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\JDK" /v CurrentVersion') DO set CurVer=%%B
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\JDK\%CurVer%" /v JavaHome') DO set JAVA_HOME=%%B

:endos
IF DEFINED JAVA_HOME (
  @ECHO The current Java runtime is %CurVer%
) ELSE (
  @ECHO Java not found.  Please recheck or reinstall Java and try again.
  GOTO end
)

@ECHO finding the latest RobotOverlord JAR...
FOR %%G in (RobotOverlord*.jar) DO SET RobotOverlord=%%G & GOTO continue


IF NOT DEFINED "%RobotOverlord%" (
  @ECHO File not found.
  GOTO end
)

:continue
@ECHO Found %RobotOverlord%

:: -Dsun.java2d.dpiaware=false may also help on some monitors
@ECHO "%JAVA_HOME%\bin\java.exe" -jar "%RobotOverlord%"
"%JAVA_HOME%\bin\java.exe" -jar "%RobotOverlord%"
GOTO endQuiet

:end
@pause

:endQuiet
@pause