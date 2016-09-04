package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.getItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.events.PokedexInspectEvent;
import pokecube.core.handlers.PlayerDataHandler.PokecubePlayerCustomData;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.items.ItemTM;

public class PokedexInspector
{
    public static boolean inspect(EntityPlayer player, boolean reward)
    {
        PokedexInspectEvent evt;
        MinecraftForge.EVENT_BUS.post(evt = new PokedexInspectEvent(player, reward));
        if (evt.isCanceled())
        {
            String uuid = evt.getEntity().getCachedUniqueIdString();
            PlayerDataHandler.getInstance().save(uuid);
        }
        return evt.isCanceled();
    }

    public PokedexInspector()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(receiveCanceled = false, priority = EventPriority.LOWEST)
    public void inspectEvent(PokedexInspectEvent evt)
    {
        String uuid = evt.getEntity().getCachedUniqueIdString();
        PokecubePlayerCustomData data = PlayerDataHandler.getInstance().getPlayerData(uuid).getData("pokecube-custom",
                PokecubePlayerCustomData.class);
        ItemStack cut = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveNames.MOVE_CUT, cut);
        ItemStack flash = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveNames.MOVE_FLASH, flash);
        ItemStack rocksmash = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveNames.MOVE_ROCKSMASH, rocksmash);
        int num = CaptureStats.getNumberUniqueCaughtBy(uuid);
        boolean hasExpShare = check(evt.getEntity(), PokecubeCore.core.getConfig().exp_shareRequirement, data.tag,
                PokecubeItems.getStack("exp_share"), num, "pokedex.inspect.exp_share", "inspect-exp_share",
                evt.shouldReward);
        boolean hasCutTM = check(evt.getEntity(), PokecubeCore.core.getConfig().cutTMRequirement, data.tag, cut, num,
                "pokedex.inspect.cutTM", "inspect-cutTM", evt.shouldReward);
        boolean hasFlashTM = check(evt.getEntity(), PokecubeCore.core.getConfig().flashTMRequirement, data.tag, flash,
                num, "pokedex.inspect.flashTM", "inspect-flashTM", evt.shouldReward);
        boolean hasRocksmashTM = check(evt.getEntity(), PokecubeCore.core.getConfig().rocksmashTMRequirement, data.tag,
                rocksmash, num, "pokedex.inspect.rocksmashTM", "inspect-rocksmashTM", evt.shouldReward);
        boolean hasMastercube = check(evt.getEntity(), PokecubeCore.core.getConfig().mastercubeRequirement, data.tag,
                PokecubeItems.getStack("mastercube"), num, "pokedex.inspect.mastercube", "inspect-mastercube",
                evt.shouldReward);
        boolean hasShinyCharm = check(evt.getEntity(), PokecubeCore.core.getConfig().shinycharmRequirement, data.tag,
                PokecubeItems.getStack("shiny_charm"), num, "pokedex.inspect.shinycharm", "inspect-shinycharm",
                evt.shouldReward);

        if (hasMastercube || hasShinyCharm || hasExpShare || hasCutTM || hasFlashTM || hasRocksmashTM)
        {
            evt.setCanceled(true);
        }
    }

    private boolean check(Entity entity, String configArg, NBTTagCompound tag, ItemStack reward, int num,
            String message, String tagString, boolean giveReward)
    {
        if (reward == null || tag.getBoolean(tagString)) return false;
        if (matches(num, PokecubeCore.core.getConfig().shinycharmRequirement))
        {
            if (giveReward)
            {
                tag.setBoolean(tagString, true);
                entity.addChatMessage(new TextComponentTranslation(message));
                EntityItem item = entity.entityDropItem(reward, 0.5f);
                item.setPickupDelay(0);
            }
            return true;
        }
        return false;
    }

    private boolean matches(int num, String arg)
    {
        int required = 0;
        if (arg.contains("%"))
        {
            required = (int) (Double.parseDouble(arg.replace("%", "")) * Database.spawnables.size());
        }
        else
        {
            required = (int) (Double.parseDouble(arg));
        }
        return required <= num;
    }
}
