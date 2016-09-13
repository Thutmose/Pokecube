/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.events.EvolveEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;

/** @author Manchou */
public abstract class EntityEvolvablePokemob extends EntityDropPokemob
{
    static class EvoTicker
    {
        final World  world;
        final Entity evo;
        final long   evoTime;

        public EvoTicker(World world, long evoTime, Entity evo)
        {
            this.world = world;
            this.evoTime = evoTime;
            this.evo = evo;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void tick(WorldTickEvent evt)
        {
            if (evt.world != world || evt.phase != Phase.END) return;
            if (evt.world.getTotalWorldTime() > evoTime)
            {
                evt.world.spawnEntityInWorld(evo);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    ItemStack      stack     = null;
    public boolean traded    = false;
    String         evolution = "";
    boolean        evolving  = false;

    public EntityEvolvablePokemob(World world)
    {
        super(world);
    }

    @Override
    public void cancelEvolve()
    {
        if (!isEvolving()) return;
        if (worldObj.isRemote)
        {
            MessageServer message = new MessageServer(MessageServer.CANCELEVOLVE, getEntityId());
            PokecubePacketHandler.sendToServer(message);
            return;
        }
        evolving = false;
        setEvolutionTicks(-1);
        this.setPokemonAIState(EVOLVING, false);
        this.displayMessageToOwner(
                new TextComponentTranslation("pokemob.evolution.cancel", this.getPokemonDisplayName()));
    }

    @Override
    public boolean canEvolve(ItemStack itemstack)
    {
        if (itemstack != null && Tools.isSameStack(itemstack, PokecubeItems.getStack("everstone"))) return false;

        if (this.getPokedexEntry().canEvolve() && !PokecubeCore.isOnClientSide())
        {
            if (evolution.isEmpty())
            {
                for (EvolutionData d : getPokedexEntry().getEvolutions())
                {
                    if (d.shouldEvolve(this, itemstack)) { return true; }
                }
            }
            else
            {
                PokedexEntry e = Database.getEntry(evolution);
                if (e != null && Pokedex.getInstance().getEntry(e.getPokedexNb()) != null) { return true; }
            }
        }
        return false;
    }

    @Override
    public IPokemob changeForme(String forme)
    {
        PokedexEntry newEntry = Database.getEntry(forme);
        if (newEntry == getPokedexEntry() && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) return this;
        if (newEntry != null)
        {
            this.forme = newEntry.getName();
            this.setPokedexEntry(newEntry);
            if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) { return this; }
        }
        else
        {
            newEntry = Database.getEntry(forme);
            if (newEntry == getPokedexEntry())
            {
                newEntry = null;
                return this;
            }
            else if (newEntry != null)
            {
                this.setPokedexEntry(newEntry);
                this.forme = newEntry.getName();
            }
            else if (Database.getEntry(getPokedexEntry().getBaseName()) != null)
            {
                newEntry = Database.getEntry(getPokedexEntry().getBaseName()).getForm(forme);
                if (newEntry != null)
                {
                    this.forme = newEntry.getName();
                    this.setPokedexEntry(newEntry);
                }
            }
        }
        if (newEntry != null && worldObj != null && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PacketChangeForme.sendPacketToNear(this, newEntry.getName(), 128);
        }
        return this;
    }

    @Override
    public IPokemob evolve(boolean delayed, boolean init)
    {
        return evolve(delayed, init, this.getHeldItemMainhand());
    }

    @Override
    public IPokemob evolve(boolean delayed, boolean init, ItemStack stack)
    {
        if (init) delayed = false;
        if (this.getPokedexEntry().canEvolve() && !isDead)
        {
            boolean neededItem = false;
            PokedexEntry evol = null;
            EvolutionData data = null;
            if (evolution == null || evolution.isEmpty())
            {
                for (EvolutionData d : getPokedexEntry().getEvolutions())
                {
                    if (d.shouldEvolve(this, stack))
                    {
                        evol = d.evolution;
                        if (!d.shouldEvolve(this, null) && stack == getHeldItemMainhand()) neededItem = true;
                        data = d;
                        break;
                    }
                }
            }
            else
            {
                evol = Database.getEntry(evolution);
            }
            if (evol != null)
            {
                EvolveEvent evt = new EvolveEvent.Pre(this, evol.getName());
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) return null;

                if (delayed)
                {
                    if (stack != null) this.stack = stack.copy();
                    else stack = null;
                    this.setEvolutionTicks(PokecubeMod.core.getConfig().evolutionTicks + 50);
                    this.setEvol(evol.getPokedexNb());
                    this.setPokemonAIState(EVOLVING, true);
                    evolving = true;
                    this.displayMessageToOwner(
                            new TextComponentTranslation("pokemob.evolution.start", this.getPokemonDisplayName()));
                    return this;
                }
                else
                {
                    IPokemob evo = megaEvolve(((EvolveEvent.Pre) evt).forme);
                    if (neededItem)
                    {
                        ((EntityEvolvablePokemob) evo).setHeldItem(null);
                    }

                    if (init) evo.specificSpawnInit();

                    if (evo != null)
                    {
                        Entity owner = evo.getPokemonOwner();
                        evt = new EvolveEvent.Post(evo);
                        MinecraftForge.EVENT_BUS.post(evt);

                        EntityPlayer player = null;
                        if (owner instanceof EntityPlayer) player = (EntityPlayer) owner;
                        if (delayed) ((EntityMovesPokemob) evo).oldLevel = evo.getLevel() - 1;
                        else((EntityMovesPokemob) evo).oldLevel = data.level - 1;
                        evo.levelUp(evo.getLevel());
                        this.setDead();
                        if (player != null && !player.getEntityWorld().isRemote && !isShadow())
                        {
                            makeShedinja(evo, player);
                        }
                    }
                    return evo;
                }
            }
        }
        return null;
    }

    public int getEvolNumber()
    {
        return dataManager.get(EVOLNBDW);
    }

    /** @return the evolutionTicks */
    @Override
    public int getEvolutionTicks()
    {
        return dataManager.get(EVOLTICKDW);
    }

    @Override
    public boolean isEvolving()
    {
        return evolving || this.getPokemonAIState(EVOLVING);
    }

    /** Returns whether the entity is in a server world */
    @Override
    public boolean isServerWorld()
    {
        return worldObj != null && super.isServerWorld();
    }

    void makeShedinja(IPokemob evo, EntityPlayer player)
    {
        if (evo.getPokedexEntry() == Database.getEntry("ninjask"))
        {
            InventoryPlayer inv = player.inventory;
            boolean hasCube = false;
            boolean hasSpace = false;
            ItemStack cube = null;
            int m = -1;
            for (int n = 0; n < inv.getSizeInventory(); n++)
            {
                ItemStack item = inv.getStackInSlot(n);
                if (item == null) hasSpace = true;
                if (!hasCube && PokecubeItems.getCubeId(item) >= 0 && !PokecubeManager.isFilled(item))
                {
                    hasCube = true;
                    cube = item;
                    m = n;
                }
                if (hasCube && hasSpace) break;

            }
            if (hasCube && hasSpace)
            {
                int cubeId = PokecubeItems.getCubeId(cube);
                Entity pokemon = PokecubeMod.core.createEntityByPokedexNb(Database.getEntry("shedinja").getPokedexNb(),
                        worldObj);
                if (pokemon != null)
                {
                    IPokemob poke = (IPokemob) pokemon;
                    poke.setPokecubeId(cubeId);
                    poke.setPokemonOwner(player);
                    poke.setExp(Tools.levelToXp(poke.getExperienceMode(), 20), true, false);
                    ((EntityLivingBase) poke).setHealth(((EntityLivingBase) poke).getMaxHealth());
                    ItemStack shedinja = PokecubeManager.pokemobToItem(poke);
                    player.addStat(PokecubeMod.get1stPokemob, 0);
                    player.addStat(PokecubeMod.catchAchievements.get(poke.getPokedexEntry()), 1);
                    cube.stackSize--;
                    if (cube.stackSize <= 0) inv.setInventorySlotContents(m, null);
                    inv.addItemStackToInventory(shedinja);
                }
            }
        }
    }

    @Override
    public IPokemob megaEvolve(String forme)
    {
        PokedexEntry newEntry = Database.getEntry(forme);
        if (newEntry == getPokedexEntry()) return this;
        Entity evolution = this;
        if (newEntry == null)
        {
            newEntry = Database.getEntry(forme);
        }
        if (newEntry != null)
        {
            setPokemonAIState(EVOLVING, true);
            if (newEntry.getPokedexNb() != getPokedexNb())
            {
                evolution = PokecubeMod.core.createEntityByPokedexNb(newEntry.getPokedexNb(), worldObj);

                if (evolution == null)
                {
                    System.err.println("No Entry for " + newEntry);
                    return this;
                }
                if (this.getPokemonNickname().equals(this.getPokedexEntry().getName())) this.setPokemonNickname("");
                evolution.copyDataFromOld(this);
                evolution.copyLocationAndAnglesFrom(this);
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.evolve.success", "green",
                        this.getPokemonDisplayName().getFormattedText(),
                        ((IPokemob) evolution).getPokedexEntry().getName());
                this.displayMessageToOwner(mess);
                this.setPokemonOwner(null);
                this.setDead();
                worldObj.removeEntity(this);
                ((IPokemob) evolution).changeForme(forme);
                ((IPokemob) evolution).setAbility(newEntry.getAbility(abilityIndex, ((IPokemob) evolution)));
                long evoTime = worldObj.getTotalWorldTime() + 2;
                if (this.addedToChunk)
                {
                    new EvoTicker(worldObj, evoTime, evolution);
                }
                ((IPokemob) evolution).setPokemonAIState(EVOLVING, true);
                if (getPokemonAIState(MEGAFORME))
                {
                    ((IPokemob) evolution).setPokemonAIState(MEGAFORME, true);
                    ((IPokemob) evolution).setEvolutionTicks(50);
                }
            }
            else
            {
                evolution = this;
                ((IPokemob) evolution).changeForme(forme);
                ((IPokemob) evolution).setAbility(newEntry.getAbility(abilityIndex, ((IPokemob) evolution)));
                ((IPokemob) evolution).setPokemonAIState(EVOLVING, true);
                if (getPokemonAIState(MEGAFORME))
                {
                    ((IPokemob) evolution).setPokemonAIState(MEGAFORME, true);
                    ((IPokemob) evolution).setEvolutionTicks(10);
                }
            }
        }
        return (IPokemob) evolution;
    }

