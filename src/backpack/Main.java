package backpack;

import simbad.gui.Simbad;

/*
 * Date: 06/2022
 * Purpose: Intelligent Agents Class
 */

/**
 * Controller Class for simulation initialization.
 * @author Parmenion Charistos
 */

public class Main {
    public static void main(String[] args) {
        Simbad frame = new Simbad(new Environment(), false);
    }
}
