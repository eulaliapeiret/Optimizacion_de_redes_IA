package IA.RedUPC;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.*;

public class RedSuccessorFunctionSA implements SuccessorFunction {

    public List getSuccessors(Object state) {
        ArrayList<Successor> retval = new ArrayList<Successor>();
        RedBoard board = (RedBoard) state;

        String lastAction = "nada hecho";
        while (lastAction.equals("nada hecho")) {
            Random r = new Random();
            int randomNum = r.nextInt(2);

            if (randomNum == 0 && board.S) {
                int i = r.nextInt(board.nodos.size());
                int j = r.nextInt(board.nodos.size());

                if (board.nodos.get(i) instanceof NSensor && board.nodos.get(j) instanceof NSensor && i != ((NSensor) board.nodos.get(j)).getIdDestino() && j != ((NSensor) board.nodos.get(i)).getIdDestino()){
                    RedBoard newBoard = board.copyOf();
                    newBoard.swap(i, j);
                    if (!hihacicle(i, newBoard.nodos) && !hihacicle(j, newBoard.nodos)) {
                        newBoard.actualizaCostesParcial(i, j);
                        lastAction = "swap(" + i + ", " + j + ")";
                        retval.add(new Successor(lastAction, newBoard));
                    }
                }
            } else if (randomNum == 1 && board.C) {

                int i = r.nextInt(board.nodos.size());
                int j = r.nextInt(board.nodos.size());

                if (i != j && board.nodos.get(i) instanceof NSensor && !board.nodos.get(j).maxRecibidos() && ((NSensor) board.nodos.get(i)).getIdDestino() != j) {
                    RedBoard newBoard = board.copyOf();
                    int z = ((NSensor) board.nodos.get(i)).getIdDestino();
                    newBoard.conecta(i, j);
                    boolean ciclo = false;
                    for (int k : newBoard.nodos.get(j).getRecibidos()) {
                        if (hihacicle(k, newBoard.nodos)) {
                            ciclo = true;
                            break;
                        }
                    }
                    if (!ciclo) {
                        lastAction = "conecta(" + i + ", " + j + ")";
                        newBoard.actualizaCostesParcialConecta(i, j, z);
                        retval.add(new Successor(lastAction, newBoard));
                    }
                }
            } else if (randomNum == 2 && board.A) {

                int i = r.nextInt(board.nodos.size());
                int j = r.nextInt(board.nodos.size());

                if (i != j && board.nodos.get(i) instanceof NSensor) {
                    int conexionesEntrantesI = ((NSensor) board.nodos.get(i)).getNumRecibidos();
                    int conexionesEntrantesJ;
                    int max = 3;
                    if (board.nodos.get(j) instanceof NSensor) {
                        conexionesEntrantesJ = ((NSensor) board.nodos.get(j)).getNumRecibidos();
                    } else {
                        conexionesEntrantesJ = ((NCentro) board.nodos.get(j)).getNumRecibidos();
                        max = 25;
                    }

                    if (conexionesEntrantesI + conexionesEntrantesJ <= max) {
                        RedBoard newBoard = board.copyOf();
                        ArrayList<Integer> recibidos = new ArrayList<>();
                        recibidos = newBoard.nodos.get(i).getRecibidos();
                        newBoard.alibera(i, j);
                        boolean ciclo = false;
                        for (Integer k : ((Nodo) board.nodos.get(i)).getRecibidos()) {
                            if (board.formariaCiclo(k, j, board.nodos)) {
                                ciclo = true;
                                //break;
                            }
                        }
                        if (!hihacicle(i, newBoard.nodos) && !ciclo) {
                            newBoard.actualizaCostesParcialAlibera(i, j, recibidos);
                            lastAction = "liberar(" + i + ", " + j + ")";
                            retval.add(new Successor(lastAction, newBoard));
                        }
                    }
                }
            }
        }
        return (retval);
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



