package pokecube.core.entity.pokemobs.genetics;

import java.util.Map;

import org.nfunk.jep.JEP;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;

public class GeneticsManager
{
    public static String                       epigeneticFunction = "rand()*(((2*v + 256) * 31) / 512)";
    private static JEP                         epigeneticParser   = new JEP();

    public static final ResourceLocation       POKECUBEGENETICS   = new ResourceLocation(PokecubeMod.ID, "genetics");
    public static final String                 GENES              = "Genes";

    public static final ResourceLocation       ABILITYGENE        = new ResourceLocation(PokecubeMod.ID, "ability");
    public static final ResourceLocation       COLOURGENE         = new ResourceLocation(PokecubeMod.ID, "colour");
    public static final ResourceLocation       SIZEGENE           = new ResourceLocation(PokecubeMod.ID, "size");
    public static final ResourceLocation       NATUREGENE         = new ResourceLocation(PokecubeMod.ID, "nature");
    public static final ResourceLocation       SHINYGENE          = new ResourceLocation(PokecubeMod.ID, "shiny");
    public static final ResourceLocation       MOVESGENE          = new ResourceLocation(PokecubeMod.ID, "moves");
    public static final ResourceLocation       IVSGENE            = new ResourceLocation(PokecubeMod.ID, "ivs");
    public static final ResourceLocation       EVSGENE            = new ResourceLocation(PokecubeMod.ID, "evs");
    public static final ResourceLocation       SPECIESGENE        = new ResourceLocation(PokecubeMod.ID, "species");

    public static Map<ResourceLocation, Float> mutationRates      = Maps.newHashMap();

    public static void initJEP()
    {
        epigeneticParser = new JEP();
        epigeneticParser.initFunTab();
        epigeneticParser.addStandardFunctions();
        epigeneticParser.initSymTab(); // clear the contents of the symbol table
        epigeneticParser.addStandardConstants();
        epigeneticParser.addComplex();
        // table
        epigeneticParser.addVariable("v", 0);
        epigeneticParser.parseExpression(epigeneticFunction);
    }

    static
    {
        mutationRates.put(ABILITYGENE, 0.1f);
        mutationRates.put(COLOURGENE, 0.01f);
        mutationRates.put(SIZEGENE, 0.1f);
        mutationRates.put(NATUREGENE, 0.05f);
        mutationRates.put(SHINYGENE, 1 / 4096f);
        mutationRates.put(MOVESGENE, 0.0f);
        mutationRates.put(IVSGENE, 0.1f);
        mutationRates.put(EVSGENE, 0.1f);
        mutationRates.put(SPECIESGENE, 0.1f);
        initJEP();
    }

    public static String[] getMutationConfig()
    {
        String[] ret = new String[mutationRates.size()];
        int i = 0;
        for (ResourceLocation key : mutationRates.keySet())
        {
            String var = key + " " + mutationRates.get(key);
            ret[i++] = var;
        }
        return ret;
    }

    public static void init()
    {
        GeneRegistry.register(AbilityGene.class);
        GeneRegistry.register(ColourGene.class);
        GeneRegistry.register(SpeciesGene.class);
        GeneRegistry.register(IVsGene.class);
        GeneRegistry.register(EVsGene.class);
        GeneRegistry.register(MovesGene.class);
        GeneRegistry.register(NatureGene.class);
        GeneRegistry.register(ShinyGene.class);
        GeneRegistry.register(SizeGene.class);
    }

