@echo off
rem ==================================================================================
rem  Builds Village Census for every supported Minecraft version and every loader.
rem  The finished jars are collected in the dist\ folder.
rem  Usage: scripts\build-all.bat   (run from the project root or from this folder)
rem ==================================================================================
setlocal
cd /d "%~dp0.."
call gradlew.bat cleanDist || exit /b 1
for %%V in (26.1.2 26.2 26.3) do (
    echo.
    echo ===== Building for Minecraft %%V =====
    call gradlew.bat build -Pmc_target=%%V || exit /b 1
)
echo.
echo All jars are in %CD%\dist
dir /b dist
endlocal
