package IA.RedUPC;

import aima.search.framework.SuccessorFunction;
import aima.search.framework.Successor;
import java.util.ArrayList;
import java.util.List;

public class RedSuccessorFunction implements SuccessorFunction {

    public List getSuccessors(Object state){
        ArrayList<Successor> retval = new ArrayList<>();
        RedBoard board = (RedBoard) state;
        String lastAction;
        double heuristico = board.heuristic();

        if(board.S) {
            for (int i = 0; i < board.nodos.size(); ++i){
                if (board.nodos.get(i) instanceof NSensor) {
                    for (int j = i + 1; j < board.nodos.size(); ++j) {
                        if (board.nodos.get(j) instanceof NSensor) {
                            if (!hihacicle2(i, j, board.nodos) && !hihacicle2(j, i, board.nodos)) {
                                RedBoard newBoard = board.copyOf();
                                newBoard.swap(i, j);
                                lastAction = "swap(" + i + ", " + j + ")";
                                newBoard.actualizaCostesParcial(i, j);
                                retval.add(new Successor(lastAction, newBoard));
                            }
                        }
                    }
                }
            }
        }
        if(board.C) {
            for (int i = 0; i < board.nodos.size(); ++i){
                if (board.nodos.get(i) instanceof NSensor) {
                    int z = ((NSensor) board.nodos.get(i)).getIdDestino();
                    for (int j = 0; j < board.nodos.size(); ++j) {
                        if (i != j && !board.nodos.get(j).maxRecibidos()) {
                            if (!hihacicle2(j, i, board.nodos)) {
                                RedBoard newBoard = board.copyOf();
                                newBoard.conecta(i, j);
                                lastAction = "conecta(" + i + ", " + j + ")";
                                newBoard.actualizaCostesParcialConecta(i, j, z);
                                retval.add(new Successor(lastAction, newBoard));
                            }
                        }
                    }
                }
            }
        }
        if(board.A) {
            for (int i = 0; i < board.nodos.size(); ++i){
                if (board.nodos.get(i) instanceof NSensor) {
                    int z = ((NSensor) board.nodos.get(i)).getIdDestino();
                    int conexionesEntrantesI = ((NSensor) board.nodos.get(i)).getNumRecibidos();
                    if (conexionesEntrantesI == 0) continue;
                    for (int j = 0; j < board.nodos.size(); ++j){
                        if (i != j){
                            int conexionesEntrantesJ;
                            int max = 3;
                            if (board.nodos.get(j) instanceof NSensor){
                                conexionesEntrantesJ = ((NSensor) board.nodos.get(j)).getNumRecibidos();
                            } else{
                                conexionesEntrantesJ = ((NCentro) board.nodos.get(j)).getNumRecibidos();
                                max = 25;
                            }
                            if (conexionesEntrantesI + conexionesEntrantesJ <= max){
                                RedBoard newBoard = board.copyOf();
                                ArrayList<Integer> recibidos = newBoard.nodos.get(i).getRecibidos();
                                newBoard.alibera(i, j);
                                boolean ciclo = false;
                                for (Integer k : ((Nodo) board.nodos.get(i)).getRecibidos()){
                                    if (board.formariaCiclo(k, j, board.nodos)){
                                        ciclo = true;
                                        break;
                                    }
                                }
                                if (!ciclo){
                                    newBoard.actualizaCostesParcialAlibera(i,j, recibidos);
                                    lastAction = "liberar(" + i + ", " + j + ")";
                                    retval.add(new Successor(lastAction, newBoard));
                                }
                            }
                        }
                    }

                }
            }
        }

        return (retval);

    }

    private boolean hihacicle2(int id, int desti, ArrayList<Nodo> nodos){
        if (id == desti) return true;
        while (nodos.get(id) instanceof NSensor){
            if (((NSensor) nodos.get(id)).getIdDestino() == desti) return true;
            id = ((NSensor) nodos.get(id)).getIdDestino();
        }
        return false;
    }

    private boolean hihacicle(int id, ArrayList<Nodo> nodos) {
        boolean[] visitados = new boolean[nodos.size()];
        return hihacicle(id, nodos, visitados);
    }

    private boolean hihacicle(int id, ArrayList<Nodo> nodos, boolean[] visitados) {
        if (visitados[id]) return true;
        visitados[id] = true;
        if (nodos.get(id) instanceof NSensor) {
            return hihacicle(((NSensor) nodos.get(id)).getIdDestino(), nodos, visitados);
        }
        return false;
    }
}
