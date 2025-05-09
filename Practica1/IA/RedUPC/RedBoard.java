package IA.RedUPC;

import IA.Red.Sensores;
import IA.Red.Sensor;
import IA.Red.Centro;
import IA.Red.CentrosDatos;

import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.*;

import static java.lang.Thread.sleep;

public class RedBoard {
    ArrayList<Nodo> nodos;
    double costeTotal = 0;
    double infoTotal = 0;
    double infoMax = 1;
    Random random = new Random();
    boolean S;
    boolean C;
    boolean A;
    /* Constructor */
    public RedBoard(int nS, int nC, String metodo, int seed1, int seed2, boolean S, boolean C, boolean A) throws InterruptedException {
       Sensores s = new Sensores(nS, seed1);
       CentrosDatos c = new CentrosDatos(nC,seed2);
       this.S = S;
       this.C = C;
       this.A = A;
       nodos = crearEstadoInicial(s,c, metodo);
       actualizaCostes();
    }

    public double getInfoTotal() {
        return infoTotal;
    }

    public double getCosteTotal() {
        return costeTotal;
    }

    public double getInfoMax() {
        return infoMax;
    }

    public RedBoard(){
        nodos = new ArrayList<>();
    }

    public ArrayList<Nodo> crearEstadoInicial(Sensores sAux, CentrosDatos cAux, String metodo) throws InterruptedException {
        ArrayList<Nodo> listaNodos = new ArrayList<>();
        for (int i = 0; i < sAux.size(); i++) {
            listaNodos.add(new NSensor(i, sAux.get(i)));
            infoMax += sAux.get(i).getCapacidad();
        }
        for (int i = 0; i < cAux.size(); i++) {
            listaNodos.add(new NCentro(i + sAux.size(), cAux.get(i)));
        }

        if (metodo.equals("greedy")){
            estadoInicialGreedy(listaNodos, sAux, cAux);
        } else if (metodo.equals("proximidad")){
            estadoInicialProximidad(listaNodos, sAux, cAux);
        } else if (metodo.equals("random")){
            //Collections.shuffle(listaNodos);
            estadoInicialRandom(listaNodos);
        }

        return listaNodos;
    }

    private void estadoInicialGreedy(ArrayList<Nodo> listaNodos, Sensores sAux, CentrosDatos cAux) throws InterruptedException {
        PriorityQueue<AbstractMap.SimpleEntry<Integer, Integer>> priorityQueue = new PriorityQueue<>(Map.Entry.comparingByValue());
        ArrayList<Integer> sensoresAsignados = new ArrayList<>();
        for (int i = 0; i < sAux.size(); ++i){
            NSensor sensor = (NSensor) listaNodos.get(i);
            int minDist = Integer.MAX_VALUE;
            int idMin = -1;
            for (int j = sAux.size(); j < listaNodos.size(); ++j){
                NCentro centro = (NCentro) listaNodos.get(j);
                int dist = (sensor.getSensor().getCoordX() - centro.getCentro().getCoordX()) + (sensor.getSensor().getCoordX() - centro.getCentro().getCoordX());
                dist += (sensor.getSensor().getCoordY() - centro.getCentro().getCoordY()) + (sensor.getSensor().getCoordY() - centro.getCentro().getCoordY());
                if (dist < minDist){
                    minDist = dist;
                    idMin = j;
                }
            }
            sensoresAsignados.add(idMin);
            priorityQueue.add(new AbstractMap.SimpleEntry<>(minDist, i));
        }
        while (!priorityQueue.isEmpty()){
            AbstractMap.SimpleEntry<Integer, Integer> entry = priorityQueue.poll();
            int sensorId = entry.getValue();
            int centroId = sensoresAsignados.get(sensorId);
            NSensor sensor = (NSensor) listaNodos.get(sensorId);
            if (! listaNodos.get(centroId).maxRecibidos()) {
                ((NSensor) listaNodos.get(sensorId)).setIdDestino(centroId);
                listaNodos.get(centroId).agregarRecibido(sensorId);
            } else {
                int minDist = Integer.MAX_VALUE;
                int idMin = -1;
                boolean colocado = false;
                for (int j = sAux.size(); j < listaNodos.size(); ++j){
                    NCentro centro = (NCentro) listaNodos.get(j);
                    int dist = (sensor.getSensor().getCoordX() - centro.getCentro().getCoordX()) + (sensor.getSensor().getCoordX() - centro.getCentro().getCoordX());
                    dist += (sensor.getSensor().getCoordY() - centro.getCentro().getCoordY()) + (sensor.getSensor().getCoordY() - centro.getCentro().getCoordY());
                    if (dist < minDist && centro.getNumRecibidos() < 25){
                        minDist = dist;
                        idMin = j;
                        colocado = true;
                    }
                }
                if (!colocado){ //Si no es pot colocar en cap centre, es coloca amb el sensor mes proper
                    for (int j = 0; j < sAux.size(); ++j){
                        if (j == sensorId) continue;
                        NSensor otroSensor = (NSensor) listaNodos.get(j);
                        if (otroSensor.getNumRecibidos() < 3 && !formariaCiclo(sensorId, j, listaNodos)){
                            int dist = (sensor.getSensor().getCoordX() - otroSensor.getSensor().getCoordX()) + (sensor.getSensor().getCoordX() - otroSensor.getSensor().getCoordX());
                            dist += (sensor.getSensor().getCoordY() - otroSensor.getSensor().getCoordY()) + (sensor.getSensor().getCoordY() - otroSensor.getSensor().getCoordY());
                            if (dist < minDist){
                                minDist = dist;
                                idMin = j;
                            }
                        }
                    }
                }
                sensoresAsignados.set(sensorId, idMin);
                priorityQueue.add(new AbstractMap.SimpleEntry<>(minDist, sensorId));
            }
            //RedVisualizer.visualizarRed(listaNodos);
            //Thread.sleep(500);
        }
    }

