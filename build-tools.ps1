# Build Management Tools - Ban Hoa Qua Online
# Su dung: powershell -ExecutionPolicy Bypass -File build-tools.ps1 [option]

param(
    [string]$Action = "help"
)

$LogFile = "build_tools.log"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# ==================== CONFIGURATION LOADING & AUTO-DETECTION ====================
$JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
$JRE_HOME = "C:\Program Files\Java\jdk-26.0.1"
$CATALINA_HOME = ""
$CATALINA_BASE = ""

# 1. Try to load from NetBeans private properties (Highly recommended as the user uses NetBeans)
if (Test-Path "nbproject/private/private.properties") {
    Get-Content "nbproject/private/private.properties" | ForEach-Object {
        $line = $_.Trim()
        if ($line.StartsWith("j2ee.server.home=")) {
            $path = $line.Substring("j2ee.server.home=".Length).Replace("\\", "/").Replace("\", "/")
            if (Test-Path $path) {
                $CATALINA_HOME = $path
            }
        }
    }
}

# 2. Load config from ini if exists
if (Test-Path "tomcat_config.ini") {
    Get-Content "tomcat_config.ini" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $parts = $line.Split('=', 2)
            if ($parts.Length -eq 2) {
                $key = $parts[0].Trim()
                $val = $parts[1].Trim()
                if ($key -eq "JAVA_HOME") { $JAVA_HOME = $val }
                elseif ($key -eq "CATALINA_HOME" -and $val) { $CATALINA_HOME = $val }
                elseif ($key -eq "CATALINA_BASE" -and $val -ne "auto-detect") { $CATALINA_BASE = $val }
            }
        }
    }
}

# 3. Auto-detect Tomcat Home if not set yet
if (-not $CATALINA_HOME -or -not (Test-Path "$CATALINA_HOME\bin\startup.bat")) {
    $commonPaths = @(
        "C:\Users\Admin\Downloads\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55",
        "C:\Program Files\Apache Software Foundation\Tomcat 10.1",
        "C:\apache-tomcat-10.1.55",
        "C:\Program Files\Apache Software Foundation\Tomcat 10.0",
        "${env:ProgramFiles}\Apache Software Foundation\Tomcat 10.1"
    )
    foreach ($path in $commonPaths) {
        if (Test-Path "$path\bin\startup.bat") {
            $CATALINA_HOME = $path
            break
        }
    }
}

# 4. Auto-detect Tomcat Base (Default to CATALINA_HOME if no custom base is found)
if (-not $CATALINA_BASE -or -not (Test-Path "$CATALINA_BASE\conf")) {
    $commonBases = @(
        "C:\Users\Admin\.tomcat"
    )
    foreach ($base in $commonBases) {
        if (Test-Path "$base\conf") {
            $CATALINA_BASE = $base
            break
        }
    }
    
    if (-not $CATALINA_BASE) {
        $CATALINA_BASE = $CATALINA_HOME
    }
}

# Set environment variables so Tomcat startup.bat and commands see them
$env:JAVA_HOME = $JAVA_HOME
$env:JRE_HOME = $JRE_HOME
if ($CATALINA_HOME) { $env:CATALINA_HOME = $CATALINA_HOME }
if ($CATALINA_BASE) { $env:CATALINA_BASE = $CATALINA_BASE }
$env:PATH = "$JAVA_HOME\bin;" + $env:PATH

function Log-Message {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [$Level] $Message"
    Add-Content -Path $LogFile -Value $logEntry
    Write-Host $logEntry
}

