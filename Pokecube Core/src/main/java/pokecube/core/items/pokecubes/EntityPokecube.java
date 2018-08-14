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
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

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
            ItemStack loot = new ItemStack(nbt.getCompoundTag("loot"));
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
        this.setItem(entityItem);
        this.shootingEntity = shootingEntity;
        if (PokecubeManager.hasMob(entityItem)) tilt = -2;
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
        if (e == shootingEntity || isReleasing() || getEntityWorld().isRemote || e instanceof EntityPokecube
                || e.isDead)
        {
            super.applyEntityCollision(e);
            return;
        }
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
        if (shootingEntity != null && pokemob != null && pokemob.getPokemonOwner() == shootingEntity) { return; }
        if (e instanceof EntityLiving && pokemob != null && ((EntityLiving) e).getHealth() > 0 && tilt == -1)
        {
            captureAttempt(e);
        }
        else if (PokecubeManager.isFilled(getItem()))
        {
            IPokemob entity1 = CapabilityPokemob.getPokemobFor(sendOut());
            if (entity1 != null && shootingEntity != null)
            {
                if (e instanceof EntityLivingBase)
                {
                    EntityLivingBase entityHit = (EntityLivingBase) e;
                    if (pokemob != null && entity1.getPokemonOwnerID() != null
                            && entity1.getPokemonOwnerID().equals(pokemob.getPokemonOwnerID()))
                    {
                        // do not attack a mob of the same team.
                    }
                    else
                    {
                        entity1.getEntity().setAttackTarget(entityHit);
                        entity1.setLogicState(LogicStates.SITTING, false);
                        if (entityHit instanceof EntityCreature)
                        {
                            ((EntityCreature) entityHit).setAttackTarget(entity1.getEntity());
                        }
                        if (pokemob != null)
                        {
                            pokemob.setCombatState(CombatStates.ANGRY, true);
                        }
                    }
                }
            }
        }
        else if (tilt == -1 && e instanceof EntityLiving && getItem().getItem() instanceof IPokecube)
        {
            IPokecube cube = (IPokecube) getItem().getItem();
            if (cube.canCapture((EntityLiving) e, getItem()))
            {
                captureAttempt(e);
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
        if (PokecubeManager.isFilled(getItem()))
        {
            String name = PokecubeManager.getOwner(getItem());
            if (!name.isEmpty())
            {
                UUID id = UUID.fromString(name);
                EntityPlayer player = getEntityWorld().getPlayerEntityByUUID(id);
                return player;
            }
        }
        return null;
    }

    public Entity copy()
    {
        EntityPokecube copy = new EntityPokecube(getEntityWorld(), shootingEntity, getItem());
        copy.posX = this.posX;
        copy.posY = this.posY;
        copy.posZ = this.posZ;
        copy.motionX = this.motionX;
        copy.motionY = this.motionY;
        copy.motionZ = this.motionZ;
        copy.setItem(getItem());
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
        this.renderYawOffset = 0;
        boolean releasing = isReleasing();

        if (shooter != null && shootingEntity == null)
        {
            shootingEntity = getEntityWorld().getPlayerEntityByUUID(shooter);
        }

        if (releasing)
        {
            Entity mob = getReleased();
            if (mob != null)
            {
                Vector3 diff = Vector3.getNewVector().set(mob).subtractFrom(v0.set(this));

                if (diff.magSq() < 4)
                {
                    diff.norm().reverse().scalarMultBy(0.25 * diff.magSq() / 4d);
                    motionX = diff.x;
                    motionY = diff.y;
                    motionZ = diff.z;
                }
                else
                {
                    motionX = motionY = motionZ = 0;
                }

                IPokemob released = CapabilityPokemob.getPokemobFor(mob);
                diff.set(mob);
                this.getLookHelper().setLookPosition(diff.x, diff.y, diff.z, 360, 0);
                this.getLookHelper().onUpdateLook();
                this.rotationYaw = -this.rotationYawHead;
                if (released == null || mob.isDead || !released.getGeneralState(GeneralStates.EXITINGCUBE))
                    this.setDead();
            }
            else this.setDead();
            return;
        }

        this.getLookHelper().setLookPosition(posX + motionX, posY + motionY, posZ + motionZ, 360, 0);
        this.getLookHelper().onUpdateLook();
        this.rotationYaw = -this.rotationYawHead;

        if (PokecubeManager.isFilled(getItem())
                || (getItem().hasTagCompound() && getItem().getTagCompound().hasKey(TagNames.MOBID)))
            time--;

        if (time == 0 && tilt >= 4) // Captured the pokemon
        {
            if (captureSucceed())
            {
                if (PokecubeManager.isFilled(getItem()))
                {
                    CaptureEvent.Post event = new CaptureEvent.Post(this);
                    MinecraftForge.EVENT_BUS.post(event);
                }
                else if (shootingEntity != null && shootingEntity instanceof EntityPlayerMP)
                {
                    if (shootingEntity instanceof FakePlayer)
                    {
                        entityDropItem(getItem(), 0.5f);
                    }
                    else Tools.giveItem((EntityPlayer) shootingEntity, getItem());
                }
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
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D
                    / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, f) * 180.0D / Math.PI);
        }

        BlockPos pos = tilePos == null ? getPosition() : tilePos;
        IBlockState state = getEntityWorld().getBlockState(pos);

        Block block = state.getBlock();
        if (state.getMaterial() != Material.AIR)
        {
            AxisAlignedBB axisalignedbb = state.getBoundingBox(getEntityWorld(), pos);

            if (axisalignedbb != null && axisalignedbb.contains(new Vec3d(this.posX, this.posY, this.posZ)))
            {// contains in 1.12
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
                if (PokecubeManager.hasMob(getItem()))
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
            if (targetEntity.getDistanceSq(this) < 4)
            {
                this.applyEntityCollision(targetEntity);
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
            if (targetEntity != null)
            {
                dir.x += targetEntity.motionX;
                dir.y += targetEntity.motionY;
                dir.z += targetEntity.motionZ;
            }
            double dist = dir.distanceTo(here) / 2;
            if (dist > 1) dist = 1;
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

    // 1.11
    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        return processInteract(player, hand, player.getHeldItem(hand));
    }

    // 1.10
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
                if (PokecubeManager.isFilled(getItem())
                        || (getItem().hasTagCompound() && (getItem().getTagCompound()).hasKey(TagNames.MOBID)))
                {
                    sendOut();
                }
                else
                {
                    if (isLoot)
                    {
                        if (cannotCollect(player)) return false;
                        players.add(new CollectEntry(player.getCachedUniqueIdString(),
                                getEntityWorld().getTotalWorldTime()));
                        ItemStack loot = ItemStack.EMPTY;

                        if (!lootStacks.isEmpty())
                        {
                            loot = lootStacks.get(new Random().nextInt(lootStacks.size()));
                            if (CompatWrapper.isValid(loot))
                            {
                                PacketPokecube.sendMessage(player, getEntityId(),
                                        getEntityWorld().getTotalWorldTime() + resetTime);
                                Tools.giveItem(player, loot.copy());
                            }
                        }
                        else if (lootTable != null)
                        {
                            LootTable loottable = getEntityWorld().getLootTableManager()
                                    .getLootTableFromLocation(lootTable);
                            LootContext.Builder lootcontext$builder = (new LootContext.Builder(
                                    (WorldServer) getEntityWorld())).withLootedEntity(this);
                            for (ItemStack itemstack : loottable.generateLootForPools(getRNG(),
                                    lootcontext$builder.build()))
                            {
                                if (CompatWrapper.isValid(itemstack))
                                {
                                    Tools.giveItem(player, itemstack.copy());
                                }
                            }
                            PacketPokecube.sendMessage(player, getEntityId(),
                                    getEntityWorld().getTotalWorldTime() + resetTime);
                        }
                        return true;
                    }
                    Tools.giveItem(player, getItem());
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
        setReleasing(nbt.getBoolean("releasing"));
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
        String lootTable = nbt.getString("lootTable");
        if (!lootTable.isEmpty())
        {
            this.lootTable = new ResourceLocation(lootTable);
        }
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
        if (isReleasing())
        {
            nbt.setBoolean("releasing", true);
        }
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
        if (lootTable != null) nbt.setString("lootTable", lootTable.toString());
        else nbt.setString("lootTable", "");
    }

    protected void captureAttempt(Entity e)
    {
        IPokemob hitten = CapabilityPokemob.getPokemobFor(e);
        if (hitten != null)
        {
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
                    hitten.setPokecube(getItem());
                    setItem(PokecubeManager.pokemobToItem(hitten));
                    PokecubeManager.setTilt(getItem(), tilt);
                    hitten.getEntity().setDead();
                    Vector3 v = Vector3.getNewVector();
                    v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
                    motionX = 0;
                    motionY = 0.1;
                    motionZ = 0;
                }
            }
            else
            {
                int n = Tools.computeCatchRate(hitten, PokecubeItems.getCubeId(getItem()));
                tilt = n;

                if (n == 5)
                {
                    time = 10;
                }
                else
                {
                    time = 20 * n;
                }

                hitten.setPokecube(getItem());
                setItem(PokecubeManager.pokemobToItem(hitten));
                PokecubeManager.setTilt(getItem(), n);
                hitten.getEntity().setDead();
                Vector3 v = Vector3.getNewVector();
                v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
                motionX = 0;
                motionY = 0.1;
                motionZ = 0;
            }
        }
        else if (e instanceof EntityLiving && getItem().getItem() instanceof IPokecube)
        {
            IPokecube cube = (IPokecube) getItem().getItem();
            EntityLiving mob = (EntityLiving) e;
            int n = 0;
            rate:
            {
                int catchRate = 250;// TODO configs for this?
                double cubeBonus = cube.getCaptureModifier(mob, PokecubeItems.getCubeId(getItem()));
                double statusbonus = 1;// TODO statuses for mobs?
                double a = Tools.getCatchRate(mob.getMaxHealth(), mob.getHealth(), catchRate, cubeBonus, statusbonus);
                if (a > 255)
                {
                    n = 5;
                    break rate;
                }
                double b = 1048560 / Math.sqrt(Math.sqrt(16711680 / a));

                if (rand.nextInt(65535) <= b)
                {
                    n++;
                }

                if (rand.nextInt(65535) <= b)
                {
                    n++;
                }

                if (rand.nextInt(65535) <= b)
                {
                    n++;
                }

                if (rand.nextInt(65535) <= b)
                {
                    n++;
                }
            }
            tilt = n;

            if (n == 5)
            {
                time = 10;
            }
            else
            {
                time = 20 * n;
            }
            ItemStack mobStack = getItem().copy();
            if (!mobStack.hasTagCompound()) mobStack.setTagCompound(new NBTTagCompound());
            String id = EntityList.getKey(mob).toString();
            mobStack.getTagCompound().setString(TagNames.MOBID, id);
            NBTTagCompound mobTag = new NBTTagCompound();
            mob.writeToNBT(mobTag);
            mobStack.getTagCompound().setTag(TagNames.OTHERMOB, mobTag);
            setItem(mobStack);
            PokecubeManager.setTilt(getItem(), n);
            mob.setDead();
            Vector3 v = Vector3.getNewVector();
            v.set(this).addTo(0, mob.height / 2, 0).moveEntity(this);
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
                    long diff = getEntityWorld().getTotalWorldTime() - s.time;
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