    private void estadoInicialProximidad(ArrayList<Nodo> listaNodos, Sensores sAux, CentrosDatos cAux) throws InterruptedException {
        int numSensores = sAux.size();
        int numCentros = cAux.size();
        int[] conexionesRecibidas = new int[listaNodos.size()];

        for (int idSensor = 0; idSensor < numSensores; idSensor++) {
            NSensor sensor = (NSensor) listaNodos.get(idSensor);
            double menorDistancia = Double.MAX_VALUE;
            int mejorDestino = -1;
            for (int i = numSensores; i < numSensores + numCentros; i++) {
                NCentro centro = (NCentro) listaNodos.get(i);
                if (conexionesRecibidas[i] < 25) {
                    int distancia = (sensor.getSensor().getCoordX() - centro.getCentro().getCoordX()) * (sensor.getSensor().getCoordX() - centro.getCentro().getCoordX());
                    distancia += (sensor.getSensor().getCoordY() - centro.getCentro().getCoordY()) * (sensor.getSensor().getCoordY() - centro.getCentro().getCoordY());
                    if (distancia < menorDistancia) {
                        menorDistancia = distancia;
                        mejorDestino = i;
                    }
                }
            }
            for (int i = 0; i < numSensores; i++) {
                if (i == idSensor) continue;
                NSensor otroSensor = (NSensor) listaNodos.get(i);

                if (conexionesRecibidas[i] < 3 && !formariaCiclo(idSensor, i, listaNodos)){
                    int distancia = (sensor.getSensor().getCoordX() - otroSensor.getSensor().getCoordX()) * (sensor.getSensor().getCoordX() - otroSensor.getSensor().getCoordX());
                    distancia += (sensor.getSensor().getCoordY() - otroSensor.getSensor().getCoordY()) * (sensor.getSensor().getCoordY() - otroSensor.getSensor().getCoordY());
                    if (distancia < menorDistancia) {
                        menorDistancia = distancia;
                        mejorDestino = i;
                    }
                }
            }
            if (mejorDestino != -1) {
                sensor.idDestino = mejorDestino;
                conexionesRecibidas[mejorDestino]++;
                if (mejorDestino >= numSensores) {
                    NCentro centro = (NCentro) listaNodos.get(mejorDestino);
                    centro.recibidos.add(idSensor);
                } else {
                    NSensor sensorDestino = (NSensor) listaNodos.get(mejorDestino);
                    sensorDestino.recibidos.add(idSensor);
                }
            }
            //RedVisualizer.visualizarRed(listaNodos);
            //Thread.sleep(500);
        }
    }