    public static void initMob(Entity mob)
    {
        IPokemob pokemob = (IPokemob) mob;
        PokedexEntry entry = pokemob.getPokedexEntry();
        IMobGenetics genes = mob.getCapability(IMobGenetics.GENETICS_CAP, null);

        genes.getAlleles().put(ABILITYGENE, new Alleles());
        genes.getAlleles().put(COLOURGENE, new Alleles());
        genes.getAlleles().put(SIZEGENE, new Alleles());
        genes.getAlleles().put(NATUREGENE, new Alleles());
        genes.getAlleles().put(SHINYGENE, new Alleles());
        genes.getAlleles().put(MOVESGENE, new Alleles());
        genes.getAlleles().put(IVSGENE, new Alleles());
        genes.getAlleles().put(EVSGENE, new Alleles());
        genes.getAlleles().put(SPECIESGENE, new Alleles());

        AbilityGene ability = new AbilityGene();
        ColourGene colours = new ColourGene();
        SpeciesGene species = new SpeciesGene();
        IVsGene ivs = new IVsGene();
        EVsGene evs = new EVsGene();
        MovesGene moves = new MovesGene();
        NatureGene nature = new NatureGene();
        ShinyGene shiny = new ShinyGene();
        SizeGene size = new SizeGene();

        Alleles alleles = genes.getAlleles().get(ABILITYGENE);
        AbilityObject abilityObj = ability.getValue();
        abilityObj.abilityIndex = (byte) pokemob.getAbilityIndex();
        Ability mobsA = pokemob.getAbility();
        Ability entryA = entry.getAbility(abilityObj.abilityIndex, pokemob);
        if (mobsA != null && (entryA == null || !pokemob.getAbility().toString().equals(entryA.toString())))
        {
            abilityObj.ability = pokemob.getAbility().toString();
        }
        alleles.getAlleles()[0] = ability;
        alleles.getAlleles()[1] = ability;
        alleles.refreshExpressed();

        alleles = genes.getAlleles().get(SPECIESGENE);
        SpeciesInfo info = new SpeciesInfo();
        info.value = pokemob.getSexe();
        info.entry = pokemob.getPokedexEntry();
        species.setValue(info);
        alleles.getAlleles()[0] = species;
        alleles.getAlleles()[1] = species;
        alleles.refreshExpressed();

        alleles = genes.getAlleles().get(IVSGENE);
        ivs.setValue(pokemob.getIVs());
        alleles.getAlleles()[0] = ivs;
        alleles.getAlleles()[1] = ivs;
        alleles.refreshExpressed();

        alleles = genes.getAlleles().get(NATUREGENE);
        nature.setValue(pokemob.getNature());
        alleles.getAlleles()[0] = nature;
        alleles.getAlleles()[1] = nature;
        alleles.refreshExpressed();

        alleles = genes.getAlleles().get(SHINYGENE);
        shiny.setValue(pokemob.isShiny());
        alleles.getAlleles()[0] = shiny;
        alleles.getAlleles()[1] = shiny;
        alleles.refreshExpressed();

        alleles = genes.getAlleles().get(SIZEGENE);
        size.setValue(pokemob.getSize());
        alleles.getAlleles()[0] = size;
        alleles.getAlleles()[1] = size;
        alleles.refreshExpressed();

        alleles = genes.getAlleles().get(MOVESGENE);
        moves.setValue(pokemob.getMoves());
        alleles.getAlleles()[0] = moves;
        alleles.getAlleles()[1] = moves;
        alleles.refreshExpressed();

        alleles = genes.getAlleles().get(MOVESGENE);
        moves.setValue(pokemob.getMoves());
        alleles.getAlleles()[0] = moves;
        alleles.getAlleles()[1] = moves;
        alleles.refreshExpressed();

        alleles = genes.getAlleles().get(EVSGENE);
        evs.setValue(pokemob.getEVs());
        alleles.getAlleles()[0] = evs;
        alleles.getAlleles()[1] = evs;
        alleles.refreshExpressed();

        if (!(mob instanceof IMobColourable)) return;

        alleles = genes.getAlleles().get(COLOURGENE);
        colours.setValue(((IMobColourable) mob).getRGBA());
        alleles.getAlleles()[0] = colours;
        alleles.getAlleles()[1] = colours;
        alleles.refreshExpressed();

    }

