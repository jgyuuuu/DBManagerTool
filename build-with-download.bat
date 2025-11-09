@echo off
chcp 65001 >nul
echo DBManagerTool Build Script

mkdir lib 2>nul
mkdir target\classes 2>nul

if not exist "lib\mysql-connector-java-8.0.33.jar" (
    echo ????MySQL??...
    powershell -Command "try {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar' -OutFile 'lib/mysql-connector-java-8.0.33.jar'; echo ????!} catch {echo ??????????}"
)

if not exist "lib\mysql-connector-java-8.0.33.jar" (
    echo.
    echo ?????MySQL???
    echo https://mvnrepository.com/artifact/mysql/mysql-connector-java/8.0.33
    echo ??? lib\ ???
    pause
    exit /b 1
)

echo ????...
javac -cp "lib\*" -d target\classes src\main\java\com\dbtool\*.java src\main\java\com\dbtool\core\*.java src\main\java\com\dbtool\util\*.java src\main\java\com\dbtool\model\*.java src\main\java\com\dbtool\command\*.java

if errorlevel 1 (
    echo ?????
    pause
    exit /b 1
)

echo ??JAR??...
jar cfe DBManagerTool.jar com.dbtool.Main -C target\classes .

echo.
echo ?????
echo ??: java -jar DBManagerTool.jar
pause
