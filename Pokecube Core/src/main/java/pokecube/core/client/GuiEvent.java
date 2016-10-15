package pokecube.core.client;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GuiEvent extends Event
{

    public GuiEvent()
    {
        // TODO Auto-generated constructor stub
    }

    @Cancelable
    public static class RenderTargetInfo extends GuiEvent
    {

    }

    @Cancelable
    public static class RenderSelectedInfo extends GuiEvent
    {

    }

}
