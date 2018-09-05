@echo on
java -jar ../obfuscator-1.65.jar --mode 0 --package --packagerMainClass Inicio --invokeDynamic --jarIn dist/SistemaDeliveryWhatsApp.jar --jarOut dist/SistemaDeliveryWhatsApp-OBF.jar
java -jar ../obfuscator-1.65.jar --mode 0 --invokeDynamic --jarIn dist/lib/WebWhatsNew.jar --jarOut dist/lib/WebWhatsNew-OBF.jar

del "dist\SistemaDeliveryWhatsApp.jar"
del "dist\lib\WebWhatsNew.jar"
move "dist\lib\WebWhatsNew-OBF.jar" "dist\lib\WebWhatsNew.jar"
move "dist\SistemaDeliveryWhatsApp-OBF.jar" "dist\SistemaDeliveryWhatsApp.jar"

pause