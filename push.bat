@echo off
chcp 65001
cd /d %~dp0

echo ==========================================
echo   Android Shop - Push Script (SSH)
echo   Repo root: %CD%
echo ==========================================
echo.

if not exist ".git" (
    echo [ERROR] .git NOT FOUND in %CD%.
    echo   Run this script inside the Android shop project folder.
    pause
    exit /b 1
)

echo [1/5] git status -sb (current changes)
git status -sb
echo.

echo [2/5] git add .
git add .
echo   Staged.
echo.

set COMMIT_MSG=Update
if "%~1"=="" goto NO_MSG
    set COMMIT_MSG=%~1
    echo   Custom msg: %COMMIT_MSG%
    goto AFTER_MSG
:NO_MSG
    echo   Default msg: %COMMIT_MSG%
    echo   (Tip: push.bat "my custom msg" to customize)
:AFTER_MSG
echo.

echo [3/5] git commit -m "%COMMIT_MSG%"
git commit -m "%COMMIT_MSG%"
set COMMIT_RC=%ERRORLEVEL%
echo.

if "%COMMIT_RC%"=="1" goto NOTHING_TO_COMMIT
if "%COMMIT_RC%"=="0" goto COMMIT_OK
    echo [ERROR] commit FAILED (exit=%COMMIT_RC%). Paused.
    pause
    exit /b %COMMIT_RC%

:NOTHING_TO_COMMIT
    echo [INFO] Nothing to commit - working tree is clean.
    echo   Skip commit, continue to pull/push sync.
    goto PULL_STAGE

:COMMIT_OK
    echo   Commit OK.
    echo.

:PULL_STAGE
echo [4/5] git pull --rebase origin master (avoid "fetch first" reject)
git pull --rebase origin master
set PULL_RC=%ERRORLEVEL%
echo.

if "%PULL_RC%"=="0" goto PULL_OK
    echo [ERROR] pull --rebase FAILED (exit=%PULL_RC%).
    echo   Fixes:
    echo   - Rebase conflict -^> resolve conflicts, then:
    echo       git add ^<files^>
    echo       git rebase --continue
    echo     (or abort: git rebase --abort)
    echo   - SSH/network -^> check: ssh -T git@github.com
    pause
    exit /b %PULL_RC%

:PULL_OK
    echo   Pull OK.
    echo.

echo [5/5] git push origin master via SSH (git@github.com:ZetaUs/Android.git)
git push origin master
set PUSH_RC=%ERRORLEVEL%
echo.

if "%PUSH_RC%"=="0" goto PUSH_OK
    echo [ERROR] push FAILED (exit=%PUSH_RC%).
    echo   Classification:
    echo   1. "Permission denied (publickey)" - SSH pub key missing on GitHub.
    echo      Paste %USERPROFILE%\.ssh\id_ed25519.pub into GitHub -^> Settings -^> SSH keys.
    echo   2. "rejected ... non-fast-forward" - remote still has newer commits after pull.
    echo      Re-run this script or re-run: git pull --rebase
    echo   3. "Repository not found" - remote URL wrong or no access.
    echo      Verify: git remote -v should show git@github.com:ZetaUs/Android.git
    echo   4. Network/timeout - retry or run: ssh -T git@github.com
    pause
    exit /b %PUSH_RC%

:PUSH_OK
echo ==========================================
echo   ALL DONE. Push SUCCESS via SSH.
echo ==========================================
git log -1 --oneline --decorate
echo.
pause
exit /b 0
