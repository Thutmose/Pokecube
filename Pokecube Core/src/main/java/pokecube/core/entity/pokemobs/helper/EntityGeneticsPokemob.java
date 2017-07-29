package pokecube.core.entity.pokemobs.helper;

import static pokecube.core.entity.pokemobs.genetics.GeneticsManager.COLOURGENE;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.entity.pokemobs.genetics.genes.ColourGene;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;

/** This class will store the various stats of the pokemob as Alleles, and will
 * provider quick getters and setters for the genes. */
public abstract class EntityGeneticsPokemob extends EntityTameablePokemob
{
    Alleles genesColour;

    public EntityGeneticsPokemob(World world)
    {
        super(world);
    }

    @Override
    public void init(int nb)
    {
        super.init(nb);
    }

    @Override
    public Ability getAbility()
    {
        return pokemobCap.getAbility();
    }

    @Override
    public int getAbilityIndex()
    {
        return pokemobCap.getAbilityIndex();
    }

    @Override
    public void setAbility(Ability ability)
    {
        pokemobCap.setAbility(ability);
    }

    @Override
    public void setAbilityIndex(int ability)
    {
        pokemobCap.setAbilityIndex(ability);
    }

    @Override
    public float getSize()
    {
        return pokemobCap.getSize();
    }

    @Override
    public void setIVs(byte[] ivs)
    {
        pokemobCap.setIVs(ivs);
    }

    @Override
    public void setEVs(byte[] evs)
    {
        pokemobCap.setEVs(evs);
    }

    @Override
    public byte[] getEVs()
    {
        return pokemobCap.getEVs();
    }

    @Override
    public byte[] getIVs()
    {
        return pokemobCap.getIVs();
    }

    @Override
    public String[] getMoves()
    {
        return pokemobCap.getMoves();
    }

    @Override
    public void setMove(int i, String moveName)
    {
        pokemobCap.setMove(i, moveName);
    }

    @Override
    public void setNature(Nature nature)
    {
        pokemobCap.setNature(nature);
    }

    @Override
    public Nature getNature()
    {
        return pokemobCap.getNature();
    }

    @Override
    public void setMoves(String[] moves)
    {
        pokemobCap.setMoves(moves);
    }

    @Override
    public void setSize(float size)
    {
        pokemobCap.setSize(size);
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
        return pokemobCap.isShiny();
    }

    @Override
    public void setShiny(boolean shiny)
    {
        pokemobCap.setShiny(shiny);
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        return pokemobCap.getPokedexEntry();
    }

    @Override
    public IPokemob setPokedexEntry(PokedexEntry newEntry)
    {
        return pokemobCap.setPokedexEntry(newEntry);
    }

    @Override
    public byte getSexe()
    {
        return pokemobCap.getSexe();
    }

    @Override
    public void setSexe(byte sexe)
    {
        pokemobCap.setSexe(sexe);
    }

    @Override
    public void onGenesChanged()
    {
        genesColour = null;
        getRGBA();
    }

    /** Use this for anything that does not change or need to be updated. */
    @Override
    public void writeSpawnData(ByteBuf data)
    {
        pokemobCap.onGenesChanged();
        IMobGenetics genes = getCapability(IMobGenetics.GENETICS_CAP, null);
        PacketBuffer buffer = new PacketBuffer(data);
        NBTTagList list = (NBTTagList) IMobGenetics.GENETICS_CAP.writeNBT(genes, null);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("g", list);
        buffer.writeCompoundTag(nbt);
        buffer.writeCompoundTag(pokemobCap.writePokemobData());
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
            pokemobCap.readPokemobData(buffer.readCompoundTag());
            pokemobCap.onGenesChanged();
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
