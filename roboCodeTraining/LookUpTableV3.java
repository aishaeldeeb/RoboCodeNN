package roboCodeTraining;

import Interfaces.LUTInterface;
import robocode.RobocodeFileOutputStream;

import java.io.*;
import java.util.Random;


public class LookUpTableV3 implements LUTInterface {

    public static double[][][][][][][][] Table;
    public static int[][][][][][][][] Visits;

    //RL
    public static int[] prevState = new int[7];
    public static int prevActionIndex;

    public static double exploreRate;
    //TODO: change every 100 rounds -0.000225x+0.9
    public static final double gamma = 0.99;
    public static final double learnRate = 0.9;


    public static final int numX = 6;
    public static final int numY = 6;
    public static final int numEnemyDis = 4; //TODO: change naming to num
    public static final int numHeadingCos = 4;
    public static final int numHeadingSin = 4;
    public static final int numBearingCos = 4;
    public static final int numBearingSin = 4;
    public static final int numActions = 5;



    public LookUpTableV3(){
        //initialize table with zeros

        Table = new double[numHeadingCos][numEnemyDis][numHeadingSin][numX][numY][numBearingCos][numBearingSin][numActions];
        Visits = new int[numHeadingCos][numEnemyDis][numHeadingSin][numX][numY][numBearingCos][numBearingSin][numActions];
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

    public int getAction(int headingCos, int enemyDistance, int headingSin, int xPos, int yPos,  int enemyBearingCos, int enemyBearingSin){

        //record visits
        for (int i = 0; i < numActions; i++ ){
            Visits[headingCos][enemyDistance][headingSin][xPos][yPos][enemyBearingCos][enemyBearingSin][i] =+ 1;
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
            return bestAction(headingCos, enemyDistance, headingSin,  xPos, yPos, enemyBearingCos, enemyBearingSin);
        }
    }

    public int bestAction(int headingCos, int enemyDistance, int headingSin, int xPos, int yPos, int enemyBearingCos, int enemyBearingSin){

        int best = 0;
        double Qmax = Double.NEGATIVE_INFINITY;

        //find state vector
        double[] actionColumn = Table[headingCos][enemyDistance][headingSin][xPos][yPos][enemyBearingCos][enemyBearingSin];

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
        int quanX = (int) (enemyDistanceValue / 100);
//        if (quanDistance > numEnemyDis - 1) {
//            quanDistance = numEnemyDis - 1;
//        }
        if (quanX >= 0 && quanX <= 1) {
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

    public void updateLUT(boolean onPolicy, double rewards,  int headingCos, int enemyDistance,int headingSin, int xPos, int yPos, int enemyBearingCos, int enemyBearingSin, int actionIndex){
        double prevQValue = Table[prevState[0]][prevState[1]][prevState[2]][prevState[3]][prevState[4]][prevState[5]][prevState[6]][prevActionIndex];
        double QCurrent = 0.0;
//        System.out.println("privious Q: " + prevQValue + "\n");
        if (onPolicy) { //on policy
            QCurrent = prevQValue + learnRate * (rewards + gamma * (Table[headingCos][enemyDistance][headingSin][xPos][yPos][enemyBearingCos][enemyBearingSin][actionIndex]));
//            System.out.println("current Q: " + QCurrent + "\n");

        }

        else { //off policy
            QCurrent = prevQValue + learnRate *  (rewards + gamma * MaxQ(headingCos, enemyDistance, headingSin, xPos, yPos, enemyBearingCos, enemyBearingSin));

        }
        Table[headingCos][enemyDistance][headingSin][xPos][yPos][enemyBearingCos][enemyBearingSin][actionIndex] = QCurrent;
        prevState= new int[]{headingCos, enemyDistance, headingSin, xPos, yPos, enemyBearingCos, enemyBearingSin};
        prevActionIndex = actionIndex;

    }

    public double MaxQ(int  headingCos, int enemyDistance, int headingSin, int xPos, int yPos, int enemyBearingCos, int enemyBearingSin) {

        double Qmax = Double.NEGATIVE_INFINITY;


        //find state vector
        double[] actionColumn = Table[headingCos][enemyDistance][headingSin][xPos][yPos][enemyBearingCos][enemyBearingSin];

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


            for (int a = 0; a < numHeadingCos; a++)
                for (int b = 0; b < numEnemyDis; b++)
                    for (int c = 0; c < numHeadingSin; c++)
                        for (int d = 0; d < numX; d++)
                            for (int e = 0; e < numY; e++)
                                for (int g = 0; g < numBearingCos; g++)
                                    for (int h = 0; h < numBearingSin; h++)
                                        for (int f = 0; f < numActions; f++){
//                                    write.printf("%d %d %d %d %d %d %1.12f %1.12f", a, b, c, d, e, f, Visits[a][b][c][d][e][f], Table[a][b][c][d][e][f]);
//                                    write.println(a + " " + b + " " + c + " " + d + " " + e + " " + f + " " + Visits[a][b][c][d][e][f] + " " + Table[a][b][c][d][e][f]);
                                            write.println(Visits[a][b][c][d][e][g][h][f] + " " + Table[a][b][c][d][e][g][h][f]);


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

        for (int a = 0; a < numHeadingCos; a++)
            for (int b = 0; b < numEnemyDis; b++)
                for (int c = 0; c < numHeadingSin; c++)
                    for (int d = 0; d < numX; d++)
                        for (int e = 0; e < numY; e++)
                            for (int g = 0; g < numBearingCos; g++)
                                for (int h = 0; h < numBearingSin; h++)
                                    for (int f = 0; f < numActions; f++)
                                        Table[a][b][c][d][e][g][h][f] = 0.0;
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
            for (int a = 0; a < numHeadingCos; a++)
                for (int b = 0; b < numEnemyDis; b++)
                    for (int c = 0; c < numHeadingSin; c++)
                        for (int d = 0; d < numX; d++)
                            for (int e = 0; e < numY; e++)
                                for (int g = 0; g < numBearingCos; g++)
                                    for (int h = 0; h < numBearingSin; h++)
                                        for (int f = 0; f < numActions; f++) {
                                            line = read.readLine();
                                            visits = (int) (line.charAt(0));
                                            Q = Double.parseDouble(String.valueOf(line.charAt(3)));

                                            Table[a][b][c][d][e][g][h][f] = Q;
                                            Visits[a][b][c][d][e][g][h][f] = visits;
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
}
