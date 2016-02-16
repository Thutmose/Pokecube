package pokecube.compat.thaumcraft;

import static pokecube.core.PokecubeItems.register;
import static pokecube.core.PokecubeItems.registerItemTexture;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubes;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.compat.CompatPokecubes;
import pokecube.core.PokecubeItems;
import pokecube.core.events.CaptureEvent.Post;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class ThaumiumPokecube// extends Mod_Pokecube_Helper
{
    public void addThaumiumPokecube()
    {
        Pokecube thaumiumpokecube = new CompatPokecubes();
        thaumiumpokecube.setUnlocalizedName("thaumiumpokecube").setCreativeTab(creativeTabPokecubes);
        register(thaumiumpokecube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) registerItemTexture(thaumiumpokecube, 0,
                new ModelResourceLocation("pokecube_compat:thaumiumpokecube", "inventory"));

        PokecubeItems.addCube(98, new Object[] { thaumiumpokecube });

        PokecubeBehavior thaumic = new PokecubeBehavior()
        {
            @Override
            public void onPreCapture(Pre evt)
            {
                EntityPokecube cube = (EntityPokecube) evt.pokecube;
                String tag = "";
                if (cube.getEntityItem().hasTagCompound())
                {
                    tag = cube.getEntityItem().getTagCompound().getString("aspect");
                }
                int m = matches(evt.caught, tag);
                double rate = m;
                cube.tilt = Tools.computeCatchRate(evt.caught, rate);
                evt.setCanceled(true);
            }

            @Override
            public void onPostCapture(Post evt)
            {

            }
        };
        PokecubeBehavior.addCubeBehavior(98, thaumic);
    }

    static int matches(IPokemob mob, String aspect)
    {
        AspectList list1 = ThaumcraftCompat.pokeTypeToAspects.get(mob.getType1());
        AspectList list2 = ThaumcraftCompat.pokeTypeToAspects.get(mob.getType2());
        int ret = has(list1, aspect) ? 3 : 0;
        if (mob.getType1() != mob.getType2() && mob.getType2() != PokeType.unknown ) ret += has(list2, aspect) ? 3 : 0;
        return ret;
    }

    static boolean has(AspectList list, String aspect)
    {
        for (Aspect a : list.aspects.keySet())
        {
            if (a.getName().equalsIgnoreCase(aspect)) return true;
        }
        return false;
    }
}
