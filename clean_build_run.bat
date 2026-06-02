@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0clean_build_run.ps1"
if %ERRORLEVEL% neq 0 (
    echo.
    echo ===================================================
    echo TIEN TRINH THAT BAI VET CACH (Exit Code: %ERRORLEVEL%^)
    echo ===================================================
    pause
)
