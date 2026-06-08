# Build Management Tools - Ban Hoa Qua Online
# Su dung: powershell -ExecutionPolicy Bypass -File build-tools.ps1 [option]

param(
    [string]$Action = "help"
)
$LogFile = "build_tools.log"

function Log-Message {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [$Level] $Message"
    Add-Content -Path $LogFile -Value $logEntry
    Write-Host $logEntry
}


# ==================== CONFIGURATION LOADING & AUTO-DETECTION ====================

function Get-PropertyFromFile {
    param(
        [string]$filePath,
        [string]$propertyName
    )
    if (Test-Path $filePath) {
        $lines = Get-Content $filePath -ErrorAction SilentlyContinue
        foreach ($line in $lines) {
            $trimmed = $line.Trim()
            if ($trimmed -and -not $trimmed.StartsWith("#")) {
                $parts = $trimmed.Split('=', 2)
                if ($parts.Length -eq 2) {
                    $key = $parts[0].Trim()
                    if ($key -eq $propertyName) {
                        $val = $parts[1].Trim()
                        if (($val.StartsWith('"') -and $val.EndsWith('"')) -or ($val.StartsWith("'") -and $val.EndsWith("'"))) {
                            $val = $val.Substring(1, $val.Length - 2)
                        }
                        return $val.Replace("\\", "/").Replace("\", "/")
                    }
                }
            }
        }
    }
    return $null
}

function Verify-JavaHome {
    param([string]$path)
    if ($path) {
        $cleanPath = $path.Replace("\\", "/").Replace("\", "/").Trim()
        if (Test-Path "$cleanPath\bin\javac.exe") {
            return (Get-Item $cleanPath).FullName
        }
    }
    return $null
}

function Verify-CatalinaHome {
    param([string]$path)
    if ($path) {
        $cleanPath = $path.Replace("\\", "/").Replace("\", "/").Trim()
        if ((Test-Path "$cleanPath\bin\startup.bat") -and (Test-Path "$cleanPath\lib\catalina.jar")) {
            return (Get-Item $cleanPath).FullName
        }
    }
    return $null
}

function Find-JavaHome {
    # 1. Try env variable
    $resolvedHome = Verify-JavaHome $env:JAVA_HOME
    if ($resolvedHome) { return $resolvedHome }

    # 2. Try NetBeans properties
    if (Test-Path "nbproject/private/private.properties") {
        $userProps = Get-PropertyFromFile "nbproject/private/private.properties" "user.properties.file"
        if ($userProps -and (Test-Path $userProps)) {
            $lines = Get-Content $userProps -ErrorAction SilentlyContinue
            foreach ($line in $lines) {
                if ($line -match '^platforms\.[^.]+\.home\s*=\s*(.+)') {
                    $p = $Matches[1].Trim()
                    $resolvedHome = Verify-JavaHome $p
                    if ($resolvedHome) { return $resolvedHome }
                }
            }
        }
    }

    # 3. Check Registry (Windows)
    $regPaths = @(
        "HKLM:\SOFTWARE\JavaSoft\JDK",
        "HKLM:\SOFTWARE\JavaSoft\Java Development Kit",
        "HKLM:\SOFTWARE\JavaSoft\Java Runtime Environment"
    )
    foreach ($regPath in $regPaths) {
        if (Test-Path $regPath) {
            $jh = Get-ItemProperty -Path $regPath -Name "JavaHome" -ErrorAction SilentlyContinue
            if ($jh) {
                $resolvedHome = Verify-JavaHome $jh.JavaHome
                if ($resolvedHome) { return $resolvedHome }
            }
            $subKeys = Get-ChildItem -Path $regPath -ErrorAction SilentlyContinue
            foreach ($sub in $subKeys) {
                $jhSub = Get-ItemProperty -Path $sub.PSPath -Name "JavaHome" -ErrorAction SilentlyContinue
                if ($jhSub) {
                    $resolvedHome = Verify-JavaHome $jhSub.JavaHome
                    if ($resolvedHome) { return $resolvedHome }
                }
            }
        }
    }

    # 4. Check typical locations
    $commonDirs = @(
        "C:\Program Files\Java",
        "C:\Program Files (x86)\Java"
    )
    foreach ($dir in $commonDirs) {
        if (Test-Path $dir) {
            $jdks = Get-ChildItem -Path $dir -Directory -Filter "jdk-*" -ErrorAction SilentlyContinue
            $jdks = $jdks | Sort-Object Name -Descending
            foreach ($jdk in $jdks) {
                $resolvedHome = Verify-JavaHome $jdk.FullName
                if ($resolvedHome) { return $resolvedHome }
            }
        }
    }

    # 5. Check PATH command 'where java'
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $javaExe = $javaCmd.Source
        $parent = Split-Path (Split-Path $javaExe -Parent) -Parent
        $resolvedHome = Verify-JavaHome $parent
        if ($resolvedHome) { return $resolvedHome }
    }

    return $null
}

function Find-CatalinaHome {
    # 1. Try env variable
    $resolvedHome = Verify-CatalinaHome $env:CATALINA_HOME
    if ($resolvedHome) { return $resolvedHome }

    # 2. Try NetBeans configuration
    if (Test-Path "nbproject/private/private.properties") {
        $nbHome = Get-PropertyFromFile "nbproject/private/private.properties" "j2ee.server.home"
        $resolvedHome = Verify-CatalinaHome $nbHome
        if ($resolvedHome) { return $resolvedHome }

        $antProps = Get-PropertyFromFile "nbproject/private/private.properties" "deploy.ant.properties.file"
        if ($antProps -and (Test-Path $antProps)) {
            $tcHome = Get-PropertyFromFile $antProps "tomcat.home"
            $resolvedHome = Verify-CatalinaHome $tcHome
            if ($resolvedHome) { return $resolvedHome }
        }
    }

    # 3. Check typical directories
    $userProfile = $env:USERPROFILE
    $commonDirs = @(
        "C:\Program Files\Apache Software Foundation",
        "C:\apache-tomcat-*",
        "C:\",
        "$userProfile\Downloads",
        "$userProfile"
    )
    foreach ($dir in $commonDirs) {
        if ($dir.Contains("*")) {
            $matches = Get-Item $dir -ErrorAction SilentlyContinue
            foreach ($m in $matches) {
                if ($m.PSIsContainer) {
                    $resolvedHome = Verify-CatalinaHome $m.FullName
                    if ($resolvedHome) { return $resolvedHome }
                }
            }
        } elseif (Test-Path $dir) {
            $tcDirs = Get-ChildItem -Path $dir -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like "*tomcat*" -or $_.Name -like "*apache-tomcat*" }
            $tcDirs = $tcDirs | Sort-Object Name -Descending
            foreach ($tcd in $tcDirs) {
                $resolvedHome = Verify-CatalinaHome $tcd.FullName
                if ($resolvedHome) { return $resolvedHome }
                
                $subDirs = Get-ChildItem -Path $tcd.FullName -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like "*tomcat*" -or $_.Name -like "*apache-tomcat*" }
                foreach ($sub in $subDirs) {
                    $resolvedHome = Verify-CatalinaHome $sub.FullName
                    if ($resolvedHome) { return $resolvedHome }
                }
            }
        }
    }

    return $null
}

