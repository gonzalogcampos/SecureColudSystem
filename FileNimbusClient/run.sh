#!/bin/sh
echo 'Compilando ficheros...'
javac ./src/*.java -d ./bin/
echo 'Iniciando aplicacion...'
java -cp ./bin/ Main