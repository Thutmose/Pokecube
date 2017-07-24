package pokecube.core.handlers;

import java.lang.reflect.Field;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.database.Database;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.events.PokedexInspectEvent;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;

public class PokedexInspector
{
    public static interface IInspectReward
    {
        boolean inspect(PokecubePlayerCustomData data, Entity entity, boolean giveReward);
    }

    public static class InspectCapturesReward implements IInspectReward
    {
        final ItemStack reward;
        final Field     configField;
        final String    message;
        final String    tagString;

        public InspectCapturesReward(ItemStack reward, Field configField, String message, String tagString)
        {
            this.reward = reward;
            this.configField = configField;
            this.message = message;
            this.tagString = tagString;
        }

        private boolean check(Entity entity, String configArg, NBTTagCompound tag, ItemStack reward, int num,
                boolean giveReward)
        {
            if (reward == null || tag.getBoolean(tagString)) return false;
            if (matches(num, configArg))
            {
                if (giveReward)
                {
                    tag.setBoolean(tagString, true);
                    entity.sendMessage(new TextComponentTranslation(message));
                    EntityPlayer entityPlayer = (EntityPlayer) entity;
                    Tools.giveItem(entityPlayer, reward);
                    PokecubePlayerDataHandler.saveCustomData(entity.getCachedUniqueIdString());
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
                required = (int) (Double.parseDouble(arg.replace("%", "")) * Database.spawnables.size() / 100d);
            }
            else
            {
                required = (int) (Double.parseDouble(arg));
            }
            return required <= num;
        }

        @Override
        public boolean inspect(PokecubePlayerCustomData data, Entity entity, boolean giveReward)
        {
            int num = CaptureStats.getNumberUniqueCaughtBy(entity.getUniqueID());
            try
            {
                return check(entity, (String) configField.get(PokecubeMod.core.getConfig()), data.tag, reward, num,
                        giveReward);
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean inspect(EntityPlayer player, boolean reward)
    {
        PokedexInspectEvent evt;
        MinecraftForge.EVENT_BUS.post(evt = new PokedexInspectEvent(player, reward));
        if (evt.isCanceled())
        {
            String uuid = evt.getEntity().getCachedUniqueIdString();
            PokecubePlayerDataHandler.getInstance().save(uuid);
        }
        return evt.isCanceled();
    }

    public static Set<IInspectReward> rewards = Sets.newHashSet();

    public static void init()
    {
        Database.loadRewards();
    }

    public PokedexInspector()
    {
        MinecraftForge.EVENT_BUS.register(this);
        rewards.clear();
        init();
    }

    @SubscribeEvent(receiveCanceled = false, priority = EventPriority.LOWEST)
    public void inspectEvent(PokedexInspectEvent evt)
    {
        String uuid = evt.getEntity().getCachedUniqueIdString();
        PokecubePlayerCustomData data = PokecubePlayerDataHandler.getInstance().getPlayerData(uuid)
                .getData(PokecubePlayerCustomData.class);
        boolean done = false;
        for (IInspectReward reward : rewards)
        {
            boolean has = reward.inspect(data, evt.getEntity(), evt.shouldReward);
            done = done || has;
            if (done && !evt.shouldReward) break;
        }
        if (done) evt.setCanceled(true);
    }
}