function Find-CatalinaBase {
    param([string]$catalinaHome)
    if ($env:CATALINA_BASE -and (Test-Path "$env:CATALINA_BASE\conf")) {
        return (Get-Item $env:CATALINA_BASE).FullName
    }

    $userProfile = $env:USERPROFILE
    $bases = @(
        "$userProfile\.tomcat",
        "$userProfile\AppData\Roaming\NetBeans\*\apache-tomcat-*"
    )
    foreach ($b in $bases) {
        if ($b.Contains("*")) {
            $matches = Get-Item $b -ErrorAction SilentlyContinue
            foreach ($m in $matches) {
                if ($m.PSIsContainer -and (Test-Path "$($m.FullName)\conf")) {
                    return $m.FullName
                }
            }
        } elseif (Test-Path "$b\conf") {
            return (Get-Item $b).FullName
        }
    }

    return $catalinaHome
}

# 1. Initialize empty vars
$JAVA_HOME = $null
$CATALINA_HOME = $null
$CATALINA_BASE = $null

# 2. Check if .env and tomcat_config.ini exist or create from templates
if (-not (Test-Path "tomcat_config.ini") -and (Test-Path "tomcat_config.ini.template")) {
    Copy-Item "tomcat_config.ini.template" "tomcat_config.ini"
    Log-Message "Created tomcat_config.ini from template." "INFO"
}
if (-not (Test-Path ".env") -and (Test-Path ".env.template")) {
    Copy-Item ".env.template" ".env"
    Log-Message "Created .env from template." "INFO"
}

