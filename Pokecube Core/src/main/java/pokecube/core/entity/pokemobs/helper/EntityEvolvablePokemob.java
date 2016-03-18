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
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.events.EvolveEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageClient;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;

/** @author Manchou */
public abstract class EntityEvolvablePokemob extends EntityDropPokemob
{
    // private int evolution;
    public boolean traded    = false;
    String         evolution = "";

    boolean evolving = false;

    public EntityEvolvablePokemob(World world)
    {
        super(world);
    }

    @Override
    public boolean canEvolve(ItemStack itemstack)
    {
        if (itemstack != null && itemstack.isItemEqual(PokecubeItems.getStack("Everstone"))) return false;

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
            MessageClient message = new MessageClient(MessageClient.CHANGEFORME, getEntityId());
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("f", newEntry.getName());
            message.buffer.writeNBTTagCompoundToBuffer(compound);
            PokecubePacketHandler.sendToAllNear(message, here, dimension, 128);
        }
        return this;
    }

    @Override
    public IPokemob evolve(boolean showAnimation)
    {
        return evolve(showAnimation, this.getHeldItem());
    }

    @Override
    public IPokemob evolve(boolean showAnimation, ItemStack stack)
    {
        if (this.getPokedexEntry().canEvolve() && !isDead)
        {
            // new Exception().printStackTrace();
            boolean neededItem = false;
            PokedexEntry evol = null;
            if (evolution == null || evolution.isEmpty())
            {
                for (EvolutionData d : getPokedexEntry().getEvolutions())
                {
                    if (d.shouldEvolve(this, stack))
                    {
                        evol = Database.getEntry(d.evolutionNb);
                        if (!d.shouldEvolve(this, null) && stack == getHeldItem()) neededItem = true;
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

                IPokemob evo = megaEvolve(((EvolveEvent.Pre) evt).forme);
                if (neededItem)
                {
                    ((EntityEvolvablePokemob) evo).setHeldItem(null);
                }

                if (showAnimation)
                {
                    evo.setEvolutionTicks(50);
                    if (evo instanceof EntityEvolvablePokemob)
                        ((EntityEvolvablePokemob) evo).setEvol(evol.getPokedexNb());
                }
                else
                {
                    evo.specificSpawnInit();
                }
                if (evo != null)
                {
                    Entity owner = evo.getPokemonOwner();
                    evt = new EvolveEvent.Post(evo);
                    MinecraftForge.EVENT_BUS.post(evt);

                    EntityPlayer player = null;
                    if (owner instanceof EntityPlayer) player = (EntityPlayer) owner;
                    ((EntityMovesPokemob) evo).oldLevel = evo.getLevel() - 1;
                    evo.levelUp(evo.getLevel());
                    this.setDead();
                    if (player != null && !player.worldObj.isRemote && !isShadow())
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
                                Entity pokemon = PokecubeMod.core.createEntityByPokedexNb(
                                        Database.getEntry("shedinja").getPokedexNb(), worldObj);
                                if (pokemon != null)
                                {
                                    IPokemob poke = (IPokemob) pokemon;
                                    poke.setPokecubeId(cubeId);
                                    poke.setPokemonOwner(player);
                                    poke.setExp(Tools.levelToXp(poke.getExperienceMode(), 20), false, false);
                                    ((EntityLivingBase) poke).setHealth(((EntityLivingBase) poke).getMaxHealth());
                                    ItemStack shedinja = PokecubeManager.pokemobToItem(poke);
                                    player.addStat(PokecubeMod.get1stPokemob, 0);
                                    player.addStat(PokecubeMod.pokemobAchievements.get(poke.getPokedexNb()), 1);

                                    cube.stackSize--;
                                    if (cube.stackSize <= 0) inv.setInventorySlotContents(m, null);
                                    inv.addItemStackToInventory(shedinja);
                                }
                            }
                        }
                    }
                }
                return evo;
            }
        }

        return null;
    }

    protected String getEvolFX()
    {
        String ret = "";
        int num = dataWatcher.getWatchableObjectInt(EVOLNBDW);
        for (EvolutionData d : getPokedexEntry().getEvolutions())
        {
            if (d.evolutionNb == num) return d.FX;
        }

        return ret;
    }

    /** @return the evolutionTicks */
    @Override
    public int getEvolutionTicks()
    {
        return dataWatcher.getWatchableObjectInt(EVOLTICKDW);
    }

    /** Returns whether the entity is in a server world */
    @Override
    public boolean isServerWorld()
    {
        return worldObj != null && super.isServerWorld();
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
                evolution.copyDataFromOld(this);
                evolution.copyLocationAndAnglesFrom(this);
                ((IPokemob) evolution).changeForme(forme);
                worldObj.spawnEntityInWorld(evolution);
                ((IPokemob) evolution).setPokemonAIState(EVOLVING, true);
                if (getPokemonAIState(MEGAFORME))
                {
                    ((IPokemob) evolution).setPokemonAIState(MEGAFORME, true);
                    ((IPokemob) evolution).setEvolutionTicks(10);

                }
                this.setDead();
                this.setPokemonOwner(null);
            }
            else
            {
                evolution = this;
                ((IPokemob) evolution).changeForme(forme);
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
            if (!evolving || forceSpawn)
            {
                this.evolve(true);
            }
            evolving = true;
            setEvolutionTicks(getEvolutionTicks() - 1);
            if (this.getPokemonAIState(IMoveConstants.TAMED)) showEvolutionFX(getEvolFX());
        }
        else
        {
            evolving = false;
        }
        if (num <= 0 && this.getPokemonAIState(EVOLVING))
        {
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
        if (getHeldItem() != null && getHeldItem().getItem() == PokecubeItems.everstone)
        {
            traded = false;
        }
        if (!this.getPokemonAIState(IMoveConstants.TAMED) && this.canEvolve(getHeldItem()))
        {
            this.evolve(false);
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
        dataWatcher.updateObject(EVOLNBDW, Integer.valueOf(num));
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
        dataWatcher.updateObject(EVOLTICKDW, new Integer(evolutionTicks));
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
