package roboCodeTraining;

import Interfaces.LUTInterface;
import robocode.RobocodeFileOutputStream;
import sun.applet.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;



public class LookUpTableV4 implements LUTInterface {

    public static double[][][][][][] Table;
    public static int[][][][][][] Visits;
    public static int rounds = 15000;

    //RL
    public static int[] prevState = new int[7];
    public static int prevActionIndex;

    public static double exploreRate;

    public static final double gamma = 0.99;
    public static final double learnRate = 0.9;


    public static final int numX = 6;
    public static final int numY = 6;
    public static final int numEnemyDis = 4;
//    public static final int numHeadingCos = 4;
//    public static final int numHeadingSin = 4;
//    public static final int numBearingCos = 4;
//    public static final int numBearingSin = 4;
    public static final int numEnergy = 4;

    public static final int numActions = 5;



    public LookUpTableV4(){
//        Table = new double[numHeadingCos][numEnemyDis][numHeadingSin][numX][numY][numActions];
//        Visits = new int[numHeadingCos][numEnemyDis][numHeadingSin][numX][numY][numActions];

        this.Table = new double[numEnemyDis][numX][numY][numEnergy][numEnergy][numActions];
        this.Visits = new int[numEnemyDis][numX][numY][numEnergy][numEnergy][numActions];


//        if (MainRobotV4.totalRounds == 0) {
//            initialiseLUT();
//        }

//        initialiseLUT();

    }

    void initEpsilon(int totalRounds){
        //TODO: change every 100 rounds -0.000225x+0.9

        exploreRate = (-0.9/rounds) * totalRounds + 0.9;
//        exploreRate = -0.000225 * rounds + 0.9;


    }


    public int getAction(double epsilon, int enemyDistance, int xPos, int yPos,  int myEnergy, int enemyEnergy){

        //record visits
        for (int i = 0; i < numActions; i++ ){
            Visits[enemyDistance][xPos][yPos][myEnergy][enemyEnergy][i] += 1;
//            System.out.println("recording visits: states: " + heading + " " + enemyDistance + " " + enemyBearing);
//            System.out.println("\n action: " + i + " visits: " + Visits[heading][enemyDistance][enemyBearing][i]);
        }
//        System.out.println("Epsilon: " + exploreRate + "\n");

        double rand =  Math.random();
        if (rand < epsilon) {

//            System.out.println("Explore policy\n");
            Random randomNum = new Random();
            int random = randomNum.nextInt( numActions);

            return random;


        }
        else {
//            System.out.println("Greedy policy\n");
            return bestAction(enemyDistance, xPos, yPos, myEnergy,enemyEnergy);
        }
    }

    public int bestAction(int enemyDistance, int xPos, int yPos, int myEnergy, int enemyEnergy){

        int best = 0;
        double Qmax = Double.NEGATIVE_INFINITY;

        //find state vector
        double[] actionColumn = Table[enemyDistance][xPos][yPos][myEnergy][enemyEnergy];

        for (int i = 0; i < numActions; i++) {


            if (actionColumn[i]> Qmax) {
                Qmax = actionColumn[i];


                best = i;

            }
        }
        return best;

    }


    public int quantizeEnemyDistance(double enemyDistanceValue) {
        // 4 Partitions
        int quanX = (int) (enemyDistanceValue / 10);
//        if (quanDistance > numEnemyDis - 1) {
//            quanDistance = numEnemyDis - 1;
//        }
        if (quanX >= 0 && quanX <= 3) {
            quanX = 0;
        } else if (quanX >= 3 && quanX < 5) {
            quanX = 1;
        } else if (quanX >= 5 && quanX < 10) {
            quanX = 2;
        } else if (quanX >= 10) {
            quanX = 3;

        }
        return quanX;
    }

    public int quantizeEnergy(double energy){

        int quantEnergy = (int) (energy / 10);
//        if (quanDistance > numEnemyDis - 1) {
//            quanDistance = numEnemyDis - 1;
//        }
        if (quantEnergy >= 0 && quantEnergy <= 3) {
            quantEnergy = 0;
        } else if (quantEnergy >= 3 && quantEnergy < 5) {
            quantEnergy = 1;
        } else if (quantEnergy >= 5 && quantEnergy < 10) {
            quantEnergy = 2;
        } else if (quantEnergy >= 10) {
            quantEnergy = 3;

        }
        return quantEnergy;

    }

