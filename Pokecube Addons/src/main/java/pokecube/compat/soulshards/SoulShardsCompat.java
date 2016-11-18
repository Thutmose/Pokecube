package pokecube.compat.soulshards;

import com.whammich.sstow.api.IEntityHandler;
import com.whammich.sstow.api.SoulShardsAPI;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class SoulShardsCompat implements IEntityHandler
{
    @Optional.Method(modid = "soulshardstow")
    @CompatClass(phase = Phase.POST)
    public static void doSoulShardsCompat()
    {
        System.out.println("SoulShards Compat");
        new pokecube.compat.soulshards.SoulShardsCompat();
    }

    public SoulShardsCompat()
    {
        SoulShardsAPI.registerEntityHandler(EntityPokemob.class, this);
    }

    @Override
    public ActionResult<? extends EntityLiving> handleLiving(World world, String entityName, BlockPos pos)
    {
        EntityLiving living = (EntityLiving) EntityList.createEntityByName(entityName, world);
        if (living == null) return null;
        living.setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(),
                MathHelper.wrapDegrees(world.rand.nextFloat() * 360F), 0F);
        if (living instanceof IPokemob)
        {
            ((IPokemob) living).specificSpawnInit();
            ((IPokemob) living).setForSpawn(SpawnHandler.getSpawnXp(world, Vector3.getNewVector().set(pos),
                    ((IPokemob) living).getPokedexEntry()));
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, living);
    }

}
