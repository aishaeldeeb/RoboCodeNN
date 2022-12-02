package roboCodeTraining;

import Interfaces.LUTInterface;
import robocode.RobocodeFileOutputStream;

import java.io.*;
import java.util.Random;


public class LookUpTableV2 implements LUTInterface {

    public static double[][][][][][] Table;
    public static int[][][][][][] Visits;

    //RL
    public static int[] prevState = new int[5];
    public static int prevActionIndex;

    public static double exploreRate;
    //TODO: change every 100 rounds -0.000225x+0.9
    public static final double gamma = 0.99;
    public static final double learnRate = 0.9;


    public static final int numX = 6;
    public static final int numY = 6;
    public static final int numEnemyDis = 4;
    public static final int numHeading = 4;
    public static final int numEnemyBearing = 4;
    public static final int numStates = numX * numY * numEnemyDis * numHeading * numEnemyBearing;
//    public static final int numStates = numEnemyDis * numHeadingCos * numHeadingSin;

    public static final int numActions = 5; //left, right, up, down, fire
    //    public static int states[][][][][] = new int[numX][numY][numHeadingCos][numEnemyDis][numHeadingSin];
//    public static int states[][][] = new int[numHeadingCos][numEnemyDis][numHeadingSin];


    public LookUpTableV2(){
        //initialize table with zeros

        Table = new double[numHeading][numEnemyDis][numEnemyBearing][numX][numY][numActions];
        Visits = new int[numHeading][numEnemyDis][numEnemyBearing][numX][numY][numActions];
        //include entry for visited
        initialiseLUT();
    }

    void initEpsilon(){
        //TODO: change every 100 rounds -0.000225x+0.9

        exploreRate = 0.9;
//        exploreRate = -0.000225 * rounds + 0.9;


    }
    void updateEpsilon(){
        //TODO: change every 100 rounds -0.000225x+0.9

//        exploreRate = -0.000225 * rounds + 0.9;
        if (exploreRate > 0)
            exploreRate =- 0.1;
    }

//    public void quantizeStates(double enemyDistance, double enemyBearing, double heading, double xPos, double yPos){
//        // quantize all parameter readings first then return state index
//        int quantizedEnemyDistance = quantizeEnemyDistance(enemyDistance);
//        int quantizedEnemyBearing = quantizeEnemyBearing(enemyBearing);
//        int quantizedHeading = quantizeEnemyBearing(heading);
//        int quantizedXPos = quantizeXPos(xPos);
//        int quantizedYPos = quantizeXPos(yPos);

//        getAction();
//        int stateIndex = states[quantizedXPos][quantizedYPos][quantizedHeading][quantizedEnemyDistance][quantizedEnemyBearing];
//        int stateIndex = states[quantizedHeading][quantizedEnemyDistance][quantizedEnemyBearing];
//        return stateIndex;
//    }
    public int getAction(int heading, int enemyDistance, int enemyBearing, int xPos, int yPos){
//        int quantizedEnemyDistance = quantizeEnemyDistance(enemyDistance);
//        int quantizedEnemyBearing = quantizeEnemyBearing(enemyBearing);
//        int quantizedHeading = quantizeEnemyBearing(heading);
//        int quantizedXPos = quantizeXPos(xPos);
//        int quantizedYPos = quantizeXPos(yPos);

        //record visits
        for (int i = 0; i < numActions; i++ ){
            Visits[heading][enemyDistance][enemyBearing][xPos][yPos][i] =+ 1;
//            System.out.println("recording visits: states: " + heading + " " + enemyDistance + " " + enemyBearing);
//            System.out.println("\n action: " + i + " visits: " + Visits[heading][enemyDistance][enemyBearing][i]);
        }
//        System.out.println("Epsilon: " + exploreRate + "\n");

        double rand =  Math.random();
        if (rand < exploreRate) {

//            System.out.println("Explore policy\n");
            Random randomNum = new Random();
            int random = randomNum.nextInt( numActions);

            return random;


        }
        else {
//            System.out.println("Greedy policy\n");
            return bestAction(heading, enemyDistance, enemyBearing, xPos, yPos);
        }
    }

