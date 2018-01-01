package pokecube.core.items.loot.datafixers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import pokecube.core.database.Database;
import pokecube.core.items.vitamins.ItemVitamin;

public class VitaminFixer implements IFixableData
{
    @Override
    public int getFixVersion()
    {
        return 0;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound)
    {
        if ("pokecube:vitamins".equals(compound.getString("id")) && compound.hasKey("tag", 10))
        {
            NBTTagCompound tag = compound.getCompoundTag("tag");
            if (tag.hasKey("vitamin"))
            {
                String type = tag.getString("vitamin");
                for (int i = 0; i < ItemVitamin.vitamins.size(); i++)
                {
                    String s = ItemVitamin.vitamins.get(i);
                    if (Database.trim(s).equals(Database.trim(type)))
                    {
                        compound.setInteger("Damage", i);
                        tag.removeTag("vitamin");
                        break;
                    }
                }
            }
            if (tag.hasNoTags()) compound.removeTag("tag");
        }
        return compound;
    }

}
