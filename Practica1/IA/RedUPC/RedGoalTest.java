package IA.RedUPC;

import aima.search.framework.GoalTest;

public class RedGoalTest implements GoalTest {
    public boolean isGoalState(Object state){

        return((RedBoard) state).is_goal();
    }
}
