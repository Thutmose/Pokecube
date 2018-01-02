package pokecube.core.events;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.database.PokedexEntry;

public class StarterEvent extends Event
{
    @Cancelable
    @HasResult
    public static class Pick extends StarterEvent
    {
        public Pick(EntityPlayer player, Collection<ItemStack> starterPack, PokedexEntry entry)
        {
            super(player, starterPack, entry);
        }
    }

    @Cancelable
    @HasResult
    public static class Pre extends StarterEvent
    {
        public Pre(EntityPlayer player)
        {
            super(player, null, null);
        }
    }

    public final EntityPlayer player;
    public List<ItemStack>    starterPack = Lists.newArrayList();

    public final PokedexEntry pick;

    public StarterEvent(EntityPlayer player, Collection<ItemStack> pack, PokedexEntry numberPicked)
    {
        this.player = player;
        if (pack != null) starterPack.addAll(pack);
        pick = numberPicked;
    }

}
