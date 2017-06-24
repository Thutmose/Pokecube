package pokecube.core.handlers.playerdata;

import net.minecraft.nbt.NBTTagCompound;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

/** Generic data to store for each player, this gives another place besides in
 * the player's entity data to store information. */
public class PokecubePlayerCustomData extends PlayerData
{
    public NBTTagCompound tag = new NBTTagCompound();

    public PokecubePlayerCustomData()
    {
    }

    @Override
    public String getIdentifier()
    {
        return "pokecube-custom";
    }

    @Override
    public String dataFileName()
    {
        return "customData";
    }

    @Override
    public boolean shouldSync()
    {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        tag.setTag("data", this.tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        this.tag = tag.getCompoundTag("data");
    }

}
