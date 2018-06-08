package pokecube.core.interfaces.capabilities.impl;

import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.ABILITYGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.COLOURGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.EVSGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.IVSGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.MOVESGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.NATUREGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.SHINYGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.SIZEGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.SPECIESGENE;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLiving;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.pokemobs.genetics.epigenes.EVsGene;
import pokecube.core.entity.pokemobs.genetics.epigenes.MovesGene;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene.AbilityObject;
import pokecube.core.entity.pokemobs.genetics.genes.ColourGene;
import pokecube.core.entity.pokemobs.genetics.genes.IVsGene;
import pokecube.core.entity.pokemobs.genetics.genes.NatureGene;
import pokecube.core.entity.pokemobs.genetics.genes.ShinyGene;
import pokecube.core.entity.pokemobs.genetics.genes.SizeGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketSyncGene;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.Alleles;

public abstract class PokemobGenes extends PokemobBase implements IMobColourable
{
    private static HashMap<Class<? extends EntityLiving>, PokedexEntry> classMap = Maps.newHashMap();

    public static boolean isRegistered(Class<? extends EntityLiving> clazz)
    {
        return classMap.containsKey(clazz);
    }

    public static void registerClass(Class<? extends EntityLiving> clazz, PokedexEntry entry)
    {
        if (classMap.containsValue(entry)) throw new IllegalArgumentException("Cannot register " + entry + " twice!");
        PokecubeMod.pokedexmap.put(entry, clazz);
        classMap.put(clazz, entry);
    }

    private void initAbilityGene()
    {
        if (genesAbility == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesAbility = genes.getAlleles().get(ABILITYGENE);
            if (genesAbility == null)
            {
                genesAbility = new Alleles();
                genes.getAlleles().put(ABILITYGENE, genesAbility);
            }
            if (genesAbility.getAlleles()[0] == null)
            {
                Random random = new Random(getRNGValue());
                PokedexEntry entry = getPokedexEntry();
                int abilityIndex = random.nextInt(100) % 2;
                if (entry.getAbility(abilityIndex, this) == null)
                {
                    if (abilityIndex != 0) abilityIndex = 0;
                    else abilityIndex = 1;
                }
                Ability ability = entry.getAbility(abilityIndex, this);
                AbilityGene gene = new AbilityGene();
                AbilityObject obj = gene.getValue();
                obj.ability = "";
                obj.abilityObject = ability;
                obj.abilityIndex = (byte) abilityIndex;
                genesAbility.getAlleles()[0] = gene;
                genesAbility.getAlleles()[1] = gene;
                genesAbility.refreshExpressed();
            }
            setAbility(getAbility());
        }
    }

    @Override
    public Ability getAbility()
    {
        if (genesAbility == null) initAbilityGene();
        AbilityObject obj = genesAbility.getExpressed().getValue();
        if (obj.abilityObject == null && !obj.searched)
        {
            if (!obj.ability.isEmpty())
            {
                Ability ability = AbilityManager.getAbility(obj.ability);
                obj.abilityObject = ability;
            }
            else
            {
                obj.abilityObject = getPokedexEntry().getAbility(obj.abilityIndex, this);
            }
            obj.searched = true;
        }
        return obj.abilityObject;
    }

    @Override
    public int getAbilityIndex()
    {
        if (genesAbility == null) initAbilityGene();
        AbilityObject obj = genesAbility.getExpressed().getValue();
        return obj.abilityIndex;
    }

    @Override
    public void setAbility(Ability ability)
    {
        if (genesAbility == null) initAbilityGene();
        AbilityObject obj = genesAbility.getExpressed().getValue();
        Ability oldAbility = obj.abilityObject;
        if (oldAbility != null && oldAbility != ability) oldAbility.destroy();
        Ability defalt = getPokedexEntry().getAbility(getAbilityIndex(), this);
        obj.abilityObject = ability;
        obj.ability = ability != null
                ? defalt != null && defalt.getName().equals(ability.getName()) ? "" : ability.toString() : "";
        if (ability != null) ability.init(this);
    }

