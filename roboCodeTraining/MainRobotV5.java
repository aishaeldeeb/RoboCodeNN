package roboCodeTraining;

import robocode.*;

import java.io.IOException;
import java.io.PrintStream;

public class MainRobotV5 extends AdvancedRobot {

    public static LookUpTableV5 lut = new LookUpTableV5();

    //stats
    public static int rounds;
    public static int totalRounds;
    public static final int avgRounds = 100; //number of round to average the win rate


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
    public static double enemyDistance;
    public static double enemyBearing;
    public static double enemyBearingDegrees;
    public static int quantizedEnemyDistance;
    public static int quantizedEnemyBearingCos;
    public static int quantizedEnemyBearingSin;
    public static int quantizedHeadingCos;
    public static int quantizedHeadingSin;

    public static double gunTurnAmt;


    public static int leftCount;
    public static int rightCount;
    public static int aheadCount;
    public static int backCount;
    public static int fireCount;


    public void run() {

        leftCount= 0;
        rightCount = 0;
        aheadCount = 0;
        backCount = 0;
        fireCount =0;
        rewards = 0.0;

        if (totalRounds == 0){
            loadLUT();

        }

//        loadLUT();

        //initilize epsilon to 0.9
        lut.initEpsilon(totalRounds);


        // divorce radar movement from gun movement
        setAdjustRadarForGunTurn(true);
        // divorce gun movement from tank movement
        setAdjustGunForRobotTurn(true);
        // initial scan
        setTurnRadarRight(360);

        while (true) {



            if (rounds == avgRounds) {
                //update epsilon value
                System.out.println("New Epsilon: " + lut.exploreRate + "\n");
                try {
                    updateLogs();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            double heading = getHeading();
            double xPos = getX();
            double yPos = getY();


            quantizedEnemyDistance = lut.quantizeEnemyDistance(enemyDistance);
//            System.out.println("Enemy Distanc: " + quantizedEnemyDistance);

            quantizedEnemyBearingCos = lut.quantizeHeading(Math.cos(enemyBearingDegrees));
            quantizedEnemyBearingSin = lut.quantizeHeading(Math.sin(enemyBearingDegrees));


            quantizedHeadingCos = lut.quantizeHeading(Math.cos(heading));
            quantizedHeadingSin = lut.quantizeHeading(Math.sin(heading));

            int quantizedXPos = lut.quantizeXPos(xPos);
            int quantizedYPos = lut.quantizeXPos(yPos);

            //pick action index based on state index
            int actionIndex = lut.getAction(quantizedHeadingCos, quantizedEnemyDistance, quantizedEnemyBearingSin, quantizedXPos, quantizedYPos,  quantizedHeadingSin, quantizedEnemyBearingCos);
            //update look up table
            lut.updateLUT(onPolicy, rewards, quantizedHeadingCos, quantizedEnemyDistance, quantizedHeadingSin,  quantizedXPos, quantizedYPos, quantizedEnemyBearingCos, quantizedEnemyBearingSin, actionIndex);
            // calculate firepower based on distance
            double firePower = Math.min(500 / enemyDistance, 3);
            if (actionIndex == left) {
                leftCount++;
                setTurnLeft(turnIncrement);
            } else if (actionIndex == right) {
                rightCount++;
                setTurnRight(turnIncrement);
            } else if (actionIndex == ahead) {
                aheadCount++;
                setAhead(aheadIncrement);
            } else if (actionIndex == back) {
                backCount++;
                setBack(backIncrement);
            } else if (actionIndex == shoot) {
                fireCount++;
                fire(firePower);
            }

            //  calculate gun turn toward enemy
            double turn = getHeading() - getGunHeading() + normalizeBearing(enemyBearingDegrees);
            // normalize the turn to take the shortest path there
            setTurnGunRight(normalizeBearing(turn));
            // if the gun is cool and we're pointed at the target, shoot!
//            if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)
//                setFire(firePower);

            execute();
        }
    }



    // normalizes a bearing to between +180 and -180
    double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    public void onScannedRobot(ScannedRobotEvent e) {

//        // If we have a target, and this isn't it, return immediately
//        // so we can get more ScannedRobotEvents.
//        if (enemyName != null && !e.getName().equals(enemyName)) {
//            return;
//        }
//
//        // If we don't have a target, well, now we do!
//        if (enemyName == null) {
//            enemyName = e.getName();
//            out.println("Tracking " + enemyName);
//        }
//        // This is our target.  Reset count (see the run method)
//        count = 0;
//        // If our target is too far away, turn and move toward it.
//        if (e.getDistance() > 150) {
//            gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
//
//            turnGunRight(gunTurnAmt); // Try changing these to setTurnGunRight,
//            turnRight(e.getBearing()); // and see how much Tracker improves...
//            // (you'll have to make Tracker an AdvancedRobot)
//            ahead(e.getDistance() - 140);
//            return;
//        }
//
//        // Our target is close.
//        gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
//        turnGunRight(gunTurnAmt);
////        fire(3);
//
//
//        // Our target is too close!  Back up.
//        if (e.getDistance() < 100) {
//            if (e.getBearing() > -90 && e.getBearing() <= 90) {
//                back(40);
//            } else {
//                ahead(40);
//            }
//        }
//        scan();
        enemyBearing = e.getBearingRadians();
        enemyBearingDegrees = e.getBearing();
        enemyDistance = e.getDistance(); //relative distance from enemy
        enemyName = e.getName();


    }

    public static double normalRelativeAngleDegrees(double angle) {
        return (angle %= 360) >= 0 ? (angle < 180) ? angle : angle - 360 : (angle >= -180) ? angle : angle + 360;
    }

    //execute when winning
    public void onWin(WinEvent event) {
        rewards += positiveReward;
        rounds += 1;
        totalRounds += 1;
        numWins += 1;

//        if (rounds == avgRounds) {
//            //update epsilon value every 100 rounds
//            lut.initEpsilon(totalRounds);
//            System.out.println("New Epsilon: " + lut.exploreRate + "\n");
//            try {
//                updateLogs();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
        logLUT();
    }

    //execute when losing
    public void onDeath(DeathEvent event) {
        rewards += negativeReward;
        rounds += 1;
        totalRounds += 1;

//        if (rounds == avgRounds) {
//            lut.initEpsilon(totalRounds);
//            System.out.println("Updating Stats");
//            try {
//                updateLogs();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
        logLUT();
    }

    public void updateLogs() throws IOException {
        winRate = 100 * (double) (numWins) / avgRounds;
        System.out.println("Win Rate" + winRate + "number of wins" + numWins + "rounds" + rounds);
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

            file1.println(lut.exploreRate);


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
            file2.println(winRate);
//            file2.println( String.format("%1.1f %d %1.1f \n", winRate, totalRounds, lut.exploreRate));


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
        // Only print if he's not already our target.
        if (enemyName != null && !enemyName.equals(e.getName())) {
            out.println("Tracking " + e.getName() + " due to collision");
        }
        // Set the target
        enemyName = e.getName();
        // Back up a bit.
        // Note:  We won't get scan events while we're doing this!
        // An AdvancedRobot might use setBack(); execute();
        gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
        turnGunRight(gunTurnAmt);
//        fire(3);
        back(50);


        if (e.getName() == enemyName)
            if (intermediateRewards) {
                rewards += negativeIntermediateReward;
            }

    }





}