    private void estadoInicialRandom(ArrayList<Nodo> listaNodos) throws InterruptedException {
        for (int i = 0; i < listaNodos.size(); ++i){
            if (listaNodos.get(i) instanceof NSensor){
                NSensor sensor = (NSensor) listaNodos.get(i);
                int destino = random.nextInt(listaNodos.size());
                while (destino == i || formariaCiclo(i, destino, listaNodos) || listaNodos.get(destino).maxRecibidos()){
                    destino = random.nextInt(listaNodos.size());
                }
                sensor.setIdDestino(destino);
                if (listaNodos.get(destino) instanceof NCentro){
                    NCentro centro = (NCentro) listaNodos.get(destino);
                    centro.agregarRecibido(i);
                } else {
                    NSensor sensorDestino = (NSensor) listaNodos.get(destino);
                    sensorDestino.agregarRecibido(i);
                }
            }
            //RedVisualizer.visualizarRed(listaNodos);
            //Thread.sleep(500);
        }
    }

    public boolean formariaCiclo(int idSensor, int idDestino, ArrayList<Nodo> listaNodos) {
        boolean[] visitado = new boolean[listaNodos.size()];
        return dfs(idSensor, idDestino, listaNodos, visitado);
    }

    private boolean dfs(int actual, int objetivo, ArrayList<Nodo> listaNodos, boolean[] visitado) {
        if (actual == objetivo) {
            return true;
        }
        visitado[actual] = true;
        NSensor sensor = (NSensor) listaNodos.get(actual);
        for (int vecino : sensor.getRecibidos()) {
            if (!visitado[vecino] && dfs(vecino, objetivo, listaNodos, visitado)) {
                return true;
            }
        }
        return false;
    }

    public void swap(int id1, int id2) {
        int destino1 = ((NSensor) nodos.get(id1)).getIdDestino();
        int destino2 = ((NSensor) nodos.get(id2)).getIdDestino();
        nodos.get(destino1).eliminaRecibido(id1);
        nodos.get(destino1).agregarRecibido(id2);
        nodos.get(destino2).eliminaRecibido(id2);
        nodos.get(destino2).agregarRecibido(id1);
        ((NSensor) nodos.get(id1)).setIdDestino(destino2);
        ((NSensor) nodos.get(id2)).setIdDestino(destino1);
        //actualizaCostesParcial(id1,id2);
    }
    /*Elimina la conneccio id1 a id1D i crea la coneccio de id1 a id2*/
    public void conecta(int id1, int id2) {
        int destino1 = ((NSensor) nodos.get(id1)).getIdDestino();
        nodos.get(destino1).eliminaRecibido(id1);
        nodos.get(id2).agregarRecibido(id1);
        ((NSensor) nodos.get(id1)).setIdDestino(id2);
        //actualizaCostesParcial(id1,id2);
    }
    /*Elimina totes les conneccions que entren a id1 i les redirecciona a id2*/
    public void alibera(int id1, int id2) {
        ArrayList<Integer> aux1 = nodos.get(id1).getRecibidos();
        for (Integer integer : aux1) {
            nodos.get(id2).agregarRecibido(integer);
            ((NSensor) nodos.get(integer)).setIdDestino(id2);
        }
        nodos.get(id1).recibidos.clear();
       //actualizaCostesParcial(id1,id2);
    }

