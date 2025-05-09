export CLASSPATH="./Practica1/out/production/Practica1:./Practica1/out/production/Practica1/AIMA.jar:./Practica1/out/production/Practica1/RedSensores.jar"

rm -f ./Practica1/out/production/Practica1/Main.class ./Practica1/out/production/Practica1/IA/RedUPC/*.class
javac -cp $CLASSPATH -d ./Practica1/out/production/Practica1 ./Practica1/IA/RedUPC/*.java ./Practica1/Main.java
java -cp $CLASSPATH Main
rm -f ./Practica1/out/production/Practica1/IA/RedUPC/*.class