    @Override
    public void setAbilityIndex(int ability)
    {
        if (genesAbility == null) initAbilityGene();
        if (ability > 2 || ability < 0) ability = 0;
        AbilityObject obj = genesAbility.getExpressed().getValue();
        obj.abilityIndex = (byte) ability;
    }

    @Override
    public float getSize()
    {
        if (genesSize == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesSize = genes.getAlleles().get(SIZEGENE);
            if (genesSize == null)
            {
                genesSize = new Alleles();
                genes.getAlleles().put(SIZEGENE, genesSize);
            }
            if (genesSize.getAlleles()[0] == null || genesSize.getAlleles()[1] == null)
            {
                SizeGene gene = new SizeGene();
                genesSize.getAlleles()[0] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesSize.getAlleles()[1] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesSize.refreshExpressed();
                setSize(genesSize.getExpressed().getValue());
            }
        }
        Float size = genesSize.getExpressed().getValue();
        return (float) (size * PokecubeMod.core.getConfig().scalefactor);
    }

    @Override
    public void setIVs(byte[] ivs)
    {
        if (genesIVs == null) getIVs();
        if (genesIVs != null) genesIVs.getExpressed().setValue(ivs);
    }

    @Override
    public void setEVs(byte[] evs)
    {
        if (genesEVs == null) getEVs();
        if (genesEVs != null) genesEVs.getExpressed().setValue(evs);
        PacketSyncGene.syncGene(getEntity(), genesEVs);
    }

    @Override
    public byte[] getEVs()
    {
        if (genesEVs == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesEVs = genes.getAlleles().get(EVSGENE);
            if (genesEVs == null)
            {
                genesEVs = new Alleles();
                genes.getAlleles().put(EVSGENE, genesEVs);
            }
            if (genesEVs.getAlleles()[0] == null || genesEVs.getAlleles()[1] == null)
            {
                EVsGene ivs = new EVsGene();
                genesEVs.getAlleles()[0] = ivs.getMutationRate() > rand.nextFloat() ? ivs.mutate() : ivs;
                genesEVs.getAlleles()[1] = ivs.getMutationRate() > rand.nextFloat() ? ivs.mutate() : ivs;
                genesEVs.refreshExpressed();
                genesEVs.getExpressed().setValue(new EVsGene().getValue());
            }
        }
        return genesEVs.getExpressed().getValue();
    }

    @Override
    public byte[] getIVs()
    {
        if (genesIVs == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesIVs = genes.getAlleles().get(IVSGENE);
            if (genesIVs == null)
            {
                genesIVs = new Alleles();
                genes.getAlleles().put(IVSGENE, genesIVs);
            }
            if (genesIVs.getAlleles()[0] == null || genesIVs.getAlleles()[1] == null)
            {
                IVsGene gene = new IVsGene();
                genesIVs.getAlleles()[0] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesIVs.getAlleles()[1] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesIVs.refreshExpressed();
            }
        }
        return genesIVs.getExpressed().getValue();
    }

    @Override
    public String[] getMoves()
    {
        String[] moves = getMoveStats().moves;
        if (genesMoves == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesMoves = genes.getAlleles().get(MOVESGENE);
            if (genesMoves == null)
            {
                genesMoves = new Alleles();
                genes.getAlleles().put(MOVESGENE, genesMoves);
            }
            if (genesMoves.getAlleles()[0] == null || genesMoves.getAlleles()[1] == null)
            {
                MovesGene gene = new MovesGene();
                gene.setValue(moves);
                genesMoves.getAlleles()[0] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesMoves.getAlleles()[1] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesMoves.refreshExpressed();
            }
        }
        return getMoveStats().moves = genesMoves.getExpressed().getValue();
    }

    @Override
    public void setMove(int i, String moveName)
    {
        // do not blanket set moves on client, or when transformed.
        if (!entity.isServerWorld() || getTransformedTo() != null) return;

        String[] moves = getMoves();
        moves[i] = moveName;
        setMoves(moves);
    }

    @Override
    public void setNature(Nature nature)
    {
        if (genesNature == null) getNature();
        if (genesNature != null) genesNature.getExpressed().setValue(nature);
    }

