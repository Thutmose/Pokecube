package pokecube.core.entity.pokemobs.helper;

import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.ABILITYGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.COLOURGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.EVSGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.IVSGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.MOVESGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.NATUREGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.SHINYGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.SIZEGENE;
import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.SPECIESGENE;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
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
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;

/** This class will store the various stats of the pokemob as Alleles, and will
 * provider quick getters and setters for the genes. */
public abstract class EntityGeneticsPokemob extends EntityTameablePokemob
{
    public static float scaleFactor = 0.075f;

    Alleles             genesSize;
    Alleles             genesIVs;
    Alleles             genesEVs;
    Alleles             genesMoves;
    Alleles             genesNature;
    Alleles             genesAbility;
    Alleles             genesColour;
    Alleles             genesShiny;
    Alleles             genesSpecies;

    public EntityGeneticsPokemob(World world)
    {
        super(world);
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
        if (world != null) onGenesChanged();
    }

    private void initAbilityGene()
    {
        if (genesAbility == null)
        {
            IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
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
                int abilityIndex = random.nextInt(100) % 2;
                if (getPokedexEntry().getAbility(abilityIndex, this) == null)
                {
                    if (abilityIndex != 0) abilityIndex = 0;
                    else abilityIndex = 1;
                }
                Ability ability = getPokedexEntry().getAbility(abilityIndex, this);
                AbilityGene gene = new AbilityGene();
                AbilityObject obj = gene.getValue();
                obj.ability = ability != null ? ability.toString() : "";
                obj.abilityObject = ability;
                obj.abilityIndex = (byte) abilityIndex;
                genesAbility.getAlleles()[0] = gene;
                genesAbility.getAlleles()[1] = gene;
                genesAbility.refreshExpressed();
                setAbility(getAbility());
            }
        }
    }

    @Override
    public Ability getAbility()
    {
        if (genesAbility == null) initAbilityGene();
        if (getPokemonAIState(MEGAFORME)) return getPokedexEntry().getAbility(0, this);
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
        obj.abilityObject = ability;
        obj.ability = ability != null ? ability.toString() : "";
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
            IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesSize = genes.getAlleles().get(SIZEGENE);
            if (genesSize == null)
            {
                genesSize = new Alleles();
                genes.getAlleles().put(SIZEGENE, genesSize);
            }
            if (genesSize.getAlleles()[0] == null || genesSize.getAlleles()[1] == null)
            {
                SizeGene size = new SizeGene();
                float scale = 1 + scaleFactor * (float) (new Random()).nextGaussian();
                size.setValue(scale);
                genesSize.getAlleles()[0] = size;
                genesSize.getAlleles()[1] = size;
                genesSize.refreshExpressed();
                setSize(scale);
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
        int[] ints = PokecubeSerializer.byteArrayAsIntArray(evs);
        dataManager.set(EVS1DW, ints[0]);
        dataManager.set(EVS2DV, ints[1]);
        if (genesEVs == null) getEVs();
        if (genesEVs != null) genesEVs.getExpressed().setValue(evs);
    }

    @Override
    public byte[] getEVs()
    {
        if (!isServerWorld())
        {
            int[] ints = new int[] { dataManager.get(EVS1DW), dataManager.get(EVS2DV) };
            byte[] evs = PokecubeSerializer.intArrayAsByteArray(ints);
            return evs;
        }
        else
        {
            if (genesEVs == null)
            {
                IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
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
                    genesEVs.getAlleles()[0] = ivs;
                    genesEVs.getAlleles()[1] = ivs;
                    genesEVs.refreshExpressed();
                    genesEVs.getExpressed().setValue(new EVsGene().getValue());
                }
            }
            return genesEVs.getExpressed().getValue();
        }
    }

    @Override
    public byte[] getIVs()
    {
        if (genesIVs == null)
        {
            IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesIVs = genes.getAlleles().get(IVSGENE);
            if (genesIVs == null)
            {
                genesIVs = new Alleles();
                genes.getAlleles().put(IVSGENE, genesIVs);
            }
            if (genesIVs.getAlleles()[0] == null || genesIVs.getAlleles()[1] == null)
            {
                IVsGene ivs = new IVsGene();
                genesIVs.getAlleles()[0] = ivs;
                genesIVs.getAlleles()[1] = ivs;
                genesIVs.refreshExpressed();
            }
        }
        return genesIVs.getExpressed().getValue();
    }

    @Override
    public String[] getMoves()
    {
        if (!isServerWorld())
        {
            String movesString = dataManager.get(MOVESDW);
            String[] moves = new String[4];
            if (movesString != null && movesString.length() > 2)
            {
                String[] movesSplit = movesString.split(",");
                for (int i = 0; i < Math.min(4, movesSplit.length); i++)
                {
                    String move = movesSplit[i];

                    if (move != null && move.length() > 1 && MovesUtils.isMoveImplemented(move))
                    {
                        moves[i] = move;
                    }
                }
            }
            return moves;
        }
        else
        {
            String[] moves = getMoveStats().moves;
            if (genesMoves == null)
            {
                IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
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
                    genesMoves.getAlleles()[0] = gene;
                    genesMoves.getAlleles()[1] = gene;
                    genesMoves.refreshExpressed();
                }
            }
            return getMoveStats().moves = genesMoves.getExpressed().getValue();
        }
    }

    @Override
    public void setMove(int i, String moveName)
    {
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
            IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
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
                genesNature.getAlleles()[0] = gene;
                genesNature.getAlleles()[1] = gene;
                genesNature.refreshExpressed();
            }
        }
        return genesNature.getExpressed().getValue();
    }

    public void setMoves(String[] moves)
    {
        if (!isServerWorld()) return;
        String movesString = "";

        if (moves != null && moves.length == 4)
        {
            if (genesMoves == null)
            {
                getMoves();
            }
            genesMoves.getExpressed().setValue(getMoveStats().moves = moves);
            int i = 0;
            for (String s : moves)
            {
                if (s != null) movesString = i++ != 0 ? movesString + ("," + s) : s;
            }
        }
        dataManager.set(MOVESDW, movesString);
    }

    @Override
    public void setSize(float size)
    {
        if (genesSize == null) getSize();
        if (isAncient()) size = 2;
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
            IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
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
                genesColour.getAlleles()[0] = gene;
                genesColour.getAlleles()[1] = gene;
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
            IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
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
                genesShiny.getAlleles()[0] = gene;
                genesShiny.getAlleles()[1] = gene;
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
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        if (genesSpecies == null)
        {
            IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
            if (genes == null) throw new RuntimeException("This should not be called here");
            genesSpecies = genes.getAlleles().get(SPECIESGENE);
            if (genesSpecies == null)
            {
                genesSpecies = new Alleles();
                genes.getAlleles().put(SPECIESGENE, genesSpecies);
            }
            if (genesSpecies.getAlleles()[0] == null)
            {
                SpeciesGene gene = new SpeciesGene();
                SpeciesInfo info = gene.getValue();

                if (getClass().getName().contains("GenericPokemob"))
                {
                    String num = getClass().getSimpleName().replace("GenericPokemob", "").trim();
                    PokedexEntry entry = Database.getEntry(Integer.parseInt(num));
                    info.entry = entry;
                }
                else
                {
                    System.out.println(this.getClass() + " " + getPokedexNb());
                    Thread.dumpStack();
                    this.setDead();
                    info.entry = Database.missingno;
                }
                info.value = Tools.getSexe(info.entry.getSexeRatio(), new Random());
                genesSpecies.getAlleles()[0] = gene;
                genesSpecies.getAlleles()[1] = gene;
                genesSpecies.refreshExpressed();
            }
        }
        SpeciesInfo info = genesSpecies.getExpressed().getValue();
        return info.entry.getForGender(getSexe());
    }

    @Override
    public IPokemob setPokedexEntry(PokedexEntry newEntry)
    {
        PokedexEntry entry = getPokedexEntry();
        SpeciesInfo info = genesSpecies.getExpressed().getValue();
        if (newEntry == null || newEntry == entry) return this;
        IPokemob ret = this;
        info.entry = newEntry;
        if (newEntry.getPokedexNb() != getPokedexNb())
        {
            ret = megaEvolve(newEntry);
        }
        if (world != null) ret.setSize((float) (ret.getSize() / PokecubeMod.core.getConfig().scalefactor));
        if (world != null && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PacketChangeForme.sendPacketToNear((Entity) ret, newEntry, 128);
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
        genesSpecies = null;
        getPokedexEntry();
        genesColour = null;
        getRGBA();

        // Refresh the datamanager for moves.
        setMoves(getMoves());
        // Refresh the datamanager for evs
        setEVs(getEVs());
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void writeSpawnData(ByteBuf data)
    {
        IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
        if (genes != null)
        {
            NBTTagList list = (NBTTagList) IMobGenetics.GENETICS_CAP.writeNBT(genes, null);
            PacketBuffer buffer = new PacketBuffer(data);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("g", list);
            buffer.writeCompoundTag(nbt);
        }
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        PacketBuffer buffer = new PacketBuffer(data);
        try
        {
            NBTTagCompound tag = buffer.readCompoundTag();
            NBTTagList list = (NBTTagList) tag.getTag("g");
            IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
            IMobGenetics.GENETICS_CAP.readNBT(genes, null, list);
            onGenesChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        onGenesChanged();
    }
}
