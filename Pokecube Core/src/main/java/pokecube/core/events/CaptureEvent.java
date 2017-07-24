package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;

@Cancelable
public class CaptureEvent extends Event
{
    @Cancelable
    /** Post is sent after the capture is completed, canceling it will prevent
     * the capture from being recorded, and will destroy the cube and the
     * mob. */
    public static class Post extends CaptureEvent
    {
        public Post(EntityPokecube pokecube)
        {
            super(pokecube);
        }

    }

    @Cancelable
    /** Pre is sent before the capture is completed, canceling it will prevent
     * capture. */
    public static class Pre extends CaptureEvent
    {
        public Pre(IPokemob hit, EntityPokecube pokecube)
        {
            super(hit, pokecube);
        }

    }

    public final ItemStack filledCube;

    public final Entity    pokecube;

    public final IPokemob  caught;

    protected CaptureEvent(EntityPokecube pokecube)
    {
        this.pokecube = pokecube;
        if (pokecube != null)
        {
            this.filledCube = pokecube.getItem();
            this.caught = PokecubeManager.itemToPokemob((pokecube.getItem()), pokecube.getEntityWorld());
        }
        else
        {
            this.filledCube = null;
            this.caught = null;
        }
    }

    protected CaptureEvent(IPokemob hit, EntityPokecube pokecube)
    {
        this.pokecube = pokecube;
        caught = hit;
        filledCube = pokecube.getItem();
    }

}