# 3. Load env variables into process
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $parts = $line.Split('=', 2)
            if ($parts.Length -eq 2) {
                $key = $parts[0].Trim()
                $val = $parts[1].Trim()
                if (($val.StartsWith('"') -and $val.EndsWith('"')) -or ($val.StartsWith("'") -and $val.EndsWith("'"))) {
                    $val = $val.Substring(1, $val.Length - 2)
                }
                [System.Environment]::SetEnvironmentVariable($key, $val, "Process")
            }
        }
    }
}

# 4. Try loading from tomcat_config.ini
if (Test-Path "tomcat_config.ini") {
    Get-Content "tomcat_config.ini" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $parts = $line.Split('=', 2)
            if ($parts.Length -eq 2) {
                $key = $parts[0].Trim()
                $val = $parts[1].Trim()
                if ($key -eq "JAVA_HOME") { $JAVA_HOME = Verify-JavaHome $val }
                elseif ($key -eq "CATALINA_HOME") { $CATALINA_HOME = Verify-CatalinaHome $val }
                elseif ($key -eq "CATALINA_BASE" -and $val -and $val -ne "auto-detect") { 
                    if (Test-Path "$val\conf") { $CATALINA_BASE = (Get-Item $val).FullName }
                }
            }
        }
    }
}

# 5. Fallback auto-detection if still null or invalid
if (-not $JAVA_HOME) {
    $JAVA_HOME = Find-JavaHome
}
if (-not $CATALINA_HOME) {
    $CATALINA_HOME = Find-CatalinaHome
}

# 6. Interactive prompt if still not found and save configuration
$saveConfig = $false
if (-not $JAVA_HOME) {
    Write-Host "`n====================================================================" -ForegroundColor Red
    Write-Host " [!] KHONG TIM THAY THU MUC JDK PHU HOP (Can bin\javac.exe de compile)" -ForegroundColor Red
    Write-Host "====================================================================" -ForegroundColor Red
    $javaInput = Read-Host "Nhap duong dan den JDK cua ban (vi du: C:\Program Files\Java\jdk-17)"
    $JAVA_HOME = Verify-JavaHome $javaInput
    while (-not $JAVA_HOME -and $javaInput) {
        Write-Host "[ERROR] Duong dan JDK khong hop le (khong tim thay bin\javac.exe)!" -ForegroundColor Red
        $javaInput = Read-Host "Nhap lai hoac nhan Ctrl+C de thoat"
        $JAVA_HOME = Verify-JavaHome $javaInput
    }
    if ($JAVA_HOME) { $saveConfig = $true }
}

if (-not $CATALINA_HOME) {
    Write-Host "`n====================================================================" -ForegroundColor Red
    Write-Host " [!] KHONG TIM THAY THU MUC TOMCAT HOME PHU HOP" -ForegroundColor Red
    Write-Host "====================================================================" -ForegroundColor Red
    $tomcatInput = Read-Host "Nhap duong dan den Tomcat cua ban (vi du: C:\apache-tomcat-10.1.55)"
    $CATALINA_HOME = Verify-CatalinaHome $tomcatInput
    while (-not $CATALINA_HOME -and $tomcatInput) {
        Write-Host "[ERROR] Duong dan Tomcat khong hop le (khong tim thay bin\startup.bat hoac lib\catalina.jar)!" -ForegroundColor Red
        $tomcatInput = Read-Host "Nhap lai hoac nhan Ctrl+C de thoat"
        $CATALINA_HOME = Verify-CatalinaHome $tomcatInput
    }
    if ($CATALINA_HOME) { $saveConfig = $true }
}

