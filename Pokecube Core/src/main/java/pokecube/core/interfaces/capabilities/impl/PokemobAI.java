package pokecube.core.interfaces.capabilities.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public abstract class PokemobAI extends PokemobEvolves
{
    private Map<AIRoutine, Boolean> enabledStates = Maps.newHashMap();

    @Override
    public float getDirectionPitch()
    {
        return dataManager.get(params.DIRECTIONPITCHDW);
    }

    @Override
    public boolean getPokemonAIState(int state)
    {
        return (dataManager.get(params.AIACTIONSTATESDW) & state) != 0;
    }

    @Override
    public EntityAIBase getGuardAI()
    {
        return guardAI;
    }

    @Override
    public ItemStack getPokecube()
    {
        return pokecube;
    }

    @Override
    public int getPokemonUID()
    {
        if (uid == -1) this.uid = PokecubeSerializer.getInstance().getNextID();
        return uid;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getTexture()
    {
        return modifyTexture(null);
    }

    @Override
    public EntityAIBase getUtilityMoveAI()
    {
        return utilMoveAI;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation modifyTexture(ResourceLocation texture)
    {
        String domain = texture == null ? getPokedexEntry().getModId() : texture.getResourceDomain();
        String texName = texture == null ? null : texture.getResourcePath();
        texName = this.getPokedexEntry().getTexture(texName, this.getSexe(), getEntity().ticksExisted);
        texture = new ResourceLocation(domain, texName);
        if (!isShiny()) return texture;
        String args = texName.substring(0, texName.length() - 4);
        return new ResourceLocation(domain, args + "s.png");
    }

    public List<AxisAlignedBB> getTileCollsionBoxes()
    {
        if (this.getEntity().getEntityWorld().isRemote && getEntity().isBeingRidden()
                && (getEntity().getServer() == null || getEntity().getServer().isDedicatedServer())
                && this.getOwner() == PokecubeCore.proxy.getPlayer((String) null))
        {
            Vector3 vec = Vector3.getNewVector();
            Vector3 vec2 = Vector3.getNewVector();
            double x = getPokedexEntry().width * getSize();
            double z = getPokedexEntry().length * getSize();
            double y = getPokedexEntry().height * getSize();
            double v = vec.setToVelocity(getEntity()).mag();
            vec.set(getEntity());
            vec2.set(x + v, y + v, z + v);
            Matrix3 mainBox = new Matrix3();
            Vector3 offset = Vector3.getNewVector();
            mainBox.boxMin().clear();
            mainBox.boxMax().x = x;
            mainBox.boxMax().z = y;
            mainBox.boxMax().y = z;
            offset.set(-mainBox.boxMax().x / 2, 0, -mainBox.boxMax().z / 2);
            double ar = mainBox.boxMax().x / mainBox.boxMax().z;
            if (ar > 2 || ar < 0.5)
                mainBox.set(2, mainBox.rows[2].set(0, 0, (-getEntity().rotationYaw) * Math.PI / 180));
            mainBox.addOffsetTo(offset).addOffsetTo(vec);
            AxisAlignedBB box = mainBox.getBoundingBox();
            AxisAlignedBB box1 = box.expand(2 + x, 2 + y, 2 + z);
            box1 = box1.addCoord(getEntity().motionX, getEntity().motionY, getEntity().motionZ);
            aabbs = mainBox.getCollidingBoxes(box1, getEntity().getEntityWorld(), getEntity().getEntityWorld());
            // Matrix3.mergeAABBs(aabbs, x/2, y/2, z/2);
            Matrix3.expandAABBs(aabbs, box);
            if (box.getAverageEdgeLength() < 3) Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
        }
        return aabbs;
    }

    public void setTileCollsionBoxes(List<AxisAlignedBB> list)
    {
        aabbs = list;
    }

    @Override
    public boolean fits(IBlockAccess world, Vector3 location, @Nullable Vector3 directionFrom)
    {
        Vector3 diffs = Vector3.getNewVector();
        mainBox.boxMin().clear();
        mainBox.boxMax().x = getPokedexEntry().width * getSize();
        mainBox.boxMax().z = getPokedexEntry().length * getSize();
        mainBox.boxMax().y = getPokedexEntry().height * getSize();
        float diff = (float) Math.max(mainBox.boxMax().y, Math.max(mainBox.boxMax().x, mainBox.boxMax().z));
        offset.set(-mainBox.boxMax().x / 2, 0, -mainBox.boxMax().z / 2);
        double ar = mainBox.boxMax().x / mainBox.boxMax().z;
        if (ar > 2 || ar < 0.5) mainBox.set(2, mainBox.rows[2].set(0, 0, (-getEntity().rotationYaw) * Math.PI / 180));
        mainBox.set(2, mainBox.rows[2].set(0, 0, (-getEntity().rotationYaw) * Math.PI / 180));
        Vector3 pos = offset.add(location);
        AxisAlignedBB aabb = mainBox.addOffsetTo(pos).getBoundingBox();
        if (aabb.maxX - aabb.minX > 3)
        {
            double meanX = aabb.minX + (aabb.maxX - aabb.minX) / 2;
            aabb = new AxisAlignedBB(meanX - 3, aabb.minY, aabb.minZ, meanX + 3, aabb.maxY, aabb.maxZ);
        }
        if (aabb.maxZ - aabb.minZ > 3)
        {
            double meanZ = aabb.minZ + (aabb.maxZ - aabb.minZ) / 2;
            aabb = new AxisAlignedBB(aabb.minX, aabb.minY, meanZ - 3, aabb.maxX, aabb.maxY, meanZ + 3);
        }
        if (aabb.maxY - aabb.minY > 3)
        {
            aabb = aabb.setMaxY(aabb.minY + 3);
        }
        aabb = aabb.expandXyz(Math.min(3, diff));
        List<AxisAlignedBB> aabbs = new Matrix3().getCollidingBoxes(aabb, getEntity().getEntityWorld(), world);
        Matrix3.expandAABBs(aabbs, aabb);
        if (aabb.getAverageEdgeLength() < 3) Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
        boolean collides = mainBox.doTileCollision(world, aabbs, Vector3.empty, getEntity(), diffs);
        return !collides;
    }

    @Override
    public void popFromPokecube()
    {
        getEntity().fallDistance = 0;
        getEntity().extinguish();
        this.setPokemonAIState(EVOLVING, false);
        popped = true;
        if (getEntity().getEntityWorld().isRemote) return;
        getEntity().playSound(this.getSound(), 0.25f, 1);
        if (this.isShiny())
        {
            Vector3 particleLoc = Vector3.getNewVector();
            for (int i = 0; i < 20; ++i)
            {
                particleLoc.set(getEntity().posX + rand.nextFloat() * getEntity().width * 2.0F - getEntity().width,
                        getEntity().posY + 0.5D + rand.nextFloat() * getEntity().height,
                        getEntity().posZ + rand.nextFloat() * getEntity().width * 2.0F - getEntity().width);
                PokecubeMod.core.spawnParticle(getEntity().getEntityWorld(),
                        EnumParticleTypes.VILLAGER_HAPPY.getParticleName(), particleLoc, null);
            }
        }
        onGenesChanged();
    }

    @Override
    public void setDirectionPitch(float pitch)
    {
        dataManager.set(params.DIRECTIONPITCHDW, pitch);
    }

    @Override
    public void setPokemonAIState(int state, boolean flag)
    {
        int byte0 = dataManager.get(params.AIACTIONSTATESDW);
        if (state == STAYING && flag)
        {
            here.set(getEntity());
            setHome(here.intX(), here.intY(), here.intZ(), 16);
        }
        if (flag)
        {
            dataManager.set(params.AIACTIONSTATESDW, Integer.valueOf((byte0 | state)));
        }
        else
        {
            dataManager.set(params.AIACTIONSTATESDW, Integer.valueOf((byte0 & -state - 1)));
        }
    }

    @Override
    public void setPokecube(ItemStack pokeballId)
    {
        if (pokeballId != CompatWrapper.nullStack)
        {
            pokeballId = pokeballId.copy();
            CompatWrapper.setStackSize(pokeballId, 1);
            if (pokeballId.hasTagCompound() && pokeballId.getTagCompound().hasKey("Pokemob"))
                pokeballId.getTagCompound().removeTag("Pokemob");
        }
        pokecube = pokeballId;
    }

    @Override
    public boolean isRoutineEnabled(AIRoutine routine)
    {
        if (enabledStates.containsKey(routine)) return enabledStates.get(routine);
        return routine.getDefault();
    }

    @Override
    public void setRoutineState(AIRoutine routine, boolean enabled)
    {
        enabledStates.put(routine, enabled);
    }

}
