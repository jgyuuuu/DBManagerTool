@echo off
chcp 65001 >nul
echo DBManagerTool Build Script

mkdir lib 2>nul
mkdir target\classes 2>nul

if not exist "lib\mysql-connector-java-8.0.33.jar" (
    echo 下载MySQL驱动...
    powershell -Command "try {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar' -OutFile 'lib/mysql-connector-java-8.0.33.jar'; echo 下载成功!} catch {echo 下载失败，请手动下载}"
)

if not exist "lib\mysql-connector-java-8.0.33.jar" (
    echo.
    echo 请手动下载MySQL驱动
    echo https://mvnrepository.com/artifact/mysql/mysql-connector-java/8.0.33
    echo 放到 lib\ 目录下
    pause
    exit /b 1
)

echo 编译...
javac -cp "lib\*" -d target\classes src\main\java\com\dbtool\*.java src\main\java\com\dbtool\core\*.java src\main\java\com\dbtool\util\*.java src\main\java\com\dbtool\model\*.java src\main\java\com\dbtool\command\*.java

if errorlevel 1 (
    echo 编译失败
    pause
    exit /b 1
)

echo 创建JAR文件...
jar cfe DBManagerTool.jar com.dbtool.Main -C target\classes .

echo.
echo 构建完成!
echo 使用方式:
echo   命令行模式: java -cp "DBManagerTool.jar;lib\*" com.dbtool.Main
echo   图形界面模式: java -cp "DBManagerTool.jar;lib\*" com.dbtool.Main --gui
echo   或者直接: java -jar DBManagerTool.jar --gui
pause