    public void actualizaCostesParcial(int id1, int id2) {
        Queue<Nodo> pendientes = new LinkedList<Nodo>();
        pendientes.add(nodos.get(id1));
        pendientes.add(nodos.get(id2));
        if(nodos.get(id1) instanceof NSensor) costeTotal -= ((NSensor) nodos.get(id1)).getCoste();
        if(nodos.get(id2) instanceof NSensor) costeTotal -= ((NSensor) nodos.get(id2)).getCoste();
        costeTotal = 0;
        infoTotal = 0;
        while (!pendientes.isEmpty()) {
            Nodo aux = pendientes.poll();
            boolean tieneInvalido = false;
            for (int x : aux.getRecibidos()) {
                if (((NSensor) nodos.get(x)).getInfoEnviable() == -1) {
                    tieneInvalido = true;
                    break;
                }
            }
            if (tieneInvalido) {
                pendientes.offer(aux);
                continue;
            }
            if (aux instanceof NCentro) {
                double info_aux = 0;
                double coste_aux = 0;
                for (int x : aux.getRecibidos()) {
                    info_aux += ((NSensor) nodos.get(x)).getInfoEnviable();
                    coste_aux += ((NSensor) nodos.get(x)).getCoste();
                }
                ((NCentro) aux).informacionRecibida = Math.min(info_aux,150);
                ((NCentro) aux).costeRecibido = coste_aux;
            } else {
                double info_aux = ((NSensor) aux).getSensor().getCapacidad();
                double costeAux = 0;
                for (int x : aux.getRecibidos()) {
                    info_aux += ((NSensor) nodos.get(x)).getInfoEnviable();
                    costeAux += ((NSensor) nodos.get(x)).getCoste();
                }
                ((NSensor) aux).infoEnviable = Math.min(info_aux, ((NSensor) aux).s.getCapacidad() * 3);
                int x1 = ((NSensor) aux).getSensor().getCoordX();
                int y1 = ((NSensor) aux).getSensor().getCoordY();
                int x2, y2;
                if (nodos.get(((NSensor) aux).idDestino) instanceof NCentro) {
                    x2 = ((NCentro) nodos.get(((NSensor) aux).idDestino)).getCentro().getCoordX();
                    y2 = ((NCentro) nodos.get(((NSensor) aux).idDestino)).getCentro().getCoordY();
                    if (!pendientes.contains(nodos.get(((NSensor) aux).idDestino)))
                        pendientes.offer(nodos.get(((NSensor) aux).idDestino));
                } else {
                    x2 = ((NSensor) nodos.get(((NSensor) aux).idDestino)).getSensor().getCoordX();
                    y2 = ((NSensor) nodos.get(((NSensor) aux).idDestino)).getSensor().getCoordY();
                    if (!pendientes.contains(nodos.get(((NSensor) aux).idDestino)))
                        pendientes.offer(nodos.get(((NSensor) aux).idDestino));
                }
                double dist = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
                ((NSensor) aux).coste = costeAux + dist * ((NSensor) aux).infoEnviable;
            }
        }
        for(Nodo x : nodos) {
            if(x instanceof NCentro) {
                infoTotal += ((NCentro) x).informacionRecibida;
                costeTotal += ((NCentro) x).costeRecibido;
            }
        }
    }

