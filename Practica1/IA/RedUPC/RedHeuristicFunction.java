package IA.RedUPC;

import aima.search.framework.HeuristicFunction;

public class RedHeuristicFunction implements HeuristicFunction {
    public double getHeuristicValue(Object n){

        return ((RedBoard) n).heuristic();
    }
}
