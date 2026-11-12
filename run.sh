#!/usr/bin/env sh
# Compila e executa a aplicação. Use "./run.sh --demo" para o fluxo automático ou "./run.sh --web" para o front-end.
set -e
cd "$(dirname "$0")"
rm -rf out/main
mkdir -p out/main
javac -encoding UTF-8 -d out/main $(find src/main/java -name "*.java")
java -Dfile.encoding=UTF-8 -cp out/main:src/main/resources br.com.plataformacursos.Main "$@"
