package pokecube.core.entity.pokemobs.genetics;

import java.util.Map;

import org.nfunk.jep.JEP;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.entity.pokemobs.genetics.epigenes.EVsGene;
import pokecube.core.entity.pokemobs.genetics.epigenes.MovesGene;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene;
import pokecube.core.entity.pokemobs.genetics.genes.ColourGene;
import pokecube.core.entity.pokemobs.genetics.genes.IVsGene;
import pokecube.core.entity.pokemobs.genetics.genes.NatureGene;
import pokecube.core.entity.pokemobs.genetics.genes.ShinyGene;
import pokecube.core.entity.pokemobs.genetics.genes.SizeGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;

public class GeneticsManager
{
    public static String                       epigeneticFunction = "rand()*(((2*v + 256) * 31) / 512)";
    public static JEP                          epigeneticParser   = new JEP();

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
        mutationRates.put(SHINYGENE, 1 / 96f);
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
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        pokemob.onGenesChanged();
    }

    public static void initFromGenes(IMobGenetics genes, IPokemob pokemob)
    {
        Entity mob = pokemob.getEntity();
        IMobGenetics mobs = mob.getCapability(IMobGenetics.GENETICS_CAP, null);
        if (genes != mobs)
        {
            mobs.getAlleles().putAll(genes.getAlleles());
        }
        pokemob.onGenesChanged();
    }

    public static void handleLoad(IPokemob pokemob)
    {
        Entity mob = pokemob.getEntity();
        IMobGenetics genes = mob.getCapability(IMobGenetics.GENETICS_CAP, null);
        if (!genes.getAlleles().isEmpty()) return;
        initMob(mob);
    }

    public static void handleEpigenetics(IPokemob pokemob)
    {
        // pokemob.onGenesChanged();
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
    public void attachItemCapability(AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getObject().getItem() instanceof ItemPokemobEgg
                && !event.getCapabilities().containsKey(POKECUBEGENETICS))
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

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (hasCapability(capability, facing)) return IMobGenetics.GENETICS_CAP.cast(genetics);
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
