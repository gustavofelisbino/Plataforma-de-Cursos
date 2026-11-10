@echo off
REM Compila e executa a aplicação. Use "run.bat --demo" para o fluxo automático.
cd /d "%~dp0"
if exist out\main rmdir /s /q out\main
mkdir out\main
dir /s /b src\main\java\*.java > out\fontes.txt
javac -encoding UTF-8 -d out\main @out\fontes.txt || exit /b 1
java -Dfile.encoding=UTF-8 -cp out\main br.com.plataformacursos.Main %*
