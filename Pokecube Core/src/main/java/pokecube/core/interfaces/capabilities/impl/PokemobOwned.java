package pokecube.core.interfaces.capabilities.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.events.MoveMessageEvent;
import pokecube.core.events.PCEvent;
import pokecube.core.events.RecallEvent;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.TagNames;
import thut.lib.CompatWrapper;

public abstract class PokemobOwned extends PokemobAI implements IInventoryChangedListener
{
    @Override
    public void displayMessageToOwner(ITextComponent message)
    {
        if (!getEntity().isServerWorld())
        {
            Entity owner = this.getPokemonOwner();
            if (owner == PokecubeCore.proxy.getPlayer((String) null))
            {
                GuiInfoMessages.addMessage(message);
            }
        }
        else
        {
            Entity owner = this.getPokemonOwner();
            if (owner instanceof EntityPlayerMP && !getEntity().isDead)
            {
                MoveMessageEvent event = new MoveMessageEvent(this, message);
                MinecraftForge.EVENT_BUS.post(event);
                PacketPokemobMessage.sendMessage((EntityPlayer) owner, getEntity().getEntityId(), event.message);
            }
        }
    }

    @Override
    public String getPokemobTeam()
    {
        if (team.isEmpty())
        {
            team = TeamManager.getTeam(getEntity());
        }
        return team;
    }

    @Override
    public void setPokemobTeam(String team)
    {
        this.team = team;
    }

    @Override
    public BlockPos getHome()
    {
        return homePos;
    }

    @Override
    public float getHomeDistance()
    {
        return homeDistance;
    }

    @Override
    public UUID getOriginalOwnerUUID()
    {
        return OTID;
    }

    @Override
    public AnimalChest getPokemobInventory()
    {
        if (pokeChest == null) initInventory();
        return pokeChest;
    }

    @Override
    public EntityLivingBase getPokemonOwner()
    {
        UUID ownerID = this.getOwnerId();
        if (ownerID == null) return null;
        try
        {
            EntityPlayer o = getEntity().getEntityWorld().getPlayerEntityByUUID(ownerID);
            players = o != null;
            if (o != null) return o;
        }
        catch (Exception e)
        {

        }
        List<Object> entities = null;
        entities = new ArrayList<Object>(getEntity().getEntityWorld().loadedEntityList);
        for (Object o : entities)
        {
            if (o instanceof EntityLivingBase)
            {
                EntityLivingBase e = (EntityLivingBase) o;
                players = o instanceof EntityPlayer;
                if (e.getUniqueID().equals(ownerID)) { return e; }
            }
        }
        return null;
    }

    @Override
    public UUID getPokemonOwnerID()
    {
        return ownerID;
    }

    @Override
    public int getSpecialInfo()
    {
        return dataManager.get(params.SPECIALINFO);
    }

    @Override
    public boolean hasHomeArea()
    {
        return homeDistance > 0;
    }

    protected void initInventory()
    {
        AnimalChest animalchest = this.pokeChest;
        this.pokeChest = new AnimalChest("PokeChest", this.invSize());
        if (animalchest != null)
        {
            animalchest.removeInventoryChangeListener(this);
            int i = Math.min(animalchest.getSizeInventory(), this.pokeChest.getSizeInventory());

            for (int j = 0; j < i; ++j)
            {
                ItemStack itemstack = animalchest.getStackInSlot(j);

                if (itemstack != CompatWrapper.nullStack)
                {
                    this.pokeChest.setInventorySlotContents(j, itemstack.copy());
                }
            }
            animalchest = null;
        }
        this.pokeChest.addInventoryChangeListener(this);
        this.handleArmourAndSaddle();
    }

    private int invSize()
    {
        return 7;
    }

    // 1.11
    public void onInventoryChanged(IInventory inventory)
    {
        handleArmourAndSaddle();
    }

    // 1.10
    public void onInventoryChanged(InventoryBasic inventory)
    {
        handleArmourAndSaddle();
    }

