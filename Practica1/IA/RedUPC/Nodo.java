package IA.RedUPC;

import java.util.ArrayList;

public abstract class Nodo {
    int id;
    ArrayList<Integer> recibidos=new ArrayList<Integer>();
    Nodo(int idIni) {
        id = idIni;
    }

    public ArrayList<Integer> getRecibidos() {
        return recibidos;
    }

    public void agregarRecibido(int id){}

    public void eliminaRecibido(int id) {
        for(int i = 0; i < this.recibidos.size(); ++i){
            if(this.recibidos.get(i) == id){
                this.recibidos.remove(i);
                break;
            }
        }
    }

    public Nodo copyOf(){
        return null;
    }

    public abstract boolean maxRecibidos();
}

