package IA.RedUPC;

import IA.Red.Sensor;

import java.util.ArrayList;

public class NSensor extends Nodo {
    Sensor s;
    int idDestino = -1;
    double coste = 0;
    double infoEnviable;

    public NSensor(int idIni, Sensor sIni) {
        super(idIni);
        s = sIni;
        infoEnviable = -1;
    }

    public int getIdDestino() {
        return idDestino;
    }

    public void setIdDestino(int idDestino) {
        this.idDestino = idDestino;
    }

    public void setCoste(double coste) {
        this.coste = coste;
    }

    public double getCoste() {
        return coste;
    }

    public double getInfoEnviable() {
        return infoEnviable;
    }

    public Sensor getSensor() {
        return s;
    }

    public void agregarRecibido(int id) {
        if(recibidos.size() < 3) recibidos.add(id);
    }

    public int getNumRecibidos() {
        return recibidos.size();
    }

    public ArrayList<Integer> getRecibidos() {
        return recibidos;
    }

    @Override
    public boolean maxRecibidos(){
        return recibidos.size() == 3;
    }

    @Override
    public NSensor copyOf() {
        NSensor copia = new NSensor(this.id, this.s);
        copia.setIdDestino(this.idDestino);
        copia.infoEnviable = this.infoEnviable;
        copia.coste = this.coste;
        copia.recibidos = new ArrayList<>(this.recibidos);
        return copia;
    }
}
