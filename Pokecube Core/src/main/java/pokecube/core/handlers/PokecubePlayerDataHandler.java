package pokecube.core.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import thut.core.common.handlers.PlayerDataHandler;

public class PokecubePlayerDataHandler extends PlayerDataHandler
{
    public static NBTTagCompound getCustomDataTag(EntityPlayer player)
    {
        PlayerDataManager manager = PokecubePlayerDataHandler.getInstance().getPlayerData(player);
        PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static NBTTagCompound getCustomDataTag(String player)
    {
        PlayerDataManager manager = PokecubePlayerDataHandler.getInstance().getPlayerData(player);
        PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static void saveCustomData(EntityPlayer player)
    {
        saveCustomData(player.getCachedUniqueIdString());
    }

    public static void saveCustomData(String cachedUniqueIdString)
    {
        getInstance().save(cachedUniqueIdString, "pokecube-custom");
    }
}
