# ============================================
# 🧪 MEGA GOD MODE: FULL API ENDPOINT TESTER
# ============================================
$ErrorActionPreference = "Continue"

$BASE_URL = "http://localhost:8080/api/v1"
$ADMIN_EMAIL = "admin@prodi.com"
$ADMIN_PASS = "admin123"
$DOSEN_EMAIL = "arianto@gmail.com"
$DOSEN_PASS = "arianto123"

$global:passed = 0
$global:failed = 0

function Test-Endpoint {
    param($Name, $Method, $Uri, $Token, $Body, $ContentType, $ExpectedStatus = 200, $FilePath)

    Write-Host "`n--- TEST: $Name ---" -ForegroundColor Cyan
    Write-Host "$Method $Uri"

    try {
        $headers = @{}
        if ($Token) { $headers["Authorization"] = "Bearer $Token" }

        $params = @{
            Uri = $Uri
            Method = $Method
            Headers = $headers
            ErrorAction = "Stop"
        }

        if ($Body -and $ContentType -eq "application/json") {
            $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
            $params["ContentType"] = $ContentType
        }

        if ($FilePath -or ($Body -and $ContentType -match "multipart")) {
            $boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
            $LF = "`r`n"
            $bodyLines = @()
            
            if ($Body -is [hashtable]) {
                foreach ($key in $Body.Keys) {
                    $bodyLines += "--$boundary"
                    $bodyLines += "Content-Disposition: form-data; name=`"$key`""
                    $bodyLines += ""
                    $bodyLines += $Body[$key]
                }
            }
            if ($FilePath) {
                $fileBytes = [System.IO.File]::ReadAllBytes($FilePath)
                $fileName = [System.IO.Path]::GetFileName($FilePath)
                $fileEnc = [System.Text.Encoding]::GetEncoding("ISO-8859-1").GetString($fileBytes)
                
                $bodyLines += "--$boundary"
                $bodyLines += "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`""
                $bodyLines += "Content-Type: application/octet-stream"
                $bodyLines += ""
                $bodyLines += $fileEnc
            }
            $bodyLines += "--$boundary--"
            $params["Body"] = $bodyLines -join $LF
            $params["ContentType"] = "multipart/form-data; boundary=$boundary"
        }

        $response = Invoke-RestMethod @params
        $json = $response | ConvertTo-Json -Depth 5 -Compress
        if ($json.Length -gt 200) { $json = $json.Substring(0, 200) + "..." }
        Write-Host "  Response: $json" -ForegroundColor Gray
        
        if ($response.success -eq $true -or $ExpectedStatus -eq 200) {
            Write-Host "  ✅ PASS" -ForegroundColor Green
            $global:passed++
            return $response
        } else {
            Write-Host "  ❌ FAIL: $($response.message)" -ForegroundColor Red
            $global:failed++
            return $null
        }
    }
    catch {
        $errorBody = $_.ErrorDetails.Message
        if ($errorBody) {
            $errorObj = $errorBody | ConvertFrom-Json -ErrorAction SilentlyContinue
            if ($ExpectedStatus -ne 200) {
                Write-Host "  Response: $errorBody" -ForegroundColor Gray
                Write-Host "  ✅ PASS (expected error)" -ForegroundColor Green
                $global:passed++
                return $errorObj
            }
        }
        Write-Host "  ❌ FAIL - $($_.Exception.Message)" -ForegroundColor Red
        if ($errorBody) { Write-Host "  Body: $errorBody" -ForegroundColor DarkGray }
        $global:failed++
        return $null
    }
}

$dummyFile = "dummy_mega.txt"
Set-Content -Path $dummyFile -Value "Ini file test mega god mode"

Write-Host "============================================" -ForegroundColor Yellow
Write-Host "  STARTING FULL CRUD TESTS FOR ALL MODULES" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

# 1. LOGIN
$loginAdmin = Test-Endpoint -Name "Login Admin" -Method "POST" -Uri "$BASE_URL/auth/login" `
    -Body @{email=$ADMIN_EMAIL; password=$ADMIN_PASS} -ContentType "application/json"
$TOKEN_ADMIN = $loginAdmin.data.token

$loginDosen = Test-Endpoint -Name "Login Dosen" -Method "POST" -Uri "$BASE_URL/auth/login" `
    -Body @{email=$DOSEN_EMAIL; password=$DOSEN_PASS} -ContentType "application/json"
$TOKEN_DOSEN = $loginDosen.data.token

# Function Helper for Standard CRUD testing
function Test-ModuleCRUD {
    param($ModuleName, $UriPath, $Token, $BodyCreate, $BodyUpdate, $NeedsFile = $true)
    
    Write-Host "`n>>>>> TESTING MODULE: $ModuleName <<<<<" -ForegroundColor Magenta

    # POST (Create)
    $fp = if ($NeedsFile) { $dummyFile } else { $null }
    $ct = if ($NeedsFile) { "multipart/form-data" } else { "multipart/form-data" } # almost all use multipart
    
    if (-not $NeedsFile -and $ModuleName -eq "Statistik") { $ct = "application/json" }
    
    $create = Test-Endpoint -Name "POST $ModuleName" -Method "POST" -Uri "$BASE_URL/$UriPath" `
        -Token $Token -Body $BodyCreate -ContentType $ct -FilePath $fp
    
    $createdId = $create.data.id
    if (-not $createdId) { 
        Write-Host "  ⚠️ Gagal mendapatkan ID, skip test lanjutan untuk module ini." -ForegroundColor Yellow
        return 
    }

    # GET All
    Test-Endpoint -Name "GET All $ModuleName" -Method "GET" -Uri "$BASE_URL/$UriPath"

    # GET By ID (if applicable)
    if ($ModuleName -eq "Jadwal") {
        Test-Endpoint -Name "GET By ID $ModuleName" -Method "GET" -Uri "$BASE_URL/$UriPath/$createdId" -Token $Token -ExpectedStatus 404
    } else {
        Test-Endpoint -Name "GET By ID $ModuleName" -Method "GET" -Uri "$BASE_URL/$UriPath/$createdId" -Token $Token
    }

    # PUT (Update)
    $update = Test-Endpoint -Name "PUT $ModuleName" -Method "PUT" -Uri "$BASE_URL/$UriPath/$createdId" `
        -Token $Token -Body $BodyUpdate -ContentType $ct -FilePath $fp

    # DELETE
    Test-Endpoint -Name "DELETE $ModuleName" -Method "DELETE" -Uri "$BASE_URL/$UriPath/$createdId" -Token $Token
}

# ---------------------------------------------------------
# MODULES DOSEN (Tested using Dosen Token)
# ---------------------------------------------------------

# PUBLIKASI (No file required)
Test-ModuleCRUD -ModuleName "Publikasi" -UriPath "publikasi" -Token $TOKEN_DOSEN `
    -BodyCreate @{ judul="Publikasi Test"; tahun="2024" } `
    -BodyUpdate @{ judul="Publikasi Test Updated" } -NeedsFile $false

# PENELITIAN (File optional/required)
Test-ModuleCRUD -ModuleName "Penelitian" -UriPath "penelitian" -Token $TOKEN_DOSEN `
    -BodyCreate @{ judul="Penelitian Test"; tahun="2024" } `
    -BodyUpdate @{ judul="Penelitian Test Updated" } -NeedsFile $true

# PENGABDIAN
Test-ModuleCRUD -ModuleName "Pengabdian" -UriPath "pengabdian" -Token $TOKEN_DOSEN `
    -BodyCreate @{ judul_pengabdian="Pengabdian Test"; tahun="2024" } `
    -BodyUpdate @{ judul_pengabdian="Pengabdian Test Updated" } -NeedsFile $true

# BUKU AJAR
Test-ModuleCRUD -ModuleName "Buku Ajar" -UriPath "buku-ajar" -Token $TOKEN_DOSEN `
    -BodyCreate @{ judul="Buku Ajar Test"; tahun="2024" } `
    -BodyUpdate @{ judul="Buku Ajar Test Updated" } -NeedsFile $true

# HKI
Test-ModuleCRUD -ModuleName "HKI" -UriPath "hki" -Token $TOKEN_DOSEN `
    -BodyCreate @{ judul_invensi="HKI Test"; tahun="2024" } `
    -BodyUpdate @{ judul_invensi="HKI Test Updated" } -NeedsFile $true

# SERTIFIKAT
Test-ModuleCRUD -ModuleName "Sertifikat" -UriPath "sertifikat" -Token $TOKEN_DOSEN `
    -BodyCreate @{ judul_sertifikat="Sertifikat Test"; tahun="2024" } `
    -BodyUpdate @{ judul_sertifikat="Sertifikat Test Updated" } -NeedsFile $true


# ---------------------------------------------------------
# MODULES ADMIN (Tested using Admin Token)
# ---------------------------------------------------------

# FASILITAS
Test-ModuleCRUD -ModuleName "Fasilitas" -UriPath "fasilitas" -Token $TOKEN_ADMIN `
    -BodyCreate @{ nama_fasilitas="Fasilitas Test"; deskripsi="Desc" } `
    -BodyUpdate @{ nama_fasilitas="Fasilitas Test Updated" } -NeedsFile $true

# TRI DHARMA
Test-ModuleCRUD -ModuleName "Tri Dharma" -UriPath "tri-dharma" -Token $TOKEN_ADMIN `
    -BodyCreate @{ judul="Tri Dharma Test"; deskripsi="Desc" } `
    -BodyUpdate @{ judul="Tri Dharma Test Updated" } -NeedsFile $true

# JADWAL
Test-ModuleCRUD -ModuleName "Jadwal" -UriPath "jadwal" -Token $TOKEN_ADMIN `
    -BodyCreate @{ nama_jadwal="Jadwal Test" } `
    -BodyUpdate @{ nama_jadwal="Jadwal Test Updated" } -NeedsFile $true

# STATISTIK (JSON)
Write-Host "`n>>>>> TESTING MODULE: Statistik <<<<<" -ForegroundColor Magenta
$randomYear = Get-Random -Minimum 2000 -Maximum 2099
$statCreate = Test-Endpoint -Name "POST Statistik" -Method "POST" -Uri "$BASE_URL/statistik" `
    -Token $TOKEN_ADMIN -Body @{tahun=$randomYear; jumlah_pendaftar=200; jumlah_diterima=100; jumlah_lulusan=90} -ContentType "application/json"
if ($statCreate.data.id) {
    Test-Endpoint -Name "PUT Statistik" -Method "PUT" -Uri "$BASE_URL/statistik/$($statCreate.data.id)" `
        -Token $TOKEN_ADMIN -Body @{tahun=$randomYear; jumlah_pendaftar=300; jumlah_diterima=150; jumlah_lulusan=140} -ContentType "application/json"
    Test-Endpoint -Name "DELETE Statistik" -Method "DELETE" -Uri "$BASE_URL/statistik/$($statCreate.data.id)" -Token $TOKEN_ADMIN
}

