package pokecube.core.events;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StarterEvent extends Event
{
    @Cancelable
    @HasResult
    public static class Pick extends StarterEvent
    {
        public Pick(EntityPlayer player, Collection<ItemStack> starterPack, int numberPicked)
        {
            super(player, starterPack, numberPicked);
        }
    }

    @Cancelable
    @HasResult
    public static class Pre extends StarterEvent
    {
        public Pre(EntityPlayer player)
        {
            super(player, null, 0);
        }
    }

    public final EntityPlayer player;
    public List<ItemStack>    starterPack = Lists.newArrayList();

    public final int          pick;

    public StarterEvent(EntityPlayer player, Collection<ItemStack> pack, int numberPicked)
    {
        this.player = player;
        if (pack != null) starterPack.addAll(pack);
        pick = numberPicked;
    }

}
