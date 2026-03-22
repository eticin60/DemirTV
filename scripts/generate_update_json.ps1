param(
  [Parameter(Mandatory=$true)][int]$VersionCode,
  [Parameter(Mandatory=$true)][string]$VersionName,
  [Parameter(Mandatory=$true)][string]$DownloadUrl,
  [bool]$ForceUpdate = $false,
  [string]$OutFile = "update.json"
)

$payload = [ordered]@{
  versionCode = $VersionCode
  versionName = $VersionName
  downloadUrl = $DownloadUrl
  forceUpdate = $ForceUpdate
}

$payload | ConvertTo-Json -Depth 3 | Set-Content -Encoding UTF8 $OutFile
Write-Host "Wrote $OutFile"