    public void actualizaCostesParcialAlibera(int id1, int id2, ArrayList<Integer> recibidosId1) {
        Queue<Nodo> pendientes = new LinkedList<Nodo>();
        pendientes.add(nodos.get(id1));
        pendientes.add(nodos.get(id2));
        for (Integer x : recibidosId1) {
            pendientes.add(nodos.get(x));
        }
        costeTotal = 0;
        infoTotal = 0;
        while (!pendientes.isEmpty()) {
            Nodo aux = pendientes.poll();
            boolean tieneInvalido = false;
            for (int x : aux.getRecibidos()) {
                if (((NSensor) nodos.get(x)).getInfoEnviable() == -1) {
                    tieneInvalido = true;
                    break;
                }
            }
            if (tieneInvalido) {
                pendientes.offer(aux);
                continue;
            }
            if (aux instanceof NCentro) {
                double info_aux = 0;
                double coste_aux = 0;
                for (int x : aux.getRecibidos()) {
                    info_aux += ((NSensor) nodos.get(x)).getInfoEnviable();
                    coste_aux += ((NSensor) nodos.get(x)).getCoste();
                }
                ((NCentro) aux).informacionRecibida = Math.min(info_aux,150);
                ((NCentro) aux).costeRecibido = coste_aux;
            } else {
                double info_aux = ((NSensor) aux).getSensor().getCapacidad();
                double costeAux = 0;
                for (int x : aux.getRecibidos()) {
                    info_aux += ((NSensor) nodos.get(x)).getInfoEnviable();
                    costeAux += ((NSensor) nodos.get(x)).getCoste();
                }
                ((NSensor) aux).infoEnviable = Math.min(info_aux, ((NSensor) aux).s.getCapacidad() * 3);
                int x1 = ((NSensor) aux).getSensor().getCoordX();
                int y1 = ((NSensor) aux).getSensor().getCoordY();
                int x2, y2;
                if (nodos.get(((NSensor) aux).idDestino) instanceof NCentro) {
                    x2 = ((NCentro) nodos.get(((NSensor) aux).idDestino)).getCentro().getCoordX();
                    y2 = ((NCentro) nodos.get(((NSensor) aux).idDestino)).getCentro().getCoordY();
                    if (!pendientes.contains(nodos.get(((NSensor) aux).idDestino)))
                        pendientes.offer(nodos.get(((NSensor) aux).idDestino));
                } else {
                    x2 = ((NSensor) nodos.get(((NSensor) aux).idDestino)).getSensor().getCoordX();
                    y2 = ((NSensor) nodos.get(((NSensor) aux).idDestino)).getSensor().getCoordY();
                    if (!pendientes.contains(nodos.get(((NSensor) aux).idDestino)))
                        pendientes.offer(nodos.get(((NSensor) aux).idDestino));
                }
                double dist = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
                ((NSensor) aux).coste = costeAux + dist * ((NSensor) aux).infoEnviable;
            }
        }
        for(Nodo x : nodos) {
            if(x instanceof NCentro) {
                infoTotal += ((NCentro) x).informacionRecibida;
                costeTotal += ((NCentro) x).costeRecibido;
            }
        }
    }

    public void actualizaCostesParcialConecta(int id1, int id2, int id3) {
        Queue<Nodo> pendientes = new LinkedList<Nodo>();
        pendientes.add(nodos.get(id1));
        pendientes.add(nodos.get(id2));
        pendientes.add(nodos.get(id3));
        costeTotal = 0;
        infoTotal = 0;
        while (!pendientes.isEmpty()) {
            Nodo aux = pendientes.poll();
            boolean tieneInvalido = false;
            for (int x : aux.getRecibidos()) {
                if (((NSensor) nodos.get(x)).getInfoEnviable() == -1) {
                    tieneInvalido = true;
                    break;
                }
            }
            if (tieneInvalido) {
                pendientes.offer(aux);
                continue;
            }
            if (aux instanceof NCentro) {
                double info_aux = 0;
                double coste_aux = 0;
                for (int x : aux.getRecibidos()) {
                    info_aux += ((NSensor) nodos.get(x)).getInfoEnviable();
                    coste_aux += ((NSensor) nodos.get(x)).getCoste();
                }
                ((NCentro) aux).informacionRecibida = Math.min(info_aux,150);
                ((NCentro) aux).costeRecibido = coste_aux;
            } else {
                double info_aux = ((NSensor) aux).getSensor().getCapacidad();
                double costeAux = 0;
                for (int x : aux.getRecibidos()) {
                    info_aux += ((NSensor) nodos.get(x)).getInfoEnviable();
                    costeAux += ((NSensor) nodos.get(x)).getCoste();
                }
                ((NSensor) aux).infoEnviable = Math.min(info_aux, ((NSensor) aux).s.getCapacidad() * 3);
                int x1 = ((NSensor) aux).getSensor().getCoordX();
                int y1 = ((NSensor) aux).getSensor().getCoordY();
                int x2, y2;
                if (nodos.get(((NSensor) aux).idDestino) instanceof NCentro) {
                    x2 = ((NCentro) nodos.get(((NSensor) aux).idDestino)).getCentro().getCoordX();
                    y2 = ((NCentro) nodos.get(((NSensor) aux).idDestino)).getCentro().getCoordY();
                    if (!pendientes.contains(nodos.get(((NSensor) aux).idDestino)))
                        pendientes.offer(nodos.get(((NSensor) aux).idDestino));
                } else {
                    x2 = ((NSensor) nodos.get(((NSensor) aux).idDestino)).getSensor().getCoordX();
                    y2 = ((NSensor) nodos.get(((NSensor) aux).idDestino)).getSensor().getCoordY();
                    if (!pendientes.contains(nodos.get(((NSensor) aux).idDestino)))
                        pendientes.offer(nodos.get(((NSensor) aux).idDestino));
                }
                double dist = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
                ((NSensor) aux).coste = costeAux + dist * ((NSensor) aux).infoEnviable;
            }
        }
        for(Nodo x : nodos) {
            if(x instanceof NCentro) {
                infoTotal += ((NCentro) x).informacionRecibida;
                costeTotal += ((NCentro) x).costeRecibido;
            }
        }
    }

