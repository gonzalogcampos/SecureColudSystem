#!/usr/bin/env bash
@echo "Preparando las carpetas..."
@rm -r out
@mkdir out
@rm -r bin
@mkdir bin
@echo "Compilando los archivos .java"
@javac -d out *.java
@echo "Creando el archivo .jar"
@jar cvfm bin/Server.jar manifest.mf -C out/ .
@echo "Finalizado"