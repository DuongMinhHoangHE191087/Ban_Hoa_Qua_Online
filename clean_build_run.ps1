$ErrorActionPreference = "Stop"

# Keep the original environment paths
$JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
$JRE_HOME = "C:\Program Files\Java\jdk-26.0.1"
$CATALINA_HOME = "C:\Users\Admin\Downloads\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55"
$CATALINA_BASE = "C:\Users\Admin\AppData\Local\JetBrains\IntelliJIdea2026.1\tomcat\5cd621e3-d3e3-40e7-9cbf-f8558242b317"

# Set environment variables for Tomcat
$env:JAVA_HOME = $JAVA_HOME
$env:JRE_HOME = $JRE_HOME
$env:CATALINA_HOME = $CATALINA_HOME
$env:CATALINA_BASE = $CATALINA_BASE
$env:PATH = "$JAVA_HOME\bin;" + $env:PATH

try {
    Write-Host "===================================================" -ForegroundColor Green
    Write-Host "[1/5] Kiem tra va dung may chu Tomcat..." -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Green

    # Try shutting down Tomcat gracefully first
    try {
        Start-Process -FilePath "$CATALINA_HOME\bin\shutdown.bat" -NoNewWindow -Wait -ErrorAction SilentlyContinue
    } catch {}

    # Force kill any process holding port 8080 (standard HTTP port) and 8005 (shutdown port)
    $ports = @(8080, 8005)
    foreach ($port in $ports) {
        $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($connections) {
            Write-Host "Phat hien tien trinh dang chiem dung cong $port. Dang cuong che dong..." -ForegroundColor Yellow
            foreach ($conn in $connections) {
                Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            }
            Start-Sleep -Seconds 1
        }
        Write-Host "Cong $port da san sang!" -ForegroundColor Cyan
    }

    Write-Host "===================================================" -ForegroundColor Green
    Write-Host "[2/5] Dong bo thu muc web va don cache (Robocopy)..." -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Green

    $webSrcDir = "web"
    $webBuildDir = "build/web"

    # Make sure build/web exists
    if (!(Test-Path $webBuildDir)) {
        New-Item -ItemType Directory -Path $webBuildDir -Force | Out-Null
    }

    # Sync web/ to build/web/ using Robocopy (extremely robust)
    if (Test-Path $webSrcDir) {
        Write-Host "Dang dong bo thu muc web/ sang build/web/..." -ForegroundColor Yellow
        # Robocopy parameters:
        # /S: copy subdirectories
        # /E: copy empty subdirectories
        # /XD WEB-INF\classes: exclude classes directory (handled by compiler)
        # /R:1: 1 retry on error
        # /W:1: 1 second wait between retries
        # /NDL: No directory logging
        # /NFL: No file logging
        $robocopyProcess = Start-Process -FilePath "robocopy" -ArgumentList "`"$webSrcDir`"", "`"$webBuildDir`"", "/s", "/e", "/xd", "WEB-INF\classes", "/w:1", "/r:1", "/ndl", "/nfl" -PassThru -Wait -NoNewWindow
        
        # Robocopy exit code >= 8 indicates fatal errors
        if ($robocopyProcess.ExitCode -ge 8) {
            throw "Robocopy dong bo file that bai voi ma loi $($robocopyProcess.ExitCode)"
        }
    }

    # Clean target classes
    $classesDir = "$webBuildDir/WEB-INF/classes"
    if (Test-Path $classesDir) {
        Remove-Item -Path $classesDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    New-Item -ItemType Directory -Path $classesDir -Force | Out-Null

    # Clean Tomcat work/temp cache
    $workDir = "$CATALINA_BASE/work"
    $tempDir = "$CATALINA_BASE/temp"
    if (Test-Path $workDir) { Remove-Item -Path $workDir -Recurse -Force -ErrorAction SilentlyContinue }
    if (Test-Path $tempDir) { Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue }
    New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

    Write-Host "Da dong bo file va don dep sach se cache!" -ForegroundColor Cyan

    Write-Host "===================================================" -ForegroundColor Green
    Write-Host "[3/5] Quet va bien dich ma nguon Java..." -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Green

    # Get all Java files
    $javaFiles = Get-ChildItem -Path "src/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
    if (-not $javaFiles) {
        throw "Khong tim thay bat ky ma nguon Java nao trong src/java!"
    }

    # Write sources.txt without UTF-8 BOM
    $sourcesPath = "sources.txt"
    [System.IO.File]::WriteAllLines($sourcesPath, $javaFiles)

    # Compile
    $classpath = "web/WEB-INF/lib/*;$CATALINA_HOME/lib/*"
    $javacArgs = @("-encoding", "UTF-8", "-cp", $classpath, "-d", $classesDir, "@$sourcesPath")
    Write-Host "Dang chay javac de bien dich..." -ForegroundColor Yellow
    $processInfo = Start-Process -FilePath "javac" -ArgumentList $javacArgs -NoNewWindow -Wait -PassThru
    if ($processInfo.ExitCode -ne 0) {
        throw "Bien dich Java that bai! Vui long kiem tra lai ma nguon."
    }
    Write-Host "Bien dich thanh cong!" -ForegroundColor Cyan

    Write-Host "===================================================" -ForegroundColor Green
    Write-Host "[4/5] Khoi dong lai Tomcat..." -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Green

    # Ensure Context XML exists in Tomcat conf so it serves Ban_Hoa_Qua_Online
    $contextConfDir = "$CATALINA_BASE/conf/Catalina/localhost"
    if (!(Test-Path $contextConfDir)) {
        New-Item -ItemType Directory -Path $contextConfDir -Force | Out-Null
    }
    $contextXmlPath = "$contextConfDir/Ban_Hoa_Qua_Online.xml"
    $docBaseAbsolute = (Get-Item "build/web").FullName
    $contextContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<Context docBase="$($docBaseAbsolute.Replace('\', '/'))" path="/Ban_Hoa_Qua_Online" reloadable="true"/>
"@
    [System.IO.File]::WriteAllText($contextXmlPath, $contextContent)
    Write-Host "Da tao Context XML tai: $contextXmlPath" -ForegroundColor Cyan

    Start-Process -FilePath "$CATALINA_HOME\bin\startup.bat" -NoNewWindow
    Start-Sleep -Seconds 2

    Write-Host "===================================================" -ForegroundColor Green
    Write-Host "[5/5] Da khoi chay thanh cong!" -ForegroundColor Green
    Write-Host "Duong dan ung dung: http://localhost:8080/Ban_Hoa_Qua_Online/" -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Green

    # Khoi dong File System Watcher de hot reload code
    Write-Host "===================================================" -ForegroundColor Green
    Write-Host "KHOI DONG TRANG THAI THEO DOI THAY DOI FILE (WATCH MODE)..." -ForegroundColor Green
    Write-Host "Theo doi src/java va web/ de tu dong compile va sync..." -ForegroundColor Green
    Write-Host "Nhan Ctrl+C de dung..." -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Green

    $javaWatcher = New-Object System.IO.FileSystemWatcher
    $javaWatcher.Path = (Get-Item "src/java").FullName
    $javaWatcher.Filter = "*.java"
    $javaWatcher.IncludeSubdirectories = $true
    $javaWatcher.EnableRaisingEvents = $true

    $webWatcher = New-Object System.IO.FileSystemWatcher
    $webWatcher.Path = (Get-Item "web").FullName
    $webWatcher.IncludeSubdirectories = $true
    $webWatcher.EnableRaisingEvents = $true

    $classesDir = "$webBuildDir/WEB-INF/classes"

    $onChange = {
        param($sender, $eventArgs)
        $path = $eventArgs.FullPath
        $changeType = $eventArgs.ChangeType
        Write-Host "[Watcher] File thay doi: $path ($changeType) luc $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Yellow

        if ($path -like "*.java") {
            Write-Host "Phat hien file Java thay doi. Dang bien dich lai..." -ForegroundColor Cyan
            try {
                $javaFiles = Get-ChildItem -Path "src/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
                [System.IO.File]::WriteAllLines("sources.txt", $javaFiles)
                # Resolve paths absolutely to avoid context reference issues
                $libPath = (Get-Item "web/WEB-INF/lib/*").FullName -join ";"
                $classpath = "web/WEB-INF/lib/*;C:\Users\Admin\Downloads\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55/lib/*"
                $javacArgs = @("-encoding", "UTF-8", "-cp", $classpath, "-d", "build/web/WEB-INF/classes", "@sources.txt")
                $processInfo = Start-Process -FilePath "javac" -ArgumentList $javacArgs -NoNewWindow -Wait -PassThru
                if ($processInfo.ExitCode -eq 0) {
                    Write-Host "Bien dich lai thanh cong! Tomcat se tu dong reload classes." -ForegroundColor Green
                } else {
                    Write-Host "Bien dich loi! Vui long kiem tra lai ma nguon." -ForegroundColor Red
                }
            } catch {
                Write-Host "Loi compile: $_" -ForegroundColor Red
            }
        } else {
            if ($path -notlike "*WEB-INF\classes*") {
                Write-Host "Phat hien thay doi trong web. Dang dong bo files..." -ForegroundColor Cyan
                try {
                    $robocopyProcess = Start-Process -FilePath "robocopy" -ArgumentList "`"web`"", "`"build/web`"", "/s", "/e", "/xd", "WEB-INF/classes", "/w:1", "/r:1", "/ndl", "/nfl" -PassThru -Wait -NoNewWindow
                    Write-Host "Dong bo hoan tat!" -ForegroundColor Green
                } catch {
                    Write-Host "Loi dong bo: $_" -ForegroundColor Red
                }
            }
        }
    }

    $javaCreated = Register-ObjectEvent $javaWatcher "Created" -Action $onChange
    $javaChanged = Register-ObjectEvent $javaWatcher "Changed" -Action $onChange
    $javaDeleted = Register-ObjectEvent $javaWatcher "Deleted" -Action $onChange

    $webCreated = Register-ObjectEvent $webWatcher "Created" -Action $onChange
    $webChanged = Register-ObjectEvent $webWatcher "Changed" -Action $onChange
    $webDeleted = Register-ObjectEvent $webWatcher "Deleted" -Action $onChange

    try {
        while ($true) {
            Start-Sleep -Seconds 1
        }
    } finally {
        Unregister-Event -SourceIdentifier $javaCreated.Name -ErrorAction SilentlyContinue
        Unregister-Event -SourceIdentifier $javaChanged.Name -ErrorAction SilentlyContinue
        Unregister-Event -SourceIdentifier $javaDeleted.Name -ErrorAction SilentlyContinue
        Unregister-Event -SourceIdentifier $webCreated.Name -ErrorAction SilentlyContinue
        Unregister-Event -SourceIdentifier $webChanged.Name -ErrorAction SilentlyContinue
        Unregister-Event -SourceIdentifier $webDeleted.Name -ErrorAction SilentlyContinue
        $javaWatcher.Dispose()
        $webWatcher.Dispose()
    }

} catch {
    Write-Host "`n===================================================" -ForegroundColor Red
    Write-Host "LUU Y: DA CO LOI XAY RA TRONG QUA TRINH THUC THI!" -ForegroundColor Red
    Write-Host "Chi tiet loi: $_" -ForegroundColor Red
    Write-Host "===================================================" -ForegroundColor Red
} finally {
    Write-Host ""
    Write-Host "Dang dung may chu Tomcat..." -ForegroundColor Yellow
    try {
        Start-Process -FilePath "C:\Users\Admin\Downloads\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55\bin\shutdown.bat" -NoNewWindow -Wait -ErrorAction SilentlyContinue
    } catch {}

    # Force kill any process holding port 8080 or 8005 to ensure everything is fully stopped
    $ports = @(8080, 8005)
    foreach ($port in $ports) {
        $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($connections) {
            Write-Host "Dang cuong che dung cac tien trinh con lai tren cong $port..." -ForegroundColor Yellow
            foreach ($conn in $connections) {
                Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            }
        }
    }
    Write-Host "Da dung Tomcat thanh cong!" -ForegroundColor Cyan
}
