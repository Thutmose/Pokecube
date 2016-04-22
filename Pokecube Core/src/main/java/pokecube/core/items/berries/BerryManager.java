/**
 * 
 */
package pokecube.core.items.berries;

import static pokecube.core.PokecubeItems.registerItemTexture;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

/** @author Oracion
 * @author Manchou */
public class BerryManager implements IMoveConstants
{

    /** Map of berry id -> block of crop */
    public static Map<Integer, Block>  berryCrops    = new HashMap<Integer, Block>();
    /** Map of berry id -> block of fruit */
    public static Map<Integer, Block>  berryFruits   = new HashMap<Integer, Block>();
    /** Map of berry id -> name of berry */
    public static Map<Integer, String> berryNames    = new HashMap<Integer, String>();
    /** Map of berry id -> flavours of berry, see {@link IMoveConstants.SPICY}
     * for the indecies of the array */
    public static Map<Integer, int[]>  berryFlavours = new HashMap<Integer, int[]>();

    public static void addBerry(String name, int id, int spicy, int dry, int sweet, int bitter, int sour)
    {
        berryNames.put(id, name);
        berryFlavours.put(id, new int[] { spicy, dry, sweet, bitter, sour });

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            ModelBakery.registerItemVariants(PokecubeItems.berries, new ResourceLocation("pokecube:" + name + "Berry"));
            registerItemTexture(PokecubeItems.berries, id,
                    new ModelResourceLocation("pokecube:" + name + "Berry", "inventory"));
        }
        PokecubeItems.addSpecificItemStack(name + "berry", new ItemStack(PokecubeItems.berries, 1, id));
        PokecubeItems.addSpecificItemStack(name, new ItemStack(PokecubeItems.berries, 1, id));
        PokecubeItems.addToHoldables(name);
    }

    public static boolean berryEffect(IPokemob pokemob, ItemStack berry)
    {

        byte status = pokemob.getStatus();
        if (!berryNames.containsKey(berry.getItemDamage())) return false;

        String berryName = berryNames.get(berry.getItemDamage());

        if (status == STATUS_PAR && berryName.equalsIgnoreCase("cheri"))
        {
            pokemob.healStatus();
            return true;
        }
        if (status == STATUS_SLP && berryName.equalsIgnoreCase("chesto"))
        {
            pokemob.healStatus();
            return true;
        }
        if ((status == STATUS_PSN || status == STATUS_PSN2) && berryName.equalsIgnoreCase("pecha"))
        {
            pokemob.healStatus();
            return true;
        }
        if (status == STATUS_BRN && berryName.equalsIgnoreCase("rawst"))
        {
            pokemob.healStatus();
            return true;
        }
        if (status == STATUS_FRZ && berryName.equalsIgnoreCase("aspear"))
        {
            pokemob.healStatus();
            return true;
        }
        if (status != STATUS_NON && berryName.equalsIgnoreCase("lum"))
        {
            pokemob.healStatus();
            return true;
        }
        EntityLivingBase entity = (EntityLivingBase) pokemob;
        float HP = entity.getHealth();
        float HPmax = entity.getMaxHealth();
        if (HP < HPmax / 3f)
        {
            if (berryName.equalsIgnoreCase("oran"))
            {
                entity.heal(10);
                return true;
            }
            else if (berryName.equalsIgnoreCase("sitrus"))
            {
                entity.heal(HPmax / 4f);
                return true;
            }
            else if (berryName.equalsIgnoreCase("enigma"))
            {
                entity.heal(HPmax / 4f);
                return true;
            }
        }

        return false;
    }

    public static Block getBerryCrop(String name)
    {
        for (Block berryCrop : berryCrops.values())
        {
            if (berryCrop.getUnlocalizedName().toLowerCase().contains(name.toLowerCase())) return berryCrop;
        }
        return null;
    }

    public static ItemStack getBerryItem(String name)
    {
        return PokecubeItems.getStack(name);
    }
}
