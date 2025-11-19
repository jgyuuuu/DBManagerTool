@echo off
chcp 65001 >nul
java -cp "DBManagerTool.jar;lib\*" com.dbtool.Main --gui
pause