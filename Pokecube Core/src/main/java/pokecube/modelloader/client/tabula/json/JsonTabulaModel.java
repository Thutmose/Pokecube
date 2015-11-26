package pokecube.modelloader.client.tabula.json;


import java.io.InputStream;
import java.util.ArrayList;

import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.CubeGroup;
import pokecube.modelloader.client.tabula.components.CubeInfo;

/**
 * Container class for {@link net.ilexiconn.llibrary.client.model.tabula.ModelJson}. Use {@link net.ilexiconn.llibrary.common.json.JsonHelper#parseTabulaModel(InputStream)} to get a new instance.
 *
 * @author Gegy1000
 * @see net.ilexiconn.llibrary.client.model.tabula.ModelJson
 * @since 0.1.0
 */
public class JsonTabulaModel {
    private int textureWidth = 64;
    private int textureHeight = 32;

    private double[] scale = new double[]{1d, 1d, 1d};

    private ArrayList<CubeGroup> cubeGroups;
    private ArrayList<CubeInfo> cubes;
    private ArrayList<Animation> anims;

    private int cubeCount;

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public double[] getScale() {
        return scale;
    }

    public ArrayList<CubeGroup> getCubeGroups() {
        return cubeGroups;
    }

    public ArrayList<CubeInfo> getCubes() {
        return cubes;
    }

    public ArrayList<Animation> getAnimations() {
        return anims;
    }

    public int getCubeCount() {
        return cubeCount;
    }
}