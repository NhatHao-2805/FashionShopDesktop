# Starts the Fashion Shop server and JavaFX clients on Windows.
[CmdletBinding()]
param(
    [ValidateSet('Both', 'Customer', 'Admin', 'None')]
    [string]$Client = 'Both',
    [ValidateRange(0, 65535)]
    [int]$Port = 0,
    [string]$XamppRoot,
    [ValidatePattern('^[a-zA-Z0-9_]{1,64}$')]
    [string]$DatabaseName,
    [ValidateSet('Check', 'Initialize', 'Migrate')]
    [string]$DatabaseAction = 'Check',
    [switch]$Offline
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$server = Join-Path $root 'server'
$customer = Join-Path $root 'client-customer'
$admin = Join-Path $root 'client-admin'
$maven = (Get-Command mvn.cmd -ErrorAction Stop).Source
$mavenFlags = @('-B')
if ($Offline) { $mavenFlags += '-o' }
Write-Host 'Checking Maven and JDK 21...'
$version = (mvn.cmd -v | Out-String)
if ($LASTEXITCODE -ne 0 -or $version -notmatch 'Java version:\s*21\.') {
    throw 'Maven must run with JDK 21. Check JAVA_HOME and mvn -v.'
}

function Test-LocalPort([int]$Number) {
    $connection = [Net.Sockets.TcpClient]::new()
    try {
        $connect = $connection.ConnectAsync('127.0.0.1', $Number)
        return $connect.Wait(500) -and $connection.Connected
    } catch {
        return $false
    } finally {
        $connection.Dispose()
    }
}

function Test-PortAvailable([int]$Number) {
    $listener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, $Number)
    try {
        $listener.Start()
        return $true
    } catch [Net.Sockets.SocketException] {
        return $false
    } finally {
        $listener.Stop()
    }
}

function Get-FashionServer([int]$Number) {
    try {
        $status = Invoke-RestMethod "http://127.0.0.1:$Number/api/health" -TimeoutSec 1
        if ($status.data.service -eq 'fashion-shop-server') { return $status.data }
    } catch {
        # An unrelated service can occupy this HTTP port.
    }
}

function Find-XamppRoot {
    if ($XamppRoot) {
        return (Resolve-Path -LiteralPath $XamppRoot).Path
    }
    $xamppFromWorkspace = if ($root -match '^(.*)[\\/]htdocs(?:[\\/]|$)') { $Matches[1] }
    foreach ($candidate in @("$env:SystemDrive/xampp", $xamppFromWorkspace) | Where-Object { $_ }) {
        if ((Test-Path "$candidate/mysql/bin/mysqld.exe") -and
            (Test-Path "$candidate/mysql/bin/my.ini")) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }
}

function Invoke-DatabaseQuery([string]$Sql) {
    $result = & $mysql '--host=127.0.0.1' '--user=root' '--connect-timeout=4' '--default-character-set=utf8mb4' -N -e $Sql
    if ($LASTEXITCODE -ne 0) {
        throw 'Cannot access local XAMPP MariaDB with the default root account. Apply the SQL manually for a custom account.'
    }
    return $result
}

function Invoke-DatabaseScript([string]$RelativePath) {
    $sql = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $root $RelativePath)
    $sql = $sql -replace '(?im)^(\s*(?:USE|CREATE DATABASE IF NOT EXISTS)\s+)fashion_shop\b', ('${1}' + $schema)
    $oldEncoding = $OutputEncoding
    try {
        $OutputEncoding = [Text.UTF8Encoding]::new($false)
        $sql | & $mysql '--host=127.0.0.1' '--user=root' '--connect-timeout=4' '--default-character-set=utf8mb4'
        if ($LASTEXITCODE -ne 0) { throw "Database script failed: $RelativePath" }
    } finally {
        $OutputEncoding = $oldEncoding
    }
}