    @Override
    public Nature getNature()
    {
        if (genesNature == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesNature = genes.getAlleles().get(NATUREGENE);
            if (genesNature == null)
            {
                genesNature = new Alleles();
                genes.getAlleles().put(NATUREGENE, genesNature);
            }
            if (genesNature.getAlleles()[0] == null || genesNature.getAlleles()[1] == null)
            {
                NatureGene gene = new NatureGene();
                genesNature.getAlleles()[0] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesNature.getAlleles()[1] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesNature.refreshExpressed();
            }
        }
        return genesNature.getExpressed().getValue();
    }

    public void setMoves(String[] moves)
    {
        // do not blanket set moves on client, or when transformed.
        if (entity.getEntityWorld().isRemote || getTransformedTo() != null) return;
        if (moves != null && moves.length == 4)
        {
            if (genesMoves == null)
            {
                getMoves();
            }
            if (genesMoves == null || genesMoves.getExpressed() == null || getMoveStats() == null)
            {
                PokecubeMod.log(Level.WARNING, "Error in setMoves " + getEntity(), new NullPointerException());
                PokecubeMod.log(Level.WARNING, "AllGenes: " + genes);
                PokecubeMod.log(Level.WARNING, "Genes: " + genesMoves);
                if (genesMoves != null) PokecubeMod.log(Level.WARNING, "Gene: " + genesMoves.getExpressed());
                else PokecubeMod.log(Level.WARNING, "Gene: " + genesMoves);
                PokecubeMod.log(Level.WARNING, "stats: " + getMoveStats());
                return;
            }
            genesMoves.getExpressed().setValue(getMoveStats().moves = moves);
        }
        PacketSyncGene.syncGene(getEntity(), genesMoves);
    }

    @Override
    public void setSize(float size)
    {
        if (genesSize == null) getSize();
        float a = 1, b = 1, c = 1;
        PokedexEntry entry = getPokedexEntry();
        if (entry != null)
        {
            a = entry.width * size;
            b = entry.height * size;
            c = entry.length * size;
            if (a < 0.01 || b < 0.01 || c < 0.01)
            {
                float min = 0.01f / Math.min(a, Math.min(c, b));
                size *= min / PokecubeMod.core.getConfig().scalefactor;
            }
        }
        genesSize.getExpressed().setValue(size);
    }

    @Override
    public int[] getRGBA()
    {
        if (genesColour == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesColour = genes.getAlleles().get(COLOURGENE);
            if (genesColour == null)
            {
                genesColour = new Alleles();
                genes.getAlleles().put(COLOURGENE, genesColour);
            }
            if (genesColour.getAlleles()[0] == null)
            {
                ColourGene gene = new ColourGene();
                genesColour.getAlleles()[0] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesColour.getAlleles()[1] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesColour.refreshExpressed();
            }
        }
        return genesColour.getExpressed().getValue();
    }

    @Override
    public void setRGBA(int... colours)
    {
        int[] rgba = getRGBA();
        for (int i = 0; i < colours.length && i < rgba.length; i++)
        {
            rgba[i] = colours[i];
        }
    }

    @Override
    public boolean isShiny()
    {
        if (genesShiny == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesShiny = genes.getAlleles().get(SHINYGENE);
            if (genesShiny == null)
            {
                genesShiny = new Alleles();
                genes.getAlleles().put(SHINYGENE, genesShiny);
            }
            if (genesShiny.getAlleles()[0] == null || genesShiny.getAlleles()[1] == null)
            {
                ShinyGene gene = new ShinyGene();
                genesShiny.getAlleles()[0] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesShiny.getAlleles()[1] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesShiny.refreshExpressed();
            }
        }
        boolean shiny = genesShiny.getExpressed().getValue();
        if (shiny && !getPokedexEntry().hasShiny)
        {
            shiny = false;
            genesShiny.getExpressed().setValue(false);
        }
        return shiny;
    }

