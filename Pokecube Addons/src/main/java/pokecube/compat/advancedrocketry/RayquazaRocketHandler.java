package pokecube.compat.advancedrocketry;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import pokecube.compat.advancedrocketry.AdvancedRocketryCompat.TeleporterNoPortal;
import pokecube.compat.advancedrocketry.AdvancedRocketryCompat.TransitionEntity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;

public class RayquazaRocketHandler
{

    public static void addDelayedTransition(long time, Entity entity, int dim, BlockPos location, Entity entity2)
    {
        try
        {
            TransitionEntity trans = new TransitionEntity(time, entity, dim, location, entity2);
            AdvancedRocketryCompat.transitionMap.put(time, trans);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    final EntityPokemob rayquaza;

    public RayquazaRocketHandler(EntityPokemob pokemob)
    {
        this.rayquaza = pokemob;
    }

    public Entity changeDimension(int newDimId)
    {
        if (rayquaza.dimension == newDimId) return rayquaza;
        return changeDimension(newDimId, rayquaza.posX, 295, rayquaza.posZ);
    }

    @Nullable
    public Entity changeDimension(int dimensionIn, double posX, double y, double posZ)
    {
        if (rayquaza.dimension == dimensionIn) return rayquaza;
        if (!rayquaza.worldObj.isRemote && !rayquaza.isDead)
        {
            List<Entity> passengers = rayquaza.getPassengers();

            if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(rayquaza, dimensionIn)) return null;
            rayquaza.worldObj.theProfiler.startSection("changeDimension");
            MinecraftServer minecraftserver = rayquaza.getServer();
            int i = rayquaza.dimension;
            WorldServer worldserver = minecraftserver.worldServerForDimension(i);
            WorldServer worldserver1 = minecraftserver.worldServerForDimension(dimensionIn);
            rayquaza.dimension = dimensionIn;

            if (i == 1 && dimensionIn == 1)
            {
                worldserver1 = minecraftserver.worldServerForDimension(0);
                rayquaza.dimension = 0;
            }
            UUID owner = rayquaza.getOwnerId();
            PokedexEntry entry = rayquaza.getPokedexEntry();
            rayquaza.setPokemonOwner((UUID) null);
            rayquaza.worldObj.removeEntity(rayquaza);
            rayquaza.setPokemonOwner(owner);
            rayquaza.megaEvolve(entry);
            rayquaza.isDead = false;
            rayquaza.worldObj.theProfiler.startSection("reposition");

            double d0 = rayquaza.posX;
            double d1 = rayquaza.posZ;
            d0 = MathHelper.clamp_double(d0 * 8.0D, worldserver1.getWorldBorder().minX() + 16.0D,
                    worldserver1.getWorldBorder().maxX() - 16.0D);
            d1 = MathHelper.clamp_double(d1 * 8.0D, worldserver1.getWorldBorder().minZ() + 16.0D,
                    worldserver1.getWorldBorder().maxZ() - 16.0D);
            d0 = (double) MathHelper.clamp_int((int) d0, -29999872, 29999872);
            d1 = (double) MathHelper.clamp_int((int) d1, -29999872, 29999872);
            float f = rayquaza.rotationYaw;
            rayquaza.setLocationAndAngles(d0, rayquaza.posY, d1, 90.0F, 0.0F);
            Teleporter teleporter = new TeleporterNoPortal(worldserver1);
            teleporter.placeInExistingPortal(rayquaza, f);
            worldserver.updateEntityWithOptionalForce(rayquaza, false);
            rayquaza.worldObj.theProfiler.endStartSection("reloading");
            Entity entity = EntityList.createEntityByName(EntityList.getEntityString(rayquaza), worldserver1);
            if (entity != null)
            {
                rayquaza.moveToBlockPosAndAngles(new BlockPos(posX, y, posZ), entity.rotationYaw, entity.rotationPitch);
                ((EntityPokemob) entity).copyDataFromOld(rayquaza);
                entity.forceSpawn = true;
                worldserver1.spawnEntityInWorld(entity);
                worldserver1.updateEntityWithOptionalForce(entity, true);
                for (Entity e : passengers)
                {
                    // Fix that darn random crash?
                    worldserver.resetUpdateEntityTick();
                    worldserver1.resetUpdateEntityTick();
                    // Transfer the player if applicable
                    // Need to handle our own removal to avoid race condition
                    // where player is mounted on client on the old entity but
                    // is already mounted to the new one on server
                    // PacketHandler.sendToPlayer(new PacketEntity(rayquaza,
                    // (byte)PacketType.DISMOUNTCLIENT.ordinal()),
                    // (EntityPlayer) e);
                    BlockPos pos = new BlockPos(posX + 16, y, posZ);
                    addDelayedTransition(worldserver.getTotalWorldTime(), e, dimensionIn, pos, entity);
                }
            }
            rayquaza.isDead = true;
            rayquaza.worldObj.theProfiler.endSection();
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            rayquaza.worldObj.theProfiler.endSection();
            return entity;
        }
        else
        {
            return null;
        }
    }
}
