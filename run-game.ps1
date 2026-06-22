$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$classpath = "lib\poi-4.1.2.jar;lib\poi-ooxml-4.1.2.jar;lib\poi-ooxml-schemas-4.1.2.jar;lib\xmlbeans-3.1.0.jar"
$sources = Get-ChildItem -Recurse src -Filter "*.java" | Select-Object -ExpandProperty FullName

javac -encoding UTF-8 -cp $classpath -d out $sources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp "out;$classpath" my_base.App
