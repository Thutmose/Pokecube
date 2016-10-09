package pokecube.core.items.pokecubes;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class EntityPokecube extends EntityPokecubeBase
{
    public static class CollectEntry
    {
        static CollectEntry createFromNBT(NBTTagCompound nbt)
        {
            String player = nbt.getString("player");
            long time = nbt.getLong("time");
            return new CollectEntry(player, time);
        }

        final String player;
        final long   time;

        public CollectEntry(String player, long time)
        {
            this.player = player;
            this.time = time;
        }

        void writeToNBT(NBTTagCompound nbt)
        {
            nbt.setString("player", player);
            nbt.setLong("time", time);
        }
    }

    public static class LootEntry
    {
        final ItemStack loot;
        final int       rolls;

        static LootEntry createFromNBT(NBTTagCompound nbt)
        {
            ItemStack loot = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("loot"));
            return new LootEntry(loot, nbt.getInteger("rolls"));
        }

        public LootEntry(ItemStack loot, int rolls)
        {
            this.loot = loot;
            this.rolls = rolls;
        }

        void writeToNBT(NBTTagCompound nbt)
        {
            NBTTagCompound loot = new NBTTagCompound();
            this.loot.writeToNBT(loot);
            nbt.setTag("loot", loot);
            nbt.setInteger("rolls", rolls);
        }

    }

    public long                    reset      = 0;
    public long                    resetTime  = 0;
    public ArrayList<CollectEntry> players    = Lists.newArrayList();
    public ArrayList<LootEntry>    loot       = Lists.newArrayList();
    public ArrayList<ItemStack>    lootStacks = Lists.newArrayList();

    public EntityPokecube(World world)
    {
        super(world);
        resetTime = 10000;
    }

    public EntityPokecube(World world, EntityLivingBase shootingEntity, ItemStack entityItem)
    {
        this(world);
        if (shootingEntity != null)
        {
            Vector3 start = Vector3.getNewVector().set(shootingEntity, false);
            Vector3 dir = Vector3.getNewVector().set(shootingEntity.getLookVec());
            start.addTo(dir).moveEntity(this);
            setVelocity(speed, dir);
            shooter = shootingEntity.getPersistentID();
        }
        this.setEntityItemStack(entityItem);
        this.shootingEntity = shootingEntity;
        if (PokecubeManager.isFilled(entityItem)) tilt = -2;
    }

    @Override
    protected void collideWithNearbyEntities()
    {
        super.collideWithNearbyEntities();
    }

    /** Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity */
    @Override
    public void applyEntityCollision(Entity e)
    {
        if (e == shootingEntity || isReleasing() || worldObj.isRemote || e instanceof EntityPokecube || e.isDead)
        {
            super.applyEntityCollision(e);
            return;
        }
        if (shootingEntity != null && e instanceof IPokemob
                && ((IPokemob) e).getPokemonOwner() == shootingEntity) { return; }
        if (e instanceof EntityLivingBase && e instanceof IPokemob && ((EntityLivingBase) e).getHealth() > 0
                && tilt == -1)
        {
            captureAttempt(e);
        }
        else if (PokecubeManager.isFilled(getEntityItem()))
        {
            IPokemob entity1 = sendOut();
            if (entity1 != null && shootingEntity != null)
            {
                if (e instanceof EntityLivingBase)
                {
                    EntityLivingBase entityHit = (EntityLivingBase) e;
                    if (entityHit instanceof IPokemob && entity1.getPokemonOwnerID() != null
                            && entity1.getPokemonOwnerID().equals(((IPokemob) entityHit).getPokemonOwnerID()))
                    {
                        // do not attack a mob of the same team.
                    }
                    else
                    {
                        ((EntityCreature) entity1).setAttackTarget(entityHit);
                        entity1.setPokemonAIState(IMoveConstants.SITTING, false);
                        if (entityHit instanceof EntityCreature)
                        {
                            ((EntityCreature) entityHit).setAttackTarget((EntityLiving) entity1);
                        }
                        if (entityHit instanceof IPokemob)
                        {
                            ((IPokemob) entityHit).setPokemonAIState(IMoveConstants.ANGRY, true);
                        }
                    }
                }
            }
        }
        else
        {
            if (e instanceof EntityPlayer)
            {
                this.processInteract((EntityPlayer) e, EnumHand.MAIN_HAND,
                        ((EntityPlayer) e).getHeldItem(EnumHand.MAIN_HAND));
            }
        }
    }

    public Entity getOwner()
    {
        if (PokecubeManager.isFilled(getEntityItem()))
        {
            String name = PokecubeManager.getOwner(getEntityItem());
            if (!name.isEmpty())
            {
                UUID id = UUID.fromString(name);
                EntityPlayer player = worldObj.getPlayerEntityByUUID(id);
                return player;
            }
        }
        return null;
    }

    public Entity copy()
    {
        EntityPokecube copy = new EntityPokecube(worldObj, shootingEntity, getEntityItem());
        copy.posX = this.posX;
        copy.posY = this.posY;
        copy.posZ = this.posZ;
        copy.motionX = this.motionX;
        copy.motionY = this.motionY;
        copy.motionZ = this.motionZ;
        copy.setEntityItemStack(getEntityItem());
        copy.tilt = this.tilt;
        copy.time = this.time;
        return copy;
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityplayer)
    {
        if (isLoot)
        {
            processInteract(entityplayer, EnumHand.MAIN_HAND, entityplayer.getHeldItemMainhand());
        }
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        if (isLoot) motionX = motionZ = 0;
        super.onUpdate();
        if (isLoot) return;
        boolean releasing = isReleasing();

        if (shooter != null && shootingEntity == null)
        {
            shootingEntity = worldObj.getPlayerEntityByUUID(shooter);
        }

        if (releasing)
        {
            motionX = motionY = motionZ = 0;
            this.setDead();
            return;
        }
        if (PokecubeManager.isFilled(getEntityItem())) time--;

        if (time == 0 && tilt >= 4) // Captured the pokemon
        {
            if (captureSucceed())
            {
                CaptureEvent.Post event = new CaptureEvent.Post(this);
                MinecraftForge.EVENT_BUS.post(event);
            }
            setDead();
            return;
        }
        else if (time < 0 && tilt >= 4)
        {
            if (shootingEntity != null)
            {
                Vector3 here = Vector3.getNewVector().set(this);
                Vector3 dir = Vector3.getNewVector().set(shootingEntity);
                double dist = dir.distanceTo(here);
                dir.subtractFrom(here);
                dir.scalarMultBy(1 / (dist));
                dir.setVelocities(this);
            }
        }
        else if (time <= 0 && tilt >= 0) // Missed the pokemon
        {
            captureFailed();
            setDead();
            return;
        }

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D
                    / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, f) * 180.0D / Math.PI);
        }

        BlockPos pos = tilePos == null ? getPosition() : tilePos;
        IBlockState state = worldObj.getBlockState(pos);

        Block block = state.getBlock();
        if (state.getMaterial() != Material.AIR)
        {
            AxisAlignedBB axisalignedbb = state.getBoundingBox(worldObj, pos);

            if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3d(this.posX, this.posY, this.posZ)))
            {
                this.inGround = true;
                tilePos = pos;
            }
            if (state.getMaterial().isLiquid())
            {
                motionY += 0.1;
            }
        }
        if (motionX == motionZ && motionZ == 0)
        {
            this.inGround = true;
        }

        if (this.inGround || tilt >= 0)
        {
            int j = block.getMetaFromState(state);

            if (block == this.tile && j == this.inData)
            {
                ++this.ticksInGround;
            }
            else
            {
                this.inGround = false;
                this.ticksInGround = 0;
            }
            if (tilt < 0 && !(targetEntity == null && targetLocation.isEmpty()))
            {
                if (PokecubeManager.isFilled(getEntityItem()))
                {
                    sendOut();
                }
                else
                {

                }
                return;
            }
        }
        if (tilt > 0 || (targetEntity != null && targetEntity.isDead))
        {
            targetEntity = null;
            if (!targetLocation.equals(Vector3.secondAxisNeg)) targetLocation.clear();
        }

        Vector3 target = Vector3.getNewVector();
        if (targetEntity != null)
        {
            target.set(targetEntity);
            if (target.distToEntity(this) < 2)
            {
                this.collideWithEntity(targetEntity);
            }
        }
        else
        {
            target.set(targetLocation);
        }
        if (!target.isEmpty() && target.y >= 0 && SEEKING)
        {
            Vector3 here = Vector3.getNewVector().set(this);
            Vector3 dir = Vector3.getNewVector().set(target);
            double dist = dir.distanceTo(here);
            if (dist > 1) dist = 1 / dist;
            else dist = 1;
            dir.subtractFrom(here);
            dir.scalarMultBy(dist);
            dir.setVelocities(this);
        }
    }

    @Override
    public void setDead()
    {
        super.setDead();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (!player.getEntityWorld().isRemote)
        {
            if (player.isSneaking() && player.capabilities.isCreativeMode)
            {
                if (stack != null)
                {
                    isLoot = true;
                    addLoot(new LootEntry(stack, 1));
                    return true;
                }
            }
            if (!isReleasing())
            {
                IPokemob pokemob = PokecubeManager.itemToPokemob(getEntityItem(), worldObj);
                if (pokemob != null) sendOut();
                else
                {
                    if (isLoot)
                    {
                        if (cannotCollect(player) || lootStacks.isEmpty()) return false;
                        players.add(new CollectEntry(player.getCachedUniqueIdString(), worldObj.getTotalWorldTime()));
                        PacketPokecube.sendMessage(player, getEntityId(), worldObj.getTotalWorldTime() + resetTime);
                        ItemStack loot = lootStacks.get(new Random().nextInt(lootStacks.size()));
                        EntityItem entityitem = player.dropItem(loot.copy(), false);
                        if (entityitem != null)
                        {
                            entityitem.setNoPickupDelay();
                            entityitem.setOwner(player.getName());
                            entityitem.playSound(POKECUBESOUND, 1, 1);
                        }
                        return true;
                    }

                    EntityItem entityitem = player.dropItem(getEntityItem(), false);
                    if (entityitem != null)
                    {
                        entityitem.setNoPickupDelay();
                        entityitem.setOwner(player.getName());
                    }
                    this.setDead();
                }
            }
        }
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        isLoot = nbt.getBoolean("isLoot");
        if (nbt.hasKey("resetTime")) resetTime = nbt.getLong("resetTime");
        players.clear();
        loot.clear();
        lootStacks.clear();
        if (nbt.hasKey("players", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("players", 10);
            for (int i = 0; i < nbttaglist.tagCount(); i++)
                players.add(CollectEntry.createFromNBT(nbttaglist.getCompoundTagAt(i)));
        }
        if (nbt.hasKey("loot", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("loot", 10);
            for (int i = 0; i < nbttaglist.tagCount(); i++)
                addLoot(LootEntry.createFromNBT(nbttaglist.getCompoundTagAt(i)));
        }
        if (loot.isEmpty()) isLoot = false;
    }

    /** Sets the position and rotation. Only difference from the other one is no
     * bounding on the rotation. Args: posX, posY, posZ, yaw, pitch */
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_,
            float p_70056_8_, int p_70056_9_)
    {
        this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
        this.setRotation(p_70056_7_, p_70056_8_);
    }

    public void setVelocity(double speed, Vector3 dir)
    {
        dir = dir.scalarMult(speed);
        dir.setVelocities(this);
    }

    public void addLoot(LootEntry entry)
    {
        loot.add(entry);
        for (int i = 0; i < entry.rolls; i++)
            lootStacks.add(entry.loot);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setLong("resetTime", resetTime);
        nbt.setBoolean("isLoot", isLoot);
        NBTTagList nbttaglist = new NBTTagList();
        for (CollectEntry entry : players)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            entry.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        if (!players.isEmpty()) nbt.setTag("players", nbttaglist);
        nbttaglist = new NBTTagList();
        for (LootEntry entry : loot)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            entry.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        if (!loot.isEmpty()) nbt.setTag("loot", nbttaglist);
    }

    protected void captureAttempt(Entity e)
    {
        IPokemob hitten = (IPokemob) e;
        if (hitten.getPokemonOwner() == shootingEntity) { return; }
        int tiltBak = tilt;
        CaptureEvent.Pre capturePre = new Pre(hitten, this);
        MinecraftForge.EVENT_BUS.post(capturePre);
        if (capturePre.isCanceled() || capturePre.getResult() == Result.DENY)
        {
            if (tilt != tiltBak)
            {
                if (tilt == 5)
                {
                    time = 10;
                }
                else
                {
                    time = 20 * tilt;
                }
                hitten.setPokecube(getEntityItem());
                setEntityItemStack(PokecubeManager.pokemobToItem(hitten));
                PokecubeManager.setTilt(getEntityItem(), tilt);
                ((Entity) hitten).setDead();
                Vector3 v = Vector3.getNewVector();
                v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
                motionX = 0;
                motionY = 0.1;
                motionZ = 0;
            }
        }
        else
        {
            int n = Tools.computeCatchRate(hitten, PokecubeItems.getCubeId(getEntityItem()));
            tilt = n;

            if (n == 5)
            {
                time = 10;
            }
            else
            {
                time = 20 * n;
            }

            hitten.setPokecube(getEntityItem());
            setEntityItemStack(PokecubeManager.pokemobToItem(hitten));
            PokecubeManager.setTilt(getEntityItem(), n);
            ((Entity) hitten).setDead();
            Vector3 v = Vector3.getNewVector();
            v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
            motionX = 0;
            motionY = 0.1;
            motionZ = 0;
        }
    }

    public boolean cannotCollect(Entity e)
    {
        if (e == null) return false;
        String name = e.getCachedUniqueIdString();
        for (CollectEntry s : players)
        {
            if (s.player.equals(name))
            {
                if (resetTime > 0)
                {
                    long diff = worldObj.getTotalWorldTime() - s.time;
                    if (diff > resetTime)
                    {
                        players.remove(s);
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
