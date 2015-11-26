package pokecube.modelloader.client.tabula.model.tabula;

import java.awt.image.BufferedImage;

import pokecube.modelloader.client.tabula.json.JsonTabulaModel;
import pokecube.modelloader.client.tabula.model.IModel;

public class TabulaModel extends JsonTabulaModel implements IModel {
    public transient BufferedImage texture;
    private String modelName;
    private String authorName;

    public String getName() {
        return modelName;
    }

    public String getAuthor() {
        return authorName;
    }
}
