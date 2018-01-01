package pokecube.core.items.loot.datafixers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import pokecube.core.database.Database;
import pokecube.core.items.ItemHeldItems;

public class HeldItemFixer implements IFixableData
{
    @Override
    public int getFixVersion()
    {
        return 0;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound)
    {
        if ("pokecube:megastone".equals(compound.getString("id")) && compound.hasKey("tag", 10))
        {
            NBTTagCompound tag = compound.getCompoundTag("tag");
            if (tag.hasKey("type"))
            {
                String type = tag.getString("type");
                for (int i = 0; i < ItemHeldItems.variants.size(); i++)
                {
                    String s = ItemHeldItems.variants.get(i);
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