    public int quantizeHeading(double headingValue){
        // 4 partitions
        int quanBearing = 0;

        if (headingValue >= -1 && headingValue <= -0.5)
            quanBearing = 0;

        else if (headingValue >= -0.5 && headingValue < 0 )
            quanBearing = 1;

        else if (headingValue >= 0 && headingValue < 0.5)
            quanBearing = 2;

        else if (headingValue > 0.5 && headingValue <= 1)
            quanBearing = 3;

        return quanBearing;
    }
    public int quantizeXPos(double XPos){
        //6 partitions
        int quanX = (int) (XPos / 100);
        if (quanX >= 0 && quanX < 1) {
            quanX = 0;
        }
        else if (quanX >= 1 && quanX < 2) {
            quanX = 1;
        }
        else if (quanX >= 2 && quanX < 3) {
            quanX = 2;
        }
        else if (quanX >= 3 && quanX < 4) {
            quanX = 3;
        }
        else if (quanX >= 4 && quanX < 5) {
            quanX = 4;
        }
        else if (quanX >= 5) {
            quanX = 5;
        }

        return quanX;
    }

    public void updateLUT(boolean onPolicy, double rewards, int enemyDistance, int xPos, int yPos, int myEnergy, int enemyEnergy, int actionIndex){
        double prevQValue = Table[prevState[0]][prevState[1]][prevState[2]][prevState[3]][prevState[4]][prevActionIndex];
        double QCurrent = 0.0;
//        System.out.println("privious Q: " + prevQValue + "\n");
        if (onPolicy) { //on policy
            double Q = (Table[enemyDistance][xPos][yPos][myEnergy][enemyEnergy][actionIndex]);
            QCurrent = prevQValue + (learnRate * (rewards + (gamma * Q)) - prevQValue);
//            System.out.println("current Q: " + QCurrent + "\n");

        }

        else { //off policy
            double max = MaxQ(enemyDistance, xPos, yPos, myEnergy, enemyEnergy);
            double max2 = MaxQ(enemyDistance, xPos, yPos, myEnergy,enemyEnergy);
            QCurrent = prevQValue + (learnRate *  (rewards + (gamma * max)) - prevQValue);


        }
//        Table[enemyDistance][xPos][yPos][myEnergy][enemyEnergy][actionIndex] = QCurrent;
        Table[prevState[0]][prevState[1]][prevState[2]][prevState[3]][prevState[4]][prevActionIndex] = QCurrent;
        prevState= new int[]{enemyDistance, xPos, yPos, myEnergy, enemyEnergy};
        prevActionIndex = actionIndex;
        double prevQ = Table[enemyDistance][xPos][yPos][myEnergy][enemyEnergy][actionIndex];
        int x =0;

    }

    public void updateState( int enemyDistance, int xPos, int yPos, int myEnergy, int enemyEnergy, int actionIndex){
        prevState= new int[]{enemyDistance, xPos, yPos, myEnergy, enemyEnergy};
        prevActionIndex = actionIndex;

    }


    public double MaxQ(int enemyDistance, int xPos, int yPos, int myEnergy, int enemyEnergy) {

        double Qmax = Double.NEGATIVE_INFINITY;


        //find state vector
        double[] actionColumn = Table[enemyDistance][xPos][yPos][myEnergy][enemyEnergy];

        for (int i = 0; i < numActions; i++) {
            if (actionColumn[i] > Qmax) {
                Qmax = actionColumn[i];
            }
        }


        return Qmax;
    }

    /**
     * @param X The input vector. An array of doubles.
     * @return The value returned by th LUT or NN for this input vector
     */
    @Override
    public double outputFor(double[] X) {
        return 0;
    }

    /**
     * This method will tell the NN or the LUT the output
     * value that should be mapped to the given input vector. I.e.
     * the desired correct output value for an input.
     *
     * @param X        The input vector
     * @param argValue The new value to learn
     * @return The error in the output for that input vector
     */
    @Override
    public double train(double[] X, double argValue) {
        return 0;
    }

    /**
     * A method to write either a LUT or weights of aneural net to a file.
     *
     * @param argFile of type File.
     */
    @Override
    public void save(File argFile) {
        PrintStream write = null;
        try {
            write = new PrintStream(new RobocodeFileOutputStream(argFile));



            for (int b = 0; b < numEnemyDis; b++)
                    for (int d = 0; d < numX; d++)
                        for (int e = 0; e < numY; e++)
                            for (int c = 0; c < numEnergy; c++)
                                for (int g = 0; g < numEnergy; g++)
                                    for (int f = 0; f < numActions; f++){
                                        write.println(Visits[b][d][e][c][g][f] + " " + Table[b][d][e][c][g][f]);
    //                                         write.printf("%d %d %d %d %d %d %1.12f %1.12f", a, b, c, d, e, f, Visits[a][b][c][d][e][f], Table[a][b][c][d][e][f]);
        //                                    write.println(a + " " + b + " " + c + " " + d + " " + e + " " + f + " " + Visits[a][b][c][d][e][f] + " " + Table[a][b][c][d][e][f]);


                                }
            if (write.checkError())
                System.out.println("Could not save the data!");
            write.close();
        } catch (IOException e) {

        } finally {
            try {
                if (write != null)
                    write.flush();
                    write.close();
            } catch (Exception e) {

            }
        }



    }

