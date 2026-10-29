Add-Type -AssemblyName System.Drawing

$brainDir = "C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3"
$targetDir = "C:\Users\bdour\.gemini\antigravity-ide\scratch\android-tv-clean-launcher\launcher\app\src\main\res\drawable"

$avatars = @{
    "avatar_superhero_1792958590428.jpg" = "ic_char_superhero.png"
    "avatar_astronaut_1792958616357.jpg" = "ic_char_astronaut.png"
    "avatar_cyberpunk_1792958758850.jpg" = "ic_char_cyberpunk.png"
    "avatar_wizard_1792958773717.jpg"    = "ic_char_wizard.png"
    "avatar_gamer_1792958788338.jpg"     = "ic_char_gamer.png"
    "avatar_explorer_1792958803513.jpg"  = "ic_char_explorer.png"
    "avatar_fox_1792958818219.jpg"       = "ic_char_fox.png"
    "avatar_ninja_1792958832286.jpg"     = "ic_char_ninja.png"
}

$destSize = 320

foreach ($srcFile in $avatars.Keys) {
    $srcPath = Join-Path $brainDir $srcFile
    $destPath = Join-Path $targetDir $avatars[$srcFile]
    
    if (-not (Test-Path $srcPath)) {
        Write-Error "Source file not found: $srcPath"
        continue
    }

    $srcBmp = [System.Drawing.Bitmap]::FromFile($srcPath)
    $destBmp = [System.Drawing.Bitmap]::new($destSize, $destSize)
    $g = [System.Drawing.Graphics]::FromImage($destBmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.DrawImage($srcBmp, 0, 0, $destSize, $destSize)
    $g.Dispose()
    $srcBmp.Dispose()

    $destBmp.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $destBmp.Dispose()
    Write-Host "Processed $($avatars[$srcFile]) ($destSize x $destSize)"
}