function Start-ShopProcess([string]$Folder, [string[]]$Arguments) {
    $target = Join-Path $Folder 'target'
    New-Item -ItemType Directory -Path $target -Force | Out-Null
    $runId = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
    $stdout = Join-Path $target "shop-$runId.log"
    $stderr = Join-Path $target "shop-$runId-error.log"
    $process = Start-Process -FilePath $maven -ArgumentList ($mavenFlags + $Arguments) -WorkingDirectory $Folder -WindowStyle Hidden `
        -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
    [pscustomobject]@{ Process = $process; Log = $stdout; ErrorLog = $stderr }
}

$config = Join-Path $server 'src/main/resources/application.properties'
if (-not (Test-Path -LiteralPath $config)) {
    $example = Join-Path $server 'src/main/resources/application.example.properties'
    $bytes = New-Object byte[] 32
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $random.GetBytes($bytes) } finally { $random.Dispose() }
    $secret = [BitConverter]::ToString($bytes).Replace('-', '')
    $content = [IO.File]::ReadAllText($example).Replace('REPLACE_WITH_YOUR_OWN_RANDOM_SECRET', $secret)
    if ($DatabaseName) {
        $content = $content.Replace(':3306/fashion_shop?', ":3306/${DatabaseName}?")
    }
    [IO.File]::WriteAllText($config, $content)
}

$dbMatch = Select-String -LiteralPath $config -Pattern '^\s*db\.url\s*=\s*jdbc:mysql://(?:localhost|127\.0\.0\.1):3306/([a-zA-Z0-9_]{1,64})(?:\?|$)'
$schema = if ($dbMatch) { $dbMatch.Matches[0].Groups[1].Value }
if ($DatabaseName -and $DatabaseName -cne $schema) {
    throw "The local configuration uses a different database. Check $config before changing schemas."
}

Write-Host 'Checking local HTTP port...'
$defaultPorts = @(8080) + @(18080..18100)
$candidates = if ($Port) { @($Port) } else { $defaultPorts }
$existing = $null
$available = 0
foreach ($candidate in $candidates) {
    if (Test-PortAvailable $candidate) {
        if ($available -eq 0) { $available = $candidate }
        continue
    }
    if ($DatabaseAction -eq 'Check' -and $schema -ceq 'fashion_shop' -and !$DatabaseName) {
        $running = Get-FashionServer $candidate
        if ($running -and $running.variant_stock) {
            $existing = $running
            break
        }
    }
}
if ($existing) {
    $Port = [int]$existing.http_port
} elseif ($Port -eq 0) {
    if ($available -eq 0) { throw 'No default HTTP port is free. Pass -Port to select another port.' }
    $Port = $available
} elseif (-not (Test-PortAvailable $Port)) {
    throw "Port $Port is already in use. Omit -Port to select a free port."
}

if ($schema) {
    Write-Host "Checking local MariaDB ($schema)..."
    $xampp = Find-XamppRoot
    if (-not (Test-LocalPort 3306)) {
        if (-not $xampp) { throw 'MariaDB is not listening. Start it or pass -XamppRoot.' }
        $ini = Join-Path $xampp 'mysql/bin/my.ini'
        $target = Join-Path $server 'target'
        New-Item -ItemType Directory -Path $target -Force | Out-Null
        $dbLog = Join-Path $target "mariadb-$(Get-Date -Format 'yyyyMMdd-HHmmss-fff')"
        Write-Host "Starting XAMPP MariaDB. Logs: $dbLog*.log"
        $dbProcess = Start-Process -FilePath (Join-Path $xampp 'mysql/bin/mysqld.exe') -WorkingDirectory $xampp `
            -ArgumentList "--defaults-file=`"$ini`"", '--standalone' -WindowStyle Hidden -PassThru `
            -RedirectStandardOutput "$dbLog.log" -RedirectStandardError "$dbLog-error.log"
        for ($attempt = 0; $attempt -lt 15 -and -not (Test-LocalPort 3306); $attempt++) {
            if ($dbProcess.HasExited) { break }
            Start-Sleep -Seconds 1
        }
        if (-not (Test-LocalPort 3306)) { throw "MariaDB did not start. Check $ini and $dbLog-error.log." }
    }
    if ($DatabaseAction -ne 'Check') {
        if (-not $xampp) { throw 'Pass -XamppRoot to locate mysql.exe, or apply the SQL manually.' }
        $mysql = Join-Path $xampp 'mysql/bin/mysql.exe'
        $exists = Invoke-DatabaseQuery "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$schema'"
        if ($DatabaseAction -eq 'Initialize') {
            if ([int]$exists -ne 0) { throw "$schema already exists. Initialization only runs on a new database." }
            Write-Host "Creating $schema and importing sample data..."
            Invoke-DatabaseScript 'database/fashion_shop_mariadb.sql'
            Invoke-DatabaseScript 'database/seed.sql'
        } else {
            if ([int]$exists -eq 0) { throw "$schema does not exist. Use -DatabaseAction Initialize." }
            $sizeColor = Invoke-DatabaseQuery "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='$schema' AND TABLE_NAME='order_items' AND COLUMN_NAME IN ('selected_size','selected_color')"
            if ([int]$sizeColor -eq 0) { Invoke-DatabaseScript 'database/migrate_size_color.sql' }
            if ([int]$sizeColor -eq 1) { throw 'The earlier size/color migration is incomplete. Repair it before migrating.' }
            $variant = Invoke-DatabaseQuery "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='$schema' AND TABLE_NAME='order_items' AND COLUMN_NAME='variant_id'"
            if ([int]$variant -eq 0) { Invoke-DatabaseScript 'database/migrate_variant_stock.sql' }
        }
    }
} elseif ($DatabaseAction -ne 'Check') {
    throw 'Apply the database scripts manually when using a custom database connection.'
}