    public int bestAction(int heading, int enemyDistance, int enemyBearing, int xPos, int yPos){

        int best = 0;
        double Qmax = Double.NEGATIVE_INFINITY;

        //find state vector
        double[] actionColumn = Table[heading][enemyDistance][enemyBearing][xPos][yPos];

        for (int i = 0; i < numActions; i++) {


            if (actionColumn[i]> Qmax) {
                Qmax = actionColumn[i];
//                if (Qmax == Double.NEGATIVE_INFINITY || Qmax == Double.POSITIVE_INFINITY){
//                    System.out.println("infinity detected!");
//
//                }
                best = i;

            }
        }
        return best;

    }


    public int quantizeEnemyDistance(double enemyDistanceValue) {
        // 4 Partitions
        //TODO: adjust based on range?
        int quanX = (int) (enemyDistanceValue / 1000);
//        if (quanDistance > numEnemyDis - 1) {
//            quanDistance = numEnemyDis - 1;
//        }
        if (quanX >= 0 && quanX < 1) {
            quanX = 0;
        } else if (quanX >= 1 && quanX < 2) {
            quanX = 1;
        } else if (quanX >= 2 && quanX < 3) {
            quanX = 2;
        } else if (quanX > 3) {
            quanX = 3;

        }
        return quanX;
    }
    public int quantizeEnemyBearing(double enemyBearingValue){

        // 4 headings
        int quanBearing = 0;

        if (enemyBearingValue >= 0 && enemyBearingValue <= Math.PI / 2)
            quanBearing = 0;

        else if (enemyBearingValue > Math.PI / 2 && enemyBearingValue <= Math.PI )
            quanBearing = 1;

        else if (enemyBearingValue > Math.PI && enemyBearingValue <= Math.PI * (3/2))
            quanBearing = 2;

        else if (enemyBearingValue > Math.PI * (3/2))
            quanBearing = 3;

        return quanBearing;
    }
    public int quantizeHeading(double headingValue){
        // 6 partitions
        int quanHeading = 0;

        if (headingValue >= 0 && headingValue <= Math.PI / 2)
            quanHeading = 0;

        else if (headingValue > Math.PI / 4 && headingValue <= Math.PI / 2)
            quanHeading = 2;

        else if (headingValue > Math.PI / 2 && headingValue <= Math.PI * (3/4))
            quanHeading = 3;

        else if (headingValue > (3/4) * Math.PI / 3 && headingValue <= Math.PI)
            quanHeading = 4;

        else if (headingValue > Math.PI)
            quanHeading = 5;

        return quanHeading;
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
//        if (quanX > 7 && quanX <= 8)
//            quanX = 7;
//        if (quanX >= 8 && quanX <= 9)
//            quanX = 8;
//        if (quanX >= 9 && quanX <= 10)
//            quanX = 9;
        return quanX;
    }
//    public int quantizeYPos(double YPos){
//
//    }

    //pick action from LUT based on state index


    //Update lookup table
    public void updateLUT(boolean onPolicy, double rewards,  int heading, int enemyDistance,int enemyBearing, int xPos, int yPos, int actionIndex){
        double prevQValue = Table[prevState[0]][prevState[1]][prevState[2]][prevState[3]][prevState[4]][prevActionIndex];
        double QCurrent = 0.0;
//        System.out.println("privious Q: " + prevQValue + "\n");
        if (onPolicy) { //on policy
            QCurrent = prevQValue + learnRate * (rewards + gamma * (Table[heading][enemyDistance][enemyBearing][xPos][yPos][actionIndex]));
//            System.out.println("current Q: " + QCurrent + "\n");

        }

        else { //off policy
            QCurrent = prevQValue + learnRate *  (rewards + gamma * MaxQ(heading, enemyDistance, enemyBearing, xPos, yPos));

        }
        Table[heading][enemyDistance][enemyBearing][xPos][yPos][actionIndex] = QCurrent;
        prevState= new int[]{heading, enemyDistance, enemyBearing, xPos, yPos};
        prevActionIndex = actionIndex;

    }