    @Override
    public void returnToPokecube()
    {
        if (returning) return;
        returning = true;
        if (PokecubeCore.isOnClientSide())
        {
            try
            {
                MessageServer packet = new MessageServer(MessageServer.RETURN, getEntity().getEntityId());
                PokecubePacketHandler.sendToServer(packet);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            if (this.getTransformedTo() != null)
            {
                this.setTransformedTo(null);
            }
            RecallEvent pre = new RecallEvent.Pre(this);
            MinecraftForge.EVENT_BUS.post(pre);
            if (pre.isCanceled()) return;
            RecallEvent evtrec = new RecallEvent(this);
            MinecraftForge.EVENT_BUS.post(evtrec);
            if (getEntity().getHealth() > 0 && evtrec.isCanceled()) { return; }

            if (getPokemonAIState(MEGAFORME) || getPokedexEntry().isMega)
            {
                this.setPokemonAIState(MEGAFORME, false);
                IPokemob base = megaEvolve(getPokedexEntry().getBaseForme());
                if (base == this) returning = false;
                if (getEntity().getEntityData().hasKey(TagNames.ABILITY))
                    base.setAbility(AbilityManager.getAbility(getEntity().getEntityData().getString(TagNames.ABILITY)));
                base.returnToPokecube();
                return;
            }

            Entity owner = getPokemonOwner();
            this.setPokemonAIState(IMoveConstants.NOMOVESWAP, false);
            this.setPokemonAIState(IMoveConstants.ANGRY, false);
            getEntity().setAttackTarget(null);
            getEntity().captureDrops = true;
            if (owner instanceof EntityPlayer)
            {
                ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                EntityPlayer player = (EntityPlayer) owner;
                boolean noRoom = false;
                if (player.isDead || player.getHealth() <= 0 || player.inventory.getFirstEmptyStack() == -1)
                {
                    noRoom = true;
                }
                if (noRoom)
                {
                    getEntity().captureDrops = true;
                    ItemTossEvent toss = new ItemTossEvent(getEntity().entityDropItem(itemstack, 0F), player);
                    MinecraftForge.EVENT_BUS.post(toss);
                    noRoom = !toss.isCanceled();
                    if (noRoom)
                    {
                        onToss(player, itemstack);
                    }
                }
                else
                {
                    boolean added = player.inventory.addItemStackToInventory(itemstack);
                    if (!added)
                    {
                        ItemTossEvent toss = new ItemTossEvent(getEntity().entityDropItem(itemstack, 0F), player);
                        MinecraftForge.EVENT_BUS.post(toss);
                        added = toss.isCanceled();
                    }
                }
                if (!owner.isSneaking() && !getEntity().isDead)
                {
                    boolean has = StatsCollector.getCaptured(getPokedexEntry(), player) > 0;
                    has = has || StatsCollector.getHatched(getPokedexEntry(), player) > 0;
                    if (!has)
                    {
                        StatsCollector.addCapture(this);
                    }
                }
                ITextComponent mess = new TextComponentTranslation("pokemob.action.return", getPokemonDisplayName());
                displayMessageToOwner(mess);
            }
            else if (getPokemonOwnerID() != null)
            {
                if (owner == null)
                {
                    ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                    ItemTossEvent toss = new ItemTossEvent(getEntity().entityDropItem(itemstack, 0F),
                            PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                    if (!toss.isCanceled())
                    {
                        onToss(null, itemstack);
                    }
                }
                else
                {
                    ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                    PCEvent event = new PCEvent(itemstack, getPokemonOwner());
                    MinecraftForge.EVENT_BUS.post(event);
                    if (!event.isCanceled())
                    {
                        ItemTossEvent toss = new ItemTossEvent(getEntity().entityDropItem(itemstack, 0F),
                                PokecubeMod.getFakePlayer());
                        MinecraftForge.EVENT_BUS.post(toss);
                        if (!toss.isCanceled())
                        {
                            onToss((EntityLivingBase) owner, itemstack);
                        }
                    }
                }
            }
            getEntity().capturedDrops.clear();
            getEntity().captureDrops = false;
            getEntity().setDead();
        }
    }

    private void onToss(EntityLivingBase owner, ItemStack itemstack)
    {
        EntityPokecube entity = new EntityPokecube(getEntity().getEntityWorld(), owner, itemstack);
        here.set(getEntity());
        here.moveEntity(entity);
        here.clear().setVelocities(entity);
        entity.targetEntity = null;
        entity.targetLocation.clear();
        getEntity().getEntityWorld().spawnEntity(entity);
    }

    @Override
    public void setHeldItem(ItemStack itemStack)
    {
        try
        {
            ItemStack oldStack = getHeldItem();
            this.getPokemobInventory().setInventorySlotContents(1, itemStack);
            getPokedexEntry().onHeldItemChange(oldStack, itemStack, this);
            dataManager.set(params.HELDITEM, itemStack);
        }
        catch (Exception e)
        {
            // Should not happen anymore
            e.printStackTrace();
        }
        getEntity().setHeldItem(EnumHand.MAIN_HAND, itemStack);
    }

    @Override
    public void setHome(int x, int y, int z, int distance)
    {
        homePos = new BlockPos(x, y, z);
        homeDistance = distance;
        if (getEntity() instanceof EntityAnimal) ((EntityAnimal) getEntity()).setHomePosAndDistance(homePos, distance);
    }

    @Override
    public void setHp(float min)
    {
        getEntity().setHealth(min);
    }

    @Override
    public void setOriginalOwnerUUID(UUID original)
    {
        OTID = original;
    }

    @Override
    public void setPokemonOwner(EntityLivingBase e)
    {
        if (e == null)
        {
            setPokemonOwner((UUID) null);
            this.setPokemonAIState(IMoveConstants.TAMED, false);
            return;
        }
        this.setPokemonAIState(IMoveConstants.TAMED, true);
        setPokemonOwner(e.getUniqueID());
        if (getOriginalOwnerUUID() == null)
        {
            setOriginalOwnerUUID(e.getUniqueID());
        }
    }

    @Override
    public void setPokemonOwner(UUID owner)
    {
        ownerID = owner;
        this.team = TeamManager.getTeam(getEntity());
        if (getEntity() instanceof EntityTameable)
        {
            ((EntityTameable) getEntity()).setOwnerId(owner);
        }
    }

    @Override
    public void setSpecialInfo(int info)
    {
        this.dataManager.set(params.SPECIALINFO, Integer.valueOf(info));
    }

    @Override
    public void specificSpawnInit()
    {
        this.setHeldItem(this.wildHeldItem());
        setSpecialInfo(getPokedexEntry().defaultSpecial);
        this.getEntity().setHealth(this.getEntity().getMaxHealth());
    }

    @Override
    public boolean isPlayerOwned()
    {
        return this.getPokemonAIState(IMoveConstants.TAMED) && players;
    }

    @Override
    public LogicMountedControl getController()
    {
        return controller;
    }

    @Override
    public boolean moveToShoulder(EntityPlayer player)
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", this.getEntityString());
        this.getEntity().writeToNBT(nbttagcompound);

        if (player.addShoulderEntity(nbttagcompound))
        {
            this.getEntity().getEntityWorld().removeEntity(this.getEntity());
            return true;
        }
        else
        {
            return false;
        }
    }

    protected final String getEntityString()
    {
        ResourceLocation resourcelocation = EntityList.getKey(this.getEntity());
        return resourcelocation == null ? null : resourcelocation.toString();
    }
}
