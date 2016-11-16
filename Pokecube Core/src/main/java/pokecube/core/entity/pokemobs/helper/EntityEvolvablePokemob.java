/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.UUID;

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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.EvolveEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.CompatWrapper;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;

/** @author Manchou */
public abstract class EntityEvolvablePokemob extends EntityDropPokemob
{
    static class EvoTicker
    {
        final World          world;
        final Entity         evo;
        final ITextComponent pre;
        final long           evoTime;

        public EvoTicker(World world, long evoTime, Entity evo, ITextComponent pre)
        {
            this.world = world;
            this.evoTime = evoTime;
            this.evo = evo;
            this.pre = pre;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void tick(WorldTickEvent evt)
        {
            if (evt.world != world || evt.phase != Phase.END) return;
            if (evt.world.getTotalWorldTime() > evoTime)
            {
                evt.world.spawnEntityInWorld(evo);
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.evolve.success", "green",
                        pre.getFormattedText(), ((IPokemob) evo).getPokedexEntry().getName());
                ((IPokemob) evo).displayMessageToOwner(mess);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    ItemStack stack     = CompatWrapper.nullStack;
    String    evolution = "";
    boolean   evolving  = false;

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
        if (itemstack != CompatWrapper.nullStack && Tools.isSameStack(itemstack, PokecubeItems.getStack("everstone"))) return false;

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
    public IPokemob evolve(boolean delayed, boolean init)
    {
        return evolve(delayed, init, this.getHeldItemMainhand());
    }

    @Override
    public IPokemob evolve(boolean delayed, boolean init, ItemStack stack)
    {
        // If Init, then don't bother about getting ready for animations and
        // such, just evolve directly.
        if (init)
        {
            boolean neededItem = false;
            PokedexEntry evol = null;
            EvolutionData data = null;
            // Find which evolution to use.
            for (EvolutionData d : this.getPokedexEntry().getEvolutions())
            {
                if (d.shouldEvolve(this, stack))
                {
                    evol = d.evolution;
                    if (!d.shouldEvolve(this, CompatWrapper.nullStack)) neededItem = true;
                    data = d;
                    break;
                }
            }
            if (evol != null)
            {
                // Send evolve event.
                EvolveEvent evt = new EvolveEvent.Pre(this, evol);
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) return null;
                // change to new forme.
                IPokemob evo = this.megaEvolve(((EvolveEvent.Pre) evt).forme);
                // Remove held item if it had one.
                if (neededItem)
                {
                    ((EntityEvolvablePokemob) evo).setHeldItem(CompatWrapper.nullStack);
                }
                // Init things like moves.
                evo.specificSpawnInit();
                ((EntityMovesPokemob) evo).oldLevel = data.level - 1;
                evo.levelUp(evo.getLevel());
                // Send post evolve event.
                evt = new EvolveEvent.Post(evo);
                MinecraftForge.EVENT_BUS.post(evt);
                ((Entity) this).setDead();
                return evo;
            }
            return null;
        }
        // Do not evolve if it is dead, or can't evolve.
        else if (this.getPokedexEntry().canEvolve() && !isDead)
        {
            boolean neededItem = false;
            PokedexEntry evol = null;
            EvolutionData data = null;
            // If it has a pre-defined evolution, use that instead, otherwise
            // look for evolution data to use.
            if (evolution == null || evolution.isEmpty())
            {
                for (EvolutionData d : getPokedexEntry().getEvolutions())
                {
                    if (d.shouldEvolve(this, stack))
                    {
                        evol = d.evolution;
                        if (!d.shouldEvolve(this, CompatWrapper.nullStack) && stack == getHeldItemMainhand()) neededItem = true;
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
                EvolveEvent evt = new EvolveEvent.Pre(this, evol);
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) return null;
                if (delayed)
                {
                    // If delayed, set the pokemob as starting to evolve, and
                    // set the evolution for display effects.
                    if (stack != CompatWrapper.nullStack) this.stack = stack.copy();
                    this.setEvolutionTicks(PokecubeMod.core.getConfig().evolutionTicks + 50);
                    this.setEvol(evol.getPokedexNb());
                    this.setPokemonAIState(EVOLVING, true);
                    evolving = true;
                    // Send the message about evolving, to let user cancel.
                    this.displayMessageToOwner(
                            new TextComponentTranslation("pokemob.evolution.start", this.getPokemonDisplayName()));
                    return this;
                }
                // Evolve the mob.
                IPokemob evo = megaEvolve(((EvolveEvent.Pre) evt).forme);
                // Clear held item if used for evolving.
                if (neededItem)
                {
                    ((EntityEvolvablePokemob) evo).setHeldItem(CompatWrapper.nullStack);
                }
                if (evo != null)
                {
                    Entity owner = evo.getPokemonOwner();
                    evt = new EvolveEvent.Post(evo);
                    MinecraftForge.EVENT_BUS.post(evt);
                    EntityPlayer player = null;
                    if (owner instanceof EntityPlayer) player = (EntityPlayer) owner;
                    if (delayed) ((EntityMovesPokemob) evo).oldLevel = evo.getLevel() - 1;
                    else if (data != null) ((EntityMovesPokemob) evo).oldLevel = data.level - 1;
                    evo.levelUp(evo.getLevel());
                    this.setDead();
                    // Try to make a shedinja, this only work for nincada.
                    // TODO move this to event.
                    if (player != null && !player.getEntityWorld().isRemote && !isShadow())
                    {
                        makeShedinja(evo, player);
                    }
                }
                return evo;
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
            ItemStack cube = CompatWrapper.nullStack;
            int m = -1;
            for (int n = 0; n < inv.getSizeInventory(); n++)
            {
                ItemStack item = inv.getStackInSlot(n);
                if (item == CompatWrapper.nullStack) hasSpace = true;
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
                Entity pokemon = PokecubeMod.core.createPokemob(Database.getEntry("shedinja"), worldObj);
                if (pokemon != null)
                {
                    ItemStack mobCube = cube.copy();
                    mobCube.stackSize = 1;
                    IPokemob poke = (IPokemob) pokemon;
                    poke.setPokecube(mobCube);
                    poke.setPokemonOwner(player);
                    poke.setExp(Tools.levelToXp(poke.getExperienceMode(), 20), true);
                    ((EntityLivingBase) poke).setHealth(((EntityLivingBase) poke).getMaxHealth());
                    ItemStack shedinja = PokecubeManager.pokemobToItem(poke);
                    StatsCollector.addCapture(poke);
                    cube.stackSize--;
                    if (cube.stackSize <= 0) inv.setInventorySlotContents(m, CompatWrapper.nullStack);
                    inv.addItemStackToInventory(shedinja);
                }
            }
        }
    }

    @Override
    public IPokemob megaEvolve(PokedexEntry newEntry)
    {
        Entity evolution = this;
        if (newEntry != null && newEntry != getPokedexEntry())
        {
            setPokemonAIState(EVOLVING, true);
            if (newEntry.getPokedexNb() != getPokedexNb())
            {
                evolution = PokecubeMod.core.createPokemob(newEntry, worldObj);
                if (evolution == null)
                {
                    System.err.println("No Entry for " + newEntry);
                    return this;
                }
                ((EntityLivingBase) evolution).setHealth(this.getHealth());
                if (this.getPokemonNickname().equals(this.getPokedexEntry().getName())) this.setPokemonNickname("");
                NBTTagCompound tag = writePokemobData();
                tag.getCompoundTag(TagNames.OWNERSHIPTAG).removeTag(TagNames.POKEDEXNB);
                tag.getCompoundTag(TagNames.VISUALSTAG).removeTag(TagNames.FORME);
                ((IPokemob) evolution).readPokemobData(tag);
                evolution.getEntityData().merge(getEntityData());
                evolution.setUniqueId(getUniqueID());
                evolution.copyLocationAndAnglesFrom(this);
                this.setPokemonOwner((UUID) null);
                this.setDead();
                ((IPokemob) evolution).setAbility(newEntry.getAbility(abilityIndex, ((IPokemob) evolution)));
                if (this.addedToChunk)
                {
                    worldObj.removeEntity(this);
                    long evoTime = worldObj.getTotalWorldTime() + 2;
                    new EvoTicker(worldObj, evoTime, evolution, this.getPokemonDisplayName());
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
                ((IPokemob) evolution).setPokedexEntry(newEntry);
                ((IPokemob) evolution).setAbility(newEntry.getAbility(abilityIndex, ((IPokemob) evolution)));
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
        if (Tools.isSameStack(getHeldItemMainhand(), PokecubeItems.getStack("everstone")))
        {
            setPokemonAIState(TRADED, false);
        }
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
}
