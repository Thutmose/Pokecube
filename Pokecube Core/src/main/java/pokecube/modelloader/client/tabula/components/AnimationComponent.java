package pokecube.modelloader.client.tabula.components;

import java.util.ArrayList;

/**
 * Container for Tabula animation components.
 *
 * @author Gegy1000
 * @since 0.1.0
 */
public class AnimationComponent {
    public double[] posChange = new double[3];
    public double[] rotChange = new double[3];
    public double[] scaleChange = new double[3];
    public double opacityChange = 0.0D;

    public double[] posOffset = new double[3];
    public double[] rotOffset = new double[3];
    public double[] scaleOffset = new double[3];
    public double opacityOffset = 0.0D;

    public ArrayList<double[]> progressionCoords;

    public String name;

    public int length;
    public int startKey;

    public boolean hidden;

    public String identifier;
}
