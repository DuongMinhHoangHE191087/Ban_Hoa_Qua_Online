$jspDir = "d:\DMHoang\Project_GitHub\Ban_Hoa_Qua_Online\web\WEB-INF\jsp"
$files = Get-ChildItem -Path $jspDir -Filter *.jsp -Recurse

$replacements = @(
    @{ Target = "tailwind.js?plugins=forms,container-queries"; Replacement = "tailwind.js" },
    @{ Target = "tailwind.js?plugins=forms"; Replacement = "tailwind.js" },
    @{ Target = "tailwind.js?plugins=container-queries"; Replacement = "tailwind.js" }
)

$count = 0
foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    $modified = $false
    
    foreach ($r in $replacements) {
        if ($content.Contains($r.Target)) {
            $content = $content.Replace($r.Target, $r.Replacement)
            $modified = $true
        }
    }
    
    if ($modified) {
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
        Write-Host "Cleaned tailwind plugins in: $($file.Name)"
        $count++
    }
}

Write-Host "Completed! Cleaned $count files."
