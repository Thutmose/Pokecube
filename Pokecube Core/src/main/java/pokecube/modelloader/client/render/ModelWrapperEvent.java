package pokecube.modelloader.client.render;

import net.minecraftforge.fml.common.eventhandler.Event;
import thut.core.client.render.wrappers.ModelWrapper;

public class ModelWrapperEvent extends Event
{
    public ModelWrapper wrapper;
    public final String name;

    public ModelWrapperEvent(ModelWrapper wrapper, String name)
    {
        this.wrapper = wrapper;
        this.name = name;
    }

}
