package backpack;

import simbad.sim.*;
import javax.vecmath.*;

/*
 * Date: 06/2022
 * Purpose: Intelligent Agents Class
 */

/**
 * Describes custom <code>EnvironmentDescription</code> objects for Simbad simulator.
 * @author Parmenion Charistos
 */

public class Environment extends EnvironmentDescription {
    Robot agent = new Robot(new Vector3d(-8,0,5), "Jim");

    /**
     * Class contructor.
     */

    public Environment() {
        add(agent);

        light1SetPosition(2,2,1);
        light1IsOn = true;
        light2IsOn = false;

        add(new Line(new Vector3d(-7,0,-3),10,this));
        add(new Line(new Vector3d(8,0,-3),4,this));
        Line line = new Line(new Vector3d(-10,0,-3),18,this);
        line.rotate90(1);
        add(line);

        Wall wall = new Wall(new Vector3d(-3,0,4),10,1,this);
        wall.rotate90(3);
        add(wall);
        add(new Wall(new Vector3d(0,0,-1),8,1,this));
        add(new Wall(new Vector3d(0,0,5),8,1,this));

        add(new Box(new Vector3d(-5,0,-3), new Vector3f(1,1,1),this));
    }
}