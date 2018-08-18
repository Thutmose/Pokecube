package pokecube.core.interfaces.pokemob;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.events.EvolveEvent;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.network.PacketHandler;

public interface ICanEvolve extends IHasEntry, IHasOwner
{

    /** Used to allow the pokemob to evolve over a few ticks */
    static class EvoTicker
    {
        /** take 10 ticks to evolve to give time to clean things up first. */
        int          tick = 10;
        /** Who we are evolving to. */
        final Entity evo;
        /** UUID to evolve into. */
        final UUID   id;

        public EvoTicker(Entity evolution, UUID id)
        {
            this.evo = evolution;
            this.id = id;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void tick(WorldTickEvent evt)
        {
            if (evt.world != evo.getEntityWorld() || evt.phase != Phase.END) return;
            boolean exists = false;
            for (Entity e : evt.world.loadedEntityList)
            {
                if (e.getUniqueID().equals(id))
                {
                    exists = true;
                    break;
                }
            }
            if (!exists && tick-- <= 0)
            {
                evo.setUniqueId(id);
                PacketHandler.sendEntityUpdate(evo);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    /** Simlar to EvoTicker, but for more general form changing. */
    static class MegaEvoTicker
    {
        final World          world;
        final Entity         mob;
        IPokemob             pokemob;
        final PokedexEntry   mega;
        final ITextComponent message;
        final long           evoTime;
        boolean              set = false;

        MegaEvoTicker(PokedexEntry mega, long evoTime, IPokemob evolver, ITextComponent message)
        {
            this.mob = evolver.getEntity();
            this.world = mob.getEntityWorld();
            this.evoTime = this.world.getTotalWorldTime() + evoTime;
            this.message = message;
            this.mega = mega;
            this.pokemob = evolver;

            // Flag as evolving
            pokemob.setGeneralState(GeneralStates.EVOLVING, true);
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
            pokemob.setEvolutionTicks(PokecubeMod.core.getConfig().evolutionTicks + 50);

            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void tick(WorldTickEvent evt)
        {
            if (evt.world != world || evt.phase != Phase.END) return;
            if (!mob.addedToChunk || mob.isDead)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }
            if (evt.world.getTotalWorldTime() >= evoTime)
            {
                if (pokemob.getCombatState(CombatStates.MEGAFORME) && pokemob.getOwner() instanceof EntityPlayerMP)
                    Triggers.MEGAEVOLVEPOKEMOB.trigger((EntityPlayerMP) pokemob.getOwner(), pokemob);
                int evoTicks = pokemob.getEvolutionTicks();
                float hp = pokemob.getEntity().getHealth();
                pokemob = pokemob.megaEvolve(mega);
                pokemob.getEntity().setHealth(hp);
                /** Flag the new mob as evolving to continue the animation
                 * effects. */
                pokemob.setGeneralState(GeneralStates.EVOLVING, true);
                pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
                pokemob.setEvolutionTicks(evoTicks);

                if (message != null)
                {
                    pokemob.displayMessageToOwner(message);
                }
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    /** Shedules mega evolution for a few ticks later
     * 
     * @param evolver
     *            the mob to schedule to evolve
     * @param newForm
     *            the form to evolve to
     * @param message
     *            the message to send on completion */
    public static void setDelayedMegaEvolve(IPokemob evolver, PokedexEntry newForm, ITextComponent message)
    {
        new MegaEvoTicker(newForm, PokecubeMod.core.getConfig().evolutionTicks / 2, evolver, message);
    }

    /** Cancels the current evoluton for the pokemob, sends appropriate message
     * to owner. */
    default void cancelEvolve()
    {
        if (!isEvolving()) return;
        EntityLivingBase entity = getEntity();
        if (getEntity().getEntityWorld().isRemote)
        {
            MessageServer message = new MessageServer(MessageServer.CANCELEVOLVE, entity.getEntityId());
            PokecubePacketHandler.sendToServer(message);
            return;
        }
        setEvolutionTicks(-1);
        this.setGeneralState(GeneralStates.EVOLVING, false);
        this.displayMessageToOwner(new TextComponentTranslation("pokemob.evolution.cancel",
                CapabilityPokemob.getPokemobFor(entity).getPokemonDisplayName()));
    }

    /** Called when give item. to override when the pokemob evolve with a stone.
     *
     * @param itemId
     *            the shifted index of the item
     * @return whether should evolve */
    default boolean canEvolve(ItemStack stack)
    {
        if (stack != ItemStack.EMPTY && Tools.isStack(stack, "everstone")) return false;
        if (this.getPokedexEntry().canEvolve() && getEntity().isServerWorld())
        {
            for (EvolutionData d : getPokedexEntry().getEvolutions())
            {
                if (d.shouldEvolve((IPokemob) this, stack)) { return true; }
            }
        }
        return false;
    }

    /** @return if we are currently evolving */
    default boolean isEvolving()
    {
        return this.getGeneralState(GeneralStates.EVOLVING);
    }

    /** Called when the level is up. Should be overridden to handle level up
     * events like evolution or move learning.
     * 
     * @param level
     *            the new level */
    default IPokemob levelUp(int level)
    {
        EntityLivingBase theEntity = getEntity();
        IPokemob theMob = CapabilityPokemob.getPokemobFor(theEntity);
        List<String> moves = Database.getLevelUpMoves(theMob.getPokedexEntry(), level, theMob.getMoveStats().oldLevel);
        Collections.shuffle(moves);
        if (!theEntity.getEntityWorld().isRemote)
        {
            ITextComponent mess = new TextComponentTranslation("pokemob.info.levelup", theMob.getPokemonDisplayName(),
                    level + "");
            theMob.displayMessageToOwner(mess);
        }
        HappinessType.applyHappiness(theMob, HappinessType.LEVEL);
        if (moves != null)
        {
            if (theMob.getGeneralState(GeneralStates.TAMED))
            {
                String[] current = theMob.getMoves();
                if (current[3] != null)
                {
                    for (String s : current)
                    {
                        if (s == null) continue;
                        for (String s1 : moves)
                        {
                            if (s.equals(s1))
                            {
                                moves.remove(s1);
                                break;
                            }
                        }
                    }
                    for (String s : moves)
                    {
                        ITextComponent move = new TextComponentTranslation(MovesUtils.getUnlocalizedMove(s));
                        ITextComponent mess = new TextComponentTranslation("pokemob.move.notify.learn",
                                theMob.getPokemonDisplayName(), move);
                        theMob.displayMessageToOwner(mess);
                        if (!theMob.getMoveStats().newMoves.contains(s))
                        {
                            theMob.getMoveStats().newMoves.add(s);
                            PacketSyncNewMoves.sendUpdatePacket((IPokemob) this);
                        }
                    }
                    PacketHandler.sendEntityUpdate(getEntity());
                    return theMob;
                }
            }
            for (String s : moves)
            {
                theMob.learn(s);
            }
        }
        return theMob;
    }

    /** Converts us to the given entry
     * 
     * @param newEntry
     *            new pokedex entry to have
     * @return the new pokemob, return this if it fails */
    default IPokemob megaEvolve(PokedexEntry newEntry)
    {
        EntityLivingBase thisEntity = getEntity();
        IPokemob thisMob = CapabilityPokemob.getPokemobFor(thisEntity);
        Entity evolution = thisEntity;
        IPokemob evoMob = thisMob;
        PokedexEntry oldEntry = getPokedexEntry();
        if (newEntry != null && newEntry != oldEntry)
        {
            setGeneralState(GeneralStates.EVOLVING, true);

            evolution = PokecubeMod.core.createPokemob(newEntry, thisEntity.getEntityWorld());
            if (evolution == null)
            {
                System.err.println("No Entry for " + newEntry);
                return thisMob;
            }
            evoMob = CapabilityPokemob.getPokemobFor(evolution);
            // Flag the mob as evolving.
            evoMob.setGeneralState(GeneralStates.EVOLVING, true);

            // Sync health and nickname
            ((EntityLivingBase) evolution).setHealth(thisEntity.getHealth());
            if (this.getPokemonNickname().equals(oldEntry.getName())) this.setPokemonNickname("");

            // Sync tags besides the ones that define species and form.
            NBTTagCompound tag = thisMob.writePokemobData();
            tag.getCompoundTag(TagNames.OWNERSHIPTAG).removeTag(TagNames.POKEDEXNB);
            tag.getCompoundTag(TagNames.VISUALSTAG).removeTag(TagNames.FORME);
            evoMob.readPokemobData(tag);

            // Sync held item
            evoMob.setHeldItem(thisMob.getHeldItem());

            // Sync genes
            IMobGenetics oldGenes = thisEntity.getCapability(IMobGenetics.GENETICS_CAP, null);
            IMobGenetics newGenes = evolution.getCapability(IMobGenetics.GENETICS_CAP, null);
            newGenes.getAlleles().putAll(oldGenes.getAlleles());
            GeneticsManager.handleEpigenetics(evoMob);
            evoMob.onGenesChanged();
            // Set entry, this should fix expressed species gene.
            evoMob.setPokedexEntry(newEntry);

            // Sync entity data, UUID and location.
            evolution.getEntityData().merge(thisEntity.getEntityData());
            // evolution.setUniqueId(thisEntity.getUniqueID());
            evolution.copyLocationAndAnglesFrom(thisEntity);

            // Sync ability back, or store old ability.
            if (getCombatState(CombatStates.MEGAFORME))
            {
                if (thisMob.getAbility() != null)
                    evolution.getEntityData().setString("Ability", thisMob.getAbility().toString());
                Ability ability = newEntry.getAbility(0, evoMob);
                if (PokecubeMod.debug) PokecubeMod.log("Mega Evolving, changing ability to " + ability);
                if (ability != null) evoMob.setAbility(ability);
            }
            else
            {
                if (thisEntity.getEntityData().hasKey("Ability"))
                {
                    String ability = thisEntity.getEntityData().getString("Ability");
                    evolution.getEntityData().removeTag("Ability");
                    if (!ability.isEmpty()) evoMob.setAbility(AbilityManager.getAbility(ability));
                    if (PokecubeMod.debug) PokecubeMod.log("Un Mega Evolving, changing ability back to " + ability);
                }
            }

            // Set this mob wild, then kill it.
            this.setPokemonOwner((UUID) null);
            thisEntity.setDead();

            // Schedule adding to world.
            if (thisEntity.addedToChunk)
            {
                evolution.getEntityWorld().spawnEntity(evolution);
                new EvoTicker(evolution, thisEntity.getUniqueID());
                PacketHandler.sendEntityUpdate(evolution);
            }
        }
        return evoMob;
    }

    /** Evolve the pokemob.
     *
     * @param delayed
     *            true if we want to display the evolution animation
     * @return the evolution or this if the evolution failed */
    default IPokemob evolve(boolean delayed, boolean init)
    {
        EntityLivingBase thisEntity = getEntity();
        IPokemob thisMob = CapabilityPokemob.getPokemobFor(thisEntity);
        return evolve(delayed, init, thisMob.getHeldItem());
    }

    /** Evolve the pokemob.
     *
     * @param delayed
     *            true if we want to display the evolution animation
     * @param init
     *            true if this is called during initialization of the mob
     * @param stack
     *            the itemstack to check for evolution.
     * @return the evolution or null if the evolution failed, or this if the
     *         evolution succeeded, but delayed. */
    default IPokemob evolve(boolean delayed, boolean init, ItemStack stack)
    {
        EntityLivingBase thisEntity = getEntity();
        IPokemob thisMob = CapabilityPokemob.getPokemobFor(thisEntity);
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
                if (d.shouldEvolve(thisMob, stack))
                {
                    evol = d.evolution;
                    if (!d.shouldEvolve(thisMob, ItemStack.EMPTY)) neededItem = true;
                    data = d;
                    break;
                }
            }
            if (evol != null)
            {
                // Send evolve event.
                EvolveEvent evt = new EvolveEvent.Pre(thisMob, evol);
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) return null;
                // change to new forme.
                IPokemob evo = this.megaEvolve(((EvolveEvent.Pre) evt).forme);
                // Remove held item if it had one.
                if (neededItem && stack == thisMob.getHeldItem())
                {
                    evo.setHeldItem(ItemStack.EMPTY);
                }
                // Init things like moves.
                evo.getMoveStats().oldLevel = data.level - 1;
                evo.levelUp(evo.getLevel());

                // Learn evolution moves and update ability.
                for (String s : evo.getPokedexEntry().getEvolutionMoves())
                    evo.learn(s);
                evo.setAbility(evo.getPokedexEntry().getAbility(thisMob.getAbilityIndex(), evo));

                // Send post evolve event.
                evt = new EvolveEvent.Post(evo);
                MinecraftForge.EVENT_BUS.post(evt);
                // Kill old entity.
                if (evo != this) getEntity().setDead();
                return evo;
            }
            return null;
        }
        // Do not evolve if it is dead, or can't evolve.
        else if (this.getPokedexEntry().canEvolve() && !thisEntity.isDead)
        {
            boolean neededItem = false;
            PokedexEntry evol = null;
            EvolutionData data = null;
            // look for evolution data to use.
            for (EvolutionData d : getPokedexEntry().getEvolutions())
            {
                if (d.shouldEvolve(thisMob, stack))
                {
                    evol = d.evolution;
                    if (!d.shouldEvolve(thisMob, ItemStack.EMPTY) && stack == thisMob.getHeldItem()) neededItem = true;
                    data = d;
                    break;
                }
            }

            if (evol != null)
            {
                EvolveEvent evt = new EvolveEvent.Pre(thisMob, evol);
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) return null;
                if (delayed)
                {
                    // If delayed, set the pokemob as starting to evolve, and
                    // set the evolution for display effects.
                    if (stack != ItemStack.EMPTY) setEvolutionStack(stack.copy());
                    this.setEvolutionTicks(PokecubeMod.core.getConfig().evolutionTicks + 50);
                    this.setEvolvingEffects(evol);
                    this.setGeneralState(GeneralStates.EVOLVING, true);
                    // Send the message about evolving, to let user cancel.
                    this.displayMessageToOwner(
                            new TextComponentTranslation("pokemob.evolution.start", thisMob.getPokemonDisplayName()));
                    return thisMob;
                }
                // Evolve the mob.
                IPokemob evo = megaEvolve(((EvolveEvent.Pre) evt).forme);
                if (evo != null)
                {
                    // Clear held item if used for evolving.
                    if (neededItem)
                    {
                        evo.setHeldItem(ItemStack.EMPTY);
                    }
                    evt = new EvolveEvent.Post(evo);
                    MinecraftForge.EVENT_BUS.post(evt);
                    // Lean any moves that should are supposed to have just
                    // learnt.
                    if (delayed) evo.getMoveStats().oldLevel = evo.getLevel() - 1;
                    else if (data != null) evo.getMoveStats().oldLevel = data.level - 1;
                    evo.levelUp(evo.getLevel());

                    // Don't immediately try evolving again, only wild ones
                    // should do that.
                    evo.setEvolutionTicks(-1);
                    evo.setGeneralState(GeneralStates.EVOLVING, false);

                    // Learn evolution moves and update ability.
                    for (String s : evo.getPokedexEntry().getEvolutionMoves())
                        evo.learn(s);
                    evo.setAbility(evo.getPokedexEntry().getAbility(thisMob.getAbilityIndex(), evo));

                    // Kill old entity.
                    if (evo != this) thisEntity.setDead();
                }
                return evo;
            }
        }
        return null;
    }

    /** The evolution tick will be set when the mob evolves and then is
     * decreased each tick. It is used to render a special effect.
     * 
     * @param evolutionTicks
     *            the evolutionTicks to set */
    void setEvolutionTicks(int evolutionTicks);

    /** This itemstack will be used to evolve the pokemob after evolutionTicks
     * runs out. */
    void setEvolutionStack(ItemStack stack);

    /** This is the itemstack we are using for evolution, it is stored here for
     * use when evolution actually occurs. */
    ItemStack getEvolutionStack();

    /** Can set a custom entry for use with colouring the evolution effects. */
    default void setEvolvingEffects(PokedexEntry entry)
    {

    }

    /** This entry is used for colouring evolution effects. */
    default PokedexEntry getEvolutionEntry()
    {
        return getPokedexEntry();
    }
}
