package roboCodeTraining;

import java.util.Random;

public class Learning {
    public static final double learningRate = 0.4;
    public static  double discountRate = 0.0;
    public static double explorationRate = 0.9;
    private int previousState;
    private int previousAction;
    public LUT LUTable;

    public Learning(LUT LUTable) {
        this.LUTable = LUTable;
    }

    public void LUTlearning(int currentState, int currentAction, double reward, boolean offPolicy) {
        double previousQ = LUTable.getQValue(previousState, previousAction);
        if (offPolicy) {
            double currentQ = (1 - learningRate) * previousQ
                    + learningRate * (reward + discountRate * LUTable.getMaxQ(currentState));
            LUTable.setQValue(previousState, previousAction, currentQ);
        } else { // onPolicy
            double currentQ = (1 - learningRate) * previousQ
                    + learningRate * (reward + discountRate * LUTable.getQValue(currentState, currentAction));
            LUTable.setQValue(previousState, previousAction, currentQ);
        }
        previousState = currentState;
        previousAction = currentAction;
    }

    public int selectAction(int state) {
        double random = Math.random();
        if (explorationRate > random) {
            Random ran = new Random();
            return ran.nextInt(((LUT.actionsNum - 1 - 0) + 1)); //generate a random number between 0 and 10?
        } else { // Pure greedy
            return LUTable.getBestAction(state);
        }
    }
}