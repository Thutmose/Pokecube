package pokecube.core.client;

import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GuiEvent extends Event
{

    public GuiEvent()
    {
    }

    @Cancelable
    public static class RenderTargetInfo extends GuiEvent
    {

    }

    @Cancelable
    public static class RenderSelectedInfo extends GuiEvent
    {

    }

    @Cancelable
    public static class RenderTeleports extends GuiEvent
    {

    }

    @Cancelable
    public static class RenderMoveMessages extends GuiEvent
    {
        final ElementType type;

        public RenderMoveMessages(ElementType type)
        {
            this.type = type;
        }

        public ElementType getType()
        {
            return type;
        }

    }

}
