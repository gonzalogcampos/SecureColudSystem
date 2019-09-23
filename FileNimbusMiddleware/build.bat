@echo Preparando las carpetas
@rd /S /Q out
@mkdir out%
@rd /S /Q bin
@mkdir bin%

@echo Compilando los archivos .java
@javac -d out src/*.java -cp lib/Java-WebSocket-1.3.9.jar;lib/javax.xml.bind.jar;lib/org.json.jar

@echo Creando el archivo .jar
@jar cvfm bin/Server.jar manifest.mf -C out/ .

@echo Finalizado