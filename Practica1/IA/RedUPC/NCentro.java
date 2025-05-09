package IA.RedUPC;

import IA.Red.Centro;

import java.util.ArrayList;

public class NCentro extends Nodo {
    Centro c;
    double costeRecibido = 0;
    double informacionRecibida = 0;
    public NCentro(int idIni, Centro cIni) {
        super(idIni);
        c = cIni;
    }
    public void agregarRecibido(int id) {
        if(recibidos.size() < 25) recibidos.add(id);
    }

    public int getNumRecibidos() {
        return recibidos.size();
    }

    public Centro getCentro() {
        return c;
    }

    public double getCoste() {
        return costeRecibido;
    }

    @Override
    public boolean maxRecibidos(){
        return recibidos.size() == 25;
    }

    @Override
    public NCentro copyOf() {
        NCentro copia = new NCentro(this.id, this.c);
        copia.informacionRecibida = this.informacionRecibida;
        copia.costeRecibido = this.costeRecibido;
        copia.recibidos = new ArrayList<>(this.recibidos);
        return copia;
    }
}
