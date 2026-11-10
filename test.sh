#!/usr/bin/env sh
# Compila a aplicação e executa as verificações automatizadas.
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out/main out/test
javac -encoding UTF-8 -d out/main $(find src/main/java -name "*.java")
javac -encoding UTF-8 -cp out/main -d out/test $(find src/test/java -name "*.java")
java -Dfile.encoding=UTF-8 -cp out/main:out/test br.com.plataformacursos.VerificacoesPlataforma
