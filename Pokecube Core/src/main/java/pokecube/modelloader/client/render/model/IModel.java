package pokecube.modelloader.client.render.model;

import java.util.Collection;
import java.util.HashMap;

import pokecube.modelloader.client.render.tabula.components.Animation;

public interface IModel
{
    public HashMap<String, IExtendedModelPart> getParts();

    public void preProcessAnimations(Collection<Animation> animations);
}
