@echo off
rem ==================================================================================
rem  One-time setup of the Git repository for Village Census.
rem  - puts the GitHub Actions workflow into .github\workflows\
rem  - creates the repository (branch "main") and the first commit
rem  Nothing is pushed or published. To publish later:
rem      git remote add origin https://github.com/alexkond12/VillageCensus.git
rem      git push -u origin main
rem ==================================================================================
setlocal
cd /d "%~dp0.."
where git >nul 2>nul || (echo Git is not installed: https://git-scm.com/download/win & pause & exit /b 1)
if exist .git (echo This folder is already a Git repository. & pause & exit /b 0)

if not exist .github\workflows mkdir .github\workflows
copy /y ci\github-workflow-build.yml .github\workflows\build.yml >nul

git init -b main || (pause & exit /b 1)
for /f "delims=" %%N in ('git config user.name') do set GIT_NAME=%%N
if not defined GIT_NAME git config user.name "alexkond12"
for /f "delims=" %%E in ('git config user.email') do set GIT_EMAIL=%%E
if not defined GIT_EMAIL git config user.email "alexkond12@users.noreply.github.com"

git add -A
git commit -m "Initial commit: Village Census 1.0.0" || (pause & exit /b 1)
echo.
echo Done. Repository created with one commit on branch "main".
pause
endlocal
