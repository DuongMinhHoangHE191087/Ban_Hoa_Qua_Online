$adminDir = "d:\DMHoang\Project_GitHub\Ban_Hoa_Qua_Online\web\WEB-INF\jsp\admin"
$files = Get-ChildItem -Path $adminDir -Filter *.jsp

$targetLayout = 'class="admin-layout flex h-screen overflow-hidden"'
$replacementLayout = 'class="admin-layout"'

$targetMain = 'class="admin-main flex-1 overflow-y-auto'
$replacementMain = 'class="admin-main'

$count = 0
foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    $modified = $false
    
    if ($content.Contains($targetLayout)) {
        $content = $content.Replace($targetLayout, $replacementLayout)
        $modified = $true
    }
    
    if ($content.Contains($targetMain)) {
        $content = $content.Replace($targetMain, $replacementMain)
        $modified = $true
    }
    
    if ($modified) {
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
        Write-Host "Unified layout structure in: $($file.Name)"
        $count++
    }
}

Write-Host "Completed! Unified $count admin JSP layout files."
