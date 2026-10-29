Add-Type -AssemblyName System.Drawing

$brainDir = "C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3"
$targetDir = "C:\Users\bdour\.gemini\antigravity-ide\scratch\android-tv-clean-launcher\launcher\app\src\main\res\drawable"

function Crop-Circle($srcBitmap, $cx, $cy, $radius, $destSize, $outFile) {
    $cropBmp = New-Object System.Drawing.Bitmap($destSize, $destSize)
    $g = [System.Drawing.Graphics]::FromImage($cropBmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $srcX = [int]($cx - $radius)
    $srcY = [int]($cy - $radius)
    $srcW = [int]($radius * 2)
    $srcH = [int]($radius * 2)

    $destRect = New-Object System.Drawing.Rectangle(0, 0, $destSize, $destSize)
    $srcRect = New-Object System.Drawing.Rectangle($srcX, $srcY, $srcW, $srcH)

    $g.DrawImage($srcBitmap, $destRect, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()

    $cropBmp.Save($outFile, [System.Drawing.Imaging.ImageFormat]::Png)
    $cropBmp.Dispose()
    Write-Host "Saved: $outFile"
}

# --- 1. PHOTOREAL ---
$photoFile = Join-Path $brainDir "avatars_photoreal_1792961423426.jpg"
$photoBmp = [System.Drawing.Bitmap]::FromFile($photoFile)
$photoNamesRow1 = @("ic_avatar_photo_man_asian", "ic_avatar_photo_woman_black", "ic_avatar_photo_man_hispanic", "ic_avatar_photo_woman_blonde", "ic_avatar_photo_man_elder")
$photoNamesRow2 = @("ic_avatar_photo_woman_southasian", "ic_avatar_photo_teen_boy", "ic_avatar_photo_woman_eastasian", "ic_avatar_photo_man_middleeastern", "ic_avatar_photo_woman_elder")

$xs = @(131, 381, 632, 883, 1133)
$r = 104

for ($i = 0; $i -lt 5; $i++) {
    $outPath = Join-Path $targetDir ($photoNamesRow1[$i] + ".png")
    Crop-Circle $photoBmp $xs[$i] 184 $r 320 $outPath
}
for ($i = 0; $i -lt 5; $i++) {
    $outPath = Join-Path $targetDir ($photoNamesRow2[$i] + ".png")
    Crop-Circle $photoBmp $xs[$i] 498 $r 320 $outPath
}
$photoBmp.Dispose()

# --- 2. ANIMALS ---
$animFile = Join-Path $brainDir "avatars_animals_1792961442770.jpg"
$animBmp = [System.Drawing.Bitmap]::FromFile($animFile)
$animNamesRow1 = @("ic_avatar_animal_lion", "ic_avatar_animal_leopard", "ic_avatar_animal_fox", "ic_avatar_animal_wolf", "ic_avatar_animal_tiger")
$animNamesRow2 = @("ic_avatar_animal_owl", "ic_avatar_animal_panda", "ic_avatar_animal_retriever", "ic_avatar_animal_eagle", "ic_avatar_animal_turtle")

for ($i = 0; $i -lt 5; $i++) {
    $outPath = Join-Path $targetDir ($animNamesRow1[$i] + ".png")
    Crop-Circle $animBmp $xs[$i] 184 $r 320 $outPath
}
for ($i = 0; $i -lt 5; $i++) {
    $outPath = Join-Path $targetDir ($animNamesRow2[$i] + ".png")
    Crop-Circle $animBmp $xs[$i] 500 $r 320 $outPath
}
$animBmp.Dispose()

# --- 3. LANDSCAPES ---
$landFile = Join-Path $brainDir "avatars_landscapes_1792961486122.jpg"
$landBmp = [System.Drawing.Bitmap]::FromFile($landFile)
$landNamesRow1 = @("ic_avatar_land_aurora", "ic_avatar_land_mountain", "ic_avatar_land_ocean", "ic_avatar_land_forest", "ic_avatar_land_canyon")
$landNamesRow2 = @("ic_avatar_land_cherry", "ic_avatar_land_desert", "ic_avatar_land_waterfall", "ic_avatar_land_autumn", "ic_avatar_land_galaxy")

for ($i = 0; $i -lt 5; $i++) {
    $outPath = Join-Path $targetDir ($landNamesRow1[$i] + ".png")
    Crop-Circle $landBmp $xs[$i] 204 $r 320 $outPath
}
for ($i = 0; $i -lt 5; $i++) {
    $outPath = Join-Path $targetDir ($landNamesRow2[$i] + ".png")
    Crop-Circle $landBmp $xs[$i] 500 $r 320 $outPath
}
$landBmp.Dispose()

# --- 4. ART & ABSTRACT ---
$artFile = Join-Path $brainDir "avatars_art_1792961506961.jpg"
$artBmp = [System.Drawing.Bitmap]::FromFile($artFile)
$artNamesRow1 = @("ic_avatar_art_kintsugi", "ic_avatar_art_prism", "ic_avatar_art_neonwave", "ic_avatar_art_liquidgold", "ic_avatar_art_watercolor")
$artNamesRow2 = @("ic_avatar_art_bauhaus", "ic_avatar_art_amethyst", "ic_avatar_art_ribbon", "ic_avatar_art_marble", "ic_avatar_art_monogram")

$artXs = @(126, 380, 632, 884, 1138)
$artR = 110

for ($i = 0; $i -lt 5; $i++) {
    $outPath = Join-Path $targetDir ($artNamesRow1[$i] + ".png")
    Crop-Circle $artBmp $artXs[$i] 246 $artR 320 $outPath
}
for ($i = 0; $i -lt 5; $i++) {
    $outPath = Join-Path $targetDir ($artNamesRow2[$i] + ".png")
    Crop-Circle $artBmp $artXs[$i] 696 $artR 320 $outPath
}
$artBmp.Dispose()

Write-Host "Successfully generated 40 categorized 320x320 avatar PNGs!"
