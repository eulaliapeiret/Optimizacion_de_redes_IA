# Práctica de busqueda local

Este proyecto implementa un algoritmo de búsqueda local para diseñar un sistema de interconexión entre una red de sensores y un conjunto de centros de datos, que permita hacer el almacenamiento de toda la información capturada. El programa permite trabajar con dos algoritmos distintos, el Hill Climbing y el Simulated Annealing.

# Uso
Para ejecutar el programa, primero se deber descomprimir el archivo zip y luego ejecutar los siguientes comandos en la terminal:

1. Añadimos permisos de ejecución al script:
```bash
chmod +x run.sh
```

2. Ejecutamos el script:
```bash
./run.sh
```

3. Una vez ejecutando el script, el mismo programa nos preguntará que datos queremos introducir, y nos dará la opción de elegir entre los dos algoritmos.
```bash
#Ejemplo:
Num sensores:
100
Num Centros: 
4
Metodo Estado Inicial: (random, proximidad, greedy)
random
¿Que operadores quieres usar?
¿Usar Swap? (Y/n)
Y
¿Usar Conecta? (Y/n)
n
¿Usar Alibera? (Y/n)
Y
¿Quieres usar semillas? (Y/n)
n
¿Quieres visualizar la red? (Y/n)
n
Que algoritmo quieres usar HillClimbing o SimulatedAnnealing? (HC/SA)
HC
```
4. Una vez introducido los datos, el programa se ejecutará y nos mostrará el coste final y la información enviada de la red, junto con el tiempo de ejecución del algoritmo.
```bash
#Ejemplo:
Coste final: 235397.0 Info final: 265.0 Info max: 265.0
Temps d'execució: 213ms
```

# Información adicional
El script solo funciona en sistemas Linux o MacOS. Si se quiere ejecutar en Windows, se deberia instalar subsistema. En este caso, no se garantiza que la opción ¿Quieres visualizar la red? Y funcione, por tanto mejor no usarla.
Para usar en Windows, se recomienda usar un IDE como Idea.