    public double MaxQ(int  heading, int enemyDistance, int enemyBearing, int xPos, int yPos) {

        double Qmax = Double.NEGATIVE_INFINITY;


        //find state vector
        double[] actionColumn = Table[heading][enemyDistance][enemyBearing][xPos][yPos];

        for (int i = 0; i < numActions; i++) {
            if (actionColumn[i] > Qmax) {
                Qmax = actionColumn[i];
//                if (Qmax == Double.NEGATIVE_INFINITY || Qmax == Double.POSITIVE_INFINITY){
//                    System.out.println("infinity detected!");
//                }
            }
        }

//        for (int i = 0; i < numActions; i++) {
//            if (Table[stateIndex][i] > Qmax) {
//                Qmax = Table[stateIndex][i];
//            }
//        }

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
//            for (int i = 0; i < numStates; i++)
//                for (int j = 0; j < numActions; j++)
//                    write.println(Table[i][j]);

            for (int a = 0; a < numHeading; a++)
                for (int b = 0; b < numEnemyDis; b++)
                    for (int c = 0; c < numEnemyBearing;c++)
                        for (int d = 0; d < numX; d++)
                            for (int e = 0; e < numY; e++)
                                for (int f = 0; f < numActions; f++) {
//                                    write.printf("%d %d %d %d %d %d %1.12f %1.12f", a, b, c, d, e, f, Visits[a][b][c][d][e][f], Table[a][b][c][d][e][f]);
//                                    write.println(a + " " + b + " " + c + " " + d + " " + e + " " + f + " " + Visits[a][b][c][d][e][f] + " " + Table[a][b][c][d][e][f]);
                                    write.println(Visits[a][b][c][d][e][f] + " " + Table[a][b][c][d][e][f]);

//                            System.out.println("Saving file\n");
//                            System.out.printf("States: %d, %d, %d, %d\n", a, b, c, d);
//                            System.out.printf("Q: %1.1f, Visits: %d\n", Table[a][b][c][d], Visits[a][b][c][d] );

                        }
            if (write.checkError())
                System.out.println("Could not save the data!");
            write.close();
        } catch (IOException e) {

        } finally {
            try {
                if (write != null)
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
        // Initialize table to zero
//        for (int i = 0; i < numStates; i++)
//            for (int j = 0; j < numActions; j++)
//                Table[i][j] = 0.0;

        // Initialize States
//        int random = 0;
//        for (int a = 0; a < numX; a++)
//            for (int b = 0; b < numY; b++)
//                for (int c = 0; c < numHeadingCos; c++)
//                    for (int d = 0; d < numEnemyDis; d++)
//                        for (int e = 0; e < numHeadingSin; e++)
//                            states[a][b][c][d][e] = random++;

        for (int a = 0; a < numHeading; a++)
            for (int b = 0; b < numEnemyDis; b++)
                for (int c = 0; c < numEnemyBearing;c++)
                    for (int d = 0; d < numX; d++)
                        for (int e = 0; e < numY; e++)
                            for (int f = 0; f < numActions; f++)
                                Table[a][b][c][d][e][f] = 0.0;
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
        int visits = Character.MIN_VALUE;
        double Q = Character.MIN_VALUE;

        try {
            read = new BufferedReader(new FileReader(argFileName));
            for (int a = 0; a < numHeading; a++)
                for (int b = 0; b < numEnemyDis; b++)
                    for (int c = 0; c < numEnemyBearing;c++)
                        for (int d = 0; d < numX; d++)
                            for (int e = 0; e < numY; e++)
                                for (int f = 0; f < numActions; f++) {
                                    line = read.readLine();
                                    visits = (int) (line.charAt(0));
                                    Q = Double.parseDouble(String.valueOf(line.charAt(3)));

                                    Table[a][b][c][d][e][f] = Q;
                                    Visits[a][b][c][d][e][f] = visits;
                                }
//            for (int a = 0; a < numHeadingCos; a++)
//                for (int b = 0; b < numEnemyDis; b++)
//                    for (int c = 0; c < numHeadingSin;c++)
//                        for (int d = 0; d < numActions; d++) {
//                            line = read.readLine();
//                            visits =(int) ( line.charAt(8));
//                            Q = Double.parseDouble(String.valueOf(line.charAt(10)));
//
//                            Table[a][b][c][d] = Q;
//                            Visits[a][b][c][d] = visits;
//
////                            System.out.println("\n Reading file \n");
////                            System.out.printf("States: %d, %d, %d, %d \n", a, b, c, d);
////                            System.out.printf("Q: %1.1f, Visits: %d \n", Table[a][b][c][d], Visits[a][b][c][d] );
//
//                        }
//            for (int i = 0; i < numStates; i++)
//                for (int j = 0; j < numActions; j++)
//                    Table[i][j] = Double.parseDouble(read.readLine());
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
}
