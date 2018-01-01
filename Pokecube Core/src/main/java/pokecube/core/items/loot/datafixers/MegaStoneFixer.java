package pokecube.core.items.loot.datafixers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import pokecube.core.database.Database;
import pokecube.core.items.megastuff.ItemMegastone;

public class MegaStoneFixer implements IFixableData
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
            if (tag.hasKey("pokemon"))
            {
                String pokemon = tag.getString("pokemon");
                for (int i = 0; i < ItemMegastone.getStonesCount(); i++)
                {
                    String s = ItemMegastone.getStone(i);
                    if (Database.trim(s).equals(Database.trim(pokemon)))
                    {
                        compound.setInteger("Damage", i);
                        tag.removeTag("pokemon");
                        break;
                    }
                }
            }
            if (tag.hasNoTags()) compound.removeTag("tag");
        }
        return compound;
    }

}
