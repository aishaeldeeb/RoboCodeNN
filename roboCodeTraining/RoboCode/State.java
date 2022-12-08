package roboCodeTraining.RoboCode;

public class State {

    public double enemyDistance;
    public double xPos;
    public double yPos;
    public double myEnergy;
    public double enemyEnergy;

    public State(double enemyDistance, double xPos, double yPos, double myEnergy, double enemyEnergy){
        this.enemyDistance = enemyDistance;
        this.xPos = xPos;
        this.yPos = yPos;
        this.myEnergy = myEnergy;
        this.enemyEnergy = enemyEnergy;
    }

    public double[] getStateVector(){
        return new double[]{enemyDistance, xPos, yPos, myEnergy, enemyEnergy};
    }
}
