@echo off
REM ============================================================================
REM Build Tools - Ban Hoa Qua Online
REM Shortcut commands for build management
REM ============================================================================

setlocal enabledelayedexpansion

if not "%~1"=="" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" "%~1"
    pause
    exit /b 0
)

:menu
cls
echo.
echo ============================================================================
echo                      BUILD TOOLS - BAN HOA QUA ONLINE
echo ============================================================================
echo.
echo  [0] CHAY TOAN BO QUY TRINH (Clean, Build, Deploy, Run & Open)
echo  [1] Kiem tra trang thai (status)
echo  [2] Xoa cache build (clean-all)
echo  [3] Xoa log (clean-logs)
echo  [4] Dung Tomcat (kill-tomcat)
echo  [5] Xem log gan day (logs)
echo  [6] Dat lai toan bo (reset)
echo  [7] Cau hinh Tomcat/Java (install-config)
echo  [8] Mo ung dung tren web (open)
echo  [9] Tro giup (help)
echo  [10] Hot-Reload Tomcat (Recompile nhanh, giu Session)
echo  [11] [DOCKER] Build Image (docker-build)
echo  [12] [DOCKER] Run / Startup App (docker-up)
echo  [13] [DOCKER] Stop Container (docker-down)
echo  [14] [DOCKER] View Logs (docker-logs)
echo  [15] [DOCKER] Full Reset (docker-reset)
echo  [16] Thoat (exit)
echo.
echo ============================================================================
set /p choice="Nhap lua chon cua ban (0-16): "

if "%choice%"=="0" (
    start "Ban Hoa Qua Online - Build & Deploy" powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" deploy
    goto menu
)
if "%choice%"=="1" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" status
    pause
    goto menu
)
if "%choice%"=="2" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" clean-all
    pause
    goto menu
)
if "%choice%"=="3" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" clean-logs
    pause
    goto menu
)
if "%choice%"=="4" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" kill-tomcat
    pause
    goto menu
)
if "%choice%"=="5" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" logs
    pause
    goto menu
)
if "%choice%"=="6" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" reset
    pause
    goto menu
)
if "%choice%"=="7" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" install-config
    pause
    goto menu
)
if "%choice%"=="8" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" open
    pause
    goto menu
)
if "%choice%"=="9" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" help
    pause
    goto menu
)
if "%choice%"=="10" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" reload
    pause
    goto menu
)
if "%choice%"=="11" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" docker-build
    pause
    goto menu
)
if "%choice%"=="12" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" docker-up
    pause
    goto menu
)
if "%choice%"=="13" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" docker-down
    pause
    goto menu
)
if "%choice%"=="14" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" docker-logs
    pause
    goto menu
)
if "%choice%"=="15" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-tools.ps1" docker-reset
    pause
    goto menu
)
if "%choice%"=="16" (
    exit /b 0
)

echo Lua chon khong hop le, vui long thu lai!
timeout /t 2 >nul
goto menu
