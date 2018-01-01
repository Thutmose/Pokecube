package pokecube.core.items.loot.datafixers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import pokecube.core.database.Database;
import pokecube.core.items.megastuff.ItemMegawearable;

public class MegaWearableFixer implements IFixableData
{
    @Override
    public int getFixVersion()
    {
        return 0;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound)
    {
        if ("pokecube:megaring".equals(compound.getString("id")) && compound.hasKey("tag", 10))
        {
            NBTTagCompound tag = compound.getCompoundTag("tag");
            if (tag.hasKey("type"))
            {
                String type = tag.getString("type");
                for (int i = 0; i < ItemMegawearable.getWearableCount(); i++)
                {
                    String s = ItemMegawearable.getWearable(i);
                    if (Database.trim(s).equals(Database.trim(type)))
                    {
                        compound.setInteger("Damage", i);
                        tag.removeTag("type");
                        break;
                    }
                }
            }
            if (tag.hasNoTags()) compound.removeTag("tag");
        }
        return compound;
    }

}
