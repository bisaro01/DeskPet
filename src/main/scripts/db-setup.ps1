#Run "powershell -ExecutionPolicy Bypass -File src/main/scripts/db-setup.ps1" from project root

$configPath = "src/main/resources/config.properties"

if (!(Test-Path $configPath)) {
    Write-Host "Missing $configPath"
    exit 1
}

$config = @{}
Get-Content $configPath | ForEach-Object {
    if ($_ -match "^\s*([^#][^=]*)=(.*)$") {
        $config[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$dbHost = $config["db.host"]
$dbPort = $config["db.port"]
$dbName = $config["db.name"]
$dbUser = $config["db.user"]
$dbPassword = $config["db.password"]

$env:PGPASSWORD = $dbPassword

$exists = psql -h $dbHost -p $dbPort -U $dbUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$dbName'"

if ($exists.Trim() -ne "1") {
    createdb -h $dbHost -p $dbPort -U $dbUser $dbName
}

psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -f src/main/resources/schema.sql
psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -f src/main/resources/seed.sql

Write-Host "Database setup complete."