import IA.RedUPC.*;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int numSens;
        int numCentres;
        String metodo;
        Random random = new Random();
        Random random2 = new Random();
        int seed1 = 1234;
        int seed2 = 4321;
        boolean S = true;
        boolean C = true;
        boolean A = true;
        String aux;
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Num Sensores: ");
        numSens = scanner.nextInt();
        System.out.println("Num Centros: ");
        numCentres = scanner.nextInt();
        System.out.println("Metodo Estado Inicial: (random, proximidad, greedy)");
        metodo = scanner.next();
        System.out.println("¿Que operadores quieres usar?");
        System.out.println("¿Usar Swap? (Y/n)");
        aux = scanner.next();
        if (aux.equals("n") || aux.equals("no") || aux.equals("N") || aux.equals("NO") || aux.equals("No")) {
            S = false;
        }
        System.out.println("¿Usar Conecta? (Y/n)");
        aux = scanner.next();
        if (aux.equals("n") || aux.equals("no") || aux.equals("N") || aux.equals("NO") || aux.equals("No")) {
            C = false;
        }
        System.out.println("¿Usar Alibera? (Y/n)");
        aux = scanner.next();
        if (aux.equals("n") || aux.equals("no") || aux.equals("N") || aux.equals("NO") || aux.equals("No")) {
            A = false;
        }

        System.out.println("¿Quieres usar semillas? (Y/n)");
        aux = scanner.next();
        if (aux.equals("Y") || aux.equals("y") || aux.equals("yes") || aux.equals("Yes") || aux.equals("YES")) {
            System.out.println("Semilla 1: ");
            seed1 = scanner.nextInt();
            System.out.println("Semilla 2: ");
            seed2 = scanner.nextInt();
        } else {
            seed1 = random.nextInt();
            seed2 = random2.nextInt();
        }

        Boolean visualizer = false;
        System.out.println("¿Quieres visualizar la red? (Y/n)");
        aux = scanner.next();
        if (aux.equals("Y") || aux.equals("y") || aux.equals("yes") || aux.equals("Yes") || aux.equals("YES")) {
            visualizer = true;
        }

        System.out.println("Que algoritmo quieres usar HillClimbing o SimulatedAnnealing? (HC/SA)");
        String aux2 = scanner.next();
        if (aux2.equals("HC") || aux2.equals("HillClimbing") || aux2.equals("Hill Climbing")) {
            aux2 = "HC";
        } else {
            aux2 = "SA";
        }
        
        long startTime = System.currentTimeMillis(); 

        RedBoard board = new RedBoard(numSens, numCentres, metodo, seed1, seed2, S, C, A);
        if (visualizer) {
            RedVisualizer.visualizarRedConControles(board.getNodos());
        }
        System.out.println("Coste inicial: " + board.getCosteTotal() + " Info inicial: " + board.getInfoTotal() + " Info max: " + (board.getInfoMax()-1));
        Problem p;
        Search search;
        
        if (aux2.equals("HC")) {
            p = new Problem(board, new RedSuccessorFunction(), new RedGoalTest(), new RedHeuristicFunction());
            search =  new HillClimbingSearch();

        } else {
            p = new Problem(board, new RedSuccessorFunctionSA(), new RedGoalTest(), new RedHeuristicFunction());
            search = new SimulatedAnnealingSearch();;
        }
        SearchAgent agent = new SearchAgent(p,search);
        if (aux2.equals("HC")) {
            printActions(agent.getActions());
        }
        printInstrumentation(agent.getInstrumentation());
        if (visualizer) {
            RedVisualizer.visualizarRedConControles(board.getNodos());
        }
        System.out.println("Coste final: " + ((RedBoard) search.getGoalState()).getCosteTotal() + " Info final: " + ((RedBoard) search.getGoalState()).getInfoTotal() + " Info max: " + (((RedBoard) search.getGoalState()).getInfoMax()-1));

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Temps d'execució: " + totalTime + "ms");

    }

    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }

    }

    private static void printActions(List actions) {
        for (int i = 0; i < actions.size(); i++) {
            String action = (String) actions.get(i);
            System.out.println(action);
        }
    }

}
