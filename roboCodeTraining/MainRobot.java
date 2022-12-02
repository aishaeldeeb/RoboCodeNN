package roboCodeTraining;

import robocode.*;

import java.io.IOException;
import java.io.PrintStream;

public class MainRobot extends AdvancedRobot {
    //look up table object


    public static LookUpTable lut = new LookUpTable();

    //stats
    public static int rounds;
    public static int totalRounds;
    public static final int avgRounds = 50; //number of round to average the win rate


    public static int numWins;
    public static double winRate;

    public static boolean intermediateRewards = true;
    public static double rewards;
    public static final double positiveReward = 3;
    public static final double negativeReward = -3;
    public static final double positiveIntermediateReward = 0.3;
    public static final double negativeIntermediateReward = -0.3;
    public static boolean onPolicy = false;

    //my robot actions
    public static final int aheadIncrement = 50;
    public static final int backIncrement = 50;
    public static final int turnIncrement = 20; //degrees
    public static final int left = 0;
    public static final int right = 1;
    public static final int ahead = 2;
    public static final int back = 3;
    public static final int shoot = 4;

    public static String enemyName;


    //enemy parameters
//    private static double enemyVelocity;
//
//    private static double enemyTime;

    public static double enemyDistance;
    //    private static double enemyHeading;
    public static double enemyBearing;
    public static double enemyBearingDegrees;
    //    private static double changeHeading;
//    private static double enemyXPos;
//    private static double enemyYPos;
    public static double turn;


    public void run() {
        rewards = 0.0;
        enemyDistance = 2000;
        loadLUT(); //load look up table
        //pioint gun and radar at enemy
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        turnRadarRightRadians(2 * Math.PI);

        while (true) {
//            setAdjustGunForRobotTurn(true);
//            setAdjustRadarForGunTurn(true);


            double heading = getHeading();
            double xPos = getX();
            double yPos = getX();

//            int stateIndex = lut.quantizeStates(enemyDistance, enemyBearing, heading, xPos, yPos);
            int stateIndex = lut.quantizeStates(enemyDistance, enemyBearing, heading, xPos, yPos);

            //pick action index based on state index
            int actionIndex = lut.getAction(stateIndex); //TODO: random number always 4
            //update look up table
            lut.updateLUT(onPolicy, rewards, stateIndex, actionIndex);

            if (actionIndex == left) {
                setTurnLeft(turnIncrement);
            } else if (actionIndex == right) {
                setTurnRight(turnIncrement);
            } else if (actionIndex == ahead) {
                setAhead(aheadIncrement);
            } else if (actionIndex == back) {
                setBack(backIncrement);
            } else if (actionIndex == shoot) {
                fire(1);
            }
            // calculate firepower based on distance
            double firePower = Math.min(500 / enemyDistance, 3);
            //  calculate gun turn toward enemy
            double turn = getHeading() - getGunHeading() + enemyBearingDegrees;
            // normalize the turn to take the shortest path there
            setTurnGunRight(normalizeBearing(turn));
            // if the gun is cool and we're pointed at the target, shoot!
            if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)
                setFire(firePower);
////            aimShoot();

            execute();
        }
    }

    public void aimShoot() {

    }

    // normalizes a bearing to between +180 and -180
    double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        //  calculate gun turn toward enemy

//        double absoluteBearing = (getHeadingRadians() + e.getBearingRadians()) % (2 * Math.PI);