# Resolve base
if (-not $CATALINA_BASE -and $CATALINA_HOME) {
    $CATALINA_BASE = Find-CatalinaBase $CATALINA_HOME
}

# Save configuration back to ini if we prompted or if we detected new working paths
if ($saveConfig -and $JAVA_HOME -and $CATALINA_HOME) {
    $configContent = "# Tomcat Configuration - Generated $(Get-Date)`r`n" +
                     "JAVA_HOME=$JAVA_HOME`r`n" +
                     "CATALINA_HOME=$CATALINA_HOME`r`n" +
                     "CATALINA_BASE=$CATALINA_BASE"
    $configContent | Out-File "tomcat_config.ini" -Encoding utf8
    Log-Message "Saved working configuration to tomcat_config.ini" "INFO"
}

# Set JRE_HOME same as JAVA_HOME
$JRE_HOME = $JAVA_HOME

# Set environment variables so Tomcat startup.bat and commands see them
$env:JAVA_HOME = $JAVA_HOME
$env:JRE_HOME = $JRE_HOME
if ($CATALINA_HOME) { $env:CATALINA_HOME = $CATALINA_HOME }
if ($CATALINA_BASE) { $env:CATALINA_BASE = $CATALINA_BASE }
$env:JAVA_OPTS = "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dfile.client.encoding=UTF-8"
$env:CATALINA_OPTS = "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dfile.client.encoding=UTF-8"
$env:PATH = "$JAVA_HOME\bin;" + $env:PATH



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
  reload         - Recompile nhanh va Hot-Reload Tomcat (giu nguyen Session)
  test           - Chay kiem thu unit tests JUnit cho he thong


Docker options:
  docker-build   - Build Docker image tu Dockerfile
  docker-up      - Khoi chay ung dung bang Docker Compose
  docker-down    - Dung va xoa container Docker
  docker-logs    - Xem log thoi gian thuc cua container Docker
  docker-reset   - Reset sach se Docker (xoa container, volume, va image)

Vi du:
  powershell -ExecutionPolicy Bypass -File build-tools.ps1 status
  powershell -ExecutionPolicy Bypass -File build-tools.ps1 docker-up

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
    Write-Host "Enter Tomcat paths (or press Enter to keep current/detected values):" -ForegroundColor Yellow
    Write-Host ""
    
    $defaultJava = if ($JAVA_HOME) { $JAVA_HOME } else { "C:\Program Files\Java\jdk-17" }
    $defaultCatalina = if ($CATALINA_HOME) { $CATALINA_HOME } else { "C:\apache-tomcat-10.1.55" }
    $defaultBase = if ($CATALINA_BASE) { $CATALINA_BASE } else { "auto-detect" }

    $java = Read-Host "Java Home [$defaultJava]"
    $catalina = Read-Host "Tomcat Home [$defaultCatalina]"
    $base = Read-Host "Tomcat Instance [$defaultBase]"
    
    $finalJava = if ($java) { Verify-JavaHome $java } else { $defaultJava }
    if ($java -and -not $finalJava) {
        Write-Host "[WARN] Duong dan JDK vua nhap khong hop le (khong tim thay bin\javac.exe). Giu nguyen gia tri mac dinh." -ForegroundColor Yellow
        $finalJava = $defaultJava
    }
    
    $finalCatalina = if ($catalina) { Verify-CatalinaHome $catalina } else { $defaultCatalina }
    if ($catalina -and -not $finalCatalina) {
        Write-Host "[WARN] Duong dan Tomcat vua nhap khong hop le (khong tim thay bin\startup.bat hoac lib\catalina.jar). Giu nguyen gia tri mac dinh." -ForegroundColor Yellow
        $finalCatalina = $defaultCatalina
    }
    
    $finalBase = if ($base) { $base } else { $defaultBase }
    
    $config = "# Tomcat Configuration - Generated $(Get-Date)`r`n" +
              "JAVA_HOME=$finalJava`r`n" +
              "CATALINA_HOME=$finalCatalina`r`n" +
              "CATALINA_BASE=$finalBase"
    
    $config | Out-File "tomcat_config.ini" -Encoding utf8
    Write-Host "Configuration saved to tomcat_config.ini" -ForegroundColor Green
    
    # Reload local variables
    $global:JAVA_HOME = $finalJava
    $global:JRE_HOME = $finalJava
    $global:CATALINA_HOME = $finalCatalina
    $global:CATALINA_BASE = if ($finalBase -eq "auto-detect") { Find-CatalinaBase $finalCatalina } else { $finalBase }
    
    $env:JAVA_HOME = $global:JAVA_HOME
    $env:JRE_HOME = $global:JRE_HOME
    $env:CATALINA_HOME = $global:CATALINA_HOME
    $env:CATALINA_BASE = $global:CATALINA_BASE
    $env:JAVA_OPTS = "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dfile.client.encoding=UTF-8"
    $env:CATALINA_OPTS = "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dfile.client.encoding=UTF-8"
    
    Log-Message "Install-config executed, updated configuration saved."
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

