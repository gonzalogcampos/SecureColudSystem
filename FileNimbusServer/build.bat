@echo Preparando las carpetas
@rd /S /Q out
@mkdir out%
@rd /S /Q bin
@mkdir bin%

@echo Compilando los archivos .java
@javac -d out src/*.java

@echo Creando el archivo .jar
@jar cvfm bin/Server.jar manifest.mf -C out/ .

@echo Finalizado