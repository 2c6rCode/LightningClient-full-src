@echo off
cd /d "%~dp0"
title LightningClient Launcher
echo Starting LightningClient...
call gradlew.bat runClient
pause