function Compile-Java-Atomic {
    param(
        [string]$webBuildDir,
        [string]$tomcatHome,
        [string]$classpath,
        [string]$compileLogFile
    )
    
    $classesDir = "$webBuildDir/WEB-INF/classes"
    $classesTempDir = "$webBuildDir/WEB-INF/classes_temp"
    
    if (Test-Path $classesTempDir) {
        Remove-Item $classesTempDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    New-Item -ItemType Directory -Path $classesTempDir -Force | Out-Null
    
    $javaFiles = Get-ChildItem -Path "src/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
    if (-not $javaFiles) {
        Write-Host "[ERROR] No Java files found in src/java!" -ForegroundColor Red
        return $false
    }
    [System.IO.File]::WriteAllLines("sources.txt", $javaFiles)
    
    $javacCmd = "javac -encoding UTF-8 -g:none -nowarn -target 17 -source 17 -cp `"$classpath`" -d `"$classesTempDir`" @sources.txt > `"$compileLogFile`" 2>&1"
    cmd.exe /c $javacCmd
    
    if ($LASTEXITCODE -eq 0) {
        if (-not (Test-Path $classesDir)) {
            New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
        }
        
        # Mirror compiled class files atomically using robocopy
        Start-Process -FilePath "robocopy" `
            -ArgumentList "`"$classesTempDir`"", "`"$classesDir`"", "/mir", "/w:1", "/r:1", "/ndl", "/nfl" `
            -NoNewWindow -Wait
            
        Remove-Item $classesTempDir -Recurse -Force -ErrorAction SilentlyContinue
        return $true
    } else {
        return $false
    }
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
        Start-Process -FilePath "robocopy" `
            -ArgumentList "`"$webSrcDir`"", "`"$webBuildDir`"", "/s", "/e", "/xd", "WEB-INF\classes", "/w:1", "/r:1", "/ndl", "/nfl" `
            -Wait -NoNewWindow
    }

    $classpath = "web/WEB-INF/lib/*;$tomcatHome/lib/*"
    $compileLogFile = "compile_output.log"
    if (Test-Path $compileLogFile) { Remove-Item $compileLogFile -Force }

    $compiled = Compile-Java-Atomic -webBuildDir $webBuildDir -tomcatHome $tomcatHome -classpath $classpath -compileLogFile $compileLogFile
    if (-not $compiled) {
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
                $compiled = Compile-Java-Atomic -webBuildDir $webBuildDir -tomcatHome $tomcatHome -classpath $classpath -compileLogFile $compileLogFile
                if ($compiled) {
                    Write-Host "[WATCH] Recompile and atomic sync completed successfully!" -ForegroundColor Green
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

function Deploy-Reload {
    Write-Host "`n===== PERFORMING FAST HOT-RELOAD =====" -ForegroundColor Green
    
    $webSrcDir = "web"
    $webBuildDir = "build/web"
    if (Test-Path $webSrcDir) {
        Write-Host "Syncing web files..." -ForegroundColor Yellow
        Start-Process -FilePath "robocopy" `
            -ArgumentList "`"$webSrcDir`"", "`"$webBuildDir`"", "/s", "/e", "/xd", "WEB-INF\classes", "/w:1", "/r:1", "/ndl", "/nfl" `
            -Wait -NoNewWindow
    }
    
    $tomcatHome = ""
    if ($env:CATALINA_HOME) { $tomcatHome = $env:CATALINA_HOME }
    
    $classpath = "web/WEB-INF/lib/*;$tomcatHome/lib/*"
    $compileLogFile = "compile_output.log"
    if (Test-Path $compileLogFile) { Remove-Item $compileLogFile -Force }
    
    $compiled = Compile-Java-Atomic -webBuildDir $webBuildDir -tomcatHome $tomcatHome -classpath $classpath -compileLogFile $compileLogFile
    if ($compiled) {
        Write-Host "[OK] Hot-reload compilation and atomic sync completed successfully!" -ForegroundColor Green
        Write-Host "Tomcat will reload the context in ~1-2 seconds. Active sessions preserved." -ForegroundColor Cyan
    } else {
        $compileErrors = Get-Content $compileLogFile -Raw -ErrorAction SilentlyContinue
        Write-Host "=================== COMPILATION ERRORS ===================" -ForegroundColor Red
        Write-Host $compileErrors -ForegroundColor Red
        Write-Host "=========================================================" -ForegroundColor Red
    }
    if (Test-Path $compileLogFile) { Remove-Item $compileLogFile -Force }
}

# Docker management functions
function Docker-Build {
    Write-Host "`n===== BUILDING DOCKER IMAGE =====" -ForegroundColor Green
    docker compose build
}

function Docker-Up {
    Write-Host "`n===== STARTING DOCKER CONTAINER =====" -ForegroundColor Green
    if (-not (Test-Path ".env")) {
        if (Test-Path ".env.template") {
            Copy-Item ".env.template" ".env"
            Write-Host "Created .env from template." -ForegroundColor Yellow
        }
    }
    docker compose up -d
    Write-Host "[OK] Docker container is starting up!" -ForegroundColor Green
    Write-Host "Go to: http://localhost:8080/Ban_Hoa_Qua_Online/" -ForegroundColor Cyan
}

function Docker-Down {
    Write-Host "`n===== STOPPING DOCKER CONTAINER =====" -ForegroundColor Yellow
    docker compose down
}

function Docker-Logs {
    Write-Host "`n===== SHOWING DOCKER LOGS (Press Ctrl+C to exit logs) =====" -ForegroundColor Cyan
    docker compose logs -f
}

function Docker-Reset {
    Write-Host "`n===== RESETTING DOCKER (Removing Volumes and Images) =====" -ForegroundColor Red
    docker compose down -v --rmi all
}

function Run-Tests {
    Write-Host "`n===== PREPARING AND RUNNING JUNIT TESTS =====" -ForegroundColor Green
    
    $testLibDir = "lib/test"
    if (-not (Test-Path $testLibDir)) {
        New-Item -ItemType Directory -Path $testLibDir -Force | Out-Null
    }
    
    # Download JUnit if not present
    $junitJar = "$testLibDir/junit-4.13.2.jar"
    if (-not (Test-Path $junitJar)) {
        Write-Host "Downloading JUnit 4..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar" -OutFile $junitJar -TimeoutSec 15
    }
    
    # Download Hamcrest if not present
    $hamcrestJar = "$testLibDir/hamcrest-core-1.3.jar"
    if (-not (Test-Path $hamcrestJar)) {
        Write-Host "Downloading Hamcrest Core..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar" -OutFile $hamcrestJar -TimeoutSec 15
    }
    
    # Recompile main classes first
    $webBuildDir = "build/web"
    $tomcatHome = $env:CATALINA_HOME
    
    $classpath = "web/WEB-INF/lib/*;$tomcatHome/lib/*"
    $compileLogFile = "compile_output.log"
    
    Write-Host "Compiling main Java classes..." -ForegroundColor Yellow
    $compiledMain = Compile-Java-Atomic -webBuildDir $webBuildDir -tomcatHome $tomcatHome -classpath $classpath -compileLogFile $compileLogFile
    if (-not $compiledMain) {
        $compileErrors = Get-Content $compileLogFile -Raw -ErrorAction SilentlyContinue
        Write-Host "Main Java compilation failed!" -ForegroundColor Red
        Write-Host $compileErrors -ForegroundColor Red
        if (Test-Path $compileLogFile) { Remove-Item $compileLogFile -Force }
        return
    }
    if (Test-Path $compileLogFile) { Remove-Item $compileLogFile -Force }
    
    # Compile tests
    $testBuildDir = "build/test/classes"
    if (Test-Path $testBuildDir) {
        Remove-Item $testBuildDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    New-Item -ItemType Directory -Path $testBuildDir -Force | Out-Null
    
    $testFiles = Get-ChildItem -Path "test" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
    if (-not $testFiles) {
        Write-Host "[ERROR] No test Java files found in test/!" -ForegroundColor Red
        return
    }
    [System.IO.File]::WriteAllLines("test_sources.txt", $testFiles)
    
    $testClasspath = "build/web/WEB-INF/classes;web/WEB-INF/lib/*;lib/test/*;$tomcatHome/lib/*"
    
    Write-Host "Compiling test Java classes..." -ForegroundColor Yellow
    $javacCmd = "javac -encoding UTF-8 -cp `"$testClasspath`" -d `"$testBuildDir`" @test_sources.txt > test_compile_output.log 2>&1"
    cmd.exe /c $javacCmd
    
    if (Test-Path "test_sources.txt") { Remove-Item "test_sources.txt" -Force }
    
    if ($LASTEXITCODE -ne 0) {
        $compileErrors = Get-Content test_compile_output.log -Raw -ErrorAction SilentlyContinue
        Write-Host "Test compilation failed!" -ForegroundColor Red
        Write-Host $compileErrors -ForegroundColor Red
        if (Test-Path "test_compile_output.log") { Remove-Item "test_compile_output.log" -Force }
        return
    }
    if (Test-Path "test_compile_output.log") { Remove-Item "test_compile_output.log" -Force }
    Write-Host "Test compilation completed successfully!" -ForegroundColor Green
    
    # Run tests using JUnit Core
    Write-Host "`nRunning JUnit Tests..." -ForegroundColor Cyan
    $runClasspath = "build/test/classes;build/web/WEB-INF/classes;web/WEB-INF/lib/*;lib/test/*;$tomcatHome/lib/*"
    
    $testClasses = @(
        "com.fruitmkt.test.ProductApprovalTest",
        "com.fruitmkt.test.CategoryCRUDTest",
        "com.fruitmkt.test.CartOrderFlowTest",
        "com.fruitmkt.test.CheckoutServletPricingRegressionTest",
        "com.fruitmkt.test.UserAuthFlowTest",
        "com.fruitmkt.test.PromotionVoucherTest",
        "com.fruitmkt.test.PromotionServletCrudToggleTest",
        "com.fruitmkt.test.PaymentDashboardSmokeTest",
        "com.fruitmkt.test.ProductBusinessRulesTest",
        "com.fruitmkt.test.SettlementAndReturnRulesTest",
        "com.fruitmkt.test.AiSearchTest",
        "com.fruitmkt.test.ChatNotificationTest"
    )
    
    $cmdRun = "java -cp `"$runClasspath`" org.junit.runner.JUnitCore $($testClasses -join ' ')"
    cmd.exe /c $cmdRun
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
    "reload" { Deploy-Reload }
    "test" { Run-Tests }
    "docker-build" { Docker-Build }
    "docker-up" { Docker-Up }
    "docker-down" { Docker-Down }
    "docker-logs" { Docker-Logs }
    "docker-reset" { Docker-Reset }
    default {
        Write-Host "Unknown option: $Action" -ForegroundColor Red
        Write-Host "Use 'help' for available options" -ForegroundColor Yellow
        Show-Help
    }
}

Write-Host ""
