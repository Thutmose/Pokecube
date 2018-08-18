package pokecube.core.interfaces.capabilities.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.advancements.CriteriaTriggers;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.events.MoveMessageEvent;
import pokecube.core.events.PCEvent;
import pokecube.core.events.RecallEvent;
import pokecube.core.events.SpawnEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

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
                if (PokecubeCore.debug)
                {
                    PokecubeMod.log(Level.INFO, message.getFormattedText());
                }
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
        if (guardCap.getActiveTask() != null) return guardCap.getActiveTask().getPos();
        return guardCap.getPrimaryTask().getPos();
    }

    @Override
    public float getHomeDistance()
    {
        if (guardCap.getActiveTask() != null) return guardCap.getActiveTask().getRoamDistance();
        return guardCap.getPrimaryTask().getRoamDistance();
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
        int info = dataManager.get(params.SPECIALINFO);
        if (info == -1)
        {
            info = isShiny() ? getPokedexEntry().defaultSpecials : getPokedexEntry().defaultSpecial;
        }
        return info;
    }

    @Override
    public boolean hasHomeArea()
    {
        return guardCap.getPrimaryTask().getRoamDistance() > 0;
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

                if (itemstack != ItemStack.EMPTY)
                {
                    this.pokeChest.setInventorySlotContents(j, itemstack.copy());
                }
            }
            animalchest = null;
        }
        this.pokeChest.addInventoryChangeListener(this);
    }

    private int invSize()
    {
        return 7;
    }

    // 1.11
    public void onInventoryChanged(IInventory inventory)
    {
    }

    // 1.10
    public void onInventoryChanged(InventoryBasic inventory)
    {
    }

    @Override
    public void returnToPokecube()
    {
        if (returning) return;
        returning = true;
        if (!getEntity().isServerWorld())
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
            this.setEvolutionTicks(0);
            this.setGeneralState(GeneralStates.EXITINGCUBE, false);
            this.setGeneralState(GeneralStates.EVOLVING, false);
            if (getCombatState(CombatStates.MEGAFORME) || getPokedexEntry().isMega)
            {
                this.setCombatState(CombatStates.MEGAFORME, false);
                float hp = getEntity().getHealth();
                IPokemob base = megaEvolve(getPokedexEntry().getBaseForme());
                base.getEntity().setHealth(hp);
                if (base == this) returning = false;
                if (getEntity().getEntityData().hasKey(TagNames.ABILITY))
                    base.setAbility(AbilityManager.getAbility(getEntity().getEntityData().getString(TagNames.ABILITY)));
                base.returnToPokecube();
                return;
            }

            if (PokecubeMod.debug) PokecubeMod.log("Recalling " + this.getEntity());

            /** If this has fainted, status should be reset. */
            if (getEntity().getHealth() <= 0)
            {
                healStatus();
                healChanges();
            }

            Entity owner = getPokemonOwner();
            /** If we have a target, and we were recalled with health, assign
             * the target to our owner instead. */
            if (this.getCombatState(CombatStates.ANGRY) && this.getEntity().getAttackTarget() != null
                    && this.getEntity().getHealth() > 0)
            {
                if (owner instanceof EntityLivingBase)
                {
                    IPokemob targetMob = CapabilityPokemob.getPokemobFor(this.getEntity().getAttackTarget());
                    if (targetMob != null)
                    {
                        this.getEntity().isDead = true;
                        targetMob.getEntity().setAttackTarget(getPokemonOwner());
                        targetMob.setCombatState(CombatStates.ANGRY, true);
                        this.getEntity().isDead = false;
                        if (PokecubeMod.debug) PokecubeMod.log("Swapping agro to cowardly owner!");
                    }
                    else
                    {
                        this.getEntity().getAttackTarget().setRevengeTarget(getPokemonOwner());
                    }
                }
            }

            this.setCombatState(CombatStates.NOMOVESWAP, false);
            this.setCombatState(CombatStates.ANGRY, false);
            getEntity().setAttackTarget(null);
            getEntity().captureDrops = true;
            EntityPlayer tosser = PokecubeMod.getFakePlayer(getEntity().getEntityWorld());
            if (owner instanceof EntityPlayer)
            {
                ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                EntityPlayer player = (EntityPlayer) owner;
                boolean noRoom = false;
                boolean ownerDead = player.isDead || player.getHealth() <= 0;
                if (ownerDead || player.inventory.getFirstEmptyStack() == -1)
                {
                    noRoom = true;
                }
                if (noRoom)
                {
                    PCEvent event = new PCEvent(itemstack.copy(), tosser);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (!event.isCanceled())
                    {
                        onToss(tosser, itemstack.copy());
                    }
                }
                else
                {
                    boolean added = player.inventory.addItemStackToInventory(itemstack);
                    if (!added)
                    {
                        PCEvent event = new PCEvent(itemstack.copy(), tosser);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (!event.isCanceled())
                        {
                            onToss(tosser, itemstack.copy());
                        }
                    }
                }
                if (!owner.isSneaking() && !getEntity().isDead && !ownerDead)
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
                ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                if (owner == null)
                {
                    PCEvent event = new PCEvent(itemstack.copy(), tosser);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (!event.isCanceled())
                    {
                        onToss(tosser, itemstack.copy());
                    }
                }
                else
                {
                    PCEvent event = new PCEvent(itemstack.copy(), (EntityLivingBase) owner);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (!event.isCanceled())
                    {
                        onToss((EntityLivingBase) owner, itemstack.copy());
                    }
                }
            }
            getEntity().capturedDrops.clear();
            getEntity().captureDrops = false;

            // Set Dead for deletion
            this.getEntity().setDead();
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
        super.setHeldItem(itemStack);
    }

    @Override
    public void setHome(int x, int y, int z, int distance)
    {
        guardCap.getPrimaryTask().setPos(new BlockPos(x, y, z));
        guardCap.getPrimaryTask().setRoamDistance(distance);
        if (getEntity() instanceof EntityAnimal)
            ((EntityAnimal) getEntity()).setHomePosAndDistance(guardCap.getPrimaryTask().getPos(), distance);
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
            // Clear team
            this.setPokemobTeam("");
            // Clear uuid
            setPokemonOwner((UUID) null);
            /*
             * unset tame.
             */
            this.setGeneralState(GeneralStates.TAMED, false);
            return;
        }
        /*
         * Set it as tame.
         */
        this.setGeneralState(GeneralStates.TAMED, true);
        /*
         * Set not to wander around by default, they can choose to enable this
         * later.
         */
        this.setRoutineState(AIRoutine.WANDER, false);
        /*
         * Set owner, and set original owner if none already exists.
         */
        setPokemonOwner(e.getUniqueID());
        if (getOriginalOwnerUUID() == null)
        {
            setOriginalOwnerUUID(e.getUniqueID());
        }
        /*
         * Trigger vanilla event for taming a mob.
         */
        if (e instanceof EntityPlayerMP && getEntity() instanceof EntityAnimal)
            CriteriaTriggers.TAME_ANIMAL.trigger((EntityPlayerMP) e, (EntityAnimal) getEntity());
    }

    @Override
    public void setPokemonOwner(UUID owner)
    {
        ownerID = owner;
        // Clear team, it will refresh it whenever it is actually checked.
        this.setPokemobTeam("");

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
    public IPokemob specificSpawnInit()
    {
        IPokemob pokemob = this;
        int maxXP = getEntity().getEntityData().getInteger("spawnExp");

        /*
         * Check to see if the mob has spawnExp defined in its data. If not, it
         * will choose how much exp it spawns with based on the position that it
         * spawns in worls with.
         */
        if (maxXP == 0)
        {
            if (!getEntity().getEntityData().getBoolean("initSpawn"))
            {
                pokemob.setHeldItem(pokemob.wildHeldItem(getEntity()));
                if (pokemob instanceof PokemobOwned) ((PokemobOwned) pokemob).updateHealth();
                pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());
                return pokemob;
            }
            getEntity().getEntityData().removeTag("initSpawn");
            Vector3 spawnPoint = Vector3.getNewVector().set(getEntity());
            maxXP = SpawnHandler.getSpawnXp(getEntity().getEntityWorld(), spawnPoint, pokemob.getPokedexEntry());
            SpawnEvent.Level event = new SpawnEvent.Level(pokemob.getPokedexEntry(), spawnPoint,
                    getEntity().getEntityWorld(), Tools.xpToLevel(pokemob.getPokedexEntry().getEvolutionMode(), -1),
                    SpawnHandler.DEFAULT_VARIANCE);
            MinecraftForge.EVENT_BUS.post(event);
            int level = event.getLevel();
            maxXP = Tools.levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), level);
        }
        getEntity().getEntityData().removeTag("spawnExp");

        // Set exp and held items.
        pokemob = pokemob.setForSpawn(maxXP);
        pokemob.setHeldItem(pokemob.wildHeldItem(getEntity()));

        // Make sure heath is valid numbers.
        if (pokemob instanceof PokemobOwned) ((PokemobOwned) pokemob).updateHealth();
        pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());

        // Reset love status to prevent immediate eggs
        this.resetLoveStatus();

        return pokemob;
    }

    @Override
    public boolean isPlayerOwned()
    {
        return this.getGeneralState(GeneralStates.TAMED) && players;
    }

    @Override
    public LogicMountedControl getController()
    {
        return controller;
    }

    @Override
    public boolean moveToShoulder(EntityPlayer player)
    {
        float scale = getSize();
        float width = getPokedexEntry().width * scale;
        float height = getPokedexEntry().height * scale;
        float length = getPokedexEntry().length * scale;
        boolean rightSize = width < 1 && height < 1 && length < 1;
        rightSize |= getPokedexEntry().canSitShoulder;
        if (!rightSize) return false;
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