    public void actualizaCostes() {
        Queue<Nodo> pendientes = new LinkedList<Nodo>();
        for (int i = 0; i < nodos.size(); ++i) {
            if (nodos.get(i).recibidos.isEmpty()) {
                pendientes.offer(nodos.get(i));
            }
        }
        while (!pendientes.isEmpty()) {
            Nodo aux = pendientes.poll();
            boolean tieneInvalido = false;
            for (int x : aux.getRecibidos()) {
                if (((NSensor) nodos.get(x)).getInfoEnviable() == -1) {
                    tieneInvalido = true;
                    break;
                }
            }
            if (tieneInvalido) {
                pendientes.offer(aux);
                continue;
            }
            if (aux instanceof NCentro) {
                double info_aux = 0;
                double coste_aux = 0;
                for (int x : aux.getRecibidos()) {
                    info_aux += ((NSensor) nodos.get(x)).getInfoEnviable();
                    coste_aux += ((NSensor) nodos.get(x)).getCoste();
                }
                ((NCentro) aux).informacionRecibida = Math.min(info_aux,150);
                ((NCentro) aux).costeRecibido = coste_aux;
                infoTotal += info_aux;
                costeTotal += coste_aux;
            } else {
                double info_aux = ((NSensor) aux).getSensor().getCapacidad();
                double costeAux = 0;
                for (int x : aux.getRecibidos()) {
                    info_aux += ((NSensor) nodos.get(x)).getInfoEnviable();
                    costeAux += ((NSensor) nodos.get(x)).getCoste();
                }
                ((NSensor) aux).infoEnviable = Math.min(info_aux, ((NSensor) aux).s.getCapacidad() * 3);
                int x1 = ((NSensor) aux).getSensor().getCoordX();
                int y1 = ((NSensor) aux).getSensor().getCoordY();
                int x2, y2;
                if (nodos.get(((NSensor) aux).idDestino) instanceof NCentro) {
                    x2 = ((NCentro) nodos.get(((NSensor) aux).idDestino)).getCentro().getCoordX();
                    y2 = ((NCentro) nodos.get(((NSensor) aux).idDestino)).getCentro().getCoordY();
                    if (!pendientes.contains(nodos.get(((NSensor) aux).idDestino)))
                        pendientes.offer(nodos.get(((NSensor) aux).idDestino));
                } else {
                    x2 = ((NSensor) nodos.get(((NSensor) aux).idDestino)).getSensor().getCoordX();
                    y2 = ((NSensor) nodos.get(((NSensor) aux).idDestino)).getSensor().getCoordY();
                    if (!pendientes.contains(nodos.get(((NSensor) aux).idDestino)))
                        pendientes.offer(nodos.get(((NSensor) aux).idDestino));
                }
                double dist = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
                ((NSensor) aux).coste = costeAux + dist * ((NSensor) aux).infoEnviable;
            }
        }
    }

    public ArrayList<Nodo> getNodos() {
        return nodos;
    }

    /* Heuristic function */
    public double heuristic() {
        return costeTotal*(infoMax-infoTotal);
    }

    /* Goal test */
    public boolean is_goal() {
        return false;
    }

    /*Hauria de funcionar*/
    public RedBoard copyOf() {
        RedBoard copia = new RedBoard();
        for (Nodo nodo : nodos) {
            copia.nodos.add(nodo.copyOf());
        }
        copia.costeTotal = costeTotal;
        copia.infoTotal = infoTotal;
        copia.infoMax = infoMax;
        copia.S = S;
        copia.C = C;
        copia.A = A;
        return copia;
    }

}
