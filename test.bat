@echo off
REM Compila a aplicação e executa as verificações automatizadas.
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out\main out\test
dir /s /b src\main\java\*.java > out\fontes-main.txt
dir /s /b src\test\java\*.java > out\fontes-test.txt
javac -encoding UTF-8 -d out\main @out\fontes-main.txt || exit /b 1
javac -encoding UTF-8 -cp out\main -d out\test @out\fontes-test.txt || exit /b 1
java -Dfile.encoding=UTF-8 -cp out\main;out\test br.com.plataformacursos.VerificacoesPlataforma