    @Override
    public void setShiny(boolean shiny)
    {
        if (genesShiny == null) isShiny();
        genesShiny.getExpressed().setValue(shiny);
        PacketSyncGene.syncGene(getEntity(), genesShiny);
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        if (genesSpecies == null)
        {
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesSpecies = genes.getAlleles().get(SPECIESGENE);
            if (genesSpecies == null)
            {
                genesSpecies = new Alleles();
                genes.getAlleles().put(SPECIESGENE, genesSpecies);
            }
            SpeciesGene gene;
            SpeciesInfo info;
            if (genesSpecies.getAlleles()[0] == null
                    || (info = (gene = genesSpecies.getExpressed()).getValue()).entry == null)
            {
                gene = new SpeciesGene();
                info = gene.getValue();
                info.entry = classMap.get(getEntity().getClass());
                if (info.entry == null)
                {
                    if (getEntity().getClass().getSimpleName().startsWith("GenericPokemob_"))
                    {
                        String name = getEntity().getClass().getSimpleName().replace("GenericPokemob_", "").trim();
                        info.entry = Database.getEntry(name);
                    }
                    else
                    {
                        System.out.println(this.getEntity().getClass());
                        Thread.dumpStack();
                        info.entry = Database.missingno;
                        throw new RuntimeException("Error with Species!");
                    }
                }
                info.value = Tools.getSexe(info.entry.getSexeRatio(), new Random());
                info.entry = info.entry.getForGender(info.value);
                info = info.clone();
                // Generate the basic genes
                genesSpecies.getAlleles()[0] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesSpecies.getAlleles()[1] = gene.getMutationRate() > rand.nextFloat() ? gene.mutate() : gene;
                genesSpecies.refreshExpressed();
                // Set the expressed gene to the info made above, this is to
                // override the gene from merging parents which results in the
                // child state.
                genesSpecies.getExpressed().setValue(info);
            }
            info = genesSpecies.getExpressed().getValue();
            info.entry = entry = info.entry.getForGender(info.value);
        }
        if (entry != null) return entry;
        SpeciesInfo info = genesSpecies.getExpressed().getValue();
        assert info.entry != null;
        return entry = info.entry;
    }

    @Override
    public IPokemob setPokedexEntry(PokedexEntry newEntry)
    {
        PokedexEntry entry = getPokedexEntry();
        SpeciesInfo info = genesSpecies.getExpressed().getValue();
        if (newEntry == null || newEntry == entry) return this;
        this.entry = newEntry;
        IPokemob ret = this;
        info.entry = newEntry;
        ret = megaEvolve(newEntry);
        if (entity.getEntityWorld() != null)
            ret.setSize((float) (ret.getSize() / PokecubeMod.core.getConfig().scalefactor));
        if (entity.getEntityWorld() != null && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PacketChangeForme.sendPacketToNear(ret.getEntity(), newEntry, 128);
        }
        return ret;
    }

    @Override
    public byte getSexe()
    {
        if (genesSpecies == null) getPokedexEntry();
        SpeciesInfo info = genesSpecies.getExpressed().getValue();
        return info.value;
    }

    @Override
    public void setSexe(byte sexe)
    {
        if (genesSpecies == null) getPokedexEntry();
        SpeciesInfo info = genesSpecies.getExpressed().getValue();
        if (sexe == NOSEXE || sexe == FEMALE || sexe == MALE || sexe == SEXLEGENDARY)
        {
            info.value = sexe;
        }
        else
        {
            System.err.println("Illegal argument. Sexe cannot be " + sexe);
            new Exception().printStackTrace();
        }
    }

    @Override
    public void onGenesChanged()
    {
        // Reset this incase gender or shininess changed..
        this.textures = null;

        genesSpecies = null;
        getPokedexEntry();
        genesSize = null;
        getSize();
        genesIVs = null;
        getIVs();
        genesEVs = null;
        getEVs();
        genesMoves = null;
        getMoves();
        genesNature = null;
        getNature();
        genesAbility = null;
        getAbility();
        genesShiny = null;
        isShiny();
        genesColour = null;
        getRGBA();

        // Refresh the datamanager for moves.
        setMoves(getMoves());
        // Refresh the datamanager for evs
        setEVs(getEVs());

        setSize((float) (getSize() / PokecubeMod.core.getConfig().scalefactor));
    }
}
