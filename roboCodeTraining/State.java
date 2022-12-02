package roboCodeTraining;

public class State {

    public double myEnergy;
    public double distance2Enemy;
    public double enemyEnergy;
    public double distance2Center;

    public State(double myEnergy, double distance2Enemy, double enemyEnergy, double distance2Center ){
        this.myEnergy = myEnergy;
        this.distance2Enemy = distance2Enemy;
        this.enemyEnergy = enemyEnergy;
        this.distance2Center = distance2Center;
    }


}