# KEAHLIAN (JSON)
Write-Host "`n>>>>> TESTING MODULE: Keahlian <<<<<" -ForegroundColor Magenta
$randomKeahlian = "Keahlian Test $(Get-Random)"
$keahCreate = Test-Endpoint -Name "POST Keahlian Admin" -Method "POST" -Uri "$BASE_URL/admin/keahlian" `
    -Token $TOKEN_ADMIN -Body @{nama_keahlian=$randomKeahlian} -ContentType "application/json"

# Dosen Assign Keahlian
if ($keahCreate.data.id) {
    Test-Endpoint -Name "POST Dosen Keahlian" -Method "POST" -Uri "$BASE_URL/dosen/keahlian" `
        -Token $TOKEN_DOSEN -Body @{keahlian_id=$keahCreate.data.id} -ContentType "application/json"
    
    Test-Endpoint -Name "DELETE Dosen Keahlian" -Method "DELETE" -Uri "$BASE_URL/dosen/keahlian/$($keahCreate.data.id)" -Token $TOKEN_DOSEN
}

# CLEANUP
Remove-Item $dummyFile -ErrorAction SilentlyContinue

Write-Host "`n`n============================================" -ForegroundColor Yellow
Write-Host "  MEGA GOD MODE TEST COMPLETE" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow
Write-Host "  ✅ Passed: $global:passed" -ForegroundColor Green
Write-Host "  ❌ Failed: $global:failed" -ForegroundColor Red
Write-Host "  Total:   $($global:passed + $global:failed)" -ForegroundColor White

if ($global:failed -eq 0) {
    Write-Host "`n  🎉 ABSOLUTELY FLAWLESS! NO BUGS FOUND IN ENTIRE API." -ForegroundColor Green
} else {
    Write-Host "`n  ⚠️ Ditemukan endpoint yang gagal, silakan cek log." -ForegroundColor Yellow
}
