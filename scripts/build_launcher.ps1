$ErrorActionPreference = "Stop"

$scriptDir = $PSScriptRoot
if (-not $scriptDir) { $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path }
$repoRoot = Split-Path -Parent $scriptDir

$toolsDir = "C:\Users\bdour\.gemini\antigravity-ide\scratch\android-tv-clean-launcher\tools"
$jdk = "$toolsDir\jdk-17.0.12+7"
$sdk = "$toolsDir\sdk"
$buildTools = "$sdk\build-tools\30.0.3"
$androidJar = "$sdk\platforms\android-30\android.jar"

$env:JAVA_HOME = $jdk
$env:PATH = "$jdk\bin;$env:PATH"

$appDir = "$repoRoot\launcher\app"
$srcDir = "$appDir\src\main"
$resDir = "$srcDir\res"
$manifest = "$srcDir\AndroidManifest.xml"
$assetsDir = "$srcDir\assets"

$outDir = "$repoRoot\launcher\build"
$compiledZip = "$outDir\compiled_res.zip"
$genDir = "$outDir\gen"
$classesDir = "$outDir\classes"

Write-Output "1. Creating clean build directories..."
Remove-Item -Path $genDir, $classesDir, $compiledZip -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $outDir, $genDir, $classesDir -Force | Out-Null

Write-Output "2. Compiling resources with aapt2..."
& "$buildTools\aapt2.exe" compile --dir "$resDir" -o "$compiledZip"
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed with exit code $LASTEXITCODE" }

Write-Output "3. Linking resources with aapt2..."
& "$buildTools\aapt2.exe" link -I "$androidJar" `
    --manifest "$manifest" `
    --java "$genDir" `
    -A "$assetsDir" `
    -o "$outDir\app-resources.apk" `
    --auto-add-overlay `
    "$compiledZip"
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed with exit code $LASTEXITCODE" }

Write-Output "4. Compiling Java sources with javac..."
$javaFiles = Get-ChildItem -Path "$srcDir\java", "$genDir" -Filter *.java -Recurse | ForEach-Object { $_.FullName }
& "$jdk\bin\javac.exe" -encoding UTF-8 `
    -cp "$androidJar" `
    -d "$classesDir" `
    -source 1.8 -target 1.8 `
    $javaFiles
if ($LASTEXITCODE -ne 0) { throw "javac failed with exit code $LASTEXITCODE" }

Write-Output "5. Converting to DEX with D8..."
$classFiles = Get-ChildItem -Path "$classesDir" -Filter *.class -Recurse | ForEach-Object { $_.FullName }
& "$jdk\bin\java.exe" -cp "$buildTools\lib\d8.jar" com.android.tools.r8.D8 --lib "$androidJar" --output "$outDir" $classFiles
if ($LASTEXITCODE -ne 0) { throw "D8 failed with exit code $LASTEXITCODE" }

Write-Output "6. Packaging DEX into APK with jar tool..."
Copy-Item "$outDir\app-resources.apk" "$outDir\app-unaligned.apk" -Force
Push-Location "$outDir"
& "$jdk\bin\jar.exe" -uf0 "app-unaligned.apk" "classes.dex"
$jarExit = $LASTEXITCODE
Pop-Location
if ($jarExit -ne 0) { throw "jar uf0 failed with exit code $jarExit" }

Write-Output "6b. Normalizing APK zip entry separators to POSIX forward slashes..."
python -c "
import zipfile, os, shutil
src = r'$outDir\app-unaligned.apk'
tmp = r'$outDir\app-unaligned-fixed.apk'
with zipfile.ZipFile(src, 'r') as zin, zipfile.ZipFile(tmp, 'w') as zout:
    for item in zin.infolist():
        data = zin.read(item.filename)
        item.filename = item.filename.replace('\\\\', '/')
        if hasattr(item, 'orig_filename'):
            item.orig_filename = item.orig_filename.replace('\\\\', '/')
        zout.writestr(item, data, compress_type=item.compress_type)
shutil.move(tmp, src)
"

Write-Output "7. Aligning APK with zipalign..."
$alignedApk = "$outDir\clean-launcher-aligned.apk"
Remove-Item $alignedApk -ErrorAction SilentlyContinue
& "$buildTools\zipalign.exe" -v -p 4 "$outDir\app-unaligned.apk" "$alignedApk"
if ($LASTEXITCODE -ne 0) { throw "zipalign failed with exit code $LASTEXITCODE" }

Write-Output "8. Resolving master debug keystore..."
$masterKey = "$repoRoot\launcher\debug.keystore"
$scratchKey = "C:\Users\bdour\.gemini\antigravity-ide\scratch\android-tv-clean-launcher\launcher\build\debug.keystore"
$debugKey = "$outDir\debug.keystore"
if (Test-Path $masterKey) {
    Copy-Item $masterKey $debugKey -Force
} elseif (Test-Path $scratchKey) {
    Copy-Item $scratchKey $debugKey -Force
}
if (-not (Test-Path $debugKey)) {
    & "$jdk\bin\keytool.exe" -genkey -v -keystore "$debugKey" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
    if ($LASTEXITCODE -ne 0) { throw "keytool failed with exit code $LASTEXITCODE" }
    Copy-Item $debugKey $masterKey -Force
}

Write-Output "9. Signing APK with apksigner..."
$finalApk = "$outDir\clean-launcher-release.apk"
Remove-Item $finalApk -ErrorAction SilentlyContinue
Copy-Item "$alignedApk" "$finalApk"
& "$jdk\bin\java.exe" -jar "$buildTools\lib\apksigner.jar" sign --ks "$debugKey" --ks-pass pass:android --key-pass pass:android --ks-key-alias androiddebugkey "$finalApk"
if ($LASTEXITCODE -ne 0) { throw "apksigner failed with exit code $LASTEXITCODE" }

Write-Output "BUILD SUCCESSFUL! Final APK: $finalApk ($( (Get-Item $finalApk).Length ) bytes)"
Copy-Item $finalApk "C:\Users\bdour\.gemini\antigravity-ide\scratch\android-tv-clean-launcher\launcher\build\clean-launcher-release.apk" -Force -ErrorAction SilentlyContinue