    /**
     * Loads the LUT or neural net weights from file. The load must of course
     * have knowledge of how the data was written out by the save method.
     * You should raise an error in the case that an attempt is being
     * made to load data into an LUT or neural net whose structure does not match
     * the data in the file. (e.g. wrong number of hidden neurons).
     *
     * @param argFileName
     * @throws IOException
     */
    @Override
    public void load(String argFileName) throws IOException {

    }

    /**
     * Initialise the lookup table to all zeros.
     */


    @Override
    public void initialiseLUT() {

        for (int b = 0; b < numEnemyDis; b++)
            for (int d = 0; d < numX; d++)
                for (int e = 0; e < numY; e++)
                    for (int c = 0; c < numY; c++)
                        for (int g = 0; g < numY; g++)
                            for (int f = 0; f < numActions; f++)
                                Table[b][d][e][c][g][f] = 0.0;
    }

    /**
     * A helper method that translates a vector being used to index the look up table
     * into an ordinal that can then be used to access the associated look up table element.
     *
     * @param X The state action vector used to index the LUT
     * @return The index where this vector maps to
     */
    @Override
    public int indexFor(double[] X) {
        return 0;
    }

    public void load(File argFileName) {

        BufferedReader read = null;
        String line ="";
        char visitChar;
        char QChar;
        int visits = Character.MIN_VALUE;
        double Q = Character.MIN_VALUE;

        try {
            read = new BufferedReader(new FileReader(argFileName));
            for (int b = 0; b < numEnemyDis; b++)
                for (int d = 0; d < numX; d++)
                    for (int e = 0; e < numY; e++)
                        for (int c = 0; c < numEnergy; c++)
                            for (int g = 0; g < numEnergy; g++)
                                for (int f = 0; f < numActions; f++){

                                    line = read.readLine();
                                    visitChar = line.charAt(0);
                                    visitChar = line.charAt(0);


                                    String[] words = line.split(" ");
//                                    visits = Character.getNumericValue(line.charAt(0));
                                    visits = Integer.valueOf(words[0]);
//                                    Q = Character.getNumericValue(line.charAt(2));

                                    Q = Double.valueOf(words[1]);
                                    Table[b][d][e][c][g][f] = Q;
                                    Visits[b][d][e][c][g][f]= visits;
                                }
        }
        catch (IOException e) {
            initialiseLUT();
        }
        catch (NumberFormatException e) {
            initialiseLUT();
        }
        finally {
            try {
                if (read != null)
                    read.close();
            } catch (IOException e) {

            }
        }
    }




    public double[][] getQStatesActions(String argFileName) {

        BufferedReader read = null;
        String line ="";
        double QValue;
        int counter = 0;

        int numInputsQ = 11;

        double[] actionOnHot;

        double[][] stateAction = new double [numEnergy*numEnergy*numEnemyDis*numX*numY*numActions][numInputsQ];

        double[] actionStateArray = new double[numInputsQ];


        try {
            read = new BufferedReader(new FileReader(argFileName));
            for (int b = 0; b < numEnemyDis; b++)
                for (int d = 0; d < numX; d++)
                    for (int e = 0; e < numY; e++)
                        for (int c = 0; c < numEnergy; c++)
                            for (int g = 0; g < numEnergy; g++)
                                for (int f = 0; f < numActions; f++){
                                    line = read.readLine();
                                    String[] words = line.split(" ");
                                    QValue = Double.valueOf(words[1]);

                                    actionStateArray[0] = preprocess(b);
                                    actionStateArray[1] = preprocess(d);
                                    actionStateArray[2] = preprocess(e);
                                    actionStateArray[3] = preprocess(c);
                                    actionStateArray[4] = preprocess(g);
                                    actionOnHot = OnHotEncode(f);

                                    for (int i = 0; i < actionOnHot.length; i++){
                                        actionStateArray[i+5] = actionOnHot[i];
                                    }

                                    actionStateArray[10] = QValue/10;

                                    for (int i = 0; i < actionStateArray.length; i++){
                                        stateAction[counter][i] = actionStateArray[i];
                                    }

                                    counter++;
                                }
        }
        catch (IOException e) {
            initialiseLUT();
        }
        catch (NumberFormatException e) {
            initialiseLUT();
        }
        finally {
            try {
                if (read != null)
                    read.close();
            } catch (IOException e) {

            }
        }
        return stateAction;
    }

    public double preprocess(int i){
        return ((double) (i+1.0))/10;
    }

    public double[] OnHotEncode(int i){
        switch(i){
            case 0:
                return new double[]{-1, -1, -1, -1, 1};
            case 1:
                return new double[]{-1, -1, -1, 1, -1};
            case 2:
                return new double[]{-1, -1, 1, -1, -1};
            case 3:
                return new double[]{-1, 1, -1, -1, -1};
            case 4:
                return new double[]{1, -1, -1, -1, -1};
            default:
                return new double[]{-1, -1, -1, -1, -1};
        }


    }

}
