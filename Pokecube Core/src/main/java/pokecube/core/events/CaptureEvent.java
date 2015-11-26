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
    public final ItemStack filledCube;
    public final Entity    pokecube;
    public final IPokemob  caught;

    protected CaptureEvent(EntityPokecube pokecube)
    {
        this.pokecube = pokecube;
        if (pokecube != null)
        {
            this.filledCube = pokecube.getEntityItem();
            this.caught = PokecubeManager.itemToPokemob((pokecube.getEntityItem()), pokecube.worldObj);
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
        filledCube = pokecube.getEntityItem();
    }

    @Cancelable
    public static class Pre extends CaptureEvent
    {
        public Pre(IPokemob hit, EntityPokecube pokecube)
        {
            super(hit, pokecube);
        }

    }

    @Cancelable
    public static class Post extends CaptureEvent
    {
        public Post(EntityPokecube pokecube)
        {
            super(pokecube);
        }

    }

}