function Show-Help {
    Write-Host @"
====================================================================
BUILD AND DEPLOY MANAGEMENT TOOLS
====================================================================

Cach su dung: powershell -ExecutionPolicy Bypass -File build-tools.ps1 [option]

Cac tuy chon:
  help           - Hien thi tro giup nay
  status         - Kiem tra trang thai Tomcat va cac port
  clean-all      - Xoa toan bo cache va build output
  clean-logs     - Xoa toan bo log files
  kill-tomcat    - Kill Tomcat process va giai phong port
  logs           - Hien thi recent logs
  reset          - Dat lai toan bo va xoa lock file
  install-config - Cau hinh lai Tomcat paths
  open           - Mo ung dung trong browser (http://localhost:8080/Ban_Hoa_Qua_Online/)
  deploy         - Chay toan bo quy trinh (Clean, Build, Deploy, Run & Watch)

Vi du:
  powershell -ExecutionPolicy Bypass -File build-tools.ps1 status
  powershell -ExecutionPolicy Bypass -File build-tools.ps1 deploy

====================================================================
"@
}

function Show-Status {
    Write-Host "`n===== TOMCAT and PORT STATUS =====" -ForegroundColor Cyan
    
    # Check ports
    $ports = @(8080, 8005, 5005)  # 5005 for debug port
    foreach ($port in $ports) {
        $connection = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($connection) {
            Write-Host "[OK] Port $($port): IN USE (PID: $($connection.OwningProcess))" -ForegroundColor Green
        } else {
            Write-Host "[FREE] Port $($port): FREE" -ForegroundColor Yellow
        }
    }
    
    # Check Tomcat directories
    Write-Host "`n===== CACHE and BUILD DIRECTORIES =====" -ForegroundColor Cyan
    
    # Safely get CATALINA_BASE
    $basePath = ""
    if ($env:CATALINA_BASE) { $basePath = $env:CATALINA_BASE }
    
    $dirs = @(
        "build/web",
        "build/generated-sources"
    )
    if ($basePath) {
        $dirs += "$basePath/work"
        $dirs += "$basePath/temp"
        $dirs += "$basePath/logs"
    }
    
    foreach ($dir in $dirs) {
        if (Test-Path $dir) {
            $files = Get-ChildItem -Path $dir -Recurse -File -ErrorAction SilentlyContinue
            $size = 0
            if ($files) {
                $size = ($files | Measure-Object -Property Length -Sum).Sum / 1MB
            }
            if (-not $size) { $size = 0 }
            Write-Host "[OK] $dir : $([math]::Round($size, 2)) MB" -ForegroundColor Green
        } else {
            Write-Host "[NOT FOUND] $dir : NOT EXISTS" -ForegroundColor Yellow
        }
    }
    
    # Check log files
    Write-Host "`n===== LOG FILES =====" -ForegroundColor Cyan
    $logFiles = @(
        "build_deploy.log",
        "build_errors.log",
        "build_debug.log",
        "build.lock"
    )
    
    foreach ($log in $logFiles) {
        if (Test-Path $log) {
            $lines = (Get-Content $log | Measure-Object -Line).Lines
            Write-Host "[OK] $log : $lines lines" -ForegroundColor Green
        }
    }
}

function Clean-All {
    Write-Host "`n===== CLEANING ALL CACHE and BUILD OUTPUT =====" -ForegroundColor Yellow
    
    $basePath = ""
    if ($env:CATALINA_BASE) { $basePath = $env:CATALINA_BASE }
    
    $dirs = @(
        "build/web/WEB-INF/classes"
    )
    if ($basePath) {
        $dirs += "$basePath/work"
        $dirs += "$basePath/temp"
        $dirs += "$basePath/webapps/Ban_Hoa_Qua_Online"
    }
    
    foreach ($dir in $dirs) {
        if (Test-Path $dir) {
            Write-Host "Removing: $dir" -ForegroundColor Yellow
            Remove-Item -Path $dir -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
    
    Write-Host "Cleanup completed!" -ForegroundColor Green
    Log-Message "Clean-all executed"
}

function Clean-Logs {
    Write-Host "`n===== CLEANING LOG FILES =====" -ForegroundColor Yellow
    
    $logFiles = @(
        "build_deploy.log",
        "build_errors.log",
        "build_debug.log",
        "compile_watch.log",
        "compile_output.log"
    )
    
    foreach ($log in $logFiles) {
        if (Test-Path $log) {
            Remove-Item $log -Force
            Write-Host "Removed: $log" -ForegroundColor Yellow
        }
    }
    
    Write-Host "Log cleanup completed!" -ForegroundColor Green
    Log-Message "Clean-logs executed"
}

function Kill-Tomcat {
    Write-Host "`n===== KILLING TOMCAT PROCESS =====" -ForegroundColor Yellow
    
    $ports = @(8080, 8005)
    foreach ($port in $ports) {
        $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($connections) {
            foreach ($conn in $connections) {
                Write-Host "Killing process $($conn.OwningProcess) on port $port" -ForegroundColor Yellow
                Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            }
        }
    }
    
    Write-Host "Tomcat process killed!" -ForegroundColor Green
    Log-Message "Kill-tomcat executed"
}

function Show-Logs {
    Write-Host "`n===== RECENT LOGS =====" -ForegroundColor Cyan
    
    if (Test-Path "build_deploy.log") {
        Write-Host "`n--- MAIN LOG (last 30 lines) ---" -ForegroundColor Green
        Get-Content "build_deploy.log" -Tail 30 | Write-Host
    }
    
    if (Test-Path "build_errors.log") {
        Write-Host "`n--- ERROR LOG ---" -ForegroundColor Red
        Get-Content "build_errors.log" | Write-Host
    }
}

function Reset-All {
    Write-Host "`n===== RESETTING ALL =====" -ForegroundColor Red
    Write-Host "This will:" -ForegroundColor Yellow
    Write-Host "  1. Kill Tomcat" -ForegroundColor Yellow
    Write-Host "  2. Clean all cache" -ForegroundColor Yellow
    Write-Host "  3. Remove build.lock" -ForegroundColor Yellow
    Write-Host "  4. Clean log files" -ForegroundColor Yellow
    Write-Host ""
    
    $confirm = Read-Host "Are you sure? (yes/no)"
    if ($confirm -eq "yes") {
        Kill-Tomcat
        Start-Sleep -Seconds 2
        Clean-All
        Clean-Logs
        if (Test-Path "build.lock") {
            Remove-Item "build.lock" -Force
            Write-Host "Removed build.lock" -ForegroundColor Green
        }
        Write-Host "`nReset completed! Run clean_build_run.bat de rebuild." -ForegroundColor Green
        Log-Message "Reset-all executed"
    } else {
        Write-Host "Reset cancelled" -ForegroundColor Yellow
    }
}

function Install-Config {
    Write-Host "`n===== TOMCAT CONFIGURATION =====" -ForegroundColor Cyan
    Write-Host "Enter Tomcat paths (or press Enter to skip):" -ForegroundColor Yellow
    Write-Host ""
    
    $java = Read-Host "Java Home [C:\Program Files\Java\jdk-26.0.1]"
    $catalina = Read-Host "Tomcat Home [C:\apache-tomcat-10.1.55]"
    $base = Read-Host "Tomcat Instance [leave empty to auto-detect]"
    
    $config = "# Tomcat Configuration - Generated $(Get-Date)`r`n" +
              "JAVA_HOME=$($java -or 'C:\Program Files\Java\jdk-26.0.1')`r`n" +
              "CATALINA_HOME=$($catalina -or 'C:\apache-tomcat-10.1.55')`r`n" +
              "CATALINA_BASE=$($base -or 'auto-detect')"
    
    $config | Out-File "tomcat_config.ini" -Encoding utf8
    Write-Host "Configuration saved to tomcat_config.ini" -ForegroundColor Green
    Log-Message "Install-config executed"
}

function Open-App {
    $appUrl = "http://localhost:8080/Ban_Hoa_Qua_Online/"
    Write-Host "`nOpening application..." -ForegroundColor Cyan
    Write-Host "URL: $appUrl" -ForegroundColor Green
    
    try {
        $response = Invoke-WebRequest -Uri $appUrl -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "[OK] Application is running!" -ForegroundColor Green
        }
    } catch {
        Write-Host "[WARN] Application may not be running. Starting browser anyway..." -ForegroundColor Yellow
    }
    
    Start-Process $appUrl
    Write-Host "Browser opened!" -ForegroundColor Green
    Log-Message "Open-app executed"
}

function Deploy-App {
    Write-Host "`n===== [1/6] STOPPING TOMCAT AND RELEASING PORTS =====" -ForegroundColor Green
    Kill-Tomcat
    
    Write-Host "`n===== [2/6] CLEANING TOMCAT CACHE =====" -ForegroundColor Green
    Clean-All
    
    # Recreate essential Tomcat folders if Base path exists
    $basePath = ""
    if ($env:CATALINA_BASE) { $basePath = $env:CATALINA_BASE }
    if ($basePath) {
        Write-Host "Recreating Tomcat directories under $basePath..." -ForegroundColor Yellow
        New-Item -ItemType Directory -Path "$basePath/temp" -Force | Out-Null
        New-Item -ItemType Directory -Path "$basePath/work" -Force | Out-Null
        New-Item -ItemType Directory -Path "$basePath/logs" -Force | Out-Null
    }

    # Detect Tomcat home
    $tomcatHome = ""
    if ($env:CATALINA_HOME) { $tomcatHome = $env:CATALINA_HOME }
    if (-not $tomcatHome) {
        Write-Host "[ERROR] CATALINA_HOME is not set and could not be auto-detected!" -ForegroundColor Red
        return
    }

    Write-Host "`n===== [3/6] SYNCING WEB FILES AND COMPILING JAVA =====" -ForegroundColor Green
    $webSrcDir = "web"
    $webBuildDir = "build/web"

    if (!(Test-Path $webBuildDir)) {
        New-Item -ItemType Directory -Path $webBuildDir -Force | Out-Null
    } else {
        Get-ChildItem -Path $webBuildDir -Recurse -Force | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
    }

    if (Test-Path $webSrcDir) {
        Write-Host "Syncing web files..." -ForegroundColor Yellow
        # Robocopy command
        $robocopyProcess = Start-Process -FilePath "robocopy" `
            -ArgumentList "`"$webSrcDir`"", "`"$webBuildDir`"", "/s", "/e", "/xd", "WEB-INF\classes", "/w:1", "/r:1", "/ndl", "/nfl" `
            -PassThru -Wait -NoNewWindow
    }

    $classesDir = "$webBuildDir/WEB-INF/classes"
    New-Item -ItemType Directory -Path $classesDir -Force | Out-Null

    $javaFiles = Get-ChildItem -Path "src/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
    if (-not $javaFiles) {
        Write-Host "[ERROR] No Java files found in src/java!" -ForegroundColor Red
        return
    }

    Write-Host "Found $($javaFiles.Count) Java source files. Compiling..." -ForegroundColor Yellow
    [System.IO.File]::WriteAllLines("sources.txt", $javaFiles)

    $classpath = "web/WEB-INF/lib/*;$tomcatHome/lib/*"
    $compileLogFile = "compile_output.log"
    if (Test-Path $compileLogFile) { Remove-Item $compileLogFile -Force }

    $javacCmd = "javac -encoding UTF-8 -g:none -nowarn -target 17 -source 17 -cp `"$classpath`" -d `"$classesDir`" @sources.txt > `"$compileLogFile`" 2>&1"
    cmd.exe /c $javacCmd

    if ($LASTEXITCODE -ne 0) {
        $compileErrors = Get-Content $compileLogFile -Raw -ErrorAction SilentlyContinue
        Write-Host "=================== COMPILATION ERRORS ===================" -ForegroundColor Red
        Write-Host $compileErrors -ForegroundColor Red
        Write-Host "=========================================================" -ForegroundColor Red
        return
    }
    if (Test-Path $compileLogFile) { Remove-Item $compileLogFile -Force }
    Write-Host "Compilation completed successfully!" -ForegroundColor Green

    Write-Host "`n===== [4/6] CREATING TOMCAT CONTEXT XML =====" -ForegroundColor Green
    if ($basePath) {
        $contextConfDir = "$basePath/conf/Catalina/localhost"
        if (!(Test-Path $contextConfDir)) {
            New-Item -ItemType Directory -Path $contextConfDir -Force | Out-Null
        }
        $contextXmlPath = "$contextConfDir/Ban_Hoa_Qua_Online.xml"
        $docBaseAbsolute = (Get-Item "build/web").FullName
        $contextContent = "<?xml version=`"1.0`" encoding=`"UTF-8`"?>`r`n" +
                          "<Context docBase=`"$($docBaseAbsolute.Replace('\', '/'))`" path=`"/Ban_Hoa_Qua_Online`" reloadable=`"true`" useHttpOnly=`"true`"/>"
        [System.IO.File]::WriteAllText($contextXmlPath, $contextContent)
        Write-Host "Context XML updated at: $contextXmlPath" -ForegroundColor Yellow
    }

    Write-Host "`n===== [5/6] STARTING TOMCAT SERVER =====" -ForegroundColor Green
    Start-Process -FilePath "$tomcatHome\bin\startup.bat" -NoNewWindow
    Write-Host "Tomcat startup script executed." -ForegroundColor Yellow

    # Wait for Tomcat to start
    Write-Host "Waiting for port 8080 (max 20 seconds)..." -ForegroundColor Yellow
    $tomcatStarted = $false
    $startTime = Get-Date
    while ((Get-Date) - $startTime -lt [TimeSpan]::FromSeconds(20)) {
        $connection = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
        if ($connection) {
            $tomcatStarted = $true
            break
        }
        Start-Sleep -Milliseconds 500
    }

    if ($tomcatStarted) {
        Write-Host "[OK] Tomcat is up and running on port 8080!" -ForegroundColor Green
        Start-Sleep -Seconds 1
        Open-App
    } else {
        Write-Host "[WARN] Tomcat port 8080 did not open in time. Trying to open app anyway..." -ForegroundColor Yellow
        Open-App
    }

    Write-Host "`n===== [6/6] STARTING WATCH MODE (Press Ctrl+C to stop) =====" -ForegroundColor Green
    
    # Watch loop
    $lastChecksum = ""
    $isBuildInProgress = $false
    
    while ($true) {
        Start-Sleep -Seconds 2
        if ($isBuildInProgress) { continue }
        
        # Simple watcher: calculate combined last write times
        $files = Get-ChildItem -Path "src/java", "web" -Recurse -File -ErrorAction SilentlyContinue
        $currentChecksum = ($files | ForEach-Object { $_.LastWriteTime.Ticks }) -join ","
        
        if ($lastChecksum -eq "") {
            $lastChecksum = $currentChecksum
            continue
        }
        
        if ($currentChecksum -ne $lastChecksum) {
            $isBuildInProgress = $true
            Write-Host "`n[WATCH] Change detected. Recompiling & Syncing..." -ForegroundColor Yellow
            $lastChecksum = $currentChecksum
            
            try {
                # Sync web
                if (Test-Path $webSrcDir) {
                    Start-Process -FilePath "robocopy" `
                        -ArgumentList "`"$webSrcDir`"", "`"$webBuildDir`"", "/s", "/e", "/xd", "WEB-INF\classes", "/w:1", "/r:1", "/ndl", "/nfl" `
                        -Wait -NoNewWindow
                }
                # Compile Java
                $javaFiles = Get-ChildItem -Path "src/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
                [System.IO.File]::WriteAllLines("sources.txt", $javaFiles)
                cmd.exe /c $javacCmd
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "[WATCH] Recompile and sync completed successfully!" -ForegroundColor Green
                } else {
                    Write-Host "[WATCH] Recompile failed!" -ForegroundColor Red
                }
            } catch {
                Write-Host "[WATCH] Error during sync: $_" -ForegroundColor Red
            } finally {
                $isBuildInProgress = $false
            }
        }
    }
}

# Main execution
switch ($Action.ToLower()) {
    "help" { Show-Help }
    "status" { Show-Status }
    "clean-all" { Clean-All }
    "clean-logs" { Clean-Logs }
    "kill-tomcat" { Kill-Tomcat }
    "logs" { Show-Logs }
    "reset" { Reset-All }
    "install-config" { Install-Config }
    "open" { Open-App }
    "deploy" { Deploy-App }
    default {
        Write-Host "Unknown option: $Action" -ForegroundColor Red
        Write-Host "Use 'help' for available options" -ForegroundColor Yellow
        Show-Help
    }
}

Write-Host ""