//        double headingValue = quantizeBearing(e.getHeadingRadians() - enemyHeading);
//        headingValue = headingValue / (getTime() - enemyTime);
//
        enemyBearing = e.getBearingRadians();
        enemyBearingDegrees = e.getBearing();
        enemyDistance = e.getDistance(); //relative distance from enemy
        enemyName = e.getName();

    }


    //execute when winning
    public void onWin(WinEvent event) {
        rewards += positiveReward;
        rounds += 1;
        totalRounds += 1;
        numWins += 1;

        if (rounds == avgRounds) {
            try {
                updateLogs();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        loadLUT();
    }

    //execute when losing
    public void onDeath(DeathEvent event) {
        rewards += negativeReward;
        rounds += 1;
        totalRounds += 1;

        if (rounds == avgRounds) {
            try {
                updateLogs();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        logLUT();
    }

    public void updateLogs() throws IOException {


//        LogFile log = new LogFile(getDataFile("my_robot.log"));


        winRate = 100 * (double) (numWins) / avgRounds;
//        log.stream.printf("%4d, %2d, %2.2f, %2.3f, %4d, %2.3f  %s\n", rounds, numWins, lut.exploreRate, totalRounds, winRate);
//
//        log.stream.flush();

//
//        savelogs("number_of_wins.dat");
//        savelogs("win_rate.dat");
//        savelogs("number_of_rounds.dat");

        saveNumWin();
        saveWinningRate();
        saveNumRounds();

        //reset
        rewards = 0;
        numWins = 0;
        rounds = 0;
    }

    public void saveNumWin(){
        PrintStream file1 = null;
        try {
            file1 = new PrintStream(new RobocodeFileOutputStream(getDataFile("number_of_wins.dat").getAbsolutePath(), true));

            file1.println(numWins);


            if (file1.checkError())
                System.out.println("can't save number of wins");
            file1.close();
        } catch (IOException e) {

        } finally {
            try {
                if (file1 != null)
                    file1.close();
            } catch (Exception e) {

            }
        }
    }
    public void saveWinningRate(){
        PrintStream file2 = null;
        try {
            file2 = new PrintStream(new RobocodeFileOutputStream(getDataFile("win_rate.dat").getAbsolutePath(), true));
//            file2.println(winRate);
            file2.println( String.format("%2.3f %d", winRate, totalRounds));


            if (file2.checkError())
                System.out.println("can't save win rate");
            file2.close();
        } catch (IOException e) {

        } finally {
            try {
                if (file2 != null)
                    file2.close();
            } catch (Exception e) {

            }
        }
    }

    public void saveNumRounds(){
        PrintStream file3 = null;
        try {
            file3 = new PrintStream(new RobocodeFileOutputStream(getDataFile("number_of_rounds.dat").getAbsolutePath(), true));
            file3.println(totalRounds);

            if (file3.checkError())
                System.out.println("can't save rounds");
            file3.close();
        } catch (IOException e) {

        } finally {
            try {
                if (file3 != null)
                    file3.close();
            } catch (Exception e) {

            }
        }
    }

    public void savelogs(String fileName) throws IOException {
        PrintStream File1 = null;
        try {

            //        PrintStream File2 = null;
            //        PrintStream File3 = null;
            File1 = new PrintStream(new RobocodeFileOutputStream(getDataFile(fileName).getAbsolutePath(), true));
            //        File2 = new PrintStream(new RobocodeFileOutputStream(getDataFile("win_rate.dat").getAbsolutePath(), true));
            //        File3 = new PrintStream(new RobocodeFileOutputStream(getDataFile("number_of_rounds.dat").getAbsolutePath(), true));

            //        System.out.printf("%4d, %2d, %2.2f, %2.3f, %4d, %2.3f  %s\n", rounds, numWins, lut.exploreRate, totalRounds, winRate);
            File1.println(numWins);
            if (File1.checkError())
                System.out.println("error");
            File1.close();

            //        File2.println(winRate);
            //        if (File2.checkError())
            //            System.out.println("error");
            //        File2.close();
            //
            //        File3.println(totalRounds);
            //        if (File3.checkError())
            //            System.out.println("error");
            //        File3.close();

        } catch (IOException e) {
            System.out.println(e);
        }
        finally {
            try {
                if (File1 != null) {
                    File1.close();
//                    File2.close();
//                    File3.close();


                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }


    public void loadLUT() {
        try {
            lut.load(getDataFile("LUTData.dat"));
        } catch (Exception e) {
            out.println(e);
        }
    }

    public void logLUT() {
        try {
            lut.save(getDataFile("LUTData.dat"));
        } catch (Exception e) {
            out.println(e);
        }
    }


    //intermediate rewards update
    public void onBulletMissed(BulletMissedEvent e) {
        if (intermediateRewards) {
            rewards += negativeIntermediateReward;

        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName() == enemyName)
            enemyDistance = 2000; //reset
    }

    public void onHitWall(HitWallEvent e) {

        if (intermediateRewards) {
            rewards += (negativeIntermediateReward + 1);
        }

    }
    public void onBulletHit(BulletHitEvent e) {
        if (e.getName() == enemyName) {
            if (intermediateRewards) {
                rewards += positiveIntermediateReward;

            }
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (e.getName() == enemyName)
            if (intermediateRewards) {
                rewards += negativeIntermediateReward;
            }

    }





}
