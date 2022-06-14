package backpack;

import simbad.sim.*;
import javax.vecmath.*;
import java.awt.*;

/*
 * Date: 06/2022
 * Purpose: Intelligent Agents Class
 */

/**
 * Describes custom <code>Agent</code> objects with a preset of sensors and specific behavior.
 * @author Parmenion Charistos
 */

public class Robot extends Agent {
    RangeSensorBelt sonars;
    RangeSensorBelt bumpers;
    LightSensor left_light;
    LightSensor right_light;
    LineSensor IR_sensors;
    boolean isTracingLine = false;
    boolean CLOCKWISE = true;

    static double K1 = 5;
    static double K2 = 0.8;
    static double K3 = 1;
    static double SAFETY = 0.8;

    /**
     * Class constructor.
     * @param position <code>Vector3d</code> object describing the agent's initial position.
     * @param name <code>String</code> containing the agent's name.
     */

    public Robot(Vector3d position, String name) {
        super(position, name);
        this.setColor(new Color3f(Color.blue));
        sonars = RobotFactory.addSonarBeltSensor(this, 12);
        bumpers = RobotFactory.addBumperBeltSensor(this, 8);
        left_light = RobotFactory.addLightSensorLeft(this);
        right_light = RobotFactory.addLightSensorRight(this);
        IR_sensors = RobotFactory.addLineSensor(this, 11);
    }

    /**
     * Set of actions to be taken once in the beginning of the simulation.
     */

    public void initBehavior() {
        setTranslationalVelocity(0.5);
    }

    /**
     * Describes the agent's behavior according to its interactions with its <code>Environment</code>.
     * This method is being executed continuously as the agent's sensory data updates and its actuators take action.
     */

    public void performBehavior() {
        // IF GOAL IS REACHED, STOP.
        if (weighLux() >= 0.06) {
            setTranslationalVelocity(0);
        } else {
            followTheLine();

            if (sonars.oneHasHit() && sonars.getQuadrantMeasurement(0,6.28) <= 0.8) {
                if (right_light.getLux() > left_light.getLux())
                    circumNavigate(sonars, CLOCKWISE);
                else
                    this.setTranslationalVelocity(0.5);
            }
        }
    }

    /**
     * Describes the agent's behavior when it has not encountered an obstacle.
     */

    public void followTheLine() {
        int left = 0, right = 0, k = 0;

        for (int i = 0; i < IR_sensors.getNumSensors() / 2; i++) {
            left += IR_sensors.hasHit(i) ? 1 : 0;
            right += IR_sensors.hasHit(IR_sensors.getNumSensors() - i - 1) ? 1 : 0;
            k++;
        }

        if (left != right) {
            this.setRotationalVelocity((left - right) / (float) k * 5);
            isTracingLine = true;
        } else if (left == 0) {
            this.setRotationalVelocity(0);
            isTracingLine = false;
        } else if (left + right > IR_sensors.getNumSensors() - 3) {
            if (right_light.getAverageLuminance() > left_light.getAverageLuminance())
                this.rotateY(-Math.PI / 4);
            else
                this.rotateY(Math.PI / 4);
            isTracingLine = true;
        }

        if (!isTracingLine)
            followTheLight();
    }

    /**
     * Describes the agent's behavior when it has not encountered an obstacle and is not tracing a line.
     */

    public void followTheLight() {
        double reading_left = this.left_light.getAverageLuminance();
        double reading_right = this.right_light.getAverageLuminance();
        this.setRotationalVelocity((reading_left - reading_right) * Math.PI / 4.0);
    }

    /**
     * @author dvrakas
     * @param a <code>Double</code> radians to transform.
     * @return equivalent radians in the interval [-π, π].
     */

    public static double wrapToPi(double a){
        if (a>Math.PI)
            return a-Math.PI*2;
        if (a<=-Math.PI)
            return a+Math.PI*2;
        return a;
    }

    /**
     * @author dvrakas
     * @param sonars Sonar sensors on this agent.
     * @param sonar Particular sonar sensor with mininum measurement from obstacle.
     * @return determined <code>Point3d</code> in space detected by the sonar.
     */

    public Point3d getSensedPoint(RangeSensorBelt sonars,int sonar){
        double v;
        if (sonars.hasHit(sonar))
            v = this.getRadius() + sonars.getMeasurement(sonar);
        else
            v = this.getRadius() + sonars.getMaxRange();
        double x = v * Math.cos(sonars.getSensorAngle(sonar));
        double z = v * Math.sin(sonars.getSensorAngle(sonar));
        return new Point3d(x,0,z);
    }

    /**
     * Describes the agent's behavior for obstacle avoidance.
     * @author dvrakas
     * @param sonars Sonar sensors on this agent.
     * @param CLOCKWISE Describes which direction to trail the obstacle towards.
     */

    public void circumNavigate(RangeSensorBelt sonars, boolean CLOCKWISE){
        int min;
        min=0;

        for (int i=1;i<sonars.getNumSensors();i++)
            if (sonars.getMeasurement(i)<sonars.getMeasurement(min))
                min=i;

        Point3d p = getSensedPoint(sonars,min);
        double d = p.distance(new Point3d(0,0,0));
        Vector3d v;
        v = CLOCKWISE? new Vector3d(-p.z,0,p.x): new Vector3d(p.z,0,-p.x);
        double phLin = Math.atan2(v.z,v.x);
        double phRot = Math.atan(K3*(d-SAFETY));

        if (CLOCKWISE)
            phRot=-phRot;

        double phRef = wrapToPi(phLin+phRot);

        this.setRotationalVelocity(K1*phRef);
        this.setTranslationalVelocity(K2*Math.cos(phRef));
    }

    /**
     * @return the average of the light measurements of the agent's sensors.
     */
    public double weighLux() {
        return (left_light.getLux() + right_light.getLux()) / 2;
    }
}