    public static void initFromGenes(IMobGenetics genes, IPokemob pokemob)
    {
        Entity mob = (Entity) pokemob;
        PokedexEntry dexentry = pokemob.getPokedexEntry();
        IMobGenetics mobs = mob.getCapability(IMobGenetics.GENETICS_CAP, null);
        if (genes != mobs)
        {
            mobs.getAlleles().putAll(genes.getAlleles());
        }
        byte[] ivs = new byte[6];
        byte[] evs = new byte[6];
        for (Map.Entry<ResourceLocation, Alleles> entry : mobs.getAlleles().entrySet())
        {
            ResourceLocation loc = entry.getKey();
            Alleles alleles = entry.getValue();
            Gene gene = alleles.getExpressed();
            if (loc.equals(ABILITYGENE))
            {
                AbilityObject abs = gene.getValue();
                pokemob.setAbilityIndex(abs.abilityIndex);
                if (!abs.ability.isEmpty()) pokemob.setAbility(AbilityManager.getAbility(abs.ability));
                else pokemob.setAbility(dexentry.getAbility(abs.abilityIndex, pokemob));
                continue;
            }
            if (loc.equals(COLOURGENE) && (mob instanceof IMobColourable))
            {
                IMobColourable col = (IMobColourable) mob;
                int[] colour = gene.getValue();
                col.setRGBA(colour);
                continue;
            }
            if (loc.equals(SPECIESGENE))
            {
                SpeciesInfo info = gene.getValue();
                pokemob.setSexe(info.value);
                pokemob.megaEvolve(info.entry);
                continue;
            }
            if (loc.equals(IVSGENE))
            {
                ivs = gene.getValue();
                continue;
            }
            if (loc.equals(EVSGENE))
            {
                evs = gene.getValue();
                continue;
            }
            if (loc.equals(MOVESGENE))
            {
                String[] moves = gene.getValue();
                for (int i = 0; i < moves.length; i++)
                {
                    if (moves[i] != null) pokemob.learn(moves[i]);
                }
                continue;
            }
            if (loc.equals(NATUREGENE))
            {
                Nature nat = gene.getValue();
                pokemob.setNature(nat);
                continue;
            }
            if (loc.equals(SHINYGENE))
            {
                boolean shiny = gene.getValue();
                pokemob.setShiny(shiny);
                continue;
            }
            if (loc.equals(SIZEGENE))
            {
                float size = gene.getValue();
                pokemob.setSize(size);
                continue;
            }
        }
        for (int i = 0; i < 6; i++)
        {
            epigeneticParser.setVarValue("v", evs[i]);
            double value = epigeneticParser.getValue();
            if (!Double.isNaN(value))
            {
                value = Math.max(0, value);
                ivs[i] += value;
                ivs[i] = (byte) Math.min(ivs[i], 31);
                ivs[i] = (byte) Math.max(ivs[i], 0);
            }
        }
        pokemob.setIVs(ivs);
    }

    public static void handleLoad(IPokemob pokemob)
    {
        Entity mob = (Entity) pokemob;
        IMobGenetics genes = mob.getCapability(IMobGenetics.GENETICS_CAP, null);
        if (!genes.getAlleles().isEmpty()) return;
        initMob(mob);
    }

    public static void handleEpigenetics(IPokemob pokemob)
    {
        Entity mob = (Entity) pokemob;
        IMobGenetics genes = mob.getCapability(IMobGenetics.GENETICS_CAP, null);
        if (genes.getAlleles().isEmpty())
        {
            initMob(mob);
        }
        try
        {
            Alleles alleles = genes.getAlleles().get(MOVESGENE);
            alleles.getExpressed().setValue(pokemob.getMoves());
            alleles.getExpressed().setValue(alleles.getExpressed().mutate().getValue());
            alleles = genes.getAlleles().get(EVSGENE);
            alleles.getExpressed().setValue(pokemob.getEVs());
            alleles = genes.getAlleles().get(IVSGENE);
            alleles.getExpressed().setValue(pokemob.getIVs());
            alleles.getExpressed().setValue(alleles.getExpressed().mutate().getValue());
            alleles = genes.getAlleles().get(SPECIESGENE);
            Gene gene = alleles.getExpressed();
            SpeciesInfo info = gene.getValue();
            info.entry = pokemob.getPokedexEntry();
        }
        catch (Exception e)
        {
            initMob(mob);
        }
    }

    public static void initEgg(IMobGenetics eggs, IMobGenetics mothers, IMobGenetics fathers)
    {
        if (eggs == null || mothers == null || fathers == null) return;
        eggs.setFromParents(mothers, fathers);
    }

    public GeneticsManager()
    {
        init();
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent.Item event)
    {
        if (event.getItemStack().getItem() instanceof ItemPokemobEgg
                && !event.getCapabilities().containsKey(POKECUBEGENETICS))
        {
            event.addCapability(POKECUBEGENETICS, new GeneticsProvider());
        }
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof IPokemob && !event.getCapabilities().containsKey(POKECUBEGENETICS))
        {
            event.addCapability(POKECUBEGENETICS, new GeneticsProvider());
        }
    }

    public static class GeneticsProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
    {
        IMobGenetics genetics = IMobGenetics.GENETICS_CAP.getDefaultInstance();

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == IMobGenetics.GENETICS_CAP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (hasCapability(capability, facing)) return (T) genetics;
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTBase nbt = IMobGenetics.GENETICS_CAP.getStorage().writeNBT(IMobGenetics.GENETICS_CAP, genetics, null);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("V", nbt);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            NBTBase nbt = tag.getTag("V");
            IMobGenetics.GENETICS_CAP.getStorage().readNBT(IMobGenetics.GENETICS_CAP, genetics, null, nbt);
        }
    }
}