    @Override
    public void onLivingUpdate()
    {
        if (this.ticksExisted > 100) forceSpawn = false;

        int num = getEvolutionTicks();
        if (num > 0)
        {
            setEvolutionTicks(getEvolutionTicks() - 1);
        }
        if (num <= 0 && this.getPokemonAIState(EVOLVING))
        {
            this.setPokemonAIState(EVOLVING, false);
        }
        if (num <= 50 && evolving)
        {
            this.evolve(false, false, stack);
            this.setPokemonAIState(EVOLVING, false);
        }
        if (PokecubeSerializer.getInstance().getPokemob(getPokemonUID()) == null)
            PokecubeSerializer.getInstance().addPokemob(this);
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (getHeldItemMainhand() != null
                && Tools.isSameStack(getHeldItemMainhand(), PokecubeItems.getStack("everstone")))
        {
            traded = false;
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        setTraded(nbttagcompound.getBoolean("traded"));
    }

    private void setEvol(int num)
    {
        dataManager.set(EVOLNBDW, Integer.valueOf(num));
    }

    @Override
    /** These have been deprecated, now override evolve() to make a custom
     * evolution occur.
     * 
     * @param evolution */
    public void setEvolution(String evolution)
    {
        this.evolution = evolution;
    }

    /** @param evolutionTicks
     *            the evolutionTicks to set */
    @Override
    public void setEvolutionTicks(int evolutionTicks)
    {
        dataManager.set(EVOLTICKDW, new Integer(evolutionTicks));
    }

    @Override
    public void setTraded(boolean trade)
    {
        traded = trade;
        setPokemonAIState(TRADED, trade);
    }

    public void showEvolutionFX(String effect)
    {
        String effectToUse = effect;
        if (effectToUse == null || "".equals(effectToUse))
        {
            effectToUse = "reddust";
        }

        if (effectToUse.startsWith("tilecrack_"))
        {
            String[] split = effectToUse.split("_");
            if (split.length == 1) effectToUse = "reddust";
            if (split.length == 2) effectToUse += "_2";
        }

        if (rand.nextInt(100) > 30)
        {
            // //TODO Evolution Particles If needed
        }
    }

    @Override
    public boolean traded()
    {
        return getPokemonAIState(TRADED);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setBoolean("traded", traded);
    }
}