if ($Client -in 'Both', 'Admin') {
    Write-Host 'Building the shared JavaFX module...'
    & $maven @mavenFlags -f (Join-Path $customer 'pom.xml') '-DskipTests' install
    if ($LASTEXITCODE -ne 0) { throw 'Failed to install the shared JavaFX module.' }
}

$url = "http://127.0.0.1:$Port"
$previousHost = $env:FASHION_HTTP_HOST
$previousPort = $env:FASHION_HTTP_PORT
$previousUrl = $env:FASHION_SERVER_URL
try {
    $env:FASHION_HTTP_HOST = '127.0.0.1'
    $env:FASHION_HTTP_PORT = "$Port"
    $env:FASHION_SERVER_URL = $url
    $backend = $null
    if (-not $existing) {
        Write-Host "Starting Fashion Shop server at $url..."
        $backend = Start-ShopProcess $server @('compile', 'exec:java',
            '-Dexec.mainClass=com.fashionshop.server.ServerApplication')
    }
    $ready = $false
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
        if ($backend -and $backend.Process.HasExited) { break }
        try {
            $health = Invoke-RestMethod "$url/api/health/db" -TimeoutSec 2
            $products = Invoke-RestMethod "$url/api/products" -TimeoutSec 2
            $active = Get-FashionServer $Port
            if ($health.success -and $products.success -and $active.variant_stock) {
                $ready = $true
                break
            }
        } catch {
            # The server can still be compiling, starting, or waiting for MariaDB.
        }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) {
        if ($backend -and -not $backend.Process.HasExited) {
            & taskkill.exe /PID $backend.Process.Id /T /F | Out-Null
        }
        $detail = if ($backend) { "See $($backend.Log) and $($backend.ErrorLog)." } else { "Check the server at $url." }
        throw "Server not ready. $detail For an existing schema, check the variant migration in README.md."
    }
    Write-Host "Fashion Shop is ready at $url."
    if ($backend) { Write-Host "Server log: $($backend.Log)" }
    foreach ($folder in @(
        if ($Client -in 'Both', 'Customer') { $customer }
        if ($Client -in 'Both', 'Admin') { $admin }
    )) {
        $frontend = Start-ShopProcess $folder @('javafx:run')
        Write-Host "Starting $(Split-Path -Leaf $folder) (log: $($frontend.Log))."
        if ($frontend.Process.WaitForExit(2000) -and $frontend.Process.ExitCode -ne 0) {
            throw "Client failed to start. See $($frontend.Log) and $($frontend.ErrorLog)."
        }
    }
} finally {
    $env:FASHION_HTTP_HOST = $previousHost
    $env:FASHION_HTTP_PORT = $previousPort
    $env:FASHION_SERVER_URL = $previousUrl
}
